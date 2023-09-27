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
 * @Description: 任务项目
 * @Author: jeecg-boot
 * @Date:   2020-01-03
 * @Version: V1.0
 */
@Data
@TableName("TASK_PROJECT")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TASK_PROJECT对象", description="任务项目")
public class TaskProject {

	/**项目ID*/
	@Excel(name = "项目ID", width = 15)
    @ApiModelProperty(value = "项目ID")
	@TableId("PROJECT_ID")
	private java.lang.String projectId;
	/**项目名称*/
	@Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
	private java.lang.String projectName;
	/**项目地*/
	@Excel(name = "项目地", width = 15)
	@ApiModelProperty(value = "项目地")
	private java.lang.String projectSite;
	/**项目地*/
	@Excel(name = "数据源（登录选择）", width = 15)
	@ApiModelProperty(value = "数据源（登录选择）")
	private java.lang.String dataSource;
	/**数据来源*/
	@Excel(name = "数据来源", width = 15)
    @ApiModelProperty(value = "数据来源")
	private java.lang.String etlSource;
	/**输出来源*/
	@Excel(name = "输出来源", width = 15)
	@ApiModelProperty(value = "输出来源")
	private java.lang.String outSource;
	/**项目开始时间*/
	@Excel(name = "项目开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "项目开始时间")
	private java.util.Date startTime;
	/**项目结束时间*/
	@Excel(name = "项目结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "项目结束时间")
	private java.util.Date endTime;
	/**项目进度维护*/
	@Excel(name = "项目进度维护", width = 15)
    @ApiModelProperty(value = "项目进度维护")
	private java.math.BigDecimal schedule;
	/**项目客户*/
	@Excel(name = "项目客户", width = 15)
    @ApiModelProperty(value = "项目客户")
	private java.lang.String customer;
	@Excel(name = "医院级别报销比例-1级", width = 15)
    @ApiModelProperty(value = "医院级别报销比例-1级")
	private Integer hospLv1PayRate;
	@Excel(name = "医院级别报销比例-2级", width = 15)
	@ApiModelProperty(value = "医院级别报销比例-2级")
	private Integer hospLv2PayRate;
	@Excel(name = "医院级别报销比例-3级", width = 15)
	@ApiModelProperty(value = "医院级别报销比例-3级")
	private Integer hospLv3PayRate;
	@Excel(name = "医院级别报销比例-未评级", width = 15)
	@ApiModelProperty(value = "医院级别报销比例-未评级")
	private Integer hospLv0PayRate;
	/**solr里COLLECTION名称*/
	@Excel(name = "solr里COLLECTION名称", width = 15)
    @ApiModelProperty(value = "solr里COLLECTION名称")
	private java.lang.String collection;
	/**createUserName*/
	@Excel(name = "createUserName", width = 15)
    @ApiModelProperty(value = "createUserName")
	private java.lang.String createUserName;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;

	/**医疗机构范围*/
	@Excel(name = "医疗机构范围", width = 15)
	@ApiModelProperty(value = "医疗机构范围")
	private java.lang.String dataOrgFilter;

	/**数据范围开始时间*/
	@Excel(name = "数据范围开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "数据范围开始时间")

	private java.util.Date dataStartTime;
	/**数据范围结束时间*/
	@Excel(name = "数据范围结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "数据范围结束时间")
	private java.util.Date dataEndTime;
}
