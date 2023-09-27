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
 * @Description: RULE_ID
 * @Author: jeecg-boot
 * @Date:   2019-12-23
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_DRUG_RULE_ID")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DRUG_RULE_ID对象", description="RULE_ID")
public class MedicalDrugRuleId {

	/**唯一编码*/
	@Excel(name = "唯一编码", width = 15)
    @ApiModelProperty(value = "唯一编码")
	private java.lang.String ruleId;
	/**药品编码*/
	@Excel(name = "药品编码", width = 15)
    @ApiModelProperty(value = "药品编码")
	private java.lang.String drugCode;

	@Excel(name = "药品类别ATC,DRUG", width = 15)
	@ApiModelProperty(value = "药品类别ATC,DRUG")
	private java.lang.String drugType;
}
