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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class ChargedayVO {
	protected String itemcode;
	protected String itemname;
	//日期到天
	protected String day;
	//数量
	protected BigDecimal amount = BigDecimal.ZERO;
	//费用
	protected BigDecimal fee = BigDecimal.ZERO;
	//基金支出金额
	protected BigDecimal fundConver = BigDecimal.ZERO;
	//单价
	protected BigDecimal unitPrice;
	//报销比例
	protected BigDecimal ratio = BigDecimal.ZERO;
	
	protected String itemnameSrc;
	protected Set<String> itemnameSrcSet;
	
	@Override
	public String toString() {
		return "ChargedayVO [itemcode=" + itemcode + ", itemname=" + itemname + ", day=" + day + ", amount=" + amount
				+ ", fee=" + fee + ", fundConver=" + fundConver + "]";
	}
	
	private void addItemnameSrc(String itemnameSrc) {
		if(itemnameSrcSet==null) {
			itemnameSrcSet = new HashSet<String>();
		}
		if(StringUtils.isNotBlank(itemnameSrc)) {
			itemnameSrcSet.add(itemnameSrc);
		}
	}
	
	/**
	 * 
	 * 功能描述：同一天同一个项目、药品合并
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年6月17日 上午9:44:56</p>
	 *
	 * @param vo
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public void merge(ChargedayVO vo) {
		amount = amount.add(vo.getAmount());
		fee = fee.add(vo.getFee());
		fundConver = fundConver.add(vo.getFundConver());
		//计算项目的最高单价
		if(unitPrice==null || vo.getUnitPrice().compareTo(unitPrice)>0) {
			unitPrice = vo.getUnitPrice();
		}
		//最高报销比例
		if(ratio==null || vo.getRatio().compareTo(ratio)>0) {
			ratio = vo.getRatio();
		}
		this.addItemnameSrc(vo.getItemnameSrc());
	}

	public String getItemcode() {
		return itemcode;
	}

	public void setItemcode(String itemcode) {
		this.itemcode = itemcode;
	}

	public String getItemname() {
		return itemname;
	}

	public void setItemname(String itemname) {
		this.itemname = itemname;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public BigDecimal getFundConver() {
		return fundConver;
	}

	public void setFundConver(BigDecimal fundConver) {
		this.fundConver = fundConver;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public BigDecimal getRatio() {
		return ratio;
	}

	public void setRatio(BigDecimal ratio) {
		this.ratio = ratio;
	}

	public String getItemnameSrc() {
		return itemnameSrc;
	}

	public void setItemnameSrc(String itemnameSrc) {
		addItemnameSrc(itemnameSrc);
		this.itemnameSrc = itemnameSrc;
	}

	public Set<String> getItemnameSrcSet() {
		return itemnameSrcSet;
	}

	public void setItemnameSrcSet(Set<String> itemnameSrcSet) {
		this.itemnameSrcSet = itemnameSrcSet;
	}
}
