/**
 * ReportPivotFieldList.java	  V1.0   2019年4月11日 下午2:47:54
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.report;

import java.math.BigDecimal;
import java.util.List;

public class ReportFacetBucketFieldList {
	private String title;
	private BigDecimal total;
	private List<ReportFacetBucketField> buckets;
		
	public int size() {
		return buckets.size();
	}	
	
	@Override
	public String toString() {
		return "ReportFacetBucketFieldList [title=" + title + ", total=" + total + ", buckets=" + buckets + "]";
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public List<ReportFacetBucketField> getBuckets() {
		return buckets;
	}

	public void setBuckets(List<ReportFacetBucketField> buckets) {
		this.buckets = buckets;
	}	
}
