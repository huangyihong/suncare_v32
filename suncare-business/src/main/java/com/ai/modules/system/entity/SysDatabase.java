package com.ai.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 数据源配置
 * @Author: jeecg-boot
 * @Date:   2022-11-22
 * @Version: V1.0
 */
@Data
@TableName("sys_database")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="sys_database对象", description="数据源配置")
public class SysDatabase {

	/**数据库中文名称*/
	@Excel(name = "数据库中文名称", width = 15)
    @ApiModelProperty(value = "数据库中文名称")
	private java.lang.String cnname;
	/**项目地*/
	@Excel(name = "项目地", width = 15)
    @ApiModelProperty(value = "项目地")
	@MedicalDict(dicCode = "SOLR_DATA_SOURCE")
	private java.lang.String dataSource;
	/**数据库密码*/
	@Excel(name = "数据库密码", width = 15)
    @ApiModelProperty(value = "数据库密码")
	private java.lang.String dbPassword;
	/**数据库用户名*/
	@Excel(name = "数据库用户名", width = 15)
    @ApiModelProperty(value = "数据库用户名")
	private java.lang.String dbUser;
	/**数据库编码*/
	@Excel(name = "数据库编码", width = 15)
    @ApiModelProperty(value = "数据库编码")
	private java.lang.String dbname;
	/**数据库类型*/
	@Excel(name = "数据库类型", width = 15)
    @ApiModelProperty(value = "数据库类型")
	@MedicalDict(dicCode = "DB_TYPE")
	private java.lang.String dbtype;
	/**驱动名称*/
	@Excel(name = "驱动名称", width = 15)
    @ApiModelProperty(value = "驱动名称")
	private java.lang.String dbver;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**remark*/
	@Excel(name = "remark", width = 15)
    @ApiModelProperty(value = "remark")
	private java.lang.String remark;
	/**连接的URL*/
	@Excel(name = "连接的URL", width = 15)
    @ApiModelProperty(value = "连接的URL")
	private java.lang.String url;
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
