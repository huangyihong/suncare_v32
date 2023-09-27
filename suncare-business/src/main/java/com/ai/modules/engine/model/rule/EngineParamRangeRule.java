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

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;

/**
 * 
 * 功能描述：药品、收费、临床路径数据范围模型参数
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineParamRangeRule extends AbsEngineParamRule {	
	//最小值
	private String min;
	//最大值
	private String max;
	
	public EngineParamRangeRule(String tableName, String colName, String min, String max) {
		super(tableName, colName, min);
		this.min = min;
		this.max = max;
	}

	@Override
	public String where() {
		if(min==null && max==null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		boolean join = this.isJoin();
		if(join) {
			EngineMapping mapping = DWB_CHARGE_DETAIL_MAPPING.get(tableName.toUpperCase());
			SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
			sb.append(plugin.parse());
		}
		if(min!=null && max==null) {
			sb.append(colName).append(":["+min+" TO *]");
		} else if(min==null && max!=null) {
			sb.append(colName).append(":[* TO "+max+"]");
		} else {
			sb.append(colName).append(":["+min+" TO "+max+"]");
		}

		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}	
}
