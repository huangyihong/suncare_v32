package com.ai.modules.drg.vo;

import com.ai.modules.drg.entity.MedicalVisitDrg;
import lombok.Data;

/**
 * @author : zhangly
 * @date : 2023/4/10 9:44
 */
@Data
public class DrgTargetDtlVo {
    /**满足MDC诊断编码*/
    private String mdcDiagCode;
    /**满足MDC诊断名称*/
    private String mdcDiagName;
    /**满足ADRG诊断编码*/
    private String adrgDiagCode;
    /**满足MDC诊断名称*/
    private String adrgDiagName;
    /**是否器官移植,呼吸机使用超过96小时或ECMO*/
    private String mdcaFlag;
    /**是否出生<29天内的新生儿*/
    private String babyFlag;
    /**是否HIV感染疾病及相关操作*/
    private String hivFlag;
    /**是否多发严重创伤*/
    private String multWoundFlag;
    /**手术编码*/
    private String surgeryCodes;
    /**手术名称*/
    private String surgeryNames;
    /**手术时间*/
    private String surgeryTimes;
    /**是否可找到对应分组条件的ADRG组*/
    private String meetAdrgFlag;
    /**是否手术室手术*/
    private String roomSurgeryFlag;
    /**是否需要判断并发症或合并症*/
    private String judgeSecDiagFlag;
    /**是否有效MCC*/
    private String mcc;
    /**是否有效CC*/
    private String cc;
    /**DRG主诊断编码*/
    private String drgDiagCode;
    /**DRG主诊断名称*/
    private String drgDiagName;

    private MedicalVisitDrg visitDrg;
}
