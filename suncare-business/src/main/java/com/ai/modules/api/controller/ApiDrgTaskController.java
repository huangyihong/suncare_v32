package com.ai.modules.api.controller;

import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.entity.DrgTask;
import com.ai.modules.drg.service.IDrgCatalogService;
import com.ai.modules.drg.service.IDrgTaskService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(tags="drg分组任务相关")
@Controller
@RequestMapping("/oauth/api/drg/task")
public class ApiDrgTaskController {
    @Autowired
    private IDrgTaskService drgTaskService;

    @ApiOperation(value = "查找任务")
    @RequestMapping(value="/get", method = {RequestMethod.POST })
    @ResponseBody
    public ApiResponse<?> get(String id) throws Exception {
        DrgTask task = drgTaskService.getById(id);
        return ApiResponse.ok(task);
    }

    @ApiOperation(value = "查找任务")
    @RequestMapping(value="/getByBatch", method = {RequestMethod.POST })
    @ResponseBody
    public ApiResponse<?> getByBatch(String batchId) throws Exception {
        DrgTask task = drgTaskService.getOne(new QueryWrapper<DrgTask>().eq("batch_id", batchId));
        return ApiResponse.ok(task);
    }

    @ApiOperation(value = "任务状态更新")
    @RequestMapping(value="/update", method = {RequestMethod.POST })
    @ResponseBody
    public ApiResponse<?> update(String batchId, String dataJson) throws Exception {
        DrgTask task = JSON.parseObject(dataJson, DrgTask.class);
        QueryWrapper<DrgTask> wrapper = new QueryWrapper<>();
        wrapper.eq("batch_id", batchId);
        drgTaskService.update(task, wrapper);
        return ApiResponse.ok();
    }
}
