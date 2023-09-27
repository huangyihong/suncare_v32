/**
 * SolrDruidConfig.java	  V1.0   2020年8月19日 下午5:54:20
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.solr;

public class SolrDruidConfig {
	//socketTimeout
	private int socketTimeout;
	//collectionTimeout
	private int collectionTimeout;
	// post solr jar包路径
	private String postJarPath;
	//限制查询条数
	private int maxRow;
	
	@Override
	public String toString() {
		return "SolrDruidConfig [socketTimeout=" + socketTimeout + ", collectionTimeout=" + collectionTimeout
				+ ", postJarPath=" + postJarPath + ", maxRow=" + maxRow + "]";
	}
	
	public String getPostJarPath() {
		return postJarPath;
	}

	public void setPostJarPath(String postJarPath) {
		this.postJarPath = postJarPath;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	public int getCollectionTimeout() {
		return collectionTimeout;
	}
	public void setCollectionTimeout(int collectionTimeout) {
		this.collectionTimeout = collectionTimeout;
	}

	public int getMaxRow() {
		return maxRow;
	}

	public void setMaxRow(int maxRow) {
		this.maxRow = maxRow;
	}
}
