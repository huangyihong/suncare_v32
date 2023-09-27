package com.ai.modules.ybFj.dto;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @Description: 医疗机构信息
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
@Data
public class YbFjOrgDto {

	/**经营性质*/
	@Excel(name = "经营性质", width = 15)
	@ApiModelProperty(value = "经营性质，字典JGFLGLDM")
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
	/**医疗机构等级*/
	@Excel(name = "医疗机构等级", width = 15)
	@ApiModelProperty(value = "医疗机构等级，字典YYDJ")
	private java.lang.String hospgrade;
	/**医疗机构级别*/
	@Excel(name = "医疗机构级别", width = 15)
	@ApiModelProperty(value = "医疗机构级别，字典YYJB")
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
	@Excel(name = "详细地址", width = 15)
	@ApiModelProperty(value = "详细地址")
	private java.lang.String orgAddress;
	/**医疗机构编码*/
	@Excel(name = "医疗机构编码", width = 15)
	@ApiModelProperty(value = "医疗机构编码")
	private java.lang.String orgId;
	/**医疗机构名称*/
	@Excel(name = "医疗机构名称", width = 15)
	@ApiModelProperty(value = "医疗机构名称")
	private java.lang.String orgName;
	/**医疗机构曾用名*/
	@Excel(name = "医疗机构曾用名", width = 15)
	@ApiModelProperty(value = "医疗机构曾用名")
	private java.lang.String orgUsedName;
	/**医疗机构类别*/
	@Excel(name = "医疗机构类别", width = 15)
	@ApiModelProperty(value = "医疗机构类别，字典JGLB")
	private java.lang.String orgtype;
	/**所有制形式编码*/
	@Excel(name = "所有制形式编码", width = 15)
	@ApiModelProperty(value = "所有制形式编码，字典JJLXBM")
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
	@Excel(name = "负责人", width = 15)
	@ApiModelProperty(value = "负责人")
	private java.lang.String responsible;
	/**负责人联系电话*/
	@Excel(name = "负责人联系电话", width = 15)
	@ApiModelProperty(value = "负责人联系电话")
	private java.lang.String responsiblePhone;
	/**统一社会信用代码*/
	@Excel(name = "统一社会信用代码", width = 15)
	@ApiModelProperty(value = "统一社会信用代码")
	private java.lang.String socialCode;
	/**地址-乡(镇、街道办事处)代码*/
	@Excel(name = "地址-乡(镇、街道办事处)代码", width = 15)
	@ApiModelProperty(value = "地址-乡(镇、街道办事处)代码")
	private java.lang.String townCode;
	/**地址-乡(镇、街道办事处)名称*/
	@Excel(name = "地址-乡(镇、街道办事处)名称", width = 15)
	@ApiModelProperty(value = "地址-乡(镇、街道办事处)名称")
	private java.lang.String townName;
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
