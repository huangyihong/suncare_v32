/**
 * WarehouseProperty.java	  V1.0   2022年6月28日 下午3:11:27
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.solr;

public class WarehouseDatasourceProperty {

	private String driverClassName;
	private String url;
	private String username;
	private String password;
	/**计算引擎方式{true:gp, false:solr}*/
	private boolean enabledProcessGp = false;
	/**计算结果存储方式{true:gp, false:solr}*/
	private boolean enabledStorageGp = false;
	
	public String getDriverClassName() {
		return driverClassName;
	}
	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isEnabledProcessGp() {
		return enabledProcessGp;
	}
	public void setEnabledProcessGp(boolean enabledProcessGp) {
		this.enabledProcessGp = enabledProcessGp;
	}
	public boolean isEnabledStorageGp() {
		return enabledStorageGp;
	}
	public void setEnabledStorageGp(boolean enabledStorageGp) {
		this.enabledStorageGp = enabledStorageGp;
	}
}
