package com.ai.modules.review.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value="DWB_DISCHARGE对象", description="出院记录")
public class DwbDischargeVo {

	@ApiModelProperty(value="就诊id")
	private String visitid;
	@ApiModelProperty(value="his就诊id")
	private String hisZyId;
	@ApiModelProperty(value="病案号")
	private String caseId;
	@ApiModelProperty(value="患者姓名")
	private String patientName;
	@ApiModelProperty(value="患者性别名称")
	private String sex;
	@ApiModelProperty(value="年龄（岁）")
	private String yearage;
	@ApiModelProperty(value="年龄（月）")
	private String monthage;
	@ApiModelProperty(value="年龄（天）")
	private String dayage;
	@ApiModelProperty(value="医疗机构编码")
	private String orgid;
	@ApiModelProperty(value="医疗机构名称")
	private String orgname;
	@ApiModelProperty(value="院区名称")
	private String aera;
	@ApiModelProperty(value="科室名称")
	private String deptname;
	@ApiModelProperty(value="病房号")
	private String roomNo;
	@ApiModelProperty(value="病床号")
	private String bedno;
	@ApiModelProperty(value="入院日期时间")
	private String admitdate;
	@ApiModelProperty(value="入院情况")
	private String admDiseasestatus;
	@ApiModelProperty(value="入院诊断名称")
	private String admitdisease;
	@ApiModelProperty(value="入院诊断疾病编码")
	private String admitdiseaseCode;
	@ApiModelProperty(value="中医“四诊”观察结果")
	private String chimedicalDiag;
	@ApiModelProperty(value="治疗过程描述")
	private String treatprocess;
	@ApiModelProperty(value="出院情况")
	private String leaveDescribe;
	@ApiModelProperty(value="出院日期时间")
	private String leavedate;
	@ApiModelProperty(value="手术日期")
	private String surgeryDate;
	@ApiModelProperty(value="手术名称")
	private String surgeryName;
	@ApiModelProperty(value="住院天数")
	private Double days;
	@ApiModelProperty(value="出院诊断-西医诊断名称")
	private String leavedisease;
	@ApiModelProperty(value="出院诊断-西医诊断编码")
	private String leavediseasecode;
	@ApiModelProperty(value="出院诊断-中医病名名称")
	private String leaveChimediDisease;
	@ApiModelProperty(value="出院诊断-中医病名编码")
	private String leaveChimediDiscode;
	@ApiModelProperty(value="出院诊断-中医证候名称")
	private String leaveChimediDisclass;
	@ApiModelProperty(value="出院诊断-中医证候编码")
	private String leaveChimediDisclasscode;
	@ApiModelProperty(value="出院时症状与体征")
	private String leavestatusDescribe;
	@ApiModelProperty(value="出院医嘱")
	private String leaveorder;
	@ApiModelProperty(value="X光片号")
	private String xDrNo;
	@ApiModelProperty(value="CT号")
	private String ctNo;
	@ApiModelProperty(value="MRI号")
	private String mriNo;
	@ApiModelProperty(value="病理号")
	private String pathonogyTestId;
	@ApiModelProperty(value="住院医师")
	private String residentDoctor;
	@ApiModelProperty(value="主治医师")
	private String doctorname;
	@ApiModelProperty(value="主任医师")
	private String directordoctor;
	@ApiModelProperty(value="签名日期时间")
	private String signdate;
	@ApiModelProperty(value="状态")
	private String status;
	@ApiModelProperty(value="数据来源机构编码")
	private String dataResouceId;
	@ApiModelProperty(value="数据来源机构名称")
	private String dataResouce;
	@ApiModelProperty(value="etl数据来源")
	private String etlSource;
	@ApiModelProperty(value="etl时间")
	private String etlTime;
	@ApiModelProperty(value="项目")
	private String project;
	@ApiModelProperty(value="项目data")
	private String projectData;

}
