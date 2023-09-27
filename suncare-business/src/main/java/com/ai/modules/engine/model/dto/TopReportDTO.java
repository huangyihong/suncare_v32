/**
 * TopResultDTO.java	  V1.0   2022年5月31日 上午11:29:57
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.dto;

import java.util.ArrayList;
import java.util.List;

import com.ai.modules.engine.util.EngineUtil;

public class TopReportDTO {
	private String tableName = EngineUtil.DWB_CHARGE_DETAIL;
	//维度字段
	private String groupBy;
	//指标字段
	private String aggregate;
	//限制条数
	private int limit;
	//排序asc, desc
	private String sort = "desc";
	//过滤条件
	private List<String> whereList;
	
	public void addWhere(String where) {
		if(whereList==null) {
			whereList = new ArrayList<String>();
		}
		whereList.add(where);
	}
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getGroupBy() {
		return groupBy;
	}
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	public String getAggregate() {
		return aggregate;
	}
	public void setAggregate(String aggregate) {
		this.aggregate = aggregate;
	}
	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public List<String> getWhereList() {
		return whereList;
	}
	public void setWhereList(List<String> whereList) {
		this.whereList = whereList;
	}
}
