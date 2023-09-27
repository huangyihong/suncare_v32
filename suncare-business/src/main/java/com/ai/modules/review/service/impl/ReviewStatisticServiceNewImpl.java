package com.ai.modules.review.service.impl;

import cn.hutool.core.date.DateUtil;
import com.ai.common.MedicalConstant;
import com.ai.common.utils.ExportXUtils;
import com.ai.modules.api.util.ApiTokenCommon;
import com.ai.modules.config.entity.MedicalOtherDict;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.service.IReviewStatisticNewService;
import com.ai.modules.task.entity.TaskProject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.solr.client.solrj.SolrQuery;
import org.jeecg.common.util.DateUtils;
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
public class ReviewStatisticServiceNewImpl implements IReviewStatisticNewService {


    @Autowired
    IMedicalActionDictService medicalActionDictService;

    @Autowired
    private IEngineActionService engineActionService;

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
    public JSONObject module0MasterInfoData(TaskProject task) throws Exception {
        // 第一层统计总
        JSONObject json = new JSONObject();
        json.put("sumTotalfee", "sum(TOTALFEE)");
        json.put("sumFundpay", "sum(FUNDPAY)");

        // 查询条件
        List<String> conditionList = getProjectCondition(task, true);

        String collection= EngineUtil.DWB_MASTER_INFO;
        JSONObject jsonObject = SolrUtil.jsonFacet(collection,  conditionList.toArray(new String[0]), json.toJSONString());

        return jsonObject;
    }

    @Override
    public JSONObject module0ChargeDetailData(TaskProject task) throws Exception {
        // 第一层统计总
        JSONObject json = new JSONObject();
        json.put("itemcodeCount", "unique(ITEMCODE)");

        // 查询条件
        List<String> conditionList = getProjectCondition(task, false);

        String collection= EngineUtil.DWB_CHARGE_DETAIL;
        JSONObject jsonObject = SolrUtil.jsonFacet(collection,  conditionList.toArray(new String[0]), json.toJSONString());

        return jsonObject;
    }

    //项目条件
    private List<String> getProjectCondition(TaskProject task, boolean isMaster) throws Exception {
        // 查询条件
        List<String> conditionList = new ArrayList<String>();
        String project_startTime = MedicalConstant.DEFAULT_START_TIME;
        String project_endTime = MedicalConstant.DEFAULT_END_TIME;
        project_startTime = task.getDataStartTime() != null ? DateUtil.format(task.getDataStartTime(), "yyyy-MM-dd") : project_startTime;
        project_endTime = task.getDataEndTime() != null ? DateUtil.format(task.getDataEndTime(), "yyyy-MM-dd") : project_endTime;

        //项目过滤条件
        ProjectFilterWhereVO filterVO = engineActionService.filterCondition(task, isMaster);
        if (StringUtils.isNotBlank(filterVO.getCondition())) {
            conditionList.add(filterVO.getCondition());
        }
        if (filterVO.isDiseaseFilter()) {
            //疾病映射不全过滤
            SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_DIAG", "VISITID", "VISITID");
            conditionList.add("*:* -" + plugin.parse() + "-DISEASENAME:?*");
        }
        //项目的数据来源
        if (StringUtils.isNotBlank(task.getEtlSource())) {
            conditionList.add("ETL_SOURCE:" + task.getEtlSource());
        }
        //业务数据时间范围
        conditionList.add("VISITDATE:[" + project_startTime + " TO " + project_endTime + "]");
        return conditionList;
    }

    @Override
    public JSONArray module1Data(SolrQuery solrQuery, String collection) throws Exception {
        // 完成判定记录数
        JSONObject json111 = new JSONObject();
        json111.put("query", "FIR_REVIEW_STATUS: (white OR blank OR grey)");
        JSONObject json112 = new JSONObject();
        JSONObject json1121 = new JSONObject();
        json1121.put("visitidCount", "unique(VISITID)");
        json1121.put("itemcodeCount", "unique(ITEMCODE)");
        json1121.put("sumActionMoney", "sum(ACTION_MONEY)");
        json1121.put("sumMinMoney", "sum(MIN_MONEY)");

        json112.put("type", "query");
        json112.put("q", "-FIR_REVIEW_STATUS: (white OR blank OR grey)");
        json112.put("facet", json1121);
        JSONObject json11 = new JSONObject();
        json11.put("review", json111);
        json11.put("unReview", json112);

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
        // facet
        // 查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json.toJSONString());
        long count = jsonObject.getLongValue("count");
        if (count == 0) {
            return new JSONArray();
        }
        // 获取不合规行为ID翻译MAP
        JSONArray actionData = jsonObject.getJSONObject("action").getJSONArray("buckets");
        List<String> actionIdList = actionData.stream().map(r -> ((JSONObject) r).getString("val")).distinct().collect(Collectors.toList());
        Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);

