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
 * @Description: 标签结果汇总
 * @Author: jeecg-boot
 * @Date:   2023-03-29
 * @Version: V1.0
 */
@Data
@TableName("yb_charge_monitor_datamining_stat")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_charge_monitor_datamining_stat对象", description="标签结果汇总")
public class YbChargeMonitorDataminingStat {

	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**关联的任务表主键*/
	@Excel(name = "关联的任务表主键", width = 15)
    @ApiModelProperty(value = "关联的任务表主键")
	private java.lang.String taskId;
	/**表名*/
	@Excel(name = "表名", width = 15)
    @ApiModelProperty(value = "表名")
	private java.lang.String tableName;
	/**标签名称*/
	@Excel(name = "标签名称", width = 15)
    @ApiModelProperty(value = "标签名称")
	private java.lang.String tagName;
	/**标签数量*/
	@Excel(name = "标签数量", width = 15)
    @ApiModelProperty(value = "标签数量")
	private java.lang.Integer count;
	/**标签时间*/
	@Excel(name = "标签时间", width = 15)
    @ApiModelProperty(value = "标签时间")
	private java.lang.String tagTime;
	/**项目地*/
	@Excel(name = "项目地", width = 15)
    @ApiModelProperty(value = "项目地")
	private java.lang.String project;
	/**数据来源*/
	@Excel(name = "数据来源", width = 15)
    @ApiModelProperty(value = "数据来源")
	private java.lang.String etlSource;
	/**标签类型*/
	@Excel(name = "标签类型", width = 15)
	@ApiModelProperty(value = "标签类型")
	private java.lang.String tagTypeName;
}
