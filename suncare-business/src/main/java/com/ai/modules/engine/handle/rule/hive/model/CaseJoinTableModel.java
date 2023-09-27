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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class CaseJoinTableModel {
	
	/**
	 * 关联dwb_master_info等特殊模型主表查询条件
	 */
	private List<String> masterList;
	
	/**
	 * 关联其他表查询条件
	 */
	private Map<String, List<String>> joinTableMap;
	
	public void addMasterWhere(String where) {
		if(masterList==null) {
			masterList = new ArrayList<String>();
		}
		masterList.add(where);
	}
	
	public void addOtherWhere(String table, String where) {
		if(joinTableMap==null) {
			joinTableMap = new HashMap<String, List<String>>();
		}
		table = table.toUpperCase();
		List<String> wheres = joinTableMap.get(table);
		if(wheres==null) {
			wheres = new ArrayList<String>();
			wheres.add(where);
			joinTableMap.put(table, wheres);
		} else {
			wheres.add(where);
		}
	}
}
