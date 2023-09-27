/**
 * ChargedetailVO.java	  V1.0   2020年9月14日 上午10:21:40
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ChargebxVO {
	protected String itemcode;
	protected String itemname;
	//日期到天
	protected String day;
	//数量
	protected BigDecimal amount = BigDecimal.ZERO;
	//费用
	protected BigDecimal fee = BigDecimal.ZERO;
	//报销比例
	protected BigDecimal ratio = BigDecimal.ONE;
	//单价
	protected BigDecimal unitPrice = BigDecimal.ZERO; 
	
	@Override
	public String toString() {
		return "ChargebxVO [itemcode=" + itemcode + ", itemname=" + itemname + ", day=" + day + ", amount=" + amount
				+ ", fee=" + fee + ", ratio=" + ratio + "]";
	}	
}
