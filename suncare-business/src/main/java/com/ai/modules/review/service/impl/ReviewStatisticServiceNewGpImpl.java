package com.ai.modules.review.service.impl;

import cn.hutool.core.date.DateUtil;
import com.ai.common.MedicalConstant;
import com.ai.common.utils.ExportXUtils;
import com.ai.modules.api.util.ApiTokenCommon;
import com.ai.modules.config.entity.MedicalOtherDict;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.review.entity.MedicalUnreasonableAction;
import com.ai.modules.review.service.IMedicalUnreasonableActionService;
import com.ai.modules.review.service.IReviewStatisticNewGpService;
import com.ai.modules.task.entity.TaskProject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@DS("greenplum")
public class ReviewStatisticServiceNewGpImpl implements IReviewStatisticNewGpService {
    @Autowired
    private IMedicalUnreasonableActionService medicalUnreasonableActionService;

    @Autowired
    IMedicalActionDictService medicalActionDictService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String,Object> module0Data(QueryWrapper<MedicalUnreasonableAction> queryWrapper) throws Exception {
        String resultFields = "id,VISITID,ITEMCODE,MIN_MONEY,ACTION_MONEY,PUSH_STATUS,SEC_PUSH_STATUS";
        String factFields = "count(distinct t.VISITID) as visitid_count,count(distinct t.ITEMCODE) as itemcode_count,sum(t.MIN_MONEY) as sum_min_money,sum(t.ACTION_MONEY) as sum_action_money," +
                "sum(case when t.PUSH_STATUS='1' then 1 else 0 end) as push_count,sum(case when t.SEC_PUSH_STATUS='1' then 1 else 0 end) as secPushCount,count(1) as count";

        List<Map<String,Object>> list = medicalUnreasonableActionService.facetFields(queryWrapper,"","",resultFields,factFields);
        if(list.size()>0){
            Map<String,Object> map = list.get(0);
            map.put("visitidCount", map.get("visitid_count"));
            map.put("itemcodeCount", map.get("itemcode_count"));
            map.put("sumMinMoney", map.get("sum_min_money"));
            map.put("sumActionMoney", map.get("sum_action_money"));
            map.put("pushCount", map.get("push_count"));
            map.put("secPushCount", map.get("sec_push_count"));
            return map;
        }

        return null;
    }

    @Override
    public Map<String,Object> module0MasterInfoData(TaskProject task) throws Exception {
        // 查询条件
        List<String> conditionList = getProjectCondition(task, true);

        String sql = "select sum(TOTALFEE)  as sum_totalfee, sum(FUNDPAY) as sum_fundpay,count(1) as count from medical.DWB_MASTER_INFO where 1=1 ";
        String whereSql = StringUtils.join(conditionList," AND ");
        if(StringUtils.isNotBlank(whereSql)){
            whereSql = " AND "+whereSql;
        }
        List<Map<String,Object>> list = jdbcTemplate.queryForList(sql+whereSql);
        if(list.size()>0){
            Map<String,Object> map = list.get(0);
            map.put("sumTotalfee", map.get("sum_totalfee"));
            map.put("sumFundpay", map.get("sum_fundpay"));
            return map;
        }
        return null;
    }

    @Override
    public Map<String,Object> module0ChargeDetailData(TaskProject task) throws Exception {
        // 查询条件
        List<String> conditionList = getProjectCondition(task, false);

        String sql ="select count(distinct ITEMCODE) as itemcode_count,count(1) as count from medical.DWB_CHARGE_DETAIL where 1=1 ";
        String whereSql = StringUtils.join(conditionList," AND ");
        if(StringUtils.isNotBlank(whereSql)){
            whereSql = " AND "+whereSql;
        }
        List<Map<String,Object>> list = jdbcTemplate.queryForList(sql+whereSql);
        if(list.size()>0){
            Map<String,Object> map = list.get(0);
            map.put("itemcodeCount", map.get("itemcode_count"));
            return map;
        }
        return null;
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
        //ProjectFilterWhereVO filterVO = engineActionService.filterCondition(task, isMaster);
        ProjectFilterWhereVO filterVO = new ProjectFilterWhereVO();
        if (StringUtils.isNotBlank(filterVO.getCondition())) {
//            conditionList.add(filterVO.getCondition());
        }
        if (filterVO.isDiseaseFilter()) {
            //疾病映射不全过滤
//            SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_DIAG", "VISITID", "VISITID");
//            conditionList.add("*:* -" + plugin.parse() + "-DISEASENAME:?*");
        }
        //项目的数据来源
        if (StringUtils.isNotBlank(task.getEtlSource())) {
            conditionList.add("ETL_SOURCE ='" + task.getEtlSource()+"'");
        }
        //业务数据时间范围
        if(isMaster){
            conditionList.add("VISITDATE>='" + project_startTime + "' and  VISITDATE<='" + project_endTime + "'");
        }else{
            conditionList.add("CHARGEDATE>='" + project_startTime + "' and  CHARGEDATE<='" + project_endTime + "'");
        }

        return conditionList;
    }

