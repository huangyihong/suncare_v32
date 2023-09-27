/**
 * DrugRuleGroupDtlVO.java	  V1.0   2020年1月3日 下午5:00:43
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.entity.vo;

import lombok.Data;

@Data
public class DrugRuleGroupDtlVO {
	private String groupId;
	private String groupName;
	private String kind;
	private String id;
	private String itemCode;
	private String itemName;
	private Integer seq;
}
