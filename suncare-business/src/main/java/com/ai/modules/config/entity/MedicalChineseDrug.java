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
 * @Description: 中草药维护
 * @Author: jeecg-boot
 * @Date:   2019-12-20
 * @Version: V1.0
 */
@Data
@TableName(value="MEDICAL_CHINESE_DRUG", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_CHINESE_DRUG对象", description="中草药维护")
public class MedicalChineseDrug {

	/**主键id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键id")
	private java.lang.String id;
	/**中草药代码*/
	@Excel(name = "中草药代码", width = 15)
    @ApiModelProperty(value = "中草药代码")
	private java.lang.String code;
	/**中草药名称*/
	@Excel(name = "中草药名称", width = 15)
    @ApiModelProperty(value = "中草药名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String name;
	/**来源编码*/
	@Excel(name = "来源编码", width = 15)
    @ApiModelProperty(value = "来源编码")
	private java.lang.String sourceCode;
	/**来源名称*/
	@Excel(name = "来源名称", width = 15)
    @ApiModelProperty(value = "来源名称")
	private java.lang.String source;
	/**药用部位编码*/
	@Excel(name = "药用部位编码", width = 15)
    @ApiModelProperty(value = "药用部位编码")
	private java.lang.String medicalPartCode;
	/**药用部位*/
	@Excel(name = "药用部位", width = 15)
    @ApiModelProperty(value = "药用部位")
	private java.lang.String medicalPart;
	/**饮片名*/
	@Excel(name = "饮片名", width = 15)
    @ApiModelProperty(value = "饮片名")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String pieceName;
	/**饮片规格编码*/
	@Excel(name = "饮片规格编码", width = 15)
    @ApiModelProperty(value = "饮片规格编码")
	private java.lang.String pieceSizeCode;
	/**饮片规格名称*/
	@Excel(name = "饮片规格名称", width = 15)
    @ApiModelProperty(value = "饮片规格名称")
	private java.lang.String pieceSize;
	/**剂型编码*/
	@Excel(name = "剂型编码", width = 15)
    @ApiModelProperty(value = "剂型编码")
	private java.lang.String dosageTypeCode;
	/**剂型名称*/
	@Excel(name = "剂型名称", width = 15)
    @ApiModelProperty(value = "剂型名称")
	private java.lang.String dosageType;
	/**炮制方法编码*/
	@Excel(name = "炮制方法编码", width = 15)
    @ApiModelProperty(value = "炮制方法编码")
	private java.lang.String methodCode;
	/**炮制方法*/
	@Excel(name = "炮制方法", width = 15)
    @ApiModelProperty(value = "炮制方法")
	private java.lang.String method;
	/**常用处方名*/
	@Excel(name = "常用处方名", width = 15)
    @ApiModelProperty(value = "常用处方名")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String prescription;
	/**产地编码*/
	@Excel(name = "产地编码", width = 15)
    @ApiModelProperty(value = "产地编码")
	private java.lang.String placeCode;
	/**产地名称*/
	@Excel(name = "产地名称", width = 15)
    @ApiModelProperty(value = "产地名称")
	private java.lang.String place;
	/**描述*/
	@Excel(name = "描述", width = 15)
    @ApiModelProperty(value = "描述")
	private java.lang.String remark;
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
	/**收费类别编码*/
	@Excel(name = "收费类别编码", width = 15)
	@ApiModelProperty(value = "收费类别编码")
	private java.lang.String chargeClassCode;
	/**收费类别名称*/
	@Excel(name = "收费类别名称", width = 15)
	@ApiModelProperty(value = "收费类别名称")
	private java.lang.String chargeClassName;
	/**国家医保编码*/
	@Excel(name = "国家医保编码", width = 15)
	@ApiModelProperty(value = "国家医保编码")
	private java.lang.String ybCode;
}
