package com.ai.modules.statistics.entity;

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
 * @Description: 报表查询条件定义表
 * @Author: jeecg-boot
 * @Date:   2020-08-21
 * @Version: V1.0
 */
@Data
@TableName("STA_REPORT_FORM_FIELD")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="STA_REPORT_FORM_FIELD对象", description="报表查询条件定义表")
public class StaReportFormField {
    
	/**主键*/
	@Excel(name = "主键", width = 15)
    @ApiModelProperty(value = "主键")
	private java.lang.String fieldId;
	/**报表ID*/
	@Excel(name = "报表ID", width = 15)
    @ApiModelProperty(value = "报表ID")
	private java.lang.String reportId;
	/**字段名*/
	@Excel(name = "字段名", width = 15)
    @ApiModelProperty(value = "字段名")
	private java.lang.String fieldName;
	/**字段中文名*/
	@Excel(name = "字段中文名", width = 15)
    @ApiModelProperty(value = "字段中文名")
	private java.lang.String fieldCnname;
	/**对应solr字段名*/
	@Excel(name = "对应solr字段名", width = 15)
    @ApiModelProperty(value = "对应solr字段名")
	private java.lang.String solrFieldName;
	/**字段类型{text:文本,dict:从字典获取选项,date:日期}*/
	@Excel(name = "字段类型{text:文本,dict:从字典获取选项,date:日期}", width = 15)
    @ApiModelProperty(value = "字段类型{text:文本,dict:从字典获取选项,date:日期}")
	private java.lang.String fieldType;
	/**数据格式化串，例如yyyy-MM-dd*/
	@Excel(name = "数据格式化串，例如yyyy-MM-dd", width = 15)
    @ApiModelProperty(value = "数据格式化串，例如yyyy-MM-dd")
	private java.lang.String fieldFmt;
	/**操作符大写，默认EQ，可选值：EQ GT GET LT LET LIKE*/
	@Excel(name = "操作符大写，默认EQ，可选值：EQ GT GET LT LET LIKE", width = 15)
    @ApiModelProperty(value = "操作符大写，默认EQ，可选值：EQ GT GET LT LET LIKE")
	private java.lang.String opType;
	/**数据类型String、BigDecimal、Date*/
	@Excel(name = "数据类型String、BigDecimal、Date", width = 15)
    @ApiModelProperty(value = "数据类型String、BigDecimal、Date")
	private java.lang.String dataType;
	/**specialType*/
	@Excel(name = "specialType", width = 15)
    @ApiModelProperty(value = "specialType")
	private java.lang.String specialType;
	/**是否可见，默认true*/
	@Excel(name = "是否可见，默认true", width = 15)
    @ApiModelProperty(value = "是否可见，默认true")
	private java.lang.String isVisible;
	/**是否必填，默认false*/
	@Excel(name = "是否必填，默认false", width = 15)
    @ApiModelProperty(value = "是否必填，默认false")
	private java.lang.String isRequired;
	/**宽度*/
	@Excel(name = "宽度", width = 15)
    @ApiModelProperty(value = "宽度")
	private java.lang.String width;
	/**对应字典的dict_id*/
	@Excel(name = "对应字典的dict_id", width = 15)
    @ApiModelProperty(value = "对应字典的dict_id")
	private java.lang.String dictId;
	/**排序*/
	@Excel(name = "排序", width = 15)
    @ApiModelProperty(value = "排序")
	private java.lang.Integer orderSeq;
}
