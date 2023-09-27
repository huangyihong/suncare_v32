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
 * @Description: 任务批次
 * @Author: jeecg-boot
 * @Date:   2020-01-08
 * @Version: V1.0
 */
@Data
@TableName("TASK_PROJECT_BATCH")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_PROJECT_BATCH对象", description="任务批次")
public class TaskProjectBatch {

	/**批次ID*/
	@Excel(name = "批次ID", width = 15)
    @ApiModelProperty(value = "批次ID")
	@TableId("BATCH_ID")
	private java.lang.String batchId;
	/**批次名称*/
	@Excel(name = "批次名称", width = 15)
    @ApiModelProperty(value = "批次名称")
	private java.lang.String batchName;
	/**风控月份*/
	@Excel(name = "风控月份", width = 15)
    @ApiModelProperty(value = "风控月份")
	private java.lang.String month;
	/**步骤进度*/
	@Excel(name = "步骤进度", width = 15)
    @ApiModelProperty(value = "步骤进度")
	private java.lang.Integer step;
	/**负责人*/
	@Excel(name = "负责人", width = 15)
    @ApiModelProperty(value = "负责人")
	private java.lang.String picId;
	/**所属项目ID*/
	@Excel(name = "所属项目ID", width = 15)
    @ApiModelProperty(value = "所属项目ID")
	private java.lang.String projectId;
	/**空未生成，wait等待执行，running正在生成，normal已生成，abnormal异常*/
	@Excel(name = "空未生成，wait等待执行，running正在生成，normal已生成，abnormal异常", width = 15)
    @ApiModelProperty(value = "空未生成，wait等待执行，running正在生成，normal已生成，abnormal异常")
	private java.lang.String status;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
    @ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateUserName;
	/**修改人ID*/
	@Excel(name = "修改人ID", width = 15)
    @ApiModelProperty(value = "修改人ID")
	private java.lang.String updateUser;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "updateTime")
	private java.util.Date updateTime;
	/**createUserName*/
	@Excel(name = "createUserName", width = 15)
    @ApiModelProperty(value = "createUserName")
	private java.lang.String createUserName;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**createTime*/
	@Excel(name = "createTime", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "createTime")
	private java.util.Date createTime;

	/**数据来源*/
	@Excel(name = "数据来源", width = 15)
	@ApiModelProperty(value = "数据来源")
	private java.lang.String etlSource;
	/**输出来源*/
	@Excel(name = "输出来源", width = 15)
	@ApiModelProperty(value = "输出来源")
	private java.lang.String outSource;
	/**项目开始时间*/
	@Excel(name = "数据开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "项目开始时间")
	private java.util.Date startTime;
	/**项目结束时间*/
	@Excel(name = "数据结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "项目结束时间")
	private java.util.Date endTime;

	@Excel(name = "自定义数据范围", width = 15)
	@ApiModelProperty(value = "自定义数据范围")
	private java.lang.String customFilter;

	@Excel(name = "配置的规则类型", width = 15)
	@ApiModelProperty(value = "配置的规则类型")
	private java.lang.String ruleTypes;

	@Excel(name = "是否自动重跑", width = 15)
	@ApiModelProperty(value = "是否自动重跑")
	private java.lang.String autoRerun;

	@Excel(name = "是否医保基金支付金额去除0", width = 15)
	@ApiModelProperty(value = "是否医保基金支付金额去除0")
	private java.lang.String ybFundRm0;
	
	@ApiModelProperty(value = "计算结果同步hive状态")
	private java.lang.String workflowState;
	
	@ApiModelProperty(value = "计算结果同步hive备注")
	private java.lang.String workflowRemark;
}
