/**
 * RangeEntity.java	  V1.0   2019年12月3日 上午11:35:53
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RangeResult {
	private Double min;
	private Double max;
	private Double avg;
	private Double median;
	private Double mode;
	private Long count;
}
