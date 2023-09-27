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

import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.engine.model.EngineConstant;
import com.ai.modules.engine.model.EngineDwsMapping;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.EngineMiddleDwsMapping;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;

/**
 * DWS层节点规则处理
 * 功能描述：
 *
 * @author  zhangly
 * Date: 2020年5月14日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class DwsNodeRuleHandle extends AbsTemplateNodeRuleHandle {

	//周期类指标
	protected String durationType;

	public DwsNodeRuleHandle(EngineNodeRule rule) {
		super(rule);
	}
	
	@Override
	public String where(boolean ignoreJoin) {
		String template = template();
		String where = handler();
		template = StringUtils.replace(template, "$where", where);
		return template;
	}

	@Override
	public String handler() {
		AbsNodeRuleHandle handle = new BaseNodeRuleHandle(rule);
		return handle.handler();
	}

	@Override
	public String template() {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(rule.getLogic())) {
			sb.append(rule.getLogic().toUpperCase()).append(" ");
		}
		if (hasReverse()) {
			sb.append("(*:* -");
		}
		sb.append("_query_:\"");
		String tableName = rule.getTableName().toUpperCase();
		boolean middleJoin = false;
		if(EngineUtil.ENGIME_MAPPING.containsKey(tableName)) {
			//直接关联dws_master_info
			EngineMapping mapping = EngineUtil.ENGIME_MAPPING.get(tableName);
			SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
			sb.append(plugin.parse());
		} else if(EngineConstant.ENGINE_DWS_MAPPING.containsKey(tableName)) {
			//关联dws_master_info_ids，需要解析节点查询条件获得数据周期关联字段
			//是否存在出院指标
			boolean leave = false;
			MedicalColConfig config = rule.getColConfig();
			if(config!=null && config.getIsLeaveHospCol()!=null && config.getIsLeaveHospCol()==1) {
				leave = true;
			}
			EngineDwsMapping mapping = EngineConstant.ENGINE_DWS_MAPPING.get(tableName);
			String from = mapping.getFrom();
			from = "ID".equalsIgnoreCase(from) ? "id" : from;
			String to = "TO_".concat(mapping.getFromIndex()).concat("_").concat(durationType).concat("ID");
			
			middleJoin = true;
			String fromIndex = "DWB_MASTER_INFO_IDS";
			if(leave) {
				fromIndex = "DWB_MASTER_INFO_LVIDS";
			}
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(fromIndex, "VISITID", "VISITID");
			sb.append(plugin.parse());
			sb.append("_query_:\\\"");
			plugin = new SolrJoinParserPlugin(mapping.getFromIndex(), from, to);
			sb.append(plugin.parse());
		} else if(EngineConstant.ENGINE_MIDDLE_DWS_MAPPING.containsKey(tableName)) {
			//中间表关联查询
			middleJoin = true;
			EngineMiddleDwsMapping mapping = EngineConstant.ENGINE_MIDDLE_DWS_MAPPING.get(tableName);
			EngineMapping middle = mapping.getMiddle();
			EngineMapping dws = mapping.getDws();
			String fromIndex = middle.getFromIndex() + "_" + durationType;
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(fromIndex, middle.getFrom(), middle.getTo());
			sb.append(plugin.parse());
			sb.append("_query_:\\\"");
			plugin = SolrJoinParserPlugin.build(dws);
			sb.append(plugin.parse());
		}
		sb.append("$where");
		if(middleJoin) {
			sb.append("\\\"");
		}
		sb.append("\"");
		if (hasReverse()) {
			sb.append(")");
		}
		return sb.toString();
	}

	public String getDurationType() {
		return durationType;
	}

	public void setDurationType(String durationType) {
		this.durationType = durationType;
	}
}
