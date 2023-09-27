package com.ai.modules.medical.service.impl;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ExcelXUtils;
import com.ai.common.utils.ExportXUtils;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ReflectHelper;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.medical.entity.MedicalClinical;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.medical.service.IMedicalRuleConfigTreatService;
import com.ai.modules.medical.vo.MedicalTreatRuleConfigIO;
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
public class MedicalRuleConfigTreatServiceImpl extends MedicalRuleConfigCommonServiceImpl implements IMedicalRuleConfigTreatService {

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
                .put("accessVisittype", new DictInfo("VisitType", "|"))
                .put("dept", new DictInfo("Department", "|"))
                .put("accessDept", new DictInfo("Department", "|"))
                .build();
        medicalDictFieldMap = ImmutableListMultimap.<String, DictInfo>builder()
                .put("age", new DictInfo("AGE_UNIT", 2))
                .put("accessAge", new DictInfo("AGE_UNIT", 2))
                .put("reviewHisDisease", new DictInfo("YESNO"))
                .put("hosplevelType", new DictInfo("CASE_RELATION_CHAR", 2))
                .build();


        String[][] commmonFields = new String[][]{
                {"id主键", "主体项目类型", "项目/项目组编码", "项目/项目组名称", "规则类型", "规则级别","级别备注","不合规行为"},
                {"ruleId", "itemTypes", "itemCodes", "itemNames", "ruleLimit", "ruleGrade", "ruleGradeRemark", "actionName"},
                {"政策依据类别", "政策依据", "所属地区", "提示信息", "适用时间", "状态(启用、禁用)", "操作原因", "更改标识(1新增0修改2删除)"},
                {"ruleBasisType", "ruleBasis", "ruleSource", "message", "dataTimes", "status","updateReason", "updateActionType"}
        };

        exportInfoMap = ImmutableMap.<String, LimitInfo>builder()
                .put("freq", new LimitInfo(null, new String[][]{
                        {"频次比较符", "频次数量"
                                , "准入就诊类型", "准入条件关系"
                                , "是否等于准入疾病组", "准入疾病组编码", "准入疾病组名称", "准入条件关系"
                                , "是否等于准入项目组", "准入项目组编码", "准入项目组名称"},
                        {"frequencyCompare", "frequencyExt2"
                                , "accessVisittypeExt1", "accessDiseaseGroupLogic"
                                , "accessDiseaseGroupCompare", "accessDiseaseGroupExt1", "accessDiseaseGroupExt1Names", "accessProjectGroupLogic"
                                , "accessProjectGroupCompare", "accessProjectGroupExt1", "accessProjectGroupExt1Names"
                        }
                }))
                .put("freq2", new LimitInfo(null, new String[][]{
                        {"项目组1比较符","项目组1编码","项目组1名称", "频次比较符1", "频次数量1"
                                ,"项目组2比较符","项目组2编码","项目组2名称", "频次比较符2", "频次数量2"
                                , "准入就诊类型", "准入条件关系"
                                , "是否等于准入疾病组", "准入疾病组编码", "准入疾病组名称", "准入条件关系"
                                , "是否等于准入项目组", "准入项目组编码", "准入项目组名称"},
                        { "frequency0Ext3","frequency0Ext4","frequency0Ext4Names", "frequency0Compare", "frequency0Ext2"
                                ,"frequency1Ext3","frequency1Ext4","frequency1Ext4Names", "frequency1Compare", "frequency1Ext2"
                                , "accessVisittypeExt1", "accessDiseaseGroupLogic"
                                , "accessDiseaseGroupCompare", "accessDiseaseGroupExt1", "accessDiseaseGroupExt1Names", "accessProjectGroupLogic"
                                , "accessProjectGroupCompare", "accessProjectGroupExt1", "accessProjectGroupExt1Names"
                        }
                }))
                .put("age", new LimitInfo(null, new String[][]{
                        {"年龄范围", "年龄单位", "是否等于准入科室", "准入科室", "准入条件关系"
                                ,"是否等于准入疾病组", "准入疾病组编码", "准入疾病组名称"},
                        {"ageExt1", "ageExt2","accessDeptCompare","accessDeptExt1", "accessDiseaseGroupLogic"
                                , "accessDiseaseGroupCompare", "accessDiseaseGroupExt1", "accessDiseaseGroupExt1Names"}
                }))
                .put("sex", new LimitInfo(null, new String[][]{
                        {"性别"}, {"sexExt1"}
                }))
                .put("visittype", new LimitInfo(null, new String[][]{
                        {"就诊类型"}, {"visittypeExt1"}
                }))
                .put("dept", new LimitInfo(null, new String[][]{
                        {"就诊类型比较符", "准入就诊类型", "科室"},
                        {"accessVisittypeCompare", "accessVisittypeExt1", "deptExt1"}
                }))
                .put("unfitGroups", new LimitInfo("一次就诊重复收费", new String[][]{
                        {"就诊类型比较符", "准入就诊类型", "一次就诊重复项目组编码(间隔符\"|\")", "一次就诊重复项目组编码名称"},
                        {"accessVisittypeCompare", "accessVisittypeExt1", "unfitGroupsExt1", "unfitGroupsExt1Names"}
                }))


