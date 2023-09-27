/**
 * ApiLoginController.java	  V1.0   2018年5月3日 下午5:45:33
 *
 * Copyright (c) 2018 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.task.entity.TaskActionFieldConfig;
import com.ai.modules.task.entity.TaskBatchActionFieldConfig;
import com.ai.modules.task.service.ITaskActionFieldConfigService;
import com.ai.modules.task.service.ITaskBatchActionFieldConfigService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags="不合规行为动态汇总")
@Controller
@RequestMapping("/oauth/api/summary")
public class ApiSummaryController {
	@Autowired
	private ITaskActionFieldConfigService configSV;
	@Autowired
	private ITaskBatchActionFieldConfigService batchConfigSV;
	@Autowired
	private ITaskActionFieldConfigService dynamicConfigSV;
	
	@ApiOperation(value = "按不合规行为名称查找不合规行为动态汇总配置")
	@RequestMapping(value="/configList", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> configList(String actionNames) throws Exception {
		List<String> actionNameList = Arrays.asList(actionNames.split(","));
		List<TaskActionFieldConfig> configList = configSV.list(new QueryWrapper<TaskActionFieldConfig>()
				.in("ACTION_NAME", actionNameList)
				.isNotNull("GROUP_FIELDS"));
		return ApiResponse.ok(configList);
	}
	
	@ApiOperation(value = "查找不合规行为动态汇总配置")
	@RequestMapping(value="/allConfigList", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> allConfigList() throws Exception {
		List<TaskActionFieldConfig> configList = configSV.list(new QueryWrapper<TaskActionFieldConfig>().isNotNull("GROUP_FIELDS"));
		return ApiResponse.ok(configList);
	}
	
	@ApiOperation(value = "查找不合规行为动态汇总配置")
	@RequestMapping(value="/config", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> config(String actionName) throws Exception {
		TaskActionFieldConfig config = configSV.getOne(new QueryWrapper<TaskActionFieldConfig>().eq("ACTION_NAME", actionName));
		return ApiResponse.ok(config);
	}
	
	@ApiOperation(value = "通过缓存查找不合规行为动态汇总配置")
	@RequestMapping(value="/configByCache", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> configByCache(String actionName) throws Exception {
		TaskActionFieldConfig config = dynamicConfigSV.queryTaskActionFieldConfigByCache(actionName);
		return ApiResponse.ok(config);
	}
	
	@ApiOperation(value = "保存批次的不合规行为动态汇总配置")
	@RequestMapping(value="/saveBatchConfig", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> saveBatchConfig(String dataJson) throws Exception {
		TaskBatchActionFieldConfig record = JSON.parseObject(dataJson, TaskBatchActionFieldConfig.class);
		batchConfigSV.save(record);
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "批量保存批次的不合规行为动态汇总配置")
	@RequestMapping(value="/batchSaveBatchConfig", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> batchSaveBatchConfig(String dataJson) throws Exception {
		List<TaskBatchActionFieldConfig> recordList = JSON.parseArray(dataJson, TaskBatchActionFieldConfig.class);
		batchConfigSV.saveBatch(recordList);
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "删除批次的不合规行为动态汇总配置")
	@RequestMapping(value="/removeBatchConfigByAction", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> removeBatchConfig(String batchId, String actionName) throws Exception {
		batchConfigSV.remove(new QueryWrapper<TaskBatchActionFieldConfig>().eq("ACTION_NAME", actionName).eq("BATCH_ID", batchId));
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "删除批次的不合规行为动态汇总配置")
	@RequestMapping(value="/removeBatchConfig", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> removeBatchConfig(String batchId) throws Exception {
		batchConfigSV.remove(new QueryWrapper<TaskBatchActionFieldConfig>().eq("BATCH_ID", batchId));
		return ApiResponse.ok();
	}
	
	@ApiOperation(value = "更新批次的不合规行为动态汇总配置")
	@RequestMapping(value="/updateBatchConfig", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> updateBatchConfig(String batchId, String actionName, String dataJson) throws Exception {
		TaskBatchActionFieldConfig up = JSON.parseObject(dataJson, TaskBatchActionFieldConfig.class);
		batchConfigSV.update(up, new QueryWrapper<TaskBatchActionFieldConfig>().eq("ACTION_NAME", actionName).eq("BATCH_ID", batchId));
		return ApiResponse.ok();
	}
}
