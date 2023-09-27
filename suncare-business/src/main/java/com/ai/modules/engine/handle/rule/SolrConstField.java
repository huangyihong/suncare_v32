/**
 * UnReasonableActionField.java	  V1.0   2020年11月5日 下午5:36:11
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule;

import java.util.LinkedHashMap;
import java.util.Map;

public class SolrConstField {
	//模型编码
	@SolrFieldAnnotation("CASE_ID")
	private String caseId;
	//模型名称
	@SolrFieldAnnotation("CASE_NAME")
	private String caseName;
	//不合规类型编码
	@SolrFieldAnnotation("ACTION_TYPE_ID")
	private String actionTypeId;
	//不合规类型名称
	@SolrFieldAnnotation("ACTION_TYPE_NAME")
	private String actionTypeName;
	//不合规编码
	@SolrFieldAnnotation("ACTION_ID")
	private String actionId;
	//不合规名称
	@SolrFieldAnnotation("ACTION_NAME")
	private String actionName;
	//不合规释义
	@SolrFieldAnnotation("ACTION_DESC")
	private String actionDesc;
	//业务类型
	@SolrFieldAnnotation("BUSI_TYPE")
	private String busiType;
	//违规限定范围
	@SolrFieldAnnotation("RULE_SCOPE")
	private String ruleScope;
	//违规限定范围名称
	@SolrFieldAnnotation("RULE_SCOPE_NAME")
	private String ruleScopeName;
	//规则依据
	@SolrFieldAnnotation("RULE_BASIS")
	private String ruleBasis;
	//不合规行为级别
	@SolrFieldAnnotation("RULE_LEVEL")
	private String ruleLevel;
	//规则ID
	@SolrFieldAnnotation("RULE_ID")
	private String ruleId;
	//规则名称
	@SolrFieldAnnotation("RULE_FNAME")
	private String ruleFname;
	//规则限定类型
	@SolrFieldAnnotation("RULE_LIMIT")
	private String ruleLimit;
	//规则级别
	@SolrFieldAnnotation("RULE_GRADE")
	private String ruleGrade;
	//规则级别备注
	@SolrFieldAnnotation("RULE_GRADE_REMARK")
	private String ruleGradeRemark;
	//项目ID
	@SolrFieldAnnotation("PROJECT_ID")
	private String projectId;
	//项目名称
	@SolrFieldAnnotation("PROJECT_NAME")
	private String projectName;
	//批次号
	@SolrFieldAnnotation("BATCH_ID")
	private String batchId;
	//批次号
	@SolrFieldAnnotation("TASK_BATCH_NAME")
	private String batchName;
	//生成时间
	@SolrFieldAnnotation("GEN_DATA_TIME")
	private String genDataTime;
	//初审初始状态
	@SolrFieldAnnotation("FIR_REVIEW_STATUS")
	private String firReviewStatus = "init";
	
	private Map<String, String> constMap = null;
	
	public void addConst(String key, String value) {
		if(constMap==null) {
			constMap = new LinkedHashMap<String, String>();
		}
		constMap.put(key, value);
	}
	
	public String getCaseId() {
		return caseId;
	}
	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}
	public String getCaseName() {
		return caseName;
	}
	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}
	public String getActionTypeId() {
		return actionTypeId;
	}
	public void setActionTypeId(String actionTypeId) {
		this.actionTypeId = actionTypeId;
	}
	public String getActionTypeName() {
		return actionTypeName;
	}
	public void setActionTypeName(String actionTypeName) {
		this.actionTypeName = actionTypeName;
	}
	public String getActionId() {
		return actionId;
	}
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	public String getActionName() {
		return actionName;
	}
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	public String getActionDesc() {
		return actionDesc;
	}
	public void setActionDesc(String actionDesc) {
		this.actionDesc = actionDesc;
	}
	public String getBusiType() {
		return busiType;
	}
	public void setBusiType(String busiType) {
		this.busiType = busiType;
	}
	public String getRuleScope() {
		return ruleScope;
	}
	public void setRuleScope(String ruleScope) {
		this.ruleScope = ruleScope;
	}
	public String getRuleScopeName() {
		return ruleScopeName;
	}
	public void setRuleScopeName(String ruleScopeName) {
		this.ruleScopeName = ruleScopeName;
	}
	public String getRuleBasis() {
		return ruleBasis;
	}
	public void setRuleBasis(String ruleBasis) {
		this.ruleBasis = ruleBasis;
	}
	public String getRuleId() {
		return ruleId;
	}
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}
	public String getRuleFname() {
		return ruleFname;
	}
	public void setRuleFname(String ruleFname) {
		this.ruleFname = ruleFname;
	}
	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getBatchId() {
		return batchId;
	}
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	public String getBatchName() {
		return batchName;
	}

	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}

	public String getGenDataTime() {
		return genDataTime;
	}
	public void setGenDataTime(String genDataTime) {
		this.genDataTime = genDataTime;
	}
	public String getFirReviewStatus() {
		return firReviewStatus;
	}
	public void setFirReviewStatus(String firReviewStatus) {
		this.firReviewStatus = firReviewStatus;
	}
	public String getRuleLevel() {
		return ruleLevel;
	}
	public void setRuleLevel(String ruleLevel) {
		this.ruleLevel = ruleLevel;
	}

	public Map<String, String> getConstMap() {
		return constMap;
	}

	public void setConstMap(Map<String, String> constMap) {
		this.constMap = constMap;
	}

	public String getRuleGrade() {
		return ruleGrade;
	}

	public void setRuleGrade(String ruleGrade) {
		this.ruleGrade = ruleGrade;
	}

	public String getRuleGradeRemark() {
		return ruleGradeRemark;
	}

	public void setRuleGradeRemark(String ruleGradeRemark) {
		this.ruleGradeRemark = ruleGradeRemark;
	}

	public String getRuleLimit() {
		return ruleLimit;
	}

	public void setRuleLimit(String ruleLimit) {
		this.ruleLimit = ruleLimit;
	}
}
