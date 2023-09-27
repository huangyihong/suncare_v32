package com.ai.modules.ybFj.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @author : zhangly
 * @date : 2023/3/17 16:47
 */
@Data
public class StatOnsiteClueVo extends StatProjectClueVo {
    /**核减数量*/
    @ApiModelProperty(value = "核减数量")
    private java.lang.Integer cutAmount;
    /**核减金额（单位：元）*/
    @ApiModelProperty(value = "核减金额（单位：元）")
   private java.math.BigDecimal cutFee;
}
