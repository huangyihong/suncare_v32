package com.ai.modules.medical.service.impl;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ExcelXUtils;
import com.ai.common.utils.ExportXUtils;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ReflectHelper;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.medical.service.IMedicalRuleConfigChargeService;
import com.ai.modules.medical.vo.MedicalChargeRuleConfigIO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Description: 通用规则配置
 * @Author: jeecg-boot
 * @Date: 2020-12-14
 * @Version: V1.0
 */
@Slf4j
@Service
public class MedicalRuleConfigChargeServiceImpl extends MedicalRuleConfigCommonServiceImpl implements IMedicalRuleConfigChargeService {

    private static Map<String, LimitInfo> exportInfoMap;
    private static ListMultimap<String, DictInfo> otherDictFieldMap;
    private static ListMultimap<String, DictInfo> medicalDictFieldMap;

    private static String[] ruleConditionFields;
    private static Set<String> accessConditionFields;

    static {
//    private void initTable() {
        otherDictFieldMap = ImmutableListMultimap.<String, DictInfo>builder()
                .put("sex", new DictInfo("sex"))
                .put("accessSex", new DictInfo("sex"))
                .put("visittype", new DictInfo("VisitType", "|"))
                .put("insurancetype", new DictInfo("medinsuranceType", "|"))
                .put("hosplevel", new DictInfo("HospLevel", "|"))
                .put("accessHosplevel", new DictInfo("HospLevel", "|"))
                .put("orgType", new DictInfo("Medical_Org_type", "|"))
                .put("accessOrgType", new DictInfo("Medical_Org_type", "|"))
                .put("dept", new DictInfo("Department", "|"))
                .build();
        medicalDictFieldMap = ImmutableListMultimap.<String, DictInfo>builder()
                .put("age", new DictInfo("AGE_UNIT", 2))
                .put("accessAge", new DictInfo("AGE_UNIT", 2))
                .put("fitTimeRange", new DictInfo("FIT_TIME_RANGE"))
                .put("unExpense", new DictInfo("YESNO"))
                .put("unCharge", new DictInfo("YESNO"))
                .put("accessVisittype", new DictInfo("freq1Visittype"))
                .build();

        String[][] commmonFields = new String[][]{
                {"id主键", "主体项目类型", "项目/项目组编码", "项目/项目组名称", "规则类型", "规则级别","级别备注","不合规行为"},
                {"ruleId", "itemTypes", "itemCodes", "itemNames", "ruleLimit", "ruleGrade", "ruleGradeRemark", "actionName"},
                {
//                    "政策依据类别",
                        "政策依据", "所属地区", "提示信息", "适用时间", "状态(启用、禁用)","操作原因", "更改标识(1新增0修改2删除)"},
                {
//                    "ruleBasisType",
                        "ruleBasis", "ruleSource", "message", "dataTimes", "status", "updateReason", "updateActionType"}
        };

        exportInfoMap = ImmutableMap.<String, LimitInfo>builder()
                .put("freq", new LimitInfo(null, new String[][]{
                        {
                                "频次比较符", "频次数量"
                                , "是否等于准入疾病组", "准入疾病组编码", "准入疾病组名称", "准入条件关系"
                                , "是否等于准入项目组", "准入项目组编码", "准入项目组名称"},
                        {
                                "frequencyCompare", "frequencyExt2"
                                , "accessDiseaseGroupCompare", "accessDiseaseGroupExt1", "accessDiseaseGroupExt1Names", "accessProjectGroupLogic"
                                , "accessProjectGroupCompare", "accessProjectGroupExt1", "accessProjectGroupExt1Names"
                        }
                }))
                .put("freq1", new LimitInfo(null, new String[][]{
                        {"频次比较符", "频次数量"
                                , "是否等于准入疾病组", "准入疾病组编码", "准入疾病组名称", "准入条件关系"
                                , "是否等于准入项目组", "准入项目组编码", "准入项目组名称","准入就诊类型"},
                        {"frequencyCompare", "frequencyExt2"
                                , "accessDiseaseGroupCompare", "accessDiseaseGroupExt1", "accessDiseaseGroupExt1Names", "accessProjectGroupLogic"
                                , "accessProjectGroupCompare", "accessProjectGroupExt1", "accessProjectGroupExt1Names","accessVisittypeExt1"}
                }))
                .put("sex", new LimitInfo(null, new String[][]{
                        {"性别"}, {"sexExt1"}
                }))
                .put("age", new LimitInfo(null, new String[][]{
                        {"年龄范围", "年龄单位"}, {"ageExt1", "ageExt2"}
                }))
                .put("visittype", new LimitInfo(null, new String[][]{
                        {"就医方式"}, {"visittypeExt1"}
                }))

                .put("insurancetype", new LimitInfo(null, new String[][]{
                        {"参保类型"}, {"insurancetypeExt1"}
                }))

                .put("hosplevel", new LimitInfo(null, new String[][]{
                        {"医院级别", "医疗机构类型"}, {"hosplevelExt1", "orgTypeExt1"}
                }))

                .put("dept", new LimitInfo(null, new String[][]{
                        {"准入医院级别", "准入条件关系", "准入医疗机构类型", "科室名称"},
                        {"accessHosplevelExt1","accessOrgTypeLogic", "accessOrgTypeExt1", "deptExt1"}
                }))
                .put("dayUnfitGroups", new LimitInfo(null, new String[][]{
                        {"一日互斥项目组编码(间隔符\"|\")", "一日互斥项目组名称"},
                        {"dayUnfitGroupsExt1", "dayUnfitGroupsExt1Names"}
                }))

                .put("unfitGroups", new LimitInfo(null, new String[][]{
                        {"互斥项目组编码(间隔符\"|\")", "互斥项目组名称"},
                        {"unfitGroupsExt1", "unfitGroupsExt1Names"}
                }))

                .put("indication", new LimitInfo(null, new String[][]{
                        {
                                "准入性别", "准入条件关系", "准入年龄范围", "准入年龄单位"
                                , "判定条件组1", "疾病组1编码", "疾病组1名称", "项目组1编码", "项目组1名称", "化验结果1"
                                , "判定条件组2", "疾病组2编码", "疾病组2名称", "项目组2编码", "项目组2名称", "化验结果2"
                                , "判定条件组3", "疾病组3编码", "疾病组3名称", "项目组3编码", "项目组3名称", "化验结果3"
                        },
                        {
                                "accessSexExt1", "accessAgeLogic", "accessAgeExt1", "accessAgeExt2"
                                , "indication0Ext1", "indication0Ext2", "indication0Ext2Names", "indication0Ext3", "indication0Ext3Names", "indication0Ext4"
                                , "indication1Ext1", "indication1Ext2", "indication1Ext2Names", "indication1Ext3", "indication1Ext3Names", "indication1Ext4"
                                , "indication2Ext1", "indication2Ext2", "indication2Ext2Names", "indication2Ext3", "indication2Ext3Names", "indication2Ext4"
                        }
                }))

                .put("fitGroups", new LimitInfo(null, new String[][]{
                        {"时间范围", "项目组编码(组内或组外且)", "项目组名称", "药品组编码(组内或组外且)", "药品组名称"},
                        {"fitTimeRangeExt1", "fitGroupsExt1", "fitGroupsExt1Names", "fitGroupsExt3", "fitGroupsExt3Names"}
                }))

                .put("unExpense", new LimitInfo(null, new String[][]{
                        {"不能报销"}, {"unExpenseExt1"}
                }))

                .put("unCharge", new LimitInfo(null, new String[][]{
                        {"不能收费"}, {"unChargeExt1"}
                }))
                .build();

        for (LimitInfo info : exportInfoMap.values()) {
            List<String> titleList = new ArrayList<>();
            titleList.addAll(Arrays.asList(commmonFields[0]));
            titleList.addAll(Arrays.asList(info.getTitles()));
            titleList.addAll(Arrays.asList(commmonFields[2]));
            info.setTitles(titleList.toArray(new String[0]));

            List<String> fieldList = new ArrayList<>();
            fieldList.addAll(Arrays.asList(commmonFields[1]));
            fieldList.addAll(Arrays.asList(info.getFields()));
            fieldList.addAll(Arrays.asList(commmonFields[3]));
            info.setFields(fieldList.toArray(new String[0]));

        }

        ruleConditionFields = new String[]{
                "frequency", "accessDiseaseGroup", "accessProjectGroup", "age", "sex", "visittype", "insurancetype"
                , "hosplevel", "orgType", "dept", "dayUnfitGroups", "unfitGroups", "indication", "accessSex", "accessAge", "accessHosplevel"
                , "fitTimeRange", "fitGroups", "unExpense", "unCharge", "accessOrgType","accessVisittype"
        };

        accessConditionFields = new HashSet<>(
                Arrays.asList("accessDiseaseGroup", "accessProjectGroup", "accessSex", "accessAge", "accessHosplevel", "accessOrgType","accessVisittype")
        );
    }


