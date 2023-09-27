/**
 * WithTableModel.java	  V1.0   2022年11月9日 下午8:02:33
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.hive.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.WithTableUtil;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.handle.rule.parse.hive.AbsHiveRuleParser;
import com.ai.modules.engine.handle.rule.parse.hive.HiveRuleIndicationParser;
import com.ai.modules.engine.handle.rule.parse.hive.HiveRuleMasterInfoParser;
import com.ai.modules.engine.handle.rule.parse.hive.HiveRuleParser;
import com.ai.modules.engine.handle.rule.parse.hive.HiveRuleXtdrqParser;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

import lombok.Data;

@Data
public class GroupWithTableModel {
	private int groupNo;
	private String type;
	private MedicalRuleConfig rule;
	private List<MedicalRuleConditionSet> conditionGrpList;
	private String logic = "AND";
	//查询的表名
	private String fromTable;
	//是否检查历史疾病
	private boolean patient;
	
	public GroupWithTableModel(int groupNo, String type, 
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> conditionGrpList,
			String fromTable) {
		this.groupNo = groupNo;
		this.type = type;
		this.rule = rule;
		this.conditionGrpList = conditionGrpList;
		this.fromTable = fromTable;
	}
	
	public GroupWithTableModel(int groupNo, String type, 
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> conditionGrpList) {
		this(groupNo, type, rule, conditionGrpList, AbsHiveRuleHandle.WITH_TABLE);
	}
	
	public WithTableModel parseCondition() {
		String prefix = "table_access";
		if("judge".equals(type)) {
			prefix = "table_judge";
		}
		WithTableModel result = null;
		if(conditionGrpList!=null) {
    		List<WithTableModel> groupWithTableList = new ArrayList<WithTableModel>();
    		List<WithTableModel> withTableList = this.parseConditionGrp();
			if(withTableList.size()==1) {
				//组内仅有一个查询条件
				WithTableModel bean = withTableList.get(0);
				//重命名别名
				bean.setAlias(prefix+"_"+groupNo);
				groupWithTableList.add(bean);
			} else {
				String sql = WithTableUtil.parseWithTableList(fromTable, withTableList);
				String alias = prefix + "_" + groupNo;
				WithTableModel withTable = new WithTableModel(alias, sql, logic);
				groupWithTableList.add(withTable);
			}
    		if(groupWithTableList.size()==1) {
    			//仅有一组且组内一个条件
    			result = groupWithTableList.get(0);
    		} else {
    			//多组条件
    			
    		}
    	}
		return result;
	}
	
	/**
	 * 
	 * 功能描述：解析每一组查询条件
	 *
	 * @author  zhangly
	 *
	 * @param ruleConditionList
	 * @return
	 */
	private List<WithTableModel> parseConditionGrp() {
		List<WithTableModel> result = null;
		for(int i=0,len=conditionGrpList.size(); i<len; i++) {
			MedicalRuleConditionSet bean = conditionGrpList.get(i);
			if(result==null) {
				result = new ArrayList<WithTableModel>();
			}
			WithTableModel withTable = parseCondition(bean);
			if(withTable!=null) {
				result.add(withTable);
			}
		}
    	return result;
	}
	
	/**
	 * 
	 * 功能描述：解析查询条件
	 *
	 * @author  zhangly
	 *
	 * @param bean
	 * @return
	 */
	private WithTableModel parseCondition(MedicalRuleConditionSet bean) {
		AbsHiveRuleParser parser = null;
		String condiType = bean.getField();
		Set<String> masterSet = AbsRuleParser.RULE_MASTER_SET;
		masterSet.remove(AbsRuleParser.RULE_CONDI_HOSPLEVELTYPE);
		if(masterSet.contains(condiType)) {
			//关联master_info
			parser = new HiveRuleMasterInfoParser(rule, bean, fromTable);
		} else if(AbsRuleParser.RULE_CONDI_INDICATION.equals(condiType)) {
			//适应症
			HiveRuleIndicationParser ruleParser = new HiveRuleIndicationParser(rule, bean, fromTable);
			ruleParser.setPatient(patient);
			parser = ruleParser;
		} else if(AbsRuleParser.RULE_CONDI_XTDRQ.equals(condiType)) {
			//特殊人群
			parser = new HiveRuleXtdrqParser(rule, bean, fromTable);
		} else {
			parser = new HiveRuleParser(rule, bean, fromTable);
		}
		WithTableModel model = parser.parseCondition();
		if(model==null) {
			return null;
		}
		if(StringUtils.isNotBlank(bean.getLogic())) {
			model.setLogic(bean.getLogic());
		}
		return model;
	}
}
