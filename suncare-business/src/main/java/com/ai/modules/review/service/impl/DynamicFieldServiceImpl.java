package com.ai.modules.review.service.impl;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.IdUtils;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.dto.DynamicLinkProp;
import com.ai.modules.review.runnable.EngineFunctionRunnable;
import com.ai.modules.review.service.IDynamicFieldService;
import com.ai.modules.task.entity.TaskActionFieldCol;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Auther: zhangpeng
 * @Date: 2021/3/23 15
 * @Description:
 */
@Slf4j
@Service
public class DynamicFieldServiceImpl implements IDynamicFieldService {


    @Override
    public void saveExtFieldValue(SolrQuery solrQuery, String collection, Map<String, Set<String>> tabFieldMap, String[][] extFieldMap, Consumer<Integer> updateProcess) throws Exception {

        DynamicLinkProp dynamicLinkProp = this.initLinkProp(tabFieldMap);
        // 添加反查关联字段
        Set<String> resultFieldSet = dynamicLinkProp.getLinkFieldSet();
        resultFieldSet.add("id");
        // 查询并多线程写入solr修改文件
        this.updateCollectionValue(collection, solrQuery.getFilterQueries(), resultFieldSet.toArray(new String[0])
                , dynamicLinkProp, (doc) -> {
                    JSONObject cacheJson = new JSONObject();
                    for (String[] array : extFieldMap) {
                        Object val = doc.getFieldValue(array[1]);
                        if (val == null || val.toString().length() == 0) {
                            val = "null";
                        }
                        cacheJson.put(array[0], SolrUtil.initActionValue(val, "set"));
                    }
                    cacheJson.put("id", doc.getFieldValue("id"));
                    return cacheJson;
                }, updateProcess);
    }


    @Override
    public void saveBreakStateValue(String collection, SolrQuery solrQuery, Map<String, Set<String>> tabFieldMap,
                                    List<TaskActionFieldCol> colList, String[] nodes, Consumer<Integer> updateProcess) throws Exception {
        Set<String> resultFieldSet = tabFieldMap.remove(collection);
        if (resultFieldSet == null) {
            resultFieldSet = new HashSet<>();
        }
        DynamicLinkProp dynamicLinkProp = this.initLinkProp(tabFieldMap);
        // 添加反查关联字段
        resultFieldSet.addAll(dynamicLinkProp.getLinkFieldSet());
        resultFieldSet.add("id");
        // 模板需要赋值的字段名
        String[] colFields = colList.stream().map(r -> collection.equals(r.getTableName())
                ?r.getColName():(r.getTableName() + "." + r.getColName())).toArray(String[]::new);
        // 输出写入文件的json
        JSONObject cacheJson = new JSONObject();
        // 查询并多线程写入solr修改文件
        this.updateCollectionValue(collection, solrQuery.getFilterQueries(), resultFieldSet.toArray(new String[0]), dynamicLinkProp
                , (doc) -> {
                    StringBuilder value = new StringBuilder();
                    int colIndex = 0;
                    for (String node : nodes) {
                        if(node == null){
                            value.append("【").append(doc.getFieldValue(colFields[colIndex++])).append("】");
                        } else {
                            value.append(node);
                        }
                    }
                    cacheJson.put("BREAK_STATE", SolrUtil.initActionValue(value, "set"));
                    cacheJson.put("id", doc.getFieldValue("id"));
                    return cacheJson;
                }, updateProcess);

    }

    // 分组统计使用
    @Override
    public Set<String> getFromOtherField(Map<String, Set<String>> tabFieldMap) {
        String[] defaultArray = {null};

        Set<String> fields = tabFieldMap.keySet().stream().map(extraCollection ->
                DynamicFieldConstant.GROUP_SINGLE_FIELD_LINK.getOrDefault(extraCollection, defaultArray)[0]
        ).filter(Objects::nonNull).collect(Collectors.toSet());

        return fields;
    }

