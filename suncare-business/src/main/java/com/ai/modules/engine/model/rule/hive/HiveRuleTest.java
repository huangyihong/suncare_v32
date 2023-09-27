/**
 * EngineParamRule.java	  V1.0   2019年12月31日 下午5:23:44
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule.hive;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
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
public class HiveRuleTest extends AbsHiveParamRule {	
	public HiveRuleTest(String colName, String compareValue, String fromTable) {
		super(colName, compareValue, fromTable);
	}

	@Override
	public String where() {
		StringBuilder sb = new StringBuilder();
		List<TestResult> list = JSON.parseArray(compareValue, TestResult.class);
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		String table = fromTable;
		for(int i=0,len=list.size(); i<len; i++) {
			TestResult bean = list.get(i);
			String itemCodes = "('" + StringUtils.replace(bean.getItemValue(), "|", "','") + "')";
			sb.setLength(0);
			String withTable = "table_"+this.getClass().getSimpleName()+"_"+i;
			sb.append("select * from ").append(table);
			sb.append(" where exists(");
			if("ITEM".equals(bean.getItemType())) {
				//项目
				sb.append("select 1 from DWB_TEST_RESULT x1 where ").append(table).append(".visitid=x1.visitid");
				sb.append(" and x1.itemcode in").append(itemCodes);
			} else {
				//项目组
				sb.append("select 1 from DWB_TEST_RESULT x1 join medical_gbdp.STD_TREATGROUP x2 on x2.TREATCODE=x1.ITEMCODE where ").append(table).append(".visitid=x1.visitid");
				sb.append(" and x2.TREATGROUP_CODE in").append(itemCodes);
			}
			if("2".equals(bean.getValueType())) {
				//定性
				sb.append(" AND x1.TIP='").append(bean.getUnitName()).append("'");
			} else {
				//定量
				sb.append(" AND x1.TESTVALUEUNIT='").append(bean.getUnitName()).append("'");
				String value = bean.getValue();
				value = StringUtils.replace(value, ",)", ",*)");
				value = StringUtils.replace(value, "(,", "(*,");
				value = StringUtils.replace(value, ",]", ",*]");
				value = StringUtils.replace(value, "[,", "[*,");
				String[] array = StringUtils.split(value, ",");
				String min = array[0];
				min = StringUtils.replace(min, "(", ">");
				min = StringUtils.replace(min, "[", ">=");
				if(!min.contains("*")) {
					sb.append(" AND x1.TEST_VALUE").append(min);
				}
				String max = array[1];
				max = StringUtils.replace(max, ")", ">");
				max = StringUtils.replace(max, "]", ">=");
				if(!max.contains("*")) {
					sb.append(" AND ").append(max).append("x1.TEST_VALUE");
				}
			}
			sb.append(")");
			withTableList.add(new WithTableModel(withTable, sb.toString()));
			table = withTable;
		}
		sb.setLength(0);
		int size = withTableList.size();
		for(int i=0; i<size; i++) {
			WithTableModel bean = withTableList.get(i);
			if(i>0) {
				sb.append("\n,");
			}
			if(i==0) {
				sb.append("with ");
			}
			sb.append(bean.getAlias()).append(" as(").append(bean.getSql()).append(")");
		}
		WithTableModel prev = withTableList.get(size-1);
		sb.append("\n");
		sb.append("select * from ").append(fromTable);
		sb.append(" where ").append(fromTable).append(".visitid in");
		sb.append("(select ").append(prev.getAlias()).append(".visitid").append(" from ").append(prev.getAlias()).append(")");
		return sb.toString();
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
