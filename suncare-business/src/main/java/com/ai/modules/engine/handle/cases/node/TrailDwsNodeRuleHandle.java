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

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.engine.model.EngineConstant;
import com.ai.modules.engine.model.EngineDwsMapping;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.EngineMiddleDwsMapping;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
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
public class TrailDwsNodeRuleHandle {
	
	protected List<EngineNodeRuleGrp> grpWheres;
	//需要查询的solr主表名
	protected String master;
	
	public TrailDwsNodeRuleHandle(List<EngineNodeRuleGrp> grpWheres) {
		this.grpWheres = grpWheres;
		this.master = EngineUtil.DWB_MASTER_INFO;
	}
	
	public TrailDwsNodeRuleHandle withMaster(String master) {
		this.master = master;
		return this;
	}
	
	public String where() {
		if(grpWheres==null || grpWheres.size()==0 || grpWheres.get(0).getRuleList().size()==0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		String tableName = grpWheres.get(0).getRuleList().get(0).getTableName();
		boolean join = false;
		if(!master.equalsIgnoreCase(tableName)) {
			if(EngineUtil.ENGIME_MAPPING.containsKey(tableName.toUpperCase())) {
				//直接关联dws_master_info
				EngineMapping mapping = EngineUtil.ENGIME_MAPPING.get(tableName.toUpperCase());
				SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
				sb.append(plugin.parse());
			} else if(EngineConstant.ENGINE_DWS_MAPPING.containsKey(tableName.toUpperCase())) {
				//关联dws_master_info_ids，需要解析节点查询条件获得数据周期关联字段
				//是否存在出院指标
				boolean leave = false;
				for(EngineNodeRuleGrp grp : grpWheres) {
					for(EngineNodeRule rule :grp.getRuleList()) {
						MedicalColConfig config = rule.getColConfig();
						if(config!=null && config.getIsLeaveHospCol()!=null && config.getIsLeaveHospCol()==1) {
							leave = true;
							break;
						}
					}
				}
				String durationType = this.parseNodeDurationType();
				EngineDwsMapping mapping = EngineConstant.ENGINE_DWS_MAPPING.get(tableName.toUpperCase());
				String from = mapping.getFrom();
				from = "ID".equalsIgnoreCase(from) ? "id" : from;
				String to = "TO_".concat(mapping.getFromIndex()).concat("_").concat(durationType).concat("ID");
				
				join = true;
				String fromIndex = "DWB_MASTER_INFO_IDS";
				if(leave) {
					fromIndex = "DWB_MASTER_INFO_LVIDS";
				}
				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(fromIndex, "VISITID", "VISITID");
				sb.append(plugin.parse());
				sb.append("_query_:\"");
				plugin = new SolrJoinParserPlugin(mapping.getFromIndex(), from, to);
				sb.append(plugin.parse());
			} else if(EngineConstant.ENGINE_MIDDLE_DWS_MAPPING.containsKey(tableName.toUpperCase())) {
				join = true;
				EngineMiddleDwsMapping mapping = EngineConstant.ENGINE_MIDDLE_DWS_MAPPING.get(tableName.toUpperCase());
				EngineMapping middle = mapping.getMiddle();
				EngineMapping dws = mapping.getDws();
				String fromIndex = middle.getFromIndex() + "_" + this.parseNodeDurationType();
				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(fromIndex, middle.getFrom(), middle.getTo());
				sb.append(plugin.parse());
				sb.append("_query_:\"");
				plugin = SolrJoinParserPlugin.build(dws);
				sb.append(plugin.parse());
			}
		}
		
		sb.append(mergeCommonWhere());
		if(join) {
			sb.append("\"");
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * 功能描述：拼接公共的查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年8月18日 下午3:55:21</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected String mergeCommonWhere() {
		StringBuilder sb = new StringBuilder();
		if(grpWheres!=null && grpWheres.size()>0) {
			int size = 0;
			for(int i=0, len=grpWheres.size(); i<len; i++) {
				EngineNodeRuleGrp grp = grpWheres.get(i);
				if(i>0) {
					sb.append(" ");
				}
				if(StringUtils.isNotBlank(grp.getLogic())) {
					sb.append(grp.getLogic().toUpperCase()).append(" ");
				}
				size = grp.getRuleList().size();
				if(size>1) {
					sb.append("(");
				}
				for(EngineNodeRule rule : grp.getRuleList()) {
					AbsNodeRuleHandle handle = new BaseNodeRuleHandle(rule);
					sb.append(handle.where(true));
					sb.append(" ");
				}
				sb.deleteCharAt(sb.length()-1);
				if(size>1) {
					sb.append(")");
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * 功能描述：解析节点查询条件获得筛查数据周期类型
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年5月26日 下午7:56:55</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private String parseNodeDurationType() {
		String durationType = "D";
		for(EngineNodeRuleGrp grp : this.grpWheres) {
			for(EngineNodeRule rule : grp.getRuleList()) {		
				if("DURATIONTYPE".equals(rule.getColName())) {
					if("日".equals(rule.getCompareValue())) {
						durationType = "D";
						break;
					} else if("周".equals(rule.getCompareValue())) {
						durationType = "W";
						break;
					} else if("月".equals(rule.getCompareValue())) {
						durationType = "M";
						break;
					} else if("季".equals(rule.getCompareValue())) {
						durationType = "Q";
						break;
					} else if("年".equals(rule.getCompareValue())) {
						durationType = "Y";
						break;
					}
				}
			}
		}
		
		return durationType;
	}
}
