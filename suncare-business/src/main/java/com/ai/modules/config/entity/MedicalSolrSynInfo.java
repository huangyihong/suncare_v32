package com.ai.modules.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: SOLR数据同步情况表
 * @Author: jeecg-boot
 * @Date:   2020-11-20
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_SOLR_SYN_INFO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_SOLR_SYN_INFO对象", description="SOLR数据同步情况表")
public class MedicalSolrSynInfo {

	/**表名*/
	@TableId(type = IdType.ID_WORKER_STR)
	@Excel(name = "表名", width = 15)
    @ApiModelProperty(value = "表名")
	private java.lang.String tableName;
	/**表中文名*/
	@Excel(name = "表中文名", width = 15)
    @ApiModelProperty(value = "表中文名")
	private java.lang.String tableCnName;
	/**HIVE更新时间*/
	@Excel(name = "HIVE更新时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "HIVE更新时间")
	private java.util.Date hiveUpdateTime;
	/**SOLR索引创建完毕时间*/
	@Excel(name = "SOLR索引创建完毕时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "SOLR索引创建完毕时间")
	private java.util.Date indexCreateTime;
	/**SOLR索引正式生效时间*/
	@Excel(name = "SOLR索引正式生效时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "SOLR索引正式生效时间")
	private java.util.Date solrUpdateTime;
	/**项目地*/
	@Excel(name = "项目地", width = 15)
    @ApiModelProperty(value = "项目地")
	private java.lang.String project;
	/**排序编号*/
	@Excel(name = "排序编号", width = 15)
    @ApiModelProperty(value = "排序编号")
	private java.lang.Integer sortOrder;
}
