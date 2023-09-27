package com.ai.modules.task.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai.common.MedicalConstant;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.MedicalExportTask;
import com.ai.modules.config.service.IMedicalExportTaskService;
import com.ai.modules.engine.service.IEngineBehaviorService;
import com.ai.modules.engine.service.IEngineCaseService;
import com.ai.modules.engine.service.IEngineChargeService;
import com.ai.modules.engine.service.IEngineClinicalService;
import com.ai.modules.engine.service.IEngineDrugService;
import com.ai.modules.engine.service.IEngineDrugUseService;
import com.ai.modules.engine.service.IEngineRuleService;
import com.ai.modules.engine.service.IEngineTreatService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.task.dto.TaskBatchExecInfo;
import com.ai.modules.task.entity.TaskAsyncActionLog;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskAsyncActionLogService;
import com.ai.modules.task.service.ITaskBatchBreakRuleDelService;
import com.ai.modules.task.service.ITaskBatchBreakRuleLogService;
import com.ai.modules.task.service.ITaskBatchBreakRuleService;
import com.ai.modules.task.service.ITaskBatchStepItemService;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.ai.modules.task.service.ITaskProjectService;
import com.ai.modules.task.vo.TaskBatchStepItemVO;
import com.ai.modules.task.vo.TaskProjectBatchVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 任务项目批次
 * @Author: jeecg-boot
 * @Date: 2020-01-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "任务项目批次")
@RestController
@RequestMapping("/task/taskProjectBatch")
public class TaskProjectBatchController extends JeecgController<TaskProjectBatch, ITaskProjectBatchService> {
    @Autowired
    private ITaskProjectBatchService taskProjectBatchService;

    @Autowired
    private ITaskBatchStepItemService taskBatchStepItemService;

    @Autowired
    private ITaskProjectService taskProjectService;

    @Autowired
    private IEngineCaseService engineCaseService;

    @Autowired
    private IEngineClinicalService engineClinicalService;

    @Autowired
    private IEngineDrugUseService engineDrugUseService;

    @Autowired
    private IEngineRuleService engineRuleService;


    @Autowired
    private ITaskBatchBreakRuleService taskBatchBreakRuleService;

    @Autowired
    private ITaskBatchBreakRuleDelService taskBatchBreakRuleDelService;

    @Autowired
    private IMedicalExportTaskService medicalExportTaskService;

    @Autowired
    private ITaskAsyncActionLogService taskAsyncActionLogService;

    @Autowired
    private IApiTaskService taskSV;

    @Autowired
    private ITaskBatchBreakRuleLogService ruleLogService;

    @Value("${engine.async}")
    private boolean async;


    @AutoLog(value = "任务项目批次-执行单个批次模型")
    @ApiOperation(value = "任务项目批次-执行单个批次模型", notes = "任务项目批次-执行单个批次模型")
    @PostMapping(value = "/deleteCase")
    public Result<?> deleteCase(String detailIds) throws Exception {

        Collection<TaskBatchBreakRuleDel> list = taskBatchBreakRuleDelService.listByIds(Arrays.asList(detailIds.split(",")));
        List<String> ids = list.stream().map(TaskBatchBreakRuleDel::getId).collect(Collectors.toList());
        // 更新状态为已删除
        TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
        bean.setStatus(MedicalConstant.RUN_STATE_DELETED);
        bean.setRecordNum(0);
        bean.setTotalAcount(new BigDecimal(0));
        bean.setActionMoney(new BigDecimal(0));

        taskBatchBreakRuleDelService.update(bean,
                new QueryWrapper<TaskBatchBreakRuleDel>().in("ID", ids));

        return Result.ok(list);
    }
    /**
     * 执行批次任务
     *
     * @param batchId
     * @return
     */
    @AutoLog(value = "任务项目批次-执行批次任务")
    @ApiOperation(value = "任务项目批次-执行批次任务", notes = "任务项目批次-执行批次任务")
    @PostMapping(value = "/execBatch")
    public Result<?> execBatch(@RequestParam(name = "batchId") String batchId) throws Exception {
        List<TaskBatchBreakRule> breakRuleList = this.taskBatchBreakRuleService.list(
                new QueryWrapper<TaskBatchBreakRule>()
                        .eq("BATCH_ID", batchId));
        if (breakRuleList.size() == 0) {
            throw new Exception("请选择至少一个规则");
        }
        Set<String> ruleTypeSet = breakRuleList.stream().map(TaskBatchBreakRule::getRuleType).collect(Collectors.toSet());

        // 创建或更新各个模型进度
        taskBatchBreakRuleDelService.save(batchId, breakRuleList);
        // 重新备份规则
        taskProjectBatchService.reBackHis(batchId);
        // 开始执行
//		 engineCaseService.generateUnreasonableActionAll(batchId, ruleTypeSet);

        // 更新状态,规则类型
        TaskProjectBatch taskProjectBatch = new TaskProjectBatch();
        taskProjectBatch.setBatchId(batchId);
        taskProjectBatch.setStep(MedicalConstant.BATCH_STEP_SYSTEM);
        taskProjectBatch.setRuleTypes(StringUtils.join(ruleTypeSet, ","));
        taskProjectBatchService.updateById(taskProjectBatch);
//		 return Result.ok("操作成功！");
        return Result.ok(ruleTypeSet);
    }

