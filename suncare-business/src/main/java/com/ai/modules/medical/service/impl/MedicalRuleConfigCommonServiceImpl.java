package com.ai.modules.medical.service.impl;

import com.ai.common.utils.ExcelXUtils;
import com.ai.common.utils.ExportXUtils;
import com.ai.common.utils.IdUtils;
import com.ai.common.utils.ReflectHelper;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.config.service.IMedicalOtherDictService;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.medical.service.IMedicalRuleConditionSetService;
import com.ai.modules.medical.service.IMedicalRuleConfigDrugService;
import com.ai.modules.medical.service.IMedicalRuleConfigService;
import com.ai.modules.medical.service.IMedicalValidService;
import com.ai.modules.medical.vo.MedicalChargeRuleConfigIO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description: 通用规则配置
 * @Author: jeecg-boot
 * @Date: 2020-12-14
 * @Version: V1.0
 */
@Slf4j
@Service
public class MedicalRuleConfigCommonServiceImpl {


    @Autowired
    IMedicalRuleConfigService medicalRuleConfigService;

    @Autowired
    IMedicalRuleConditionSetService medicalRuleConditionSetService;

    @Autowired
    IMedicalDictService medicalDictService;

    @Autowired
    IMedicalActionDictService medicalActionDictService;

    @Autowired
    IMedicalOtherDictService medicalOtherDictService;

    @Autowired
    IMedicalValidService medicalValidService;

    static String[][] commmonFields = new String[][]{
            {"id主键", "项目编码", "项目名称", "规则类型", "规则级别","级别备注","不合规行为"},
            {"ruleId", "itemCodes", "itemNames", "ruleLimit", "ruleGrade", "ruleGradeRemark", "actionName"},
            {"政策依据类别", "政策依据", "所属地区", "提示信息", "适用时间", "状态(启用、禁用)", "操作原因", "更改标识(1新增0修改2删除)"},
            {"ruleBasisType", "ruleBasis", "ruleSource", "message", "dataTimes", "status","updateReason", "updateActionType"}
    };


    @Data
    static class LimitInfo {
        private String name;
        private String[] titles;
        private String[] fields;

        LimitInfo(String name, String[][] fields) {
            this.name = name;
            this.titles = fields[0];
            this.fields = fields[1];
        }
    }

    @Data
    static class DictInfo {
        private String dictCode;
        private Boolean multi;
        private String separator;
        // 翻译字段 ext1 ext2
        private String ext;

        public DictInfo(String dictCode) {
            this.dictCode = dictCode;
            this.multi = false;
            this.ext = "Ext1";
        }

        public DictInfo(String dictCode, int extNo) {
            this.dictCode = dictCode;
            this.multi = false;
            this.ext = "Ext" + extNo;
        }

        public DictInfo(String dictCode, String separator) {
            this.dictCode = dictCode;
            this.multi = true;
            this.separator = separator;
            this.ext = "Ext1";
        }

        public DictInfo(String dictCode, String separator, int extNo) {
            this.dictCode = dictCode;
            this.multi = true;
            this.separator = separator;
            this.ext = "Ext" + extNo;
        }
    }

    @Data
    static class FieldInfo {
        private String type;
        private String field;
        private String ext;
        private Function<MedicalRuleConditionSet, Boolean> condition;


        public FieldInfo(String type, String field, int extNo) {
            this.type = type;
            this.field = field;
            this.ext = "ext" + extNo;
        }
        public FieldInfo(String type, String field, int extNo, Function<MedicalRuleConditionSet, Boolean> condition) {
            this.type = type;
            this.field = field;
            this.ext = "ext" + extNo;
            this.condition = condition;
        }
    }


    @Data
    static class InvalidExport {
        private String ruleId;
        private String code;
        private String name;
        private String actionName;
        private String ruleLimit;
        private String diseaseGroupCodes;
        private String projectGroupCodes;
        private String drugGroupCodes;
        private String atcCodes;
        private String itemCodes;

        private List<String> diseaseGroupCodeList = new ArrayList<>();
        private List<String> projectGroupCodeList = new ArrayList<>();
        private List<String> drugGroupCodeList = new ArrayList<>();
        private List<String> atcCodeList = new ArrayList<>();


    }


    Function<String, Boolean> startWithNum = str -> {
        char start = str.charAt(0);
        return start >= '0' && start <= '9';
    };

    Function<String, Integer> startWithNumIndex = str -> {
        for (int i = 0, len = str.length(); i < len; i++) {
            char start = str.charAt(i);
            if (start >= '0' && start <= '9') {
                return i;
            }
        }
        return -1;
    };


    Function<String, Integer> indexOfUpper = str -> {
        for(int i = 0, len = str.length(); i < len; i++){
            char start = str.charAt(i);
            if(start >= 'A' && start <= 'Z'){
                return i;
            }
        }
        return -1;
    };

    public Map<String, String> getActionMap(){
        List<MedicalActionDict> actionList = medicalActionDictService.list();
        Map<String, String> actionDict = new HashMap<>();
        for(MedicalActionDict bean: actionList){
            actionDict.put(bean.getActionId(), bean.getActionName());
        }
        return actionDict;
    }


    public Map<String, String> getActionNameMap(){
        List<MedicalActionDict> actionList = medicalActionDictService.list();
        Map<String, String> actionDict = new HashMap<>();
        for(MedicalActionDict bean: actionList){
            actionDict.put(bean.getActionName(), bean.getActionId());
        }
        return actionDict;
    }



