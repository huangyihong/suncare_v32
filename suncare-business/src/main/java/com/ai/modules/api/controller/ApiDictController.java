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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.config.service.IMedicalColConfigService;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.config.service.IMedicalProjectGroupService;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.ai.modules.config.vo.MedicalGroupVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags="字典解析相关")
@Controller
@RequestMapping("/oauth/api/dict")
public class ApiDictController {
	@Autowired
    private IMedicalDictService dictService;
	@Autowired
    private IMedicalColConfigService configService;
	@Autowired
	private IMedicalActionDictService actionDictService;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private IMedicalProjectGroupService projectGroupService;
	
	@ApiOperation(value = "字典解析", notes = "字典解析")
	@RequestMapping(value="/parseText", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> parseText(String code, String key, HttpServletRequest req) throws Exception {
		String text = dictService.queryDictTextByKey(code, key);
		return ApiResponse.ok(text);
	}
	
	@ApiOperation(value = "字典解析", notes = "字典解析")
	@RequestMapping(value="/parse", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> parseText(String code) throws Exception {
		List<MedicalDictItemVO> list = dictService.queryByType(code);
		return ApiResponse.ok(list);
	}
	
	@ApiOperation(value = "根据类型获取字典列表", notes = "字典解析")
	@RequestMapping(value="/dictByKind", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> dictByKind(String kind) throws Exception {
		List<MedicalDictItemVO> list = dictService.queryMedicalDictByKind(kind);
		return ApiResponse.ok(list);
	}
	
	@ApiOperation(value = "数仓库表字段", notes = "数仓库表字段")
	@RequestMapping(value="/medicalColConfig", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> medicalColConfig(String tableName, String colName) throws Exception {
		MedicalColConfig config = configService.getMedicalColConfigByCache(colName, tableName);
		return ApiResponse.ok(config);
	}
	
	@ApiOperation(value = "查找项目组最近变更时间", notes = "查找项目组最近变更时间")
	@RequestMapping(value="/project/lasttime", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> lasttime() throws Exception {
		String sql = "SELECT GREATEST(max(CREATE_TIME), max(UPDATE_TIME)) MAX_TIME FROM MEDICAL_PROJECT_GROUP";
		String value = jdbcTemplate.queryForObject(sql, String.class);
		return ApiResponse.ok(value);
	}
	
	@ApiOperation(value = "查找不合规行为列表", notes = "查找不合规行为列表")
	@RequestMapping(value="/action/list", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> actionList() throws Exception {
		List<MedicalActionDict> list = actionDictService.list();
		return ApiResponse.ok(list);
	}
	
	@ApiOperation(value = "根据项目组编码获取项目列表", notes = "根据项目组编码获取项目列表")
	@RequestMapping(value="/projectGrp", method = {RequestMethod.POST })
	@ResponseBody
	public ApiResponse<?> projectGrp(String groupCode) throws Exception {
		String[] array = groupCode.split(",");
		List<String> codes = Arrays.asList(array);
		List<MedicalGroupVO> list = projectGroupService.queryGroupItemByGroupCodes(codes);
		return ApiResponse.ok(list);
	}
}
