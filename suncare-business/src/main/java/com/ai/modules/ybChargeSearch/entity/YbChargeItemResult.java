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
 * @Description: 收费项目汇总结果表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_item_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_item_result对象", description="收费项目汇总结果表")
public class YbChargeItemResult {

	/**收费等级*/
	@Excel(name = "收费等级", width = 15)
    @ApiModelProperty(value = "收费等级")
	private java.lang.String chargeattri;
	/**科室名称*/
	@Excel(name = "科室名称", width = 15)
    @ApiModelProperty(value = "科室名称")
	private java.lang.String deptname;
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
	/**医保收费编码*/
	@Excel(name = "医保收费编码", width = 15)
    @ApiModelProperty(value = "医保收费编码")
	private java.lang.String itemcode;
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
	/**数量*/
	@Excel(name = "数量", width = 15)
    @ApiModelProperty(value = "数量")
	private java.math.BigDecimal sumAmount;
	/**汇总金额*/
	@Excel(name = "汇总金额", width = 15)
    @ApiModelProperty(value = "汇总金额")
	private java.math.BigDecimal sumFee;
	/**标签*/
	@Excel(name = "标签", width = 15)
    @ApiModelProperty(value = "标签")
	private java.lang.String tagName;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
	private java.lang.String visittype;
	/**年*/
	@Excel(name = "年", width = 15)
    @ApiModelProperty(value = "年")
	private java.lang.String year;

	/**机构orgid*/
	@Excel(name = "机构orgid", width = 15)
	@ApiModelProperty(value = "机构orgid")
	private java.lang.String orgid;

	/**自付比例*/
	@Excel(name = "自付比例", width = 15)
	@ApiModelProperty(value = "自付比例")
	private java.lang.String selfpayProp;

	/**医保类型*/
	@Excel(name = "医保类型", width = 15)
	@ApiModelProperty(value = "医保类型")
	private java.lang.String insurancetypename;
}
