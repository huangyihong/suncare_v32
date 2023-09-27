package com.ai.modules.ybFj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.MedicalDict;
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
@TableName("yb_fj_project")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_project对象", description="飞检项目信息")
public class YbFjProject {
	/**检查负责机构*/
	@Excel(name = "检查负责机构", width = 15)
	@ApiModelProperty(value = "检查负责机构")
	private java.lang.String actionOrg;
	/**检查负责人*/
	@Excel(name = "检查负责人", width = 15)
    @ApiModelProperty(value = "检查负责人")
	private java.lang.String actionFzr;
	/**负责人联系方式*/
	@Excel(name = "负责人联系方式", width = 15)
    @ApiModelProperty(value = "负责人联系方式")
	private java.lang.String actionFzrPhone;
	/**检查小组*/
	@Excel(name = "检查小组", width = 15)
    @ApiModelProperty(value = "检查小组")
	private java.lang.String actionTeam;
	/**检查所属行动*/
	@Excel(name = "检查所属行动", width = 15)
    @ApiModelProperty(value = "检查所属行动")
	private java.lang.String actionTitle;
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
    @ApiModelProperty(value = "数据结束日期")
	private java.util.Date checkEnddate;
	/**数据开始日期*/
	@Excel(name = "数据开始日期", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "数据开始日期")
	private java.util.Date checkStartdate;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
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
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String projectId;
	/**检查名称*/
	@Excel(name = "检查名称", width = 15)
    @ApiModelProperty(value = "检查名称")
	private java.lang.String projectName;
	/**检查编码*/
	@Excel(name = "检查编码", width = 15)
    @ApiModelProperty(value = "检查编码")
	private java.lang.String projectNo;
	/**状态{init:未归档,finish:已归档,close:关闭}*/
	@Excel(name = "状态{init:未归档,finish:已归档,close:关闭}", width = 15)
    @ApiModelProperty(value = "状态{init:未归档,finish:已归档,close:关闭}，字典")
	@MedicalDict(dicCode = "FJ_PROJECT_STATE")
	private java.lang.String projectState;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
	private java.util.Date updateTime;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
    @ApiModelProperty(value = "更新人")
	private java.lang.String updateUser;
	/**更新人姓名*/
	@Excel(name = "更新人姓名", width = 15)
    @ApiModelProperty(value = "更新人姓名")
	private java.lang.String updateUsername;
	/**紧急程度{low:低,medium:中,high:高}*/
	@Excel(name = "紧急程度{low:低,medium:中,high:高}", width = 15)
    @ApiModelProperty(value = "紧急程度{low:低,medium:中,high:高}，字典URGENT_LEVEL")
	@MedicalDict(dicCode = "URGENT_LEVEL")
	private java.lang.String urgentLevel;
}
