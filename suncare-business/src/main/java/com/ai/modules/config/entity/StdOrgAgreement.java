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
 * @Description: 医疗机构医保协议相关参数
 * @Author: jeecg-boot
 * @Date:   2020-12-03
 * @Version: V1.0
 */
@Data
@TableName(value="std_org_agreement", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="std_org_agreement对象", description="医疗机构医保协议相关参数")
public class StdOrgAgreement {

	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**医疗机构编码*/
	@Excel(name = "医疗机构编码", width = 15)
    @ApiModelProperty(value = "医疗机构编码")
	private java.lang.String orgid;
	/**医疗机构名称*/
	@Excel(name = "医疗机构名称", width = 15)
    @ApiModelProperty(value = "医疗机构名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String orgname;
	/**医疗保险类别代码*/
	@Excel(name = "医疗保险类别代码", width = 15)
    @ApiModelProperty(value = "医疗保险类别代码")
	private java.lang.String surancetypecode;
	/**医疗保险类别名称*/
	@Excel(name = "医疗保险类别名称", width = 15)
    @ApiModelProperty(value = "医疗保险类别名称")
	private java.lang.String surancetypename;
	/**均次住院天数*/
	@Excel(name = "均次住院天数", width = 15)
    @ApiModelProperty(value = "均次住院天数")
	private java.lang.Integer pertimeZydays;
	/**单病种结算率(%)*/
	@Excel(name = "单病种结算率(%)", width = 15)
    @ApiModelProperty(value = "单病种结算率(%)")
	private java.math.BigDecimal drgsettleRatio;
	/**均次住院费用*/
	@Excel(name = "均次住院费用", width = 15)
    @ApiModelProperty(value = "均次住院费用")
	private java.math.BigDecimal zypertimeAmt;
	/**床日费用*/
	@Excel(name = "床日费用", width = 15)
    @ApiModelProperty(value = "床日费用")
	private java.math.BigDecimal zyDayavgAmt;
	/**药费占住院费用比例(%)*/
	@Excel(name = "药费占住院费用比例(%)", width = 15)
    @ApiModelProperty(value = "药费占住院费用比例(%)")
	private java.math.BigDecimal medFeeratio;
	/**百人门诊住院率(%)*/
	@Excel(name = "百人门诊住院率(%)", width = 15)
    @ApiModelProperty(value = "百人门诊住院率(%)")
	private java.math.BigDecimal outConvertInRatio;
	/**实际报销比例(%)*/
	@Excel(name = "实际报销比例(%)", width = 15)
    @ApiModelProperty(value = "实际报销比例(%)")
	private java.math.BigDecimal fundpayRatio;
	/**适用开始时间*/
	@Excel(name = "适用开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "适用开始时间")
	private java.util.Date startdate;
	/**适用截止时间*/
	@Excel(name = "适用截止时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "适用截止时间")
	private java.util.Date enddate;
	/**政策依据*/
	@Excel(name = "政策依据", width = 15)
    @ApiModelProperty(value = "政策依据")
	private java.lang.String policybasis;
	/**新增人*/
	@Excel(name = "新增人", width = 15)
	@ApiModelProperty(value = "新增人")
	private java.lang.String createUser;
	/**新增人姓名*/
	@Excel(name = "新增人姓名", width = 15)
	@ApiModelProperty(value = "新增人姓名")
	private java.lang.String createUsername;
	/**新增时间*/
	@Excel(name = "新增时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "新增时间")
	private java.util.Date createTime;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
	@ApiModelProperty(value = "修改人")
	private java.lang.String updateUser;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
	@ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateUsername;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
}
