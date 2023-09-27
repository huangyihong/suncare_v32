/**
 * EngineServiceImpl.java	  V1.0   2019年11月29日 上午11:06:14
 * <p>
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 * <p>
 * Modification history(By    Time    Reason):
 * <p>
 * Description:
 */

package com.ai.modules.review.service.impl;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ExportUtils;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.review.mapper.DwbMapper;
import com.ai.modules.review.service.IDwbService;
import com.ai.modules.review.vo.*;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jxl.write.WritableSheet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@DS("greenplum")
public class DwbServiceImpl implements IDwbService {


    @Autowired
    private DwbMapper dwbMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public IPage<DwbDiagVo> dwbDiagList(Page<DwbDiagVo> page, Wrapper<DwbDiagVo> wrapper) {
        return this.dwbMapper.dwbDiagList(page,wrapper);
    }

    @Override
    public IPage<DwbMasterInfoVo> dwbMasterInfoList(Page<DwbMasterInfoVo> page, Wrapper<DwbMasterInfoVo> wrapper) {
        return this.dwbMapper.dwbMasterInfoList(page,wrapper);
    }

    @Override
    public IPage<DwbChargeDetailVo> dwbChargeDetailList(Page<DwbChargeDetailVo> page, Wrapper<DwbChargeDetailVo> wrapper) {
        return this.dwbMapper.dwbChargeDetailList(page,wrapper);
    }

    @Override
    public IPage<DwbClientVo> dwbClientList(Page<DwbClientVo> page, Wrapper<DwbClientVo> wrapper) {
        return this.dwbMapper.dwbClientList(page,wrapper);
    }

    @Override
    public IPage<DwbOrganizationVo> dwbOrganizationList(Page<DwbOrganizationVo> page, Wrapper<DwbOrganizationVo> wrapper) {
        return this.dwbMapper.dwbOrganizationList(page,wrapper);
    }

    @Override
    public IPage<DwbOrderVo> dwbOrderList(Page<DwbOrderVo> page, Wrapper<DwbOrderVo> wrapper) {
        return this.dwbMapper.dwbOrderList(page,wrapper);
    }

    @Override
    public IPage<DwbDoctorVo> dwbDoctorList(Page<DwbDoctorVo> page, Wrapper<DwbDoctorVo> wrapper) {
        return this.dwbMapper.dwbDoctorList(page,wrapper);
    }

    @Override
    public Map<String, Object> queryDwbSettlementByVisitid(String visitid) {
        String sql = "select UNDER_INITIAL_LINE_FEE,FUND_PROP,INDIV_ACCT_PAY,FUNDPAY from medical.dwb_settlement where VISITID=? ";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,visitid);
        if(list.size()>0){
           return list.get(0);
        }
        return null;
    }

    private String[] exportMasterFields1 = {"VISITID", "CLIENTNAME", "SEX", "YEARAGE", "DISEASENAME",
            "ORGNAME", "HOSPLEVEL", "DEPTNAME", "VISITDATE", "LEAVEDATE", "TOTALFEE", "ETL_SOURCE_NAME",};
    private String[] exportMasterFields2 = {"VISITID", "ITEMCLASS", "ITEMNAME", "AMOUNT", "ITEMPRICE", "FEE"};
    private String[] exportMasterFields = {"VISITID", "CLIENTNAME", "SEX", "YEARAGE", "DISEASENAME",
            "ITEMCLASS", "ITEMNAME", "AMOUNT", "ITEMPRICE", "FEE",
            "ORGNAME", "HOSPLEVEL", "DEPTNAME", "VISITDATE", "LEAVEDATE", "TOTALFEE", "ETL_SOURCE_NAME",};
    private String[] exportMasterTitles = {"就诊ID", "姓名", "性别", "年龄", "疾病诊断名称",
            "项目类别", "项目名称", "项目数量", "项目单价", "项目总金额",
            "医疗机构名称", "医疗机构级别", "就诊科室", "就诊时间", "出院时间", "就诊总金额", "ETL来源"
    };

    @Override
    public void exportClientMasterInfo(String visitidParam, WritableSheet sheet) throws Exception {
        String sql = "select "+StringUtils.join(exportMasterFields1,",")+" from medical.dwb_master_info where CLIENTID in (select distinct CLIENTID from medical.DWB_MASTER_INFO where VISITID=? )";
        List<Map<String, Object>> masterList = jdbcTemplate.queryForList(sql,visitidParam);

        List<String> visitids = masterList.stream()
                .sorted(Comparator.comparing(a -> ((String) a.get("VISITID"))))
                .map(a -> (String) a.get("VISITID")).collect(Collectors.toList());

        List<Map<String, Object>> resultList = new ArrayList<>();
        sql = "select "+StringUtils.join(exportMasterFields2,",")+" from medical.dwb_charge_detail where VISITID in ('"+StringUtils.join(visitids,"','")+"') order by VISITID  asc, ITEMCODE asc limit 1000000";
        List<Map<String, Object>> chargeDocuments = jdbcTemplate.queryForList(sql);
        if (chargeDocuments.size() > 0) {
            Map<String, List<Map<String, Object>>> chargeMap = new HashMap<>();
            List<Map<String, Object>> cacheDocuments = new ArrayList<>();
            chargeMap.put((String) chargeDocuments.get(0).get("VISITID"), cacheDocuments);
            // 都是相同排序，比较并归纳
            for (int i = 0, j = 0, jLen = chargeDocuments.size(); ; ) {
                Map<String, Object> chargeDoc = chargeDocuments.get(j);
                String chargeVisitid = (String) chargeDoc.get("VISITID");
                String visitid = visitids.get(i);
                if (!visitid.equals(chargeVisitid)) {
                    ++i;
                    chargeMap.put(visitids.get(i), cacheDocuments = new ArrayList<>());
                } else {
                    cacheDocuments.add(chargeDoc);
                    if (++j == jLen) {
                        break;
                    }
                }
            }
            // 一对多构造输出列表
            for (Map<String, Object> document : masterList) {
                Map<String, Object> cacheMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : document.entrySet()) {
                    cacheMap.put(entry.getKey().toUpperCase(), entry.getValue());
                }
                String visitid = (String) document.get("VISITID");
                log.info(visitid + ":" + (String) document.get("DISEASENAME"));

                List<Map<String, Object>> documentList = chargeMap.get(visitid);
                if (documentList != null && documentList.size() > 0) {
                    for (Map<String, Object> chargeDoc : documentList) {
                        Map<String, Object> map = new HashMap<>(cacheMap);
                        for (Map.Entry<String, Object> entry : chargeDoc.entrySet()) {
                            map.put(entry.getKey().toUpperCase(), entry.getValue());
                        }
                        resultList.add(map);
                    }
                } else {
                    log.info("为空：" + document.get("DISEASENAME"));
                }

            }
        }

        ExportUtils.exportExl(resultList, exportMasterTitles, exportMasterFields, sheet, sheet.getName());
    }

}
