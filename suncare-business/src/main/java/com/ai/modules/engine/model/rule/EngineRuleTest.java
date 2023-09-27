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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.alibaba.fastjson.JSON;

/**
 * 
 * 功能描述：化验结果规则
 *
 * @author  zhangly
 * Date: 2021年2月1日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineRuleTest extends AbsEngineParamRule {	
	public EngineRuleTest(String colName, String compareValue) {
		super(colName, compareValue);
	}

	@Override
	public String where() {
		StringBuilder sb = new StringBuilder();
		List<TestResult> list = JSON.parseArray(compareValue, TestResult.class);
		List<String> wheres = new ArrayList<String>();
		for(TestResult bean : list) {
			sb.setLength(0);
			sb.append("_query_:\"");
			EngineMapping mapping = new EngineMapping("DWB_TEST_RESULT", "VISITID", "VISITID");
			SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
			sb.append(plugin.parse());
			String itemCodes = "(" + StringUtils.replace(bean.getItemValue(), "|", " OR ") + ")";
			if("ITEM".equals(bean.getItemType())) {
				//项目
				sb.append("ITEMCODE:").append(itemCodes);			
			} else {
				//项目组
				sb.append("_query_:\\\"");
				plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
				sb.append(plugin.parse());
				sb.append("TREATGROUP_CODE:").append(itemCodes);
				sb.append("\\\"");
			}				
			if("2".equals(bean.getValueType())) {
				//定性
				sb.append(" AND TIP:").append(bean.getUnitName());
			} else {
				//定量
				sb.append(" AND TESTVALUEUNIT:").append(bean.getUnitName());
				sb.append(" AND TEST_VALUE:");
				String value = bean.getValue().replace("(", "{");
				value = value.replace(")", "}");
				value = value.replace(",", " TO ");
				sb.append(value);
			}
			sb.append("\"");
			wheres.add(sb.toString());
		}
		String condition = StringUtils.join(wheres, " AND ");
		int size = list.size();
		if(size>1) {
			condition = "(" + condition + ")";
		}
		return condition;
	}
	
	static class TestResult {
		private String itemType;
		private String itemValue;
		private String valueType;
		private String unit;
		private String unitName;
		private String value;
		public String getItemType() {
			return itemType;
		}
		public void setItemType(String itemType) {
			this.itemType = itemType;
		}
		public String getItemValue() {
			return itemValue;
		}
		public void setItemValue(String itemValue) {
			this.itemValue = itemValue;
		}
		public String getValueType() {
			return valueType;
		}
		public void setValueType(String valueType) {
			this.valueType = valueType;
		}
		public String getUnit() {
			return unit;
		}
		public void setUnit(String unit) {
			this.unit = unit;
		}
		public String getUnitName() {
			return unitName;
		}
		public void setUnitName(String unitName) {
			this.unitName = unitName;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
}