                .put("indication", new LimitInfo(null, new String[][]{
                        {"准入性别", "准入条件关系", "准入年龄范围", "准入年龄单位", "是否审核历史疾病"
                                , "判定条件组1", "疾病组1编码", "疾病组1名称", "项目组1编码", "项目组1名称", "药品组1编码", "药品组1名称", "化验结果1"
                                , "判定条件组2", "疾病组2编码", "疾病组2名称", "项目组2编码", "项目组2名称", "药品组2编码", "药品组2名称", "化验结果2"
                                , "判定条件组3", "疾病组3编码", "疾病组3名称", "项目组3编码", "项目组3名称", "药品组3编码", "药品组3名称", "化验结果3"
                        },
                        {"accessSexExt1", "accessAgeLogic", "accessAgeExt1", "accessAgeExt2", "reviewHisDiseaseExt1"
                                , "indication0Ext1", "indication0Ext2", "indication0Ext2Names", "indication0Ext3", "indication0Ext3Names", "indication0Ext5", "indication0Ext5Names", "indication0Ext4"
                                , "indication1Ext1", "indication1Ext2", "indication1Ext2Names", "indication1Ext3", "indication1Ext3Names", "indication1Ext5", "indication1Ext5Names", "indication1Ext4"
                                , "indication2Ext1", "indication2Ext2", "indication2Ext2Names", "indication2Ext3", "indication2Ext3Names", "indication2Ext5", "indication2Ext5Names", "indication2Ext4"
                        }
                }))

                .put("unIndication", new LimitInfo(null, new String[][]{
                        {
                                "准入性别", "准入条件关系", "准入年龄范围", "准入年龄单位", "是否审核历史疾病"
                                , "判定条件组1", "疾病组1编码", "疾病组1名称", "项目组1编码", "项目组1名称", "药品组1编码", "药品组1名称", "化验结果1"
                                , "判定条件组2", "疾病组2编码", "疾病组2名称", "项目组2编码", "项目组2名称", "药品组2编码", "药品组2名称", "化验结果2"
                                , "判定条件组3", "疾病组3编码", "疾病组3名称", "项目组3编码", "项目组3名称", "药品组3编码", "药品组3名称", "化验结果3"
                        },
                        {
                                "accessSexExt1", "accessAgeLogic", "accessAgeExt1", "accessAgeExt2", "reviewHisDiseaseExt1"
                                , "unIndication0Ext1", "unIndication0Ext2", "unIndication0Ext2Names", "unIndication0Ext3", "unIndication0Ext3Names", "unIndication0Ext5", "unIndication0Ext5Names", "unIndication0Ext4"
                                , "unIndication1Ext1", "unIndication1Ext2", "unIndication1Ext2Names", "unIndication1Ext3", "unIndication1Ext3Names", "unIndication1Ext5", "unIndication1Ext5Names", "unIndication1Ext4"
                                , "unIndication2Ext1", "unIndication2Ext2", "unIndication2Ext2Names", "unIndication2Ext3", "unIndication2Ext3Names", "unIndication2Ext5", "unIndication2Ext5Names", "unIndication2Ext4"
                        }
                }))
                .put("diagWrong", new LimitInfo(null, new String[][]{
                        {"疾病组编码(单选)", "疾病组名称", "历史项目组编码", "历史项目组名称"},
                        {"diseaseGroupExt1", "diseaseGroupExt1Names", "hisGroupsExt1", "hisGroupsExt1Names"}
                }))

