/**
 * DrugDurationVO.java	  V1.0   2021年4月15日 下午5:33:20
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.vo;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class PayDurationVO extends ChargeVO {
	//原始项目名称集合
	private Set<String> itemnameSrcSet;
	//用药天数
	private int payDay;
	
	public void addItemnameSrc(String itemnameSrc) {
		if(itemnameSrcSet==null) {
			itemnameSrcSet = new HashSet<String>();
		}
		if(StringUtils.isNotBlank(itemnameSrc)) {
			itemnameSrcSet.add(itemnameSrc);
		}
	}
	
	public void addItemnameSrc(Set<String> set) {
		if(itemnameSrcSet==null) {
			itemnameSrcSet = new HashSet<String>();
		}
		if(set!=null) {
			itemnameSrcSet.addAll(set);
		}
	}

	public Set<String> getItemnameSrcSet() {
		return itemnameSrcSet;
	}

	public void setItemnameSrcSet(Set<String> itemnameSrcSet) {
		this.itemnameSrcSet = itemnameSrcSet;
	}

	public int getPayDay() {
		return payDay;
	}

	public void setPayDay(int payDay) {
		this.payDay = payDay;
	}
}
