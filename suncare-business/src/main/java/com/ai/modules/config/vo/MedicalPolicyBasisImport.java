package com.ai.modules.config.vo;

import com.ai.modules.config.entity.MedicalPolicyBasis;
import lombok.Data;

@Data
public class MedicalPolicyBasisImport extends MedicalPolicyBasis {
    private String startEndDateStr;//使用时间
    private String importActionType;//更新标志
}