    @Override
    public void exportExcel(QueryWrapper<MedicalRuleConfig> queryWrapper, OutputStream os) throws Exception {
//        this.initTable();
        List<MedicalChargeRuleConfigIO> list = this.medicalRuleConfigService.listChargeIO(queryWrapper);
        List<MedicalRuleConditionSet> conditionSetList = new ArrayList<>();
        // 分页查询条件
        List<String> ruleIds = list.stream().map(MedicalChargeRuleConfigIO::getRuleId).collect(Collectors.toList());
        for (int i = 0, j, len = ruleIds.size(); i < len; i = j) {
            j = i + 1000;
            if (j > len) {
                j = len;
            }
            conditionSetList.addAll(medicalRuleConditionSetService.list(
                    new QueryWrapper<MedicalRuleConditionSet>()
                            .orderByAsc("RULE_ID", "GROUP_NO", "ORDER_NO")
                            .in("RULE_ID", ruleIds.subList(i, j))
            ));
        }

        List<String> conditionFieldList = Arrays.asList(ruleConditionFields);
        List<String> ioFieldList = Arrays.stream(MedicalChargeRuleConfigIO.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
        // 筛选出需要赋值给IO类的字段
        List<String> conditionFieldInList = conditionFieldList.stream().map(f ->
                ioFieldList.stream().filter(r -> r.startsWith(f) && !r.endsWith("Name")
                        && !r.endsWith("Names") && !startWithNum.apply(r.substring(f.length()))).toArray(String[]::new))
                .flatMap(Arrays::stream).collect(Collectors.toList());

        SimpleDateFormat dataTimeSdf = new SimpleDateFormat("yyyy-MM-dd");

        // 各种限定条件归类
        Map<String, List<MedicalChargeRuleConfigIO>> limitListMap = new HashMap<>();

        Map<String, String> ruleLimitDict = medicalDictService.queryMapByType(this.RULE_LIMIT_DICT);
        Map<String, String> relationDict = medicalDictService.queryMapByType("CASE_RELATION_CHAR");
        Map<String, String> statusDict = medicalDictService.queryMapByType("SWITCH_STATUS");
        Map<String, String> actionDict = this.getActionMap();

        for (MedicalChargeRuleConfigIO bean : list) {
            String ruleId = bean.getRuleId();

            String startTime = bean.getStartTime() == null ? "2000-01-01" : dataTimeSdf.format(bean.getStartTime());
            String endTime = bean.getEndTime() == null ? "2099-12-31" : dataTimeSdf.format(bean.getEndTime());
            String ruleLimit = bean.getRuleLimit();
            bean.setDataTimes(startTime + "到" + endTime);
            bean.setUpdateActionType("0");
            bean.setRuleLimit(ruleLimitDict.get(ruleLimit));
            // 已保存名称
//            bean.setRuleSource(medicalOtherDictService.getValueByCode("region", bean.getRuleSource()));
            bean.setActionName(actionDict.getOrDefault(bean.getActionId(), bean.getActionId()));
            bean.setStatus(statusDict.get(bean.getStatus()));

            if("PROJECTGRP".equals(bean.getItemTypes())){
                bean.setItemTypes("项目组");
            }
            if("PROJECT".equals(bean.getItemTypes())||StringUtils.isBlank(bean.getItemTypes())){
                bean.setItemTypes("项目");
            }

            int endIndex = -1, index = 0;
            List<MedicalRuleConditionSet> conditionSetChildList = new ArrayList<>();
            Iterator<MedicalRuleConditionSet> conditionSetIterator = conditionSetList.iterator();
            while (conditionSetIterator.hasNext()) {
                MedicalRuleConditionSet conditionBean = conditionSetIterator.next();
                if (ruleId.equals(conditionBean.getRuleId())) {
                    endIndex = index;
                    conditionSetChildList.add(conditionBean);
                    conditionSetIterator.remove();
                } else if (endIndex != -1) {
                    break;
                }
                index++;
            }
            // 处理翻译规则里的数据
            for (MedicalRuleConditionSet conditionBean : conditionSetChildList) {
                String field = conditionBean.getField();
                if (otherDictFieldMap.containsKey(field)) {
                    List<DictInfo> dictInfos = otherDictFieldMap.get(field);
                    for (DictInfo dictInfo : dictInfos) {
                        String dictCode = dictInfo.getDictCode();
                        String propName = dictInfo.getExt();
                        Object valueObj = ReflectHelper.getValue(conditionBean, propName);
                        if (valueObj == null || "".equals(valueObj.toString())) {
                            continue;
                        }
                        String value = valueObj.toString();
                        if (dictInfo.getMulti()) {
                            String separator = dictInfo.getSeparator();
                            String propValue = medicalValidService.transMedicalOtherDictCodes(value, new String[]{separator}, dictCode);
                            ReflectHelper.setValue(conditionBean, propName, propValue);
                        } else {
                            String propValue = medicalOtherDictService.getValueByCode(dictCode, value);
                            ReflectHelper.setValue(conditionBean, propName, propValue);
                        }
                    }

                }

                if (medicalDictFieldMap.containsKey(field)) {
                    List<DictInfo> dictInfos = medicalDictFieldMap.get(field);
                    for (DictInfo dictInfo : dictInfos) {
                        String dictCode = dictInfo.getDictCode();
                        String propName = dictInfo.getExt();
                        Object valueObj = ReflectHelper.getValue(conditionBean, propName);
                        if (valueObj == null || "".equals(valueObj.toString())) {
                            continue;
                        }
                        String value = valueObj.toString();
                        if (dictInfo.getMulti()) {
                            String separator = dictInfo.getSeparator();
                            String propValue = medicalValidService.transMedicalDictCodes(value, new String[]{separator}, dictCode);
                            ReflectHelper.setValue(conditionBean, propName, propValue);
                        } else {
                            String propValue = medicalDictService.queryDictTextByKey(dictCode, value);
                            ReflectHelper.setValue(conditionBean, propName, propValue);
                        }
                    }
                }
            }


            // 设置输出bean中的条件组字段
            for (MedicalRuleConditionSet conditionBean : conditionSetChildList) {
                String field = conditionBean.getField();
                if (conditionFieldList.contains(field)) {
                    List<String> propList = conditionFieldInList.stream().filter(r -> r.startsWith(field)).collect(Collectors.toList());
                    for (String propField : propList) {
                        String propFieldName = propField.replace(field, "");
                        propFieldName = propFieldName.substring(0, 1).toLowerCase() + propFieldName.substring(1);
                        Object value = ReflectHelper.getValue(conditionBean, propFieldName);
                        if("logic".equals(propFieldName) && value != null){
                            value = relationDict.get(value.toString());
                        }
                        ReflectHelper.setValue(bean, propField, value);
                    }
                    if ("indication".equals(field)) {
                        // 设置当前分组的值
                        ReflectHelper.setValue(bean, "indication" + conditionBean.getOrderNo() + "Ext1", bean.getIndicationExt1());
                        ReflectHelper.setValue(bean, "indication" + conditionBean.getOrderNo() + "Ext2", bean.getIndicationExt2());
                        ReflectHelper.setValue(bean, "indication" + conditionBean.getOrderNo() + "Ext3", bean.getIndicationExt3());
                        ReflectHelper.setValue(bean, "indication" + conditionBean.getOrderNo() + "Ext4", bean.getIndicationExt4());
                    }
                }

            }

            limitListMap.computeIfAbsent(ruleLimit, k -> new ArrayList<>()).add(bean);
        }


        // 翻译疾病组
        List<String> diseaseGroupOldList = new ArrayList<>();
        for (MedicalChargeRuleConfigIO bean : list) {
            diseaseGroupOldList.add(bean.getAccessDiseaseGroupExt1());
            diseaseGroupOldList.add(bean.getIndication0Ext2());
            diseaseGroupOldList.add(bean.getIndication1Ext2());
            diseaseGroupOldList.add(bean.getIndication2Ext2());
        }

        List<String> diseaseGroupNewList = medicalValidService.transDiseaseGroupCodes(diseaseGroupOldList, new String[]{"|", ","});

        int index = 0;
        for (MedicalChargeRuleConfigIO bean : list) {
            bean.setAccessDiseaseGroupExt1Names(diseaseGroupNewList.get(index++));
            bean.setIndication0Ext2Names(diseaseGroupNewList.get(index++));
            bean.setIndication1Ext2Names(diseaseGroupNewList.get(index++));
            bean.setIndication2Ext2Names(diseaseGroupNewList.get(index++));
        }

        // 翻译项目组
        List<String> treatGroupOldList = new ArrayList<>();
        for (MedicalChargeRuleConfigIO bean : list) {
            treatGroupOldList.add(bean.getAccessProjectGroupExt1());
            treatGroupOldList.add(bean.getDayUnfitGroupsExt1());
            treatGroupOldList.add(bean.getUnfitGroupsExt1());
            treatGroupOldList.add(bean.getIndication0Ext3());
            treatGroupOldList.add(bean.getIndication1Ext3());
            treatGroupOldList.add(bean.getIndication2Ext3());
            treatGroupOldList.add(bean.getFitGroupsExt1());
        }

        List<String> treatGroupNewList = medicalValidService.transTreatGroupCodes(treatGroupOldList, new String[]{"|", ","});

        index = 0;
        for (MedicalChargeRuleConfigIO bean : list) {
            bean.setAccessProjectGroupExt1Names(treatGroupNewList.get(index++));
            bean.setDayUnfitGroupsExt1Names(treatGroupNewList.get(index++));
            bean.setUnfitGroupsExt1Names(treatGroupNewList.get(index++));
            bean.setIndication0Ext3Names(treatGroupNewList.get(index++));
            bean.setIndication1Ext3Names(treatGroupNewList.get(index++));
            bean.setIndication2Ext3Names(treatGroupNewList.get(index++));
            bean.setFitGroupsExt1Names(treatGroupNewList.get(index++));
        }

        // 翻译药品组
        List<String> transCodeList = new ArrayList<>();
        for (MedicalChargeRuleConfigIO bean : list) {
            transCodeList.add(bean.getFitGroupsExt3());
        }

        transCodeList = medicalValidService.transDrugGroupCodes(transCodeList, new String[]{"|", ","});

        index = 0;
        for (MedicalChargeRuleConfigIO bean : list) {
            bean.setFitGroupsExt3Names(transCodeList.get(index++));
        }


        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 导出数据
        for (Map.Entry<String, List<MedicalChargeRuleConfigIO>> entry : limitListMap.entrySet()) {
            String sheetCode = entry.getKey();
            int numIndex = startWithNumIndex.apply(sheetCode);
            if (numIndex == -1) {
                numIndex = entry.getKey().length();
            }
            String limit = entry.getKey().substring(0, numIndex);
            LimitInfo limitInfo = exportInfoMap.get(sheetCode);
            if(limitInfo==null){
                limitInfo = exportInfoMap.get(limit);
            }

            if (limitInfo == null) {
                continue;
            }

            String title = ruleLimitDict.get(sheetCode);
            ExportXUtils.exportExl(entry.getValue(), MedicalChargeRuleConfigIO.class, limitInfo.getTitles(), limitInfo.getFields(), workbook, title);

        }


        workbook.write(os);
        workbook.dispose();

    }


    @Transactional
    @Override
    public Result importExcel(MultipartFile file) throws Exception {
        String[] sheetNames = ExcelXUtils.readSheetNames(file.getInputStream());
        List<MedicalRuleConfig> addList = new ArrayList<>();
        List<MedicalRuleConfig> updateList = new ArrayList<>();
        List<String> delList = new ArrayList<>();

        List<MedicalRuleConditionSet> conditionSetAddList = new ArrayList<>();

        List<String> conditionFieldList = Arrays.asList(ruleConditionFields);

        /*List<String> ioFieldList = Arrays.stream(MedicalChargeRuleConfigIO.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());

        // 筛选出需要赋值给IO类的字段
        List<String> conditionFieldInList = conditionFieldList.stream().map(f ->
                ioFieldList.stream().filter(r -> r.startsWith(f) && !r.endsWith("Name") && !r.endsWith("Names")).toArray(String[]::new))
                .flatMap(Arrays::stream).collect(Collectors.toList());

        conditionFieldInList.add("frequencyExt1");*/


        Map<String, String> ruleLimitDict = medicalDictService.queryNameMapByType(this.RULE_LIMIT_DICT);

        int sheetIndex = -1;
        for (String sheetName : sheetNames) {
            sheetIndex++;
            String sheetCode = ruleLimitDict.get(sheetName);

            List<String> errorMsgList = new ArrayList<>();

            if (sheetCode == null) {
                throw new Exception("sheet页-" + sheetName + ":不存在规则类型-" + sheetName);
            }

            int numIndex = startWithNumIndex.apply(sheetCode);
            if (numIndex == -1) {
                numIndex = sheetCode.length();
            }

            String limitCode = sheetCode.substring(0, numIndex);

            LimitInfo limitInfo = exportInfoMap.get(sheetCode);
            if(limitInfo==null){
                limitInfo = exportInfoMap.get(limitCode);
            }

            if (limitInfo == null) {
                continue;
            }

            List<MedicalChargeRuleConfigIO> list = ExcelXUtils.readSheet(MedicalChargeRuleConfigIO.class, limitInfo.getFields(), sheetIndex, 1, file.getInputStream());
            List<MedicalRuleConditionSet> conditionSetList = new ArrayList<>();
            Set<String> itemCodesSet = new HashSet<>();//主体项目编码

            // 按照字段顺序筛选， groupNo跟页面字段顺序相同
            List<String> conditionFieldInList = Arrays.stream(limitInfo.getFields()).filter(r ->
                    !r.endsWith("Name") && !r.endsWith("Names") && conditionFieldList.stream().anyMatch(r::startsWith)
            ).collect(Collectors.toList());

            if("freq".equals(limitCode)){
                conditionFieldInList.add("frequencyExt1");
            }
            SimpleDateFormat dataTimeSdf = new SimpleDateFormat("yyyy-MM-dd");

            Map<String, String> actionDict = this.getActionNameMap();

            Map<String, String> limitToFreqDict = medicalDictService.queryMapByType("ACTION_TO_FREQ");
            Map<String, String> relationDict = medicalDictService.queryNameMapByType("CASE_RELATION_CHAR");
            Map<String, String> statusDict = medicalDictService.queryNameMapByType("SWITCH_STATUS");

            int index = 1;  // 标题栏算第一行
            for (MedicalChargeRuleConfigIO bean : list) {
                index++;
                String indexMsg = "第" + index + "行";
                String updateActionType = bean.getUpdateActionType();
                if ("1".equals(updateActionType) || bean.getRuleId() == null || bean.getRuleId().length() == 0) {
                    bean.setRuleId(IdUtils.uuid());
                    addList.add(bean);
                } else if ("0".equals(updateActionType) || updateActionType == null) {
                    updateList.add(bean);
                } else if ("2".equals(updateActionType)) {
                    delList.add(bean.getRuleId());
                    continue;
                }

                String actionName = bean.getActionName();
                if (StringUtils.isBlank(actionName)) {
                    errorMsgList.add(indexMsg + "：不合规行为不能为空");
                } else {
                    String actionId = actionDict.get(actionName);
                    if (actionId == null) {
                        errorMsgList.add(indexMsg + "：不合规行为不存在：" + bean.getActionName());
                    } else {
                        bean.setActionId(actionId);
                    }
                }

                String ruleLimit = bean.getRuleLimit();
                if (StringUtils.isBlank(ruleLimit)) {
                    errorMsgList.add(indexMsg + "：规则类型不能为空");
                } else {
                    String ruleLimitCode = ruleLimitDict.get(ruleLimit);
                    if (ruleLimitCode == null) {
                        errorMsgList.add(indexMsg + "：规则类型不存在：" + bean.getRuleLimit());
                    } else {
                        bean.setRuleLimit(ruleLimitCode);
                        if ("freq".equals(limitCode)) {
                            for (Map.Entry<String, String> entry : limitToFreqDict.entrySet()) {
                                if (ruleLimit.contains(entry.getKey())) {
                                    String[] vals = entry.getValue().split("_");
                                    bean.setFrequencyExt1(vals[1]);
                                    break;
                                }
                            }
                        }

                    }
                }

               /* String ruleBasisType = bean.getRuleBasisType();
                if (StringUtils.isBlank(ruleBasisType)) {
                    errorMsgList.add(indexMsg + "：政策依据类型不能为空");
                } else {
                    String ruleBasisTypeCode = medicalOtherDictService.getCodeByValue("rule_sourcetype", bean.getRuleBasisType());
                    if (ruleBasisTypeCode == null) {
                        errorMsgList.add(indexMsg + "：政策依据类型不存在：" + ruleBasisType);
                    } else {
                        bean.setRuleBasisType(ruleBasisTypeCode);
                    }
                }*/


                bean.setActionType(RULE_TYPE);
                bean.setRuleType(RULE_TYPE);
                if (bean.getRuleSource() != null && bean.getRuleSource().length() > 0) {
                    bean.setRuleSourceCode(medicalOtherDictService.getCodeByValue("region", bean.getRuleSource()));
                }
                if (bean.getRuleSourceCode() == null) {
                    errorMsgList.add(indexMsg + "：所属地区为空或不存在");

                }

                String status = bean.getStatus();
                if(StringUtils.isBlank(status)){
                    bean.setStatus(MedicalConstant.SWITCH_NORMAL);
                } else {
                    bean.setStatus(statusDict.get(status));
                    if(bean.getStatus() == null){
                        errorMsgList.add(indexMsg + "：状态不存在-" + status);
                    }
                }

                if (bean.getMessage() == null || bean.getMessage().length() == 0) {
                    errorMsgList.add(indexMsg + "：提示信息为空");

                }

                // 设置数据有效时间
                String[] dataTimeArray;
                String dataTimes = bean.getDataTimes();
                if (dataTimes != null && (dataTimes = dataTimes.replaceAll(" ", "")).length() > 0) {
                    dataTimeArray = dataTimes.split("到");
                    if (dataTimeArray.length != 2) {
                        if (dataTimeArray.length == 1) {
                            dataTimeArray = new String[]{dataTimeArray[0], "2099-12-31"};
                        } else {
                            errorMsgList.add(indexMsg + "：适用时间区间值不正确：" + dataTimes);
                            dataTimeArray = new String[]{"2000-01-01", "2099-12-31"};
                        }
                    }
                } else {
                    dataTimeArray = new String[]{"2000-01-01", "2099-12-31"};
                }
                try {
                    bean.setStartTime(dataTimeSdf.parse(dataTimeArray[0]));
                    bean.setEndTime(dataTimeSdf.parse(dataTimeArray[1]));
                } catch (Exception e) {
                    errorMsgList.add(indexMsg + "：适用时间格式不正确：" + dataTimes);
                }

                if("项目组".equals(bean.getItemTypes())){
                    bean.setItemTypes("PROJECTGRP");
                }
                if("项目".equals(bean.getItemTypes())){
                    bean.setItemTypes("PROJECT");
                }

                // 构造准入或判断条件
                Map<String, MedicalRuleConditionSet> conditionSetMap = new HashMap<>();
                AtomicInteger accessIndex = new AtomicInteger();
                AtomicInteger judgeIndex = new AtomicInteger();
                // conditionFieldInList   IO里的字段
                Map<String, Integer> fieldOrderMap = new HashMap<>();
                for (String field : conditionFieldInList) {
                    Object val = ReflectHelper.getValue(bean, field);
                    if (val == null || val.toString().length() == 0) {
                        continue;
                    }
                    Optional<String> optional = conditionFieldList.stream().filter(field::startsWith).findFirst();
                    if (optional.isPresent()) {
                        String conditionField = optional.get();
                        String conditionProp = field.replace(conditionField, "");
                        String startChart = conditionProp.substring(0, 1);
                        String orderNoStr = startWithNum.apply(startChart) ? startChart : "";

                        MedicalRuleConditionSet conditionSet = conditionSetMap.computeIfAbsent(conditionField + orderNoStr, k -> {
                            MedicalRuleConditionSet setBean = new MedicalRuleConditionSet();
//                            setBean.setId(IdUtils.uuid());
                            int orderNo = "".equals(orderNoStr) ? 0 : Integer.parseInt(orderNoStr);
                            if (accessConditionFields.contains(conditionField)) {
                                Integer groupNo = fieldOrderMap.computeIfAbsent(conditionField, r -> accessIndex.getAndIncrement());
                                setBean.setGroupNo(groupNo);
                                setBean.setType("access");
                            } else {
                                Integer groupNo = fieldOrderMap.computeIfAbsent(conditionField, r -> judgeIndex.getAndIncrement());
                                setBean.setGroupNo(groupNo);
                                setBean.setType("judge");
                            }
                            setBean.setOrderNo(orderNo);
                            setBean.setCompare("=");
                            if("indication".equals(conditionField) && orderNo > 0){
                                setBean.setLogic("OR");
                            } else {
                                setBean.setLogic("AND");
                            }

                            setBean.setField(conditionField);
                            setBean.setRuleId(bean.getRuleId());
                            return setBean;
                        });

                        if ("".equals(orderNoStr)) {
                            conditionProp = startChart.toLowerCase() + conditionProp.substring(1);
                        } else {
                            conditionProp = conditionProp.charAt(1) + conditionProp.substring(2);
                        }
                        if("logic".equals(conditionProp)){
                            String relationChar = relationDict.get(val);
                            if(relationChar == null){
                                errorMsgList.add("准入条件关系不存在:" + val);
                            } else {
                                val = relationChar;
                            }
                        }
                        ReflectHelper.setValue(conditionSet, conditionProp, val);
                    }
                }

                conditionSetList.addAll(conditionSetMap.values());
                itemCodesSet.add(bean.getItemCodes());

            }

            if(itemCodesSet.size()>0){
                // 验证主体项目编码
                try {
                    medicalValidService.validTreatProjectAndEquipmentCodesAndGroupCodes(itemCodesSet.toArray(new String[0]), "主体项目或项目组");
                } catch (Exception e) {
                    errorMsgList.add(e.getMessage());
                }
            }

            if (conditionSetList.size() > 0) {
                // 验证疾病组
                Set<String> codes = conditionSetList.stream()
                        .filter(r -> ("accessDiseaseGroup".equals(r.getField())
                        ) && r.getExt1() != null && r.getExt1().length() > 0)
                        .map(r -> r.getExt1().split("[|,]"))
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toSet());
                codes.addAll(conditionSetList.stream()
                        .filter(r -> ("indication".equals(r.getField()))
                                && r.getExt2() != null && r.getExt2().length() > 0)
                        .map(r -> r.getExt2().split("[|,]"))
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toList()));

                try {
                    medicalValidService.validDiseaseGroupCodes(codes.toArray(new String[0]), "疾病组");
                } catch (Exception e) {
                    errorMsgList.add(e.getMessage());
                }

                // 验证项目组
                codes = conditionSetList.stream()
                        .filter(r -> ("accessProjectGroup".equals(r.getField())
                                || "dayUnfitGroups".equals(r.getField())
                                || "unfitGroups".equals(r.getField())
                                || "fitGroups".equals(r.getField())
                        ) && r.getExt1() != null && r.getExt1().length() > 0)
                        .map(r -> r.getExt1().split("[|,]"))
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toSet());
                codes.addAll(conditionSetList.stream()
                        .filter(r -> ("indication".equals(r.getField())
                        ) && r.getExt3() != null && r.getExt3().length() > 0)
                        .map(r -> r.getExt3().split("[|,]"))
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toList()));
                try {
                    medicalValidService.validTreatGroupCodes(codes.toArray(new String[0]), "项目组");
                } catch (Exception e) {
                    errorMsgList.add(e.getMessage());
                }
                // 医疗服务项目
                //限适应症的化验结果
                /*codes = conditionSetList.stream()
                        .filter(r -> "indication".equals(r.getField()) && r.getExt4() != null && r.getExt4().length() > 0)
                        .map(r -> r.getExt4().split("[|,]"))
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toSet());
                try {
                    medicalValidService.validTreatmentCodes(codes.toArray(new String[0]), "医疗服务项目");
                } catch (Exception e) {
                    errorMsgList.add(e.getMessage());
                }*/

