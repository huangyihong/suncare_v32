package com.ai.modules.review.controller;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.modules.api.util.ApiTokenCommon;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.MedicalOtherDict;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.service.IReviewStatisticService;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: zhangpeng
 * @Date: 2021/7/21 09
 * @Description:
 */
@Slf4j
@Api(tags = "审核统计")
@RestController
@RequestMapping("/apiReviewStatistic")
public class ApiReviewStatisticController {

    @Autowired
    IReviewStatisticService service;

    @Autowired
    IMedicalActionDictService medicalActionDictService;

    @AutoLog(value = "审核统计-不合规行为列表查询")
    @ApiOperation(value = "审核统计-不合规行为列表查询", notes = "审核统计-不合规行为列表查询")
    @GetMapping(value = "/termActionData")
    public Result<?> termActionData(String batchIds,
                                    String projectIds,
                                    String pushStatus,
                                    String secPushStatus,
                                    Integer step,
                                    HttpServletRequest req) throws Exception {
        // 查询符合步骤的批次
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));

        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        if(StringUtils.isNotBlank(pushStatus)){
            solrQuery.addFilterQuery("PUSH_STATUS:1");
        } else if(StringUtils.isNotBlank(secPushStatus)){
            solrQuery.addFilterQuery("SEC_PUSH_STATUS:1");
        }
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        List<JSONObject> list = facetActionData(collection, solrQuery);
        return Result.ok(list);
    }

    @AutoLog(value = "全量统计")
    @ApiOperation(value = "全量统计", notes = "全量统计")
    @GetMapping(value = "/module0")
    public Result<?> module0(
            MedicalUnreasonableActionVo searchObj,
            String batchIds,
            String projectIds,
            Integer step,
            HttpServletRequest req) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        JSONObject jsonObject = this.service.module0Data(solrQuery, collection);

        return Result.ok(jsonObject);

    }

    @AutoLog(value = "全量统计导出")
    @ApiOperation(value = "全量统计导出", notes = "全量统计导出")
    @RequestMapping(value = "/module0Export")
    public Result<?> module0Export(
            MedicalUnreasonableActionVo searchObj,
            String batchIds,
            String projectIds,
            Integer step,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        JSONObject jsonObject = this.service.module0Data(solrQuery, collection);
        String[] titles = {"总记录数", "总就诊ID数", "总收费项目数", "总违规金额", "总违规基金金额", "已初审记录数", "已复审记录数"};
        String[] fields = {"count", "visitidCount", "itemcodeCount", "sumMinMoney", "sumActionMoney", "pushCount", "secPushCount"};
        this.service.export(jsonObject, titles, fields, "全量统计", response.getOutputStream());

        return null;

    }


    @AutoLog(value = "审核统计-初审实时-未判定统计")
    @ApiOperation(value = "审核统计-初审实时-未判定统计", notes = "审核统计-初审实时-未判定统计")
    @GetMapping(value = "/module1")
    public Result<?> module1(
            MedicalUnreasonableActionVo searchObj,
            String batchIds,
            String projectIds,
            Integer step,
            HttpServletRequest req) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        // facet
        // 查询
        JSONObject jsonObject = this.service.module1Data(solrQuery, collection);

        return Result.ok(jsonObject);

    }

    @AutoLog(value = "审核统计-初审实时-未判定统计导出")
    @ApiOperation(value = "审核统计-初审实时-未判定统计导出", notes = "审核统计-初审实时-未判定统计导出")
    @GetMapping(value = "/module1Export")
    public Result<?> module1Export(
            MedicalUnreasonableActionVo searchObj,
            String batchIds,
            String projectIds,
            Integer step,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        // facet
        // 查询
        JSONObject jsonObject = this.service.module1Data(solrQuery, collection);

        String[] titles = {"已完成判定记录数", "未判定就诊ID数", "未判定记录数", "未判定收费项目数", "未判定违规金额"};
        String[] fields = {"push_count", "unPush_visitidCount", "unPush_count", "unPush_itemcodeCount", "unPush_sumMinMoney"};
        this.service.export(jsonObject, titles, fields, "初审实时-未判定统计", response.getOutputStream());

        return null;

    }

    @AutoLog(value = "审核统计-复审实时-全量统计")
    @ApiOperation(value = "审核统计-复审实时-全量统计", notes = "审核统计-复审实时-全量统计")
    @GetMapping(value = "/module2")
    public Result<?> module2(
            String batchIds,
            String projectIds,
            String secReviewStatus,
            Integer step,
            HttpServletRequest req) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
