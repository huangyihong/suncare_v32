package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2020/3/12 10
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalClinicalRangeGroupVO extends MedicalClinicalRangeGroup {
    private String clinicalCode;
    private String clinicalName;
    private String remark;
}
