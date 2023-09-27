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
 * @Date:   2021-03-16
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_RULE_CONDITION_COLUMN")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_RULE_CONDITION_COLUMN对象", description="规则依赖字段表")
public class MedicalRuleConditionColumn {
    
	/**类型*/
	@Excel(name = "类型", width = 15)
    @ApiModelProperty(value = "类型")
	private java.lang.String relyType;
	/**字段名*/
	@Excel(name = "字段名", width = 15)
    @ApiModelProperty(value = "字段名")
	private java.lang.String relyColumn;
	/**requiredField*/
	@Excel(name = "requiredField", width = 15)
    @ApiModelProperty(value = "requiredField")
	private java.lang.String requiredField;
	/**类型{access:准入条件，judge:判断条件}*/
	@Excel(name = "fieldType", width = 15)
    @ApiModelProperty(value = "fieldType")
	private java.lang.String fieldType;
}
