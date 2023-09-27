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
 * @Description: 合理用药配置
 * @Author: jeecg-boot
 * @Date:   2020-11-05
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_DRUGUSE")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DRUGUSE对象", description="合理用药配置")
public class MedicalDruguse {

	/**规则主键*/
	@TableId("RULE_ID")
	@Excel(name = "规则主键", width = 15)
    @ApiModelProperty(value = "规则主键")
	private java.lang.String ruleId;
	@Excel(name = "规则编码", width = 15)
	@ApiModelProperty(value = "规则编码")
	private java.lang.String ruleCode;
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
	/**性别*/
	@Excel(name = "性别", width = 15)
    @ApiModelProperty(value = "性别")
	private java.lang.String sex;
	/**年龄范围-数学表达式*/
	@Excel(name = "年龄范围-数学表达式", width = 15)
    @ApiModelProperty(value = "年龄范围-数学表达式")
	private java.lang.String age;
	@Excel(name = "年龄单位", width = 15)
	@ApiModelProperty(value = "年龄单位")
	private java.lang.String ageUnit;
	/**不合规行为类型*/
	@Excel(name = "不合规行为类型", width = 15)
    @ApiModelProperty(value = "不合规行为类型")
	private java.lang.String actionType;
	/**提示信息*/
	@Excel(name = "提示信息", width = 15)
    @ApiModelProperty(value = "提示信息")
	private java.lang.String message;
	/**政策依据*/
	@Excel(name = "政策依据", width = 15)
    @ApiModelProperty(value = "政策依据")
	private java.lang.String ruleBasis;
	/**政策依据类别*/
	@Excel(name = "政策依据类别", width = 15)
    @ApiModelProperty(value = "政策依据类别")
	private java.lang.String ruleBasisType;
	/**不合规行为ID*/
	@Excel(name = "不合规行为ID", width = 15)
    @ApiModelProperty(value = "不合规行为ID")
	private java.lang.String actionId;
	/**不合规行为名称*/
	@Excel(name = "不合规行为名称", width = 15)
    @ApiModelProperty(value = "不合规行为名称")
	private java.lang.String actionName;
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
}
