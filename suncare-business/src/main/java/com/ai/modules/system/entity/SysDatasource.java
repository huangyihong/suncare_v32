package com.ai.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @Description: 项目地配置
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Data
@TableName("sys_datasource")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="sys_datasource对象", description="项目地配置")
public class SysDatasource {

	/**项目地编码*/
	@Excel(name = "项目地编码", width = 15)
    @ApiModelProperty(value = "项目地编码")
	private java.lang.String code;
	/**数仓项目*/
	@Excel(name = "数仓项目", width = 15)
    @ApiModelProperty(value = "数仓项目")
	private java.lang.String dataProject;
	/**数仓版本*/
	@Excel(name = "数仓版本", width = 15)
    @ApiModelProperty(value = "数仓版本")
	private java.lang.String dataVersion;
	/**数据源*/
	@Excel(name = "数据源", width = 15)
    @ApiModelProperty(value = "数据源")
	private java.lang.String databaseSource;
	/**数据来源*/
	@Excel(name = "数据来源", width = 15)
    @ApiModelProperty(value = "数据来源")
	@MedicalDict(dicCode = "ETL_SOURCE")
	private java.lang.String etlSource;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private java.lang.String id;
	/**项目地名称*/
	@Excel(name = "项目地名称", width = 15)
    @ApiModelProperty(value = "项目地名称")
	private java.lang.String name;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**solr数据源*/
	@Excel(name = "solr数据源", width = 15)
    @ApiModelProperty(value = "solr数据源")
	private java.lang.String solrAddr;
	/**所属系统*/
	@Excel(name = "所属系统", width = 15)
	@ApiModelProperty(value = "所属系统")
	@MedicalDict(dicCode = "SYSTEM_CODE")
	private java.lang.String systemCode;
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

	/**项目后端地址*/
	@Excel(name = "项目后端地址", width = 15)
	@ApiModelProperty(value = "项目后端地址")
	private java.lang.String suncareV3Url;

	/**报告后端地址*/
	@Excel(name = "报告后端地址", width = 15)
	@ApiModelProperty(value = "报告后端地址")
	private java.lang.String riskportalSolrUrl;

	/**数据库编码*/
	@Excel(name = "数据库编码", width = 15)
	@ApiModelProperty(value = "数据库编码")
	@TableField(exist = false)
	private java.lang.String dbname;

	/**数据库中文名称*/
	@Excel(name = "数据库中文名称", width = 15)
	@ApiModelProperty(value = "数据库中文名称")
	@TableField(exist = false)
	private java.lang.String cnname;
}
