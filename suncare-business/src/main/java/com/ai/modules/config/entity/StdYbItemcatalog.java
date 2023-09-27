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
 * @Description: 医疗服务项目医保目录
 * @Author: jeecg-boot
 * @Date:   2021-04-13
 * @Version: V1.0
 */
@Data
@TableName(value="std_yb_itemcatalog", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="std_yb_itemcatalog对象", description="医疗服务项目医保目录")
public class StdYbItemcatalog {

	/**主键id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键id")
	private java.lang.String id;
	/**亚信项目编码*/
	@Excel(name = "亚信项目编码", width = 15)
    @ApiModelProperty(value = "亚信项目编码")
	private java.lang.String itemcode;
	/**亚信项目名称*/
	@Excel(name = "亚信项目名称", width = 15)	
    @ApiModelProperty(value = "亚信项目名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String itemname;
	/**财务分类编码(原始)*/
	@Excel(name = "财务分类编码(原始)", width = 15)
    @ApiModelProperty(value = "财务分类编码(原始)")
	private java.lang.String chargeclassIdSrc;
	/**财务分类名称(原始)*/
	@Excel(name = "财务分类名称(原始)", width = 15)
    @ApiModelProperty(value = "财务分类名称(原始)")
	private java.lang.String chargeclassSrc;
	/**目录原始项目编码*/
	@Excel(name = "目录原始项目编码", width = 15)
    @ApiModelProperty(value = "目录原始项目编码")
	private java.lang.String itemcodeSrc;
	/**目录原始项目名称*/
	@Excel(name = "目录原始项目名称", width = 15)
    @ApiModelProperty(value = "目录原始项目名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String itemnameSrc;
	/**项目内涵*/
	@Excel(name = "项目内涵", width = 15)
    @ApiModelProperty(value = "项目内涵")
	private java.lang.String itemContent;
	/**除外内容*/
	@Excel(name = "除外内容", width = 15)
    @ApiModelProperty(value = "除外内容")
	private java.lang.String exceptContent;
	/**计价单位*/
	@Excel(name = "计价单位", width = 15)
    @ApiModelProperty(value = "计价单位")
	private java.lang.String chargeunit;
	/**市三级价格(元)*/
	@Excel(name = "市三级价格(元)", width = 15)
    @ApiModelProperty(value = "市三级价格(元)")
	private java.math.BigDecimal itemprice3;
	/**市二级价格(元)*/
	@Excel(name = "市二级价格(元)", width = 15)
    @ApiModelProperty(value = "市二级价格(元)")
	private java.math.BigDecimal itemprice2;
	/**市一级价格(元)*/
	@Excel(name = "市一级价格(元)", width = 15)
    @ApiModelProperty(value = "市一级价格(元)")
	private java.math.BigDecimal itemprice1;
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
	/**项目说明*/
	@Excel(name = "项目说明", width = 15)
    @ApiModelProperty(value = "项目说明")
	private java.lang.String itemnote;
	/**项目父级编码*/
	@Excel(name = "项目父级编码", width = 15)
    @ApiModelProperty(value = "项目父级编码")
	private java.lang.String parentcode;
	/**项目父级名称*/
	@Excel(name = "项目父级名称", width = 15)
    @ApiModelProperty(value = "项目父级名称")
	private java.lang.String parentname;
	/**项目地id*/
	@Excel(name = "项目地id", width = 15)
    @ApiModelProperty(value = "项目地id")
	private java.lang.String projectAreaId;
	/**项目地名称*/
	@Excel(name = "项目地名称", width = 15)
    @ApiModelProperty(value = "项目地名称")
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
	/**省级价格(元)*/
	@Excel(name = "省级价格(元)", width = 15)
	@ApiModelProperty(value = "省级价格(元)")
	private java.math.BigDecimal itempriceProvince;
	/**县一级价格(元)*/
	@Excel(name = "县一级价格(元)", width = 15)
	@ApiModelProperty(value = "县一级价格(元)")
	private java.math.BigDecimal itempricecounty1;
	/**县二级价格(元)*/
	@Excel(name = "县二级价格(元)", width = 15)
	@ApiModelProperty(value = "县二级价格(元)")
	private java.math.BigDecimal itempricecounty2;
	/**县三级价格(元)*/
	@Excel(name = "县三级价格(元)", width = 15)
	@ApiModelProperty(value = "县三级价格(元)")
	private java.math.BigDecimal itempricecounty3;
	/**首先个人自付比例*/
	@Excel(name = "首先个人自付比例", width = 15)
	@ApiModelProperty(value = "首先个人自付比例")
	private java.math.BigDecimal fundpayProp;
}
