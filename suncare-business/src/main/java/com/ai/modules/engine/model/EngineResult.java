/**
 * EngineResult.java	  V1.0   2019年12月30日 下午12:54:51
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class EngineResult {
	protected boolean success;
	protected String message;
	//记录数
	private Integer count = 0;
	//违规主体数
	private Integer objectCount = 0;
	//总金额
	private BigDecimal money = BigDecimal.ZERO;
	//违规金额
	private BigDecimal actionMoney = BigDecimal.ZERO;
	
	public EngineResult() {
		
	}
	
	public EngineResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	
	public static EngineResult ok() {
		EngineResult result = new EngineResult(true, "success");
		return result;
	}
	
	public static EngineResult error(String message) {
		EngineResult result = new EngineResult(false, message);
		return result;
	}
	
	public void merge(EngineResult bean) {
		count = count + bean.getCount();
		money = money.add(bean.getMoney());
		actionMoney = actionMoney.add(bean.getActionMoney());
	}
}
