package com.ai.modules.review.controller;

import com.ai.common.MedicalConstant;
import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ExportUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.engine.util.stream.FacetConstruct;
import com.ai.modules.formal.service.IMedicalFormalBehaviorService;
import com.ai.modules.formal.service.IMedicalFormalCaseItemRelaService;
import com.ai.modules.formal.vo.MedicalFormalBehaviorVO;
import com.ai.modules.formal.vo.MedicalFormalCaseItemRelaVO;
import com.ai.modules.his.service.IHisMedicalFormalCaseService;
import com.ai.modules.review.service.IReviewSecondService;
import com.ai.modules.review.service.IReviewService;
import com.ai.modules.review.vo.*;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.service.ITaskBatchBreakRuleService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "模型结果总览")
@RestController
@RequestMapping("/reviewSecond")
public class ReviewSecondController {

    @Autowired
    IReviewSecondService service;

    @Autowired
    IMedicalFormalBehaviorService medicalFormalBehaviorService;

    @Autowired
    private IMedicalFormalCaseItemRelaService medicalFormalCaseItemRelaService;

    private String[] unreasonableFqs = {
            "FIR_REVIEW_STATUS:(\"blank\",\"grey\")",
            "PUSH_STATUS:1"
    };

    private SolrQuery commonSolrQuery(DwbMasterInfoVo masterInfoVo, String batchId, HttpServletRequest req) {
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(masterInfoVo, req.getParameterMap());
        List<String> unreasonableFqList = initUnreasonableFq(batchId, req);
        SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "VISITID", "VISITID");
        solrQuery.addFilterQuery(plugin.parse() + StringUtils.join(unreasonableFqList, " AND "));

