package com.ai.modules.review.service.impl;

import com.ai.common.utils.ExportXUtils;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.service.IReviewStatisticService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.solr.client.solrj.SolrQuery;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: zhangpeng
 * @Date: 2021/7/21 09
 * @Description:
 */
@Service
@Slf4j
public class ReviewStatisticServiceImpl implements IReviewStatisticService {


    @Autowired
    IMedicalActionDictService medicalActionDictService;

    @Override
    public JSONObject module0Data(SolrQuery solrQuery, String collection) throws Exception {
        // 第一层统计总
        JSONObject json = new JSONObject();
        json.put("visitidCount", "unique(VISITID)");
        json.put("itemcodeCount", "unique(ITEMCODE)");
        json.put("sumMinMoney", "sum(MIN_MONEY)");
        json.put("sumActionMoney", "sum(ACTION_MONEY)");
        // 第二层查询已初审和已复审
        JSONObject json1 = new JSONObject();
        json1.put("type", "query");
        json1.put("q", "PUSH_STATUS:1");
        json.put("facet1", json1);

        JSONObject json2 = new JSONObject();
        json2.put("type", "query");
        json2.put("q", "SEC_PUSH_STATUS:1");
        json.put("facet2", json2);
        // facet
        // 查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json.toJSONString());
        long count = jsonObject.getLongValue("count");
        if (count > 0) {
            JSONObject facet1 = (JSONObject) jsonObject.remove("facet1");
            JSONObject facet2 = (JSONObject) jsonObject.remove("facet2");

            jsonObject.put("pushCount", facet1.get("count"));
            jsonObject.put("secPushCount", facet2.get("count"));
        }

        return jsonObject;

    }

    @Override
    public JSONObject module1Data(SolrQuery solrQuery, String collection) throws Exception {
        // 完成判定记录数
        JSONObject json1 = new JSONObject();
        json1.put("query", "PUSH_STATUS:1");
        JSONObject json2 = new JSONObject();
        JSONObject json21 = new JSONObject();
        json21.put("visitidCount", "unique(VISITID)");
        json21.put("itemcodeCount", "unique(ITEMCODE)");
//        json21.put("sumMaxMoney", "sum(MAX_MONEY)");
        json21.put("sumMinMoney", "sum(MIN_MONEY)");

        json2.put("type", "query");
        json2.put("q", "-PUSH_STATUS:1");
        json2.put("facet", json21);
        JSONObject json = new JSONObject();
        json.put("push", json1);
        json.put("unPush", json2);
        // facet
        // 查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json.toJSONString());

        if(jsonObject.getLongValue("count") > 0){
            JSONObject pushJson = (JSONObject)jsonObject.remove("push");
            JSONObject unPushJson = (JSONObject)jsonObject.remove("unPush");
            for(String key: pushJson.keySet()){
                jsonObject.put("push_" + key , pushJson.get(key));
            }
            for(String key: unPushJson.keySet()){
                jsonObject.put("unPush_" + key , unPushJson.get(key));
            }
        }

        return jsonObject;
    }

