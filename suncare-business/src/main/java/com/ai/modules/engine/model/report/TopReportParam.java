/**
 * TopReportParam.java	  V1.0   2022年5月30日 下午4:59:30
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.model.report;

import java.util.ArrayList;
import java.util.List;

public class TopReportParam {
	/**查询条件*/
	private List<String> wheres = new ArrayList<String>();
	/**维度1限制条数*/
	private int limit;
	/**group by 参数*/
	private String groupBy;
	/**solr统计函数*/
	private String staFunction;
	/**排序字段*/
	private String sort;
	/**solr collection*/
	private String collection;

	public String[] whereSolrFq() {
		if(wheres==null){
			return null;
		}
		String fq[] = new String[this.wheres.size()];
		wheres.toArray(fq);
		return fq;

	}

	public List<String> getWheres() {
		return wheres;
	}
	public void setWheres(List<String> wheres) {
		this.wheres = wheres;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public String getGroupBy() {
		return groupBy;
	}
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	public String getStaFunction() {
		return staFunction;
	}
	public void setStaFunction(String staFunction) {
		this.staFunction = staFunction;
	}
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public String getCollection() {
		return collection;
	}
	public void setCollection(String collection) {
		this.collection = collection;
	}
}
