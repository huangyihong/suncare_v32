package com.ai.modules.ybChargeSearch.entity;

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
 * @Description: 医保收费项目汇总及对比结果表
 * @Author: jeecg-boot
 * @Date:   2022-10-09
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_itemcompare_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_itemcompare_result对象", description="医保收费项目汇总及对比结果表")
public class YbChargeItemcompareResult {
    
	/**收费等级*/
	@Excel(name = "收费等级", width = 15)
    @ApiModelProperty(value = "收费等级")
	private java.lang.String chargeattri;
	/**对比机构均次均值金额*/
	@Excel(name = "对比机构均次均值金额", width = 15)
    @ApiModelProperty(value = "对比机构均次均值金额")
	private java.math.BigDecimal compareAvgAmt;
	/**本机构均次金额*/
	@Excel(name = "本机构均次金额", width = 15)
    @ApiModelProperty(value = "本机构均次金额")
	private java.math.BigDecimal currentAvgAmt;
	/**本机构年度项目总额*/
	@Excel(name = "本机构年度项目总额", width = 15)
    @ApiModelProperty(value = "本机构年度项目总额")
	private java.math.BigDecimal currentTotalAmt;
	/**异常标签*/
	@Excel(name = "异常标签", width = 15)
    @ApiModelProperty(value = "异常标签")
	private java.lang.String errorFlag;
	/**本机构年度项目总额×超出均值百分比*/
	@Excel(name = "本机构年度项目总额×超出均值百分比", width = 15)
    @ApiModelProperty(value = "本机构年度项目总额×超出均值百分比")
	private java.math.BigDecimal exceedValue;
	/**医院收费名称*/
	@Excel(name = "医院收费名称", width = 15)
    @ApiModelProperty(value = "医院收费名称")
	private java.lang.String hisItemname;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**收费类别*/
	@Excel(name = "收费类别", width = 15)
    @ApiModelProperty(value = "收费类别")
	private java.lang.String itemclass;
	/**医保收费名称*/
	@Excel(name = "医保收费名称", width = 15)
    @ApiModelProperty(value = "医保收费名称")
	private java.lang.String itemname;
	/**单价*/
	@Excel(name = "单价", width = 15)
    @ApiModelProperty(value = "单价")
	private java.math.BigDecimal itemprice;
	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
    @ApiModelProperty(value = "机构名称")
	private java.lang.String orgname;
	/**超出均值百分比*/
	@Excel(name = "超出均值百分比", width = 15)
    @ApiModelProperty(value = "超出均值百分比")
	private java.math.BigDecimal percentValue;
	/**数量*/
	@Excel(name = "数量", width = 15)
    @ApiModelProperty(value = "数量")
	private java.math.BigDecimal sumAmount;
	/**金额*/
	@Excel(name = "金额", width = 15)
    @ApiModelProperty(value = "金额")
	private java.math.BigDecimal sumFee;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
	private java.lang.String visittype;
	/**收费年份*/
	@Excel(name = "收费年份", width = 15)
    @ApiModelProperty(value = "收费年份")
	private java.lang.String year;
}
