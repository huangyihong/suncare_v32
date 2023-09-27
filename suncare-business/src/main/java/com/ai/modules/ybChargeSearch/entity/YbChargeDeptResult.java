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
 * @Description: 科室就诊金额前10结果表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_dept_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_dept_result对象", description="科室就诊金额前10结果表")
public class YbChargeDeptResult {

	/**数量*/
	@Excel(name = "数量", width = 15)
    @ApiModelProperty(value = "数量")
	private java.math.BigDecimal amount;
	/**收费日期*/
	@Excel(name = "收费日期", width = 15)
    @ApiModelProperty(value = "收费日期")
	private java.lang.String charge;
	/**收费等级*/
	@Excel(name = "收费等级", width = 15)
    @ApiModelProperty(value = "收费等级")
	private java.lang.String chargeattri;
	/**科室名称*/
	@Excel(name = "科室名称", width = 15)
    @ApiModelProperty(value = "科室名称")
	private java.lang.String deptname;
	/**诊断*/
	@Excel(name = "诊断", width = 15)
    @ApiModelProperty(value = "诊断")
	private java.lang.String dis;
	/**医生姓名*/
	@Excel(name = "医生姓名", width = 15)
    @ApiModelProperty(value = "医生姓名")
	private java.lang.String doctorname;
	/**金额*/
	@Excel(name = "金额", width = 15)
    @ApiModelProperty(value = "金额")
	private java.math.BigDecimal fee;
	/**报销金额*/
	@Excel(name = "报销金额", width = 15)
    @ApiModelProperty(value = "报销金额")
	private java.math.BigDecimal fundpay;
	/**医院项目名称*/
	@Excel(name = "医院项目名称", width = 15)
    @ApiModelProperty(value = "医院项目名称")
	private java.lang.String hisItemname;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**医保收费编码*/
	@Excel(name = "医保收费编码", width = 15)
    @ApiModelProperty(value = "医保收费编码")
	private java.lang.String itemcode;
	/**医保项目名称*/
	@Excel(name = "医保项目名称", width = 15)
    @ApiModelProperty(value = "医保项目名称")
	private java.lang.String itemname;
	/**单价*/
	@Excel(name = "单价", width = 15)
    @ApiModelProperty(value = "单价")
	private java.math.BigDecimal itemprice;
	/**出院日期*/
	@Excel(name = "出院日期", width = 15)
    @ApiModelProperty(value = "出院日期")
	private java.lang.String leavedate;
	/**患者名称*/
	@Excel(name = "患者名称", width = 15)
    @ApiModelProperty(value = "患者名称")
	private java.lang.String name;
	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
    @ApiModelProperty(value = "机构名称")
	private java.lang.String orgname;
	/**金额排名*/
	@Excel(name = "金额排名", width = 15)
    @ApiModelProperty(value = "金额排名")
	private java.lang.Integer rank1;
	/**性别*/
	@Excel(name = "性别", width = 15)
    @ApiModelProperty(value = "性别")
	private java.lang.String sex;
	/**标签*/
	@Excel(name = "标签", width = 15)
    @ApiModelProperty(value = "标签")
	private java.lang.String tagName;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**总费用*/
	@Excel(name = "总费用", width = 15)
    @ApiModelProperty(value = "总费用")
	private java.math.BigDecimal totalfee;
	/**就诊日期*/
	@Excel(name = "就诊日期", width = 15)
    @ApiModelProperty(value = "就诊日期")
	private java.lang.String visitdate;
	/**就诊号*/
	@Excel(name = "就诊号", width = 15)
    @ApiModelProperty(value = "就诊号")
	private java.lang.String visitid;
	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
	private java.lang.String visittype;
	/**就诊年份*/
	@Excel(name = "就诊年份", width = 15)
    @ApiModelProperty(value = "就诊年份")
	private java.lang.String year;
	/**年龄*/
	@Excel(name = "年龄", width = 15)
    @ApiModelProperty(value = "年龄")
	private java.lang.String yearage;

	/**机构orgid*/
	@Excel(name = "机构orgid", width = 15)
	@ApiModelProperty(value = "机构orgid")
	private java.lang.String orgid;

	/**自付比例*/
	@Excel(name = "自付比例", width = 15)
	@ApiModelProperty(value = "自付比例")
	private java.lang.String selfpayProp;


	/**患者id*/
	@Excel(name = "患者id", width = 15)
	@ApiModelProperty(value = "患者id")
	private java.lang.String  clientid;
	/**身份证号*/
	@Excel(name = "身份证号", width = 15)
	@ApiModelProperty(value = "身份证号")
	private java.lang.String  idNo;

	/**住院天数*/
	@Excel(name = "住院天数", width = 15)
	@ApiModelProperty(value = "住院天数")
	private java.lang.Integer zyDays;

	/**疾病主诊断*/
	@Excel(name = "疾病主诊断", width = 15)
	@ApiModelProperty(value = "疾病主诊断")
	private java.lang.String  disPrimary;
	/**疾病次诊断*/
	@Excel(name = "疾病次诊断", width = 15)
	@ApiModelProperty(value = "疾病次诊断")
	private java.lang.String  disSecondary;

	/**单位地址*/
	@Excel(name = "单位地址", width = 15)
	@ApiModelProperty(value = "单位地址")
	private java.lang.String  workplacename;

	/**医保类型*/
	@Excel(name = "医保类型", width = 15)
	@ApiModelProperty(value = "医保类型")
	private java.lang.String insurancetypename;
}