    /**
     * 执行单个批次模型
     *
     * @param batchId
     * @return
     */
    @AutoLog(value = "任务项目批次-执行单个批次模型")
    @ApiOperation(value = "任务项目批次-执行单个批次模型", notes = "任务项目批次-执行单个批次模型")
    @PostMapping(value = "/execCase")
    public Result<?> execCase(String detailIds, String batchId, String busiId, String caseId) throws Exception {
        Collection<TaskBatchBreakRuleDel> ruleDelList = null;
        if (StringUtils.isNotBlank(detailIds)) {
            Collection<TaskBatchBreakRuleDel> list = taskBatchBreakRuleDelService.listByIds(Arrays.asList(detailIds.split(",")));
            List<String> ids = list.stream().map(TaskBatchBreakRuleDel::getId).collect(Collectors.toList());
            // 更新状态为等待
            TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
            bean.setStatus(MedicalConstant.RUN_STATE_WAIT);
            taskBatchBreakRuleDelService.update(bean,
                    new QueryWrapper<TaskBatchBreakRuleDel>().in("ID", ids));

            List<String> caseIdList = list.stream()
                    .filter(r -> MedicalConstant.RULE_TYPE_CASE.equals(r.getRuleType())
                    || MedicalConstant.RULE_TYPE_NEWCASE.equals(r.getRuleType()))
                    .map(TaskBatchBreakRuleDel::getCaseId)
                    .distinct().collect(Collectors.toList());
            if (caseIdList.size() > 0) {
                // 备份模型
                taskProjectBatchService.reBackHis(batchId, caseIdList);
            }

            // 开始任务
			 /*for(TaskBatchBreakRuleDel ruleDel: list){
			     // 执行模型规则
			 	if(MedicalConstant.RULE_TYPE_CASE.equals(ruleDel.getRuleType())){
					engineCaseService.generateMedicalUnreasonableActionByThreadPool(ruleDel.getBatchId(), ruleDel.getBusiId(), ruleDel.getCaseId());
				// 执行临床路径
			 	} else if(MedicalConstant.RULE_TYPE_CLINICAL_NEW.equals(ruleDel.getRuleType())){
                    engineClinicalService.generateMedicalUnreasonableClinicalAction(ruleDel.getBatchId(), ruleDel.getCaseId());
				}

             }*/
            ruleDelList = list;
        } else {
            ruleDelList = new ArrayList<>();
            TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
            bean.setBatchId(batchId);
            bean.setCaseId(caseId);
            if(StringUtils.isNotBlank(busiId)){
                bean.setBusiId(busiId);
                bean.setRuleType(MedicalConstant.RULE_TYPE_CASE);
            } else {
                bean.setRuleType(MedicalConstant.RULE_TYPE_NEWCASE);
            }

			/*taskBatchBreakRuleDelService.update(batchId, busiId, caseId);
	 	 	engineCaseService.generateMedicalUnreasonableActionByThreadPool(batchId, busiId, caseId);*/
            ruleDelList.add(bean);
        }
//		 return Result.ok("操作成功！");
        return Result.ok(ruleDelList);
    }

    @AutoLog(value = "任务项目批次-执行单个批次模型")
    @ApiOperation(value = "任务项目批次-执行单个批次模型", notes = "任务项目批次-执行单个批次模型")
    @PostMapping(value = "/execByRuleId")
    public Result<?> execByRuleId(String batchId, String ruleId, String ruleType) throws Exception {
        if (MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL.equals(ruleType)) {
            engineClinicalService.generateMedicalUnreasonableClinicalActionByThreadPool(batchId, ruleId);

        } else if (MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE.equals(ruleType)) {
            engineDrugUseService.generateUnreasonableActionByThreadPool(batchId, ruleId);

        } else if (MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE.equals(ruleType)) {
            engineRuleService.generateUnreasonableActionByThreadPool(batchId, ruleId);

        } else if (MedicalConstant.ENGINE_BUSI_TYPE_NEWTREAT.equals(ruleType)) {
            engineRuleService.generateUnreasonableActionByThreadPool(batchId, ruleId);

        } else if (MedicalConstant.ENGINE_BUSI_TYPE_NEWDRUG.equals(ruleType)) {
            engineRuleService.generateUnreasonableActionByThreadPool(batchId, ruleId);

        }
        return Result.ok("操作成功！");
    }

