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
 * @Description: 分解住院明细数据
 * @Author: jeecg-boot
 * @Date:   2023-06-15
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_dws_inhospital_apart")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_dws_inhospital_apart对象", description="分解住院明细数据")
public class YbChargeDwsInhospitalApart {
    
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**yx患者编号*/
	@Excel(name = "yx患者编号", width = 15)
    @ApiModelProperty(value = "yx患者编号")
	private java.lang.String clientid;
	/**身份证件号码*/
	@Excel(name = "身份证件号码", width = 15)
    @ApiModelProperty(value = "身份证件号码")
	private java.lang.String idNo;
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
	/**本次住院就诊id*/
	@Excel(name = "本次住院就诊id", width = 15)
    @ApiModelProperty(value = "本次住院就诊id")
	private java.lang.String zyIdThis;
	/**本次住院病案号*/
	@Excel(name = "本次住院病案号", width = 15)
    @ApiModelProperty(value = "本次住院病案号")
	private java.lang.String caseIdThis;
	/**本次住院日期*/
	@Excel(name = "本次住院日期", width = 15)
    @ApiModelProperty(value = "本次住院日期")
	private java.lang.String admitdateThis;
	/**本次出院日期*/
	@Excel(name = "本次出院日期", width = 15)
    @ApiModelProperty(value = "本次出院日期")
	private java.lang.String leavedateThis;
	/**本次住院医疗机构编码*/
	@Excel(name = "本次住院医疗机构编码", width = 15)
    @ApiModelProperty(value = "本次住院医疗机构编码")
	private java.lang.String orgidThis;
	/**本次住院医疗机构名称*/
	@Excel(name = "本次住院医疗机构名称", width = 15)
    @ApiModelProperty(value = "本次住院医疗机构名称")
	private java.lang.String orgnameThis;
	/**科室名称（原始）*/
	@Excel(name = "科室名称（原始）", width = 15)
    @ApiModelProperty(value = "科室名称（原始）")
	private java.lang.String deptnameThis;
	/**本次住院负责医师姓名*/
	@Excel(name = "本次住院负责医师姓名", width = 15)
    @ApiModelProperty(value = "本次住院负责医师姓名")
	private java.lang.String doctornameThis;
	/**本次住院天数*/
	@Excel(name = "本次住院天数", width = 15)
    @ApiModelProperty(value = "本次住院天数")
	private java.lang.Integer zyDaysThis;
	/**本次就诊疾病名称*/
	@Excel(name = "本次就诊疾病名称", width = 15)
    @ApiModelProperty(value = "本次就诊疾病名称")
	private java.lang.String diseasenameThis;
	/**本次住院总费用*/
	@Excel(name = "本次住院总费用", width = 15)
    @ApiModelProperty(value = "本次住院总费用")
	private java.math.BigDecimal totalfeeThis;
	/**本次住院医保基金支付金额*/
	@Excel(name = "本次住院医保基金支付金额", width = 15)
    @ApiModelProperty(value = "本次住院医保基金支付金额")
	private java.math.BigDecimal fundpayThis;
	/**上次就诊ID*/
	@Excel(name = "上次就诊ID", width = 15)
    @ApiModelProperty(value = "上次就诊ID")
	private java.lang.String zyIdLast;
	/**上次住院病案号*/
	@Excel(name = "上次住院病案号", width = 15)
    @ApiModelProperty(value = "上次住院病案号")
	private java.lang.String caseIdLast;
	/**上次住院日期*/
	@Excel(name = "上次住院日期", width = 15)
    @ApiModelProperty(value = "上次住院日期")
	private java.lang.String admitdateLast;
	/**上次出院日期*/
	@Excel(name = "上次出院日期", width = 15)
    @ApiModelProperty(value = "上次出院日期")
	private java.lang.String leavedateLast;
	/**上次住院医疗机构编码*/
	@Excel(name = "上次住院医疗机构编码", width = 15)
    @ApiModelProperty(value = "上次住院医疗机构编码")
	private java.lang.String orgidLast;
	/**上次就诊医疗机构名称*/
	@Excel(name = "上次就诊医疗机构名称", width = 15)
    @ApiModelProperty(value = "上次就诊医疗机构名称")
	private java.lang.String orgnameLast;
	/**上次就诊科室名称*/
	@Excel(name = "上次就诊科室名称", width = 15)
    @ApiModelProperty(value = "上次就诊科室名称")
	private java.lang.String deptnameLast;
	/**上次住院负责医师姓名*/
	@Excel(name = "上次住院负责医师姓名", width = 15)
    @ApiModelProperty(value = "上次住院负责医师姓名")
	private java.lang.String doctornameLast;
	/**上次住院天数*/
	@Excel(name = "上次住院天数", width = 15)
    @ApiModelProperty(value = "上次住院天数")
	private java.lang.Integer zyDaysLast;
	/**上次疾病诊断名称*/
	@Excel(name = "上次疾病诊断名称", width = 15)
    @ApiModelProperty(value = "上次疾病诊断名称")
	private java.lang.String diseasenameLast;
	/**上次住院总费用*/
	@Excel(name = "上次住院总费用", width = 15)
    @ApiModelProperty(value = "上次住院总费用")
	private java.math.BigDecimal totalfeeLast;
	/**上次住院医保基金支付金额*/
	@Excel(name = "上次住院医保基金支付金额", width = 15)
    @ApiModelProperty(value = "上次住院医保基金支付金额")
	private java.math.BigDecimal fundpayLast;
	/**两次住院间隔天数*/
	@Excel(name = "两次住院间隔天数", width = 15)
    @ApiModelProperty(value = "两次住院间隔天数")
	private java.lang.Integer zyApartDays;
	/**本次上次主要诊断是否一致*/
	@Excel(name = "本次上次主要诊断是否一致", width = 15)
    @ApiModelProperty(value = "本次上次主要诊断是否一致")
	private java.lang.String primarydiagSameSign;
	/**本次上次医疗机构是否一致*/
	@Excel(name = "本次上次医疗机构是否一致", width = 15)
    @ApiModelProperty(value = "本次上次医疗机构是否一致")
	private java.lang.String orgSameSign;
	/**本次主要疾病名称*/
	@Excel(name = "本次主要疾病名称", width = 15)
    @ApiModelProperty(value = "本次主要疾病名称")
	private java.lang.String priDiseasenameThis;
	/**上次主要疾病名称*/
	@Excel(name = "上次主要疾病名称", width = 15)
    @ApiModelProperty(value = "上次主要疾病名称")
	private java.lang.String priDiseasenameLast;
	/**标签数量*/
	@Excel(name = "标签数量", width = 15)
    @ApiModelProperty(value = "标签数量")
	private java.lang.Integer tagCount;
	/**标签列表*/
	@Excel(name = "标签列表", width = 15)
    @ApiModelProperty(value = "标签列表")
	private java.lang.String tagName;
}
