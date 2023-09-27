/**
 * ApiCaseController.java	  V1.0   2020年12月26日 下午6:37:57
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.engine.mapper.QueryMedicalFormalFlowRuleGradeMapper;
import com.ai.modules.engine.mapper.QueryTaskBatchBreakRuleMapper;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineRuleGrade;
import com.ai.modules.engine.model.FormalFlowRule;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags="模型相关")
@Controller
@RequestMapping("/oauth/api/case")
public class ApiCaseController {
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
    
    @ApiOperation(value = "根据批次号查找已选中的模型")
	@RequestMapping(value="/batchCaseList", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> batchCaseList(String batchId) throws Exception {
		/*List<HisMedicalFormalCase> caseList = hisCaseService.list(new QueryWrapper<HisMedicalFormalCase>()
				.eq("BATCH_ID", batchId)
				.inSql("CASE_ID", "SELECT DISTINCT CASE_ID FROM HIS_MEDICAL_FORMAL_CASE_BUSI A, TASK_BATCH_BREAK_RULE B WHERE A.BATCH_ID=B.BATCH_ID AND A.BUSI_ID=B.RULE_ID AND B.RULE_TYPE='01' AND A.BATCH_ID='"+batchId+"'"));*/
    	List<HisMedicalFormalCase> caseList = qryRuleMapper.findHisMedicalFormalCase(batchId);
		return ApiResponse.ok(caseList);
	}
    
    @ApiOperation(value = "根据批次号业务组号查找已选中的模型")
	@RequestMapping(value="/batchCaseListByBusiid", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> batchCaseList(String batchId, String busiId) throws Exception {
    	List<HisMedicalFormalCase> caseList = hisCaseService.queryByBusiId(batchId, busiId);
		return ApiResponse.ok(caseList);
	}
    
    @ApiOperation(value = "根据模型编号查找模型")
	@RequestMapping(value="/batchCaseByCaseid", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> batchCaseByCaseid(String batchId, String caseId) throws Exception {
    	HisMedicalFormalCase bean = hisCaseService.queryByCaseId(batchId, caseId);
		return ApiResponse.ok(bean);
	}
    
    @ApiOperation(value = "根据模型编号查找模型关联的项目")
	@RequestMapping(value="/caseRela", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> caseRela(String caseId) throws Exception {
    	MedicalFormalCaseItemRela rela = formalRelaService.getOne(new QueryWrapper<MedicalFormalCaseItemRela>().eq("CASE_ID", caseId));
		return ApiResponse.ok(rela);
	}
    
    @ApiOperation(value = "根据模型编号查找模型的评分列表")
	@RequestMapping(value="/gradeList", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> gradeList(String batchId, String caseId) throws Exception {
    	List<EngineRuleGrade> gradeList = qryRuleGradeMapper.queryEngineRuleGrade(batchId, caseId);
		return ApiResponse.ok(gradeList);
	}
    
    @ApiOperation(value = "查找特殊模型列表")
	@RequestMapping(value="/classifyList", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> classifyList() throws Exception {
		List<MedicalSpecialCaseClassify> classifyList = classifySV.list();
		return ApiResponse.ok(classifyList);
	}
    
    @ApiOperation(value = "查找特殊模型列表")
	@RequestMapping(value="/classify", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> classify(String classifyId) throws Exception {
    	MedicalSpecialCaseClassify bean = classifySV.getOne(new QueryWrapper<MedicalSpecialCaseClassify>().eq("CLASSIFY_ID", classifyId));
		return ApiResponse.ok(bean);
	}
    
    @ApiOperation(value = "按模型ID递归查询流程节点")
	@RequestMapping(value="/flow", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> flow(String caseId) throws Exception {
    	List<EngineNode> nodeList = flowMapper.recursionMedicalFormalFlowByCaseid(caseId);
		return ApiResponse.ok(nodeList);
	}
    
    @ApiOperation(value = "按模型ID查询流程节点规则条件")
	@RequestMapping(value="/flow/rule", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> flowRule(String caseId) throws Exception {
    	List<EngineNodeRule> ruleList = ruleMapper.queryMedicalFormalFlowRuleByCaseid(caseId);
		return ApiResponse.ok(ruleList);
	}
    
    @ApiOperation(value = "按批次号、模型ID递归查询流程节点")
	@RequestMapping(value="/hisflow", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> hisflow(String batchId, String caseId) throws Exception {
    	List<EngineNode> nodeList = hisflowMapper.recursionMedicalFormalFlowByCaseid(caseId, batchId);
		return ApiResponse.ok(nodeList);
	}
    
    @ApiOperation(value = "按批次号、模型ID查询流程节点")
	@RequestMapping(value="/queryHisMedicalFormalFlow", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> queryHisMedicalFormalFlow(String batchId, String caseId) throws Exception {
    	List<EngineNode> nodeList = hisflowMapper.queryHisMedicalFormalFlow(caseId, batchId);
		return ApiResponse.ok(nodeList);
	}
    
    @ApiOperation(value = "按批次号、模型ID递归查询流程节点")
	@RequestMapping(value="/hisflow/rule", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> hisFlowRule(String batchId, String caseId) throws Exception {
    	List<FormalFlowRule> ruleList = hisruleMapper.queryMedicalFormalFlowRuleByCaseid(caseId, batchId);
		return ApiResponse.ok(ruleList);
	}
    
    @ApiOperation(value = "查找模板流程节点的所有查询条件")
	@RequestMapping(value="/tmpl/rule", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> tmplRule(String nodeId, String nodeCode) throws Exception {
    	List<FormalFlowRule> ruleList = hisruleMapper.queryMedicalFormalFlowRuleByTmpl(nodeId, nodeCode);
		return ApiResponse.ok(ruleList);
	}
    
    @ApiOperation(value = "查找模型")
	@RequestMapping(value="/formal/byId", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> formalCase(String caseId) throws Exception {
    	MedicalFormalCase bean = caseService.getById(caseId);
		return ApiResponse.ok(bean);
	}
    
    @ApiOperation(value = "按模型ID查询流程节点")
	@RequestMapping(value="/formal/flow", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> formalFlow(String caseId) throws Exception {
    	List<EngineNode> nodeList = flowMapper.findMedicalFormalFlowByCaseid(caseId);
		return ApiResponse.ok(nodeList);
	}
    
    @ApiOperation(value = "查找所有模型")
	@RequestMapping(value="/formal/all", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> formalCaseAll() throws Exception {
    	List<MedicalFormalCase> list = caseService.list(new QueryWrapper<MedicalFormalCase>().isNull("CASE_CLASSIFY"));
		return ApiResponse.ok(list);
	}
    
    @ApiOperation(value = "按探查模型ID查询流程节点规则条件")
	@RequestMapping(value="/probe/flow/rule", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> probeFlowRule(String caseId) throws Exception {
    	List<EngineNodeRule> ruleList = ruleMapper.queryMedicalProbeFlowRuleByCaseid(caseId);
		return ApiResponse.ok(ruleList);
	}
    
    @ApiOperation(value = "查找探查模型")
	@RequestMapping(value="/probe/byId", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> probeCase(String caseId) throws Exception {
    	MedicalProbeCase bean = probeCaseService.getById(caseId);
		return ApiResponse.ok(bean);
	}
    
    @ApiOperation(value = "查找所有探查模型")
	@RequestMapping(value="/probe/all", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> probeCaseAll() throws Exception {
    	List<MedicalProbeCase> list = probeCaseService.list();
		return ApiResponse.ok(list);
	}
}
