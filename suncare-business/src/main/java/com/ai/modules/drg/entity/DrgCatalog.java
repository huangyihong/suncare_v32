package com.ai.modules.drg.entity;

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
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: DRG分组目录版本表
 * @Author: jeecg-boot
 * @Date:   2023-02-20
 * @Version: V1.0
 */
@Data
@TableName("drg_catalog")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="drg_catalog对象", description="DRG分组目录版本表")
public class DrgCatalog {

	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**版本编号*/
	@Excel(name = "版本编号", width = 15)
    @ApiModelProperty(value = "版本编号")
	private java.lang.String versionCode;
	/**版本名称*/
	@Excel(name = "版本名称", width = 15)
    @ApiModelProperty(value = "版本名称")
	private java.lang.String versionName;
	/**MDC目录版本*/
	@Excel(name = "MDC目录版本", width = 15)
    @ApiModelProperty(value = "MDC目录版本")
	//@Dict(dicCode = "version_code",dicText="version_name",dictTable="(select version_code,version_name from drg_catalog where catalog_type='MDC_V') t ")
	private java.lang.String mdcCatalogV;
	/**ADRG目录版本*/
	@Excel(name = "ADRG目录版本", width = 15)
    @ApiModelProperty(value = "ADRG目录版本")
	//@Dict(dicCode = "version_code",dicText="version_name",dictTable="(select version_code,version_name from drg_catalog where catalog_type='ADRG_V') t ")
	private java.lang.String adrgCatalogV;
	/**MDC主诊表版本*/
	@Excel(name = "MDC主诊表版本", width = 15)
    @ApiModelProperty(value = "MDC主诊表版本")
	//@Dict(dicCode = "version_code",dicText="version_name",dictTable="(select version_code,version_name from drg_catalog where catalog_type='MDC_INFO_V') t ")
	private java.lang.String mdcInfoV;
	/**ADRG列表版本*/
	@Excel(name = "ADRG列表版本", width = 15)
    @ApiModelProperty(value = "ADRG列表版本")
	//@Dict(dicCode = "version_code",dicText="version_name",dictTable="(select version_code,version_name from drg_catalog where catalog_type='ADRG_LIST_V') t ")
	private java.lang.String adrgListV;
	/**MCC信息版本*/
	@Excel(name = "MCC信息版本", width = 15)
    @ApiModelProperty(value = "MCC信息版本")
	//@Dict(dicCode = "version_code",dicText="version_name",dictTable="(select version_code,version_name from drg_catalog where catalog_type='MCC_INFO_V') t ")
	private java.lang.String mccInfoV;
	/**CC信息版本*/
	@Excel(name = "CC信息版本", width = 15)
    @ApiModelProperty(value = "CC信息版本")
	//@Dict(dicCode = "version_code",dicText="version_name",dictTable="(select version_code,version_name from drg_catalog where catalog_type='CC_INFO_V') t ")
	private java.lang.String ccInfoV;
	/**排除表版本*/
	@Excel(name = "排除表版本", width = 15)
    @ApiModelProperty(value = "排除表版本")
	//@Dict(dicCode = "version_code",dicText="version_name",dictTable="(select version_code,version_name from drg_catalog where catalog_type='EXCLUDE_INFO_V') t ")
	private java.lang.String excludeInfoV;
	/**手术室手术版本*/
	@Excel(name = "手术室手术版本", width = 15)
    @ApiModelProperty(value = "手术室手术版本")
	//@Dict(dicCode = "version_code",dicText="version_name",dictTable="(select version_code,version_name from drg_catalog where catalog_type='SURGERY_INFO_V') t ")
	private java.lang.String surgeryInfoV;
	/**ICD10版本*/
	@Excel(name = "ICD10版本", width = 15)
    @ApiModelProperty(value = "ICD10版本")
	@MedicalDict(dicCode = "DRG_IDC_V")
	private java.lang.String icd10V;
	/**ICD9版本*/
	@Excel(name = "ICD9版本", width = 15)
    @ApiModelProperty(value = "ICD9版本")
	@MedicalDict(dicCode = "DRG_IDC_V")
	private java.lang.String icd9V;
	/**目录类型*/
	@Excel(name = "目录类型", width = 15)
    @ApiModelProperty(value = "目录类型")
	private java.lang.String catalogType;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**状态(1启用0禁用)*/
	@Excel(name = "状态(1启用0禁用)", width = 15)
    @ApiModelProperty(value = "状态(1启用0禁用)")
	private java.lang.String status;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
	private java.util.Date updateTime;
	/**更新人姓名*/
	@Excel(name = "更新人姓名", width = 15)
    @ApiModelProperty(value = "更新人姓名")
	private java.lang.String updateUsername;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
    @ApiModelProperty(value = "更新人")
	private java.lang.String updateUser;
}
