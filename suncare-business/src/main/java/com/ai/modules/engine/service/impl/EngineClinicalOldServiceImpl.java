/**
 * EngineDrugServiceImpl.java	  V1.0   2020年1月2日 上午11:07:02
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.MedicalConstant;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.ai.modules.engine.mapper.QueryTaskBatchBreakRuleMapper;
import com.ai.modules.engine.model.dto.ActionTypeDTO;
import com.ai.modules.engine.model.rule.AbsEngineParamRule;
import com.ai.modules.engine.model.rule.EngineParamAgeRule;
import com.ai.modules.engine.model.rule.EngineParamClinicalProjRule;
import com.ai.modules.engine.model.rule.EngineParamRangeRule;
import com.ai.modules.engine.model.rule.EngineParamRule;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.IEngineClinicalOldService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineClinicalOldServiceImpl implements IEngineClinicalOldService {
	@Autowired
    private IApiTaskService taskSV;
	@Autowired
	private QueryTaskBatchBreakRuleMapper queryRuleMapper;
	@Autowired
    private IApiDictService dictSV;
	@Autowired
	private IEngineActionService engineActionService;

	@Override
	public void generateMedicalUnreasonableClinicalAction(TaskProject task, TaskProjectBatch batch, String itemCode, List<MedicalDrugRule> ruleList) throws Exception {
		List<String> conditionList = new ArrayList<String>();
		for(MedicalDrugRule rule : ruleList) {
			List<String> tempList = this.parseClinicalRuleCondition(rule);
			if(!tempList.isEmpty()) {
				conditionList.addAll(tempList);
			}
		}
		conditionList.add("ITEMCODE:"+itemCode);
		if (StringUtils.isNotBlank(batch.getEtlSource())) {
            conditionList.add("ETL_SOURCE:" + batch.getEtlSource());
        }
		
		// 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/" + EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION + "/" + batch.getBatchId() + "/" + itemCode + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");
        ActionTypeDTO dto = this.getActionTypeDTO();
        Map<String, MedicalActionDict> actionDictMap = dictSV.queryActionDict();
        SolrUtil.exportByPager(conditionList, EngineUtil.DWB_CHARGE_DETAIL, (map, index) -> {
            // 循环一条数据写入文件
        	engineActionService.writerJson(fileWriter, map, task, batch, itemCode, ruleList, null, dto, actionDictMap);
        });
        // 文件尾
        fileWriter.write("]");
        fileWriter.close();
        
        //导入solr
      	SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION);
	}

	@Override
	public void generateMedicalUnreasonableClinicalAction(String batchId) throws Exception {
		boolean success = true;
		String error = null;
		try {
			TaskBatchStepItem entity = new TaskBatchStepItem();
			entity.setUpdateTime(new Date());
			entity.setStartTime(new Date());
			entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
			taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_CLINICAL, entity);

			TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
			if(batch==null) {
				throw new RuntimeException("未找到任务批次");
			}
			TaskProject task = taskSV.findTaskProject(batch.getProjectId());
			if(task==null) {
				throw new RuntimeException("未找到项目");
			}
			//删除历史solr数据
			engineActionService.deleteSolr(batchId, "3", false);
			List<MedicalDrugRule> drugRuleList = queryRuleMapper.queryMedicalClinicalRuleByBatchid(batchId);
			if(drugRuleList!=null && drugRuleList.size()>0) {
				//按疾病分组
				Map<String, List<MedicalDrugRule>> drugRuleGroupMap = drugRuleList.stream().collect(Collectors.groupingBy(MedicalDrugRule::getDrugCode));
				//遍历疾病
				for(Map.Entry<String, List<MedicalDrugRule>> entry : drugRuleGroupMap.entrySet()) {
					try {
						this.generateMedicalUnreasonableClinicalAction(task, batch, entry.getKey(), entry.getValue());
					} catch(Exception e) {
						log.error("", e);
						success = false;
						error = error + "\n" + e.getMessage();
					}
				}
			}
		} catch(Exception e) {
			success = false;
			error = e.getMessage();
			throw e;
		} finally {
			TaskBatchStepItem entity = new TaskBatchStepItem();
			entity.setUpdateTime(new Date());
			entity.setEndTime(new Date());
			entity.setStatus(success? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
			entity.setMsg(success? null : error);
			taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_CLINICAL, entity);
		}
	}

	@Override
	public List<String> parseClinicalRuleCondition(MedicalDrugRule rule) {
		List<AbsEngineParamRule> list = new ArrayList<AbsEngineParamRule>();
		if(StringUtils.isNotBlank(rule.getAge())) {
			//年龄
			AbsEngineParamRule paramRule = new EngineParamAgeRule(EngineUtil.DWB_MASTER_INFO, "YEARAGE", rule.getAge());
			list.add(paramRule);
		}
		if(StringUtils.isNotBlank(rule.getSex())) {
			//性别
			AbsEngineParamRule paramRule = new EngineParamRule(EngineUtil.DWB_MASTER_INFO, "SEX_CODE", rule.getSex());
			list.add(paramRule);
		}
		if(StringUtils.isNotBlank(rule.getJzlx())) {
			//就医方式
			AbsEngineParamRule paramRule = new EngineParamRule(EngineUtil.DWB_MASTER_INFO, "VISITTYPE_ID", rule.getJzlx());
			list.add(paramRule);
		}
		if(StringUtils.isNotBlank(rule.getYblx())) {
			//参保类型
			AbsEngineParamRule paramRule = new EngineParamRule(EngineUtil.DWB_MASTER_INFO, "INSURANCETYPE", rule.getYblx());
			list.add(paramRule);
		}
		if(StringUtils.isNotBlank(rule.getYyjb())) {
			//医院级别
			AbsEngineParamRule paramRule = new EngineParamRule(EngineUtil.DWB_MASTER_INFO, "HOSPLEVEL", rule.getYyjb());
			list.add(paramRule);
		}
		if(StringUtils.isNotBlank(rule.getOffice())) {
			//科室
			AbsEngineParamRule paramRule = new EngineParamRule(EngineUtil.DWB_MASTER_INFO, "DEPTID", rule.getOffice());
			list.add(paramRule);
		}
		if(StringUtils.isNotBlank(rule.getInhospitalMin())
				|| StringUtils.isNotBlank(rule.getInhospitalMax())) {
			//住院天数
			AbsEngineParamRule paramRule = new EngineParamRangeRule(EngineUtil.DWB_MASTER_INFO, "ZY_DAYS", rule.getInhospitalMin(), rule.getInhospitalMax());
			list.add(paramRule);
		}

		//治疗项目字典
		List<MedicalDictItemVO> dictList = dictSV.queryMedicalDictByKind("1");
		//治疗项目字典分组
		Map<String, List<MedicalDictItemVO>> dictGroupMap = dictList.stream().collect(Collectors.groupingBy(MedicalDictItemVO::getGroupId));
		if(StringUtils.isNotBlank(rule.getClinicProjects())) {
			//诊疗项目
			AbsEngineParamRule paramRule = new EngineParamClinicalProjRule("ITEMCODE", rule.getClinicProjects(), dictGroupMap);
			list.add(paramRule);
		}

		List<String> conditionList = new ArrayList<String>();
		for(AbsEngineParamRule bean : list) {
			String condition = bean.where();
			if(condition!=null) {
				conditionList.add(condition);
			}
		}
		//conditionList.add("ITEMCODE:"+rule.getDrugCode());
		log.info("fq:" + StringUtils.join(conditionList, ","));
		return conditionList;
	}
	
	private ActionTypeDTO getActionTypeDTO() {
    	String busiType = MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL;
    	ActionTypeDTO dto = new ActionTypeDTO();
        dto.setActionTypeId(busiType);
        dto.setActionTypeName(dictSV.queryDictTextByKey("ACTION_TYPE", busiType));
        return dto;
    }
}
