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

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CompareFlowDTO extends CaseFlowDTO {
	/** 节点流程图 */
	@ApiModelProperty(value = "第二个节点流程图json字符串")
	private String compareFlowJson;
	/** 节点规则集合 */
	@ApiModelProperty(value = "第二个节点规则json字符串")
	private String compareRules;
	/** 计算交集或差集 */
	@ApiModelProperty(value = "计算交集或差集{intersect:交集,diff:差集}")
	private String compareType;
}
