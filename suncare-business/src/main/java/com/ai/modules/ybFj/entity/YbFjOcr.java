package com.ai.modules.ybFj.entity;

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
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: OCR识别工具
 * @Author: jeecg-boot
 * @Date:   2023-03-03
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_ocr")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_ocr对象", description="OCR识别工具")
public class YbFjOcr {

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
	/**生成的结果文档*/
	@Excel(name = "生成的结果文档", width = 15)
    @ApiModelProperty(value = "生成的结果文档")
	private java.lang.String exportName;
	/**生成文档路径*/
	@Excel(name = "生成文档路径", width = 15)
    @ApiModelProperty(value = "生成文档路径")
	private java.lang.String exportPath;
	/**生成文档类型*/
	@Excel(name = "生成文档类型", width = 15)
    @ApiModelProperty(value = "生成文档类型")
	@MedicalDict(dicCode = "FILE_TYPE")
	private java.lang.String exportType;
	/**源图片*/
	@Excel(name = "源图片", width = 15)
    @ApiModelProperty(value = "源图片")
	private java.lang.String filePath;
	/**源图片名称*/
	@Excel(name = "源图片名称", width = 15)
	@ApiModelProperty(value = "源图片名称")
	private java.lang.String fileName;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**任务结果说明*/
	@Excel(name = "任务结果说明", width = 15)
    @ApiModelProperty(value = "任务结果说明")
	private java.lang.String taskDesc;
	/**任务名称*/
	@Excel(name = "任务名称", width = 15)
    @ApiModelProperty(value = "任务名称")
	private java.lang.String taskName;
	/**任务状态 0=未识别 ;1=正在进行；2=识别成功；3=识别异常; 4=等待识别*/
	@Excel(name = "任务状态 0=未识别 ;1=正在进行；2=识别成功；3=识别异常; 4=等待识别", width = 15)
    @ApiModelProperty(value = "任务状态 0=未识别 ;1=正在进行；2=识别成功；3=识别异常; 4=等待识别")
	private java.lang.String taskStatus;
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
}
