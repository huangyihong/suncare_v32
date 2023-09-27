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
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 基础数据维护操作日志
 * @Author: jeecg-boot
 * @Date:   2019-12-19
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_AUDIT_LOG")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_AUDIT_LOG对象", description="基础数据维护操作日志")
public class MedicalAuditLog {
    
	/**主键id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键id")
	private java.lang.String id;
	/**操作表*/
	@Excel(name = "操作表", width = 15)
    @ApiModelProperty(value = "操作表")
	private java.lang.String tableName;
	/**记录主键*/
	@Excel(name = "记录主键", width = 15)
    @ApiModelProperty(value = "记录主键")
	private java.lang.String recordKey;
	/**操作类型(新增add 修改update 删除delete 撤销cancel  清理clean)*/
	@Excel(name = "操作类型(新增add 修改update 删除delete 撤销cancel  清理clean)", width = 15)
    @ApiModelProperty(value = "操作类型(新增add 修改update 删除delete 撤销cancel  清理clean)")
	@Dict(dicCode = "audit_actionType")
	private java.lang.String actionType;
	/**操作人*/
	@Excel(name = "操作人", width = 15)
    @ApiModelProperty(value = "操作人")
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
	/**操作原因*/
	@Excel(name = "操作原因", width = 15)
    @ApiModelProperty(value = "操作原因")
	private java.lang.String actionReason;
	/**审核结果(0待审核 1审核通过 2审核不通过)*/
	@Excel(name = "审核结果(0待审核 1审核通过 2审核不通过)", width = 15)
    @ApiModelProperty(value = "审核结果(0待审核 1审核通过 2审核不通过)")
	@Dict(dicCode = "base_auditResult")
	private java.lang.String auditResult;
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
	/**操作内容*/
	@Excel(name = "操作内容", width = 15)
    @ApiModelProperty(value = "操作内容")
	private java.lang.String actionContent;
	/**修改内容json*/
	@Excel(name = "修改内容json", width = 15)
    @ApiModelProperty(value = "修改内容json")
	private java.lang.String updateJson;
}
