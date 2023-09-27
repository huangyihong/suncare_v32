package com.ai.modules.ybChargeSearch.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;


@Data
public class YbChargeSearchTaskFunCountVo {

	/**功能大类*/
	@Excel(name = "功能大类", width = 15)
	@ApiModelProperty(value = "功能大类")
	private String bigTitle;

	/**功能小类*/
	@Excel(name = "功能小类", width = 15)
	@ApiModelProperty(value = "功能小类")
	private String smallTitle;





}
