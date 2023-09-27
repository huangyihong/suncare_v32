package com.ai.modules.ybFj.vo;

import com.ai.modules.ybFj.entity.YbFjProjectClueFile;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.List;

/**
 * @author : zhangly
 * @date : 2023/3/13 9:30
 */
@Data
public class TaskClueVo {
    /**涉及数量*/
    @Excel(name = "涉及数量", width = 15)
    @ApiModelProperty(value = "涉及数量")
    private Integer caseAmount;
    /**涉及金额（单位：元）*/
    @Excel(name = "涉及金额（单位：元）", width = 15)
    @ApiModelProperty(value = "涉及金额（单位：元）")
    private java.math.BigDecimal caseFee;
    /**涉及医保基金金额（单位：元）*/
    @Excel(name = "涉及医保基金金额（单位：元）", width = 15)
    @ApiModelProperty(value = "涉及医保基金金额（单位：元）")
    private java.math.BigDecimal caseFundFee;

    private List<YbFjProjectClueFile> fileList;
}
