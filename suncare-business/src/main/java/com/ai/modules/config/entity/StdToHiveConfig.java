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
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 基础数据同步到HIVE的配置文件
 * @Author: jeecg-boot
 * @Date:   2021-01-05
 * @Version: V1.0
 */
@Data
@TableName("STD_TO_HIVE_CONFIG")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="STD_TO_HIVE_CONFIG对象", description="基础数据同步到HIVE的配置文件")
public class StdToHiveConfig {
    
	/**源表表名*/
	@Excel(name = "源表表名", width = 15)
    @ApiModelProperty(value = "源表表名")
	private java.lang.String stableName;
	/**源表数据源*/
	@Excel(name = "源表数据源", width = 15)
    @ApiModelProperty(value = "源表数据源")
	private java.lang.String sdbname;
	/**目标表表名*/
	@Excel(name = "目标表表名", width = 15)
    @ApiModelProperty(value = "目标表表名")
	private java.lang.String ttableName;
	/**目标表数据源*/
	@Excel(name = "目标表数据源", width = 15)
    @ApiModelProperty(value = "目标表数据源")
	private java.lang.String tdbname;
	/**目标临时表*/
	@Excel(name = "目标临时表", width = 15)
    @ApiModelProperty(value = "目标临时表")
	private java.lang.String tmpTtableName;
	/**sqlStr*/
	@Excel(name = "sqlStr", width = 15)
    @ApiModelProperty(value = "sqlStr")
	private java.lang.String sqlStr;
	/**同步状态(0不需要1需要同步)*/
	@Excel(name = "同步状态(0不需要1需要同步)", width = 15)
    @ApiModelProperty(value = "同步状态(0不需要1需要同步)")
	private java.lang.String syncState;
	/**表中文名称*/
	@Excel(name = "表中文名称", width = 15)
    @ApiModelProperty(value = "表中文名称")
	private java.lang.String cname;
}
