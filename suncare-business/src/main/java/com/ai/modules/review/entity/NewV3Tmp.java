package com.ai.modules.review.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 医保风控数据推送表
 * @Author: jeecg-boot
 * @Date:   2020-09-07
 * @Version: V1.0
 */
@Data
@TableName("NEWS_V3_TMP")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="NEWS_V3_TMP对象", description="医保风控数据推送表")
public class NewV3Tmp {
	/**主键ID*/
	@Excel(name = "主键ID", width = 15)
	@ApiModelProperty(value = "主键ID")
    @TableId("TMP_ID")
	private String tmpId;
	/**周期ID*/
	@Excel(name = "周期ID", width = 15)
	@ApiModelProperty(value = "周期ID")
	private String issueId;
	/**周期名称*/
	@Excel(name = "周期名称", width = 15)
	@ApiModelProperty(value = "周期名称")
	private String issueName;
	/**项目客户ID*/
	@Excel(name = "项目客户ID", width = 15)
	@ApiModelProperty(value = "项目客户ID")
	private String xmkhId;
	/**项目客户名称*/
	@Excel(name = "项目客户名称", width = 15)
	@ApiModelProperty(value = "项目客户名称")
	private String xmkhName;
	/**项目名称ID*/
	@Excel(name = "项目名称ID", width = 15)
	@ApiModelProperty(value = "项目名称ID")
	private String xmmcId;
	/**项目名称*/
	@Excel(name = "项目名称", width = 15)
	@ApiModelProperty(value = "项目名称")
	private String xmmcName;
	/**违规项目名称ID*/
	@Excel(name = "违规项目名称ID", width = 15)
	@ApiModelProperty(value = "违规项目名称ID")
	private String wgxmmcId;
	/**违规项目名称*/
	@Excel(name = "违规项目名称", width = 15)
	@ApiModelProperty(value = "违规项目名称")
	private String wgxmmcName;
	/**任务批次ID*/
	@Excel(name = "任务批次ID", width = 15)
	@ApiModelProperty(value = "任务批次ID")
	private String taskBatchId;
	/**任务批次名称*/
	@Excel(name = "任务批次名称", width = 15)
	@ApiModelProperty(value = "任务批次名称")
	private String taskBatchName;
	/**就诊ID*/
	@Excel(name = "就诊ID", width = 15)
	@ApiModelProperty(value = "就诊ID")
	private String visitid;
	/**原始就诊ID*/
	@Excel(name = "原始就诊ID", width = 15)
	@ApiModelProperty(value = "原始就诊ID")
	private String oldVisitid;
	/**0.待处理,1.已处理*/
	@Excel(name = "0.待处理,1.已处理", width = 15)
	@ApiModelProperty(value = "0.待处理,1.已处理")
	private String handleStatus;
	/**不合规行为类型ID*/
	@Excel(name = "不合规行为类型ID", width = 15)
	@ApiModelProperty(value = "不合规行为类型ID")
	private String bhgxwlxId;
	/**不合规行为类型显示值*/
	@Excel(name = "不合规行为类型显示值", width = 15)
	@ApiModelProperty(value = "不合规行为类型显示值")
	private String bhgxwlxName;
	/**不合规行为名称ID*/
	@Excel(name = "不合规行为名称ID", width = 15)
	@ApiModelProperty(value = "不合规行为名称ID")
	private String bhgxwmcId;
	/**不合规行为名称显示值*/
	@Excel(name = "不合规行为名称显示值", width = 15)
	@ApiModelProperty(value = "不合规行为名称显示值")
	private String bhgxwmcName;
	/**不合规行为释义*/
	@Excel(name = "不合规行为释义", width = 15)
	@ApiModelProperty(value = "不合规行为释义")
	private String bhgxwsy;
	/**费用总金额（元）*/
	@Excel(name = "费用总金额（元）", width = 15)
	@ApiModelProperty(value = "费用总金额（元）")
	private java.math.BigDecimal totalfee2;
	/*	*//**现金支付（元）*//*
	@Excel(name = "现金支付（元）", width = 15)
    @ApiModelProperty(value = "现金支付（元）")
	private java.math.BigDecimal selfpay;*/
	/**医保支付（元）*/
	@Excel(name = "医保支付（元）", width = 15)
	@ApiModelProperty(value = "医保支付（元）")
	private java.math.BigDecimal fundpay;
	/**统筹支付（元）*//*
	@Excel(name = "统筹支付（元）", width = 15)
    @ApiModelProperty(value = "统筹支付（元）")
	private java.math.BigDecimal overallPay;*/
	/**药品费（元）*/
	@Excel(name = "药品费（元）", width = 15)
	@ApiModelProperty(value = "药品费（元）")
	private java.math.BigDecimal drugfee;
	/**涉及金额（元）*/
	@Excel(name = "涉及金额（元）", width = 15)
	@ApiModelProperty(value = "涉及金额（元）")
	private java.math.BigDecimal sjje;
	/**监管建议*/
	@Excel(name = "监管建议", width = 15)
	@ApiModelProperty(value = "监管建议")
	private String jgjy;
	/**医疗机构编码*/
	@Excel(name = "医疗机构编码", width = 15)
	@ApiModelProperty(value = "医疗机构编码")
	private String hospitalId;
	/**医疗机构名称*/
	@Excel(name = "医疗机构名称", width = 15)
	@ApiModelProperty(value = "医疗机构名称")
	private String hospitalName;
	/**科室编码*/
	@Excel(name = "科室编码", width = 15)
	@ApiModelProperty(value = "科室编码")
	private String deptid;
	/**科室名称*/
	@Excel(name = "科室名称", width = 15)
	@ApiModelProperty(value = "科室名称")
	private String deptname;

