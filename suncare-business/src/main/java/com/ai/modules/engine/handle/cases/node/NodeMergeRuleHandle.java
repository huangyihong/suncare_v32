/**
 * NodeSingleRuleHandle.java	  V1.0   2020年4月10日 上午9:23:18
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;

/**
 * 
 * 功能描述：合并节点查询条件
 *
 * @author  zhangly
 * Date: 2020年4月10日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class NodeMergeRuleHandle extends AbsNodeHandle {

	public NodeMergeRuleHandle(EngineNode node) {
		super(node);
	}

	@Override
	public String parseConditionExpression() {
		if(EngineUtil.NODE_TYPE_START.equalsIgnoreCase(node.getNodeType())
				|| EngineUtil.NODE_TYPE_END.equalsIgnoreCase(node.getNodeType())) {
			//开始或结束节点
			return null;
		}
						
		int size = node.getWheres()==null ? 0 : node.getWheres().size();
		StringBuilder sb = new StringBuilder();
		boolean diam = EngineUtil.NODE_TYPE_CONDITIONAL.equalsIgnoreCase(node.getNodeType()); //是否条件节点
		if(diam && "NO".equalsIgnoreCase(node.getCondition())) {
			sb.append("*:* -");
		}
		if(size>1) {
			sb.append("(");
		}
		//解析当前节点的查询条件
		sb.append(this.parseConditionExpressionMerge());
		if(size>1) {
			sb.append(")");
		}
		return sb.toString();
	}

	private String parseConditionExpressionMerge() {
		if(node.getWheres()!=null) {
			//节点所有的查询条件合并
			EngineNodeRule first = new EngineNodeRule();
			BeanUtils.copyProperties(node.getWheres().get(0).getRuleList().get(0), first);
			AbsNodeRuleHandle templateHandle = NodeRuleHandleFactory.getNodeRuleHandle(first);
			if(templateHandle instanceof AbsTemplateNodeRuleHandle) {
				//获取脚本模板，再替换查询条件
				AbsTemplateNodeRuleHandle porcess = (AbsTemplateNodeRuleHandle)templateHandle;
				String template = porcess.template();
				StringBuilder whereSb = new StringBuilder();
				for(EngineNodeRuleGrp grp : node.getWheres()) {
					if(StringUtils.isNotBlank(grp.getLogic())) {
						whereSb.append(" ").append(grp.getLogic().toUpperCase()).append(" ");
					}
					List<EngineNodeRule> ruleList = grp.getRuleList();
					if(ruleList.size()>1) {
						whereSb.append("(");
					}
					for(EngineNodeRule nodeRule : ruleList) {
						AbsNodeRuleHandle handle = new JoinNodeRuleHandle(nodeRule);
						if(StringUtils.isNotBlank(nodeRule.getLogic())) {
							whereSb.append(" ").append(nodeRule.getLogic().toUpperCase()).append(" ");
						}
						whereSb.append(handle.handler());
					}
					if(ruleList.size()>1) {
						whereSb.append(")");
					}
				}
				template = StringUtils.replace(template, "$where", whereSb.toString());
				return template;
			} else {
				StringBuilder sb = new StringBuilder();
				boolean isJoin = this.isJoin(first.getTableName());
				if(isJoin) {
					sb.append("_query_:\"");
					EngineMapping mapping = EngineUtil.ENGIME_MAPPING.get(first.getTableName().toUpperCase());
					if(mapping==null) {
						mapping = new EngineMapping(first.getTableName().toUpperCase(), "VISITID", "VISITID");
					}
					SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
					sb.append(plugin.parse());
				}
				int size = node.getWheres().size();
				if(size>1) {
					sb.append("(");
				}
				//遍历节点组
				for(EngineNodeRuleGrp grp : node.getWheres()) {
					if(StringUtils.isNotBlank(grp.getLogic())) {
						sb.append(" ").append(grp.getLogic().toUpperCase()).append(" ");
					}
					List<EngineNodeRule> ruleList = grp.getRuleList();
					if(ruleList.size()>1) {
						sb.append("(");
					}
					String condition = null;
					for(EngineNodeRule nodeRule : ruleList) {
						AbsNodeRuleHandle handle = NodeRuleHandleFactory.getNodeRuleHandle(nodeRule);
						condition = handle.where(true);
						sb.append(condition).append(" ");
					}
					sb.deleteCharAt(sb.length()-1);
					if(ruleList.size()>1) {
						sb.append(")");
					}
				}
				if(size>1) {
					sb.append(")");
				}
				if(isJoin) {
					sb.append("\"");
				}
				return sb.toString();
			}
		}
		return null;
	}
}
