/**
 * MedicalCaseColumnVO.java	  V1.0   2021年3月15日 上午10:34:10
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.vo;

import com.ai.modules.formal.entity.MedicalFormalCase;

import lombok.Data;

@Data
public class MedicalCaseColumnVO extends MedicalFormalCase {
	private String tableName;
	private String colName;
}
