package com.ai.modules.task.controller;

import com.ai.common.MedicalConstant;
import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.config.service.IMedicalColConfigService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.review.service.IDynamicFieldService;
import com.ai.modules.task.entity.TaskActionBatchExtmap;
import com.ai.modules.task.entity.TaskActionFieldConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.*;
import com.ai.modules.task.vo.TaskActionFieldColVO;
import com.ai.modules.task.vo.TaskActionFieldConfigVO;
import com.alibaba.fastjson.JSON;
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
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
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
@RequestMapping("/task/taskActionFieldConfig")
public class TaskActionFieldConfigController extends JeecgController<TaskActionFieldConfig, ITaskActionFieldConfigService> {
    @Autowired
    private ITaskActionFieldConfigService taskActionFieldConfigService;

    @Autowired
    private ITaskActionFieldColService taskActionFieldColService;

    @Autowired
    private ITaskActionBatchExtmapService taskActionBatchExtmapService;

    @Autowired
    IMedicalActionDictService medicalActionDictService;


    /**
     * 分页列表查询
     *
     * @param taskActionFieldConfig
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "不同不合规行为显示字段配置-分页列表查询")
    @ApiOperation(value = "不同不合规行为显示字段配置-分页列表查询", notes = "不同不合规行为显示字段配置-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(TaskActionFieldConfig taskActionFieldConfig,
                                   String searchActionName,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<TaskActionFieldConfig> queryWrapper = QueryGenerator.initQueryWrapper(taskActionFieldConfig, req.getParameterMap());
        if(StringUtils.isNotBlank(searchActionName)){
        	searchActionName=StringUtils.replace(searchActionName, "*", "");
            queryWrapper.exists("SELECT 1 FROM MEDICAL_ACTION_DICT a WHERE TASK_ACTION_FIELD_CONFIG.ACTION_ID=a.ACTION_ID and "+DbDataEncryptUtil.decryptFunc("ACTION_NAME")+" like '%" + searchActionName + "%'");
        }

        Page<TaskActionFieldConfig> page = new Page<TaskActionFieldConfig>(pageNo, pageSize);
        IPage<TaskActionFieldConfig> pageList = taskActionFieldConfigService.page(page, queryWrapper);
        List<TaskActionFieldConfig> list = pageList.getRecords();
        List<String> actionIdList = list.stream().map(TaskActionFieldConfig::getActionId).distinct().collect(Collectors.toList());
        Map<String, String> actionMap = medicalActionDictService.getMapByCodes(actionIdList);
        list.forEach(r -> {
            String actionName = actionMap.get(r.getActionId());
            if(actionName != null){
                r.setActionName(actionName);
            }
        });
        return Result.ok(pageList);
    }

    @AutoLog(value = "不同不合规行为显示字段配置-列表查询")
    @ApiOperation(value = "不同不合规行为显示字段配置-列表查询", notes = "不同不合规行为显示字段配置-列表查询")
    @GetMapping(value = "/listFieldConfigByAction")
    public Result<?> listFieldConfigByAction(@RequestParam(name = "actionId") String actionId,
                                             @RequestParam(name = "batchId") String batchId) {
        // 获取汇总沉淀批次历史记录
        TaskActionBatchExtmap taskActionBatchExtmap = taskActionBatchExtmapService.getOne(new QueryWrapper<TaskActionBatchExtmap>()
                .eq("ACTION_ID", actionId)
                .eq("BATCH_ID", batchId)
        );

        JSONObject resultJson = new JSONObject();

        if(taskActionBatchExtmap != null && StringUtils.isNotBlank(taskActionBatchExtmap.getGroupFields())){
            resultJson.put("groupFields", taskActionBatchExtmap.getGroupFields());
        } else {
            // 没有沉淀字段，直接获取配置
            TaskActionFieldConfig taskActionFieldConfig = taskActionFieldConfigService.getOne(
                    new QueryWrapper<TaskActionFieldConfig>()
                            .eq("PLATFORM", MedicalConstant.PLATFORM_SERVICE)
                            .eq("STATUS", "normal")
                            .eq("ACTION_ID", actionId), false);
            if(taskActionFieldConfig == null){
                return Result.ok();
            }
            resultJson.put("groupFields", taskActionFieldConfig.getGroupFields());
        }

        // 获取列和搜索条件
        List<TaskActionFieldColVO> colList = taskActionFieldColService.queryColByAction(MedicalConstant.PLATFORM_SERVICE, actionId, null);
        List<TaskActionFieldColVO> serList = taskActionFieldColService.querySerColByAction(MedicalConstant.PLATFORM_SERVICE, actionId, null);

        if(colList.size() > 0){
            resultJson.put("colConfigs", colList);
        }
        if(serList.size() > 0){
            resultJson.put("serConfigs", serList);
        }

        return Result.ok(resultJson);
    }

    /*@AutoLog(value = "不同不合规行为显示字段配置-列表查询")
    @ApiOperation(value = "不同不合规行为显示字段配置-列表查询", notes = "不同不合规行为显示字段配置-列表查询")
    @GetMapping(value = "/listWithColConfig")
    public Result<?> listWithColConfig(TaskActionFieldConfig taskActionFieldConfig, HttpServletRequest req) {
        QueryWrapper<TaskActionFieldConfig> queryWrapper = QueryGenerator.initQueryWrapper(taskActionFieldConfig, req.getParameterMap());
        queryWrapper.eq("STATUS", "normal");
        List<TaskActionFieldConfig> taskActionFieldConfigList = taskActionFieldConfigService.list(queryWrapper);

        if (taskActionFieldConfigList.size() > 0) {
            String[] ids = taskActionFieldConfigList.stream().map(TaskActionFieldConfig::getId).toArray(String[]::new);
            List<TaskActionFieldColVO> colList = taskActionFieldConfigService.queryColByConfigIds(ids, MedicalConstant.PLATFORM_SERVICE);

            JSONObject resultJson = new JSONObject();
            resultJson.put("fields", taskActionFieldConfigList);
            resultJson.put("colConfigs", colList);
            return Result.ok(resultJson);
        } else {
            return Result.ok();
        }
    }*/

