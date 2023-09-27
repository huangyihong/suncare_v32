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
import org.jeecg.common.util.dbencrypt.EncryptTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 新版本政策法规
 * @Author: jeecg-boot
 * @Date:   2021-08-04
 * @Version: V1.0
 */
@Data
@TableName(value="MEDICAL_POLICY", autoResultMap=true)
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_POLICY对象", description="新版本政策法规")
public class MedicalPolicy {
    
	/**政策法规id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "政策法规id")
	private java.lang.String id;
	/**政策法规编号*/
	@Excel(name = "政策法规编号", width = 15)
    @ApiModelProperty(value = "政策法规编号")
	private java.lang.String policyCode;
	/**信息类型code*/
	@Excel(name = "信息类型code", width = 15)
    @ApiModelProperty(value = "信息类型code")
	private java.lang.String policyTypeCode;
	/**信息类型name*/
	@Excel(name = "信息类型name", width = 15)
    @ApiModelProperty(value = "信息类型name")
	private java.lang.String policyTypeName;
	/**效力级别code*/
	@Excel(name = "效力级别code", width = 15)
    @ApiModelProperty(value = "效力级别code")
	private java.lang.String effectLevelCode;
	/**效力级别name*/
	@Excel(name = "效力级别name", width = 15)
    @ApiModelProperty(value = "效力级别name")
	private java.lang.String effectLevelName;
	/**文件用途分类code*/
	@Excel(name = "文件用途分类code", width = 15)
    @ApiModelProperty(value = "文件用途分类code")
	private java.lang.String policyServiceClassCode;
	/**文件用途分类name*/
	@Excel(name = "文件用途分类name", width = 15)
    @ApiModelProperty(value = "文件用途分类name")
	private java.lang.String policyServiceClassName;
	/**文件名称*/
	@Excel(name = "文件名称", width = 15)
    @ApiModelProperty(value = "文件名称")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String policyName;
	/**政策文号*/
	@Excel(name = "政策文号", width = 15)
    @ApiModelProperty(value = "政策文号")
	@TableField(typeHandler = EncryptTypeHandler.class)
	private java.lang.String policyDocNumber;
	/**相对应废止的政策文号*/
	@Excel(name = "相对应废止的政策文号", width = 15)
    @ApiModelProperty(value = "相对应废止的政策文号")
	private java.lang.String policyDiscardDocNumber;
	/**发文机关*/
	@Excel(name = "发文机关", width = 15)
    @ApiModelProperty(value = "发文机关")
	private java.lang.String issuingOffice;
	/**发文机关所属地区*/
	@Excel(name = "发文机关所属地区", width = 15)
    @ApiModelProperty(value = "发文机关所属地区")
	private java.lang.String issuingOfficeArea;
	/**发文时间*/
	@Excel(name = "发文时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "发文时间")
	private java.util.Date issuingDate;
	/**政策文件原始下载网址*/
	@Excel(name = "政策文件原始下载网址", width = 15)
    @ApiModelProperty(value = "政策文件原始下载网址")
	private java.lang.String docOriginalUrl;
	/**适用地区*/
	@Excel(name = "适用地区", width = 15)
    @ApiModelProperty(value = "适用地区")
	private java.lang.String applyArea;
	/**适用人群*/
	@Excel(name = "适用人群", width = 15)
    @ApiModelProperty(value = "适用人群")
	private java.lang.String applyPeople;
	/**适用开始时间*/
	@Excel(name = "适用开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "适用开始时间")
	private java.util.Date effectStartdate;
	/**适用截止时间*/
	@Excel(name = "适用截止时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "适用截止时间")
	private java.util.Date effectEnddate;
	/**数据状态*/
	@Excel(name = "数据状态", width = 15)
    @ApiModelProperty(value = "数据状态")
	private java.lang.String dataStatus;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**新增人*/
	@Excel(name = "新增人", width = 15)
    @ApiModelProperty(value = "新增人")
	private java.lang.String createUser;
	/**新增人姓名*/
	@Excel(name = "新增人姓名", width = 15)
    @ApiModelProperty(value = "新增人姓名")
	private java.lang.String createUsername;
	/**新增时间*/
	@Excel(name = "新增时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "新增时间")
	private java.util.Date createTime;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
	private java.lang.String updateUser;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
    @ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateUsername;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**附件名称*/
	@Excel(name = "附件名称", width = 15)
    @ApiModelProperty(value = "附件名称")
	private java.lang.String filenames;
	/**适用地区id*/
	@Excel(name = "适用地区id", width = 15)
    @ApiModelProperty(value = "适用地区id")
	private java.lang.String applyAreaId;
	/**序号*/
	@Excel(name = "序号", width = 15)
    @ApiModelProperty(value = "序号")
	private java.lang.Long isOrder;
	/**正文附件*/
	@Excel(name = "正文附件", width = 15)
    @ApiModelProperty(value = "正文附件")
	private java.lang.String textFilenames;
}
