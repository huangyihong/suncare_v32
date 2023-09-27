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
 * @Description: 规则依赖字段明细表
 * @Author: jeecg-boot
 * @Date:   2021-05-12
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_RULE_RELY_DTL")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_RULE_RELY_DTL对象", description="规则依赖字段明细表")
public class MedicalRuleRelyDtl {
    
	/**ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "ID")
	private java.lang.String id;
	/**规则id*/
	@Excel(name = "规则id", width = 15)
    @ApiModelProperty(value = "规则id")
	private java.lang.String ruleId;
	/**表名*/
	@Excel(name = "表名", width = 15)
    @ApiModelProperty(value = "表名")
	private java.lang.String tableName;
	/**表字段名*/
	@Excel(name = "表字段名", width = 15)
    @ApiModelProperty(value = "表字段名")
	private java.lang.String columnName;
	/**不合规行为ID*/
	@Excel(name = "不合规行为ID", width = 15)
    @ApiModelProperty(value = "不合规行为ID")
	private java.lang.String actionId;
	/**不合规行为名称*/
	@Excel(name = "不合规行为名称", width = 15)
    @ApiModelProperty(value = "不合规行为ID")
	private java.lang.String actionName;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**类型{access:准入条件，judge:判断条件}*/
	@Excel(name = "fieldType", width = 15)
    @ApiModelProperty(value = "fieldType")
	private java.lang.String fieldType;
	/**规则分类{CASE:模型,DRUG:药品合规,CHARGE:收费合规,TREAT:合理诊疗}*/
	@Excel(name = "ruleType", width = 15)
    @ApiModelProperty(value = "ruleType")
	private java.lang.String ruleType;
	/**规则类型*/
	@Excel(name = "ruleLimit", width = 15)
    @ApiModelProperty(value = "ruleLimit")
	private java.lang.String ruleLimit;
}
