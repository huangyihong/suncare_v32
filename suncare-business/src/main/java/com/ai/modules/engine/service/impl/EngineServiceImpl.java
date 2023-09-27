/**
 * EngineServiceImpl.java	  V1.0   2019年11月29日 上午11:06:14
 * <p>
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 * <p>
 * Modification history(By    Time    Reason):
 * <p>
 * Description:
 */

package com.ai.modules.engine.service.impl;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ExportXUtils;
import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.cases.node.TrailDwsNodeRuleHandle;
import com.ai.modules.engine.model.EchartsEntity;
import com.ai.modules.engine.model.EchartsSeriesEntity;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.model.RangeEntity;
import com.ai.modules.engine.model.RangeResult;
import com.ai.modules.engine.model.ReportFacetBucketField;
import com.ai.modules.engine.model.dto.CaseFlowDTO;
import com.ai.modules.engine.model.dto.CompareCaseFlowDTO;
import com.ai.modules.engine.model.dto.CompareFlowDTO;
import com.ai.modules.engine.model.dto.EchartCaseFlowDTO;
import com.ai.modules.engine.model.dto.EchartCompareCaseFlowDTO;
import com.ai.modules.engine.model.dto.EngineCaseDTO;
import com.ai.modules.engine.model.dto.EngineCaseFlowDTO;
import com.ai.modules.engine.model.vo.MedicalCaseVO;
import com.ai.modules.engine.parse.EngineNodeResolver;
import com.ai.modules.engine.service.IEngineDwsService;
import com.ai.modules.engine.service.IEngineService;
import com.ai.modules.engine.service.api.IApiCaseService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.vo.CaseNode;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineServiceImpl implements IEngineService {
	@Autowired
    private IApiDictService dictSV;
	@Autowired
    private IApiCaseService caseSV;
    @Autowired
    private IEngineDwsService dwsService;

    @Override
    public List<EngineNode> parseEngineCaseDTO(EngineCaseDTO dto) throws Exception {
        if (StringUtils.isBlank(dto.getNodes())) {
            throw new EngineBizException("未传递流程节点！");
        }
        if (StringUtils.isBlank(dto.getRules())) {
            throw new EngineBizException("未传递节点参数！");
        }
        //流程节点列表
        List<EngineNode> nodeList = JSON.parseArray(dto.getNodes(), EngineNode.class);
        //规则列表
        List<EngineNodeRule> ruleList = this.parseNodeRule(dto.getRules());
        return parseParentAndRule(nodeList, ruleList);
    }

    private List<EngineNode> parseParentAndRule(List<EngineNode> nodeList, List<EngineNodeRule> ruleList) {
        Map<String, EngineNode> nodeMap = new HashMap<String, EngineNode>();
        //规则按节点分组
        Map<String, List<EngineNodeRule>> nodeRuleMap = ruleList.stream().collect(Collectors.groupingBy(EngineNodeRule::getNodeCode));
        nodeList.forEach(node -> {
            nodeMap.put(node.getNodeCode(), node);
            List<EngineNodeRule> tempRuleList = nodeRuleMap.get(node.getNodeCode());
            if (tempRuleList == null) {
                return;
            }
            //规则按组号分组
            Map<Integer, List<EngineNodeRule>> grpRuleMap = tempRuleList.stream().collect(Collectors.groupingBy(EngineNodeRule::getGroupNo));
            grpRuleMap = grpRuleMap.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            if (grpRuleMap != null) {
                //按组号排序
//				grpRuleMap.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey()));
                int index = 0;
                List<EngineNodeRuleGrp> wheres = new ArrayList<EngineNodeRuleGrp>();
                for (Map.Entry<Integer, List<EngineNodeRule>> entry : grpRuleMap.entrySet()) {
                    List<EngineNodeRule> tempList = entry.getValue();
                    //按组内规则排序
                    tempList = tempList.stream().sorted(Comparator.comparing(EngineNodeRule::getOrderNo)).collect(Collectors.toList());
                    EngineNodeRuleGrp grp = new EngineNodeRuleGrp();
                    if (index == 0) {
                        //组间第一组的逻辑运算符设置为null
                        grp.setLogic(null);
                    } else {
                        grp.setLogic(tempList.get(0).getLogic());
                    }
                    grp.setRuleList(tempList);
                    //组内第一个条件的逻辑运算符设置为null
                    tempList.get(0).setLogic(null);
                    wheres.add(grp);
                    index++;
                }
                node.setWheres(wheres);
            }
        });

        //再次遍历节点，设置它的父级节点
        nodeList.forEach(node -> {
        	EngineNode parent = nodeMap.get(node.getPrevNodeCode());
            if(parent!=null) {
            	parent.setCondition(node.getPrevNodeCondition());
            }
        });
        return nodeList;
    }

    @Override
    public List<EngineNode> parseEngineCaseDTO(CaseFlowDTO dto) throws Exception {
        if (StringUtils.isBlank(dto.getFlowJson())) {
            throw new EngineBizException("未传递流程节点！");
        }
        if (StringUtils.isBlank(dto.getRules())) {
            throw new EngineBizException("未传递节点参数！");
        }
        //流程节点列表
        List<EngineNode> nodeList = this.parseFlowJson(dto.getFlowJson());
        //规则列表
        List<EngineNodeRule> ruleList = this.parseNodeRule(dto.getRules());
        return parseParentAndRule(nodeList, ruleList);
    }


    private List<EngineNodeRule> parseNodeRule(String rules) {
        //规则列表
        List<EngineNodeRule> ruleList = JSON.parseArray(rules, EngineNodeRule.class);
        for (EngineNodeRule rule : ruleList) {
            MedicalColConfig config = dictSV.queryMedicalColConfig(rule.getTableName(), rule.getColName());
            if (config != null) {
                rule.setColConfig(config);
            }
        }
        return ruleList;
    }

    /**
     * 功能描述：解析节点流程图json字符串
     *
     * @param flowJson
     * @return <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     * @author zhangly
     * <p>创建日期 ：2019年12月2日 下午3:50:11</p>
     */
    private List<EngineNode> parseFlowJson(String flowJson) {
        List<EngineNode> result = new ArrayList<EngineNode>();
        JSONObject jsonObject = (JSONObject) JSONObject.parse(flowJson);
        JSONArray nodeArray = jsonObject.getJSONArray("nodeDataArray");
        JSONArray linkArray = jsonObject.getJSONArray("linkDataArray");
        Map<String, CaseNode> map = new HashMap<>();
        for (int i = 0, len = nodeArray.size(); i < len; i++) {
            JSONObject json = nodeArray.getJSONObject(i);
            boolean isGroup = json.getBooleanValue("isGroup");
            if(isGroup){
                continue;
            }
            String key = json.getString("key");
            CaseNode node = new CaseNode();
            node.setKey(key);
            node.setData(json);
            map.put(key, node);
        }
        for (int i = 0, len = linkArray.size(); i < len; i++) {
            JSONObject json = linkArray.getJSONObject(i);
            String from = json.getString("from");
            String to = json.getString("to");
            CaseNode fromNode = map.get(from);
            CaseNode toNode = map.get(to);
            toNode.setParent(fromNode);
            fromNode.addChild(toNode);
            // 节点条件为否
            if (StringUtils.isNotEmpty(json.getString("visible"))
                    && "否".equals(json.get("text"))) {
                toNode.setFromYes(false);
            }
        }
        for (Map.Entry<String, CaseNode> entry : map.entrySet()) {
            EngineNode node = new EngineNode();
            CaseNode caseNode = entry.getValue();
            node.setNodeCode(caseNode.getKey());
            JSONObject json = caseNode.getData();
            String type = json.getString("type");
            node.setNodeType(type);
            node.setNodeName(json.getString("name"));
            if (caseNode.getParent() != null) {
                node.setPrevNodeCode(caseNode.getParent().getKey());
            }
            node.setPrevNodeCondition(caseNode.isFromYes() ? "YES" : "NO");
            result.add(node);
        }
        return result;
    }

    @Override
    public Set<String> constructConditionExpression(List<EngineNode> nodeList) throws Exception {
        Set<String> fq = new LinkedHashSet<String>();
        nodeList.forEach(node -> {
            String condition = EngineUtil.parseConditionExpression(node);
            if (StringUtils.isNotBlank(condition)) {
                fq.add(condition);
            }
        });
        return fq;
    }

    /**
     * 构造返回的动态字段列表
     *
     * @param ruleList
     * @return
     * @throws Exception
     */
    public Set<String> constructFields(List<EngineNodeRule> ruleList) throws Exception {
        Set<String> flSet = new HashSet<>();
        for (EngineNodeRule rule : ruleList) {
            MedicalColConfig colConfig = rule.getColConfig();
            // 不是主表字段跳过
            if (!EngineUtil.DWB_MASTER_INFO.equals(colConfig.getTabName())) {
                continue;
            }
            String fl = rule.getColName();
            // 虚拟字段
            if (colConfig.getColType() == 2) {
                fl += (":" + colConfig.getColValueExpressionSolr());
            }
            flSet.add(fl);
        }
        return flSet;
    }

    List<String> trialFl = Arrays.asList("VISITID", "CLIENTNAME", "SEX", "YEARAGE", "ORGNAME");

    @Override
    public Set<String> constructTrialFq(EngineCaseFlowDTO dto) throws Exception {
        if (StringUtils.isBlank(dto.getFlowJson())) {
            throw new EngineBizException("未传递流程节点！");
        }
        if (StringUtils.isBlank(dto.getRules())) {
            throw new EngineBizException("未传递节点参数！");
        }
        //规则列表
        List<EngineNodeRule> ruleList = this.parseNodeRule(dto.getRules());
        List<EngineNode> nodes = this.parseParentAndRule(this.parseFlowJson(dto.getFlowJson()), ruleList);
        // 构造查询条件
        Set<String> fqSet = this.constructConditionExpression(nodes);
        return fqSet;
    }

    @Override
    public IPage<SolrDocument> trial(IPage<SolrDocument> page, EngineCaseFlowDTO dto) throws Exception {
        // 构造返回字段列表
        //Set<String> flSet = this.constructFields(ruleList);
        // 添加必要字段
        //flSet.addAll(trialFl);
        // 构造查询条件
        Set<String> fqSet = this.constructTrialFq(dto);
        SolrQuery query = new SolrQuery("*:*");
        // 设定返回字段
        //flSet.forEach(query::addField);
        Set<String> virtualSet = new HashSet<String>();
        for (int i = 0, len = dto.getCols().length; i < len; i++) {
            String field = dto.getCols()[i];
            MedicalColConfig config = dictSV.queryMedicalColConfig("DWB_MASTER_INFO", field);
            if (config != null && config.getColType() == 2 && StringUtils.isNotBlank(config.getColValueExpressionSolr())) {
                //虚拟字段
                query.set(field, config.getColValueExpressionSolr());
                query.addField(field + ":$" + field);
                virtualSet.add(field);
            } else {
                query.addField(field);
            }
        }
        // 设定查询字段
        fqSet.forEach(query::addFilterQuery);
        query.setStart((int) page.offset());
        query.setRows((int) page.getSize());
        if (StringUtils.isNotBlank(dto.getColumn())) {
            String[] columns = StringUtils.split(dto.getColumn(), ",");
            String[] orders = StringUtils.split(dto.getOrder(), ",");
            if (columns.length != orders.length) {
                throw new EngineBizException("参数排序字段与排序方式的个数不一致");
            }
            for (int i = 0, len = columns.length; i < len; i++) {
                String order = orders[i];
                String column = columns[i];
                if (virtualSet.contains(column)) {
                    column = "$".concat(column);
                }
                if ("asc".equalsIgnoreCase(order)) {
                    query.addSort(column, ORDER.asc);
                } else {
                    query.addSort(column, ORDER.desc);
                }
            }
        } else {
            query.addSort("VISITID", ORDER.asc);
        }
        QueryResponse queryResponse = SolrUtil.call(query, EngineUtil.DWB_MASTER_INFO);
        SolrDocumentList documents = queryResponse.getResults();
        //			MedicalCaseVO vo = SolrUtil.solrDocumentToPojo(doc, MedicalCaseVO.class, EngineUtil.FIELD_MAPPING);
        List<SolrDocument> result = new ArrayList<>(documents);
        page.setRecords(result);
        page.setTotal(documents.getNumFound());
        return page;
    }

    private String[] exportMasterFields = {"VISITID", "CLIENTNAME", "SEX", "YEARAGE", "DISEASENAME",
//            null,null,null,null,null,
            "ORGNAME", "HOSPLEVEL", "DEPTNAME", "VISITDATE", "LEAVEDATE", "TOTALFEE", "ETL_SOURCE_NAME",};

    private String[] exportMasterTitles = {"就诊ID", "姓名", "性别", "年龄", "疾病诊断名称",
            "医疗机构名称", "医疗机构级别", "就诊科室", "就诊时间", "出院时间", "就诊总金额", "ETL来源"
    };

    private String[] exportChargeFields = {"VISITID","ITEMCLASS", "ITEMNAME", "AMOUNT", "ITEMPRICE", "FEE"};


    private String[] exportFields = {"VISITID", "CLIENTNAME", "SEX", "YEARAGE", "DISEASENAME",
            "ITEMCLASS", "ITEMNAME", "AMOUNT", "ITEMPRICE", "FEE",
            "ORGNAME", "HOSPLEVEL", "DEPTNAME", "VISITDATE", "LEAVEDATE", "TOTALFEE", "ETL_SOURCE_NAME",};
    private String[] exportTitles = {"就诊ID", "姓名", "性别", "年龄", "疾病诊断名称",
            "项目类别", "项目名称", "项目数量", "项目单价", "项目总金额",
            "医疗机构名称", "医疗机构级别", "就诊科室", "就诊时间", "出院时间", "就诊总金额", "ETL来源"
    };

    @Override
    public Integer trialExport(EngineCaseFlowDTO dto, OutputStream os) throws Exception {
        Set<String> fqSet = this.constructTrialFq(dto);
        SolrQuery query = new SolrQuery("*:*");
        // 设定查询字段
        fqSet.forEach(query::addFilterQuery);
        query.setFields(exportMasterFields);
        query.setRows(1000000);
//        query.setRows(201);

        if (StringUtils.isNotBlank(dto.getColumn())) {
            String[] columns = StringUtils.split(dto.getColumn(), ",");
            String[] orders = StringUtils.split(dto.getOrder(), ",");
            if (columns.length != orders.length) {
                throw new EngineBizException("参数排序字段与排序方式的个数不一致");
            }
            for (int i = 0, len = columns.length; i < len; i++) {
                String order = orders[i];
                String column = columns[i];
                MedicalColConfig config = dictSV.queryMedicalColConfig("DWB_MASTER_INFO", column);
                if (config != null && config.getColType() == 2 && StringUtils.isNotBlank(config.getColValueExpressionSolr())) {
                    //虚拟字段
                    query.set(column, config.getColValueExpressionSolr());
                    column = "$".concat(column);
                }
                if ("asc".equalsIgnoreCase(order)) {
                    query.addSort(column, ORDER.asc);
                } else {
                    query.addSort(column, ORDER.desc);
                }
            }
        } else {
            query.addSort("VISITID", ORDER.asc);
        }


        Function<List<SolrDocument>, List<Map<String, Object>>> dealFun = cacheList -> {
            List<Map<String, Object>> resultList = new ArrayList<>();

            List<String> visitids = cacheList.stream()
                    .sorted(Comparator.comparing(a -> ((String) a.getFieldValue("VISITID"))))
                    .map(a -> (String) a.getFieldValue("VISITID")).collect(Collectors.toList());

            SolrQuery chargeQuery = new SolrQuery("*:*");
            chargeQuery.addFilterQuery("VISITID:(\"" + StringUtils.join(visitids, "\",\"") + "\")");
            chargeQuery.setFields(exportChargeFields);
            chargeQuery.addSort("VISITID", ORDER.asc);
            chargeQuery.addSort("ITEMCODE", ORDER.asc);

            try {
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
                    for (SolrDocument document : cacheList) {
                        Map<String, Object> cacheMap = new HashMap<>();
                        for(Map.Entry<String, Object> entry: document.entrySet()){
                            cacheMap.put(entry.getKey(),entry.getValue());
                        }

                        String visitid = (String) document.getFieldValue("VISITID");
                        SolrDocumentList documentList = chargeMap.get(visitid);
                        if (documentList != null && documentList.size() > 0) {
                            for (SolrDocument chargeDoc : documentList) {
                                Map<String, Object> map = new HashMap<>(cacheMap);
                                for(Map.Entry<String, Object> entry: chargeDoc.entrySet()){
                                    map.put(entry.getKey(),entry.getValue());
                                }
                                resultList.add(map);
                            }
                        }

                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            return resultList;
        };
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<SolrDocument> cacheList = new ArrayList<>();

        SolrUtil.exportDoc(query, EngineUtil.DWB_MASTER_INFO, (doc, index) -> {
            cacheList.add(doc);
            if (cacheList.size() == 100 && resultList.size() < 1000000) {
                resultList.addAll(dealFun.apply(cacheList));
                cacheList.clear();
            }
        });

        if(cacheList.size() > 0 && resultList.size() < 1000000){
            resultList.addAll(dealFun.apply(cacheList));
        }

//        ExcelUtils.writeOneSheetSXSSFWorkbook(resultList, exportTitles, exportFields,"病例试算", os);
        List<Map<String, Object>> exportList = resultList;
        if(exportList.size() > 1000000){
            exportList = exportList.subList(0, 1000000);
        }
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 生成一个表格
        ExportXUtils.exportExl(exportList,exportTitles, exportFields,workbook,"试算-项目明细");

        workbook.write(os);
        workbook.dispose();

        return exportList.size();
       /* StringBuilder sb = new StringBuilder("leftOuterJoin(");
        sb.append("search(DWB_MASTER_INFO, q=\"*:*\"");
        sb.append(",fl=\"VISITID,").append(StringUtils.join(exportMasterFields, ",")).append("\"");
        for (String fq : fqSet) {
            sb.append(",fq=\"").append(fq.replaceAll("\"","\\\\\"")).append("\"");
        }
        sb.append(",rows=").append(100);
        sb.append(",sort=\"VISITID asc\"),");

        sb.append("search(DWB_CHARGE_DETAIL, q=\"*:*\"");
        sb.append(",fl=\"VISITID,").append(StringUtils.join(exportChargeFields, ",")).append("\"");
        sb.append(",rows=").append(Integer.MAX_VALUE);
        sb.append(",sort=\"VISITID asc\")");

        sb.append(",on=\"VISITID\")");

        List<Map<String, Object>> list = SolrUtil.stream(sb.toString());

        ExportUtils.exportExl(list,exportTitles,exportFields,sheet,sheet.getName());*/


    }

    @Override
    public void trialExportMasterInfo(EngineCaseFlowDTO dto, SolrQuery query, OutputStream os) throws Exception {
        query.setFields(exportMasterFields);
        if (StringUtils.isNotBlank(dto.getColumn())) {
            String[] columns = StringUtils.split(dto.getColumn(), ",");
            String[] orders = StringUtils.split(dto.getOrder(), ",");
            if (columns.length != orders.length) {
                throw new EngineBizException("参数排序字段与排序方式的个数不一致");
            }
            for (int i = 0, len = columns.length; i < len; i++) {
                String order = orders[i];
                String column = columns[i];
                MedicalColConfig config = dictSV.queryMedicalColConfig("DWB_MASTER_INFO", column);
                if (config != null && config.getColType() == 2 && StringUtils.isNotBlank(config.getColValueExpressionSolr())) {
                    //虚拟字段
                    query.set(column, config.getColValueExpressionSolr());
                    column = "$".concat(column);
                }
                if ("asc".equalsIgnoreCase(order)) {
                    query.addSort(column, ORDER.asc);
                } else {
                    query.addSort(column, ORDER.desc);
                }
            }
        } else {
            query.addSort("VISITID", ORDER.asc);
        }


        List<Map<String, Object>> resultList = new ArrayList<>();

        SolrUtil.exportDoc(query, EngineUtil.DWB_MASTER_INFO, (doc, index) -> {
            resultList.add(doc);
        });

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 生成一个表格
        ExportXUtils.exportExl(resultList,exportMasterTitles, exportMasterFields,workbook,"试算-就诊明细");

        workbook.write(os);
        workbook.dispose();
    }

    /**
     * 功能描述：计算指标数据范围[最小值,最大值]
     *
     * @param fqSet
     * @param field
     * @throws Exception <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     * @author zhangly
     * <p>创建日期 ：2019年12月4日 上午9:08:32</p>
     */
    private RangeResult range(String collection, Set<String> fqSet, String field) throws Exception {
        SolrQuery query = new SolrQuery("*:*");
        query.setRows(0);
        // 设定查询字段
        if (fqSet != null) {
            fqSet.forEach(query::addFilterQuery);
        }

        StringBuilder sb = new StringBuilder("{");
        sb.append("min:").append("\"min(").append(field).append(")\"");
        sb.append(",max:").append("\"max(").append(field).append(")\"");
        sb.append(",avg:").append("\"avg(").append(field).append(")\"");
        sb.append(",median:").append("\"percentile(").append(field).append(",50)\"");
        sb.append(",mode:{type:terms,field:").append(field).append(",limit:1}");
        sb.append("}");
        query.set("json.facet", sb.toString());
        try {
            QueryResponse queryResponse = SolrUtil.call(query, collection);
            NestableJsonFacet nestableJsonFacet = queryResponse.getJsonFacetingResponse();
            long count = nestableJsonFacet.getCount();
            if (count == 0) {
                return null;
            }
            // 众数分组
            List<BucketJsonFacet> modeBuckets = nestableJsonFacet.getBucketBasedFacets("mode").getBuckets();
            if (modeBuckets.size() == 0) {
                return null;
            }

            double min = nestableJsonFacet.getStatFacetValue("min").doubleValue();
            double max = nestableJsonFacet.getStatFacetValue("max").doubleValue();
            double avg = nestableJsonFacet.getStatFacetValue("avg").doubleValue();
            double median = nestableJsonFacet.getStatFacetValue("median").doubleValue();
            double mode = Double.parseDouble(modeBuckets.get(0).getVal().toString());


            RangeResult result = new RangeResult();
            result.setMin(min);
            result.setMax(max);
            result.setAvg(avg);
            result.setMedian(median);
            result.setMode(mode);
            result.setCount(count);
            return result;

            // 中位数
		/*	BigDecimal median = getSolrCurMedian(fqSet,facetField,rangeBuckets);
			echart.setMedian(median.setScale(2,BigDecimal.ROUND_HALF_UP));*/
        } catch (Exception e) {
            log.error("solr exception=", e);
            throw new EngineBizException("solr查询失败：" + e.getMessage());
        }
    }

    /**
     * 功能描述：指标数据范围分组
     *
     * @param fqSet
     * @param field
     * @param size
     * @return
     * @throws Exception <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     * @author zhangly
     * <p>创建日期 ：2019年12月4日 上午9:08:46</p>
     */
    private List<RangeEntity> range(RangeResult rangeResult, int size, String gapType) throws Exception {
        double min = rangeResult.getMin();
        double max = rangeResult.getMax();
        double avg = rangeResult.getAvg();
        List<RangeEntity> result = new LinkedList<RangeEntity>();
        size = size <= 0 || size > 10 ? 5 : size;
        if (max < min * 1.2) {
            //最大值小于最小值乘以一个系数时不再继续分组
            RangeEntity entity = new RangeEntity();
            entity.setMin(min);
            entity.setMax(max);
            entity.setAxis(entity.getMin() + "~" + entity.getMax());
            result.add(entity);
        } else if ("avgLog".equals(gapType)) {
            // 平均数前五等分 以平均数的log均分（逆序）， 后五等分以最大值-均数log均分
            double avgLogMod = Math.log10(avg - min) / 5;
            double lastLogMod = Math.log10(max - avg) / 5;

            double lastMax = min;
            for (int i = 1; i < 6; i++) {
                RangeEntity entity = new RangeEntity();
                entity.setMin(lastMax);
                entity.setMax(avg + 1 - Math.pow(10, avgLogMod * (5 - i)));

                double minVal = entity.getMin();
                double maxVal = lastMax = entity.getMax();

                if (minVal - Math.floor(minVal) > (1e-10)) {
                    minVal = Double.parseDouble(String.format("%.2f", minVal));
                }
                if (maxVal - Math.floor(maxVal) > (1e-10)) {
                    maxVal = Double.parseDouble(String.format("%.2f", maxVal));
                }
                entity.setAxis(minVal + "~" + maxVal);
                result.add(entity);
            }
            for (int i = 1; i < 6; i++) {
                RangeEntity entity = new RangeEntity();
                entity.setMin(lastMax);
                entity.setMax(avg + Math.pow(10, lastLogMod * i));

                double minVal = entity.getMin();
                double maxVal = lastMax = entity.getMax();

                if (minVal - Math.floor(minVal) > (1e-10)) {
                    minVal = Double.parseDouble(String.format("%.2f", minVal));
                }
                if (maxVal - Math.floor(maxVal) > (1e-10)) {
                    maxVal = Double.parseDouble(String.format("%.2f", maxVal));
                }
                entity.setAxis(minVal + "~" + maxVal);
                result.add(entity);
            }
        } else if ("maxLog".equals(gapType)) {
            double beginVal = max <= 0 ? min : 0;
            double lastLogMod = Math.log10(max - min) / 10;

            double lastMax = min;

            for (int i = 1; i < 11; i++) {
                RangeEntity entity = new RangeEntity();
                entity.setMin(lastMax);
                entity.setMax(beginVal + Math.pow(10, lastLogMod * i));

                double minVal = entity.getMin();
                double maxVal = lastMax = entity.getMax();

                if (minVal - Math.floor(minVal) > (1e-10)) {
                    minVal = Double.parseDouble(String.format("%.2f", minVal));
                }
                if (maxVal - Math.floor(maxVal) > (1e-10)) {
                    maxVal = Double.parseDouble(String.format("%.2f", maxVal));
                }
                entity.setAxis(minVal + "~" + maxVal);
                result.add(entity);
            }
        } else {
            double mod = Double.parseDouble(String.format("%.2f", (max - min) / 10));
            for (int i = 1; i <= size; i++) {
                RangeEntity entity = new RangeEntity();
                entity.setMin(min + (i - 1) * mod);
                entity.setMax(min + i * mod);
                if (entity.getMax() > max) {
                    entity.setMax(max);
                }
                double minVal = entity.getMin();
                double maxVal = entity.getMax();
                if (minVal - Math.floor(minVal) > (1e-10)) {
                    minVal = Double.parseDouble(String.format("%.2f", minVal));
                }
                if (maxVal - Math.floor(maxVal) > (1e-10)) {
                    maxVal = Double.parseDouble(String.format("%.2f", maxVal));
                }
                entity.setAxis(minVal + "~" + maxVal);
                result.add(entity);
                if (entity.getMax() - max > 0) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public EchartsEntity echart(EchartCaseFlowDTO dto) throws Exception {
        String facetField = dto.getFacetField();
        if (dto.isDWS()) {
            //规则
            List<EngineNodeRuleGrp> grpWheres = dwsService.parseNodeRule(dto.getRules());
            String collection = grpWheres.get(0).getRuleList().get(0).getTableName().toUpperCase();
            TrailDwsNodeRuleHandle handle = new TrailDwsNodeRuleHandle(grpWheres).withMaster(collection);
            Set<String> fqSet = new HashSet<String>();
            fqSet.add(handle.where());
            return this.echart(collection, facetField, dto.getMin(), dto.getMax(), fqSet, 10, dto.getGapType());
        }
        List<EngineNode> nodes = this.parseEngineCaseDTO(dto);
        // 构造查询条件
        Set<String> fqSet = this.constructConditionExpression(nodes);
        return this.echart(EngineUtil.DWB_MASTER_INFO, facetField, dto.getMin(), dto.getMax(), fqSet, 10, dto.getGapType());
    }

/*
    private EchartsEntity echart2(String facetField, Set<String> fqSet, int limit) throws Exception {
        if (StringUtils.isBlank(facetField)) {
            throw new EngineBizException("facetField参数不能为空！");
        }
        MedicalColConfig config = configService.getMedicalColConfig(facetField, "DWB_MASTER_INFO");
        if (config == null) {
            throw new EngineBizException("DWB_MASTER_INFO未找到" + facetField + "字段配置信息");
        }
        EchartsEntity echart = new EchartsEntity();
        echart.setLegend(new String[]{config.getColChnName()});
        if ("NUMBER".equalsIgnoreCase(config.getDataType())
                || "INT".equalsIgnoreCase(config.getDataType())) {
            // 指标数据范围分组
            RangeResult rangeResult = this.range(fqSet, facetField);
            List<RangeEntity> rangeList = this.range(rangeResult, limit);
            if (rangeList == null || rangeList.size() == 0) {
                return null;
            }
            String url = SolrUtil.SOLR_URL + "/" + EngineUtil.DWB_MASTER_INFO + "/stream";
            Map<String, String> params = new HashMap<String, String>();
            StringBuilder sb = new StringBuilder("tuple(");
            sb.append("range=list(");
            int index = 0;
            int len = rangeList.size();
            // x轴数据
            String[] xAxis = new String[len];
            for (RangeEntity entity : rangeList) {
                xAxis[index] = entity.getAxis();
                sb.append("stats(" + EngineUtil.DWB_MASTER_INFO + ",q=\"*:*\",fq=\"{!frange l=");
                sb.append(entity.getMin());
                sb.append(" u=").append(entity.getMax());
                sb.append("}").append(facetField).append("\",count(*)");
                sb.append("),");
                index++;
            }
            sb.append("),stat=list(");
            // 求平均
            String avgField = "avg(" + facetField + ")";
            sb.append("stats(" + EngineUtil.DWB_MASTER_INFO + ",q=\"*:*\",").append(avgField).append("),");
            // 众数
            sb.append("facet(" + EngineUtil.DWB_MASTER_INFO + ",q=\"*:*\",buckets=\"").append(facetField).append(" \",bucketSorts=\"count(*) desc\", count(*),rows=1),");
            sb.append(")");
            sb.append(")");
            params.put("expr", sb.toString());
            JSONObject resultObj = new JSONObject(SolrUtil.stream(sb.toString()).get(0));
            // y轴数据
            List<EchartsSeriesEntity> series = new ArrayList<EchartsSeriesEntity>();
            EchartsSeriesEntity seriesEntity = new EchartsSeriesEntity(config.getColChnName());
            JSONArray rangeArray = resultObj.getJSONArray("range");
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject = rangeArray.getJSONObject(i);
                seriesEntity.add(jsonObject.getLong("count(*)"));
            }
            // 统计值
            JSONArray statArray = resultObj.getJSONArray("stat");
            String avgVal = statArray.getJSONObject(0).getString(avgField);
            String modeVal = statArray.getJSONObject(1).getString(facetField);
            series.add(seriesEntity);
            echart.setxAxis(xAxis);
            echart.setSeries(series);
            echart.setAvg(new BigDecimal(avgVal).setScale(2, BigDecimal.ROUND_HALF_UP));
            echart.setMode(new BigDecimal(modeVal).setScale(2, BigDecimal.ROUND_HALF_UP));
        } else {
            //直接分组
            List<ReportFacetBucketField> list = singleDim(fqSet, facetField, limit);
            if (list == null || list.size() == 0) {
                return null;
            }
            // x轴数据
            String[] xAxis = new String[list.size()];
            // y轴数据
            EchartsSeriesEntity seriesEntity = new EchartsSeriesEntity(config.getColChnName());
            int index = 0;
            for (ReportFacetBucketField bucketField : list) {
                xAxis[index++] = bucketField.getField();
                seriesEntity.add(bucketField.getValue().longValue());
            }
            List<EchartsSeriesEntity> series = new ArrayList<EchartsSeriesEntity>();
            series.add(seriesEntity);
            echart.setxAxis(xAxis);
            echart.setSeries(series);
        }

        return echart;
    }
*/

    private EchartsEntity echart(String collection, String facetField, BigDecimal minRange, BigDecimal maxRange, Set<String> fqSet, int limit, String gapType) throws Exception {
        if (StringUtils.isBlank(facetField)) {
            throw new EngineBizException("facetField参数不能为空！");
        }
        MedicalColConfig config = dictSV.queryMedicalColConfig(collection, facetField);
        if (config == null) {
            throw new EngineBizException(collection + "未找到" + facetField + "字段配置信息");
        }
        if ("NUMBER".equalsIgnoreCase(config.getDataType())
                || "INT".equalsIgnoreCase(config.getDataType())) {
            // 指标数据范围分组
            if (minRange != null && maxRange != null) {
                fqSet.add(facetField + ":[" + minRange + " TO " + maxRange + "]");
            }
            RangeResult rangeResult = this.range(collection, fqSet, facetField);
            if (rangeResult == null) {
                return EchartsEntity.empty();
            }

            List<RangeEntity> rangeList = this.range(rangeResult, limit, gapType);
            if (rangeList == null || rangeList.size() == 0) {
                return EchartsEntity.empty();
            } else if (rangeList.size() == 1) {
                //直接分组
                return rangeByNone(collection, facetField, fqSet, limit, rangeResult);
            }

            if ("eq".equals(gapType)) {
                // 此方法使用type:range效率更高，但可以rangeByList替换
                return this.rangeByEqGap(collection, facetField, fqSet, rangeList, rangeResult);
            } else {
                return this.rangeByList(collection, facetField, fqSet, rangeList, rangeResult);
            }


        } else {
            //直接分组
            return rangeByNone(collection, facetField, fqSet, limit, null);
        }


    }

    // 均等份范围分组
    private EchartsEntity rangeByEqGap(String collection, String facetField, Set<String> fqSet, List<RangeEntity> rangeList, RangeResult rangeResult) throws Exception {
        Double min = rangeResult.getMin();
        Double max = rangeResult.getMax();
        Double avg = rangeResult.getAvg();
        Double mode = rangeResult.getMode();

        Double mod = Double.parseDouble(String.format("%.2f", (max - min) / rangeList.size()));

        SolrQuery solrQuery = new SolrQuery("*:*");
        if (fqSet != null) {
            fqSet.forEach(solrQuery::addFilterQuery);
        }
        solrQuery.setRows(0);
        // 构造json.facet
        StringBuilder sb = new StringBuilder("{");
        // 范围区间
        sb.append("range:{type: range,field :").append(facetField);
        sb.append(",start:").append(min).append(",end:").append(max).append(",gap:").append(mod);
        sb.append("}");
        // 结尾
        sb.append("}");
        solrQuery.add("json.facet", sb.toString());
        // x轴数据
        int index = 0;
        int len = rangeList.size();
        String[] xAxis = new String[len];
        for (RangeEntity entity : rangeList) {
            xAxis[index] = entity.getAxis();
            index++;
        }
        sb.append("}");
        QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
        NestableJsonFacet nestableJsonFacet = queryResponse.getJsonFacetingResponse();
        List<BucketJsonFacet> rangeBuckets = nestableJsonFacet.getBucketBasedFacets("range").getBuckets();
        // y轴数据
        List<EchartsSeriesEntity> series = new ArrayList<EchartsSeriesEntity>();
//        EchartsSeriesEntity seriesEntity = new EchartsSeriesEntity(config.getColChnName());
        EchartsSeriesEntity seriesEntity = new EchartsSeriesEntity("病例数");
        for (BucketJsonFacet bucket : rangeBuckets) {
            long count = bucket.getCount();
            seriesEntity.add(count);
        }
        EchartsEntity echart = new EchartsEntity();
//        echart.setLegend(new String[]{config.getColChnName()});
//        echart.setLegend(new String[]{"病例数"});
        // 图形坐标
        series.add(seriesEntity);
        echart.setxAxis(xAxis);
        echart.setSeries(series);
        // 平均数
        echart.setAvg(BigDecimal.valueOf(avg).setScale(2, BigDecimal.ROUND_HALF_UP));
        // 中位数
        echart.setMedian(BigDecimal.valueOf(rangeResult.getMedian()).setScale(2, BigDecimal.ROUND_HALF_UP));
        // 众数
        echart.setMode(BigDecimal.valueOf(mode).setScale(2, BigDecimal.ROUND_HALF_UP));
        echart.setCount(rangeResult.getCount());
        return echart;
    }

    // 随意分配各部分大小分组
    private EchartsEntity rangeByList(String collection, String facetField, Set<String> fqSet, List<RangeEntity> rangeList, RangeResult rangeResult) throws Exception {

        Double avg = rangeResult.getAvg();
        Double mode = rangeResult.getMode();

        SolrQuery solrQuery = new SolrQuery("*:*");
        if (fqSet != null) {
            fqSet.forEach(solrQuery::addFilterQuery);
        }
        solrQuery.setRows(0);
        // 构造json.facet
        StringBuilder sb = new StringBuilder("{");
        // 范围区间
        int rangeIndex = 0, rangeSize = rangeList.size();
        for (RangeEntity range : rangeList) {
            sb.append("\"").append(range.getAxis()).append("\":{query:\"")
                    .append(facetField)
                    .append(":[").append(range.getMin()).append(" TO ").append(range.getMax())
                    .append(++rangeIndex == rangeSize ? "]" : "}")
                    .append("\"},");
        }
        // 结尾
        sb.append("}");
        solrQuery.add("json.facet", sb.toString());
        // x轴数据
        int index = 0;
        int len = rangeList.size();
        String[] xAxis = new String[len];
        for (RangeEntity entity : rangeList) {
            xAxis[index] = entity.getAxis();
            index++;
        }
        QueryResponse queryResponse = SolrUtil.call(solrQuery, collection);
        NestableJsonFacet rangeFacet = queryResponse.getJsonFacetingResponse();
        // y轴数据
        List<EchartsSeriesEntity> series = new ArrayList<EchartsSeriesEntity>();
        EchartsSeriesEntity seriesEntity = new EchartsSeriesEntity("病例数");
        for (RangeEntity range : rangeList) {
            long count = rangeFacet.getQueryFacet(range.getAxis()).getCount();
            seriesEntity.add(count);
        }
        EchartsEntity echart = new EchartsEntity();
        // 图形坐标
        series.add(seriesEntity);
        echart.setxAxis(xAxis);
        echart.setSeries(series);
        // 平均数
        echart.setAvg(BigDecimal.valueOf(avg).setScale(2, BigDecimal.ROUND_HALF_UP));
        echart.setMedian(BigDecimal.valueOf(rangeResult.getMedian()).setScale(2, BigDecimal.ROUND_HALF_UP));
        echart.setMode(BigDecimal.valueOf(mode).setScale(2, BigDecimal.ROUND_HALF_UP));
        echart.setCount(rangeResult.getCount());

        return echart;
    }

    // 直接分组统计，不设范围分组
    private EchartsEntity rangeByNone(String collection, String facetField, Set<String> fqSet, int limit, RangeResult rangeResult) throws Exception {
        List<ReportFacetBucketField> list = singleDim(collection, fqSet, facetField, limit);
        if (list == null || list.size() == 0) {
            return EchartsEntity.empty();
        }
        // x轴数据
        String[] xAxis = new String[list.size()];
        // y轴数据
        EchartsSeriesEntity seriesEntity = new EchartsSeriesEntity("病例数");
        int index = 0;
        for (ReportFacetBucketField bucketField : list) {
            xAxis[index++] = bucketField.getField();
            seriesEntity.add(bucketField.getValue().longValue());
        }
        List<EchartsSeriesEntity> series = new ArrayList<EchartsSeriesEntity>();
        series.add(seriesEntity);


        EchartsEntity echart = new EchartsEntity();
        echart.setxAxis(xAxis);
        echart.setSeries(series);

        if (rangeResult == null) {
            // 统计平均数，众数
            List<Long> data = echart.getSeries().get(0).getData();
            /*BigDecimal total = new BigDecimal(0);
            long totalCount = 0;
            int maxIndex = 0;
            long maxCount = -1;
            for(int i = 0,len = xAxis.length; i < len; i++){
                long count = data.get(i);
                if(count > maxCount){
                    maxCount = count;
                    maxIndex = i;
                }
                totalCount += count;
                total = total.add(new BigDecimal(Double.valueOf(xAxis[i]) * count));
            }
            echart.setAvg(total.divide(new BigDecimal(totalCount),2, BigDecimal.ROUND_HALF_UP).setScale(0, BigDecimal.ROUND_UP));
            echart.setMode(new BigDecimal(xAxis[maxIndex]));
            echart.setCount(totalCount);*/
            echart.setCount(data.stream().reduce(Long::sum).orElse(0L));
        } else {
            echart.setAvg(BigDecimal.valueOf(rangeResult.getAvg()));
            echart.setMode(BigDecimal.valueOf(rangeResult.getMode()));
            echart.setMedian(BigDecimal.valueOf(rangeResult.getMedian()).setScale(2, BigDecimal.ROUND_HALF_UP));
            echart.setCount(rangeResult.getCount());
        }

        return echart;
    }

    private BigDecimal getSolrCurMedian(Set<String> fqSet, String facetField, List<BucketJsonFacet> rangeBuckets) throws Exception {
        Long totalCount = rangeBuckets.stream()
                .map(BucketJsonFacet::getCount)
                .reduce(Long::sum).orElse(0L);
        long medianIndex;
        int medianNum;
        if (totalCount % 2 == 0) {
            medianIndex = totalCount / 2;
            medianNum = 2;
        } else {
            medianIndex = (totalCount + 1) / 2;
            medianNum = 1;
        }

        String[] rangeArray = {"*", "*"};
        long countAdd = 0, curIndex = 0;
        for (int i = 0, len = rangeBuckets.size(); i < len; i++) {
            long count = rangeBuckets.get(i).getCount();
            countAdd += count;
            if (countAdd >= medianIndex) {
                // 在这个区间的坐标
                curIndex = medianIndex - countAdd + count;
                rangeArray[0] = rangeBuckets.get(i).getVal().toString();
                if (i + 1 < len) {
                    rangeArray[1] = rangeBuckets.get(i + 1).getVal().toString();
                }
                if (medianNum == 2 && medianIndex == countAdd) {
                    rangeArray[1] = i + 2 == len ? rangeBuckets.get(i + 1).getVal().toString() : rangeBuckets.get(i + 2).getVal().toString();
                }
                break;
            }
        }

        SolrQuery solrQuery = new SolrQuery("*:*");
        if (fqSet != null) {
            fqSet.forEach(solrQuery::addFilterQuery);
        }
        solrQuery.addFilterQuery(facetField + ":[" + rangeArray[0] + " TO " + rangeArray[1] + "}");
        solrQuery.setFields(facetField);
        solrQuery.addSort(facetField, SolrQuery.ORDER.asc);//根据值排序
        solrQuery.addSort("id", SolrQuery.ORDER.asc);//根据主键排序
        solrQuery.setStart((int) curIndex);
        solrQuery.setRows(medianNum);
        SolrDocumentList list = SolrUtil.call(solrQuery, EngineUtil.DWB_MASTER_INFO).getResults();
        ;

        BigDecimal medianTotal = new BigDecimal(0);
        for (SolrDocument doc : list) {
            medianTotal = medianTotal.add(new BigDecimal(doc.getFieldValue(facetField).toString()));
        }

        return medianTotal.divide(BigDecimal.valueOf(medianNum));
		/*long endIndex = curIndex + 1;
		if(endIndex % 10 > 0){
			endIndex = endIndex / 10 * 10;
		}
		String cursorMark = CursorMarkParams.CURSOR_MARK_START;//游标初始化
		int totalRow = 0;
		SolrDocumentList resultList;
		do {
			solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);//变化游标条件
			QueryResponse rsp = SolrUtil.call(solrQuery,EngineUtil.DWB_MASTER_INFO);//执行多次查询读取
			String nextCursorMark = rsp.getNextCursorMark();//获取下次游标

			resultList = rsp.getResults();
			totalRow += resultList.size();
			//如果两次游标一样，说明数据拉取完毕，可以结束循环了
			if (cursorMark.equals(nextCursorMark)) {
				done = true;
			}
			cursorMark = nextCursorMark;
		}while(totalRow > cursorIndex );*/

    }

    public List<ReportFacetBucketField> singleDim(String collection, Set<String> fqSet, String facetField, int limit) throws Exception {
        SolrQuery query = new SolrQuery();
        // 设定查询字段
        String q = "*:*";
        query.add("q", q);
        if (fqSet != null) {
            fqSet.forEach(query::addFilterQuery);
        }
        query.setRows(0);
        JSONObject facetJsonMap = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "terms");
        jsonObject.put("limit", limit);
        jsonObject.put("field", facetField);
        facetJsonMap.put("categories", jsonObject);

        String facetJson = JSON.toJSONString(facetJsonMap);

        query.set("json.facet", facetJson);
        query.setFacet(true);
        query.setFacetLimit(10);
        try {
            List<ReportFacetBucketField> result = null;
            QueryResponse queryResponse = SolrUtil.call(query, collection);
            NamedList<Object> namedList = queryResponse.getResponse();
            NamedList<Object> facetsMap = (NamedList<Object>) namedList.get("facets");
            long count = Long.parseLong(facetsMap.get("count").toString());
            if (count == 0) {
                return null;
            }
            NamedList<Object> categoriesMap = (NamedList<Object>) facetsMap.get("categories");
            List<NamedList<Object>> list = (List<NamedList<Object>>) categoriesMap.get("buckets");
            if (list != null) {
                result = new ArrayList<ReportFacetBucketField>();
                for (NamedList<Object> bean : list) {
                    String field = bean.get("val").toString();
                    BigDecimal value = new BigDecimal(bean.get("count").toString());
                    ReportFacetBucketField facetBucketField = new ReportFacetBucketField(field, value);
                    result.add(facetBucketField);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("solr exception=", e);
            throw new EngineBizException("solr查询失败：" + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        EngineServiceImpl service = new EngineServiceImpl();
//		List<RangeEntity> list = service.range(null, "YEARAGE", 10);
	/*	log.info("size:"+list.size());
		log.info("range:"+list.toString());*/
    }

    @Override
    public IPage<MedicalCaseVO> compare(IPage<MedicalCaseVO> page, CompareCaseFlowDTO dto) throws Exception {
        // 节点查询条件
        String[] conditionArray = parseCompareCaseFlowCondition(dto);
        SolrQuery query = new SolrQuery();
        // 设定查询字段
        String q = "*:*";
        query.add("q", q);

        if (EngineUtil.COMPARE_UNION.equals(dto.getCompareType())) {
            //并集
            String condition = conditionArray[0] + " OR " + conditionArray[1];
            query.addFilterQuery(condition);
        } else {
            query.addFilterQuery(conditionArray[0]);
            if (EngineUtil.COMPARE_INTERSECT.equals(dto.getCompareType())) {
                //交集
                query.addFilterQuery(conditionArray[1]);
            } else {
                //差集
                query.addFilterQuery("-" + conditionArray[1]);
            }
        }
        query.setStart((int) page.offset());
        query.setRows((int) page.getSize());
        QueryResponse queryResponse = SolrUtil.call(query, EngineUtil.DWB_MASTER_INFO);
        SolrDocumentList documents = queryResponse.getResults();
        List<MedicalCaseVO> result = new ArrayList<MedicalCaseVO>();

        for (SolrDocument doc : documents) {
            MedicalCaseVO vo = SolrUtil.solrDocumentToPojo(doc, MedicalCaseVO.class, EngineUtil.FIELD_MAPPING);
            result.add(vo);
        }
        page.setRecords(result);
        page.setTotal(documents.getNumFound());
        return page;
    }

    /**
     * 功能描述：两个节点交集、差集的查询条件
     *
     * @param dto
     * @return
     * @throws Exception <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     * @author zhangly
     * <p>创建日期 ：2019年12月5日 上午11:39:12</p>
     */
    private String[] parseCompareCaseFlowCondition(CompareFlowDTO dto) throws Exception {
        if (StringUtils.isBlank(dto.getFlowJson())) {
            throw new EngineBizException("未传递流程节点！");
        }
        if (StringUtils.isBlank(dto.getRules())) {
            throw new EngineBizException("未传递节点参数！");
        }
        if (StringUtils.isBlank(dto.getCompareFlowJson())) {
            throw new EngineBizException("未传递第二个流程节点！");
        }
        if (StringUtils.isBlank(dto.getCompareRules())) {
            throw new EngineBizException("未传递第二个节点参数！");
        }
        //流程节点列表
        List<EngineNode> nodeList = this.parseFlowJson(dto.getFlowJson());
        if (nodeList == null || nodeList.size() == 0) {
            throw new EngineBizException("未传递流程节点！");
        }
        //节点规则列表
        List<EngineNodeRule> ruleList = this.parseNodeRule(dto.getRules());
        if (ruleList == null || ruleList.size() == 0) {
            throw new EngineBizException("未传递节点参数！");
        }
        nodeList = parseParentAndRule(nodeList, ruleList);
        // 节点查询条件
        Set<String> fqSet = this.constructConditionExpression(nodeList);

        //第二个流程节点列表
        List<EngineNode> compareNodeList = this.parseFlowJson(dto.getCompareFlowJson());
        if (compareNodeList == null || compareNodeList.size() == 0) {
            throw new EngineBizException("未传递第二个流程节点！");
        }
        //第二个节点规则列表
        List<EngineNodeRule> compareRuleList = JSON.parseArray(dto.getCompareRules(), EngineNodeRule.class);
        if (compareRuleList == null || compareRuleList.size() == 0) {
            throw new EngineBizException("未传递第二个节点参数！");
        }
        for (EngineNodeRule rule : compareRuleList) {
            MedicalColConfig config = dictSV.queryMedicalColConfig(rule.getTableName(), rule.getColName());
            if (config != null) {
                rule.setColConfig(config);
            }
        }
        compareNodeList = parseParentAndRule(compareNodeList, compareRuleList);
        // 第二个节点查询条件
        Set<String> compareFqSet = this.constructConditionExpression(compareNodeList);

        String[] conditionArray = new String[2];
        StringBuilder condition = new StringBuilder();
        condition.append("(");
        int index = 0;
        for (String fq : fqSet) {
            if (index > 0) {
                condition.append(" AND ");
            }
            condition.append(fq);
            index++;
        }
        condition.append(")");
        conditionArray[0] = condition.toString();
        condition.setLength(0);
        condition.append("(");
        index = 0;
        for (String fq : compareFqSet) {
            if (index > 0) {
                condition.append(" AND ");
            }
            condition.append(fq);
            index++;
        }
        condition.append(")");
        conditionArray[1] = condition.toString();
        return conditionArray;
    }

    @Override
    public EchartsEntity echart(EchartCompareCaseFlowDTO dto) throws Exception {
        String facetField = dto.getFacetField();
        // 两个节点的查询条件
        String[] conditionArray = parseCompareCaseFlowCondition(dto);
        Set<String> fqSet = new HashSet<String>();
        if (EngineUtil.COMPARE_UNION.equals(dto.getCompareType())) {
            //并集
            String condition = conditionArray[0] + " OR " + conditionArray[1];
            fqSet.add(condition);
        } else {
            fqSet.add(conditionArray[0]);
            if (EngineUtil.COMPARE_INTERSECT.equals(dto.getCompareType())) {
                //交集
                fqSet.add(conditionArray[1]);
            } else {
                //差集
                fqSet.add("-" + conditionArray[1]);
            }
        }
        return this.echart(EngineUtil.DWB_MASTER_INFO, facetField, dto.getMin(), dto.getMax(), fqSet, 10, dto.getGapType());
    }

    @Override
    public List<List<EngineNode>> queryFormalEngineNode(String caseId) {
        List<EngineNode> nodeList = caseSV.recursionMedicalFormalFlowByCaseid(caseId);
        List<EngineNodeRule> ruleList = caseSV.queryMedicalFormalFlowRuleByCaseid(caseId);
        EngineNodeResolver resolver = EngineNodeResolver.getInstance();
        return resolver.parseMultFlow(nodeList, ruleList);
    }

    @Override
    public List<List<EngineNode>> queryHisFormalEngineNode(String caseId, String batchId) {
    	//oracle使用递归树查找流程节点
        //List<EngineNode> nodeList = caseSV.recursionMedicalFormalFlowByCaseid(caseId, batchId);
    	//mysql
    	List<EngineNode> nodeList = caseSV.queryHisMedicalFormalFlow(caseId, batchId);
        List<EngineNode> endNodeList = nodeList.stream().filter(s->"end".equals(s.getNodeType())).collect(Collectors.toList());
        Map<String, EngineNode> nodeMap = new HashMap<String, EngineNode>();
    	for(EngineNode node : nodeList) {
    		nodeMap.put(node.getNodeCode(), node);
    	}
    	List<EngineNode> result = new ArrayList<EngineNode>();
    	for(EngineNode node : endNodeList) {
    		recursionMedicalFormalFlow(result, nodeMap, node);
    	}
    	nodeList.clear();
    	nodeList = result;
    	List<EngineNodeRule> ruleList = caseSV.queryMedicalFormalFlowRuleByCaseid(caseId, batchId);
        EngineNodeResolver resolver = EngineNodeResolver.getInstance();
        return resolver.parseMultFlow(nodeList, ruleList);
    }
    
    private void recursionMedicalFormalFlow(List<EngineNode> result, Map<String, EngineNode> nodeMap, EngineNode node) {
    	result.add(node);
    	if(StringUtils.isNotBlank(node.getPrevNodeCode()) && nodeMap.containsKey(node.getPrevNodeCode())) {
    		EngineNode prev = nodeMap.get(node.getPrevNodeCode());
    		recursionMedicalFormalFlow(result, nodeMap, prev);
    	}
    }
    
    @Override
    public List<EngineNodeRule> queryHisFormalEngineNodeRule(String caseId, String batchId) {
    	List<EngineNode> nodeList = caseSV.queryHisMedicalFormalFlow(caseId, batchId);
        List<EngineNodeRule> ruleList = caseSV.queryMedicalFormalFlowRuleByCaseid(caseId, batchId);
        for(EngineNode node : nodeList) {
        	if(node.getNodeType().contains("_v")) {
            	//模板节点
        		List<EngineNodeRule> tempRuleList = caseSV.queryMedicalFormalFlowRuleByTmpl(node.getParamCode(), node.getNodeCode());
        		if(tempRuleList!=null && tempRuleList.size()>0) {
        			ruleList.addAll(tempRuleList);
        		}
            }
        }
        return ruleList;
    }

    /**
     * 功能描述：构造模型流程查询条件，多个之间是或关系
     *
     * @param flowList
     * @return
     * @throws Exception <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     * @author zhangly
     * <p>创建日期 ：2019年12月9日 下午2:39:07</p>
     */
    @Override
    public String[] constructFormalCaseCondition(List<List<EngineNode>> flowList) throws Exception {
        String[] result = new String[flowList.size()];
        // 节点查询条件
        Set<String> fqSet = null;
        for (int i = 0, len = flowList.size(); i < len; i++) {
            List<EngineNode> nodeList = flowList.get(i);
            fqSet = this.constructConditionExpression(nodeList);
            StringBuilder condition = new StringBuilder();
            condition.append("(");
            int index = 0;
            for (String fq : fqSet) {
                if (index > 0) {
                    condition.append(" AND ");
                }
                condition.append(fq);
                index++;
            }
            condition.append(")");
            result[i] = condition.toString();
        }
        return result;
    }


}
