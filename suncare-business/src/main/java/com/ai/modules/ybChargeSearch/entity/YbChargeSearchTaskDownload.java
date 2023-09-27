package com.ai.modules.ybChargeSearch.entity;

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
 * @Description: 文件下载日志表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_search_task_download")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_search_task_download对象", description="文件下载日志表")
public class YbChargeSearchTaskDownload {
    
	/**下载时间*/
	@Excel(name = "下载时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "下载时间")
	private java.util.Date createTime;
	/**下载人*/
	@Excel(name = "下载人", width = 15)
    @ApiModelProperty(value = "下载人")
	private java.lang.String createUser;
	/**下载人ID*/
	@Excel(name = "下载人ID", width = 15)
    @ApiModelProperty(value = "下载人ID")
	private java.lang.String createUserId;
	/**文件名*/
	@Excel(name = "文件名", width = 15)
    @ApiModelProperty(value = "文件名")
	private java.lang.String fileName;
	/**文件路径*/
	@Excel(name = "文件路径", width = 15)
    @ApiModelProperty(value = "文件路径")
	private java.lang.String filePath;
	/**文件大小*/
	@Excel(name = "文件大小", width = 15)
    @ApiModelProperty(value = "文件大小")
	private java.lang.Double fileSize;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**记录数*/
	@Excel(name = "记录数", width = 15)
    @ApiModelProperty(value = "记录数")
	private java.lang.Integer recordCount;
	/**任务id*/
	@Excel(name = "任务id", width = 15)
    @ApiModelProperty(value = "任务id")
	private java.lang.String taskId;
	/**任务类型(search收费明细查询yearStatistics年度统计)*/
	@Excel(name = "任务类型(search收费明细查询yearStatistics年度统计)", width = 15)
    @ApiModelProperty(value = "任务类型(search收费明细查询yearStatistics年度统计)")
	private java.lang.String taskType;
}
