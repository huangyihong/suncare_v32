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

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@ApiModel(value = "试算对象", description = "试算")
public class EchartCaseFlowDTO extends CaseFlowDTO {
	@ApiModelProperty(value = "分组统计字段名")

	private String gapType;
	private String facetField;
	private BigDecimal min;
	private BigDecimal max;
	
	/** 是否DWS节点*/
	@ApiModelProperty(value = "是否DWS节点")
	@JsonProperty("isDWS")
	private boolean isDWS;
}
