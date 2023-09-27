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
public class MedicalTreatRuleConfigIO extends MedicalRuleConfig {
    // freq
    private String frequencyExt1;
    private String frequencyCompare;
    private String frequencyExt2;
    // freq2
    private String frequencyExt3;
    private String frequencyExt4;
    private String frequency0Compare;
    private String frequency0Ext1;
    private String frequency0Ext2;
    private String frequency0Ext3;
    private String frequency0Ext4;
    private String frequency0Ext4Names;
    private String frequency1Compare;
    private String frequency1Ext1;
    private String frequency1Ext2;
    private String frequency1Ext3;
    private String frequency1Ext4;
    private String frequency1Ext4Names;


    private String accessDiseaseGroupLogic;
    private String accessDiseaseGroupCompare;
    private String accessDiseaseGroupExt1;
    private String accessDiseaseGroupExt1Names;
    private String accessProjectGroupLogic;
    private String accessProjectGroupCompare;
    private String accessProjectGroupExt1;
    private String accessProjectGroupExt1Names;
    // age
    private String ageExt1;
    private String ageExt2;
    private String accessDeptCompare;
    private String accessDeptExt1;
//    private String ageExt2Name;
    // sex
    private String sexExt1;
//    private String sexExt1Name;
    // visittype
    private String visittypeExt1;
//    private String visittypeExt1Name;
    // dept
    private String accessVisittypeCompare;
    private String accessVisittypeExt1;
    private String deptExt1;


    // unfitGroups
    private String unfitGroupsExt1;
    private String unfitGroupsExt1Names;

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
    private String indication0Ext5;
    private String indication0Ext5Names;
    private String indication0Ext4;

    private String indication1Ext1;
    private String indication1Ext2;
    private String indication1Ext2Names;
    private String indication1Ext3;
    private String indication1Ext3Names;
    private String indication1Ext5;
    private String indication1Ext5Names;
    private String indication1Ext4;

    private String indication2Ext1;
    private String indication2Ext2;
    private String indication2Ext2Names;
    private String indication2Ext3;
    private String indication2Ext3Names;
    private String indication2Ext5;
    private String indication2Ext5Names;
    private String indication2Ext4;

    private String unIndicationExt1;
    private String unIndicationExt2;
    private String unIndicationExt3;
    private String unIndicationExt4;
    private String unIndicationExt5;

    private String unIndication0Ext1;
    private String unIndication0Ext2;
    private String unIndication0Ext2Names;
    private String unIndication0Ext3;
    private String unIndication0Ext3Names;
    private String unIndication0Ext5;
    private String unIndication0Ext5Names;
    private String unIndication0Ext4;

    private String unIndication1Ext1;
    private String unIndication1Ext2;
    private String unIndication1Ext2Names;
    private String unIndication1Ext3;
    private String unIndication1Ext3Names;
    private String unIndication1Ext5;
    private String unIndication1Ext5Names;
    private String unIndication1Ext4;

    private String unIndication2Ext1;
    private String unIndication2Ext2;
    private String unIndication2Ext2Names;
    private String unIndication2Ext3;
    private String unIndication2Ext3Names;
    private String unIndication2Ext5;
    private String unIndication2Ext5Names;
    private String unIndication2Ext4;

    // diagWrong && itemWrong
    private String diseaseGroupExt1;
    private String diseaseGroupExt1Names;
    private String hisGroupsExt1;
    private String hisGroupsExt1Names;
    // 一日重复收费
    private String dayUnfitGroupsExt1;
    private String dayUnfitGroupsExt1Names;




    private String dataTimes;
    private String updateActionType;
}
