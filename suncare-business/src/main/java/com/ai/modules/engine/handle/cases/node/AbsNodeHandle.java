/**
 * AbsNodeHandle.java	  V1.0   2020年4月10日 上午9:18:35
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node;

import java.util.HashSet;
import java.util.Set;

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.util.EngineUtil;

public abstract class AbsNodeHandle {
	protected EngineNode node;
	
	public AbsNodeHandle(EngineNode node) {
		this.node = node;
	}
	
	public abstract String parseConditionExpression();
	
	protected boolean isJoin(String tableName) {
		Set<String> filter = new HashSet<String>();
		filter.add(EngineUtil.DWB_MASTER_INFO);
		filter.add("STD_DIAGGROUP");
		filter.add("STD_TREATMENT");
		filter.add("STD_TREATGROUP");
		filter.add("STD_DRUGGROUP");
		return !filter.contains(tableName);
	}
}
