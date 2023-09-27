/**
 * EchartsEntity.java	  V1.0   2019年4月9日 下午2:21:25
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.report;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReportEchartsSeriesEntity implements Serializable {
	/** 标题*/
	private String name;
	/** 图表数据*/
	private List<BigDecimal> data;
	
	public ReportEchartsSeriesEntity() {
		
	}
	
	public ReportEchartsSeriesEntity(String name) {
		this.name = name;
		data = new ArrayList<BigDecimal>();
	}
	
	public void add(List<BigDecimal> values) {
		data.addAll(values);
	}
	
	public void add(BigDecimal value) {
		data.add(value);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<BigDecimal> getData() {
		return data;
	}
}
