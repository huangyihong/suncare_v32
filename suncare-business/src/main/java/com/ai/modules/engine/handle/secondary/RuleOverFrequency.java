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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.jeecg.common.util.UUIDGenerator;

import com.ai.modules.engine.handle.fee.FeeResult;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.vo.ChargebxVO;
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
public class RuleOverFrequency extends AbsRuleSecondHandle {

	public RuleOverFrequency(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList,
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
  		String collection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
    	boolean slave = false;
  		  		
  		String ruleLimit = rule.getRuleLimit();
		Set<String> cycleSet = new HashSet<String>();
  		//cycleSet.add("freq2");
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
  		MedicalRuleConditionSet ruleCondition = judgeList.get(0);
  		
  		List<String> conditionList = new ArrayList<String>();
    	conditionList.add("RULE_ID:"+rule.getRuleId());
    	conditionList.add("BATCH_ID:"+batchId);
    	conditionList.add("ITEM_QTY:{0 TO *}");    	
    	
    	// 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batchId + "/" + rule.getRuleId() + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        fileWriter.write("[");
        
    	SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {    		
    		List<FeeResult> feeResultList = this.compulate(ruleCondition, doc); //超频次违规金额    		
    		if(feeResultList!=null && feeResultList.size()>0) {
    			int num = 0;
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
        });
    	fileWriter.write("]");
    	fileWriter.flush();
  		fileWriter.close();
  		//导入solr
	    SolrUtil.importJsonToSolr(importFilePath, collection, slave);
	}
	
	private List<FeeResult> compulate(MedicalRuleConditionSet ruleCondition, SolrDocument document) {
		List<FeeResult> result = new ArrayList<FeeResult>();
		String period = ruleCondition.getExt1();
		//限制频次
		BigDecimal limit = new BigDecimal(ruleCondition.getExt2());
		String compare = ruleCondition.getCompare();
		if("<".equals(compare)) {
			limit = limit.subtract(BigDecimal.ONE);
		}
		BigDecimal frequency = limit;		
		if("1time".equals(period) || "avgday".equals(period)) {
			FeeResult feeResult = new FeeResult();
			BigDecimal money = BigDecimal.ZERO;
			//报销比例
			BigDecimal ratio = BigDecimal.ZERO;
			if(document.get("SELFPAY_PROP_MIN")!=null) {
				ratio = new BigDecimal(document.get("SELFPAY_PROP_MIN").toString());
				ratio = BigDecimal.ONE.subtract(ratio);
			}			
			//项目使用的次数
			BigDecimal qty = new BigDecimal(document.get("ITEM_QTY").toString());
			feeResult.setCnt(qty);			
			//项目总费用
			BigDecimal amt = new BigDecimal(document.get("ITEM_AMT").toString());
			//项目单价
			BigDecimal avg = BigDecimal.ZERO;
			if(document.get("ITEMPRICE_MAX")!=null) {
				avg = new BigDecimal(document.get("ITEMPRICE_MAX").toString());
			}
			
			if("1time".equals(period)) {
				//一次就诊，违规金额=(一次就诊项目实际总数量-限制频次)*单价*报销比例
				money = avg.multiply(qty.subtract(frequency));
				feeResult.setMoney(money);
				money = money.multiply(ratio);
				feeResult.setOutCnt(qty.subtract(frequency));
			} else {
				//日均次，违规金额=(一次就诊ID项目实际总数量-限制频次*住院天数)*单价*报销比例
				BigDecimal day = BigDecimal.ONE;
				if(document.get("ZY_DAYS_CALCULATE")!=null) {
					day = new BigDecimal(document.get("ZY_DAYS_CALCULATE").toString());
				}
				//超出使用数量=一次就诊ID项目实际总数量-限制频次*住院天数
				BigDecimal cnt = frequency.multiply(day);
				cnt = qty.subtract(cnt);
				money = avg.multiply(cnt);
				feeResult.setMoney(money);
				money = money.multiply(ratio);
				if(money.compareTo(BigDecimal.ZERO)<0) {
					money = BigDecimal.ZERO;
				}
				feeResult.setOutCnt(cnt);
			}
			money = money.setScale(2, BigDecimal.ROUND_HALF_DOWN);
			feeResult.setActionMoney(money);
			result.add(feeResult);
		} else {			
			List<String> conditionList = new ArrayList<String>();
			String visitid = document.get("VISITID").toString();
			if("1day".equals(period)) {
				//日
				EngineMapping mapping = new EngineMapping("MAPPER_DWS_PATIENT_CHARGEITEM_SUM_D", "DWSID", "id");
				SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
				conditionList.add(plugin.parse()+"VISITID:"+visitid);
				conditionList.add("DURATIONTYPE:日");
			} else if("1month".equals(period)) {
				//月
				EngineMapping mapping = new EngineMapping("MAPPER_DWS_PATIENT_CHARGEITEM_SUM_M", "DWSID", "id");
				SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
				conditionList.add(plugin.parse()+"VISITID:"+visitid);
				conditionList.add("DURATIONTYPE:月");
			} else if("7day".equals(period)) {
				//周
				EngineMapping mapping = new EngineMapping("MAPPER_DWS_PATIENT_CHARGEITEM_SUM_W", "DWSID", "id");
				SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
				conditionList.add(plugin.parse()+"VISITID:"+visitid);
				conditionList.add("DURATIONTYPE:周");
			} else if("3month".equals(period)) {
				//季
				EngineMapping mapping = new EngineMapping("MAPPER_DWS_PATIENT_CHARGEITEM_SUM_Q", "DWSID", "id");
				SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
				conditionList.add(plugin.parse()+"VISITID:"+visitid);
				conditionList.add("DURATIONTYPE:季");
			} else if("1year".equals(period)) {
				//年
				EngineMapping mapping = new EngineMapping("MAPPER_DWS_PATIENT_CHARGEITEM_SUM_Y", "DWSID", "id");
				SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
				conditionList.add(plugin.parse()+"VISITID:"+visitid);
				conditionList.add("DURATIONTYPE:年");
			} else {
				conditionList.add("DURATIONTYPE:"+period);
			}
			conditionList.add("ITEMCODE:"+document.get("ITEMCODE").toString());			
			conditionList.add("DURATION:?*");
			conditionList.add("ITEM_QTY:{0 TO *}");
			SolrQuery solrQuery = new SolrQuery("*:*");
			// 设定查询字段
			solrQuery.addFilterQuery(conditionList.toArray(new String[0]));
			solrQuery.setStart(0);
			solrQuery.setRows(EngineUtil.MAX_ROW);    		
			solrQuery.addField("VISITID");
			solrQuery.addField("ITEMCODE");
			solrQuery.addField("ITEMNAME");
			solrQuery.addField("DURATION");
			solrQuery.addField("ITEM_QTY");
			solrQuery.addField("ITEM_TOTAL_AMT");
			solrQuery.addField("ITEMPRICE_MAX");
			solrQuery.addField("SELFPAY_PROP_MIN");
			List<ChargebxVO> list = new ArrayList<ChargebxVO>();
			try {
				SolrUtil.exportDoc(solrQuery, "DWS_PATIENT_CHARGEITEM_SUM", (map, index) -> {
					//当天项目使用的次数
					BigDecimal itemQty = new BigDecimal(map.get("ITEM_QTY").toString());
					if(itemQty.compareTo(frequency)>0) {
						Object value = map.get("DURATION");
	    				String day = value.toString();
	    				String code = map.get("ITEMCODE").toString();
	    				ChargebxVO vo = new ChargebxVO();
	    				vo.setDay(day);
	    				vo.setItemcode(code);
	    				vo.setItemname(map.get("ITEMNAME").toString());
	    				vo.setAmount(itemQty);
	    				vo.setFee(new BigDecimal(map.get("ITEM_TOTAL_AMT").toString()));
	    				if(map.get("ITEMPRICE_MAX")!=null) {
	    					vo.setUnitPrice(new BigDecimal(map.get("ITEMPRICE_MAX").toString()));
	    				}
	    				if(map.get("SELFPAY_PROP_MIN")!=null) {
	    					BigDecimal ratio = new BigDecimal(map.get("SELFPAY_PROP_MIN").toString());
	    					vo.setRatio(BigDecimal.ONE.subtract(ratio));
	    				}
	    				list.add(vo);										
					}				
		        });
			} catch(Exception e) {
				log.error("", e);
			}
			//超频次，计算违规基金金额=超出数量*单价*报销比例；违规金额=超出数量*单价			
			int index = 0;
			for(ChargebxVO vo : list) {
				FeeResult feeResult = new FeeResult();
				BigDecimal fee = BigDecimal.ZERO;
				fee = vo.getFee().divide(vo.getAmount(),4, BigDecimal.ROUND_HALF_UP);
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
				index++;
			}
		}
		return result;
	}
	
