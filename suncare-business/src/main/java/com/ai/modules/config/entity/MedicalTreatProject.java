package com.ai.modules.config.entity;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 医疗服务项目
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
@Data
@TableName(value="MEDICAL_TREAT_PROJECT", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_TREAT_PROJECT对象", description="医疗服务项目")
public class MedicalTreatProject {
    
	/**诊疗项目id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "诊疗项目id")
	private java.lang.String id;
	/**上级分类编码*/
	@Excel(name = "上级分类编码", width = 15)
    @ApiModelProperty(value = "上级分类编码")
	private java.lang.String typeCode;
	/**上级分类名称*/
	@Excel(name = "上级分类名称", width = 15)
    @ApiModelProperty(value = "上级分类名称")
	private java.lang.String typeName;
	/**项目编码*/
	@Excel(name = "项目编码", width = 15)
    @ApiModelProperty(value = "项目编码")
	private java.lang.String code;
	/**项目名称*/
	@Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String name;
	/**财务分类*/
	@Excel(name = "财务分类", width = 15)
    @ApiModelProperty(value = "财务分类")
	private java.lang.String financeType;
	/**计价单位名称*/
	@Excel(name = "计价单位名称", width = 15)
    @ApiModelProperty(value = "计价单位名称")
	private java.lang.String chargeType;
	/**单价*/
	@Excel(name = "单价", width = 15)
    @ApiModelProperty(value = "单价")
	private java.lang.String priceUnit;
	/**除外内容*/
	@Excel(name = "除外内容", width = 15)
    @ApiModelProperty(value = "除外内容")
	private java.lang.String exceptContent;
	/**项目内涵*/
	@Excel(name = "项目内涵", width = 15)
    @ApiModelProperty(value = "项目内涵")
	private java.lang.String projectContent;
	/**排序号*/
	@Excel(name = "排序号", width = 15)
    @ApiModelProperty(value = "排序号")
	private java.lang.Long orderNum;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**项目分类编码2级*/
	@Excel(name = "项目分类编码2级", width = 15)
    @ApiModelProperty(value = "项目分类编码2级")
	private java.lang.String type2Code;
	/**项目分类名称2级*/
	@Excel(name = "项目分类名称2级", width = 15)
    @ApiModelProperty(value = "项目分类名称2级")
	private java.lang.String type2Name;
	/**项目分类编码3级*/
	@Excel(name = "项目分类编码3级", width = 15)
    @ApiModelProperty(value = "项目分类编码3级")
	private java.lang.String type3Code;
	/**项目分类名称3级*/
	@Excel(name = "项目分类名称3级", width = 15)
    @ApiModelProperty(value = "项目分类名称3级")
	private java.lang.String type3Name;
	/**项目分类编码4级*/
	@Excel(name = "项目分类编码4级", width = 15)
    @ApiModelProperty(value = "项目分类编码4级")
	private java.lang.String type4Code;
	/**项目分类名称4级*/
	@Excel(name = "项目分类名称4级", width = 15)
    @ApiModelProperty(value = "项目分类名称4级")
	private java.lang.String type4Name;
	/**旧项目编码*/
	@Excel(name = "旧项目编码", width = 15)
    @ApiModelProperty(value = "旧项目编码")
	private java.lang.String treatmentOldcode;
	/**有效标志*/
	@Excel(name = "有效标志", width = 15)
    @ApiModelProperty(value = "有效标志")
	private java.lang.String validStatus;
	/**特殊材料标志*/
	@Excel(name = "特殊材料标志", width = 15)
    @ApiModelProperty(value = "特殊材料标志")
	private java.lang.String specialmark;
	/**收费类别编码*/
	@Excel(name = "收费类别编码", width = 15)
    @ApiModelProperty(value = "收费类别编码")
	private java.lang.String chargeCode;
	/**收费类别名称*/
	@Excel(name = "收费类别名称", width = 15)
    @ApiModelProperty(value = "收费类别名称")
	private java.lang.String charge;
	/**计价单位编码*/
	@Excel(name = "计价单位编码", width = 15)
    @ApiModelProperty(value = "计价单位编码")
	private java.lang.String chargeTypeCode;
	/**说明*/
	@Excel(name = "说明", width = 15)
    @ApiModelProperty(value = "说明")
	private java.lang.String instructions;
	/**项目分类编码1级*/
	@Excel(name = "项目分类编码1级", width = 15)
    @ApiModelProperty(value = "项目分类编码1级")
	private java.lang.String type1Code;
	/**项目分类名称1级*/
	@Excel(name = "项目分类名称1级", width = 15)
    @ApiModelProperty(value = "项目分类名称1级")
	private java.lang.String type1Name;
	/**数据状态(0待生效 1有效 2无效)*/
	@Excel(name = "数据状态(0待生效 1有效 2无效)", width = 15)
    @ApiModelProperty(value = "数据状态(0待生效 1有效 2无效)")
	@Dict(dicCode = "base_data_state")
	private java.lang.String state;
	/**审核结果(0待审核 1审核通过 2审核不通过)*/
	@Excel(name = "审核结果(0待审核 1审核通过 2审核不通过)", width = 15)
    @ApiModelProperty(value = "审核结果(0待审核 1审核通过 2审核不通过)")
	@Dict(dicCode = "base_auditResult")
	private java.lang.String auditResult;
	/**操作类型(新增add 修改update 删除delete)*/
	@Excel(name = "操作类型(新增add 修改update 删除delete)", width = 15)
    @ApiModelProperty(value = "操作类型(新增add 修改update 删除delete)")
	@Dict(dicCode = "base_actionType")
	private java.lang.String actionType;
	/**新增人*/
	@Excel(name = "新增人", width = 15)
    @ApiModelProperty(value = "新增人")
	private java.lang.String createStaff;
	/**新增人姓名*/
	@Excel(name = "新增人姓名", width = 15)
    @ApiModelProperty(value = "新增人姓名")
	private java.lang.String createStaffName;
	/**新增时间*/
	@Excel(name = "新增时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "新增时间")
	private java.util.Date createTime;
	/**新增原因*/
	@Excel(name = "新增原因", width = 15)
    @ApiModelProperty(value = "新增原因")
	private java.lang.String createReason;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
	private java.lang.String updateStaff;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
    @ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateStaffName;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**修改原因*/
	@Excel(name = "修改原因", width = 15)
    @ApiModelProperty(value = "修改原因")
	private java.lang.String updateReason;
	/**删除人*/
	@Excel(name = "删除人", width = 15)
    @ApiModelProperty(value = "删除人")
	private java.lang.String deleteStaff;
	/**删除人姓名*/
	@Excel(name = "删除人姓名", width = 15)
    @ApiModelProperty(value = "删除人姓名")
	private java.lang.String deleteStaffName;
	/**删除时间*/
	@Excel(name = "删除时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "删除时间")
	private java.util.Date deleteTime;
	/**删除原因*/
	@Excel(name = "删除原因", width = 15)
    @ApiModelProperty(value = "删除原因")
	private java.lang.String deleteReason;
	/**最新操作人*/
	@Excel(name = "最新操作人", width = 15)
    @ApiModelProperty(value = "最新操作人")
	private java.lang.String actionStaff;
	/**操作人姓名*/
	@Excel(name = "操作人姓名", width = 15)
    @ApiModelProperty(value = "操作人姓名")
	private java.lang.String actionStaffName;
	/**操作时间*/
	@Excel(name = "操作时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "操作时间")
	private java.util.Date actionTime;
	/**审核人*/
	@Excel(name = "审核人", width = 15)
    @ApiModelProperty(value = "审核人")
	private java.lang.String auditStaff;
	/**审核人姓名*/
	@Excel(name = "审核人姓名", width = 15)
    @ApiModelProperty(value = "审核人姓名")
	private java.lang.String auditStaffName;
	/**审核时间*/
	@Excel(name = "审核时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
	private java.util.Date auditTime;
	/**审核意见*/
	@Excel(name = "审核意见", width = 15)
    @ApiModelProperty(value = "审核意见")
	private java.lang.String auditOpinion;
}
