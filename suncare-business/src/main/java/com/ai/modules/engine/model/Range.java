/**
 * Range.java	  V1.0   2021年4月1日 下午2:32:29
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

public class Range implements Comparable<Range> {
	private int min;
	private int max;

	public Range(int min, int max) {
		assert min <= max;
		this.min = min;
		this.max = max;
	}

	public boolean includes(int hash) {
		return hash >= min && hash <= max;
	}

	public boolean isSubsetOf(Range superset) {
		return superset.min <= min && superset.max >= max;
	}

	public boolean overlaps(Range other) {
		return includes(other.min) || includes(other.max) || isSubsetOf(other);
	}

	@Override
	public String toString() {
		return Integer.toHexString(min) + '-' + Integer.toHexString(max);
	}

	@Override
	public int hashCode() {
		// difficult numbers to hash... only the highest bits will tend to differ.
		// ranges will only overlap during a split, so we can just hash the lower range.
		return (min >> 28) + (min >> 25) + (min >> 21) + min;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != getClass())
			return false;
		Range other = (Range) obj;
		return this.min == other.min && this.max == other.max;
	}

	@Override
	public int compareTo(Range that) {
		int mincomp = Integer.compare(this.min, that.min);
		return mincomp == 0 ? Integer.compare(this.max, that.max) : mincomp;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}
}
