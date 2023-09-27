package com.ai.modules.medical.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 规则依赖字段表
 * @Author: jeecg-boot
 * @Date:   2022-01-20
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_RULE_RELY")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_RULE_RELY对象", description="规则依赖字段表")
public class MedicalRuleRely {

	/**ruleType*/
	@Excel(name = "ruleType", width = 15)
    @ApiModelProperty(value = "ruleType")
	private java.lang.String ruleType;
	/**ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "ID")
	private java.lang.String id;
	/**判断条件字段*/
	@Excel(name = "判断条件字段", width = 15)
    @ApiModelProperty(value = "判断条件字段")
	private java.lang.String judgeColumn;
	/**规则类型*/
	@Excel(name = "规则类型", width = 15)
    @ApiModelProperty(value = "规则类型")
	private java.lang.String ruleLimit;
	/**准入条件字段*/
	@Excel(name = "准入条件字段", width = 15)
    @ApiModelProperty(value = "准入条件字段")
	private java.lang.String accessColumn;
	/**规则id*/
	@Excel(name = "规则id", width = 15)
    @ApiModelProperty(value = "规则id")
	private java.lang.String ruleId;
	/**规则名称*/
	@Excel(name = "规则名称", width = 15)
    @ApiModelProperty(value = "规则名称")
	private java.lang.String ruleName;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**不合规行为名称*/
	@Excel(name = "不合规行为名称", width = 15)
    @ApiModelProperty(value = "不合规行为名称")
	private java.lang.String actionName;
	/**不合规行为ID*/
	@Excel(name = "不合规行为ID", width = 15)
    @ApiModelProperty(value = "不合规行为ID")
	private java.lang.String actionId;
}
