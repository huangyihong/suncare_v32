/**
 * Demo.java	  V1.0   2022年5月19日 下午4:05:53
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

public class WorkflowSolr {
	private String project;
	private String table_name;
	private String insert_segment;
	private String table_update_time;
	private String update_type;
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public String getTable_name() {
		return table_name;
	}
	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}
	public String getInsert_segment() {
		return insert_segment;
	}
	public void setInsert_segment(String insert_segment) {
		this.insert_segment = insert_segment;
	}
	public String getTable_update_time() {
		return table_update_time;
	}
	public void setTable_update_time(String table_update_time) {
		this.table_update_time = table_update_time;
	}
	public String getUpdate_type() {
		return update_type;
	}
	public void setUpdate_type(String update_type) {
		this.update_type = update_type;
	}
}
