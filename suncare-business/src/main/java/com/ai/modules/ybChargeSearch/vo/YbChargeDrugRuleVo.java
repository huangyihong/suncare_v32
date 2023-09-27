package com.ai.modules.ybChargeSearch.vo;

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
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 药品案例库
 * @Author: jeecg-boot
 * @Date:   2023-02-08
 * @Version: V1.0
 */
@Data
public class YbChargeDrugRuleVo {

	/**创建人*/
	@Excel(name = "创建人", width = 15)
	@ApiModelProperty(value = "创建人")
	private java.lang.String createdBy;
	/**创建人名称*/
	@Excel(name = "创建人名称", width = 15)
	@ApiModelProperty(value = "创建人名称")
	private java.lang.String createdByName;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "创建时间")
	private java.util.Date createdTime;
	/**剂型*/
	@Excel(name = "剂型", width = 15)
	@ApiModelProperty(value = "剂型")
	private java.lang.String dosageType;
	/**药品名称*/
	@Excel(name = "药品名称", width = 15)
	@ApiModelProperty(value = "药品名称")
	private java.lang.String drugName;
	/**药品分类*/
	@Excel(name = "药品分类", width = 15)
	@ApiModelProperty(value = "药品分类")
	private java.lang.String drugType;
	/**药品分类(小)*/
	@Excel(name = "药品分类(小)", width = 15)
	@ApiModelProperty(value = "药品分类(小)")
	private java.lang.String drugTypeSmall;
	/**审核状态0.待审核1.审核通过2.审核不通过3.停用*/
	@Excel(name = "审核状态0.待审核1.审核通过2.审核不通过3.停用", width = 15)
	@ApiModelProperty(value = "审核状态0.待审核1.审核通过2.审核不通过3.停用")
	@MedicalDict(dicCode = "EXAMINE_STATUS")
	private java.lang.String examineStatus;
	/**医保类别*/
	@Excel(name = "医保类别", width = 15)
	@ApiModelProperty(value = "医保类别")
	private java.lang.String funType;
	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
	@ApiModelProperty(value = "id")
	private java.lang.String id;
	/**限制内容*/
	@Excel(name = "限制内容", width = 15)
	@ApiModelProperty(value = "限制内容")
	private java.lang.String limitContent;
	/**限制类型*/
	@Excel(name = "限制类型", width = 15)
	@ApiModelProperty(value = "限制类型")
	@MedicalDict(dicCode = "DC_DRUG_LIMIT_TYPE")
	private java.lang.String limitType;
	/**备注*/
	@Excel(name = "备注", width = 15)
	@ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**整理人*/
	@Excel(name = "整理人", width = 15)
	@ApiModelProperty(value = "整理人")
	private java.lang.String sorter;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
	@ApiModelProperty(value = "更新人")
	private java.lang.String updatedBy;
	/**更新人名称*/
	@Excel(name = "更新人名称", width = 15)
	@ApiModelProperty(value = "更新人名称")
	private java.lang.String updatedByName;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "更新时间")
	private java.util.Date updatedTime;

	/**机构名称*/
	@Excel(name = "机构名称", width = 15)
	@ApiModelProperty(value = "机构名称")
	private java.lang.String orgs;

	/**结算结束时间*/
	@Excel(name = "结算结束时间", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "结算结束时间")
	private java.util.Date chargedateEnddate;
	/**结算开始时间*/
	@Excel(name = "结算开始时间", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "结算开始时间")
	private java.util.Date chargedateStartdate;

	/**数据来源(医保数据src)*/
	@Excel(name = "数据来源(医保数据src)", width = 15)
	@ApiModelProperty(value = "数据来源(医保数据src)")
	@MedicalDict(dicCode = "SEARCH_ETL_SOURCE")
	private java.lang.String etlSource;

	private java.lang.String itemClass;
}
