/**
 * EngineMapping.java	  V1.0   2019年11月28日 下午4:31:46
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

public class EngineMapping {
	private String fromIndex;
	private String from;
	private String to;
	
	public EngineMapping(String fromIndex, String from, String to) {
		this.fromIndex = fromIndex;
		this.from = from;
		this.to = to;
	}
	
	public EngineMapping(String fromIndex) {
		this(fromIndex, "visitid", "visitid");
	}
	
	public String getFromIndex() {
		return fromIndex;
	}
	public void setFromIndex(String fromIndex) {
		this.fromIndex = fromIndex;
	}
	
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
}
