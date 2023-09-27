package com.ai.modules.medical.entity.dto;

import com.ai.modules.medical.entity.MedicalDruguse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2021/1/8 16
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalDruguseIO extends MedicalDruguse {

    private String group1Name;
    private String group1DiseaseGroup;
    private String group1DiseaseGroupName;
    private String group1TreatGroup;
    private String group1TreatGroupName;
    private String group1Treatment;
    private String group1TreatmentName;


    private String group2Name;
    private String group2DiseaseGroup;
    private String group2DiseaseGroupName;
    private String group2TreatGroup;
    private String group2TreatGroupName;
    private String group2Treatment;
    private String group2TreatmentName;

    private String group3Name;
    private String group3DiseaseGroup;
    private String group3DiseaseGroupName;
    private String group3TreatGroup;
    private String group3TreatGroupName;
    private String group3Treatment;
    private String group3TreatmentName;
}
