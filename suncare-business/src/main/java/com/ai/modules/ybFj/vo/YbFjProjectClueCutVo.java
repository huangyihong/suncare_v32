package com.ai.modules.ybFj.vo;

import com.ai.modules.ybFj.entity.YbFjProjectClue;
import io.swagger.annotations.ApiModelProperty;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @author : zhangly
 * @date : 2023/3/27 16:54
 */
public class YbFjProjectClueCutVo extends YbFjProjectClue {

    /**核减数量*/
    @Excel(name = "核减数量", width = 15)
    @ApiModelProperty(value = "核减数量")
    private Integer cutAmount;
    /**核减金额（单位：元）*/
    @Excel(name = "核减金额（单位：元）", width = 15)
    @ApiModelProperty(value = "核减金额（单位：元）")
    private java.math.BigDecimal cutFee;
}
