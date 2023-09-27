package com.ai.modules.review.service.impl;

import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.review.dto.DynamicLinkProp;
import com.ai.modules.task.entity.TaskActionFieldCol;
import com.alibaba.fastjson.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 风控结果动态字段 常量
 */
public class DynamicFieldConstant {

    public static Map<String, Map<String, String>> MULTI_FIELD_LINK = new HashMap<>();
    public static Map<String, String[]> SINGLE_FIELD_LINK = new HashMap<>();
//    private static Map<String, String> MULTI_FIELD_LINK_DEFAULT = new HashMap<>();

    public static Map<String, String[]> GROUP_SINGLE_FIELD_LINK = new HashMap<>();


    public static Set<String> NOT_MULTI_VALUE = new HashSet<>();

    static {

        // 多字段关联
        Map<String, String> DWS_ONEBED_MOREPATIENTS = new HashMap<>();
        DWS_ONEBED_MOREPATIENTS.put("ORGID", "ORGID");
        DWS_ONEBED_MOREPATIENTS.put("DEPTNAME_SRC", "DEPTNAME_SRC");
        DWS_ONEBED_MOREPATIENTS.put("ETL_SOURCE", "ETL_SOURCE");
        DWS_ONEBED_MOREPATIENTS.put("CHARGEDATE", "DURATION");

        Map<String, String> DWS_ITEMEXCHANGE1_DIFFITEM = new HashMap<>();
        DWS_ITEMEXCHANGE1_DIFFITEM.put("ITEMCODE", "YB_ITEMCODE");
        DWS_ITEMEXCHANGE1_DIFFITEM.put("ITEMCODE_SRC", "YB_ITEMCODE_SRC");
        DWS_ITEMEXCHANGE1_DIFFITEM.put("HIS_ITEMCODE", "HIS_ITEMCODE");
        DWS_ITEMEXCHANGE1_DIFFITEM.put("HIS_ITEMCODE_SRC", "HIS_ITEMCODE_SRC");
        DWS_ITEMEXCHANGE1_DIFFITEM.put("ITEMNAME", "YB_ITEMNAME");
        DWS_ITEMEXCHANGE1_DIFFITEM.put("ITEMNAME_SRC", "YB_ITEMNAME_SRC");
        DWS_ITEMEXCHANGE1_DIFFITEM.put("HIS_ITEMNAME", "HIS_ITEMNAME");
        DWS_ITEMEXCHANGE1_DIFFITEM.put("HIS_ITEMNAME_SRC", "HIS_ITEMNAME_SRC");

        Map<String, String> DWB_CHARGE_DETAIL = new HashMap<>();
        DWB_CHARGE_DETAIL.put("VISITID", "VISITID");
        DWB_CHARGE_DETAIL.put("ITEMCODE", "ITEMCODE");

        MULTI_FIELD_LINK.put(EngineUtil.DWB_CHARGE_DETAIL, DWB_CHARGE_DETAIL);
        MULTI_FIELD_LINK.put("DWS_ONEBED_MOREPATIENTS", DWS_ONEBED_MOREPATIENTS);
        MULTI_FIELD_LINK.put("DWS_ITEMEXCHANGE1_DIFFITEM", DWS_ITEMEXCHANGE1_DIFFITEM);
        // 单字段关联
        SINGLE_FIELD_LINK.put("DWB_CHRONIC_PATIENT", new String[]{"CLIENTID", "CLIENTID"});
        SINGLE_FIELD_LINK.put("DWB_SETTLEMENT", new String[]{"VISITID", "VISITID"});
        // 就诊ID关联
        SINGLE_FIELD_LINK.put("DWB_MASTER_INFO", new String[]{"VISITID", "VISITID"});
        SINGLE_FIELD_LINK.put("STD_ORGANIZATION", new String[]{"VISITID", "VISITID"});
        SINGLE_FIELD_LINK.put("DWB_DIAG", new String[]{"VISITID", "VISITID"});
        SINGLE_FIELD_LINK.put("DWS_ZYAPART_DAYS", new String[]{"VISITID", "ZY_ID_THIS"});
        SINGLE_FIELD_LINK.put("DWS_CLINIC_INHOSPITAL", new String[]{"VISITID", "MZ_ID_INHOSPITAL"});
        SINGLE_FIELD_LINK.put("DWS_INHOSPITAL_OVERLAP", new String[]{"VISITID", "ZY_ID_OTHER"});
        SINGLE_FIELD_LINK.put("DWS_DOCTORDEATH_PRACTICE_DETAIL", new String[]{"VISITID", "VISITID"});
        SINGLE_FIELD_LINK.put("DWS_CLIENTDEATH_VISIT_DETAIL", new String[]{"VISITID", "VISITID"});
        SINGLE_FIELD_LINK.put("DWS_RXAPART_DAYS", new String[]{"VISITID", "VISITID_THIS"});
        SINGLE_FIELD_LINK.put("APP_FK_1VISIT_GROUPQTY", new String[]{"ITEM_ID", "id"});
//        SINGLE_FIELD_LINK.put("APP_FK_1VISIT_GROUPQTY", new String[]{"VISITID", "VISITID"});
        SINGLE_FIELD_LINK.put("DWS_PATIENT_1VISIT_ITEMSUM", new String[]{"VISITID", "VISITID"});
        SINGLE_FIELD_LINK.put("DWS_PATIENT_1VISIT_TREATCLASSSUM", new String[]{"VISITID", "VISITID"});
        SINGLE_FIELD_LINK.put("DWS_PATIENT_1VISIT_DRUGCLASSSUM", new String[]{"VISITID", "VISITID"});


        // ITEM_ID关联
        SINGLE_FIELD_LINK.put("DWS_ORDER_HISITEM_DIFF_DETAIL", new String[]{"ITEM_ID", "id"});
        SINGLE_FIELD_LINK.put("DWS_ORDER_YBITEM_DIFF_DETAIL", new String[]{"ITEM_ID", "id"});
        SINGLE_FIELD_LINK.put("DWS_HISITEMQTYMOREORDER_DETAIL", new String[]{"ITEM_ID", "id"});
        SINGLE_FIELD_LINK.put("DWS_YBITEMQTYMOREORDER_DETAIL", new String[]{"ITEM_ID", "id"});
        SINGLE_FIELD_LINK.put("DWS_ITEMEXCHANGE2_MORECHARGE", new String[]{"ITEM_ID", "id"});
        SINGLE_FIELD_LINK.put("DWS_ITEMPRICE_HIGHER_DETAIL", new String[]{"ITEM_ID", "id"});
        SINGLE_FIELD_LINK.put("DWS_WEST_TCM", new String[]{"ITEM_ID", "id"});
        SINGLE_FIELD_LINK.put("APP_FK_INHOSPITAL_TOGETHER", new String[]{"ITEM_ID", "id"});
        SINGLE_FIELD_LINK.put("APP_FK_PRESCRIPTLATECHARGE", new String[]{"ITEM_ID", "id"});
        SINGLE_FIELD_LINK.put("APP_FK_INHOSPITAL_FAMILY", new String[]{"ITEM_ID", "id"});
        SINGLE_FIELD_LINK.put("DWS_ITEMEXCHANGE1_DETAIL", new String[]{"ITEM_ID", "id"});

        // 关联查询的属性值不拼接的表
        NOT_MULTI_VALUE.add("APP_FK_INHOSPITAL_TOGETHER");
        NOT_MULTI_VALUE.add("APP_FK_PRESCRIPTLATECHARGE");
        NOT_MULTI_VALUE.add("APP_FK_INHOSPITAL_FAMILY");

        GROUP_SINGLE_FIELD_LINK.put("APP_FK_INHOSPITAL_TOGETHER", new String[]{"TOGETHERID", "GROUPID"});
        GROUP_SINGLE_FIELD_LINK.put("STD_ORGANIZATION", new String[]{"ORGID", "ORGID"});
        GROUP_SINGLE_FIELD_LINK.put("DWB_DOCTOR", new String[]{"DOCTORID", "DOCTORID"});
        GROUP_SINGLE_FIELD_LINK.put("DWB_CLIENT", new String[]{"CLIENTID", "CLIENTID"});

    }

