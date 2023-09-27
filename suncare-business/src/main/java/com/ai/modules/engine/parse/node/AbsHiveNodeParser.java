/**
 * AbsNodeHandle.java	  V1.0   2020年4月10日 上午9:18:35
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.parse.node;

import com.ai.modules.engine.model.EngineNode;

public abstract class AbsHiveNodeParser {
	protected EngineNode node;
	//是否是主表（引擎sql中的表）
	protected boolean master;
	protected String alias;
	
	public AbsHiveNodeParser(EngineNode node, boolean master, String alias) {
		this.node = node;
		this.master = master;
		this.alias = alias;
	}
	
	public AbsHiveNodeParser(EngineNode node, String alias) {
		this(node, true, alias);
	}
	
	public abstract String handler();
}
