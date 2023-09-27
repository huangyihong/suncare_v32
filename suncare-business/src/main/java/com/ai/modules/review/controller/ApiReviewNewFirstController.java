package com.ai.modules.review.controller;

import com.ai.common.MedicalConstant;
import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.*;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.entity.MedicalExportTask;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.config.service.IMedicalExportTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.dto.*;
import com.ai.modules.review.service.IDynamicFieldService;
import com.ai.modules.review.service.IReviewNewFirstService;
import com.ai.modules.review.service.IReviewService;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.review.vo.ReviewSystemDrugViewVo;
import com.ai.modules.task.entity.TaskActionFieldCol;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.internal.Function;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "项目初审")
@RestController
@RequestMapping("/apiReviewFirst")
public class ApiReviewNewFirstController {

    @Autowired
    IReviewNewFirstService service;

    @Autowired
    IReviewService reviewService;

    @Autowired
    IMedicalExportTaskService medicalExportTaskService;

    @Autowired
    IMedicalDictService medicalDictService;

    @Autowired
    IDynamicFieldService dynamicFieldService;

    @Autowired
    IMedicalActionDictService medicalActionDictService;

//    @Autowired
//    ITaskActionFieldColService taskActionFieldColService;


    private static Map<String, String> FIELD_DRUG_MAPPING = SolrUtil.initFieldMap(ReviewSystemDrugViewVo.class);

    /*private String initMasterQuery(DwbMasterInfoDyParam masterInfoParam) {
        // DWB_MASTER_INFO表参数
        JSONObject masterJson = (JSONObject) JSONObject.toJSON(masterInfoParam);
        String masterFq = masterJson.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> oConvertUtils.camelToUnderlineUpper(e.getKey()) + ":" + e.getValue())
                .collect(Collectors.joining(" AND "));
        return masterFq;
    }*/