    @AutoLog(value = "任务项目批次-执行单个批次模型")
    @ApiOperation(value = "任务项目批次-执行单个批次模型", notes = "任务项目批次-执行单个批次模型")
    @PostMapping(value = "/execDrug")
    public Result<?> execDrug(@RequestParam(name = "batchId") String batchId,
                              @RequestParam(name = "ruleType") String ruleType,
                              @RequestParam(name = "itemCode") String itemCode
    ) throws Exception {

        engineCaseService.generateUnreasonableDrugAction(batchId, ruleType, itemCode);
        return Result.ok("操作成功，开始重跑！");
    }

    @AutoLog(value = "任务项目批次-重新执行批次单个类型任务")
    @ApiOperation(value = "任务项目批次-重新执行批次单个类型任务", notes = "任务项目批次-重新执行批次单个类型任务")
    @PostMapping(value = "/execReRun")
    public Result<?> execReRun(String batchId, String ruleTypes) throws Exception {
        // 更新tab页运行状态
		/* taskBatchStepItemService.update(new UpdateWrapper<TaskBatchStepItem>()
				 .eq("STEP", 1)
				 .eq("BATCH_ID", batchId)
				 .eq("ITEM_ID", ruleType)
				 .set("STATUS", MedicalConstant.RUN_STATE_RUNNING)
		 );*/

		List<String> ruleTypeList = Arrays.asList(ruleTypes.split(","));
        Collection<TaskBatchBreakRuleDel> dataList = new ArrayList<>();
        if (ruleTypeList.indexOf(MedicalConstant.RULE_TYPE_CASE) > -1 || ruleTypeList.indexOf(MedicalConstant.RULE_TYPE_NEWCASE) > -1) {
            QueryWrapper<TaskBatchBreakRuleDel> queryWrapper = new QueryWrapper<TaskBatchBreakRuleDel>()
                    .in("RULE_TYPE", MedicalConstant.RULE_TYPE_CASE, MedicalConstant.RULE_TYPE_NEWCASE)
                    .eq("BATCH_ID", batchId)
                    .eq("STATUS", MedicalConstant.RUN_STATE_ABNORMAL);
            // 获取失败列表
            List<TaskBatchBreakRuleDel> list = taskBatchBreakRuleDelService.list(queryWrapper);
            dataList.addAll(list);
            // 备份模型
            List<String> caseIdList = list.stream().map(TaskBatchBreakRuleDel::getCaseId).distinct().collect(Collectors.toList());
            if (caseIdList.size() > 0) {
                taskProjectBatchService.reBackHis(batchId, caseIdList);
            }
            // 更新状态为等待
            TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
            bean.setStatus(MedicalConstant.RUN_STATE_WAIT);
            taskBatchBreakRuleDelService.update(bean, queryWrapper);
            // 开始任务
            /*for (TaskBatchBreakRuleDel ruleDel : list) {
                // 执行模型规则
                engineCaseService.generateMedicalUnreasonableActionByThreadPool(ruleDel.getBatchId(), ruleDel.getBusiId(), ruleDel.getCaseId());
            }*/
        }
        if (ruleTypeList.indexOf(MedicalConstant.RULE_TYPE_CLINICAL_NEW) > -1) {
            QueryWrapper<TaskBatchBreakRuleDel> queryWrapper = new QueryWrapper<TaskBatchBreakRuleDel>()
                    .eq("RULE_TYPE", MedicalConstant.RULE_TYPE_CLINICAL_NEW)
                    .eq("BATCH_ID", batchId)
                    .eq("STATUS", MedicalConstant.RUN_STATE_ABNORMAL);
            // 获取失败列表
            List<TaskBatchBreakRuleDel> list = taskBatchBreakRuleDelService.list(queryWrapper);
            dataList.addAll(list);
            // 更新状态为等待
            TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
            bean.setStatus(MedicalConstant.RUN_STATE_WAIT);
            bean.setRuleType(MedicalConstant.RULE_TYPE_CLINICAL_NEW);
            taskBatchBreakRuleDelService.update(bean, queryWrapper);
            // 开始任务
            /*for (TaskBatchBreakRuleDel ruleDel : list) {
                // 执行临床路径
                engineClinicalService.generateMedicalUnreasonableClinicalActionByThreadPool(ruleDel.getBatchId(), ruleDel.getCaseId());
            }*/
        } /*else if (MedicalConstant.RULE_TYPE_DRUG.equals(ruleType)) {
            engineDrugService.generateUnreasonableActionFailRerun(batchId);
        } else if (MedicalConstant.RULE_TYPE_CHARGE.equals(ruleType)) {
            engineChargeService.generateUnreasonableActionFailRerun(batchId);
        } else if (MedicalConstant.RULE_TYPE_TREAT.equals(ruleType)) {
            engineTreatService.generateUnreasonableActionFailRerun(batchId);
        } else if (MedicalConstant.RULE_TYPE_DRUGUSE.equals(ruleType)) {
            engineDrugUseService.generateUnreasonableActionFailRerun(batchId);
        } else if (MedicalConstant.RULE_TYPE_NEWCHARGE.equals(ruleType)) {
            engineRuleService.generateUnreasonableActionFailRerun(batchId, MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE);
        }*/
//        return Result.ok("操作成功，开始重跑！");
        return Result.ok(dataList);
    }