    Map<String, InvalidExport> getInvalidRuleMap(List<MedicalRuleConditionSet> conditionSetList, Map<String, List<FieldInfo>> validFieldMap) throws Exception {

        Set<String> diseaseGroupCodes = new HashSet<>();
        Set<String> projectGroupCodes = new HashSet<>();
        Set<String> drugGroupCodes = new HashSet<>();
        Set<String> atcCodes = new HashSet<>();

        for (MedicalRuleConditionSet conditionSet : conditionSetList) {
            List<FieldInfo> fieldInfos = validFieldMap.get(conditionSet.getField());
            for (FieldInfo fieldInfo : fieldInfos) {
                // 跳过不符合的字段规则
                if (fieldInfo.getCondition() != null && !fieldInfo.getCondition().apply(conditionSet)) {
                    continue;
                }
                Object obj = ReflectHelper.getValue(conditionSet, fieldInfo.getExt());
                if (obj == null) {
                    continue;
                }
                if ("diseaseGroup".equals(fieldInfo.getType())) {
                    diseaseGroupCodes.addAll(Arrays.asList(obj.toString().split("[|,]")));
                } else if ("projectGroup".equals(fieldInfo.getType())) {
                    projectGroupCodes.addAll(Arrays.asList(obj.toString().split("[|,]")));
                } else if ("drugGroup".equals(fieldInfo.getType())) {
                    drugGroupCodes.addAll(Arrays.asList(obj.toString().split("[|,]")));
                } else if ("ATC".equals(fieldInfo.getType())) {
                    atcCodes.addAll(Arrays.asList(obj.toString().split("[|,]")));
                }
            }
        }

        // 转换为不存在的编码
        if (diseaseGroupCodes.size() > 0) {
            diseaseGroupCodes = new HashSet<>(medicalValidService.invalidDiseaseGroupCodes(diseaseGroupCodes.toArray(new String[0])));
        }

        if (projectGroupCodes.size() > 0) {
            projectGroupCodes = new HashSet<>(medicalValidService.invalidTreatGroupCodes(projectGroupCodes.toArray(new String[0])));
        }

        if (drugGroupCodes.size() > 0) {
            drugGroupCodes = new HashSet<>(medicalValidService.invalidDrugGroupCodes(drugGroupCodes.toArray(new String[0])));
        }
        if (atcCodes.size() > 0) {
            atcCodes = new HashSet<>(medicalValidService.invalidAtcCodes(atcCodes.toArray(new String[0])));
        }
        Set<String> diseaseGroupCodesFinal = diseaseGroupCodes;
        Set<String> projectGroupCodesFinal = projectGroupCodes;
        Set<String> drugGroupCodesFinal = drugGroupCodes;
        Set<String> atcCodesFinal = atcCodes;

        Map<String, InvalidExport> invalidExportMap = new HashMap<>();

        for (MedicalRuleConditionSet conditionSet : conditionSetList) {
            List<FieldInfo> fieldInfos = validFieldMap.get(conditionSet.getField());
            for (FieldInfo fieldInfo : fieldInfos) {
                // 跳过不符合的字段规则
                if (fieldInfo.getCondition() != null && !fieldInfo.getCondition().apply(conditionSet)) {
                    continue;
                }
                Object obj = ReflectHelper.getValue(conditionSet, fieldInfo.getExt());
                if (obj == null) {
                    continue;
                }
                if ("diseaseGroup".equals(fieldInfo.getType())) {
                    if (diseaseGroupCodes.size() == 0) {
                        continue;
                    }
                    Set<String> noExist = Arrays.stream(obj.toString().split("[|,]")).filter(diseaseGroupCodesFinal::contains).collect(Collectors.toSet());
                    if (noExist.size() > 0) {
                        InvalidExport invalidExport = invalidExportMap.computeIfAbsent(conditionSet.getRuleId(), k -> new InvalidExport());
                        invalidExport.getDiseaseGroupCodeList().addAll(noExist);
                    }
                } else if ("projectGroup".equals(fieldInfo.getType())) {
                    if (projectGroupCodes.size() == 0) {
                        continue;
                    }
                    Set<String> noExist = Arrays.stream(obj.toString().split("[|,]")).filter(projectGroupCodesFinal::contains).collect(Collectors.toSet());
                    if (noExist.size() > 0) {
                        InvalidExport invalidExport = invalidExportMap.computeIfAbsent(conditionSet.getRuleId(), k -> new InvalidExport());
                        invalidExport.getProjectGroupCodeList().addAll(noExist);
                    }
                } else if ("drugGroup".equals(fieldInfo.getType())) {
                    if (drugGroupCodes.size() == 0) {
                        continue;
                    }
                    Set<String> noExist = Arrays.stream(obj.toString().split("[|,]")).filter(drugGroupCodesFinal::contains).collect(Collectors.toSet());
                    if (noExist.size() > 0) {
                        InvalidExport invalidExport = invalidExportMap.computeIfAbsent(conditionSet.getRuleId(), k -> new InvalidExport());
                        invalidExport.getDrugGroupCodeList().addAll(noExist);
                    }
                } else if ("ATC".equals(fieldInfo.getType())) {
                    if (atcCodes.size() == 0) {
                        continue;
                    }
                    Set<String> noExist = Arrays.stream(obj.toString().split("[|,]")).filter(atcCodesFinal::contains).collect(Collectors.toSet());
                    if (noExist.size() > 0) {
                        InvalidExport invalidExport = invalidExportMap.computeIfAbsent(conditionSet.getRuleId(), k -> new InvalidExport());
                        invalidExport.getAtcCodeList().addAll(noExist);
                    }

                }
            }
        }
        return invalidExportMap;
    }



}
