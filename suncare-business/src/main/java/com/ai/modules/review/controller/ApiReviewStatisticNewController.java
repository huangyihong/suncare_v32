package com.ai.modules.review.controller;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.modules.api.util.ApiTokenCommon;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.MedicalOtherDict;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.service.IReviewStatisticNewService;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
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
@RequestMapping("/apiReviewStatisticNew")
public class ApiReviewStatisticNewController {

    @Autowired
    IReviewStatisticNewService service;

    @Autowired
    IMedicalActionDictService medicalActionDictService;

    @Autowired
    IApiTaskService apiTaskService;

    @AutoLog(value = "不合规行为列表查询")
    @ApiOperation(value = "不合规行为列表查询", notes = "不合规行为列表查询")
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
        //dwb_master_info 项目总数统计
        if(StringUtils.isBlank(projectIds)){
            projectIds = batchList.get(0).getProjectId();
        }
        TaskProject  project = apiTaskService.findTaskProject(projectIds);
        jsonObject.put("dwbMasterInfo",this.service.module0MasterInfoData(project));
        jsonObject.put("dwbChargeDetail",this.service.module0ChargeDetailData(project));
        return Result.ok(jsonObject);

    }


    @AutoLog(value = "初审实时-未判定统计")
    @ApiOperation(value = "初审实时-未判定统计", notes = "初审实时-未判定统计")
    @GetMapping(value = "/module1")
    public Result<?> module1(
            String actionId,
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
//        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        if(StringUtils.isNotBlank(actionId)){
            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
        }
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        // facet
        // 查询
        JSONArray list = this.service.module1Data(solrQuery, collection);
        // 返回结构模仿IPage
        JSONObject result = new JSONObject();
        result.put("records", list);
        result.put("total", list.size());
        return Result.ok(result);

    }

    @AutoLog(value = "初审实时-未判定统计导出")
    @ApiOperation(value = "初审实时-未判定统计导出", notes = "初审实时-未判定统计导出")
    @GetMapping(value = "/module1Export")
    public Result<?> module1Export(
            String actionId,
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
//        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        // 导出全量不过滤
        /*if(StringUtils.isNotBlank(actionId)){
            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
        }*/
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        // facet
        // 查询
        JSONArray list = this.service.module1Data(solrQuery, collection);

        String[] titles = {"不合规行为名称","已完成判定记录数", "未判定记录数", "未判定就诊ID数", "未判定收费项目数", "未判定违规金额", "未判定基金金额"};
        String[] fields = {"actionName", "review_count", "unReview_count", "unReview_visitidCount", "unReview_itemcodeCount", "unReview_sumMinMoney", "unReview_sumActionMoney"};
        this.service.export(list, titles, fields, "初审实时-未判定统计", response.getOutputStream());

        return null;

    }

    @AutoLog(value = "初审实时-全量统计")
    @ApiOperation(value = "初审实时-全量统计", notes = "初审实时-全量统计")
    @GetMapping(value = "/module2")
    public Result<?> module2(
            String batchIds,
            String projectIds,
            String firReviewStatus,
            String actionId,
            String groupBy,
            Integer step,
            HttpServletRequest req) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        if(StringUtils.isNotBlank(firReviewStatus)){
            solrQuery.addFilterQuery("FIR_REVIEW_STATUS:" + firReviewStatus);
        }
        if(StringUtils.isNotBlank(actionId)){
            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
        }
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        JSONArray list = this.service.module2Data(solrQuery, collection, groupBy);
        // 返回结构模仿IPage
        JSONObject result = new JSONObject();
        result.put("records", list);
        result.put("total", list.size());
        return Result.ok(result);
    }

    @AutoLog(value = "初审实时-全量统计导出")
    @ApiOperation(value = "初审实时-全量统计导出", notes = "初审实时-全量统计导出")
    @GetMapping(value = "/module2Export")
    public Result<?> module2Export(
            String batchIds,
            String projectIds,
            String firReviewStatus,
            String actionId,
            String groupBy,
            Integer step,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
//        if(StringUtils.isNotBlank(firReviewStatus)){
//            solrQuery.addFilterQuery("FIR_REVIEW_STATUS:" + firReviewStatus);
//        }
//        if(StringUtils.isNotBlank(actionId)){
//            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
//        }
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        JSONArray list = this.service.module2ExportData(solrQuery, collection, "ACTION_ID", "FIR_REVIEW_STATUS");

        String[] titles = {
                "不合规行为名称", "就诊ID数","记录数", "收费项目数", "违规金额", "违规基金金额",
                "黑名单就诊ID数","黑名单记录数",  "黑名单收费项目数", "黑名单违规金额", "黑名单违规基金金额",
                "白名单就诊ID数","白名单记录数",  "白名单收费项目数", "白名单违规金额", "白名单违规基金金额",
                "灰名单就诊ID数","灰名单记录数",  "灰名单收费项目数", "灰名单违规金额", "灰名单违规基金金额",
        };
        String[] fields = {
                "valName", "visitidCount", "count", "itemcodeCount", "sumMinMoney", "sumActionMoney",
                "blank_visitidCount","blank_count",  "blank_itemcodeCount", "blank_sumMinMoney", "blank_sumActionMoney",
                "white_visitidCount","white_count",  "white_itemcodeCount", "white_sumMinMoney", "white_sumActionMoney",
                "grey_visitidCount","grey_count",  "grey_itemcodeCount", "grey_sumMinMoney", "grey_sumActionMoney",
        };
        this.service.export(list, titles, fields, "初审实时-全量统计", response.getOutputStream());

        return null;

    }

    @AutoLog(value = "复审实时-全量统计")
    @ApiOperation(value = "复审实时-全量统计", notes = "复审实时-全量统计")
    @GetMapping(value = "/module3")
    public Result<?> module3(
            String batchIds,
            String projectIds,
            String secReviewStatus,
            String actionId,
            String groupBy,
            Integer step,
            HttpServletRequest req) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        solrQuery.addFilterQuery("PUSH_STATUS:1");
        if(StringUtils.isNotBlank(secReviewStatus)){
            solrQuery.addFilterQuery("SEC_REVIEW_STATUS:" + secReviewStatus);
        }
        if(StringUtils.isNotBlank(actionId)){
            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
        }
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        JSONArray list = this.service.module2Data(solrQuery, collection, groupBy);
        // 返回结构模仿IPage
        JSONObject result = new JSONObject();
        result.put("records", list);
        result.put("total", list.size());
        return Result.ok(result);
    }

    @AutoLog(value = "复审实时-全量统计导出")
    @ApiOperation(value = "复审实时-全量统计导出", notes = "复审实时-全量统计导出")
    @GetMapping(value = "/module3Export")
    public Result<?> module3Export(
            String batchIds,
            String projectIds,
            String secReviewStatus,
            String actionId,
            String groupBy,
            Integer step,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        solrQuery.addFilterQuery("PUSH_STATUS:1");
//        if(StringUtils.isNotBlank(secReviewStatus)){
//            solrQuery.addFilterQuery("SEC_REVIEW_STATUS", secReviewStatus);
//        }
//        if(StringUtils.isNotBlank(actionId)){
//            solrQuery.addFilterQuery("ACTION_ID", actionId);
//        }
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        JSONArray list = this.service.module2ExportData(solrQuery, collection, "ACTION_ID", "SEC_REVIEW_STATUS");

        String[] titles = {
                "不合规行为名称", "就诊ID数","记录数", "收费项目数", "违规金额", "违规基金金额",
                "黑名单就诊ID数","黑名单记录数",  "黑名单收费项目数", "黑名单违规金额", "黑名单违规基金金额",
                "白名单就诊ID数","白名单记录数",  "白名单收费项目数", "白名单违规金额", "白名单违规基金金额",
                "灰名单就诊ID数","灰名单记录数",  "灰名单收费项目数", "灰名单违规金额", "灰名单违规基金金额",
        };
        String[] fields = {
                "valName", "visitidCount", "count", "itemcodeCount", "sumMinMoney", "sumActionMoney",
                "blank_visitidCount","blank_count",  "blank_itemcodeCount", "blank_sumMinMoney", "blank_sumActionMoney",
                "white_visitidCount","white_count",  "white_itemcodeCount", "white_sumMinMoney", "white_sumActionMoney",
                "grey_visitidCount","grey_count",  "grey_itemcodeCount", "grey_sumMinMoney", "grey_sumActionMoney",
        };
        this.service.export(list, titles, fields, "复审实时-全量统计", response.getOutputStream());

        return null;

    }


    @AutoLog(value = "白名单归因统计")
    @ApiOperation(value = "白名单归因统计", notes = "白名单归因统计")
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

    @AutoLog(value = "白名单归因统计")
    @ApiOperation(value = "白名单归因统计", notes = "白名单归因统计")
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
//        if (StringUtils.isNotBlank(actionId)) {
//            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
//        }

        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");

        solrQuery.addFilterQuery("FIR_REVIEW_STATUS:white AND FIR_REVIEW_CLASSIFY:?* OR (SEC_REVIEW_STATUS:white AND SEC_REVIEW_CLASSIFY:?*)");

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        JSONArray actionData = this.service.module4Data(solrQuery, collection);

        List<String> titles = new ArrayList<>(Arrays.asList("不合规行为", "白名单记录数"));
        List<String> fields = new ArrayList<>(Arrays.asList("actionName", "count"));

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
        });
        // 动态添加标题
        dictList.forEach(r -> {
            if(keySet.contains(r.getCode())){
                titles.add(r.getValue() + "数量");
                fields.add(r.getCode());
            }
        });

        this.service.export(actionData, titles.toArray(new String[0]), fields.toArray(new String[0]), "白名单归因统计", response.getOutputStream());
        return null;
    }

    @AutoLog(value = "初复审结果对比")
    @ApiOperation(value = "初复审结果对比", notes = "初复审结果对比")
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

    @AutoLog(value = "初复审结果对比")
    @ApiOperation(value = "初复审结果对比", notes = "初复审结果对比")
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
//        if (StringUtils.isNotBlank(actionId)) {
//            solrQuery.addFilterQuery("ACTION_ID:" + actionId);
//        }

        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        solrQuery.addFilterQuery("PUSH_STATUS:1 OR SEC_PUSH_STATUS:1");

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        JSONArray actionData = this.service.module5ExportData(solrQuery, collection);

        String[] titles = {
                "不合规行为", "就诊ID数变化", "记录数变化", "收费项目数变化", "违规金额变化", "违规基金金额变化",
                 "黑名单就诊ID数变化","黑名单记录数变化", "黑名单收费项目数变化", "黑名单违规金额变化", "黑名单违规基金金额变化",
                "白名单就诊ID数变化","白名单记录数变化",  "白名单收费项目数变化", "白名单违规金额变化", "白名单违规基金金额变化",
                "灰名单就诊ID数变化","灰名单记录数变化",  "灰名单收费项目数变化", "灰名单违规金额变化", "灰名单违规基金金额变化",
        };
        String[] fields = {
                "actionName", "visitidCount", "count", "itemcodeCount", "sumMinMoney", "sumActionMoney",
                "blank_visitidCount","blank_count",  "blank_itemcodeCount", "blank_sumMinMoney", "blank_sumActionMoney",
                "white_visitidCount","white_count",  "white_itemcodeCount", "white_sumMinMoney", "white_sumActionMoney",
                "grey_visitidCount","grey_count",  "grey_itemcodeCount", "grey_sumMinMoney", "grey_sumActionMoney",
        };

        this.service.export(actionData, titles, fields, "初复审结果对比", response.getOutputStream());
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

    @AutoLog(value = "初审实时-医疗机构")
    @ApiOperation(value = "初审实时-医疗机构", notes = "初审实时-医疗机构")
    @GetMapping(value = "/module6")
    public Result<?> module6(
            String batchIds,
            String projectIds,
            String groupBy,
            Integer step,
            HttpServletRequest req) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        JSONArray list = this.service.module6Data(solrQuery, collection, groupBy);
        // 返回结构模仿IPage
        JSONObject result = new JSONObject();
        result.put("records", list);
        result.put("total", list.size());
        return Result.ok(result);
    }

    @AutoLog(value = "初审实时-医疗机构导出")
    @ApiOperation(value = "初审实时-医疗机构导出", notes = "初审实时-医疗机构导出")
    @GetMapping(value = "/module6Export")
    public Result<?> module6Export(
            String batchIds,
            String projectIds,
            String groupBy,
            Integer step,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        JSONArray list = this.service.module6ExportData(solrQuery, collection, groupBy);

        String[] titles = {
                "医疗机构名称", "输出记录数", "输出基金金额",
                "未判定总就诊ID数", "未判定总记录数","未判定总基金金额",
                "已判定总就诊ID数","已判定总记录数","已判定总基金金额",
                "黑名单记录数","黑名单记录占比","黑名单基金金额	","黑名单基金金额占比","黑名单就诊ID数",
                "白名单记录数","白名单记录占比","白名单基金金额","白名单基金金额占比","白名单就诊ID数",
                "灰名单记录数","灰名单记录占比","灰名单基金金额","灰名单基金金额占比","灰名单就诊ID数"
        };
        String[] fields = {
                "valName", "total_count", "total_sumActionMoney",
                "nojudge_visitidcount","nojudge_count","nojudge_sumActionMoney",
                "judge_visitidcount","judge_count","judge_sumActionMoney",
                "blank_count","blank_countRatio","blank_sumActionMoney","blank_sumActionMoneyRatio","blank_visitidcount",
                "white_count","white_countRatio","white_sumActionMoney","white_sumActionMoneyRatio","white_visitidcount",
                "grey_count","grey_countRatio","grey_sumActionMoney","grey_sumActionMoneyRatio","grey_visitidcount"
        };
        this.service.export(list, titles, fields, "初审实时-医疗机构", response.getOutputStream());

        return null;

    }

    @AutoLog(value = "初审-规则级别统计导出")
    @ApiOperation(value = "初审-规则级别统计导出", notes = "初审-规则级别统计导出")
    @GetMapping(value = "/module7Export")
    public Result<?> module7Export(
            String batchIds,
            String projectIds,
            String groupBy,
            Integer step,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        List<TaskProjectBatch> batchList = queryBatch(batchIds, projectIds, step);
        if (batchList.size() == 0) {
            return Result.ok(new JSONObject());
        }
        String batchIdsStr = batchList.stream().map(TaskProjectBatch::getBatchId).collect(Collectors.joining(" , "));
        // 构造主表条件
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:(" + batchIdsStr + ")");
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        JSONArray list = this.service.module2Data(solrQuery, collection, groupBy);

        String[] titles = {
                "规则级别", "线索就诊ID数(去重)","线索记录数", "线索收费项目数(去重)", "线索违规金额", "线索基金金额",
                "就诊ID占比","记录占比",  "违规金额占比"
        };
        String[] fields = {
                "valName", "visitidCount", "count", "itemcodeCount", "sumMinMoney", "sumActionMoney",
                "visitidCountRatio","countRatio",  "sumMinMoneyRatio" };
        this.service.export(list, titles, fields, "初审实时-全量统计", response.getOutputStream());

        return null;

    }
}
