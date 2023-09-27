package com.ai.modules.medical.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.StringUtil;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalColumnQuality;
import com.ai.modules.medical.service.IMedicalColumnQualityService;
import com.ai.modules.medical.vo.DwbDataqualitySolrVO;
import com.ai.modules.medical.vo.MedicalColumnQualityExportVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @Description: 规则依赖字段质量表
* @Author: jeecg-boot
* @Date:   2021-03-22
* @Version: V1.0
*/
@Slf4j
@Api(tags="规则依赖字段质量表")
@RestController
@RequestMapping("/api/medical/medicalColumnQuality")
public class ApiColumnQualityController {
    @Autowired
    private IMedicalColumnQualityService medicalColumnQualityService;

    /**
     * 分页列表查询
     *
     * @param dwbDataqualitySolrVO
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "规则依赖字段质量表-分页列表查询")
    @ApiOperation(value="规则依赖字段质量表-分页列表查询", notes="规则依赖字段质量表-分页列表查询")
    @GetMapping(value = "/solrlist")
    public Result<?> querySolrPageList(DwbDataqualitySolrVO dwbDataqualitySolrVO,
                                       @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                       @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                       HttpServletRequest req) throws Exception {
        SolrQuery solrQuery = generateSolrQuery(dwbDataqualitySolrVO, req);
        Page<DwbDataqualitySolrVO> page = new Page<>(pageNo, pageSize);
        IPage<DwbDataqualitySolrVO> pageList = SolrQueryGenerator.page(page, DwbDataqualitySolrVO.class,
                solrQuery, "DWB_DATAQUALITY_SOLR", SolrUtil.initFieldMap(DwbDataqualitySolrVO.class));
        return Result.ok(pageList);
    }

    private SolrQuery generateSolrQuery(DwbDataqualitySolrVO dwbDataqualitySolrVO, HttpServletRequest req) {
        String orgid = dwbDataqualitySolrVO.getOrgid();
        String actionNames = dwbDataqualitySolrVO.getActionNames();
        String hasResult = dwbDataqualitySolrVO.getHasResult();
        String etlSource = dwbDataqualitySolrVO.getEtlSource();
        if(StringUtils.isNotBlank(orgid)){
            dwbDataqualitySolrVO.setOrgid(null);
        }
        if(StringUtils.isNotBlank(actionNames)){
            dwbDataqualitySolrVO.setActionNames(null);
        }
        if(StringUtils.isNotBlank(etlSource)){
            dwbDataqualitySolrVO.setEtlSource(null);
        }
        dwbDataqualitySolrVO.setHasResult(null);
        if(StringUtils.isNotBlank(dwbDataqualitySolrVO.getTableName())){
            dwbDataqualitySolrVO.setTableName(dwbDataqualitySolrVO.getTableName().toUpperCase());
        }
        if(StringUtils.isNotBlank(dwbDataqualitySolrVO.getColumnName())){
            dwbDataqualitySolrVO.setColumnName(dwbDataqualitySolrVO.getColumnName().toUpperCase());
        }
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(dwbDataqualitySolrVO, req.getParameterMap());
        if(StringUtils.isNotBlank(orgid)){
            solrQuery.addFilterQuery("ORGID:(\"" + orgid.replace(",","\",\"") + "\")");
        }
        if(StringUtils.isNotBlank(actionNames)){
            solrQuery.addFilterQuery("ACTION_NAMES:(*" + actionNames.replace(",","* OR *") + "*)");
        }
        if(StringUtils.isNotBlank(etlSource)){
            solrQuery.addFilterQuery("ETL_SOURCE:(\"" + etlSource.replace(",","\",\"") + "\")");
        }
        if(StringUtils.isNotBlank(hasResult)){
            if("1".equals(hasResult)){
                solrQuery.addFilterQuery("RESULT:*");
            }else{
                solrQuery.addFilterQuery("-RESULT:*");
            }
        }
        solrQuery.addField("HAS_RESULT:if(eq(RESULT,0.0),'是',if(RESULT,'是','否'))");
        solrQuery.addField("ETL_SOURCE_NAME:if(eq(ETL_SOURCE,'A01'),'医保',if(eq(ETL_SOURCE,'A02'),'农合',if(eq(ETL_SOURCE,'A03'),'HIS',if(eq(ETL_SOURCE,'A04'),'药店',''))))");
        return solrQuery;
    }

    /**
     * 分页列表查询
     *
     * @param medicalColumnQuality
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "规则依赖字段质量表-分页列表查询")
    @ApiOperation(value="规则依赖字段质量表-分页列表查询", notes="规则依赖字段质量表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(MedicalColumnQuality medicalColumnQuality,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) throws Exception {
        String fq = getProjectFq(req);

        //是否有质控结果:获取solr表DWB_DATAQUALITY 表名+字段名
        List<String> tableColumn = querySolrTableColumn(medicalColumnQuality, req, fq);

        //查询参数
        Map<String, String> map = new HashMap<>();
        for(Map.Entry<String, String[]> entry: req.getParameterMap().entrySet()){
            String[] values = entry.getValue();
            if(values != null && values.length > 0){
                map.put(entry.getKey(), entry.getValue()[0]);
            }
        }

        if(tableColumn.size()==0&&"1".equals(req.getParameter("hasResult"))){
            IPage<MedicalColumnQuality> pageList = new Page<MedicalColumnQuality>(pageNo, pageSize);
            pageList.setTotal(0);
            pageList.setRecords(new ArrayList<>());
            return Result.ok(pageList);
        }
        if(tableColumn.size()>0){
            map.put("tableColumn", String.join(",",tableColumn));
        }

        IPage<MedicalColumnQuality> pageList = ApiTokenUtil.Page("/medical/medicalColumnQuality/list", map, MedicalColumnQuality.class);
        if(pageList.getRecords().size()==0){
            return Result.ok(pageList);
        }
        //关联solr表DWB_DATAQUALITY 查询数据完整性
        Map<String, Double> dataqualityMap = new HashMap<>();
        if(!"0".equals(req.getParameter("hasResult"))){
            dataqualityMap = querySolrResult(fq, pageList.getRecords());
        }
        //返回结果
        IPage<MedicalColumnQualityExportVO> pageListVO = new Page<MedicalColumnQualityExportVO>(pageNo, pageSize);
        pageListVO.setTotal(pageList.getTotal());
        List<MedicalColumnQualityExportVO> listVO = setColumnQualityResult(pageList.getRecords(), dataqualityMap,new String[]{"1","0"});
        pageListVO.setRecords(listVO);
        return Result.ok(pageListVO);
    }

    private List<MedicalColumnQualityExportVO> setColumnQualityResult(List<MedicalColumnQuality> list, Map<String, Double> dataqualityMap,String[] hasResult) {
        List<MedicalColumnQualityExportVO> listVO = new ArrayList<>();
        for(MedicalColumnQuality bean:list){
            MedicalColumnQualityExportVO vo = new MedicalColumnQualityExportVO();
            BeanUtil.copyProperties(bean, vo);
            if(dataqualityMap.get(bean.getTableName()+"&"+bean.getColumnName().toUpperCase())!=null){
                vo.setHasResult(hasResult[0]);
                vo.setResult(dataqualityMap.get(bean.getTableName()+"&"+bean.getColumnName().toUpperCase()));
            }else{
                vo.setHasResult(hasResult[1]);
            }
            listVO.add(vo);
        }
        return listVO;
    }


    private String getProjectFq(HttpServletRequest req) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String project = user.getDataSource();
        String fq = "RULETYPE_ID:04 AND PROJECT:"+project;
        String etlSource = req.getParameter("etlSource");
        if(StringUtils.isNotBlank(etlSource)){
            fq +=" AND ETL_SOURCE:"+etlSource;
        }
        String orgid = req.getParameter("orgid");
        if(StringUtils.isNotBlank(orgid)){
            fq +=" AND ORGID:(\"" + orgid.replace(",","\",\"") + "\")";
        }else{
            fq +=" AND ORGID:ALL";
        }
        return fq;
    }

    private Map<String, Double> querySolrResult(String fq, List<MedicalColumnQuality> data) throws Exception {
        //关联solr表DWB_DATAQUALITY 查询数据完整性
        Map<String, Double> dataqualityMap = new HashMap<>();
        List<String> queryStrList = new ArrayList<>();
        for(MedicalColumnQuality bean:data){
            queryStrList.add("(TABLENAME:"+bean.getTableName()+" AND (COLUMNNAME:"+bean.getColumnName().toUpperCase()+" OR COLUMNNAME:"+bean.getColumnName().toLowerCase()+"))");
        }
        SolrQuery solrQuery1 = new SolrQuery("*:*");
        solrQuery1.addFilterQuery(fq);
        solrQuery1.addFilterQuery(StringUtil.join(queryStrList, " OR "));
        solrQuery1.setFields("TABLENAME,COLUMNNAME,RESULT");
        solrQuery1.setRows(data.size());
        SolrDocumentList dataqualityList = SolrUtil.call(solrQuery1, "DWB_DATAQUALITY").getResults();
        for (SolrDocument solrDoc : dataqualityList) {
            dataqualityMap.put(solrDoc.getFieldValue("TABLENAME").toString()+"&"+solrDoc.getFieldValue("COLUMNNAME").toString().toUpperCase(), Double.parseDouble(solrDoc.getFieldValue("RESULT").toString()));
        }
        return dataqualityMap;
    }

    private List<String> querySolrTableColumn(MedicalColumnQuality medicalColumnQuality, HttpServletRequest req, String fq) throws Exception {
        List<String> tableColumn = new ArrayList<>();
        String hasResult = req.getParameter("hasResult");
        if(StringUtils.isNotBlank(hasResult)){
            List<String> queryStrList = new ArrayList<>();
            queryStrList.add(fq);
            if(StringUtils.isNotBlank(medicalColumnQuality.getTableName())){
                queryStrList.add("TABLENAME:"+medicalColumnQuality.getTableName());
            }
            if(StringUtils.isNotBlank(medicalColumnQuality.getColumnName())){
                queryStrList.add("COLUMNNAME:"+medicalColumnQuality.getColumnName().toUpperCase()+" OR COLUMNNAME:"+medicalColumnQuality.getColumnName().toLowerCase());
            }
            String[] fqs = queryStrList.toArray(new String[0]);
            String[] fls = {"TABLENAME","COLUMNNAME"};
            SolrDocumentList dataqualityList = SolrQueryGenerator.list("DWB_DATAQUALITY", fqs, fls);
            for (SolrDocument solrDoc : dataqualityList) {
                tableColumn.add(solrDoc.getFieldValue("TABLENAME").toString()+"."+solrDoc.getFieldValue("COLUMNNAME").toString().toUpperCase());
            }
        }
        return tableColumn;
    }

    /**
     * 质控质量表中的机构查询
     * @return
     */
    @AutoLog(value = "质控质量表中的机构查询")
    @ApiOperation(value = "质控质量表中的机构查询", notes = "质控质量表中的机构查询")
    @GetMapping(value = "/queryDwbDataqualityOrg")
    public Result<?> queryDwbDataqualityOrg() {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String project = user.getDataSource();
        String fq = "RULETYPE_ID:04 AND PROJECT:"+project;

        JSONObject facetChild = new JSONObject();
        facetChild.put("ORGNAME", "max(ORGNAME)");
        JSONObject termFacet = new JSONObject();
        termFacet.put("type", "terms");
        termFacet.put("field", "ORGID");
        termFacet.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        termFacet.put("overrequest", Integer.MAX_VALUE);
        termFacet.put("facet", facetChild);

        List<Map<String,String>> list = new ArrayList<>();
        try {
            SolrUtil.jsonFacet("DWB_DATAQUALITY_SOLR", new String[]{fq}, termFacet.toJSONString(), (json) -> {
                String code = json.getString("val");
                String name = json.getString("ORGNAME");
                if(!"ALL".equals(code)&&StringUtils.isNotBlank(code)){
                    Map<String,String> bean = new HashMap<>();
                    bean.put("code",code);
                    bean.put("name",name);
                    list.add(bean);
                }
            });
        } catch (Exception e){
            log.info("质控质量表中的机构查询-获取数据失败:" +  e.getMessage() );
        }
        return Result.ok(list);
    }

