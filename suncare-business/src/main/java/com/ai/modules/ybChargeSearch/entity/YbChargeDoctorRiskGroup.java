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
 * @Description: 医生异常情况汇总表
 * @Author: jeecg-boot
 * @Date:   2023-02-09
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_doctor_risk_group")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_doctor_risk_group对象", description="医生异常情况汇总表")
public class YbChargeDoctorRiskGroup {
    
	/**科室映射后名称*/
	@Excel(name = "科室映射后名称", width = 15)
    @ApiModelProperty(value = "科室映射后名称")
	private java.lang.String deptname;
	/**科室原始名称*/
	@Excel(name = "科室原始名称", width = 15)
    @ApiModelProperty(value = "科室原始名称")
	private java.lang.String deptnameSrc;
	/**医生ID*/
	@Excel(name = "医生ID", width = 15)
    @ApiModelProperty(value = "医生ID")
	private java.lang.String doctorid;
	/**医生姓名*/
	@Excel(name = "医生姓名", width = 15)
    @ApiModelProperty(value = "医生姓名")
	private java.lang.String doctorname;
	/**数据来源*/
	@Excel(name = "数据来源", width = 15)
    @ApiModelProperty(value = "数据来源")
	private java.lang.String etlSource;
	/**基金支付金额*/
	@Excel(name = "基金支付金额", width = 15)
    @ApiModelProperty(value = "基金支付金额")
	private java.math.BigDecimal fundpaySum;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**当年门诊人次*/
	@Excel(name = "当年门诊人次", width = 15)
    @ApiModelProperty(value = "当年门诊人次")
	private java.lang.Integer mzCount;
	/**当年门诊天数*/
	@Excel(name = "当年门诊天数", width = 15)
    @ApiModelProperty(value = "当年门诊天数")
	private java.lang.Integer mzDaysCnt;
	/**当年门诊天数（有基金报销）*/
	@Excel(name = "当年门诊天数（有基金报销）", width = 15)
    @ApiModelProperty(value = "当年门诊天数（有基金报销）")
	private java.lang.Integer mzFundpayDaysCnt;
	/**医院名称*/
	@Excel(name = "医院名称", width = 15)
    @ApiModelProperty(value = "医院名称")
	private java.lang.String orgname;
	/**标签数量*/
	@Excel(name = "标签数量", width = 15)
    @ApiModelProperty(value = "标签数量")
	private java.lang.Integer tagCount;
	/**标签列表*/
	@Excel(name = "标签列表", width = 15)
    @ApiModelProperty(value = "标签列表")
	private java.lang.String tagName;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**就诊总费用*/
	@Excel(name = "就诊总费用", width = 15)
    @ApiModelProperty(value = "就诊总费用")
	private java.math.BigDecimal totalfeeSum;
	/**年*/
	@Excel(name = "年", width = 15)
    @ApiModelProperty(value = "年")
	private java.lang.String yyear;
	/**当年负责住院人次*/
	@Excel(name = "当年负责住院人次", width = 15)
    @ApiModelProperty(value = "当年负责住院人次")
	private java.lang.Integer zyCount;
}