    @Override
    public JSONObject module2Data(SolrQuery solrQuery, String collection, String secReviewStatus) throws Exception {
        JSONObject json11 = new JSONObject();
        // 有判定结果过滤的情况下多加一层
        if (StringUtils.isNotBlank(secReviewStatus)) {
            // 无条件统计层
            json11.put("totalVisitidCount", "unique(VISITID)");
            json11.put("totalSumMinMoney", "sum(MIN_MONEY)");
            // 当前条件统计层
            JSONObject json1111 = new JSONObject();
            json1111.put("visitidCount", "unique(VISITID)");
            json1111.put("itemcodeCount", "unique(ITEMCODE)");
            json1111.put("sumMinMoney", "sum(MIN_MONEY)");
            json1111.put("sumActionMoney", "sum(ACTION_MONEY)");
            // 当前条件过滤层
            JSONObject json111 = new JSONObject();
            json111.put("type", "query");
            json111.put("q", "SEC_REVIEW_STATUS:" + secReviewStatus);
            json111.put("facet", json1111);

            json11.put("facet", json111);
        } else {
            // 当前条件统计层
            json11.put("visitidCount", "unique(VISITID)");
            json11.put("itemcodeCount", "unique(ITEMCODE)");
            json11.put("sumMinMoney", "sum(MIN_MONEY)");
            json11.put("sumActionMoney", "sum(ACTION_MONEY)");
        }

        // facet查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json11.toJSONString());

        log.info(jsonObject.toJSONString());
        // 展开里层
        if (StringUtils.isNotBlank(secReviewStatus)) {
            Object count = jsonObject.remove("count");
            jsonObject.put("totalCount", count);
            if(count != null && Long.parseLong(count.toString()) > 0){
                jsonObject.putAll((JSONObject) jsonObject.remove("facet"));
                double visitidRatio = jsonObject.getLongValue("visitidCount") * 100.0 / jsonObject.getLongValue("totalVisitidCount");
                jsonObject.put("visitidRatio", (double)Math.round(visitidRatio * 100) / 100 + "%");
                double countRatio = jsonObject.getLongValue("count") * 100.0 / jsonObject.getLongValue("totalCount");
                jsonObject.put("countRatio", (double)Math.round(countRatio * 100) / 100 + "%");
                double sumMinMoneyRatio = jsonObject.getLongValue("sumMinMoney") * 100.0 / jsonObject.getLongValue("totalSumMinMoney");
                jsonObject.put("sumMinMoneyRatio", (double)Math.round(sumMinMoneyRatio * 100) / 100 + "%");
            }

        }
        return jsonObject;
    }

    @Override
    public JSONArray module3Data(SolrQuery solrQuery, String collection, String secReviewStatus) throws Exception {

        JSONObject json11 = new JSONObject();
        // 有判定结果过滤的情况下多加一层
        if (StringUtils.isNotBlank(secReviewStatus)) {
            // 无条件统计层
            json11.put("totalVisitidCount", "unique(VISITID)");
            json11.put("totalSumMinMoney", "sum(MIN_MONEY)");
            // 当前条件统计层
            JSONObject json1111 = new JSONObject();
            json1111.put("visitidCount", "unique(VISITID)");
            json1111.put("itemcodeCount", "unique(ITEMCODE)");
            json1111.put("sumMinMoney", "sum(MIN_MONEY)");
            json1111.put("sumActionMoney", "sum(ACTION_MONEY)");
            // 当前条件过滤层
            JSONObject json111 = new JSONObject();
            json111.put("type", "query");
            json111.put("q", "SEC_REVIEW_STATUS:" + secReviewStatus);
            json111.put("facet", json1111);

            json11.put("facet", json111);
        } else {
            // 当前条件统计层
            json11.put("visitidCount", "unique(VISITID)");
            json11.put("itemcodeCount", "unique(ITEMCODE)");
            json11.put("sumMinMoney", "sum(MIN_MONEY)");
            json11.put("sumActionMoney", "sum(ACTION_MONEY)");
        }

        // 以不合规行为ID分组
        JSONObject json1 = new JSONObject();
        json1.put("type", "terms");
        json1.put("field", "ACTION_ID");
        json1.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        json1.put("overrequest", Integer.MAX_VALUE);
        json1.put("facet", json11);

        JSONObject json = new JSONObject();
        json.put("action", json1);

        // facet查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json.toJSONString());

        log.info(jsonObject.toJSONString());
        long count = jsonObject.getLongValue("count");
        if (count == 0) {
            return new JSONArray();
        }
        // 获取不合规行为ID翻译MAP
        JSONArray actionData = jsonObject.getJSONObject("action").getJSONArray("buckets");
        List<String> actionIdList = actionData.stream().map(r -> ((JSONObject) r).getString("val")).distinct().collect(Collectors.toList());
        Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);