    @Override
    public List<JSONObject> module1Data(List<String> batchIdList,String actionId) throws Exception {
        String[] status = {"white","blank","grey"};
        //完成判定的记录数
        QueryWrapper<MedicalUnreasonableAction> queryWrapper1 = new QueryWrapper();
        queryWrapper1.in("BATCH_ID",batchIdList);
        if(StringUtils.isNotBlank(actionId)){
            queryWrapper1.eq("ACTION_ID",actionId);
        }
        queryWrapper1.notIn("FIR_REVIEW_STATUS",status);
        queryWrapper1.select("ACTION_ID,count(distinct VISITID) as visitid_count,count(distinct ITEMCODE) as itemcode_count,sum(MIN_MONEY) as sum_min_money,sum(ACTION_MONEY) as sum_action_money,count(1) as un_review_count ")
                .groupBy("ACTION_ID").orderByDesc("un_review_count");
        List<Map<String, Object>> unReviewList = this.medicalUnreasonableActionService.listMaps(queryWrapper1);
        //未完成判定的记录数
        QueryWrapper<MedicalUnreasonableAction> queryWrapper2 = new QueryWrapper();
        queryWrapper2.in("BATCH_ID",batchIdList);
        if(StringUtils.isNotBlank(actionId)){
            queryWrapper2.eq("ACTION_ID",actionId);
        }
        queryWrapper2.in("FIR_REVIEW_STATUS",status);
        queryWrapper2.select("ACTION_ID,count(1) as review_count ")
                .groupBy("ACTION_ID").orderByDesc("review_count");;
        List<Map<String, Object>> reviewList = this.medicalUnreasonableActionService.listMaps(queryWrapper2);
        // 获取不合规行为ID翻译MAP
        Set<String> actionIdList = unReviewList.stream().map(r ->(String)r.get("action_id")).collect(Collectors.toSet());
        reviewList.forEach(r->{
            actionIdList.add((String)r.get("action_id"));
        });
        Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
        Map<String,Map<String,Object>> actionResultMap = new HashMap<>();
        unReviewList.forEach(r->{
            actionResultMap.put((String)r.get("action_id"),r);
        });
        reviewList.forEach(r->{
            Map<String,Object> bean = actionResultMap.computeIfAbsent((String)r.get("action_id"), k -> r);
            bean.put("review_count",r.get("review_count"));
        });

        //返回结果数据
        List<JSONObject> actionData = new ArrayList<>();
        for (Map.Entry<String, Map<String,Object>> entry : actionResultMap.entrySet()) {
            String actionKey = entry.getKey();
            Map<String,Object> r = entry.getValue();
            JSONObject actionJson = new JSONObject();
            actionJson.put("val",actionKey);
            actionJson.put("actionName",actionNameMap.get(actionKey));
            actionJson.put("review_count",r.get("review_count")==null?0:r.get("review_count"));
            actionJson.put("unReview_count",r.get("un_review_count")==null?0:r.get("un_review_count"));
            actionJson.put("unReview_visitidCount",r.get("visitid_count")==null?0:r.get("visitid_count"));
            actionJson.put("unReview_itemcodeCount",r.get("itemcode_count")==null?0:r.get("itemcode_count"));
            actionJson.put("unReview_sumMinMoney",r.get("sum_min_money")==null?0:r.get("sum_min_money"));
            actionJson.put("unReview_sumActionMoney",r.get("sum_action_money")==null?0:r.get("sum_action_money"));
            actionJson.put("count",actionJson.getIntValue("review_count")+actionJson.getIntValue("unReview_count"));
            actionData.add(actionJson);
        }

        JSONObject totalData = new JSONObject();
        totalData.put("actionName", "全部（" + actionData.size() + "）");
        for (int i = 0, len = actionData.size(); i < len; i++) {
            JSONObject actionJson = actionData.get(i);
            for(String key: actionJson.keySet()){
                if(!"val".equals(key)&&!"actionName".equals(key)){
                    double val = actionJson.getDoubleValue(key);
                    totalData.put(key, totalData.getDoubleValue(key) + val);
                }

            }
        }

        actionData.add(0, totalData);
        //排序
        Comparator<JSONObject> comp = (o1, o2) -> o2.getIntValue("count") - o1.getIntValue("count");
        actionData = actionData.stream().sorted(comp).collect(Collectors.toList());
        return actionData;
    }

    @Override
    public void export(List<JSONObject> list, String[] titles, String[] fields, String title, OutputStream os) throws Exception {

        List<Map<String,Object>> dataList = new ArrayList<>();
        for(JSONObject map: list){
            Map<String,Object> bean = new HashMap<>();
            for(String field: fields){
                Object val = map.get(field);
                if(val == null){
                    val = 0;
                    bean.put(field, val);
                } else {
                    if(val instanceof Double){
                        val = (double)Math.round((Double)val * 100) / 100;
                        bean.put(field, val);
                    } else if(val instanceof BigDecimal){
                        val = ((BigDecimal)val).setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
                        bean.put(field, val);
                    }else{
                        bean.put(field, val);
                    }
                }
            }
            dataList.add(bean);
        }

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        ExportXUtils.exportExl(dataList, titles, fields, workbook, title);
        workbook.write(os);
        workbook.dispose();
    }