        return solrQuery;
    }

    private List<String> initUnreasonableFq(String batchId, HttpServletRequest req) {
        List<String> unreasonableFqList = new ArrayList<>(Arrays.asList(unreasonableFqs));
        unreasonableFqList.add("BATCH_ID:" + batchId);

        String clinicalIds = req.getParameter("clinicalIds");
        if (StringUtils.isNotBlank(clinicalIds)) {
            unreasonableFqList.add("CLINICAL_IDS:" + clinicalIds);
        }
        return unreasonableFqList;
    }

    /**
     * 不合理行为分页列表查询
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "不合理行为-分页列表查询")
    @ApiOperation(value = "不合理行为-分页列表查询", notes = "不合理行为-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> reviewSecondList(DwbMasterInfoVo masterInfoVo,
                                      @RequestParam(name = "batchId") String batchId,
                                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                      HttpServletRequest req) throws Exception {

        SolrQuery solrQuery = commonSolrQuery(masterInfoVo, batchId, req);
        Page<ReviewSecondVo> page = new Page<>(pageNo, pageSize);
        IPage<ReviewSecondVo> pageList = SolrQueryGenerator.page(page, ReviewSecondVo.class,
                solrQuery, EngineUtil.DWB_MASTER_INFO, SolrQueryGenerator.REVIEW_SECOND_MAPPING);

        if (pageList.getRecords().size() > 0) {

            Map<String, ReviewSecondVo> map = new HashMap<>();
            for (ReviewSecondVo bean : pageList.getRecords()) {
                map.put(bean.getVisitid(), bean);
            }
            String visitIdFq = "VISITID:(\"" + StringUtils.join(map.keySet(), "\",\"") + "\")";

            SolrQuery unreasonableQuery = new SolrQuery("*:*");
            unreasonableQuery.addFilterQuery(visitIdFq, "BATCH_ID:" + batchId);
            unreasonableQuery.setFields("id", "VISITID", "FIR_REVIEW_STATUS", "SEC_REVIEW_STATUS", "REVIEW_CASE_IDS");
            SolrDocumentList unreasonableList = SolrQueryGenerator.list(EngineUtil.MEDICAL_UNREASONABLE_ACTION, unreasonableQuery);
            for (SolrDocument doc : unreasonableList) {
                ReviewSecondVo bean = map.get(doc.getFieldValue("VISITID").toString());
                bean.setId(doc.getFieldValue("id").toString());
                if (doc.containsKey("FIR_REVIEW_STATUS")) {
                    bean.setFirReviewStatus(doc.getFieldValue("FIR_REVIEW_STATUS").toString());
                }
                if (doc.containsKey("SEC_REVIEW_STATUS")) {
                    bean.setSecReviewStatus(doc.getFieldValue("SEC_REVIEW_STATUS").toString());
                }
                if (doc.containsKey("REVIEW_CASE_IDS")) {
                    bean.setReviewCaseIds(doc.getFieldValues("REVIEW_CASE_IDS").stream().map(Object::toString).collect(Collectors.toList()));
                }
            }


        }

        return Result.ok(pageList);
    }

    /**
     * 直接导出excel
     *
     * @throws Exception
     */
    @RequestMapping(value = "/exportXls")
    public void exportXls(DwbMasterInfoVo masterInfoVo,
                          @RequestParam(name = "batchId") String batchId,
                          HttpServletRequest req,
                          HttpServletResponse response) throws Exception {
        SolrQuery[] solrQuerys;
        // 选中数据
        String selections = req.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            int qLen = selectionList.size() / 100;
            if (selectionList.size() % 100 > 0) {
                qLen++;
            }
            solrQuerys = new SolrQuery[qLen];
            for (int i = 0, len = selectionList.size(); i < len; i += 100) {
                int j = i + 100;
                if (j > len) {
                    j = len;
                }
                SolrQuery solrQuery = new SolrQuery("*:*");
                SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "VISITID", "VISITID");
                solrQuery.addFilterQuery(plugin.parse() + "id:(\"" + StringUtils.join(selectionList.subList(i, j), "\",\"") + "\")");
                solrQuerys[i / 100] = solrQuery;
            }
        } else {
            solrQuerys = new SolrQuery[]{commonSolrQuery(masterInfoVo, batchId, req)};
        }

        String excelName = "不合规病例_导出";
        String title = excelName + System.currentTimeMillis();
        //response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        OutputStream os = response.getOutputStream();
        this.service.exportExcel(solrQuerys, batchId, os);
    }

    @RequestMapping(value = "/exportXlsThread")
    public Result exportXlsThread(DwbMasterInfoVo masterInfoVo,
                                  @RequestParam(name = "batchId") String batchId,
                                  HttpServletRequest req) throws Exception {
        SolrQuery solrQuery = commonSolrQuery(masterInfoVo, batchId, req);
        //查询总条数
        long count = SolrQueryGenerator.count(EngineUtil.DWB_MASTER_INFO, solrQuery);
        solrQuery.setRows((int) count);
        String excelName = "不合规病例_导出";
        ThreadUtils.EXPORT_POOL.addRemote(excelName, "xlsx", (int) count, (os) -> {
            try {
                this.service.exportExcel(new SolrQuery[]{solrQuery}, batchId, os);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(e.getMessage());
            }
            return Result.ok();
        });

        return Result.ok("等待导出");

    }


    @RequestMapping(value = "/exportOverview")
    public void exportOverview(@RequestParam(name = "batchId") String batchId, HttpServletRequest req, HttpServletResponse response) throws Exception {

        OutputStream os = response.getOutputStream();
        // 创建文件输出流
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        try {
            // 获取模型对应的不合规行为
            List<MedicalFormalBehaviorVO> behaviorCaseList = medicalFormalBehaviorService.selectBehaviorCaseByBatch(batchId);
            Map<String, List<MedicalFormalBehaviorVO>> caseBehaviorMap = new HashMap<>();
            Map<String, MedicalFormalBehaviorVO> behaviorIdMap = new HashMap<>();
            for (MedicalFormalBehaviorVO bean : behaviorCaseList) {
                behaviorIdMap.put(bean.getId(), bean);
                List<MedicalFormalBehaviorVO> list = caseBehaviorMap.computeIfAbsent(bean.getCaseId(), k -> new ArrayList<>());
                list.add(bean);
            }

            this.service.exportExcelCase(batchId, caseBehaviorMap, workbook);
            this.service.exportExcelHosp(batchId, behaviorIdMap, workbook);
            this.service.exportExcelDoc(batchId, behaviorIdMap, workbook);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workbook.write(os);
            workbook.dispose();
        }
    }

    @RequestMapping(value = "/exportOverviewThread")
    public Result exportOverviewThread(@RequestParam(name = "batchId") String batchId, HttpServletRequest req, HttpServletResponse response) throws Exception {

        String excelName = "不合规总览_导出";
        ThreadUtils.EXPORT_POOL.addRemote(excelName, "xls", -1, (os) -> {
            // 创建文件输出流
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            try {
                // 获取模型对应的不合规行为
                List<MedicalFormalBehaviorVO> behaviorCaseList = medicalFormalBehaviorService.selectBehaviorCaseByBatch(batchId);
                Map<String, List<MedicalFormalBehaviorVO>> caseBehaviorMap = new HashMap<>();
                Map<String, MedicalFormalBehaviorVO> behaviorIdMap = new HashMap<>();
                for (MedicalFormalBehaviorVO bean : behaviorCaseList) {
                    behaviorIdMap.put(bean.getId(), bean);
                    List<MedicalFormalBehaviorVO> list = caseBehaviorMap.computeIfAbsent(bean.getCaseId(), k -> new ArrayList<>());
                    list.add(bean);
                }

                this.service.exportExcelCase(batchId, caseBehaviorMap, workbook);
                this.service.exportExcelHosp(batchId, behaviorIdMap, workbook);
                this.service.exportExcelDoc(batchId, behaviorIdMap, workbook);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(e.getMessage());
            } finally {
                try {
                    workbook.write(os);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                workbook.dispose();

            }
            return Result.ok();
        });

        return Result.ok("等待导出");

    }


    @AutoLog(value = "不合规病例-统计导出")
    @ApiOperation(value = "不合规病例-统计导出", notes = "不合规病例-统计导出")
    @RequestMapping(value = "/exportStatistics")
    public Result exportStatistics(DwbMasterInfoVo masterInfoVo,
                                 @RequestParam(name = "batchId") String batchId,
                                 @RequestParam(name = "type") String type,
                                 HttpServletRequest req,
                                 HttpServletResponse response) throws Exception {

        SolrQuery solrQuery = SolrQueryGenerator.initQuery(masterInfoVo, req.getParameterMap());
        List<String> unreasonableFqList = initUnreasonableFq(batchId, req);

        OutputStream os = response.getOutputStream();
        if ("ITEM_DETAIL".equals(type)) {
            service.exportStatItemDetail(solrQuery, unreasonableFqList, batchId, os);
        } else if ("ITEM_TOTAL".equals(type)) {
            service.exportStatItemTotal(solrQuery, unreasonableFqList, batchId, os);
        } else if ("CASE_TOTAL".equals(type)) {
            service.exportStatCaseTotal(solrQuery, unreasonableFqList, os);
        }

        return Result.ok("成功");

    }


}