                // 验证药品组
                codes = conditionSetList.stream()
                        .filter(r -> ("fitGroups".equals(r.getField())
                        ) && r.getExt3() != null && r.getExt3().length() > 0)
                        .map(r -> r.getExt3().split("[|,]"))
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toSet());
                try {
                    medicalValidService.validDrugGroupCodes(codes.toArray(new String[0]), "药品组");
                } catch (Exception e) {
                    errorMsgList.add(e.getMessage());
                }


                // 转译其他字典项
                for (Map.Entry<String, DictInfo> entry : otherDictFieldMap.entries()) {

                    DictInfo dictInfo = entry.getValue();
                    String dictCode = dictInfo.getDictCode();
                    String field = entry.getKey();
                    String extField = dictInfo.getExt().toLowerCase();

                    for (MedicalRuleConditionSet bean : conditionSetList) {
                        if (!field.equals(bean.getField())) {
                            continue;
                        }
                        Object valueObj = ReflectHelper.getValue(bean, extField);
                        if (valueObj == null || "".equals(valueObj.toString())) {
                            continue;
                        }
                        String valueCode = valueObj.toString();
                        if (dictInfo.getMulti()) {
                            String separator = dictInfo.getSeparator();
                            try {
                                String value = medicalValidService.transMedicalOtherDictNames(valueCode, new String[]{separator}, dictCode);
                                ReflectHelper.setValue(bean, extField, value);
                            } catch (Exception e) {
                                errorMsgList.add(e.getMessage());
                            }
                        } else {
                            String value = medicalOtherDictService.getCodeByValue(dictCode, valueCode);
                            if (value == null) {
                                errorMsgList.add("其他字典-" + dictCode + "不存在:" + valueCode);
                            } else {
                                ReflectHelper.setValue(bean, extField, value);
                            }
                        }
                    }
                }

                // 转译医疗系统字典项
                for (Map.Entry<String, DictInfo> entry : medicalDictFieldMap.entries()) {
                    DictInfo dictInfo = entry.getValue();
                    String dictCode = dictInfo.getDictCode();
                    String field = entry.getKey();
                    String extField = dictInfo.getExt().toLowerCase();

                    for (MedicalRuleConditionSet bean : conditionSetList) {
                        if (!field.equals(bean.getField())) {
                            continue;
                        }
                        Object valueObj = ReflectHelper.getValue(bean, extField);
                        if (valueObj == null || "".equals(valueObj.toString())) {
                            continue;
                        }
                        String valueCode = valueObj.toString();
                        if (dictInfo.getMulti()) {
                            String separator = dictInfo.getSeparator();
                            try {
                                String value = medicalValidService.transMedicalDictNames(valueCode, new String[]{separator}, dictCode);
                                ReflectHelper.setValue(bean, extField, value);
                            } catch (Exception e) {
                                errorMsgList.add(e.getMessage());
                            }
                        } else {
                            String value = medicalDictService.queryDictKeyByText(dictCode, valueCode);
                            if (value == null) {
                                errorMsgList.add("医疗字典-" + dictCode + "不存在:" + valueCode);
                            } else {
                                ReflectHelper.setValue(bean, extField, value);
                            }
                        }
                    }
                }

                conditionSetAddList.addAll(conditionSetList);

            }

            if (errorMsgList.size() > 0) {
                throw new Exception("sheet页-" + sheetName + "\n" + StringUtils.join(errorMsgList, "\n"));
            }


        }