    @Override
    public List<JSONObject> module2Data(QueryWrapper<MedicalUnreasonableAction> queryWrapper, String groupBy) throws Exception {
        queryWrapper.select(groupBy+",count(distinct VISITID) as visitid_count,count(distinct ITEMCODE) as itemcode_count,sum(MIN_MONEY) as sum_min_money,sum(ACTION_MONEY) as sum_action_money,count(1) as count ")
                .groupBy(groupBy).orderByDesc("count");;
        List<Map<String, Object>> list = this.medicalUnreasonableActionService.listMaps(queryWrapper);
        if(list.size()>0){
            // 获取不合规行为ID翻译MAP
            JSONObject valMap = new JSONObject();
            if("ACTION_ID".equals(groupBy)){
                List<String> actionIdList = list.stream().map(r -> (String)r.get(groupBy.toLowerCase())).distinct().collect(Collectors.toList());
                Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
                valMap.putAll(actionNameMap);
            } else if("FIR_REVIEW_STATUS".equals(groupBy)){
                valMap = ApiTokenCommon.queryMedicalDictMapByKey("FIRST_REVIEW_STATUS");
            } else if("SEC_REVIEW_STATUS".equals(groupBy)){
                valMap = ApiTokenCommon.queryMedicalDictMapByKey("FIRST_REVIEW_STATUS");
            }
            List<JSONObject> actionData = new ArrayList<>();
            JSONObject totalData = new JSONObject();
            totalData.put("valName", "全部（" + list.size() + "）");
            JSONObject finalValMap = valMap;
            list.forEach(map->{
                JSONObject actionJson = new JSONObject();
                actionJson.put("visitidCount", map.get("visitid_count"));
                actionJson.put("itemcodeCount", map.get("itemcode_count"));
                actionJson.put("sumMinMoney", map.get("sum_min_money"));
                actionJson.put("sumActionMoney", map.get("sum_action_money"));
                actionJson.put("count", map.get("count"));
                actionJson.put("val", map.get(groupBy.toLowerCase()));
                actionJson.put("valName", finalValMap.get(actionJson.getString("val"))!=null? finalValMap.get(actionJson.getString("val")):actionJson.getString("val"));
                actionData.add(actionJson);
                totalData.put("count",actionJson.getIntValue("count")+totalData.getIntValue("count"));
                totalData.put("visitidCount",actionJson.getIntValue("visitidCount")+totalData.getIntValue("visitidCount"));
                totalData.put("itemcodeCount",actionJson.getIntValue("itemcodeCount")+totalData.getIntValue("itemcodeCount"));
                totalData.put("sumMinMoney",actionJson.getDoubleValue("sumMinMoney")+totalData.getDoubleValue("sumMinMoney"));
                totalData.put("sumActionMoney",actionJson.getDoubleValue("sumActionMoney")+totalData.getDoubleValue("sumActionMoney"));
            });
            List<String> ratioNameList = Arrays.asList("visitidCount", "count", "sumMinMoney");
            // 全部占比字段
            for(String name: ratioNameList){
                totalData.put(name + "Ratio", "--");
            }
            for (int i = 0, len = actionData.size(); i < len; i++) {
                JSONObject actionJson = actionData.get(i);
                // 占比字段
                for(String name: ratioNameList){
                    double ratioVal = actionJson.getDoubleValue(name) / totalData.getDoubleValue(name);
                    actionJson.put(name + "Ratio", (double)Math.round(ratioVal * 100) + "%");
                }
            }
            actionData.add(0, totalData);
            return actionData;
        }else{
            return new ArrayList<>();
        }


    }

