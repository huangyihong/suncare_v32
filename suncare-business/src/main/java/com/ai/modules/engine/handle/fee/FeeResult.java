/**
 * FeeResult.java	  V1.0   2021年1月5日 上午10:27:19
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.fee;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FeeResult {
	//违规金额
	private BigDecimal money = BigDecimal.ZERO;
	//可纳入计算违规基金支出金额
	protected BigDecimal actionMoney = BigDecimal.ZERO;
	//违规基金支出金额
	protected BigDecimal fundMoney = BigDecimal.ZERO;
	//项目频次/数量
	protected BigDecimal cnt;
	//超出频次/数量
	protected BigDecimal outCnt;
	//周期
	protected String duration;
}
