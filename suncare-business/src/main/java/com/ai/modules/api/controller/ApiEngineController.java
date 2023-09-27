/**
 * ApiEngineController.java	  V1.0   2020年12月14日 下午3:02:35
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.common.MedicalConstant;
import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.config.entity.MedicalDrug;
import com.ai.modules.config.entity.MedicalEquipment;
import com.ai.modules.config.entity.MedicalStdAtc;
import com.ai.modules.config.entity.MedicalTreatProject;
import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.config.entity.StdHoslevelFundpayprop;
import com.ai.modules.config.service.IMedicalDrugService;
import com.ai.modules.config.service.IMedicalEquipmentService;
import com.ai.modules.config.service.IMedicalStdAtcService;
import com.ai.modules.config.service.IMedicalTreatProjectService;
import com.ai.modules.config.service.IMedicalYbDrugService;
import com.ai.modules.config.service.IStdHoslevelFundpaypropService;
import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.mapper.QueryTaskBatchBreakRuleMapper;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.entity.MedicalDruguseRuleGroup;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.medical.entity.MedicalRuleEngine;
import com.ai.modules.medical.entity.dto.QryMedicalDrugRuleDTO;
import com.ai.modules.medical.entity.dto.QryMedicalRuleConfigDTO;
import com.ai.modules.medical.service.IMedicalDruguseRuleGroupService;
import com.ai.modules.medical.service.IMedicalDruguseService;
import com.ai.modules.medical.service.IMedicalRuleConditionSetService;
import com.ai.modules.medical.service.IMedicalRuleConfigService;
import com.ai.modules.medical.service.IMedicalRuleEngineService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags="计算引擎相关")
@Controller
@RequestMapping("/oauth/api/engine")
public class ApiEngineController {
	@Autowired
	private IMedicalStdAtcService atcSV;
	@Autowired
	private IMedicalDrugService drugSV;
	@Autowired
	private IMedicalTreatProjectService treatSV;
	@Autowired
	private IMedicalEquipmentService equipmentSV;
	@Autowired
	private IMedicalRuleConfigService ruleConfigSV;
	@Autowired
	private IMedicalRuleConditionSetService ruleConditionSV;
	@Autowired
    private QueryTaskBatchBreakRuleMapper queryRuleMapper;
	@Autowired
	private IMedicalDruguseService druguseService;
	@Autowired
	private IMedicalDruguseRuleGroupService druguseRuleGroupService;
	@Autowired
	private IMedicalRuleEngineService ruleEngineService;
	@Autowired
	private IStdHoslevelFundpaypropService fundpaypropSV;
	@Autowired
	private IMedicalYbDrugService drugrepeatService;
	
	/*
	@ApiOperation(value = "查询项目批次已选择的用药不合理规则", notes = "查询项目批次已选择的用药不合理规则")
	@RequestMapping(value="/rule/druguse", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> druguse(String batchId) throws Exception {
		List<MedicalDruguse> drugRuleList = queryRuleMapper.queryMedicalDruguseByBatchid(batchId);
		return ApiResponse.ok(drugRuleList);
	}*/
	
	@ApiOperation(value = "查询项目批次已选择的规则（新版）", notes = "查询项目批次已选择的规则（新版）")
	@RequestMapping(value="/new/rule", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> ruleList(String batchId, String stepType) throws Exception {
		QryMedicalRuleConfigDTO dto = new QryMedicalRuleConfigDTO();
		dto.setBatchId(batchId);
		dto.setRuleType(stepType);
		List<MedicalRuleConfig> ruleList = queryRuleMapper.queryMedicalRuleConfigByBatchid(dto);
		return ApiResponse.ok(ruleList);
	}
	
	@ApiOperation(value = "查询项目批次跑失败的规则（新版）", notes = "查询项目批次跑失败的规则（新版）")
	@RequestMapping(value="/new/rule/fail", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> failRuleList(String batchId, String stepType) throws Exception {
		QryMedicalRuleConfigDTO dto = new QryMedicalRuleConfigDTO();
		dto.setBatchId(batchId);
		dto.setRuleType(stepType);
		List<MedicalRuleConfig> ruleList = queryRuleMapper.queryMedicalRuleConfigFail(dto);
		return ApiResponse.ok(ruleList);
	}
	
	@ApiOperation(value = "查询某个规则（新版）", notes = "查询某个规则（新版）")
	@RequestMapping(value="/new/ruleById", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> rule(String ruleId) throws Exception {
		MedicalRuleConfig rule = ruleConfigSV.getOne(new QueryWrapper<MedicalRuleConfig>().eq("RULE_ID", ruleId));
		return ApiResponse.ok(rule);
	}
	
	@ApiOperation(value = "查找规则条件（新版）", notes = "查找规则条件（新版）")
	@RequestMapping(value="/new/ruleConditionList", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> ruleConditionList(String ruleId) throws Exception {
		List<MedicalRuleConditionSet> ruleConditionList = ruleConditionSV.list(new QueryWrapper<MedicalRuleConditionSet>().eq("RULE_ID", ruleId));
		return ApiResponse.ok(ruleConditionList);
	}
	
	@ApiOperation(value = "查询项目批次已选择的规则", notes = "查询项目批次已选择的规则")
	@RequestMapping(value="/rule", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> drugRuleList(String batchId, String stepType) throws Exception {
		QryMedicalDrugRuleDTO whereDTO = new QryMedicalDrugRuleDTO();
        whereDTO.setBatchId(batchId);
        whereDTO.setRuleType(stepType);
        List<MedicalDrugRule> ruleList = queryRuleMapper.queryMedicalDrugRule(whereDTO);
		return ApiResponse.ok(ruleList);
	}
	
	@ApiOperation(value = "查询项目批次已选择的规则(使用分页查询)", notes = "查询项目批次已选择的规则(使用分页查询)")
	@RequestMapping(value="/ruleByPager", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> drugRuleListByPager(String batchId, String stepType) throws Exception {
		if(StringUtils.isBlank(batchId)) {
			throw new EngineBizException("批次号batchId参数不能为空");
		}
		if(StringUtils.isBlank(stepType)) {
			throw new EngineBizException("批次规则类型stepType参数不能为空");
		}
		QueryWrapper<QryMedicalDrugRuleDTO> wrapper = new QueryWrapper<QryMedicalDrugRuleDTO>();
        wrapper.eq("z.rule_type", stepType);
        wrapper.eq("z.batch_id", batchId);
        IPage<MedicalDrugRule> page = new Page<MedicalDrugRule>(1, 1000);
        IPage<MedicalDrugRule> ruleList = queryRuleMapper.queryMedicalDrugRuleByPager(page, wrapper);
        List<MedicalDrugRule> result = ruleList.getRecords();
        if(ruleList.getPages()>1) {
        	for(int i=2; i<=ruleList.getPages(); i++) {
        		page.setCurrent(i);
        		ruleList = queryRuleMapper.queryMedicalDrugRuleByPager(page, wrapper);
        		result.addAll(ruleList.getRecords());
        	}
        }
		return ApiResponse.ok(result);
	}
	
	@ApiOperation(value = "查询项目批次中跑失败的规则", notes = "查询项目批次中跑失败的规则")
	@RequestMapping(value="/rule/fail", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> failDrugRuleList(String batchId, String stepType) throws Exception {
		QryMedicalDrugRuleDTO whereDTO = new QryMedicalDrugRuleDTO();
        whereDTO.setBatchId(batchId);
        whereDTO.setRuleType(stepType);
        List<MedicalDrugRule> ruleList = queryRuleMapper.queryMedicalDrugRuleFail(whereDTO);
		return ApiResponse.ok(ruleList);
	}
	
	@ApiOperation(value = "查询某个规则", notes = "查询某个规则")
	@RequestMapping(value="/ruleById", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> drugRule(String ruleId) throws Exception {
		List<MedicalDrugRule> ruleList = queryRuleMapper.queryMedicalDrugRuleByRuleid(ruleId);
		return ApiResponse.ok(ruleList);
	}
	
	@ApiOperation(value = "获取药品名称", notes = "获取药品名称")
	@RequestMapping(value="/queryDrugName", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> queryDrugName(String itemcode) throws Exception {
		String itemname = null;
		if(itemcode.startsWith("869")) {
			MedicalDrug drug = drugSV.getOne(new QueryWrapper<MedicalDrug>().eq("CODE", itemcode));
			if(drug!=null) {
				itemname = drug.getName();
			}
		} else {
			MedicalStdAtc atc = atcSV.getOne(new QueryWrapper<MedicalStdAtc>().eq("CODE", itemcode));
			if(atc!=null) {
				itemname = atc.getName();
			}
		}
		return ApiResponse.ok(itemname);
	}
	
	@ApiOperation(value = "获取收费项目、诊疗项目等名称", notes = "获取收费项目、诊疗项目等名称")
	@RequestMapping(value="/queryTreatName", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> queryTreatName(String itemcode) throws Exception {
		String itemname = null;
		MedicalTreatProject treat = treatSV.getOne(new QueryWrapper<MedicalTreatProject>().eq("CODE", itemcode));
		if(treat!=null) {
			itemname = treat.getName();
		} else {
			MedicalEquipment equipment = equipmentSV.getOne(new QueryWrapper<MedicalEquipment>().eq("PRODUCTCODE", itemcode));
			if(equipment!=null) {
				itemname = equipment.getProductname();
			}
		}
		return ApiResponse.ok(itemname);
	}
	
	@ApiOperation(value = "查找批次中的某个药品、收费项目、诊疗项目规则", notes = "查找批次中的某个药品、收费项目、诊疗项目规则")
	@RequestMapping(value="/ruleByItem", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> drugRuleByItem(String batchId, String stepType, String itemCode) throws Exception {
		QryMedicalDrugRuleDTO whereDTO = new QryMedicalDrugRuleDTO();
        whereDTO.setBatchId(batchId);
        whereDTO.setRuleType(stepType);
        whereDTO.setItemCode(itemCode);
        List<MedicalDrugRule> ruleList = queryRuleMapper.queryMedicalDrugRuleByItem(whereDTO);
		return ApiResponse.ok(ruleList);
	}
	
	@ApiOperation(value = "查询项目批次已选择的用药规则")
	@RequestMapping(value="/druguse", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> druguseRuleList(String batchId) throws Exception {
		List<MedicalDruguse> ruleList = queryRuleMapper.queryMedicalDruguseByBatchid(batchId);
		return ApiResponse.ok(ruleList);
	}
	
	@ApiOperation(value = "查询项目批次中跑失败的用药规则")
	@RequestMapping(value="/druguse/fail", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> failDruguseRuleList(String batchId) throws Exception {
		List<MedicalDruguse> ruleList = queryRuleMapper.queryMedicalDruguseFail(batchId);
		return ApiResponse.ok(ruleList);
	}
	
	@ApiOperation(value = "查询用药规则")
	@RequestMapping(value="/druguse/byId", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> druguseRule(String ruleId) throws Exception {
		MedicalDruguse rule = druguseService.getById(ruleId);
		return ApiResponse.ok(rule);
	}
	
	@ApiOperation(value = "查询用药规则筛查条件")
	@RequestMapping(value="/druguse/ruleGroup", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> druguseRuleGroup(String ruleId) throws Exception {
		List<MedicalDruguseRuleGroup> ruleGroups = druguseRuleGroupService.list(new QueryWrapper<MedicalDruguseRuleGroup>().eq("RULE_ID", ruleId));
		return ApiResponse.ok(ruleGroups);
	}
	
	@ApiOperation(value = "是否启用hive计算引擎")
	@RequestMapping(value="/openHive", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> openHive(String ruleId, String ruleType) throws Exception {
		int count = ruleEngineService.count(new QueryWrapper<MedicalRuleEngine>()
        		.eq("RULE_ID", ruleId)
        		.eq("STATUS", MedicalConstant.STATUS_VALID)
        		.eq("RULE_TYPE", ruleType));
		return ApiResponse.ok(count);
	}
	
	@ApiOperation(value = "医院等级报销比例")
	@RequestMapping(value="/fundpayRatio", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> fundpayRatio(String datasource) throws Exception {
		List<StdHoslevelFundpayprop> list = fundpaypropSV.list(new QueryWrapper<StdHoslevelFundpayprop>().eq("PROJECT", datasource));
		return ApiResponse.ok(list);
	}
	
	@ApiOperation(value = "查询重复用药规则")
	@RequestMapping(value="/drugrepeat", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> drugrepeatRuleList() throws Exception {
		List<MedicalYbDrug> ruleList = drugrepeatService.list();
		return ApiResponse.ok(ruleList);
	}
	
	@ApiOperation(value = "查询项目批次中跑失败的重复用药规则")
	@RequestMapping(value="/drugrepeat/fail", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> failDrugrepeatRuleList(String batchId) throws Exception {
		List<MedicalYbDrug> ruleList = queryRuleMapper.queryMedicalDrugrepeatFail(batchId);
		return ApiResponse.ok(ruleList);
	}
	
	@ApiOperation(value = "查询某个重复用药规则")
	@RequestMapping(value="/drugrepeat/byParent", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> drugrepeatByParent(String parentCode) throws Exception {
		List<MedicalYbDrug> ruleList = drugrepeatService.list(new QueryWrapper<MedicalYbDrug>().eq("PARENT_CODE", parentCode));
		return ApiResponse.ok(ruleList);
	}
}
