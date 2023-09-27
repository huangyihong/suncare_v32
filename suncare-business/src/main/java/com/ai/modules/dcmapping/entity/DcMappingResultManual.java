package com.ai.modules.dcmapping.entity;

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
 * @Description: 采集映射源表字段手工标记结果表
 * @Author: jeecg-boot
 * @Date:   2022-04-25
 * @Version: V1.0
 */
@Data
@TableName("DC_MAPPING_RESULT_MANUAL")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="DC_MAPPING_RESULT_MANUAL对象", description="采集映射源表字段手工标记结果表")
public class DcMappingResultManual {
    
	/**手工标记结果ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "手工标记结果ID")
	private java.lang.String id;
	/**任务ID，对应DC_MAPPING_TASK的ID字段*/
	@Excel(name = "任务ID，对应DC_MAPPING_TASK的ID字段", width = 15)
    @ApiModelProperty(value = "任务ID，对应DC_MAPPING_TASK的ID字段")
	private java.lang.String taskId;
	/**客户源表*/
	@Excel(name = "客户源表", width = 15)
    @ApiModelProperty(value = "客户源表")
	private java.lang.String sourceTableName;
	/**客户源表字段名*/
	@Excel(name = "客户源表字段名", width = 15)
    @ApiModelProperty(value = "客户源表字段名")
	private java.lang.String sourceColumnName;
	/**目标表名*/
	@Excel(name = "目标表名", width = 15)
    @ApiModelProperty(value = "目标表名")
	private java.lang.String destTableName;
	/**目标表字段名*/
	@Excel(name = "目标表字段名", width = 15)
    @ApiModelProperty(value = "目标表字段名")
	private java.lang.String destColumnName;
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
}
