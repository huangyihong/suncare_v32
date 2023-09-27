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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;

import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.handle.rule.RuleHandleFactory;
import com.ai.modules.engine.handle.rule.parse.RuleIgnoreNullParser;
import com.ai.modules.engine.handle.secondary.AbsRuleSecondHandle;
import com.ai.modules.engine.model.rule.EngineParamSelfJoinRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：一次就诊互斥规则（互斥项目组中的项目作为违规项目，规则主体项目作为冲突项目）
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class SolrMutexRuleHandle extends SolrRuleHandle {
	
	public SolrMutexRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
			List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, rule, ruleConditionList);
	}
	
	@Override
	public void generateUnreasonableAction() throws Exception {
		if(ignoreRun()) {
			//忽略运行
			return;
		}
		String groupcode = parseGroupcode();
		if(groupcode.indexOf("|")>-1) {
			String[] array = StringUtils.split(groupcode, "|");
			groupcode = "(" + StringUtils.join(array, " OR ") + ")";
		}
		List<String> conditionList = new ArrayList<String>();
		SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
        conditionList.add(plugin.parse() + "TREATGROUP_CODE:" + groupcode);
        if(!this.isProjectGrp()) {
        	conditionList.add("-ITEMCODE:"+rule.getItemCodes());//排查与自身互斥
        }
        conditionList.add("ITEM_QTY:{0 TO *}");
		conditionList.add("ITEM_AMT:{0 TO *}");
		conditionList.addAll(this.parseCommonCondition());
		//添加准入与限定条件
		conditionList.addAll(this.parseCondition());
        //添加过滤掉指标为空值的条件
		RuleIgnoreNullParser ignoreNullParser = new RuleIgnoreNullParser(rule, ruleConditionList);
  		conditionList.addAll(ignoreNullParser.ignoreNullWhere());
  		
  		int count = this.handle(conditionList, EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM);
        if(count>0) {
        	RuleHandleFactory factory = new RuleHandleFactory(task, batch, trail, rule, ruleConditionList);
        	List<AbsRuleSecondHandle> handleList = factory.buildRuleSecondHandle();
        	if(handleList!=null && handleList.size()>0) {
        		//规则二次处理
        		 for(AbsRuleSecondHandle handle : handleList) {
        			 handle.execute();
        		 }
        	}
        	String busiType = this.getBusiType();
            //同步数据
            syncUnreasonableAction(rule, busiType, false);
            //不合规行为汇总
            String[] fqs = new String[] {"RULE_ID:"+rule.getRuleId()};
            engineActionService.executeGroupBy(batch.getBatchId(), rule.getActionId(), fqs);
        }        
	}
	
	@Override
	protected List<String> parseCondition() {
		List<String> result = new ArrayList<String>();
	    if(ruleConditionList!=null) {
	    	//准入条件
	    	String condition = this.parseAccessCondition();
	    	if(StringUtils.isNotBlank(condition)) {
	    		result.add(condition);
	    	}
	    	//互斥条件
	    	condition = this.parseMutexCondition();
	    	result.add(condition);
	    }	            
	    return result;
	}
	
	private String parseGroupcode() {		
		Set<String> groupcodeSet = new HashSet<String>();
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
    	if(judgeList==null || judgeList.size()==0) {
    		throw new RuntimeException(rule.getItemNames()+"规则未配置判断条件！");
    	}
    	for(MedicalRuleConditionSet bean : judgeList) {
			groupcodeSet.add(bean.getExt1());
		}    
    	String result = StringUtils.join(groupcodeSet, "|");
    	return result;
	}
	
	private String parseMutexCondition() {
		if(this.isProjectGrp()) {
			//主体是项目组
			StringBuilder sb = new StringBuilder();
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWS_PATIENT_1VISIT_ITEMSUM", "VISITID", "VISITID");
			sb.append("_query_:\"");
			sb.append(plugin.parse());
			plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
	        sb.append("_query_:\\\""+plugin.parse() + "TREATGROUP_CODE:" + rule.getItemCodes()+"\\\"");
			sb.append(" AND FUND_COVER:{0 TO *} AND SELFPAY_PROP_MIN:[0 TO 1}");
			sb.append("\"");
			return sb.toString();
		} else {
			EngineParamSelfJoinRule paramRule = new EngineParamSelfJoinRule("ITEMCODE", rule.getItemCodes());
			paramRule.addCondition("FUND_COVER:{0 TO *}");
			paramRule.addCondition("SELFPAY_PROP_MIN:[0 TO 1}");
	        return paramRule.where();
		}
	}
	
	@Override
	protected JSONObject writerJson(BufferedWriter fileWriter, SolrDocument document, String actionTypeName, Map<String, MedicalActionDict> actionDictMap) {		
		JSONObject json = this.parseJSONObject(document, actionTypeName, actionDictMap);
		String itemcode = document.get("ITEMCODE").toString();
		if(itemcode.equals(rule.getItemCodes())) {
			//自己与自己互斥
			return null;
		}
		json.put("MUTEX_ITEM_CODE", rule.getItemCodes());
		json.put("MUTEX_ITEM_NAME", rule.getItemCodes().concat(EngineUtil.SPLIT_KEY).concat(rule.getItemNames()));
        try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (Exception e) {

        }
        return json;
    }
}
