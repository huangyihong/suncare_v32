/**
 * AbsClinicalRuleHandle.java	  V1.0   2020年4月23日 上午11:06:22
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.clinical;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;

public abstract class AbsClinicalRuleHandle {
	//表名
	protected String tableName;
	//字段名
	protected String colName;
	//比较运算符
	protected String compareType;
	//比较值
	protected String compareValue;
	
	public AbsClinicalRuleHandle(String tableName, String colName, String compareValue) {
		this.tableName = tableName;
		this.colName = colName;
		this.compareType = "=";
		this.compareValue = compareValue;
	}
	
	public AbsClinicalRuleHandle(String tableName, String colName, String compareType, String compareValue) {
		this.tableName = tableName;
		this.colName = colName;
		this.compareType = compareType;
		this.compareValue = compareValue;
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
		if(!EngineUtil.DWB_MASTER_INFO.equalsIgnoreCase(tableName)
				&& EngineUtil.ENGIME_MAPPING.containsKey(tableName.toUpperCase())) {
			return true;
		}
		return false;
	}
	
	protected abstract String handler();
	
	public String where() {
		StringBuilder sb = new StringBuilder();
		boolean join = this.isJoin();
		if(join) {
			sb.append("_query_:\"");
			EngineMapping mapping = EngineUtil.ENGIME_MAPPING.get(tableName.toUpperCase());
			if(mapping==null) {
				mapping = new EngineMapping(tableName.toUpperCase(), "VISITID", "VISITID");
			}
			SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
			sb.append(plugin.parse());
		}
		
		sb.append(handler());
		
		if(join) {
			sb.append("\"");
		}
		return sb.toString();
	}
}