    @Override
    public List<JSONObject> module2ExportData(List<String> batchIdList,String groupBy, String statusField) throws Exception {
        // 构造判定状态统计数值
        List<MedicalOtherDict> statusDictList = ApiTokenCommon.queryMedicalDictListByKey("FIRST_REVIEW_STATUS");
        statusDictList.add(new MedicalOtherDict());//全部
        // 获取不合规行为ID翻译MAP
        Set<String> actionIdList = new HashSet<>();
        Map<String,JSONObject> actionResultMap = new HashMap<>();
        statusDictList.forEach(dict -> {
            QueryWrapper<MedicalUnreasonableAction> queryWrapper1 = new QueryWrapper();
            queryWrapper1.in("BATCH_ID",batchIdList);
            if(StringUtils.isNotBlank(dict.getCode())){
                queryWrapper1.eq(statusField,dict.getCode());
            }
            queryWrapper1.select(groupBy+",count(distinct VISITID) as visitid_count,count(distinct ITEMCODE) as itemcode_count,sum(MIN_MONEY) as sum_min_money,sum(ACTION_MONEY) as sum_action_money,count(1) as count ")
                    .groupBy(groupBy).orderByDesc("count");
            List<Map<String, Object>> list = this.medicalUnreasonableActionService.listMaps(queryWrapper1);
            list.forEach(r->{
                actionIdList.add((String)r.get("action_id"));
                JSONObject bean = actionResultMap.computeIfAbsent((String)r.get(groupBy.toLowerCase()), k -> new JSONObject());
                String dictField ="";
                if(StringUtils.isNotBlank(dict.getCode())){
                    dictField = dict.getCode()+"_";
                }
                bean.put(dictField+"visitidCount", r.get("visitid_count"));
                bean.put(dictField+"itemcodeCount", r.get("itemcode_count"));
                bean.put(dictField+"sumMinMoney", r.get("sum_min_money"));
                bean.put(dictField+"sumActionMoney", r.get("sum_action_money"));
                bean.put(dictField+"count", r.get("count"));
                bean.put("val", r.get(groupBy.toLowerCase()));
            });
        });
        Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
        //返回结果数据
        List<JSONObject> actionData = new ArrayList<>();
        for (Map.Entry<String, JSONObject> entry : actionResultMap.entrySet()) {
            String actionKey = entry.getKey();
            JSONObject actionJson = entry.getValue();
            actionJson.put("val",actionKey);
            actionJson.put("valName",actionNameMap.get(actionKey));
            actionData.add(actionJson);
        }

        JSONObject totalData = new JSONObject();
        totalData.put("valName", "全部（" + actionData.size() + "）");
        for (int i = 0, len = actionData.size(); i < len; i++) {
            JSONObject actionJson = actionData.get(i);
            for(String key: actionJson.keySet()){
                if(!"val".equals(key)&&!"valName".equals(key)){
                    double val = actionJson.getDoubleValue(key);
                    totalData.put(key, totalData.getDoubleValue(key) + val);
                }
            }
        }

        actionData.add(0, totalData);
        //排序
        Comparator<JSONObject> comp = (o1, o2) -> o2.getIntValue("count") - o1.getIntValue("count");
        actionData = actionData.stream().sorted(comp).collect(Collectors.toList());
        return actionData;
    }

    @Override
    public List<JSONObject> module4Data(List<String> batchIdList) throws Exception {
        String firSql = "select ACTION_ID,FIR_REVIEW_CLASSIFY as REVIEW_CLASSIFY,count(1) as count from medical_unreasonable_action where 1=1 " +
                " and FIR_REVIEW_STATUS = 'white'  and  FIR_REVIEW_CLASSIFY is not null and FIR_REVIEW_CLASSIFY !='' " +
                " and batch_id in ('"+StringUtils.join(batchIdList,"','")+"') " +
                " group by ACTION_ID,FIR_REVIEW_CLASSIFY";
        String secSql = "select ACTION_ID,SEC_REVIEW_CLASSIFY as REVIEW_CLASSIFY,count(1) as count from medical_unreasonable_action where 1=1 " +
                " and SEC_REVIEW_STATUS = 'white'  and  SEC_REVIEW_CLASSIFY is not null and SEC_REVIEW_CLASSIFY !='' " +
                " and batch_id in ('"+StringUtils.join(batchIdList,"','")+"') " +
                " group by ACTION_ID,SEC_REVIEW_CLASSIFY";
        String sql = "select ACTION_ID,REVIEW_CLASSIFY,sum(count) as count from ( " +
                "("+firSql+")" +
                " union " +
                "("+secSql+")" +
                " ) t group by ACTION_ID,REVIEW_CLASSIFY";
        List<Map<String,Object>> list = jdbcTemplate.queryForList(sql);
        if(list.size()>0){
            // 获取不合规行为ID翻译MAP
            Set<String> actionIdList = new HashSet<>();


            Map<String,JSONObject> actionResultMap = new HashMap<>();

            list.forEach(r->{
                actionIdList.add((String)r.get("action_id"));
                JSONObject bean = actionResultMap.computeIfAbsent((String)r.get("action_id"), k -> new JSONObject());
                bean.put("val",r.get("action_id"));
                bean.put((String)r.get("REVIEW_CLASSIFY"),r.get("count"));
            });
            Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
            //返回结果数据
            List<JSONObject> actionData = new ArrayList<>();
            JSONObject totalData = new JSONObject();
            totalData.put("actionName", "全部（" + actionIdList.size() + "）");
            int totalCount = 0;
            for (Map.Entry<String, JSONObject> entry : actionResultMap.entrySet()) {
                String actionKey = entry.getKey();
                JSONObject actionJson = entry.getValue();
                int count = 0;
                for(String key: actionJson.keySet()){
                    if(!"val".equals(key)&&!"valName".equals(key)){
                        int val = actionJson.getIntValue(key);
                        totalData.put(key, totalData.getIntValue(key) + val);
                        count += val;
                    }
                }
                totalCount +=count;
                actionJson.put("actionName",actionNameMap.get(actionKey));
                actionJson.put("count",count);
                actionJson.put("totalCount",count);
                actionData.add(actionJson);
            }

            totalData.put("count", totalCount);
            totalData.put("totalCount",totalCount);
            actionData.add(0, totalData);
            //排序
            Comparator<JSONObject> comp = (o1, o2) -> o2.getIntValue("count") - o1.getIntValue("count");
            actionData = actionData.stream().sorted(comp).collect(Collectors.toList());

            return actionData;
        }else{
            return new ArrayList<>();
        }
    }

