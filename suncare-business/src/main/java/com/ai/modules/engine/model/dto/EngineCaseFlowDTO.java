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

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "试算对象", description = "试算")
public class EngineCaseFlowDTO extends CaseFlowDTO {	
	@ApiModelProperty(value = "页码")
	private int pageNo = 1;
	@ApiModelProperty(value = "每页显示条数")
	private int pageSize = 10;
	
	/** 节点规则试算展示结果字段*/
	@ApiModelProperty(value = "节点规则试算展示结果字段")
	private String[] cols;
	/** 是否DWS节点*/
	@ApiModelProperty(value = "是否DWS节点")
	@JsonProperty("isDWS")
	private boolean isDWS;
	
	/** 排序字段*/
	@ApiModelProperty(value = "排序字段")
	private String column;
	/** 排序方式*/
	@ApiModelProperty(value = "排序方式")
	private String order;
}
