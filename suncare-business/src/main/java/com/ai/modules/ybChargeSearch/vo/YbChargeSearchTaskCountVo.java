package com.ai.modules.ybChargeSearch.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;


@Data
public class YbChargeSearchTaskCountVo {

	/**周开始时间*/
	@Excel(name = "周开始时间", width = 15)
	@ApiModelProperty(value = "周开始时间")
	private String weekStart;

	/**周结束时间*/
	@Excel(name = "周结束时间", width = 15)
	@ApiModelProperty(value = "周结束时间")
	private String weekEnd;

	/**周数*/
	@Excel(name = "周数", width = 15)
	@ApiModelProperty(value = "周数")
	private int weekNum;

	/**项目地*/
	@Excel(name = "项目地", width = 15)
	@ApiModelProperty(value = "项目地")
	private String dataSource;

	/**统计数量*/
	@Excel(name = "统计数量", width = 15)
	@ApiModelProperty(value = "统计数量")
	private int dataNum;



}
