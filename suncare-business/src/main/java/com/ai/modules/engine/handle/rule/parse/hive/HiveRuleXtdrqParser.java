/**
 * RuleParser.java	  V1.0   2020年12月18日 下午5:59:21
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.parse.hive;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.hive.WithTableUtil;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.rule.hive.HiveParamGrpRule;
import com.ai.modules.engine.model.rule.hive.HiveParamIndicationRule;
import com.ai.modules.engine.model.rule.hive.HiveRuleMasterInfo;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

/**
 * 
 * 功能描述：限特定人群规则解析
 *
 * @author  zhangly
 * Date: 2021年1月15日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveRuleXtdrqParser extends AbsHiveRuleParser {
	
	public HiveRuleXtdrqParser(MedicalRuleConfig rule, MedicalRuleConditionSet condition, String fromTable) {
		super(rule, condition, fromTable);
	}

	@Override
	public WithTableModel parseCondition() {
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		if(StringUtils.isNotBlank(condition.getExt1())) {
			//年龄
			HiveRuleMasterInfo paramRule = new HiveRuleMasterInfo(fromTable);
			paramRule.setAgeUnit(condition.getExt2());
			paramRule.setAgeRange(condition.getExt1());
			String alias = "table_"+this.getClass().getSimpleName()+"_master";
			WithTableModel withTable = new WithTableModel(alias, paramRule.where());
			withTableList.add(withTable);
		}
		if(StringUtils.isNotBlank(condition.getExt4())) {
			//疾病组
			HiveParamIndicationRule paramRule = new HiveParamIndicationRule("DIAGGROUP_CODE", condition.getExt4(), fromTable);			
			String compare = condition.getExt3();
			if("≠".equals(compare)) {
				paramRule.setReverse(true);
			}
			String alias = "table_"+this.getClass().getSimpleName()+"_diag";
			WithTableModel withTable = new WithTableModel(alias, paramRule.where());
			withTableList.add(withTable);
		}
		if(StringUtils.isNotBlank(condition.getExt6())) {
			//药品组
			HiveParamGrpRule paramRule = new HiveParamGrpRule("STD_DRUGGROUP", "DRUGGROUP_CODE", condition.getExt6(), fromTable);
			String compare = condition.getExt5();
			if("≠".equals(compare)) {
				paramRule.setReverse(true);
			}
			String alias = "table_"+this.getClass().getSimpleName()+"_diag";
			WithTableModel withTable = new WithTableModel(alias, paramRule.where());
			withTableList.add(withTable);
		}
		if(StringUtils.isNotBlank(condition.getExt8())) {
			//项目组
			HiveParamGrpRule paramRule = new HiveParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", condition.getExt8(), fromTable);
			String compare = condition.getExt7();
			if("≠".equals(compare)) {
				paramRule.setReverse(true);
			}
			String alias = "table_"+this.getClass().getSimpleName()+"_treat";
			WithTableModel withTable = new WithTableModel(alias, paramRule.where());
			withTableList.add(withTable);
		}
		if(StringUtils.isNotBlank(condition.getExt10())) {
			//医嘱
			String compare = condition.getExt9();
			StringBuilder sb = new StringBuilder();
			sb.append("select * from ").append(fromTable);
			sb.append(" where visitid");
			if("notlike".equals(compare)) {
				sb.append(" not");
			}
			sb.append(" in(select visitid from ").append(EngineUtil.DWB_ORDER).append(" x1");
			sb.append(" where ");
			sb.append("ITEMNAME like '%").append(condition.getExt10()).append("%'");
			sb.append(")");
			String alias = "table_"+this.getClass().getSimpleName()+"_order";
			WithTableModel withTable = new WithTableModel(alias, sb.toString());
			withTableList.add(withTable);
		}		
		String alias = this.buildWithTable();
		if(withTableList.size()==1) {
			WithTableModel bean = withTableList.get(0);
			bean.setAlias(alias);
			return bean;
		}
		return new WithTableModel(alias, WithTableUtil.parseWithTableList(fromTable, withTableList));
	}
}
