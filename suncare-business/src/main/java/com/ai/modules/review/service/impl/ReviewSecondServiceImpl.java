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
import com.ai.common.utils.ExportXUtils;
import com.ai.modules.action.dto.BreakBehaviorCaseExport;
import com.ai.modules.action.dto.BreakBehaviorClientExport;
import com.ai.modules.action.dto.BreakBehaviorDocExport;
import com.ai.modules.action.dto.BreakBehaviorHospExport;
import com.ai.modules.action.entity.MedicalBreakBehaviorResult;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.service.impl.EngineBehaviorServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.mapper.MedicalFormalCaseItemRelaMapper;
import com.ai.modules.formal.vo.MedicalFormalBehaviorVO;
import com.ai.modules.formal.vo.MedicalFormalCaseItemRelaVO;
import com.ai.modules.review.service.IReviewSecondService;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.review.vo.ReviewSecondVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReviewSecondServiceImpl implements IReviewSecondService {

    @Autowired
    MedicalFormalCaseItemRelaMapper medicalFormalCaseItemRelaMapper;

    private static Map<String, String> fStatusDictMap;
    private static Map<String, String> sStatusDictMap;

    static {
        fStatusDictMap = new HashMap<>();
        fStatusDictMap.put("white", "白名单");
        fStatusDictMap.put("blank", "黑名单");
        fStatusDictMap.put("grey", "灰名单");

        sStatusDictMap = new HashMap<>();
        sStatusDictMap.put("00", "待审核");
        sStatusDictMap.put("01", "待客户确认");
        sStatusDictMap.put("02", "审核不通过");
        sStatusDictMap.put("03", "已撤回");
        sStatusDictMap.put("04", "客户已确认");
        sStatusDictMap.put("05", "客户已驳回");
    }

    @Override
    public void exportExcel(SolrQuery[] solrQuerys, String batchId, OutputStream os) throws Exception {
        List<ReviewSecondVo> list = new ArrayList<>();
        for (SolrQuery solrQuery : solrQuerys) {
            solrQuery.setRows(1000000);
            list.addAll(SolrQueryGenerator.list(EngineUtil.DWB_MASTER_INFO, solrQuery, ReviewSecondVo.class
                    , SolrQueryGenerator.REVIEW_SECOND_MAPPING));
        }
        Map<String, String> relaQueryMap = new HashMap<>();

        List<MedicalFormalCaseItemRelaVO> relaList = medicalFormalCaseItemRelaMapper.listVoByBatchId(batchId);
        for (MedicalFormalCaseItemRelaVO rela : relaList) {
            Set<String> itemIds = new HashSet<>(Arrays.asList(rela.getItemIds().split(",")));
            String type = rela.getType();
            String queryStr;
            if ("drugGroup".equals(type)) {
            	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
                queryStr = "_query_:\\\"" + plugin.parse() + "DRUGGROUP_CODE:(\\\\\"" + StringUtils.join(itemIds, "\\\\\",\\\\\"") + "\\\\\")\\\"";
            } else if ("projectGroup".equals(type)) {
            	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
                queryStr = "_query_:\\\"" + plugin.parse() + "TREATGROUP_CODE:(\\\\\"" + StringUtils.join(itemIds, "\\\\\",\\\\\"") + "\\\\\")\\\"";
            } else {
                queryStr = "ITEMCODE:(\\\"" + StringUtils.join(itemIds, "\\\",\\\"") + "\\\")";
            }
            relaQueryMap.put(rela.getCaseId(), queryStr);
        }

        if (list.size() > 0) {

            for (int i = 0, j = 0, len = list.size(); j < len; i += 100) {
                j = i + 100;
                if (j > len) {
                    j = len;
                }
                List<String> facetList = new ArrayList<>();
                Map<String, ReviewSecondVo> map = new HashMap<>();
                for (ReviewSecondVo bean : list.subList(i, j)) {
                    map.put(bean.getVisitid(), bean);
                }
                String visitIdFq = "VISITID:(\"" + StringUtils.join(map.keySet(), "\",\"") + "\")";

                SolrQuery unreasonableQuery = new SolrQuery("*:*");
                unreasonableQuery.addFilterQuery(visitIdFq, "BATCH_ID:" + batchId);
                unreasonableQuery.setFields("id", "VISITID", "FIR_REVIEW_STATUS", "SEC_REVIEW_STATUS", "REVIEW_CASE_IDS");
                SolrDocumentList unreasonableList = SolrQueryGenerator.list(EngineUtil.MEDICAL_UNREASONABLE_ACTION, unreasonableQuery);
                for (SolrDocument doc : unreasonableList) {
                    String visitId = doc.getFieldValue("VISITID").toString();
                    ReviewSecondVo bean = map.get(visitId);
                    bean.setId(doc.getFieldValue("id").toString());
                    if (doc.containsKey("FIR_REVIEW_STATUS")) {
                        bean.setFirReviewStatus(fStatusDictMap.get(doc.getFieldValue("FIR_REVIEW_STATUS").toString()));
                    }
                    if (doc.containsKey("SEC_REVIEW_STATUS")) {
                        bean.setSecReviewStatus(sStatusDictMap.get(doc.getFieldValue("SEC_REVIEW_STATUS").toString()));
                    }

                    if (doc.containsKey("REVIEW_CASE_IDS")) { // 当前流程肯定不为空
                        List<String> caseIds = doc.getFieldValues("REVIEW_CASE_IDS").stream().map(Object::toString).collect(Collectors.toList());
                        bean.setReviewCaseIds(caseIds);
                        List<String> queryStrs = caseIds.stream().map(relaQueryMap::get).filter(Objects::nonNull).collect(Collectors.toList());
                        if (queryStrs.size() > 0) {
                            String q = "VISITID:\\\"" + visitId + "\\\" AND (" + StringUtils.join(queryStrs, " OR ") + ")";
                            facetList.add(String.format("\"%s\":{type:query, q:\"%s\", facet: {itemQtySum:\"sum(ITEM_QTY)\",itemAmtSum:\"sum(ITEM_AMT)\"}}"
                                    , visitId, q));
                        } else {
                            bean.setRelaItemKind(0);
                            bean.setRelaItemCount(0);
                            bean.setRelaItemFee(0.0);
                        }
                    }
                }
                if (facetList.size() > 0) {
                    String facetStr = "{ " + StringUtils.join(facetList, ",") + "}";
                    JSONObject resultJon = SolrUtil.jsonFacet(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, null, facetStr);
                    for (Map.Entry<String, Object> entry : resultJon.entrySet()) {
                        ReviewSecondVo bean = map.get(entry.getKey());
                        if (bean != null) {
                            JSONObject json = (JSONObject) entry.getValue();
                            bean.setRelaItemKind(json.getIntValue("count"));
                            if (bean.getRelaItemKind() == 0) {
                                bean.setRelaItemCount(0);
                                bean.setRelaItemFee(0.0);
                            } else {
                                bean.setRelaItemCount(json.getIntValue("itemQtySum"));
                                bean.setRelaItemFee(json.getDouble("itemAmtSum"));
                            }
                        }

                    }
                }

            }

        }


        String titleStr = "就诊流水ID号,原始就诊ID,疑似程度,医疗机构名称,医院等级,就诊类型,就诊科室,医生姓名,病人姓名,就诊金额,就诊日期,流程状态,关联项目数,关联项目总个数,关联项目总金额";
        String[] titles = titleStr.split(",");
        String fieldStr = "visitid,ybVisitid,firReviewStatus,orgname,hospgrade,visittype,deptname,doctorname,clientname,totalfee,visitdate,secReviewStatus,relaItemKind,relaItemCount,relaItemFee";//导出的字段
        String[] fields = fieldStr.split(",");
        // 创建文件输出流

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 生成一个表格
        ExportXUtils.exportExl(list, ReviewSecondVo.class, titles, fields, workbook, "不合规病例");

        workbook.write(os);
        workbook.dispose();

    }


    private Map<String, String> caseFieldMap = SolrUtil.initFieldMap(BreakBehaviorCaseExport.class);

    private Map<String, String> behaviorFieldMap = SolrUtil.initFieldMap(MedicalBreakBehaviorResult.class);

    private String[] caseFields = {"visitid", "targetName", "targetType", "behaviorName", "orgname", "visittype", "insurancetype", "deptname", "doctorname", "clientname", "totalfee", "visitdate"};
    private String[] hospFields = {"targetName", "casePay", "caseNum", "targetType", "behaviorName"};
    private String[] clientFields = {"clientid", "targetName", "casePay", "caseNum", "ruleNum"};

    @Override
    public void exportExcelCase(String batchId, Map<String, List<MedicalFormalBehaviorVO>> caseBehaviorMap, SXSSFWorkbook workbook) throws Exception {

        String unreasonableFq = "BATCH_ID:" + batchId + " AND PUSH_STATUS:1";

        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery(unreasonableFq);
        // 设置返回字段
        for (Map.Entry<String, String> entry : caseFieldMap.entrySet()) {
            solrQuery.addField(entry.getValue());
        }
        Map<String, BreakBehaviorClientExport> clientExportMap = new HashMap<>();

        List<BreakBehaviorCaseExport> exportList = new ArrayList<>();
        SolrUtil.exportDoc(solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (doc, index) -> {
            try {
                Collection caseIdList = doc.getFieldValues("REVIEW_CASE_IDS");
                List<MedicalFormalBehaviorVO> behaviorList = new ArrayList<>();
                for (Object caseId : caseIdList) {
                    List<MedicalFormalBehaviorVO> list = caseBehaviorMap.get(caseId);
                    if (list != null) {
                        behaviorList.addAll(list);
                    }
                }
                for (MedicalFormalBehaviorVO behaviorVO : behaviorList) {
                    BreakBehaviorCaseExport caseExport = SolrUtil.solrDocumentToPojo(doc, BreakBehaviorCaseExport.class, caseFieldMap);

                    String behaviorType = behaviorVO.getActionType();
                    caseExport.setTargetType(behaviorType);
                    caseExport.setTargetName((String) doc.getFieldValue(EngineBehaviorServiceImpl.FIELDS_MAPPING.get(behaviorType)[1]));
                    caseExport.setBehaviorName(behaviorVO.getActionName());
                    exportList.add(caseExport);
                }

                String clientid = (String) doc.getFieldValue("CLIENTID");
                if (clientid != null) {
                    Object totalFee = doc.getFieldValue("TOTALFEE");
                    BreakBehaviorClientExport clientExport = clientExportMap.computeIfAbsent(clientid, k -> new BreakBehaviorClientExport(clientid, (String) doc.getFieldValue("CLIENTNAME")));
                    clientExport.addCaseNum(1);
                    if (totalFee != null) {
                        clientExport.addCasePay(new BigDecimal(totalFee.toString()));
                    }
                    clientExport.addRuleIds(caseIdList);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        if (exportList.size() == 0) {
            throw new Exception("没有需要导出的数据");
        }

        // 导出
        ExportXUtils.exportExl(exportList, BreakBehaviorCaseExport.class, caseFields, workbook, "不合规病例");
        // 导出
        List<BreakBehaviorClientExport> clientExportList = Arrays.asList(clientExportMap.values().toArray(new BreakBehaviorClientExport[0]));
        clientExportList.sort((a, b) -> b.getCasePay().compareTo(a.getCasePay()));
        ExportXUtils.exportExl(clientExportList, BreakBehaviorClientExport.class, clientFields, workbook, "不合规参保人");

    }

    @Override
    public void exportExcelHosp(String batchId, Map<String, MedicalFormalBehaviorVO> behaviorIdMap, SXSSFWorkbook workbook) throws Exception {

        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:" + batchId);
        solrQuery.addFilterQuery("TARGET_TYPE:5");
        solrQuery.addSort("CASE_PAY", SolrQuery.ORDER.desc);
        // 设置返回字段
        for (Map.Entry<String, String> entry : behaviorFieldMap.entrySet()) {
            solrQuery.addField(entry.getValue());
        }
        List<BreakBehaviorHospExport> exportList = new ArrayList<>();
        SolrUtil.exportDoc(solrQuery, EngineUtil.MEDICAL_BREAK_BEHAVIOR_RESULT, (doc, index) -> {
            try {
                for (int i = 1; i < 50; i++) {
                    String behaviorId = (String) doc.getFieldValue("BA" + i + "_ID");
                    if (behaviorId == null) {
                        break;
                    }
//                    BreakBehaviorHospExport exportBean = SolrUtil.solrDocumentToPojo(doc, BreakBehaviorHospExport.class, caseFieldMap);
                    MedicalFormalBehaviorVO behaviorVO = behaviorIdMap.get(behaviorId);
                    BreakBehaviorHospExport exportBean = new BreakBehaviorHospExport();
                    exportBean.setTargetName((String) doc.getFieldValue("TARGET_NAME"));
                    exportBean.setCasePay(Double.parseDouble(doc.getFieldValue("BA" + i + "_CASE_PAY").toString()));
                    exportBean.setCaseNum(Integer.parseInt(doc.getFieldValue("BA" + i + "_CASE_NUM").toString()));
                    exportBean.setTargetType(behaviorVO.getActionType());
                    exportBean.setBehaviorName(behaviorVO.getActionName());
                    exportList.add(exportBean);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        // 导出
        ExportXUtils.exportExl(exportList, BreakBehaviorHospExport.class, hospFields, workbook, "不合规医疗机构");

    }

    @Override
    public void exportExcelDoc(String batchId, Map<String, MedicalFormalBehaviorVO> behaviorIdMap, SXSSFWorkbook workbook) throws Exception {

        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:" + batchId);
        solrQuery.addFilterQuery("TARGET_TYPE:3");
        solrQuery.addSort("CASE_PAY", SolrQuery.ORDER.desc);
        // 设置返回字段
        for (Map.Entry<String, String> entry : behaviorFieldMap.entrySet()) {
            solrQuery.addField(entry.getValue());
        }
        List<BreakBehaviorDocExport> exportList = new ArrayList<>();
        SolrUtil.exportDoc(solrQuery, EngineUtil.MEDICAL_BREAK_BEHAVIOR_RESULT, (doc, index) -> {
            try {
                for (int i = 1; i < 50; i++) {
                    String behaviorId = (String) doc.getFieldValue("BA" + i + "_ID");
                    if (behaviorId == null) {
                        break;
                    }
//                    BreakBehaviorHospExport exportBean = SolrUtil.solrDocumentToPojo(doc, BreakBehaviorHospExport.class, caseFieldMap);
                    MedicalFormalBehaviorVO behaviorVO = behaviorIdMap.get(behaviorId);
                    BreakBehaviorDocExport exportBean = new BreakBehaviorDocExport();
                    exportBean.setTargetName((String) doc.getFieldValue("TARGET_NAME"));
                    exportBean.setCasePay(Double.parseDouble(doc.getFieldValue("BA" + i + "_CASE_PAY").toString()));
                    exportBean.setCaseNum(Integer.parseInt(doc.getFieldValue("BA" + i + "_CASE_NUM").toString()));
                    exportBean.setTargetType(behaviorVO.getActionType());
                    exportBean.setBehaviorName(behaviorVO.getActionName());
                    exportList.add(exportBean);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        // 导出
        ExportXUtils.exportExl(exportList, BreakBehaviorDocExport.class, hospFields, workbook, "不合规医护人员");

    }

    @Override
    public void exportStatItemDetail(SolrQuery masterQuery, List<String> unreasonableFqList, String batchId, OutputStream os) throws Exception {
        List<MedicalFormalCaseItemRelaVO> relaList = medicalFormalCaseItemRelaMapper.listVoByBatchId(batchId);

        if (relaList.size() == 0) {
            throw new Exception("请先配置模型关联的项目、药品或组");
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (MedicalFormalCaseItemRelaVO caseItemRela : relaList) {
            if (StringUtils.isBlank(caseItemRela.getItemIds())) {
                continue;
            }

            List<String> fqs = initStatFacetQuery(caseItemRela, unreasonableFqList, masterQuery);

            JSONObject facetChild = new JSONObject();
            facetChild.put("itemName", "max(ITEMNAME)");
            facetChild.put("itemQtySum", "sum(ITEM_QTY)");
            facetChild.put("itemAmtSum", "sum(ITEM_AMT)");

            JSONObject termFacet = new JSONObject();
            termFacet.put("type", "terms");
            termFacet.put("field", "ITEMCODE");
            termFacet.put("limit", Integer.MAX_VALUE);
            // 每个分片取的数据数量
            termFacet.put("overrequest", Integer.MAX_VALUE);
            termFacet.put("facet", facetChild);

            String caseName = caseItemRela.getCaseName();
            SolrUtil.jsonFacet(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, fqs.toArray(new String[0])
                    , termFacet.toJSONString(), (json) -> {
                        json.put("caseName", caseName);
                        resultList.add(json.getInnerMap());
                    });
        }

        if (resultList.size() == 0) {
            throw new Exception("导出结果为空");
        }

        String[] statTitles = {"模型名称", "项目编码", "项目名称", "项目总数量", "项目总金额"};
        String[] statFields = {"caseName", "val", "itemName", "itemQtySum", "itemAmtSum"};

        WritableWorkbook wwb = Workbook.createWorkbook(os);
        ExportUtils.exportExl(resultList, statTitles, statFields, wwb, "模型项目明细统计");

        wwb.write();
        wwb.close();


    }

    @Override
    public void exportStatItemTotal(SolrQuery masterQuery, List<String> unreasonableFqList, String batchId, OutputStream os) throws Exception {
        List<MedicalFormalCaseItemRelaVO> relaList = medicalFormalCaseItemRelaMapper.listVoByBatchId(batchId);

        if (relaList.size() == 0) {
            throw new Exception("请先配置模型关联的项目、药品或组");
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (MedicalFormalCaseItemRelaVO caseItemRela : relaList) {
            if (StringUtils.isBlank(caseItemRela.getItemIds())) {
                continue;
            }

            JSONObject facetChild = new JSONObject();
            facetChild.put("itemQtySum", "sum(ITEM_QTY)");
            facetChild.put("itemAmtSum", "sum(ITEM_AMT)");

            List<String> fqs = initStatFacetQuery(caseItemRela, unreasonableFqList, masterQuery);
            String caseName = caseItemRela.getCaseName();

            JSONObject jsonObject = SolrUtil.jsonFacet(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, fqs.toArray(new String[0]), facetChild.toJSONString());
            jsonObject.put("caseName", caseName);
            if (jsonObject.getIntValue("count") == 0) {
                jsonObject.put("itemQtySum", 0);
                jsonObject.put("itemAmtSum", 0);
            }
            resultList.add(jsonObject.getInnerMap());
        }

        if (resultList.size() == 0) {
            throw new Exception("导出结果为空");
        }

        String[] statTitles = {"模型名称", "项目总数量", "项目总金额"};
        String[] statFields = {"caseName", "itemQtySum", "itemAmtSum"};

        WritableWorkbook wwb = Workbook.createWorkbook(os);
        ExportUtils.exportExl(resultList, statTitles, statFields, wwb, "模型项目统计");

        wwb.write();
        wwb.close();
    }


    @Override
    public void exportStatCaseTotal(SolrQuery masterQuery, List<String> unreasonableFqList, OutputStream os) throws Exception {
        String[] masterFqs = masterQuery.getFilterQueries();
        if (masterFqs != null && masterFqs.length > 0) {
        	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "VISITID");
            unreasonableFqList.add(plugin.parse() + StringUtils.join(masterFqs, " AND ").replace("\"", "\\\""));
        }

        JSONObject facetChild = new JSONObject();
        facetChild.put("totalfee", "max(TOTALFEE)");

        JSONObject termFacet = new JSONObject();
        termFacet.put("type", "terms");
        termFacet.put("field", "CASE_NAME");
        termFacet.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        termFacet.put("overrequest", Integer.MAX_VALUE);
        termFacet.put("facet", facetChild);
        List<Map<String, Object>> resultList = new ArrayList<>();

        SolrUtil.jsonFacet(EngineUtil.MEDICAL_UNREASONABLE_ACTION
                , unreasonableFqList.toArray(new String[0])
                , termFacet.toJSONString(), (json) -> {
                    String[] vals = json.getString("val").split(",");
                    Map<String, Object> map = new HashMap<>();
                    map.put("caseName", vals[1]);
                    map.put("count", json.getIntValue("count"));
                    map.put("totalfee", json.getBigDecimal("totalfee"));
                    resultList.add(map);

                });

        String[] statTitles = {"模型名称", "违反病例数", "就诊总金额"};
        String[] statFields = {"caseName", "count", "totalfee"};

        WritableWorkbook wwb = Workbook.createWorkbook(os);
        ExportUtils.exportExl(resultList, statTitles, statFields, wwb, "模型病例金额统计");

        wwb.write();
        wwb.close();
    }


    private List<String> initStatFacetQuery(MedicalFormalCaseItemRelaVO caseItemRela, List<String> unreasonableFqList, SolrQuery masterQuery) {
        String caseId = caseItemRela.getCaseId();

        Set<String> itemIds = new HashSet<>(Arrays.asList(caseItemRela.getItemIds().split(",")));


        String unreasonableQuery = StringUtils.join(unreasonableFqList, " AND ") + " AND REVIEW_CASE_IDS:" + caseId;

        List<String> fqList = new ArrayList<>();
        SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("MEDICAL_UNREASONABLE_ACTION", "VISITID", "VISITID");
        fqList.add(plugin.parse() + unreasonableQuery);

        String[] masterFqs = masterQuery.getFilterQueries();
        if (masterFqs != null && masterFqs.length > 0) {
        	plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "VISITID");
            fqList.add(plugin.parse() + StringUtils.join(masterFqs, " AND "));
        }

        String type = caseItemRela.getType();
        if ("drugGroup".equals(type)) {
        	plugin = new SolrJoinParserPlugin("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
            fqList.add(plugin.parse() + "DRUGGROUP_CODE:(\"" + StringUtils.join(itemIds, "\",\"") + "\")");

        } else if ("projectGroup".equals(type)) {
        	plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
            fqList.add(plugin.parse() + "TREATGROUP_CODE:(\"" + StringUtils.join(itemIds, "\",\"") + "\")");
        } else {
            fqList.add("ITEMCODE:(\"" + StringUtils.join(itemIds, "\",\"") + "\")");
        }


        return fqList;
    }
}
