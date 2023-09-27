/**
 * QueryMedicalRuleConfig.java	  V1.0   2022年1月25日 上午11:07:37
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.vo;

import java.util.List;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class QueryMedicalRuleConfigVO {
	/**规则ID*/
	@Excel(name = "规则ID", width = 15)
    @ApiModelProperty(value = "规则ID")
	@TableId("RULE_ID")
	private java.lang.String ruleId;
	/**项目编码(多值)*/
	@Excel(name = "项目编码(多值)", width = 15)
    @ApiModelProperty(value = "项目编码(多值)")
	private java.lang.String itemCodes;
	/**项目名称(多值)*/
	@Excel(name = "项目名称(多值)", width = 15)
    @ApiModelProperty(value = "项目名称(多值)")
	private java.lang.String itemNames;
	/**项目类型(多值)*/
	@Excel(name = "项目类型(多值)", width = 15)
    @ApiModelProperty(value = "项目类型(多值)")
	private java.lang.String itemTypes;
	/**规则编码*/
	@Excel(name = "规则编码", width = 15)
    @ApiModelProperty(value = "规则编码")
	private java.lang.String ruleCode;
	/**规则类型*/
	@Excel(name = "规则类型", width = 15)
    @ApiModelProperty(value = "规则类型")
	private java.lang.String ruleType;
	/**规则限定类型*/
	@Excel(name = "规则限定类型", width = 15)
	@ApiModelProperty(value = "规则限定类型")
	private java.lang.String ruleLimit;
	/**规则来源*/
	@Excel(name = "规则来源", width = 15)
    @ApiModelProperty(value = "规则来源")
	private java.lang.String ruleSource;
	/**规则来源编码*/
	@Excel(name = "规则来源编码", width = 15)
	@ApiModelProperty(value = "规则来源编码")
	private java.lang.String ruleSourceCode;
	/**规则依据*/
	@Excel(name = "规则依据", width = 15)
    @ApiModelProperty(value = "规则依据")
	private java.lang.String ruleBasis;
	/**政策依据类别*/
	@Excel(name = "政策依据类别", width = 15)
    @ApiModelProperty(value = "政策依据类别")
	private java.lang.String ruleBasisType;
	/**提示信息*/
	@Excel(name = "提示信息", width = 15)
    @ApiModelProperty(value = "提示信息")
	private java.lang.String message;
	/**不合理行为ID*/
	@Excel(name = "不合理行为ID", width = 15)
    @ApiModelProperty(value = "不合理行为ID")
	private java.lang.String actionId;
	/**不合理行为类型*/
	@Excel(name = "不合理行为类型", width = 15)
    @ApiModelProperty(value = "不合理行为类型")
	private java.lang.String actionType;
	/**不合理行为名称*/
	@Excel(name = "不合理行为名称", width = 15)
    @ApiModelProperty(value = "不合理行为名称")
	private java.lang.String actionName;
	/**开始时间*/
	@Excel(name = "开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "开始时间")
	private java.util.Date startTime;
	/**结束时间*/
	@Excel(name = "结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "结束时间")
	private java.util.Date endTime;
	/**试算状态(不存储，只做显示)*/
	@Excel(name = "试算状态(不存储，只做显示)", width = 15)
    @ApiModelProperty(value = "试算状态(不存储，只做显示)")
	private java.lang.String trailStatus;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
    @ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateUsername;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
	private java.lang.String updateUser;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;

	@Excel(name = "状态", width = 15)
	@ApiModelProperty(value = "状态")
	private java.lang.String status;
	
	private List<MedicalRuleConditionSet> conditionList;

	@Override
	public String toString() {
		return "QueryMedicalRuleConfigVO [ruleId=" + ruleId + ", itemCodes=" + itemCodes + ", itemNames=" + itemNames
				+ ", itemTypes=" + itemTypes + ", ruleCode=" + ruleCode + ", ruleType=" + ruleType + ", ruleLimit="
				+ ruleLimit + ", ruleSource=" + ruleSource + ", ruleSourceCode=" + ruleSourceCode + ", ruleBasis="
				+ ruleBasis + ", ruleBasisType=" + ruleBasisType + ", message=" + message + ", actionId=" + actionId
				+ ", actionType=" + actionType + ", actionName=" + actionName + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", trailStatus=" + trailStatus + ", updateUsername=" + updateUsername
				+ ", updateUser=" + updateUser + ", updateTime=" + updateTime + ", createUsername=" + createUsername
				+ ", createUser=" + createUser + ", createTime=" + createTime + ", status=" + status
				+ ", conditionList=" + conditionList + "]";
	}
}