    private static List<TaskActionFieldCol> fixColList;

    static {
        fixColList = new ArrayList<>();
        String resultCollection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        String[][] tableColTitleArray = {
                {
                        resultCollection, EngineUtil.DWB_MASTER_INFO, resultCollection
                        , resultCollection, resultCollection, resultCollection
                        , resultCollection, resultCollection, "DWS_CLINIC_INHOSPITAL"
                        , "DWS_CLINIC_INHOSPITAL", resultCollection, EngineUtil.DWB_DIAG
                        , EngineUtil.DWB_DIAG
                },
                {
                        "ORGNAME", "ALIA_CASE_ID", "YB_VISITID"
                        , "CLIENTNAME", "SEX", "YEARAGE"
                        , "VISITTYPE", "VISITDATE", "ADMITDATE_THIS"
                        , "LEAVEDATE_THIS", "ZY_DAYS", "DEPTNAME_SRC"
                        , "DISEASENAME_SRC"
                },
                {
                        "医疗机构名称", "病案号", "原始就诊ID"
                        , "患者姓名", "性别", "年龄（岁）"
                        , "就诊类型", "就诊时间", "入院日期"
                        , "出院日期", "住院天数", "科室名称（原始）"
                        , "疾病诊断名称（原始）"
                }
        };

        for (int i = 0, len = tableColTitleArray[1].length; i < len; i++) {
            TaskActionFieldCol col = new TaskActionFieldCol();
            col.setTableName(tableColTitleArray[0][i]);
            col.setColName(tableColTitleArray[1][i]);
            col.setColCnname(tableColTitleArray[2][i]);
            fixColList.add(col);
        }

    }