    @Override
    public void addGroupAttrFromOther(List<Map<String, Object>> resultList, Map<String, Set<String>> tabFieldMap) throws Exception {

        Map<String, Map<String, List<Map<String, Object>>>> fieldResultMap = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : tabFieldMap.entrySet()) {
            String extraCollection = entry.getKey();
            String[] linkArray = DynamicFieldConstant.GROUP_SINGLE_FIELD_LINK.get(extraCollection);
            if (linkArray == null) {
                continue;
            }
            String linkField = linkArray[1];
            String groupField = linkArray[0];
            Map<String, List<Map<String, Object>>> resultMap = fieldResultMap.get(groupField);
            if (resultMap == null) {
                resultMap = this.initResultsMap(resultList, groupField);
                fieldResultMap.put(groupField, resultMap);
            }

            Set<String> vals = resultMap.keySet();
            if(vals.size() == 0){
                continue;
            }

            String linkFq = linkField + ":(\"" + StringUtils.join(vals, "\",\"") + "\")";


            Set<String> fieldList = entry.getValue();
            SolrQuery extraQuery = new SolrQuery("*:*");
            extraQuery.addFilterQuery(linkFq);
            extraQuery.setFields(fieldList.toArray(new String[0]));
            extraQuery.addField(linkField);
            SolrDocumentList documents = SolrQueryGenerator.list(extraCollection, extraQuery);
            for (SolrDocument doc : documents) {
                String groupByVal = doc.get(linkField).toString();
                List<Map<String, Object>> beans = resultMap.get(groupByVal);
                for (Map.Entry<String, Object> docEntry : doc.entrySet()) {
                    for (Map<String, Object> bean : beans) {
                        String fieldName = extraCollection + "." + docEntry.getKey();
                        Object oldVal = bean.get(fieldName);
                        Object val = docEntry.getValue();
                        if (oldVal == null) {
                            bean.put(fieldName, val);
                        } else {
                            if (!DynamicFieldConstant.NOT_MULTI_VALUE.contains(extraCollection)) {
                                Set<String> valSet = new HashSet<>(Arrays.asList((oldVal + "," + val).split(",")));
                                bean.put(fieldName, StringUtils.join(valSet, ","));
                            }
                        }
                    }

                }

            }
        }

    }

    @Override
    public Map<String, SolrDocumentList> listDynamicResult(Map<String, Set<String>> tabFieldMap, SolrQuery solrQuery) throws Exception {
        Map<String, SolrDocumentList> map = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : tabFieldMap.entrySet()) {
            String extraCollection = entry.getKey();
            solrQuery.setFields(entry.getValue().toArray(new String[0]));
            SolrDocumentList documents = SolrQueryGenerator.list(extraCollection, solrQuery);
            map.put(extraCollection, documents);
        }

        return map;
    }

    @Override
    public DynamicLinkProp initLinkProp(Map<String, Set<String>> tabFieldMap) throws Exception {
        return DynamicFieldConstant.initLinkProp(tabFieldMap);
    }


    @Override
    public void addAttrFromOther(List<SolrDocument> list,
                                 DynamicLinkProp dynamicLinkProp) throws Exception {
        if (dynamicLinkProp.getSingleLinkMap().size() > 0) {
            this.addAttrFromSingleField(dynamicLinkProp.getSingleLinkMap(), list);
        }

        if (dynamicLinkProp.getMultiLinkMap().size() > 0) {
            this.addAttrFromMultiField(dynamicLinkProp.getMultiLinkMap(), list);
        }
    }

    private void addAttrFromSingleField(List<Map.Entry<String, Set<String>>> tabFieldMap, List<SolrDocument> dataList) throws Exception {

        SolrQuery extraQuery = new SolrQuery("*:*");

        for (Map.Entry<String, Set<String>> entry : tabFieldMap) {
            String extraCollection = entry.getKey();
            String[] linkArray =DynamicFieldConstant.SINGLE_FIELD_LINK.get(extraCollection);

            Map<String, List<SolrDocument>> map = new HashMap<>();
            for (SolrDocument bean : dataList) {
                Object obj = bean.get(linkArray[0]);
                if (obj == null || obj.toString().length() == 0) {
                    continue;
                }
                List<SolrDocument> list = map.computeIfAbsent(obj.toString(), k -> new ArrayList<>());
                list.add(bean);
            }

            if (map.size() == 0) {
                continue;
            }
            StringBuilder sb = new StringBuilder(":(");
            for(String val: map.keySet()){
                sb.append("\"").append(EngineUtil.escapeQueryChars(val)).append("\",");
            }

            String linkFq = sb.substring(0, sb.length() - 1) + ")";

            String linkField = linkArray[1];
            extraQuery.setFields(entry.getValue().toArray(new String[0]));
            extraQuery.addField(linkField);
            extraQuery.setFilterQueries(linkField + linkFq);
            SolrDocumentList documents = SolrQueryGenerator.list(extraCollection, extraQuery);
            for (SolrDocument doc : documents) {
                String linkVal = doc.remove(linkField).toString();

                for (SolrDocument bean : map.get(linkVal)) {
                    for (Map.Entry<String, Object> docEntry : doc.entrySet()) {
                        String fieldName = extraCollection + "." + docEntry.getKey();
                        Object oldVal = bean.get(fieldName);
                        Object val = docEntry.getValue();
                        if (oldVal == null) {
                            bean.put(fieldName, val);
                        } else {
                            if (!DynamicFieldConstant.NOT_MULTI_VALUE.contains(extraCollection)) {
                                Set<String> valSet = new HashSet<>(Arrays.asList((oldVal + "," + val).split(",")));
                                bean.put(fieldName, StringUtils.join(valSet, ","));
                            }

                        }
                    }
                }

            }
        }
    }



    private void addAttrFromMultiField(List<Map.Entry<String, Set<String>>> tabFieldMap, List<SolrDocument> dataList) throws Exception {

        SolrQuery extraQuery = new SolrQuery("*:*");

        for (Map.Entry<String, Set<String>> entry : tabFieldMap) {
            String extraCollection = entry.getKey();

            Map<String, String> linkFieldMap = DynamicFieldConstant.MULTI_FIELD_LINK.get(extraCollection);
            // 主副字段对照顺序统一
            List<String> masterFields = new ArrayList<>();
            List<String> sideFields = new ArrayList<>();
            for (Map.Entry<String, String> entry1 : linkFieldMap.entrySet()) {
                masterFields.add(entry1.getKey());
                sideFields.add(entry1.getValue());
            }

            Map<String, List<SolrDocument>> map = new HashMap<>();
            Set<String> queryStrSet = new HashSet<>();

            int fieldCount = masterFields.size();
            // 遍历结果，每条记录的几个关联字段AND, 记录间OR
            for (SolrDocument bean : dataList) {
                List<String> values = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < fieldCount; i++) {
                    Object obj = bean.get(masterFields.get(i));
                    if (obj == null || obj.toString().length() == 0) {
                        break;
                    }
                    values.add(obj.toString());
                    if (sb.length() > 0) {
                        sb.append(" AND ");
                    }
                    sb.append(sideFields.get(i)).append(":\"")
                            .append(EngineUtil.escapeQueryChars(obj.toString()))
                            .append("\"");
                }
                // 跳过存在关联字段为空的记录
                if (values.size() != masterFields.size()) {
                    continue;
                }

                List<SolrDocument> list = map.computeIfAbsent(StringUtils.join(values, "::"), k -> new ArrayList<>());
                list.add(bean);
                queryStrSet.add("(" + sb.toString() + ")");

            }

            if (map.size() == 0) {
                continue;
            }
            // 副表查询返回字段
            Set<String> fields = entry.getValue();
            Set<String> fieldSet = new HashSet<>(sideFields);
            fieldSet.addAll(fields);
            extraQuery.setFields(fieldSet.toArray(new String[0]));
            extraQuery.addFilterQuery(StringUtils.join(queryStrSet, " OR "));

            SolrDocumentList documents = SolrQueryGenerator.list(extraCollection, extraQuery);
            for (SolrDocument doc : documents) {
                String linkVal = sideFields.stream().map(r -> doc.getFieldValue(r).toString()).collect(Collectors.joining("::"));
                List<SolrDocument> linkResult = map.get(linkVal);
                if (linkResult == null) {
                    continue;
                }
                for (SolrDocument bean : linkResult) {
                    for (String field : fields) {
                        Object val = doc.getFieldValue(field);
                        if (val == null) {
                            continue;
                        }
                        String fieldName = extraCollection + "." + field;
                        Object oldVal = bean.get(fieldName);
                        if (oldVal == null) {
                            bean.put(fieldName, val);
                        } else {
                            if (!DynamicFieldConstant.NOT_MULTI_VALUE.contains(extraCollection)) {
                                Set<String> valSet = new HashSet<>(Arrays.asList((oldVal + "," + val).split(",")));
                                bean.put(fieldName, StringUtils.join(valSet, ","));
                            }
                        }
                    }
                }

            }
        }
    }

    @Override
    public List<String> getSearchFqs(String dynamicSearch) {
        if (dynamicSearch == null || "{}".equals(dynamicSearch)) {
            return new ArrayList<>();
        }
        Map<String, JSONObject> tableSearchMap = DynamicFieldConstant.initTableSearchMap(dynamicSearch);
        return getFqs(tableSearchMap);
    }

    public void updateCollectionValue(String collection, String[] fqs, String[] fls, DynamicLinkProp dynamicLinkProp,
                                      Function<SolrDocument, JSONObject> getWriteJson , Consumer<Integer> updateProcess) throws Exception {

        long totalCount = SolrQueryGenerator.count(collection, fqs);

        String filePath = SolrUtil.importFolder + File.separator + EngineUtil.SOLR_CACHE_DATA + File.separator + System.currentTimeMillis() + "_" + IdUtils.uuid();

        int maxFileCount = 200000;
        int maxSearchCount = 1000;

        int fileCount = (int) (totalCount / maxFileCount);
        if (totalCount % maxFileCount > 0) {
            fileCount++;
        }

        log.info("需要沉淀模板的数据量：" + totalCount + "，文件数：" + fileCount);

        final CountDownLatch count = new CountDownLatch(fileCount);
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(fileCount > 2 ? 3 : fileCount);

        AtomicReference<String> importFileRf = new AtomicReference<>();
        BufferedWriter[] fileWriter = new BufferedWriter[]{null};

        List<SolrDocument> childList = new ArrayList<>(1000);
        SolrUtil.exportDocByPager(fqs, fls, collection, (document, index) -> {
            try {
                childList.add(document);
                if (index % maxFileCount == 0) {
                    // 数据写入xml
                    String importFilePath = filePath + "_" + (index / maxFileCount) + ".json";
                    fileWriter[0] = new BufferedWriter(
                            new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
                    //写文件头
                    fileWriter[0].write("[");
                    importFileRf.set(importFilePath);
                }
                int j = index + 1;
                // 到达反查最大记录数
                if(j % maxSearchCount == 0 || j >= totalCount){
                    log.info("处理数据：" + index + " ~ " + j );
                    // 查询副表字段
                    this.addAttrFromOther(childList, dynamicLinkProp);
                    for (SolrDocument doc : childList) {
                        JSONObject cacheJson = getWriteJson.apply(doc);
                        fileWriter[0].write(cacheJson.toJSONString());
                        fileWriter[0].write(",\n");
                    }
                    childList.clear();
                    if(j % maxFileCount == 0 || j >= totalCount) {
                        // 到达文件最大记录数
                        // 写文件尾，并关闭文件流
                        fileWriter[0].write("]");
                        fileWriter[0].close();
                        // 导入Solr文件线程池
                        int finalFileSize1 = j >= totalCount? (int) (totalCount % maxFileCount) :maxFileCount;
                        String finalImportFilePath = importFileRf.get();
                        fixedThreadPool.submit(new EngineFunctionRunnable(() -> {
                            try {
                                SolrUtil.importJsonToSolr(finalImportFilePath, collection);
                                updateProcess.accept(finalFileSize1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                count.countDown();
                            }
                        }));
                    }
                }
            } catch (Exception e){
                log.error(e.getMessage());
            }

        });

        count.await();
        fixedThreadPool.shutdown();
    }

    private List<String> getFqs(Map<String, JSONObject> tableSearchMap) {
        List<String> fqList = new ArrayList<>();
        // 添加主表条件
        JSONObject actionSearch = tableSearchMap.remove(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        if (actionSearch != null) {
            fqList.addAll(initFqs(actionSearch));
        }

        // 其他表JOIN条件
        for (Map.Entry<String, JSONObject> entry : tableSearchMap.entrySet()) {
            String tableName = entry.getKey();
            Map<String, String> linkFieldMap = DynamicFieldConstant.getLinkFields(tableName);
            List<String> fqs = initFqs(entry.getValue());
            if (fqs.size() == 0) {
                continue;
            }
            String fq = StringUtils.join(fqs, " AND ");
            for (Map.Entry<String, String> linkEntry : linkFieldMap.entrySet()) {
                SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(tableName, linkEntry.getValue(), linkEntry.getKey());
                fqList.add(plugin.parse() + fq);
            }
        }
        return fqList;
    }

    private List<String> initFqs(JSONObject param) {
        List<String> fqList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            String fieldName = entry.getKey();
            Object valueObj = entry.getValue();
            if (valueObj == null || "null".equals(valueObj.toString())) {
                continue;
            }
            String value = valueObj.toString().trim();
            if (value.length() == 0) {
                continue;
            }

            String beginValue = null, endValue = null;
            if (fieldName.endsWith(QueryGenerator.BEGIN)) {
                fieldName = fieldName.substring(0, fieldName.lastIndexOf("_"));
                Object obj = param.replace(fieldName + QueryGenerator.END, null);

                if(value.equals(QueryGenerator.NOT_NULL) || QueryGenerator.NOT_NULL.equals(obj)){
                    value = QueryGenerator.NOT_NULL;
                } else if(value.equals(QueryGenerator.NULL) || QueryGenerator.NULL.equals(obj)){
                    value = QueryGenerator.NULL;
                } else {
                    beginValue = value;
                    endValue = obj == null ? "*" : obj.toString().trim();
                }
            } else if (fieldName.endsWith(QueryGenerator.END)) {
                fieldName = fieldName.substring(0, fieldName.lastIndexOf("_"));
                Object obj = param.replace(fieldName + QueryGenerator.BEGIN, null);
                if(value.equals(QueryGenerator.NOT_NULL) || QueryGenerator.NOT_NULL.equals(obj)){
                    value = QueryGenerator.NOT_NULL;
                } else if(value.equals(QueryGenerator.NULL) || QueryGenerator.NULL.equals(obj)){
                    value = QueryGenerator.NULL;
                } else {
                    endValue = value;
                    beginValue = obj == null ? "*" : obj.toString().trim();
                }
            }

            if (beginValue != null) {
                fqList.add(fieldName + ":[" + beginValue + " TO " + endValue + "]");
                continue;
            }

            QueryRuleEnum rule;
            if(value.startsWith("*") && value.endsWith("*")){
                String newVal = value.substring(1, value.length() - 1);
                rule = SolrQueryGenerator.convert2Rule(newVal);
                if(rule == QueryRuleEnum.EQ){
                    rule = QueryRuleEnum.LIKE;
                    valueObj = SolrQueryGenerator.replaceValue(rule, valueObj);
                } else {
                    valueObj = SolrQueryGenerator.replaceValue(rule, newVal);
                }
            } else {
                rule = QueryRuleEnum.EQ;
                valueObj = SolrQueryGenerator.replaceValue(rule, valueObj);
            }
//            QueryRuleEnum rule = SolrQueryGenerator.convert2Rule(value);
//            valueObj = SolrQueryGenerator.replaceValue(rule, valueObj);
            List<String> queryList = SolrQueryGenerator.getQueryRule(fieldName, rule, valueObj);
            fqList.addAll(queryList);

            /*if(QueryGenerator.NOT_NULL.equals(value)){
                value = "?*";
            } else if (value.startsWith("*") && value.endsWith("*")) {
                value = value.substring(1, value.length() - 1);
                if(QueryGenerator.NOT_NULL.equals(value)){
                    value = "?*";
                } else if(value.startsWith(QueryGenerator.NOT_EQUAL)){

                } else {
                    value = "*" + EngineUtil.escapeQueryChars(value.substring(1, value.length() - 1)) + "*";
                }
            } else if(value.startsWith(QueryGenerator.NOT_EQUAL)){
                fieldName = "-" + fieldName;
                value = value.substring(1);
                if(QueryGenerator.NOT_NULL.equals(value)){
                    value = "?*";
                } else {
                    value = EngineUtil.escapeQueryChars(value);
                }

            }*/
        }
        return fqList;

    }

    private static QueryRuleEnum convert2Rule(Object value) {
        QueryRuleEnum rule = null;

        String val = value.toString();

        if(QueryGenerator.NOT_NULL.equals(val)){
            rule = QueryRuleEnum.NOT_NULL;
        } else if (val.startsWith(QueryGenerator.STAR) && val.endsWith(QueryGenerator.STAR)) {
            val = val.substring(1, val.length() - 1);
            rule = convert2Rule(val);
            if(rule == null){
                rule = QueryRuleEnum.LIKE;
            }
        } else if(val.startsWith(QueryGenerator.NOT_EQUAL)){
            rule = QueryRuleEnum.NE;

        }
        return rule;
    }

    private Map<String, List<Map<String, Object>>> initResultsMap(List<Map<String, Object>> buckets, String field) {
        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        for (int i = 0, len = buckets.size(); i < len; i++) {
            Map<String, Object> json = buckets.get(i);
            Object val = json.get(field);
            if (val != null) {
                map.computeIfAbsent(val.toString(), k -> new ArrayList<>()).add(json);
            }
        }
        return map;
    }






}