        if (StringUtils.isNotBlank(secReviewStatus)) {
            Iterator<Object> it = actionData.iterator();
            while (it.hasNext()) {
                JSONObject actionJson = (JSONObject) it.next();
                actionJson.put("totalCount", actionJson.remove("count"));
                actionJson.put("actionName", actionNameMap.get(actionJson.getString("val")));
                JSONObject facetData = (JSONObject) actionJson.remove("facet");
                // 移除判定条件过滤后记录0的数据
                if (facetData.getLongValue("count") == 0) {
                    it.remove();
                } else {
                    actionJson.putAll(facetData);
                }
            }
        } else {
            for (int i = 0, len = actionData.size(); i < len; i++) {
                JSONObject actionJson = actionData.getJSONObject(i);
                actionJson.put("actionName", actionNameMap.get(actionJson.getString("val")));
            }
        }
        return actionData;
    }

    @Override
    public JSONArray module4Data(SolrQuery solrQuery, String collection) throws Exception {
        JSONObject json11 = new JSONObject();
        // 初审归因分组层
        JSONObject json111 = new JSONObject();
        json111.put("type", "terms");
        json111.put("field", "FIR_REVIEW_CLASSIFY");
        json111.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        json111.put("overrequest", Integer.MAX_VALUE);
        // 复审归因分组层
        JSONObject json112 = new JSONObject();
        json112.put("type", "terms");
        json112.put("field", "SEC_REVIEW_CLASSIFY");
        json112.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        json112.put("overrequest", Integer.MAX_VALUE);
        // 不合规行为统计层
        json11.put("visitidCount", "unique(VISITID)");
        json11.put("itemcodeCount", "unique(ITEMCODE)");
        json11.put("sumMinMoney", "sum(MIN_MONEY)");
        json11.put("sumActionMoney", "sum(ACTION_MONEY)");
        json11.put("facet1", json111);
        json11.put("facet2", json112);
        // 以不合规行为ID分组
        JSONObject json1 = new JSONObject();
        json1.put("type", "terms");
        json1.put("field", "ACTION_ID");
        json1.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        json1.put("overrequest", Integer.MAX_VALUE);
        json1.put("facet", json11);

        JSONObject json = new JSONObject();
        json.put("action", json1);
        // facet查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json.toJSONString());

        log.info(jsonObject.toJSONString());
        long count = jsonObject.getLongValue("count");
        if (count == 0) {
            return new JSONArray();
        }
        // 获取不合规行为ID翻译MAP
        JSONArray actionData = jsonObject.getJSONObject("action").getJSONArray("buckets");
        List<String> actionIdList = actionData.stream().map(r -> ((JSONObject) r).getString("val")).distinct().collect(Collectors.toList());
        Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);

        for (Object actionDatum : actionData) {
            JSONObject actionJson = (JSONObject) actionDatum;
            actionJson.put("totalCount", count);
            actionJson.put("actionName", actionNameMap.get(actionJson.getString("val")));
            JSONObject facetData1 = (JSONObject) actionJson.remove("facet1");
            JSONObject facetData2 = (JSONObject) actionJson.remove("facet2");
            Map<String, Long> valMap = new HashMap<>();
            for (Object o : facetData1.getJSONArray("buckets")) {
                JSONObject valJson = (JSONObject) o;
                valMap.put(valJson.getString("val"), valJson.getLong("count"));
            }
            for (Object o : facetData2.getJSONArray("buckets")) {
                JSONObject valJson = (JSONObject) o;
                String val = valJson.getString("val");
                valMap.put(val, valMap.getOrDefault(val, 0L) + valJson.getLong("count"));
            }
            actionJson.putAll(valMap);
        }
        return actionData;
    }

    @Override
    public JSONArray module5Data(SolrQuery solrQuery, String collection, String secReviewStatus) throws Exception {
        JSONObject json1111 = new JSONObject();
        json1111.put("visitidCount", "unique(VISITID)");
        json1111.put("itemcodeCount", "unique(ITEMCODE)");
        json1111.put("sumMinMoney", "sum(MIN_MONEY)");
        json1111.put("sumActionMoney", "sum(ACTION_MONEY)");
        JSONObject json1121 = new JSONObject();
        json1121.putAll(json1111);
        // 推送到复审的数据
        JSONObject json111 = new JSONObject();
        json111.put("type", "query");
        json111.put("q", "PUSH_STATUS:1");
        json111.put("facet", json1111);
        // 推送到用户报告的数据
        JSONObject json112 = new JSONObject();
        json112.put("type", "query");
        json112.put("q", "SEC_PUSH_STATUS:1");
        json112.put("facet", json1121);
        JSONObject json11 = new JSONObject();
        json11.put("facet1", json111);
        json11.put("facet2", json112);

        if (StringUtils.isNotBlank(secReviewStatus)) {
            json111.replace("q", "PUSH_STATUS:1" + " AND FIR_REVIEW_STATUS:" + secReviewStatus);
            json112.replace("q", "SEC_PUSH_STATUS:1" + " AND SEC_REVIEW_STATUS:" + secReviewStatus);
        }

        // 以不合规行为ID分组
        JSONObject json1 = new JSONObject();
        json1.put("type", "terms");
        json1.put("field", "ACTION_ID");
        json1.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        json1.put("overrequest", Integer.MAX_VALUE);
        json1.put("facet", json11);

        JSONObject json = new JSONObject();
        json.put("action", json1);

        // facet查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json.toJSONString());

        log.info(jsonObject.toJSONString());
        long count = jsonObject.getLongValue("count");
        if (count == 0) {
            return new JSONArray();
        }
        // 获取不合规行为ID翻译MAP
        JSONArray actionData = jsonObject.getJSONObject("action").getJSONArray("buckets");
        List<String> actionIdList = actionData.stream().map(r -> ((JSONObject) r).getString("val")).distinct().collect(Collectors.toList());
        Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);

        for (int i = 0, len = actionData.size(); i < len; i++) {
            JSONObject actionJson = actionData.getJSONObject(i);
            actionJson.put("actionName", actionNameMap.get(actionJson.getString("val")));
        }
        return actionData;
    }

    @Override
    public void export(JSONObject jsonObject, String[] titles, String[] fields, String title, OutputStream os) throws Exception {
        for(String field: fields){
            Object val = jsonObject.get(field);
            if(val == null){
                jsonObject.put(field, 0);
            } else {
                if(val instanceof Double){
                    val = (double)Math.round((Double)val * 100) / 100;
                    jsonObject.put(field, val);
                } else if(val instanceof BigDecimal){
                    val = ((BigDecimal)val).setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
                    jsonObject.put(field, val);
                }
            }
        }
        /*for(Map.Entry<String, Object> entry: jsonObject.entrySet()){
            Object val = entry.getValue();
            if(val instanceof Double){
                entry.setValue((double)Math.round((Double)val * 100) / 100);
            } else if(val instanceof BigDecimal){
                entry.setValue(((BigDecimal)val).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
        }*/
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(jsonObject.getInnerMap());

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        ExportXUtils.exportExl(list, titles, fields, workbook, title);
        workbook.write(os);
        workbook.dispose();
    }

    @Override
    public void export(JSONArray jsonArray, String[] titles, String[] fields, String title, OutputStream os) throws Exception {

        List<Map<String, Object>> list = jsonArray.stream().map(r -> ((JSONObject)r).getInnerMap()).collect(Collectors.toList());

        for(Map<String, Object> map: list){

            for(String field: fields){
                Object val = map.get(field);
                if(val == null){
                    map.put(field, 0);
                } else {
                    if(val instanceof Double){
                        val = (double)Math.round((Double)val * 100) / 100;
                        map.put(field, val);
                    } else if(val instanceof BigDecimal){
                        val = ((BigDecimal)val).setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
                        map.put(field, val);
                    }
                }
            }

            /*for(Map.Entry<String, Object> entry: map.entrySet()){
                Object val = entry.getValue();
                if(val instanceof Double){
                    entry.setValue((double)Math.round((Double)val * 100) / 100);
                } else if(val instanceof BigDecimal){
                    entry.setValue(((BigDecimal)val).setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros());
                }
            }*/
        }

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        ExportXUtils.exportExl(list, titles, fields, workbook, title);
        workbook.write(os);
        workbook.dispose();
    }
}
