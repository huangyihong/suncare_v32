package com.ai.modules.review.controller;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.engine.util.stream.FacetConstruct;
import com.ai.modules.formal.service.IMedicalFormalCaseItemRelaService;
import com.ai.modules.formal.vo.MedicalFormalCaseItemRelaVO;
import com.ai.modules.review.service.IReviewService;
import com.ai.modules.review.vo.*;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Slf4j
@Api(tags = "模型结果总览")
@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private IReviewService service;

    @Autowired
    private IMedicalFormalCaseItemRelaService medicalFormalCaseItemRelaService;

    private static Map<String, String> FIELD_MAPPING = SolrUtil.initFieldMap(MedicalUnreasonableActionVo.class);

    private static Map<String, String> MASTER_FIELD_MAPPING = SolrUtil.initFieldMap(DwbMasterInfoVo.class);

    private static Map<String, String> ORDER_FIELD_MAPPING = SolrUtil.initFieldMap(DwbOrderVo.class);
    private static Map<String, String> UNREASONABLE_DRUG_FIELD_MAPPING = SolrUtil.initFieldMap(MedicalUnreasonableDrugActionVo.class);

    private static Map<String, String> CHARGE_FIELD_MAPPING = SolrUtil.initFieldMap(DwbChargeDetailVo.class);
    private static Map<String, String> DIAG_FIELD_MAPPING = SolrUtil.initFieldMap(DwbDiagVo.class);

    /**
     * 人工审查-分页列表查询
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = " 人工审查-分页列表查询")
    @ApiOperation(value = "人工审查-分页列表查询", notes = "人工审查-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(MedicalUnreasonableActionVo medicalUnreasonableActionVo,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) throws Exception {
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(medicalUnreasonableActionVo, req.getParameterMap());

        Page<MedicalUnreasonableActionVo> page = new Page<>(pageNo, pageSize);
        IPage<MedicalUnreasonableActionVo> pageList = SolrQueryGenerator.page(page, MedicalUnreasonableActionVo.class,
                solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, FIELD_MAPPING);

        return Result.ok(pageList);
    }

    @AutoLog(value = "违反规则查询")
    @ApiOperation(value = "违反规则查询", notes = "违反规则查询")
    @GetMapping(value = "/queryBreakRule")
    public Result<?> queryBreakRule(String visitid, String batchId) throws Exception {
        String[] fls = {"BUSI_TYPE", "CASE_ID", "CASE_NAME", "ACTION_ID", "ACTION_NAME", "ACTION_TYPE", "ACTION_TYPE_NAME", "ACTION_DESC"};
        String[] fqs = {"VISITID:" + visitid, "BATCH_ID:" + batchId};

        SolrDocumentList list = SolrQueryGenerator.list(EngineUtil.MEDICAL_UNREASONABLE_ACTION, fqs, fls);
        return Result.ok(list);
    }


    @AutoLog(value = "违反药品和收费结果查询")
    @ApiOperation(value = "违反药品和收费结果查询", notes = "违反药品和收费结果查询")
    @GetMapping(value = "/queryUnreasonableDrugAction")
    public Result<?> queryUnreasonableDrugAction(MedicalUnreasonableDrugActionVo medicalUnreasonableDrugActionVo, HttpServletRequest req) throws Exception {
        List<MedicalUnreasonableDrugActionVo> list = SolrQueryGenerator.list(medicalUnreasonableDrugActionVo,
                req.getParameterMap(), EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, UNREASONABLE_DRUG_FIELD_MAPPING);
        IPage<MedicalUnreasonableDrugActionVo> pageList = new Page<>();
        pageList.setRecords(list);
        return Result.ok(pageList);

    }


    @AutoLog(value = "药品项目列表查询")
    @ApiOperation(value = "药品项目列表查询", notes = "药品项目列表查询")
    @GetMapping(value = "/queryChargeItemByVisitId")
    public Result<?> queryChargeByVisitId(@RequestParam(name = "visitid") String visitid) throws Exception {

        FacetConstruct construct = new FacetConstruct(EngineUtil.DWB_CHARGE_DETAIL);
        construct.addBucket("CHARGECLASS_ID", "ITEMCODE", "ITEMNAME");
        construct.addQuery("VISITID:" + visitid);
        construct.addStat("sum(FEE)");
        List<Map<String, Object>> list = SolrUtil.stream(construct.toExpression());
        return Result.ok(list);
    }

    @AutoLog(value = "药品项目列表查询")
    @ApiOperation(value = "药品项目列表查询", notes = "药品项目列表查询")
    @GetMapping(value = "/queryVisitCountByChargeItemCodes")
    public Result<?> queryVisitCountByChargeItemCodes(@RequestParam(name = "itemCodes") String itemCodes) throws Exception {

        FacetConstruct construct = new FacetConstruct(EngineUtil.DWB_CHARGE_DETAIL);
        construct.addBucket("ITEMCODE", "VISITID");
        construct.addQuery("ITEMCODE:(" + itemCodes + ")");
        List<Map<String, Object>> list = SolrUtil.stream(construct.toExpression());
        return Result.ok(list);
    }

    /**
     * 通过visitid和batchId查询审查过程
     *
     * @param id
     * @return
     * @throws Exception
     */
    @AutoLog(value = "不合理行为就诊记录审查表- 通过visitid和batchId查询审查过程")
    @ApiOperation(value = "不合理行为就诊记录审查表- 通过visitid和batchId查询审查过程", notes = "不合理行为就诊记录审查表- 通过visitid和batchId查询审查过程")
    @GetMapping(value = "/queryByVisitidAndBatchId")
    public Result<?> queryByVisitid(@RequestParam(name = "visitid", required = true) String visitid, @RequestParam(name = "batchId", required = true) String batchId) throws Exception {
        String[] fqs = {"VISITID:" + visitid, "BATCH_ID:" + batchId};

        MedicalUnreasonableActionVo bean = SolrQueryGenerator.getOne(EngineUtil.MEDICAL_UNREASONABLE_ACTION, fqs, MedicalUnreasonableActionVo.class, FIELD_MAPPING);

        return Result.ok(bean);
    }

    /**
     * 通过visitid和batchId查询审查过程
     *
     * @param id
     * @return
     * @throws Exception
     */
    @AutoLog(value = "不合理行为就诊记录审查表- 通过id查询审查过程")
    @ApiOperation(value = "不合理行为就诊记录审查表- 通过id查询审查过程", notes = "不合理行为就诊记录审查表- 通过id查询审查过程")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) throws Exception {
        String[] fqs = {"id:" + id};

        MedicalUnreasonableActionVo bean = SolrQueryGenerator.getOne(EngineUtil.MEDICAL_UNREASONABLE_ACTION, fqs, MedicalUnreasonableActionVo.class, FIELD_MAPPING);

        return Result.ok(bean);
    }

    /**
     * 保存多个审查结果
     *
     * @param obj
     * @return
     * @throws Exception
     */
    @AutoLog(value = "不合理行为就诊记录审查表-保存多个审查结果")
    @ApiOperation(value = "不合理行为就诊记录审查表-保存多个审查结果", notes = "不合理行为就诊记录审查表-保存多个审查结果")
    @PutMapping(value = "/saveReviews")
    public Result<?> saveReviews(@RequestBody JSONObject obj) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        service.saveReviews(obj, user);
        return Result.ok("保存成功!");
    }

    /**
     * 保存多个审查结果
     *
     * @param obj
     * @return
     * @throws Exception
     */
    @AutoLog(value = "不合理行为就诊记录审查表-保存多个审查结果")
    @ApiOperation(value = "不合理行为就诊记录审查表-保存多个审查结果", notes = "不合理行为就诊记录审查表-保存多个审查结果")
    @PutMapping(value = "/updateById")
    public Result<?> updateById(@RequestBody MedicalUnreasonableActionVo bean) throws Exception {
        SolrQueryGenerator.updateById(EngineUtil.MEDICAL_UNREASONABLE_ACTION, bean);
        return Result.ok("保存成功!");
    }

    /**
     * 不合理行为分页列表查询（第二次审查）
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "不合理行为-分页列表查询")
    @ApiOperation(value = "不合理行为-分页列表查询", notes = "不合理行为-分页列表查询")
    @GetMapping(value = "/reviewSecondlist")
    public Result<?> reviewSecondlist(MedicalUnreasonableActionVo medicalUnreasonableActionVo,
                                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                      HttpServletRequest req) throws Exception {
        SolrQuery solrQuery = commonSolrQuery(medicalUnreasonableActionVo, req);

        Page<MedicalUnreasonableActionVo> page = new Page<>(pageNo, pageSize);
        IPage<MedicalUnreasonableActionVo> pageList = SolrQueryGenerator.page(page, MedicalUnreasonableActionVo.class,
                solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, FIELD_MAPPING);
        return Result.ok(pageList);
    }

    private SolrQuery commonSolrQuery(MedicalUnreasonableActionVo medicalUnreasonableActionVo, HttpServletRequest req) {
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(medicalUnreasonableActionVo, req.getParameterMap());
        solrQuery.addFilterQuery("FIR_REVIEW_STATUS:blank OR FIR_REVIEW_STATUS:grey");
        solrQuery.addFilterQuery("PUSH_STATUS:1");//灰名单或者黑名单并且为推送状态的才显示
        solrQuery.setSort("VISITDATE", SolrQuery.ORDER.desc);
        return solrQuery;
    }


    /**
     * 保存不合理行为确认结果（客户审查）
     *
     * @param obj
     * @return
     * @throws
     * @throws SolrServerException
     */
    @AutoLog(value = "不合理行为就诊记录审查表-保存客户确认结果")
    @ApiOperation(value = "不合理行为就诊记录审查表-保存客户确认结果", notes = "不合理行为就诊记录审查表-保存客户确认结果")
    @PutMapping(value = "/saveCustomReview")
    public Result<?> saveCustomReview(@RequestBody JSONObject obj) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        service.saveCustomReview(obj, user);
        return Result.ok("保存成功!");
    }

    /**
     * 通过visitid查询就诊信息DWB_MASTER_INFO
     *
     * @param visitid
     * @return
     * @throws Exception
     */
    @AutoLog(value = "就诊信息- 通过visitid查询")
    @ApiOperation(value = "就诊信息- 通过visitid查询", notes = "就诊信息-通过visitid查询")
    @GetMapping(value = "/queryDwbMasterInfoByVisitid")
    public Result<?> queryDwbMasterInfoByVisitid(@RequestParam(name = "visitid", required = true) String visitid) throws Exception {
        DwbMasterInfoVo bean = service.getDwbMasterInfoByVisitidBySolr(visitid);
        return Result.ok(bean);
    }

    /**
     * 通过visitid查询就诊信息DWB_MASTER_INFO
     *
     * @param visitid
     * @return
     * @throws Exception
     */
    @AutoLog(value = "就诊信息- 通过visitid查询相同患者的就诊记录")
    @ApiOperation(value = "就诊信息- 通过visitid查询相同患者的就诊记录", notes = "就诊信息-通过visitid查询相同患者的就诊记录")
    @GetMapping(value = "/queryClientDwbMasterInfoByVisitid")
    public Result<?> queryClientDwbMasterInfoByVisitid(@RequestParam(name = "visitid", required = true) String visitid) throws Exception {
        SolrQuery solrQuery = new SolrQuery("*:*");
        SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "CLIENTID", "CLIENTID");
        solrQuery.addFilterQuery(plugin.parse() + "VISITID:\"" + visitid +"\"");
        solrQuery.setSort("VISITDATE", SolrQuery.ORDER.asc);
        List<DwbMasterInfoVo> list = SolrQueryGenerator.list(EngineUtil.DWB_MASTER_INFO, solrQuery, DwbMasterInfoVo.class, MASTER_FIELD_MAPPING);
        IPage<DwbMasterInfoVo> pageList = new Page<>();
        pageList.setRecords(list);

        return Result.ok(pageList);
    }

    @AutoLog(value = "就诊信息- 通过visitid查询相同患者的就诊记录")
    @ApiOperation(value = "就诊信息- 通过visitid查询相同患者的就诊记录", notes = "就诊信息-通过visitid查询相同患者的就诊记录")
    @RequestMapping(value = "/exportClientMasterInfoByVisitid")
    public void exportClientMasterInfoByVisitid(@RequestParam(name = "visitid") String visitidParam, HttpServletResponse response) throws Exception {
        WritableWorkbook wwb = null;
        try {
            wwb = Workbook.createWorkbook(response.getOutputStream());
            WritableSheet sheet = wwb.createSheet("历史就诊记录", 0);
            service.exportClientMasterInfo(visitidParam, sheet);
        } catch (Exception e) {
            throw e;
        } finally {
            if (wwb != null) {
                wwb.write();
                wwb.close();
            }

        }


    }

    @AutoLog(value = "就诊信息-通过条件查询医嘱明细")
    @ApiOperation(value = "就诊信息- 通过条件查询医嘱明细", notes = "就诊信息-通过条件查询医嘱明细")
    @GetMapping(value = "/queryDwbOrderByVisitid")
    public Result<?> queryDwbOrderByVisitid(
            DwbOrderVo dwbOrderVo,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "99999999") Integer pageSize,
            HttpServletRequest req) throws Exception {
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(dwbOrderVo, req.getParameterMap());
        Page<DwbOrderVo> page = new Page<>(pageNo, pageSize);
        IPage<DwbOrderVo> pageList = SolrQueryGenerator.page(page, DwbOrderVo.class,
                solrQuery, EngineUtil.DWB_ORDER, ORDER_FIELD_MAPPING);
        return Result.ok(pageList);
    }

    /**
     * 通过clientid查询病人信息DWB_CLIENT
     *
     * @param clientid
     * @return
     * @throws Exception
     */
    @AutoLog(value = "就诊信息- 通过clientid查询")
    @ApiOperation(value = "就诊信息- 通过clientid查询", notes = "就诊信息-通过clientid查询")
    @GetMapping(value = "/queryDwbClientByClientid")
    public Result<?> queryDwbClientByClientid(@RequestParam(name = "clientid", required = true) String clientid) throws Exception {
        DwbClientVo bean = service.getDwbClientByClientidBySolr(clientid);
        return Result.ok(bean);
    }

    /**
     * 通过orgid查询医院信息DWB_ORGANIZATION
     *
     * @param clientid
     * @return
     * @throws Exception
     */
    @AutoLog(value = "医院信息- 通过orgid查询")
    @ApiOperation(value = "医院信息- 通过orgid查询", notes = "医院信息-通过orgid查询")
    @GetMapping(value = "/queryDwbOrganizationByOrgid")
    public Result<?> queryDwbOrganizationByOrgid(@RequestParam(name = "orgid", required = true) String orgid) throws Exception {
        DwbOrganizationVo bean = service.getDwbOrganizationByOrgidBySolr(orgid);
        return Result.ok(bean);
    }

    /**
     * 通过doctorid查询医院信息DWB_DOCTOR
     *
     * @param doctorid
     * @return
     * @throws Exception
     */
    @AutoLog(value = "医生信息- 通过doctorid查询")
    @ApiOperation(value = "医生信息- 通过doctorid查询", notes = "医生信息-通过doctorid查询")
    @GetMapping(value = "/queryDwbDoctorByDoctorid")
    public Result<?> queryDwbDoctorByDoctorid(@RequestParam(name = "doctorid", required = true) String doctorid) throws Exception {
        DwbDoctorVo bean = service.getDwbDoctorByDoctoridBySolr(doctorid);
        return Result.ok(bean);
    }

    /**
     * 收费明细-分页列表查询
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = " 收费明细-分页列表查询")
    @ApiOperation(value = "收费明细-分页列表查询", notes = "收费明细-分页列表查询")
    @GetMapping(value = "/dwbChargeDetailList")
    public Result<?> queryDwbChargeDetailPageList(
            DwbChargeDetailVo dwbChargeDetailVo,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) throws Exception {

        SolrQuery solrQuery = SolrQueryGenerator.initQuery(dwbChargeDetailVo, req.getParameterMap());
        Page<DwbChargeDetailVo> page = new Page<>(pageNo, pageSize);
        IPage<DwbChargeDetailVo> pageList = SolrQueryGenerator.page(page, DwbChargeDetailVo.class,
                solrQuery, EngineUtil.DWB_CHARGE_DETAIL, CHARGE_FIELD_MAPPING);
        return Result.ok(pageList);
    }

    /**
     * 新增不合规行为（手工录入）
     *
     * @param obj
     * @return
     * @throws
     * @throws SolrServerException
     */
   /* @AutoLog(value = "新增不合规行为-手工录入")
    @ApiOperation(value = "新增不合规行为-手工录入", notes = "新增不合规行为-手工录入")
    @PutMapping(value = "/saveMedicalUnreasonableAction")
    public Result<?> saveMedicalUnreasonableAction(@RequestBody MedicalUnreasonableActionVo bean) throws Exception {
        service.saveMedicalUnreasonableAction(bean);
        return Result.ok("保存成功!");
    }*/


    /**
     * 疾病诊断-列表查询
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = " 疾病诊断-列表查询")
    @ApiOperation(value = "疾病诊断-列表查询", notes = "疾病诊断-列表查询")
    @GetMapping(value = "/dwbDiagList")
    public Result<?> queryDwbDiagList(DwbDiagVo dwbDiagVo, HttpServletRequest req) throws Exception {

        SolrQuery solrQuery = SolrQueryGenerator.initQuery(dwbDiagVo,req.getParameterMap());
        Page<DwbDiagVo> page = new Page<>(1, 2000);
        IPage<DwbDiagVo> pageList = SolrQueryGenerator.page(page, DwbDiagVo.class, solrQuery, EngineUtil.DWB_DIAG, DIAG_FIELD_MAPPING);

        return Result.ok(pageList);
    }

    @AutoLog(value = " 疾病诊断-列表查询")
    @ApiOperation(value = "疾病诊断-列表查询", notes = "疾病诊断-列表查询")
    @GetMapping(value = "/queryDwbSettlementByVisitid")
    public Result<?> queryDwbSettlementByVisitid(@RequestParam(name = "visitid") String visitid) throws Exception {

        SolrDocument document = SolrQueryGenerator.getOne(EngineUtil.DWB_SETTLEMENT
                , new String[]{"VISITID:" + visitid}
                , new String[]{"UNDER_INITIAL_LINE_FEE", "FUND_PROP", "INDIV_ACCT_PAY", "FUNDPAY"}
                );
        return Result.ok(document);
    }

    @AutoLog(value = " 疾病诊断-列表查询")
    @ApiOperation(value = "疾病诊断-列表查询", notes = "疾病诊断-列表查询")
    @GetMapping(value = "/queryRelaChargeDetail")
    public Result<?> queryRelaChargeDetail(
            DwbChargeDetailVo dwbChargeDetailVo,
            @RequestParam(name = "batchId") String batchId,
            @RequestParam(name = "visitid") String visitid,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) throws Exception {

        Page<DwbChargeDetailVo> page = new Page<DwbChargeDetailVo>(pageNo, pageSize);
        IPage<DwbChargeDetailVo> pageList;
        String[] unreasonableFqs = {"BATCH_ID:" + batchId, "VISITID:" + visitid, "BUSI_TYPE:CASE"};
        String[] unreasonableFls = {"CASE_ID"};
        SolrDocumentList solrDocuments = SolrQueryGenerator.list(EngineUtil.MEDICAL_UNREASONABLE_ACTION, unreasonableFqs, unreasonableFls);
        if (solrDocuments.size() > 0) {
            String[] caseIds = solrDocuments.stream().map(doc -> String.valueOf(doc.getFieldValue("CASE_ID"))).distinct().toArray(String[]::new);
            //List<MedicalFormalCaseItemRelaVO> relaList = medicalFormalCaseItemRelaService.listVoByBatchIdAndCaseIds(batchId, caseIds);
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("batchId", batchId);
            paramMap.put("caseIds", StringUtils.join(caseIds, ","));
            List<MedicalFormalCaseItemRelaVO> relaList = ApiTokenUtil.getArray("/formal/medicalFormalCaseItemRela/listVoByBatchIdAndCaseIds", paramMap, MedicalFormalCaseItemRelaVO.class);
            if(relaList.size() > 0){
                Set<String> drugGroupSet = new HashSet<>();
                Set<String> projectGroupSet = new HashSet<>();
                Set<String> itemSet = new HashSet<>();
                for (MedicalFormalCaseItemRelaVO bean : relaList) {
                    Set<String> itemIds = new HashSet<>(Arrays.asList(bean.getItemIds().split(",")));
                    String type = bean.getType();
                    if ("drugGroup".equals(type)) {
                        drugGroupSet.addAll(itemIds);
                    } else if ("projectGroup".equals(type)) {
                        projectGroupSet.addAll(itemIds);
                    } else {
                        itemSet.addAll(itemIds);
                    }
                }
                List<String> itemFqList = new ArrayList<>();
                if (drugGroupSet.size() > 0) {
                	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
                    String fq = "_query_:\"" + plugin.parse() + "DRUGGROUP_CODE:(" + StringUtils.join(drugGroupSet, " OR ") + ")\"";
                    itemFqList.add(fq);
                }
                if (projectGroupSet.size() > 0) {
                	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
                    String fq = "_query_:\"" + plugin.parse() + "TREATGROUP_CODE:(" + StringUtils.join(projectGroupSet, " OR ") + ")\"";
                    itemFqList.add(fq);
                }
                if (itemSet.size() > 0) {
                    String fq = "ITEMCODE:(" + StringUtils.join(itemSet, " OR ") + ")";
                    itemFqList.add(fq);
                }
                SolrQuery solrQuery = SolrQueryGenerator.initQuery(dwbChargeDetailVo, req.getParameterMap());
                solrQuery.addFilterQuery(StringUtils.join(itemFqList, " OR "));
                pageList = SolrQueryGenerator.page(page, DwbChargeDetailVo.class,
                        solrQuery, EngineUtil.DWB_CHARGE_DETAIL, CHARGE_FIELD_MAPPING);
            } else {
                pageList = this.setPageBlank(page);
            }
        } else {
            pageList = this.setPageBlank(page);
        }
        return Result.ok(pageList);
    }

    private <T> IPage<T> setPageBlank(Page<T> page){
        page.setTotal(0);
        page.setRecords(new ArrayList<>());
        return page;
    }

    /**
     * 通过visitid查询入院记录
     *
     * @param visitid
     * @return
     * @throws Exception
     */
    @AutoLog(value = "入院记录- 通过visitid查询")
    @ApiOperation(value = "入院记录- 通过visitid查询", notes = "入院记录-通过visitid查询")
    @GetMapping(value = "/queryDwbAdmmisionByVisitid")
    public Result<?> queryDwbAdmmisionByVisitid(@RequestParam(name = "visitid", required = true) String visitid) throws Exception {
        DwbAdmmisionVo bean = service.getDwbAdmmisionByVisitidBySolr(visitid);
        return Result.ok(bean);
    }

    /**
     * 通过visitid查询入院记录
     *
     * @param visitid
     * @return
     * @throws Exception
     */
    @AutoLog(value = "出院记录- 通过visitid查询")
    @ApiOperation(value = "出院记录- 通过visitid查询", notes = "出院记录-通过visitid查询")
    @GetMapping(value = "/queryDwbDischargeByVisitid")
    public Result<?> queryDwbDischargeByVisitid(@RequestParam(name = "visitid", required = true) String visitid) throws Exception {
        DwbDischargeVo bean = service.getDwbDischargeByVisitidBySolr(visitid);
        return Result.ok(bean);
    }


}