	/**
	 * 
	 * 功能描述：周期限频次规则拆分结果写入文件
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
	protected void writeSplit(BufferedWriter fileWriter, SolrDocument doc, FeeResult feeResult, boolean create) {
    	String content = "超频次/数量发生时间：%s";
    	content = String.format(content, feeResult.getDuration());
    	JSONObject json = new JSONObject();
    	if(create) {
    		//新增
    		for(Entry<String, Object> entry : doc.entrySet()) {
    			if(!"_version_".equals(entry.getKey())) {
    				json.put(entry.getKey(), entry.getValue());
    			}
    		}
    		json.put("id", UUIDGenerator.generate());
    		json.put("ARRAY_ACTION_MONEY", feeResult.getActionMoney());
			json.put("ARRAY_MONEY", feeResult.getMoney());
			json.put("MIN_MONEY", feeResult.getMoney());
			json.put("MAX_MONEY", feeResult.getMoney());
			json.put("AI_ITEM_CNT", feeResult.getCnt());
			json.put("AI_OUT_CNT", feeResult.getOutCnt());
			if(StringUtils.isNotBlank(feeResult.getDuration())) {
				json.put("BREAK_RULE_TIME", feeResult.getDuration());
				json.put("BREAK_RULE_CONTENT", content);
			}
    	} else {    	
    		//更新
			json.put("id", doc.get("id").toString());						
			JSONObject up = new JSONObject();
			up.put("add", feeResult.getActionMoney());
			json.put("ARRAY_ACTION_MONEY", up);
			up = new JSONObject();
			up.put("add", feeResult.getMoney());
			json.put("ARRAY_MONEY", up);
			up = new JSONObject();
			up.put("set", feeResult.getMoney());
			json.put("MIN_MONEY", up);
			up = new JSONObject();
			up.put("set", feeResult.getMoney());
			json.put("MAX_MONEY", up);
			up = new JSONObject();
			up.put("set", feeResult.getCnt());
			json.put("AI_ITEM_CNT", up);
			up = new JSONObject();
			up.put("set", feeResult.getOutCnt());
			json.put("AI_OUT_CNT", up);
			if(StringUtils.isNotBlank(feeResult.getDuration())) {
				up = new JSONObject();
				up.put("set", feeResult.getDuration());
				json.put("BREAK_RULE_TIME", up);
				up = new JSONObject();
				up.put("set", content);
				json.put("BREAK_RULE_CONTENT", up);
			}
    	}
    	try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (IOException e) {
        }
    }
}
