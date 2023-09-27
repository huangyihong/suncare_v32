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
public class ChargedetailVO extends ChargedayVO {
	//收费日期到秒
	//private String chargedate;
	//处方日期
	private String prescripttime;

	@Override
	public String toString() {
		return "ChargedetailVO [itemcode=" + itemcode + ", itemname=" + itemname + ", prescripttime=" + prescripttime
				+ ", day=" + day + ", amount=" + amount + ", fee=" + fee + "]";
	}
	
	public ChargedetailVO sub(ChargedetailVO vo) {
		if(amount.compareTo(BigDecimal.ZERO)<=0) {
			return vo;
		}
		//剩余还未冲销的值
		BigDecimal remain = vo.getAmount();
		remain = remain.add(amount);
		BigDecimal remainFee = vo.getFee();
		remainFee = remainFee.add(fee);
		BigDecimal remainConver = vo.getFundConver();
		remainConver = remainConver.add(fundConver);
		if(amount.compareTo(vo.getAmount().abs())>0) {
			amount = amount.add(vo.getAmount());
			fee = fee.add(vo.getFee());
			fundConver = fundConver.add(vo.getFundConver());
		} else {
			amount = BigDecimal.ZERO;
			fee = BigDecimal.ZERO;
			fundConver = BigDecimal.ZERO;
		}	
		vo.setAmount(remain);
		vo.setFee(remainFee);
		vo.setFundConver(remainConver);
		return vo;
	}
}
