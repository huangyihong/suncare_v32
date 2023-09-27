/**
 * EchartCaseFlowDTO.java	  V1.0   2019年12月4日 下午2:22:13
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(value = "试算两个节点病例的交集或差集分组统计", description = "试算两个节点病例的交集或差集分组统计")
public class EchartCompareCaseFlowDTO extends CompareFlowDTO {
	@ApiModelProperty(value = "分组统计字段名")
	private String facetField;
	@ApiModelProperty(value = "分组统计类型")
	private String gapType;

	private BigDecimal min;
	private BigDecimal max;
}