    @Override
    public List<JSONObject> module5Data(List<String> batchIdList, String secReviewStatus) throws Exception {
        // 推送到复审的数据
        QueryWrapper<MedicalUnreasonableAction> queryWrapper1 = new QueryWrapper();
        queryWrapper1.in("BATCH_ID",batchIdList);
        queryWrapper1.eq("PUSH_STATUS","1");
        if (StringUtils.isNotBlank(secReviewStatus)) {
            queryWrapper1.eq("FIR_REVIEW_STATUS",secReviewStatus);
        }
        queryWrapper1.select("ACTION_ID,count(distinct VISITID) as visitid_count,count(distinct ITEMCODE) as itemcode_count,sum(MIN_MONEY) as sum_min_money,sum(ACTION_MONEY) as sum_action_money,count(1) as count ")
                .groupBy("ACTION_ID").orderByDesc("count");
        List<Map<String, Object>> firReviewList = this.medicalUnreasonableActionService.listMaps(queryWrapper1);
        // 推送到用户报告的数据
        QueryWrapper<MedicalUnreasonableAction> queryWrapper2 = new QueryWrapper();
        queryWrapper2.in("BATCH_ID",batchIdList);
        queryWrapper2.eq("SEC_PUSH_STATUS","1");
        if (StringUtils.isNotBlank(secReviewStatus)) {
            queryWrapper2.eq("SEC_REVIEW_STATUS",secReviewStatus);
        }
        queryWrapper2.select("ACTION_ID,count(distinct VISITID) as visitid_count,count(distinct ITEMCODE) as itemcode_count,sum(MIN_MONEY) as sum_min_money,sum(ACTION_MONEY) as sum_action_money,count(1) as count ")
                .groupBy("ACTION_ID").orderByDesc("count");;
        List<Map<String, Object>> secReviewList = this.medicalUnreasonableActionService.listMaps(queryWrapper2);
        // 获取不合规行为ID翻译MAP
        Set<String> actionIdList = new HashSet<>();


        Map<String,JSONObject> actionResultMap = new HashMap<>();

        firReviewList.forEach(r->{
            actionIdList.add((String)r.get("action_id"));
            JSONObject bean = actionResultMap.computeIfAbsent((String)r.get("action_id"), k -> new JSONObject());
            bean.put("facet1",r);
            JSONObject facet2 = new JSONObject();
            facet2.put("action_id",r.get("action_id"));
            facet2.put("visitid_count",0l);
            facet2.put("itemcode_count",0l);
            facet2.put("sum_min_money",0);
            facet2.put("sum_action_money",0);
            facet2.put("count",0l);
            bean.put("facet2",facet2);
        });
        secReviewList.forEach(r->{
            actionIdList.add((String)r.get("action_id"));
            JSONObject bean = actionResultMap.computeIfAbsent((String)r.get("action_id"), k -> new JSONObject());
            bean.put("facet2",r);
            JSONObject facet2 = new JSONObject();
            facet2.put("action_id",r.get("action_id"));
            facet2.put("visitid_count",0l);
            facet2.put("itemcode_count",0l);
            facet2.put("sum_min_money",0);
            facet2.put("sum_action_money",0);
            facet2.put("count",0l);
            bean.put("facet1",facet2);
        });
        Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
        //返回结果数据
        List<JSONObject> actionData = new ArrayList<>();
        for (Map.Entry<String, JSONObject> entry : actionResultMap.entrySet()) {
            String actionKey = entry.getKey();
            JSONObject actionJson = entry.getValue();
            JSONObject facet1 = (JSONObject) JSON.toJSON(actionJson.remove("facet1"));
            JSONObject facet2 = (JSONObject) JSON.toJSON(actionJson.remove("facet2"));
            long facet1Count = facet1.getLongValue("count");
            long facet2Count = facet2.getLongValue("count");
            actionJson.put("visitidCount", facet2.getLongValue("visitid_count") - facet1.getLongValue("visitid_count"));
            actionJson.put("count", facet2Count - facet1Count);
            actionJson.put("itemcodeCount", facet2.getLongValue("itemcode_count") - facet1.getLongValue("itemcode_count"));
            actionJson.put("sumMinMoney", facet2.getDoubleValue("sum_min_money") - facet1.getDoubleValue("sum_min_money"));
            actionJson.put("sumActionMoney", facet2.getDoubleValue("sum_action_money") - facet1.getDoubleValue("sum_action_money"));
            actionJson.put("actionName", actionNameMap.get(actionKey));
            actionJson.put("val", actionKey);
            actionData.add(actionJson);
        }
        return actionData;
    }

