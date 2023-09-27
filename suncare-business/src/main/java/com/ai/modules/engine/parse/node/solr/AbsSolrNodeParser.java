/**
 * AbsNodeHandle.java	  V1.0   2020年4月10日 上午9:18:35
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.parse.node.solr;

import com.ai.modules.engine.model.EngineNode;

public abstract class AbsSolrNodeParser {
	protected EngineNode node;
	
	public AbsSolrNodeParser(EngineNode node) {
		this.node = node;
	}
	
	public abstract String handler();
	
	protected boolean isJoin(String tableName) {
		return true;
	}
}
