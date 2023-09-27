package com.ai.modules.ybFj.entity;

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
 * @Description: 飞检项目现场核查线索明细
 * @Author: jeecg-boot
 * @Date:   2023-06-07
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_project_clue_onsite_dtl")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_project_clue_onsite_dtl对象", description="飞检项目现场核查线索明细")
public class YbFjProjectClueOnsiteDtl {
    
	/**唯一ID*/
	@Excel(name = "唯一ID", width = 15)
    @ApiModelProperty(value = "唯一ID")
	private String clueDtlId;
	/**关联的飞检项目主键*/
	@Excel(name = "关联的飞检项目主键", width = 15)
    @ApiModelProperty(value = "关联的飞检项目主键")
	private String projectId;
	/**关联yb_fj_project_org.project_org_id*/
	@Excel(name = "关联yb_fj_project_org.project_org_id", width = 15)
    @ApiModelProperty(value = "关联yb_fj_project_org.project_org_id")
	private String projectOrgId;
	/**yb_fj_project_clue.clue_id*/
	@Excel(name = "yb_fj_project_clue.clue_id", width = 15)
    @ApiModelProperty(value = "yb_fj_project_clue.clue_id")
	private String clueId;
	/**医疗机构名称*/
	@Excel(name = "医疗机构名称", width = 15)
    @ApiModelProperty(value = "医疗机构名称")
	private String orgname;
	/**就诊id*/
	@Excel(name = "就诊id", width = 15)
    @ApiModelProperty(value = "就诊id")
	private String visitid;
	/**医生姓名*/
	@Excel(name = "医生姓名", width = 15)
    @ApiModelProperty(value = "医生姓名")
	private String doctorname;
	/**科室名称*/
	@Excel(name = "科室名称", width = 15)
    @ApiModelProperty(value = "科室名称")
	private String deptname;
	/**就诊类型*/
	@Excel(name = "就诊类型", width = 15)
    @ApiModelProperty(value = "就诊类型")
	private String visittype;
	/**病人姓名*/
	@Excel(name = "病人姓名", width = 15)
    @ApiModelProperty(value = "病人姓名")
	private String clientname;
	/**性别*/
	@Excel(name = "性别", width = 15)
    @ApiModelProperty(value = "性别")
	private String sex;
	/**就诊日期*/
	@Excel(name = "就诊日期", width = 15)
    @ApiModelProperty(value = "就诊日期")
	private String visitdate;
	/**离院日期*/
	@Excel(name = "离院日期", width = 15)
    @ApiModelProperty(value = "离院日期")
	private String leavedate;
	/**年龄*/
	@Excel(name = "年龄", width = 15)
    @ApiModelProperty(value = "年龄")
	private java.math.BigDecimal yearage;
	/**疾病名称*/
	@Excel(name = "疾病名称", width = 15)
    @ApiModelProperty(value = "疾病名称")
	private String dis;
	/**his项目名称*/
	@Excel(name = "his项目名称", width = 15)
    @ApiModelProperty(value = "his项目名称")
	private String hisItemname;
	/**医保项目名称*/
	@Excel(name = "医保项目名称", width = 15)
    @ApiModelProperty(value = "医保项目名称")
	private String itemname;
	/**项目类别*/
	@Excel(name = "项目类别", width = 15)
    @ApiModelProperty(value = "项目类别")
	private String chargeattri;
	/**数量*/
	@Excel(name = "数量", width = 15)
    @ApiModelProperty(value = "数量")
	private Integer sl;
	/**费用*/
	@Excel(name = "费用", width = 15)
    @ApiModelProperty(value = "费用")
	private java.math.BigDecimal fy;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private Date createTime;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private String createUsername;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private String createUser;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
	private Date updateTime;
	/**更新人姓名*/
	@Excel(name = "更新人姓名", width = 15)
    @ApiModelProperty(value = "更新人姓名")
	private String updateUsername;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
    @ApiModelProperty(value = "更新人")
	private String updateUser;
	/**项目类型*/
	@Excel(name = "项目类型", width = 15)
    @ApiModelProperty(value = "项目类型")
	private String itemclass;
	/**年份*/
	@Excel(name = "年份", width = 15)
    @ApiModelProperty(value = "年份")
	private String year;
	/**医保编号*/
	@Excel(name = "医保编号", width = 15)
    @ApiModelProperty(value = "医保编号")
	private String insureNo;
	/**身份证*/
	@Excel(name = "身份证", width = 15)
    @ApiModelProperty(value = "身份证")
	private String idcard;
	/**单位地址*/
	@Excel(name = "单位地址", width = 15)
    @ApiModelProperty(value = "单位地址")
	private String companyAddr;
	/**医疗机构编码*/
	@Excel(name = "医疗机构编码", width = 15)
    @ApiModelProperty(value = "医疗机构编码")
	private String orgcode;
	/**医院地区*/
	@Excel(name = "医院地区", width = 15)
    @ApiModelProperty(value = "医院地区")
	private String orgRegion;
	/**结算时间*/
	@Excel(name = "结算时间", width = 15)
    @ApiModelProperty(value = "结算时间")
	private String settlementTime;
	/**住院天数*/
	@Excel(name = "住院天数", width = 15)
    @ApiModelProperty(value = "住院天数")
	private Integer zyDays;
	/**患者类型*/
	@Excel(name = "患者类型", width = 15)
    @ApiModelProperty(value = "患者类型")
	private String clientType;
	/**异地类型*/
	@Excel(name = "异地类型", width = 15)
    @ApiModelProperty(value = "异地类型")
	private String offsiteType;
	/**参保地区*/
	@Excel(name = "参保地区", width = 15)
    @ApiModelProperty(value = "参保地区")
	private String insuredRegion;
	/**医疗总费用*/
	@Excel(name = "医疗总费用", width = 15)
    @ApiModelProperty(value = "医疗总费用")
	private java.math.BigDecimal totalfee;
	/**统筹基金支出费用*/
	@Excel(name = "统筹基金支出费用", width = 15)
    @ApiModelProperty(value = "统筹基金支出费用")
	private java.math.BigDecimal fundpay;
	/**大病支付总额*/
	@Excel(name = "大病支付总额", width = 15)
    @ApiModelProperty(value = "大病支付总额")
	private java.math.BigDecimal dbpay;
	/**个人账户支出费用*/
	@Excel(name = "个人账户支出费用", width = 15)
    @ApiModelProperty(value = "个人账户支出费用")
	private java.math.BigDecimal acctpay;
	/**费用科室*/
	@Excel(name = "费用科室", width = 15)
    @ApiModelProperty(value = "费用科室")
	private String feeDeptname;
	/**收费医生姓名*/
	@Excel(name = "收费医生姓名", width = 15)
    @ApiModelProperty(value = "收费医生姓名")
	private String feeDoctorname;
	/**his项目编码*/
	@Excel(name = "his项目编码", width = 15)
    @ApiModelProperty(value = "his项目编码")
	private String hisItemcode;
	/**医保项目编码*/
	@Excel(name = "医保项目编码", width = 15)
    @ApiModelProperty(value = "医保项目编码")
	private String itemcode;
	/**费用发生时间*/
	@Excel(name = "费用发生时间", width = 15)
    @ApiModelProperty(value = "费用发生时间")
	private String chargedate;
	/**收费类别*/
	@Excel(name = "收费类别", width = 15)
    @ApiModelProperty(value = "收费类别")
	private String chargeclass;
	/**项目自付比例*/
	@Excel(name = "项目自付比例", width = 15)
    @ApiModelProperty(value = "项目自付比例")
	private java.math.BigDecimal selfpayProp;
	/**单价*/
	@Excel(name = "单价", width = 15)
    @ApiModelProperty(value = "单价")
	private java.math.BigDecimal itemprice;
	/**违规金额*/
	@Excel(name = "违规金额", width = 15)
    @ApiModelProperty(value = "违规金额")
	private java.math.BigDecimal wgFee;
}
