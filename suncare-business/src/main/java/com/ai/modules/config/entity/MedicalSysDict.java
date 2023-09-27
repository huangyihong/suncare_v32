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
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 医疗字典
 * @Author: jeecg-boot
 * @Date:   2019-11-22
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_SYS_DICT")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_SYS_DICT对象", description="医疗字典")
public class MedicalSysDict {

	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**字典类型*/
	@Excel(name = "字典类型", width = 15)
    @ApiModelProperty(value = "字典类型")
	private java.lang.String dictType;
	/**字典code*/
	@Excel(name = "字典code", width = 15)
    @ApiModelProperty(value = "字典code")
	private java.lang.String code;
	/**字典value*/
	@Excel(name = "字典value", width = 15)
    @ApiModelProperty(value = "字典value")
	private java.lang.String value;
	/**字典顺序*/
	@Excel(name = "字典顺序", width = 15)
    @ApiModelProperty(value = "字典顺序")
	private java.lang.Long isOrder;
	/**描述*/
	@Excel(name = "描述", width = 15)
    @ApiModelProperty(value = "描述")
	private java.lang.String remark;
	/**机构ID*/
	@Excel(name = "机构ID", width = 15)
    @ApiModelProperty(value = "机构ID")
	private java.lang.String orgId;
	/**种类(1治疗项目 2治疗方式 3重复用药 4二限用药 5适用症 9其他)*/
	@Excel(name = "种类(1治疗项目 2治疗方式 3重复用药 4二限用药 5适用症 9其他)", width = 15)
    @ApiModelProperty(value = "种类(1治疗项目 2治疗方式 3重复用药 4二限用药 5适用症 9其他)")
	@MedicalDict(dicCode = "MEDICAL_DICT_KIND")
	private java.lang.String kind;
}