                .put("itemWrong", new LimitInfo("本次项目与既往项目不符", new String[][]{
                        {"历史项目组编码", "历史项目组名称"},
                        {"hisGroupsExt1", "hisGroupsExt1Names"}
                }))
                // 一日重复收费
                .put("YRCFSF", new LimitInfo(null, new String[][]{
                        {"一日互斥项目组编码(间隔符\"|\")", "一日互斥项目组名称"},
                        {"dayUnfitGroupsExt1", "dayUnfitGroupsExt1Names"}
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
                "frequency", "accessDiseaseGroup", "accessProjectGroup", "age", "sex"
                , "visittype", "accessVisittype", "dept", "unfitGroups", "indication"
                , "reviewHisDisease", "accessSex", "accessAge", "unIndication"
                , "diseaseGroup", "hisGroups", "accessDept", "dayUnfitGroups"
        };

        accessConditionFields = new HashSet<>(
                Arrays.asList("accessDiseaseGroup", "accessProjectGroup", "accessSex", "accessAge", "accessVisittype", "accessDept")
        );
    }


    @Override
    public void exportExcel(QueryWrapper<MedicalRuleConfig> queryWrapper, OutputStream os) throws Exception {
//        this.initTable();
        List<MedicalTreatRuleConfigIO> list = this.medicalRuleConfigService.listTreatIO(queryWrapper);
        List<MedicalRuleConditionSet> conditionSetList = new ArrayList<>();
        // 分页查询条件
        List<String> ruleIds = list.stream().map(MedicalTreatRuleConfigIO::getRuleId).collect(Collectors.toList());
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
        List<String> ioFieldList = Arrays.stream(MedicalTreatRuleConfigIO.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
        // 筛选出需要赋值给IO类的字段
        List<String> conditionFieldInList = conditionFieldList.stream().map(f ->
                ioFieldList.stream().filter(r -> r.startsWith(f) && !r.endsWith("Name")
                        && !r.endsWith("Names") && !startWithNum.apply(r.substring(f.length()))).toArray(String[]::new))
                .flatMap(Arrays::stream).collect(Collectors.toList());

        SimpleDateFormat dataTimeSdf = new SimpleDateFormat("yyyy-MM-dd");

        // 各种限定条件归类
        Map<String, List<MedicalTreatRuleConfigIO>> limitListMap = new HashMap<>();

        Map<String, String> ruleLimitDict = medicalDictService.queryMapByType(this.RULE_LIMIT_DICT);
        Map<String, String> relationDict = medicalDictService.queryMapByType("CASE_RELATION_CHAR");
        Map<String, String> statusDict = medicalDictService.queryMapByType("SWITCH_STATUS");
        Map<String, String> actionDict = this.getActionMap();

        for (MedicalTreatRuleConfigIO bean : list) {
            String ruleId = bean.getRuleId();

            String startTime = bean.getStartTime() == null ? "2000-01-01" : dataTimeSdf.format(bean.getStartTime());
            String endTime = bean.getEndTime() == null ? "2099-12-31" : dataTimeSdf.format(bean.getEndTime());
            String ruleLimit = bean.getRuleLimit();
            bean.setDataTimes(startTime + "到" + endTime);
            bean.setUpdateActionType("0");
            bean.setRuleLimit(ruleLimitDict.get(ruleLimit));
            bean.setRuleBasisType(medicalOtherDictService.getValueByCode("rule_sourcetype", bean.getRuleBasisType()));
            bean.setActionName(actionDict.getOrDefault(bean.getActionId(), bean.getActionId()));
            bean.setStatus(statusDict.get(bean.getStatus()));
            // 已保存名称
//            bean.setRuleSource(medicalOtherDictService.getValueByCode("region", bean.getRuleSource()));

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
                        if ("logic".equals(propFieldName) && value != null) {
                            value = relationDict.get(value.toString());
                        }
                        ReflectHelper.setValue(bean, propField, value);
                    }
                    if ("indication".equals(field)) {
                        // 设置当前分组的值
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext1", bean.getIndicationExt1());
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext2", bean.getIndicationExt2());
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext3", bean.getIndicationExt3());
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext4", bean.getIndicationExt4());
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext5", bean.getIndicationExt5());
                    } else if ("unIndication".equals(field)) {
                        // 设置当前分组的值
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext1", bean.getUnIndicationExt1());
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext2", bean.getUnIndicationExt2());
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext3", bean.getUnIndicationExt3());
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext4", bean.getUnIndicationExt4());
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext5", bean.getUnIndicationExt5());
                    }else if ("frequency".equals(field) && "freq2".equals(ruleLimit)) {
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Compare", bean.getFrequencyCompare());
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext1", bean.getFrequencyExt1());
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext2", bean.getFrequencyExt2());
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext3", bean.getFrequencyExt3());
                        ReflectHelper.setValue(bean, field + conditionBean.getOrderNo() + "Ext4", bean.getFrequencyExt4());
                    }
                }

            }

            limitListMap.computeIfAbsent(ruleLimit, k -> new ArrayList<>()).add(bean);
        }