    @AutoLog(value = "系统审核-规则结果分页列表查询")
    @ApiOperation(value = "系统审核-规则结果分页列表查询", notes = "系统审核-规则结果分页列表查询")
    @GetMapping(value = "/dynamicColsList")
    public Result<?> queryPageList(
            MedicalUnreasonableActionVo searchObj,
//            DwbMasterInfoDyParam masterInfoParam,
            String ruleId,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "fields") String fields,
            String groupBys,
            String dynamicSearch,
            HttpServletRequest req) throws Exception {
        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        // 构造动态查询条件
        List<String> searchFqs = dynamicFieldService.getSearchFqs(dynamicSearch);
        if (searchFqs.size() > 0) {
            solrQuery.addFilterQuery(searchFqs.toArray(new String[0]));
        }

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        // 试算
        if (StringUtils.isBlank(searchObj.getBatchId())) {
            solrQuery.addFilterQuery("BATCH_ID:" + ruleId);
            collection = EngineUtil.MEDICAL_TRAIL_ACTION;
        }

        // 分割表和字段
        Map<String, Set<String>> tabFieldMap = new HashMap<>();
        for (String filed : fields.split(",")) {
            String[] tabFiledArray = filed.split("\\.");
            if(tabFiledArray.length == 1){
                tabFiledArray = new String[]{EngineUtil.MEDICAL_UNREASONABLE_ACTION, tabFiledArray[0]};
            }
            if ("action".equals(tabFiledArray[0])) {
                continue;
            }
            if (tabFiledArray[1].startsWith("ALIA")) {
                tabFiledArray[1] = tabFiledArray[1] + ":" + tabFiledArray[1].substring(tabFiledArray[1].indexOf("_") + 1);
            }
            Set<String> fieldList = tabFieldMap.computeIfAbsent(tabFiledArray[0], k -> new HashSet<>());
            fieldList.add(tabFiledArray[1]);
        }

        if (StringUtils.isNotBlank(groupBys)) {

            List<String> groupByList = Arrays.asList(groupBys.split(","));
            // fields: val,count, sum(ACTION_MONEY), sum(MAX_ACTION_MONEY), sum(MIN_MONEY), sum(MAX_MONEY)
            List<String> facetFields = tabFieldMap.remove(EngineUtil.MEDICAL_UNREASONABLE_ACTION).stream().filter(r -> !groupByList.contains(r)).collect(Collectors.toList());
            JSONObject facetChild = new JSONObject();
            for (String field : facetFields) {
                facetChild.put(field, field);

            }

            // 不合规行为名称替换为编码
            boolean isGroupActionName = groupByList.contains("ACTION_NAME");
            if(isGroupActionName && !groupByList.contains("ACTION_ID")){
                groupByList.set(groupByList.indexOf("ACTION_NAME"), "ACTION_ID");
                facetChild.put("max(ACTION_NAME)", "ACTION_NAME");
            }

            Map<String, String> linkChild = new HashMap<>();

            Set<String> linkFields = dynamicFieldService.getFromOtherField(tabFieldMap);
            for (String field : linkFields) {
                linkChild.put("max(" + field + ")", field);
            }

            StringBuilder sb = new StringBuilder("facet(" + EngineUtil.MEDICAL_UNREASONABLE_ACTION + ",q=\"*:*\"");

            String column = req.getParameter(QueryGenerator.ORDER_COLUMN);
            String orderType = req.getParameter(QueryGenerator.ORDER_TYPE);
            // 排序
            sb.append(" ,bucketSorts=\"");
            if (StringUtils.isNotBlank(column) && StringUtils.isNotBlank(orderType)) {
                String[] cols = column.split(",");
                String[] orders = orderType.split(",");
                String[] colOrders = new String[cols.length];
                for (int i = 0, len = cols.length; i < len; i++) {
                    colOrders[i] = cols[i] + " " + orders[i];
                }
                sb.append(StringUtils.join(colOrders, ","));
            } else {
                sb.append(groupByList.get(0)).append(" asc");
            }
            sb.append("\"");
            // 分组
            sb.append(",buckets=\"").append(StringUtils.join(groupByList,",")).append("\"");

            for (String fq : solrQuery.getFilterQueries()) {
                sb.append(",fq=\"").append(fq.replaceAll("\"", "\\\\\"")).append("\"");
            }
            String countFacet = "let(a=" + sb.toString() + ",rows=-1,count(*)),count=length(a))";
            // 分页
            sb.append(",offset=").append(pageSize * (pageNo - 1));
            sb.append(",rows=").append(pageSize);
            // 统计
            sb.append(",").append(StringUtils.join(facetChild.keySet(), ","));
            // 关联
            sb.append(",").append(StringUtils.join(linkChild.keySet(), ","));
            sb.append(")");

            Page<Map<String, Object>> page = new Page<>(pageNo, pageSize);

            int count = Integer.parseInt(SolrUtil.stream(countFacet).get(0).get("count").toString());
            page.setTotal(count);
            if (count > 0) {
                List<Map<String, Object>> list = SolrUtil.stream(sb.toString());
                for (Map<String, Object> map : list) {
                    for (Map.Entry<String, String> entry : linkChild.entrySet()) {
                        map.put(entry.getValue(), map.remove(entry.getKey()));
                    }
                    String id = null;
                    for (String groupBy : groupByList) {
                        String groupByVal = map.get(groupBy).toString();
                        id = id == null ? groupByVal : (id + "::" + groupByVal);
                    }
                    map.put("id", id);
                }
                // 数据库反查不合规行为名称
                if(isGroupActionName){
                    List<String> actionIdList = list.stream().map(r -> String.valueOf(r.get("ACTION_ID"))).distinct().collect(Collectors.toList());
                    Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
                    list.forEach(r -> {
                        Object actionId = r.get("ACTION_ID");
                        if(actionId != null){
                            String actionName = actionNameMap.get(actionId.toString());
                            if(actionName != null){
                                r.put("ACTION_NAME",actionName);
                            } else {
                                r.put("ACTION_NAME", r.get("max(ACTION_NAME)"));
                            }
                        }
                    });
                }

                page.setRecords(list);
                dynamicFieldService.addGroupAttrFromOther(list, tabFieldMap);

            } else {
                page.setRecords(new ArrayList<>());
            }

            return Result.ok(page);
        }

        Page<SolrDocument> page = new Page<>(pageNo, pageSize);
        IPage<SolrDocument> pageList = reviewService.pageDynamicResult(tabFieldMap, solrQuery, collection, page);
        return Result.ok(pageList);
    }

    @AutoLog(value = "系统审核-模型动态字段结果导出")
    @ApiOperation(value = "系统审核-模型动态字段结果导出", notes = "系统审核-模型动态字段结果导出")
    @GetMapping(value = "/dynamicColsExportByCase")
    public Result<?> dynamicColsExportByCase(
            @RequestParam(name = "batchId") String batchId,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            String[] values = entry.getValue();
            if (values.length > 0) {
                params.put(entry.getKey(), values[0]);
            }
        }
        List<TaskBatchBreakRuleDel> ruleDelList = ApiTokenUtil.getArray("/task/taskBatchBreakRuleDel/queryList", params, TaskBatchBreakRuleDel.class);
        List<String> caseIdList = ruleDelList.stream().map(TaskBatchBreakRuleDel::getCaseId).distinct().collect(Collectors.toList());
        if(caseIdList.size() == 0){
            return Result.error("没有需要导出的模型");
        }

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;


        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:" + batchId);
        solrQuery.addFilterQuery("CASE_ID:(" + StringUtil.join(caseIdList, " OR ") + ")");

        return this.exportMultiActionFile(collection, solrQuery, null, false, response);
    }


    @AutoLog(value = "系统审核-动态字段结果导出")
    @ApiOperation(value = "系统审核-动态字段结果导出", notes = "系统审核-动态字段结果导出")
    @RequestMapping(value = "/dynamicColsExport")
    public Result<?> dynamicColsExport(
            MedicalUnreasonableActionVo searchObj,
//            DwbMasterInfoDyParam masterInfoParam,
            String ruleId,
            String fixCols,
            String groupBys,
            String dynamicSearch,
            String fields,
            String fieldTitles,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {

      /*  SolrDocumentList documents = SolrQueryGenerator.list(EngineUtil.MEDICAL_UNREASONABLE_ACTION, new String[]{"BATCH_ID:bc4a0484ca41fbe060006c2ababaab2e AND SEC_PUSH_STATUS:1 AND PUSH_STATUS: 1"}, new String[]{"id"});
        BufferedWriter fileWriter;

            // 数据写入xml
            String importFilePath = SolrUtil.importFolder + EngineUtil.SOLR_IMPORT_STEP + System.currentTimeMillis() +  ".json";

            fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
            fileWriter.write("[");
                JSONObject json = new JSONObject();
            documents.forEach(r -> {

                json.put("id", r.getFieldValue("id"));
                json.put("SEC_PUSH_USERNAME", SolrUtil.initActionValue("", "set"));
                json.put("SEC_PUSH_USERID", SolrUtil.initActionValue("", "set"));
                json.put("SEC_PUSH_STATUS", SolrUtil.initActionValue("", "set"));
                try {
                    fileWriter.write(json.toJSONString());
                    fileWriter.write(",\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });



        //写文件尾
        fileWriter.write("]");
        fileWriter.close();
        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        if(true){
            return null;
        }*/

        //

        boolean isStep2 = "1".equals(searchObj.getPushStatus());

        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        // 构造动态查询条件
        List<String> searchFqs = dynamicFieldService.getSearchFqs(dynamicSearch);
        if (searchFqs.size() > 0) {
            solrQuery.addFilterQuery(searchFqs.toArray(new String[0]));
        }

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        // 试算
        if (StringUtils.isBlank(searchObj.getBatchId())) {
            solrQuery.addFilterQuery("BATCH_ID:" + ruleId);
            collection = EngineUtil.MEDICAL_TRAIL_ACTION;
        }


        // 针对单独的不合规行为导出
        if (StringUtils.isNotBlank(searchObj.getActionId())) {
            if (StringUtils.isNotBlank(groupBys)) {
                List<String> exportFields = new ArrayList<>();
                // 分割表和字段
                Map<String, Set<String>> tabFieldMap = new HashMap<>();
                for (String filed : fields.split(",")) {
                    String[] tabFiledArray = filed.split("\\.");
                    if(tabFiledArray.length == 1){
                        tabFiledArray = new String[]{EngineUtil.MEDICAL_UNREASONABLE_ACTION, tabFiledArray[0]};
                    }
                    if ("action".equals(tabFiledArray[0])) {
                        continue;
                    }
                    if(EngineUtil.MEDICAL_UNREASONABLE_ACTION.equals(tabFiledArray[0])){
                        exportFields.add(tabFiledArray[1]);
                    } else {
                        exportFields.add(tabFiledArray[0] + "." + tabFiledArray[1]);
                    }

                    if (tabFiledArray[1].startsWith("ALIA")) {
                        tabFiledArray[1] = tabFiledArray[1] + ":" + tabFiledArray[1].substring(tabFiledArray[1].indexOf("_") + 1);
                    }
                    Set<String> fieldList = tabFieldMap.computeIfAbsent(tabFiledArray[0], k -> new HashSet<>());
                    fieldList.add(tabFiledArray[1]);
                }


                List<String> groupByList = Arrays.asList(groupBys.split(","));
                // fields: val,count, sum(ACTION_MONEY), sum(MAX_ACTION_MONEY), sum(MIN_MONEY), sum(MAX_MONEY)
                List<String> facetFields = tabFieldMap.remove(collection).stream().filter(r -> !groupByList.contains(r)).collect(Collectors.toList());

                JSONObject facetChild = new JSONObject();
                for (String field : facetFields) {
                    facetChild.put(field, field);

                }

                boolean isGroupActionName = groupByList.contains("ACTION_NAME");
                if(isGroupActionName && !groupByList.contains("ACTION_ID")){
                    groupByList.set(groupByList.indexOf("ACTION_NAME"), "ACTION_ID");
                    facetChild.put("max(ACTION_NAME)", "ACTION_NAME");
                }

                Map<String, String> linkChild = new HashMap<>();

                Set<String> linkFields = dynamicFieldService.getFromOtherField(tabFieldMap);
                for (String field : linkFields) {
                    linkChild.put("max(" + field + ")", field);
                }

                StringBuilder sb = new StringBuilder("facet(" + collection + ",q=\"*:*\"");

                String column = req.getParameter(QueryGenerator.ORDER_COLUMN);
                String orderType = req.getParameter(QueryGenerator.ORDER_TYPE);
                // 排序
                sb.append(" ,bucketSorts=\"");
                String[] colOrders;
                if (StringUtils.isNotBlank(column) && StringUtils.isNotBlank(orderType)) {
                    String[] cols = column.split(",");
                    String[] orders = orderType.split(",");
                    colOrders = new String[cols.length];
                    for (int i = 0, len = cols.length; i < len; i++) {
                        colOrders[i] = cols[i] + " " + orders[i];
                    }
                    sb.append(StringUtils.join(colOrders, ","));
                } else {
                    String order = groupByList.get(0) + " asc";
                    colOrders = new String[]{order};
                    sb.append(order);
                }
                sb.append("\"");
                // 分组
                sb.append(",buckets=\"").append(StringUtils.join(groupByList,",")).append("\"");

                for (String fq : solrQuery.getFilterQueries()) {
                    sb.append(",fq=\"").append(fq.replaceAll("\"", "\\\\\"")).append("\"");
                }
                String countFacet = "let(a=" + sb.toString() + ",rows=-1,count(*)),count=length(a))";
                // 分页
//            sb.append(",offset=").append(pageSize * (pageNo - 1));
//            sb.append(",rows=").append(pageSize);
                // 统计
                if (facetChild.size() > 0) {
                    sb.append(",").append(StringUtils.join(facetChild.keySet(), ","));
                }
                // 关联
                if (linkChild.size() > 0) {
                    sb.append(",").append(StringUtils.join(linkChild.keySet(), ","));
                }

                int count = Integer.parseInt(SolrUtil.stream(countFacet).get(0).get("count").toString());

                exportFields.add(0, "id");
                exportFields.add("reviewStatus");
                exportFields.add("reviewRemark");
                exportFields.add("reviewClassify");
                fieldTitles = "记录ID," + fieldTitles + ",判定结果,判定理由,白名单归因";
                if (count < 30000 && tabFieldMap.size() * count < 80000) {
//                if (count < 1 && tabFieldMap.size() * count < 80000) {
                    sb.append(",rows=-1");
                    sb.append(")");
                   /* OutputStream os = response.getOutputStream();
                    this.reviewService.dynamicResultExport(exportFields.toArray(new String[0]), fieldTitles.split(",")
                            , tabFieldMap, groupByList, linkChild, sb.toString(), os);*/

                    AtomicReference<Result> result = new AtomicReference<>();
                    String finalFieldTitles = fieldTitles;
                    ThreadUtils.EXPORT_POOL.saveRemoteTask("分组统计" + "_导出", "xlsx", count, path -> {
                        try {
                            FileOutputStream fileOS = new FileOutputStream(path);
                            this.reviewService.dynamicResultExport(exportFields.toArray(new String[0]), finalFieldTitles.split(",")
                                    , tabFieldMap, groupByList, isGroupActionName,  linkChild, sb.toString(), fileOS);
                            FileDownloadHandler handler = new FileDownloadHandler();

                            handler.download(response, path.substring(path.lastIndexOf(File.separator) + 1) , path);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Result res = Result.error(e.getMessage());
                            result.set(res);
                            return res;
                        }
                        Result res = Result.ok();
                        result.set(res);
                        return res;
                    });
                    if(!result.get().isSuccess()){
                        return result.get();
                    }
                    return null;
                } else {
                    List<Integer> counts = new ArrayList<>();

                    for (int i = 0, j, len = count; i < len; i = j) {
                        j = i + 500000;
                        if (j > len) {
                            j = len;
                        }
                        counts.add(j - i);
                    }
                    String finalCollection1 = collection;
                    String finalFieldTitles1 = fieldTitles;
//                    OutputStream os = response.getOutputStream();
                    ThreadUtils.EXPORT_POOL.addRemoteMulti("分组统计" + "_导出", "xlsx"
                            , counts, (osList) -> {
                                try {
                                    this.reviewService.dynamicGroupExport(
                                            finalCollection1, solrQuery, colOrders, tabFieldMap
                                            , groupByList, isGroupActionName, facetFields, linkFields
                                            , finalFieldTitles1.split(","), exportFields.toArray(new String[0]), osList);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return Result.error(e.getMessage());
                                }
                                return Result.ok();
                            });
                    return Result.ok("等待导出，共有 " + counts.size() + " 个文件");
                }
            }

            long count = SolrQueryGenerator.count(collection, solrQuery);
            String ruleType = searchObj.getBusiType();
            String actionId = searchObj.getActionId();
            String title = StringUtils.isBlank(actionId) ? ("DRUG".equals(ruleType) ? "药品合规结果" : "CHARGE".equals(ruleType) ? "收费合规结果" : "CLINICAL".equals(ruleType) ? "临床路径合规结果" : "规则结果数据")
                    : medicalActionDictService.queryNameByActionId(actionId);

            List<TaskActionFieldCol> colList = StringUtils.isBlank(actionId)?new ArrayList<>():this.queryColByActionId(MedicalConstant.PLATFORM_SERVICE, actionId);
            if (colList.size() == 0) {
                colList = this.queryDefColSimple(MedicalConstant.PLATFORM_SERVICE);
            }

            // 设置固定字段
            if (StringUtils.isNotBlank(fixCols)) {
                colList = this.toAddFixCol(colList);
            }
            DynamicFieldConfig fieldConfig = new DynamicFieldConfig(colList);
            Map<String, Set<String>> tabFieldMap = fieldConfig.getTabFieldMap();
            if (tabFieldMap.size() * count < 200000) {
//                OutputStream os = response.getOutputStream();
//                this.reviewService.dynamicResultExport(fieldConfig, solrQuery, collection, os);
                AtomicReference<Result> result = new AtomicReference<>();
                String finalCollection2 = collection;
                ThreadUtils.EXPORT_POOL.saveRemoteTask(title, "xlsx", (int) count, path -> {
                    try {
                        FileOutputStream fileOS = new FileOutputStream(path);
                        this.reviewService.dynamicResultExport(fieldConfig, solrQuery, finalCollection2,isStep2, fileOS);
                        FileDownloadHandler handler = new FileDownloadHandler();

                        handler.download(response, path.substring(path.lastIndexOf(File.separator) + 1) , path);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Result res = Result.error(e.getMessage());
                        result.set(res);
                        return res;
                    }
                    Result res = Result.ok();
                    result.set(res);
                    return res;
                });
                if(!result.get().isSuccess()){
                    return result.get();
                }
                return null;
            } else {
                List<SolrQuery> queries = new ArrayList<>();

                for (int i = 0, j, len = (int) count; i < len; i = j) {
                    j = i + 500000;
                    if (j > len) {
                        j = len;
                    }
                    SolrQuery query = solrQuery.getCopy();
                    query.setSorts(solrQuery.getSorts());
                    query.setStart(i);
                    query.setRows(j - i);
                    queries.add(query);
                }
                String finalCollection = collection;
                int i = 1, len = queries.size();
                for (SolrQuery query : queries) {
                    List<TaskActionFieldCol> finalColList = colList;
                    ThreadUtils.EXPORT_POOL.addRemote(title + "_导出" + (len == 1 ? "" : ("(" + i++ + ")")), "xlsx", query.getRows(), (os) -> {
                        try {
                            this.reviewService.dynamicResultExport(finalColList, query, finalCollection,isStep2, os);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Result.error(e.getMessage());
                        }
                        return Result.ok();
                    });
                }
                return Result.ok("等待导出,共有 " + len + " 个文件");
            }
        } else {
            // 多个不合规行为分文件动态字段导出
            return this.exportMultiActionFile(collection, solrQuery, fixCols, isStep2, response);
        }

    }


    @AutoLog(value = "系统审核-规则结果全选获取")
    @ApiOperation(value = "系统审核-规则结果全选获取", notes = "系统审核-规则结果全选获取")
    @GetMapping(value = "/selectAll")
    public Result<?> selectAll(
            MedicalUnreasonableActionVo searchObj,
//            DwbMasterInfoDyParam masterInfoParam,
            String ruleId,
            String groupBys,
            String dynamicSearch,
            HttpServletRequest req) throws Exception {
        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        // 构造动态查询条件
        List<String> searchFqs = dynamicFieldService.getSearchFqs(dynamicSearch);
        if (searchFqs.size() > 0) {
            solrQuery.addFilterQuery(searchFqs.toArray(new String[0]));
        }

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        // 试算
        if (StringUtils.isBlank(searchObj.getBatchId())) {
            solrQuery.addFilterQuery("BATCH_ID:" + ruleId);
            collection = EngineUtil.MEDICAL_TRAIL_ACTION;
        }

        if (StringUtils.isNotBlank(groupBys)) {

            List<String> groupByList = Arrays.asList(groupBys.split(","));

            StringBuilder sb = new StringBuilder("facet(" + EngineUtil.MEDICAL_UNREASONABLE_ACTION + ",q=\"*:*\"");

            String column = req.getParameter(QueryGenerator.ORDER_COLUMN);
            String orderType = req.getParameter(QueryGenerator.ORDER_TYPE);
            // 排序
            sb.append(" ,bucketSorts=\"");
            if (StringUtils.isNotBlank(column) && StringUtils.isNotBlank(orderType)) {
                String[] cols = column.split(",");
                String[] orders = orderType.split(",");
                String[] colOrders = new String[cols.length];
                for (int i = 0, len = cols.length; i < len; i++) {
                    colOrders[i] = cols[i] + " " + orders[i];
                }
                sb.append(StringUtils.join(colOrders, ","));
            } else {
                sb.append(groupByList.get(0)).append(" asc");
            }
            sb.append("\"");
            // 分组
            sb.append(",buckets=\"").append(StringUtils.join(groupByList,",")).append("\"");

            for (String fq : solrQuery.getFilterQueries()) {
                sb.append(",fq=\"").append(fq.replaceAll("\"", "\\\\\"")).append("\"");
            }
            sb.append(",count(*))");

            List<Map<String, Object>> list = SolrUtil.stream(sb.toString());
            for (Map<String, Object> map : list) {
                String id = null;
                for (String groupBy : groupByList) {
                    String groupByVal = map.get(groupBy).toString();
                    id = id == null ? groupByVal : (id + "::" + groupByVal);
                }
                map.put("ID", id);
            }
            return Result.ok(list);
        }

        solrQuery.setFields("ID:id", "NAME:VISITID");
        solrQuery.setRows(Integer.MAX_VALUE);
        SolrDocumentList list = SolrQueryGenerator.list(collection, solrQuery);
        return Result.ok(list);
    }

    private Result<?> exportMultiActionFile(String collection, SolrQuery solrQuery,String fixCols,boolean isStep2, HttpServletResponse response) throws Exception {
        //            Map<String, Long> map = facetActionCount(collection, solrQuery);
        List<JSONObject> actionList = facetActionData(collection, solrQuery);
        // 只有一个不合规行为并且记录数较小  直接输出
        JSONObject actionFirst;
        if (actionList.size() == 1 && (actionFirst = actionList.get(0)).getLongValue("count") < 200000) {
            String actionName = actionFirst.getString("name");
            String actionId = actionFirst.getString("val");
            if(StringUtils.isBlank(actionName)){
                actionName = actionFirst.getString("actionName");
            }
            List<TaskActionFieldCol> colList = this.queryColByActionId(MedicalConstant.PLATFORM_SERVICE, actionId);
            if (colList.size() == 0) {
                colList = this.queryDefColSimple(MedicalConstant.PLATFORM_SERVICE);
            }
            // 设置固定字段
            if (StringUtils.isNotBlank(fixCols)) {
                colList = this.toAddFixCol(colList);
            }

//                OutputStream os = response.getOutputStream();
//                this.reviewService.dynamicResultExport(colList, solrQuery, collection, os);
            AtomicReference<Result> result = new AtomicReference<>();
            List<TaskActionFieldCol> finalColList = colList;
            ThreadUtils.EXPORT_POOL.saveRemoteTask(actionName + "_导出", "xlsx", actionFirst.getIntValue("count"), path -> {
                try {
                    FileOutputStream fileOS = new FileOutputStream(path);
                    this.reviewService.dynamicResultExport(finalColList, solrQuery, collection,isStep2, fileOS);
                    FileDownloadHandler handler = new FileDownloadHandler();

                    handler.download(response, path.substring(path.lastIndexOf(File.separator) + 1) , path);

                } catch (Exception e) {
                    e.printStackTrace();
                    Result res = Result.error(e.getMessage());
                    result.set(res);
                    return res;
                }
                Result res = Result.ok();
                result.set(res);
                return res;
            });
            if(!result.get().isSuccess()){
                return result.get();
            }
            return null;
        } else {
            int fileCount = 0;
            List<TaskActionFieldCol> defColList = null;
            for (JSONObject actionJson : actionList) {
                String actionName = actionJson.getString("name");
                String actionId = actionJson.getString("val");
                if(StringUtils.isBlank(actionName)){
                    actionName = actionJson.getString("actionName");
                }
                List<TaskActionFieldCol> colList = this.queryColByActionId(MedicalConstant.PLATFORM_SERVICE, actionId);
                if (colList.size() == 0) {
                    if (defColList == null) {
                        defColList = this.queryDefColSimple(MedicalConstant.PLATFORM_SERVICE);
                    }
                    colList = defColList;
                }
                // 设置固定字段
                if (StringUtils.isNotBlank(fixCols)) {
                    colList = this.toAddFixCol(colList);
                }
                int pageNum = 0;
                for (int i = 0, j, len = actionJson.getIntValue("count"); i < len; i = j) {
                    j = i + 500000;
                    if (j > len) {
                        j = len;
                    }
                    pageNum++;
                    SolrQuery query = solrQuery.getCopy();
                    query.setSorts(solrQuery.getSorts());
                    query.setStart(i);
                    query.setRows(j - i);
                    query.addFilterQuery("ACTION_ID:\"" + actionId + "\"");
                    String title = actionName + "_导出" + (i == 0 && j == len ? "" : "(" + pageNum + ")");
                    List<TaskActionFieldCol> finalColList = colList;
                    ThreadUtils.EXPORT_POOL.addRemote(title, "xlsx", query.getRows(), (os) -> {
                        try {
                            this.reviewService.dynamicResultExport(finalColList, query, collection,isStep2, os);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Result.error(e.getMessage());
                        }
                        return Result.ok();
                    });
                    fileCount++;
                }
            }

            if(actionList.size() == 0){
                List<TaskActionFieldCol> colList = this.queryDefColSimple(MedicalConstant.PLATFORM_SERVICE);
                // 设置固定字段
                if (StringUtils.isNotBlank(fixCols)) {
                    colList = this.toAddFixCol(colList);
                }
                int count = (int) SolrQueryGenerator.count(collection, solrQuery);
                int pageNum = 0;
                for (int i = 0, j, len = count; i < len; i = j) {
                    j = i + 500000;
                    if (j > len) {
                        j = len;
                    }
                    pageNum++;
                    SolrQuery query = solrQuery.getCopy();
                    query.setSorts(solrQuery.getSorts());
                    query.setStart(i);
                    query.setRows(j - i);
                    String title = "规则结果数据_导出" + (i == 0 && j == len ? "" : "(" + pageNum + ")");
                    List<TaskActionFieldCol> finalColList = colList;
                    ThreadUtils.EXPORT_POOL.addRemote(title, "xlsx", query.getRows(), (os) -> {
                        try {
                            this.reviewService.dynamicResultExport(finalColList, query, collection,isStep2, os);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Result.error(e.getMessage());
                        }
                        return Result.ok();
                    });
                    fileCount++;
                }
            }
            return Result.ok("等待导出,共有 " + fileCount + " 个文件");
        }
    }

    private List<TaskActionFieldCol> toAddFixCol(List<TaskActionFieldCol> colList) {
        colList = colList.stream().filter(col -> fixColList.stream().noneMatch(
                r -> r.getTableName().equals(col.getTableName()) && r.getColName().equals(col.getColName())
        )).collect(Collectors.toList());
        colList.addAll(0, fixColList);
        return colList;
    }

    private static List<TaskActionFieldCol> fixColList;

    static {
        fixColList = new ArrayList<>();
        String resultCollection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        String[][] tableColTitleArray = {
                {
                        resultCollection, EngineUtil.DWB_MASTER_INFO, resultCollection
                        , resultCollection, resultCollection, resultCollection
                        , resultCollection, resultCollection, "DWS_CLINIC_INHOSPITAL"
                        , "DWS_CLINIC_INHOSPITAL", resultCollection, EngineUtil.DWB_DIAG
                        , EngineUtil.DWB_DIAG
                },
                {
                        "ORGNAME", "ALIA_CASE_ID", "YB_VISITID"
                        , "CLIENTNAME", "SEX", "YEARAGE"
                        , "VISITTYPE", "VISITDATE", "ADMITDATE_THIS"
                        , "LEAVEDATE_THIS", "ZY_DAYS", "DEPTNAME_SRC"
                        , "DISEASENAME_SRC"
                },
                {
                        "医疗机构名称", "病案号", "原始就诊ID"
                        , "患者姓名", "性别", "年龄（岁）"
                        , "就诊类型", "就诊时间", "入院日期"
                        , "出院日期", "住院天数", "科室名称（原始）"
                        , "疾病诊断名称（原始）"
                }
        };

        for (int i = 0, len = tableColTitleArray[1].length; i < len; i++) {
            TaskActionFieldCol col = new TaskActionFieldCol();
            col.setTableName(tableColTitleArray[0][i]);
            col.setColName(tableColTitleArray[1][i]);
            col.setColCnname(tableColTitleArray[2][i]);
            fixColList.add(col);
        }

    }

    private List<TaskActionFieldCol> queryDefColSimple(String platformService) {

        Map<String, String> map = new HashMap<>();
        map.put("platform", platformService);

        List<TaskActionFieldCol> list = ApiTokenUtil.getArray("/task/taskActionFieldCol/getDefSelectCol", map, TaskActionFieldCol.class);

        return list;
    }

    /*private List<TaskActionFieldCol> queryColByActionName(String platformService, String actionName) {

        Map<String, String> map = new HashMap<>();
        map.put("platform", platformService);
        map.put("actionName", actionName);

        List<TaskActionFieldCol> list = ApiTokenUtil.getArray("/task/taskActionFieldCol/getColByAction", map, TaskActionFieldCol.class);

        return list;
    }*/

    private List<TaskActionFieldCol> queryColByActionId(String platformService, String actionId) {

        Map<String, String> map = new HashMap<>();
        map.put("platform", platformService);
        map.put("actionId", actionId);

        List<TaskActionFieldCol> list = ApiTokenUtil.getArray("/task/taskActionFieldCol/getColByAction", map, TaskActionFieldCol.class);

        return list;
    }
    @AutoLog(value = "初审-判定结果")
    @ApiOperation(value = "初审-判定结果", notes = "初审-判定结果")
    @PutMapping(value = "/updateReviewStatus")
    public Result<?> updateReviewStatus(String ids, String groupBys, String reviewInfo,
                                        @RequestParam(name = "batchId") String batchId,
                                        MedicalUnreasonableActionVo searchObj,
//                                        DwbMasterInfoDyParam masterInfoParam,
                                        String dynamicSearch,
                                        HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ReviewInfoDTO reviewObj = JSONObject.parseObject(reviewInfo, ReviewInfoDTO.class);

        JSONObject commonJson = initInputJson(reviewObj);
        // 判断结果信息
        commonJson.put("FIR_REVIEW_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonJson.put("FIR_REVIEW_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
        commonJson.put("FIR_REVIEW_TIME", SolrUtil.initActionValue(TimeUtil.getNowTime(), "set"));
        if("white".equals(reviewObj.getFirReviewStatus())){
            commonJson.put("PUSH_STATUS", SolrUtil.initActionValue("", "set"));
        }

        if (StringUtils.isBlank(ids) || StringUtils.isNotBlank(groupBys)) {
            // 构造主表条件
            SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
            // 清除排序字段，分组排序val或count会报错
            solrQuery.clearSorts();
//            solrQuery.addFilterQuery("-PUSH_STATUS:1");
            solrQuery.addFilterQuery("-(FIR_REVIEW_STATUS:" + reviewObj.getFirReviewStatus() + " AND FIR_REVIEW_CLASSIFY:\"" + reviewObj.getFirReviewClassify() + "\")");
            // 分组勾选条件
            if (StringUtils.isNotBlank(ids) && StringUtils.isNotBlank(groupBys)) {
                List<String> groupByList = Arrays.asList(groupBys.split(","));
                if (groupByList.size() == 1) {
                    solrQuery.addFilterQuery(groupByList.get(0) + ":(\"" + ids.replaceAll(",", "\",\"") + "\")");
                } else {
                    String fq = Arrays.stream(ids.split(",")).map(id -> {
                        String[] groupVals = id.split("::");
                        final AtomicInteger i = new AtomicInteger(0);
                        int valsLen = groupVals.length;
                        // 构造 (f1:"1" AND  f2:"2") OR (f1:"2" AND  f2:"3")
                        return "(" + groupByList.stream()
                                .map(r -> r + ":\"" + (i.get() >= valsLen ? "" : groupVals[i.getAndIncrement()]) + "\"")
                                .collect(Collectors.joining(" AND "))
                                + ")";
                    }).collect(Collectors.joining(" OR "));
                    solrQuery.addFilterQuery(fq);
                }
            }
            // 构造动态查询条件
            List<String> searchFqs = dynamicFieldService.getSearchFqs(dynamicSearch);
            if (searchFqs.size() > 0) {
                solrQuery.addFilterQuery(searchFqs.toArray(new String[0]));
            }
            // DWB_MASTER_INFO表参数
         /*   String masterFq = initMasterQuery(masterInfoParam);
            if (masterFq.length() > 0) {
                solrQuery.addFilterQuery("{!join fromIndex=" + EngineUtil.DWB_MASTER_INFO + " from=VISITID to=VISITID}"
                        + masterFq);
            }*/

            /*ThreadUtils.ASYNC_POOL.addJudge(searchObj,solrQuery.getFilterQueries(), 4950, (processFunc) ->{
                try {

                    for(int i = 0; i < 100; i ++){
                        Thread.sleep(1000);
                        processFunc.accept(i);
                    }
                    return Result.ok("判定成功");
                } catch (Exception e) {
                    e.printStackTrace();
                    return Result.error(e.getMessage());
                }
            });

            if(true){
                return Result.ok("判定成功");
            }*/


            long count = SolrQueryGenerator.count(EngineUtil.MEDICAL_UNREASONABLE_ACTION, solrQuery);

            if (count == 0) {
                return Result.error("没有需要判定的记录");
            }
            if (count < 200000) {
                service.pushRecord(solrQuery, commonJson);
                return Result.ok("判定成功");
            } else {
                /*String ds = SolrUtil.getLoginUserDatasource();

                EngineReviewBySolrQueryRunnable runnable = new EngineReviewBySolrQueryRunnable(ds, solrQuery, commonJson);
                ThreadUtils.THREAD_SOLR_REQUEST_POOL.add(runnable);
*/
                ThreadUtils.ASYNC_POOL.addJudge(searchObj, solrQuery.getFilterQueries(), (int) count, (processFunc) -> {
                    try {
                        service.pushRecord(solrQuery, commonJson, processFunc);
                        return Result.ok("判定成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Result.error(e.getMessage());
                    }
                });
                return Result.ok("正在批量修改判定结果，请稍后查看");
            }


        } else {
            SolrInputDocument commonDoc = new SolrInputDocument();
            for (Map.Entry<String, Object> entry : commonJson.entrySet()) {
                commonDoc.setField(entry.getKey(), entry.getValue());
            }
            this.service.pushRecordByIds(Arrays.asList(ids.split(",")), commonDoc);
            return Result.ok("判定成功");
        }


    }

    @AutoLog(value = "初审-推送合规结果")
    @ApiOperation(value = "初审-推送合规结果", notes = "初审-推送合规结果")
    @PutMapping(value = "/pushRecord")
    public Result<?> pushRecord(String ids, String groupBys, String reviewInfo,
                                @RequestParam(name = "batchId") String batchId,
                                MedicalUnreasonableActionVo searchObj,
//                                DwbMasterInfoDyParam masterInfoParam,
                                String dynamicSearch,
                                HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ReviewInfoDTO reviewObj = JSONObject.parseObject(reviewInfo, ReviewInfoDTO.class);

        boolean isPush = "1".equals(reviewObj.getPushStatus());

        JSONObject commonJson = new JSONObject();
        commonJson.put("PUSH_STATUS", SolrUtil.initActionValue(reviewObj.getPushStatus(), "set"));
        commonJson.put("PUSH_TIME", SolrUtil.initActionValue(TimeUtil.getNowTime(), "set"));
        commonJson.put("PUSH_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonJson.put("PUSH_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));

        if (isPush) {
            commonJson.put("SEC_REVIEW_STATUS", SolrUtil.initActionValue("init", "set"));
        } else {
            // 复审状态
            commonJson.put("SEC_PUSH_STATUS", SolrUtil.initActionValue("0", "set"));
            commonJson.put("HANDLE_STATUS", SolrUtil.initActionValue("", "set"));
        }
        String actionState = isPush ? "推送" : "撤销";

        if (StringUtils.isBlank(ids) || StringUtils.isNotBlank(groupBys)) {
            // 构造主表条件
            SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
            if(isPush){
                solrQuery.addFilterQuery("FIR_REVIEW_STATUS:(grey OR blank)");
                solrQuery.addFilterQuery("-PUSH_STATUS:1");
            } else {
                solrQuery.addFilterQuery("PUSH_STATUS:1");
            }
            // 清除排序字段，分组排序val或count会报错
            solrQuery.clearSorts();
//            solrQuery.addFilterQuery("-SEC_PUSH_STATUS:1");
            // 分组勾选条件
            if (StringUtils.isNotBlank(ids) && StringUtils.isNotBlank(groupBys)) {
                List<String> groupByList = Arrays.asList(groupBys.split(","));
                if (groupByList.size() == 1) {
                    solrQuery.addFilterQuery(groupByList.get(0) + ":(\"" + ids.replaceAll(",", "\",\"") + "\")");
                } else {
                    String fq = Arrays.stream(ids.split(",")).map(id -> {
                        String[] groupVals = id.split("::");
                        final AtomicInteger i = new AtomicInteger(0);
                        int valsLen = groupVals.length;
                        // 构造 (f1:"1" AND  f2:"2") OR (f1:"2" AND  f2:"3")
                        return "(" + groupByList.stream()
                                .map(r -> r + ":\"" + (i.get() >= valsLen ? "" : groupVals[i.getAndIncrement()]) + "\"")
                                .collect(Collectors.joining(" AND "))
                                + ")";
                    }).collect(Collectors.joining(" OR "));
                    solrQuery.addFilterQuery(fq);
                }
            }
            // 构造动态查询条件
            List<String> searchFqs = dynamicFieldService.getSearchFqs(dynamicSearch);
            if (searchFqs.size() > 0) {
                solrQuery.addFilterQuery(searchFqs.toArray(new String[0]));
            }

            long count = SolrQueryGenerator.count(EngineUtil.MEDICAL_UNREASONABLE_ACTION, solrQuery);
            if (count == 0) {
                return Result.error("没有需要" + actionState + "的记录");
            }
            if (count < 200000) {
                service.pushRecord(solrQuery, commonJson);
                return Result.ok(actionState + "成功");
            } else {
                ThreadUtils.ASYNC_POOL.addPush1st(searchObj, isPush, solrQuery.getFilterQueries(), (int) count, (processFunc) -> {
                    try {
                        service.pushRecord(solrQuery, commonJson, processFunc);
                        return Result.ok(actionState + "成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Result.error(e.getMessage());
                    }
                });
                return Result.ok("正在" + actionState + "，请稍后查看状态");
            }

        } else {
            /*SolrInputDocument commonDoc = new SolrInputDocument();
            for (Map.Entry<String, Object> entry : commonJson.entrySet()) {
                commonDoc.setField(entry.getKey(), entry.getValue());
            }
            this.service.pushRecordByIds(Arrays.asList(ids.split(",")), commonDoc);*/
            List<String> idList = Arrays.asList(ids.split(","));
            long totalCount = 0;
            for (int i = 0, j, len = idList.size(); i < len; i = j) {
                j = i + 1000;
                if (j > len) {
                    j = len;
                }
                SolrQuery solrQuery = new SolrQuery("*:*");
                if(isPush){
                    solrQuery.addFilterQuery("FIR_REVIEW_STATUS:(grey OR blank)");
                    solrQuery.addFilterQuery("-PUSH_STATUS:1");
                } else {
                    solrQuery.addFilterQuery("PUSH_STATUS:1");
                }
                solrQuery.addFilterQuery("id:(" + StringUtil.join(idList.subList(i, j), " OR ") + ")");
                long count = SolrQueryGenerator.count(EngineUtil.MEDICAL_UNREASONABLE_ACTION, solrQuery);
                if(count == 0){
                    continue;
                }
                totalCount += count;
                service.pushRecord(solrQuery, commonJson);
            }
            if(totalCount == 0){
                return Result.error("没有需要" + actionState + "的记录");
            }

            return Result.ok(actionState + "成功");
        }


    }

    @AutoLog(value = "初审-推送模型结果")
    @ApiOperation(value = "初审-推送模型结果", notes = "初审-推送模型结果")
    @PutMapping(value = "/pushCase")
    public Result<?> pushCase(String ids, String batchId, String path,
                              String reviewInfo,
                              TaskBatchBreakRuleDel searchObj,
                              HttpServletRequest req) throws Exception {
        List<String> caseIdList;
        if (StringUtils.isBlank(ids)) {
            Map<String, String> params = new HashMap<>();
            for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
                String[] values = entry.getValue();
                if (values.length > 0) {
                    params.put(entry.getKey(), values[0]);
                }
            }
            caseIdList = ApiTokenUtil.getArray("/reviewFirst/pushCase", params, String.class);
        } else {
            // 勾选的
            caseIdList = Arrays.asList(ids.split(","));
        }
        if(caseIdList.size() == 0){
            return Result.error("没有需要推送的模型");
        }

        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ReviewInfoDTO reviewObj = JSONObject.parseObject(reviewInfo, ReviewInfoDTO.class);

        JSONObject commonJson = new JSONObject();
        commonJson.put("PUSH_STATUS", SolrUtil.initActionValue(reviewObj.getPushStatus(), "set"));
        commonJson.put("PUSH_TIME", SolrUtil.initActionValue(TimeUtil.getNowTime(), "set"));
        commonJson.put("PUSH_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonJson.put("PUSH_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
        boolean isPush = "1".equals(reviewObj.getPushStatus());
        if (isPush) {
            commonJson.put("SEC_REVIEW_STATUS", SolrUtil.initActionValue("init", "set"));
        } else {
            // 复审状态
            commonJson.put("SEC_PUSH_STATUS", SolrUtil.initActionValue("0", "set"));
            commonJson.put("HANDLE_STATUS", SolrUtil.initActionValue("", "set"));
        }
        String actionState = isPush ? "推送" : "撤销";

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        MedicalUnreasonableActionVo actionVo = new MedicalUnreasonableActionVo();
        actionVo.setBatchId(batchId);
        long totalCount = 0;

        for (String caseId : caseIdList) {
            SolrQuery solrQuery = new SolrQuery("*:*");
            solrQuery.addFilterQuery("BATCH_ID:" + batchId, "CASE_ID:" + caseId);
            if(isPush){
                solrQuery.addFilterQuery("FIR_REVIEW_STATUS:(grey OR blank)");
                solrQuery.addFilterQuery("-PUSH_STATUS:1");
            } else {
                solrQuery.addFilterQuery("PUSH_STATUS:1");
            }
//        solrQuery.addFilterQuery("-PUSH_STATUS:" + (isPush?"1":"0"));
//            solrQuery.addFilterQuery("-SEC_PUSH_STATUS:1");
            TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
            bean.setBatchId(batchId);
            bean.setCaseId(caseId);
            bean.setReviewTime(new Date());
            bean.setReviewUserid(user.getId());
            bean.setReviewUsername(user.getRealname());

            long count = SolrQueryGenerator.count(collection, solrQuery);
            if(count == 0){
                bean.setStatus(MedicalConstant.REVIEW_STATE_PUSH_ABNORMAL);
                bean.setErrorMsg(actionState + "失败: 没有符合的数据");
                this.updateFlowCase(bean);
                continue;
            }
            bean.setStatus(MedicalConstant.REVIEW_STATE_PUSH_WAIT);
            this.updateFlowCase(bean);
            totalCount += count;
            ThreadUtils.ASYNC_POOL.addPush1st(actionVo, isPush, solrQuery.getFilterQueries(), (int) count
                    , (processFunc) -> {
                        Result result;
                        bean.setStatus(MedicalConstant.REVIEW_STATE_PUSHING);
                        this.updateFlowCase(bean);
                        try {
                            IReviewNewFirstService service = SpringContextUtils.getApplicationContext().getBean(IReviewNewFirstService.class);
//                    service.pushBatchByCaseId(collection, solrQuery, commonJson, isPush);
                            service.pushRecord(solrQuery, commonJson, processFunc);
                            // 更新
                            bean.setStatus(isPush ? MedicalConstant.REVIEW_STATE_PUSHED : MedicalConstant.RUN_STATE_NORMAL);
                            bean.setErrorMsg(actionState + "成功");
                            result = Result.ok("操作成功");
                        } catch (Exception e) {
                            bean.setStatus(MedicalConstant.REVIEW_STATE_PUSH_ABNORMAL);
                            bean.setErrorMsg(actionState + "失败:" +e.getMessage());
                            result = Result.error(e.getMessage());
                        } finally {
                            this.updateFlowCase(bean);
                        }
                        return result;
                    });
        }

        if(totalCount == 0){
            return Result.error("没有需要" + actionState + "的记录");
        }
        return Result.ok("正在" + actionState +"，请稍后查看状态");
    }

    private void updateFlowCase(TaskBatchBreakRuleDel bean){
        ApiTokenUtil.putBodyApi("/task/taskBatchBreakRuleDel/updateByCaseId", bean);

    }


    @AutoLog(value = "系统审核-列表内容统计")
    @ApiOperation(value = "系统审核-列表内容统计", notes = "系统审核-列表内容统计")
    @GetMapping(value = "/facetFields")
    public Result<?> facetFields(
            MedicalUnreasonableActionVo searchObj,
//            DwbMasterInfoDyParam masterInfoParam,
            String ruleId,
            String dynamicSearch,
            HttpServletRequest req) throws Exception {

        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        // 构造动态查询条件
        List<String> searchFqs = dynamicFieldService.getSearchFqs(dynamicSearch);
        if (searchFqs.size() > 0) {
            solrQuery.addFilterQuery(searchFqs.toArray(new String[0]));
        }
        // DWB_MASTER_INFO表参数
        /*String masterFq = initMasterQuery(masterInfoParam);
        if (masterFq.length() > 0) {
            solrQuery.addFilterQuery("{!join fromIndex=" + EngineUtil.DWB_MASTER_INFO + " from=VISITID to=VISITID}"
                    + masterFq);
        }*/
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        // 试算
        if (StringUtils.isBlank(searchObj.getBatchId())) {
            solrQuery.addFilterQuery("BATCH_ID:" + ruleId);
            collection = EngineUtil.MEDICAL_TRAIL_ACTION;
        }

        JSONObject facetJson = new JSONObject();
        facetJson.put("sum(MAX_ACTION_MONEY)", "sum(MAX_ACTION_MONEY)");
        facetJson.put("sum(ACTION_MONEY)", "sum(ACTION_MONEY)");
        facetJson.put("sum(MAX_MONEY)", "sum(MAX_MONEY)");
        facetJson.put("sum(MIN_MONEY)", "sum(MIN_MONEY)");
        facetJson.put("unique(VISITID)", "unique(VISITID)");
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), facetJson.toJSONString());
        return Result.ok(jsonObject);

    }

    /*@AutoLog(value = "项目流程-不合规行为列表查询")
    @ApiOperation(value = "项目流程-不合规行为列表查询", notes = "项目流程-不合规行为列表查询")
    @GetMapping(value = "/termActionName")
    public Result<?> termActionName(MedicalUnreasonableActionVo searchObj,
                                    String ruleId,
                                    HttpServletRequest req) throws Exception {

        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        // 试算
        if (StringUtils.isBlank(searchObj.getBatchId())) {
            solrQuery.addFilterQuery("BATCH_ID:" + ruleId);
            collection = EngineUtil.MEDICAL_TRAIL_ACTION;
        }

        Map<String, Long> map = facetActionCount(collection, solrQuery);
        return Result.ok(map);
    }*/

    @AutoLog(value = "项目流程-不合规行为列表查询")
    @ApiOperation(value = "项目流程-不合规行为列表查询", notes = "项目流程-不合规行为列表查询")
    @GetMapping(value = "/termActionData")
    public Result<?> termActionData(MedicalUnreasonableActionVo searchObj,
                                    String ruleId,
                                    HttpServletRequest req) throws Exception {

        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        // 试算
        if (StringUtils.isNotBlank(ruleId)) {
            solrQuery.addFilterQuery("BATCH_ID:" + ruleId);
            collection = EngineUtil.MEDICAL_TRAIL_ACTION;
        }

        List<JSONObject> list = facetActionData(collection, solrQuery);
        return Result.ok(list);
    }


    /*private Map<String, Long> facetActionCount(String collection, SolrQuery solrQuery) throws Exception {

    	// facet
        JSONObject termFacet = new JSONObject();
        termFacet.put("type", "terms");
        termFacet.put("field", "ACTION_NAME");
        termFacet.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        termFacet.put("overrequest", Integer.MAX_VALUE);

        Map<String, Long> map = new HashMap<>();
        // 查询
        SolrUtil.jsonFacet(collection
                , solrQuery.getFilterQueries(), termFacet.toJSONString()
                , json -> map.put(json.getString("val"), json.getLong("count")));


        return map;
    }*/

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


    @AutoLog(value = "项目流程-不合规行为列表查询")
    @ApiOperation(value = "项目流程-不合规行为列表查询", notes = "项目流程-不合规行为列表查询")
    @GetMapping(value = "/termHisItem")
    public Result<?> termHisItem(MedicalUnreasonableActionVo searchObj,
                                 String ruleId,
                                 HttpServletRequest req) throws Exception {

        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());

        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        // 试算
        if (StringUtils.isBlank(searchObj.getBatchId())) {
            solrQuery.addFilterQuery("BATCH_ID:" + ruleId);
            collection = EngineUtil.MEDICAL_TRAIL_ACTION;
        }

        String facetStr = "{CASE_NAME:{" +
                "\"overrequest\":" + Integer.MAX_VALUE +
                ",\"limit\":" + Integer.MAX_VALUE +
                ",\"field\":\"CASE_NAME\"" +
                ",\"type\":\"terms\"" +
                ",facet:{HIS_ITEMNAME:{" +
                "\"field\":\"HIS_ITEMNAME\"" +
                ",\"type\":\"terms\"}}" +
                "},HIS_ITEMNAME:{" +
                "\"overrequest\":" + Integer.MAX_VALUE +
                ",\"limit\":" + Integer.MAX_VALUE +
                ",\"field\":\"HIS_ITEMNAME\"" +
                ",\"type\":\"terms\"" +
                "facet:{CASE_NAME:{" +
                ",\"field\":\"CASE_NAME\"" +
                ",\"type\":\"terms\"}}" +
                "}}";
        // facet
        // 查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), facetStr);
        return Result.ok(jsonObject);
    }

    @AutoLog(value = "项目流程-项目关联记录")
    @ApiOperation(value = "项目流程-项目关联记录", notes = "项目流程-项目关联记录")
    @GetMapping(value = "/termItemCode")
    public Result<?> termItemCode(@RequestParam(name = "batchId") String batchId,
                                  String itemCode,
                                  String ruleId,
                                  HttpServletRequest req) throws Exception {

        SolrQuery solrQuery = new SolrQuery("*:*");

        solrQuery.addFilterQuery("BATCH_ID:" + batchId);
        if (StringUtils.isNotBlank(itemCode)) {
            solrQuery.addFilterQuery("CASE_ID:" + itemCode);
        }
        if (StringUtils.isNotBlank(ruleId)) {
            solrQuery.addFilterQuery("RULE_ID:" + ruleId);
        }
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        String facetStr = "{pushCount:{\"query\":\"PUSH_STATUS:1\"}}";

        Map<String, Long> map = new HashMap<>();
        // 查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), facetStr);
        map.put("count", jsonObject.getLong("count"));
        map.put("pushCount", jsonObject.getJSONObject("pushCount").getLong("count"));
        return Result.ok(map);
    }


    @AutoLog(value = "初审-获取初审信息")
    @ApiOperation(value = "初审-获取初审信息", notes = "初审-获取初审信息")
    @GetMapping(value = "/queryReviewInfoById")
    public Result<?> queryReviewInfoById(@RequestParam(name = "id") String id) throws Exception {

        String[] fqs = {"id:" + id};

        ReviewInfoDTO bean = SolrQueryGenerator.getOne(EngineUtil.MEDICAL_UNREASONABLE_ACTION, fqs,
                ReviewInfoDTO.class, SolrQueryGenerator.UNREASONABLE_ACTION_MAPPING);

        return Result.ok(bean);
    }

   /* @AutoLog(value = "系统审核-固定字段结果导出")
    @ApiOperation(value = "系统审核-固定字段结果导出", notes = "系统审核-固定字段结果导出")
    @RequestMapping(value = "/drugListExport")
    public Result<?> drugListExport(MedicalUnreasonableActionVo searchObj,
                                    DwbMasterInfoDyParam masterInfoParam,
                                    String batchId,
                                    String ruleId,
                                    HttpServletRequest req,
                                    HttpServletResponse response) throws Exception {
        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        String collection;
        if (StringUtils.isNotBlank(batchId)) {
            solrQuery.addFilterQuery("BATCH_ID:" + batchId);
            collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        } else {
            solrQuery.addFilterQuery("BATCH_ID:" + ruleId);
            collection = EngineUtil.MEDICAL_TRAIL_ACTION;
        }
        // DWB_MASTER_INFO表参数
        String masterFq = initMasterQuery(masterInfoParam);
        if (masterFq.length() > 0) {
            solrQuery.addFilterQuery("{!join fromIndex=" + EngineUtil.DWB_MASTER_INFO + " from=VISITID to=VISITID}"
                    + masterFq);
        }

        long count = SolrQueryGenerator.count(collection, solrQuery);
        String ruleType = searchObj.getBusiType();
        String title = "DRUG".equals(ruleType) ? "药品合规结果" : "收费合规结果";

        if (count > 10000) {
            ThreadUtils.EXPORT_POOL.addRemote(title + "_导出", "xlsx", (int) count, (os) -> {
                try {
                    this.service.exportDrugList(solrQuery, collection, FIELD_DRUG_MAPPING, title, ruleType, os);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Result.error(e.getMessage());
                }
                return Result.ok();
            });
            return Result.ok("等待导出");
        } else {
//            response.reset();
//            response.setContentType("application/octet-stream; charset=utf-8");
//            response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + "导出" + System.currentTimeMillis() + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            OutputStream os = response.getOutputStream();

            this.service.exportDrugList(solrQuery, collection, FIELD_DRUG_MAPPING, title, ruleType, os);
            return null;

        }

    }*/

    private List<String> drugStatisticsGroupByNames = Arrays.asList("限定范围", "患者", "项目", "就诊ID");

    @AutoLog(value = "系统审核-药品规则结果统计导出")
    @ApiOperation(value = "系统审核-药品规则结果统计导出", notes = "系统审核-药品规则结果统计导出")
    @GetMapping(value = "/drugExportStatistics")
    public Result<?> drugExportStatistics(MedicalUnreasonableActionVo searchObj,
                                          String batchId,
                                          String ruleId,
                                          String groupByName,
                                          HttpServletRequest req,
                                          HttpServletResponse response) throws Exception {
        if (!drugStatisticsGroupByNames.contains(groupByName)) {
            return Result.error("统计字段不存在");
        }
        String fileNameFormat = ("DRUG".equals(searchObj.getBusiType()) ? "药品" : "收费") + "合规统计-初审-%s维度" + batchId + ".xlsx";
        String fileName = String.format(fileNameFormat, groupByName);
        MedicalExportTask medicalExportTask = medicalExportTaskService.findByName(fileName);
        if (medicalExportTask != null) {
            String exportStatus = medicalExportTask.getStatus();
            if ("00".equals(exportStatus)) {
                return Result.ok("结果正在统计中...，请稍后再试");
            } else if ("01".equals(exportStatus)) {
                File file = new File(medicalExportTask.getFileFullpath());
                if (file.exists()) {
                    // 输出文件流
                    FileDownloadHandler handler = new FileDownloadHandler();
                    handler.download(response, fileName, medicalExportTask.getFileFullpath());
                    return null;
                }
            }

        }

        String collection;
        if (StringUtils.isNotBlank(ruleId)) {
            searchObj.setBatchId(ruleId);
            collection = EngineUtil.MEDICAL_TRAIL_ACTION;
        } else {
            collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        }
        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());

        List<String> fileNameList = drugStatisticsGroupByNames.stream().map(str -> String.format(fileNameFormat, str)).collect(Collectors.toList());