    public static Map<String, JSONObject> initTableSearchMap(String dynamicSearch) {
        JSONObject searchJson = JSONObject.parseObject(dynamicSearch);
        Map<String, JSONObject> tableSearchMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : searchJson.entrySet()) {
            int spIndex = entry.getKey().indexOf(".");
            String tableName = entry.getKey().substring(0, spIndex);
            String colName = entry.getKey().substring(spIndex + 1);
            if (colName.startsWith("ALIA")) {
                colName = colName.substring(colName.indexOf("_") + 1);
            }
            JSONObject json = tableSearchMap.computeIfAbsent(tableName, k -> new JSONObject());
            json.put(colName, entry.getValue());
        }
        return tableSearchMap;
    }

    public static Map<String, String> getLinkFields(String tableName) {
        Map<String, String> linkFieldMap = MULTI_FIELD_LINK.get(tableName);
        if (linkFieldMap == null) {
            String[] linkFields = SINGLE_FIELD_LINK.get(tableName);
            linkFieldMap = new HashMap<>();
            linkFieldMap.put(linkFields[0], linkFields[1]);

        }
        return linkFieldMap;
    }

    public static Map<String, Set<String>> getSplitTableField(String fields,String type) {
        // 分割表和字段
        Map<String, Set<String>> tabFieldMap = new HashMap<>();
        for (String filed : fields.split(",")) {
            String[] tabFiledArray = filed.split("\\.");
            if (tabFiledArray.length == 1) {
                tabFiledArray = new String[]{EngineUtil.MEDICAL_UNREASONABLE_ACTION, tabFiledArray[0]};
            }
            if ("action".equals(tabFiledArray[0])) {
                continue;
            }
            if (tabFiledArray[1].startsWith("ALIA")) {
                if(!"solr".equals(type)){
                    tabFiledArray[1] = tabFiledArray[1].substring(tabFiledArray[1].indexOf("_") + 1) + " as "+tabFiledArray[1];
                }else{
                    tabFiledArray[1] = tabFiledArray[1] + ":" + tabFiledArray[1].substring(tabFiledArray[1].indexOf("_") + 1);
                }

            }
            Set<String> fieldList = tabFieldMap.computeIfAbsent(tabFiledArray[0], k -> new HashSet<>());
            fieldList.add(tabFiledArray[1]);
        }
        return tabFieldMap;
    }

    public static DynamicLinkProp initLinkProp(Map<String, Set<String>> tabFieldMap) throws Exception {
        List<Map.Entry<String, Set<String>>> multiLinkMap = new ArrayList<>();
        List<Map.Entry<String, Set<String>>> singleLinkMap = new ArrayList<>();
        Set<String> resultFieldSet = new HashSet<>();

        for (Map.Entry<String, Set<String>> entry : tabFieldMap.entrySet()) {
            String tableName = entry.getKey();
            if (DynamicFieldConstant.SINGLE_FIELD_LINK.containsKey(tableName)) {
                singleLinkMap.add(entry);
            } else if (DynamicFieldConstant.MULTI_FIELD_LINK.containsKey(tableName)) {
                multiLinkMap.add(entry);
            } else {
                throw new Exception(tableName + "未设置关联信息，请联系管理员");
            }
        }

        // 多字段连接，关联字段添加
        if (multiLinkMap.size() > 0) {
            Set<String> sideFieldSet = multiLinkMap.stream()
                    .map(e -> DynamicFieldConstant.MULTI_FIELD_LINK.get(e.getKey()))
                    .flatMap(r -> Arrays.stream(r.keySet().toArray(new String[0])))
                    .collect(Collectors.toSet());
            resultFieldSet.addAll(sideFieldSet);
        }

        if (singleLinkMap.size() > 0) {
            Set<String> sideFieldSet = singleLinkMap.stream()
                    .map(e -> DynamicFieldConstant.SINGLE_FIELD_LINK.get(e.getKey())[0])
                    .collect(Collectors.toSet());
            resultFieldSet.addAll(sideFieldSet);
        }
        DynamicLinkProp prop = new DynamicLinkProp();
        prop.setMultiLinkMap(multiLinkMap);
        prop.setSingleLinkMap(singleLinkMap);
        prop.setLinkFieldSet(resultFieldSet);
        return prop;
    }

     public static Set<String> resultFieldSet(Map<String, Set<String>> tabFieldMap) throws Exception{
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

         DynamicLinkProp dynamicLinkProp = DynamicFieldConstant.initLinkProp(tabFieldMap);
         // 添加反查关联字段
         resultFieldSet.addAll(dynamicLinkProp.getLinkFieldSet());
         // 关联必查
         resultFieldSet.add("id");
         return resultFieldSet;
     }

     public static Set<String> resultLinkFieldSet(String dynamicSearch){
         Set<String> resultFields = new HashSet<>();
         Map<String, JSONObject> tableSearchMap = null;
         if (dynamicSearch != null && !"{}".equals(dynamicSearch)) {
             tableSearchMap = DynamicFieldConstant.initTableSearchMap(dynamicSearch);
         }
         if(tableSearchMap!=null){
             for (Map.Entry<String, JSONObject> entry : tableSearchMap.entrySet()) {
                 String tableName = entry.getKey();
                 if(EngineUtil.MEDICAL_UNREASONABLE_ACTION.equals(tableName)){
                     continue;
                 }
                 Map<String, String> linkFieldMap = DynamicFieldConstant.getLinkFields(tableName);
                 for (Map.Entry<String,String> linkFieldMapEntry : linkFieldMap.entrySet()) {
                     resultFields.add(linkFieldMapEntry.getKey());
                 }
             }
         }
         return resultFields;
     }

    // 分组统计使用
    public static Set<String> getFromOtherField(Map<String, Set<String>> tabFieldMap) {
        String[] defaultArray = {null};

        Set<String> fields = tabFieldMap.keySet().stream().map(extraCollection ->
                DynamicFieldConstant.GROUP_SINGLE_FIELD_LINK.getOrDefault(extraCollection, defaultArray)[0]
        ).filter(Objects::nonNull).collect(Collectors.toSet());

        return fields;
    }

    public static List<TaskActionFieldCol> toAddFixCol(List<TaskActionFieldCol> colList) {
        colList = colList.stream().filter(col -> fixColList.stream().noneMatch(
                r -> r.getTableName().equals(col.getTableName()) && r.getColName().equals(col.getColName())
        )).collect(Collectors.toList());
        colList.addAll(0, fixColList);
        return colList;
    }
}
