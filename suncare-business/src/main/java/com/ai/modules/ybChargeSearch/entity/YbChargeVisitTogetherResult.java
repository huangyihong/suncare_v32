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
 * @Description: 结伴就医明细表
 * @Author: jeecg-boot
 * @Date:   2023-02-22
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_visit_together_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_visit_together_result对象", description="结伴就医明细表")
public class YbChargeVisitTogetherResult {

	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**算法名称*/
	@Excel(name = "算法名称", width = 15)
    @ApiModelProperty(value = "算法名称")
	private java.lang.String tagName;
	/**结伴组ID*/
	@Excel(name = "结伴组ID", width = 15)
    @ApiModelProperty(value = "结伴组ID")
	private java.lang.String groupid;
	/**结伴组名称*/
	@Excel(name = "结伴组名称", width = 15)
    @ApiModelProperty(value = "结伴组名称")
	private java.lang.String groupname;
	/**结伴人数*/
	@Excel(name = "结伴人数", width = 15)
    @ApiModelProperty(value = "结伴人数")
	private java.lang.String groupPatientQty;
	/**结伴次数*/
	@Excel(name = "结伴次数", width = 15)
    @ApiModelProperty(value = "结伴次数")
	private java.lang.String groupCnt;
	/**结伴所有人ID*/
	@Excel(name = "结伴所有人ID", width = 15)
    @ApiModelProperty(value = "结伴所有人ID")
	private java.lang.String clientidList;
	/**结伴所有人姓名*/
	@Excel(name = "结伴所有人姓名", width = 15)
    @ApiModelProperty(value = "结伴所有人姓名")
	private java.lang.String clientnameList;
	/**结伴就诊原始医疗机构名称列表*/
	@Excel(name = "结伴就诊原始医疗机构名称列表", width = 15)
    @ApiModelProperty(value = "结伴就诊原始医疗机构名称列表")
	private java.lang.String orgnameSrcList;
	/**就诊ID*/
	@Excel(name = "就诊ID", width = 15)
    @ApiModelProperty(value = "就诊ID")
	private java.lang.String visitid;
	/**原始就诊ID*/
	@Excel(name = "原始就诊ID", width = 15)
    @ApiModelProperty(value = "原始就诊ID")
	private java.lang.String visitidSrc;
	/**机构ID*/
	@Excel(name = "机构ID", width = 15)
    @ApiModelProperty(value = "机构ID")
	private java.lang.String orgid;
	/**患者编号*/
	@Excel(name = "患者编号", width = 15)
    @ApiModelProperty(value = "患者编号")
	private java.lang.String clientid;
	/**姓名*/
	@Excel(name = "姓名", width = 15)
    @ApiModelProperty(value = "姓名")
	private java.lang.String clientname;
	/**性别*/
	@Excel(name = "性别", width = 15)
    @ApiModelProperty(value = "性别")
	private java.lang.String sex;
	/**年龄*/
	@Excel(name = "年龄", width = 15)
    @ApiModelProperty(value = "年龄")
	private java.lang.String yearage;
	/**原始就诊类型*/
	@Excel(name = "原始就诊类型", width = 15)
    @ApiModelProperty(value = "原始就诊类型")
	private java.lang.String visittypeSrc;
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
	private java.lang.String zyDaysCalculate;
	/**主诊断原始名称*/
	@Excel(name = "主诊断原始名称", width = 15)
    @ApiModelProperty(value = "主诊断原始名称")
	private java.lang.String diseasenamePrimarySrc;
	/**其他诊断原始名称*/
	@Excel(name = "其他诊断原始名称", width = 15)
    @ApiModelProperty(value = "其他诊断原始名称")
	private java.lang.String diseasenameOtherSrc;
	/**本次就诊原始医疗机构名称*/
	@Excel(name = "本次就诊原始医疗机构名称", width = 15)
    @ApiModelProperty(value = "本次就诊原始医疗机构名称")
	private java.lang.String orgnameSrc;
	/**基金支付金额*/
	@Excel(name = "基金支付金额", width = 15)
    @ApiModelProperty(value = "基金支付金额")
	private java.math.BigDecimal fundpay;
	/**医疗总金额*/
	@Excel(name = "医疗总金额", width = 15)
    @ApiModelProperty(value = "医疗总金额")
	private java.math.BigDecimal totalfee;
	/**个人支付金额*/
	@Excel(name = "个人支付金额", width = 15)
    @ApiModelProperty(value = "个人支付金额")
	private java.math.BigDecimal selfpay;
	/**科室名称(原始)*/
	@Excel(name = "科室名称(原始)", width = 15)
	@ApiModelProperty(value = "科室名称(原始)")
	private java.lang.String deptnameSrc;
	/**医生姓名*/
	@Excel(name = "医生姓名", width = 15)
	@ApiModelProperty(value = "医生姓名")
	private java.lang.String doctorname;
	/**参保类型*/
	@Excel(name = "参保类型", width = 15)
	@ApiModelProperty(value = "参保类型")
	private java.lang.String insurancetype;

	/**原主键id*/
	@Excel(name = "原主键id", width = 15)
	@ApiModelProperty(value = "原主键id")
	private java.lang.String srcId;

	/**标注内容*/
	@Excel(name = "标注内容", width = 15)
	@ApiModelProperty(value = "标注内容")
	private java.lang.String labelName;

	/**标注人*/
	@Excel(name = "标注人", width = 15)
	@ApiModelProperty(value = "标注人")
	private java.lang.String labelUser;

	/**标注时间*/
	@Excel(name = "标注时间", width = 15)
	@ApiModelProperty(value = "标注时间")
	private java.lang.String labelTime;


}
