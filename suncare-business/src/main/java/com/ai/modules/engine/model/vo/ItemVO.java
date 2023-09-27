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
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;

@Data
public class ItemVO {
	//互斥项目编码
	private String itemcode;
	//互斥项目名称
	private String itemname;
	//主体项目数量
	private BigDecimal cnt = BigDecimal.ZERO;
	//主体项目费用
	protected BigDecimal fee = BigDecimal.ZERO;
	//主体项目基金支出金额
	protected BigDecimal fundCover = BigDecimal.ZERO;
	//主体项目单价
	protected BigDecimal unitPrice = null;
	//互斥数量
	private BigDecimal mutexCnt = BigDecimal.ZERO;
	//互斥项目费用
	protected BigDecimal mutexFee = BigDecimal.ZERO;
	//互斥项目基金支出金额
	protected BigDecimal mutexFundCover = BigDecimal.ZERO;
	//互斥发生日期
	private Set<String> daySet = new LinkedHashSet<String>();
	//互斥项目单价
	protected BigDecimal mutexPrice = null;
	
	public ItemVO(String itemcode, String itemname) {
		this.itemcode = itemcode;
		this.itemname = itemname;
	}
	
	/**
	 * 计算最小、最大违规基金支出金额
	 * 功能描述：
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月20日 下午3:39:17</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public BigDecimal[] computeFoundArray() {
		BigDecimal[] result = new BigDecimal[2];
		if(fundCover.compareTo(mutexFundCover)>0) {
			result[0] = mutexFundCover;
			result[1] = fundCover;
		} else {
			result[0] = fundCover;
			result[1] = mutexFundCover;
		}
		return result;
	}
	
	/**
	 * 计算最小、最大违规金额
	 * 功能描述：
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月20日 下午3:39:17</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public BigDecimal[] computeFeeArray() {
		BigDecimal[] result = new BigDecimal[2];
		if(fee.compareTo(mutexFee)>0) {
			result[0] = mutexFee;
			result[1] = fee;
		} else {
			result[0] = fee;
			result[1] = mutexFee;
		}
		return result;
	}
	
	public void addMutexCnt(BigDecimal cnt1, BigDecimal cnt2, String day) {
		this.cnt = cnt.add(cnt1);
		this.mutexCnt = mutexCnt.add(cnt2);
		this.daySet.add(day);
	}
	
	public void addMainMoney(BigDecimal itemFee, BigDecimal itemFundCover) {
		fee = fee.add(itemFee);
		fundCover = fundCover.add(itemFundCover);
	}
	
	public void addMutexMoney(BigDecimal itemFee, BigDecimal itemFundCover) {
		mutexFee = mutexFee.add(itemFee);
		mutexFundCover = mutexFundCover.add(itemFundCover);
	}
	
	public void computeMutexPrice(BigDecimal price) {
		if(mutexPrice==null || price.compareTo(mutexPrice)>0) {
			mutexPrice = price;
		}
	}
	
	public void computeUnitPrice(BigDecimal price) {
		if(unitPrice==null || price.compareTo(unitPrice)>0) {
			unitPrice = price;
		}
	}
		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itemcode == null) ? 0 : itemcode.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemVO other = (ItemVO) obj;
		if (itemcode == null) {
			if (other.itemcode != null)
				return false;
		} else if (!itemcode.equals(other.itemcode))
			return false;
		return true;
	}
}