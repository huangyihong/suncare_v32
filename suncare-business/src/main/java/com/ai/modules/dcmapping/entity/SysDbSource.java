package com.ai.modules.dcmapping.entity;

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
 * @Description: 数据库数据源
 * @Author: jeecg-boot
 * @Date:   2022-04-18
 * @Version: V1.0
 */
@Data
@TableName("SYS_DB_SOURCE")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="SYS_DB_SOURCE对象", description="数据库数据源")
public class SysDbSource {

	/**数据源名称*/
	@Excel(name = "数据源名称", width = 15)
    @ApiModelProperty(value = "数据源名称")
	@TableId(type = IdType.ID_WORKER_STR)
	private java.lang.String dbSourceName;
	/**数据库类型*/
	@Excel(name = "数据库类型", width = 15)
    @ApiModelProperty(value = "数据库类型")
	private java.lang.String dbType;
	/**数据库主机*/
	@Excel(name = "数据库主机", width = 15)
    @ApiModelProperty(value = "数据库主机")
	private java.lang.String dbHost;
	/**数据库端口*/
	@Excel(name = "数据库端口", width = 15)
    @ApiModelProperty(value = "数据库端口")
	private java.lang.Integer dbPort;
	/**数据库用户*/
	@Excel(name = "数据库用户", width = 15)
    @ApiModelProperty(value = "数据库用户")
	private java.lang.String dbUser;
	/**数据库密码*/
	@Excel(name = "数据库密码", width = 15)
    @ApiModelProperty(value = "数据库密码")
	private java.lang.String dbPassword;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String dbName;
	/**remark*/
	@Excel(name = "remark", width = 15)
    @ApiModelProperty(value = "remark")
	private java.lang.String remark;
}
