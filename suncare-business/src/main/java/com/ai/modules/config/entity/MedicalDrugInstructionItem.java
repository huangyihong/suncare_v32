package com.ai.modules.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 药品说明书子项
 * @Author: jeecg-boot
 * @Date:   2020-11-03
 * @Version: V1.0
 */
@Data
@TableName(value="MEDICAL_DRUG_INSTRUCTION_ITEM", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DRUG_INSTRUCTION_ITEM对象", description="药品说明书子项")
public class MedicalDrugInstructionItem {

	/**药品说明书子项ID*/
	@TableId(type = IdType.ID_WORKER_STR)
	@Excel(name = "药品说明书子项ID", width = 15)
    @ApiModelProperty(value = "药品说明书子项ID")
	private java.lang.String itemId;
	/**药品说明书ID*/
	@Excel(name = "药品说明书ID", width = 15)
    @ApiModelProperty(value = "药品说明书ID")
	private java.lang.String parentId;
	/**子项编码*/
	@Excel(name = "子项编码", width = 15)
    @ApiModelProperty(value = "子项编码")
	private java.lang.String itemCode;
	/**子项的值*/
	@Excel(name = "子项的值", width = 15)
    @ApiModelProperty(value = "子项的值")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String itemValue;
	/**顺序*/
	@Excel(name = "顺序", width = 15)
    @ApiModelProperty(value = "顺序")
	private java.lang.Long isOrder;
	/**指向表*/
	@Excel(name = "指向表", width = 15)
    @ApiModelProperty(value = "指向表")
	private java.lang.String tableType;
}
