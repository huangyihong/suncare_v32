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
public class ChargeVO {
	protected String itemcode;
	protected String itemname;
	//数量
	protected BigDecimal amount = BigDecimal.ZERO;
	//费用
	protected BigDecimal fee = BigDecimal.ZERO;
	//基金支出金额
	protected BigDecimal fundConver = BigDecimal.ZERO;
	//处方日期
	protected String day;
	//原始项目名称
	protected String itemnameSrc;
	
	@Override
	public String toString() {
		return "ChargeVO [itemcode=" + itemcode + ", itemname=" + itemname + ", amount=" + amount + ", fee=" + fee
				+ ", fundConver=" + fundConver + "]";
	}	
}
