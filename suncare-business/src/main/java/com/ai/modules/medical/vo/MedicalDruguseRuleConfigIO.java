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
public class MedicalDruguseRuleConfigIO extends MedicalRuleConfig {

    // indication
    private String accessAgeLogic;
    private String accessAgeExt1;
    private String accessAgeExt2;
    private String accessSexExt1;
    private String reviewHisDiseaseExt1;
    private String excludeInHospExt1;
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
    // 检验结果
    private String indication00Ext4ItemType;
    private String indication00Ext4ItemValue;
    private String indication00Ext4ItemName;
    private String indication00Ext4ValueType;
    private String indication00Ext4Value;
    private String indication00Ext4Unit;
    private String indication00Ext4UnitName;
    private String indication01Ext4ItemType;
    private String indication01Ext4ItemValue;
    private String indication01Ext4ItemName;
    private String indication01Ext4ValueType;
    private String indication01Ext4Value;
    private String indication01Ext4Unit;
    private String indication01Ext4UnitName;
    private String indication02Ext4ItemType;
    private String indication02Ext4ItemValue;
    private String indication02Ext4ItemName;
    private String indication02Ext4ValueType;
    private String indication02Ext4Value;
    private String indication02Ext4Unit;
    private String indication02Ext4UnitName;
    private String indication10Ext4ItemType;
    private String indication10Ext4ItemValue;
    private String indication10Ext4ItemName;
    private String indication10Ext4ValueType;
    private String indication10Ext4Value;
    private String indication10Ext4Unit;
    private String indication10Ext4UnitName;
    private String indication11Ext4ItemType;
    private String indication11Ext4ItemValue;
    private String indication11Ext4ItemName;
    private String indication11Ext4ValueType;
    private String indication11Ext4Value;
    private String indication11Ext4Unit;
    private String indication11Ext4UnitName;
    private String indication12Ext4ItemType;
    private String indication12Ext4ItemValue;
    private String indication12Ext4ItemName;
    private String indication12Ext4ValueType;
    private String indication12Ext4Value;
    private String indication12Ext4Unit;
    private String indication12Ext4UnitName;
    private String indication20Ext4ItemType;
    private String indication20Ext4ItemValue;
    private String indication20Ext4ItemName;
    private String indication20Ext4ValueType;
    private String indication20Ext4Value;
    private String indication20Ext4Unit;
    private String indication20Ext4UnitName;
    private String indication21Ext4ItemType;
    private String indication21Ext4ItemValue;
    private String indication21Ext4ItemName;
    private String indication21Ext4ValueType;
    private String indication21Ext4Value;
    private String indication21Ext4Unit;
    private String indication21Ext4UnitName;
    private String indication22Ext4ItemType;
    private String indication22Ext4ItemValue;
    private String indication22Ext4ItemName;
    private String indication22Ext4ValueType;
    private String indication22Ext4Value;
    private String indication22Ext4Unit;
    private String indication22Ext4UnitName;

    private String dataTimes;
    private String updateActionType;
}