    @Override
    public List<JSONObject> module5ExportData(List<String> batchIdList) throws Exception {
        // 构造判定状态统计数值
        List<MedicalOtherDict> statusDictList = ApiTokenCommon.queryMedicalDictListByKey("FIRST_REVIEW_STATUS");
        statusDictList.add(new MedicalOtherDict());//全部
        // 获取不合规行为ID翻译MAP
        Set<String> actionIdList = new HashSet<>();
        Map<String,JSONObject> actionResultMap = new HashMap<>();
        statusDictList.forEach(dict -> {
            String dictField ="";
            if(StringUtils.isNotBlank(dict.getCode())){
                dictField = dict.getCode()+"_";
            }
            // 推送到复审的数据
            QueryWrapper<MedicalUnreasonableAction> queryWrapper1 = new QueryWrapper();
            queryWrapper1.in("BATCH_ID",batchIdList);
            queryWrapper1.eq("PUSH_STATUS","1");
            if(StringUtils.isNotBlank(dict.getCode())){
                queryWrapper1.eq("FIR_REVIEW_STATUS",dict.getCode());
            }
            queryWrapper1.select("ACTION_ID,count(distinct VISITID) as visitid_count,count(distinct ITEMCODE) as itemcode_count,sum(MIN_MONEY) as sum_min_money,sum(ACTION_MONEY) as sum_action_money,count(1) as count ")
                    .groupBy("ACTION_ID").orderByDesc("count");
            List<Map<String, Object>> firReviewList = this.medicalUnreasonableActionService.listMaps(queryWrapper1);
            // 推送到用户报告的数据
            QueryWrapper<MedicalUnreasonableAction> queryWrapper2 = new QueryWrapper();
            queryWrapper2.in("BATCH_ID",batchIdList);
            if(StringUtils.isNotBlank(dict.getCode())){
                queryWrapper2.eq("SEC_PUSH_STATUS","1");
            }
            queryWrapper2.eq("SEC_REVIEW_STATUS",dict.getCode());
            queryWrapper2.select("ACTION_ID,count(distinct VISITID) as visitid_count,count(distinct ITEMCODE) as itemcode_count,sum(MIN_MONEY) as sum_min_money,sum(ACTION_MONEY) as sum_action_money,count(1) as count ")
                    .groupBy("ACTION_ID").orderByDesc("count");;
            List<Map<String, Object>> secReviewList = this.medicalUnreasonableActionService.listMaps(queryWrapper2);

            String finalDictField = dictField;
            firReviewList.forEach(r->{
                actionIdList.add((String)r.get("action_id"));
                JSONObject bean = actionResultMap.computeIfAbsent((String)r.get("action_id"), k -> new JSONObject());
                bean.put(finalDictField +"facet1",r);
                JSONObject facet2 = new JSONObject();
                facet2.put("action_id",r.get("action_id"));
                facet2.put("visitid_count",0l);
                facet2.put("itemcode_count",0l);
                facet2.put("sum_min_money",0.0);
                facet2.put("sum_action_money",0.0);
                facet2.put("count",0l);
                bean.put(finalDictField +"facet2",facet2);
            });

            secReviewList.forEach(r->{
                actionIdList.add((String)r.get("action_id"));
                JSONObject bean = actionResultMap.computeIfAbsent((String)r.get("action_id"), k -> new JSONObject());
                bean.put(finalDictField+"facet2",r);
                JSONObject facet2 = new JSONObject();
                facet2.put("action_id",r.get("action_id"));
                facet2.put("visitid_count",0l);
                facet2.put("itemcode_count",0l);
                facet2.put("sum_min_money",0.0);
                facet2.put("sum_action_money",0.0);
                facet2.put("count",0l);
                bean.put(finalDictField+"facet1",facet2);
            });

        });
        Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
        //返回结果数据
        List<JSONObject> actionData = new ArrayList<>();
        for (Map.Entry<String, JSONObject> entry : actionResultMap.entrySet()) {
            String actionKey = entry.getKey();
            JSONObject actionJson = entry.getValue();
            statusDictList.forEach(dict -> {
                String dictField ="";
                if(StringUtils.isNotBlank(dict.getCode())){
                    dictField = dict.getCode()+"_";
                }
                JSONObject facet1 = (JSONObject) JSON.toJSON(actionJson.remove(dictField+"facet1"));
                JSONObject facet2 = (JSONObject) JSON.toJSON(actionJson.remove(dictField+"facet2"));
                if(facet1==null){
                    facet1 = new JSONObject();
                    facet1.put("action_id",actionKey);
                    facet1.put("visitid_count",0l);
                    facet1.put("itemcode_count",0l);
                    facet1.put("sum_min_money",0.0);
                    facet1.put("sum_action_money",0.0);
                    facet1.put("count",0l);
                }
                if(facet2==null){
                    facet2 = new JSONObject();
                    facet2.put("action_id",actionKey);
                    facet2.put("visitid_count",0l);
                    facet2.put("itemcode_count",0l);
                    facet2.put("sum_min_money",0.0);
                    facet2.put("sum_action_money",0.0);
                    facet2.put("count",0l);
                }
                long facet1Count = facet1.getLongValue("count");
                long facet2Count = facet2.getLongValue("count");
                actionJson.put(dictField+"visitidCount", facet2.getLongValue("visitid_count") - facet1.getLongValue("visitid_count"));
                actionJson.put(dictField+"count", facet2Count - facet1Count);
                actionJson.put(dictField+"itemcodeCount", facet2.getLongValue("itemcode_count") - facet1.getLongValue("itemcode_count"));
                actionJson.put(dictField+"sumMinMoney", facet2.getDoubleValue("sum_min_money") - facet1.getDoubleValue("sum_min_money"));
                actionJson.put(dictField+"sumActionMoney", facet2.getDoubleValue("sum_action_money") - facet1.getDoubleValue("sum_action_money"));

            });
            actionJson.put("actionName", actionNameMap.get(actionKey));
            actionJson.put("val", actionKey);
            actionData.add(actionJson);
        }
        return actionData;
    }





