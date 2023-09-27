package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalClinical;
import com.ai.modules.medical.entity.MedicalClinicalAccessGroup;
import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/3/11 17
 * @Description:
 */
@Data
public class MedicalClinicalVO {
    private String clinicalId;
    // 分为MedicalClinical和MedicalClinicalInfo
    private JSONObject baseInfo;
    private List<MedicalClinicalAccessGroup> approveGroup;
    private List<MedicalClinicalAccessGroup> rejectGroup;
    private List<MedicalClinicalRangeGroup> drugRange;
    private List<MedicalClinicalRangeGroup> projectRange;
}
