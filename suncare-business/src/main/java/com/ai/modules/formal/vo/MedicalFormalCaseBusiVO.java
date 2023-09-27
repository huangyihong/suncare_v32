package com.ai.modules.formal.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.ai.modules.formal.entity.MedicalFormalCase;

import lombok.Data;

@Data
public class MedicalFormalCaseBusiVO extends MedicalFormalCase {
	private String busiId;
    private String busiName;
    private String custName;
    private String busiStatus;
    private BigDecimal dataMoney1;
    private BigDecimal dataMoney2;
    private String relaId;
}
