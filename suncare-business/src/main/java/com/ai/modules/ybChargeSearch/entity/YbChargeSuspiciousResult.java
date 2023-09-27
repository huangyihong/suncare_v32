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
 * @Description: 可疑就诊标签汇总表
 * @Author: jeecg-boot
 * @Date:   2023-04-18
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_suspicious_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_suspicious_result对象", description="可疑就诊标签汇总表")
public class YbChargeSuspiciousResult {

	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**机构ID*/
	@Excel(name = "机构ID", width = 15)
    @ApiModelProperty(value = "机构ID")
	private java.lang.String orgid;
	/**原始医院名称*/
	@Excel(name = "原始医院名称", width = 15)
    @ApiModelProperty(value = "原始医院名称")
	private java.lang.String orgnameSrc;
	/**原始就诊号*/
	@Excel(name = "原始就诊号", width = 15)
    @ApiModelProperty(value = "原始就诊号")
	private java.lang.String visitidSrc;
	/**原始就诊类型*/
	@Excel(name = "原始就诊类型", width = 15)
    @ApiModelProperty(value = "原始就诊类型")
	private java.lang.String visittypeSrc;
	/**原始科室名称*/
	@Excel(name = "原始科室名称", width = 15)
    @ApiModelProperty(value = "原始科室名称")
	private java.lang.String deptnameSrc;
	/**医生姓名*/
	@Excel(name = "医生姓名", width = 15)
    @ApiModelProperty(value = "医生姓名")
	private java.lang.String doctorname;
	/**患者姓名*/
	@Excel(name = "患者姓名", width = 15)
    @ApiModelProperty(value = "患者姓名")
	private java.lang.String clientname;
	/**患者标签*/
	@Excel(name = "患者标签", width = 15)
    @ApiModelProperty(value = "患者标签")
	private java.lang.String patientTagName;
	/**原始年龄*/
	@Excel(name = "原始年龄", width = 15)
    @ApiModelProperty(value = "原始年龄")
	private java.lang.String yearageSrc;
	/**原始性别*/
	@Excel(name = "原始性别", width = 15)
    @ApiModelProperty(value = "原始性别")
	private java.lang.String sexSrc;
	/**就诊日期*/
	@Excel(name = "就诊日期", width = 15)
    @ApiModelProperty(value = "就诊日期")
	private java.lang.String visitdate;
	/**出院日期*/
	@Excel(name = "出院日期", width = 15)
    @ApiModelProperty(value = "出院日期")
	private java.lang.String leavedate;
	/**住院天数*/
	@Excel(name = "住院天数", width = 15)
    @ApiModelProperty(value = "住院天数")
	private java.lang.String zyDays;
	/**主诊断原始名称*/
	@Excel(name = "主诊断原始名称", width = 15)
    @ApiModelProperty(value = "主诊断原始名称")
	private java.lang.String diseasenamePrimarySrc;
	/**其他诊断原始名称*/
	@Excel(name = "其他诊断原始名称", width = 15)
    @ApiModelProperty(value = "其他诊断原始名称")
	private java.lang.String diseasenameOtherSrc;
	/**总费用*/
	@Excel(name = "总费用", width = 15)
    @ApiModelProperty(value = "总费用")
	private java.math.BigDecimal totalfee;
	/**基金支付金额*/
	@Excel(name = "基金支付金额", width = 15)
    @ApiModelProperty(value = "基金支付金额")
	private java.math.BigDecimal fundpay;
	/**标签数量*/
	@Excel(name = "标签数量", width = 15)
    @ApiModelProperty(value = "标签数量")
	private java.lang.Integer tagCount;
	/**标签列表*/
	@Excel(name = "标签列表", width = 15)
    @ApiModelProperty(value = "标签列表")
	private java.lang.String tagName;

	/**医保类型*/
	@Excel(name = "医保类型", width = 15)
	@ApiModelProperty(value = "医保类型")
	private java.lang.String insurancetypename;
}