        // 翻译疾病组
        List<String> diseaseGroupOldList = new ArrayList<>();
        for (MedicalTreatRuleConfigIO bean : list) {
            diseaseGroupOldList.add(bean.getAccessDiseaseGroupExt1());
            diseaseGroupOldList.add(bean.getIndication0Ext2());
            diseaseGroupOldList.add(bean.getIndication1Ext2());
            diseaseGroupOldList.add(bean.getIndication2Ext2());
            diseaseGroupOldList.add(bean.getUnIndication0Ext2());
            diseaseGroupOldList.add(bean.getUnIndication1Ext2());
            diseaseGroupOldList.add(bean.getUnIndication2Ext2());
            diseaseGroupOldList.add(bean.getDiseaseGroupExt1());
        }

        List<String> diseaseGroupNewList = medicalValidService.transDiseaseGroupCodes(diseaseGroupOldList, new String[]{"|", ","});

        int index = 0;
        for (MedicalTreatRuleConfigIO bean : list) {
            bean.setAccessDiseaseGroupExt1Names(diseaseGroupNewList.get(index++));
            bean.setIndication0Ext2Names(diseaseGroupNewList.get(index++));
            bean.setIndication1Ext2Names(diseaseGroupNewList.get(index++));
            bean.setIndication2Ext2Names(diseaseGroupNewList.get(index++));
            bean.setUnIndication0Ext2Names(diseaseGroupNewList.get(index++));
            bean.setUnIndication1Ext2Names(diseaseGroupNewList.get(index++));
            bean.setUnIndication2Ext2Names(diseaseGroupNewList.get(index++));
            bean.setDiseaseGroupExt1Names(diseaseGroupNewList.get(index++));
        }

