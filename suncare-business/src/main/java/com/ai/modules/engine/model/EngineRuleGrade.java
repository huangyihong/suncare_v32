/**
 * EngineRuleGrade.java	  V1.0   2020年5月9日 下午3:58:41
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import java.math.BigDecimal;

public class EngineRuleGrade {
	//评分指标字段
	private String fieldName;
	//基准值
	private BigDecimal standard;
	//权重
	private BigDecimal weight;
	//计算方式
	private String method;
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public BigDecimal getStandard() {
		return standard;
	}
	public void setStandard(BigDecimal standard) {
		this.standard = standard;
	}
	public BigDecimal getWeight() {
		return weight;
	}
	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
}
