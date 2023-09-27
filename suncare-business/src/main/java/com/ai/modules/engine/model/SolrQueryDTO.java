/**
 * SolrQueryDTO.java	  V1.0   2020年9月29日 下午3:17:59
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery.SortClause;

import lombok.Data;

@Data
public class SolrQueryDTO {
	/**
	 * 查询条件
	 */
	private String[] fq;
	/**
	 * 查询字段
	 */
	private String[] fl;
	/**
	 * 排序字段
	 */
	private List<SortClause> sorts;
	/**
	 * 每页条数
	 */
	private int rows;
	
	public void addSortClause(SortClause sort) {
		if(sorts==null) {
			sorts = new ArrayList<SortClause>();
		}
		sorts.add(sort);
	}
}
