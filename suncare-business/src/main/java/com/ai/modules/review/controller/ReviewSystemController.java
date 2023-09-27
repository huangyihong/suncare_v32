package com.ai.modules.review.controller;

import com.ai.common.MedicalConstant;
import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ExportXUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.entity.MedicalFormalFlowRuleGrade;
import com.ai.modules.review.service.IReviewSystemService;
import com.ai.modules.review.vo.*;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.service.ITaskBatchBreakRuleDelService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jxl.Workbook;
import jxl.write.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.Boolean;
import java.lang.Number;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "模型结果总览")
@RestController
@RequestMapping("/reviewSystem")
public class ReviewSystemController {

    @Autowired
    private IReviewSystemService service;

    @Autowired
    private ITaskBatchBreakRuleDelService taskBatchBreakRuleDelService;

    private static Map<String, String> FIELD_MAPPING = SolrUtil.initFieldMap(ReviewSystemViewVo.class);

    private static Map<String, String> FIELD_DRUG_MAPPING = SolrUtil.initFieldMap(ReviewSystemDrugViewVo.class);

    /**
     * 人工审查-分页列表查询
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "系统审核-规则结果分页列表查询")
    @ApiOperation(value = "系统审核-规则结果分页列表查询", notes = "系统审核-规则结果分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(DwbMasterInfoVo dwbMasterInfo,
                                   @RequestParam(name = "batchId") String batchId,
                                   String caseId,
                                   String clinicalId,
                                   String reviewCaseIds,
                                   String grades,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) throws Exception {

        // 构造主表条件
        SolrQuery solrQuery = new SolrQuery("*:*");
        if (StringUtils.isNotBlank(caseId)) {
            String unreasonableFq = "BATCH_ID:" + batchId + " AND CASE_ID:" + caseId;
            if ("1".equals(reviewCaseIds)) {
                unreasonableFq += " AND REVIEW_CASE_IDS:" + caseId;
            } else if ("0".equals(reviewCaseIds)) {
                unreasonableFq += " AND -REVIEW_CASE_IDS:" + caseId;
            }
            SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "VISITID", "VISITID");
            String unreasonableJoinFq = "_query_:\"" + plugin.parse() + unreasonableFq + "\"";
            solrQuery = this.constructListQuery(dwbMasterInfo, unreasonableJoinFq, caseId, grades, req);
        } else if (StringUtils.isNotBlank(clinicalId)) {
            solrQuery = this.constructListClinicalQuery(dwbMasterInfo, batchId, clinicalId, req);
        }

        Page<ReviewSystemViewVo> page = new Page<>(pageNo, pageSize);
        IPage<ReviewSystemViewVo> pageList = SolrQueryGenerator.page(page, ReviewSystemViewVo.class,
                solrQuery, EngineUtil.DWB_MASTER_INFO, FIELD_MAPPING);

        if (pageList.getRecords().size() > 0) {

            Map<String, ReviewSystemViewVo> map = new HashMap<>();
            for (ReviewSystemViewVo bean : pageList.getRecords()) {
                map.put(bean.getVisitid(), bean);
            }
            String visitIdFq = "VISITID:(\"" + StringUtils.join(map.keySet(), "\",\"") + "\")";

            SolrQuery solrQuery1 = new SolrQuery("*:*");
            solrQuery1.addFilterQuery(visitIdFq, "BATCH_ID:" + batchId);
            solrQuery1.setFields("id", "VISITID", "REVIEW_CASE_IDS");
            SolrDocumentList unreasonableList = SolrUtil.call(solrQuery1, EngineUtil.MEDICAL_UNREASONABLE_ACTION).getResults();
            for (SolrDocument doc : unreasonableList) {
                ReviewSystemViewVo bean = map.get(doc.getFieldValue("VISITID").toString());
                bean.setId(doc.getFieldValue("id").toString());
                bean.setReviewCaseIds(doc.containsKey("REVIEW_CASE_IDS") ?
                        doc.getFieldValues("REVIEW_CASE_IDS").stream().map(Object::toString).collect(Collectors.toList())
                        : new ArrayList<>());

            }


        }

        return Result.ok(pageList);
    }

    @AutoLog(value = "系统审核-规则结果分页列表查询")
    @ApiOperation(value = "系统审核-规则结果分页列表查询", notes = "系统审核-规则结果分页列表查询")
    @GetMapping(value = "/drugList")
    public Result<?> queryPageDrugList(ReviewSystemDrugViewVo searchObj,
                                       String batchId,
                                       String ruleId,
                                       @RequestParam(name = "ruleType") String ruleType,
                                       String mSex,
                                       String mVisittypeId,
                                       String mYearage,
                                       String mDiseasename,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                       HttpServletRequest req) throws Exception {
        // 构造主表条件
        SolrQuery solrQuery = initDrugQuery(searchObj, ruleType,
                mSex, mVisittypeId, mYearage, mDiseasename, req);

        String collection;
        if (StringUtils.isNotBlank(batchId)) {
            solrQuery.addFilterQuery("BATCH_ID:" + batchId);
            collection = EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
        } else {
            solrQuery.addFilterQuery("BATCH_ID:" + ruleId);
            collection = EngineUtil.MEDICAL_TRAIL_DRUG_ACTION;
        }

        Page<ReviewSystemDrugViewVo> page = new Page<>(pageNo, pageSize);
        IPage<ReviewSystemDrugViewVo> pageList = SolrQueryGenerator.page(page, ReviewSystemDrugViewVo.class,
                solrQuery, collection, FIELD_DRUG_MAPPING);

        if (pageList.getRecords().size() > 0) {

            Map<String, ReviewSystemDrugViewVo> map = new HashMap<>();
            for (ReviewSystemDrugViewVo bean : pageList.getRecords()) {
                /*if(bean.getRuleScope() != null){
                    bean.setRuleScopes(StringUtils.join(bean.getRuleScope(),","));
                }*/
                map.put(bean.getVisitid(), bean);
            }
            String visitIdFq = "VISITID:(\"" + StringUtils.join(map.keySet(), "\",\"") + "\")";

            SolrQuery solrQuery1 = new SolrQuery("*:*");
            solrQuery1.addFilterQuery(visitIdFq);
            solrQuery1.setFields("VISITID","SEX", "YEARAGE","MONTHAGE","DAYAGE", "DISEASENAME", "DISEASECODE");
            SolrDocumentList masterList = SolrUtil.call(solrQuery1, EngineUtil.DWB_MASTER_INFO).getResults();
            for (SolrDocument doc : masterList) {
                ReviewSystemDrugViewVo bean = map.get(doc.getFieldValue("VISITID").toString());
                Object sexObj = doc.getFieldValue("SEX");
                Object yearageObj = doc.getFieldValue("YEARAGE");
                Object monthageObj = doc.getFieldValue("MONTHAGE");
                Object dayageObj = doc.getFieldValue("DAYAGE");
                Object diseasenameObj = doc.getFieldValue("DISEASENAME");
                Object diseasecodeObj = doc.getFieldValue("DISEASECODE");
                if (sexObj != null) {
                    bean.setSex(sexObj.toString());
                }
                if (yearageObj != null) {
                    bean.setYearage((Double) yearageObj);
                }
                if (monthageObj != null) {
                    bean.setMonthage((Double) monthageObj);
                }
                if (dayageObj != null) {
                    bean.setDayage((Double) dayageObj);
                }

                if (diseasenameObj != null) {
                    if(diseasecodeObj != null){
                        bean.setDiseasename(diseasenameObj + "(" + diseasecodeObj +")");
                    } else {
                        bean.setDiseasename(diseasenameObj.toString());
                    }

                }

            }

        }

        return Result.ok(pageList);
    }

    private SolrQuery initDrugQuery(ReviewSystemDrugViewVo searchObj, String ruleType,
                                    String visittypeId, String sex, String yearage,
                                    String diseasename, HttpServletRequest req) {
        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());


        solrQuery.addFilterQuery("RULE_TYPE:" + ruleType);

       /* if (StringUtils.isNotBlank(searchObj.getRuleDesc())) {
            solrQuery.addFilterQuery("RULE_FDESC:" + searchObj.getRuleDesc());
            solrQuery.removeFilterQuery("RULE_DESC:" + searchObj.getRuleDesc());
        }*/

        List<String> masterQs = new ArrayList<>();
        if (StringUtils.isNotBlank(visittypeId)) {
            masterQs.add("VISITTYPE_ID:" + visittypeId);
        }

        if (StringUtils.isNotBlank(sex)) {
            masterQs.add("SEX:" + sex);
        }

        if (StringUtils.isNotBlank(yearage)) {
            masterQs.add("YEARAGE:" + yearage);
        }

        if (StringUtils.isNotBlank(diseasename)) {
            masterQs.add("DISEASENAME:" + diseasename);
        }

        if (masterQs.size() > 0) {
        	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "VISITID");
            solrQuery.addFilterQuery(plugin.parse() + StringUtils.join(masterQs, " AND "));
        }

        return solrQuery;
    }

    /*@AutoLog(value = "系统审核-规则结果分页列表查询")
    @ApiOperation(value = "系统审核-规则结果分页列表查询", notes = "系统审核-规则结果分页列表查询")
    @GetMapping(value = "/drugList")
    public Result<?> queryPageDrugList(DwbMasterInfoVo dwbMasterInfo,
                                       String batchId,
                                       String ruleId,
                                       @RequestParam(name = "ruleType") String ruleType,
                                       String itemname,
                                       String itemcode,
                                       String ruleDesc,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                       HttpServletRequest req) throws Exception {
        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(dwbMasterInfo, req.getParameterMap());


        String fq = "RULE_TYPE:" + ruleType;
        String collection;

        if(StringUtils.isNotBlank(batchId)){
            fq += " AND BATCH_ID:" + batchId;
            collection = EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
        } else {
            fq += " AND BATCH_ID:" + ruleId;
            collection = EngineUtil.MEDICAL_TRAIL_DRUG_ACTION;
        }
        if (StringUtils.isNotBlank(itemname)) {
            fq += " AND ITEMNAME:" + itemname;
        }
        if (StringUtils.isNotBlank(itemcode)) {
            fq += " AND ITEMCODE:" + itemcode;
        }
        if(StringUtils.isNotBlank(ruleDesc)){
            fq += " AND RULE_FDESC:" + ruleDesc;
        }
        solrQuery.addFilterQuery("{!join from=VISITID fromIndex=" + collection + " to=VISITID}" + fq);


        Page<ReviewSystemViewVo> page = new Page<>(pageNo, pageSize);
        IPage<ReviewSystemViewVo> pageList = SolrQueryGenerator.page(page, ReviewSystemViewVo.class,
                solrQuery, EngineUtil.DWB_MASTER_INFO, FIELD_MAPPING);

        if (pageList.getRecords().size() > 0) {

            Map<String, ReviewSystemViewVo> map = new HashMap<>();
            for (ReviewSystemViewVo bean : pageList.getRecords()) {
                bean.setRuleDesc(new HashSet<>());
                map.put(bean.getVisitid(), bean);
            }
            String visitIdFq = "VISITID:(\"" + StringUtils.join(map.keySet(), "\",\"") + "\")";

            SolrQuery solrQuery1 = new SolrQuery("*:*");
            solrQuery1.addFilterQuery(visitIdFq, "BATCH_ID:" + batchId);
            solrQuery1.setFields("VISITID", "RULE_FDESC");
            SolrDocumentList unreasonableList = SolrUtil.call(solrQuery1, collection).getResults();
            for (SolrDocument doc : unreasonableList) {
                ReviewSystemViewVo bean = map.get(doc.getFieldValue("VISITID").toString());
                Collection descList = doc.getFieldValues("RULE_FDESC");
                for(Object descObj: descList){
                    String desc = descObj.toString();
                    if(desc.contains("::")){
                        bean.getRuleDesc().add(desc.substring(desc.indexOf("::") + 2));
                    }
                }
            }

        }

        return Result.ok(pageList);
    }*/

    @AutoLog(value = "系统审核-规则结果列表导出")
    @ApiOperation(value = "系统审核-规则结果列表导出", notes = "系统审核-规则结果列表导出")
    @RequestMapping(value = "/drugListExport")
    public void drugListExport(ReviewSystemDrugViewVo searchObj,
                               String batchId,
                               String ruleId,
                               @RequestParam(name = "ruleType") String ruleType,
                               String mSex,
                               String mVisittypeId,
                               String mYearage,
                               String mDiseasename,
                               HttpServletRequest req,
                               HttpServletResponse response) throws Exception {
        // 构造主表条件
        SolrQuery solrQuery = initDrugQuery(searchObj, ruleType,
                mSex, mVisittypeId, mYearage, mDiseasename, req);

        String collection;
        if (StringUtils.isNotBlank(batchId)) {
            solrQuery.addFilterQuery("BATCH_ID:" + batchId);
            collection = EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
        } else {
            solrQuery.addFilterQuery("BATCH_ID:" + ruleId);
            collection = EngineUtil.MEDICAL_TRAIL_DRUG_ACTION;
        }

        String title = "1".equals(ruleType) ? "药品合规结果" : "收费合规结果";
        //response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + "导出" + System.currentTimeMillis() + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        OutputStream os = response.getOutputStream();

        this.service.exportDrugList(solrQuery, collection, FIELD_DRUG_MAPPING, title,ruleType, os);
    }


    @AutoLog(value = "系统审核-规则结果列表导出")
    @ApiOperation(value = "系统审核-规则结果列表导出", notes = "系统审核-规则结果列表导出")
    @RequestMapping(value = "/drugListExportThread")
    public Result<?> drugListExportThread(ReviewSystemDrugViewVo searchObj,
                                          String batchId,
                                          String ruleId,
                                          @RequestParam(name = "ruleType") String ruleType,
                                          String mSex,
                                          String mVisittypeId,
                                          String mYearage,
                                          String mDiseasename,
                                          HttpServletRequest req) throws Exception {

        // 构造主表条件
        SolrQuery solrQuery = initDrugQuery(searchObj, ruleType,
                mSex, mVisittypeId, mYearage, mDiseasename, req);

        String collection;
        if (StringUtils.isNotBlank(batchId)) {
            solrQuery.addFilterQuery("BATCH_ID:" + batchId);
            collection = EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
        } else {
            solrQuery.addFilterQuery("BATCH_ID:" + ruleId);
            collection = EngineUtil.MEDICAL_TRAIL_DRUG_ACTION;
        }

        long count = SolrQueryGenerator.count(collection, solrQuery);

        String excelName = "1".equals(ruleType) ? "药品合规结果" : "收费合规结果";
        ThreadUtils.EXPORT_POOL.addRemote(excelName + "_导出", "xlsx", (int) count, (os) -> {
            try {
                this.service.exportDrugList(solrQuery, collection, FIELD_DRUG_MAPPING, excelName,ruleType, os);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(e.getMessage());
            }
            return Result.ok();
        });

        return Result.ok("等待导出");
    }

    @AutoLog(value = "系统审核-药品规则结果统计导出")
    @ApiOperation(value = "系统审核-药品规则结果统计导出", notes = "系统审核-药品规则结果统计导出")
    @GetMapping(value = "/drugExportStatistics")
    public void drugExportStatistics(DwbMasterInfoVo dwbMasterInfo,
                                     String batchId,
                                     String ruleId,
                                     @RequestParam(name = "ruleType") String ruleType,
                                     @RequestParam(name = "groupBy") String groupBy,
                                     String itemname,
                                     String itemcode,
                                     String ruleDesc,
                                     HttpServletRequest req,
                                     HttpServletResponse response) throws Exception {
        // 构造主表条件
        SolrQuery masterSolrQuery = SolrQueryGenerator.initQuery(dwbMasterInfo, req.getParameterMap());
        String[] masterFqs = masterSolrQuery.getFilterQueries();

        String collection;
        String fq = "RULE_TYPE:" + ruleType;
        if (StringUtils.isNotBlank(itemname)) {
            fq += " AND ITEMNAME:" + itemname;
        }
        if (StringUtils.isNotBlank(itemcode)) {
            fq += " AND ITEMCODE:" + itemcode;
        }
        if (StringUtils.isNotBlank(ruleDesc)) {
            fq += " AND RULE_FDESC:" + ruleDesc;
        }
        if (StringUtils.isNotBlank(batchId)) {
            fq += " AND BATCH_ID:" + batchId;
            collection = EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
        } else {
            fq += " AND BATCH_ID:" + ruleId;
            collection = EngineUtil.MEDICAL_TRAIL_DRUG_ACTION;
        }
        List<String> fqList = new ArrayList<>();
        fqList.add(fq);
        if (masterFqs != null) {
        	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "VISITID");
            fqList.add(plugin.parse() + StringUtils.join(masterFqs, " AND "));
        }
        String title = "药品结果统计字段-";
        if ("ITEMCODE".equals(groupBy)) {
            title += "项目";
        } else if ("ORGID".equals(groupBy)) {
            title += "医疗机构";
        }
        title += System.currentTimeMillis();
        //response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        OutputStream os = response.getOutputStream();
        this.exportExcel(collection, fqList.toArray(new String[0]), groupBy, os);
    }

    private void exportExcel(String collection, String[] fqs, String groupBy, OutputStream os) throws Exception {
        String[] titles = {"", "", "出现次数", "涉及总金额"};
        String[] fields = {"val", "", "count", "sum(ITEM_AMT)"};
//        String[] titles = {"医疗机构编码","医疗机构名称", "出现次数", "涉及总金额"};
        if ("ITEMCODE".equals(groupBy)) {
            titles[0] = "项目编码";
            titles[1] = "项目名称";
            fields[1] = "max(ITEMNAME)";
        } else if ("ORGID".equals(groupBy)) {
            titles[0] = "医疗机构编码";
            titles[1] = "医疗机构名称";
            fields[1] = "max(ORGNAME)";
        }

        WritableCellFormat textFormat = new WritableCellFormat(NumberFormats.TEXT);
        WritableWorkbook wwb = Workbook.createWorkbook(os);
        WritableSheet sheet = wwb.createSheet("sheet1", 0);
        AtomicInteger startHang = new AtomicInteger();
        for (int i = 0, len = titles.length; i < len; i++) {
            sheet.addCell(new Label(i, startHang.get(), titles[i]));
        }

        int fieldLen = fields.length;

        JSONObject facetChild = new JSONObject();
        facetChild.put(fields[1], fields[1]);
        facetChild.put(fields[3], fields[3]);

        JSONObject jsonFacet = new JSONObject();
        jsonFacet.put("type", "terms");
        jsonFacet.put("field", groupBy);
        jsonFacet.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        jsonFacet.put("overrequest", Integer.MAX_VALUE);
        jsonFacet.put("facet", facetChild);

        SolrUtil.jsonFacet(collection, fqs, jsonFacet.toJSONString(), jsonObj -> {
            startHang.incrementAndGet();
            for (int i = 0; i < fieldLen; i++) {
                Object val = jsonObj.get(fields[i]);
                if (val != null) {
                    if (val instanceof Double || val instanceof Float) {
                        val = new BigDecimal(val.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
                    } else if (val instanceof BigDecimal) {
                        val = ((BigDecimal) val).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    Label label = new Label(i, startHang.get(), val.toString(),
                            textFormat);
                    try {
                        sheet.addCell(label);
                    } catch (WriteException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        wwb.write();
        wwb.close();
    }

    private SolrQuery constructListQuery(DwbMasterInfoVo dwbMasterInfo,
                                         String unreasonableFq,
                                         String caseId,
                                         String grades,
                                         HttpServletRequest req) throws Exception {

        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(dwbMasterInfo, req.getParameterMap());
        solrQuery.removeFilterQuery("CASE_ID:" + caseId);
        SolrQuery.ORDER gradeOrder = null;
        List<SolrQuery.SortClause> sortList = solrQuery.getSorts();
        for (SolrQuery.SortClause sortClause : sortList) {
            if ("GRADE_VALUE".equals(sortClause.getItem())) {
                solrQuery.removeSort("GRADE_VALUE");
                gradeOrder = sortClause.getOrder();
                break;
            }
        }
        // 结果表查询条件
        solrQuery.addFilterQuery(unreasonableFq);

        // 设置返回字段
        for (Map.Entry<String, String> entry : FIELD_MAPPING.entrySet()) {
            solrQuery.addField(entry.getValue());
        }
//        solrQuery.setFields("VISITID","CLIENTNAME","SEX","YEARAGE","TOTALFEE","DISEASENAME");
        // 评分
        if (StringUtils.isNotBlank(grades)) {
            List<MedicalFormalFlowRuleGrade> gradeList = JSONArray.parseArray(URLDecoder.decode(grades), MedicalFormalFlowRuleGrade.class);
            List<String> facetList = gradeList.stream().map(grade -> String.format("max-%s:\"max(%s)\"", grade.getEvaluateField(), grade.getEvaluateField())).collect(Collectors.toList());

            solrQuery.set("json.facet", "{" + StringUtils.join(facetList, ",") + "}");
            solrQuery.setRows(0);

            QueryResponse response = SolrUtil.call(solrQuery, EngineUtil.DWB_MASTER_INFO);
            long recordNum = response.getResults().getNumFound();
            if (recordNum == 0) {
                throw new Exception("此次模型条件下的记录为空");
            }
            NestableJsonFacet jsonFacet = response.getJsonFacetingResponse();
            List<String> gradeParams = new ArrayList<>();
            for (MedicalFormalFlowRuleGrade grade : gradeList) {
                String field = grade.getEvaluateField();
                Number valObj = jsonFacet.getStatFacetValue("max-" + field);
                if (valObj != null) {
                    double val = valObj.doubleValue() - grade.getStandardVal().doubleValue();
                    if (val == 0) {
                        throw new Exception("字段：" + field + "的最大值=基准值");
                    }
                    String param = "mul(div(" +
                            "sub(" + field + "," + grade.getStandardVal() + ")," + val +
                            ")," + grade.getWeight() + ")";
                    gradeParams.add(param);
                }
            }


            if (gradeParams.size() > 0) {
                // 评分区间
                String gradeMin = req.getParameter("gradeMin");
                String gradeMax = req.getParameter("gradeMax");
                gradeMin = StringUtils.isBlank(gradeMin) || !StringUtils.isNumeric(gradeMin) ? "" : (" l=" + gradeMin);
                gradeMax = StringUtils.isBlank(gradeMax) || !StringUtils.isNumeric(gradeMax) ? "" : (" u=" + gradeMax);
                if (!("".equals(gradeMin) && "".equals(gradeMax))) {
                    String rangeFq = "{!frange " + gradeMin + gradeMax + "}$GRADE_VALUE";
                    solrQuery.addFilterQuery(rangeFq);
                }
                solrQuery.set("GRADE_VALUE", "sum(" + StringUtils.join(gradeParams, ",") + ")");
                solrQuery.addField("GRADE_VALUE:$GRADE_VALUE");
                if (gradeOrder != null) {
                    solrQuery.addSort("$GRADE_VALUE", gradeOrder);
                } else if (sortList.size() == 0) {
                    solrQuery.addSort("$GRADE_VALUE", SolrQuery.ORDER.desc);
                }
            }
            solrQuery.remove("json.facet");
        }

        return solrQuery;

    }


    private SolrQuery constructListClinicalQuery(DwbMasterInfoVo dwbMasterInfo,
                                                 String batchId,
                                                 String clinicalId,
                                                 HttpServletRequest req) throws Exception {

        // 构造主表条件
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(dwbMasterInfo, req.getParameterMap());
        // 结果表查询条件
        String unreasonableFq = "BATCH_ID:" + batchId + " AND CLINICAL_IDS:" + clinicalId;
        SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "VISITID", "VISITID");
        solrQuery.addFilterQuery(plugin.parse() + unreasonableFq);
        // 设置返回字段
        for (Map.Entry<String, String> entry : FIELD_MAPPING.entrySet()) {
            solrQuery.addField(entry.getValue());
        }

        return solrQuery;

    }

    @AutoLog(value = " 系统审核-设置规则模型已审核")
    @ApiOperation(value = "系统审核-设置规则模型已审核", notes = "系统审核-设置规则模型已审核")
    @PutMapping(value = "/auditedCase")
    public Result<?> auditedCase(@RequestParam(name = "batchId") String batchId,
                                 @RequestParam(name = "caseId") String caseId) throws Exception {

        String[] fqs = {"BATCH_ID:" + batchId, "CASE_ID:" + caseId};
        long count = SolrQueryGenerator.count(EngineUtil.MEDICAL_UNREASONABLE_ACTION, fqs);
        UpdateWrapper<TaskBatchBreakRuleDel> updateWrapper = new UpdateWrapper<TaskBatchBreakRuleDel>()
                .eq("BATCH_ID", batchId)
                .eq("CASE_ID", caseId)
                .set("REVIEW_STATUS", MedicalConstant.REVIEW_STATE_AUDITED)
                .set("REVIEW_TIME", new Date())
                .set("REVIEW_ACOUNT", count);

        taskBatchBreakRuleDelService.update(updateWrapper);

        return Result.ok("操作成功");
    }

    @AutoLog(value = " 系统审核-设置当前条件下模型结果就诊记录是否通过")
    @ApiOperation(value = "系统审核-设置当前条件下模型结果就诊记录是否通过", notes = "系统审核-设置当前条件下模型结果就诊记录是否通过")
    @GetMapping(value = "/pushReviewAll")
    public Result<?> pushReviewAll(DwbMasterInfoVo dwbMasterInfo,
                                   @RequestParam(name = "batchId") String batchId,
                                   @RequestParam(name = "caseId") String caseId,
                                   String reviewCaseIds,
                                   String grades,
                                   @RequestParam(name = "isPush", defaultValue = "true") boolean isPush,
                                   HttpServletRequest req) throws Exception {

        String unreasonableFq = "BATCH_ID:" + batchId + " AND CASE_ID:" + caseId;
        if ("1".equals(reviewCaseIds)) {
            unreasonableFq += " AND REVIEW_CASE_IDS:" + caseId;
        } else if ("0".equals(reviewCaseIds)) {
            unreasonableFq += " AND -REVIEW_CASE_IDS:" + caseId;
        }
        SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "VISITID", "VISITID");
        String unreasonableJoinFq = "_query_:\"" + plugin.parse() + unreasonableFq + "\"";
        // 构造主表条件
        SolrQuery masterQuery = this.constructListQuery(dwbMasterInfo, unreasonableJoinFq, caseId, grades, req);
        // 评分后移除副表join
        masterQuery.removeFilterQuery(unreasonableJoinFq);

        String[] masterFqs = masterQuery.getFilterQueries();
        // 构造主表条件
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.setFields("id");
        if (masterFqs != null && masterFqs.length > 0) {
            String gradeParam = masterQuery.get("GRADE_VALUE");
            if (gradeParam != null) {
                for (int i = 0, len = masterFqs.length; i < len; i++) {
                    if (masterFqs[i].contains("$GRADE_VALUE")) {
                        masterFqs[i] = masterFqs[i].replace("$GRADE_VALUE", gradeParam);
                        break;
                    }
                }
            }
            plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "VISITID");
            solrQuery.addFilterQuery(plugin.parse() + StringUtils.join(masterQuery.getFilterQueries(), " AND "));
        }
        // 主表join的条件给自身
        solrQuery.addFilterQuery(unreasonableFq);
        solrQuery.addFilterQuery((isPush ? "-REVIEW_CASE_IDS:" : "REVIEW_CASE_IDS:") + caseId);
        String action = isPush ? "add" : "removeregex";
        // 获取查询出的ID
        SolrClient solrClient = SolrUtil.getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        SolrUtil.export(solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (map, index) -> {
            String id = String.valueOf(map.get("id"));
            // 构造更新参数
            SolrInputDocument document = new SolrInputDocument();
            document.setField("id", id);
            document.setField("REVIEW_CASE_IDS", SolrUtil.initActionValue(caseId, action));
            try {
                solrClient.add(document);
            } catch (SolrServerException | IOException e) {
                log.error("", e);
            }
        });
        // 提交
        solrClient.commit();
        solrClient.close();
        return Result.ok("操作成功");
    }

    @AutoLog(value = " 系统审核-设置模型结果就诊记录是否通过")
    @ApiOperation(value = "系统审核-设置模型结果就诊记录是否通过", notes = "系统审核-设置模型结果就诊记录是否通过")
    @PutMapping(value = "/pushReview")
    public Result<?> pushReview(@RequestParam(name = "ids") String ids,
                                @RequestParam(name = "caseId") String caseId,
                                @RequestParam(name = "isPush") boolean isPush) throws Exception {

        service.operateReviewCaseId(ids.split(","), caseId, isPush ? "add" : "removeregex");

        return Result.ok("操作成功");
    }
}
