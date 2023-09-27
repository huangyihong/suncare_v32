package com.ai.modules.review.vo;

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
 * @Description: 疾病诊断
 * @Author: jeecg-boot
 * @Date:   2020-04-09
 * @Version: V1.0
 */
@Data
@TableName("Dwb_Diag")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="Dwb_Diag对象", description="疾病诊断")
public class DwbDiagVo {

	/**dwb就诊唯一id*/
	@Excel(name = "dwb就诊唯一id", width = 15)
    @ApiModelProperty(value = "dwb就诊唯一id")
	private java.lang.String visitid;
	/**医保/农合就诊id*/
	@Excel(name = "医保/农合就诊id", width = 15)
    @ApiModelProperty(value = "医保/农合就诊id")
	private java.lang.String ybVisitid;
	/**his就诊id*/
	@Excel(name = "his就诊id", width = 15)
    @ApiModelProperty(value = "his就诊id")
	private java.lang.String hisVisitid;
	/**就诊日期时间*/
	@Excel(name = "就诊日期时间", width = 15)
    @ApiModelProperty(value = "就诊日期时间")
	private java.lang.String visitdate;
	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
	private java.lang.String visittypeId;
	/**就诊类型名称*/
	@Excel(name = "就诊类型名称", width = 15)
    @ApiModelProperty(value = "就诊类型名称")
	private java.lang.String visittype;
	/**就诊医疗机构编码*/
	@Excel(name = "就诊医疗机构编码", width = 15)
    @ApiModelProperty(value = "就诊医疗机构编码")
	private java.lang.String orgid;
	/**就诊医疗机构名称*/
	@Excel(name = "就诊医疗机构名称", width = 15)
    @ApiModelProperty(value = "就诊医疗机构名称")
	private java.lang.String orgname;
	/**病案号*/
	@Excel(name = "病案号", width = 15)
    @ApiModelProperty(value = "病案号")
	private java.lang.String caseId;
	/**患者id*/
	@Excel(name = "患者id", width = 15)
    @ApiModelProperty(value = "患者id")
	private java.lang.String clientid;
	/**患者姓名*/
	@Excel(name = "患者姓名", width = 15)
    @ApiModelProperty(value = "患者姓名")
	private java.lang.String clientname;
	/**身份证号*/
	@Excel(name = "身份证号", width = 15)
    @ApiModelProperty(value = "身份证号")
	private java.lang.String idNo;
	/**就诊科室id*/
	@Excel(name = "就诊科室id", width = 15)
    @ApiModelProperty(value = "就诊科室id")
	private java.lang.String deptid;
	/**就诊科室名称*/
	@Excel(name = "就诊科室名称", width = 15)
    @ApiModelProperty(value = "就诊科室名称")
	private java.lang.String deptname;
	/**医生id*/
	@Excel(name = "医生id", width = 15)
    @ApiModelProperty(value = "医生id")
	private java.lang.String doctorid;
	/**医生姓名*/
	@Excel(name = "医生姓名", width = 15)
    @ApiModelProperty(value = "医生姓名")
	private java.lang.String doctorname;
	/**诊断日期*/
	@Excel(name = "诊断日期", width = 15)
    @ApiModelProperty(value = "诊断日期")
	private java.lang.String diagdate;
	/**入院日期*/
	@Excel(name = "入院日期", width = 15)
    @ApiModelProperty(value = "入院日期")
	private java.lang.String admitdate;
	/**出院日期*/
	@Excel(name = "出院日期", width = 15)
    @ApiModelProperty(value = "出院日期")
	private java.lang.String leavedate;
	/**住院天数*/
	@Excel(name = "住院天数", width = 15)
    @ApiModelProperty(value = "住院天数")
	private java.lang.Double zyDays;
	/**诊断疾病编码*/
	@Excel(name = "诊断疾病编码", width = 15)
    @ApiModelProperty(value = "诊断疾病编码")
	private java.lang.String diseasecode;
	/**诊断疾病名称*/
	@Excel(name = "诊断疾病名称", width = 15)
    @ApiModelProperty(value = "诊断疾病名称")
	private java.lang.String diseasename;
	/**诊断疾病编码_src*/
	@Excel(name = "诊断疾病编码_src", width = 15)
	@ApiModelProperty(value = "诊断疾病编码_src")
	private java.lang.String diseasecodeSrc;
	/**诊断疾病名称_src*/
	@Excel(name = "诊断疾病名称_src", width = 15)
	@ApiModelProperty(value = "诊断疾病名称_src")
	private java.lang.String diseasenameSrc;
	/**医疗费用总金额*/
	@Excel(name = "医疗费用总金额", width = 15)
    @ApiModelProperty(value = "医疗费用总金额")
	private java.math.BigDecimal totalfee;
	/**入院病情代码*/
	@Excel(name = "入院病情代码", width = 15)
    @ApiModelProperty(value = "入院病情代码")
	private java.lang.String admDiseasestatus;
	/**主要诊断标识*/
	@Excel(name = "主要诊断标识", width = 15)
    @ApiModelProperty(value = "主要诊断标识")
	private java.lang.String primarydiagSign;
	/**疾病诊断类型代码，1：门诊，2：入院，3：出院*/
	@Excel(name = "疾病诊断类型代码，1：门诊，2：入院，3：出院", width = 15)
    @ApiModelProperty(value = "疾病诊断类型代码，1：门诊，2：入院，3：出院")
	private java.lang.String diagtypeCode;
	/**中西医诊断标识*/
	@Excel(name = "中西医诊断标识", width = 15)
    @ApiModelProperty(value = "中西医诊断标识")
	private java.lang.String chimedicalSign;
	/**诊断依据代码*/
	@Excel(name = "诊断依据代码", width = 15)
    @ApiModelProperty(value = "诊断依据代码")
	private java.lang.String basisCode;
	/**疾病状态编码*/
	@Excel(name = "疾病状态编码", width = 15)
    @ApiModelProperty(value = "疾病状态编码")
	private java.lang.String diseasestatusCode;
	/**治疗结果*/
	@Excel(name = "治疗结果", width = 15)
    @ApiModelProperty(value = "治疗结果")
	private java.lang.String diagResult;
	/**数据来源机构编码*/
	@Excel(name = "数据来源机构编码", width = 15)
    @ApiModelProperty(value = "数据来源机构编码")
	private java.lang.String dataResouceId;
	/**数据来源机构名称*/
	@Excel(name = "数据来源机构名称", width = 15)
    @ApiModelProperty(value = "数据来源机构名称")
	private java.lang.String dataResouce;
	/**etl数据来源*/
	@Excel(name = "etl数据来源", width = 15)
    @ApiModelProperty(value = "etl数据来源")
	private java.lang.String etlSource;
	/**etl来源名称*/
	@Excel(name = "etl来源名称", width = 15)
    @ApiModelProperty(value = "etl来源名称")
	private java.lang.String etlSourceName;
	/**项目地名称*/
	@Excel(name = "项目地名称", width = 15)
    @ApiModelProperty(value = "项目地名称")
	private java.lang.String projectarea;
	/**项目地编码，省级项目就是省级编码，区县级项目就是区县编码*/
	@Excel(name = "项目地编码，省级项目就是省级编码，区县级项目就是区县编码", width = 15)
    @ApiModelProperty(value = "项目地编码，省级项目就是省级编码，区县级项目就是区县编码")
	private java.lang.String projectareaid;
	/**etl处理时间*/
	@Excel(name = "etl处理时间", width = 15)
    @ApiModelProperty(value = "etl处理时间")
	private java.lang.String etlTime;
	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "id")
	private java.lang.String id;
}
