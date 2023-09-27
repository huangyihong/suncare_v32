/**
 * EngineCaseServiceImpl.java	  V1.0   2020年1月16日 下午2:56:18
 * <p>
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 * <p>
 * Modification history(By    Time    Reason):
 * <p>
 * Description:
 */

package com.ai.modules.engine.service.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.engine.handle.cases.AbsCaseHandle;
import com.ai.modules.engine.handle.cases.CaseHandleFactory;
import com.ai.modules.engine.handle.secondary.cases.AbsCaseSecondHandle;
import com.ai.modules.engine.handle.secondary.cases.OutputDiagHandle;
import com.ai.modules.engine.model.EngineCntResult;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.model.RTimer;
import com.ai.modules.engine.model.dto.BatchItemDTO;
import com.ai.modules.engine.runnable.AbsEngineRunnable;
import com.ai.modules.engine.runnable.EngineCaseRunnable;
import com.ai.modules.engine.runnable.EngineItemRunnable;
import com.ai.modules.engine.runnable.EngineRuleConfigByBatchRunnable;
import com.ai.modules.engine.runnable.EngineRunnable;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.IEngineCaseService;
import com.ai.modules.engine.service.IEngineChargeService;
import com.ai.modules.engine.service.IEngineClinicalService;
import com.ai.modules.engine.service.IEngineDrugService;
import com.ai.modules.engine.service.IEngineDrugUseService;
import com.ai.modules.engine.service.IEngineGreenplumService;
import com.ai.modules.engine.service.IEngineRuleService;
import com.ai.modules.engine.service.IEngineService;
import com.ai.modules.engine.service.IEngineTreatService;
import com.ai.modules.engine.service.api.IApiCaseService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineCaseServiceImpl implements IEngineCaseService {
	
	private static String[] CASE_FIELDS = new String[] {
    		"VISITID", "CLIENTID", "ID_NO", "INSURANCETYPE", "CLIENTNAME", "SEX_CODE", "SEX", "SEX_CODE_SRC", "SEX_SRC",
            "BIRTHDAY", "YEARAGE", "MONTHAGE", "DAYAGE", "VISITTYPE_ID", "VISITTYPE", "VISITTYPE_ID_SRC", "VISITTYPE_SRC", "VISITDATE", "ORGID", "ORGNAME", "ORGID_SRC", "ORGNAME_SRC",
            "HOSPLEVEL", "HOSPGRADE", "DEPTID", "DEPTNAME", "DEPTID_SRC", "DEPTNAME_SRC", "DOCTORID", "DOCTORNAME", "TOTALFEE", "LEAVEDATE",
            "DISEASECODE", "DISEASENAME", "DISEASECODE_SRC", "DISEASENAME_SRC", "VISITID_DUMMY", "YB_VISITID", "HIS_VISITID", "VISITID_CONNECT",
            "ZY_DAYS", "ZY_DAYS_CALCULATE", "FUNDPAY", "FUN_SETTLEWAY_ID", "FUN_SETTLEWAY_NAME", "DATA_RESOUCE_ID", "DATA_RESOUCE", "ETL_SOURCE", "ETL_SOURCE_NAME", "ETL_TIME"};
	
	public static final Map<String, String> CASE_FIELD_MAPPING = new LinkedHashMap<String, String>();
	static {
		//key=结果表字段名, value=dwb_master_info表字段名
		for (String field : CASE_FIELDS) {
			CASE_FIELD_MAPPING.put(field, field);
		}
		//病案号
		CASE_FIELD_MAPPING.put("MEDICAL_NO", "CASE_ID");
	}
	
    @Autowired
    private IEngineService engineService;
	@Autowired
    private IApiTaskService taskSV;
    @Autowired
    private IEngineDrugService drugService;
    @Autowired
    private IEngineChargeService chargeService;
    @Autowired
    private IEngineTreatService treatService;
    @Autowired
    private IEngineClinicalService clinicalService;
	@Autowired
    private IApiCaseService caseSV;
    @Autowired
    private IEngineDrugUseService engineDrugUseService;
    @Autowired
    private IEngineRuleService ruleSV;
    @Autowired
	private IEngineActionService engineActionService;
    @Autowired
    private IApiDictService dictSV;
    @Autowired
    private IEngineGreenplumService greenplumService;
    @Value("${engine.async}")
	private boolean async;

    @Override
    public void generateUnreasonableAction(String batchId) {
    	RTimer rtimer = new RTimer();
    	SimpleDateFormat df = new SimpleDateFormat("H:mm:ss.SSS", Locale.getDefault());
    	df.setTimeZone(TimeZone.getTimeZone("UTC"));

        boolean success = true;
        String error = "出现异常";
        Set<String> stepTypes = new HashSet<String>();
        stepTypes.add(MedicalConstant.RULE_TYPE_CASE);
        stepTypes.add(MedicalConstant.RULE_TYPE_NEWCASE);
        try {
        	TaskBatchStepItem step = new TaskBatchStepItem();
            step.setUpdateTime(new Date());
            step.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            taskSV.updateTaskBatchStepItem(batchId, stepTypes, step);

            TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
            if (batch == null) {
                throw new RuntimeException("未找到任务批次");
            }
            TaskProject task = taskSV.findTaskProject(batch.getProjectId());
            if (task == null) {
                throw new RuntimeException("未找到项目");
            }
        	//先导出已审核的不合规行为结果
            EngineCntResult cntResult = null;
			if(!HiveJDBCUtil.enabledStorageGp()) {
				cntResult = engineActionService.importApprovalAction(batchId, MedicalConstant.ENGINE_BUSI_TYPE_CASE);
	        	//删除历史数据
	            String where = "BUSI_TYPE:%s AND BATCH_ID:%s";
	            where = String.format(where, MedicalConstant.ENGINE_BUSI_TYPE_CASE, batchId);
	        	SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
			} else {
				//删除gp
	        	greenplumService.remove(batchId, MedicalConstant.ENGINE_BUSI_TYPE_CASE);
			}

        	Map<String, EngineResult> caseResultMap = new HashMap<String, EngineResult>();
            //List<TaskBatchBreakRule> batchRuleList = taskBatchRuleMapper.selectList(new QueryWrapper<TaskBatchBreakRule>().eq("BATCH_ID", batchId).eq("RULE_TYPE", "01"));
        	List<HisMedicalFormalCase> caseList = caseSV.findHisMedicalFormalCase(batchId);
        	log.info("批次模型数量:"+caseList.size());
            //遍历模型
        	for (HisMedicalFormalCase formalCase : caseList) {
                //按每一个模型跑不合理行为病例数据
                EngineResult res = this.generate(task, batch, formalCase);
                success = success && res.isSuccess();
                caseResultMap.put(formalCase.getCaseId(), res);
            }

        	if(!HiveJDBCUtil.enabledStorageGp()) {
	    		if(cntResult.isSuccess() && cntResult.getCount()>0) {
	    			//最后导入已审核的不合规行为结果
	    			SolrUtil.importJsonToSolrNotDeleteFile(cntResult.getMessage(), EngineUtil.MEDICAL_UNREASONABLE_ACTION);
	    		}
        	} else {
        		//回填gp
        		greenplumService.backFill(batchId, MedicalConstant.ENGINE_BUSI_TYPE_CASE);
        	}
        } catch(Exception e) {
        	success = false;
        	error = e.getMessage();
        	log.error("", e);
        } finally {
        	TaskBatchStepItem step = new TaskBatchStepItem();
            step.setUpdateTime(new Date());
            step.setStatus(success? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
            if (!success) {
                error = error.length() > 2000 ? error.substring(0, 2000) : error;
                step.setMsg(error);
            }
            taskSV.updateTaskBatchStepItem(batchId, stepTypes, step);
        }
        log.info(batchId+"批次模型总耗时:" + df.format(new Date((long)rtimer.getTime())));
    }

    @Override
    public void generateUnreasonableAction(String batchId, String itemCode) {

    }

    private EngineResult generate(TaskProject task, TaskProjectBatch batch, HisMedicalFormalCase formalCase) {
        Set<String> stepTypes = new HashSet<String>();
        stepTypes.add(MedicalConstant.RULE_TYPE_CASE);
        stepTypes.add(MedicalConstant.RULE_TYPE_NEWCASE);
        boolean success = true;
        String error = "";
        EngineResult res = EngineResult.ok();
        try {
        	//更新模型任务状态
            TaskBatchBreakRuleDel entity = new TaskBatchBreakRuleDel();
            entity.setStartTime(new Date());
            entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            taskSV.updateTaskBatchBreakRuleDel(batch.getBatchId(), stepTypes, formalCase.getCaseId(), entity);

            //计算引擎
            CaseHandleFactory factory = new CaseHandleFactory(task.getDataSource(), task, batch, formalCase);
            AbsCaseHandle handle = factory.build();
            res = handle.generateUnreasonableAction();       
            if(!HiveJDBCUtil.enabledProcessGp()) {
            	//满足条件的诊断信息
                List<EngineNodeRule> ruleList = engineService.queryHisFormalEngineNodeRule(formalCase.getCaseId(), batch.getBatchId());
                AbsCaseSecondHandle secondHandle = new OutputDiagHandle(formalCase, ruleList);
                secondHandle.execute();
                
                //不合规行为汇总
                String[] fqs = new String[] {"CASE_ID:"+formalCase.getCaseId()};
                engineActionService.executeGroupBy(batch.getBatchId(), formalCase.getActionId(), fqs);
            }
        } catch(InvocationTargetException e) {
        	log.error("", e);
        	error = e.getTargetException().getMessage();
        	success = false;
        	res.setSuccess(success);
        } catch(Exception e) {
        	log.error("", e);
        	error = e.getMessage();
        	success = false;
        	res.setSuccess(success);
        } finally {
        	TaskBatchBreakRuleDel entity = new TaskBatchBreakRuleDel();
            if (!success) {
                entity.setStatus(MedicalConstant.RUN_STATE_ABNORMAL);
                error = error!=null&&error.length() > 2000 ? error.substring(0, 2000) : error;
                entity.setErrorMsg(error);
            } else {
                entity.setStatus(MedicalConstant.RUN_STATE_NORMAL);
                entity.setRecordNum(res.getCount());
                entity.setObjectNum(res.getObjectCount());
                entity.setTotalAcount(res.getMoney());
                entity.setActionMoney(res.getActionMoney());
                entity.setErrorMsg("执行成功");
            }
            entity.setEndTime(new Date());
            taskSV.updateTaskBatchBreakRuleDel(batch.getBatchId(), stepTypes, formalCase.getCaseId(), entity);
        }

        return res;
    }

    @Override
    public void generateMedicalUnreasonableAction(String batchId, String caseId) throws Exception {
        Set<String> stepTypes = new HashSet<String>();
        stepTypes.add(MedicalConstant.RULE_TYPE_CASE);
        stepTypes.add(MedicalConstant.RULE_TYPE_NEWCASE);
        try {
        	TaskBatchStepItem step = new TaskBatchStepItem();
            step.setUpdateTime(new Date());
            step.setStartTime(new Date());
            step.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            taskSV.updateTaskBatchStepItem(batchId, stepTypes, step);

        	TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
            if (batch == null) {
                throw new RuntimeException("未找到任务批次");
            }
            TaskProject task = taskSV.findTaskProject(batch.getProjectId());
            if (task == null) {
                throw new RuntimeException("未找到项目");
            }

            //先导出已审核的不合规行为结果
            EngineCntResult cntResult = null;
            if(!HiveJDBCUtil.enabledStorageGp()) {
            	cntResult = engineActionService.importApprovalAction(batchId, MedicalConstant.ENGINE_BUSI_TYPE_CASE, caseId);
            	//删除历史数据
                String where = "BUSI_TYPE:%s AND BATCH_ID:%s AND CASE_ID:%s";
                where = String.format(where, MedicalConstant.ENGINE_BUSI_TYPE_CASE, batchId, caseId);
                SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
            } else {
            	//删除gp
                greenplumService.removeCase(batchId, caseId);
            }

			HisMedicalFormalCase formalCase = caseSV.findHisMedicalFormalCaseByCaseid(batchId, caseId);            
            this.generate(task, batch, formalCase);
            
            if(!HiveJDBCUtil.enabledStorageGp()) {
            	if(cntResult.isSuccess() && cntResult.getCount()>0) {
        			//最后回填已审核的不合规行为结果
        			SolrUtil.importJsonToSolr(cntResult.getMessage(), EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        		}
            } else {
            	//回填gp
                greenplumService.backFillCase(batchId, caseId);
            }            
        } catch (Exception e) {
            throw e;
        } finally {
            //设置批次中业务组内的模型进度
        	this.settingTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_CASE);
        	//设置批次中模型进度（新版）
        	this.settingTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_NEWCASE);
        }
    }

    private void settingTaskBatchStepItem(String batchId, String stepType) {
    	List<TaskBatchBreakRuleDel> caseList = taskSV.findTaskBatchBreakRuleDel(batchId, stepType);
        if(caseList!=null && caseList.size()>0) {
        	Map<String, List<TaskBatchBreakRuleDel>> caseMap = caseList.stream().collect(Collectors.groupingBy(TaskBatchBreakRuleDel::getStatus));
            if (caseMap.containsKey(MedicalConstant.RUN_STATE_WAIT)) {
                //存在等待的任务
            } else if (caseMap.containsKey(MedicalConstant.RUN_STATE_ABNORMAL)) {
                //存在失败的任务
            	TaskBatchStepItem step = new TaskBatchStepItem();
                step.setUpdateTime(new Date());
                step.setEndTime(new Date());
                step.setStatus(MedicalConstant.RUN_STATE_ABNORMAL);
                taskSV.updateTaskBatchStepItem(batchId, stepType, step);
            } else {
            	TaskBatchStepItem step = new TaskBatchStepItem();
                step.setUpdateTime(new Date());
                step.setEndTime(new Date());
                step.setStatus(MedicalConstant.RUN_STATE_NORMAL);
                taskSV.updateTaskBatchStepItem(batchId, stepType, step);
            }
        }
    }

    @Override
    public EngineResult generateUnreasonableDrugAction(String batchId, String ruleType, String itemCode) {
        String datasource = SolrUtil.getLoginUserDatasource();
        if(MedicalConstant.ENGINE_BUSI_TYPE_CASE.equals(ruleType)){
            //不合规模型
            ThreadUtils.THREAD_CASE_POOL.add(new EngineItemRunnable(datasource, batchId, itemCode, this));
        } else if(MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL.equals(ruleType)){
            //不合规临床路径
            ThreadUtils.THREAD_CASE_POOL.add(new EngineItemRunnable(datasource, batchId, itemCode, clinicalService));
        } else if(MedicalConstant.ENGINE_BUSI_TYPE_DRUG.equals(ruleType)){
            //药品不合规
        	drugService.generateUnreasonableActionByThreadPool(batchId, itemCode);
        } else if(MedicalConstant.ENGINE_BUSI_TYPE_CHARGE.equals(ruleType)){
            //收费不合规
        	chargeService.generateUnreasonableActionByThreadPool(batchId, itemCode);
        } else if(MedicalConstant.ENGINE_BUSI_TYPE_TREAT.equals(ruleType)){
            //诊疗不合理
        	treatService.generateUnreasonableActionByThreadPool(batchId, itemCode);
        } else if(MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE.equals(ruleType)
        		|| MedicalConstant.ENGINE_BUSI_TYPE_DRUGREPEAT.equals(ruleType)){
            //用药不合理或重复用药
            engineDrugUseService.generateUnreasonableActionByThreadPool(batchId, itemCode);
        } else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE.equals(ruleType)){
            //新版收费不合规
        	ruleSV.generateUnreasonableActionByThreadPool(batchId, itemCode);
        } else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWTREAT.equals(ruleType)){
            //新版诊疗不合理
        	ruleSV.generateUnreasonableActionByThreadPool(batchId, itemCode);
        } else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWDRUG.equals(ruleType)){
            //新版药品不合规
        	ruleSV.generateUnreasonableActionByThreadPool(batchId, itemCode);
        }
        EngineResult result = EngineResult.ok();
        result.setMessage("本批次任务正在处理中...");
        return result;
    }
    
    @Override
    public EngineResult generateUnreasonableRuleAction(List<BatchItemDTO> itemList) {
        for(BatchItemDTO item : itemList) {
        	String batchId = item.getBatchId();
        	String ruleType = item.getItemType();
        	String itemCode = item.getItemId();
        	if(MedicalConstant.ENGINE_BUSI_TYPE_DRUG.equals(ruleType)){
                //药品不合规
            	drugService.generateUnreasonableActionByThreadPool(batchId, itemCode);
            } else if(MedicalConstant.ENGINE_BUSI_TYPE_CHARGE.equals(ruleType)){
                //收费不合规
            	chargeService.generateUnreasonableActionByThreadPool(batchId, itemCode);
            } else if(MedicalConstant.ENGINE_BUSI_TYPE_TREAT.equals(ruleType)){
                //诊疗不合理
            	treatService.generateUnreasonableActionByThreadPool(batchId, itemCode);
            } else if(MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE.equals(ruleType)
            		|| MedicalConstant.ENGINE_BUSI_TYPE_DRUGREPEAT.equals(ruleType)){
                //用药不合理或重复用药
                engineDrugUseService.generateUnreasonableActionByThreadPool(batchId, itemCode);
            } else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE.equals(ruleType)){
                //新版收费不合规
            	ruleSV.generateUnreasonableActionByThreadPool(batchId, itemCode);
            } else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWTREAT.equals(ruleType)){
                //新版诊疗不合理
            	ruleSV.generateUnreasonableActionByThreadPool(batchId, itemCode);
            } else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWDRUG.equals(ruleType)){
                //新版药品不合规
            	ruleSV.generateUnreasonableActionByThreadPool(batchId, itemCode);
            }
        }
        EngineResult result = EngineResult.ok();
        result.setMessage("本批次任务正在处理中...");
        return result;
    }

    @Override
    public EngineResult generateUnreasonableActionAll(String batchId, Set<String> ruleTypeSet) {
        initStep(batchId, ruleTypeSet);
        String datasource = SolrUtil.getLoginUserDatasource();
        if(async) {
            //调用cmd命令执行任务，-serial 20101016164021 -f EngineJobHandler -ds funan -pc 1873497fddcbed501aa07dd4c16e5f46
            String cmd = "taskrun.sh -serial %s -f EngineJobHandler -ds %s -pc %s";
            cmd = String.format(cmd, DateUtils.formatDate(new Date(), "yyyyMMddHHmmss"), datasource, batchId);
            log.info("cmd:{}", cmd);
            try {
                Process pro = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
            } catch (IOException e) {
                log.error("", e);
            }

        } else {
            if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_CASE)
            		|| ruleTypeSet.contains(MedicalConstant.RULE_TYPE_NEWCASE)){
                //不合规模型
                ThreadUtils.THREAD_CASE_POOL.add(new EngineRunnable(datasource, batchId, this));
            }
            if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_CLINICAL_NEW)){
                //不合规临床路径
                ThreadUtils.THREAD_CASE_POOL.add(new EngineRunnable(datasource, batchId, clinicalService));
            }
            if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_DRUG)){
                //药品不合规
                ThreadUtils.THREAD_DRUG_POOL.add(new EngineRunnable(datasource, batchId, drugService));
            }
            if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_CHARGE)){
                //收费不合规
                ThreadUtils.THREAD_DRUG_POOL.add(new EngineRunnable(datasource, batchId, chargeService));
            }
            if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_TREAT)){
                //诊疗不合理
                ThreadUtils.THREAD_DRUG_POOL.add(new EngineRunnable(datasource, batchId, treatService));
            }
            if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_DRUGUSE)){
                //用药不合理
                ThreadUtils.THREAD_DRUG_POOL.add(new EngineRunnable(datasource, batchId, engineDrugUseService));
            }
            if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_NEWCHARGE)){
                //新版收费不合规
            	AbsEngineRunnable runnable = new EngineRuleConfigByBatchRunnable(datasource, batchId, MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE);
                ThreadUtils.THREAD_DRUG_POOL.add(runnable);
            }

            if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_NEWTREAT)){
                //新版诊疗不合理
            	AbsEngineRunnable runnable = new EngineRuleConfigByBatchRunnable(datasource, batchId, MedicalConstant.ENGINE_BUSI_TYPE_NEWTREAT);
                ThreadUtils.THREAD_DRUG_POOL.add(runnable);
            }

            if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_NEWDRUG)){
                //新版药品不合规
            	AbsEngineRunnable runnable = new EngineRuleConfigByBatchRunnable(datasource, batchId, MedicalConstant.ENGINE_BUSI_TYPE_NEWDRUG);
                ThreadUtils.THREAD_DRUG_POOL.add(runnable);
            }
        }

        EngineResult result = EngineResult.ok();
        result.setMessage("本批次任务正在处理中...");
        return result;
    }

    @Override
    public void generateUnreasonableActionByBatch(String batchId, Set<String> ruleTypeSet) {
        initStep(batchId, ruleTypeSet);
        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_CASE)
        		|| ruleTypeSet.contains(MedicalConstant.RULE_TYPE_NEWCASE)){
            try {
                //不合规模型
                generateUnreasonableAction(batchId);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_CLINICAL_NEW)){
            //临床路径
            try {
                clinicalService.generateUnreasonableAction(batchId);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_DRUG)){
            //药品不合规
            try {
                drugService.generateUnreasonableAction(batchId);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_CHARGE)){
            //收费不合规
            try {
                chargeService.generateUnreasonableAction(batchId);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_TREAT)){
            //诊疗不合理
            try {
                treatService.generateUnreasonableAction(batchId);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_DRUGUSE)){
            //用药不合理
            try {
                engineDrugUseService.generateUnreasonableAction(batchId);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_NEWCHARGE)){
            //新版收费不合规
            try {
            	ruleSV.generateUnreasonableAction(batchId, MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE);
            } catch (Exception e) {
                log.error("", e);
            }
        }

        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_NEWTREAT)){
            //新版诊疗不合理
            try {
            	ruleSV.generateUnreasonableAction(batchId, MedicalConstant.ENGINE_BUSI_TYPE_NEWTREAT);
            } catch (Exception e) {
                log.error("", e);
            }
        }

        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_NEWDRUG)){
            //新版药品不合规
            try {
            	ruleSV.generateUnreasonableAction(batchId, MedicalConstant.ENGINE_BUSI_TYPE_NEWDRUG);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    @Override
    public void initStep(String batchId, Set<String> ruleTypes) {
		/*QueryWrapper<TaskBatchStepItem> wrapper = new QueryWrapper<TaskBatchStepItem>()
		        .eq("BATCH_ID", batchId)
		        .eq("STEP", 1);
		stepItemService.remove(wrapper);*/
    	for(String itemId : ruleTypes) {
    		boolean canRun = taskSV.canRun(batchId, itemId);
    		if(!canRun) {
    			throw new RuntimeException("任务批次正在运行中");
    		}
    	}
        taskSV.removeTaskBatchStepItem(batchId);

        Date now = new Date();
        String status = MedicalConstant.RUN_STATE_WAIT;
        List<TaskBatchStepItem> stepList = ruleTypes.stream().map(type -> {
            TaskBatchStepItem step = new TaskBatchStepItem();
            step.setId(UUIDGenerator.generate());
            step.setBatchId(batchId);
            step.setStep(1);
            step.setItemId(type);
            step.setCreateTime(now);
            step.setStatus(status);
            return step;
        }).collect(Collectors.toList());
        taskSV.saveTaskBatchStepItem(stepList);
    }

    @Override
    public void generateMedicalUnreasonableActionByThreadPool(String batchId, String busiId, String caseId) {
    	String datasource = SolrUtil.getLoginUserDatasource();
    	EngineCaseRunnable runnable = new EngineCaseRunnable(datasource);
    	runnable.setBatchId(batchId);
    	runnable.setCaseId(caseId);
    	ThreadUtils.THREAD_CASE_POOL.add(runnable);
    }      
}
