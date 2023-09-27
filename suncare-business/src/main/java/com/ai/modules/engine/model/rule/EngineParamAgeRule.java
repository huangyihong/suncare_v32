/**
 * EngineParamRule.java	  V1.0   2019年12月31日 下午5:23:44
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;

/**
 * 
 * 功能描述：药品、收费、临床路径年龄模型参数
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineParamAgeRule extends AbsEngineParamRule {	
	
	public EngineParamAgeRule(String tableName, String colName, String compareValue) {
		super(tableName, colName, compareValue);
	}

	@Override
	public String where() {
		if(StringUtils.isBlank(compareValue)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		boolean join = this.isJoin();
		if(join) {
			EngineMapping mapping = DWB_CHARGE_DETAIL_MAPPING.get(tableName.toUpperCase());
			SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
			sb.append(plugin.parse());
		}
		switch (compareValue) {
			case "1":
				//新生儿
				sb.append("DAYAGE:[0 TO 28]");
				break;
			case "2":
				//小儿
				sb.append("MONTHAGE:[0 TO 1]");
				break;
			case "3":
				//儿童
				sb.append(colName).append(":[0 TO 18}");
				break;
			case "4":
				//老年人
				sb.append(colName).append(":[60 TO *]");
				break;
			default:
				//成年人
				sb.append(colName).append(":[18 TO *]");
				break;
		}
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}	
}
