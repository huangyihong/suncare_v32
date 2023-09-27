/**
 * TopReportController.java	  V1.0   2022年5月31日 上午11:28:46
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.controller;

import java.util.List;

import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.*;

import com.ai.modules.engine.handle.report.TopReportHandle;
import com.ai.modules.engine.model.dto.TopReportDTO;
import com.ai.modules.engine.model.report.ReportFacetBucketField;
import com.ai.modules.engine.model.report.TopReportParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags="指标统计分析")
@RestController
@RequestMapping("/apiTask/top/report")
public class TopReportController {

	@ApiOperation(value = "排序指标统计分析", notes = "排序指标统计分析")
    @PostMapping(value = "/analysis")
    public Result<?> analysis(@RequestBody TopReportDTO dto) throws Exception {
		TopReportParam reportParam = new TopReportParam();
		reportParam.setCollection(dto.getTableName());
		reportParam.setGroupBy(dto.getGroupBy());
		if(!"count".equalsIgnoreCase(dto.getAggregate())) {
			String function = "sum(%s)";
			function = String.format(function, dto.getAggregate());
			reportParam.setStaFunction(function);
		}
		reportParam.setSort(dto.getSort());
		reportParam.setLimit(dto.getLimit());
		reportParam.setWheres(dto.getWhereList());
		TopReportHandle handle = new TopReportHandle(reportParam);
		List<ReportFacetBucketField> dataList = handle.singleDimCallSolr();
		return Result.ok(dataList);
    }
}