   /* @AutoLog(value = "不同不合规行为显示字段配置-更新搜索条件")
    @ApiOperation(value = "不同不合规行为显示字段配置-更新搜索条件", notes = "不同不合规行为显示字段配置-更新搜索条件")
    @PostMapping(value = "/editSearch")
    public Result<?> editSearch(@RequestBody TaskActionFieldSearchDTO taskActionFieldSearch) throws Exception {
        taskActionFieldConfigService.editSearch(taskActionFieldSearch.getConfigId(), taskActionFieldSearch.getCols());
        return Result.ok("添加成功！");
    }*/


    /**
     * 添加
     *
     * @param taskActionFieldConfig
     * @return
     */
    @AutoLog(value = "不同不合规行为显示字段配置-添加")
    @ApiOperation(value = "不同不合规行为显示字段配置-添加", notes = "不同不合规行为显示字段配置-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody TaskActionFieldConfigVO taskActionFieldConfig) throws Exception {
        if (taskActionFieldConfig.getMulti()) {
            List<String> actionIds = Arrays.asList(taskActionFieldConfig.getActionId().split(","));
            QueryWrapper<TaskActionFieldConfig> queryWrapper = new QueryWrapper<TaskActionFieldConfig>()
                    .eq("PLATFORM", taskActionFieldConfig.getPlatform())
                    .in("ACTION_ID", actionIds);
            List<TaskActionFieldConfig> list = taskActionFieldConfigService.list(queryWrapper);
            if (list.size() > 0) {
                return Result.error("重复配置不合规行为：" + list.stream().map(TaskActionFieldConfig::getActionName).collect(Collectors.joining("，")));
            }

            List<String> actionNames = Arrays.asList(taskActionFieldConfig.getActionName().split(","));

            for(int i = 0; i < actionIds.size(); i++){
                TaskActionFieldConfig config = new TaskActionFieldConfig();
                BeanUtils.copyProperties(taskActionFieldConfig, config);
                config.setActionId(actionIds.get(i));
                config.setActionName(actionNames.get(i));
                list.add(config);
            }

            taskActionFieldConfigService.saveConfigs(list, taskActionFieldConfig.getCols(), taskActionFieldConfig.getSearchs());
        } else {
            QueryWrapper<TaskActionFieldConfig> queryWrapper = new QueryWrapper<TaskActionFieldConfig>()
                    .eq("PLATFORM", taskActionFieldConfig.getPlatform())
                    .eq("ACTION_ID", taskActionFieldConfig.getActionId());
            int count = taskActionFieldConfigService.count(queryWrapper);
            if (count > 0) {
                return Result.error("重复配置不合规行为");
            }
            taskActionFieldConfigService.saveConfig(taskActionFieldConfig);
        }
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param taskActionFieldConfig
     * @return
     */
    @AutoLog(value = "不同不合规行为显示字段配置-编辑")
    @ApiOperation(value = "不同不合规行为显示字段配置-编辑", notes = "不同不合规行为显示字段配置-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody TaskActionFieldConfigVO taskActionFieldConfig) throws Exception {
        String actionId = taskActionFieldConfig.getActionId();
        if (StringUtils.isNotBlank(actionId)) {
            QueryWrapper<TaskActionFieldConfig> queryWrapper = new QueryWrapper<TaskActionFieldConfig>()
                    .eq("ACTION_ID", actionId)
                    .eq("PLATFORM", taskActionFieldConfig.getPlatform())
                    .ne("ID", taskActionFieldConfig.getId());
            int count = taskActionFieldConfigService.count(queryWrapper);
            if (count > 0) {
                return Result.error("重复配置不合规行为");
            }
        }
        taskActionFieldConfigService.updateConfig(taskActionFieldConfig);
        return Result.ok("编辑成功!");
    }

    @AutoLog(value = "不同不合规行为显示字段配置-更新主体部分字段")
    @ApiOperation(value = "不同不合规行为显示字段配置-更新主体部分字段", notes = "不同不合规行为显示字段配置-更新主体部分字段")
    @PutMapping(value = "/editConfig")
    public Result<?> editConfig(@RequestBody TaskActionFieldConfig taskActionFieldConfig) throws Exception {
        taskActionFieldConfigService.updateById(taskActionFieldConfig);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "不同不合规行为显示字段配置-通过id删除")
    @ApiOperation(value = "不同不合规行为显示字段配置-通过id删除", notes = "不同不合规行为显示字段配置-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        taskActionFieldConfigService.removeConfigById(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "不同不合规行为显示字段配置-批量删除")
    @ApiOperation(value = "不同不合规行为显示字段配置-批量删除", notes = "不同不合规行为显示字段配置-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.taskActionFieldConfigService.removeConfigByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "不同不合规行为显示字段配置-通过id查询")
    @ApiOperation(value = "不同不合规行为显示字段配置-通过id查询", notes = "不同不合规行为显示字段配置-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        TaskActionFieldConfig taskActionFieldConfig = taskActionFieldConfigService.getById(id);
        return Result.ok(taskActionFieldConfig);
    }

    @AutoLog(value = "不同不合规行为显示字段配置-通过id查询")
    @ApiOperation(value = "不同不合规行为显示字段配置-通过id查询", notes = "不同不合规行为显示字段配置-通过id查询")
    @GetMapping(value = "/queryByActionId")
    public Result<?> queryByActionId(@RequestParam(name = "actionId", required = true) String actionId) {
        TaskActionFieldConfig taskActionFieldConfig = taskActionFieldConfigService.getOne(
                new QueryWrapper<TaskActionFieldConfig>()
                        .eq("ACTION_ID", actionId)
                        .eq("PLATFORM", MedicalConstant.PLATFORM_SERVICE)
        );
        return Result.ok(taskActionFieldConfig);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param taskActionFieldConfig
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, TaskActionFieldConfig taskActionFieldConfig) {
        return super.exportXls(request, taskActionFieldConfig, TaskActionFieldConfig.class, "不同不合规行为显示字段配置");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, TaskActionFieldConfig.class);
    }
}