        JSONObject totalData = new JSONObject();
        totalData.put("actionName", "全部（" + actionData.size() + "）");
        for (int i = 0, len = actionData.size(); i < len; i++) {
            JSONObject actionJson = actionData.getJSONObject(i);
            actionJson.put("actionName", actionNameMap.get(actionJson.getString("val")));
            JSONObject pushJson = (JSONObject)actionJson.remove("review");
            JSONObject unPushJson = (JSONObject)actionJson.remove("unReview");
            for(String key: pushJson.keySet()){
                String newKey = "review_" + key;
                double val = pushJson.getDoubleValue(key);
                actionJson.put(newKey , val);
                totalData.put(newKey, totalData.getDoubleValue(newKey) + val);
            }
            for(String key: unPushJson.keySet()){
                String newKey = "unReview_" + key;
                double val = unPushJson.getDoubleValue(key);
                actionJson.put(newKey , val);
                totalData.put(newKey, totalData.getDoubleValue(newKey) + val);
            }
        }

        actionData.add(0, totalData);
        return actionData;
    }

  /*  @Override
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
*/
    @Override
    public JSONArray module2Data(SolrQuery solrQuery, String collection, String groupBy) throws Exception {
        // 统计数值
        JSONObject json11 = new JSONObject();
        json11.put("visitidCount", "unique(VISITID)");
        json11.put("itemcodeCount", "unique(ITEMCODE)");
        json11.put("sumActionMoney", "sum(ACTION_MONEY)");
        json11.put("sumMinMoney", "sum(MIN_MONEY)");

        // 以不合规行为ID分组
        JSONObject json1 = new JSONObject();
        json1.put("type", "terms");
        json1.put("field", groupBy);
        json1.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        json1.put("overrequest", Integer.MAX_VALUE);
        json1.put("facet", json11);
        JSONObject json2 = new JSONObject();
        json2.put("type", "query");
        json2.put("q", "*:*");
        json2.put("facet", new JSONObject(json11));
        JSONObject json = new JSONObject();
        json.put("dataArray", json1);
        json.put("totalData", json2);
        // facet
        // 查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json.toJSONString());
        long count = jsonObject.getLongValue("count");
        if (count == 0) {
            return new JSONArray();
        }
        // 获取不合规行为ID翻译MAP
        JSONArray dataArray = jsonObject.getJSONObject("dataArray").getJSONArray("buckets");
        JSONObject totalData = jsonObject.getJSONObject("totalData");

        JSONObject valMap = new JSONObject();

        if("ACTION_ID".equals(groupBy)){
            List<String> actionIdList = dataArray.stream().map(r -> ((JSONObject) r).getString("val")).distinct().collect(Collectors.toList());
            Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
            valMap.putAll(actionNameMap);
        } else if("FIR_REVIEW_STATUS".equals(groupBy)){
            valMap = ApiTokenCommon.queryMedicalDictMapByKey("FIRST_REVIEW_STATUS");
        } else if("SEC_REVIEW_STATUS".equals(groupBy)){
            valMap = ApiTokenCommon.queryMedicalDictMapByKey("FIRST_REVIEW_STATUS");
        }


        List<String> ratioNameList = Arrays.asList("visitidCount", "count", "sumMinMoney");
        totalData.put("valName", "全部（" + dataArray.size() + "）");
        totalData.put("count", count);
        // 全部占比字段
        for(String name: ratioNameList){
            totalData.put(name + "Ratio", "--");
        }
        for (int i = 0, len = dataArray.size(); i < len; i++) {
            JSONObject actionJson = dataArray.getJSONObject(i);
            actionJson.put("valName", valMap.get(actionJson.getString("val"))!=null?valMap.get(actionJson.getString("val")):actionJson.getString("val"));
            // 占比字段
            for(String name: ratioNameList){
                double ratioVal = actionJson.getDoubleValue(name) / totalData.getDoubleValue(name);
                actionJson.put(name + "Ratio", (double)Math.round(ratioVal * 100) + "%");
            }
        }

        dataArray.add(0, totalData);
        return dataArray;
    }

    @Override
    public JSONArray module2ExportData(SolrQuery solrQuery, String collection, String groupBy, String statusField) throws Exception {
        // 统计数值
        JSONObject json11 = new JSONObject();
        json11.put("visitidCount", "unique(VISITID)");
        json11.put("itemcodeCount", "unique(ITEMCODE)");
        json11.put("sumActionMoney", "sum(ACTION_MONEY)");
        json11.put("sumMinMoney", "sum(MIN_MONEY)");
        JSONObject statClone = (JSONObject) json11.clone();
        // 构造判定状态统计数值
        List<MedicalOtherDict> statusDictList = ApiTokenCommon.queryMedicalDictListByKey("FIRST_REVIEW_STATUS");
        statusDictList
//                .stream().filter(r -> !"init".equals(r.getCode()))
                .forEach(r -> {
                    JSONObject json = new JSONObject();
                    json.put("type", "query");
                    json.put("q", statusField + ":" + r.getCode());
                    json.put("facet", statClone.clone());
                    json11.put(r.getCode(), json);
                });
        // 以不合规行为ID分组
        JSONObject json1 = new JSONObject();
        json1.put("type", "terms");
        json1.put("field", groupBy);
        json1.put("limit", Integer.MAX_VALUE);
        // 每个分片取的数据数量
        json1.put("overrequest", Integer.MAX_VALUE);
        json1.put("facet", JSONObject.parseObject(json11.toJSONString()));

        JSONObject json2 = new JSONObject();
        json2.put("type", "query");
        json2.put("q", "*:*");
        json2.put("facet", JSONObject.parseObject(json11.toJSONString()));
        JSONObject json = new JSONObject();
        json.put("dataArray", json1);
        json.put("totalData", json2);

       log.info(json.toJSONString());
        // facet
        // 查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json.toJSONString());
        long count = jsonObject.getLongValue("count");
        if (count == 0) {
            return new JSONArray();
        }
        // 获取不合规行为ID翻译MAP
        JSONArray dataArray = jsonObject.getJSONObject("dataArray").getJSONArray("buckets");
        JSONObject totalData = jsonObject.getJSONObject("totalData");

        JSONObject valMap = new JSONObject();

        List<String> actionIdList = dataArray.stream().map(r -> ((JSONObject) r).getString("val")).distinct().collect(Collectors.toList());
        Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
        valMap.putAll(actionNameMap);

        totalData.put("valName", "全部（" + dataArray.size() + "）");
        totalData.put("count", count);
        // 判定状态数值统计加前缀平铺到父级
        for(MedicalOtherDict medicalOtherDict : statusDictList){
            String statusCode = medicalOtherDict.getCode();
            JSONObject statusJson = (JSONObject) totalData.remove(statusCode);
            for (Map.Entry<String, Object> entry : statusJson.entrySet()) {
                totalData.put(statusCode + "_" + entry.getKey(), entry.getValue());
            }
        }
        for (int i = 0, len = dataArray.size(); i < len; i++) {
            JSONObject actionJson = dataArray.getJSONObject(i);
            actionJson.put("valName", valMap.get(actionJson.getString("val")));
            // 判定状态数值统计加前缀平铺到父级
            for(MedicalOtherDict medicalOtherDict : statusDictList){
                String statusCode = medicalOtherDict.getCode();
                JSONObject statusJson = (JSONObject) actionJson.remove(statusCode);
                for (Map.Entry<String, Object> entry : statusJson.entrySet()) {
                    actionJson.put(statusCode + "_" + entry.getKey(), entry.getValue());
                }
            }
        }

        dataArray.add(0, totalData);
        return dataArray;
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

        JSONObject json = new JSONObject().fluentPut("action", new JSONObject()
                // 以不合规行为ID分组
                .fluentPut("type", "terms")
                .fluentPut("field", "ACTION_ID")
                .fluentPut("limit", Integer.MAX_VALUE)
                .fluentPut("overrequest", Integer.MAX_VALUE)
                .fluentPut("facet", new JSONObject()
                        // 初审归因分组层
                        .fluentPut("facet1", new JSONObject()
                                .fluentPut("type", "terms")
                                .fluentPut("field", "FIR_REVIEW_CLASSIFY")
                                .fluentPut("limit", Integer.MAX_VALUE)
                                .fluentPut("overrequest", Integer.MAX_VALUE))
                        .fluentPut("facet2", new JSONObject()
                                // 过滤初审重复项
                                .fluentPut("type", "query")
                                .fluentPut("q", "-FIR_REVIEW_STATUS:white")
                                .fluentPut("facet", new JSONObject().fluentPut("facet1", new JSONObject()
                                        // 复审归因分组层
                                        .fluentPut("type", "terms")
                                        .fluentPut("field", "SEC_REVIEW_CLASSIFY")
                                        .fluentPut("limit", Integer.MAX_VALUE)
                                        .fluentPut("overrequest", Integer.MAX_VALUE)
                                ))
                        )
                )
        );
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

        JSONObject totalData = new JSONObject();
        totalData.put("actionName", "全部（" + actionData.size() + "）");
        totalData.put("count", count);
        totalData.put("totalCount", count);

        for (Object actionDatum : actionData) {
            JSONObject actionJson = (JSONObject) actionDatum;
            actionJson.put("totalCount", count);
            actionJson.put("actionName", actionNameMap.get(actionJson.getString("val")));
            JSONObject facetData1 = (JSONObject) actionJson.remove("facet1");
            JSONObject facetData2 = ((JSONObject) actionJson.remove("facet2")).getJSONObject("facet1");
            Map<String, Long> valMap = new HashMap<>();
            for (Object o : facetData1.getJSONArray("buckets")) {
                JSONObject valJson = (JSONObject) o;
                valMap.put(valJson.getString("val"), valJson.getLong("count"));
            }
            if(facetData2 != null){
                for (Object o : facetData2.getJSONArray("buckets")) {
                    JSONObject valJson = (JSONObject) o;
                    String val = valJson.getString("val");
                    valMap.put(val, valMap.getOrDefault(val, 0L) + valJson.getLong("count"));
                }
            }
            for(Map.Entry<String, Long> entry: valMap.entrySet()){
                totalData.put(entry.getKey(), totalData.getLongValue(entry.getKey()) + entry.getValue());
            }

            actionJson.putAll(valMap);
        }

        actionData.add(0, totalData);

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
            // 格式化数据
            JSONObject facet1 = (JSONObject) actionJson.remove("facet1");
            JSONObject facet2 = (JSONObject) actionJson.remove("facet2");
            long facet1Count = facet1.getLongValue("count");
            long facet2Count = facet2.getLongValue("count");
            actionJson.put("visitidCount", facet2.getLongValue("visitidCount") - facet1.getLongValue("visitidCount"));
            actionJson.put("count", facet2Count - facet1Count);
            actionJson.put("itemcodeCount", facet2.getLongValue("itemcodeCount") - facet1.getLongValue("itemcodeCount"));
            actionJson.put("sumMinMoney", facet2.getDoubleValue("sumMinMoney") - facet1.getDoubleValue("sumMinMoney"));
            actionJson.put("sumActionMoney", facet2.getDoubleValue("sumActionMoney") - facet1.getDoubleValue("sumActionMoney"));

        }
        return actionData;
    }

    @Override
    public JSONArray module5ExportData(SolrQuery solrQuery, String collection) throws Exception {
        JSONObject json1111 = new JSONObject();
        json1111.put("visitidCount", "unique(VISITID)");
        json1111.put("itemcodeCount", "unique(ITEMCODE)");
        json1111.put("sumMinMoney", "sum(MIN_MONEY)");
        json1111.put("sumActionMoney", "sum(ACTION_MONEY)");

        JSONObject json1121 = (JSONObject) json1111.clone();

        JSONObject statClone = (JSONObject) json1111.clone();
        // 构造判定状态统计数值
        List<MedicalOtherDict> statusDictList = ApiTokenCommon.queryMedicalDictListByKey("FIRST_REVIEW_STATUS");
        statusDictList
//                .stream().filter(r -> !"init".equals(r.getCode()))
                .forEach(r -> {
                    JSONObject json = new JSONObject();
                    json.put("type", "query");
                    json.put("q", "FIR_REVIEW_STATUS:" + r.getCode());
                    json.put("facet", statClone.clone());
                    json1111.put(r.getCode(), json);
                    JSONObject json1 = new JSONObject();
                    json1.put("type", "query");
                    json1.put("q", "SEC_REVIEW_STATUS:" + r.getCode());
                    json1.put("facet", statClone.clone());
                    json1121.put(r.getCode(), json1);
                });


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
            // 格式化数据
            JSONObject facet1 = (JSONObject) actionJson.remove("facet1");
            JSONObject facet2 = (JSONObject) actionJson.remove("facet2");
            long facet1Count = facet1.getLongValue("count");
            long facet2Count = facet2.getLongValue("count");
            actionJson.put("visitidCount", facet2.getLongValue("visitidCount") - facet1.getLongValue("visitidCount"));
            actionJson.put("count", facet2Count - facet1Count);
            actionJson.put("itemcodeCount", facet2.getLongValue("itemcodeCount") - facet1.getLongValue("itemcodeCount"));
            actionJson.put("sumMinMoney", facet2.getDoubleValue("sumMinMoney") - facet1.getDoubleValue("sumMinMoney"));
            actionJson.put("sumActionMoney", facet2.getDoubleValue("sumActionMoney") - facet1.getDoubleValue("sumActionMoney"));

            // 判定状态数值统计加前缀平铺到父级
            for(MedicalOtherDict medicalOtherDict : statusDictList){
                String statusCode = medicalOtherDict.getCode();
                JSONObject statusJson1 = facet1Count == 0?new JSONObject():facet1.getJSONObject(statusCode);
                JSONObject statusJson2 = facet2Count == 0?new JSONObject():facet2.getJSONObject(statusCode);
                actionJson.put(statusCode + "_" + "visitidCount", statusJson2.getLongValue("visitidCount") - statusJson1.getLongValue("visitidCount"));
                actionJson.put(statusCode + "_" + "count", statusJson2.getLongValue("count") - statusJson1.getLongValue("count"));
                actionJson.put(statusCode + "_" + "itemcodeCount", statusJson2.getLongValue("itemcodeCount") - statusJson1.getLongValue("itemcodeCount"));
                actionJson.put(statusCode + "_" + "sumMinMoney", statusJson2.getDoubleValue("sumMinMoney") - statusJson1.getDoubleValue("sumMinMoney"));
                actionJson.put(statusCode + "_" + "sumActionMoney", statusJson2.getDoubleValue("sumActionMoney") - statusJson1.getDoubleValue("sumActionMoney"));
            }
        }

        return actionData;
    }

    @Override
    public void export(JSONObject jsonObject, String[] titles, String[] fields, String title, OutputStream os) throws Exception {
        for(String field: fields){
            Object val = jsonObject.get(field);
            if(val == null){
                val = 0;
                jsonObject.put(field, val);
            } else {
                if(val instanceof Double){
                    val = (double)Math.round((Double)val * 100) / 100;
                    jsonObject.put(field, val);
                } else if(val instanceof BigDecimal){
                    val = ((BigDecimal)val).setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
                    jsonObject.put(field, val);
                }
            }
//            jsonObject.put(field, val.toString());

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
                    val = 0;
                    map.put(field, val);
                } else {
                    if(val instanceof Double){
                        val = (double)Math.round((Double)val * 100) / 100;
                        map.put(field, val);
                    } else if(val instanceof BigDecimal){
                        val = ((BigDecimal)val).setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
                        map.put(field, val);
                    }
                }
//                map.put(field, val.toString());

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


    @Override
    public JSONArray module6Data(SolrQuery solrQuery, String collection, String groupBy) throws Exception {
        JSONObject jsonSum = new JSONObject();
        jsonSum.put("visitidcount", "unique(VISITID)");
        jsonSum.put("sumActionMoney", "sum(ACTION_MONEY)");

        //输出记录
        JSONObject totalJson = new JSONObject();
        totalJson.put("type", "query");
        totalJson.put("q", "*:*");
        totalJson.put("facet", new JSONObject(jsonSum));


        // 已判定记录
        JSONObject judgeJson = new JSONObject();
        judgeJson.put("type", "query");
        judgeJson.put("q", "FIR_REVIEW_STATUS: (white OR blank OR grey)");
        judgeJson.put("facet", new JSONObject(jsonSum));

        // 黑名单记录
        JSONObject blankJson = new JSONObject();
        blankJson.put("type", "query");
        blankJson.put("q", "FIR_REVIEW_STATUS: blank");
        blankJson.put("facet", new JSONObject(jsonSum));



        JSONObject json11 = new JSONObject();
        json11.put("totalFact", totalJson);
        json11.put("judgeFact", judgeJson);
        json11.put("blankFact", blankJson);

        // 以医疗机构名称分组
        JSONObject json1 = new JSONObject();
        json1.put("type", "terms");
        json1.put("field", groupBy);
        json1.put("limit", Integer.MAX_VALUE);
        json1.put("overrequest", Integer.MAX_VALUE);
        json1.put("facet", json11);

        JSONObject json = new JSONObject();
        json.put("dataArray", json1);


        // facet查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json.toJSONString());

        log.info(jsonObject.toJSONString());
        long count = jsonObject.getLongValue("count");
        if (count == 0) {
            return new JSONArray();
        }

        JSONArray dataArray = jsonObject.getJSONObject("dataArray").getJSONArray("buckets");
        List<String> countNameList = Arrays.asList("total_count","judge_count", "blank_count","total_sumActionMoney","judge_sumActionMoney","blank_sumActionMoney");
        List<String> factNameList = Arrays.asList("totalFact","judgeFact", "blankFact","totalFact", "judgeFact", "blankFact");
        JSONObject totalData = new JSONObject();
        totalData.put("valName", "全部（" + dataArray.size() + "）");
        for(String name: countNameList){
            totalData.put(name, 0);
        }
        List<String> countRatioList = Arrays.asList("blank_count");
        List<String> sumActionMoneyRatioList = Arrays.asList("blank_sumActionMoney");
        for (int i = 0, len = dataArray.size(); i < len; i++) {
            JSONObject dataJson = dataArray.getJSONObject(i);
            dataJson.put("valName", dataJson.getString("val"));
            Map<String,JSONObject> dataFactMap = new HashMap<>();
            for(String factName:factNameList){
                if(dataFactMap.get(factName)==null){
                    dataFactMap.put(factName,(JSONObject) dataJson.remove(factName));
                }
            }

            for(int j = 0; j < countNameList.size(); j++){
                if(countNameList.get(j).indexOf("count")!=-1){
                    int index = countNameList.get(j).lastIndexOf("_");
                    String key = countNameList.get(j).substring(index+1);
                    dataJson.put(countNameList.get(j),  dataFactMap.get(factNameList.get(j)).getLongValue(key));
                    totalData.put(countNameList.get(j), totalData.getLongValue(countNameList.get(j))+dataFactMap.get(factNameList.get(j)).getLongValue(key));
                    for(String countRatioName:countRatioList){
                        if(countRatioName.equals(countNameList.get(j))&&dataJson.getLongValue("judge_count")>0){
                            double ratioVal = dataJson.getLongValue(countRatioName)*100 / dataJson.getLongValue("judge_count");
                            dataJson.put(countRatioName+"Ratio", (double)Math.round(ratioVal) + "%");
                        }
                    }

                }
                if(countNameList.get(j).indexOf("sumActionMoney")!=-1){
                    dataJson.put(countNameList.get(j),  dataFactMap.get(factNameList.get(j)).getDoubleValue("sumActionMoney"));
                    totalData.put(countNameList.get(j), totalData.getDoubleValue(countNameList.get(j))+dataFactMap.get(factNameList.get(j)).getDoubleValue("sumActionMoney"));
                    for(String sumActionMoneyRatioName:sumActionMoneyRatioList){
                        if(sumActionMoneyRatioName.equals(countNameList.get(j))&&dataJson.getDoubleValue("judge_sumActionMoney")>0){
                            double ratioVal = dataJson.getDoubleValue(sumActionMoneyRatioName)*100 / dataJson.getDoubleValue("judge_sumActionMoney");
                            dataJson.put(sumActionMoneyRatioName+"Ratio", (double)Math.round(ratioVal) + "%");
                        }
                    }
                }
            }
        }
        for(String countRatioName:countRatioList){
            if(totalData.getLongValue(countRatioName)>0){
                double countRatioVal = totalData.getLongValue(countRatioName)*100 / totalData.getLongValue("judge_count");
                totalData.put(countRatioName+"Ratio", (double)Math.round(countRatioVal) + "%");
            }
        }
        for(String sumActionMoneyRatioName:sumActionMoneyRatioList){
            if(totalData.getDoubleValue(sumActionMoneyRatioName)>0){
                double sumActionMoneyRatioVal = totalData.getDoubleValue(sumActionMoneyRatioName) / totalData.getDoubleValue("judge_sumActionMoney");
                totalData.put(sumActionMoneyRatioName+"Ratio", (double)Math.round(sumActionMoneyRatioVal * 100) + "%");
            }
        }
        dataArray.add(0, totalData);
        return dataArray;
    }

    @Override
    public JSONArray module6ExportData(SolrQuery solrQuery, String collection, String groupBy) throws Exception {
        JSONObject jsonSum = new JSONObject();
        jsonSum.put("visitidcount", "unique(VISITID)");
        jsonSum.put("sumActionMoney", "sum(ACTION_MONEY)");

        JSONObject totalJson = new JSONObject();
        totalJson.put("type", "query");
        totalJson.put("q", "*:*");
        totalJson.put("facet", new JSONObject(jsonSum));
        // 未判定记录
        JSONObject nojudgeJson = new JSONObject();
        nojudgeJson.put("type", "query");
        nojudgeJson.put("q", "-FIR_REVIEW_STATUS: (white OR blank OR grey)");
        nojudgeJson.put("facet", new JSONObject(jsonSum));

        // 已判定记录
        JSONObject judgeJson = new JSONObject();
        judgeJson.put("type", "query");
        judgeJson.put("q", "FIR_REVIEW_STATUS: (white OR blank OR grey)");
        judgeJson.put("facet", new JSONObject(jsonSum));

        // 黑名单记录
        JSONObject blankJson = new JSONObject();
        blankJson.put("type", "query");
        blankJson.put("q", "FIR_REVIEW_STATUS: blank");
        blankJson.put("facet", new JSONObject(jsonSum));

        // 白名单记录
        JSONObject whiteJson = new JSONObject();
        whiteJson.put("type", "query");
        whiteJson.put("q", "FIR_REVIEW_STATUS: white");
        whiteJson.put("facet", new JSONObject(jsonSum));

        // 灰名单记录
        JSONObject greyJson = new JSONObject();
        greyJson.put("type", "query");
        greyJson.put("q", "FIR_REVIEW_STATUS: grey");
        greyJson.put("facet", new JSONObject(jsonSum));

        JSONObject json11 = new JSONObject();
        json11.put("totalFact", totalJson);
        json11.put("nojudgeFact", nojudgeJson);
        json11.put("judgeFact", judgeJson);
        json11.put("blankFact", blankJson);
        json11.put("whiteFact", whiteJson);
        json11.put("greyFact", greyJson);



        // 以医疗机构名称分组
        JSONObject json1 = new JSONObject();
        json1.put("type", "terms");
        json1.put("field", groupBy);
        json1.put("limit", Integer.MAX_VALUE);
        json1.put("overrequest", Integer.MAX_VALUE);
        json1.put("facet", json11);

        JSONObject json = new JSONObject();
        json.put("dataArray", json1);


        // facet查询
        JSONObject jsonObject = SolrUtil.jsonFacet(collection, solrQuery.getFilterQueries(), json.toJSONString());

        log.info(jsonObject.toJSONString());
        long count = jsonObject.getLongValue("count");
        if (count == 0) {
            return new JSONArray();
        }

        JSONArray dataArray = jsonObject.getJSONObject("dataArray").getJSONArray("buckets");
        List<String> countNameList = Arrays.asList("total_count", "nojudge_count","judge_count", "blank_count", "white_count", "grey_count","total_sumActionMoney","nojudge_sumActionMoney","judge_sumActionMoney","blank_sumActionMoney","white_sumActionMoney","grey_sumActionMoney","total_visitidcount","nojudge_visitidcount","judge_visitidcount","blank_visitidcount","white_visitidcount","grey_visitidcount");
        List<String> factNameList = Arrays.asList("totalFact", "nojudgeFact","judgeFact", "blankFact","whiteFact","greyFact","totalFact","nojudgeFact", "judgeFact", "blankFact","whiteFact","greyFact","totalFact", "nojudgeFact","judgeFact", "blankFact","whiteFact","greyFact");
        JSONObject totalData = new JSONObject();
        totalData.put("valName", "全部（" + dataArray.size() + "）");
        for(String name: countNameList){
            totalData.put(name, 0);
        }
        List<String> countRatioList = Arrays.asList("blank_count", "white_count", "grey_count");
        List<String> sumActionMoneyRatioList = Arrays.asList("blank_sumActionMoney", "white_sumActionMoney", "grey_sumActionMoney");
        for (int i = 0, len = dataArray.size(); i < len; i++) {
            JSONObject dataJson = dataArray.getJSONObject(i);
            dataJson.put("valName", dataJson.getString("val"));
            Map<String,JSONObject> dataFactMap = new HashMap<>();
            for(String factName:factNameList){
                if(dataFactMap.get(factName)==null){
                    dataFactMap.put(factName,(JSONObject) dataJson.remove(factName));
                }
            }

            for(int j = 0; j < countNameList.size(); j++){
                if(countNameList.get(j).indexOf("count")!=-1){
                    int index = countNameList.get(j).lastIndexOf("_");
                    String key = countNameList.get(j).substring(index+1);
                    dataJson.put(countNameList.get(j),  dataFactMap.get(factNameList.get(j)).getLongValue(key));
                    totalData.put(countNameList.get(j), totalData.getLongValue(countNameList.get(j))+dataFactMap.get(factNameList.get(j)).getLongValue(key));
                    for(String countRatioName:countRatioList){
                        if(countRatioName.equals(countNameList.get(j))&&dataJson.getLongValue("judge_count")>0){
                            double ratioVal = dataJson.getLongValue(countRatioName)*100 / dataJson.getLongValue("judge_count");
                            dataJson.put(countRatioName+"Ratio", (double)Math.round(ratioVal) + "%");
                        }
                    }

                }
                if(countNameList.get(j).indexOf("sumActionMoney")!=-1){
                    dataJson.put(countNameList.get(j),  dataFactMap.get(factNameList.get(j)).getDoubleValue("sumActionMoney"));
                    totalData.put(countNameList.get(j), totalData.getDoubleValue(countNameList.get(j))+dataFactMap.get(factNameList.get(j)).getDoubleValue("sumActionMoney"));
                    for(String sumActionMoneyRatioName:sumActionMoneyRatioList){
                        if(sumActionMoneyRatioName.equals(countNameList.get(j))&&dataJson.getDoubleValue("judge_sumActionMoney")>0){
                            double ratioVal = dataJson.getDoubleValue(sumActionMoneyRatioName)*100 / dataJson.getDoubleValue("judge_sumActionMoney");
                            dataJson.put(sumActionMoneyRatioName+"Ratio", (double)Math.round(ratioVal) + "%");
                        }
                    }
                }

            }
        }
        for(String countRatioName:countRatioList){
            if(totalData.getLongValue(countRatioName)>0){
                double countRatioVal = totalData.getLongValue(countRatioName)*100 / totalData.getLongValue("judge_count");
                totalData.put(countRatioName+"Ratio", (double)Math.round(countRatioVal) + "%");
            }
        }
        for(String sumActionMoneyRatioName:sumActionMoneyRatioList){
            if(totalData.getDoubleValue(sumActionMoneyRatioName)>0){
                double sumActionMoneyRatioVal = totalData.getDoubleValue(sumActionMoneyRatioName) / totalData.getDoubleValue("judge_sumActionMoney");
                totalData.put(sumActionMoneyRatioName+"Ratio", (double)Math.round(sumActionMoneyRatioVal * 100) + "%");
            }
        }
        dataArray.add(0, totalData);
        return dataArray;
    }
}
