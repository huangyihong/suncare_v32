package com.ai.modules.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: 各地不同医院级别报销比例
 * @Author: jeecg-boot
 * @Date:   2020-11-16
 * @Version: V1.0
 */
@Data
@TableName("STD_HOSLEVEL_FUNDPAYPROP")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="std_hoslevel_fundpayprop对象", description="各地不同医院级别报销比例")
public class StdHoslevelFundpayprop {

	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**项目地名称*/
	@Excel(name = "项目地名称", width = 15)
    @ApiModelProperty(value = "项目地名称")
	private java.lang.String project;
	/**医院级别编码*/
	@Excel(name = "医院级别编码", width = 15)
    @ApiModelProperty(value = "医院级别编码")
	private java.lang.String hosplevel;
	/**医院级别名称*/
	@Excel(name = "医院级别名称", width = 15)
    @ApiModelProperty(value = "医院级别名称")
	private java.lang.String hosplevelName;
	/**报销比例*/
	@Excel(name = "报销比例", width = 15)
    @ApiModelProperty(value = "报销比例")
	private java.lang.Long fundpayprop;
	/**适用开始时间*/
	@Excel(name = "适用开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "适用开始时间")
	private java.util.Date startdate;
	/**适用截止时间*/
	@Excel(name = "适用截止时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "适用截止时间")
	private java.util.Date enddate;
	/**新增人*/
	@Excel(name = "新增人", width = 15)
    @ApiModelProperty(value = "新增人")
	private java.lang.String createUser;
	/**新增人姓名*/
	@Excel(name = "新增人姓名", width = 15)
    @ApiModelProperty(value = "新增人姓名")
	private java.lang.String createUsername;
	/**新增时间*/
	@Excel(name = "新增时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "新增时间")
	private java.util.Date createTime;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
	private java.lang.String updateUser;
	/**修改人姓名*/
	@Excel(name = "修改人姓名", width = 15)
    @ApiModelProperty(value = "修改人姓名")
	private java.lang.String updateUsername;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
}
