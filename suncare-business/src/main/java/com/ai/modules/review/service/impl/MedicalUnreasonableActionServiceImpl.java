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

import com.ai.common.utils.*;
import com.ai.modules.api.util.ApiTokenCommon;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.review.dto.DynamicFieldConfig;
import com.ai.modules.review.dto.DynamicLinkProp;
import com.ai.modules.review.dto.ReviewInfoDTO;
import com.ai.modules.review.entity.MedicalUnreasonableAction;
import com.ai.modules.review.mapper.MedicalUnreasonableActionMapper;
import com.ai.modules.review.service.IMedicalUnreasonableActionService;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@DS("greenplum")
public class MedicalUnreasonableActionServiceImpl extends ServiceImpl<MedicalUnreasonableActionMapper, MedicalUnreasonableAction> implements IMedicalUnreasonableActionService {

    @Autowired
    IMedicalActionDictService medicalActionDictService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public JSONObject facetBatchCount(String batchIds) {
        List<String> batchIdArr = Arrays.asList(batchIds.split(","));
        String sql = "select sum(case when push_status='1' then 1 else 0 end) as first_push_count,\n" +
                " sum(case when sec_push_status='1' then 1 else 0 end) as push_data_count,\n" +
                " sum(case when handle_status='1' then 1 else 0 end) as news_handle_count,\n" +
                " count(1) as count,batch_id\n" +
                "from medical_unreasonable_action t where batch_id in (:batchIds) \n" +
                "group by batch_id";

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        Map<String, Object> query  = new HashMap<>();
        query.put("batchIds", batchIdArr);
        List<Map<String,Object>> list = namedParameterJdbcTemplate.queryForList(sql,query);
        JSONObject jsonObject = new JSONObject();
        list.stream().forEach(map->{
            String batchId = (String)map.get("BATCH_ID");
            Map<String,Object> resultMap = new HashMap<>();
            for(Map.Entry<String, Object> it : map.entrySet()){
                String key = StringCamelUtils.underline2Camel(it.getKey());
                if("count".equals(it.getKey())){
                    resultMap.put(key,it.getValue());
                }else{
                    Map<String,Object> countMap = new HashMap<>();
                    countMap.put("count",it.getValue());
                    resultMap.put(key,countMap);
                }
            }
            jsonObject.put(batchId,resultMap);
        });
        batchIdArr.stream().forEach(batchId->{
            if(jsonObject.get(batchId)==null){
                Map<String,Object> countMap = new HashMap<>();
                countMap.put("count",0);
                jsonObject.put(batchId,countMap);
            }
        });
        return jsonObject;
    }

    @Override
    public List<Map<String, Object>> facetActionData(QueryWrapper<MedicalUnreasonableAction> queryWrapper) {
        queryWrapper.select("ACTION_ID,ACTION_NAME, count(1) as count ")
                .groupBy("ACTION_ID","ACTION_NAME").orderByDesc("count");
        List<Map<String, Object>> list = this.listMaps(queryWrapper);
        return list;
    }

    @Override
    public IPage<MedicalUnreasonableAction> selectPageVO(Page<MedicalUnreasonableAction> page, QueryWrapper<MedicalUnreasonableAction> queryWrapper, String joinSql, String whereSql,String fields,String orderbySql) {
        return this.baseMapper.selectPageVO(page,queryWrapper,joinSql,whereSql,fields,orderbySql);
    }

    @Override
    public int selectCount(QueryWrapper<MedicalUnreasonableAction> queryWrapper,String joinSql,String whereSql,String fields){
        return this.baseMapper.selectCount(queryWrapper,joinSql,whereSql,fields);
    }

    @Override
    public IPage<Map<String,Object>> selectMapPageVO(Page<Map<String,Object>> page, QueryWrapper<MedicalUnreasonableAction> queryWrapper, String joinSql, String whereSql, String fields,String orderbySql,String linkFields) {
        IPage<Map<String,Object>> pageList = this.baseMapper.selectMapPageVO(page,queryWrapper,joinSql,whereSql,fields,orderbySql,linkFields);
        if (pageList.getRecords().size() > 0) {
            List<Map<String, Object>> list = pageList.getRecords();
            List<Map<String, Object>> records = new ArrayList<>();
            list.forEach(r -> {
                records.add(transformUpperCase(r));
            });
            pageList.setRecords(records);
        }
        return pageList;
    }

    @Override
    public List<Map<String,Object>> selectMapVO(QueryWrapper<MedicalUnreasonableAction> queryWrapper, String joinSql, String whereSql, String fields,String orderbySql,String linkFields) {
        List<Map<String, Object>> list = this.baseMapper.selectMapVO(queryWrapper,joinSql,whereSql,fields,orderbySql,linkFields);
        List<Map<String, Object>> records = new ArrayList<>();
        list.forEach(r -> {
            records.add(transformUpperCase(r));
        });
        return records;
    }

    @Override
    public List<String> getSearchSqls(String dynamicSearch,QueryWrapper<MedicalUnreasonableAction> queryWrapper,String dataSource,Map<String, Set<String>> tabFieldMap) throws ParseException {
        Map<String, JSONObject> tableSearchMap = null;
        if (dynamicSearch != null && !"{}".equals(dynamicSearch)) {
            tableSearchMap = DynamicFieldConstant.initTableSearchMap(dynamicSearch);
        }
        return getSqls(tableSearchMap,queryWrapper,dataSource,tabFieldMap);
    }

