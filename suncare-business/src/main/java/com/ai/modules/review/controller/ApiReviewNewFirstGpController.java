package com.ai.modules.review.controller;

import cn.hutool.core.util.StrUtil;
import com.ai.common.MedicalConstant;
import com.ai.common.utils.FileDownloadHandler;
import com.ai.common.utils.ThreadUtils;
import com.ai.common.utils.TimeUtil;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.review.dto.DynamicFieldConfig;
import com.ai.modules.review.dto.ReviewInfoDTO;
import com.ai.modules.review.entity.MedicalUnreasonableAction;
import com.ai.modules.review.service.IMedicalUnreasonableActionService;
import com.ai.modules.review.service.impl.DynamicFieldConstant;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.task.entity.TaskActionFieldCol;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "项目初审")
@RestController
@RequestMapping("/gp/apiReviewFirst")
public class ApiReviewNewFirstGpController {
    @Autowired
    private IMedicalUnreasonableActionService medicalUnreasonableActionService;
    @Autowired
    IMedicalActionDictService medicalActionDictService;

    @AutoLog(value = "系统审核-规则结果分页列表查询")
    @ApiOperation(value = "系统审核-规则结果分页列表查询", notes = "系统审核-规则结果分页列表查询")
    @GetMapping(value = "/dynamicColsList")
    public Result<?> dynamicColsList(
            MedicalUnreasonableAction searchObj,
            String ruleId,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "fields") String fields,
            String groupBys,
            String dynamicSearch,
            HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 试算
        if (StringUtils.isBlank(searchObj.getBatchId())) {
            searchObj.setBatchId(ruleId);
        }
        // 构造主表条件
        Map<String, String[]> parameterMap = new HashMap(req.getParameterMap());
        parameterMap.remove(QueryGenerator.ORDER_COLUMN);
        QueryWrapper<MedicalUnreasonableAction> queryWrapper = QueryGenerator.initQueryWrapper(searchObj, parameterMap);

        //动态查询字段
        Map<String, Set<String>> tabFieldMap = DynamicFieldConstant.getSplitTableField(fields,"gp");
        Set<String> resultFieldSet = DynamicFieldConstant.resultFieldSet(tabFieldMap);
        //left join关联字段
        resultFieldSet.addAll(DynamicFieldConstant.resultLinkFieldSet(dynamicSearch));
        //主表字段加入排序字段
        List<String> orderByList = getOrderCol(req, resultFieldSet);
        String selectFields = "t."+StringUtils.join(resultFieldSet.toArray(), ",t.");
        String orderbySql =  StringUtils.join(orderByList.toArray(), ",");



        // 构造动态查询条件
        List<String> searchFqs = medicalUnreasonableActionService.getSearchSqls(dynamicSearch,queryWrapper,user.getDataSource(),tabFieldMap);
        Set<String> searchFqSet = new HashSet<>(searchFqs);
        String joinSql = StringUtils.join(searchFqSet.stream().filter(t->t.startsWith("left join")).collect(Collectors.toList()), " ");
        String whereSql = StringUtils.join(searchFqSet.stream().filter(t->!t.startsWith("left join")&&!(t.indexOf(" as ")>-1&&t.indexOf("表")>-1)).collect(Collectors.toList())," AND ");
        String linkFields = StringUtils.join(searchFqSet.stream().filter(t->t.indexOf(" as ")>-1&&t.indexOf("表")>-1).collect(Collectors.toList()),",");
        if (StringUtils.isNotBlank(groupBys)) {
            tabFieldMap = DynamicFieldConstant.getSplitTableField(fields,"gp");
            List<String> groupByList = Arrays.asList(groupBys.split(","));
            List<String> facetFields = tabFieldMap.remove(EngineUtil.MEDICAL_UNREASONABLE_ACTION).stream().filter(r -> !groupByList.contains(r)).collect(Collectors.toList());
            Set<String> factFieldSet = new HashSet<>();
            JSONObject facetChild = new JSONObject();
            Map<String,String> fieldMapping = new HashMap<>();
            for (String field : facetFields) {
                facetChild.put(field, field);
                if(!"count(*)".equals(field)){
                    String tfield = field.replace("(","__").replace(")","__");
                    factFieldSet.add(field + " as "+ tfield);
                    fieldMapping.put(tfield.toLowerCase(),field);
                }else{
                    factFieldSet.add(field);
                    fieldMapping.put("count","count(*)");
                }
            }

            // 不合规行为名称替换为编码
            boolean isGroupActionName = groupByList.contains("ACTION_NAME");
            if(isGroupActionName && !groupByList.contains("ACTION_ID")){
                groupByList.set(groupByList.indexOf("ACTION_NAME"), "ACTION_ID");
                facetChild.put("max(ACTION_NAME)", "ACTION_NAME");
                factFieldSet.add("max(ACTION_NAME) as ACTION_NAME");
                fieldMapping.put("action_name","ACTION_NAME");
            }

            Map<String, String> linkChild = new HashMap<>();

            Set<String> linkFieldsSet = DynamicFieldConstant.getFromOtherField(tabFieldMap);
            for (String field : linkFieldsSet) {
                linkChild.put("max(" + field + ")", field);
                factFieldSet.add("max(" + field + ") as "+field);
                fieldMapping.put(field.toLowerCase(),field);
            }
            for (String field : groupByList) {
                fieldMapping.put(field.toLowerCase(),field);
            }

            String groupByFields = StringUtils.join(groupByList,",");
            String factFields = groupBys;
            if(factFieldSet.size()>0){
                factFields +=","+StringUtils.join(factFieldSet,",");
            }

            Page<Map<String, Object>> page = new Page<>(pageNo, pageSize);
            IPage<Map<String,Object>> pageList = medicalUnreasonableActionService.facetFieldsPage(page,queryWrapper,joinSql,whereSql,"",factFields,groupByFields,orderbySql);

            List<Map<String,Object>>  list = pageList.getRecords();
            if (pageList.getTotal() > 0) {
                for (Map<String, Object> map : list) {
                    for (Map.Entry<String, String> entry : linkChild.entrySet()) {
                        map.put(entry.getValue(), map.remove(entry.getKey().toLowerCase()));
                    }
                    String id = null;

                    for (String groupBy : groupByList) {
                        if(map.get(groupBy.toLowerCase())!=null){
                            String groupByVal = map.get(groupBy.toLowerCase()).toString();
                            id = id == null ? groupByVal : (id + "::" + groupByVal);
                        }
                    }
                    map.put("id", id);
                }
                // 数据库反查不合规行为名称
                if(isGroupActionName){
                    List<String> actionIdList = list.stream().map(r -> String.valueOf(r.get("ACTION_ID".toLowerCase()))).distinct().collect(Collectors.toList());
                    Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
                    list.forEach(r -> {
                        Object actionId = r.get("ACTION_ID".toLowerCase());
                        if(actionId != null){
                            String actionName = actionNameMap.get(actionId.toString());
                            if(actionName != null){
                                r.put("ACTION_NAME",actionName);
                            } else {
                                r.put("ACTION_NAME", r.get("ACTION_NAME".toLowerCase()));
                            }
                        }
                    });
                }
                List<Map<String,Object>> resultList = new ArrayList<>();
                list.forEach(r -> {
                    Map<String, Object> resultMap = new HashMap<>();
                    Set<String> keySet = r.keySet();
                    for (String key : keySet) {
                        String newKey = fieldMapping.get(key);
                        if(StringUtils.isNotBlank(newKey)){
                            resultMap.put(newKey, r.get(key));
                        }else{
                            resultMap.put(key, r.get(key));
                        }
                    }
                    resultList.add(resultMap);
                });

                page.setRecords(resultList);
            }
            return Result.ok(page);
        }

