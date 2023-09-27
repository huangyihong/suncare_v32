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
 * @Description: 采集映射任务信息表
 * @Author: jeecg-boot
 * @Date:   2022-04-18
 * @Version: V1.0
 */
@Data
@TableName("DC_MAPPING_TASK")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="DC_MAPPING_TASK对象", description="采集映射任务信息表")
public class DcMappingTask {

	/**任务ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "任务ID")
	private java.lang.String id;
	/**采集数据的客户名称*/
	@Excel(name = "采集数据的客户名称", width = 15)
    @ApiModelProperty(value = "采集数据的客户名称")
	private java.lang.String customerName;
	/**任务状态 0=未映射 ;1=正在进行；2=映射成功；3=映射异常*/
	@Excel(name = "任务状态 0=未映射 ;1=正在进行；2=映射成功；3=映射异常", width = 15)
    @ApiModelProperty(value = "任务状态 0=未映射 ;1=正在进行；2=映射成功；3=映射异常")
	private java.lang.String taskStatus;
	/**源数据库名称*/
	@Excel(name = "源数据库名称", width = 15)
    @ApiModelProperty(value = "源数据库名称")
	private java.lang.String sourceDbName;
	/**源数据库类型，冗余字段*/
	@Excel(name = "源数据库类型，冗余字段", width = 15)
    @ApiModelProperty(value = "源数据库类型，冗余字段")
	private java.lang.String sourceDbType;
	/**HIS厂商名称*/
	@Excel(name = "HIS厂商名称", width = 15)
    @ApiModelProperty(value = "HIS厂商名称")
	private java.lang.String hisProducer;
	/**HIS版本号*/
	@Excel(name = "HIS版本号", width = 15)
    @ApiModelProperty(value = "HIS版本号")
	private java.lang.String hisVersion;
	/**从每张表中抽取出来的数据条数*/
	@Excel(name = "从每张表中抽取出来的数据条数", width = 15)
	@ApiModelProperty(value = "从每张表中抽取出来的数据条数")
	private java.lang.Integer fetchRowFromEachTable;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**状态 是否有效(1.有效,0.无效)*/
	@Excel(name = "状态 是否有效(1.有效,0.无效)", width = 15)
    @ApiModelProperty(value = "状态 是否有效(1.有效,0.无效)")
	private java.lang.String status;
	/**映射数据表数量*/
	@Excel(name = "映射数据表数量", width = 15)
    @ApiModelProperty(value = "映射数据表数量")
	private java.lang.Integer mappingTableCount;
	/**开始映射时间*/
	@Excel(name = "开始映射时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "开始映射时间")
	private java.util.Date mappingTimeStart;
	/**结束映射时间*/
	@Excel(name = "结束映射时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "结束映射时间")
	private java.util.Date mappingTimeEnd;
	/**项目地名称，同数仓中的project*/
	@Excel(name = "项目地名称，同数仓中的project", width = 15)
    @ApiModelProperty(value = "项目地名称，同数仓中的project")
	private java.lang.String project;
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
