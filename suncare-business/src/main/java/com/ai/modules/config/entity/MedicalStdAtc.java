package com.ai.modules.config.entity;


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
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: ATC药品级别信息
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
@Data
@TableName(value="MEDICAL_STD_ATC", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_STD_ATC对象", description="ATC药品级别信息")
public class MedicalStdAtc {

	/**药品信息id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键id")
	private String id;
	/**ATC药品编码*/
	@Excel(name = "ATC药品编码", width = 15)
    @ApiModelProperty(value = "ATC药品编码")
	private String code;
	/**ATC药品名称*/
	@Excel(name = "ATC药品名称", width = 15)
    @ApiModelProperty(value = "ATC药品名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private String name;
	/**ATC药品1级代码*/
	@Excel(name = "ATC药品1级代码", width = 15)
    @ApiModelProperty(value = "ATC药品1级代码")
	private String act1Code;
	/**ATC药品1级名称*/
	@Excel(name = "ATC药品1级名称", width = 15)
    @ApiModelProperty(value = "ATC药品1级名称")
	private String act1Name;
	/**ATC药品2级代码*/
	@Excel(name = "ATC药品2级代码", width = 15)
    @ApiModelProperty(value = "ATC药品2级代码")
	private String act2Code;
	/**ATC药品2级名称*/
	@Excel(name = "ATC药品2级名称", width = 15)
    @ApiModelProperty(value = "ATC药品2级名称")
	private String act2Name;
	/**ATC药品3级代码*/
	@Excel(name = "ATC药品3级代码", width = 15)
    @ApiModelProperty(value = "ATC药品3级代码")
	private String act3Code;
	/**ATC药品3级名称*/
	@Excel(name = "ATC药品3级名称", width = 15)
    @ApiModelProperty(value = "ATC药品3级名称")
	private String act3Name;
	/**ATC药品4级代码*/
	@Excel(name = "ATC药品4级代码", width = 15)
    @ApiModelProperty(value = "ATC药品4级代码")
	private String act4Code;
	/**ATC药品4级名称*/
	@Excel(name = "ATC药品4级名称", width = 15)
    @ApiModelProperty(value = "ATC药品4级名称")
	private String act4Name;
	/**收费类别编码*/
	@Excel(name = "收费类别编码", width = 15)
	@ApiModelProperty(value = "收费类别编码")
	private String chargeClassCode;
	/**收费类别名称*/
	@Excel(name = "收费类别名称", width = 15)
	@ApiModelProperty(value = "收费类别名称")
	private String chargeClassName;
	/**剂型代码*/
	@Excel(name = "剂型代码", width = 15)
	@ApiModelProperty(value = "剂型代码")
	private java.lang.String dosageCode;
	/**剂型名称*/
	@Excel(name = "剂型名称", width = 15)
	@ApiModelProperty(value = "剂型名称")
	private java.lang.String dosage;
	/**备注*/
	@Excel(name = "备注", width = 15)
	@ApiModelProperty(value = "备注")
	private String remark;
	/**数据状态(0待生效 1有效 2无效)*/
	@Excel(name = "数据状态(0待生效 1有效 2无效)", width = 15)
    @ApiModelProperty(value = "数据状态(0待生效 1有效 2无效)")
	@Dict(dicCode = "base_data_state")
	private String state;
	/**审核结果(0待审核 1审核通过 2审核不通过)*/
	@Excel(name = "审核结果(0待审核 1审核通过 2审核不通过)", width = 15)
    @ApiModelProperty(value = "审核结果(0待审核 1审核通过 2审核不通过)")
	@Dict(dicCode = "base_auditResult")
	private String auditResult;
	/**操作类型(新增add 修改update 删除delete)*/
	@Excel(name = "操作类型(新增add 修改update 删除delete)", width = 15)
    @ApiModelProperty(value = "操作类型(新增add 修改update 删除delete)")
	@Dict(dicCode = "base_actionType")
	private String actionType;
	/**新增人*/
	@Excel(name = "新增人", width = 15)
    @ApiModelProperty(value = "新增人")
	private String createStaff;
	/**新增人姓名*/
	@Excel(name = "新增人姓名", width = 15)
    @ApiModelProperty(value = "新增人姓名")
	private String createStaffName;
	/**新增时间*/
	@Excel(name = "新增时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "新增时间")
	private java.util.Date createTime;
	/**新增原因*/
	@Excel(name = "新增原因", width = 15)
    @ApiModelProperty(value = "新增原因")
	private String createReason;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
	private String updateStaff;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
    @ApiModelProperty(value = "修改人姓名")
	private String updateStaffName;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**修改原因*/
	@Excel(name = "修改原因", width = 15)
    @ApiModelProperty(value = "修改原因")
	private String updateReason;
	/**删除人*/
	@Excel(name = "删除人", width = 15)
    @ApiModelProperty(value = "删除人")
	private String deleteStaff;
	/**删除人姓名*/
	@Excel(name = "删除人姓名", width = 15)
    @ApiModelProperty(value = "删除人姓名")
	private String deleteStaffName;
	/**删除时间*/
	@Excel(name = "删除时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "删除时间")
	private java.util.Date deleteTime;
	/**删除原因*/
	@Excel(name = "删除原因", width = 15)
    @ApiModelProperty(value = "删除原因")
	private String deleteReason;
	/**最新操作人*/
	@Excel(name = "最新操作人", width = 15)
    @ApiModelProperty(value = "最新操作人")
	private String actionStaff;
	/**操作人姓名*/
	@Excel(name = "操作人姓名", width = 15)
    @ApiModelProperty(value = "操作人姓名")
	private String actionStaffName;
	/**操作时间*/
	@Excel(name = "操作时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "操作时间")
	private java.util.Date actionTime;
	/**审核人*/
	@Excel(name = "审核人", width = 15)
    @ApiModelProperty(value = "审核人")
	private String auditStaff;
	/**审核人姓名*/
	@Excel(name = "审核人姓名", width = 15)
    @ApiModelProperty(value = "审核人姓名")
	private String auditStaffName;
	/**审核时间*/
	@Excel(name = "审核时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
	private java.util.Date auditTime;
	/**审核意见*/
	@Excel(name = "审核意见", width = 15)
    @ApiModelProperty(value = "审核意见")
	private String auditOpinion;
}