//        for(String groupType: new String[]{"ruleScope","clientid", "caseId", " visitid"}){

        Function<List<OutputStream>, Result> func = osList -> {
            try {
                drugExportStatistics(solrQuery, collection, osList);
                return Result.ok();
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(e.getMessage());
            }
        };
        long count = SolrQueryGenerator.count(collection, solrQuery);
        if (count > 1000) {
            ThreadUtils.EXPORT_POOL.addUnRandom(fileNameList, func);
            return Result.ok("开始统计，请稍后尝试下载");
        } else {
            // 数量量小直接统计下载
            ThreadUtils.EXPORT_POOL.addUnRandomSync(fileNameList, func);
            medicalExportTask = medicalExportTaskService.findByName(fileName);
            String exportStatus = medicalExportTask.getStatus();
            if ("02".equals(exportStatus)) {
                return Result.error("统计导出失败：" + medicalExportTask.getErrorMsg());
            } else if ("01".equals(exportStatus)) {
                FileDownloadHandler handler = new FileDownloadHandler();
                handler.download(response, fileName, medicalExportTask.getFileFullpath());
                return null;
            } else {
                return Result.ok("结果正在统计中...，请稍后再试");
            }

        }


    }

    @AutoLog(value = "初审-批量导入审核数据")
    @ApiOperation(value = "初审-批量导入审核数据", notes = "初审-批量导入审核数据")
    @PostMapping(value = "/importReviewExcel")
    public Result<?> importCaseExcel(@RequestParam("file") MultipartFile file, MedicalUnreasonableActionVo searchObj
            , HttpServletResponse response) {
        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {
                String msg = this.service.importReviewStatus(file, searchObj);
                if (msg == null) {
                    return Result.ok("数据量过大，正在后台异步导入，可在“异步操作日志”中查看进度");
                } else {
                    return Result.ok("导入成功，" + msg);
                }

            } catch (Exception e) {
//                e.printStackTrace();
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        } else {
            return Result.error("导入失败，文件存在问题");
        }

    }

    @AutoLog(value = "初审-批量导入分组统计审核数据")
    @ApiOperation(value = "初审-批量导入分组统计审核数据", notes = "初审-批量导入分组统计审核数据")
    @PostMapping(value = "/importGroupReviewExcel")
    public Result<?> importGroupReviewExcel(@RequestParam("file") MultipartFile file, MedicalUnreasonableActionVo searchObj
            , String dynamicSearch, HttpServletRequest req) {
        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {
                // 构造主表条件
                SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
                // 构造动态查询条件
                List<String> searchFqs = dynamicFieldService.getSearchFqs(dynamicSearch);
                if (searchFqs.size() > 0) {
                    solrQuery.addFilterQuery(searchFqs.toArray(new String[0]));
                }
                solrQuery.clearSorts();
                String msg = this.service.importGroupReviewStatus(file, searchObj, solrQuery);
                if (msg == null) {
                    return Result.ok("数据量过大，正在后台异步导入，可在“异步操作日志”中查看进度");
                } else {
                    return Result.ok("导入成功，" + msg);
                }

            } catch (Exception e) {
//                e.printStackTrace();
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        } else {
            return Result.error("导入失败，文件存在问题");
        }

    }

    private void drugExportStatistics(SolrQuery solrQuery, String collection, List<OutputStream> osList) throws Exception {

        String[] visitTitles = {"就诊ID", "医疗机构名称", "医疗机构级别", "病人姓名", "性别", "年龄(岁)", "年龄(月)", "年龄(天)", "就诊类型", "就诊日期",
                "住院天数", "诊断疾病名称", "违反限定范围种类数", "违规项目数量", "涉及金额"};
        String[] visitFields = {"visitid", "orgname", "hosplevel", "clientname", "sex", "yearage", "monthage", "dayage", "visittype", "visitdate",
                "zyDaysCalculate", "diseasename", "ruleScopeCount", "sumItemQty", "sumFee"};

        ExportXTarget.Page<StatisticVisit> exportVisitPage = ExportXUtils.initExport(StatisticVisit.class,
                visitTitles, visitFields, "就诊ID维度统计");

        String[] clientTitles = {"患者", "违反限定范围数量", "违规就诊id数", "违规项目种类数量", "涉及金额"};
        String[] clientFields = {"clientid", "ruleScopeCount", "visitidCount", "caseIdCount", "sumFee"};
        ExportXTarget.Page<StatisticClient> exportClientPage = ExportXUtils.initExport(StatisticClient.class,
                clientTitles, clientFields, "患者维度统计");
        // 导出条件
        solrQuery.setSort("CLIENTID", SolrQuery.ORDER.asc);
        solrQuery.addSort("VISITID", SolrQuery.ORDER.asc);
        // 对比验证患者和就诊记录
        final String[] clientId = {"", ""};
        //  同个患者的记录
        List<SolrDocument> clientDocList = new ArrayList<>();
        // List 里不同就诊记录的起坐标点
        AtomicInteger visitIdStartIndex = new AtomicInteger();

        Map<String, StatisticRuleScope> ruleScopeMap = new HashMap<>();
        Map<String, StatisticRule> ruleMap = new HashMap<>();

        SolrUtil.exportDoc(solrQuery, collection, (doc, index) -> {
            try {
                String clientid = (String) doc.getFieldValue("CLIENTID");
                String visitid = (String) doc.getFieldValue("VISITID");
                String caseId = (String) doc.getFieldValue("CASE_ID");
                String caseName = (String) doc.getFieldValue("CASE_NAME");
                Double actionMoney = (Double) doc.getFieldValue("ACTION_MONEY");
                List<String> ruleScopes = doc.getFieldValues("RULE_SCOPE_NAME").stream().map(Object::toString).collect(Collectors.toList());
                for (String ruleScope : ruleScopes) {
                    // 限定范围维度归纳
                    StatisticRuleScope statisticRuleScope = ruleScopeMap.get(ruleScope);
                    if (statisticRuleScope == null) {
                        ruleScopeMap.put(ruleScope, statisticRuleScope = new StatisticRuleScope(ruleScope));
                    }
                    statisticRuleScope.addClientid(clientid);
                    statisticRuleScope.addCaseId(caseId);
                    statisticRuleScope.addVisitid(visitid);
                    statisticRuleScope.addFee(actionMoney);
                }

                // 项目维度归纳
                StatisticRule statisticRule = ruleMap.get(caseId);
                if (statisticRule == null) {
                    ruleMap.put(caseId, statisticRule = new StatisticRule(caseId, caseName));
                }
                statisticRule.addRuleScope(ruleScopes);
                statisticRule.addVisitid(visitid);
                statisticRule.addFee(actionMoney);


                if (!clientId[1].equals(visitid)) {
                    List<SolrDocument> visitDocList = clientDocList.subList(visitIdStartIndex.get(), clientDocList.size());
                    if (visitDocList.size() > 0) {
                        StatisticVisit statisticVisit = SolrUtil.solrDocumentToPojo(visitDocList.get(0), StatisticVisit.class, FIELD_DRUG_MAPPING);
                        statisticVisit.setBaseInfo(visitDocList);
                        // 就诊维度写入记录
                        exportVisitPage.write(statisticVisit);
                    }
                    if (!clientId[0].equals(clientid)) {
                        if (clientDocList.size() > 0) {
                            StatisticClient statisticClient = new StatisticClient(clientId[0], clientDocList);
                            // 病人维度写入记录
                            exportClientPage.write(statisticClient);
                        }
                        clientId[0] = clientid;
                        clientDocList.clear();
                    }
                    clientId[1] = visitid;
                    // 重置就诊记录的起坐标点
                    visitIdStartIndex.set(clientDocList.size());
                }

                clientDocList.add(doc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (clientDocList.size() > 0) {
            List<SolrDocument> visitDocList = clientDocList.subList(visitIdStartIndex.get(), clientDocList.size());
            if (visitDocList.size() > 0) {
                StatisticVisit statisticVisit = SolrUtil.solrDocumentToPojo(visitDocList.get(0), StatisticVisit.class, FIELD_DRUG_MAPPING);
                statisticVisit.setBaseInfo(visitDocList);
                // 就诊维度写入记录
                exportVisitPage.write(statisticVisit);
            }
            StatisticClient statisticClient = new StatisticClient(clientId[0], clientDocList);
            // 病人维度写入记录
            exportClientPage.write(statisticClient);
        }


        String[] ruleScopeTitles = {"违反限定范围", "违规患者数量", "违规项目种类数量", "违规就诊id数量", "涉及金额"};
        String[] ruleScopeFields = {"code", "clientidCount", "caseIdCount", "visitidCount", "sumFee"};
        ExportXTarget.Page<StatisticRuleScope> exportRuleScopePage = ExportXUtils.initExport(StatisticRuleScope.class,
                ruleScopeTitles, ruleScopeFields, "限定范围维度统计");
        // 限定范围记录输出
        for (StatisticRuleScope statisticRuleScope : ruleScopeMap.values()) {
            statisticRuleScope.toCount();
            exportRuleScopePage.write(statisticRuleScope);
        }

        String[] ruleTitles = {"项目编码", "项目名称", "违反限定范围种类数", "违规就诊id数", "涉及金额"};
        String[] ruleFields = {"code", "name", "ruleScopeCount", "visitidCount", "sumFee"};
        ExportXTarget.Page<StatisticRule> exportRulePage = ExportXUtils.initExport(StatisticRule.class,
                ruleTitles, ruleFields, "项目维度统计");
        // 限定范围记录输出
        for (StatisticRule statisticRule : ruleMap.values()) {
            statisticRule.toCount();
            exportRulePage.write(statisticRule);
        }

        exportRuleScopePage.write(osList.get(0));
        exportClientPage.write(osList.get(1));
        exportRulePage.write(osList.get(2));
        exportVisitPage.write(osList.get(3));

    }

    private JSONObject initInputJson(Object obj) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        JSONObject json = new JSONObject();
        List<Field> fieldList = new ArrayList<>();
        Class clzz = obj.getClass();
        do {
            fieldList.addAll(Arrays.asList(clzz.getDeclaredFields()));
            clzz = clzz.getSuperclass(); //得到父类,然后赋给自己
            //当父类为null的时候说明到达了最上层的父类(Object类).
        } while (clzz != null);

        for (Field field : fieldList) {
            field.setAccessible(true);
            String name = field.getName();
            Object value = field.get(obj);
            String docName = StringCamelUtils.camel2Underline(name);
            if (value != null) {
                if ("id".equals(name)) {
                    json.put(name, value);
                } else {
                    json.put(docName, SolrUtil.initActionValue(value, "set"));
                }
            }
        }

        return json;
    }

    private SolrInputDocument initInputDocument(Object obj) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SolrInputDocument doc = new SolrInputDocument();
        List<Field> fieldList = new ArrayList<>();
        Class clzz = obj.getClass();
        do {
            fieldList.addAll(Arrays.asList(clzz.getDeclaredFields()));
            clzz = clzz.getSuperclass(); //得到父类,然后赋给自己
            //当父类为null的时候说明到达了最上层的父类(Object类).
        } while (clzz != null);

        for (Field field : fieldList) {
            field.setAccessible(true);
            String name = field.getName();
            Object value = field.get(obj);
            String docName = StringCamelUtils.camel2Underline(name);
            if (value != null) {
                if ("id".equals(name)) {
                    doc.setField(name, value);
                } else {
                    doc.setField(docName, SolrUtil.initActionValue(value, "set"));
                }
            }
        }

        return doc;
    }

    @AutoLog(value = "AI判定-判定标签")
    @ApiOperation(value = "AI判定-判定标签", notes = "AI判定-判定标签")
    @PutMapping(value = "/updatePredictLabel")
    public Result<?> updatePredictLabel(String ids, String groupBys, String reviewInfo,
                                        @RequestParam(name = "batchId") String batchId,
                                        MedicalUnreasonableActionVo searchObj,
//                                        DwbMasterInfoDyParam masterInfoParam,
                                        String dynamicSearch,
                                        HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ReviewInfoDTO reviewObj = JSONObject.parseObject(reviewInfo, ReviewInfoDTO.class);

        JSONObject commonJson = initInputJson(reviewObj);

        if (StringUtils.isBlank(ids) || StringUtils.isNotBlank(groupBys)) {
            // 构造主表条件
            SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
            // 清除排序字段，分组排序val或count会报错
            solrQuery.clearSorts();
            solrQuery.addFilterQuery("-PREDICT_LABEL:" + reviewObj.getPredictLabel() );
            // 分组勾选条件
            if (StringUtils.isNotBlank(ids) && StringUtils.isNotBlank(groupBys)) {
                List<String> groupByList = Arrays.asList(groupBys.split(","));
                if (groupByList.size() == 1) {
                    solrQuery.addFilterQuery(groupByList.get(0) + ":(\"" + ids.replaceAll(",", "\",\"") + "\")");
                } else {
                    String fq = Arrays.stream(ids.split(",")).map(id -> {
                        String[] groupVals = id.split("::");
                        final AtomicInteger i = new AtomicInteger(0);
                        int valsLen = groupVals.length;
                        // 构造 (f1:"1" AND  f2:"2") OR (f1:"2" AND  f2:"3")
                        return "(" + groupByList.stream()
                                .map(r -> r + ":\"" + (i.get() >= valsLen ? "" : groupVals[i.getAndIncrement()]) + "\"")
                                .collect(Collectors.joining(" AND "))
                                + ")";
                    }).collect(Collectors.joining(" OR "));
                    solrQuery.addFilterQuery(fq);
                }
            }
            // 构造动态查询条件
            List<String> searchFqs = dynamicFieldService.getSearchFqs(dynamicSearch);
            if (searchFqs.size() > 0) {
                solrQuery.addFilterQuery(searchFqs.toArray(new String[0]));
            }

            long count = SolrQueryGenerator.count(EngineUtil.MEDICAL_UNREASONABLE_ACTION, solrQuery);

            if (count == 0) {
                return Result.error("没有需要判定的记录");
            }
            if (count < 200000) {
                service.pushRecord(solrQuery, commonJson);
                return Result.ok("判定成功");
            } else {
                ThreadUtils.ASYNC_POOL.addJudge(searchObj, solrQuery.getFilterQueries(), (int) count, (processFunc) -> {
                    try {
                        service.pushRecord(solrQuery, commonJson, processFunc);
                        return Result.ok("判定成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Result.error(e.getMessage());
                    }
                });
                return Result.ok("正在批量修改判定结果，请稍后查看");
            }


        } else {
            SolrInputDocument commonDoc = new SolrInputDocument();
            for (Map.Entry<String, Object> entry : commonJson.entrySet()) {
                commonDoc.setField(entry.getKey(), entry.getValue());
            }
            this.service.pushRecordByIds(Arrays.asList(ids.split(",")), commonDoc);
            return Result.ok("判定成功");
        }


    }

}
