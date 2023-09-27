/**
 * SolrIndicationRuleHandle.java	  V1.0   2021年3月22日 下午3:12:07
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.solr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;

import com.ai.modules.api.util.ApiOauthClientUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.handle.fee.FeeResult;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.handle.rule.parse.RuleIgnoreNullParser;
import com.ai.modules.engine.handle.secondary.AbsRuleSecondHandle;
import com.ai.modules.engine.handle.secondary.RuleOverFrequencyFromDetail;
import com.ai.modules.engine.handle.sync.SyncUtil;
import com.ai.modules.engine.model.rule.EngineRuleMasterInfo;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：合理诊疗一日就诊限频次规则
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class SolrTreatOverFreqRuleHandle extends SolrRuleHandle {
	
	public SolrTreatOverFreqRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
			List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, rule, ruleConditionList);
	}
	
	@Override
	public void generateUnreasonableAction() throws Exception {
		if(ignoreRun()) {
			//忽略运行
			return;
		}
		
		//是否需要先筛选出满足规则的数据
  		boolean sync = HiveJDBCUtil.isHive() && !HiveJDBCUtil.isSmall();
		if(sync) {
			//数仓hive模式
			SyncUtil.syncTreatFreq(task.getDataSource());
		}
		
		int total = 0;
		String itemcode = rule.getItemCodes();
		if(itemcode.indexOf(",")>-1) {
			String[] array = StringUtils.split(itemcode, ",");
			itemcode = "(" + StringUtils.join(array, " OR ") + ")";
		}
		List<String> conditionList = new ArrayList<String>();
		List<MedicalRuleConditionSet> judgeList = ruleConditionList.stream().filter(s->"judge".equals(s.getType())).collect(Collectors.toList());
		//按判断条件每个组跑规则
		for(MedicalRuleConditionSet ruleCondition : judgeList) {
			conditionList.clear();
			conditionList.add("ITEMCODE:" + itemcode);
	        conditionList.add("ITEM_QTY:{0 TO *}");
			conditionList.add("ITEM_AMT:{0 TO *}");
			conditionList.add("PRESCRIPTTIME_DAY:?*");
			conditionList.addAll(this.parseCommonCondition());
			//追加就诊类型=住院过滤条件
		    EngineRuleMasterInfo paramRule = new EngineRuleMasterInfo();
		    paramRule.setJzlx("ZY01");
		    conditionList.add(paramRule.where());
			//添加准入条件
	    	String condition = this.parseAccessCondition();
	    	if(StringUtils.isNotBlank(condition)) {
	    		conditionList.add(condition);
	    	}
			//添加限定条件
			conditionList.add(this.parseCondition(ruleCondition));
	        //添加过滤掉指标为空值的条件
			RuleIgnoreNullParser ignoreNullParser = new RuleIgnoreNullParser(rule, ruleConditionList);
	  		conditionList.addAll(ignoreNullParser.ignoreNullWhere());
	  		
	  		String drugCollection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
			String desc = null;
	  		if(StringUtils.isNotBlank(rule.getActionType())) {        	
				desc = ApiOauthClientUtil.parseText("ACTION_TYPE", rule.getActionType());
	        }
	  		BigDecimal frequency = this.getFrequency(ruleCondition);
	  		String actionTypeName = desc; //不合规行为类型名称
	  		//不合规行为字典映射  		
	        Map<String, MedicalActionDict> actionDictMap = dictSV.queryActionDict();
	        // 数据写入文件
	        String importFilePath = SolrUtil.importFolder + "/" + drugCollection + "/" + batch.getBatchId() + "/" + rule.getRuleId() + ".json";
	        BufferedWriter fileWriter = new BufferedWriter(
	                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
	        //写文件头
	        fileWriter.write("[");
	        String collection = sync ? "MEDICAL_PATIENT_1VISIT_1DAY_ITEMSUM_TREAT_FREQ" : "DWS_PATIENT_1VISIT_1DAY_ITEMSUM";
	        int count = SolrUtil.exportDocByPager(conditionList, collection, (doc, index) -> {
	            // 循环每条数据写入文件
	        	writerJson(fileWriter, doc, actionTypeName, actionDictMap, frequency);
	        });
	        // 文件尾
	        fileWriter.write("]");
	        fileWriter.flush();
	        fileWriter.close();
	        //导入solr
	        SolrUtil.importJsonToSolr(importFilePath, drugCollection);
	        total = total + count;
	        
	        if(count>0) {
	        	//规则二次处理
	        	List<MedicalRuleConditionSet> subList = new ArrayList<MedicalRuleConditionSet>();
	        	subList.add(ruleCondition);
	        	AbsRuleSecondHandle handle = new RuleOverFrequencyFromDetail(task, batch, rule, subList, trail);        	
	        	handle.execute();
	        }
		}		                
  		if(total>0) {
        	String busiType = this.getBusiType();
            //同步数据
            syncUnreasonableAction(rule, busiType, false);
            //不合规行为汇总
            String[] fqs = new String[] {"RULE_ID:"+rule.getRuleId()};
            engineActionService.executeGroupBy(batch.getBatchId(), rule.getActionId(), fqs);
        }        
	}
	
	/**
	 * 
	 * 功能描述：解析判断条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月24日 下午3:13:34</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	@Override
	protected String parseJudgeCondition() {		
		//限定条件
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
    	if(judgeList!=null) {
    		List<String> wheres = new ArrayList<String>();
    		for(MedicalRuleConditionSet bean : judgeList) {
    			String where = this.parseCondition(bean);
    			if(StringUtils.isNotBlank(where)) {
    				wheres.add(where);
    			}
    		}
    		String condition = StringUtils.join(wheres, " OR ");
    		return condition;
    	}    	
    	return null;
	}
	
	private String parseCondition(MedicalRuleConditionSet bean) {		
		String condiType = bean.getField();
		String frequency = bean.getExt2();
		if(StringUtils.isBlank(frequency)) {
			return null;
		}
		if(AbsRuleParser.RULE_CONDI_FREQUENCY.equals(condiType)) {
			String compare = bean.getCompare();
			StringBuilder sb = new StringBuilder();
			//黑名单限频次取反
			sb.append("(*:* -");
			sb.append("ITEM_QTY:");
			if(">".equals(compare)) {
				sb.append("{").append(frequency).append(" TO *}");
			} else if(">=".equals(compare)) {
				sb.append("[").append(frequency).append(" TO *}");
			} else if("<".equals(compare)) {
				sb.append("{* TO ").append(frequency).append("}");
			} else if("<=".equals(compare)) {
				sb.append("{* TO ").append(frequency).append("]");
			} else {
				sb.append(frequency);
			}
			sb.append(")");
			if(StringUtils.isNotBlank(bean.getExt4())) {
				//项目组前提条件
				sb.append(" AND ");
				if("≠".equals(bean.getExt3())) {
					sb.append("(*:* -");
				}
				sb.append("_query_:\"");
				//是否需要先筛选出满足规则的数据
		  		boolean sync = HiveJDBCUtil.isHive() && !HiveJDBCUtil.isSmall();
				String fromIndex = sync ? "MEDICAL_PATIENT_1VISIT_1DAY_ITEMSUM_TREAT_FREQ" : "DWS_PATIENT_1VISIT_1DAY_ITEMSUM";
				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(fromIndex, "TEMPKEY", "TEMPKEY");
				sb.append(plugin.parse());
				plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
				sb.append("_query_:\\\"");
				sb.append(plugin.parse());
				String value = "(" + StringUtils.replace(bean.getExt4(), "|", " OR ") + ")";
				sb.append("TREATGROUP_CODE:").append(value);
				sb.append("\\\" AND ITEM_QTY:{0 TO *} AND ITEM_AMT:{0 TO *}");			
				sb.append("\"");
				if("≠".equals(bean.getExt3())) {
					sb.append(")");
				}
			}
			return sb.toString();
		}
        return null;
	}
	
	protected JSONObject writerJson(BufferedWriter fileWriter, SolrDocument document, 
			String actionTypeName, Map<String, MedicalActionDict> actionDictMap, BigDecimal frequency) {		
		JSONObject json = this.parseJSONObject(document, actionTypeName, actionDictMap);
		//计算超出部分
		FeeResult feeResult = this.over(document, frequency);
		if(feeResult!=null) {
			String content = "超频次/数量发生时间：%s";
	    	content = String.format(content, feeResult.getDuration());
			json.put("ARRAY_ACTION_MONEY", feeResult.getActionMoney());
			json.put("ARRAY_MONEY", feeResult.getMoney());
			json.put("MIN_MONEY", feeResult.getMoney());
			json.put("MAX_MONEY", feeResult.getMoney());
			json.put("MIN_ACTION_MONEY", feeResult.getFundMoney());
			json.put("MAX_ACTION_MONEY", feeResult.getFundMoney());
			json.put("AI_ITEM_CNT", feeResult.getCnt());
			json.put("AI_OUT_CNT", feeResult.getOutCnt());
			json.put("BREAK_RULE_TIME", feeResult.getDuration());
			json.put("BREAK_RULE_CONTENT", content);
		}
        try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (Exception e) {

        }
        return json;
    }
	
	private BigDecimal getFrequency(MedicalRuleConditionSet ruleCondition) {
		//限制频次
		BigDecimal frequency = new BigDecimal(ruleCondition.getExt2());
		String compare = ruleCondition.getCompare();
		if("<".equals(compare)) {
			frequency = frequency.subtract(BigDecimal.ONE);
		}
		return frequency;
	}
		
	/**
	 * 
	 * 功能描述：计算超出部分的违规金额、数量
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年12月17日 下午4:47:13</p>
	 *
	 * @param doc
	 * @param frequency
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private FeeResult over(SolrDocument doc, BigDecimal frequency) {
		FeeResult feeResult = new FeeResult();
		//使用平均值方式计算违规金额
		BigDecimal fee = new BigDecimal(doc.get("ITEM_AMT").toString());
		BigDecimal amount = new BigDecimal(doc.get("ITEM_QTY").toString());
		fee = fee.divide(amount,4, BigDecimal.ROUND_HALF_UP);
		fee = fee.setScale(2, BigDecimal.ROUND_HALF_DOWN);
		fee = fee.multiply(amount.subtract(frequency));
		BigDecimal ratio = new BigDecimal(doc.get("SELFPAY_PROP_MIN").toString());
		ratio = BigDecimal.ONE.subtract(ratio);
		BigDecimal actionFee = fee.multiply(ratio);
		actionFee = actionFee.setScale(2, BigDecimal.ROUND_HALF_DOWN);
		feeResult.setMoney(fee);
		feeResult.setActionMoney(actionFee);
		feeResult.setCnt(amount);
		feeResult.setOutCnt(amount.subtract(frequency));
		feeResult.setDuration(doc.get("PRESCRIPTTIME_DAY").toString());
		//使用平均值方式计算违规基金支出金额
		BigDecimal fundcover = new BigDecimal(doc.get("FUND_COVER").toString());
        fundcover = fundcover.divide(amount,4, BigDecimal.ROUND_HALF_UP);
        fundcover = fundcover.setScale(2, BigDecimal.ROUND_HALF_DOWN);
        fundcover = fundcover.multiply(amount.subtract(frequency));
        feeResult.setFundMoney(fundcover);
		return feeResult;
	}
}
