/**
 * EngineTableRelationshipsEntity.java	  V1.0   2020年11月18日 下午4:30:29
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

public class EngineTableRelationshipsEntity {
	private String from;
	private String to;
	private String fromIndex;
	private String toIndex;
	private String where;
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getFromIndex() {
		return fromIndex;
	}
	public void setFromIndex(String fromIndex) {
		this.fromIndex = fromIndex;
	}
	public String getToIndex() {
		return toIndex;
	}
	public void setToIndex(String toIndex) {
		this.toIndex = toIndex;
	}
	public String getWhere() {
		return where;
	}
	public void setWhere(String where) {
		this.where = where;
	}
}
