package com.ai.modules.task.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ai.common.MedicalConstant;
import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ExcelXUtils;
import com.ai.common.utils.StringUtil;
import com.ai.common.utils.ThreadUtils;
import com.ai.common.utils.TimeUtil;
import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.api.util.ApiTokenCommon;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.engine.model.dto.BatchItemDTO;
import com.ai.modules.engine.service.*;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.runnable.EngineFunctionRunnable;
import com.ai.modules.review.vo.DwbMasterInfoVo;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.MD5Util;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @Description: 任务项目批次
 * @Author: jeecg-boot
 * @Date:   2020-01-03
 * @Version: V1.0
 */
@Slf4j
@Api(tags="任务项目批次")
@RestController
@RequestMapping("/apiTask/taskProjectBatch")
public class ApiTaskProjectBatchController extends JeecgController<TaskProjectBatch, ITaskProjectBatchService> {

    @Autowired
    private IEngineCaseService engineCaseService;

    @Autowired
    private IEngineClinicalService engineClinicalService;

    @Autowired
    private IEngineDrugService engineDrugService;
    @Autowired
    private IEngineDrugUseService engineDrugUseService;

    @Autowired
    private IEngineChargeService engineChargeService;

    @Autowired
    private IEngineTreatService engineTreatService;

    @Autowired
    private IEngineRuleService engineRuleService;

    @Autowired
    private IEngineResultHandle engineResultHandle;

    @Value("${engine.async}")
    private boolean async;

    @AutoLog(value = "审核统计-不合规行为列表查询")
    @ApiOperation(value = "审核统计-不合规行为列表查询", notes = "审核统计-不合规行为列表查询")
    @GetMapping(value = "/queryBatchAndProjectFilter")
    public Result<?> queryBatchAndProjectFilter(MedicalUnreasonableActionVo searchObj, HttpServletRequest req) throws Exception {

        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        // facet
        JSONObject termFacet = new JSONObject();
        termFacet.put("type", "terms");
        termFacet.put("field", "BATCH_ID");
        termFacet.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        termFacet.put("overrequest", Integer.MAX_VALUE);

        List<JSONObject> list = new ArrayList<>();
        // 查询
        SolrUtil.jsonFacet(EngineUtil.MEDICAL_UNREASONABLE_ACTION
                , solrQuery.getFilterQueries(), termFacet.toJSONString()
                , list::add);
        List<String> batchIdList = list.stream().map(r -> String.valueOf(r.get("val"))).collect(Collectors.toList());
        if(batchIdList.size() == 0){
            return Result.ok();
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("batchId", StringUtils.join(batchIdList,","));

        JSONObject json = ApiTokenUtil.getJson("/task/taskProjectBatch/queryListAndProject", paramMap);
        return Result.ok(json);
    }

    @AutoLog(value = "任务项目批次-删除模型数据")
    @ApiOperation(value="任务项目批次-删除模型数据", notes="任务项目批次-删除模型数据")
    @PostMapping(value = "/deleteCase")
    public Result<?> deleteCase(@RequestParam(name="detailIds") String detailIds, @RequestParam(name="batchId") String batchId) throws Exception {

        // 创建或更新各个模型进度
        // 重新备份规则
        // 更新状态,规则类型
        Map<String, String> map = new HashMap<>();
        map.put("detailIds", detailIds);

        // 设置数据库状态
        List<TaskBatchBreakRuleDel> list = ApiTokenUtil.postArray("/task/taskProjectBatch/deleteCase", map, TaskBatchBreakRuleDel.class);

        List<String> caseIdList = list.stream().map(TaskBatchBreakRuleDel::getCaseId).distinct().collect(Collectors.toList());

        // 删除沉淀数据
        String query = "BATCH_ID:\"" + batchId + "\"" + " AND BUSI_TYPE:CASE AND CASE_ID:(\"" + StringUtils.join(caseIdList, "\",\"") +"\")";
        SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, query);

        return Result.ok("操作成功！");
    }

