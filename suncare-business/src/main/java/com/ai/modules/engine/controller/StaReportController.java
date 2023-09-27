/**
 * StaReportController.java	  V1.0   2020年8月21日 上午11:21:07
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai.modules.engine.model.report.ReportFormField;
import com.ai.modules.engine.model.report.StatisticsReportModel;
import com.ai.modules.engine.service.report.IStaReportService;
import com.alibaba.fastjson.JSON;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags="报表分析")
@RestController
@RequestMapping("/engine/report")
public class StaReportController {
	@Autowired
	private IStaReportService reportService;
	
	@AutoLog(value = "图表")
	@ApiOperation(value = "图表", notes = "图表")
	@PostMapping(value = "/echart")
	public Result<?> echart(@RequestParam String reportId, 
			@RequestParam(name="whereJson", defaultValue="[]") String whereJson, HttpServletRequest req) throws Exception {
		List<ReportFormField> whereFields = JSON.parseArray(whereJson, ReportFormField.class);
		StatisticsReportModel reportModel = reportService.gerenate(reportId, whereFields);
		return Result.ok(reportModel);
	}
}
