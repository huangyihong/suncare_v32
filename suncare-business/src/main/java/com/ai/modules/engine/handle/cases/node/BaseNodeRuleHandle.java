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

import java.util.List;

import org.jeecg.common.util.SpringContextUtils;

import com.ai.modules.config.vo.MedicalDictItemVO;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.service.api.IApiDictService;

public class BaseNodeRuleHandle extends AbsNodeRuleHandle {

	public BaseNodeRuleHandle(EngineNodeRule rule) {
		super(rule);
	}

	public String handler() {
		StringBuilder sb = new StringBuilder();

		String colName = "id".equals(rule.getColName())?rule.getColName():rule.getColName().toUpperCase();
		String compareType = rule.getCompareType();
		String value = rule.getCompareValue();
	/*	MedicalColConfig conf = rule.getColConfig();
		if(conf!=null && "DATE".equalsIgnoreCase(conf.getDataType())
				&& value.length()>10) {
			value = value.replaceAll("\\\\ ", "T");
		}*/
		if("=".equals(compareType) || "regx".equals(compareType)) {
			sb.append(colName).append(":").append(value);
		} else if (compareType.equals(">")) {
			// 大于
			sb.append(colName).append(":{").append(value).append(" TO *}");
		} else if (compareType.equals(">=")) {
			// 大于等于
			sb.append(colName).append(":[").append(value).append(" TO *]");
		} else if (compareType.equals("<")) {
			// 小于
			sb.append(colName).append(":{* TO ").append(value).append("}");
		} else if (compareType.equals("<=")) {
			// 小于等于
			sb.append(colName).append(":[* TO ").append(value).append("]");
		} else if (compareType.equals("<>")) {
			// 不等于
			if(hasReverse()) {
				sb.append(colName).append(":").append(value);
			} else {
				sb.append("(*:* -(").append(colName).append(":").append(value).append("))");
			}
		} else if (compareType.equalsIgnoreCase("like")) {
			// 包含
			sb.append(colName).append(":*").append(value).append("*");
		} else if (compareType.equalsIgnoreCase("llike")) {
			// 以..开始
			sb.append(colName).append(":").append(value).append("*");
		} else if (compareType.equalsIgnoreCase("rlike")) {
			// 以..结尾
			sb.append(colName).append(":*").append(value);
		} else if (compareType.equalsIgnoreCase("notlike")) {
			// 不包含
			//sb.append("-(").append(colName).append(":*").append(value).append("*").append(")");
			sb.append(colName).append(":*").append(value).append("*");
		} else if (compareType.equalsIgnoreCase("in")
				|| compareType.equalsIgnoreCase("notin")) {
			//存在或不存在
			IApiDictService dictService = SpringContextUtils.getApplicationContext().getBean(IApiDictService.class);
			List<MedicalDictItemVO> dictList = dictService.queryMedicalDictByGroupId(value);
			if(dictList!=null) {
				if(compareType.equalsIgnoreCase("notin")) {
					sb.append("-(");
				}
				sb.append(colName).append(":");
				for(int i=0, len=dictList.size(); i<len; i++) {
					MedicalDictItemVO item = dictList.get(i);
					sb.append("(");
					if(i>0) {
						sb.append(" OR ");
					}
					sb.append(item.getCode());
					sb.append(")");
				}
				if(compareType.equalsIgnoreCase("notin")) {
					sb.append(")");
				}
			}
		} else {
			sb.append(colName).append(":").append(value);
		}
		return sb.toString();
	}
}