    /**
     * 执行批次任务
     *
     * @param batchId
     * @return
     */
    @AutoLog(value = "任务项目批次-执行批次任务")
    @ApiOperation(value="任务项目批次-执行批次任务", notes="任务项目批次-执行批次任务")
    @PostMapping(value = "/execBatch")
    public Result<?> execBatch(@RequestParam(name="batchId") String batchId) throws Exception {

        // 创建或更新各个模型进度
        // 重新备份规则
        // 更新状态,规则类型
        Map<String, String> map = new HashMap<>();
        map.put("batchId", batchId);

        Set<String> ruleTypeSet = new HashSet<>(
                ApiTokenUtil.postArray("/task/taskProjectBatch/execBatch", map, String.class));

        // 开始执行
        engineCaseService.generateUnreasonableActionAll(batchId, ruleTypeSet);
        return Result.ok("操作成功！");
    }

    /**
     * 执行单个批次模型
     *
     * @param batchId
     * @return
     */
    @AutoLog(value = "任务项目批次-执行单个批次模型")
    @ApiOperation(value="任务项目批次-执行单个批次模型", notes="任务项目批次-执行单个批次模型")
    @PostMapping(value = "/execCase")
    public Result<?> execCase(String detailIds,String batchId,String busiId, String caseId, HttpServletRequest req) throws Exception {
        // 更新状态为等待
        // 备份模型
        Map<String, String> map = new HashMap<>();
        map.put("detailIds", detailIds);
        map.put("batchId", batchId);
        map.put("busiId", busiId);
        map.put("caseId", caseId);
        List<TaskBatchBreakRuleDel> list = ApiTokenUtil.postArray("/task/taskProjectBatch/execCase", map, TaskBatchBreakRuleDel.class);
        // 开始任务
        for(TaskBatchBreakRuleDel ruleDel: list){

            if(MedicalConstant.RULE_TYPE_CASE.equals(ruleDel.getRuleType())){
                // 执行模型业务组规则
                engineCaseService.generateMedicalUnreasonableActionByThreadPool(ruleDel.getBatchId(), ruleDel.getBusiId(), ruleDel.getCaseId());
            } else if(MedicalConstant.RULE_TYPE_CLINICAL_NEW.equals(ruleDel.getRuleType())){
                // 执行临床路径
                engineClinicalService.generateMedicalUnreasonableClinicalActionByThreadPool(ruleDel.getBatchId(), ruleDel.getCaseId());
            } else if(MedicalConstant.RULE_TYPE_NEWCASE.equals(ruleDel.getRuleType())){
                // 执行模型
                engineCaseService.generateMedicalUnreasonableActionByThreadPool(ruleDel.getBatchId(), ruleDel.getBusiId(), ruleDel.getCaseId());
            }

        }
        return Result.ok("操作成功！");
    }

