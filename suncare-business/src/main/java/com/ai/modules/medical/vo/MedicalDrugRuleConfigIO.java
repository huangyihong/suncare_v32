package com.ai.modules.medical.vo;

import org.jeecg.common.aspect.annotation.AutoResultMap;

import com.ai.modules.medical.entity.MedicalRuleConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2020/12/18 14
 * @Description:
 */

@EqualsAndHashCode(callSuper = true)
@Data
@AutoResultMap
public class MedicalDrugRuleConfigIO extends MedicalRuleConfig {

    // age
    private String ageExt1;
    private String ageExt2;
    private String accessDeptCompare;
    private String accessDeptExt1;
    private String accessDiseaseGroupLogic;
    private String accessDiseaseGroupCompare;
    private String accessDiseaseGroupExt1;
    private String accessDiseaseGroupExt1Names;
    // sex
    private String sexExt1;
    // visittype
    private String visittypeExt1;

    // insurancetype
    private String insurancetypeExt1;
    // hosplevel
    private String hosplevelExt1;
    // orgType
    private String orgTypeExt1;
    // dept
    private String deptExt1;
    private String accessHosplevelCompare;
    private String accessHosplevelExt1;
    private String accessOrgTypeLogic;
    private String accessOrgTypeCompare;
    private String accessOrgTypeExt1;
    // indication
    private String accessAgeLogic;
    private String accessAgeExt1;
    private String accessAgeExt2;
    private String accessSexExt1;
    private String reviewHisDiseaseExt1;
    private String indicationExt1;
    private String indicationExt2;
    private String indicationExt3;
    private String indicationExt4;
    private String indicationExt5;

    private String indication0Ext1;
    private String indication0Ext2;
    private String indication0Ext2Names;
    private String indication0Ext3;
    private String indication0Ext3Names;
    private String indication0Ext4;
    private String indication0Ext5;
    private String indication0Ext5Names;

    private String indication1Ext1;
    private String indication1Ext2;
    private String indication1Ext2Names;
    private String indication1Ext3;
    private String indication1Ext3Names;
    private String indication1Ext4;
    private String indication1Ext5;
    private String indication1Ext5Names;

    private String indication2Ext1;
    private String indication2Ext2;
    private String indication2Ext2Names;
    private String indication2Ext3;
    private String indication2Ext3Names;
    private String indication2Ext4;
    private String indication2Ext5;
    private String indication2Ext5Names;

    //unExpense
    private String unExpenseExt1;

    // hosplevelType
    private String hosplevelTypeExt1;
    private String hosplevelTypeExt2;
    private String hosplevelTypeExt3;
    private String hosplevelType0Ext1;
    private String hosplevelType0Ext2;
    private String hosplevelType0Ext3;
    private String hosplevelType1Ext1;
    private String hosplevelType1Ext2;
    private String hosplevelType1Ext3;
    private String hosplevelType2Ext1;
    private String hosplevelType2Ext2;
    private String hosplevelType2Ext3;
    private String hosplevelType3Ext1;
    private String hosplevelType3Ext2;
    private String hosplevelType3Ext3;
    private String hosplevelType4Ext1;
    private String hosplevelType4Ext2;
    private String hosplevelType4Ext3;
    private String hosplevelType5Ext1;
    private String hosplevelType5Ext2;
    private String hosplevelType5Ext3;
    // secDrugExt1
    private String secDrugExt1;
    private String secDrugExt1Names;
    // unpayDrugExt1
    private String unpayDrugExt1;
    private String unpayDrugExt1Names;
    private String unpayDrugExt2;
    // dosage
    private String durationTypeExt1;
    private String dosageExt1;
    private String dosageExt2;
    // payDuration
//    private String durationTypeExt1;
    private String accessVisittypeExt1;
    private String payDurationExt1;
    private String payDurationExt2;
    // drugDuration
//    private String durationTypeExt1;
    private String drugDurationExt1;
    private String drugDurationExt2;

    // drugusage
    private String accessProjectGroupLogic;
    private String accessProjectGroupCompare;
    private String accessProjectGroupExt1;
    private String accessProjectGroupExt1Names;
    private String accessDrugGroupLogic;
    private String accessDrugGroupCompare;
    private String accessDrugGroupExt1;
    private String accessDrugGroupExt1Names;
    private String drugUsageExt1;
    private String drugUsageExt2;
    private String drugUsageExt3;
    // 015
    private String diseaseGroupExt1;
    private String diseaseGroupExt1Names;

    //药品使用缺少必要药品或项目
    private String reviewHisItemExt1;
    private String itemOrDrugGroupExt1;
    private String itemOrDrugGroupExt1Names;
    private String itemOrDrugGroupExt3;
    private String itemOrDrugGroupExt3Names;

    // XTDRQ限特殊人群
    private String xtdrq0Ext1;
    private String xtdrq0Ext2;
    private String xtdrq0Ext3;
    private String xtdrq0Ext4;
    private String xtdrq0Ext4Names;
    private String xtdrq0Ext5;
    private String xtdrq0Ext6;
    private String xtdrq0Ext6Names;
    private String xtdrq0Ext7;
    private String xtdrq0Ext8;
    private String xtdrq0Ext8Names;
    private String xtdrq0Ext9;
    private String xtdrq0Ext10;
    private String xtdrq1Ext1;
    private String xtdrq1Ext2;
    private String xtdrq1Ext3;
    private String xtdrq1Ext4;
    private String xtdrq1Ext4Names;
    private String xtdrq1Ext5;
    private String xtdrq1Ext6;
    private String xtdrq1Ext6Names;
    private String xtdrq1Ext7;
    private String xtdrq1Ext8;
    private String xtdrq1Ext8Names;
    private String xtdrq1Ext9;
    private String xtdrq1Ext10;
    private String xtdrq2Ext1;
    private String xtdrq2Ext2;
    private String xtdrq2Ext3;
    private String xtdrq2Ext4;
    private String xtdrq2Ext4Names;
    private String xtdrq2Ext5;
    private String xtdrq2Ext6;
    private String xtdrq2Ext6Names;
    private String xtdrq2Ext7;
    private String xtdrq2Ext8;
    private String xtdrq2Ext8Names;
    private String xtdrq2Ext9;
    private String xtdrq2Ext10;

    private String dataTimes;
    private String updateActionType;
}
