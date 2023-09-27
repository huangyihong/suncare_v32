package com.ai.modules.task.controller;

import com.ai.modules.review.service.IMedicalUnreasonableActionService;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
* @Description: 任务项目批次
* @Author: jeecg-boot
* @Date:   2020-01-03
* @Version: V1.0
*/
@Slf4j
@Api(tags="任务项目批次")
@RestController
@RequestMapping("/gp/apiTask/taskProjectBatch")
public class ApiTaskProjectBatchGpController extends JeecgController<TaskProjectBatch, ITaskProjectBatchService> {

    @Autowired
    private IMedicalUnreasonableActionService medicalUnreasonableActionService;

    /**
     * 批次审核数据统计
     */
    @AutoLog(value = "任务项目批次-批次审核数据统计")
    @ApiOperation(value="任务项目批次-批次审核数据统计", notes="任务项目批次-批次审核数据统计")
    @GetMapping(value = "/facetBatchCount")
    public Result<?> facetBatchCount(@RequestParam(name="batchIds") String batchIds,
                                       HttpServletRequest req) throws Exception {
        JSONObject resultJon = medicalUnreasonableActionService.facetBatchCount(batchIds);
        return Result.ok(resultJon);
    }
}
