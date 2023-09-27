/**
 * AbsNodeRuleHandle.java	  V1.0   2020年4月9日 下午12:03:38
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.parse.node;

import com.ai.modules.engine.model.EngineNodeRule;

public abstract class AbsHiveNodeRuleParser {
	protected EngineNodeRule rule;
	protected boolean master;
	protected String alias;
	
	public AbsHiveNodeRuleParser(EngineNodeRule rule, boolean master, String alias) {
		this.rule = rule;
		this.master = master;
		this.alias = alias;
	}
	
	public AbsHiveNodeRuleParser(EngineNodeRule rule, String alias) {
		this(rule, true, alias);
	}
		
	public abstract String handler();
}