    @AutoLog(value = "任务项目批次-重新执行批次单个类型任务")
    @ApiOperation(value = "任务项目批次-重新执行批次单个类型任务", notes = "任务项目批次-重新执行批次单个类型任务")
    @PostMapping(value = "/abnormalTask")
    public Result<?> abnormalTask(String dataSource){
        this.abnormalCase(dataSource);
        this.abnormalRule(dataSource);
        this.abnormalStep(dataSource);
        this.abnormalExport(dataSource);
        this.abnormalImportAsync(dataSource);
        return Result.ok();
    }

    //@PostConstruct
    public void startRun() {
        if (SpringContextUtils.isProd() && !async && ApiTokenUtil.IS_CENTER) {
            for(String dataSource: ApiTokenUtil.getNodeDataSources()){
                this.abnormalTask(dataSource);
            }
        }
    }

    /*@PostConstruct
    public void startRun() {
        if (SpringContextUtils.isProd() && !async) {
            this.abnormalCase(null);
            this.abnormalStep(null);
            this.abnormalExport(null);
        }
    }*/


    private void abnormalCase(String dataSource) {
        QueryWrapper<TaskBatchBreakRuleDel> queryWrapper = new QueryWrapper<TaskBatchBreakRuleDel>()
                .in("STATUS", MedicalConstant.RUN_STATE_RUNNING, MedicalConstant.RUN_STATE_WAIT);
        if(StringUtils.isNotBlank(dataSource)){
            String inSqlBatchId = "SELECT p1.BATCH_ID from TASK_PROJECT_BATCH p1 JOIN TASK_PROJECT p2 ON p1.PROJECT_ID=p2.PROJECT_ID where p2.DATA_SOURCE = '" + dataSource +"'";
            queryWrapper.inSql("BATCH_ID", inSqlBatchId);
        }
        TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
        bean.setStatus(MedicalConstant.RUN_STATE_ABNORMAL);
        bean.setErrorMsg("系统重启");
        taskBatchBreakRuleDelService.update(bean, queryWrapper);
    }

    private void abnormalRule(String dataSource) {
        QueryWrapper<TaskBatchBreakRuleLog> queryWrapper = new QueryWrapper<TaskBatchBreakRuleLog>()
                .in("STATUS", MedicalConstant.RUN_STATE_RUNNING, MedicalConstant.RUN_STATE_WAIT);
        if(StringUtils.isNotBlank(dataSource)){
            String inSqlBatchId = "SELECT p1.BATCH_ID from TASK_PROJECT_BATCH p1 JOIN TASK_PROJECT p2 ON p1.PROJECT_ID=p2.PROJECT_ID where p2.DATA_SOURCE = '" + dataSource +"'";
            queryWrapper.inSql("BATCH_ID", inSqlBatchId);
        }
        TaskBatchBreakRuleLog bean = new TaskBatchBreakRuleLog();
        bean.setStatus(MedicalConstant.RUN_STATE_ABNORMAL);
        bean.setMessage("系统重启");
        ruleLogService.update(bean, queryWrapper);
    }

    private void abnormalStep(String dataSource) {
        QueryWrapper<TaskBatchStepItem> queryWrapper = new QueryWrapper<TaskBatchStepItem>()
                .in("STATUS", MedicalConstant.RUN_STATE_RUNNING, MedicalConstant.RUN_STATE_WAIT);
        if(StringUtils.isNotBlank(dataSource)){
            String inSqlBatchId = "SELECT p1.BATCH_ID from TASK_PROJECT_BATCH p1 JOIN TASK_PROJECT p2 ON p1.PROJECT_ID=p2.PROJECT_ID where p2.DATA_SOURCE = '" + dataSource +"'";
            queryWrapper.inSql("BATCH_ID", inSqlBatchId);
        }
        TaskBatchStepItem bean = new TaskBatchStepItem();
        bean.setStatus(MedicalConstant.RUN_STATE_ABNORMAL);
        bean.setMsg("系统重启");
        taskBatchStepItemService.update(bean, queryWrapper);
    }

    private void abnormalExport(String dataSource) {
        QueryWrapper<MedicalExportTask> queryWrapper = new QueryWrapper<MedicalExportTask>()
                .in("STATUS", "00", "-1");
        if(StringUtils.isNotBlank(dataSource)){
            queryWrapper.eq("DATA_SOURCE", dataSource);
        }
        MedicalExportTask bean = new MedicalExportTask();
        bean.setStatus("02");
        bean.setErrorMsg("系统重启");
        medicalExportTaskService.update(bean, queryWrapper);
    }

