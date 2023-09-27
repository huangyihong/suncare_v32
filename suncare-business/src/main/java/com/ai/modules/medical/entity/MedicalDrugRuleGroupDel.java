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
 * @Description: 医疗字典分组子项
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_DRUG_RULE_GROUP_DEL")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DRUG_RULE_GROUP_DEL对象", description="医疗字典分组子项")
public class MedicalDrugRuleGroupDel {
    
	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "id")
	private java.lang.String id;
	/**groupId*/
	@Excel(name = "groupId", width = 15)
    @ApiModelProperty(value = "groupId")
	private java.lang.String groupId;
	/**code*/
	@Excel(name = "code", width = 15)
    @ApiModelProperty(value = "code")
	private java.lang.String code;
	/**value*/
	@Excel(name = "value", width = 15)
    @ApiModelProperty(value = "value")
	private java.lang.String value;
	/**isOrder*/
	@Excel(name = "isOrder", width = 15)
    @ApiModelProperty(value = "isOrder")
	private java.lang.Long isOrder;
}
