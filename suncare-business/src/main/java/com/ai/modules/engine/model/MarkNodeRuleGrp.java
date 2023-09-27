/**
 * MarkNodeRuleGrp.java	  V1.0   2020年9月24日 下午8:27:13
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import lombok.Data;

@Data
public class MarkNodeRuleGrp {
	private Boolean single = null;
	private String type;
	private EngineNodeRule rule;
}
