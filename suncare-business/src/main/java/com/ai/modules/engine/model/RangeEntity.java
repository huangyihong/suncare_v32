/**
 * RangeEntity.java	  V1.0   2019年12月3日 上午11:35:53
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.model;

public class RangeEntity {
	private double min;
	private double max;
	private String axis;

	@Override
	public String toString() {
		return "RangeEntity [min=" + min + ", max=" + max + ", axis=" + axis + "]";
	}
	public double getMin() {
		return min;
	}
	public void setMin(double min) {
		this.min = min;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	public String getAxis() {
		return axis;
	}
	public void setAxis(String axis) {
		this.axis = axis;
	}
}
