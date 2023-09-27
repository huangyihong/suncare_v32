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

@Data
@ApiModel(value = "试算对象", description = "试算两个节点病例的交集或差集")
public class CompareCaseFlowDTO extends CompareFlowDTO {
	@ApiModelProperty(value = "页码")
	private int pageNo = 1;
	@ApiModelProperty(value = "每页显示条数")
	private int pageSize = 10;
}
