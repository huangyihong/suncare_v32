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
@ApiModel(value = "试算对象", description = "试算")
public class CaseFlowDTO {
	/** 节点流程图 */
	@ApiModelProperty(value = "节点流程图json字符串")
	private String flowJson;
	/** 节点规则集合 */
	@ApiModelProperty(value = "节点规则json字符串")
	private String rules;
}
