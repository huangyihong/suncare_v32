/**
 * AbsHiveNodeRuleHandle.java	  V1.0   2022年12月2日 上午11:05:00
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node.hive;

import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.parse.node.AbsHiveNodeRuleParser;
import com.ai.modules.engine.parse.node.HiveNodeRuleParser;
import com.ai.modules.engine.util.EngineUtil;

public abstract class AbsHiveNodeRuleHandle {
	
	protected EngineNodeRule rule;
	protected String fromTable;
	protected String alias;
	
	public AbsHiveNodeRuleHandle(EngineNodeRule rule, String fromTable, String alias) {
		this.rule = rule;
		this.fromTable = fromTable;
		this.alias = alias;
	}
	
	public AbsHiveNodeRuleHandle(EngineNodeRule rule, String fromTable) {
		this(rule, fromTable, null);
	}
	
	public abstract String script();
	
	protected abstract String template();
	
	public String where(boolean master) {
		AbsHiveNodeRuleParser parser = new HiveNodeRuleParser(rule, master, alias);
		return parser.handler();
	}
	
	public String where() {
		return where(true);
	}
	
	/**
	 * 
	 * 功能描述：是否需要关联查询
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年4月9日 下午2:45:15</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected boolean isJoin() {
		if(!EngineUtil.DWB_MASTER_INFO.equalsIgnoreCase(rule.getTableName())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * 功能描述：是否取反
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	protected boolean hasReverse() {
		String compareType = rule.getCompareType();
		if(isJoin()) {
			return compareType.equals("<>") || compareType.equalsIgnoreCase("notlike");
		}
		return compareType.equalsIgnoreCase("notlike");
	}
}
