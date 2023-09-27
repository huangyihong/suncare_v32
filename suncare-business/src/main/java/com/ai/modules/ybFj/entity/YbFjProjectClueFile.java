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
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 飞检项目线索附件
 * @Author: jeecg-boot
 * @Date:   2023-03-08
 * @Version: V1.0
 */
@Data
@TableName("yb_fj_project_clue_file")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_fj_project_clue_file对象", description="飞检项目线索附件")
public class YbFjProjectClueFile {
    
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
	@Excel(name = "唯一ID", width = 15)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String fileId;
	/**关联的飞检项目主键*/
	@Excel(name = "关联的飞检项目主键", width = 15)
    @ApiModelProperty(value = "关联的飞检项目主键")
	private java.lang.String projectId;
	/**关联yb_fj_project_org.project_org_id*/
	@Excel(name = "关联yb_fj_project_org.project_org_id", width = 15)
    @ApiModelProperty(value = "关联yb_fj_project_org.project_org_id")
	private java.lang.String projectOrgId;
	/**关联yb_fj_project_clue.clue_id*/
	@Excel(name = "关联yb_fj_project_clue.clue_id", width = 15)
    @ApiModelProperty(value = "关联yb_fj_project_clue.clue_id")
	private java.lang.String clueId;
	/**所属分类*/
	@Excel(name = "所属分类", width = 15)
    @ApiModelProperty(value = "所属分类")
	private java.lang.String stepType;
	/**所属环节*/
	@Excel(name = "所属环节", width = 15)
	@ApiModelProperty(value = "所属环节")
	private java.lang.String stepGroup;
	/**操作方式{up:上传,op:输出}*/
	@Excel(name = "操作方式{up:上传,op:输出}", width = 15)
    @ApiModelProperty(value = "操作方式{up:上传,op:输出}")
	private java.lang.String operType;
	/**文件类型*/
	@Excel(name = "文件类型", width = 15)
    @ApiModelProperty(value = "文件类型")
	private java.lang.String fileType;
	/**文件大小*/
	@Excel(name = "文件大小", width = 15)
    @ApiModelProperty(value = "文件大小")
	private Long fileSize;
	/**文件原名*/
	@Excel(name = "文件原名", width = 15)
    @ApiModelProperty(value = "文件原名")
	private java.lang.String fileSrcname;
	/**文件名*/
	@Excel(name = "文件名", width = 15)
    @ApiModelProperty(value = "文件名")
	private java.lang.String fileName;
	/**存储路径*/
	@Excel(name = "存储路径", width = 15)
    @ApiModelProperty(value = "存储路径")
	private java.lang.String filePath;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建人姓名*/
	@Excel(name = "创建人姓名", width = 15)
    @ApiModelProperty(value = "创建人姓名")
	private java.lang.String createUsername;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createUser;
}
