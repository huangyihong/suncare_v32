/**
 * EngineDwsMapping.java	  V1.0   2020年5月26日 下午5:34:16
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

/**
 * 
 * 功能描述：dws与dwb通过中间表关联关系
 *
 * @author  zhangly
 * Date: 2020年5月26日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineMiddleDwsMapping {
	//dws表
	private EngineMapping dws;
	//dws与dwb关联的中间表
	private EngineMapping middle;
	//dws表id生成模板
	private String toTemplate;
	
	public EngineMiddleDwsMapping(EngineMapping dws, EngineMapping middle, String toTemplate) {
		this.dws = dws;
		this.middle = middle;
	}
	
	public EngineMiddleDwsMapping(String dwsTable, String middleTable, String toTemplate) {
		dws = new EngineMapping(dwsTable, "id", "DWSID");
		middle = new EngineMapping(middleTable, "VISITID", "VISITID");
		this.toTemplate = toTemplate;
	}
	
	public EngineMiddleDwsMapping(String dwsTable, String toTemplate) {
		dws = new EngineMapping(dwsTable, "id", "DWSID");
		middle = new EngineMapping("MAPPER_"+dwsTable, "VISITID", "VISITID");
		this.toTemplate = toTemplate;
	}

	public EngineMapping getMiddle() {
		return middle;
	}

	public void setMiddle(EngineMapping middle) {
		this.middle = middle;
	}

	public EngineMapping getDws() {
		return dws;
	}

	public void setDws(EngineMapping dws) {
		this.dws = dws;
	}

	public String getToTemplate() {
		return toTemplate;
	}

	public void setToTemplate(String toTemplate) {
		this.toTemplate = toTemplate;
	}
}
