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
 * @Description: 重复用药
 * @Author: jeecg-boot
 * @Date:   2021-06-08
 * @Version: V1.0
 */
@Data
@TableName(value="medical_yb_drug", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="medical_yb_drug对象", description="重复用药")
public class MedicalYbDrug {

	/**主键id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键id")
	private java.lang.String id;
	/**编码*/
	@Excel(name = "编码", width = 15)
    @ApiModelProperty(value = "编码")
	private java.lang.String code;
	/**名称*/
	@Excel(name = "名称", width = 15)
    @ApiModelProperty(value = "名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String name;
	/**父级编码*/
	@Excel(name = "父级编码", width = 15)
    @ApiModelProperty(value = "父级编码")
	private java.lang.String parentCode;
	/**父级名称*/
	@Excel(name = "父级名称", width = 15)
    @ApiModelProperty(value = "父级名称")
	private java.lang.String parentName;
	/**指向表*/
	@Excel(name = "指向表", width = 15)
    @ApiModelProperty(value = "指向表")
	private java.lang.String tableType;
	/**描述*/
	@Excel(name = "描述", width = 15)
    @ApiModelProperty(value = "描述")
	private java.lang.String remark;
	/**新增人*/
	@Excel(name = "新增人", width = 15)
	@ApiModelProperty(value = "新增人")
	private java.lang.String createStaff;
	/**新增人姓名*/
	@Excel(name = "新增人姓名", width = 15)
	@ApiModelProperty(value = "新增人姓名")
	private java.lang.String createStaffName;
	/**新增时间*/
	@Excel(name = "新增时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "新增时间")
	private java.util.Date createTime;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
	@ApiModelProperty(value = "修改人")
	private java.lang.String updateStaff;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
	@ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateStaffName;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**顺序*/
	@Excel(name = "顺序", width = 15)
	@ApiModelProperty(value = "顺序")
	private java.lang.Long isOrder;
	/**剂型代码*/
	@Excel(name = "剂型代码", width = 15)
	@ApiModelProperty(value = "剂型代码")
	private java.lang.String dosageCode;
	/**剂型名称*/
	@Excel(name = "剂型名称", width = 15)
	@ApiModelProperty(value = "剂型名称")
	private java.lang.String dosage;

	/**规则级别*/
	@Excel(name = "规则级别", width = 15)
	@ApiModelProperty(value = "规则级别")
	private java.lang.String ruleGrade;

	/**级别备注*/
	@Excel(name = "级别备注", width = 15)
	@ApiModelProperty(value = "级别备注")
	private java.lang.String ruleGradeRemark;
}
