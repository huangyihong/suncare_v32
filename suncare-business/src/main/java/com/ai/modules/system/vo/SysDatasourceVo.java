package com.ai.modules.system.vo;

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

/**
 * @Description: 项目地配置
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Data
public class SysDatasourceVo {

	/**项目地编码*/
	@Excel(name = "项目地编码", width = 15)
    @ApiModelProperty(value = "项目地编码")
	private String code;
	/**数仓项目*/
	@Excel(name = "数仓项目", width = 15)
    @ApiModelProperty(value = "数仓项目")
	private String dataProject;
	/**数仓版本*/
	@Excel(name = "数仓版本", width = 15)
    @ApiModelProperty(value = "数仓版本")
	private String dataVersion;
	/**数据源*/
	@Excel(name = "数据源", width = 15)
    @ApiModelProperty(value = "数据源")
	private String databaseSource;
	/**数据来源*/
	@Excel(name = "数据来源", width = 15)
    @ApiModelProperty(value = "数据来源")
	@MedicalDict(dicCode = "ETL_SOURCE")
	private String etlSource;
	/**唯一ID*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "唯一ID")
	private String id;
	/**项目地名称*/
	@Excel(name = "项目地名称", width = 15)
    @ApiModelProperty(value = "项目地名称")
	private String name;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private String remark;
	/**solr数据源*/
	@Excel(name = "solr数据源", width = 15)
    @ApiModelProperty(value = "solr数据源")
	private String solrAddr;
	/**所属系统*/
	@Excel(name = "所属系统", width = 15)
	@ApiModelProperty(value = "所属系统")
	@MedicalDict(dicCode = "SYSTEM_CODE")
	private String systemCode;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
	@ApiModelProperty(value = "创建人")
	private String createdBy;
	/**创建人名称*/
	@Excel(name = "创建人名称", width = 15)
	@ApiModelProperty(value = "创建人名称")
	private String createdByName;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "创建时间")
	private java.util.Date createdTime;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
	@ApiModelProperty(value = "更新人")
	private String updatedBy;
	/**更新人名称*/
	@Excel(name = "更新人名称", width = 15)
	@ApiModelProperty(value = "更新人名称")
	private String updatedByName;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "更新时间")
	private java.util.Date updatedTime;

	/**项目后端地址*/
	@Excel(name = "项目后端地址", width = 15)
	@ApiModelProperty(value = "项目后端地址")
	private String suncareV3Url;

	/**报告后端地址*/
	@Excel(name = "报告后端地址", width = 15)
	@ApiModelProperty(value = "报告后端地址")
	private String riskportalSolrUrl;

	/**方向*/
	private String direction;

	/**查询关键字*/
	private String word;

}
