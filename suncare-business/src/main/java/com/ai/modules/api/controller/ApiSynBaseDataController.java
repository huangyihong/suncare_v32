/**
 * ApiTaskController.java	  V1.0   2020年12月23日 下午2:53:23
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskBatchBreakRuleDelService;
import com.ai.modules.task.service.ITaskBatchBreakRuleLogService;
import com.ai.modules.task.service.ITaskBatchBreakRuleService;
import com.ai.modules.task.service.ITaskBatchStepItemService;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.ai.modules.task.service.ITaskProjectService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags="同步基础数据api")
@Controller
@RequestMapping("/oauth/api/synBaseData")
public class ApiSynBaseDataController {
	@Autowired
    private ITaskProjectService taskSV;
	@Autowired
	private ITaskProjectBatchService batchSV;
	@Autowired
    private ITaskBatchStepItemService stepItemService;
	@Autowired
	private ITaskBatchBreakRuleLogService logService;
	@Autowired
    private ITaskBatchBreakRuleService taskBatchRuleSV;
	@Autowired
    private ITaskBatchBreakRuleDelService taskBatchRuleDelSV;
	
	@ApiOperation(value = "根据id查找项目")
	@RequestMapping(value="/project", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> task(String projectId) throws Exception {
		TaskProject task = taskSV.getById(projectId);
		return ApiResponse.ok(task);
	}
	
	@ApiOperation(value = "根据id查找项目批次")
	@RequestMapping(value="/batch", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> batch(String batchId) throws Exception {
		TaskProjectBatch batch = batchSV.getById(batchId);
		return ApiResponse.ok(batch);
	}
	
	@ApiOperation(value = "更新项目批次进度")
	@RequestMapping(value="/step/update", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> step(String batchId, String step, String stepType, String dataJson) throws Exception {
		QueryWrapper<TaskBatchStepItem> wrapper = new QueryWrapper<TaskBatchStepItem>()
                .eq("BATCH_ID", batchId)
                .eq("STEP", 1)
                .eq("ITEM_ID", stepType);
		TaskBatchStepItem entity = JSON.parseObject(dataJson, TaskBatchStepItem.class);
        stepItemService.update(entity, wrapper);
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "保存项目批次进度")
	@RequestMapping(value="/step/save", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> saveStep(String dataJson) throws Exception {
		TaskBatchStepItem entity = JSON.parseObject(dataJson, TaskBatchStepItem.class);
        stepItemService.save(entity);
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "保存项目批次进度")
	@RequestMapping(value="/step/batchSave", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> batchSaveStep(String dataJson) throws Exception {
		List<TaskBatchStepItem> entityList = JSON.parseArray(dataJson, TaskBatchStepItem.class);
        stepItemService.saveBatch(entityList);
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "删除项目批次进度")
	@RequestMapping(value="/step/remove", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> removeStep(String batchId) throws Exception {
		QueryWrapper<TaskBatchStepItem> wrapper = new QueryWrapper<TaskBatchStepItem>()
                .eq("BATCH_ID", batchId)
                .eq("STEP", 1);
        stepItemService.remove(wrapper);
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "删除批次规则进度日志")
	@RequestMapping(value="/batch/log/remove", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> removeLog(String batchId, String busiType) throws Exception {
		logService.remove(new QueryWrapper<TaskBatchBreakRuleLog>()
        		.eq("ITEM_TYPE", busiType)
        		.eq("BATCH_ID", batchId));
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "保存批次规则进度日志")
	@RequestMapping(value="/batch/log/save", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> saveLog(String dataJson) throws Exception {
		List<TaskBatchBreakRuleLog> logList = JSON.parseArray(dataJson, TaskBatchBreakRuleLog.class);
		logService.saveBatch(logList);
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "修改批次规则进度日志")
	@RequestMapping(value="/batch/log/update", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> updateLog(String batchId, String busiType, String itemId, String dataJson) throws Exception {
		QueryWrapper<TaskBatchBreakRuleLog> wrapper = new QueryWrapper<TaskBatchBreakRuleLog>()
	            .eq("BATCH_ID", batchId)
	            .eq("ITEM_TYPE", busiType)
	            .eq("ITEM_ID", itemId);
		TaskBatchBreakRuleLog log = JSON.parseObject(dataJson, TaskBatchBreakRuleLog.class);
		logService.update(log, wrapper);
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "修改批次规则进度日志")
	@RequestMapping(value="/batch/log/updateByItemids", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> updateLogByItemids(String batchId, String busiType, String itemIds, String dataJson) throws Exception {
		List<String> itemIdList = Arrays.asList(itemIds.split(","));
		QueryWrapper<TaskBatchBreakRuleLog> wrapper = new QueryWrapper<TaskBatchBreakRuleLog>()
	            .eq("BATCH_ID", batchId)
	            .eq("ITEM_TYPE", busiType)
	            .in("ITEM_ID", itemIdList);
		TaskBatchBreakRuleLog log = JSON.parseObject(dataJson, TaskBatchBreakRuleLog.class);
		logService.update(log, wrapper);
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "修改批次规则进度日志")
	@RequestMapping(value="/batch/log/groupByStatus", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> groupByStatus(String batchId, String busiType) throws Exception {
		Map<String, Integer> statusMap = logService.groupByStatus(batchId, busiType);
		return ApiResponse.ok(statusMap);
	}
	
	@ApiOperation(value = "批量更新批次规则运行状态")
	@RequestMapping(value="/batch/log/wait", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> waitLog(String batchId, String busiType, String codes) throws Exception {
		List<String> codeList = Arrays.asList(codes.split(","));
		logService.waitTaskBatchBreakRuleLog(batchId, busiType, codeList);
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "查找项目批次已选中的业务组、临床路径等")
	@RequestMapping(value="/batch/rule", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> batchRule(String batchId, String ruleId) throws Exception {
		QueryWrapper<TaskBatchBreakRule> wrapper = new QueryWrapper<TaskBatchBreakRule>()
                .eq("BATCH_ID", batchId)
                .eq("RULE_ID", ruleId);
        List<TaskBatchBreakRule> items = taskBatchRuleSV.list(wrapper);
		return ApiResponse.ok(items);
	}
	
	@ApiOperation(value = "查找项目批次已选中的业务组、临床路径等")
	@RequestMapping(value="/batch/ruleByStep", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> batchRuleByStep(String batchId, String stepType) throws Exception {
		QueryWrapper<TaskBatchBreakRule> wrapper = new QueryWrapper<TaskBatchBreakRule>()
                .eq("BATCH_ID", batchId)
                .eq("RULE_TYPE", stepType);
        List<TaskBatchBreakRule> items = taskBatchRuleSV.list(wrapper);
		return ApiResponse.ok(items);
	}
	
	@ApiOperation(value = "查找项目批次已选中的模型、临床路径进度等")
	@RequestMapping(value="/batch/rule/progress", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> progressList(String batchId, String stepType) throws Exception {
		QueryWrapper<TaskBatchBreakRuleDel> wrapper = new QueryWrapper<TaskBatchBreakRuleDel>()
                .eq("BATCH_ID", batchId)
                .eq("RULE_TYPE", stepType);
        List<TaskBatchBreakRuleDel> items = taskBatchRuleDelSV.list(wrapper);
		return ApiResponse.ok(items);
	}
	
	@ApiOperation(value = "更新项目批次已选中的模型、临床路径进度等")
	@RequestMapping(value="/batch/rule/updateProgress", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> updateProgress(String batchId, String stepType, String caseId, String dataJson) throws Exception {
		QueryWrapper<TaskBatchBreakRuleDel> wrapper = new QueryWrapper<TaskBatchBreakRuleDel>()
                .eq("BATCH_ID", batchId)
                .eq("RULE_TYPE", stepType)
                .eq("CASE_ID", caseId);
		TaskBatchBreakRuleDel up = JSON.parseObject(dataJson, TaskBatchBreakRuleDel.class);
        taskBatchRuleDelSV.update(up, wrapper);
		return ApiResponse.ok();
	}
}
