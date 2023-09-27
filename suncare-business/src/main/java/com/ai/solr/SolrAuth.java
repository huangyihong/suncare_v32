/**
 * SolrAuth.java	  V1.0   2021年11月4日 下午3:27:56
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.solr;

public class SolrAuth {
	//身份认证-用户名
	private String user;
	//身份认证-密码
	private String password;
	
	public SolrAuth(String user, String password) {
		this.user = user;
		this.password = password;
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
