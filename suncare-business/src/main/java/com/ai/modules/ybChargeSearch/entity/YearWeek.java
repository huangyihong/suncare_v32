package com.ai.modules.ybChargeSearch.entity;

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
 * @Description: 年度周表
 * @Author: jeecg-boot
 * @Date:   2022-12-05
 * @Version: V1.0
 */
@Data
@TableName("yb_year_week")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="yb_year_week对象", description="年度周表")
public class YearWeek {

	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "id")
	private java.lang.String id;
	/**周结束时间*/
	@Excel(name = "周结束时间", width = 15)
    @ApiModelProperty(value = "周结束时间")
	private java.lang.String weekEnd;
	/**周数*/
	@Excel(name = "周数", width = 15)
    @ApiModelProperty(value = "周数")
	private java.lang.Integer weekNum;
	/**周开始时间*/
	@Excel(name = "周开始时间", width = 15)
    @ApiModelProperty(value = "周开始时间")
	private java.lang.String weekStart;
	/**年份*/
	@Excel(name = "年份", width = 15)
    @ApiModelProperty(value = "年份")
	private java.lang.Integer year;
}
