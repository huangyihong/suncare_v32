/**
 * ICaseServiceImpl.java	  V1.0   2020年12月26日 下午6:49:25
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.ai.modules.engine.mapper.QueryMedicalFormalFlowRuleGradeMapper;
import com.ai.modules.engine.mapper.QueryTaskBatchBreakRuleMapper;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineRuleGrade;
import com.ai.modules.engine.service.api.IApiCaseService;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.entity.MedicalFormalCaseItemRela;
import com.ai.modules.formal.mapper.MedicalFormalFlowMapper;
import com.ai.modules.formal.mapper.MedicalFormalFlowRuleMapper;
import com.ai.modules.formal.service.IMedicalFormalCaseItemRelaService;
import com.ai.modules.formal.service.IMedicalFormalCaseService;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.his.mapper.HisMedicalFormalFlowMapper;
import com.ai.modules.his.mapper.HisMedicalFormalFlowRuleMapper;
import com.ai.modules.his.service.IHisMedicalFormalCaseService;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.medical.service.IMedicalSpecialCaseClassifyService;
import com.ai.modules.probe.entity.MedicalProbeCase;
import com.ai.modules.probe.service.IMedicalProbeCaseService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

//@Service
public class CaseServiceImpl implements IApiCaseService {
	@Autowired
    private IHisMedicalFormalCaseService hisCaseService;
	@Autowired
    private QueryMedicalFormalFlowRuleGradeMapper qryRuleGradeMapper;
    @Autowired
    private IMedicalFormalCaseItemRelaService formalRelaService;
    @Autowired
	private IMedicalSpecialCaseClassifyService classifySV;
    @Autowired
    private IMedicalFormalCaseService caseService;
    @Autowired
    private MedicalFormalFlowMapper flowMapper;
    @Autowired
    private MedicalFormalFlowRuleMapper ruleMapper;
    @Autowired
    private HisMedicalFormalFlowMapper hisflowMapper;
    @Autowired
    private HisMedicalFormalFlowRuleMapper hisruleMapper;
    @Autowired
    private QueryTaskBatchBreakRuleMapper qryRuleMapper;
    @Autowired
    private IMedicalProbeCaseService probeCaseService;

	@Override
	public List<HisMedicalFormalCase> findHisMedicalFormalCase(String batchId) {
		/*List<HisMedicalFormalCase> caseList = hisCaseService.list(new QueryWrapper<HisMedicalFormalCase>()
				.eq("BATCH_ID", batchId)
				.inSql("CASE_ID", "SELECT DISTINCT CASE_ID FROM HIS_MEDICAL_FORMAL_CASE_BUSI A, TASK_BATCH_BREAK_RULE B WHERE A.BATCH_ID=B.BATCH_ID AND A.BUSI_ID=B.RULE_ID AND B.RULE_TYPE='01' AND A.BATCH_ID='"+batchId+"'"));*/
		List<HisMedicalFormalCase> caseList = qryRuleMapper.findHisMedicalFormalCase(batchId);
		return caseList;
	}

	@Override
	public List<HisMedicalFormalCase> findHisMedicalFormalCase(String batchId, String busiId) {
		List<HisMedicalFormalCase> caseList = hisCaseService.queryByBusiId(batchId, busiId);
		return caseList;
	}

	@Override
	public HisMedicalFormalCase findHisMedicalFormalCaseByCaseid(String batchId, String caseId) {
		return hisCaseService.queryByCaseId(batchId, caseId);
	}

	@Override
	public MedicalFormalCaseItemRela findMedicalFormalCaseItemRela(String caseId) {
		MedicalFormalCaseItemRela rela = formalRelaService.getOne(new QueryWrapper<MedicalFormalCaseItemRela>().eq("CASE_ID", caseId));
		return rela;
	}

	@Override
	public List<EngineRuleGrade> findEngineRuleGrade(String batchId, String caseId) {
		List<EngineRuleGrade> gradeList = qryRuleGradeMapper.queryEngineRuleGrade(batchId, caseId);
		return gradeList;
	}

	@Override
	public List<MedicalSpecialCaseClassify> findMedicalSpecialCaseClassify() {
		List<MedicalSpecialCaseClassify> classifyList = classifySV.list();
		return classifyList;
	}

	@Override
	public MedicalSpecialCaseClassify findMedicalSpecialCaseClassify(String classifyId) {
		MedicalSpecialCaseClassify bean = classifySV.getOne(new QueryWrapper<MedicalSpecialCaseClassify>().eq("CLASSIFY_ID", classifyId));
		return bean;
	}

	@Override
	public List<EngineNode> recursionMedicalFormalFlowByCaseid(String caseId) {
		List<EngineNode> nodeList = flowMapper.recursionMedicalFormalFlowByCaseid(caseId);
		return nodeList;
	}

	@Override
	public List<EngineNodeRule> queryMedicalFormalFlowRuleByCaseid(String caseId) {
		List<EngineNodeRule> ruleList = ruleMapper.queryMedicalFormalFlowRuleByCaseid(caseId);
		return ruleList;
	}

	@Override
	public List<EngineNode> recursionMedicalFormalFlowByCaseid(String caseId, String batchId) {
		List<EngineNode> nodeList = hisflowMapper.recursionMedicalFormalFlowByCaseid(caseId, batchId);
		return nodeList;
	}
	
	@Override
	public List<EngineNode> queryHisMedicalFormalFlow(String caseId, String batchId) {
		List<EngineNode> nodeList = hisflowMapper.queryHisMedicalFormalFlow(caseId, batchId);
		return nodeList;
	}

	@Override
	public List<EngineNodeRule> queryMedicalFormalFlowRuleByCaseid(String caseId, String batchId) {
		List<EngineNodeRule> ruleList = hisruleMapper.queryEngineNodeRuleByCaseid(caseId, batchId);
		return ruleList;
	}

	@Override
	public List<EngineNodeRule> queryMedicalFormalFlowRuleByTmpl(String nodeId, String nodeCode) {
		List<EngineNodeRule> ruleList = hisruleMapper.queryEngineNodeRuleByTmpl(nodeId, nodeCode);
		return ruleList;
	}

	@Override
	public MedicalFormalCase findMedicalFormalCase(String caseId) {
		return caseService.getById(caseId);
	}

	@Override
	public List<EngineNode> findMedicalFormalFlowByCaseid(String caseId) {
		return flowMapper.findMedicalFormalFlowByCaseid(caseId);
	}

	@Override
	public List<MedicalFormalCase> findMedicalFormalCaseAll() {
		return caseService.list(new QueryWrapper<MedicalFormalCase>().isNull("CASE_CLASSIFY"));
	}

	@Override
	public List<EngineNodeRule> queryMedicalProbeFlowRuleByCaseid(String caseId) {
		return ruleMapper.queryMedicalProbeFlowRuleByCaseid(caseId);
	}

	@Override
	public MedicalProbeCase findMedicalProbeCase(String caseId) {
		return probeCaseService.getById(caseId);
	}

	@Override
	public List<MedicalProbeCase> findMedicalProbeCaseAll() {
		return probeCaseService.list();
	}
	
}
