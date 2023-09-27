package com.ai.modules.ybFj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: 医疗机构信息
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_org")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_org对象", description="医疗机构信息")
public class YbFjOrg {

	/**审核意见*/
	@Excel(name = "审核意见", width = 15)
    @ApiModelProperty(value = "审核意见")
	private java.lang.String auditOpinion;
	/**审核结果(init:待审核 finish:审核通过 fail:审核不通过)*/
	@Excel(name = "审核结果(init:待审核 finish:审核通过 fail:审核不通过)", width = 15)
    @ApiModelProperty(value = "审核结果(init:待审核 finish:审核通过 fail:审核不通过)")
	@MedicalDict(dicCode = "FJXMSHZT")
	private java.lang.String auditState;
	/**审核时间*/
	@Excel(name = "审核时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
	private java.util.Date auditTime;
	/**审核人*/
	@Excel(name = "审核人", width = 15)
    @ApiModelProperty(value = "审核人")
	private java.lang.String auditUser;
	/**审核人姓名*/
	@Excel(name = "审核人姓名", width = 15)
    @ApiModelProperty(value = "审核人姓名")
	private java.lang.String auditUserName;
	/**经营性质*/
	@Excel(name = "*经营性质", width = 15)
    @ApiModelProperty(value = "经营性质，字典JGFLGLDM")
	@MedicalDict(dicCode = "JGFLGLDM")
	private java.lang.String busstype;
	/**地址-市(地区、州)代码*/
	@Excel(name = "地址-市(地区、州)代码", width = 15)
    @ApiModelProperty(value = "地址-市(地区、州)代码")
	private java.lang.String cityCode;
	/**地址-市(地区、州)名称*/
	@Excel(name = "地址-市(地区、州)名称", width = 15)
    @ApiModelProperty(value = "地址-市(地区、州)名称")
	private java.lang.String cityName;
	/**地址-县(区)代码*/
	@Excel(name = "地址-县(区)代码", width = 15)
    @ApiModelProperty(value = "地址-县(区)代码")
	private java.lang.String countyCode;
	/**地址-县(区)名称*/
	@Excel(name = "地址-县(区)名称", width = 15)
    @ApiModelProperty(value = "地址-县(区)名称")
	private java.lang.String countyName;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;
	/**医疗机构等级*/
	@Excel(name = "医疗机构等级", width = 15)
    @ApiModelProperty(value = "医疗机构等级")
	@MedicalDict(dicCode = "YYDJ")
	private java.lang.String hospgrade;
	/**医疗机构级别*/
	@Excel(name = "医疗机构级别", width = 15)
    @ApiModelProperty(value = "医疗机构级别，字典YYDJ")
	@MedicalDict(dicCode = "YYJB")
	private java.lang.String hosplevel;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**法定代表人*/
	@Excel(name = "法定代表人", width = 15)
    @ApiModelProperty(value = "法定代表人")
	private java.lang.String legalperson;
	/**法定联系电话*/
	@Excel(name = "法定联系电话", width = 15)
    @ApiModelProperty(value = "法定联系电话")
	private java.lang.String legalpersonPhone;
	/**详细地址*/
	@Excel(name = "*详细地址", width = 15)
    @ApiModelProperty(value = "详细地址")
	private java.lang.String orgAddress;
	/**医疗机构编码*/
	@Excel(name = "*医疗机构编码", width = 15)
    @ApiModelProperty(value = "医疗机构编码")
	private java.lang.String orgId;
	/**医疗机构名称*/
	@Excel(name = "*医疗机构名称", width = 15)
    @ApiModelProperty(value = "医疗机构名称")
	private java.lang.String orgName;
	/**医疗机构曾用名*/
	@Excel(name = "医疗机构曾用名", width = 15)
    @ApiModelProperty(value = "医疗机构曾用名")
	private java.lang.String orgUsedName;
	/**医疗机构类别*/
	@Excel(name = "*医疗机构类别", width = 15)
    @ApiModelProperty(value = "医疗机构类别，字典JGLB")
	@MedicalDict(dicCode = "JGLB")
	private java.lang.String orgtype;
	/**所有制形式编码*/
	@Excel(name = "*所有制形式编码", width = 15)
    @ApiModelProperty(value = "所有制形式编码，字典JJLXBM")
	@MedicalDict(dicCode = "JJLXBM")
	private java.lang.String ownershipCode;
	/**地址-省(自治区、直辖市)代码*/
	@Excel(name = "地址-省(自治区、直辖市)代码", width = 15)
    @ApiModelProperty(value = "地址-省(自治区、直辖市)代码")
	private java.lang.String provinceCode;
	/**地址-省(自治区、直辖市)名称*/
	@Excel(name = "地址-省(自治区、直辖市)名称", width = 15)
    @ApiModelProperty(value = "地址-省(自治区、直辖市)名称")
	private java.lang.String provinceName;
	/**负责人*/
	@Excel(name = "*负责人", width = 15)
    @ApiModelProperty(value = "负责人")
	private java.lang.String responsible;
	/**负责人联系电话*/
	@Excel(name = "*负责人联系电话", width = 15)
    @ApiModelProperty(value = "负责人联系电话")
	private java.lang.String responsiblePhone;
	/**统一社会信用代码*/
	@Excel(name = "*统一社会信用代码", width = 15)
    @ApiModelProperty(value = "统一社会信用代码")
	private java.lang.String socialCode;
	/**数据状态(0待生效 1有效 2无效)*/
	@Excel(name = "数据状态(0待生效 1有效 2无效)", width = 15)
    @ApiModelProperty(value = "数据状态(0待生效 1有效 2无效)")
	private java.lang.String state;
	/**地址-乡(镇、街道办事处)代码*/
	@Excel(name = "地址-乡(镇、街道办事处)代码", width = 15)
    @ApiModelProperty(value = "地址-乡(镇、街道办事处)代码")
	private java.lang.String townCode;
	/**地址-乡(镇、街道办事处)名称*/
	@Excel(name = "地址-乡(镇、街道办事处)名称", width = 15)
    @ApiModelProperty(value = "地址-乡(镇、街道办事处)名称")
	private java.lang.String townName;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
	private java.util.Date updateTime;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
    @ApiModelProperty(value = "更新人")
	private java.lang.String updateUser;
	/**更新人姓名*/
	@Excel(name = "更新人姓名", width = 15)
    @ApiModelProperty(value = "更新人姓名")
	private java.lang.String updateUsername;
	/**地址-村(街、路、弄等)代码*/
	@Excel(name = "地址-村(街、路、弄等)代码", width = 15)
    @ApiModelProperty(value = "地址-村(街、路、弄等)代码")
	private java.lang.String villageCode;
	/**地址-村(街、路、弄等)名称*/
	@Excel(name = "地址-村(街、路、弄等)名称", width = 15)
    @ApiModelProperty(value = "地址-村(街、路、弄等)名称")
	private java.lang.String villageName;

	/**床位数*/
	@Excel(name = "床位数", width = 15)
	@ApiModelProperty(value = "床位数")
	private java.lang.Integer bedAmount;

}
