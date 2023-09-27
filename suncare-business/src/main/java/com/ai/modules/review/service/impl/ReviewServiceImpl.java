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
import com.ai.common.utils.*;
import com.ai.modules.api.util.ApiTokenCommon;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.config.service.IMedicalOtherDictService;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.dto.DynamicFieldConfig;
import com.ai.modules.review.dto.DynamicLinkProp;
import com.ai.modules.review.service.IDynamicFieldService;
import com.ai.modules.review.service.IReviewService;
import com.ai.modules.review.util.SolrQueryUtil;
import com.ai.modules.review.vo.*;
import com.ai.modules.task.entity.TaskActionFieldCol;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jxl.write.WritableSheet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReviewServiceImpl implements IReviewService {

    @Autowired
    IMedicalActionDictService medicalActionDictService;

    @Autowired
    IMedicalOtherDictService medicalOtherDictService;

    @Autowired
    IDynamicFieldService dynamicFieldService;

    @Override
    public IPage<SolrDocument> pageDynamicResult(Map<String, Set<String>> tabFieldMap, SolrQuery solrQuery, String collection, Page<SolrDocument> page) throws Exception {
        Set<String> resultFields = tabFieldMap.remove(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        if (resultFields == null) {
            resultFields = new HashSet<>();
        }

        Set<String> resultFieldSet = new HashSet<>(resultFields);
        // 前端规则编辑用到
        resultFieldSet.add("RULE_ID");
        resultFieldSet.add("ACTION_TYPE_ID");

        boolean isActionNameIn = resultFieldSet.contains("ACTION_NAME");
        if (isActionNameIn) {
            resultFieldSet.add("ACTION_ID");
        }

        DynamicLinkProp dynamicLinkProp = dynamicFieldService.initLinkProp(tabFieldMap);
        // 添加反查关联字段
        resultFieldSet.addAll(dynamicLinkProp.getLinkFieldSet());
        // 关联必查
        resultFieldSet.add("id");
        // 查询结果列表
        IPage<SolrDocument> pageList = SolrQueryGenerator.page(
                page, solrQuery, collection, resultFieldSet.toArray(new String[0]));
        // 查询副表字段 以VISITID 关联
        if (pageList.getRecords().size() > 0 && tabFieldMap.size() > 0) {
            List<SolrDocument> list = pageList.getRecords();
            // 翻译白名单归因字段
            JSONObject reviewClassifyMap = ApiTokenCommon.queryOtherDictMapByType("reasontype");
            if (resultFieldSet.contains("SEC_REVIEW_CLASSIFY")) {
                list.forEach(r -> {
                    Object code = r.getFieldValue("SEC_REVIEW_CLASSIFY");
                    if (code != null && !"".equals(code)) {
                        r.put("SEC_REVIEW_CLASSIFY", reviewClassifyMap.get(code.toString()));
                    }
                });
            }
            if (resultFieldSet.contains("FIR_REVIEW_CLASSIFY")) {
                list.forEach(r -> {
                    Object code = r.getFieldValue("FIR_REVIEW_CLASSIFY");
                    if (code != null && !"".equals(code)) {
                        r.put("FIR_REVIEW_CLASSIFY", reviewClassifyMap.get(code.toString()));
                    }
                });
            }
            if (isActionNameIn) {
                List<String> actionIdList = list.stream().map(r -> String.valueOf(r.get("ACTION_ID"))).distinct().collect(Collectors.toList());
                Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
                list.forEach(r -> {
                    Object actionId = r.get("ACTION_ID");
                    if (actionId != null) {
                        String actionName = actionNameMap.get(actionId.toString());
                        if (actionName != null) {
                            r.put("ACTION_NAME", actionName);
                        }
                    }
                });
            }

            if(list.size() > 1000){
                for (int i = 0, j, len = list.size(); i < len; i = j) {
                    j = i + 500;
                    if (j > len) {
                        j = len;
                    }
                    List<SolrDocument> childList = list.subList(i, j);
                    dynamicFieldService.addAttrFromOther(childList, dynamicLinkProp);
                }
            } else {
                dynamicFieldService.addAttrFromOther(list, dynamicLinkProp);
            }



        }
        return pageList;
    }

    @Override
    public void dynamicResultExport(String[] fields, String[] fieldTitles, Map<String, Set<String>> tabFieldMap, SolrQuery solrQuery, String collection, boolean isStep2, OutputStream os) throws Exception {

        Set<String> resultFieldSet = tabFieldMap.remove(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        if (resultFieldSet == null) {
            resultFieldSet = new HashSet<>();
        }
        DynamicLinkProp dynamicLinkProp = dynamicFieldService.initLinkProp(tabFieldMap);
        // 添加反查关联字段
        resultFieldSet.addAll(dynamicLinkProp.getLinkFieldSet());

        boolean isActionNameIn = resultFieldSet.contains("ACTION_NAME");
        if (isActionNameIn) {
            resultFieldSet.add("ACTION_ID");
        }

        resultFieldSet.add("id");
        if (solrQuery.getSorts().size() == 0) {
            solrQuery.setSort("id", SolrQuery.ORDER.asc);
        }

        if (solrQuery.getRows() == null || solrQuery.getRows() == 0) {
            solrQuery.setRows(1000000);
        }
        solrQuery.setFields(resultFieldSet.toArray(new String[0]));
        // 查询结果列表
        SolrDocumentList resultDocList = SolrQueryGenerator.list(collection, solrQuery);

        // 查询副表字段
        if (tabFieldMap.size() > 0) {
            for (int i = 0, j, len = resultDocList.size(); i < len; i = j) {
                j = i + 500;
                if (j > len) {
                    j = len;
                }
                List<SolrDocument> childList = resultDocList.subList(i, j);
                dynamicFieldService.addAttrFromOther(childList, dynamicLinkProp);
            }

        }
        // 数据库反查不合规行为名称
        if (isActionNameIn) {
            List<String> actionIdList = resultDocList.stream().map(r -> String.valueOf(r.get("ACTION_ID"))).distinct().collect(Collectors.toList());
            Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
            resultDocList.forEach(r -> {
                Object actionId = r.get("ACTION_ID");
                if (actionId != null) {
                    String actionName = actionNameMap.get(actionId.toString());
                    if (actionName != null) {
                        r.put("ACTION_NAME", actionName);
                    }
                }
            });
        }


        JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictMapByKey("FIRST_REVIEW_STATUS");
        JSONObject pushStatusMap = ApiTokenCommon.queryMedicalDictMapByKey("FIRST_PUSH_STATUS");
        JSONObject reviewClassifyMap = ApiTokenCommon.queryOtherDictMapByType("reasontype");


        Consumer<SolrDocument> peekFun = isStep2 ? bean -> {
            if (bean.containsKey("PREDICT_LABEL")) {
                bean.put("PREDICT_LABEL", reviewStatusMap.get(bean.getFieldValue("PREDICT_LABEL").toString()));
            }
            if (bean.containsKey("FIR_REVIEW_STATUS")) {
                bean.put("FIR_REVIEW_STATUS", reviewStatusMap.get(bean.getFieldValue("FIR_REVIEW_STATUS").toString()));
            }
            if (bean.containsKey("SEC_REVIEW_STATUS")) {
                bean.put("SEC_REVIEW_STATUS", reviewStatusMap.get(bean.getFieldValue("SEC_REVIEW_STATUS").toString()));
            }
            if (bean.containsKey("SEC_PUSH_STATUS") && !"".equals(bean.getFieldValue("SEC_PUSH_STATUS"))) {
                bean.put("SEC_PUSH_STATUS", pushStatusMap.get(bean.getFieldValue("SEC_PUSH_STATUS").toString()));
            } else {
                bean.put("SEC_PUSH_STATUS", "待推送");
            }
            if (bean.containsKey("SEC_REVIEW_CLASSIFY")) {
                bean.put("SEC_REVIEW_CLASSIFY", reviewClassifyMap.get(bean.getFieldValue("SEC_REVIEW_CLASSIFY").toString()));
            }
        } : bean -> {
            if (bean.containsKey("PREDICT_LABEL")) {
                bean.put("PREDICT_LABEL", reviewStatusMap.get(bean.getFieldValue("PREDICT_LABEL").toString()));
            }
            if (bean.containsKey("FIR_REVIEW_STATUS")) {
                bean.put("FIR_REVIEW_STATUS", reviewStatusMap.get(bean.getFieldValue("FIR_REVIEW_STATUS").toString()));
            }
            if (bean.containsKey("FIR_REVIEW_CLASSIFY")) {
                bean.put("FIR_REVIEW_CLASSIFY", reviewClassifyMap.get(bean.getFieldValue("FIR_REVIEW_CLASSIFY").toString()));
            }

            if (bean.containsKey("PUSH_STATUS") && !"".equals(bean.getFieldValue("PUSH_STATUS"))) {
                bean.put("PUSH_STATUS", pushStatusMap.getOrDefault(bean.getFieldValue("PUSH_STATUS").toString(), "未通过"));
            } else {
//                bean.put("PUSH_STATUS", "待推送");
                bean.put("PUSH_STATUS", "未通过");
            }
        };

        List<Map<String, Object>> resultList = resultDocList.stream().peek(peekFun).map(SolrDocument::getFieldValueMap).collect(Collectors.toList());

        List<String> titleList = new ArrayList<>(Arrays.asList(fieldTitles));
        List<String> exportFields = new ArrayList<>(Arrays.asList(fields));

        for (int i = 0, len = titleList.size(); i < len; i++) {
            String field = titleList.get(i);
            if (StringUtils.isBlank(field)) {
                titleList.remove(i);
                exportFields.remove(i);
                --i;
                --len;
            }
        }

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 生成一个表格
        ExportXUtils.exportExl(resultList, titleList.toArray(new String[0]), exportFields.toArray(new String[0])
                , workbook, "不合规病例");

        workbook.write(os);
        workbook.dispose();
    }


    @Override
    public void dynamicGroupExport(String collection, SolrQuery solrQuery, String[] colOrders, Map<String, Set<String>> tabFieldMap
            , List<String> groupByList, boolean isGroupActionName, List<String> facetFields, Set<String> linkFields
            , String[] fieldTitles, String[] fields, List<OutputStream> osList) throws Exception {
        String[][] facetFieldArray = new String[facetFields.size()][3];
        int facetFieldIndex = 0;

        for (String field : facetFields) {
            field = field.replaceAll(" ", "");
            int methodLastIndex = field.indexOf("(");
            String colName = field.substring(methodLastIndex + 1, field.lastIndexOf(")"));
            String[] array = facetFieldArray[facetFieldIndex++];
            array[0] = colName;
            array[1] = field.substring(0, methodLastIndex);
            array[2] = field;
        }

        solrQuery.clearSorts();
        // 设置返回字段
        // 关联字段
        linkFields.forEach(solrQuery::addField);
        // 统计字段
        Arrays.stream(facetFieldArray).filter(r -> !"*".equals(r[0])).forEach(r -> solrQuery.addField(r[0]));

        // 分组字段
        groupByList.forEach(solrQuery::addField);

        solrQuery.setRows(Integer.MAX_VALUE);
        // 获取全部数据
        SolrDocumentList list = SolrQueryGenerator.list(collection, solrQuery);
        // 根据字段值分组
        Map<String, List<SolrDocument>> map = list.stream().collect(Collectors.groupingBy(
                r -> groupByList.stream().map(g -> String.valueOf(r.getFieldValue(g)))
                        .collect(Collectors.joining("::"))
        ));

        List<Map<String, Object>> exportList = new ArrayList<>();
        for (List<SolrDocument> childList : map.values()) {
            SolrDocument doc1st = childList.get(0);
            Map<String, Object> record = new HashMap<>();
            groupByList.forEach(g -> record.put(g, doc1st.getFieldValue(g)));
            linkFields.forEach(g -> record.put(g, doc1st.getFieldValue(g)));
            for (String[] array : facetFieldArray) {
                String colName = array[0];
                String methodName = array[1];
                Object value = "sum".equals(methodName) ? childList.stream().map(r -> {
                    Object val = r.getFieldValue(colName);
                    if (val == null) {
                        val = 0.0;
                    }
                    return Double.parseDouble(val.toString());
                }).reduce(Double::sum).get()
                        : "count".equals(methodName) ? childList.size()
                        : "unique".equals(methodName) ? childList.stream().map(r -> r.getFieldValue(colName)).distinct().count()
                        : "max".equals(methodName) || "min".equals(methodName) ? doc1st.getFieldValue(colName) : 0.0;

                record.put(array[2], value);
            }
            String id = groupByList.stream().map(r -> r + ":" + EngineUtil.escapeQueryChars(record.get(r).toString()))
                    .collect(Collectors.joining(" AND "));

            record.put("id", id);
            exportList.add(record);
        }

        if (tabFieldMap.size() > 0 && exportList.size() > 0) {
            for (int i = 0, j, len = exportList.size(); i < len; i = j) {
                j = i + 500;
                if (j > len) {
                    j = len;
                }
                List<Map<String, Object>> subList = exportList.subList(i, j);
                dynamicFieldService.addGroupAttrFromOther(subList, tabFieldMap);

            }

        }

        // 数据库反查不合规行为名称
        if (isGroupActionName) {
            List<String> actionIdList = exportList.stream().map(r -> String.valueOf(r.get("ACTION_ID"))).distinct().collect(Collectors.toList());
            Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
            exportList.forEach(r -> {
                Object actionId = r.get("ACTION_ID");
                if (actionId != null) {
                    String actionName = actionNameMap.get(actionId.toString());
                    if (actionName != null) {
                        r.put("ACTION_NAME", actionName);
                    } else {
                        r.put("ACTION_NAME", r.get("max(ACTION_NAME)"));
                    }
                }
            });
        }

        exportList.sort((r1, r2) -> {
            for (String colOrder : colOrders) {
                String colName = colOrder.substring(0, colOrder.lastIndexOf(" "));
                String order = colOrder.substring(colOrder.lastIndexOf(" ") + 1);
                boolean isAsc = "asc".equals(order);
                Object val1 = r1.get(colName);
                Object val2 = r2.get(colName);
                int compareVal;
                if (val1 == null) {
                    compareVal = val2 == null ? 0 : isAsc ? -1 : 1;
                } else if (val2 == null) {
                    compareVal = isAsc ? 1 : -1;
                } else {
                    if (val1 instanceof Number) {
                        double val = Double.parseDouble(val1.toString()) - Double.parseDouble(val2.toString());
                        compareVal = val == 0 ? 0 : isAsc ? val > 0 ? 1 : -1 : val > 0 ? -1 : 1;
                    } else {
                        compareVal = isAsc ? val1.toString().compareTo(val2.toString()) : val2.toString().compareTo(val1.toString());
                    }
                }
                if (compareVal != 0) {
                    return compareVal;
                }
            }
            return 0;
        });


        for (int i = 0, len = osList.size(); i < len; i++) {
            int start = i * 500000;
            int end = (i + 1) * 500000;
            if (end > exportList.size()) {
                end = exportList.size();
            }

            SXSSFWorkbook workbook = new SXSSFWorkbook();
            // 生成一个表格
            ExportXUtils.exportExl(exportList.subList(start, end), fieldTitles, fields, workbook, "统计分组结果");

            workbook.write(osList.get(i));
            workbook.dispose();
        }

    }

    @Override
    public void dynamicGroupMultiTableExport(String collection, SolrQuery solrQuery, String[] colOrders, Map<String, Set<String>> tabFieldMap
            , List<String> groupByList, List<String> facetFields, Set<String> linkFields
            , String[] fieldTitles, String[] fields, List<OutputStream> osList) throws Exception {
        String[][] facetFieldArray = new String[facetFields.size()][3];
        int facetFieldIndex = 0;

        for (String field : facetFields) {
            field = field.replaceAll(" ", "");
            int methodLastIndex = field.indexOf("(");
            String colName = field.substring(methodLastIndex + 1, field.lastIndexOf(")"));
            String[] array = facetFieldArray[facetFieldIndex++];
            array[0] = colName;
            array[1] = field.substring(0, methodLastIndex);
            array[2] = field;
        }

        solrQuery.clearSorts();
        // 设置返回字段
        // 关联字段
        linkFields.forEach(solrQuery::addField);
        // 统计字段
        Arrays.stream(facetFieldArray).filter(r -> !"*".equals(r[0])).forEach(r -> solrQuery.addField(r[0]));

        // 分组字段
        groupByList.forEach(solrQuery::addField);

        solrQuery.setRows(Integer.MAX_VALUE);
        // 获取全部数据
        SolrDocumentList list = SolrQueryGenerator.list(collection, solrQuery);
        Set<String> fieldSet = tabFieldMap.remove(collection);
        DynamicLinkProp dynamicLinkProp = dynamicFieldService.initLinkProp(tabFieldMap);
        if (tabFieldMap.size() > 0 && list.size() > 0) {
            for (int i = 0, j, len = list.size(); i < len; i = j) {
                j = i + 500;
                if (j > len) {
                    j = len;
                }
                List<SolrDocument> subList = list.subList(i, j);
                dynamicFieldService.addAttrFromOther(subList, dynamicLinkProp);

            }

        }

        // 根据字段值分组
        Map<String, List<SolrDocument>> map = list.stream().collect(Collectors.groupingBy(
                r -> groupByList.stream().map(g -> String.valueOf(r.getFieldValue(g)))
                        .collect(Collectors.joining("::"))
        ));

        List<Map<String, Object>> exportList = new ArrayList<>();
        for (List<SolrDocument> childList : map.values()) {
            SolrDocument doc1st = childList.get(0);
            Map<String, Object> record = new HashMap<>();
            groupByList.forEach(g -> record.put(g, doc1st.getFieldValue(g)));
            linkFields.forEach(g -> record.put(g, doc1st.getFieldValue(g)));
            for (String[] array : facetFieldArray) {
                String colName = array[0];
                String methodName = array[1];
                Object value = "sum".equals(methodName) ? childList.stream().map(r -> {
                    Object val = r.getFieldValue(colName);
                    if (val == null) {
                        val = 0.0;
                    }
                    return Double.parseDouble(val.toString());
                }).reduce(Double::sum).get()
                        : "count".equals(methodName) ? childList.size()
                        : "unique".equals(methodName) ? childList.stream().map(r -> r.getFieldValue(colName)).distinct().count()
                        : "max".equals(methodName) || "min".equals(methodName) ? doc1st.getFieldValue(colName) : 0.0;

                record.put(array[2], value);
            }
            String id = groupByList.stream().map(r -> r + ":" + EngineUtil.escapeQueryChars(record.get(r).toString()))
                    .collect(Collectors.joining(" AND "));

            record.put("id", id);
            exportList.add(record);
        }

        exportList.sort((r1, r2) -> {
            for (String colOrder : colOrders) {
                String colName = colOrder.substring(0, colOrder.lastIndexOf(" "));
                String order = colOrder.substring(colOrder.lastIndexOf(" ") + 1);
                boolean isAsc = "asc".equals(order);
                Object val1 = r1.get(colName);
                Object val2 = r2.get(colName);
                int compareVal;
                if (val1 == null) {
                    compareVal = val2 == null ? 0 : isAsc ? -1 : 1;
                } else if (val2 == null) {
                    compareVal = isAsc ? 1 : -1;
                } else {
                    if (val1 instanceof Number) {
                        double val = Double.parseDouble(val1.toString()) - Double.parseDouble(val2.toString());
                        compareVal = val == 0 ? 0 : isAsc ? val > 0 ? 1 : -1 : val > 0 ? -1 : 1;
                    } else {
                        compareVal = isAsc ? val1.toString().compareTo(val2.toString()) : val2.toString().compareTo(val1.toString());
                    }
                }
                if (compareVal != 0) {
                    return compareVal;
                }
            }
            return 0;
        });


        for (int i = 0, len = osList.size(); i < len; i++) {
            int start = i * 500000;
            int end = (i + 1) * 500000;
            if (end > exportList.size()) {
                end = exportList.size();
            }

            SXSSFWorkbook workbook = new SXSSFWorkbook();
            // 生成一个表格
            ExportXUtils.exportExl(exportList.subList(start, end), fieldTitles, fields, workbook, "统计分组结果");

            workbook.write(osList.get(i));
            workbook.dispose();
        }

    }

    @Override
    public void dynamicResultExport(String[] fields, String[] fieldTitles, Map<String, Set<String>> tabFieldMap, List<String> groupByList, boolean isGroupActionName, Map<String, String> linkChild, String facetStr, OutputStream os) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        SolrUtil.stream(facetStr, (map, index) -> {
            for (Map.Entry<String, String> entry : linkChild.entrySet()) {
                map.put(entry.getValue(), map.remove(entry.getKey()));
            }
            String id = groupByList.stream().map(r -> r + ":" + EngineUtil.escapeQueryChars(map.get(r).toString()))
                    .collect(Collectors.joining(" AND "));
            map.put("id", id);
            list.add(map);
        });

        if (tabFieldMap.size() > 0 && list.size() > 0) {
            for (int i = 0, j, len = list.size(); i < len; i = j) {
                j = i + 500;
                if (j > len) {
                    j = len;
                }
                List<Map<String, Object>> subList = list.subList(i, j);
                dynamicFieldService.addGroupAttrFromOther(subList, tabFieldMap);

            }

        }

        // 数据库反查不合规行为名称
        if (isGroupActionName) {
            List<String> actionIdList = list.stream().map(r -> String.valueOf(r.get("ACTION_ID"))).distinct().collect(Collectors.toList());
            Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
            list.forEach(r -> {
                Object actionId = r.get("ACTION_ID");
                if (actionId != null) {
                    String actionName = actionNameMap.get(actionId.toString());
                    if (actionName != null) {
                        r.put("ACTION_NAME", actionName);
                    } else {
                        r.put("ACTION_NAME", r.get("max(ACTION_NAME)"));
                    }
                }
            });
        }

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 生成一个表格
        ExportXUtils.exportExl(list, fieldTitles, fields, workbook, "统计分组结果");

        workbook.write(os);
        workbook.dispose();
    }

    @Override
    public DwbAdmmisionVo getDwbAdmmisionByVisitidBySolr(String visitid) throws Exception {
        SolrQuery query = new SolrQuery();
        query.add("q", "*:*");
        query.addFilterQuery("VISITID:\"" + visitid + "\"");
        QueryResponse queryResponse = SolrUtil.call(query, EngineUtil.DWB_ADMMISION);
        SolrDocumentList documents = queryResponse.getResults();
        List<String> result = new ArrayList<String>();
        if (documents.size() > 0) {
            SolrDocument doc = documents.get(0);
            Map<String, Object> map = SolrQueryUtil.putSolrResult(doc);
            DwbAdmmisionVo bean = ObjectMapUtils.mapToObject(map, DwbAdmmisionVo.class);
            return bean;
        }
        return null;
    }

    @Override
    public DwbDischargeVo getDwbDischargeByVisitidBySolr(String visitid) throws Exception {
        SolrQuery query = new SolrQuery();
        query.add("q", "*:*");
        query.addFilterQuery("VISITID:\"" + visitid + "\"");
        QueryResponse queryResponse = SolrUtil.call(query, EngineUtil.DWB_DISCHARGE);
        SolrDocumentList documents = queryResponse.getResults();
        List<String> result = new ArrayList<String>();
        if (documents.size() > 0) {
            SolrDocument doc = documents.get(0);
            Map<String, Object> map = SolrQueryUtil.putSolrResult(doc);
            DwbDischargeVo bean = ObjectMapUtils.mapToObject(map, DwbDischargeVo.class);
            return bean;
        }
        return null;
    }

    @Override
    public void dynamicResultExport(DynamicFieldConfig fieldConfig, SolrQuery solrQuery, String collection, Boolean isStep2, OutputStream os) throws Exception {

        Map<String, Set<String>> tabFieldMap = fieldConfig.getTabFieldMap();
        Set<String> list = tabFieldMap.get(EngineUtil.MEDICAL_UNREASONABLE_ACTION);

        List<String> fields = fieldConfig.getFields();
        List<String> titles = fieldConfig.getTitles();
        fields.add(0, "id");
        titles.add(0, "记录ID");

        if (isStep2) {
            // 初审判定，判定人，复审判定，复审人
            fields.add("FIR_REVIEW_STATUS");
            fields.add("FIR_REVIEW_REMARK");
            fields.add("FIR_REVIEW_USERNAME");
            fields.add("SEC_REVIEW_STATUS");
            fields.add("SEC_REVIEW_REMARK");
            fields.add("SEC_REVIEW_CLASSIFY");
            fields.add("SEC_REVIEW_USERNAME");
//            fields.add("SEC_PUSH_STATUS");

            titles.add("初审判定");
            titles.add("初审判定理由");
            titles.add("初审人");
            titles.add("复审判定");
            titles.add("复审判定理由");
            titles.add("白名单归因");
            titles.add("复审人");
//            titles.add("推送状态");

            list.add("FIR_REVIEW_STATUS");
            list.add("FIR_REVIEW_REMARK");
            list.add("FIR_REVIEW_USERNAME");
            list.add("SEC_REVIEW_STATUS");
            list.add("SEC_REVIEW_REMARK");
            list.add("SEC_REVIEW_CLASSIFY");
            list.add("SEC_REVIEW_USERNAME");
//            list.add("SEC_PUSH_STATUS");
        } else {
            fields.add("PREDICT_LABEL");
            fields.add("PROBILITY");
            fields.add("FIR_REVIEW_USERNAME");
            fields.add("FIR_REVIEW_STATUS");
            fields.add("FIR_REVIEW_REMARK");
            fields.add("FIR_REVIEW_CLASSIFY");
            fields.add("PUSH_STATUS");

            titles.add("Label(AI标签)");
            titles.add("Pr(概率)");
            titles.add("审核人");
            titles.add("判定结果");
            titles.add("判定理由");
            titles.add("白名单归因");
            titles.add("推送状态");

            list.add("PREDICT_LABEL");
            list.add("PROBILITY");
            list.add("FIR_REVIEW_USERNAME");
            list.add("FIR_REVIEW_STATUS");
            list.add("FIR_REVIEW_REMARK");
            list.add("FIR_REVIEW_CLASSIFY");
            list.add("PUSH_STATUS");
        }


        this.dynamicResultExport(fields.toArray(new String[0]), titles.toArray(new String[0]), tabFieldMap, solrQuery, collection, isStep2, os);
    }


    @Override
    public void dynamicResultExport(List<TaskActionFieldCol> colList, SolrQuery solrQuery, String collection, Boolean isStep2, OutputStream os) throws Exception {
        DynamicFieldConfig fieldConfig = new DynamicFieldConfig(colList);


        this.dynamicResultExport(fieldConfig, solrQuery, collection, isStep2, os);
    }


    @Override
    public void saveReviews(JSONObject obj, LoginUser user) throws Exception {
        String[] ids = obj.getString("ids").split(",");
        SolrClient solr = SolrUtil.getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        for (String id : ids) {
            SolrInputDocument doc = initInputDocument(obj, MedicalUnreasonableActionVo.class);
            doc.setField("id", id);
            //第一次审查信息
            doc.setField("FIR_REVIEW_USERID", SolrUtil.initActionValue(user.getId(), "set"));
            doc.setField("FIR_REVIEW_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
            doc.setField("FIR_REVIEW_TIME", SolrUtil.initActionValue(TimeUtil.getNowTime(), "set"));
            doc.setField("PUSH_USERID", SolrUtil.initActionValue(user.getId(), "set"));
            doc.setField("PUSH_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
//			doc.setField("ACTION_COUNT", documents.getNumFound());
            doc.setField("SEC_REVIEW_STATUS", SolrUtil.initActionValue("00", "set"));//待审核
            solr.add(doc);
        }

        solr.commit();
        solr.close();

    }


    private SolrInputDocument initInputDocument(JSONObject json, Class clzz) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SolrInputDocument doc = new SolrInputDocument();
        List<Field> fieldList = new ArrayList<>();
        do {
            fieldList.addAll(Arrays.asList(clzz.getDeclaredFields()));
            clzz = clzz.getSuperclass(); //得到父类,然后赋给自己
            //当父类为null的时候说明到达了最上层的父类(Object类).
        } while (clzz != null);

        for (Field field : fieldList) {
            String name = field.getName();
            Object value = json.get(name);
            String docName = StringCamelUtils.camel2Underline(name);
            if (value != null) {
                doc.setField(docName, SolrUtil.initActionValue(value, "set"));
            }
        }

        return doc;
    }


    //MEDICAL_UNREASONABLE_ACTION查询条件字段的比较类型
    public static Map<String, String> keySymbolMap = new HashMap<String, String>() {
        {
            put("visitid", "like");
            put("batchId", "equal");
            put("actionType", "equal");
            put("actionName", "like");
            put("secReviewStatus", "equal");
            put("hosplevel", "equal");
            put("hospgrade", "equal");
            put("orgname", "like");
            put("clientname", "like");
            put("doctorname", "like");
            put("dataResouceId", "equal");
            put("mainFlag", "equal");
            put("firReviewStatus", "equal");
        }
    };


    //solr查询MEDICAL_UNREASONABLE_ACTION分页数据
    private void getMedicalUnreasonableActionVoPage(Page<MedicalUnreasonableActionVo> page, List<String> fqList)
            throws Exception {
        SolrQuery query = new SolrQuery();
        // 设定查询字段
        String q = "*:*";
        query.add("q", q);
        fqList.forEach(query::addFilterQuery);
        query.set("sort", "VISITDATE desc");
        query.setStart((int) page.offset());
        query.setRows((int) page.getSize());
        QueryResponse queryResponse = SolrUtil.call(query, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        SolrDocumentList documents = queryResponse.getResults();
        List<MedicalUnreasonableActionVo> result = new ArrayList<MedicalUnreasonableActionVo>();
        for (SolrDocument doc : documents) {
            Map<String, Object> map = SolrQueryUtil.putSolrResult(doc);
            MedicalUnreasonableActionVo bean = (MedicalUnreasonableActionVo) ObjectMapUtils.mapToObject(map, MedicalUnreasonableActionVo.class);
            result.add(bean);
        }
        page.setRecords(result);
        page.setTotal(documents.getNumFound());
    }

    @Override
    public void saveCustomReview(JSONObject obj, LoginUser user) throws Exception {
        SolrClient solr = SolrUtil.getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        SolrQuery query = new SolrQuery();
        query.add("q", "*:*");
        String ids = (String) obj.get("id");
        String[] id_arr = ids.split(",");
        String fq = "";
        for (String id : id_arr) {
            if (fq.length() > 0) {
                fq += " OR ";
            }
            fq += " id:" + id;
        }
        query.addFilterQuery(fq);
    }

    @Override
    public DwbMasterInfoVo getDwbMasterInfoByVisitidBySolr(String visitid) throws Exception {
        SolrQuery query = new SolrQuery();
        query.add("q", "*:*");
        query.addFilterQuery("VISITID:\"" + visitid + "\"");
        QueryResponse queryResponse = SolrUtil.call(query, EngineUtil.DWB_MASTER_INFO);
        SolrDocumentList documents = queryResponse.getResults();
        List<String> result = new ArrayList<String>();
        if (documents.size() > 0) {
            SolrDocument doc = documents.get(0);
            Map<String, Object> map = SolrQueryUtil.putSolrResult(doc);
            DwbMasterInfoVo bean = ObjectMapUtils.mapToObject(map, DwbMasterInfoVo.class);
            return bean;
        }
        return null;
    }

    @Override
    public DwbClientVo getDwbClientByClientidBySolr(String clientid) throws Exception {
        SolrQuery query = new SolrQuery();
        query.add("q", "*:*");
        query.addFilterQuery("CLIENTID:\"" + clientid + "\"");
        QueryResponse queryResponse = SolrUtil.call(query, EngineUtil.DWB_CLIENT);
        SolrDocumentList documents = queryResponse.getResults();
        List<String> result = new ArrayList<String>();
        if (documents.size() > 0) {
            SolrDocument doc = documents.get(0);
            Map<String, Object> map = SolrQueryUtil.putSolrResult(doc);
            DwbClientVo bean = ObjectMapUtils.mapToObject(map, DwbClientVo.class);
            return bean;
        }
        return null;
    }

    @Override
    public List<DwbClientVo> getDwbClientByClientidsBySolr(List<String> clientids) throws Exception {
        SolrQuery query = new SolrQuery();
        query.add("q", "*:*");
        query.addFilterQuery("CLIENTID:" + "(\"" + StringUtils.join(clientids, "\",\"") + "\")");
        QueryResponse queryResponse = SolrUtil.call(query, EngineUtil.DWB_CLIENT);
        SolrDocumentList documents = queryResponse.getResults();
        List<DwbClientVo> result = new ArrayList<>();
        for (SolrDocument doc : documents) {
            Map<String, Object> map = SolrQueryUtil.putSolrResult(doc);
            DwbClientVo bean = (DwbClientVo) ObjectMapUtils.mapToObject(map, DwbClientVo.class);
            result.add(bean);
        }
        return result;
    }

    @Override
    public DwbOrganizationVo getDwbOrganizationByOrgidBySolr(String orgid) throws Exception {
        SolrQuery query = new SolrQuery();
        query.add("q", "*:*");
        query.addFilterQuery("ORGID:\"" + orgid + "\"");
        QueryResponse queryResponse = SolrUtil.call(query, EngineUtil.DWB_ORGANIZATION);
        SolrDocumentList documents = queryResponse.getResults();
        List<String> result = new ArrayList<String>();
        if (documents.size() > 0) {
            SolrDocument doc = documents.get(0);
            Map<String, Object> map = SolrQueryUtil.putSolrResult(doc);
            DwbOrganizationVo bean = ObjectMapUtils.mapToObject(map, DwbOrganizationVo.class);
            return bean;
        }
        return null;
    }

    @Override
    public DwbDoctorVo getDwbDoctorByDoctoridBySolr(String doctorid) throws Exception {
        SolrQuery query = new SolrQuery();
        query.add("q", "*:*");
        query.addFilterQuery("DOCTORID:\"" + doctorid + "\"");
        QueryResponse queryResponse = SolrUtil.call(query, EngineUtil.DWB_DOCTOR);
        SolrDocumentList documents = queryResponse.getResults();
        List<String> result = new ArrayList<String>();
        if (documents.size() > 0) {
            SolrDocument doc = documents.get(0);
            Map<String, Object> map = SolrQueryUtil.putSolrResult(doc);
            DwbDoctorVo bean = (DwbDoctorVo) ObjectMapUtils.mapToObject(map, DwbDoctorVo.class);
            return bean;
        }
        return null;
    }

    @Override
    public List<DwbDoctorVo> getDwbDoctorByDoctoridsBySolr(List<String> doctorids) throws Exception {
        SolrQuery query = new SolrQuery();
        query.add("q", "*:*");
        query.addFilterQuery("DOCTORID:" + "(\"" + StringUtils.join(doctorids, "\",\"") + "\")");
        QueryResponse queryResponse = SolrUtil.call(query, EngineUtil.DWB_DOCTOR);
        SolrDocumentList documents = queryResponse.getResults();
        List<DwbDoctorVo> result = new ArrayList<>();
        for (SolrDocument doc : documents) {
            Map<String, Object> map = SolrQueryUtil.putSolrResult(doc);
            DwbDoctorVo bean = (DwbDoctorVo) ObjectMapUtils.mapToObject(map, DwbDoctorVo.class);
            result.add(bean);
        }
        return result;
    }


    @Override
    public void exportExcel(SolrQuery[] solrQuerys, OutputStream os) throws Exception {
        List<MedicalUnreasonableActionVo> list = new ArrayList<MedicalUnreasonableActionVo>();
        for (SolrQuery solrQuery : solrQuerys) {
            SolrUtil.exportDoc(solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (doc, index) -> {
                try {
                    MedicalUnreasonableActionVo bean = SolrUtil.solrDocumentToPojo(doc, MedicalUnreasonableActionVo.class, SolrUtil.initFieldMap(MedicalUnreasonableActionVo.class));
                    if ("white".equals(bean.getFirReviewStatus())) {
                        bean.setFirReviewStatus("白名单");
                    } else if ("blank".equals(bean.getFirReviewStatus())) {
                        bean.setFirReviewStatus("黑名单");
                    } else if ("grey".equals(bean.getFirReviewStatus())) {
                        bean.setFirReviewStatus("灰名单");
                    }
                    if ("white".equals(bean.getFirReviewStatus())) {
                        bean.setFirReviewStatus("白名单");
                    } else if ("blank".equals(bean.getFirReviewStatus())) {
                        bean.setFirReviewStatus("黑名单");
                    } else if ("grey".equals(bean.getFirReviewStatus())) {
                        bean.setFirReviewStatus("灰名单");
                    }
                    if ("00".equals(bean.getSecReviewStatus())) {
                        bean.setSecReviewStatus("待审核");
                    } else if ("01".equals(bean.getSecReviewStatus())) {
                        bean.setSecReviewStatus("待客户确认");
                    } else if ("02".equals(bean.getSecReviewStatus())) {
                        bean.setSecReviewStatus("审核不通过");
                    } else if ("03".equals(bean.getSecReviewStatus())) {
                        bean.setSecReviewStatus("已撤回");
                    } else if ("04".equals(bean.getSecReviewStatus())) {
                        bean.setSecReviewStatus("客户已确认");
                    } else if ("05".equals(bean.getSecReviewStatus())) {
                        bean.setSecReviewStatus("客户已驳回");
                    }
                    list.add(bean);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
        }

        String titleStr = "就诊流水ID号,疑似程度,医疗机构名称,医院等级,就诊类型,就诊科室,医生姓名,病人姓名,就诊金额,就诊日期,流程状态";
        String[] titles = titleStr.split(",");
        String fieldStr = "visitid,firReviewStatus,orgname,hospgrade,visittype,deptname,doctorname,clientname,totalfee,visitdate,secReviewStatus";//导出的字段
        String[] fields = fieldStr.split(",");

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 生成一个表格
        ExportXUtils.exportExl(list, MedicalUnreasonableActionVo.class, titles, fields, workbook, "不合规病例");

        workbook.write(os);
        workbook.dispose();

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
        SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "CLIENTID", "CLIENTID");
        String[] fqs = {plugin.parse() + "VISITID:\"" + visitidParam + "\""};
        SolrDocumentList masterDocuments = SolrQueryGenerator.list(EngineUtil.DWB_MASTER_INFO, fqs, exportMasterFields1);

        List<String> visitids = masterDocuments.stream()
                .sorted(Comparator.comparing(a -> ((String) a.getFieldValue("VISITID"))))
                .map(a -> (String) a.getFieldValue("VISITID")).collect(Collectors.toList());

        SolrQuery chargeQuery = new SolrQuery("*:*");
        chargeQuery.addFilterQuery("VISITID:(\"" + StringUtils.join(visitids, "\",\"") + "\")");
        chargeQuery.setFields(exportMasterFields2);
        chargeQuery.addSort("VISITID", SolrQuery.ORDER.asc);
        chargeQuery.addSort("ITEMCODE", SolrQuery.ORDER.asc);

        List<Map<String, Object>> resultList = new ArrayList<>();
        chargeQuery.setRows(1000000);
        SolrDocumentList chargeDocuments = SolrQueryGenerator.list(EngineUtil.DWB_CHARGE_DETAIL, chargeQuery);
        if (chargeDocuments.size() > 0) {
            Map<String, SolrDocumentList> chargeMap = new HashMap<>();
            SolrDocumentList cacheDocuments = new SolrDocumentList();
            chargeMap.put((String) chargeDocuments.get(0).getFieldValue("VISITID"), cacheDocuments);
            // 都是相同排序，比较并归纳
            for (int i = 0, j = 0, jLen = chargeDocuments.size(); ; ) {
                SolrDocument chargeDoc = chargeDocuments.get(j);
                String chargeVisitid = (String) chargeDoc.getFieldValue("VISITID");
                String visitid = visitids.get(i);
                if (!visitid.equals(chargeVisitid)) {
                    ++i;
                    chargeMap.put(visitids.get(i), cacheDocuments = new SolrDocumentList());
                } else {
                    cacheDocuments.add(chargeDoc);
                    if (++j == jLen) {
                        break;
                    }
                }
            }
            // 一对多构造输出列表
            for (SolrDocument document : masterDocuments) {
                Map<String, Object> cacheMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : document.entrySet()) {
                    cacheMap.put(entry.getKey(), entry.getValue());
                }
                String visitid = (String) document.getFieldValue("VISITID");
                log.info(visitid + ":" + (String) document.getFieldValue("DISEASENAME"));

                SolrDocumentList documentList = chargeMap.get(visitid);
                if (documentList != null && documentList.size() > 0) {
                    for (SolrDocument chargeDoc : documentList) {
                        Map<String, Object> map = new HashMap<>(cacheMap);
                        for (Map.Entry<String, Object> entry : chargeDoc.entrySet()) {
                            map.put(entry.getKey(), entry.getValue());
                        }
                        resultList.add(map);
                    }
                } else {
                    log.info("为空：" + document.getFieldValue("DISEASENAME"));
                }

            }
        }

        ExportUtils.exportExl(resultList, exportMasterTitles, exportMasterFields, sheet, sheet.getName());
    }
}