        // 翻译项目组
        List<String> treatGroupOldList = new ArrayList<>();
        for (MedicalTreatRuleConfigIO bean : list) {
            treatGroupOldList.add(bean.getAccessProjectGroupExt1());
            treatGroupOldList.add(bean.getUnfitGroupsExt1());
            treatGroupOldList.add(bean.getIndication0Ext3());
            treatGroupOldList.add(bean.getIndication1Ext3());
            treatGroupOldList.add(bean.getIndication2Ext3());
            treatGroupOldList.add(bean.getUnIndication0Ext3());
            treatGroupOldList.add(bean.getUnIndication1Ext3());
            treatGroupOldList.add(bean.getUnIndication2Ext3());
            treatGroupOldList.add(bean.getHisGroupsExt1());
            treatGroupOldList.add(bean.getFrequency0Ext4());
            treatGroupOldList.add(bean.getFrequency1Ext4());
            treatGroupOldList.add(bean.getDayUnfitGroupsExt1());

        }

        List<String> treatGroupNewList = medicalValidService.transTreatGroupCodes(treatGroupOldList, new String[]{"|", ","});

        index = 0;
        for (MedicalTreatRuleConfigIO bean : list) {
            bean.setAccessProjectGroupExt1Names(treatGroupNewList.get(index++));
            bean.setUnfitGroupsExt1Names(treatGroupNewList.get(index++));
            bean.setIndication0Ext3Names(treatGroupNewList.get(index++));
            bean.setIndication1Ext3Names(treatGroupNewList.get(index++));
            bean.setIndication2Ext3Names(treatGroupNewList.get(index++));
            bean.setUnIndication0Ext3Names(treatGroupNewList.get(index++));
            bean.setUnIndication1Ext3Names(treatGroupNewList.get(index++));
            bean.setUnIndication2Ext3Names(treatGroupNewList.get(index++));
            bean.setHisGroupsExt1Names(treatGroupNewList.get(index++));
            bean.setFrequency0Ext4Names(treatGroupNewList.get(index++));
            bean.setFrequency1Ext4Names(treatGroupNewList.get(index++));
            bean.setDayUnfitGroupsExt1Names(treatGroupNewList.get(index++));
        }

        // 翻译药品组
        List<String> drugGroupOldList = new ArrayList<>();
        for (MedicalTreatRuleConfigIO bean : list) {
            drugGroupOldList.add(bean.getIndication0Ext5());
            drugGroupOldList.add(bean.getIndication1Ext5());
            drugGroupOldList.add(bean.getIndication2Ext5());
            drugGroupOldList.add(bean.getUnIndication0Ext5());
            drugGroupOldList.add(bean.getUnIndication1Ext5());
            drugGroupOldList.add(bean.getUnIndication2Ext5());
        }

        List<String> drugGroupNewList = medicalValidService.transDrugGroupCodes(drugGroupOldList, new String[]{"|", ","});

