package com.ai.modules.formal.vo;

import com.ai.modules.formal.entity.MedicalFormalBehavior;

import lombok.Data;

@Data
public class MedicalFormalBehaviorVO extends MedicalFormalBehavior {
    private String caseIds;
    private String caseId;
}
