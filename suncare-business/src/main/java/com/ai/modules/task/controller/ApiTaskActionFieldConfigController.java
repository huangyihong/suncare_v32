package com.ai.modules.task.controller;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ObjectMapUtils;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.service.IDynamicFieldService;
import com.ai.modules.task.entity.TaskActionFieldConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskActionFieldConfigService;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.ai.modules.task.vo.TaskActionFieldColVO;
import com.ai.modules.task.vo.TaskActionFieldConfigVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 不同不合规行为显示字段配置
 * @Author: jeecg-boot
 * @Date: 2020-10-12
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "不同不合规行为显示字段配置")
@RestController
@RequestMapping("/apiTask/taskActionFieldConfig")
public class ApiTaskActionFieldConfigController extends JeecgController<TaskActionFieldConfig, ITaskActionFieldConfigService> {
    @Autowired
    private ITaskActionFieldConfigService taskActionFieldConfigService;

    @Autowired
    private ITaskProjectBatchService taskProjectBatchService;

    @Autowired
    private IDynamicFieldService dynamicFieldService;

    @AutoLog(value = "不同不合规行为显示字段配置-沉淀分组字段值")
    @ApiOperation(value = "不同不合规行为显示字段配置-沉淀分组字段值", notes = "不同不合规行为显示字段配置-沉淀分组字段值")
    @PostMapping(value = "/saveExtData")
    public Result<?> saveExtData(String dataSources,
                                 String projectIds,
                                 String batchIds,
                                 @RequestParam(name = "actionId") String actionId,
                                 @RequestParam(name = "groupFields") String groupFields,
                                 HttpServletRequest req) throws Exception {


        List<String> batchIdList = StringUtils.isNotBlank(batchIds)?
                Arrays.asList(batchIds.split(",")) : new ArrayList<>();

        if(StringUtils.isNotBlank(dataSources) || StringUtils.isNotBlank(projectIds)){
            Map<String, String> map = new HashMap<>();
            map.put("dataSources", dataSources);
            map.put("projectIds", projectIds);
            List<TaskProjectBatch> taskProjectBatchList = ApiTokenUtil.getArray("/task/taskProjectBatch/queryByConditions", map, TaskProjectBatch.class);
            batchIdList.addAll(taskProjectBatchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.toList()));
        }

        if(batchIdList.size() == 0){
            throw new Exception("缺少沉淀项目批次范围");
        }

        taskActionFieldConfigService.addGroupByTask(groupFields, batchIdList.toArray(new String[0]), actionId, new String[0], true);

        return Result.ok("正在沉淀分组数据，请等待");
    }


    @AutoLog(value = "不同不合规行为显示字段配置-沉淀违规说明")
    @ApiOperation(value = "不同不合规行为显示字段配置-沉淀违规说明", notes = "不同不合规行为显示字段配置-沉淀违规说明")
    @PostMapping(value = "/saveBreakStateTemplData")
    public Result<?> saveBreakStateTemplData(
                                MedicalActionDict medicalActionDict,
                                String dataSources,
                                 String projectIds,
                                 String batchIds,
                                 @RequestParam(name = "actionId") String actionId,
                                 HttpServletRequest req) throws Exception {


        List<String> batchIdList = StringUtils.isNotBlank(batchIds)?
                Arrays.asList(batchIds.split(",")) : new ArrayList<>();

        if(StringUtils.isNotBlank(dataSources) || StringUtils.isNotBlank(projectIds)){
            Map<String, String> map = new HashMap<>();
            map.put("dataSources", dataSources);
            map.put("projectIds", projectIds);
            List<TaskProjectBatch> taskProjectBatchList = ApiTokenUtil.getArray("/task/taskProjectBatch/queryByConditions", map, TaskProjectBatch.class);
            batchIdList.addAll(taskProjectBatchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.toList()));
        }

        if(batchIdList.size() == 0){
            throw new Exception("缺少沉淀项目批次范围");
        }

        taskActionFieldConfigService.addBreakStateTemplTask(medicalActionDict, actionId, batchIdList.toArray(new String[0]), new String[0], true);

        return Result.ok("正在沉淀违规说明数据，请等待");
    }

}
