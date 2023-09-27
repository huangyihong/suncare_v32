/**
 * ClinicalRequiredGroupVO.java	  V1.0   2020年11月20日 下午4:06:06
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.vo;

public class ClinicalRequiredGroupVO {
	private String groupType;
	private String groupCodes;
	private String groupNames;
	
	public ClinicalRequiredGroupVO(String groupType, String groupCodes, String groupNames) {
		this.groupType = groupType;
		this.groupCodes = groupCodes;
		this.groupNames = groupNames;
	}
	
	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public String getGroupCodes() {
		return groupCodes;
	}
	public void setGroupCodes(String groupCodes) {
		this.groupCodes = groupCodes;
	}
	public String getGroupNames() {
		return groupNames;
	}
	public void setGroupNames(String groupNames) {
		this.groupNames = groupNames;
	}
}
