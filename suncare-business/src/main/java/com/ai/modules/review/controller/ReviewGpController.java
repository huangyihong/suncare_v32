package com.ai.modules.review.controller;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.formal.vo.MedicalFormalCaseItemRelaVO;
import com.ai.modules.review.entity.MedicalUnreasonableAction;
import com.ai.modules.review.service.IDwbService;
import com.ai.modules.review.service.IMedicalUnreasonableActionService;
import com.ai.modules.review.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Slf4j
@Api(tags = "模型结果总览")
@RestController
@RequestMapping("/gp/review")
public class ReviewGpController {
    @Autowired
    private IDwbService dwbService;
    @Autowired
    private IMedicalUnreasonableActionService medicalUnreasonableActionService;
    /**
     * 疾病诊断-列表查询
     *
     * @param req
     * @return
     */
    @AutoLog(value = " 疾病诊断-列表查询")
    @ApiOperation(value = "疾病诊断-列表查询", notes = "疾病诊断-列表查询")
    @GetMapping(value = "/dwbDiagList")
    public Result<?> queryDwbDiagList(DwbDiagVo dwbDiagVo, HttpServletRequest req) throws Exception {
        QueryWrapper<DwbDiagVo> queryWrapper = QueryGenerator.initQueryWrapper(dwbDiagVo, req.getParameterMap());
        Page<DwbDiagVo> page = new Page<>(1, 2000);
        IPage<DwbDiagVo> pageList = dwbService.dwbDiagList(page,queryWrapper);
        return Result.ok(pageList);
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
        DwbMasterInfoVo bean = new DwbMasterInfoVo();
        QueryWrapper<DwbMasterInfoVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("VISITID",visitid);
        Page<DwbMasterInfoVo> page = new Page<>(1, 10);
        IPage<DwbMasterInfoVo> pageList = dwbService.dwbMasterInfoList(page,queryWrapper);
        if(pageList.getRecords().size()>0){
            bean = pageList.getRecords().get(0);
        }
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
        QueryWrapper<DwbChargeDetailVo> queryWrapper = QueryGenerator.initQueryWrapper(dwbChargeDetailVo, req.getParameterMap());
        Page<DwbChargeDetailVo> page = new Page<>(pageNo, pageSize);
        IPage<DwbChargeDetailVo> pageList = dwbService.dwbChargeDetailList(page,queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 通过clientid查询病人信息DWB_CLIENT
     *
     * @param clientid
     * @return
     * @throws Exception
     */
    @AutoLog(value = "病人信息- 通过clientid查询")
    @ApiOperation(value = "病人信息- 通过clientid查询", notes = "病人信息-通过clientid查询")
    @GetMapping(value = "/queryDwbClientByClientid")
    public Result<?> queryDwbClientByClientid(@RequestParam(name = "clientid", required = true) String clientid) throws Exception {
        DwbClientVo bean = new DwbClientVo();
        QueryWrapper<DwbClientVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("CLIENTID",clientid);
        Page<DwbClientVo> page = new Page<>(1, 10);
        IPage<DwbClientVo> pageList = dwbService.dwbClientList(page,queryWrapper);
        if(pageList.getRecords().size()>0){
            bean = pageList.getRecords().get(0);
        }
        return Result.ok(bean);
    }

    /**
     * 通过orgid查询医院信息DWB_ORGANIZATION
     *
     * @param orgid
     * @return
     * @throws Exception
     */
    @AutoLog(value = "医院信息- 通过orgid查询")
    @ApiOperation(value = "医院信息- 通过orgid查询", notes = "医院信息-通过orgid查询")
    @GetMapping(value = "/queryDwbOrganizationByOrgid")
    public Result<?> queryDwbOrganizationByOrgid(@RequestParam(name = "orgid", required = true) String orgid) throws Exception {
        DwbOrganizationVo bean = new DwbOrganizationVo();
        QueryWrapper<DwbOrganizationVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ORGID",orgid);
        Page<DwbOrganizationVo> page = new Page<>(1, 10);
        IPage<DwbOrganizationVo> pageList = dwbService.dwbOrganizationList(page,queryWrapper);
        if(pageList.getRecords().size()>0){
            bean = pageList.getRecords().get(0);
        }
        return Result.ok(bean);
    }

    @AutoLog(value = "医嘱明细-通过条件查询医嘱明细")
    @ApiOperation(value = "医嘱明细- 通过条件查询医嘱明细", notes = "医嘱明细-通过条件查询医嘱明细")
    @GetMapping(value = "/queryDwbOrderByVisitid")
    public Result<?> queryDwbOrderByVisitid(
            DwbOrderVo dwbOrderVo,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "99999999") Integer pageSize,
            HttpServletRequest req) throws Exception {
        QueryWrapper<DwbOrderVo> queryWrapper = QueryGenerator.initQueryWrapper(dwbOrderVo, req.getParameterMap());
        Page<DwbOrderVo> page = new Page<>(pageNo, pageSize);
        IPage<DwbOrderVo> pageList = dwbService.dwbOrderList(page,queryWrapper);
        return Result.ok(pageList);
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
        QueryWrapper<MedicalUnreasonableAction> queryWrapper  = new QueryWrapper<>();
        queryWrapper.eq("BATCH_ID",batchId);
        queryWrapper.eq("VISITID",visitid);
        queryWrapper.eq("BUSI_TYPE","CASE");
        queryWrapper.select("CASE_ID");
        List<MedicalUnreasonableAction> list = medicalUnreasonableActionService.list(queryWrapper);
        if (list.size() > 0) {
            String[] caseIds = list.stream().map(bean -> bean.getCaseId()).distinct().toArray(String[]::new);
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
                QueryWrapper<DwbChargeDetailVo> wrapper = new QueryWrapper<>();
                wrapper.and(w->{
                    if (drugGroupSet.size() > 0) {
                        String insql = StringUtils.join(drugGroupSet,"','");
                        w.or().inSql("ITEMCODE","select ATC_DRUGCODE from medical_gbdp.STD_DRUGGROUP where DRUGGROUP_CODE in ('"+insql+"')");
                    }
                    if (projectGroupSet.size() > 0) {
                        String insql = StringUtils.join(projectGroupSet,"','");
                        w.or().inSql("ITEMCODE","select TREATCODE from medical_gbdp.STD_DRUGGROUP where TREATGROUP_CODE in ('"+insql+"')");
                    }
                    if (itemSet.size() > 0) {
                        w.or().in("ITEMCODE",itemSet);
                    }
                    return w;
                });

                pageList = dwbService.dwbChargeDetailList(page,wrapper);
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
     * 通过doctorid查询医生信息DWB_DOCTOR
     *
     * @param doctorid
     * @return
     * @throws Exception
     */
    @AutoLog(value = "医生信息- 通过doctorid查询")
    @ApiOperation(value = "医生信息- 通过doctorid查询", notes = "医生信息-通过doctorid查询")
    @GetMapping(value = "/queryDwbDoctorByDoctorid")
    public Result<?> queryDwbDoctorByDoctorid(@RequestParam(name = "doctorid", required = true) String doctorid) throws Exception {
        DwbDoctorVo bean = new DwbDoctorVo();
        QueryWrapper<DwbDoctorVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("DOCTORID",doctorid);
        Page<DwbDoctorVo> page = new Page<>(1, 10);
        IPage<DwbDoctorVo> pageList = dwbService.dwbDoctorList(page,queryWrapper);
        if(pageList.getRecords().size()>0){
            bean = pageList.getRecords().get(0);
        }
        return Result.ok(bean);
    }

    @AutoLog(value = " 疾病诊断-列表查询")
    @ApiOperation(value = "疾病诊断-列表查询", notes = "疾病诊断-列表查询")
    @GetMapping(value = "/queryDwbSettlementByVisitid")
    public Result<?> queryDwbSettlementByVisitid(@RequestParam(name = "visitid") String visitid) throws Exception {
        Map<String,Object> map = dwbService.queryDwbSettlementByVisitid(visitid);
        return Result.ok(map);
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

        QueryWrapper<DwbMasterInfoVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.inSql("CLIENTID","select distinct CLIENTID from medical.DWB_MASTER_INFO where VISITID='"+visitid+"'");
        queryWrapper.orderByAsc("VISITDATE");
        Page<DwbMasterInfoVo> page = new Page<>(1, 999999);
        IPage<DwbMasterInfoVo> pageList = dwbService.dwbMasterInfoList(page,queryWrapper);

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
            dwbService.exportClientMasterInfo(visitidParam, sheet);
        } catch (Exception e) {
            throw e;
        } finally {
            if (wwb != null) {
                wwb.write();
                wwb.close();
            }

        }


    }

}
