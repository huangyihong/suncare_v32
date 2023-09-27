package com.ai.modules.drg.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = false)
@Data
public class SrcYbSettlementVo {
    private String settlementid;

    private String clientid;

    private String clientname;

    private BigDecimal totalfee;

    private BigDecimal fundProp;

    private BigDecimal fundCover;

    private BigDecimal allfundPay;

    private BigDecimal selfpay;

    private BigDecimal refuseFee;

    private BigDecimal seriousillCover;

    private BigDecimal otherPay;
}
