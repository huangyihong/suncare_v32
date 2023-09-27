package com.ai.modules.task.controller;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ExcelXUtils;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.api.util.ApiTokenCommon;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.runnable.EngineFunctionRunnable;
import com.ai.modules.review.service.IReviewService;
import com.ai.modules.review.vo.DwbAdmmisionVo;
import com.ai.modules.review.vo.DwbDischargeVo;
import com.ai.modules.review.vo.DwbMasterInfoVo;
import com.ai.modules.task.entity.AiTask;
import com.ai.modules.task.service.IAiTaskService;
import com.ai.modules.task.vo.AiModelResultVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiFunction;

/**
 * @Description: AI任务表
 * @Author: jeecg-boot
 * @Date:   2022-02-28
 * @Version: V1.0
 */
@Slf4j
@Api(tags="AI任务表")
@RestController
@RequestMapping("/apiTask/aiTask")
public class ApiAiTaskController extends JeecgController<AiTask, IAiTaskService> {

    @Autowired
    private IAiTaskService aiTaskService;
    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "AI任务表-通过id删除")
    @ApiOperation(value="AI任务表-通过id删除", notes="AI任务表-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name="id") String id) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        ApiResponse apiResponse = ApiTokenUtil.deleteApi("/task/aiTask/delete", map);
        if(apiResponse.isSuccess()){
            SolrUtil.delete("AI_MODEL_RESULT", "TASK_ID:" + id);
            return Result.ok("删除成功!");
        } else {
            return Result.error(apiResponse.getMessage());
        }
    }

    @AutoLog(value = "任务文件导入")
    @ApiOperation(value = "任务文件导入", notes = "任务文件导入")
    @PostMapping(value = "/importTaskFileAction")
    public Result<?> importTaskFileAction(@RequestParam("file") MultipartFile file, AiTask aiTask, HttpServletResponse response) {
        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {
                if(StringUtils.isBlank(aiTask.getTaskName())){
                    return Result.error("任务名称为空，保存失败：");
                }
                String msg = this.importTaskFileAction(file, aiTask);
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

    public String importTaskFileAction(MultipartFile file, AiTask aiTask) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        aiTask.setDataSource(user.getDataSource());
        int batchSize = 100;
        ApiResponse apiResponse = ApiTokenUtil.postBodyApi("/task/aiTask/saveOrUpdateAiTask", aiTask);
        if(apiResponse.isSuccess()) {
            //修改任务文件 先删除solr中的ai_model_result数据
            if(StringUtils.isNotBlank(aiTask.getId())){
                SolrUtil.delete("AI_MODEL_RESULT","TASK_ID:" + aiTask.getId());
            }
            Map<String,Object> data  = ApiTokenUtil.parseObject(apiResponse, HashMap.class);
            JSONObject taskJson = (JSONObject)data.get("task");
            AiTask taskBean = JSON.toJavaObject(taskJson,AiTask.class);
            aiTask.setId(taskBean.getId());

            String[] mappingFields = new String[]{"mxId","batchId","orgname","visitid","itemcode","itemname","probability","predictResult","feature","reason"};
            List<AiModelResultVO> list = ExcelXUtils.readSheet(AiModelResultVO.class, mappingFields, 0, 1, file.getInputStream());

            //JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictNameMapByKey("FIRST_REVIEW_STATUS");
            BiFunction<List<AiModelResultVO>, JSONObject, Exception> actionFun = (dataList, json) -> {
                BufferedWriter fileWriter;
                try {
                    // 数据写入xml
                    String importFilePath = SolrUtil.importFolder +  "/importAiModelResult/" + System.currentTimeMillis() + "_" + list.size() + ".json";

                    fileWriter = new BufferedWriter(
                            new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
                    fileWriter.write("[");

                    List<AiModelResultVO> actionVoList = new ArrayList<>();
                    for (int i = 0; i < dataList.size(); i++) {
                        AiModelResultVO record = dataList.get(i);
                        validateAiModelResultVO(record,aiTask, i);
                        actionVoList.add(record);
                        if(actionVoList.size()==batchSize){
                            listWriteJson(actionVoList,fileWriter);
                            actionVoList.clear();
                        }
                    }

                    if(actionVoList.size()>0){
                        listWriteJson(actionVoList,fileWriter);
                        actionVoList.clear();
                    }
                    //写文件尾
                    fileWriter.write("]");
                    fileWriter.close();

                    //导入solr
                    SolrUtil.importJsonToSolr(importFilePath, "AI_MODEL_RESULT");

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

    private void validateAiModelResultVO(AiModelResultVO record,AiTask aiTask,int i) throws Exception {
        if(StringUtils.isBlank(record.getMxId())){
            throw new Exception("导入的数据中“明细标识id”不能为空，如：第" + (i + 2) + "行数据“明细标识id”为空");
        }
        if(StringUtils.isBlank(record.getVisitid())){
            throw new Exception("导入的数据中“就诊id”不能为空，如：第" + (i + 2) + "行数据“就诊id”为空");
        }
        if(StringUtils.isBlank(record.getItemcode())){
            throw new Exception("导入的数据中“项目编码”不能为空，如：第" + (i + 2) + "行数据“项目编码”为空");
        }
       /* String reviewStatus = reviewStatusMap.getOrDefault(record.getLabel(), "init").toString();
        record.setLabel(reviewStatus);*/
        record.setId(record.getMxId()+"_"+aiTask.getId());
        record.setTaskId(aiTask.getId());
        record.setHandleLabel("init");
        record.setHandleStatus(0);
        record.setDataSource(aiTask.getDataSource());

    }

    private void listWriteJson(List<AiModelResultVO> actionVoList,BufferedWriter fileWriter) throws Exception{
        actionVoList.forEach(actionVo ->{
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

    /**
     * 分页列表查询
     *
     * @param aiModelResultVO
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "模型结果表-分页列表查询")
    @ApiOperation(value="模型结果表-分页列表查询", notes="模型结果表-分页列表查询")
    @GetMapping(value = "/solrlist")
    public Result<?> solrlist(AiModelResultVO aiModelResultVO,
                                       @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                       @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                       HttpServletRequest req) throws Exception {
        String id = "";
        if(StringUtils.isNotBlank(aiModelResultVO.getId())){
            id= aiModelResultVO.getId();
            aiModelResultVO.setId(null);
        }
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(aiModelResultVO, req.getParameterMap());
        if(StringUtils.isNotBlank(id)){
            solrQuery.addFilterQuery("id:\"" +id+"\"");
        }
        Page<AiModelResultVO> page = new Page<>(pageNo, pageSize);
        IPage<AiModelResultVO> pageList = SolrQueryGenerator.page(page, AiModelResultVO.class,
                solrQuery, "AI_MODEL_RESULT", SolrUtil.initFieldMap(AiModelResultVO.class));
        return Result.ok(pageList);
    }

    /**
     * 数量查询
     *
     * @param aiModelResultVO
     * @param req
     * @return
     */
    @AutoLog(value = "模型结果表-数量查询")
    @ApiOperation(value="模型结果表-数量查询", notes="模型结果表-数量查询")
    @GetMapping(value = "/solrlistCount")
    public Result<?> solrlistCount(AiModelResultVO aiModelResultVO,
                                       HttpServletRequest req) throws Exception {
        String id = "";
        if(StringUtils.isNotBlank(aiModelResultVO.getId())){
            id= aiModelResultVO.getId();
            aiModelResultVO.setId(null);
        }
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(aiModelResultVO, req.getParameterMap());
        if(StringUtils.isNotBlank(id)){
            solrQuery.addFilterQuery("id:" +id);
        }
        long count = SolrQueryGenerator.count("AI_MODEL_RESULT",solrQuery);
        return Result.ok(count);
    }

    /**
     * 模型结果临床合理状态判定
     *
     * @param bean
     * @return
     */
    @AutoLog(value = "模型结果临床合理状态判定")
    @ApiOperation(value="模型结果临床合理状态判定", notes="模型结果临床合理状态判定")
    @PostMapping(value = "/handleLabel")
    public Result<?> handleLabel(@RequestBody AiModelResultVO bean) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", bean.getId());
        if(bean.getHandleLabel()!=null){
            setDocUpdateFieldValue(doc, "HANDLE_LABEL", bean.getHandleLabel());
        }
        if(bean.getHandleReason()!=null){
            setDocUpdateFieldValue(doc, "HANDLE_REASON", bean.getHandleReason());
        }
        if(bean.getIsBlack()!=null) {
            setDocUpdateFieldValue(doc, "IS_BLACK", bean.getIsBlack());
        }
        //将内容插入SOLR
        String ds = SolrUtil.getLoginUserDatasource();
        SolrClient solrClient = SolrUtil.getSolrClient("AI_MODEL_RESULT",ds,false);
        solrClient.add(doc);
        solrClient.commit();
        return Result.ok("操作成功！");
    }

    /**
     * 模型结果审核
     *
     * @param   @return
     */
    @AutoLog(value = "模型结果审核")
    @ApiOperation(value="模型结果审核", notes="模型结果审核")
    @PostMapping(value = "/handleModelResult")
    public Result<?> handleModelResult(@RequestBody AiModelResultVO aiModelResultVO) throws Exception {
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(aiModelResultVO, null);
        solrQuery.addFilterQuery("HANDLE_LABEL:init");
        long count = SolrQueryGenerator.count("AI_MODEL_RESULT",solrQuery);
        if(count>0){
           return  Result.error("当前还有"+count+"条项目数据没有进行判定,请先进行判定");
        }
        SolrQuery solrQuery1 = SolrQueryGenerator.initQuery(aiModelResultVO, null);
        solrQuery1.addFilterQuery("HANDLE_STATUS:0");
        List<AiModelResultVO> list= SolrQueryGenerator.list("AI_MODEL_RESULT",solrQuery1,AiModelResultVO.class, SolrUtil.initFieldMap(AiModelResultVO.class));
        //将内容插入SOLR
        String ds = SolrUtil.getLoginUserDatasource();
        SolrClient solrClient = SolrUtil.getSolrClient("AI_MODEL_RESULT",ds,false);
        for(AiModelResultVO bean:list){
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", bean.getId());
            setDocUpdateFieldValue(doc, "HANDLE_STATUS", 1);
            solrClient.add(doc);
        }
        solrClient.commit();
        return Result.ok("操作成功！");
    }

    private void setDocUpdateFieldValue(SolrInputDocument doc ,String fieldName ,Object value){
        String newValue="";
        if (value != null){
            newValue ="" + value;
        }
        HashMap<String ,String> map = new HashMap<String ,String>();
        map.put("set", newValue);

        doc.addField(fieldName, map);
    }


    @AutoLog(value = "模型结果-审核数量统计")
    @ApiOperation(value = "模型结果-审核数量统计", notes = "模型结果-审核数量统计")
    @GetMapping(value = "/modelResultFact")
    public Result<?> modelResultFact(AiModelResultVO searchObj,  HttpServletRequest req) throws Exception {

        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());


        String collection = "AI_MODEL_RESULT";

        JSONObject totalJson = new JSONObject();
        totalJson.put("type", "query");
        totalJson.put("q", "*:*");
        // 已审核数量
        JSONObject handleJson = new JSONObject();
        handleJson.put("type", "query");
        handleJson.put("q", "HANDLE_STATUS: 1");

        // 准确数量
        JSONObject rightJson = new JSONObject();
        rightJson.put("type", "query");
        rightJson.put("q", "HANDLE_STATUS: 1 AND ((PREDICT_RESULT:0 AND IS_BLACK:1) OR (PREDICT_RESULT:1 AND IS_BLACK:0))");

        JSONObject json11 = new JSONObject();
        json11.put("totalFact", totalJson);
        json11.put("handleFact", handleJson);
        json11.put("rightFact", rightJson);

        // facet查询
        SolrUtil.setCacheExpireSeconds(1);
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json11.toJSONString());
        log.info(jsonObject.toJSONString());

        Map<String,Object> data = new HashMap<>();
        long count = jsonObject.getLongValue("count");
        if (count == 0) {
            data.put("totalCount",0);
            data.put("handleCount",0);
            data.put("rightCount",0);
            return Result.ok(data);
        }
        data.put("totalCount",jsonObject.getJSONObject("totalFact").get("count"));
        data.put("handleCount",jsonObject.getJSONObject("handleFact").get("count"));
        data.put("rightCount",jsonObject.getJSONObject("rightFact").get("count"));
        return Result.ok(data);

    }

    @AutoLog(value = "模型结果-列表审核数量统计")
    @ApiOperation(value="模型结果-列表审核数量统计", notes="模型结果-列表审核数量统计")
    @GetMapping(value = "/modelResultFactList")
    public Result<?> modelResultFactList(@RequestParam(name="taskIds") String taskIds,
                                     HttpServletRequest req) throws Exception {

        List<String> facetList = new ArrayList<>();
        for(String taskId: taskIds.split(",")){
            String q = "TASK_ID:" + taskId;
            facetList.add(String.format("\"%s\":{type:query, q:\"%s\", " +
                    "facet: {" +
                    "createCount:{" +
                    "type:query," +
                    "q:\"*:*\"" +
                    "}" +
                    ",auditCount:{" +
                    "type:query," +
                    "q:\"HANDLE_STATUS:1\"" +
                    "}" +
                    "}}", taskId, q));
        }
        String facetStr = "{ " + StringUtils.join(facetList, ",") + "}";
        SolrUtil.setCacheExpireSeconds(1);
        JSONObject resultJon = SolrUtil.jsonFacet("AI_MODEL_RESULT", null, facetStr);
        return Result.ok(resultJon);
    }

    @AutoLog(value = "模型结果-按visitid分组查询分页")
    @ApiOperation(value="模型结果-按visitid分组查询分页", notes="模型结果-按visitid分组查询分页")
    @GetMapping(value = "/modelResultFactPage")
    public Result<?> modelResultFactPage(AiModelResultVO aiModelResultVO,
                                         @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                         @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                         String facetFields,
                                         String groupBys,
                                         HttpServletRequest req) throws Exception {
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(aiModelResultVO, req.getParameterMap());
        //分组字段
        List<String> groupByList = new ArrayList<>();
        if (StringUtils.isNotBlank(groupBys)) {
            groupByList = Arrays.asList(groupBys.split(","));
        }else{
            groupByList.add("VISITID");
        }
        //统计字段
        if (StringUtils.isBlank(facetFields)) {
            facetFields = "count(*)";
        }


        StringBuilder sb = new StringBuilder("facet(AI_MODEL_RESULT,q=\"*:*\"");
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
        if(solrQuery.getFilterQueries()!=null){
            for (String fq : solrQuery.getFilterQueries()) {
                sb.append(",fq=\"").append(fq.replaceAll("\"", "\\\\\"")).append("\"");
            }
        }
        String countFacet = "let(a=" + sb.toString() + ",rows=-1,count(*)),count=length(a))";
        // 分页
        sb.append(",offset=").append(pageSize * (pageNo - 1));
        sb.append(",rows=").append(pageSize);
        // 统计
        sb.append(",").append(facetFields);
        sb.append(")");
        Page<Map<String, Object>> page = new Page<>(pageNo, pageSize);
        SolrUtil.setCacheExpireSeconds(1);//solr不缓存
        int count = Integer.parseInt(SolrUtil.stream(countFacet).get(0).get("count").toString());
        page.setTotal(count);
        if (count > 0) {
            SolrUtil.setCacheExpireSeconds(1);
            List<Map<String, Object>> list = SolrUtil.stream(sb.toString());
            for (Map<String, Object> map : list) {
                String id = null;
                for (String groupBy : groupByList) {
                    String groupByVal = map.get(groupBy).toString();
                    id = id == null ? groupByVal : (id + "::" + groupByVal);
                }
                map.put("id", id);
               /* for (Map.Entry<String, String> entry : facetChild.entrySet()) {
                    map.put(entry.getValue(), map.remove(entry.getKey()));
                }*/
            }
            page.setRecords(list);
        } else {
            page.setRecords(new ArrayList<>());
        }
        return Result.ok(page);
    }

    /**
     * 直接导出excel
     *
     * @param req
     * @param response
     * @param aiModelResultVO
     * @throws Exception
     */
    @RequestMapping(value = "/exportExcelSolr")
    public void exportExcelSolr(HttpServletRequest req, HttpServletResponse response, AiModelResultVO aiModelResultVO) throws Exception {
        String title = req.getParameter("title");
        if (org.apache.commons.lang.StringUtils.isBlank(title)) {
            title = "ai结果已审核_导出";
        }
        //response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes("UTF-8"), "iso-8859-1"));
        try {
            OutputStream os = response.getOutputStream();

            SolrQuery solrQuery = SolrQueryGenerator.initQuery(aiModelResultVO, req.getParameterMap());
            solrQuery.setSort("VISITID",SolrQuery.ORDER.desc);
            //明细层级
            List<AiModelResultVO> listVO = SolrQueryGenerator.list("AI_MODEL_RESULT",solrQuery,AiModelResultVO.class,SolrUtil.initFieldMap(AiModelResultVO.class));
            //就诊层级
            List<Map<String, Object>> listMap = this.solrFacetExportData(solrQuery,"AI_MODEL_RESULT","VISITID");

            String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
            aiTaskService.exportExcelSolr(listVO, listMap, os, suffix);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Map<String, Object>> solrFacetExportData(SolrQuery solrQuery, String collection, String groupBy) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();

        JSONObject totalJson = new JSONObject();
        totalJson.put("type", "query");
        totalJson.put("q", "*:*");
        totalJson.put("facet",new JSONObject()
                        .fluentPut("batchId", "max(BATCH_ID)")
                        .fluentPut("orgname", "max(ORGNAME)")
                );

        //可疑标记
        JSONObject predictResultJson = new JSONObject();
        predictResultJson.put("type", "query");
        predictResultJson.put("q", "PREDICT_RESULT: 0");

        // 黑名单记录
        JSONObject blankJson = new JSONObject();
        blankJson.put("type", "query");
        blankJson.put("q", "HANDLE_LABEL: blank");

        // 白名单记录
        JSONObject whiteJson = new JSONObject();
        whiteJson.put("type", "query");
        whiteJson.put("q", "HANDLE_LABEL: white");

        // 灰名单记录
        JSONObject greyJson = new JSONObject();
        greyJson.put("type", "query");
        greyJson.put("q", "HANDLE_LABEL: grey");

        JSONObject json11 = new JSONObject();
        json11.put("totalFact", totalJson);
        json11.put("predictResultFact", predictResultJson);
        json11.put("blankFact", blankJson);
        json11.put("whiteFact", whiteJson);
        json11.put("greyFact", greyJson);



        // 以VISITID分组
        JSONObject json1 = new JSONObject();
        json1.put("type", "terms");
        json1.put("field", groupBy);
        json1.put("limit", Integer.MAX_VALUE);
        json1.put("overrequest", Integer.MAX_VALUE);
        json1.put("facet", json11);

        JSONObject json = new JSONObject();
        json.put("dataArray", json1);


        // facet查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json.toJSONString());

        log.info(jsonObject.toJSONString());
        long count = jsonObject.getLongValue("count");
        if (count == 0) {
            return list;
        }

        List<String> countNameList = Arrays.asList("total_count","predict_result_count","blank_count", "white_count", "grey_count");
        List<String> factNameList = Arrays.asList( "totalFact","predictResultFact","blankFact","whiteFact","greyFact");
        List<String> countRatioList = Arrays.asList("blank_count", "white_count", "grey_count");

        JSONArray dataArray = jsonObject.getJSONObject("dataArray").getJSONArray("buckets");
        for (int i = 0, len = dataArray.size(); i < len; i++) {
            JSONObject dataJson = dataArray.getJSONObject(i);
            dataJson.put("visitid", dataJson.getString("val"));
            Map<String,JSONObject> dataFactMap = new HashMap<>();
            for(String factName:factNameList){
                if(dataFactMap.get(factName)==null){
                    dataFactMap.put(factName,(JSONObject) dataJson.remove(factName));
                }
            }

            for(int j = 0; j < countNameList.size(); j++){
                int index = countNameList.get(j).lastIndexOf("_");
                String key = countNameList.get(j).substring(index+1);
                dataJson.put(countNameList.get(j),  dataFactMap.get(factNameList.get(j)).getLongValue(key));
            }
            for(String countRatioName:countRatioList){
                if(dataJson.getLongValue("total_count")>0){
                    double ratioVal = dataJson.getLongValue(countRatioName)*10000 / dataJson.getLongValue("total_count");
                    dataJson.put(countRatioName+"_ratio", (double)Math.round(ratioVal)/100.0 + "%");
                }
            }
            //模型id 和 机构名称
            dataJson.put("batchId",dataFactMap.get("totalFact").getString("batchId"));
            dataJson.put("orgname",dataFactMap.get("totalFact").getString("orgname"));
            list.add(dataJson);
        }
        return list;
    }

}