    private void abnormalImportAsync(String dataSource) {
        QueryWrapper<TaskAsyncActionLog> queryWrapper = new QueryWrapper<TaskAsyncActionLog>()
                .in("STATUS", MedicalConstant.RUN_STATE_RUNNING, MedicalConstant.RUN_STATE_WAIT);
        if(StringUtils.isNotBlank(dataSource)){
            queryWrapper.eq("DATA_SOURCE", dataSource);
        }
        TaskAsyncActionLog bean = new TaskAsyncActionLog();
        bean.setStatus(MedicalConstant.RUN_STATE_ABNORMAL);
        bean.setMsg("系统重启");
        taskAsyncActionLogService.update(bean, queryWrapper);
    }

    /**
     * 批次进度进入下一步
     *
     * @param batchId
     * @return
     */
    @AutoLog(value = "任务项目批次-批次进度进入下一步")
    @ApiOperation(value = "任务项目批次-批次进度进入下一步", notes = "任务项目批次-批次进度进入下一步")
    @PostMapping(value = "/nextStep")
    public Result<?> nextStep(@RequestParam(name = "batchId") String batchId) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        TaskProjectBatch bean = taskProjectBatchService.getById(batchId);
        bean.setStep(bean.getStep() + 1);
        bean.setUpdateTime(new Date());
        bean.setUpdateUser(user.getId());
        bean.setUpdateUserName(user.getRealname());
		 /*if(bean.getStep() == 3){
			 EngineResult engineResult = engineBehaviorService.generateUnreasonableBehaviorAll(bean.getBatchId());
			 if(!engineResult.isSuccess()){
			 	 Result<Object> result = Result.error(engineResult.getMessage());
				 result.setResult(bean.getStep());
				 return result;
			 }
		 }*/
        taskProjectBatchService.updateById(bean);
        return Result.ok("操作成功！");
    }

    @AutoLog(value = "任务项目批次-跳到第三步")
    @ApiOperation(value = "任务项目批次-跳到第三步", notes = "任务项目批次-跳到第三步")
    @PostMapping(value = "/to3rdStep")
    public Result<?> to3rdStep(@RequestParam(name = "batchId") String batchId) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        TaskProjectBatch bean = taskProjectBatchService.getById(batchId);
        bean.setStep(3);
        bean.setUpdateTime(new Date());
        bean.setUpdateUser(user.getId());
        bean.setUpdateUserName(user.getRealname());
		 /*if(bean.getStep() == 3){
			 EngineResult engineResult = engineBehaviorService.generateUnreasonableBehaviorAll(bean.getBatchId());
			 if(!engineResult.isSuccess()){
			 	 Result<Object> result = Result.error(engineResult.getMessage());
				 result.setResult(bean.getStep());
				 return result;
			 }
		 }*/
        taskProjectBatchService.updateById(bean);
        return Result.ok("操作成功！");
    }

    /**
     * 批次进度退回上一步
     *
     * @param batchId
     * @return
     */
    @AutoLog(value = "任务项目批次-批次进度退回上一步")
    @ApiOperation(value = "任务项目批次-批次进度退回上一步", notes = "任务项目批次-批次进度退回上一步")
    @PostMapping(value = "/lastStep")
    public Result<?> lastStep(@RequestParam(name = "batchId") String batchId) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        TaskProjectBatch bean = taskProjectBatchService.getById(batchId);
        bean.setStep(bean.getStep() - 1);
        bean.setUpdateTime(new Date());
        bean.setUpdateUser(user.getId());
        bean.setUpdateUserName(user.getRealname());
        taskProjectBatchService.updateById(bean);
        return Result.ok("操作成功！");
    }

    @AutoLog(value = "任务项目批次-无分页列表查询")
    @ApiOperation(value = "任务项目批次-无分页列表查询", notes = "任务项目批次-无分页列表查询")
    @GetMapping(value = "/queryList")
    public Result<?> queryList(TaskProjectBatch taskProjectBatch, HttpServletRequest req) throws Exception {
        QueryWrapper<TaskProjectBatch> queryWrapper = QueryGenerator.initQueryWrapper(taskProjectBatch, req.getParameterMap());
        List<TaskProjectBatch> list = taskProjectBatchService.list(queryWrapper);
        return Result.ok(list);
    }

    @AutoLog(value = "任务项目批次-无分页列表和项目")
    @ApiOperation(value = "任务项目批次-无分页列表和项目", notes = "任务项目批次-无分页列表和项目")
    @GetMapping(value = "/queryListAndProject")
    public Result<?> queryListAndProject(TaskProjectBatch taskProjectBatch, HttpServletRequest req) throws Exception {
        JSONObject json = new JSONObject();

        QueryWrapper<TaskProjectBatch> queryWrapper = QueryGenerator.initQueryWrapper(taskProjectBatch, req.getParameterMap());
        List<TaskProjectBatch> list = taskProjectBatchService.list(queryWrapper);
        json.put("batchList", list);
        List<String> projectIds = list.stream().map(TaskProjectBatch::getProjectId).distinct().collect(Collectors.toList());
        List<TaskProject> projectList = taskProjectService.list(new QueryWrapper<TaskProject>().in("PROJECT_ID", projectIds));
        json.put("projectList", projectList);
        return Result.ok(json);
    }

    /**
     * 分页列表查询
     *
     * @param taskProjectBatch
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "任务项目批次-分页列表查询")
    @ApiOperation(value = "任务项目批次-分页列表查询", notes = "任务项目批次-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(TaskProjectBatch taskProjectBatch,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) throws Exception {
        QueryWrapper<TaskProjectBatch> queryWrapper = QueryGenerator.initQueryWrapper(taskProjectBatch, req.getParameterMap());
        Page<TaskProjectBatchVO> page = new Page<TaskProjectBatchVO>(pageNo, pageSize);
        IPage<TaskProjectBatchVO> pageList = taskProjectBatchService.pageVO(page, queryWrapper);
       /* List<TaskProjectBatchVO> dataList = pageList.getRecords();
        //json.facet获取批次审查数量和推送数量
        Map<String, TaskProjectBatchVO> dataMap = new HashMap<>();
        List<String> facetList = new ArrayList<>();
        for (TaskProjectBatchVO bean : dataList) {
            if (bean.getStep() == 0) {
                continue;
            }
            String q = "BATCH_ID:\\\"" + bean.getBatchId() + "\\\" ";
            facetList.add(String.format("\"%s\":{type:query, q:\"%s\", facet: {pushDataCount:{type:query,q:\"SEC_PUSH_STATUS:1\"}}}", bean.getBatchId(), q));
            dataMap.put(bean.getBatchId(), bean);
        }
        if (facetList.size() > 0) {
            String facetStr = "{ " + StringUtils.join(facetList, ",") + "}";
            JSONObject resultJon = SolrUtil.jsonFacet(EngineUtil.MEDICAL_UNREASONABLE_ACTION, null, facetStr);
            for (Map.Entry<String, Object> entry : resultJon.entrySet()) {
                TaskProjectBatchVO bean = dataMap.get(entry.getKey());
                if (bean != null) {
                    JSONObject json = (JSONObject) entry.getValue();
                    bean.setDataCount(json.getIntValue("count"));
                    if (bean.getDataCount() == 0) {
                        bean.setPushDataCount(0);
                    } else {
                        JSONObject json2 = (JSONObject) json.get("pushDataCount");
                        bean.setPushDataCount(json2.getIntValue("count"));
                    }
                }
            }
        }*/
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param taskProjectBatch
     * @return
     */
    @AutoLog(value = "任务项目批次-添加")
    @ApiOperation(value = "任务项目批次-添加", notes = "任务项目批次-添加")
    @PostMapping(value = "/add")
    public Result<?> add(TaskProjectBatch taskProjectBatch, @RequestParam(name = "ruleData", defaultValue = "[]") String ruleData) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<TaskBatchBreakRule> ruleList = JSONObject.parseArray(ruleData, TaskBatchBreakRule.class);
        Date nowTime = new Date();
        taskProjectBatch.setCreateTime(nowTime);
        taskProjectBatch.setCreateUser(user.getId());
        taskProjectBatch.setCreateUserName(user.getRealname());
        taskProjectBatch.setStep(0);
        taskProjectBatchService.saveBatch(taskProjectBatch, ruleList);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param taskProjectBatch
     * @return
     */
    @AutoLog(value = "任务项目批次-编辑")
    @ApiOperation(value = "任务项目批次-编辑", notes = "任务项目批次-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(TaskProjectBatch taskProjectBatch, @RequestParam(name = "ruleData", defaultValue = "[]") String ruleData, @RequestParam(name = "editRuleTypes", defaultValue = "") String editRuleTypes) {

        List<TaskBatchBreakRule> ruleList = JSONObject.parseArray(ruleData, TaskBatchBreakRule.class);
        taskProjectBatchService.updateBatch(taskProjectBatch, ruleList, Arrays.asList(editRuleTypes.split(",")));
        return Result.ok("编辑成功!");
    }

    @AutoLog(value = "任务项目批次-编辑批次信息")
    @ApiOperation(value = "任务项目批次-编辑批次信息", notes = "任务项目批次-编辑批次信息")
    @PutMapping(value = "/editInfo")
    public Result<?> editInfo(TaskProjectBatch taskProjectBatch) {
        taskProjectBatchService.updateById(taskProjectBatch);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "任务项目批次-通过id删除")
    @ApiOperation(value = "任务项目批次-通过id删除", notes = "任务项目批次-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        try {
            TaskProjectBatch bean = taskProjectBatchService.removeBatch(id);
            return Result.ok(bean);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
/*
	@AutoLog(value = "任务项目批次-批量删除")
	@ApiOperation(value="任务项目批次-批量删除", notes="任务项目批次-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.taskProjectBatchService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
*/

    /**
     * 通过id查询
     */
    @AutoLog(value = "任务项目批次-获取所有的项目和批次")
    @ApiOperation(value = "任务项目批次-获取所有的项目和批次", notes = "任务项目批次-获取所有的项目和批次")
    @GetMapping(value = "/getAllProjectAndBatch")
    public Result<?> getAllProjectAndBatch() {
        List<TaskProject> projectList = taskProjectService.list();
        List<TaskProjectBatch> batchList = taskProjectBatchService.list();
        JSONObject json = new JSONObject();
        json.put("projects", projectList);
        json.put("batchs", batchList);
        return Result.ok(json);
    }

    @AutoLog(value = "任务项目和任务项目批次-通过批次id查询")
    @ApiOperation(value = "任务项目和任务项目批次-通过批次id查询", notes = "任务项目批次-通过批次id查询")
    @GetMapping(value = "/queryExecInfoById")
    public Result<?> queryExecInfoById(@RequestParam(name = "batchId", required = true) String batchId) {
        TaskBatchExecInfo execInfo = this.taskProjectBatchService.queryExecInfoById(batchId);
        return Result.ok(execInfo);
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "任务项目批次-通过id查询")
    @ApiOperation(value = "任务项目批次-通过id查询", notes = "任务项目批次-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        TaskProjectBatch taskProjectBatch = taskProjectBatchService.getById(id);
        return Result.ok(taskProjectBatch);
    }

    @AutoLog(value = "任务项目批次-通过项目id查询")
    @ApiOperation(value = "任务项目批次-通过项目id查询", notes = "任务项目批次-通过项目id查询")
    @GetMapping(value = "/queryByProjectId")
    public Result<?> queryByProjectId(@RequestParam(name = "projectId", required = true) String projectId) {
        List<TaskProjectBatch> list = taskProjectBatchService.list(new QueryWrapper<TaskProjectBatch>().eq("PROJECT_ID", projectId));
        return Result.ok(list);
    }

    @AutoLog(value = "任务项目批次-通过项目id查询")
    @ApiOperation(value = "任务项目批次-通过项目id查询", notes = "任务项目批次-通过项目id查询")
    @GetMapping(value = "/queryPushedByProjectId")
    public Result<?> queryPushedByProjectId(@RequestParam(name = "projectId", required = true) String projectId) {
        List<TaskProjectBatch> list = taskProjectBatchService.list(
                new QueryWrapper<TaskProjectBatch>()
                        .eq("PROJECT_ID", projectId)
                        .eq("step", 4));
        return Result.ok(list);
    }

    @AutoLog(value = "任务项目批次-通过项目id查询")
    @ApiOperation(value = "任务项目批次-通过项目id查询", notes = "任务项目批次-通过项目id查询")
    @GetMapping(value = "/queryListFilter")
    public Result<?> queryListFilter(String projectIds, String batchIds, Integer step) {
        QueryWrapper<TaskProjectBatch> queryWrapper = new QueryWrapper<>();

        if(StringUtils.isNotBlank(projectIds) && StringUtils.isNotBlank(batchIds)){
            queryWrapper.and(q -> q
                    .in("PROJECT_ID", Arrays.asList(projectIds.split(",")))
                    .or()
                    .in("BATCH_ID", Arrays.asList(batchIds.split(",")))
            );
        } else if(StringUtils.isNotBlank(projectIds)){
            queryWrapper.in("PROJECT_ID", Arrays.asList(projectIds.split(",")));
        } else if(StringUtils.isNotBlank(batchIds)){
            queryWrapper.in("BATCH_ID", Arrays.asList(batchIds.split(",")));
        }

        if(step != null){
            queryWrapper.eq("STEP", step);
        }


        List<TaskProjectBatch> list = taskProjectBatchService.list(queryWrapper);
        return Result.ok(list);
    }


    /**
     * 通过批次id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "任务项目和任务项目批次-通过批次id查询")
    @ApiOperation(value = "任务项目和任务项目批次-通过批次id查询", notes = "任务项目批次-通过批次id查询")
    @GetMapping(value = "/queryBatchAndProjectById")
    public Result<?> queryBatchAndProjectById(@RequestParam(name = "id", required = true) String id) {
        Map<String, Object> data = new HashMap<String, Object>();
        TaskProjectBatch taskProjectBatch = taskProjectBatchService.getById(id);
        TaskProject taskProject = new TaskProject();
        if (taskProjectBatch != null) {
            taskProject = taskProjectService.getById(taskProjectBatch.getProjectId());
        }
        data.put("project", taskProject);
        data.put("projectBatch", taskProjectBatch);
        return Result.ok(data);
    }

    /**
     * 更新项目批次信息
     *
     * @param bean
     * @return
     */
    @AutoLog(value = "更新项目批次信息-更新")
    @ApiOperation(value = "更新项目批次信息-更新", notes = "更新项目批次信息-更新")
    @PutMapping(value = "/updateTaskProjectBatch")
    public Result<?> updateTaskProjectBatch(@RequestBody TaskProjectBatch bean) {
        Integer step = bean.getStep();
		/*if(step == 3){
			EngineResult engineResult = engineBehaviorService.generateUnreasonableBehaviorAll(bean.getBatchId());
			if(engineResult.isSuccess()){
				LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
				bean.setUpdateTime(new Date());
				bean.setUpdateUser(user.getId());
				bean.setUpdateUserName(user.getRealname());
				taskProjectBatchService.updateById(bean);
			} else {
				Result.error(engineResult.getMessage());
			}
		}*/
        return Result.ok("操作成功");
    }


    @AutoLog(value = "更新项目批次信息-更新")
    @ApiOperation(value = "更新项目批次信息-更新", notes = "更新项目批次信息-更新")
    @GetMapping(value = "/topBatchTask")
    public Result<?> updateTaskProjectBatch(@RequestParam(name = "topNum", defaultValue = "8") int topNum) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<TaskBatchStepItemVO> list = taskProjectBatchService.selectTopBatchItems(topNum, user.getDataSource());
        return Result.ok(list);
    }

    @AutoLog(value = "特殊批次信息-保存")
    @ApiOperation(value = "特殊批次信息-保存", notes = "特殊批次信息-保存")
    @PostMapping(value = "/saveTaskProjectBatchAndStep")
    public Result<?> saveTaskProjectBatchAndStep(@RequestBody TaskProjectBatch taskProjectBatch) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //生成批次信息
        taskProjectBatch.setStep(1);
        taskProjectBatch.setRuleTypes(MedicalConstant.RULE_TYPE_MANUAL);
        taskProjectBatch.setCreateTime(new Date());
        taskProjectBatch.setCreateUser(user.getId());
        taskProjectBatch.setCreateUserName(user.getRealname());
        taskProjectBatchService.save(taskProjectBatch);
        //插入批次步骤信息
        String batchId = taskProjectBatch.getBatchId();
        Set<String> ruleTypeSet = new HashSet();
        ruleTypeSet.add(taskProjectBatch.getRuleTypes());
        engineCaseService.initStep(batchId,ruleTypeSet);

        //获取project
        TaskProject project = taskProjectService.getById(taskProjectBatch.getProjectId());

        Map<String,Object> data = new HashMap<>();
        data.put("project",project);
        data.put("batchId",taskProjectBatch.getBatchId());
        return  Result.ok(data);
    }

    @AutoLog(value = "特殊批次步骤信息-更新")
    @ApiOperation(value = "特殊批次步骤信息-更新", notes = "特殊批次信息-更新")
    @PostMapping(value = "/updateTaskBatchStepItem")
    public Result<?> updateTaskBatchStepItem(@RequestParam(name = "batchId", required = true) String batchId) {
        //修改批次步骤状态
        TaskBatchStepItem step = new TaskBatchStepItem();
        step.setUpdateTime(new Date());
        step.setEndTime(new Date());
        step.setStatus(MedicalConstant.RUN_STATE_NORMAL);
        taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_MANUAL, step);
        return Result.ok("操作成功");
    }


    @AutoLog(value = "特殊批次步骤信息-更新")
    @ApiOperation(value = "特殊批次步骤信息-更新", notes = "特殊批次信息-更新")
    @GetMapping(value = "/queryByConditions")
    public Result<?> queryByConditions(String dataSources,
                                       String projectIds) {

        String[] dsArray = StringUtils.isNotBlank(dataSources)?dataSources.split(","): null;
        String[] pjArray = StringUtils.isNotBlank(projectIds)?projectIds.split(","): null;

        if(dsArray != null || pjArray != null){
            List<TaskProjectBatch> taskProjectBatchList = taskProjectBatchService.queryBatchByProjectOrDs(dsArray, pjArray);
            return Result.ok(taskProjectBatchList);
        }
        return Result.ok(new ArrayList<>());
    }

    @AutoLog(value = "查询项目批次名称")
    @ApiOperation(value = "查询项目批次名称", notes = "查询项目批次名称")
    @GetMapping(value = "/queryProjectBatchName")
    public Result<?> queryProjectBatchName(String batchIds, String projectIds) {
        JSONObject jsonObject = new JSONObject();
        if(StringUtils.isNotBlank(projectIds)){
            List<TaskProject> list = taskProjectService.list(new QueryWrapper<TaskProject>()
                    .in("PROJECT_ID", Arrays.asList(projectIds.split(",")))
                    .select("PROJECT_NAME")
            );
            jsonObject.put("projectNames", list.stream().map(TaskProject::getProjectName).collect(Collectors.joining(",")));
        }
        if(StringUtils.isNotBlank(batchIds)){
            List<TaskProjectBatch> list = taskProjectBatchService.list(new QueryWrapper<TaskProjectBatch>()
                    .in("BATCH_ID", Arrays.asList(batchIds.split(",")))
                    .select("BATCH_NAME")
            );
            jsonObject.put("batchNames", list.stream().map(TaskProjectBatch::getBatchName).collect(Collectors.joining(",")));
        }
        return Result.ok(jsonObject);

    }

}
