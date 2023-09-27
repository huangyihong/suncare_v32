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
public class MedicalChargeRuleConfigIO extends MedicalRuleConfig {

    // freq
    private String frequencyExt1;
    private String frequencyCompare;
    private String frequencyExt2;
    private String accessDiseaseGroupCompare;
    private String accessDiseaseGroupExt1;
    private String accessDiseaseGroupExt1Names;
    private String accessProjectGroupLogic;
    private String accessProjectGroupCompare;
    private String accessProjectGroupExt1;
    private String accessProjectGroupExt1Names;
    private String accessVisittypeExt1;
    // age
    private String ageExt1;
    private String ageExt2;
//    private String ageExt2Name;
    // sex
    private String sexExt1;
//    private String sexExt1Name;
    // visittype
    private String visittypeExt1;
//    private String visittypeExt1Name;

    // unfitGroupsDay
    private String dayUnfitGroupsExt1;
    private String dayUnfitGroupsExt1Names;
    // unfitGroups
    private String unfitGroupsExt1;
    private String unfitGroupsExt1Names;
    // insurancetype
    private String insurancetypeExt1;
    // hosplevel
    private String hosplevelExt1;
    // orgType
    private String orgTypeExt1;
    // dept
    private String deptExt1;
    private String accessHosplevelExt1;
    private String accessOrgTypeLogic;
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

    private String indication0Ext1;
    private String indication0Ext2;
    private String indication0Ext2Names;
    private String indication0Ext3;
    private String indication0Ext3Names;
    private String indication0Ext4;

    private String indication1Ext1;
    private String indication1Ext2;
    private String indication1Ext2Names;
    private String indication1Ext3;
    private String indication1Ext3Names;
    private String indication1Ext4;

    private String indication2Ext1;
    private String indication2Ext2;
    private String indication2Ext2Names;
    private String indication2Ext3;
    private String indication2Ext3Names;
    private String indication2Ext4;

    //fitGroups
    private String fitTimeRangeExt1;
    private String fitGroupsExt1;
    private String fitGroupsExt1Names;
    private String fitGroupsExt2;
    private String fitGroupsExt3;
    private String fitGroupsExt3Names;
    //unExpense
    private String unExpenseExt1;
    //unCharge
    private String unChargeExt1;


    private String dataTimes;
    private String updateActionType;
}
