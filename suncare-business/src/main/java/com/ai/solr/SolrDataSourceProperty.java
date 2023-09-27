/**
 * SolrDataSourceProperty.java	  V1.0   2020年8月19日 下午5:52:54
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.solr;

public class SolrDataSourceProperty {
	//zookeeper地址
	private String zk;
	//solr地址
	private String url;
	//是否集群模式，多分片，默认false
	private boolean cluster = false;
	//身份认证-用户名
	private String user;
	//身份认证-密码
	private String password;
	
	//用来计算药品、收费合规的备用机器
	private String slaveZk;
	private String slaveUrl;
	
	@Override
	public String toString() {
		return "SolrDataSourceProperty [zk=" + zk + ", url=" + url + ", slaveZk=" + slaveZk + ", slaveUrl=" + slaveUrl
				+ "]";
	}
	
	public String getZk() {
		return zk;
	}
	public void setZk(String zk) {
		this.zk = zk;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getSlaveZk() {
		return slaveZk;
	}

	public void setSlaveZk(String slaveZk) {
		this.slaveZk = slaveZk;
	}

	public String getSlaveUrl() {
		return slaveUrl;
	}

	public void setSlaveUrl(String slaveUrl) {
		this.slaveUrl = slaveUrl;
	}

	public boolean isCluster() {
		return cluster;
	}

	public void setCluster(boolean cluster) {
		this.cluster = cluster;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