	/**就诊金额（元）*/
	@Excel(name = "就诊金额（元）", width = 15)
	@ApiModelProperty(value = "就诊金额（元）")
	private java.math.BigDecimal totalfee;
	/**患者编号*/
	@Excel(name = "患者编号", width = 15)
	@ApiModelProperty(value = "患者编号")
	private String clientid;
	/**患者姓名*/
	@Excel(name = "患者姓名", width = 15)
	@ApiModelProperty(value = "患者姓名")
	private String clientname;
	/**性别*/
	@Excel(name = "性别", width = 15)
	@ApiModelProperty(value = "性别")
	private String sex;
	/**年龄*/
	@Excel(name = "年龄", width = 15)
	@ApiModelProperty(value = "年龄")
	private String age;
	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
	@ApiModelProperty(value = "就诊类型")
	private String visittype;
	/**就诊时间*/
	@Excel(name = "就诊时间", width = 15)
	@ApiModelProperty(value = "就诊时间")
	private String visitdate;
	/**诊断疾病名称*/
	@Excel(name = "诊断疾病名称", width = 15)
	@ApiModelProperty(value = "诊断疾病名称")
	private String zdjbmc;
	/**治疗结果*/
	@Excel(name = "治疗结果", width = 15)
	@ApiModelProperty(value = "治疗结果")
	private String result;
	/**住院天数*/
	@Excel(name = "住院天数", width = 15)
	@ApiModelProperty(value = "住院天数")
	private String zyDays;
	/**出院日期*/
	@Excel(name = "出院日期", width = 15)
	@ApiModelProperty(value = "出院日期")
	private String leavedate;
	/**出院原因*/
	@Excel(name = "出院原因", width = 15)
	@ApiModelProperty(value = "出院原因")
	private String leavereason;
	/**付费方式*/
	@Excel(name = "付费方式", width = 15)
	@ApiModelProperty(value = "付费方式")
	private String payway;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
	@ApiModelProperty(value = "创建人")
	private String createBy;
	/**创建人名称*/
	@Excel(name = "创建人名称", width = 15)
	@ApiModelProperty(value = "创建人名称")
	private String createByName;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "创建时间")
	private Date createTime;
	/**处理人*/
	@Excel(name = "处理人", width = 15)
	@ApiModelProperty(value = "处理人")
	private String updateBy;
	/**处理人名称*/
	@Excel(name = "处理人名称", width = 15)
	@ApiModelProperty(value = "处理人名称")
	private String updateName;
	/**处理时间*/
	@Excel(name = "处理时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "处理时间")
	private Date updateTime;

	/**就诊医师编码*/
	@Excel(name = "就诊医师编码", width = 15)
	@ApiModelProperty(value = "就诊医师编码")
	private java.lang.String doctorid;
	/**就诊医师姓名*/
	@Excel(name = "就诊医师姓名", width = 15)
	@ApiModelProperty(value = "就诊医师姓名")
	private java.lang.String doctorname;
	/**数据来源*/
	@Excel(name = "数据来源", width = 15)
	@ApiModelProperty(value = "数据来源")
	private java.lang.String dataSource;
	/**投保类型*/
	@Excel(name = "投保类型", width = 15)
	@ApiModelProperty(value = "投保类型")
	private java.lang.String insurancetype;
	/**计算住院天数*/
	@Excel(name = "计算住院天数", width = 15)
	@ApiModelProperty(value = "计算住院天数")
	private String zyDaysCalculate;
	/**最大涉案金额*/
	@Excel(name = "最大涉案金额", width = 15)
	@ApiModelProperty(value = "最大涉案金额")
	private java.math.BigDecimal maxActionMoney;


}