    private List<String> getSqls(Map<String, JSONObject> tableSearchMap,QueryWrapper<MedicalUnreasonableAction> queryWrapper,String dataSource,Map<String, Set<String>> tabFieldMap) throws ParseException {
        List<String> fqList = new ArrayList<>();
        Map<String,Map<String,List<String>>> leftJoinMap = new HashMap<>();
        if(tableSearchMap!=null){
            JSONObject actionSearch = tableSearchMap.remove(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
            // 添加主表条件
            if (actionSearch != null) {
                JSONObject searchJson = new JSONObject();
                Map<String, String[]> parameterMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : actionSearch.entrySet()) {
                    String fieldName = entry.getKey();
                    Object valueObj = entry.getValue();
                    if (valueObj == null || "null".equals(valueObj.toString())) {
                        continue;
                    }
                    String value = valueObj.toString().trim();
                    if (value.length() == 0) {
                        continue;
                    }
                    if(fieldName.endsWith(QueryGenerator.BEGIN)){
                        fieldName =fieldName.replace(QueryGenerator.BEGIN,"");
                        fieldName = oConvertUtils.camelName(fieldName)+QueryGenerator.BEGIN;
                    }
                    if(fieldName.endsWith(QueryGenerator.END)){
                        fieldName =fieldName.replace(QueryGenerator.END,"");
                        fieldName = oConvertUtils.camelName(fieldName)+QueryGenerator.END;
                    }
                    if(fieldName.endsWith(QueryGenerator.IN)){
                        fieldName =fieldName.replace(QueryGenerator.IN,"");
                        fieldName = oConvertUtils.camelName(fieldName)+QueryGenerator.IN;
                    }
                    if(fieldName.endsWith(QueryGenerator.NOT_IN)){
                        fieldName =fieldName.replace(QueryGenerator.NOT_IN,"");
                        fieldName = oConvertUtils.camelName(fieldName)+QueryGenerator.NOT_IN;
                    }
                    parameterMap.put(fieldName,new String[]{value});
                    searchJson.put(fieldName,valueObj);
                }
                MedicalUnreasonableAction searchVo = JSONObject.parseObject(searchJson.toJSONString(),MedicalUnreasonableAction.class);
                QueryGenerator.installMplus(queryWrapper, searchVo, parameterMap);
            }
            // 其他表JOIN条件
            for (Map.Entry<String, JSONObject> entry : tableSearchMap.entrySet()) {
                String tableName = entry.getKey();
                List<String> whereField = new ArrayList<>();
                List<String> whereSqls = initSqls(entry.getValue(),tableName+".",whereField);
                if(whereSqls.size()==0){
                    continue;
                }
                //fqList.add(getLeftJoinSql(dataSource, tableName));
                //String whereSql = StringUtils.join(whereSqls, " AND ");
                //fqList.add(whereSql);
                Map<String,List<String>> tableSqlMap = new HashMap<>();
                tableSqlMap.put("whereSql",whereSqls);
                tableSqlMap.put("linkField",getLinkFields(tableName));
                tableSqlMap.put("whereField",whereField);
                leftJoinMap.put(tableName,tableSqlMap);
            }
        }


        //关联表 join条件和select字段
        if(tabFieldMap!=null){
            for (Map.Entry<String, Set<String>> entry : tabFieldMap.entrySet()) {
                String tableName = entry.getKey();
                //fqList.add(getLeftJoinSql(dataSource, tableName));
                List<String> selectField = new ArrayList<>();
                entry.getValue().forEach(t->{
                    String field =t;
                    String asField = field;
                    if(t.indexOf(" as ")>-1){
                        field = t.split(" as ")[0];
                        asField= t.split(" as ")[1];
                    }
                    fqList.add(tableName+"."+field+" as " + tableName+"表"+ asField);
                    selectField.add(field);
                });
                Map<String,List<String>> tableSqlMap = leftJoinMap.computeIfAbsent(tableName, k -> new HashMap<>());
                tableSqlMap.put("linkField",getLinkFields(tableName));
                tableSqlMap.put("selectField",selectField);
            }
        }

        //拼接left join 多条记录只取一条用max(展示字段)
        for (Map.Entry<String, Map<String, List<String>>> entry : leftJoinMap.entrySet()) {
            String tableName = entry.getKey();
            Map<String,List<String>> tableSqlMap = entry.getValue();
            List<String> selectField = tableSqlMap.get("selectField");
            List<String> whereSql = tableSqlMap.get("whereSql");
            List<String> linkField = tableSqlMap.get("linkField");
            List<String> whereField = tableSqlMap.get("whereField");
            if(selectField==null||selectField.size()==0){
                //没有展示字段，不需要max group by
                fqList.add(getLeftJoinSql(dataSource, tableName));
                if(whereSql!=null){
                    fqList.add(StringUtils.join(whereSql, " AND "));
                }
            }else{
                fqList.add(getLeftJoinMaxSql(dataSource, tableName,new HashSet<String>(selectField),new HashSet<String>(linkField),whereSql,whereField));
                if(whereSql!=null){
                    fqList.add(StringUtils.join(whereSql, " AND "));
                }
            }
        }
        return fqList;
    }

    private String getLeftJoinSql(String dataSource, String tableName) {
        Map<String, String> linkFieldMap = DynamicFieldConstant.getLinkFields(tableName);
        String leftJoin = "left join "+"medical."+tableName+" "+tableName +" on ";
        String onSql = "";
        for (Map.Entry<String, String> linkEntry : linkFieldMap.entrySet()) {
            if(StringUtils.isNotBlank(onSql)){
                onSql +=" and ";
            }
            onSql +=" t."+linkEntry.getKey()+"="+tableName+"."+linkEntry.getValue();
        }
        return leftJoin+onSql+" and "+tableName+".project='"+dataSource+"'";
    }

    private String getLeftJoinMaxSql(String dataSource, String tableName,Set<String> selectField,Set<String> linkField,List<String> whereSql,List<String> whereField) {

        String tableSql = " ( select ";
        if(whereField!=null){
            selectField.addAll(whereField);
        }
        for(String field:selectField){
            tableSql +=" string_agg(distinct cast("+field+" as VARCHAR), ',') as "+field+",";

           // tableSql +=" max("+field+") as "+field+",";
        }
        tableSql +=StringUtils.join(linkField, ",");
        tableSql += " from "+"medical."+tableName+" "+tableName +" where "+tableName+".project='"+dataSource+"'";
        if(whereSql!=null){
            tableSql +=" AND "+StringUtils.join(whereSql, " AND ");
        }
        tableSql +=" group by "+StringUtils.join(linkField, ",");
        tableSql +=") "+tableName;
        String onSql = "";
        Map<String, String> linkFieldMap = DynamicFieldConstant.getLinkFields(tableName);
        for (Map.Entry<String, String> linkEntry : linkFieldMap.entrySet()) {
            if(StringUtils.isNotBlank(onSql)){
                onSql +=" and ";
            }
            onSql +=" t."+linkEntry.getKey()+"="+tableName+"."+linkEntry.getValue();
        }
        String leftJoin = "left join "+tableSql+" on ";
        return leftJoin+onSql;
    }

    private List<String> getLinkFields(String tableName) {
        Map<String, String> linkFieldMap = DynamicFieldConstant.getLinkFields(tableName);
        List<String> fields = new ArrayList<>();
        for (Map.Entry<String, String> linkEntry : linkFieldMap.entrySet()) {
            fields.add(linkEntry.getValue());
        }
        return fields;
    }

