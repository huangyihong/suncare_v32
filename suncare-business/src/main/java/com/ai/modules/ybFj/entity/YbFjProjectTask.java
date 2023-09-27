package com.ai.modules.ybFj.entity;

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
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 飞检项目审核任务表
 * @Author: jeecg-boot
 * @Date:   2023-03-10
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_project_task")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_project_task对象", description="飞检项目审核任务表")
public class YbFjProjectTask {
    
	/**唯一ID*/
	@Excel(name = "唯一ID", width = 15)
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String taskId;
	/**关联的飞检项目主键*/
	@Excel(name = "关联的飞检项目主键", width = 15)
    @ApiModelProperty(value = "关联的飞检项目主键")
	private java.lang.String projectId;
	/**关联yb_fj_project_org.project_org_id*/
	@Excel(name = "关联yb_fj_project_org.project_org_id", width = 15)
    @ApiModelProperty(value = "关联yb_fj_project_org.project_org_id")
	private java.lang.String projectOrgId;
	/**任务名称*/
	@Excel(name = "任务名称", width = 15)
    @ApiModelProperty(value = "任务名称")
	private java.lang.String taskName;
	/**任务类型*/
	@Excel(name = "任务类型", width = 15)
	@ApiModelProperty(value = "任务类型")
	private java.lang.String taskType;
	/**线索数量*/
	@Excel(name = "线索数量", width = 15)
    @ApiModelProperty(value = "线索数量")
	private java.lang.Integer caseAmount;
	/**线索金额（单位：元）*/
	@Excel(name = "线索金额（单位：元）", width = 15)
    @ApiModelProperty(value = "线索金额（单位：元）")
	private java.math.BigDecimal caseFee;
	/**线索医保基金金额（单位：元）*/
	@Excel(name = "线索医保基金金额（单位：元）", width = 15)
    @ApiModelProperty(value = "线索医保基金金额（单位：元）")
	private java.math.BigDecimal caseFundFee;
	/**任务说明*/
	@Excel(name = "任务说明", width = 15)
    @ApiModelProperty(value = "任务说明")
	private java.lang.String taskRemark;
	/**紧急程度{low:低,medium:中,high:高}*/
	@Excel(name = "紧急程度{low:低,medium:中,high:高}", width = 15)
    @ApiModelProperty(value = "紧急程度{low:低,medium:中,high:高}")
	private java.lang.String urgentLevel;
	/**期望审核完成时间*/
	@Excel(name = "期望审核完成时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "期望审核完成时间")
	private java.util.Date hopedAuditTime;
	/**审核结果(init:待审核 finish:通过 fail:不通过 reject 驳回)*/
	@Excel(name = "审核结果(init:待审核 finish:通过 fail:不通过 reject 驳回)", width = 15)
    @ApiModelProperty(value = "审核结果(init:待审核 finish:通过 fail:不通过 reject 驳回)")
	private java.lang.String auditState;
	/**线索审核人*/
	@Excel(name = "线索审核人", width = 15)
    @ApiModelProperty(value = "线索审核人")
	private java.lang.String auditUser;
	/**审核人姓名*/
	@Excel(name = "审核人姓名", width = 15)
    @ApiModelProperty(value = "审核人姓名")
	private java.lang.String auditUserName;
	/**审核时间*/
	@Excel(name = "审核时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
	private java.util.Date auditTime;
	/**审核意见*/
	@Excel(name = "审核意见", width = 15)
    @ApiModelProperty(value = "审核意见")
	private java.lang.String auditOpinion;
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
