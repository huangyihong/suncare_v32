package com.ai.modules.task.entity;

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
 * @Description: 不同不合规行为显示字段配置
 * @Author: jeecg-boot
 * @Date:   2020-10-12
 * @Version: V1.0
 */
@Data
@TableName("TASK_ACTION_FIELD_CONFIG")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_ACTION_FIELD_CONFIG对象", description="不同不合规行为显示字段配置")
public class TaskActionFieldConfig {

	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	@Excel(name = "特殊模型配置ID", width = 15)
	@ApiModelProperty(value = "特殊模型配置ID")
	private java.lang.String classifyId;
	/**不合规行为ID*/
	@Excel(name = "不合规行为ID", width = 15)
    @ApiModelProperty(value = "不合规行为ID")
	private java.lang.String actionId;
	/**不合规行为名称*/
	@Excel(name = "不合规行为名称", width = 15)
    @ApiModelProperty(value = "不合规行为名称")
	private java.lang.String actionName;
	/**状态01 开启*/
	@Excel(name = "状态normal开启 stop ", width = 15)
    @ApiModelProperty(value = "状态normal开启 stop 开启")
	private java.lang.String status;
	/**字段(多值)*/
	@Excel(name = "字段(多值)", width = 15)
    @ApiModelProperty(value = "字段(多值)")
	private java.lang.String fields;
	/**字段名称(多值)*/
	@Excel(name = "字段名称(多值)", width = 15)
    @ApiModelProperty(value = "字段名称(多值)")
	private java.lang.String fieldTitles;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
	private java.lang.String updateUser;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
    @ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateUsername;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;

	@Excel(name = "汇总层级字段", width = 15)
    @ApiModelProperty(value = "汇总层级字段")
	private java.lang.String groupFields;

	@Excel(name = "适用平台", width = 15)
	@ApiModelProperty(value = "适用平台")
	private java.lang.String platform;
}