    @AutoLog(value = "任务项目批次-执行单个批次模型")
    @ApiOperation(value="任务项目批次-执行单个批次模型", notes="任务项目批次-执行单个批次模型")
    @PostMapping(value = "/execByRuleId")
    public Result<?> execByRuleId(String batchId,String ruleId, String ruleType) throws Exception {
        if(MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL.equals(ruleType)){
            engineClinicalService.generateMedicalUnreasonableClinicalActionByThreadPool(batchId, ruleId);

        } else if(MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE.equals(ruleType)){
            engineDrugUseService.generateUnreasonableActionByThreadPool(batchId, ruleId);

        } else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE.equals(ruleType)){
            engineRuleService.generateUnreasonableActionByThreadPool(batchId, ruleId);

        } else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWTREAT.equals(ruleType)){
            engineRuleService.generateUnreasonableActionByThreadPool(batchId, ruleId);

        } else if(MedicalConstant.ENGINE_BUSI_TYPE_NEWDRUG.equals(ruleType)){
            engineRuleService.generateUnreasonableActionByThreadPool(batchId, ruleId);

        }
        return Result.ok("操作成功！");
    }

    @AutoLog(value = "任务项目批次-执行单个批次模型")
    @ApiOperation(value="任务项目批次-执行单个批次模型", notes="任务项目批次-执行单个批次模型")
    @PostMapping(value = "/execDrug")
    public Result<?> execDrug(@RequestParam(name="batchId") String batchId,
                              @RequestParam(name="ruleType") String ruleType,
                              @RequestParam(name="itemCode") String itemCode
    ) throws Exception {

        engineCaseService.generateUnreasonableDrugAction(batchId, ruleType, itemCode);
        return Result.ok("操作成功，开始重跑！");
    }

    @AutoLog(value = "任务项目批次-批量执行单个规则")
    @ApiOperation(value="任务项目批次-批量执行单个规则", notes="任务项目批次-批量执行单个规则")
    @PostMapping(value = "/batchExecDrug")
    public Result<?> batchExecDrug(@RequestParam(name="dataJson") String dataJson) throws Exception {
        List<BatchItemDTO> itemList = JSON.parseArray(dataJson, BatchItemDTO.class);
        engineCaseService.generateUnreasonableRuleAction(itemList);
        return Result.ok("操作成功，开始重跑！");
    }

    @AutoLog(value = "任务项目批次-重新执行批次单个类型任务")
    @ApiOperation(value="任务项目批次-重新执行批次单个类型任务", notes="任务项目批次-重新执行批次单个类型任务")
    @PostMapping(value = "/execReRun")
    public Result<?> execReRun(String batchId,String ruleTypes) throws Exception {
        Set<String> ruleTypeSet = new HashSet<>(Arrays.asList(ruleTypes.split(",")));

        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_CASE)
                || ruleTypeSet.contains(MedicalConstant.RULE_TYPE_NEWCASE)
                || ruleTypeSet.contains(MedicalConstant.RULE_TYPE_CLINICAL_NEW)
        ){
            Map<String, String> map = new HashMap<>();
            map.put("batchId", batchId);
            map.put("ruleTypes", ruleTypes);
            List<TaskBatchBreakRuleDel> list = ApiTokenUtil.postArray("/task/taskProjectBatch/execReRun", map, TaskBatchBreakRuleDel.class);

            for(TaskBatchBreakRuleDel ruleDel: list){
                if(MedicalConstant.RULE_TYPE_CASE.equals(ruleDel.getRuleType())
                        || MedicalConstant.RULE_TYPE_NEWCASE.equals(ruleDel.getRuleType())
                ){
                    // 获取失败列表
                    // 备份模型
                    // 更新状态为等待
                    // 执行模型规则
                    engineCaseService.generateMedicalUnreasonableActionByThreadPool(ruleDel.getBatchId(), ruleDel.getBusiId(), ruleDel.getCaseId());
                } else if(MedicalConstant.RULE_TYPE_CLINICAL_NEW.equals(ruleDel.getRuleType())){
                    // 获取失败列表
                    // 更新状态为等待
                    // 执行临床路径
                    engineClinicalService.generateMedicalUnreasonableClinicalActionByThreadPool(ruleDel.getBatchId(), ruleDel.getCaseId());
                }
            }
        }

        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_DRUG)) {
            engineDrugService.generateUnreasonableActionFailRerun(batchId);
        }
        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_CHARGE)){
            engineChargeService.generateUnreasonableActionFailRerun(batchId);
        }
        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_TREAT)){
            engineTreatService.generateUnreasonableActionFailRerun(batchId);
        }
        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_DRUGUSE)){
            engineDrugUseService.generateUnreasonableActionFailRerun(batchId);
        }
        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_NEWCHARGE)){
            engineRuleService.generateUnreasonableActionFailRerun(batchId, MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE);
        }

        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_NEWTREAT)){
            engineRuleService.generateUnreasonableActionFailRerun(batchId, MedicalConstant.ENGINE_BUSI_TYPE_NEWTREAT);
        }
        if(ruleTypeSet.contains(MedicalConstant.RULE_TYPE_NEWDRUG)){
            engineRuleService.generateUnreasonableActionFailRerun(batchId, MedicalConstant.ENGINE_BUSI_TYPE_NEWDRUG);
        }
        return Result.ok("操作成功，开始重跑！");
    }

    //@PostConstruct
    public void startRun()  {
        if(SpringContextUtils.isProd() && !async && !ApiTokenUtil.IS_CENTER){
            log.info("当前节点负责数据源：" + ApiTokenUtil.getNodeDataSources());
            for(String dataSource: ApiTokenUtil.getNodeDataSources()){
                Map<String, String> map = new HashMap<>();
                map.put("dataSource", dataSource);
                try {
                    String text = ApiTokenUtil.doPost(ApiTokenUtil.API_URL, "/task/taskProjectBatch/abnormalTask", map);
                    ApiResponse<?> apiResponse = JSON.parseObject(text, ApiResponse.class);
                    if(apiResponse.isSuccess()){
                        log.info(dataSource + "异步线程任务设为失败-" + apiResponse.getMessage() );
                    } else {
                        log.info(dataSource + "异步线程任务设为失败-失败：" + apiResponse.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info(dataSource + "异步线程任务设为失败-失败：" + e.getMessage());
                }
            }

        }
    }




    /**
     * 批次审核数据统计
     */
    @AutoLog(value = "任务项目批次-批次审核数据统计")
    @ApiOperation(value="任务项目批次-批次审核数据统计", notes="任务项目批次-批次审核数据统计")
    @GetMapping(value = "/facetBatchCount")
    public Result<?> facetBatchCount(@RequestParam(name="batchIds") String batchIds,
                                     HttpServletRequest req) throws Exception {

        List<String> facetList = new ArrayList<>();
        for(String batchId: batchIds.split(",")){
            String q = "BATCH_ID:" + batchId;
            facetList.add(String.format("\"%s\":{type:query, q:\"%s\", " +
                    "facet: {" +
                    "firstPushCount:{" +
                    "type:query," +
                    "q:\"PUSH_STATUS:1\"" +
                    "}" +
                    ",pushDataCount:{" +
                    "type:query," +
                    "q:\"SEC_PUSH_STATUS:1\"" +
                    "}" +
                    ",newsHandleCount:{" +
                    "type:query," +
                    "q:\"HANDLE_STATUS:1\"" +
                    "}" +
                    "}}", batchId, q));
        }
        String facetStr = "{ " + StringUtils.join(facetList, ",") + "}";

        //设置缓存时间5分钟
        SolrUtil.setCacheExpireSeconds(300);
        JSONObject resultJon = SolrUtil.jsonFacet(EngineUtil.MEDICAL_UNREASONABLE_ACTION, null, facetStr);
        return Result.ok(resultJon);
    }


    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "任务项目批次-通过id删除")
    @ApiOperation(value="任务项目批次-通过id删除", notes="任务项目批次-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name="id") String id) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        ApiResponse apiResponse = ApiTokenUtil.deleteApi("/task/taskProjectBatch/delete", map);
        if(apiResponse.isSuccess()){
            TaskProjectBatch taskProjectBatch = ApiTokenUtil.parseObject(apiResponse, TaskProjectBatch.class);
            if(taskProjectBatch == null || taskProjectBatch.getStep() >= MedicalConstant.BATCH_STEP_SYSTEM){
                SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "BATCH_ID:" + id);
                SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, "BATCH_ID:" + id);
            }
            return Result.ok("删除成功!");
        } else {
            return Result.error(apiResponse.getMessage());
        }
    }

    @AutoLog(value = "违规明细导入")
    @ApiOperation(value = "违规明细导入", notes = "违规明细导入")
    @PostMapping(value = "/importMedicalUnreasonableAction")
    public Result<?> importMedicalUnreasonableAction(@RequestParam("file") MultipartFile file, TaskProjectBatch taskProjectBatch, HttpServletResponse response) {
        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {
                if(StringUtils.isBlank(taskProjectBatch.getBatchName())){
                    return Result.error("批次名称为空，导入失败：");
                }
                if(StringUtils.isBlank(taskProjectBatch.getMonth())){
                    return Result.error("风控月份为空，导入失败：");
                }
                String msg = this.importMedicalUnreasonableAction(file, taskProjectBatch);
                if(msg == null){
                    return Result.ok("数据量过大，正在后台异步导入,请稍后刷新查看");
                } else {
                    return Result.ok("导入成功，" + msg);
                }

            } catch (Exception e) {
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        } else {
            return Result.error("导入失败，文件存在问题");
        }

    }


    public String importMedicalUnreasonableAction(MultipartFile file, TaskProjectBatch taskProjectBatch) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        int batchSize = 100;
        ApiResponse apiResponse = ApiTokenUtil.postBodyApi("/task/taskProjectBatch/saveTaskProjectBatchAndStep", taskProjectBatch);
        if(apiResponse.isSuccess()) {
            Map<String,Object> data  = ApiTokenUtil.parseObject(apiResponse, HashMap.class);
            JSONObject projectJson = (JSONObject)data.get("project");
            TaskProject project = JSON.toJavaObject(projectJson,TaskProject.class);
            String batchId = (String)data.get("batchId");
            if(project==null){
                throw new Exception("找不到项目信息，项目ID参数异常");
            }
            taskProjectBatch.setBatchId(batchId);

            String[] mappingFields = new String[]{"itemname","itemcode","actionMoney","maxActionMoney","visitid","actionTypeName","actionName","actionDesc","ruleBasis","minMoney","maxMoney","firReviewUsername","firReviewStatus"};
            List<MedicalUnreasonableActionVo> list = ExcelXUtils.readSheet(MedicalUnreasonableActionVo.class, mappingFields, 0, 1, file.getInputStream());

            JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictNameMapByKey("FIRST_REVIEW_STATUS");
            JSONObject actionTypeMap = ApiTokenCommon.queryMedicalDictNameMapByKey("ACTION_TYPE");
            JSONObject actionListMap = ApiTokenCommon.queryMedicalDictNameMapByKey("ACTION_LIST");


            BiFunction<List<MedicalUnreasonableActionVo>, JSONObject, Exception> actionFun = (dataList,json) -> {
                BufferedWriter fileWriter;
                try {
                    // 数据写入xml
                    String importFilePath = SolrUtil.importFolder +  "/importMedicalUnreasonableAction/" + System.currentTimeMillis() + "_" + list.size() + ".json";

                    fileWriter = new BufferedWriter(
                            new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
                    fileWriter.write("[");

                    List<MedicalUnreasonableActionVo> actionVoList = new ArrayList<>();
                    for (int i = 0; i < dataList.size(); i++) {
                        MedicalUnreasonableActionVo record = dataList.get(i);
                        validateMedicalUnreasonableActionVo(taskProjectBatch, user, project, reviewStatusMap, actionTypeMap, actionListMap, i, record);

                        actionVoList.add(record);
                        if(actionVoList.size()==batchSize){
                            batchQueryMasterInfo(actionVoList,fileWriter);
                            actionVoList.clear();
                        }
                    }

                    if(actionVoList.size()>0){
                        batchQueryMasterInfo(actionVoList,fileWriter);
                        actionVoList.clear();
                    }
                    //写文件尾
                    fileWriter.write("]");
                    fileWriter.close();

                    //导入solr
                    SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
                    Map<String, String> map = new HashMap<>();
                    map.put("batchId", batchId);
                    String text = ApiTokenUtil.doPost(ApiTokenUtil.API_URL, "/task/taskProjectBatch/updateTaskBatchStepItem", map);
                    ApiResponse apiResponse2 = JSON.parseObject(text, ApiResponse.class);
                    if(!apiResponse2.isSuccess()){
                        throw new Exception("操作API接口失败");
                    }
                    return null;
                } catch (Exception e) {
                    return e;
                }
            };
            if (list.size() > 200000) {
                String ds = SolrUtil.getLoginUserDatasource();
                ThreadUtils.THREAD_SOLR_REQUEST_POOL.add(new EngineFunctionRunnable(ds,user.getToken(), () -> {
                    Exception e = actionFun.apply(list, null);
                }));
                return null;
            }else{
                Exception e = actionFun.apply(list, null);
                if (e == null) {
                    return "数据量：" + list.size();
                } else {
                    throw e;
                }
            }
        }else{
            throw new Exception("操作API接口失败");
        }
    }

    private void validateMedicalUnreasonableActionVo(TaskProjectBatch taskProjectBatch, LoginUser user, TaskProject project, JSONObject reviewStatusMap, JSONObject actionTypeMap, JSONObject actionListMap, int i, MedicalUnreasonableActionVo record) throws Exception {
        /*if(StringUtils.isBlank(record.getItemname())){
            throw new Exception("导入的数据中“项目名称”不能为空，如：第" + (i + 2) + "行数据“项目名称”为空");
        }*/
        if(StringUtils.isBlank(record.getItemcode())){
            throw new Exception("导入的数据中“项目编码”不能为空，如：第" + (i + 2) + "行数据“项目编码”为空");
        }
        if(StringUtils.isBlank(record.getVisitid())){
            throw new Exception("导入的数据中“就诊id”不能为空，如：第" + (i + 2) + "行数据“就诊id”为空");
        }
        if(StringUtils.isBlank(record.getVisitid())){
            throw new Exception("导入的数据中“就诊id”不能为空，如：第" + (i + 2) + "行数据“就诊id”为空");
        }
        if(StringUtils.isBlank(record.getActionTypeName())){
            throw new Exception("导入的数据中“不合规行为类型”不能为空，如：第" + (i + 2) + "行数据“不合规行为类型”为空");
        }
        String actionTypeId = actionTypeMap.getOrDefault(record.getActionTypeName(), "").toString();
        record.setActionTypeId(actionTypeId);

        if(StringUtils.isBlank(record.getActionTypeId())){
            throw new Exception("导入的数据中“不合规行为类型”在系统中不存在，如：第" + (i + 2) + "行数据“不合规行为类型”数据");
        }

        if(StringUtils.isBlank(record.getActionName())){
            throw new Exception("导入的数据中“不合规行为”不能为空，如：第" + (i + 2) + "行数据“不合规行为”为空");
        }
        String actionId = actionListMap.getOrDefault(record.getActionName(), "").toString();
        record.setActionId(actionId);

        if(StringUtils.isBlank(record.getActionId())){
            throw new Exception("导入的数据中“不合规行为”在系统中不存在，如：第" + (i + 2) + "行数据“不合规行为”数据");
        }

        record.setProjectId(project.getProjectId());
        record.setProjectName(project.getProjectName());
        record.setBatchId(taskProjectBatch.getBatchId());
        record.setTaskBatchName(taskProjectBatch.getBatchName());
        record.setBusiType(MedicalConstant.ENGINE_BUSI_TYPE_MANUAL);

        String reviewStatus = reviewStatusMap.getOrDefault(record.getFirReviewStatus(), "init").toString();
        record.setFirReviewStatus(reviewStatus);
        record.setFirReviewTime(TimeUtil.getNowTime());
        if(StringUtils.isBlank(record.getFirReviewUsername())){
            record.setFirReviewUserid(user.getId());
            record.setFirReviewUsername(user.getRealname());
        }
    }

    private void batchQueryMasterInfo(List<MedicalUnreasonableActionVo> actionVoList,BufferedWriter fileWriter) throws Exception{
        //获取主表信息
        Set<String> visitidList = new HashSet();
        actionVoList.forEach(actionVo ->{
            visitidList.add(actionVo.getVisitid());
        });
        String visitIdFq = "VISITID:(\"" + StringUtil.join(visitidList, "\",\"") + "\")";
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery(visitIdFq);
        solrQuery.setFields("VISITID","CLIENTID","INSURANCETYPE","CLIENTNAME","SEX_CODE",
                "SEX","BIRTHDAY","YEARAGE","MONTHAGE","DAYAGE","VISITTYPE_ID","VISITTYPE",
                "VISITDATE","ORGID","ORGNAME","HOSPLEVEL","HOSPGRADE","ORGTYPE_CODE",
                "ORGTYPE","DEPTID","DEPTNAME","DEPTID_SRC","DEPTNAME_SRC","DOCTORID","DOCTORNAME",
                "TOTALFEE","LEAVEDATE","DISEASECODE","DISEASENAME",
                "YB_VISITID","HIS_VISITID","VISITID_DUMMY","VISITID_CONNECT","ZY_DAYS","ZY_DAYS_CALCULATE",
                "FUNDPAY","DATA_RESOUCE_ID","DATA_RESOUCE","ETL_SOURCE","ETL_SOURCE_NAME","ETL_TIME","HIS_VISITID");
        solrQuery.setRows(visitidList.size());
        List<DwbMasterInfoVo> masterList = SolrQueryGenerator.list(EngineUtil.DWB_MASTER_INFO,solrQuery,DwbMasterInfoVo.class,SolrUtil.initFieldMap(DwbMasterInfoVo.class));
        Map<String, DwbMasterInfoVo> masterMap = new HashMap<>();
        for (DwbMasterInfoVo masterInfo : masterList) {
            masterMap.put(masterInfo.getVisitid(),masterInfo);
        }

        actionVoList.forEach(actionVo ->{
            DwbMasterInfoVo masterInfo = masterMap.get(actionVo.getVisitid());
            if(masterInfo!=null){
                masterInfo.setCaseId(null);
                BeanUtil.copyProperties(masterInfo,actionVo);
            }
            actionVo.setCaseName(actionVo.getItemname());
            actionVo.setCaseId(actionVo.getItemcode());
            String template = "${batchId}_${itemCode}_${actionName}_${visitid}";
            Properties properties = new Properties();
            properties.put("batchId", actionVo.getBatchId());
            properties.put("itemCode", actionVo.getItemcode());
            properties.put("actionName", actionVo.getActionName());
            properties.put("visitid", actionVo.getVisitid());
            template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
            String id = MD5Util.MD5Encode(template, "UTF-8");
            actionVo.setId(id);
            try {
                commonWriteJson(fileWriter, actionVo, "id");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private void commonWriteJson(BufferedWriter bufWriter, Object bean, String idFiled) throws IOException {
        JSONObject commonDoc = new JSONObject();
        JSONObject jsonBean = JSONObject.parseObject(JSONObject.toJSON(bean).toString());
        commonDoc.put("id", jsonBean.get(idFiled));
        for(Map.Entry<String, Object> entry : jsonBean.entrySet()) {
            if(!"ID".equals(oConvertUtils.camelToUnderlineUpper(entry.getKey()))){
                commonDoc.put(oConvertUtils.camelToUnderlineUpper(entry.getKey()), entry.getValue());
            }
        }
        bufWriter.write(commonDoc.toJSONString());
        bufWriter.write(',');
        bufWriter.write("\n");
    }

    @AutoLog(value = "任务项目批次-ai辅助判定")
    @ApiOperation(value="任务项目批次-ai辅助判定", notes="任务项目批次-ai辅助判定")
    @PostMapping(value = "/aiAudit")
    public Result<?> aiAudit(String batchId) throws Exception {
        engineResultHandle.syncSolr2WarehouseByThread(batchId);
        return Result.ok("操作成功，开始执行AI辅助判定！");
    }


}
