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

import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;

public class DiseaseGrpNodeRuleHandle extends AbsNodeRuleHandle {
	//替代字段名
	private String replaceName;
	
	public DiseaseGrpNodeRuleHandle(EngineNodeRule rule, String replaceName) {
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
		QueryWrapper<MedicalDiseaseGroupItem> queryWrapper = new QueryWrapper<MedicalDiseaseGroupItem>();
		//queryWrapper.eq("GROUP_ID", rule.getCompareValue());
		queryWrapper.inSql("GROUP_ID", "SELECT GROUP_ID FROM MEDICAL_DISEASE_GROUP WHERE GROUP_CODE='"+rule.getCompareValue()+"'");
		IMedicalDiseaseGroupItemService service = SpringContextUtils.getApplicationContext().getBean(IMedicalDiseaseGroupItemService.class);
		List<MedicalDiseaseGroupItem> dataList = service.list(queryWrapper);
		if(dataList==null || dataList.size()==0) {
			sb.append(colName).append(":*");
		} else {
			if(compareType.equalsIgnoreCase("notin") || compareType.equals("<>")) {
				sb.append("-(");
			}
			sb.append(colName).append(":");
			sb.append("(");
			for(int i=0, len=dataList.size(); i<len; i++) {
				MedicalDiseaseGroupItem item = dataList.get(i);
				if(i>0) {
					sb.append(" OR ");
				}
				sb.append(item.getCode());
			}
			sb.append(")");
		}*/
		sb.append("_query_:");
		SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_DIAGGROUP", "DISEASECODE", colName);
		sb.append(plugin.parse());

		String field = "DIAGGROUP_CODE";
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
}
