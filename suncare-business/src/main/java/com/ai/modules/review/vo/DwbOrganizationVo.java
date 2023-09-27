package com.ai.modules.review.vo;

import org.jeecgframework.poi.excel.annotation.Excel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="DWB_ORGANIZATION对象", description="医院信息")
public class DwbOrganizationVo {
    
	/**医疗机构联系电话*/
	@Excel(name = "医疗机构联系电话", width = 15)
    @ApiModelProperty(value = "医疗机构联系电话")
	private java.lang.String orgphone;
	/**医疗机构地址*/
	@Excel(name = "医疗机构地址", width = 15)
    @ApiModelProperty(value = "医疗机构地址")
	private java.lang.String orgaddress;
	/**地址-省(自治区、直辖市）代码*/
	@Excel(name = "地址-省(自治区、直辖市）代码", width = 15)
    @ApiModelProperty(value = "地址-省(自治区、直辖市）代码")
	private java.lang.String addrprovinceCode;
	/**地址-省(自治区、直辖市）名称*/
	@Excel(name = "地址-省(自治区、直辖市）名称", width = 15)
    @ApiModelProperty(value = "地址-省(自治区、直辖市）名称")
	private java.lang.String addrprovinceName;
	/**地址-市(地区、州)代码*/
	@Excel(name = "地址-市(地区、州)代码", width = 15)
    @ApiModelProperty(value = "地址-市(地区、州)代码")
	private java.lang.String addrcityCode;
	/**地址-市(地区、州)名称*/
	@Excel(name = "地址-市(地区、州)名称", width = 15)
    @ApiModelProperty(value = "地址-市(地区、州)名称")
	private java.lang.String addrcityName;
	/**地址-县(区)代码*/
	@Excel(name = "地址-县(区)代码", width = 15)
    @ApiModelProperty(value = "地址-县(区)代码")
	private java.lang.String addrcountyCode;
	/**地址-县(区)名称*/
	@Excel(name = "地址-县(区)名称", width = 15)
    @ApiModelProperty(value = "地址-县(区)名称")
	private java.lang.String addrcountyName;
	/**地址-乡(镇、街道办事处)代码*/
	@Excel(name = "地址-乡(镇、街道办事处)代码", width = 15)
    @ApiModelProperty(value = "地址-乡(镇、街道办事处)代码")
	private java.lang.String addrtownCode;
	/**地址-村(街、路、弄等)代码*/
	@Excel(name = "地址-村(街、路、弄等)代码", width = 15)
    @ApiModelProperty(value = "地址-村(街、路、弄等)代码")
	private java.lang.String addrvillageCode;
	/**新农合定点医疗机构标志*/
	@Excel(name = "新农合定点医疗机构标志", width = 15)
    @ApiModelProperty(value = "新农合定点医疗机构标志")
	private java.lang.String nhRegisterSign;
	/**医保定点医疗机构标志*/
	@Excel(name = "医保定点医疗机构标志", width = 15)
    @ApiModelProperty(value = "医保定点医疗机构标志")
	private java.lang.String registerSign;
	/**医疗机构批准床位数*/
	@Excel(name = "医疗机构批准床位数", width = 15)
    @ApiModelProperty(value = "医疗机构批准床位数")
	private java.lang.String approvedbeds;
	/**医疗机构实际开放床位数*/
	@Excel(name = "医疗机构实际开放床位数", width = 15)
    @ApiModelProperty(value = "医疗机构实际开放床位数")
	private java.lang.String openbeds;
	/**隶属关系*/
	@Excel(name = "隶属关系", width = 15)
    @ApiModelProperty(value = "隶属关系")
	private java.lang.String startupcorp;
	/**设置/主办单位类别*/
	@Excel(name = "设置/主办单位类别", width = 15)
    @ApiModelProperty(value = "设置/主办单位类别")
	private java.lang.String startupcorpCatalog;
	/**上级机构ID*/
	@Excel(name = "上级机构ID", width = 15)
    @ApiModelProperty(value = "上级机构ID")
	private java.lang.String superiorId;
	/**主管单位名称*/
	@Excel(name = "主管单位名称", width = 15)
    @ApiModelProperty(value = "主管单位名称")
	private java.lang.String superior;
	/**所属医保机构编码*/
	@Excel(name = "所属医保机构编码", width = 15)
    @ApiModelProperty(value = "所属医保机构编码")
	private java.lang.String insuranceOrg;
	/**所属医保机构名称*/
	@Excel(name = "所属医保机构名称", width = 15)
    @ApiModelProperty(value = "所属医保机构名称")
	private java.lang.String insuranceOrgname;
	/**法人姓名*/
	@Excel(name = "法人姓名", width = 15)
    @ApiModelProperty(value = "法人姓名")
	private java.lang.String legalperson;
	/**法人证件类型*/
	@Excel(name = "法人证件类型", width = 15)
    @ApiModelProperty(value = "法人证件类型")
	private java.lang.String legalpersonIdtype;
	/**法人身份证件号码*/
	@Excel(name = "法人身份证件号码", width = 15)
    @ApiModelProperty(value = "法人身份证件号码")
	private java.lang.String legalpersonId;
	/**法人联系地址*/
	@Excel(name = "法人联系地址", width = 15)
    @ApiModelProperty(value = "法人联系地址")
	private java.lang.String legalpersonAddr;
	/**法人联系电话*/
	@Excel(name = "法人联系电话", width = 15)
    @ApiModelProperty(value = "法人联系电话")
	private java.lang.String legalpersonPhone;
	/**数据来源机构编码*/
	@Excel(name = "数据来源机构编码", width = 15)
    @ApiModelProperty(value = "数据来源机构编码")
	private java.lang.String dataResouceId;
	/**数据来源机构名称*/
	@Excel(name = "数据来源机构名称", width = 15)
    @ApiModelProperty(value = "数据来源机构名称")
	private java.lang.String dataResouce;
	/**有效标志*/
	@Excel(name = "有效标志", width = 15)
    @ApiModelProperty(value = "有效标志")
	private java.lang.String validSign;
	/**etl数据来源*/
	@Excel(name = "etl数据来源", width = 15)
    @ApiModelProperty(value = "etl数据来源")
	private java.lang.String etlSource;
	/**etl处理时间*/
	@Excel(name = "etl处理时间", width = 15)
    @ApiModelProperty(value = "etl处理时间")
	private java.lang.String etlTime;
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
	/**组织机构代码*/
	@Excel(name = "组织机构代码", width = 15)
    @ApiModelProperty(value = "组织机构代码")
	private java.lang.String orgcode;
	/**医疗机构级别*/
	@Excel(name = "医疗机构级别", width = 15)
    @ApiModelProperty(value = "医疗机构级别")
	private java.lang.String hosplevel;
	/**医疗机构级别_名称*/
	@Excel(name = "医疗机构级别_名称", width = 15)
    @ApiModelProperty(value = "医疗机构级别_名称")
	private java.lang.String hosplevelName;
	/**医疗机构等级*/
	@Excel(name = "医疗机构等级", width = 15)
    @ApiModelProperty(value = "医疗机构等级")
	private java.lang.String hospgrade;
	/**医疗机构等级_名称*/
	@Excel(name = "医疗机构等级_名称", width = 15)
    @ApiModelProperty(value = "医疗机构等级_名称")
	private java.lang.String hospgradeName;
	/**卫生机构类别*/
	@Excel(name = "卫生机构类别", width = 15)
    @ApiModelProperty(value = "卫生机构类别")
	private java.lang.String medicalOrgType;
	/**卫生机构类别_名称(医院、药店、中医院、综合医院、卫生室等) */
	@Excel(name = "卫生机构类别_名称(医院、药店、中医院、综合医院、卫生室等) ", width = 15)
    @ApiModelProperty(value = "卫生机构类别_名称(医院、药店、中医院、综合医院、卫生室等) ")
	private java.lang.String medicalOrgTypeName;
	/**医疗机构经营性质*/
	@Excel(name = "医疗机构经营性质", width = 15)
    @ApiModelProperty(value = "医疗机构经营性质")
	private java.lang.String busstype;
	/**医疗机构经营性质_名称*/
	@Excel(name = "医疗机构经营性质_名称", width = 15)
    @ApiModelProperty(value = "医疗机构经营性质_名称")
	private java.lang.String busstypeName;
	/**所有制形式*/
	@Excel(name = "所有制形式", width = 15)
    @ApiModelProperty(value = "所有制形式")
	private java.lang.String owntype;
	/**所有制形式名称*/
	@Excel(name = "所有制形式名称", width = 15)
    @ApiModelProperty(value = "所有制形式名称")
	private java.lang.String owntypeName;
	/**医疗机构行政级别*/
	@Excel(name = "医疗机构行政级别", width = 15)
    @ApiModelProperty(value = "医疗机构行政级别")
	private java.lang.String adminlevel;
	/**医疗机构邮政编码*/
	@Excel(name = "医疗机构邮政编码", width = 15)
    @ApiModelProperty(value = "医疗机构邮政编码")
	private java.lang.String orgpostcode;
}
