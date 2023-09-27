/**
 * ReportPivotField.java	  V1.0   2019年4月11日 下午2:24:29
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import java.math.BigDecimal;

/**
 * 
 * 功能描述：指标轴数据对象
 *
 * @author  zhangly
 * Date: 2019年4月11日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class ReportFacetBucketField {
	/**指标名称*/
	private String field;
	/**指标值*/
	private BigDecimal value;
	
	public ReportFacetBucketField(String field, BigDecimal value) {
		this.field = field;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "ReportFacetBucketField [field=" + field + ", value=" + value + "]";
	}

	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
}