/*

        if(true){
            return "成功";
        }
*/

        List<String> updateIds = updateList.stream().map(MedicalRuleConfig::getRuleId).collect(Collectors.toList());
        Set<String> existIds = new HashSet<>();
        for (int i = 0, j, len = updateIds.size(); i < len; i = j) {
            j = i + 1000;
            if (j > len) {
                j = len;
            }
            List<String> ids = medicalRuleConfigService.listMaps(new QueryWrapper<MedicalRuleConfig>()
                    .select("RULE_ID")
                    .in("RULE_ID", updateIds.subList(i, j)))
                    .stream()
                    .map(r -> r.get("RULE_ID").toString()).collect(Collectors.toList());
            existIds.addAll(ids);
        }

        // 更新项ID不存在转为新增
        updateList.removeIf(r -> {
            boolean toAddList = !existIds.contains(r.getRuleId());
            if (toAddList) {
                addList.add(r);
            }
            return toAddList;
        });


        StringBuilder sb = new StringBuilder();

        if (updateList.size() > 0) {
            this.medicalRuleConfigService.updateBatchById(updateList);
            if (sb.length() > 0) {
                sb.append("，");
            }
            sb.append("更新记录数：").append(updateList.size());
        }
        if (addList.size() > 0) {
            this.medicalRuleConfigService.saveBatch(addList);
            if (sb.length() > 0) {
                sb.append("，");
            }
            sb.append("新增记录数：").append(addList.size());
        }
        if (delList.size() > 0) {
            for (int i = 0, j, len = delList.size(); i < len; i = j) {
                j = i + 1000;
                if (j > len) {
                    j = len;
                }
                this.medicalRuleConfigService.removeByRuleIds(delList.subList(i, j));
            }
            if (sb.length() > 0) {
                sb.append("，");
            }
            sb.append("删除记录数： ").append(updateList.size());
        }

        // 删除更新的条件组
        List<String> ruleIds = updateList.stream().map(MedicalRuleConfig::getRuleId).collect(Collectors.toList());
        for (int i = 0, j, len = ruleIds.size(); i < len; i = j) {
            j = i + 1000;
            if (j > len) {
                j = len;
            }
            medicalRuleConditionSetService.remove(new QueryWrapper<MedicalRuleConditionSet>().in("RULE_ID", ruleIds.subList(i, j)));
        }
        if (conditionSetAddList.size() > 0) {
            // 插入条件组
            medicalRuleConditionSetService.saveBatch(conditionSetAddList);
        }

       // return sb.toString();
        return Result.ok(sb.toString(),addList.size()+updateList.size()+delList.size());
    }

    // key - field
    private static Map<String, List<FieldInfo>> validFieldMap;
    //    private void initInvalid(){
    static {
        List<FieldInfo> fieldInfoList = new ArrayList<>(Arrays.asList(
                new FieldInfo("diseaseGroup", "indication", 2)
                , new FieldInfo("diseaseGroup", "accessDiseaseGroup", 1)
                , new FieldInfo("projectGroup", "indication", 3)
                , new FieldInfo("projectGroup", "accessProjectGroup", 1)
                , new FieldInfo("projectGroup", "dayUnfitGroups", 1)
                , new FieldInfo("projectGroup", "unfitGroups", 1)
                , new FieldInfo("projectGroup", "fitGroups", 1)
                , new FieldInfo("drugGroup", "fitGroups", 3)
                /*, new FieldInfo("drugGroup", "indication", 5)
                , new FieldInfo("drugGroup", "accessDrugGroup", 1)
                , new FieldInfo("drugGroup", "unpayDrug", 1, r -> "DRUGGROUP".equals(r.getExt2()))
                , new FieldInfo("drugGroup", "itemOrDrugGroup", 3)
                , new FieldInfo("ATC", "secDrug", 1)
                , new FieldInfo("ATC", "unpayDrug", 1, r -> "ATC".equals(r.getExt2()))
        */
                ));

        validFieldMap = new HashMap<>();
        fieldInfoList.forEach(fieldInfo -> {
            List<FieldInfo> list = validFieldMap.computeIfAbsent(fieldInfo.getField(), k -> new ArrayList<>());
            list.add(fieldInfo);
        });
    }

    @Override
    public void exportInvalidExcel(QueryWrapper<MedicalRuleConfig> queryWrapper, OutputStream os) throws Exception {
//        this.initInvalid();
        List<MedicalRuleConfig> list = this.medicalRuleConfigService.list(queryWrapper);

        List<MedicalRuleConditionSet> conditionSetList = new ArrayList<>();
        // 分页查询条件
        List<String> ruleIds = list.stream().map(MedicalRuleConfig::getRuleId).collect(Collectors.toList());
        for (int i = 0, j, len = ruleIds.size(); i < len; i = j) {
            j = i + 1000;
            if (j > len) {
                j = len;
            }
            conditionSetList.addAll(medicalRuleConditionSetService.list(
                    new QueryWrapper<MedicalRuleConditionSet>()
                            .orderByAsc("RULE_ID", "GROUP_NO", "ORDER_NO")
                            .in("RULE_ID", ruleIds.subList(i, j))
                            // 过滤不需要验证的条件
                            .in(DbDataEncryptUtil.decryptFunc("FIELD"), validFieldMap.keySet())
            ));
        }
        // 字典
        Map<String, String> actionDict = this.getActionMap();
        Map<String, String> ruleLimitDict = medicalDictService.queryMapByType(RULE_LIMIT_DICT);

        // 验证不通过导出主体
        Map<String, InvalidExport> invalidRuleMap = this.getInvalidRuleMap(conditionSetList, validFieldMap);

        // 主体编码存在验证
        Set<String> noExistCode = new HashSet<>(medicalValidService.invalidTreatOrEquipmentCodes(
                list.stream().map(MedicalRuleConfig::getItemCodes).distinct().toArray(String[]::new)));

        List<InvalidExport> exportList = new ArrayList<>();
        for(MedicalRuleConfig bean: list){
            InvalidExport invalidExport = invalidRuleMap.get(bean.getRuleId());
            // 主体编码存在验证
            String code = bean.getItemCodes();
            if(noExistCode.contains(code) ){
                if(invalidExport == null){
                    invalidExport = new InvalidExport();
                }
                invalidExport.setItemCodes(code);
            } else if(invalidExport == null){
                continue;
            }

            invalidExport.setDiseaseGroupCodes(String.join(",", invalidExport.getDiseaseGroupCodeList()));
            invalidExport.setDrugGroupCodes(String.join(",", invalidExport.getDrugGroupCodeList()));
            invalidExport.setProjectGroupCodes(String.join(",", invalidExport.getProjectGroupCodeList()));
            invalidExport.setAtcCodes(String.join(",", invalidExport.getAtcCodeList()));
            // 设置其他基本信息
            invalidExport.setRuleId(bean.getRuleId());
            invalidExport.setCode(bean.getItemCodes());
            invalidExport.setName(bean.getItemNames());
            invalidExport.setActionName(actionDict.getOrDefault(bean.getActionId(), bean.getActionId()));
            invalidExport.setRuleLimit(ruleLimitDict.get(bean.getRuleLimit()));
            exportList.add(invalidExport);
        }

        if(exportList.size() == 0){
            throw new Exception("没有失效明细数据");
        }

        String[] titles = {"规则ID", "项目编码", "项目名称", "不合规行为", "规则类别", "缺失项目主体"
                , "缺失疾病组"
                , "缺失药品组"
                , "缺失项目组"
//                , "缺失ATC"
        };
        String[] fields = {"ruleId", "code", "name", "actionName", "ruleLimit", "itemCodes"
                , "diseaseGroupCodes"
                , "drugGroupCodes"
                , "projectGroupCodes"
//                , "atcCodes"
        };

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        ExportXUtils.exportExl(exportList, InvalidExport.class, titles, fields, workbook, "失效收费合规");
        workbook.write(os);
        workbook.dispose();
    }

}