        index = 0;
        for (MedicalTreatRuleConfigIO bean : list) {
            bean.setIndication0Ext5Names(drugGroupNewList.get(index++));
            bean.setIndication1Ext5Names(drugGroupNewList.get(index++));
            bean.setIndication2Ext5Names(drugGroupNewList.get(index++));
            bean.setUnIndication0Ext5Names(drugGroupNewList.get(index++));
            bean.setUnIndication1Ext5Names(drugGroupNewList.get(index++));
            bean.setUnIndication2Ext5Names(drugGroupNewList.get(index++));
        }

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 导出数据
        for (Map.Entry<String, List<MedicalTreatRuleConfigIO>> entry : limitListMap.entrySet()) {
            String sheetCode = entry.getKey();
            String limit;
            if("freq2".equals(sheetCode)){
                limit = sheetCode;
            } else {
                int numIndex = startWithNumIndex.apply(sheetCode);
                if (numIndex == -1) {
                    numIndex = entry.getKey().length();
                }
                limit = entry.getKey().substring(0, numIndex);
            }


            LimitInfo limitInfo = exportInfoMap.get(limit);
            if (limitInfo == null) {
                continue;
            }

            String title = ruleLimitDict.get(sheetCode);
            ExportXUtils.exportExl(entry.getValue(), MedicalTreatRuleConfigIO.class, limitInfo.getTitles(), limitInfo.getFields(), workbook, title);

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

        Map<String, String> ruleLimitDict = medicalDictService.queryNameMapByType(this.RULE_LIMIT_DICT);

        int sheetIndex = -1;
        for (String sheetName : sheetNames) {
            sheetIndex++;
            String sheetCode = ruleLimitDict.get(sheetName);

            List<String> errorMsgList = new ArrayList<>();

            if (sheetCode == null) {
                throw new Exception("sheet页-" + sheetName + ":不存在规则类型-" + sheetName);
            }
            String limitCode;
            if("freq2".equals(sheetCode)) {
                limitCode = sheetCode;
            } else {
                int numIndex = startWithNumIndex.apply(sheetCode);
                if (numIndex == -1) {
                    numIndex = sheetCode.length();
                }

                limitCode = sheetCode.substring(0, numIndex);
            }



            LimitInfo limitInfo = exportInfoMap.get(limitCode);
            if (limitInfo == null) {
                continue;
            }

            List<MedicalTreatRuleConfigIO> list = ExcelXUtils.readSheet(MedicalTreatRuleConfigIO.class, limitInfo.getFields(), sheetIndex, 1, file.getInputStream());
            List<MedicalRuleConditionSet> conditionSetList = new ArrayList<>();
            Set<String> itemCodesSet = new HashSet<>();//主体项目编码
            // 按照字段顺序筛选， groupNo跟页面字段顺序相同
            List<String> conditionFieldInList = Arrays.stream(limitInfo.getFields()).filter(r ->
                !r.endsWith("Name") && !r.endsWith("Names") && conditionFieldList.stream().anyMatch(r::startsWith)
            ).collect(Collectors.toList());

            if("freq".equals(limitCode)){
                conditionFieldInList.add("frequencyExt1");
            } else if("freq2".equals(limitCode)){
                conditionFieldInList.add("frequency0Ext1");
                conditionFieldInList.add("frequency1Ext1");
            }


            SimpleDateFormat dataTimeSdf = new SimpleDateFormat("yyyy-MM-dd");

            Map<String, String> actionDict = this.getActionNameMap();

            Map<String, String> limitToFreqDict = medicalDictService.queryMapByType("ACTION_TO_FREQ");
            Map<String, String> relationDict = medicalDictService.queryNameMapByType("CASE_RELATION_CHAR");
            Map<String, String> statusDict = medicalDictService.queryNameMapByType("SWITCH_STATUS");

            int index = 1;  // 标题栏算第一行
            for (MedicalTreatRuleConfigIO bean : list) {
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
                        if (limitCode.startsWith("freq")) {
                            for (Map.Entry<String, String> entry : limitToFreqDict.entrySet()) {
                                if (ruleLimit.contains(entry.getKey())) {
                                    String[] vals = entry.getValue().split("_");
                                    bean.setFrequencyExt1(vals[1]);
                                    bean.setFrequency0Ext1(vals[1]);
                                    bean.setFrequency1Ext1(vals[1]);
                                    break;
                                }
                            }
                        }

                    }
                }

                String ruleBasisType = bean.getRuleBasisType();
                if (StringUtils.isBlank(ruleBasisType)) {
                    errorMsgList.add(indexMsg + "：政策依据类型不能为空");
                } else {
                    String ruleBasisTypeCode = medicalOtherDictService.getCodeByValue("rule_sourcetype", bean.getRuleBasisType());
                    if (ruleBasisTypeCode == null) {
                        errorMsgList.add(indexMsg + "：政策依据类型不存在：" + ruleBasisType);
                    } else {
                        bean.setRuleBasisType(ruleBasisTypeCode);
                    }
                }


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
                    if (!optional.isPresent()) {
                        continue;
                    }
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
                        if (("indication".equals(conditionField)
                                || "unIndication".equals(conditionField)
                                || "frequency".equals(conditionField)
                        ) && orderNo > 0) {
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
                        conditionProp = String.valueOf(conditionProp.charAt(1)).toLowerCase() + conditionProp.substring(2);
                    }
                    if ("logic".equals(conditionProp)) {
                        String relationChar = relationDict.get(val);
                        if (relationChar == null) {
                            errorMsgList.add("准入条件关系不存在:" + val);
                        } else {
                            val = relationChar;
                        }
                    }
                    ReflectHelper.setValue(conditionSet, conditionProp, val);

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
                                || "diseaseGroup".equals(r.getField())
                        ) && r.getExt1() != null && r.getExt1().length() > 0)
                        .map(r -> r.getExt1().split("[|,]"))
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toSet());
                codes.addAll(conditionSetList.stream()
                        .filter(r -> ("indication".equals(r.getField())
                                        || "unIndication".equals(r.getField())
                                ) && r.getExt2() != null && r.getExt2().length() > 0
                        )
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
                                || "unfitGroups".equals(r.getField())
                                || "hisGroups".equals(r.getField())
                                || "dayUnfitGroups".equals(r.getField())
                        ) && r.getExt1() != null && r.getExt1().length() > 0)
                        .map(r -> r.getExt1().split("[|,]"))
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toSet());
                codes.addAll(conditionSetList.stream()
                        .filter(r -> ("indication".equals(r.getField())
                                || "unIndication".equals(r.getField())
                        ) && r.getExt3() != null && r.getExt3().length() > 0)
                        .map(r -> r.getExt3().split("[|,]"))
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toList()));
                codes.addAll(conditionSetList.stream()
                        .filter(r -> ("frequency".equals(r.getField())
                        ) && r.getExt4() != null && r.getExt4().length() > 0)
                        .map(r -> r.getExt4().split("[|,]"))
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toList()));

                try {
                    medicalValidService.validTreatGroupCodes(codes.toArray(new String[0]), "项目组");
                } catch (Exception e) {
                    errorMsgList.add(e.getMessage());
                }

                // 验证药品组
                codes = conditionSetList.stream()
                        .filter(r -> ("indication".equals(r.getField())
                                || "unIndication".equals(r.getField())
                        ) && r.getExt5() != null && r.getExt5().length() > 0)
                        .map(r -> r.getExt5().split("[|,]"))
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
                , new FieldInfo("diseaseGroup", "unIndication", 2)
                , new FieldInfo("diseaseGroup", "accessDiseaseGroup", 1)
                , new FieldInfo("diseaseGroup", "diseaseGroup", 1)

                , new FieldInfo("projectGroup", "accessProjectGroup", 1)
                , new FieldInfo("projectGroup", "unfitGroups", 1)
                , new FieldInfo("projectGroup", "indication", 3)
                , new FieldInfo("projectGroup", "unIndication", 3)
                , new FieldInfo("projectGroup", "hisGroups", 1)
                , new FieldInfo("projectGroup", "frequency", 4)
                , new FieldInfo("projectGroup", "dayUnfitGroups", 1)

                , new FieldInfo("drugGroup", "indication", 5)
                , new FieldInfo("drugGroup", "unIndication", 5)
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
            if(noExistCode.contains(code)){
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
        String[] titles = {"规则ID", "项目编码", "项目名称", "不合规行为", "规则类别", "缺失项目主体", "缺失疾病组", "缺失药品组", "缺失项目组"};
        String[] fields = {"ruleId", "code", "name", "actionName", "ruleLimit", "itemCodes", "diseaseGroupCodes", "drugGroupCodes", "projectGroupCodes"};

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        ExportXUtils.exportExl(exportList, InvalidExport.class, titles, fields, workbook, "失效合理诊疗");
        workbook.write(os);
        workbook.dispose();
    }


}
