package com.ai.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 文件上传
 * @Author: jeecg-boot
 * @Date:   2019-12-18
 * @Version: V1.0
 */
@Data
@TableName("SYS_FILE")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="SYS_FILE对象", description="文件上传")
public class SysFile {

	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**文件名称*/
	@Excel(name = "文件名称", width = 15)
    @ApiModelProperty(value = "文件名称")
	private java.lang.String fileName;
	/**文件路径*/
	@Excel(name = "文件路径", width = 15)
    @ApiModelProperty(value = "文件路径")
	private java.lang.String filePath;
	/**关联表表名*/
	@Excel(name = "关联表表名", width = 15)
    @ApiModelProperty(value = "关联表表名")
	private java.lang.String relationalTable;
	/**关联表主键列名*/
	@Excel(name = "关联表主键列名", width = 15)
    @ApiModelProperty(value = "关联表主键列名")
	private java.lang.String relationalColumn;
	/**创建时间*/
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**创建者ID*/
	@Excel(name = "创建者ID", width = 15)
    @ApiModelProperty(value = "创建者ID")
	private java.lang.String createrId;
	/**创建者名称*/
	@Excel(name = "创建者名称", width = 15)
    @ApiModelProperty(value = "创建者名称")
	private java.lang.String createrName;
	/**文件大小*/
	@Excel(name = "文件大小", width = 15)
    @ApiModelProperty(value = "文件大小")
	private java.lang.String fileSize;
	/**MD5值*/
	@Excel(name = "MD5值", width = 15)
    @ApiModelProperty(value = "MD5值")
	private java.lang.String fileMd5;
	/**后缀名*/
	@Excel(name = "后缀名", width = 15)
    @ApiModelProperty(value = "后缀名")
	private java.lang.String fileExt;
	/**文件内容*/
	@Excel(name = "文件内容", width = 15)
    @ApiModelProperty(value = "文件内容")
	private byte[] fileContent;
	/**保存方式：0数据库，1本地磁盘*/
	@Excel(name = "保存方式：0数据库，1本地磁盘", width = 15)
    @ApiModelProperty(value = "保存方式：0数据库，1本地磁盘")
	private java.lang.String saveType;
	/**关联附件ID或关联文档ID*/
	@Excel(name = "关联附件ID或关联文档ID", width = 15)
    @ApiModelProperty(value = "关联附件ID或关联文档ID")
	private java.lang.String pid;
	/**排序号*/
	@Excel(name = "排序号", width = 15)
    @ApiModelProperty(value = "排序号")
	private java.lang.Integer orderNo;
}
