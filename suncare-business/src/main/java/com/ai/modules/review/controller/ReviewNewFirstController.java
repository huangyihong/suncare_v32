package com.ai.modules.review.controller;

import com.ai.common.MedicalConstant;
import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.*;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.dto.*;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.review.vo.ReviewSystemDrugViewVo;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.service.ITaskBatchBreakRuleDelService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "模型结果总览")
@RestController
@RequestMapping("/reviewFirst")
public class ReviewNewFirstController {


    @Autowired
    ITaskBatchBreakRuleDelService taskBatchBreakRuleDelService;


    private static Map<String, String> FIELD_DRUG_MAPPING = SolrUtil.initFieldMap(ReviewSystemDrugViewVo.class);

    @AutoLog(value = "初审-推送模型结果")
    @ApiOperation(value = "初审-推送模型结果", notes = "初审-推送模型结果")
    @GetMapping(value = "/pushCase")
    public Result<?> pushCase(String ids, String batchId,
                              TaskBatchBreakRuleDel searchObj,
                              HttpServletRequest req) throws Exception {

            // 当前条件列表全部
            QueryWrapper<TaskBatchBreakRuleDel> queryWrapper = QueryGenerator.initQueryWrapper(searchObj, req.getParameterMap());
            List<TaskBatchBreakRuleDel> list = taskBatchBreakRuleDelService.list(queryWrapper);
            List<String> caseIdList = list.stream().map(TaskBatchBreakRuleDel::getCaseId).distinct().collect(Collectors.toList());

            //  更新正在推送状态
          /*  TaskBatchBreakRuleDel taskBatchBreakRuleDel = new TaskBatchBreakRuleDel();
            taskBatchBreakRuleDel.setStatus(MedicalConstant.REVIEW_STATE_PUSHING);
            taskBatchBreakRuleDelService.update(taskBatchBreakRuleDel,
                    new QueryWrapper<TaskBatchBreakRuleDel>()
                            .gt("RECORD_NUM", 0)
                            .eq("BATCH_ID", batchId)
                            .in("CASE_ID", caseIdList)
            );*/


        return Result.ok(caseIdList);
    }



    private List<String> drugStatisticsGroupByNames = Arrays.asList("限定范围", "患者", "项目", "就诊ID");

   /* @AutoLog(value = "系统审核-药品规则结果统计导出")
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
*/

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



}
