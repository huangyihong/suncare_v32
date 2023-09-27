/**
 * AbsHiveNodeHandle.java	  V1.0   2022年12月2日 上午11:15:25
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node.hive;

import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.EngineNode;

public abstract class AbsHiveNodeHandle {
	protected EngineNode node;
	protected String fromTable;
	
	public AbsHiveNodeHandle(EngineNode node, String fromTable) {
		this.node = node;
		this.fromTable = fromTable;
	}
	
	public abstract WithTableModel parseWithTableModel();
	
	protected String buildWithTableAlias() {
		return fromTable+"_"+node.getOrderNo();
	}
}
