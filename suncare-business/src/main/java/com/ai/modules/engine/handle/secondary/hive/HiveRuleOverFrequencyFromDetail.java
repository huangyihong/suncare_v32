/**
 * RuleOverFrequency.java	  V1.0   2021年1月15日 上午10:40:56
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.secondary.hive;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
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

import com.ai.modules.engine.handle.secondary.AbsRuleSecondHandle;
import com.ai.modules.engine.model.vo.ChargedetailVO;
import com.ai.modules.engine.model.vo.OverFreqComputeVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 功能描述：超频次二次处理
 *
 * @author  zhangly
 * Date: 2021年1月15日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Slf4j
public class HiveRuleOverFrequencyFromDetail extends AbsRuleSecondHandle {

	public HiveRuleOverFrequencyFromDetail(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList,
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
  		String collection = EngineUtil.MEDICAL_TRAIL_ACTION;
    	boolean slave = false;
  		  		
  		String ruleLimit = rule.getRuleLimit();
		Set<String> cycleSet = new HashSet<String>();
  		cycleSet.add("freq4");
  		cycleSet.add("freq5");
  		cycleSet.add("freq6");
  		cycleSet.add("freq7");
  		if(cycleSet.contains(ruleLimit)) {
  			//排除周、月、季、年周期限频次
  			String query = "RULE_ID:%s AND BATCH_ID:%s";
  			query = String.format(query, rule.getRuleId(), batchId);
  			SolrUtil.delete(collection, query, slave);
  			return;
  		}  		  		
  		BigDecimal limit = getFrequency(); //频次
  		
  		List<String> conditionList = new ArrayList<String>();
    	conditionList.add("RULE_ID:"+rule.getRuleId());
    	conditionList.add("BATCH_ID:"+batchId);	
		StringBuilder sb = new StringBuilder();
		SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWS_PATIENT_1VISIT_1DAY_ITEMSUM", "VISITID", "VISITID");
		sb.append("_query_:\"");
		sb.append(plugin.parse());
		sb.append("ITEMCODE:").append(rule.getItemCodes()).append(" AND ITEM_QTY:{* TO 0}\"");
        conditionList.add(sb.toString());
		
    	List<String> visitidList = new ArrayList<String>();
    	Map<String, OverFreqComputeVO> computeMap = new HashMap<String, OverFreqComputeVO>();  	    	
    	int count = SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
    		String visitid = doc.get("VISITID").toString();
    		visitidList.add(visitid);
    		if(!computeMap.containsKey(visitid)) {
    			OverFreqComputeVO vo = new OverFreqComputeVO(limit, visitid, false);
    			vo.addSolrDocument(doc);
    			computeMap.put(visitid, vo);
    		} else {
    			computeMap.get(visitid).addSolrDocument(doc);
    		}
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
    		conditionList.add("PRESCRIPTTIME:?*");
    		conditionList.add("ITEMCODE:"+rule.getItemCodes());
    		    		
    	    for(List<String> subList : mglist) {
    	    	String visitidFq = "VISITID:(\"" + StringUtils.join(subList, "\",\"") + "\")";
    	    	SolrQuery solrQuery = new SolrQuery("*:*");
    			// 设定查询字段
    			solrQuery.addFilterQuery(conditionList.toArray(new String[0]));
    			solrQuery.addFilterQuery(visitidFq);
    			solrQuery.setStart(0);
    			solrQuery.setRows(EngineUtil.MAX_ROW);    		
    			solrQuery.addField("id");
    			solrQuery.addField("VISITID");
    			solrQuery.addField("ITEMCODE");
    			solrQuery.addField("ITEMNAME");
    			solrQuery.addField("ITEMNAME_SRC");
    			solrQuery.addField("CLIENTID");
    			solrQuery.addField("CHARGEDATE");
    			solrQuery.addField("PRESCRIPTTIME");
    			solrQuery.addField("AMOUNT");
    			solrQuery.addField("FEE");
    			solrQuery.addField("FUND_COVER");
    			solrQuery.addField("ITEMPRICE");
    			solrQuery.addField("SELFPAY_PROP");
    			solrQuery.setSort(SolrQuery.SortClause.asc("VISITID"));
    			SolrUtil.export(solrQuery, EngineUtil.DWB_CHARGE_DETAIL, slave, (map, index) -> {
    				//处方日期PRESCRIPTTIME
    				Object value = map.get("PRESCRIPTTIME");
    				String prescripttime = value.toString();
    				String day = DateUtils.dateformat(prescripttime, "yyyy-MM-dd");
    				String visitId = map.get("VISITID").toString();
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
    				OverFreqComputeVO computeVO = computeMap.get(visitId);
    				computeVO.add(vo);
    	        });
    	    }
    	    // 数据写入文件
            String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batchId + "/" + rule.getRuleId() + ".json";
            BufferedWriter fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
            fileWriter.write("[");
            //非一日互斥
	    	List<String> notOverList = new ArrayList<String>();
            for(Map.Entry<String, OverFreqComputeVO> entry : computeMap.entrySet()) {
            	OverFreqComputeVO computeVO = entry.getValue();
            	List<SolrDocument> documentList = computeVO.compute();
            	for(SolrDocument doc : documentList) {
    	    		BigDecimal amount = new BigDecimal(doc.get("ITEM_AMT").toString());
    	    		if(amount.compareTo(BigDecimal.ZERO)>0) {
    	    			this.writeOver(fileWriter, doc);
    	    		} else {
    	    			notOverList.add(doc.get("id").toString());
    	    		}
    	    		if(notOverList.size()==pageSize) {
    	    			String query = "RULE_ID:%s AND BATCH_ID:%s AND id:(%s)";
    	    			query = String.format(query, rule.getRuleId(), batch.getBatchId(), StringUtils.join(notOverList, " OR "));
    	    			SolrUtil.delete(collection, query, slave);
    	    			notOverList.clear();
    	    		}
    	    	}
            }
            if(notOverList.size()>0) {
            	String query = "RULE_ID:%s AND BATCH_ID:%s AND id:(%s)";
    			query = String.format(query, rule.getRuleId(), batch.getBatchId(), StringUtils.join(notOverList, " OR "));
    			SolrUtil.delete(collection, query, slave);
    			notOverList.clear();
            }
        	fileWriter.write("]");
        	fileWriter.flush();
      		fileWriter.close();
      		//导入solr
    	    SolrUtil.importJsonToSolr(importFilePath, collection, slave);
    	}    	    	
	}
		
	/**
	 * 
	 * 功能描述：冲销后超频次结果写入文件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年9月7日 上午10:23:05</p>
	 *
	 * @param fileWriter
	 * @param doc
	 * @param feeResult
	 * @param create
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected void writeOver(BufferedWriter fileWriter, SolrDocument doc) {
    	JSONObject json = new JSONObject();
    	//更新
		json.put("id", doc.get("id").toString());						
		JSONObject up = new JSONObject();
		up.put("set", doc.get("MIN_MONEY"));
		json.put("MIN_MONEY", up);
		up = new JSONObject();
		up.put("set", doc.get("MAX_MONEY"));
		json.put("MAX_MONEY", up);
		up = new JSONObject();
		up.put("set", doc.get("ACTION_MONEY"));
		json.put("ACTION_MONEY", up);
		up = new JSONObject();
		up.put("set", doc.get("MAX_ACTION_MONEY"));
		json.put("MAX_ACTION_MONEY", up);
		up = new JSONObject();
		up.put("set", doc.get("AI_ITEM_CNT"));
		json.put("AI_ITEM_CNT", up);
		up = new JSONObject();
		up.put("set", doc.get("AI_OUT_CNT"));
		json.put("AI_OUT_CNT", up);
    	try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (IOException e) {
        }
    }
	
	private BigDecimal getFrequency() {
		//限制频次
		List<MedicalRuleConditionSet> judgeList = ruleConditionList.stream().filter(s->"judge".equals(s.getType())).collect(Collectors.toList());
		MedicalRuleConditionSet ruleCondition = judgeList.get(0);
		BigDecimal frequency = new BigDecimal(ruleCondition.getExt2());
		String compare = ruleCondition.getCompare();
		if("<".equals(compare)) {
			frequency = frequency.subtract(BigDecimal.ONE);
		}
		return frequency;
	}
}