    private List<String> initSqls(JSONObject param,String alias,List<String> whereField) throws ParseException {
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
            // 添加 判断是否有区间值
            QueryRuleEnum rule = null;
            if (fieldName.endsWith(QueryGenerator.BEGIN)) {
                fieldName = fieldName.substring(0, fieldName.lastIndexOf("_"));
                rule = QueryRuleEnum.GE;
            }else if (fieldName.endsWith(QueryGenerator.END)) {
                fieldName = fieldName.substring(0, fieldName.lastIndexOf("_"));
                rule = QueryRuleEnum.LE;
            }else{
                rule = QueryGenerator.convert2Rule(value);
            }
            fqList.add(alias+QueryGenerator.getSingleSqlByRule(rule,fieldName,value,false)) ;
            whereField.add(fieldName);
        }
        return fqList;

    }

    @Override
    public IPage<Map<String,Object>> pageDynamicResult(Map<String, Set<String>> tabFieldMap,Set<String> resultFieldSet,IPage<Map<String,Object>> pageList) throws Exception {
        // 查询副表字段 以VISITID 关联
        if (pageList.getRecords().size() > 0 && tabFieldMap.size() > 0) {
            List<Map<String,Object>> list = pageList.getRecords();
            // 翻译白名单归因字段
            JSONObject reviewClassifyMap = ApiTokenCommon.queryOtherDictMapByType("reasontype");
            list.forEach(r -> {
                if (resultFieldSet.contains("SEC_REVIEW_CLASSIFY")) {
                    if (r.get("SEC_REVIEW_CLASSIFY") != null && !"".equals(r.get("SEC_REVIEW_CLASSIFY"))) {
                        r.put("SEC_REVIEW_CLASSIFY",reviewClassifyMap.get(r.get("SEC_REVIEW_CLASSIFY")));
                    }
                }
                if (resultFieldSet.contains("FIR_REVIEW_CLASSIFY")) {
                    if (r.get("FIR_REVIEW_CLASSIFY") != null && !"".equals(r.get("FIR_REVIEW_CLASSIFY"))) {
                        r.put("FIR_REVIEW_CLASSIFY",reviewClassifyMap.get(r.get("FIR_REVIEW_CLASSIFY")));
                    }
                }
            });
            boolean isActionNameIn = resultFieldSet.contains("ACTION_NAME");
            if (isActionNameIn) {
                List<String> actionIdList = list.stream().map(r -> String.valueOf(r.get("ACTION_ID"))).distinct().collect(Collectors.toList());
                Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
                list.forEach(r -> {
                    if (r.get("ACTION_ID") != null) {
                        String actionName = actionNameMap.get(r.get("ACTION_ID"));
                        if (actionName != null) {
                            r.put("ACTION_NAME", actionName);
                        }
                    }
                });
            }

            DynamicLinkProp dynamicLinkProp = DynamicFieldConstant.initLinkProp(tabFieldMap);
            if(list.size() > 1000){
                for (int i = 0, j, len = list.size(); i < len; i = j) {
                    j = i + 500;
                    if (j > len) {
                        j = len;
                    }
                    List<Map<String,Object>> childList = list.subList(i, j);
                    this.addFieldFromOther(childList, dynamicLinkProp);
                }
            } else {
                this.addFieldFromOther(list, dynamicLinkProp);
            }
        }

        return null;
    }


    private Map<String,Object> transformUpperCase(Map<String, Object> orgMap) {
        Map<String, Object> resultMap = new HashMap<>();
        if (orgMap == null || orgMap.isEmpty()) {
            return resultMap;
        }
        Set<String> keySet = orgMap.keySet();
        for (String key : keySet) {
            String newKey = key.toUpperCase();
            if("ID".equals(newKey)) {
                newKey = "id";
            }
            newKey = newKey.replace("表",".");
            resultMap.put(newKey, orgMap.get(key));
        }
        return resultMap;
    }

    @Override
    public void addFieldFromOther(List<Map<String,Object>> list,
                                  DynamicLinkProp dynamicLinkProp) throws Exception {
        if (dynamicLinkProp.getSingleLinkMap().size() > 0) {
            this.addFieldFromSingleField(dynamicLinkProp.getSingleLinkMap(), list);
        }

        if (dynamicLinkProp.getMultiLinkMap().size() > 0) {
            this.addAttrFromMultiField(dynamicLinkProp.getMultiLinkMap(), list);
        }
    }

    private void addFieldFromSingleField(List<Map.Entry<String, Set<String>>> tabFieldMap, List<Map<String,Object>> dataList) throws Exception {
        for (Map.Entry<String, Set<String>> entry : tabFieldMap) {
            String extraCollection = entry.getKey();
            String[] linkArray = DynamicFieldConstant.SINGLE_FIELD_LINK.get(extraCollection);

            Map<String, List<Map<String,Object>>> map = new HashMap<>();

            for (Map<String,Object> bean : dataList) {

                Object obj = bean.get(linkArray[0]);
                if (obj == null || obj.toString().length() == 0) {
                    continue;
                }
                List<Map<String,Object>> list = map.computeIfAbsent(obj.toString(), k -> new ArrayList<>());
                list.add(bean);
            }

            if (map.size() == 0) {
                continue;
            }
            StringBuilder sb = new StringBuilder(" in (");
            for(String val: map.keySet()){
                sb.append("'").append(EngineUtil.escapeQueryChars(val)).append("',");
            }
            String linkField = linkArray[1];
            String insql = linkField + sb.substring(0, sb.length() - 1) + ")";
            String fields = linkField+","+StringUtils.join(entry.getValue().toArray(), ",");
            List<Map<String,Object>> linkResults = this.jdbcTemplate.queryForList("select "+fields+ " from medical."+extraCollection +" where "+insql);
            for(Map<String,Object> linkMap:linkResults){
                Map<String,Object> linkBean = transformUpperCase(linkMap);
                String linkVal = (String) linkBean.get(linkField);
                for (Map<String,Object> bean : map.get(linkVal)) {
                    for (Map.Entry<String, Object> docEntry : linkBean.entrySet()) {
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

    private void addAttrFromMultiField(List<Map.Entry<String, Set<String>>> tabFieldMap, List<Map<String,Object>> dataList) throws Exception {
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

            Map<String, List<Map<String,Object>>> map = new HashMap<>();
            Set<String> queryStrSet = new HashSet<>();

            int fieldCount = masterFields.size();
            // 遍历结果，每条记录的几个关联字段AND, 记录间OR
            for (Map<String,Object> bean : dataList) {
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
                    sb.append(sideFields.get(i)).append("='")
                            .append(EngineUtil.escapeQueryChars(obj.toString()))
                            .append("'");
                }
                // 跳过存在关联字段为空的记录
                if (values.size() != masterFields.size()) {
                    continue;
                }

                List<Map<String,Object>> list = map.computeIfAbsent(StringUtils.join(values, "::"), k -> new ArrayList<>());
                list.add(bean);
                queryStrSet.add("(" + sb.toString() + ")");

            }

            if (map.size() == 0) {
                continue;
            }
            // 副表查询返回字段
            Set<String> fields = entry.getValue();
            Set<String> fieldSet = new HashSet<>(sideFields);
            String insql = StringUtils.join(queryStrSet, " OR ");
            String fieldSelect = StringUtils.join(fieldSet.toArray(), ",")+","+StringUtils.join(fields.toArray(), ",");
            List<Map<String,Object>> linkResults = this.jdbcTemplate.queryForList("select "+fieldSelect+ " from medical."+extraCollection +" where "+insql);

            for(Map<String,Object> linkMap:linkResults){
                Map<String,Object> linkBean = transformUpperCase(linkMap);
                String linkVal = sideFields.stream().map(r -> (String)linkBean.get(r)).collect(Collectors.joining("::"));
                List<Map<String,Object>> linkResult = map.get(linkVal);
                if (linkResult == null) {
                    continue;
                }
                for (Map<String,Object> bean : linkResult) {
                    for (String field : fields) {
                        Object val = linkMap.get(field);
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
    public List<Map<String, Object>> facetFields(QueryWrapper<MedicalUnreasonableAction> queryWrapper, String joinSql, String whereSql, String selectFields, String factFields) {
        return this.baseMapper.facetFields(queryWrapper,joinSql,whereSql,selectFields,factFields);
    }

    @Override
    public IPage<Map<String,Object>> facetFieldsPage(Page<Map<String,Object>> page,QueryWrapper<MedicalUnreasonableAction> queryWrapper, String joinSql, String whereSql, String selectFields, String factFields,String groupByFields,String orderbySql) {
        return this.baseMapper.facetFieldsPage(page,queryWrapper,joinSql,whereSql,selectFields,factFields,groupByFields,orderbySql);
    }

    @Override
    public int facetFieldsCount(QueryWrapper<MedicalUnreasonableAction> queryWrapper,String joinSql,String whereSql,String selectFields,String factFields,String groupByFields){
        return this.baseMapper.facetFieldsCount(queryWrapper,joinSql,whereSql,selectFields,factFields,groupByFields);
    }

    @Override
    public List<Map<String, Object>> facetActionData(QueryWrapper<MedicalUnreasonableAction> queryWrapper, String joinSql, String whereSql, String selectFields) {
        return this.baseMapper.facetActionData(queryWrapper,joinSql,whereSql,selectFields);
    }

    @Override
    public void resultMapping(List<Map<String, Object>> list,Set<String> resultFieldSet) {
        // 翻译白名单归因字段
        JSONObject reviewClassifyMap = ApiTokenCommon.queryOtherDictMapByType("reasontype");
        list.forEach(r -> {
            if (resultFieldSet.contains("SEC_REVIEW_CLASSIFY")) {
                if (r.get("SEC_REVIEW_CLASSIFY") != null && !"".equals(r.get("SEC_REVIEW_CLASSIFY"))) {
                    r.put("SEC_REVIEW_CLASSIFY", reviewClassifyMap.get(r.get("SEC_REVIEW_CLASSIFY")));
                }
            }
            if (resultFieldSet.contains("FIR_REVIEW_CLASSIFY")) {
                if (r.get("FIR_REVIEW_CLASSIFY") != null && !"".equals(r.get("FIR_REVIEW_CLASSIFY"))) {
                    r.put("FIR_REVIEW_CLASSIFY", reviewClassifyMap.get(r.get("FIR_REVIEW_CLASSIFY")));
                }
            }
        });
        boolean isActionNameIn = resultFieldSet.contains("ACTION_NAME");
        if (isActionNameIn) {
            List<String> actionIdList = list.stream().map(r -> String.valueOf(r.get("ACTION_ID"))).distinct().collect(Collectors.toList());
            Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
            list.forEach(r -> {
                if (r.get("ACTION_ID") != null) {
                    String actionName = actionNameMap.get(r.get("ACTION_ID"));
                    if (actionName != null) {
                        r.put("ACTION_NAME", actionName);
                    }
                }
            });
        }
    }

    @Override
    public void updateReviewStatus(QueryWrapper<MedicalUnreasonableAction> queryWrapper, String joinSql, String whereSql,String fields,ReviewInfoDTO reviewObj) {
        this.baseMapper.updateReviewStatus(queryWrapper,joinSql,whereSql,fields,reviewObj);
    }

    @Override
    public void dynamicResultExport(List<Map<String,Object>> resultList,DynamicFieldConfig fieldConfig, boolean isStep2, OutputStream os) throws Exception {
        Map<String, Set<String>> tabFieldMap = fieldConfig.getTabFieldMap();

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

            titles.add("初审判定");
            titles.add("初审判定理由");
            titles.add("初审人");
            titles.add("复审判定");
            titles.add("复审判定理由");
            titles.add("白名单归因");
            titles.add("复审人");
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
        }

        this.dynamicResultExport(resultList,fields.toArray(new String[0]), titles.toArray(new String[0]), isStep2, os);
    }

    @Override
    public void dynamicGroupExport(List<Map<String, Object>> list, String[] fields, String[] fieldTitles,Map<String, String> linkChild,List<String> groupByList,Map<String,String> fieldMapping, OutputStream os) throws Exception {
        List<Map<String,Object>> resultList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            for (Map.Entry<String, String> entry : linkChild.entrySet()) {
                map.put(entry.getValue(), map.remove(entry.getKey()));
            }
            String id = groupByList.stream().map(r -> r + ":" + EngineUtil.escapeQueryChars(map.get(r.toLowerCase()).toString()))
                    .collect(Collectors.joining(" AND "));
            map.put("id", id);
        }
        // 数据库反查不合规行为名称
        boolean isGroupActionName = groupByList.contains("ACTION_NAME");
        if(isGroupActionName){
            List<String> actionIdList = list.stream().map(r -> String.valueOf(r.get("ACTION_ID".toLowerCase()))).distinct().collect(Collectors.toList());
            Map<String, String> actionNameMap = medicalActionDictService.queryNameMapByActionIds(actionIdList);
            list.forEach(r -> {
                Object actionId = r.get("ACTION_ID".toLowerCase());
                if(actionId != null){
                    String actionName = actionNameMap.get(actionId.toString());
                    if(actionName != null){
                        r.put("ACTION_NAME",actionName);
                    } else {
                        r.put("ACTION_NAME", r.get("ACTION_NAME".toLowerCase()));
                    }
                }
            });
        }

        list.forEach(r -> {
            Map<String, Object> resultMap = new HashMap<>();
            Set<String> keySet = r.keySet();
            for (String key : keySet) {
                String newKey = fieldMapping.get(key);
                if(StringUtils.isNotBlank(newKey)){
                    resultMap.put(newKey, r.get(key));
                }else{
                    resultMap.put(key, r.get(key));
                }
            }
            resultList.add(resultMap);
        });



        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 生成一个表格
        ExportXUtils.exportExl(resultList, fieldTitles, fields, workbook, "统计分组结果");

        workbook.write(os);
        workbook.dispose();
    }

    public void dynamicResultExport(List<Map<String,Object>> resultList,String[] fields, String[] fieldTitles,boolean isStep2, OutputStream os) throws Exception {

        JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictMapByKey("FIRST_REVIEW_STATUS");
        JSONObject pushStatusMap = ApiTokenCommon.queryMedicalDictMapByKey("FIRST_PUSH_STATUS");
        JSONObject reviewClassifyMap = ApiTokenCommon.queryOtherDictMapByType("reasontype");


        Consumer<Map<String,Object>> peekFun = isStep2 ? r -> {
            if (r.get("PREDICT_LABEL") != null && !"".equals(r.get("PREDICT_LABEL"))) {
                r.put("PREDICT_LABEL", reviewStatusMap.get(r.get("PREDICT_LABEL")));
            }
            if (r.get("FIR_REVIEW_STATUS") != null && !"".equals(r.get("FIR_REVIEW_STATUS"))) {
                r.put("FIR_REVIEW_STATUS", reviewStatusMap.get(r.get("FIR_REVIEW_STATUS")));
            }
            if (r.get("SEC_REVIEW_STATUS") != null && !"".equals(r.get("SEC_REVIEW_STATUS"))) {
                r.put("SEC_REVIEW_STATUS", reviewStatusMap.get(r.get("SEC_REVIEW_STATUS")));
            }
            if (r.get("SEC_PUSH_STATUS") != null && !"".equals(r.get("SEC_PUSH_STATUS"))) {
                r.put("SEC_PUSH_STATUS", pushStatusMap.get(r.get("SEC_PUSH_STATUS")));
            }else{
                r.put("SEC_PUSH_STATUS","待推送");
            }
            if (r.get("SEC_REVIEW_CLASSIFY") != null && !"".equals(r.get("SEC_REVIEW_CLASSIFY"))) {
                r.put("SEC_REVIEW_CLASSIFY", reviewClassifyMap.get(r.get("SEC_REVIEW_CLASSIFY")));
            }
        } : r -> {
            if (r.get("PREDICT_LABEL") != null && !"".equals(r.get("PREDICT_LABEL"))) {
                r.put("PREDICT_LABEL", reviewStatusMap.get(r.get("PREDICT_LABEL")));
            }
            if (r.get("FIR_REVIEW_STATUS") != null && !"".equals(r.get("FIR_REVIEW_STATUS"))) {
                r.put("FIR_REVIEW_STATUS", reviewStatusMap.get(r.get("FIR_REVIEW_STATUS")));
            }
            if (r.get("FIR_REVIEW_CLASSIFY") != null && !"".equals(r.get("FIR_REVIEW_CLASSIFY"))) {
                r.put("FIR_REVIEW_CLASSIFY", reviewClassifyMap.get(r.get("FIR_REVIEW_CLASSIFY")));
            }
            if (r.get("PUSH_STATUS") != null && !"".equals(r.get("PUSH_STATUS"))) {
                r.put("PUSH_STATUS", pushStatusMap.getOrDefault(r.get("PUSH_STATUS"),"未通过"));
            }else{
                r.put("PUSH_STATUS","未通过");
            }
        };

        resultList.stream().peek(peekFun).collect(Collectors.toList());

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
    public String importReviewStatus(MultipartFile file, MedicalUnreasonableActionVo searchObj) throws Exception {
        List<List<String>> list = ExcelXUtils.readSheet(0, 0, file.getInputStream());
        List<String> titles = list.remove(0);
        int idIndex = titles.indexOf("记录ID");
        int reviewStatusIndex = titles.indexOf("判定结果");
        int reviewRemarkIndex = titles.indexOf("判定理由");
        int pushStatusIndex = titles.indexOf("推送状态");
        int reviewClassifyIndex = titles.indexOf("白名单归因");
        if (idIndex == -1) {
            throw new Exception("缺少“记录ID”列");
        }
        if (reviewStatusIndex == -1) {
            throw new Exception("缺少“判定结果”列");
        }
        if (reviewRemarkIndex == -1) {
            throw new Exception("缺少“判定理由”列");
        }
        if (pushStatusIndex == -1) {
            throw new Exception("缺少“通过状态”列");
        }


        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        MedicalUnreasonableAction commonBean = new MedicalUnreasonableAction();
        commonBean.setFirReviewUserid(user.getId());
        commonBean.setFirReviewUsername(user.getRealname());
        commonBean.setFirReviewTime(TimeUtil.getNowTime());
        commonBean.setPushUserid(user.getId());
        commonBean.setPushUsername(user.getRealname());

        JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictNameMapByKey("FIRST_REVIEW_STATUS");
        JSONObject pushStatusMap = ApiTokenCommon.queryMedicalDictNameMapByKey("FIRST_PUSH_STATUS");
        JSONObject reviewClassifyMap = ApiTokenCommon.queryOtherDictNameMapByType("reasontype");
        pushStatusMap.put("未通过", "");


        Map<String, AtomicInteger> reviewStatusCountMap = new HashMap<>();
        Map<String, AtomicInteger> pushStatusMapCountMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : reviewStatusMap.entrySet()) {
            reviewStatusCountMap.put(entry.getValue().toString(), new AtomicInteger());
        }
        for (Map.Entry<String, Object> entry : pushStatusMap.entrySet()) {
            pushStatusMapCountMap.put(entry.getValue().toString(), new AtomicInteger());
        }

        BiFunction<List<List<String>>, MedicalUnreasonableAction, Exception> actionFun = (dataList, commonBean1) -> {

            try {
                List<MedicalUnreasonableAction> updateList = new ArrayList<>();
                for (List<String> record : dataList) {
                    if (reviewStatusIndex >= record.size()) {
                        continue;
                    }
                    String reviewStatusVal = record.get(reviewStatusIndex);
                    String reviewStatus = reviewStatusMap.getOrDefault(reviewStatusVal, "init").toString();

                    reviewStatusCountMap.get(reviewStatus).incrementAndGet();
                    String pushStatus = pushStatusIndex < record.size() ?
                            pushStatusMap.getOrDefault(record.get(pushStatusIndex), "").toString()
                            : "";
                    pushStatusMapCountMap.get(pushStatus).incrementAndGet();
                    String reviewRemark = reviewRemarkIndex < record.size() ?record.get(reviewRemarkIndex): "";

                    String reviewClassify = reviewClassifyIndex > -1 && reviewClassifyIndex < record.size() ? record.get(reviewClassifyIndex): "";

                    if(StringUtils.isNotBlank(reviewClassify)){
                        reviewClassify = reviewClassifyMap.getString(reviewClassify);
                        if(reviewClassify == null){
                            reviewClassify = "";
                        }
                    }
                    MedicalUnreasonableAction updateBean = new MedicalUnreasonableAction();
                    BeanUtils.copyProperties(commonBean1,updateBean);
                    updateBean.setId(record.get(idIndex));
                    updateBean.setFirReviewStatus(reviewStatus);
                    updateBean.setFirReviewRemark(reviewRemark);
                    updateBean.setFirReviewClassify(reviewClassify);
                    updateBean.setPushStatus(pushStatus);
                    if(!"1".equals(pushStatus)){
                        updateBean.setSecPushStatus("0");
                        updateBean.setHandleStatus("");
                    }
                    updateList.add(updateBean);

                }
                this.updateBatchById(updateList);

                return null;
            } catch (Exception e) {
                return e;
            }
        };
        if (list.size() > 200000) {

            ThreadUtils.ASYNC_POOL.addFirstImport(searchObj, list.size(), (processFunc) -> {
                Exception e = actionFun.apply(list, commonBean);
                if (e == null) {
                    List<String> msg = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : reviewStatusMap.entrySet()) {
                        msg.add(entry.getKey() + "：" + reviewStatusCountMap.get(entry.getValue().toString()));
                    }
                    for (Map.Entry<String, Object> entry : pushStatusMap.entrySet()) {
                        msg.add(entry.getKey() + "：" + pushStatusMapCountMap.get(entry.getValue().toString()));
                    }
                    processFunc.accept(list.size());
                    return Result.ok("数据量：" + list.size() + "，" + StringUtils.join(msg, "，") );
                } else {
                    return Result.error(e.getMessage());
                }
            });
            return null;
        } else {
            Exception e = actionFun.apply(list, commonBean);
            if (e == null) {
                List<String> msg = new ArrayList<>();
                for (Map.Entry<String, Object> entry : reviewStatusMap.entrySet()) {
                    msg.add(entry.getKey() + "：" + reviewStatusCountMap.get(entry.getValue().toString()));
                }
                for (Map.Entry<String, Object> entry : pushStatusMap.entrySet()) {
                    msg.add(entry.getKey() + "：" + pushStatusMapCountMap.get(entry.getValue().toString()));
                }
                return "数据量：" + list.size() + "，" + StringUtils.join(msg, "，");
            } else {
                throw e;
            }

        }
    }

    @Override
    public String importReviewStatusSec(MultipartFile file, MedicalUnreasonableActionVo searchObj) throws Exception {
        List<List<String>> list = ExcelXUtils.readSheet(0, 0, file.getInputStream());
        List<String> titles = list.remove(0);
        int idIndex = titles.indexOf("记录ID");
        int reviewStatusIndex = titles.indexOf("复审判定");
        int reviewClassifyIndex = titles.indexOf("白名单归因");
        int reviewRemarkIndex = titles.indexOf("复审判定理由");
        if (idIndex == -1) {
            throw new Exception("缺少“记录ID”列");
        }
        if (reviewStatusIndex == -1) {
            throw new Exception("缺少“复审判定”列");
        }
        if (reviewRemarkIndex == -1) {
            throw new Exception("缺少“复审判定理由”列");
        }


        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        MedicalUnreasonableAction commonBean = new MedicalUnreasonableAction();
        commonBean.setSecReviewUserid(user.getId());
        commonBean.setSecReviewUsername(user.getRealname());
        commonBean.setSecReviewTime(TimeUtil.getNowTime());

        JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictNameMapByKey("FIRST_REVIEW_STATUS");
        JSONObject reviewClassifyMap = ApiTokenCommon.queryOtherDictNameMapByType("reasontype");


        Map<String, AtomicInteger> reviewStatusCountMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : reviewStatusMap.entrySet()) {
            reviewStatusCountMap.put(entry.getValue().toString(), new AtomicInteger());
        }

        BiFunction<List<List<String>>, MedicalUnreasonableAction, Exception> actionFun = (dataList, commonBean1) -> {

            try {
                List<MedicalUnreasonableAction> updateList = new ArrayList<>();
                for (List<String> record : dataList) {
                    if (reviewStatusIndex >= record.size()) {
                        continue;
                    }
                    String reviewStatusVal = record.get(reviewStatusIndex);
                    String reviewStatus = reviewStatusMap.getOrDefault(reviewStatusVal, "init").toString();

                    reviewStatusCountMap.get(reviewStatus).incrementAndGet();

                    String reviewRemark = reviewRemarkIndex < record.size() ? record.get(reviewRemarkIndex) : "";

                    String reviewClassify = reviewClassifyIndex > -1 && reviewClassifyIndex < record.size() ? record.get(reviewClassifyIndex): "";
                    if(StringUtils.isNotBlank(reviewClassify)){
                        reviewClassify = reviewClassifyMap.getString(reviewClassify);
                        if(reviewClassify == null){
                            reviewClassify = "";
                        }
                    }
                    MedicalUnreasonableAction updateBean = new MedicalUnreasonableAction();
                    BeanUtils.copyProperties(commonBean1,updateBean);
                    updateBean.setId(record.get(idIndex));
                    updateBean.setSecReviewStatus(reviewStatus);
                    updateBean.setSecReviewRemark(reviewRemark);
                    updateBean.setSecReviewClassify(reviewClassify);
                    updateList.add(updateBean);
                }
                this.updateBatchById(updateList);

                return null;
            } catch (Exception e) {
                return e;
            }
        };
        if (list.size() > 200000) {

            ThreadUtils.ASYNC_POOL.addSecImport(searchObj, list.size(), (processFunc) -> {
                Exception e = actionFun.apply(list, commonBean);
                if (e == null) {
                    List<String> msg = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : reviewStatusMap.entrySet()) {
                        msg.add(entry.getKey() + "：" + reviewStatusCountMap.get(entry.getValue().toString()));
                    }
                    processFunc.accept(list.size());
                    return Result.ok("数据量：" + list.size() + "，" + StringUtils.join(msg, "，") );
                } else {
                    return Result.error(e.getMessage());
                }
            });
            return null;
        } else {
            Exception e = actionFun.apply(list, commonBean);
            if (e == null) {
                List<String> msg = new ArrayList<>();
                for (Map.Entry<String, Object> entry : reviewStatusMap.entrySet()) {
                    msg.add(entry.getKey() + "：" + reviewStatusCountMap.get(entry.getValue().toString()));
                }
                return "数据量：" + list.size() + "，" + StringUtils.join(msg, "，");
            } else {
                throw e;
            }

        }
    }

    @Override
    public String importGroupReviewStatus(MultipartFile file, MedicalUnreasonableAction searchObj, String dynamicSearch, HttpServletRequest req) throws Exception{
        List<List<String>> dataList = ExcelXUtils.readSheet(0, 0, file.getInputStream());
        List<String> titles = dataList.remove(0);
        int idIndex = titles.indexOf("记录ID");
        int reviewStatusIndex = titles.indexOf("判定结果");
        int countIndex = titles.indexOf("数量");
        int reviewClassifyIndex = titles.indexOf("白名单归因");
        int reviewRemarkIndex = titles.indexOf("判定理由");
        if (idIndex == -1) {
            throw new Exception("缺少“记录ID”列");
        }
        if (reviewStatusIndex == -1) {
            throw new Exception("缺少“判定结果”列");
        }
        if (reviewRemarkIndex == -1) {
            throw new Exception("缺少“判定理由”列");
        }

        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ReviewInfoDTO reviewObj = new ReviewInfoDTO();
        reviewObj.setFirReviewUserid(user.getId());
        reviewObj.setFirReviewUsername(user.getRealname());
        reviewObj.setFirReviewTime(TimeUtil.getNowTime());

        JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictNameMapByKey("FIRST_REVIEW_STATUS");
        JSONObject reviewClassifyMap = ApiTokenCommon.queryOtherDictNameMapByType("reasontype");

        String reviewStatusCache;
        int totalCount = countIndex == -1 ? -1 : 0;
        Map<String, List<String>> map = new HashMap<>();
        for (List<String> record : dataList) {
            if (reviewStatusIndex >= record.size()
                    || StringUtils.isBlank(reviewStatusCache = record.get(reviewStatusIndex))
            ) {
                continue;
            }
            Object statusObj = reviewStatusMap.get(reviewStatusCache);
            if (statusObj == null) {
                throw new Exception("判定结果不存在：" + reviewStatusCache);
            }

            String fqStr = record.get(idIndex);
            String reviewRemark = reviewRemarkIndex < record.size() ? record.get(reviewRemarkIndex) : "";
            String reviewClassify = reviewClassifyIndex > -1 && reviewClassifyIndex < record.size() ? record.get(reviewClassifyIndex) : "";

            map.computeIfAbsent(reviewStatusCache + "::" + reviewRemark + "::" + reviewClassify, k -> new ArrayList<>()).add(fqStr);

            if (countIndex > -1) {
                totalCount += Integer.parseInt(record.get(countIndex));
            }
        }

        Function<Consumer<Integer>, Object> actionFun = (processFunc) -> {
            Map<String, Integer> statusCountMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String key = entry.getKey();
                int index = key.indexOf("::");
                int lastIndex = key.lastIndexOf("::");
                String reviewStatusName = key.substring(0, index);
                String reviewStatus = reviewStatusMap.get(reviewStatusName).toString();
                String reviewRemark = key.substring(index + 2, lastIndex);
                String reviewClassify = key.substring(lastIndex + 2);
                if(StringUtils.isNotBlank(reviewClassify)){
                    reviewClassify = reviewClassifyMap.getString(reviewClassify);
                    if(reviewClassify == null){
                        reviewClassify = "";
                    }
                }

                ReviewInfoDTO updateBean = new ReviewInfoDTO();
                BeanUtils.copyProperties(reviewObj,updateBean);
                updateBean.setFirReviewStatus(reviewStatus);
                updateBean.setFirReviewRemark(reviewRemark);
                updateBean.setFirReviewClassify(reviewClassify);

                List<String> itemList = entry.getValue();
                itemList = itemList.stream().map(r->{
                    r = r.replaceAll(":","='").replaceAll(" AND ","' AND ")+"'";
                    return r;
                }).collect(Collectors.toList());
                for (int i = 0, j, len = itemList.size(); i < len; i = j) {
                    j = i + 500;
                    if (j > len) {
                        j = len;
                    }
                    //分组记录id
                    String groupIdSql = "(" + StringUtils.join(itemList.subList(i, j), ") OR (") + ")";


                    // 构造动态查询条件
                    Map<String, String[]> parameterMap = new HashMap(req.getParameterMap());
                    parameterMap.remove(QueryGenerator.ORDER_COLUMN);
                    // 构造主表条件
                    QueryWrapper<MedicalUnreasonableAction> queryWrapper =  QueryGenerator.initQueryWrapper(searchObj, parameterMap);
                    queryWrapper.apply(" not (FIR_REVIEW_STATUS='" + reviewStatus + "' AND FIR_REVIEW_CLASSIFY='" + reviewClassify +"') and ("+groupIdSql+")");

                    List<String> searchFqs = null;
                    try {
                        searchFqs = this.getSearchSqls(dynamicSearch,queryWrapper,user.getDataSource(),null);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String joinSql = StringUtils.join(searchFqs.stream().filter(t->t.startsWith("left join")).collect(Collectors.toList()), " ");
                    String whereSql = StringUtils.join(searchFqs.stream().filter(t->!t.startsWith("left join")).collect(Collectors.toList())," AND ");
                    //left join 字段
                    Set<String> resultFieldSet = DynamicFieldConstant.resultLinkFieldSet(dynamicSearch);
                    String fields = "id";
                    if(resultFieldSet.size()>0){
                        fields = "t."+StringUtils.join(resultFieldSet.toArray(), ",t.");
                    }
                    int count = this.selectCount(queryWrapper,joinSql,whereSql,fields);
                    try {
                        this.updateReviewStatus(queryWrapper,joinSql,whereSql, fields,updateBean);
                        statusCountMap.put(reviewStatusName, statusCountMap.getOrDefault(reviewStatusName, 0) + count);
                    } catch (Exception e) {
                        return e;
                    }
                }

            }
            return statusCountMap;
        };

        if (totalCount == -1 || totalCount > 200000) {
            int finalTotalCount = totalCount;
            MedicalUnreasonableActionVo searchObjVo = new MedicalUnreasonableActionVo();
            BeanUtils.copyProperties(searchObj,searchObjVo);
            ThreadUtils.ASYNC_POOL.addFirstGroupImport(searchObjVo, totalCount, (processFunc) -> {
                Object e = actionFun.apply(processFunc);
                if (e instanceof Exception) {
                    return Result.error(((Exception) e).getMessage());
                } else {
                    List<String> msg = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : ((Map<String, Integer>) e).entrySet()) {
                        msg.add(entry.getKey() + "：" + entry.getValue());
                    }
                    return Result.ok("数据量：" + (finalTotalCount == -1 ? "未知" : finalTotalCount) + "，" + StringUtils.join(msg, "，"));
                }
            });
            return null;
        } else {

            Object e = actionFun.apply(count -> {
            });
            if (e instanceof Exception) {
                throw (Exception) e;
            } else {
                List<String> msg = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : ((Map<String, Integer>) e).entrySet()) {
                    msg.add(entry.getKey() + "：" + entry.getValue());
                }
                return "数据量：" + totalCount + "，" + StringUtils.join(msg, "，");

            }

        }
    }

    @Override
    public String importGroupReviewStatusSec(MultipartFile file, MedicalUnreasonableAction searchObj, String dynamicSearch, HttpServletRequest req) throws Exception{
        List<List<String>> dataList = ExcelXUtils.readSheet(0, 0, file.getInputStream());
        List<String> titles = dataList.remove(0);
        int idIndex = titles.indexOf("记录ID");
        int reviewStatusIndex = titles.indexOf("判定结果");
        int countIndex = titles.indexOf("数量");
        int reviewClassifyIndex = titles.indexOf("白名单归因");
        int reviewRemarkIndex = titles.indexOf("判定理由");
        if (idIndex == -1) {
            throw new Exception("缺少“记录ID”列");
        }
        if (reviewStatusIndex == -1) {
            throw new Exception("缺少“判定结果”列");
        }
        if (reviewRemarkIndex == -1) {
            throw new Exception("缺少“判定理由”列");
        }

        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ReviewInfoDTO reviewObj = new ReviewInfoDTO();
        reviewObj.setSecReviewUserid(user.getId());
        reviewObj.setSecReviewUsername(user.getRealname());
        reviewObj.setSecReviewTime(TimeUtil.getNowTime());

        JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictNameMapByKey("FIRST_REVIEW_STATUS");
        JSONObject reviewClassifyMap = ApiTokenCommon.queryOtherDictNameMapByType("reasontype");

        String reviewStatusCache;
        int totalCount = countIndex == -1 ? -1 : 0;
        Map<String, List<String>> map = new HashMap<>();
        for (List<String> record : dataList) {
            if (reviewStatusIndex >= record.size()
                    || StringUtils.isBlank(reviewStatusCache = record.get(reviewStatusIndex))
            ) {
                continue;
            }
            Object statusObj = reviewStatusMap.get(reviewStatusCache);
            if (statusObj == null) {
                throw new Exception("判定结果不存在：" + reviewStatusCache);
            }

            String fqStr = record.get(idIndex);
            String reviewRemark = reviewRemarkIndex < record.size() ? record.get(reviewRemarkIndex) : "";
            String reviewClassify = reviewClassifyIndex > -1 && reviewClassifyIndex < record.size() ? record.get(reviewClassifyIndex) : "";

            map.computeIfAbsent(reviewStatusCache + "::" + reviewRemark+ "::" + reviewClassify, k -> new ArrayList<>()).add(fqStr);

            if (countIndex > -1) {
                totalCount += Integer.parseInt(record.get(countIndex));
            }
        }

        Function<Consumer<Integer>, Object> actionFun = (processFunc) -> {
            Map<String, Integer> statusCountMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String key = entry.getKey();
                int index = key.indexOf("::");
                int lastIndex = key.lastIndexOf("::");
                String reviewStatusName = key.substring(0, index);
                String reviewStatus = reviewStatusMap.get(reviewStatusName).toString();
                String reviewRemark = key.substring(index + 2, lastIndex);
                String reviewClassify = key.substring(lastIndex + 2);
                if(StringUtils.isNotBlank(reviewClassify)){
                    reviewClassify = reviewClassifyMap.getString(reviewClassify);
                    if(reviewClassify == null){
                        reviewClassify = "";
                    }
                }

                ReviewInfoDTO updateBean = new ReviewInfoDTO();
                BeanUtils.copyProperties(searchObj,updateBean);
                updateBean.setSecReviewStatus(reviewStatus);
                updateBean.setSecReviewRemark(reviewRemark);
                updateBean.setSecReviewClassify(reviewClassify);

                List<String> itemList = entry.getValue();
                itemList = itemList.stream().map(r->{
                    r = r.replaceAll(":","='").replaceAll(" AND ","' AND ")+"'";
                    return r;
                }).collect(Collectors.toList());
                for (int i = 0, j, len = itemList.size(); i < len; i = j) {
                    j = i + 500;
                    if (j > len) {
                        j = len;
                    }
                    //分组记录id
                    String groupIdSql = "(" + StringUtils.join(itemList.subList(i, j), ") OR (") + ")";


                    // 构造动态查询条件
                    Map<String, String[]> parameterMap = new HashMap(req.getParameterMap());
                    parameterMap.remove(QueryGenerator.ORDER_COLUMN);
                    // 构造主表条件
                    QueryWrapper<MedicalUnreasonableAction> queryWrapper =  QueryGenerator.initQueryWrapper(searchObj, parameterMap);
                    queryWrapper.eq("PUSH_STATUS","1");
                    queryWrapper.apply(" not (SEC_REVIEW_STATUS='" + reviewStatus + "' AND SEC_REVIEW_CLASSIFY='" + reviewClassify +"') and ("+groupIdSql+")");

                    List<String> searchFqs = null;
                    try {
                        searchFqs = this.getSearchSqls(dynamicSearch,queryWrapper,user.getDataSource(),null);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String joinSql = StringUtils.join(searchFqs.stream().filter(t->t.startsWith("left join")).collect(Collectors.toList()), " ");
                    String whereSql = StringUtils.join(searchFqs.stream().filter(t->!t.startsWith("left join")).collect(Collectors.toList())," AND ");
                    //left join 字段
                    Set<String> resultFieldSet = DynamicFieldConstant.resultLinkFieldSet(dynamicSearch);
                    String fields = "id";
                    if(resultFieldSet.size()>0){
                        fields = "t."+StringUtils.join(resultFieldSet.toArray(), ",t.");
                    }
                    int count = this.selectCount(queryWrapper,joinSql,whereSql,fields);
                    try {
                        this.updateReviewStatus(queryWrapper,joinSql,whereSql, fields,updateBean);
                        statusCountMap.put(reviewStatusName, statusCountMap.getOrDefault(reviewStatusName, 0) + count);
                    } catch (Exception e) {
                        return e;
                    }
                }

            }
            return statusCountMap;
        };




        if (totalCount == -1 || totalCount > 200000) {
            int finalTotalCount = totalCount;
            MedicalUnreasonableActionVo searchObjVo = new MedicalUnreasonableActionVo();
            BeanUtils.copyProperties(searchObj,searchObjVo);
            ThreadUtils.ASYNC_POOL.addSecGroupImport(searchObjVo, totalCount, (processFunc) -> {
                Object e = actionFun.apply(processFunc);
                if (e instanceof Exception) {
                    return Result.error(((Exception) e).getMessage());
                } else {
                    List<String> msg = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : ((Map<String, Integer>) e).entrySet()) {
                        msg.add(entry.getKey() + "：" + entry.getValue());
                    }
                    return Result.ok("数据量：" + (finalTotalCount == -1 ? "未知" : finalTotalCount) + "，" + StringUtils.join(msg, "，"));
                }
            });
            return null;
        } else {

            Object e = actionFun.apply(count -> {
            });
            if (e instanceof Exception) {
                throw (Exception) e;
            } else {
                List<String> msg = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : ((Map<String, Integer>) e).entrySet()) {
                    msg.add(entry.getKey() + "：" + entry.getValue());
                }
                return "数据量：" + totalCount + "，" + StringUtils.join(msg, "，");

            }

        }
    }

}
