/**
 * MedicalCaseVO.java	  V1.0   2019年11月29日 下午5:45:08
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="病例对象", description="病例")
public class MedicalCaseVO {
	@ApiModelProperty(value = "就诊编号")
	private String jzid;
	@ApiModelProperty(value = "就诊人ID")
	private String patientid;
	@ApiModelProperty(value = "就诊人姓名")
	private String patientname;
	@ApiModelProperty(value = "性别")
	private String sex;
	@ApiModelProperty(value = "年龄")
	private Double age;
	@ApiModelProperty(value = "医疗机构编码")
	private String orgid;
	@ApiModelProperty(value = "医疗机构名称")
	private String orgname;
	@ApiModelProperty(value = "就诊科室")
	private String deptname;
	@ApiModelProperty(value = "就诊科室编码")
	private String deptid;

	@ApiModelProperty(value = "就诊日期")
	private String visitdate;

	@ApiModelProperty(value = "就诊医师编码")
	private String doctorid;

	@ApiModelProperty(value = "就诊医师姓名")
	private String doctorname;

	@ApiModelProperty(value = "病理诊断疾病编码 ")
	private String pathonogyDiseasecode;

	@ApiModelProperty(value = "病理诊断名称")
	private String pathonogyDisease;

	@ApiModelProperty(value = "就诊类型代码")
	private String visittypeId;

	@ApiModelProperty(value = "就诊类型名称")
	private String visittype;

	@ApiModelProperty(value = "参保类别")
	private String insurancetype;

	@ApiModelProperty(value = "出院日期")
	private String leavedate;

	@ApiModelProperty(value = "医疗费用总金额")
	private Double totalfee;

	@ApiModelProperty(value = "状态")
	private String firReviewStatus;


	@Override
	public String toString() {
		return "MedicalCaseVO [jzid=" + jzid + ", patientid=" + patientid + ", patientname=" + patientname + ", sex="
				+ sex + ", age=" + age + ", orgid=" + orgid + ", orgname=" + orgname + ", deptname=" + deptname
				+ ", deptid=" + deptid + ", visitdate=" + visitdate + ", doctorid=" + doctorid + ", doctorname="
				+ doctorname + ", pathonogyDiseasecode=" + pathonogyDiseasecode + ", pathonogyDisease=" + pathonogyDisease
				+ ", visittypeId=" + visittypeId + ", visittype=" + visittype + ", insurancetype=" + insurancetype
				+ ", leavedate=" + leavedate + ", totalfee=" + totalfee + ",firReviewStatus=" + firReviewStatus + "]";
	}
}
