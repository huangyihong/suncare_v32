/**
 * EngineNodeRuleHandler.java	  V1.0   2020年4月9日 上午11:23:06
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.parse.node;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.SpringContextUtils;

import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.service.api.IApiDictService;

public class HiveNodeRuleParser extends AbsHiveNodeRuleParser {

	public HiveNodeRuleParser(EngineNodeRule rule, boolean master, String alias) {
		super(rule, master, alias);
	}
	
	public HiveNodeRuleParser(EngineNodeRule rule, String alias) {
		super(rule, alias);
	}

	public String handler() {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(alias)) {
			sb.append(alias).append(".");
		}
		String colName = rule.getColName();
		String compareType = rule.getCompareType();
		String value = rule.getCompareValue();
		if("null".equalsIgnoreCase(value)) {
			if("=".equals(compareType)) {
				sb.append(colName).append(" is null");
			} else if("<>".equals(compareType)) {
				sb.append(colName).append(" is not null");
			} else {
				sb.append(colName).append("='null'");
			}
			return sb.toString();
		}
		MedicalColConfig conf = rule.getColConfig();
		if(null!= conf
				&& conf.getColType()==2
				&& StringUtils.isNotBlank(conf.getColValueExpressionSolr())
				&& !"VIRTUAL".equals(conf.getColValueExpressionSolr())) {
			//虚拟字段
			String express = conf.getColValueExpressionSolr();
			if(express.startsWith("sum")) {
				express = StringUtils.replace(express, "sum", "");
				express = StringUtils.replace(express, ",", "+");
			}
			colName = express;
		}
		boolean isNumber = StringUtils.isNotBlank(conf.getDataType()) && "NUMBER".equalsIgnoreCase(conf.getDataType());
		if("=".equals(compareType) 
				|| compareType.equals(">")
				|| compareType.equals(">=")
				|| compareType.equals("<")
				|| compareType.equals("<=")) {
			sb.append(colName).append(compareType);
			if(!isNumber) {
				sb.append("'");
			}
			sb.append(value);
			if(!isNumber) {
				sb.append("'");
			}
		} else if(compareType.equals("<>")) {
			if(master) {
				sb.append(colName).append(compareType);
			} else {
				sb.append(colName).append("=");
			}
			if(!isNumber) {
				sb.append("'");
			}
			sb.append(value);
			if(!isNumber) {
				sb.append("'");
			}
		} else if("regx".equals(compareType)) {
			//正则表达式
			if("*?".equals(value) || "?*".equals(value)) {
				//非空
				sb.append(colName).append(" is not null");
			} else {
				sb.append(colName).append(" rlike '").append(value).append("'");
			}
		} else if (compareType.equalsIgnoreCase("like")) {
			// 包含
			sb.append(colName).append(" like '%").append(value).append("%'");
		} else if (compareType.equalsIgnoreCase("llike")) {
			// 以..开始
			sb.append(colName).append(" like '").append(value).append("%'");
		} else if (compareType.equalsIgnoreCase("rlike")) {
			// 以..结尾
			sb.append(colName).append(" like '%").append(value).append("'");
		} else if (compareType.equalsIgnoreCase("notlike")) {
			// 不包含
			if(master) {
				sb.append(colName).append(" not like '%").append(value).append("%'");
			} else {
				sb.append(colName).append(" like '%").append(value).append("%'");
			}
		} else if (compareType.equalsIgnoreCase("in")
				|| compareType.equalsIgnoreCase("notin")) {
			//存在或不存在
			IApiDictService dictService = SpringContextUtils.getApplicationContext().getBean(IApiDictService.class);
			List<MedicalDictItemVO> dictList = dictService.queryMedicalDictByGroupId(value);
			if(dictList!=null) {
				sb.append(colName);
				if(compareType.equalsIgnoreCase("in")) {
					sb.append(" in(");
				} else {
					sb.append(" not in(");
				}
				for(int i=0, len=dictList.size(); i<len; i++) {
					MedicalDictItemVO item = dictList.get(i);
					if(i>0) {
						sb.append(",");
					}
					sb.append("'");
					sb.append(item.getCode());
					sb.append("'");
				}
				sb.append(")");
			}
		} else {
			sb.append(colName).append(compareType);
			if(!isNumber) {
				sb.append("'");
			}
			sb.append(value);
			if(!isNumber) {
				sb.append("'");
			}
		}
		return sb.toString();
	}
}
