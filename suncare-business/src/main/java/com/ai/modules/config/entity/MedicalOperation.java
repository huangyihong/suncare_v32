package com.ai.modules.config.entity;

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
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 手术信息
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
@Data
@TableName(value="MEDICAL_OPERATION", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_OPERATION对象", description="手术信息")
public class MedicalOperation {
    
	/**手术编码ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "手术编码ID")
	private java.lang.String id;
	/**编码*/
	@Excel(name = "编码", width = 15)
    @ApiModelProperty(value = "编码")
	private java.lang.String code;
	/**名称*/
	@Excel(name = "名称", width = 15)
    @ApiModelProperty(value = "名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String name;
	/**拼音简称*/
	@Excel(name = "拼音简称", width = 15)
    @ApiModelProperty(value = "拼音简称")
	private java.lang.String shortName;
	/**手术级别*/
	@Excel(name = "手术级别", width = 15)
    @ApiModelProperty(value = "手术级别")
	private java.lang.String operationLevel;
	/**排序号*/
	@Excel(name = "排序号", width = 15)
    @ApiModelProperty(value = "排序号")
	private java.lang.Long orderNum;
	/**描述*/
	@Excel(name = "描述", width = 15)
    @ApiModelProperty(value = "描述")
	private java.lang.String remark;
	/**手术类别码*/
	@Excel(name = "手术类别码", width = 15)
    @ApiModelProperty(value = "手术类别码")
	private java.lang.String typeCode;
	/**手术类别名称*/
	@Excel(name = "手术类别名称", width = 15)
    @ApiModelProperty(value = "手术类别名称")
	private java.lang.String typeName;
	/**手术级别编码*/
	@Excel(name = "手术级别编码", width = 15)
    @ApiModelProperty(value = "手术级别编码")
	private java.lang.String operationLevelCode;
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
	/**章编码*/
	@Excel(name = "章编码", width = 15)
    @ApiModelProperty(value = "章编码")
	private java.lang.String type1Code;
	/**章名称*/
	@Excel(name = "章名称", width = 15)
    @ApiModelProperty(value = "章名称")
	private java.lang.String type1Name;
	/**节编码*/
	@Excel(name = "节编码", width = 15)
    @ApiModelProperty(value = "节编码")
	private java.lang.String type2Code;
	/**节名称*/
	@Excel(name = "节名称", width = 15)
    @ApiModelProperty(value = "节名称")
	private java.lang.String type2Name;
	/**类目编码*/
	@Excel(name = "类目编码", width = 15)
    @ApiModelProperty(value = "类目编码")
	private java.lang.String type3Code;
	/**类目名称*/
	@Excel(name = "类目名称", width = 15)
    @ApiModelProperty(value = "类目名称")
	private java.lang.String type3Name;
	/**亚目编码*/
	@Excel(name = "亚目编码", width = 15)
    @ApiModelProperty(value = "亚目编码")
	private java.lang.String type4Code;
	/**亚目名称*/
	@Excel(name = "亚目名称", width = 15)
    @ApiModelProperty(value = "亚目名称")
	private java.lang.String type4Name;
	/**手术无菌程度代码*/
	@Excel(name = "手术无菌程度代码", width = 15)
    @ApiModelProperty(value = "手术无菌程度代码")
	private java.lang.String bacterialLevel;
	/**手术无菌程度名称*/
	@Excel(name = "手术无菌程度名称", width = 15)
    @ApiModelProperty(value = "手术无菌程度名称")
	private java.lang.String bacterialLevelName;
}
