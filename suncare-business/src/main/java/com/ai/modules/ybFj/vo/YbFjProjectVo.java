package com.ai.modules.ybFj.vo;

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

import java.util.Date;
import java.util.List;

/**
 * @Description: 飞检项目信息
 * @Author: jeecg-boot
 * @Date:   2023-02-01
 * @Version: V1.0
 */
@Data
public class YbFjProjectVo {

	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private String projectId;
	/**项目名称*/
	@Excel(name = "项目名称", width = 15)
    @ApiModelProperty(value = "项目名称")
	private String projectName;
	/**调查开始日期*/
	@Excel(name = "调查开始日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "调查开始日期")
	private Date surveyStartdate;
	/**调查结束日期*/
	@Excel(name = "调查结束日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "调查结束日期")
	private Date surveyEnddate;
	/**检查开始日期*/
	@Excel(name = "检查开始日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "检查开始日期")
	private Date checkStartdate;
	/**检查结束日期*/
	@Excel(name = "检查结束日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "检查结束日期")
	private Date checkEnddate;
	/**主办单位*/
	@Excel(name = "主办单位", width = 15)
    @ApiModelProperty(value = "主办单位")
	private String hostUnit;
	/**检查小组数量*/
	@Excel(name = "检查小组数量", width = 15)
    @ApiModelProperty(value = "检查小组数量")
	private Integer checkTeamNum;
	/**医疗机构编码范围*/
	@Excel(name = "医疗机构编码范围", width = 15)
    @ApiModelProperty(value = "医疗机构编码范围")
	private Object orgCodes;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "创建时间")
	private Date createTime;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
	@ApiModelProperty(value = "创建人姓名")
	private String createUsername;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
	@ApiModelProperty(value = "创建人")
	private String createUser;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "更新时间")
	private Date updateTime;
	/**更新人姓名*/
	@Excel(name = "更新人姓名", width = 15)
	@ApiModelProperty(value = "更新人姓名")
	private String updateUsername;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
	@ApiModelProperty(value = "更新人")
	private String updateUser;

	/**医疗机构编码列表*/
	@Excel(name = "医疗机构编码列表", width = 15)
	@ApiModelProperty(value = "医疗机构编码列表")
	private String dataOrgFilter;

	private List<String> codes;
}