        Page<Map<String,Object>> page = new Page<>(pageNo, pageSize);
        IPage<Map<String,Object>> pageList = medicalUnreasonableActionService.selectMapPageVO(page, queryWrapper,joinSql,whereSql,selectFields,orderbySql,linkFields);
        List<Map<String,Object>> list = pageList.getRecords();
        medicalUnreasonableActionService.resultMapping(list,resultFieldSet);
        return Result.ok(pageList);
    }

    //主表排序字段
    private List<String> getOrderCol(HttpServletRequest req, Set<String> resultFieldSet) {
        String column = req.getParameter(QueryGenerator.ORDER_COLUMN);
        String order = req.getParameter(QueryGenerator.ORDER_TYPE);
        List<String> orderByList = new ArrayList<>();
        if (oConvertUtils.isNotEmpty(column) && oConvertUtils.isNotEmpty(order)) {
            String[] columns = column.split(",");
            String[] orders = order.split(",");
            for(int i = 0, len = columns.length; i < len; i++){
                String col = columns[i];
                if(StrUtil.isUpperCase(col)){//全部为大写
                    col = col.toLowerCase();
                }
                if(col.indexOf("_")==-1){//没有下划线
                    col = oConvertUtils.camelToUnderlineUpper(col);
                }
                if(columns[i].indexOf("(")>-1&&columns[i].indexOf(")")>-1){
                    col = columns[i];
                }
                resultFieldSet.add(col.toUpperCase());
                orderByList.add(col+" "+orders[i]);
            }
        }
        return orderByList;
    }

    @AutoLog(value = "系统审核-动态字段结果导出")
    @ApiOperation(value = "系统审核-动态字段结果导出", notes = "系统审核-动态字段结果导出")
    @RequestMapping(value = "/dynamicColsExport")
    public Result<?> dynamicColsExport(
            MedicalUnreasonableAction searchObj,
            String ruleId,
            String fixCols,
            String groupBys,
            String dynamicSearch,
            String fields,
            String fieldTitles,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        boolean isStep2 = "1".equals(searchObj.getPushStatus());

        // 试算
        if (StringUtils.isBlank(searchObj.getBatchId())) {
            searchObj.setBatchId(ruleId);
        }
        // 构造主表条件
        Map<String, String[]> parameterMap = new HashMap(req.getParameterMap());
        if(StringUtils.isNotBlank(groupBys)){
            parameterMap.remove(QueryGenerator.ORDER_COLUMN);
        }
        QueryWrapper<MedicalUnreasonableAction> queryWrapper = QueryGenerator.initQueryWrapper(searchObj, parameterMap);

        //动态查询字段
        Map<String, Set<String>> tabFieldMap = DynamicFieldConstant.getSplitTableField(fields,"gp");
        Set<String> resultFieldSet = DynamicFieldConstant.resultFieldSet(tabFieldMap);
        resultFieldSet.add("ACTION_ID");
        resultFieldSet.add("ACTION_NAME");
        //left join关联字段
        resultFieldSet.addAll(DynamicFieldConstant.resultLinkFieldSet(dynamicSearch));
        //主表字段加入排序字段
        List<String> orderByList = getOrderCol(req, resultFieldSet);
        String selectFields = "t."+StringUtils.join(resultFieldSet.toArray(), ",t.");
        String orderbySql =  StringUtils.join(orderByList.toArray(), ",");

        // 构造动态查询条件
        List<String> searchFqs = medicalUnreasonableActionService.getSearchSqls(dynamicSearch,queryWrapper,user.getDataSource(),tabFieldMap);
        Set<String> searchFqSet = new HashSet<>(searchFqs);
        String joinSql = StringUtils.join(searchFqSet.stream().filter(t->t.startsWith("left join")).collect(Collectors.toList()), " ");
        String whereSql = StringUtils.join(searchFqSet.stream().filter(t->!t.startsWith("left join")&&!(t.indexOf(" as ")>-1&&t.indexOf("表")>-1)).collect(Collectors.toList())," AND ");
        String linkFields = StringUtils.join(searchFqSet.stream().filter(t->t.indexOf(" as ")>-1&&t.indexOf("表")>-1).collect(Collectors.toList()),",");

        // 针对单独的不合规行为导出
        if (StringUtils.isNotBlank(searchObj.getActionId())) {
            if (StringUtils.isNotBlank(groupBys)) {
                tabFieldMap = DynamicFieldConstant.getSplitTableField(fields,"gp");
                List<String> groupByList = Arrays.asList(groupBys.split(","));
                List<String> facetFields = tabFieldMap.remove(EngineUtil.MEDICAL_UNREASONABLE_ACTION).stream().filter(r -> !groupByList.contains(r)).collect(Collectors.toList());
                Set<String> factFieldSet = new HashSet<>();
                JSONObject facetChild = new JSONObject();
                Map<String,String> fieldMapping = new HashMap<>();
                for (String field : facetFields) {
                    facetChild.put(field, field);
                    if(!"count(*)".equals(field)){
                        String tfield = field.replace("(","__").replace(")","__");
                        factFieldSet.add(field + " as "+ tfield);
                        fieldMapping.put(tfield.toLowerCase(),field);
                    }else{
                        factFieldSet.add(field);
                        fieldMapping.put("count","count(*)");
                    }
                }

                // 不合规行为名称替换为编码
                boolean isGroupActionName = groupByList.contains("ACTION_NAME");
                if(isGroupActionName && !groupByList.contains("ACTION_ID")){
                    groupByList.set(groupByList.indexOf("ACTION_NAME"), "ACTION_ID");
                    facetChild.put("max(ACTION_NAME)", "ACTION_NAME");
                    factFieldSet.add("max(ACTION_NAME) as ACTION_NAME");
                    fieldMapping.put("action_name","ACTION_NAME");
                }

                Map<String, String> linkChild = new HashMap<>();

                Set<String> linkFieldsSet = DynamicFieldConstant.getFromOtherField(tabFieldMap);
                for (String field : linkFieldsSet) {
                    linkChild.put("max(" + field + ")", field);
                    factFieldSet.add("max(" + field + ") as "+field);
                    fieldMapping.put(field.toLowerCase(),field);
                }
                for (String field : groupByList) {
                    fieldMapping.put(field.toLowerCase(),field);
                }

                String groupByFields = StringUtils.join(groupByList,",");
                String factFields = groupBys;
                if(factFieldSet.size()>0){
                    factFields +=","+StringUtils.join(factFieldSet,",");
                }

                List<String> exportFields = new ArrayList<>();
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
                }

                exportFields.add(0, "id");
                exportFields.add("reviewStatus");
                exportFields.add("reviewRemark");
                exportFields.add("reviewClassify");
                fieldTitles = "记录ID," + fieldTitles + ",判定结果,判定理由,白名单归因";
                String[] fieldTitlesList =  fieldTitles.split(",");
                String[] exportFieldsList = exportFields.toArray(new String[0]);

                String finalFactFields = factFields;
                int count =medicalUnreasonableActionService.facetFieldsCount(queryWrapper,joinSql,whereSql,"",factFields,groupByFields);
                if (count < 200000) {
                    Page<Map<String, Object>> page = new Page<>(1, count);
                    AtomicReference<Result> result = new AtomicReference<>();

                    ThreadUtils.EXPORT_POOL.saveRemoteTask("分组统计" + "_导出", "xlsx", count, path -> {
                        try {
                            FileOutputStream fileOS = new FileOutputStream(path);

                            IPage<Map<String,Object>> pageList = medicalUnreasonableActionService.facetFieldsPage(page,queryWrapper,joinSql,whereSql,"", finalFactFields,groupByFields,orderbySql);
                            List<Map<String,Object>>  resultList = pageList.getRecords();
                            this.medicalUnreasonableActionService.dynamicGroupExport(resultList,exportFieldsList,fieldTitlesList,linkChild,groupByList,fieldMapping, fileOS);
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
                }else{
                    List<Page<Map<String,Object>>> pages = new ArrayList<>();

                    int pageNum = 0;
                    int pageSize = 500000;
                    for (int i = 0, j, len = (int) count; i < len; i = j) {
                        j = i + 500000;
                        if (j > len) {
                            pageSize = len -(j-500000);//最后一页记录数
                            j = len;

                        }
                        Page<Map<String,Object>> page = new Page<>(pageNum++, pageSize);
                        pages.add(page);
                    }
                    int i = 1, len = pages.size();
                    for (Page<Map<String,Object>> page : pages) {
                        ThreadUtils.EXPORT_POOL.addRemote( "分组统计_导出" + (len == 1 ? "" : ("(" + i++ + ")")), "xlsx", (int)page.getSize(), (os) -> {
                            try {
                                IPage<Map<String,Object>> pageList = medicalUnreasonableActionService.facetFieldsPage(page,queryWrapper,joinSql,whereSql,"", finalFactFields,groupByFields,orderbySql);
                                List<Map<String,Object>>  resultList = pageList.getRecords();
                                this.medicalUnreasonableActionService.dynamicGroupExport(resultList,exportFieldsList,fieldTitlesList,linkChild,groupByList,fieldMapping, os);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return Result.error(e.getMessage());
                            }
                            return Result.ok();
                        });
                    }
                    return Result.ok("等待导出,共有 " + len + " 个文件");
                }
            }

            int count = medicalUnreasonableActionService.selectCount(queryWrapper,joinSql,whereSql,selectFields);
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
                colList = DynamicFieldConstant.toAddFixCol(colList);
            }
            DynamicFieldConfig fieldConfig = new DynamicFieldConfig(colList);
            if (count < 200000) {
                AtomicReference<Result> result = new AtomicReference<>();
                ThreadUtils.EXPORT_POOL.saveRemoteTask(title, "xlsx", (int) count, path -> {
                    try {
                        FileOutputStream fileOS = new FileOutputStream(path);
                        List<Map<String,Object>> resultList = this.medicalUnreasonableActionService.selectMapVO(queryWrapper,joinSql,whereSql,selectFields,orderbySql,linkFields);
                        this.medicalUnreasonableActionService.dynamicResultExport(resultList,fieldConfig, isStep2, fileOS);
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

                List<Page<Map<String,Object>>> pages = new ArrayList<>();

                int pageNum = 0;
                int pageSize = 500000;
                for (int i = 0, j, len = (int) count; i < len; i = j) {
                    j = i + 500000;
                    if (j > len) {
                        pageSize = len -(j-500000);//最后一页记录数
                        j = len;

                    }
                    Page<Map<String,Object>> page = new Page<>(pageNum++, pageSize);
                    pages.add(page);
                }
                int i = 1, len = pages.size();
                for (Page<Map<String,Object>> page : pages) {
                    ThreadUtils.EXPORT_POOL.addRemote(title + "_导出" + (len == 1 ? "" : ("(" + i++ + ")")), "xlsx", (int)page.getSize(), (os) -> {
                        try {
                            IPage<Map<String,Object>> pageList = medicalUnreasonableActionService.selectMapPageVO(page, queryWrapper,joinSql,whereSql,selectFields,orderbySql,linkFields);
                            List<Map<String,Object>> resultList = pageList.getRecords();
                            this.medicalUnreasonableActionService.dynamicResultExport(resultList,fieldConfig, isStep2, os);
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
            return this.exportMultiActionFile(queryWrapper, joinSql,whereSql,selectFields,orderbySql,linkFields, fixCols, isStep2,user.getDataSource(), response);
        }
    }

    @AutoLog(value = "系统审核-模型动态字段结果导出")
    @ApiOperation(value = "系统审核-模型动态字段结果导出", notes = "系统审核-模型动态字段结果导出")
    @GetMapping(value = "/dynamicColsExportByCase")
    public Result<?> dynamicColsExportByCase(
            @RequestParam(name = "batchId") String batchId,
            HttpServletRequest req,
            HttpServletResponse response) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
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

        QueryWrapper<MedicalUnreasonableAction> queryWrapper =new QueryWrapper();
        queryWrapper.eq("BATCH_ID",batchId);
        queryWrapper.in("CASE_ID",caseIdList);

        return this.exportMultiActionFile(queryWrapper, "", "", "","","",null,false,user.getDataSource(), response);
    }

    private Result<?> exportMultiActionFile(QueryWrapper<MedicalUnreasonableAction> queryWrapper,String joinSql,String whereSql,String selectFields,String orderbySql,String linkFields,String fixCols, boolean isStep2,String dataSource,HttpServletResponse response) throws Exception {
        QueryWrapper<MedicalUnreasonableAction> queryWrapper1  = queryWrapper.clone();

        boolean hasSelectFields = true;
        if(StringUtils.isBlank(selectFields)){
            hasSelectFields = false;
        }
        List<Map<String,Object>> actionList = new ArrayList<>();
        if(hasSelectFields){
            actionList = this.medicalUnreasonableActionService.facetActionData(queryWrapper1,joinSql,whereSql,selectFields);
        }else{
            actionList = this.medicalUnreasonableActionService.facetActionData(queryWrapper1);
        }
        Map<String,Object> actionFirst = new HashMap<>();
        if (actionList.size() == 1 && Integer.parseInt(actionList.get(0).get("count").toString())< 200000) {
            actionFirst = actionList.get(0);
            String actionName = (String)actionFirst.get("action_name");
            String actionId = (String)actionFirst.get("action_id");
            List<TaskActionFieldCol> colList = this.queryColByActionId(MedicalConstant.PLATFORM_SERVICE, actionId);
            if (colList.size() == 0) {
                colList = this.queryDefColSimple(MedicalConstant.PLATFORM_SERVICE);
            }
            // 设置固定字段
            if (StringUtils.isNotBlank(fixCols)) {
                colList = DynamicFieldConstant.toAddFixCol(colList);
            }
            AtomicReference<Result> result = new AtomicReference<>();
            DynamicFieldConfig fieldConfig = new DynamicFieldConfig(colList);
            if(!hasSelectFields){
                Map<String,String> searchFqMap = this.exportMultiActionSearchFqMap(fieldConfig,null,queryWrapper,dataSource,isStep2);
                selectFields = searchFqMap.get("selectFields");
                joinSql = searchFqMap.get("joinSql");
                whereSql = searchFqMap.get("whereSql");
                linkFields = searchFqMap.get("linkFields");
            }

            String finalJoinSql = joinSql;
            String finalWhereSql = whereSql;
            String finalSelectFields = selectFields;
            String finalLinkFields = linkFields;
            ThreadUtils.EXPORT_POOL.saveRemoteTask(actionName + "_导出", "xlsx", Integer.parseInt(actionFirst.get("count").toString()), path -> {
                try {
                    FileOutputStream fileOS = new FileOutputStream(path);
                    List<Map<String,Object>> resultList = this.medicalUnreasonableActionService.selectMapVO(queryWrapper, finalJoinSql, finalWhereSql, finalSelectFields,orderbySql, finalLinkFields);
                    this.medicalUnreasonableActionService.dynamicResultExport(resultList,fieldConfig, isStep2, fileOS);
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
        }else{
            int fileCount = 0;
            List<TaskActionFieldCol> defColList = null;
            for (Map<String,Object> actionMap : actionList) {
                String actionName = (String)actionMap.get("action_name");
                String actionId = (String)actionMap.get("action_id");
                List<TaskActionFieldCol> colList = this.queryColByActionId(MedicalConstant.PLATFORM_SERVICE, actionId);
                if (colList.size() == 0) {
                    if (defColList == null) {
                        defColList = this.queryDefColSimple(MedicalConstant.PLATFORM_SERVICE);
                    }
                    colList = defColList;
                }
                // 设置固定字段
                if (StringUtils.isNotBlank(fixCols)) {
                    colList = DynamicFieldConstant.toAddFixCol(colList);
                }



                int pageNum = 0;
                int pageSize = 500000;
                for (int i = 0, j, len = Integer.parseInt(actionMap.get("count").toString()); i < len; i = j) {
                    j = i + 500000;
                    if (j > len) {
                        pageSize = len -(j-500000);//最后一页记录数
                        j = len;
                    }
                    Page<Map<String,Object>> page = new Page<>(pageNum++, pageSize);
//                    queryWrapper.eq("ACTION_ID",actionId);
                    String whereSql2 = whereSql;
                    if(StringUtils.isNotBlank(whereSql)){
                        whereSql2 +=" and ";
                    }
                    whereSql2 +=" ACTION_ID='"+actionId+"'";

                    String whereSqlFinal = whereSql2;

                    String title = actionName + "_导出" + (i == 0 && j == len ? "" : "(" + pageNum + ")");
                    DynamicFieldConfig fieldConfig = new DynamicFieldConfig(colList);
                    if(!hasSelectFields){
                        Map<String,String> searchFqMap = this.exportMultiActionSearchFqMap(fieldConfig,null,queryWrapper,dataSource,isStep2);
                        selectFields = searchFqMap.get("selectFields");
                        joinSql = searchFqMap.get("joinSql");
                        whereSql = searchFqMap.get("whereSql");
                        linkFields = searchFqMap.get("linkFields");
                    }
                    String finalJoinSql1 = joinSql;
                    String finalSelectFields1 = selectFields;
                    String finalLinkFields1 = linkFields;
                    ThreadUtils.EXPORT_POOL.addRemote(title, "xlsx", (int)page.getSize(), (os) -> {
                        try {
                            IPage<Map<String,Object>> pageList = medicalUnreasonableActionService.selectMapPageVO(page, queryWrapper, finalJoinSql1,whereSqlFinal, finalSelectFields1,orderbySql, finalLinkFields1);
                            List<Map<String,Object>> resultList = pageList.getRecords();
                            this.medicalUnreasonableActionService.dynamicResultExport(resultList,fieldConfig, isStep2, os);
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
                    colList = DynamicFieldConstant.toAddFixCol(colList);
                }
                int count = medicalUnreasonableActionService.selectCount(queryWrapper,joinSql,whereSql,selectFields);
                int pageNum = 0;
                int pageSize = 500000;
                for (int i = 0, j, len = count; i < len; i = j) {
                    j = i + 500000;
                    if (j > len) {
                        pageSize = len -(j-500000);//最后一页记录数
                        j = len;
                    }
                    pageNum++;
                    Page<Map<String,Object>> page = new Page<>(pageNum++, pageSize);
                    String title = "规则结果数据_导出" + (i == 0 && j == len ? "" : "(" + pageNum + ")");
                    DynamicFieldConfig fieldConfig = new DynamicFieldConfig(colList);
                    if(!hasSelectFields){
                        Map<String,String> searchFqMap = this.exportMultiActionSearchFqMap(fieldConfig,null,queryWrapper,dataSource,isStep2);
                        selectFields = searchFqMap.get("selectFields");
                        joinSql = searchFqMap.get("joinSql");
                        whereSql = searchFqMap.get("whereSql");
                        linkFields = searchFqMap.get("linkFields");
                    }
                    String finalJoinSql2 = joinSql;
                    String finalWhereSql1 = whereSql;
                    String finalSelectFields2 = selectFields;
                    String finalLinkFields2 = linkFields;
                    ThreadUtils.EXPORT_POOL.addRemote(title, "xlsx", (int)page.getSize(), (os) -> {
                        try {
                            IPage<Map<String,Object>> pageList = medicalUnreasonableActionService.selectMapPageVO(page, queryWrapper, finalJoinSql2, finalWhereSql1, finalSelectFields2,orderbySql, finalLinkFields2);
                            List<Map<String,Object>> resultList = pageList.getRecords();
                            this.medicalUnreasonableActionService.dynamicResultExport(resultList,fieldConfig, isStep2, os);
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

    @AutoLog(value = "系统审核-列表内容统计")
    @ApiOperation(value = "系统审核-列表内容统计", notes = "系统审核-列表内容统计")
    @GetMapping(value = "/facetFields")
    public Result<?> facetFields(
            MedicalUnreasonableAction searchObj,
            String ruleId,
            String dynamicSearch,
            HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 试算
        if (StringUtils.isBlank(searchObj.getBatchId())) {
            searchObj.setBatchId(ruleId);
        }
        Map<String, String[]> parameterMap = new HashMap(req.getParameterMap());
        parameterMap.remove(QueryGenerator.ORDER_COLUMN);
        // 构造主表条件
        QueryWrapper<MedicalUnreasonableAction> queryWrapper = QueryGenerator.initQueryWrapper(searchObj, parameterMap);

        // 构造动态查询条件
        List<String> searchFqs = medicalUnreasonableActionService.getSearchSqls(dynamicSearch,queryWrapper,user.getDataSource(),null);
        String joinSql = StringUtils.join(searchFqs.stream().filter(t->t.startsWith("left join")).collect(Collectors.toList()), " ");
        String whereSql = StringUtils.join(searchFqs.stream().filter(t->!t.startsWith("left join")).collect(Collectors.toList())," AND ");
        String resultFields = "id,MAX_ACTION_MONEY,ACTION_MONEY,MAX_MONEY,MIN_MONEY,VISITID";
        String factFields = "sum(t.MAX_ACTION_MONEY) as MAX_ACTION_MONEY,sum(t.ACTION_MONEY) as ACTION_MONEY,sum(t.MAX_MONEY) as MAX_MONEY,sum(t.MIN_MONEY) as MIN_MONEY, count(distinct t.VISITID) as VISITID_COUNT,count(1) as count";
        //left join 字段
        Set<String> resultFieldSet = DynamicFieldConstant.resultLinkFieldSet(dynamicSearch);
        resultFieldSet.addAll(Arrays.asList(resultFields.split(",")));
        String selectFields = "t."+StringUtils.join(resultFieldSet.toArray(), ",t.");
        List<Map<String,Object>> list = medicalUnreasonableActionService.facetFields(queryWrapper,joinSql,whereSql,selectFields,factFields);
        if(list.size()>0){
            Map<String,Object> map = list.get(0);
            map.put("sum(MAX_ACTION_MONEY)", map.get("max_action_money"));
            map.put("sum(ACTION_MONEY)", map.get("action_money"));
            map.put("sum(MAX_MONEY)", map.get("max_money"));
            map.put("sum(MIN_MONEY)", map.get("min_money"));
            map.put("unique(VISITID)", map.get("visitid_count"));
            return Result.ok(map);
        }
        return Result.ok();
    }

    @AutoLog(value = "项目流程-不合规行为列表查询")
    @ApiOperation(value = "项目流程-不合规行为列表查询", notes = "项目流程-不合规行为列表查询")
    @GetMapping(value = "/termActionData")
    public Result<?> termActionData(MedicalUnreasonableAction searchObj,
                                      String ruleId,
                                      HttpServletRequest req) throws Exception {
        QueryWrapper<MedicalUnreasonableAction> queryWrapper = QueryGenerator.initQueryWrapper(searchObj, req.getParameterMap());
        // 试算
        if (StringUtils.isNotBlank(ruleId)) {
            queryWrapper.eq("BATCH_ID",ruleId);
        }
        List<Map<String,Object>> list = medicalUnreasonableActionService.facetActionData(queryWrapper);
        List<String> actionIdList = list.stream().map(r -> String.valueOf(r.get("action_id"))).distinct().collect(Collectors.toList());
        Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
        list.forEach(map -> {
            map.put("name",actionNameMap.get((String)map.get("action_id")));
            map.put("val",(String)map.get("action_id"));
        });
        return Result.ok(list);
    }

    @AutoLog(value = "初审-判定结果")
    @ApiOperation(value = "初审-判定结果", notes = "初审-判定结果")
    @PutMapping(value = "/updateReviewStatus")
    public Result<?> updateReviewStatus(String ids, String groupBys, String reviewInfo,
                                        @RequestParam(name = "batchId") String batchId,
                                        MedicalUnreasonableAction searchObj,
                                        String dynamicSearch,
                                        HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 判断结果信息
        ReviewInfoDTO reviewObj = JSONObject.parseObject(reviewInfo, ReviewInfoDTO.class);
        reviewObj.setFirReviewUserid(user.getId());
        reviewObj.setFirReviewUsername(user.getRealname());
        reviewObj.setFirReviewTime(TimeUtil.getNowTime());
        if("white".equals(reviewObj.getFirReviewStatus())){
            reviewObj.setPushStatus("");
        }

        if (StringUtils.isBlank(ids) || StringUtils.isNotBlank(groupBys)) {
            // 构造主表条件
            Map<String, String[]> parameterMap = new HashMap(req.getParameterMap());
            parameterMap.remove(QueryGenerator.ORDER_COLUMN);
            QueryWrapper<MedicalUnreasonableAction> queryWrapper = QueryGenerator.initQueryWrapper(searchObj, parameterMap);
            queryWrapper.apply("not (FIR_REVIEW_STATUS = '"+reviewObj.getFirReviewStatus()+"' and FIR_REVIEW_CLASSIFY='"+reviewObj.getFirReviewClassify()+"' )");
            // 分组勾选条件
            if (StringUtils.isNotBlank(ids) && StringUtils.isNotBlank(groupBys)) {
                List<String> groupByList = Arrays.asList(groupBys.split(","));
                if (groupByList.size() == 1) {
                    queryWrapper.in(groupByList.get(0),ids.split(","));
                } else {
                    queryWrapper.and(wrapper -> {
                        Arrays.stream(ids.split(",")).forEach(id -> {
                            String[] groupVals = id.split("::");
                            wrapper.or().and(j ->{
                                for(int i=0;i<groupByList.size();i++){
                                    j.eq(groupByList.get(i),groupVals[i]);
                                }
                                return j;
                            });
                        });
                        return wrapper;
                    });
                }
            }
            // 构造动态查询条件
            List<String> searchFqs = medicalUnreasonableActionService.getSearchSqls(dynamicSearch,queryWrapper,user.getDataSource(),null);
            String joinSql = StringUtils.join(searchFqs.stream().filter(t->t.startsWith("left join")).collect(Collectors.toList()), " ");
            String whereSql = StringUtils.join(searchFqs.stream().filter(t->!t.startsWith("left join")).collect(Collectors.toList())," AND ");
            //left join 字段
            Set<String> resultFieldSet = DynamicFieldConstant.resultLinkFieldSet(dynamicSearch);
            resultFieldSet.add("ID");
            String fields = "t."+StringUtils.join(resultFieldSet.toArray(), ",t.");
            int count = medicalUnreasonableActionService.selectCount(queryWrapper,joinSql,whereSql,fields);
            if (count == 0) {
                return Result.error("没有需要判定的记录");
            }
            if (count < 200000) {
                medicalUnreasonableActionService.updateReviewStatus(queryWrapper,joinSql,whereSql,fields,reviewObj);
                return Result.ok("判定成功");
            } else {

                String finalFields = fields;
                MedicalUnreasonableActionVo searchObjVo = new MedicalUnreasonableActionVo();
                BeanUtils.copyProperties(searchObj,searchObjVo);
                ThreadUtils.ASYNC_POOL.addJudge(searchObjVo, new String[]{}, (int) count, (processFunc) -> {
                    try {
                        medicalUnreasonableActionService.updateReviewStatus(queryWrapper,joinSql,whereSql, finalFields,reviewObj);
                        return Result.ok("判定成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Result.error(e.getMessage());
                    }
                });

                return Result.ok("正在批量修改判定结果，请稍后查看");
            }
        } else {
            List<MedicalUnreasonableAction> list = new ArrayList<>();
            Arrays.asList(ids.split(",")).stream().forEach(id->{
                MedicalUnreasonableAction bean =  new MedicalUnreasonableAction();
                BeanUtils.copyProperties(reviewObj,bean);
                bean.setId(id);
                list.add(bean);
            });
            medicalUnreasonableActionService.updateBatchById(list);
            return Result.ok("判定成功");
        }
    }

    @AutoLog(value = "初审-推送合规结果")
    @ApiOperation(value = "初审-推送合规结果", notes = "初审-推送合规结果")
    @PutMapping(value = "/pushRecord")
    public Result<?> pushRecord(String ids, String groupBys, String reviewInfo,
                                @RequestParam(name = "batchId") String batchId,
                                MedicalUnreasonableAction searchObj,
                                String dynamicSearch,
                                HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // 判断结果信息
        ReviewInfoDTO reviewObj = JSONObject.parseObject(reviewInfo, ReviewInfoDTO.class);
        reviewObj.setPushUserid(user.getId());
        reviewObj.setPushUsername(user.getRealname());
        reviewObj.setPushTime(TimeUtil.getNowTime());
        boolean isPush = "1".equals(reviewObj.getPushStatus());
        if (isPush) {
            reviewObj.setSecReviewStatus("init");
        } else {
            // 复审状态
            reviewObj.setSecPushStatus("0");
            reviewObj.setHandleStatus("");
        }
        String actionState = isPush ? "推送" : "撤销";
        // 构造主表条件
        Map<String, String[]> parameterMap = new HashMap(req.getParameterMap());
        parameterMap.remove(QueryGenerator.ORDER_COLUMN);
        QueryWrapper<MedicalUnreasonableAction> queryWrapper = QueryGenerator.initQueryWrapper(searchObj, parameterMap);
        if(isPush){
            queryWrapper.apply("(FIR_REVIEW_STATUS='grey' or FIR_REVIEW_STATUS='blank') and (PUSH_STATUS!='1' or PUSH_STATUS is null)");
        } else {
            queryWrapper.eq("PUSH_STATUS","1");
        }
        // 分组勾选条件
        if (StringUtils.isNotBlank(ids) && StringUtils.isNotBlank(groupBys)) {
            List<String> groupByList = Arrays.asList(groupBys.split(","));
            if (groupByList.size() == 1) {
                queryWrapper.in(groupByList.get(0),ids.split(","));
            } else {
                queryWrapper.and(wrapper -> {
                    Arrays.stream(ids.split(",")).forEach(id -> {
                        String[] groupVals = id.split("::");
                        wrapper.or().and(j ->{
                            for(int i=0;i<groupByList.size();i++){
                                j.eq(groupByList.get(i),groupVals[i]);
                            }
                            return j;
                        });
                    });
                    return wrapper;
                });
            }
        }else if(StringUtils.isNotBlank(ids)){
            queryWrapper.in("ID",ids.split(","));
        }
        // 构造动态查询条件
        List<String> searchFqs = medicalUnreasonableActionService.getSearchSqls(dynamicSearch,queryWrapper,user.getDataSource(),null);
        String joinSql = StringUtils.join(searchFqs.stream().filter(t->t.startsWith("left join")).collect(Collectors.toList()), " ");
        String whereSql = StringUtils.join(searchFqs.stream().filter(t->!t.startsWith("left join")).collect(Collectors.toList())," AND ");
        //left join 字段
        Set<String> resultFieldSet = DynamicFieldConstant.resultLinkFieldSet(dynamicSearch);
        resultFieldSet.add("ID");
        String fields = "t."+StringUtils.join(resultFieldSet.toArray(), ",t.");

        int count = medicalUnreasonableActionService.selectCount(queryWrapper,joinSql,whereSql,fields);
        if (count == 0) {
            return Result.error("没有需要" + actionState + "的记录");
        }
        if (count < 200000) {
            medicalUnreasonableActionService.updateReviewStatus(queryWrapper,joinSql,whereSql,fields,reviewObj);
            return Result.ok(actionState + "成功");
        } else {
            String finalFields = fields;
            MedicalUnreasonableActionVo searchObjVo = new MedicalUnreasonableActionVo();
            BeanUtils.copyProperties(searchObj,searchObjVo);
            ThreadUtils.ASYNC_POOL.addJudge(searchObjVo, new String[]{}, (int) count, (processFunc) -> {
                try {
                    medicalUnreasonableActionService.updateReviewStatus(queryWrapper,joinSql,whereSql, finalFields,reviewObj);
                    return Result.ok("判定成功");
                } catch (Exception e) {
                    e.printStackTrace();
                    return Result.error(e.getMessage());
                }
            });
            return Result.ok("正在" + actionState + "，请稍后查看状态");
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
        reviewObj.setPushUserid(user.getId());
        reviewObj.setPushUsername(user.getRealname());
        reviewObj.setPushTime(TimeUtil.getNowTime());
        boolean isPush = "1".equals(reviewObj.getPushStatus());
        if (isPush) {
            reviewObj.setSecReviewStatus("init");
        } else {
            // 复审状态
            reviewObj.setSecPushStatus("0");
            reviewObj.setHandleStatus("");
        }
        String actionState = isPush ? "推送" : "撤销";

        MedicalUnreasonableActionVo actionVo = new MedicalUnreasonableActionVo();
        actionVo.setBatchId(batchId);
        long totalCount = 0;

        for (String caseId : caseIdList) {

            QueryWrapper<MedicalUnreasonableAction> queryWrapper =new QueryWrapper();
            queryWrapper.eq("BATCH_ID",batchId);
            queryWrapper.eq("CASE_ID",caseId);
            if(isPush){
                queryWrapper.apply("(FIR_REVIEW_STATUS='grey' or FIR_REVIEW_STATUS='blank') and (PUSH_STATUS!='1' or PUSH_STATUS is null)");
            } else {
                queryWrapper.eq("PUSH_STATUS","1");
            }

            TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
            bean.setBatchId(batchId);
            bean.setCaseId(caseId);
            bean.setReviewTime(new Date());
            bean.setReviewUserid(user.getId());
            bean.setReviewUsername(user.getRealname());

            int count = medicalUnreasonableActionService.selectCount(queryWrapper,"","","id");
            if (count == 0) {
                return Result.error("没有需要" + actionState + "的记录");
            }
            if(count == 0){
                bean.setStatus(MedicalConstant.REVIEW_STATE_PUSH_ABNORMAL);
                bean.setErrorMsg(actionState + "失败: 没有符合的数据");
                this.updateFlowCase(bean);
                continue;
            }
            bean.setStatus(MedicalConstant.REVIEW_STATE_PUSH_WAIT);
            this.updateFlowCase(bean);
            totalCount += count;
            ThreadUtils.ASYNC_POOL.addPush1st(actionVo, isPush, new String[]{}, (int) count
                    , (processFunc) -> {
                        Result result;
                        bean.setStatus(MedicalConstant.REVIEW_STATE_PUSHING);
                        this.updateFlowCase(bean);
                        try {
                            medicalUnreasonableActionService.updateReviewStatus(queryWrapper,"","","id",reviewObj);
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



    private List<TaskActionFieldCol> queryColByActionId(String platformService, String actionId) {
        Map<String, String> map = new HashMap<>();
        map.put("platform", platformService);
        map.put("actionId", actionId);

        List<TaskActionFieldCol> list = ApiTokenUtil.getArray("/task/taskActionFieldCol/getColByAction", map, TaskActionFieldCol.class);

        return list;
    }

    private List<TaskActionFieldCol> queryDefColSimple(String platformService) {
        Map<String, String> map = new HashMap<>();
        map.put("platform", platformService);

        List<TaskActionFieldCol> list = ApiTokenUtil.getArray("/task/taskActionFieldCol/getDefSelectCol", map, TaskActionFieldCol.class);

        return list;
    }

    private Map<String,String> exportMultiActionSearchFqMap(DynamicFieldConfig fieldConfig,String dynamicSearch, QueryWrapper<MedicalUnreasonableAction> queryWrapper,String dataSource,boolean isStep2) throws Exception{
        Map<String, Set<String>> tabFieldMap = fieldConfig.getTabFieldMap();
        Set<String> resultFieldSet = DynamicFieldConstant.resultFieldSet(tabFieldMap);
        resultFieldSet.add("ACTION_ID");
        resultFieldSet.add("ACTION_NAME");
        if (isStep2) {
            // 初审判定，判定人，复审判定，复审人
            resultFieldSet.add("FIR_REVIEW_STATUS");
            resultFieldSet.add("FIR_REVIEW_REMARK");
            resultFieldSet.add("FIR_REVIEW_USERNAME");
            resultFieldSet.add("SEC_REVIEW_STATUS");
            resultFieldSet.add("SEC_REVIEW_REMARK");
            resultFieldSet.add("SEC_REVIEW_CLASSIFY");
            resultFieldSet.add("SEC_REVIEW_USERNAME");
        } else {
            resultFieldSet.add("PREDICT_LABEL");
            resultFieldSet.add("PROBILITY");
            resultFieldSet.add("FIR_REVIEW_USERNAME");
            resultFieldSet.add("FIR_REVIEW_STATUS");
            resultFieldSet.add("FIR_REVIEW_REMARK");
            resultFieldSet.add("FIR_REVIEW_CLASSIFY");
            resultFieldSet.add("PUSH_STATUS");
        }
        String selectFields = "t."+StringUtils.join(resultFieldSet.toArray(), ",t.");
        // 构造动态查询条件
        List<String> searchFqs = medicalUnreasonableActionService.getSearchSqls(dynamicSearch,queryWrapper,dataSource,tabFieldMap);
        Set<String> searchFqSet = new HashSet<>(searchFqs);
        String joinSql = StringUtils.join(searchFqSet.stream().filter(t->t.startsWith("left join")).collect(Collectors.toList()), " ");
        String whereSql = StringUtils.join(searchFqSet.stream().filter(t->!t.startsWith("left join")&&!(t.indexOf(" as ")>-1&&t.indexOf("表")>-1)).collect(Collectors.toList())," AND ");
        String linkFields = StringUtils.join(searchFqSet.stream().filter(t->t.indexOf(" as ")>-1&&t.indexOf("表")>-1).collect(Collectors.toList()),",");
        Map<String,String> searchFqMap = new HashMap<>();
        searchFqMap.put("selectFields",selectFields);
        searchFqMap.put("joinSql",joinSql);
        searchFqMap.put("whereSql",whereSql);
        searchFqMap.put("linkFields",linkFields);
        return searchFqMap;
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
                String msg = this.medicalUnreasonableActionService.importReviewStatus(file, searchObj);
                if (msg == null) {
                    return Result.ok("数据量过大，正在后台异步导入，可在“异步操作日志”中查看进度");
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

    @AutoLog(value = "初审-批量导入分组统计审核数据")
    @ApiOperation(value = "初审-批量导入分组统计审核数据", notes = "初审-批量导入分组统计审核数据")
    @PostMapping(value = "/importGroupReviewExcel")
    public Result<?> importGroupReviewExcel(@RequestParam("file") MultipartFile file, MedicalUnreasonableAction searchObj
            , String dynamicSearch, HttpServletRequest req) {
        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {

                String msg = this.medicalUnreasonableActionService.importGroupReviewStatus(file, searchObj, dynamicSearch,req);
                if (msg == null) {
                    return Result.ok("数据量过大，正在后台异步导入，可在“异步操作日志”中查看进度");
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

    @AutoLog(value = "初审-获取初审信息")
    @ApiOperation(value = "初审-获取初审信息", notes = "初审-获取初审信息")
    @GetMapping(value = "/queryReviewInfoById")
    public Result<?> queryReviewInfoById(@RequestParam(name = "id") String id) throws Exception {
        MedicalUnreasonableAction beanVo = medicalUnreasonableActionService.getById(id);
        ReviewInfoDTO bean = new ReviewInfoDTO();
        BeanUtils.copyProperties(beanVo,bean);
        return Result.ok(bean);
    }

    @AutoLog(value = "系统审核-规则结果全选获取")
    @ApiOperation(value = "系统审核-规则结果全选获取", notes = "系统审核-规则结果全选获取")
    @GetMapping(value = "/selectAll")
    public Result<?> selectAll(
            MedicalUnreasonableAction searchObj,
            String ruleId,
            String groupBys,
            String dynamicSearch,
            HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 试算
        if (StringUtils.isBlank(searchObj.getBatchId())) {
            searchObj.setBatchId(ruleId);
        }
        // 构造主表条件
        Map<String, String[]> parameterMap = new HashMap(req.getParameterMap());
        parameterMap.remove(QueryGenerator.ORDER_COLUMN);
        QueryWrapper<MedicalUnreasonableAction> queryWrapper = QueryGenerator.initQueryWrapper(searchObj, parameterMap);


        // 构造动态查询条件
        List<String> searchFqs = medicalUnreasonableActionService.getSearchSqls(dynamicSearch,queryWrapper,user.getDataSource(),null);
        Set<String> searchFqSet = new HashSet<>(searchFqs);
        String joinSql = StringUtils.join(searchFqSet.stream().filter(t->t.startsWith("left join")).collect(Collectors.toList()), " ");
        String whereSql = StringUtils.join(searchFqSet.stream().filter(t->!t.startsWith("left join")&&!(t.indexOf(" as ")>-1&&t.indexOf("表")>-1)).collect(Collectors.toList())," AND ");
        String linkFields = StringUtils.join(searchFqSet.stream().filter(t->t.indexOf(" as ")>-1&&t.indexOf("表")>-1).collect(Collectors.toList()),",");
        if (StringUtils.isNotBlank(groupBys)) {
            List<String> groupByList = Arrays.asList(groupBys.split(","));
            String groupByFields = StringUtils.join(groupByList,",");
            String factFields = groupBys;
            int count =medicalUnreasonableActionService.facetFieldsCount(queryWrapper,joinSql,whereSql,"",factFields,groupByFields);
            Page<Map<String, Object>> page = new Page<>(1, count);
            IPage<Map<String,Object>> pageList = medicalUnreasonableActionService.facetFieldsPage(page,queryWrapper,joinSql,whereSql,"",factFields,groupByFields,"");
            List<Map<String,Object>>  list = pageList.getRecords();
            for (Map<String, Object> map : list) {
                String id = null;
                for (String groupBy : groupByList) {
                    String groupByVal = map.get(groupBy.toLowerCase()).toString();
                    id = id == null ? groupByVal : (id + "::" + groupByVal);
                    map.put(groupBy, map.get(groupBy.toLowerCase()));
                }
                map.put("ID", id);
            }
            return Result.ok(list);
        }
        List<Map<String,Object>> list = medicalUnreasonableActionService.selectMapVO(queryWrapper,joinSql,whereSql,"t.id,t.VISITID as NAME","",linkFields);
        for (Map<String, Object> map : list) {
            map.put("ID", map.get("id"));
        }
        return Result.ok(list);
    }


    @AutoLog(value = "AI判定-判定标签")
    @ApiOperation(value = "AI判定-判定标签", notes = "AI判定-判定标签")
    @PutMapping(value = "/updatePredictLabel")
    public Result<?> updatePredictLabel(String ids, String groupBys, String reviewInfo,
                                        @RequestParam(name = "batchId") String batchId,
                                        MedicalUnreasonableAction searchObj,
                                        String dynamicSearch,
                                        HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 判断结果信息
        ReviewInfoDTO reviewObj = JSONObject.parseObject(reviewInfo, ReviewInfoDTO.class);
        Map<String, String[]> parameterMap = new HashMap(req.getParameterMap());
        parameterMap.remove(QueryGenerator.ORDER_COLUMN);
        if (StringUtils.isBlank(ids) || StringUtils.isNotBlank(groupBys)) {
            // 构造主表条件
            QueryWrapper<MedicalUnreasonableAction> queryWrapper = QueryGenerator.initQueryWrapper(searchObj, parameterMap);
            queryWrapper.apply("(PREDICT_LABEL != '"+reviewObj.getPredictLabel()+"' or PREDICT_LABEL is null)");
            // 分组勾选条件
            if (StringUtils.isNotBlank(ids) && StringUtils.isNotBlank(groupBys)) {
                List<String> groupByList = Arrays.asList(groupBys.split(","));
                if (groupByList.size() == 1) {
                    queryWrapper.in(groupByList.get(0),ids.split(","));
                } else {
                    queryWrapper.and(wrapper -> {
                        Arrays.stream(ids.split(",")).forEach(id -> {
                            String[] groupVals = id.split("::");
                            wrapper.or().and(j ->{
                                for(int i=0;i<groupByList.size();i++){
                                    j.eq(groupByList.get(i),groupVals[i]);
                                }
                                return j;
                            });
                        });
                        return wrapper;
                    });
                }
            }
            // 构造动态查询条件
            List<String> searchFqs = medicalUnreasonableActionService.getSearchSqls(dynamicSearch,queryWrapper,user.getDataSource(),null);
            String joinSql = StringUtils.join(searchFqs.stream().filter(t->t.startsWith("left join")).collect(Collectors.toList()), " ");
            String whereSql = StringUtils.join(searchFqs.stream().filter(t->!t.startsWith("left join")).collect(Collectors.toList())," AND ");
            //left join 字段
            Set<String> resultFieldSet = DynamicFieldConstant.resultLinkFieldSet(dynamicSearch);
            resultFieldSet.add("ID");
            String fields = "t."+StringUtils.join(resultFieldSet.toArray(), ",t.");
            int count = medicalUnreasonableActionService.selectCount(queryWrapper,joinSql,whereSql,fields);
            if (count == 0) {
                return Result.error("没有需要判定的记录");
            }
            if (count < 200000) {
                medicalUnreasonableActionService.updateReviewStatus(queryWrapper,joinSql,whereSql,fields,reviewObj);
                return Result.ok("判定成功");
            } else {

                String finalFields = fields;
                MedicalUnreasonableActionVo searchObjVo = new MedicalUnreasonableActionVo();
                BeanUtils.copyProperties(searchObj,searchObjVo);
                ThreadUtils.ASYNC_POOL.addJudge(searchObjVo, new String[]{}, (int) count, (processFunc) -> {
                    try {
                        medicalUnreasonableActionService.updateReviewStatus(queryWrapper,joinSql,whereSql, finalFields,reviewObj);
                        return Result.ok("判定成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Result.error(e.getMessage());
                    }
                });

                return Result.ok("正在批量修改判定结果，请稍后查看");
            }


        } else {
            List<MedicalUnreasonableAction> list = new ArrayList<>();
            Arrays.asList(ids.split(",")).stream().forEach(id->{
                MedicalUnreasonableAction bean =  new MedicalUnreasonableAction();
                BeanUtils.copyProperties(reviewObj,bean);
                bean.setId(id);
                list.add(bean);
            });
            medicalUnreasonableActionService.updateBatchById(list);
            return Result.ok("判定成功");
        }
    }

}
