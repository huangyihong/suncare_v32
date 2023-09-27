/**
 * JoinTableModel.java	  V1.0   2022年11月8日 下午4:35:31
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.hive.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class JoinTableModel {
	
	/**
	 * 关联dwb_master_info表查询条件
	 */
	private List<String> masterList;
	
	/**
	 * 关联其他表查询条件
	 */
	private List<WithTableModel> withTableList;
	
	public void addMasterWhere(String where) {
		if(masterList==null) {
			masterList = new ArrayList<String>();
		}
		masterList.add(where);
	}
	
	public void addWithTable(String sql) {
		if(withTableList==null) {
			withTableList = new ArrayList<WithTableModel>();
		}
		
		String alias = "table_filter_" + withTableList.size();
		WithTableModel withTable = new WithTableModel(alias, sql);
		withTableList.add(withTable);
	}
}
