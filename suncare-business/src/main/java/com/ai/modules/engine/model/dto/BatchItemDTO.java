/**
 * BatchItemDTO.java	  V1.0   2021年4月29日 下午3:59:37
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.dto;

import lombok.Data;

@Data
public class BatchItemDTO {
	private String batchId;
	private String itemType;
	private String itemId;
}
