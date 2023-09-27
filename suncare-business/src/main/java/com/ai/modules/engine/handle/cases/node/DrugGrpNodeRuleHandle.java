/**
 * EngineNodeRuleHandler.java	  V1.0   2020年4月9日 上午11:23:06
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

public class DrugGrpNodeRuleHandle extends AbsNodeRuleHandle {
	//替代字段名
	private String replaceName;
	
	public DrugGrpNodeRuleHandle(EngineNodeRule rule, String replaceName) {
		super(rule);
		this.replaceName = replaceName;
	}
	
	public String handler() {
		StringBuilder sb = new StringBuilder();
		
		String colName = rule.getColName().toUpperCase();
		if(StringUtils.isNotBlank(replaceName)) {
			colName = replaceName;
		}
		String compareType = rule.getCompareType();
		
		/*//查找疾病组中的疾病
		QueryWrapper<MedicalDrugGroupItem> queryWrapper = new QueryWrapper<MedicalDrugGroupItem>();
		//queryWrapper.eq("GROUP_ID", rule.getCompareValue());
		queryWrapper.inSql("GROUP_ID", "SELECT GROUP_ID FROM MEDICAL_DRUG_GROUP WHERE GROUP_CODE='"+rule.getCompareValue()+"'");
		IMedicalDrugGroupItemService service = SpringContextUtils.getApplicationContext().getBean(IMedicalDrugGroupItemService.class);
		List<MedicalDrugGroupItem> dataList = service.list(queryWrapper);
		if(dataList==null || dataList.size()==0) {
			sb.append(colName).append(":*");
		} else {
			if(compareType.equalsIgnoreCase("notin") || compareType.equals("<>")) {
				sb.append("-(");
			}
			sb.append(colName).append(":");
			sb.append("(");
			for(int i=0, len=dataList.size(); i<len; i++) {
				MedicalDrugGroupItem item = dataList.get(i);
				if(i>0) {
					sb.append(" OR ");
				}
				sb.append(item.getCode());
			}
			sb.append(")");
		}*/
		sb.append("_query_:");
		EngineMapping mapping = new EngineMapping("STD_DRUGGROUP", "ATC_DRUGCODE", colName);
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		
		String field = "DRUGGROUP_CODE";
		if("=".equals(compareType) || "regx".equals(compareType)) {
			sb.append(field).append(":").append(rule.getCompareValue());
		} else if(compareType.equalsIgnoreCase("notin") || compareType.equals("<>")) {
			sb.append("-").append(field).append(":").append(rule.getCompareValue());
		} else if (compareType.equalsIgnoreCase("like")) {
			// 包含
			sb.append(field).append(":*").append(rule.getCompareValue()).append("*");
		} else if (compareType.equalsIgnoreCase("llike")) {
			// 以..开始
			sb.append(field).append(":").append(rule.getCompareValue()).append("*");
		} else if (compareType.equalsIgnoreCase("rlike")) {
			// 以..结尾
			sb.append(field).append(":*").append(rule.getCompareValue());
		} else if (compareType.equalsIgnoreCase("notlike")) {
			// 不包含
			sb.append(field).append(":*").append(rule.getCompareValue()).append("*");
		}
		return sb.toString();
	}
	
	public String where(boolean ignoreJoin) {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(rule.getLogic())) {
			sb.append(rule.getLogic().toUpperCase()).append(" ");
		}
		if(ignoreJoin) {
			// 忽略join
			sb.append(handler());
			return sb.toString();
		}
		
		boolean join = this.isJoin();
		String compareType = rule.getCompareType();
		if (compareType.equalsIgnoreCase("notlike")) {
			sb.append("(*:* -");
		}
		if(join) {
			sb.append("_query_:\"");
			EngineMapping mapping = EngineUtil.ENGIME_MAPPING.get(rule.getTableName().toUpperCase());
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
