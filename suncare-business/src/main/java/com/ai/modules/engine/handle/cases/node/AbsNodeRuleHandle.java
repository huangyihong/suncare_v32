/**
 * AbsNodeRuleHandle.java	  V1.0   2020年4月9日 下午12:03:38
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;

public abstract class AbsNodeRuleHandle {
	protected EngineNodeRule rule;
	
	public AbsNodeRuleHandle(EngineNodeRule rule) {
		//特殊字符处理
		rule.escapeQueryChars();
		this.rule = rule;
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
	
	public abstract String handler();
	
	/**
	 * 
	 * 功能描述：节点的查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年5月16日 下午5:14:10</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public String where() {
		return where(false);
	}
	
	/**
	 * 
	 * 功能描述：节点的查询条件，忽略join
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年5月16日 下午5:14:46</p>
	 *
	 * @param ignoreJoin
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public String where(boolean ignoreJoin) {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(rule.getLogic())) {
			sb.append(rule.getLogic().toUpperCase()).append(" ");
		}
		if(ignoreJoin) {
			// 忽略join
			if (hasReverse()) {
				sb.append("(*:* -");
			}
			String where = handler();
			//join关联查询时，遇到()字符多加一次斜杆\
			where = this.replaceSpecialChar(where);
			sb.append(where);
			if (hasReverse()) {
				sb.append(")");
			}
			return sb.toString();
		}
				
		if (hasReverse()) {
			sb.append("(*:* -");
		}
		boolean join = this.isJoin();
		if(join) {
			sb.append("_query_:\"");
			EngineMapping mapping = EngineUtil.ENGIME_MAPPING.get(rule.getTableName().toUpperCase());
			if(mapping==null) {
				mapping = new EngineMapping(rule.getTableName().toUpperCase(), "VISITID", "VISITID");
			}
			SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
			sb.append(plugin.parse());
		}
		String where = handler();
		//join关联查询时，遇到()字符多加一次斜杆\
		where = this.replaceSpecialChar(where);
		sb.append(where);
		
		if(join) {
			sb.append("\"");
		}
		if (hasReverse()) {
			sb.append(")");
		}
		return sb.toString();
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
		return false;
	}
	
	private String replaceSpecialChar(String where) {
		if(this.isJoin()) {
			//join关联查询时，遇到()字符多加一次斜杆\
			String value = rule.getCompareValue();
			value = StringUtils.replace(value, "(", "\\(");
			value = StringUtils.replace(value, ")", "\\)");
			value = StringUtils.replace(value, "\\\\", "\\");
			where = StringUtils.replace(where, rule.getCompareValue(), value);
		}
		return where;
	}
}
