/**
 * AbsNodeRuleHandle.java	  V1.0   2020年4月9日 下午12:03:38
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.parse.node.solr;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;

public abstract class AbsSolrNodeRuleHandle {
	public static Map<String, EngineMapping> ENGIME_ACTION_MAPPING = new HashMap<String, EngineMapping>();
	static {
		EngineMapping mapping = new EngineMapping("DWB_CLIENT", "CLIENT_ID", "CLIENT_ID");
		ENGIME_ACTION_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("STD_ORGANIZATION", "ORGID", "ORGID");
		ENGIME_ACTION_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_DEPARTMENT", "DEPTID", "DEPTID");
		ENGIME_ACTION_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_DOCTOR", "DOCTORID", "DOCTORID");
		ENGIME_ACTION_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWS_ITEMEXCHANGE2_MORECHARGE", "id", "ITEM_ID");
		ENGIME_ACTION_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWS_WEST_TCM", "id", "ITEM_ID");
		ENGIME_ACTION_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWS_ITEMPRICE_HIGHER_DETAIL", "id", "ITEM_ID");
		ENGIME_ACTION_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWS_ORDER_HISITEM_DIFF_DETAIL", "id", "ITEM_ID");
		ENGIME_ACTION_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWS_ORDER_YBITEM_DIFF_DETAIL", "id", "ITEM_ID");
		ENGIME_ACTION_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWS_HISITEMQTYMOREORDER_DETAIL", "id", "ITEM_ID");
		ENGIME_ACTION_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWS_YBITEMQTYMOREORDER_DETAIL", "id", "ITEM_ID");
		ENGIME_ACTION_MAPPING.put(mapping.getFromIndex(), mapping);
	}
	
	protected EngineNodeRule rule;
	
	public AbsSolrNodeRuleHandle(EngineNodeRule rule) {
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
		return true;
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
	protected String where(boolean ignoreJoin) {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(rule.getLogic())) {
			sb.append(rule.getLogic().toUpperCase()).append(" ");
		}
		String compareType = rule.getCompareType();
		if(ignoreJoin) {
			// 忽略join
			if (compareType.equalsIgnoreCase("notlike")) {
				sb.append("(*:* -");
			}
			sb.append(handler());
			if (compareType.equalsIgnoreCase("notlike")) {
				sb.append(")");
			}
			return sb.toString();
		}
				
		if (compareType.equalsIgnoreCase("notlike")) {
			sb.append("(*:* -");
		}
		boolean join = this.isJoin();
		if(join) {
			sb.append("_query_:\"");
			EngineMapping mapping = ENGIME_ACTION_MAPPING.get(rule.getTableName().toUpperCase());
			if(mapping==null) {
				mapping = new EngineMapping(rule.getTableName().toUpperCase(), "VISITID", "VISITID");
			}
			SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
			sb.append(plugin.parse());
		}
		
		sb.append(handler());
		
		if(join) {
			sb.append("\"");
		}
		if (compareType.equalsIgnoreCase("notlike")) {
			sb.append(")");
		}
		return sb.toString();
	}
}
