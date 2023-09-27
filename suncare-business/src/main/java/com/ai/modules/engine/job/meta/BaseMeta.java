/**
 * BaseMeta.java	  V1.0   2020年2月12日 上午9:22:00
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.job.meta;

import java.util.Map;

public class BaseMeta {
	//任务流水号
	protected String serialNo;
	//任务处理类标识
	protected String func;
	//solr数据源
	protected String datasource;
	//所有参数集合
	protected Map<String, String> params;	
	
	public BaseMeta(String serialNo, String func, String datasource) {
		this.serialNo = serialNo;
		this.func = func;
		this.datasource = datasource;
	}
	
	public BaseMeta(String serialNo, String func, String datasource, Map<String, String> params) {
		this.serialNo = serialNo;
		this.func = func;
		this.datasource = datasource;
		this.params = params;
	}
	
	public BaseMeta(Map<String, String> params) {
		String serialNo = params.get("serialNo");
		String func = params.get("func");
		String ds = params.get("datasource");
		this.serialNo = serialNo;
		this.func = func;
		this.datasource = ds;
		this.params = params;
	}

	@Override
	public String toString() {
		return "BaseMeta [serialNo=" + serialNo + ", func=" + func + ", datasource=" + datasource + ", params=" + params
				+ "]";
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getFunc() {
		return func;
	}

	public void setFunc(String func) {
		this.func = func;
	}

	public String getDatasource() {
		return datasource;
	}

	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
}
