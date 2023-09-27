/**
 * ApiCaseServiceImpl.java	  V1.0   2020年12月26日 下午6:48:34
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineRuleGrade;
import com.ai.modules.engine.model.FormalFlowRule;
import com.ai.modules.engine.service.api.IApiCaseService;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.entity.MedicalFormalCaseItemRela;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.probe.entity.MedicalProbeCase;

@Service
public class ApiCaseServiceImpl implements IApiCaseService {

	@Override
	public List<HisMedicalFormalCase> findHisMedicalFormalCase(String batchId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("batchId", batchId);
		List<HisMedicalFormalCase> result = ApiOauthUtil.responseArray("/oauth/api/case/batchCaseList", busiParams, "post", HisMedicalFormalCase.class);
		return result;
	}

	@Override
	public List<HisMedicalFormalCase> findHisMedicalFormalCase(String batchId, String busiId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("batchId", batchId);
		busiParams.put("busiId", busiId);
		List<HisMedicalFormalCase> result = ApiOauthUtil.responseArray("/oauth/api/case/batchCaseListByBusiid", busiParams, "post", HisMedicalFormalCase.class);
		return result;
	}

	@Override
	public HisMedicalFormalCase findHisMedicalFormalCaseByCaseid(String batchId, String caseId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("batchId", batchId);
		busiParams.put("caseId", caseId);
		HisMedicalFormalCase result = ApiOauthUtil.response("/oauth/api/case/batchCaseByCaseid", busiParams, "post", HisMedicalFormalCase.class);
		return result;
	}

	@Override
	public MedicalFormalCaseItemRela findMedicalFormalCaseItemRela(String caseId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("caseId", caseId);
		MedicalFormalCaseItemRela result = ApiOauthUtil.response("/oauth/api/case/caseRela", busiParams, "post", MedicalFormalCaseItemRela.class);
		return result;
	}

	@Override
	public List<EngineRuleGrade> findEngineRuleGrade(String batchId, String caseId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("batchId", batchId);
		busiParams.put("caseId", caseId);
		List<EngineRuleGrade> result = ApiOauthUtil.responseArray("/oauth/api/case/gradeList", busiParams, "post", EngineRuleGrade.class);
		return result;
	}

	@Override
	public List<MedicalSpecialCaseClassify> findMedicalSpecialCaseClassify() {
		List<MedicalSpecialCaseClassify> result = ApiOauthUtil.responseArray("/oauth/api/case/classifyList", null, "post", MedicalSpecialCaseClassify.class);
		return result;
	}

	@Override
	public MedicalSpecialCaseClassify findMedicalSpecialCaseClassify(String classifyId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("classifyId", classifyId);
		MedicalSpecialCaseClassify result = ApiOauthUtil.response("/oauth/api/case/classify", busiParams, "post", MedicalSpecialCaseClassify.class);
		return result;
	}

	@Override
	public List<EngineNode> recursionMedicalFormalFlowByCaseid(String caseId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("caseId", caseId);
		List<EngineNode> result = ApiOauthUtil.responseArray("/oauth/api/case/flow", busiParams, "post", EngineNode.class);
		return result;
	}

	@Override
	public List<EngineNodeRule> queryMedicalFormalFlowRuleByCaseid(String caseId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("caseId", caseId);
		List<EngineNodeRule> result = ApiOauthUtil.responseArray("/oauth/api/case/flow/rule", busiParams, "post", EngineNodeRule.class);
		return result;
	}

	@Override
	public List<EngineNode> recursionMedicalFormalFlowByCaseid(String caseId, String batchId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("batchId", batchId);
		busiParams.put("caseId", caseId);
		List<EngineNode> result = ApiOauthUtil.responseArray("/oauth/api/case/hisflow", busiParams, "post", EngineNode.class);
		return result;
	}
	
	@Override
	public List<EngineNode> queryHisMedicalFormalFlow(String caseId, String batchId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("batchId", batchId);
		busiParams.put("caseId", caseId);
		List<EngineNode> result = ApiOauthUtil.responseArray("/oauth/api/case/queryHisMedicalFormalFlow", busiParams, "post", EngineNode.class);
		return result;
	}

	@Override
	public List<EngineNodeRule> queryMedicalFormalFlowRuleByCaseid(String caseId, String batchId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("batchId", batchId);
		busiParams.put("caseId", caseId);
		List<FormalFlowRule> list = ApiOauthUtil.responseArray("/oauth/api/case/hisflow/rule", busiParams, "post", FormalFlowRule.class);
		List<EngineNodeRule> result = new ArrayList<EngineNodeRule>();
		for(FormalFlowRule record : list) {
			EngineNodeRule rule = this.copy(record);
			result.add(rule);
		}
		return result;
	}

	@Override
	public List<EngineNodeRule> queryMedicalFormalFlowRuleByTmpl(String nodeId, String nodeCode) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("nodeId", nodeId);
		busiParams.put("nodeCode", nodeCode);
		List<FormalFlowRule> list = ApiOauthUtil.responseArray("/oauth/api/case/tmpl/rule", busiParams, "post", FormalFlowRule.class);
		List<EngineNodeRule> result = new ArrayList<EngineNodeRule>();
		for(FormalFlowRule record : list) {
			EngineNodeRule rule = this.copy(record);
			result.add(rule);
		}
		return result;
	}
	
	private EngineNodeRule copy(FormalFlowRule record) {
		EngineNodeRule rule = new EngineNodeRule();
		rule.setTableName(record.getTableName());
		rule.setColName(record.getColName());
		rule.setCompareType(record.getCompareType());
		rule.setCompareValue(record.getCompareValue());
		rule.setLogic(record.getLogic());
		rule.setOrderNo(record.getOrderNo());
		rule.setGroupNo(record.getGroupNo());
		rule.setNodeCode(record.getNodeCode());
		rule.setColConfig(record.getColConfig());
		return rule;
	}

	@Override
	public MedicalFormalCase findMedicalFormalCase(String caseId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("caseId", caseId);
		MedicalFormalCase result = ApiOauthUtil.response("/oauth/api/case/formal/byId", busiParams, "post", MedicalFormalCase.class);
		return result;
	}

	@Override
	public List<EngineNode> findMedicalFormalFlowByCaseid(String caseId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("caseId", caseId);
		List<EngineNode> result = ApiOauthUtil.responseArray("/oauth/api/case/formal/flow", busiParams, "post", EngineNode.class);
		return result;
	}

	@Override
	public List<MedicalFormalCase> findMedicalFormalCaseAll() {
		List<MedicalFormalCase> result = ApiOauthUtil.responseArray("/oauth/api/case/formal/all", null, "post", MedicalFormalCase.class);
		return result;
	}
	
	@Override
	public List<EngineNodeRule> queryMedicalProbeFlowRuleByCaseid(String caseId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("caseId", caseId);
		List<EngineNodeRule> result = ApiOauthUtil.responseArray("/oauth/api/case/probe/flow/rule", busiParams, "post", EngineNodeRule.class);
		return result;
	}

	@Override
	public MedicalProbeCase findMedicalProbeCase(String caseId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("caseId", caseId);
		MedicalProbeCase result = ApiOauthUtil.response("/oauth/api/case/probe/byId", busiParams, "post", MedicalProbeCase.class);
		return result;
	}

	@Override
	public List<MedicalProbeCase> findMedicalProbeCaseAll() {
		List<MedicalProbeCase> result = ApiOauthUtil.responseArray("/oauth/api/case/probe/all", null, "post", MedicalProbeCase.class);
		return result;
	}
}
