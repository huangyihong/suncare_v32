package com.ai.modules.review.vo;

import org.jeecgframework.poi.excel.annotation.Excel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="DWB_DOCTOR对象", description="医生信息")
public class DwbDoctorVo {
	/**id*/
    @ApiModelProperty(value = "id")
	private java.lang.String id;
	/**医疗机构编码*/
	@Excel(name = "医疗机构编码", width = 15)
    @ApiModelProperty(value = "医疗机构编码")
	private java.lang.String orgid;
	/**医疗机构名称*/
	@Excel(name = "医疗机构名称", width = 15)
    @ApiModelProperty(value = "医疗机构名称")
	private java.lang.String orgname;
	/**医师编码*/
	@Excel(name = "医师编码", width = 15)
    @ApiModelProperty(value = "医师编码")
	private java.lang.String doctorid;
	/**医师姓名*/
	@Excel(name = "医师姓名", width = 15)
    @ApiModelProperty(value = "医师姓名")
	private java.lang.String doctorname;
	/**医护人员性别*/
	@Excel(name = "医护人员性别", width = 15)
    @ApiModelProperty(value = "医护人员性别")
	private java.lang.String sex;
	/**出生日期*/
	@Excel(name = "出生日期", width = 15)
    @ApiModelProperty(value = "出生日期")
	private java.lang.String birthday;
	/**民族代码*/
	@Excel(name = "民族代码", width = 15)
    @ApiModelProperty(value = "民族代码")
	private java.lang.String nationcode;
	/**身份证件类型代码*/
	@Excel(name = "身份证件类型代码", width = 15)
    @ApiModelProperty(value = "身份证件类型代码")
	private java.lang.String identifytype;
	/**身份证件号码*/
	@Excel(name = "身份证件号码", width = 15)
    @ApiModelProperty(value = "身份证件号码")
	private java.lang.String idNo;
	/**所在科室编码*/
	@Excel(name = "所在科室编码", width = 15)
    @ApiModelProperty(value = "所在科室编码")
	private java.lang.String deptid;
	/**所在科室名称*/
	@Excel(name = "所在科室名称", width = 15)
    @ApiModelProperty(value = "所在科室名称")
	private java.lang.String deptname;
	/**专业技术职称*/
	@Excel(name = "专业技术职称", width = 15)
    @ApiModelProperty(value = "专业技术职称")
	private java.lang.String technicaltitle;
	/**行政职务*/
	@Excel(name = "行政职务", width = 15)
    @ApiModelProperty(value = "行政职务")
	private java.lang.String adminpost;
	/**专业特长*/
	@Excel(name = "专业特长", width = 15)
    @ApiModelProperty(value = "专业特长")
	private java.lang.String expertise;
	/**医师资格证编号*/
	@Excel(name = "医师资格证编号", width = 15)
    @ApiModelProperty(value = "医师资格证编号")
	private java.lang.String doccertifyId;
	/**执业证书编号*/
	@Excel(name = "执业证书编号", width = 15)
    @ApiModelProperty(value = "执业证书编号")
	private java.lang.String practicecertifyNo;
	/**执业类别 */
	@Excel(name = "执业类别 ", width = 15)
    @ApiModelProperty(value = "执业类别 ")
	private java.lang.String practiceclass;
	/**医师执业范围代码*/
	@Excel(name = "医师执业范围代码", width = 15)
    @ApiModelProperty(value = "医师执业范围代码")
	private java.lang.String practicescope;
	/**第一执业机构*/
	@Excel(name = "第一执业机构", width = 15)
    @ApiModelProperty(value = "第一执业机构")
	private java.lang.String practiceaddr;
	/**多点执业标志*/
	@Excel(name = "多点执业标志", width = 15)
    @ApiModelProperty(value = "多点执业标志")
	private java.lang.String multiaddrSign;
	/**第2执业机构*/
	@Excel(name = "第2执业机构", width = 15)
    @ApiModelProperty(value = "第2执业机构")
	private java.lang.String practiceorg2;
	/**第2执业机构类别*/
	@Excel(name = "第2执业机构类别", width = 15)
    @ApiModelProperty(value = "第2执业机构类别")
	private java.lang.String practiceorg2Type;
	/**第3执业机构*/
	@Excel(name = "第3执业机构", width = 15)
    @ApiModelProperty(value = "第3执业机构")
	private java.lang.String practiceorg3;
	/**第3执业机构类别*/
	@Excel(name = "第3执业机构类别", width = 15)
    @ApiModelProperty(value = "第3执业机构类别")
	private java.lang.String practiceorg3Type;
	/**最高学历*/
	@Excel(name = "最高学历", width = 15)
    @ApiModelProperty(value = "最高学历")
	private java.lang.String education;
	/**最高学位*/
	@Excel(name = "最高学位", width = 15)
    @ApiModelProperty(value = "最高学位")
	private java.lang.String academicdegree;
	/**最高学历毕业院校*/
	@Excel(name = "最高学历毕业院校", width = 15)
    @ApiModelProperty(value = "最高学历毕业院校")
	private java.lang.String college;
	/**最高学历毕业日期*/
	@Excel(name = "最高学历毕业日期", width = 15)
    @ApiModelProperty(value = "最高学历毕业日期")
	private java.lang.String graduatedate;
	/**最高学历专业代码*/
	@Excel(name = "最高学历专业代码", width = 15)
    @ApiModelProperty(value = "最高学历专业代码")
	private java.lang.String majorCode;
	/**最高学历类别*/
	@Excel(name = "最高学历类别", width = 15)
    @ApiModelProperty(value = "最高学历类别")
	private java.lang.String educationtype;
	/**参加工作日期*/
	@Excel(name = "参加工作日期", width = 15)
    @ApiModelProperty(value = "参加工作日期")
	private java.lang.String workdate;
	/**是否获得国家住院医师规范化培训合格证书*/
	@Excel(name = "是否获得国家住院医师规范化培训合格证书", width = 15)
    @ApiModelProperty(value = "是否获得国家住院医师规范化培训合格证书")
	private java.lang.String residenttrainSign;
	/**住院医师规范化培训合格证书编码*/
	@Excel(name = "住院医师规范化培训合格证书编码", width = 15)
    @ApiModelProperty(value = "住院医师规范化培训合格证书编码")
	private java.lang.String residenttrainCode;
	/**联系电话*/
	@Excel(name = "联系电话", width = 15)
    @ApiModelProperty(value = "联系电话")
	private java.lang.String phone;
	/**联系地址*/
	@Excel(name = "联系地址", width = 15)
    @ApiModelProperty(value = "联系地址")
	private java.lang.String address;
	/**在岗状态*/
	@Excel(name = "在岗状态", width = 15)
    @ApiModelProperty(value = "在岗状态")
	private java.lang.String poststatus;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String creator;
	/**创建日期时间*/
	@Excel(name = "创建日期时间", width = 15)
    @ApiModelProperty(value = "创建日期时间")
	private java.lang.String createdate;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
	private java.lang.String modifier;
	/**修改日期时间*/
	@Excel(name = "修改日期时间", width = 15)
    @ApiModelProperty(value = "修改日期时间")
	private java.lang.String modifydate;
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
	/**etl处理时间*/
	@Excel(name = "etl处理时间", width = 15)
    @ApiModelProperty(value = "etl处理时间")
	private java.lang.String etlTime;
	/**第一学历*/
	@Excel(name = "第一学历", width = 15)
    @ApiModelProperty(value = "第一学历")
	private java.lang.String education1;
	/**第一学位*/
	@Excel(name = "第一学位", width = 15)
    @ApiModelProperty(value = "第一学位")
	private java.lang.String degree1;
	/**第一学历毕业院校*/
	@Excel(name = "第一学历毕业院校", width = 15)
    @ApiModelProperty(value = "第一学历毕业院校")
	private java.lang.String college1;
	/**第一学历毕业日期*/
	@Excel(name = "第一学历毕业日期", width = 15)
    @ApiModelProperty(value = "第一学历毕业日期")
	private java.lang.String graddate1;
	/**第一学历类别*/
	@Excel(name = "第一学历类别", width = 15)
    @ApiModelProperty(value = "第一学历类别")
	private java.lang.String majorCode1;
	/**第一学历专业代码*/
	@Excel(name = "第一学历专业代码", width = 15)
    @ApiModelProperty(value = "第一学历专业代码")
	private java.lang.String educationtype1;
}
