package com.ai.modules.medical.vo;

import lombok.Data;

/**
 * @Auther: zhangpeng
 * @Date: 2020/4/27 17
 * @Description:
 */
@Data
public class MedicalClinicalAccessGroupImport {
    private String clinicalId;
    private String clinicalCode;
    private Integer groupNo;
    private String groupName;
    private Integer patientAgeMin;
    private Integer patientAgeMax;
    private String patientAgeUnit;
    private String hospBelongTo;
    private String diseaseGroup;
    private String operation;
    private String checkItem;
    private String checkItemDesc;
    private String drugGroup;
    private String pathology;
}
