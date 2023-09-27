/**
 * DrugSecondLineHandle.java	  V1.0   2020年11月4日 下午4:09:05
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.solr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.MD5Util;
import org.jeecg.common.util.SpringContextUtils;

import com.ai.common.MedicalConstant;
import com.ai.modules.api.util.ApiOauthClientUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.handle.rule.AbsSolrRuleHandle;
import com.ai.modules.engine.handle.rule.RuleHandleFactory;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.handle.rule.parse.RuleIgnoreNullParser;
import com.ai.modules.engine.handle.rule.parse.RuleIndicationParser;
import com.ai.modules.engine.handle.rule.parse.RuleMasterInfoParser;
import com.ai.modules.engine.handle.rule.parse.RuleParser;
import com.ai.modules.engine.handle.secondary.AbsRuleSecondHandle;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.service.impl.EngineActionServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 功能描述：solr方式计算不合规行为
 *
 * @author  zhangly
 * Date: 2020年11月4日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Slf4j
public class SolrRuleHandle extends AbsSolrRuleHandle {
	//规则对象
	protected MedicalRuleConfig rule;
	//规则条件
	protected List<MedicalRuleConditionSet> ruleConditionList;
	
	protected IEngineActionService engineActionService = SpringContextUtils.getApplicationContext().getBean(IEngineActionService.class);
	protected IApiDictService dictSV = SpringContextUtils.getApplicationContext().getBean(IApiDictService.class);
	
	/**
	 * 
	 * 构造函数：
	 *
	 * @param task 任务项目
	 * @param batch 任务批次
	 * @param trail 是否试算
	 * @param rule 规则
	 * @param ruleConditionList 规则筛查条件
	 */
	public SolrRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail);
		this.rule = rule;
		this.ruleConditionList = ruleConditionList;
	}
	
	/**
	 * 
	 * 功能描述：规则是否忽略执行
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年12月10日 上午9:28:47</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected boolean ignoreRun() {
		return ignoreRun(rule);
	}

	@Override
	public void generateUnreasonableAction() throws Exception {
		if(ignoreRun()) {
			//忽略运行
			return;
		}		
		
		String itemcode = rule.getItemCodes();
		List<String> conditionList = new ArrayList<String>();
		if(this.isProjectGrp()) {
			//项目组
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
			conditionList.add(plugin.parse() + "TREATGROUP_CODE:" + itemcode);
		} else {
			conditionList.add("ITEMCODE:" + itemcode);
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
  		Set<String> nextSet = new HashSet<String>();
  		//必要前提条件
  		nextSet.add("fitGroups1");
        if(count>0 || nextSet.contains(rule.getRuleLimit())) {
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
	
	protected int handle(List<String> conditionList, String fromCollection) throws Exception {
		String drugCollection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
		String desc = null;
  		if(StringUtils.isNotBlank(rule.getActionType())) {        	
			desc = ApiOauthClientUtil.parseText("ACTION_TYPE", rule.getActionType());
        }
  		String actionTypeName = desc; //不合规行为类型名称
  		//不合规行为字典映射  		
        Map<String, MedicalActionDict> actionDictMap = dictSV.queryActionDict();
        // 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/" + drugCollection + "/" + batch.getBatchId() + "/" + rule.getRuleId() + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");
        int count = SolrUtil.exportDocByPager(conditionList, fromCollection, (doc, index) -> {
            // 循环每条数据写入文件
        	writerJson(fileWriter, doc, actionTypeName, actionDictMap);
        });
        // 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();

        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, drugCollection);
        return count;
	}
	
	/**
	 * 
	 * 功能描述：公共查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年12月10日 上午9:44:50</p>
	 *
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected List<String> parseCommonCondition(boolean exclude) throws Exception {
		//项目的数据时间范围
  		String project_startTime = MedicalConstant.DEFAULT_START_TIME;
		String project_endTime = MedicalConstant.DEFAULT_END_TIME;
		project_startTime = task.getDataStartTime()!=null ? DateUtil.format(task.getDataStartTime(), "yyyy-MM-dd") : project_startTime;
        project_endTime = task.getDataEndTime()!=null ? DateUtil.format(task.getDataEndTime(), "yyyy-MM-dd") : project_endTime;
		//批次的数据时间范围
  		String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
  		String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
  		batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
  		batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
  		//规则的数据时间范围
        String rule_startTime = DateUtil.format(rule.getStartTime(), "yyyy-MM-dd");
        String rule_endTime = DateUtil.format(rule.getEndTime(), "yyyy-MM-dd");
		List<String> conditionList = new ArrayList<String>();
		//项目过滤条件
        ProjectFilterWhereVO filterVO = engineActionService.filterCondition(task, false);
        if(StringUtils.isNotBlank(filterVO.getCondition())) {
        	conditionList.add(filterVO.getCondition());
        }
        if(filterVO.isDiseaseFilter() && EngineUtil.existsDisease(ruleConditionList)) {
        	//疾病映射不全过滤
        	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_DIAG", "VISITID", "VISITID");
        	conditionList.add("*:* -"+plugin.parse()+"-DISEASENAME:?*");
        }
        if (StringUtils.isNotBlank(task.getEtlSource())) {
            conditionList.add("ETL_SOURCE:" + task.getEtlSource());
        }
        if (StringUtils.isNotBlank(batch.getEtlSource())) {
            conditionList.add("ETL_SOURCE:" + batch.getEtlSource());
        }
        //项目数据时间范围限制
        StringBuilder sb = new StringBuilder();		
		sb.append("VISITDATE:");
		sb.append("[").append(project_startTime).append(" TO ").append(project_endTime).append("]");
        conditionList.add(sb.toString());
        //批次数据时间范围限制
        sb.setLength(0);		
		sb.append("VISITDATE:");
		sb.append("[").append(batch_startTime).append(" TO ").append(batch_endTime).append("]");
        conditionList.add(sb.toString());
        if(StringUtils.isNotBlank(task.getDataOrgFilter())) {
        	//医疗机构范围限制
        	String value = task.getDataOrgFilter();
        	value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
        	conditionList.add("ORGID:"+value);
        }
        if(StringUtils.isNotBlank(batch.getCustomFilter())) {
        	//自定义数据范围限制
        	String value = batch.getCustomFilter();
        	value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
        	conditionList.add("ORGID:"+value);
        }
        //规则的数据时间范围限制
        sb.setLength(0);
		sb.append("VISITDATE:");
		sb.append("[").append(rule_startTime);
		sb.append(" TO ");
		sb.append(rule_endTime).append("]");
		conditionList.add(sb.toString());
		if("1".equals(batch.getYbFundRm0())) {
			//过滤掉病例基金支出金额为0的数据
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
			conditionList.add(plugin.parse()+"FUNDPAY:{0 TO *}");
		}
		if(!exclude) {
			//基金支出金额>0
			conditionList.add("FUND_COVER:{0 TO *}");
			//自付比例<0
			conditionList.add("SELFPAY_PROP_MIN:[0 TO 1}");			
		}
  		return conditionList;
	}
	
	protected List<String> parseCommonCondition() throws Exception {
		return parseCommonCondition(false);
	}
	
	/**
	 * 
	 * 功能描述：准入条件、判断条件分组
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月16日 下午4:12:37</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private List<List<MedicalRuleConditionSet>> parseCondition(String type, Set<String> exclude) {
		return parseCondition(ruleConditionList, type, exclude);
	}
	
	protected List<MedicalRuleConditionSet> parseConditionList(String type, Set<String> exclude) {
		List<MedicalRuleConditionSet> accessList = ruleConditionList.stream().filter(s->type.equals(s.getType())).collect(Collectors.toList());
		if(exclude!=null && exclude.size()>0) {
			accessList = accessList.stream().filter(s->!exclude.contains(s.getField())).collect(Collectors.toList());
		}
		return accessList;
	}
	
	/**
	 * 
	 * 功能描述：不合规查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月9日 下午5:49:29</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected List<String> parseCondition() {
		List<String> result = new ArrayList<String>();
	    if(ruleConditionList!=null) {
	    	//准入条件
	    	String condition = this.parseAccessCondition();
	    	if(StringUtils.isNotBlank(condition)) {
	    		result.add(condition);
	    	}
	    	//判断条件
	    	condition = this.parseJudgeCondition();
	    	if(StringUtils.isBlank(condition)) {
	    		throw new RuntimeException(rule.getItemNames()+"规则未配置判断条件！");
	    	}
	    	result.add(condition);
	    }	            
	    return result;
	}
	
	/**
	 * 
	 * 功能描述：解析准入条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月24日 下午3:09:20</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected String parseAccessCondition() {		
		List<List<MedicalRuleConditionSet>> accessGrpList = this.parseCondition("access", null);
    	if(accessGrpList!=null) {
    		StringBuilder sb = new StringBuilder();    		
    		int group = 0; //组序号
    		for(List<MedicalRuleConditionSet> accessList : accessGrpList) {
    			//组内的第一个逻辑符作为组与组之间的关系符
    			String logic = accessList.get(0).getLogic();
    			if(group>0) {
    				sb.append(" ").append(logic).append(" ");
    			}
    			int size = accessList.size();
    			if(size>1) {
    				sb.append("(");
    			}
    			int order = 0;//组内限定条件序号
    			for(MedicalRuleConditionSet bean : accessList) {
    				String where = this.parseCondition(bean);
    				if(order>0) {
    					sb.append(" ").append(bean.getLogic()).append(" ");
    				}
    				sb.append(where);
    				order++;
    			}
    			if(size>1) {
    				sb.append(")");
    			}
    			group++;
    		}
    		return sb.toString();
    	}
    	return null;
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
	protected String parseJudgeCondition() {
		//限定条件
    	Set<String> exclude = new HashSet<String>();
    	exclude.add("fitTimeRange");
    	List<List<MedicalRuleConditionSet>> judgeGrpList = this.parseCondition("judge", exclude);
    	if(judgeGrpList!=null) {
    		StringBuilder sb = new StringBuilder();
    		int group = 0; //组序号
    		for(List<MedicalRuleConditionSet> judgeList : judgeGrpList) {
    			//组内的第一个逻辑符作为组与组之间的关系符
    			String logic = judgeList.get(0).getLogic();
    			if(group>0) {
    				sb.append(" ").append(logic).append(" ");
    			}
    			int size = judgeList.size();
    			if(size>1) {
    				sb.append("(");
    			}
    			int order = 0;//组内限定条件序号
    			for(MedicalRuleConditionSet bean : judgeList) {
    				String where = this.parseCondition(bean);
    				if(order>0) {
    					sb.append(" ").append(bean.getLogic()).append(" ");
    				}
    				sb.append(where);
    				order++;
    			}
    			if(size>1) {
    				sb.append(")");
    			}
    			group++;
    		}
    		//黑名单取反
    		String condition = "*:* -(" + sb.toString() + ")";
    		return condition;
    	}
    	return null;
	}
	
	private String parseCondition(MedicalRuleConditionSet bean) {
		AbsRuleParser parser = null;
		String condiType = bean.getField();
		if(AbsRuleParser.RULE_MASTER_SET.contains(condiType)) {
			//关联master_info
			parser = new RuleMasterInfoParser(rule, bean);
		} else if(AbsRuleParser.RULE_CONDI_INDICATION.equals(condiType)) {
			//适应症
			parser = new RuleIndicationParser(rule, bean);
		} else {
			parser = new RuleParser(rule, bean);
		}
		return parser.parseCondition();		
	}
	
	protected JSONObject parseJSONObject(SolrDocument document, String actionTypeName, Map<String, MedicalActionDict> actionDictMap) {
		JSONObject json = new JSONObject();
        String id = MD5Util.MD5Encode(batch.getBatchId().concat("_").concat(rule.getRuleId()).concat("_").concat((String) document.get("id")), "utf-8");
        json.put("id", id);
        for (String field : EngineActionServiceImpl.DURG_ACTION_FIELD) {
            Object val = document.get(field);
            if (val != null) {
                json.put(field, val);
            }
        }
        json.put("ITEM_ID", document.get("id"));        
        json.put("RULE_ID", rule.getRuleId());
        json.put("RULE_NAME", rule.getItemNames());
        json.put("RULE_DESC", rule.getMessage());
        String ruleFName = rule.getRuleId() + "::" + rule.getItemNames();
        json.put("RULE_FNAME", ruleFName);
        String ruleFDescs = rule.getRuleId() + "::" + rule.getMessage();
        json.put("RULE_FDESC", ruleFDescs);
        json.put("RULE_BASIS", rule.getRuleBasis());
        json.put("RULE_TYPE", this.getBusiType());
        json.put("RULE_SCOPE", rule.getRuleLimit());
        json.put("RULE_SCOPE_NAME", rule.getActionName());
        json.put("ACTION_TYPE_ID", rule.getActionType());
        json.put("ACTION_TYPE_NAME", actionTypeName);        
        json.put("ACTION_ID", rule.getActionId());
        json.put("ACTION_NAME", rule.getActionName());
        if(actionDictMap.containsKey(rule.getActionId())) {
        	MedicalActionDict actionDict = actionDictMap.get(rule.getActionId());
        	json.put("ACTION_NAME", actionDict.getActionName());
        	json.put("RULE_LEVEL", actionDict.getRuleLevel());
        }
        json.put("ACTION_DESC", rule.getMessage());
        json.put("RULE_LIMIT", rule.getRuleLimit());
        json.put("RULE_GRADE", rule.getRuleGrade());
        json.put("RULE_GRADE_REMARK", rule.getRuleGradeRemark());
        //基金支出金额
        json.put("MIN_ACTION_MONEY", document.get("FUND_COVER"));
        json.put("MAX_ACTION_MONEY", document.get("FUND_COVER"));
        //收费项目费用
        json.put("MIN_MONEY", document.get("ITEM_AMT"));
        json.put("MAX_MONEY", document.get("ITEM_AMT"));
        
        json.put("GEN_DATA_TIME", DateUtils.now());
        json.put("PROJECT_ID", task.getProjectId());
        json.put("PROJECT_NAME", task.getProjectName());
        json.put("BATCH_ID", batch.getBatchId());
        json.put("TASK_BATCH_NAME", batch.getBatchName());
        return json;
	}
	
	protected JSONObject writerJson(BufferedWriter fileWriter, SolrDocument document, String actionTypeName, Map<String, MedicalActionDict> actionDictMap) {		
		JSONObject json = this.parseJSONObject(document, actionTypeName, actionDictMap);
        try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (Exception e) {

        }
        return json;
    }
	
	protected JSONObject writerJsonFromDwsChargeDetial(BufferedWriter fileWriter, SolrDocument document, String actionTypeName, Map<String, MedicalActionDict> actionDictMap) {		
		JSONObject json = new JSONObject();
        String id = MD5Util.MD5Encode(batch.getBatchId().concat("_").concat(rule.getRuleId()).concat("_").concat((String) document.get("id")), "utf-8");
        json.put("id", id);
        for (String field : EngineActionServiceImpl.DURG_ACTION_FIELD) {
        	if(EngineActionServiceImpl.DETAIL_DRUGACTION_MAPPING.containsKey(field)) {
        		field = EngineActionServiceImpl.DETAIL_DRUGACTION_MAPPING.get(field);
        	}
            Object val = document.get(field);
            if (val != null) {
                json.put(field, val);
            }
        }
                
        json.put("RULE_ID", rule.getRuleId());
        json.put("RULE_NAME", rule.getItemNames());
        json.put("RULE_DESC", rule.getMessage());
        String ruleFName = rule.getRuleId() + "::" + rule.getItemNames();
        json.put("RULE_FNAME", ruleFName);
        String ruleFDescs = rule.getRuleId() + "::" + rule.getMessage();
        json.put("RULE_FDESC", ruleFDescs);
        json.put("RULE_BASIS", rule.getRuleBasis());
        json.put("RULE_TYPE", this.getBusiType());
        json.put("RULE_SCOPE", rule.getRuleLimit());
        json.put("RULE_SCOPE_NAME", rule.getActionName());
        json.put("ACTION_TYPE_ID", rule.getActionType());
        json.put("ACTION_TYPE_NAME", actionTypeName);        
        json.put("ACTION_ID", rule.getActionId());
        json.put("ACTION_NAME", rule.getActionName());
        if(actionDictMap.containsKey(rule.getActionId())) {
        	MedicalActionDict actionDict = actionDictMap.get(rule.getActionId());
        	json.put("ACTION_NAME", actionDict.getActionName());
        	json.put("RULE_LEVEL", actionDict.getRuleLevel());
        }
        json.put("ACTION_DESC", rule.getMessage());
        //基金支出金额
        json.put("MIN_ACTION_MONEY", document.get("FUND_COVER"));
        json.put("MAX_ACTION_MONEY", document.get("FUND_COVER"));
        //收费项目费用
        json.put("MIN_MONEY", document.get("FEE"));
        json.put("MAX_MONEY", document.get("FEE"));
        json.put("BREAK_RULE_TIME", document.get("CHARGEDATE"));
        
        json.put("GEN_DATA_TIME", DateUtils.now());
        json.put("PROJECT_ID", task.getProjectId());
        json.put("PROJECT_NAME", task.getProjectName());
        json.put("BATCH_ID", batch.getBatchId());
        json.put("TASK_BATCH_NAME", batch.getBatchName());
        try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (Exception e) {

        }
        return json;
    }
	
	protected String getBusiType() {
		return getBusiType(rule);
	}
	
	/**
	 * 
	 * 功能描述：判断主体项目是否为项目组
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	protected boolean isProjectGrp() {
		String itemType = rule.getItemTypes();
		if(MedicalConstant.ITEM_PROJECTGRP.equals(itemType)) {
			return true;
		}
		return false;
	}
}
