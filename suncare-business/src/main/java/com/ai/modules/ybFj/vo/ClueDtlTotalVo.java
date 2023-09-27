package com.ai.modules.ybFj.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : zhangly
 * @date : 2023/6/14 11:18
 */
@Data
public class ClueDtlTotalVo {
    /**明细项目总数量*/
    private Integer totalSl;
    /**明细违规总金额*/
    private BigDecimal totalWgFee;

    public ClueDtlTotalVo(Integer totalSl, BigDecimal totalWgFee) {
        this.totalSl = totalSl;
        this.totalWgFee = totalWgFee;
    }
}