//        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        solrQuery.addFilterQuery("SEC_PUSH_STATUS:1");
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        JSONObject jsonObject = this.service.module2Data(solrQuery, collection, secReviewStatus);

        return Result.ok(jsonObject);
    }

    @AutoLog(value = "审核统计-复审实时-全量统计导出")
    @ApiOperation(value = "审核统计-复审实时-全量统计导出", notes = "审核统计-复审实时-全量统计导出")
    @GetMapping(value = "/module2Export")
    public Result<?> module2Export(
            String batchIds,
            String projectIds,
            String secReviewStatus,
            Integer step,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
//        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        solrQuery.addFilterQuery("SEC_PUSH_STATUS:1");
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        JSONObject jsonObject = this.service.module2Data(solrQuery, collection, secReviewStatus);
        List<String> titles = new ArrayList<>(Arrays.asList("就诊ID数", "记录数", "收费项目数", "违规金额", "违规基金金额"));
        List<String> fields = new ArrayList<>(Arrays.asList("visitidCount", "count", "itemcodeCount", "sumMinMoney", "sumActionMoney"));
        if (StringUtils.isNotBlank(secReviewStatus)) {
            titles.addAll(Arrays.asList("就诊ID占比", "记录占比", "违规金额占比"));
            fields.addAll(Arrays.asList("visitidRatio", "countRatio", "sumMinMoneyRatio"));
        }


        this.service.export(jsonObject, titles.toArray(new String[0]), fields.toArray(new String[0]), "复审实时-全量统计", response.getOutputStream());

        return null;

    }

    @AutoLog(value = "审核统计-复审实时-不合规行为统计")
    @ApiOperation(value = "审核统计-复审实时-不合规行为统计", notes = "审核统计-复审实时-不合规行为统计")
    @GetMapping(value = "/module3")
    public Result<?> module3(
            String batchIds,
            String projectIds,
            String actionId,
            String secReviewStatus,
            Integer step,
            HttpServletRequest req) throws Exception {
        // 构造主表条件
//        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        SolrQuery solrQuery = new SolrQuery("*:*");
        if (StringUtils.isNotBlank(actionId)) {
            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
        }

        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        solrQuery.addFilterQuery("SEC_PUSH_STATUS:1");
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        JSONArray actionData = this.service.module3Data(solrQuery, collection, secReviewStatus);
        // 返回结构模仿IPage
        JSONObject result = new JSONObject();
        result.put("records", actionData);
        result.put("total", actionData.size());
        return Result.ok(result);

    }

    @AutoLog(value = "审核统计-复审实时-不合规行为统计导出")
    @ApiOperation(value = "审核统计-复审实时-不合规行为统计导出", notes = "审核统计-复审实时-不合规行为统计导出")
    @GetMapping(value = "/module3Export")
    public Result<?> module3Export(
            String batchIds,
            String projectIds,
            String actionId,
            String secReviewStatus,
            Integer step,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        // 构造主表条件
//        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        SolrQuery solrQuery = new SolrQuery("*:*");
        if (StringUtils.isNotBlank(actionId)) {
            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
        }

        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        solrQuery.addFilterQuery("SEC_PUSH_STATUS:1");
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        JSONArray actionData = this.service.module3Data(solrQuery, collection, secReviewStatus);
        List<String> titles = new ArrayList<>(Arrays.asList("不合规行为", "就诊ID数", "记录数", "收费项目数", "违规金额", "违规基金金额"));
        List<String> fields = new ArrayList<>(Arrays.asList("actionName", "visitidCount", "count", "itemcodeCount", "sumMinMoney", "sumActionMoney"));
        if (StringUtils.isNotBlank(secReviewStatus)) {
            titles.addAll(Arrays.asList("就诊ID占比", "记录占比", "违规金额占比"));
            fields.addAll(Arrays.asList("visitidRatio", "countRatio", "sumMinMoneyRatio"));
            for (int i = 0, len = actionData.size(); i < len; i++) {
                JSONObject jsonObject = actionData.getJSONObject(i);
                double visitidRatio = jsonObject.getLongValue("visitidCount") * 100.0 / jsonObject.getLongValue("totalVisitidCount");
                jsonObject.put("visitidRatio", (double)Math.round(visitidRatio * 100) / 100 + "%");
                double countRatio = jsonObject.getLongValue("count") * 100.0 / jsonObject.getLongValue("totalCount");
                jsonObject.put("countRatio", (double)Math.round(countRatio * 100) / 100 + "%");
                double sumMinMoneyRatio = jsonObject.getLongValue("sumMinMoney") * 100.0 / jsonObject.getLongValue("totalSumMinMoney");
                jsonObject.put("sumMinMoneyRatio", (double)Math.round(sumMinMoneyRatio * 100) / 100 + "%");
            }
        }

        this.service.export(actionData, titles.toArray(new String[0]), fields.toArray(new String[0]), "不合规行为统计", response.getOutputStream());

        return null;

    }


    @AutoLog(value = "审核统计-白名单归因统计")
    @ApiOperation(value = "审核统计-白名单归因统计", notes = "审核统计-白名单归因统计")
    @GetMapping(value = "/module4")
    public Result<?> module4(
            String batchIds,
            String projectIds,
            String actionId,
            Integer step,
            HttpServletRequest req) throws Exception {
        // 构造主表条件
//        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        SolrQuery solrQuery = new SolrQuery("*:*");
        if (StringUtils.isNotBlank(actionId)) {
            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
        }

        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");

        solrQuery.addFilterQuery("FIR_REVIEW_STATUS:white AND FIR_REVIEW_CLASSIFY:?* OR (SEC_REVIEW_STATUS:white AND SEC_REVIEW_CLASSIFY:?*)");

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        JSONArray actionData = this.service.module4Data(solrQuery, collection);

        // 返回结构模仿IPage
        JSONObject result = new JSONObject();
        result.put("records", actionData);
        result.put("total", actionData.size());
        return Result.ok(result);

    }

    @AutoLog(value = "审核统计-白名单归因统计")
    @ApiOperation(value = "审核统计-白名单归因统计", notes = "审核统计-白名单归因统计")
    @GetMapping(value = "/module4Export")
    public Result<?> module4Export(
            String batchIds,
            String projectIds,
            String actionId,
            Integer step,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        // 构造主表条件
//        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        SolrQuery solrQuery = new SolrQuery("*:*");
        if (StringUtils.isNotBlank(actionId)) {
            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
        }

        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");

        solrQuery.addFilterQuery("FIR_REVIEW_STATUS:white AND FIR_REVIEW_CLASSIFY:?* OR (SEC_REVIEW_STATUS:white AND SEC_REVIEW_CLASSIFY:?*)");

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        JSONArray actionData = this.service.module4Data(solrQuery, collection);

        List<String> titles = new ArrayList<>(Arrays.asList("不合规行为", "就诊ID数", "记录数", "收费项目数", "违规金额", "违规基金金额"));
        List<String> fields = new ArrayList<>(Arrays.asList("actionName", "visitidCount", "count", "itemcodeCount", "sumMinMoney", "sumActionMoney"));

        List<MedicalOtherDict> dictList = ApiTokenCommon.queryOtherDictListByType("reasontype");
        Set<String> codeSet = dictList.stream().map(MedicalOtherDict::getCode).collect(Collectors.toSet());
        Set<String> keySet = new HashSet<>();
        // 格式化数据
        actionData.forEach(r -> {
            JSONObject jsonObject = (JSONObject)r;
            jsonObject.keySet().forEach(j -> {
                if(codeSet.contains(j)){
                    keySet.add(j);
                    long val = jsonObject.getLongValue(j);
                    double valRatio = val * 100.0 / jsonObject.getLongValue("totalCount");
                    jsonObject.put(j, val + " / " + (double)Math.round(valRatio * 100) / 100 + "%");
                }
            });
            double valRatio = jsonObject.getLongValue("count") * 100.0 / jsonObject.getLongValue("totalCount");
            jsonObject.put("actionRatio", (double)Math.round(valRatio * 100) / 100 + "%");
        });
        // 动态添加标题
        dictList.forEach(r -> {
            if(keySet.contains(r.getCode())){
                titles.add(r.getValue() + "数量");
                fields.add(r.getCode());
            }
        });
        titles.add("不合规行为占比");
        fields.add("actionRatio");

        this.service.export(actionData, titles.toArray(new String[0]), fields.toArray(new String[0]), "白名单归因统计", response.getOutputStream());
        return null;
    }

    @AutoLog(value = "审核统计-初复审结果对比")
    @ApiOperation(value = "审核统计-初复审结果对比", notes = "审核统计-初复审结果对比")
    @GetMapping(value = "/module5")
    public Result<?> module5(
            String batchIds,
            String projectIds,
            String actionId,
            String secReviewStatus,
            Integer step,
            HttpServletRequest req) throws Exception {
        // 构造主表条件
//        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        SolrQuery solrQuery = new SolrQuery("*:*");
        if (StringUtils.isNotBlank(actionId)) {
            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
        }

        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        solrQuery.addFilterQuery("PUSH_STATUS:1 OR SEC_PUSH_STATUS:1");

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        JSONArray actionData = this.service.module5Data(solrQuery, collection, secReviewStatus);
        // 返回结构模仿IPage
        JSONObject result = new JSONObject();
        result.put("records", actionData);
        result.put("total", actionData.size());
        return Result.ok(result);

    }

    @AutoLog(value = "审核统计-初复审结果对比")
    @ApiOperation(value = "审核统计-初复审结果对比", notes = "审核统计-初复审结果对比")
    @GetMapping(value = "/module5Export")
    public Result<?> module5Export(
            String batchIds,
            String projectIds,
            String actionId,
            String secReviewStatus,
            Integer step,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        // 构造主表条件
//        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        SolrQuery solrQuery = new SolrQuery("*:*");
        if (StringUtils.isNotBlank(actionId)) {
            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
        }

        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        solrQuery.addFilterQuery("PUSH_STATUS:1 OR SEC_PUSH_STATUS:1");

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        JSONArray actionData = this.service.module5Data(solrQuery, collection, secReviewStatus);
        // 格式化数据
        actionData.forEach(r -> {
            JSONObject jsonObject = (JSONObject)r;
            JSONObject facet1 = (JSONObject) jsonObject.remove("facet1");
            JSONObject facet2 = (JSONObject) jsonObject.remove("facet2");
            jsonObject.put("visitidCount", facet2.getLongValue("visitidCount") - facet1.getLongValue("visitidCount"));
            jsonObject.put("count", facet2.getLongValue("count") - facet1.getLongValue("count"));
            jsonObject.put("itemcodeCount", facet2.getLongValue("itemcodeCount") - facet1.getLongValue("itemcodeCount"));
            jsonObject.put("sumMinMoney", facet2.getDoubleValue("sumMinMoney") - facet1.getDoubleValue("sumMinMoney"));
            jsonObject.put("sumActionMoney", facet2.getDoubleValue("sumActionMoney") - facet1.getDoubleValue("sumActionMoney"));
        });

        List<String> titles = Arrays.asList("不合规行为", "就诊ID数变化", "记录数变化", "收费项目数变化", "违规金额变化", "违规基金金额变化");
        List<String> fields = Arrays.asList("actionName", "visitidCount", "count", "itemcodeCount", "sumMinMoney", "sumActionMoney");

        this.service.export(actionData, titles.toArray(new String[0]), fields.toArray(new String[0]), "初复审结果对比", response.getOutputStream());
        return null;

    }


    private List<TaskProjectBatch> queryBatch(String batchIds, String projectIds, Integer step) {

        Map<String, String> map = new HashMap<>();
        map.put("batchIds", batchIds);
        map.put("projectIds", projectIds);
        if(step != null){
            map.put("step", step + "");
        }

        List<TaskProjectBatch> list = ApiTokenUtil.getArray("/task/taskProjectBatch/queryListFilter", map, TaskProjectBatch.class);

        return list;
    }

    private List<JSONObject> facetActionData(String collection, SolrQuery solrQuery) throws Exception {

        // facet
        JSONObject termFacet = new JSONObject();
        termFacet.put("type", "terms");
        termFacet.put("field", "ACTION_ID");
        termFacet.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        termFacet.put("overrequest", Integer.MAX_VALUE);
        JSONObject facetChild = new JSONObject();
        facetChild.put("actionName", "max(ACTION_NAME)");
        termFacet.put("facet", facetChild);

        List<JSONObject> list = new ArrayList<>();
        // 查询
        SolrUtil.jsonFacet(collection
                , solrQuery.getFilterQueries(), termFacet.toJSONString()
                , list::add);

        List<String> actionIdList = list.stream().map(r -> String.valueOf(r.get("val"))).distinct().collect(Collectors.toList());
        Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
        list.forEach(r -> r.put("name", actionNameMap.get(r.getString("val"))));

        return list;
    }
}
