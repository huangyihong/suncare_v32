package com.ai.modules.config.vo;

import com.ai.modules.config.entity.MedicalDiseaseDiag;
import com.ai.modules.config.entity.MedicalDiseaseGroup;
import lombok.Data;

@Data
public class MedicalDiseaseDiagToGroup {
	private MedicalDiseaseGroup group;
	private MedicalDiseaseDiag params;
}
