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
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 医疗字典子项
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_DICT_ITEM")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DICT_ITEM对象", description="医疗字典子项")
public class MedicalDictItem {

	/**字典项ID*/
	@Excel(name = "字典项ID", width = 15)
    @ApiModelProperty(value = "字典项ID")
	@TableId("ITEM_ID")
	private java.lang.String itemId;
	/**分组ID*/
	@Excel(name = "分组ID", width = 15)
    @ApiModelProperty(value = "分组ID")
	private java.lang.String groupId;
	/**字典项编码*/
	@Excel(name = "字典项编码", width = 15)
    @ApiModelProperty(value = "字典项编码")
	private java.lang.String code;
	/**字典项的值*/
	@Excel(name = "字典项的值", width = 15)
    @ApiModelProperty(value = "字典项的值")
	private java.lang.String value;
	/**顺序*/
	@Excel(name = "顺序", width = 15)
    @ApiModelProperty(value = "顺序")
	private java.lang.Long isOrder;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**字典类型*/
	@Excel(name = "字典类型", width = 15)
    @ApiModelProperty(value = "字典类型")
	private java.lang.String kind;
}
