package com.ai.modules.config.entity;

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
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 药品分组项
 * @Author: jeecg-boot
 * @Date:   2020-03-02
 * @Version: V1.0
 */
@Data
@TableName(value="MEDICAL_DRUG_GROUP_ITEM", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DRUG_GROUP_ITEM对象", description="药品分组项")
public class MedicalDrugGroupItem {
    
	/**药品项ID*/
	@Excel(name = "药品项ID", width = 15)
    @ApiModelProperty(value = "药品项ID")
	@TableId("ITEM_ID")
	private java.lang.String itemId;
	/**分组ID*/
	@Excel(name = "分组ID", width = 15)
    @ApiModelProperty(value = "分组ID")
	private java.lang.String groupId;
	/**药品项编码*/
	@Excel(name = "药品项编码", width = 15)
    @ApiModelProperty(value = "药品项编码")
	private java.lang.String code;
	/**药品项的值*/
	@Excel(name = "药品项的值", width = 15)
    @ApiModelProperty(value = "药品项的值")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String value;
	/**顺序*/
	@Excel(name = "顺序", width = 15)
    @ApiModelProperty(value = "顺序")
	private java.lang.Long isOrder;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**指向表**/
	@Excel(name = "指向表", width = 15)
    @ApiModelProperty(value = "指向表")
	private java.lang.String tableType;
}
