package com.ai.modules.ybFj.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : zhangly
 * @date : 2023/3/17 16:47
 */
@Data
public class StatProjectClueVo {
    /**涉及数量*/
    @ApiModelProperty(value = "涉及数量")
    private Integer caseAmount;
    /**涉及人次*/
    @ApiModelProperty(value = "涉及人次")
    private Integer casePersonCnt;
    /**涉及金额（单位：元）*/
    @ApiModelProperty(value = "涉及金额（单位：元）")
    private java.math.BigDecimal caseFee;
    /**涉及医保基金金额（单位：元）*/
    @ApiModelProperty(value = "涉及医保基金金额（单位：元）")
    private java.math.BigDecimal caseFundFee;
    /**问题类别*/
    @ApiModelProperty(value = "问题类别")
    private String issueType;
    /**问题类型*/
    @ApiModelProperty(value = "问题类型")
    private String issueSubtype;
}
