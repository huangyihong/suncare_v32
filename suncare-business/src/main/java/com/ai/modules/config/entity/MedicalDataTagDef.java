package com.ai.modules.config.entity;

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
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 标签管理
 * @Author: jeecg-boot
 * @Date:   2021-11-04
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_DATA_TAG_DEF")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_DATA_TAG_DEF对象", description="标签管理")
public class MedicalDataTagDef {
    
	/**唯一ID*/
	@Excel(name = "唯一ID", width = 15)
	@TableId("TAG_ID")
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String tagId;
	/**标签定义所属表*/
	@Excel(name = "标签定义所属表", width = 15)
    @ApiModelProperty(value = "标签定义所属表")
	@MedicalDict(dicCode="DATA_TAG_OWN_TABLE")
	private java.lang.String ownTableName;
	/**标签英文名称*/
	@Excel(name = "标签英文名称", width = 15)
    @ApiModelProperty(value = "标签英文名称")
	private java.lang.String tagName;
	/**标签中文名称*/
	@Excel(name = "标签中文名称", width = 15)
    @ApiModelProperty(value = "标签中文名称")
	private java.lang.String tagChnName;
	/**标签详细说明*/
	@Excel(name = "标签详细说明", width = 15)
    @ApiModelProperty(value = "标签详细说明")
	private java.lang.String tagDesc;
	/**标签类型（质控/业务）*/
	@Excel(name = "标签类型（质控/业务）", width = 15)
    @ApiModelProperty(value = "标签类型（质控/业务）")
	@MedicalDict(dicCode="DATA_TAG_TYPE")
	private java.lang.String tagType;
	
	/**标签状态  0-待处理  1-可用*/
	@Excel(name = "标签状态  0-待处理  1-可用", width = 15)
    @ApiModelProperty(value = "标签状态  0-待处理  1-可用")
	@MedicalDict(dicCode="DATA_TAG_STATUS")
	private java.lang.String tagState;
	
	/**新增人*/
	@Excel(name = "新增人", width = 15)
    @ApiModelProperty(value = "新增人")
	private java.lang.String createUser;
	/**新增人姓名*/
	@Excel(name = "新增人姓名", width = 15)
    @ApiModelProperty(value = "新增人姓名")
	private java.lang.String createUsername;
	/**新增时间*/
	@Excel(name = "新增时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
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
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
}
