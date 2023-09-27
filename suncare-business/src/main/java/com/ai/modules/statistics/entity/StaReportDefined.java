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
 * @Description: 报表定义表
 * @Author: jeecg-boot
 * @Date:   2020-08-21
 * @Version: V1.0
 */
@Data
@TableName("STA_REPORT_DEFINED")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="STA_REPORT_DEFINED对象", description="报表定义表")
public class StaReportDefined {
    
	/**主键*/
	@Excel(name = "主键", width = 15)
    @ApiModelProperty(value = "主键")
	private java.lang.String reportId;
	/**报表名*/
	@Excel(name = "报表名", width = 15)
    @ApiModelProperty(value = "报表名")
	private java.lang.String reportName;
	/**第一维度*/
	@Excel(name = "第一维度", width = 15)
    @ApiModelProperty(value = "第一维度")
	private java.lang.String dim1;
	/**第二维度*/
	@Excel(name = "第二维度", width = 15)
    @ApiModelProperty(value = "第二维度")
	private java.lang.String dim2;
	/**第一维度返回条数限制*/
	@Excel(name = "第一维度返回条数限制", width = 15)
    @ApiModelProperty(value = "第一维度返回条数限制")
	private java.lang.Integer limitCnt1;
	/**第二维度返回条数限制*/
	@Excel(name = "第二维度返回条数限制", width = 15)
    @ApiModelProperty(value = "第二维度返回条数限制")
	private java.lang.Integer limitCnt2;
	/**统计函数，比如sum(JZZJE0)，不填默认取count作为统计值*/
	@Excel(name = "统计函数，比如sum(JZZJE0)，不填默认取count作为统计值", width = 15)
    @ApiModelProperty(value = "统计函数，比如sum(JZZJE0)，不填默认取count作为统计值")
	private java.lang.String staFunction;
	/**dateRangeType*/
	@Excel(name = "dateRangeType", width = 15)
    @ApiModelProperty(value = "dateRangeType")
	private java.lang.String dateRangeType;
	/**dateRangeBegin*/
	@Excel(name = "dateRangeBegin", width = 15)
    @ApiModelProperty(value = "dateRangeBegin")
	private java.lang.String dateRangeBegin;
	/**dateRangeEnd*/
	@Excel(name = "dateRangeEnd", width = 15)
    @ApiModelProperty(value = "dateRangeEnd")
	private java.lang.String dateRangeEnd;
	/**日期格式*/
	@Excel(name = "日期格式", width = 15)
    @ApiModelProperty(value = "日期格式")
	private java.lang.String dateRangeFmt;
	/**扩展参数*/
	@Excel(name = "扩展参数", width = 15)
    @ApiModelProperty(value = "扩展参数")
	private java.lang.String extParam;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createDate;
	/**分组*/
	@Excel(name = "分组", width = 15)
    @ApiModelProperty(value = "分组")
	private java.lang.String groupId;
	/**地址*/
	@Excel(name = "地址", width = 15)
    @ApiModelProperty(value = "地址")
	private java.lang.String reportUrl;
	/**默认查询参数*/
	@Excel(name = "默认查询参数", width = 15)
    @ApiModelProperty(value = "默认查询参数")
	private java.lang.String defQueryParam;
	/**状态*/
	@Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
	private java.lang.String status;
	/**默认图表*/
	@Excel(name = "默认图表", width = 15)
    @ApiModelProperty(value = "默认图表")
	private java.lang.String defChartType;
	/**是否按数量排行：1是，0否*/
	@Excel(name = "是否按数量排行：1是，0否", width = 15)
    @ApiModelProperty(value = "是否按数量排行：1是，0否")
	private java.lang.String sort;
	/**报表类型{D:日,W:周,M:月,Q:季度,Y:年}*/
	@Excel(name = "报表类型{D:日,W:周,M:月,Q:季度,Y:年}", width = 15)
    @ApiModelProperty(value = "报表类型{D:日,W:周,M:月,Q:季度,Y:年}")
	private java.lang.String reportType;
	/**报表细类{tongbi:同比,huanbi:环比,moving:移动平均,js:算术表达式计算}*/
	@Excel(name = "报表细类{tongbi:同比,huanbi:环比,moving:移动平均,js:算术表达式计算}", width = 15)
    @ApiModelProperty(value = "报表细类{tongbi:同比,huanbi:环比,moving:移动平均,js:算术表达式计算}")
	private java.lang.String reportSubtype;
	/**特殊报表的处理类名*/
	@Excel(name = "特殊报表的处理类名", width = 15)
    @ApiModelProperty(value = "特殊报表的处理类名")
	private java.lang.String customClazz;
	/**js算术表达式*/
	@Excel(name = "js算术表达式", width = 15)
    @ApiModelProperty(value = "js算术表达式")
	private java.lang.String jsCrithExpress;
	/**报表序号*/
	@Excel(name = "报表序号", width = 15)
    @ApiModelProperty(value = "报表序号")
	private java.lang.Integer orderSeq;
	/**solr collection*/
	@Excel(name = "solr collection", width = 15)
    @ApiModelProperty(value = "solr collection")
	private java.lang.String solrCollection;
	/**维度字典*/
	@Excel(name = "维度字典", width = 15)
    @ApiModelProperty(value = "维度字典")
	private java.lang.String dimDict;
}
