/**
 * QueryTaskBatchRuleServiceImpl.java	  V1.0   2020年12月22日 上午9:18:16
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
import org.springframework.stereotype.Service;

import com.ai.common.MedicalConstant;
import com.ai.modules.config.entity.MedicalDrug;
import com.ai.modules.config.entity.MedicalEquipment;
import com.ai.modules.config.entity.MedicalStdAtc;
import com.ai.modules.config.entity.MedicalTreatProject;
import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.config.service.IMedicalDrugService;
import com.ai.modules.config.service.IMedicalEquipmentService;
import com.ai.modules.config.service.IMedicalStdAtcService;
import com.ai.modules.config.service.IMedicalTreatProjectService;
import com.ai.modules.config.service.IMedicalYbDrugService;
import com.ai.modules.engine.mapper.QueryTaskBatchBreakRuleMapper;
import com.ai.modules.engine.service.api.IApiQueryTaskBatchRuleService;
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

//@Service
public class QueryTaskBatchRuleServiceImpl implements IApiQueryTaskBatchRuleService {

	@Autowired
    private QueryTaskBatchBreakRuleMapper queryRuleMapper;
	@Autowired
	private IMedicalRuleConfigService ruleSV;
	@Autowired
	private IMedicalRuleConditionSetService ruleConditionSV;
	@Autowired
	private IMedicalStdAtcService atcSV;
	@Autowired
	private IMedicalDrugService drugSV;
	@Autowired
	private IMedicalTreatProjectService treatSV;
	@Autowired
	private IMedicalEquipmentService equipmentSV;
	@Autowired
	private IMedicalDruguseService druguseService;
	@Autowired
	private IMedicalDruguseRuleGroupService druguseRuleGroupService;
	@Autowired
	private IMedicalRuleEngineService ruleEngineService;
	@Autowired
	private IMedicalYbDrugService drugrepeatService;
	
	@Override
	public List<MedicalRuleConfig> queryMedicalRuleConfig(String batchId, String stepType) {
		QryMedicalRuleConfigDTO dto = new QryMedicalRuleConfigDTO();
		dto.setBatchId(batchId);
		dto.setRuleType(stepType);
		List<MedicalRuleConfig> ruleList = queryRuleMapper.queryMedicalRuleConfigByBatchid(dto);
		return ruleList;
	}
	
	@Override
	public List<MedicalRuleConfig> queryMedicalRuleConfigFail(String batchId, String stepType) {
		QryMedicalRuleConfigDTO dto = new QryMedicalRuleConfigDTO();
		dto.setBatchId(batchId);
		dto.setRuleType(stepType);
		List<MedicalRuleConfig> ruleList = queryRuleMapper.queryMedicalRuleConfigFail(dto);
		return ruleList;
	}

	@Override
	public List<MedicalRuleConditionSet> queryMedicalRuleConditionSet(String ruleId) {
		List<MedicalRuleConditionSet> ruleConditionList = ruleConditionSV.list(new QueryWrapper<MedicalRuleConditionSet>().eq("RULE_ID", ruleId));
		return ruleConditionList;
	}

	@Override
	public MedicalRuleConfig queryMedicalRuleConfig(String ruleId) {
		MedicalRuleConfig rule = ruleSV.getOne(new QueryWrapper<MedicalRuleConfig>().eq("RULE_ID", ruleId));
    	return rule;
	}

	@Override
	public List<MedicalDrugRule> queryMedicalDrugRule(String batchId, String stepType) {
		 QryMedicalDrugRuleDTO whereDTO = new QryMedicalDrugRuleDTO();
         whereDTO.setBatchId(batchId);
         whereDTO.setRuleType(stepType);
         List<MedicalDrugRule> ruleList = queryRuleMapper.queryMedicalDrugRule(whereDTO);
         return ruleList;
	}

	@Override
	public List<MedicalDrugRule> queryMedicalDrugRuleFail(String batchId, String stepType) {
		QryMedicalDrugRuleDTO whereDTO = new QryMedicalDrugRuleDTO();
        whereDTO.setBatchId(batchId);
        whereDTO.setRuleType(stepType);
        List<MedicalDrugRule> ruleList = queryRuleMapper.queryMedicalDrugRuleFail(whereDTO);
        return ruleList;
	}

	@Override
	public List<MedicalDrugRule> queryMedicalDrugRuleByRuleid(String ruleId) {
		List<MedicalDrugRule> ruleList = queryRuleMapper.queryMedicalDrugRuleByRuleid(ruleId);
		return ruleList;
	}

	@Override
	public String getDrugname(String itemcode) {
		if(itemcode.startsWith("869")) {
			MedicalDrug drug = drugSV.getOne(new QueryWrapper<MedicalDrug>().eq("CODE", itemcode));
			if(drug!=null) {
				return drug.getName();
			}
		} else {
			MedicalStdAtc atc = atcSV.getOne(new QueryWrapper<MedicalStdAtc>().eq("CODE", itemcode));
			if(atc!=null) {
				return atc.getName();
			}
		}
    	return null;
	}

	@Override
	public List<MedicalDrugRule> queryMedicalDrugRuleByItem(String batchId, String stepType, String itemCode) {
		QryMedicalDrugRuleDTO whereDTO = new QryMedicalDrugRuleDTO();
        whereDTO.setBatchId(batchId);
        whereDTO.setRuleType(stepType);
        whereDTO.setItemCode(itemCode);
        List<MedicalDrugRule> ruleList = queryRuleMapper.queryMedicalDrugRuleByItem(whereDTO);
        return ruleList;
	}

	@Override
	public String getTreatname(String itemcode) {
		MedicalTreatProject treat = treatSV.getOne(new QueryWrapper<MedicalTreatProject>().eq("CODE", itemcode));
		if(treat!=null) {
			return treat.getName();
		} else {
			MedicalEquipment equipment = equipmentSV.getOne(new QueryWrapper<MedicalEquipment>().eq("PRODUCTCODE", itemcode));
			if(equipment!=null) {
				return equipment.getProductname();
			}
		}
    	return null;
	}

	@Override
	public List<MedicalDruguse> queryMedicalDruguseByBatchid(String batchId) {
		return queryRuleMapper.queryMedicalDruguseByBatchid(batchId);
	}

	@Override
	public List<MedicalDruguse> queryMedicalDruguseFail(String batchId) {
		return queryRuleMapper.queryMedicalDruguseFail(batchId);
	}

	@Override
	public MedicalDruguse queryMedicalDruguse(String ruleId) {
		return druguseService.getById(ruleId);
	}

	@Override
	public List<MedicalDruguseRuleGroup> queryMedicalDruguseRuleGroup(String ruleId) {
		List<MedicalDruguseRuleGroup> ruleGroups = druguseRuleGroupService.list(new QueryWrapper<MedicalDruguseRuleGroup>().eq("RULE_ID", ruleId));
		return ruleGroups;
	}

	@Override
	public boolean existsMedicalRuleEngine(String ruleId, String ruleType) {
		int count = ruleEngineService.count(new QueryWrapper<MedicalRuleEngine>()
        		.eq("RULE_ID", ruleId)
        		.eq("STATUS", MedicalConstant.STATUS_VALID)
        		.eq("RULE_TYPE", ruleType));
		return count>0;
	}

	@Override
	public List<MedicalYbDrug> queryMedicalDrugrepeat() {
		return drugrepeatService.list();
	}

	@Override
	public List<MedicalYbDrug> queryMedicalDrugrepeatFail(String batchId) {
		return queryRuleMapper.queryMedicalDrugrepeatFail(batchId);
	}

	@Override
	public List<MedicalYbDrug> queryMedicalDrugrepeat(String ruleId) {
		return drugrepeatService.list(new QueryWrapper<MedicalYbDrug>().eq("PARENT_CODE", ruleId));
	}
}
