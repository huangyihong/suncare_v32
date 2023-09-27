/**
 * EngineCaseDTO.java	  V1.0   2019年11月29日 上午10:42:02
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
public class EngineCaseDTO {
	/*@ApiModelProperty(value = "主键")
	private String caseId;
	*//** 编号 */
	/*
	@ApiModelProperty(value = "编号")
	private String caseCode;
	*//** 探查名称 */
	/*
	@ApiModelProperty(value = "探查名称")
	private String caseName;
	*//** 节点集合 */
	@ApiModelProperty(value = "节点json字符串")
	private String nodes;
	/** 节点规则集合 */
	@ApiModelProperty(value = "节点规则json字符串")
	private String rules;
}
