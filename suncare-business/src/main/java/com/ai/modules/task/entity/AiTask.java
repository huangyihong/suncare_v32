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
 * @Description: AI任务表
 * @Author: jeecg-boot
 * @Date:   2022-02-28
 * @Version: V1.0
 */
@Data
@TableName("ai_task")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="ai_task对象", description="AI任务表")
public class AiTask {

	/**新增人*/
	@Excel(name = "新增人", width = 15)
    @ApiModelProperty(value = "新增人")
	private java.lang.String createUser;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
    @ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateUsername;
	/**主键id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键id")
	private java.lang.String id;
	/**项目地*/
	@Excel(name = "项目地", width = 15)
    @ApiModelProperty(value = "项目地")
	private java.lang.String projectSite;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
	private java.lang.String updateUser;
	/**任务名称*/
	@Excel(name = "任务名称", width = 15)
    @ApiModelProperty(value = "任务名称")
	private java.lang.String taskName;
	/**数据源（登录选择）*/
	@Excel(name = "数据源（登录选择）", width = 15)
    @ApiModelProperty(value = "数据源（登录选择）")
	private java.lang.String dataSource;
	/**任务文件路径*/
	@Excel(name = "任务文件路径", width = 15)
    @ApiModelProperty(value = "任务文件路径")
	private java.lang.String filenames;
	/**新增时间*/
	@Excel(name = "新增时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "新增时间")
	private java.util.Date createTime;
	/**新增人姓名*/
	@Excel(name = "新增人姓名", width = 15)
    @ApiModelProperty(value = "新增人姓名")
	private java.lang.String createUsername;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**任务状态*/
	@Excel(name = "任务状态", width = 15)
    @ApiModelProperty(value = "任务状态")
	private java.lang.String status;
}
