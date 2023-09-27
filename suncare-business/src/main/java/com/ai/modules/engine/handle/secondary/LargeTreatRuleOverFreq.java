/**
 * RuleOverFrequency.java	  V1.0   2021年1月15日 上午10:40:56
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.secondary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.jeecg.common.util.DateUtils;

import com.ai.modules.engine.handle.fee.FeeResult;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.vo.ChargedayVO;
import com.ai.modules.engine.model.vo.ChargedetailVO;
import com.ai.modules.engine.model.vo.TreatOverFreqComputeVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 功能描述：诊疗项目一日就诊超频次二次处理
 *
 * @author  zhangly
 * Date: 2021年1月15日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Slf4j
public class LargeTreatRuleOverFreq extends RuleOverFrequency {

	public LargeTreatRuleOverFreq(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList,
			Boolean trail) {
		super(task, batch, rule, ruleConditionList, trail);
	}

	@Override
	public void execute() throws Exception {
		String batchId = batch.getBatchId();
  		List<MedicalRuleConditionSet> judgeList = ruleConditionList.stream().filter(s->"judge".equals(s.getType())).collect(Collectors.toList());
  		if(judgeList==null || judgeList.size()==0) {
  			throw new Exception("未找到限频次配置！");
  		}
  		//项目组
    	Set<String> mutexCodeSet = new HashSet<String>();
    	for(MedicalRuleConditionSet bean : judgeList) {
    		if(StringUtils.isNotBlank(bean.getExt4())) {
    			mutexCodeSet.add(bean.getExt4());
    		}
    	}
    	
  		List<String> conditionList = new ArrayList<String>();
    	conditionList.add("RULE_ID:"+rule.getRuleId());
    	conditionList.add("BATCH_ID:"+batchId);
    	conditionList.add("ITEM_QTY:{0 TO *}");
    	String collection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
    	boolean slave = false;
    	
    	List<String> visitidList = new ArrayList<String>();
    	Map<String, SolrDocument> visitMap = new HashMap<String, SolrDocument>();
    	int count = SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
    		String visitid = doc.get("VISITID").toString();
    		visitidList.add(visitid);
    		visitMap.put(visitid, doc);
        });
    	if(count>0) {
    		int pageSize = 500;
    		int pageNum = (visitidList.size() + pageSize - 1) / pageSize;
    		//数据分割
    		List<List<String>> mglist = new ArrayList<>();
    	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
    	    	mglist.add(visitidList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
    	    });
    	    
    	    conditionList.clear();
    	    conditionList.add("RULE_ID:"+rule.getRuleId());
        	conditionList.add("BATCH_ID:"+batchId);
	    	conditionList.add("PRESCRIPTTIME:?*");
    		StringBuilder sb = new StringBuilder();
    		sb.append("ITEMCODE:").append(rule.getItemCodes());
    		if(mutexCodeSet.size()>0) {
    			sb.append(" OR ");
        		sb.append("_query_:\"");
        		EngineMapping mapping = new EngineMapping("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
        		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
        		sb.append(plugin.parse());
        		sb.append("TREATGROUP_CODE:");
        		String mutexCode = StringUtils.join(mutexCodeSet, "|");
        		String values = "(" + StringUtils.replace(mutexCode, "|", " OR ") + ")";
    			sb.append(values);
        		sb.append("\"");        		
    		}
    		conditionList.add(sb.toString());
    		
    		// 数据写入文件
            String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batchId + "/" + rule.getRuleId() + ".json";
            BufferedWriter fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
            fileWriter.write("[");
            
    		Map<String, TreatOverFreqComputeVO> computeMap = new HashMap<String, TreatOverFreqComputeVO>();
    	    for(List<String> subList : mglist) {
    	    	computeMap.clear();
    	    	String visitidFq = "VISITID:(\"" + StringUtils.join(subList, "\",\"") + "\")";
    	    	SolrQuery solrQuery = new SolrQuery("*:*");
    			// 设定查询字段
    			solrQuery.addFilterQuery(conditionList.toArray(new String[0]));
    			solrQuery.addFilterQuery(visitidFq);
    			solrQuery.setStart(0);
    			solrQuery.setRows(EngineUtil.MAX_ROW);
    			solrQuery.setSort(SolrQuery.SortClause.asc("VISITID"));
    			SolrUtil.export(solrQuery, EngineUtil.MEDICAL_CHARGE_DETAIL, slave, (map, index) -> {
    				//处方日期PRESCRIPTTIME
    				Object value = map.get("PRESCRIPTTIME");
    				String prescripttime = value.toString();
    				String day = DateUtils.dateformat(prescripttime, "yyyy-MM-dd");
    				String visitid = map.get("VISITID").toString();
    				String code = map.get("ITEMCODE").toString();
    				ChargedetailVO vo = new ChargedetailVO();
    				vo.setPrescripttime(prescripttime);
    				vo.setDay(day);
    				vo.setItemcode(code);
    				vo.setItemname(map.get("ITEMNAME").toString());
    				vo.setAmount(new BigDecimal(map.get("AMOUNT").toString()));
    				vo.setFee(new BigDecimal(map.get("FEE").toString()));
    				//基金支出金额
    				if(map.get("FUND_COVER")!=null) {
    					vo.setFundConver(new BigDecimal(map.get("FUND_COVER").toString()));
    				} 
    				if(map.get("ITEMPRICE")!=null) {
    					vo.setUnitPrice(new BigDecimal(map.get("ITEMPRICE").toString()));
    				}
    				if(map.get("ITEMNAME_SRC")!=null) {
    					vo.setItemnameSrc(map.get("ITEMNAME_SRC").toString());
    				}
    				if(map.get("SELFPAY_PROP")!=null) {
    					BigDecimal ratio = new BigDecimal(map.get("SELFPAY_PROP").toString());
    					vo.setRatio(BigDecimal.ONE.subtract(ratio));
    				}
    				if(!computeMap.containsKey(visitid)) {
    	    			TreatOverFreqComputeVO computeVo = new TreatOverFreqComputeVO(visitid, rule.getItemCodes());
    	    			computeMap.put(visitid, computeVo);
    	    			computeVo.add(vo);
    	    		} else {
    	    			TreatOverFreqComputeVO computeVo = computeMap.get(visitid);
    	    			computeVo.add(vo);
    	    		}
    	        });
    			
    			for(Map.Entry<String, TreatOverFreqComputeVO> entry : computeMap.entrySet()) {
    				List<FeeResult> feeResultList = this.compulate(judgeList, entry.getValue()); //超频次违规金额    		
    	    		if(feeResultList!=null && feeResultList.size()>0) {
    	    			int num = 0;
    	    			SolrDocument doc = visitMap.get(entry.getKey());
    	    			for(FeeResult feeResult : feeResultList) {
    	    				if(num==0) {
    	    					//第一个只做更新
    	    					this.writeSplit(fileWriter, doc, feeResult, false);
    	    				} else {
    	    					this.writeSplit(fileWriter, doc, feeResult, true);
    	    				}
    	    				num++;
    	    			}
    				}
    			}
    	    }
    	    fileWriter.write("]");
        	fileWriter.flush();
      		fileWriter.close();
      		//导入solr
    	    SolrUtil.importJsonToSolr(importFilePath, collection, slave);
    	}
    	//删除未找到超出频次的结果
    	StringBuilder sb = new StringBuilder();
    	sb.setLength(0);
	    sb.append("BATCH_ID:").append(batchId);
	    sb.append(" AND -AI_OUT_CNT:{0 TO *}");
	    sb.append(" AND ITEMCODE:").append(rule.getItemCodes());
	    sb.append(" AND RULE_ID:").append(rule.getRuleId());
    	SolrUtil.delete(collection, sb.toString(), slave);
	}
	
	/**
	 * 
	 * 功能描述：计算违规金额
	 * 【项目一日数量超出临床合理性】的规则有以下一种情况：主体项目A超数量规则有以下两种情况，当存在项目B时，项目A一日超出4次即为违规；当不存在项目B时，项目A一日超出9次即为违规。
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年9月8日 下午2:38:33</p>
	 *
	 * @param judgeList
	 * @param computeVO
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private List<FeeResult> compulate(List<MedicalRuleConditionSet> judgeList, TreatOverFreqComputeVO computeVO) {
		List<FeeResult> result = new ArrayList<FeeResult>();
		BigDecimal limit1 = null; //不存在项目的限制次数
		BigDecimal limit2 = null; //存在项目的限制次数
		String compare1 = "<";
		String compare2 = "<";
		for(MedicalRuleConditionSet bean : judgeList) {
			if(StringUtils.isBlank(bean.getExt4()) || "≠".equals(bean.getExt3())) {
				limit1 = new BigDecimal(bean.getExt2());
				compare1 = bean.getCompare();
			}
			if("=".equals(bean.getExt3()) && StringUtils.isNotBlank(bean.getExt4())) {
				limit2 = new BigDecimal(bean.getExt2());
				compare2 = bean.getCompare();
			}
		}
		List<ChargedayVO> detailList = computeVO.offsetDetail();
		Set<String> mutexDaySet = computeVO.existsMutexDay();
		for(ChargedayVO vo : detailList) {
			BigDecimal frequency = null;
			if(mutexDaySet.contains(vo.getDay())) {
				//同一天存在项目
				frequency = limit2;
				if(frequency!=null && "<".equals(compare2)) {
					frequency = frequency.subtract(BigDecimal.ONE);
				}				
			} else {
				//同一天不存在项目
				frequency = limit1;
				if(frequency!=null && "<".equals(compare1)) {
					frequency = frequency.subtract(BigDecimal.ONE);
				}
			}
			if(frequency!=null && vo.getAmount().compareTo(frequency)>0) {
				//超出限制次数
				FeeResult feeResult = new FeeResult();
				BigDecimal fee = BigDecimal.ZERO;
				fee = vo.getFee().divide(vo.getAmount(), 4, BigDecimal.ROUND_HALF_UP);
				fee = fee.setScale(2, BigDecimal.ROUND_HALF_DOWN);
				fee = fee.multiply(vo.getAmount().subtract(frequency));
				BigDecimal actionFee = fee.multiply(vo.getRatio());
				actionFee = actionFee.setScale(2, BigDecimal.ROUND_HALF_DOWN);
				feeResult.setMoney(fee);
				feeResult.setActionMoney(actionFee);
				feeResult.setCnt(vo.getAmount());
				feeResult.setOutCnt(vo.getAmount().subtract(frequency));
				feeResult.setDuration(vo.getDay());
				result.add(feeResult);
			}
		}
		return result;
	}
}
