/**
 * EchartsEntity.java	  V1.0   2019年4月9日 下午2:21:25
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EchartsSeriesEntity implements Serializable {
	/** 标题*/
	private String name;
	/** 图表数据*/
	private List<Long> data;

	public EchartsSeriesEntity() {
		data = new ArrayList<Long>();
	}

	@Override
	public String toString() {
		return "EchartsSeriesEntity [name=" + name + ", data=" + data + "]";
	}

	public EchartsSeriesEntity(String name) {
		this.name = name;
		data = new ArrayList<Long>();
	}

	public void add(List<Long> values) {
		data.addAll(values);
	}

	public void add(long value) {
		data.add(value);
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Long> getData() {
		return data;
	}
}
