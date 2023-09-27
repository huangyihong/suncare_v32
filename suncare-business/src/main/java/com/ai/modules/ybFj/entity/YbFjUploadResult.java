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
 * @Description: 生成文件信息
 * @Author: jeecg-boot
 * @Date:   2023-02-07
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_upload_result")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_upload_result对象", description="生成文件信息")
public class YbFjUploadResult {

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
	/**生成文件类型编码*/
	@Excel(name = "生成文件类型编码", width = 15)
    @ApiModelProperty(value = "生成文件类型编码")
	@MedicalDict(dicCode = "FJ_FILE_TYPE")
	private java.lang.String exportType;
	/**生成文件完整路径*/
	@Excel(name = "生成文件完整路径", width = 15)
    @ApiModelProperty(value = "生成文件完整路径")
	private java.lang.String fileFullpath;
	/**生成文件名称*/
	@Excel(name = "生成文件名称", width = 15)
    @ApiModelProperty(value = "生成文件名称")
	private java.lang.String fileName;
	/**生成文件路径*/
	@Excel(name = "生成文件路径", width = 15)
    @ApiModelProperty(value = "生成文件路径")
	private java.lang.String filePath;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**任务结果说明*/
	@Excel(name = "任务结果说明", width = 15)
    @ApiModelProperty(value = "任务结果说明")
	private java.lang.String taskDesc;
	/**任务状态 0=未识别 ;1=正在进行；2=识别成功；3=识别异常; 4=等待识别*/
	@Excel(name = "任务状态 0=未识别 ;1=正在进行；2=识别成功；3=识别异常; 4=等待识别", width = 15)
    @ApiModelProperty(value = "任务状态 0=未识别 ;1=正在进行；2=识别成功；3=识别异常; 4=等待识别")
	private java.lang.String taskStatus;
	/**导出模板code*/
	@Excel(name = "导出模板codes", width = 15)
	@ApiModelProperty(value = "导出模板codes")
	@Dict(dicCode = "template_code",dicText="template_name",dictTable="(select * from yb_fj_template_export where use_status='1') t ")
	private java.lang.Integer templateCode;
	/**导入模板id*/
	@Excel(name = "导入模板id", width = 15)
    @ApiModelProperty(value = "导入模板id")
	@Dict(dicCode = "id",dicText="import_name",dictTable="yb_fj_template_import")
	private java.lang.String templateImportId;
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
	/**上传文件id*/
	@Excel(name = "上传文件id", width = 15)
    @ApiModelProperty(value = "上传文件id")
	private java.lang.String uploadId;
}
