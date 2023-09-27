package com.ai.modules.ybFj.dto;

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

/**
 * @Description: 飞检项目信息
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
@Data
public class YbFjProjectDto {
	/**检查负责机构*/
	@Excel(name = "检查负责机构", width = 15)
	@ApiModelProperty(value = "检查负责机构")
	private java.lang.String actionOrg;
	/**检查负责人*/
	@Excel(name = "检查负责人", width = 15)
    @ApiModelProperty(value = "检查负责人")
	private String actionFzr;
	/**负责人联系方式*/
	@Excel(name = "负责人联系方式", width = 15)
    @ApiModelProperty(value = "负责人联系方式")
	private String actionFzrPhone;
	/**检查小组*/
	@Excel(name = "检查小组", width = 15)
    @ApiModelProperty(value = "检查小组")
	private String actionTeam;
	/**检查所属行动*/
	@Excel(name = "检查所属行动", width = 15)
    @ApiModelProperty(value = "检查所属行动")
	private String actionTitle;
	/**检查开始日期*/
	@Excel(name = "检查开始日期", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "检查开始日期，格式yyyy-MM-dd")
	private Date actionDate;
	/**数据结束日期*/
	@Excel(name = "数据结束日期", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "数据结束日期，格式yyyy-MM-dd")
	private Date checkEnddate;
	/**数据开始日期*/
	@Excel(name = "数据开始日期", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "数据开始日期，格式yyyy-MM-dd")
	private Date checkStartdate;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private String projectId;
	/**医疗机构编码范围*/
	@Excel(name = "医疗机构编码范围", width = 15)
    @ApiModelProperty(value = "医疗机构编码范围，多值逗号分割")
	private String orgIds;
	/**检查名称*/
	@Excel(name = "检查名称", width = 15)
    @ApiModelProperty(value = "检查名称")
	private String projectName;
	/**检查编码*/
	@Excel(name = "检查编码", width = 15)
    @ApiModelProperty(value = "检查编码")
	private String projectNo;
	/**紧急程度{low:低,medium:中,high:高}*/
	@Excel(name = "紧急程度{low:低,medium:中,high:高}", width = 15)
    @ApiModelProperty(value = "紧急程度{low:低,medium:中,high:高}，字典URGENT_LEVEL")
	private String urgentLevel;
}