    @Override
    public List<JSONObject> module6Data(List<String> batchIdList, String groupBy) throws Exception {
        Map<String,String> dictMap = new HashMap<>();
        dictMap.put("total","");
        dictMap.put("judge","white,blank,grey");
        dictMap.put("blank","blank");
        Map<String,JSONObject> actionResultMap = new HashMap<>();
        for (Map.Entry<String, String> entry : dictMap.entrySet()) {
            String type = entry.getKey();
            String dict = entry.getValue();
            QueryWrapper<MedicalUnreasonableAction> queryWrapper1 = new QueryWrapper();
            queryWrapper1.in("BATCH_ID",batchIdList);
            if(StringUtils.isNotBlank(dict)&&dict.split(",").length>1){
                queryWrapper1.in("FIR_REVIEW_STATUS",dict.split(","));
            }else if(StringUtils.isNotBlank(dict)){
                queryWrapper1.eq("FIR_REVIEW_STATUS",dict);
            }
            queryWrapper1.select(groupBy+",count(distinct VISITID) as visitid_count,sum(ACTION_MONEY) as sum_action_money,count(1) as count ")
                    .groupBy(groupBy).orderByDesc("count");
            List<Map<String, Object>> list = this.medicalUnreasonableActionService.listMaps(queryWrapper1);
            list.forEach(r->{
                JSONObject bean = actionResultMap.computeIfAbsent((String)r.get(groupBy.toLowerCase()), k -> new JSONObject());
                String dictField = type+"_";
                bean.put(dictField+"visitidCount", r.get("visitid_count"));
                bean.put(dictField+"sumActionMoney", r.get("sum_action_money"));
                bean.put(dictField+"count", r.get("count"));
                bean.put("val", r.get(groupBy.toLowerCase()));
            });
        }
        List<String> countRatioList = Arrays.asList("blank_count");
        List<String> sumActionMoneyRatioList = Arrays.asList("blank_sumActionMoney");
        //返回结果数据
        List<JSONObject> actionData = new ArrayList<>();
        for (Map.Entry<String, JSONObject> entry : actionResultMap.entrySet()) {
            String actionKey = entry.getKey();
            JSONObject actionJson = entry.getValue();
            actionJson.put("val",actionKey);
            actionJson.put("valName",actionKey);
            for (Map.Entry<String, String> dictentry : dictMap.entrySet()) {
                String type = dictentry.getKey();
                String dictField = type+"_";
                actionJson.put(dictField+"visitidCount", actionJson.getLongValue(dictField+"visitidCount"));
                actionJson.put(dictField+"sumActionMoney", actionJson.getDoubleValue(dictField+"sumActionMoney"));
                actionJson.put(dictField+"count", actionJson.getLongValue(dictField+"count"));
            }
            for(String countRatioName:countRatioList){
                if(actionJson.getLongValue(countRatioName)>0){
                    double countRatioVal = actionJson.getLongValue(countRatioName)*100 / actionJson.getLongValue("judge_count");
                    actionJson.put(countRatioName+"Ratio", (double)Math.round(countRatioVal) + "%");
                }
            }
            for(String sumActionMoneyRatioName:sumActionMoneyRatioList){
                if(actionJson.getDoubleValue(sumActionMoneyRatioName)>0){
                    double sumActionMoneyRatioVal = actionJson.getDoubleValue(sumActionMoneyRatioName) / actionJson.getDoubleValue("judge_sumActionMoney");
                    actionJson.put(sumActionMoneyRatioName+"Ratio", (double)Math.round(sumActionMoneyRatioVal * 100) + "%");
                }
            }
            actionData.add(actionJson);
        }

        JSONObject totalData = new JSONObject();
        totalData.put("valName", "全部（" + actionData.size() + "）");
        for (int i = 0, len = actionData.size(); i < len; i++) {
            JSONObject actionJson = actionData.get(i);
            for(String key: actionJson.keySet()){
                if(!"val".equals(key)&&!"valName".equals(key)&&key.indexOf("Ratio")==-1){
                    double val = actionJson.getDoubleValue(key);
                    totalData.put(key, totalData.getDoubleValue(key) + val);
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
        actionData.add(0, totalData);
        //排序
        Comparator<JSONObject> comp = (o1, o2) -> o2.getIntValue("count") - o1.getIntValue("count");
        actionData = actionData.stream().sorted(comp).collect(Collectors.toList());
        return actionData;
    }

    @Override
    public List<JSONObject> module6ExportData(List<String> batchIdList, String groupBy) throws Exception {
        Map<String,String> dictMap = new HashMap<>();
        dictMap.put("total","");
        dictMap.put("nojudge","white,blank,grey");
        dictMap.put("judge","white,blank,grey");
        dictMap.put("blank","blank");
        dictMap.put("white","white");
        dictMap.put("grey","grey");
        Map<String,JSONObject> actionResultMap = new HashMap<>();
        for (Map.Entry<String, String> entry : dictMap.entrySet()) {
            String type = entry.getKey();
            String dict = entry.getValue();
            QueryWrapper<MedicalUnreasonableAction> queryWrapper1 = new QueryWrapper();
            queryWrapper1.in("BATCH_ID",batchIdList);
            if(StringUtils.isNotBlank(dict)&&dict.split(",").length>1){
                if("nojudge".equals(type)){
                    queryWrapper1.notIn("FIR_REVIEW_STATUS",dict.split(","));
                }else{
                    queryWrapper1.in("FIR_REVIEW_STATUS",dict.split(","));
                }
            }else if(StringUtils.isNotBlank(dict)){
                queryWrapper1.eq("FIR_REVIEW_STATUS",dict);
            }
            queryWrapper1.select(groupBy+",count(distinct VISITID) as visitid_count,sum(ACTION_MONEY) as sum_action_money,count(1) as count ")
                    .groupBy(groupBy).orderByDesc("count");
            List<Map<String, Object>> list = this.medicalUnreasonableActionService.listMaps(queryWrapper1);
            list.forEach(r->{
                JSONObject bean = actionResultMap.computeIfAbsent((String)r.get(groupBy.toLowerCase()), k -> new JSONObject());
                String dictField = type+"_";
                bean.put(dictField+"visitidCount", r.get("visitid_count"));
                bean.put(dictField+"sumActionMoney", r.get("sum_action_money"));
                bean.put(dictField+"count", r.get("count"));
                bean.put("val", r.get(groupBy.toLowerCase()));
            });
        }
        List<String> countRatioList = Arrays.asList("blank_count", "white_count", "grey_count");
        List<String> sumActionMoneyRatioList = Arrays.asList("blank_sumActionMoney", "white_sumActionMoney", "grey_sumActionMoney");
        //返回结果数据
        List<JSONObject> actionData = new ArrayList<>();
        for (Map.Entry<String, JSONObject> entry : actionResultMap.entrySet()) {
            String actionKey = entry.getKey();
            JSONObject actionJson = entry.getValue();
            actionJson.put("val",actionKey);
            actionJson.put("valName",actionKey);
            for (Map.Entry<String, String> dictentry : dictMap.entrySet()) {
                String type = dictentry.getKey();
                String dictField = type+"_";
                actionJson.put(dictField+"visitidCount", actionJson.getLongValue(dictField+"visitidCount"));
                actionJson.put(dictField+"sumActionMoney", actionJson.getDoubleValue(dictField+"sumActionMoney"));
                actionJson.put(dictField+"count", actionJson.getLongValue(dictField+"count"));
            }
            for(String countRatioName:countRatioList){
                if(actionJson.getLongValue(countRatioName)>0){
                    double countRatioVal = actionJson.getLongValue(countRatioName)*100 / actionJson.getLongValue("judge_count");
                    actionJson.put(countRatioName+"Ratio", (double)Math.round(countRatioVal) + "%");
                }
            }
            for(String sumActionMoneyRatioName:sumActionMoneyRatioList){
                if(actionJson.getDoubleValue(sumActionMoneyRatioName)>0){
                    double sumActionMoneyRatioVal = actionJson.getDoubleValue(sumActionMoneyRatioName) / actionJson.getDoubleValue("judge_sumActionMoney");
                    actionJson.put(sumActionMoneyRatioName+"Ratio", (double)Math.round(sumActionMoneyRatioVal * 100) + "%");
                }
            }
            actionData.add(actionJson);
        }

        JSONObject totalData = new JSONObject();
        totalData.put("valName", "全部（" + actionData.size() + "）");
        for (int i = 0, len = actionData.size(); i < len; i++) {
            JSONObject actionJson = actionData.get(i);
            for(String key: actionJson.keySet()){
                if(!"val".equals(key)&&!"valName".equals(key)&&key.indexOf("Ratio")==-1){
                    double val = actionJson.getDoubleValue(key);
                    totalData.put(key, totalData.getDoubleValue(key) + val);
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
        actionData.add(0, totalData);
        //排序
        Comparator<JSONObject> comp = (o1, o2) -> o2.getIntValue("count") - o1.getIntValue("count");
        actionData = actionData.stream().sorted(comp).collect(Collectors.toList());
        return actionData;
    }


}
