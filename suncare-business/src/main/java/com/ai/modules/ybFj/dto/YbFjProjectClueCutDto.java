package com.ai.modules.ybFj.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 飞检项目线索核减
 * @Author: jeecg-boot
 * @Date:   2023-03-14
 * @Version: V1.0
 */
@Data
public class YbFjProjectClueCutDto {
    
	/**唯一ID*/
	@Excel(name = "唯一ID", width = 15)
    @ApiModelProperty(value = "唯一ID")
	private String clueId;
	/**核减数量*/
	@Excel(name = "核减数量", width = 15)
    @ApiModelProperty(value = "核减数量")
	private Integer cutAmount;
	/**核减人次*/
	@Excel(name = "核减人次", width = 15)
	@ApiModelProperty(value = "核减人次")
	private Integer cutPersonCnt;
	/**核减金额（单位：元）*/
	@Excel(name = "核减金额（单位：元）", width = 15)
    @ApiModelProperty(value = "核减金额（单位：元）")
	private java.math.BigDecimal cutFee;
	/**核减医保基金金额（单位：元）*/
	@Excel(name = "核减医保基金金额（单位：元）", width = 15)
    @ApiModelProperty(value = "核减医保基金金额（单位：元）")
	private java.math.BigDecimal cutFundFee;
}
