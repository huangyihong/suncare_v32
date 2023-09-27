package com.ai.modules.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: 药品医保目录
 * @Author: jeecg-boot
 * @Date:   2021-04-13
 * @Version: V1.0
 */
@Data
@TableName(value="std_yb_drugcatalog", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="std_yb_drugcatalog对象", description="药品医保目录")
public class StdYbDrugcatalog {

	/**主键id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键id")
	private java.lang.String id;

	/**药监药品本位码*/
	@Excel(name = "药监药品本位码", width = 15)
    @ApiModelProperty(value = "药监药品本位码")
	private java.lang.String drugcode869;
	/**药监药品名称*/
	@Excel(name = "药监药品名称", width = 15)
    @ApiModelProperty(value = "药监药品名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String drugname869;
	/**医保药品代码*/
	@Excel(name = "医保药品代码", width = 15)
    @ApiModelProperty(value = "医保药品代码")
	private java.lang.String drugcodeYbregister;
	/**注册药品名称*/
	@Excel(name = "注册药品名称", width = 15)
    @ApiModelProperty(value = "注册药品名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String drugnameYbregister;

	/**亚信药品编码*/
	@Excel(name = "亚信药品编码", width = 15)
    @ApiModelProperty(value = "亚信药品编码")
	private java.lang.String drugcode;

	/**亚信药品名称*/
	@Excel(name = "亚信药品名称", width = 15)
    @ApiModelProperty(value = "亚信药品名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String drugname;
	/**亚信药品收费类别编码*/
	@Excel(name = "亚信药品收费类别编码", width = 15)
    @ApiModelProperty(value = "亚信药品收费类别编码")
	private java.lang.String chargeclassId;
	/**亚信药品收费类别名称*/
	@Excel(name = "亚信药品收费类别名称", width = 15)
    @ApiModelProperty(value = "亚信药品收费类别名称")
	private java.lang.String chargeclass;
	/**医保目录编码*/
	@Excel(name = "医保目录编码", width = 15)
    @ApiModelProperty(value = "医保药品编码(原始)")
	private java.lang.String drugcodeSrc;
	/**医保药品名称(原始)*/
	@Excel(name = "医保药品名称(原始)", width = 15)
    @ApiModelProperty(value = "医保药品名称(原始)")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String drugnameSrc;
	/**剂型名称(原始)*/
	@Excel(name = "剂型名称(原始)", width = 15)
    @ApiModelProperty(value = "剂型名称(原始)")
	private java.lang.String dosageNameSrc;
	/**规格*/
	@Excel(name = "规格", width = 15)
    @ApiModelProperty(value = "规格")
	private java.lang.String specificaion;
	/**药品生产企业编码*/
	@Excel(name = "药品生产企业编码", width = 15)
    @ApiModelProperty(value = "药品生产企业编码")
	private java.lang.String manufactorCode;
	/**药品生产企业名称*/
	@Excel(name = "药品生产企业名称", width = 15)
    @ApiModelProperty(value = "药品生产企业名称")
	private java.lang.String manufactor;
	/**药品父级编码*/
	@Excel(name = "药品父级编码", width = 15)
    @ApiModelProperty(value = "药品父级编码")
	private java.lang.String parentcode;
	/**药品父级名称*/
	@Excel(name = "药品父级名称", width = 15)
    @ApiModelProperty(value = "药品父级名称")
	private java.lang.String parentname;
	/**收费项目等级名称(原始)*/
	@Excel(name = "收费项目等级名称(原始)", width = 15)
    @ApiModelProperty(value = "收费项目等级名称(原始)")
	private java.lang.String chargeattriSrc;
	/**收费项目等级编码(映射后)*/
	@Excel(name = "收费项目等级编码(映射后)", width = 15)
    @ApiModelProperty(value = "收费项目等级编码(映射后)")
	private java.lang.String chargeattricode;
	/**收费项目等级名称(映射后)*/
	@Excel(name = "收费项目等级名称(映射后)", width = 15)
    @ApiModelProperty(value = "收费项目等级名称(映射后)")
	private java.lang.String chargeattriname;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String itemnote;
	/**适用地id*/
	@Excel(name = "适用地id", width = 15)
    @ApiModelProperty(value = "适用地id")
	private java.lang.String projectAreaId;
	/**适用地名称*/
	@Excel(name = "适用地名称", width = 15)
    @ApiModelProperty(value = "适用地名称")
	private java.lang.String projectArea;
	/**适用的所有制形式*/
	@Excel(name = "适用的所有制形式", width = 15)
    @ApiModelProperty(value = "适用的所有制形式")
	private java.lang.String owntype;
	/**适用的所有制形式名称*/
	@Excel(name = "适用的所有制形式名称", width = 15)
    @ApiModelProperty(value = "适用的所有制形式名称")
	private java.lang.String owntypeName;
	/**有效开始时间*/
	@Excel(name = "有效开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "有效开始时间")
	private java.util.Date startdate;
	/**有效截止时间*/
	@Excel(name = "有效截止时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "有效截止时间")
	private java.util.Date enddate;
	/**医保目录版本*/
	@Excel(name = "医保目录版本", width = 15)
    @ApiModelProperty(value = "医保目录版本")
	private java.lang.String fileName;
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
	/**修改原因*/
	@Excel(name = "修改原因", width = 15)
	@ApiModelProperty(value = "修改原因")
	private java.lang.String updateReason;

	/**最小包装数量*/
	@Excel(name = "最小包装数量", width = 15)
	@ApiModelProperty(value = "最小包装数量")
	private java.lang.String packageNum;

	/**最小包装单位*/
	@Excel(name = "最小包装单位", width = 15)
	@ApiModelProperty(value = "最小包装单位")
	private java.lang.String packageUnit;

	/**最小制剂单位*/
	@Excel(name = "最小制剂单位", width = 15)
	@ApiModelProperty(value = "最小制剂单位")
	private java.lang.String preparationUnit;

	/**包装材质*/
	@Excel(name = "包装材质", width = 15)
	@ApiModelProperty(value = "包装材质")
	private java.lang.String packMaterial;
}
