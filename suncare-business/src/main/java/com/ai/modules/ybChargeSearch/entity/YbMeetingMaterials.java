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
 * @Description: 上会材料主表
 * @Author: jeecg-boot
 * @Date:   2022-11-29
 * @Version: V1.0
 */
@Data
@TableName("yb_meeting_materials")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_meeting_materials对象", description="上会材料主表")
public class YbMeetingMaterials {

	/**行动名称*/
	@Excel(name = "行动名称", width = 15)
    @ApiModelProperty(value = "行动名称")
	private java.lang.String actionname;
	/**文书字号*/
	@Excel(name = "文书字号", width = 15)
    @ApiModelProperty(value = "文书字号")
	private java.lang.String codename;
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
	/**行政处罚事先告知书下载地址*/
	@Excel(name = "行政处罚事先告知书下载地址", width = 15)
    @ApiModelProperty(value = "行政处罚事先告知书下载地址")
	private java.lang.String xzcftzsUrl;
	/**拟处理意见报告下载地址*/
	@Excel(name = "拟处理意见报告下载地址", width = 15)
	@ApiModelProperty(value = "拟处理意见报告下载地址")
	private java.lang.String nclyjbgUrl;
	/**检查小组*/
	@Excel(name = "检查小组", width = 15)
    @ApiModelProperty(value = "检查小组")
	private java.lang.String groupTeam;
	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**医院名称*/
	@Excel(name = "医院名称", width = 15)
    @ApiModelProperty(value = "医院名称")
	private java.lang.String orgname;
	/**调查开始时间*/
	@Excel(name = "调查开始时间", width = 20)
    @ApiModelProperty(value = "调查开始时间")
	private java.lang.String startdate;
	/**检查时间范围*/
	@Excel(name = "检查时间范围", width = 15)
    @ApiModelProperty(value = "检查时间范围")
	private java.lang.String timerange;
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
