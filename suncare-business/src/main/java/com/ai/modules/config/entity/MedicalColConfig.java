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
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 表字段配置
 * @Author: jeecg-boot
 * @Date:   2019-12-06
 * @Version: V1.0
 */
@Data
@TableName("MEDICAL_COL_CONFIG")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MEDICAL_COL_CONFIG对象", description="表字段配置")
public class MedicalColConfig {

	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "主键")
	private java.lang.String id;
	/**字段名或指标别名*/
	@Excel(name = "字段名或指标别名", width = 15)
    @ApiModelProperty(value = "字段名或指标别名")
	private java.lang.String colName;
	/**归属表名*/
	@Excel(name = "归属表名", width = 15)
    @ApiModelProperty(value = "归属表名")
	private java.lang.String tabName;
	/**指标或字段中文名*/
	@Excel(name = "指标或字段中文名", width = 15)
    @ApiModelProperty(value = "指标或字段中文名")
	private java.lang.String colChnName;
	/**指标详细描述*/
	@Excel(name = "指标详细描述", width = 15)
    @ApiModelProperty(value = "指标详细描述")
	private java.lang.String colDesc;
	/**字段类型，标识是字段还是指标 1 字段 2 指标*/
	@Excel(name = "字段类型，标识是字段还是指标 1 字段 2 指标", width = 15)
    @ApiModelProperty(value = "字段类型，标识是字段还是指标 1 字段 2 指标")
	@Dict(dicCode = "colType")
	private java.lang.Integer colType;
	/**指标取值表达式（只针对指标）(having 区域)*/
	@Excel(name = "指标取值表达式（只针对指标）(having 区域)", width = 15)
    @ApiModelProperty(value = "指标取值表达式（只针对指标）(having 区域)")
	private java.lang.String colValueExpression;
	/**是否做条件过滤字段（只针对字段）1：是；其它值不是*/
	@Excel(name = "是否做条件过滤字段（只针对字段）1：是；其它值不是", width = 15)
    @ApiModelProperty(value = "是否做条件过滤字段（只针对字段）1：是；其它值不是")
	private java.lang.Integer isWhereCol;
	/**是否可为数据分组字段（只针对字段）1：是；其它值不是*/
	@Excel(name = "是否可为数据分组字段（只针对字段）1：是；其它值不是", width = 15)
    @ApiModelProperty(value = "是否可为数据分组字段（只针对字段）1：是；其它值不是")
	private java.lang.Integer isGroupbyCol;
	/**是否做条件过滤字段（只针对字段）1：是；其它值不是*/
	@Excel(name = "是否做结果列表显示字段（只针对字段）1：是；其它值不是", width = 15)
	@ApiModelProperty(value = "否做结果列表显示字段（只针对字段）1：是；其它值不是")
	private java.lang.Integer isDisplayCol;
	/**做为过滤条件时的输入方式*/
	@Excel(name = "做为过滤条件时的输入方式", width = 15)
    @ApiModelProperty(value = "做为过滤条件时的输入方式")
	private java.lang.Integer whereInputType;
	/**下拉字段type*/
	@Excel(name = "下拉字段type", width = 15)
    @ApiModelProperty(value = "下拉字段type")
	private java.lang.String selectType;
	/**排序序号*/
	@Excel(name = "排序序号", width = 15)
    @ApiModelProperty(value = "排序序号")
	private java.lang.Double colOrder;
	/**是否可为详情页字段（只针对字段）1：是；其它值不是
*/
	@Excel(name = "是否可为详情页字段（只针对字段）1：是；其它值不是", width = 15)
    @ApiModelProperty(value = "是否可为详情页字段（只针对字段）1：是；其它值不是")
	private java.lang.Integer isDetailCol;

	@Excel(name = "是否是出院统计指标1：是；其它值不是", width = 15)
    @ApiModelProperty(value = "是否是出院统计指标1：是；其它值不是")
	private java.lang.Integer isLeaveHospCol;
	/**机构ID*/
	@Excel(name = "机构ID", width = 15)
    @ApiModelProperty(value = "机构ID")
	private java.lang.String orgId;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
	private java.lang.String createStaff;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
	private java.lang.String updateStaff;
	/**修改时间*/
	@Excel(name = "修改时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "修改时间")
	private java.util.Date updateTime;
	/**删除状态（0 正常 1 删除）*/
	@Excel(name = "删除状态（0 正常 1 删除）", width = 15)
    @ApiModelProperty(value = "删除状态（0 正常 1 删除）")
	private java.lang.String dataStatus;
	/**数据类型*/
	@Excel(name = "数据类型", width = 15)
    @ApiModelProperty(value = "数据类型")
	private java.lang.String dataType;
	/**格式化*/
	@Excel(name = "格式化", width = 15)
    @ApiModelProperty(value = "格式化")
	private java.lang.String format;
	/**该字段在SQL中的过滤表达式(where区域）*/
	@Excel(name = "该字段在SQL中的过滤表达式(where区域）", width = 15)
    @ApiModelProperty(value = "该字段在SQL中的过滤表达式(where区域）")
	private java.lang.String colValueExpression2;
	/**是否作为指标字段，为空表示不是；如果不为空表示适用的监控类型：1单病例，2病人监控，3医生监控，4科室监控，5医院监控，该字段允许多值*/
	@Excel(name = "是否作为指标字段，为空表示不是；如果不为空表示适用的监控类型：1单病例，2病人监控，3医生监控，4科室监控，5医院监控，该字段允许多值", width = 15)
    @ApiModelProperty(value = "是否作为指标字段，为空表示不是；如果不为空表示适用的监控类型：1单病例，2病人监控，3医生监控，4科室监控，5医院监控，该字段允许多值")
	private java.lang.String jklx;
	/**该字段在SOLR中的过滤表达式(where区域）*/
	@Excel(name = "该字段在SOLR中的过滤表达式(where区域）", width = 15)
    @ApiModelProperty(value = "该字段在SOLR中的过滤表达式(where区域）")
	private java.lang.String colValueExpressionSolr;
	/**该字段在SOLR中的计算阈值表达式(having 区域)*/
	@Excel(name = "该字段在SOLR中的计算阈值表达式(having 区域)", width = 15)
    @ApiModelProperty(value = "该字段在SOLR中的计算阈值表达式(having 区域)")
	private java.lang.String colValueExpression2Solr;
	/**字段值的类型*/
	@Excel(name = "字段值的类型", width = 15)
    @ApiModelProperty(value = "字段值的类型")
	private java.lang.String colValueType;
}
