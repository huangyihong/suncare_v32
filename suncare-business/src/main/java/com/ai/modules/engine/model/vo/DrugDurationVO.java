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

public class DrugDurationVO extends ChargeVO {
	//超过用药时限日期
	private Set<String> days;
	//原始项目名称集合
	private Set<String> itemnameSrcSet;
	
	public void addDay(String day) {
		if(days==null) {
			days = new HashSet<String>();
		}
		days.add(day);
	}
	
	public void addItemnameSrc(String itemnameSrc) {
		if(itemnameSrcSet==null) {
			itemnameSrcSet = new HashSet<String>();
		}
		if(StringUtils.isNotBlank(itemnameSrc)) {
			itemnameSrcSet.add(itemnameSrc);
		}
	}

	public Set<String> getDays() {
		return days;
	}

	public void setDays(Set<String> days) {
		this.days = days;
	}

	public Set<String> getItemnameSrcSet() {
		return itemnameSrcSet;
	}

	public void setItemnameSrcSet(Set<String> itemnameSrcSet) {
		this.itemnameSrcSet = itemnameSrcSet;
	}
}