    /**
     * 直接导出excel
     *
     * @param req
     * @param response
     * @param medicalColumnQuality
     * @throws Exception
     */
    @RequestMapping(value = "/exportExcelNew")
    public void exportExcel(HttpServletRequest req, HttpServletResponse response, MedicalColumnQuality medicalColumnQuality) throws Exception {
        String title = req.getParameter("title");
        if (StringUtils.isBlank(title)) {
            title = "项目数据质量验证_导出";
        }
        //response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes("UTF-8"), "iso-8859-1"));
        try {
            OutputStream os = response.getOutputStream();
            String fq = getProjectFq(req);

            //是否有质控结果:获取solr表DWB_DATAQUALITY 表名+字段名
            List<String> tableColumn = querySolrTableColumn(medicalColumnQuality, req, fq);


            Map<String, String> map = new HashMap<>();
            for(Map.Entry<String, String[]> entry: req.getParameterMap().entrySet()){
                String[] values = entry.getValue();
                if(values != null && values.length > 0){
                    map.put(entry.getKey(), entry.getValue()[0]);
                }
            }
            if(tableColumn.size()>0){
                map.put("tableColumn", String.join(",",tableColumn));
            }

            List<MedicalColumnQualityExportVO> listVO = new ArrayList<>();
            if(!(tableColumn.size()==0&&"1".equals(req.getParameter("hasResult")))){
                List<MedicalColumnQuality> list = ApiTokenUtil.getArray("/medical/medicalColumnQuality/queryList", map, MedicalColumnQuality.class);
                //关联solr表DWB_DATAQUALITY 查询数据完整性
                Map<String, Double> dataqualityMap = new HashMap<>();
                if(list.size()>0&&!"0".equals(req.getParameter("hasResult"))){
                    dataqualityMap = querySolrResult(fq, list);
                }
                listVO = setColumnQualityResult(list, dataqualityMap,new String[]{"是","否"});
            }
            String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
            medicalColumnQualityService.exportExcel(listVO, os, suffix);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 直接导出excel
     *
     * @param req
     * @param response
     * @param dwbDataqualitySolrVO
     * @throws Exception
     */
    @RequestMapping(value = "/exportExcelSolr")
    public void exportExcelSolr(HttpServletRequest req, HttpServletResponse response, DwbDataqualitySolrVO dwbDataqualitySolrVO) throws Exception {
        String title = req.getParameter("title");
        if (StringUtils.isBlank(title)) {
            title = "项目数据质量验证_导出";
        }
        //response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((title + ".xls").getBytes("UTF-8"), "iso-8859-1"));
        try {
            OutputStream os = response.getOutputStream();

            SolrQuery solrQuery = generateSolrQuery(dwbDataqualitySolrVO, req);

            List<DwbDataqualitySolrVO> listVO = SolrQueryGenerator.list("DWB_DATAQUALITY_SOLR",solrQuery,DwbDataqualitySolrVO.class,SolrUtil.initFieldMap(DwbDataqualitySolrVO.class));
            String suffix = ExcelTool.OFFICE_EXCEL_2010_POSTFIX;//导出文件的后缀xlsx、xls
            medicalColumnQualityService.exportExcelSolr(listVO, os, suffix);
        } catch (Exception e) {
            throw e;
        }
    }

}
