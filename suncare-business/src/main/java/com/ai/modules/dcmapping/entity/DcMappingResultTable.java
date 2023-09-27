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
 * @Description: 采集映射表名称映射结果表
 * @Author: jeecg-boot
 * @Date:   2022-04-18
 * @Version: V1.0
 */
@Data
@TableName("DC_MAPPING_RESULT_TABLE")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="DC_MAPPING_RESULT_TABLE对象", description="采集映射表名称映射结果表")
public class DcMappingResultTable {

	/**表映射结果ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "表映射结果ID")
	private java.lang.String id;
	/**任务ID，对应DC_MAPPING_TASK的ID字段*/
	@Excel(name = "任务ID，对应DC_MAPPING_TASK的ID字段", width = 15)
    @ApiModelProperty(value = "任务ID，对应DC_MAPPING_TASK的ID字段")
	private java.lang.String taskId;
	/**目标表名*/
	@Excel(name = "目标表名", width = 15)
    @ApiModelProperty(value = "目标表名")
	private java.lang.String destTableName;
	/**客户源表*/
	@Excel(name = "客户源表", width = 15)
    @ApiModelProperty(value = "客户源表")
	private java.lang.String sourceTableName;
	/**概率*/
	@Excel(name = "概率", width = 15)
    @ApiModelProperty(value = "概率")
	private java.lang.Float pr;
	/**两张表是否关联，通过前台人工标记， 0=否 ;1=是*/
	@Excel(name = "两张表是否关联，通过前台人工标记， 0=否 ;1=是", width = 15)
    @ApiModelProperty(value = "两张表是否关联，通过前台人工标记， 0=否 ;1=是")
	private java.lang.String isRelation;
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
