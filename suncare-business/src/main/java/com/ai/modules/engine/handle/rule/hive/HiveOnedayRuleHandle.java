/**
 * DrugSecondLineHandle.java	  V1.0   2020年11月4日 下午4:09:05
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.hive;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.model.JoinTableModel;
import com.ai.modules.engine.handle.rule.hive.model.JudgeWithTableScript;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.handle.rule.parse.hive.HiveRuleIgnoreNullParser;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 一日相关规则基类
 * @author  zhangly
 * Date: 2022年11月29日
 */
public class HiveOnedayRuleHandle extends HiveRuleHandle {
		
	public HiveOnedayRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource, rule, ruleConditionList);
	}
	
	/**
	 * 
	 * 功能描述：dwb_master_info关联dws_patient_1visit_1day_itemsum
	 *
	 * @author  zhangly
	 *
	 * @param impala 计算方式{true:impala模式, false:hive模式}
	 * @return
	 * 
	 */
	@Override
	protected String masterInfoJoinDwsChargeSql(boolean impala) throws Exception {
		String dws = "dws_patient_1visit_1day_itemsum";
		StringBuilder sql = new StringBuilder();
		sql.append("select * from ").append(dws);
		sql.append(" where ");
		//主体项目查询条件
		sql.append(StringUtils.join(this.parseWhere(), " and "));
		//默认查询条件
		sql.append(this.appendDefaultWhere());
		//公共查询条件
		sql.append(this.appendCommonWhere());
		String withSql = sql.toString();
		JoinTableModel joinTableModel = this.joinTables();
		if(joinTableModel.getWithTableList()!=null 
				&& joinTableModel.getWithTableList().size()>0) {
			sql.setLength(0);
			sql.append("with ").append(dws).append(" as(");
			sql.append(withSql);
			sql.append(")");
			for(WithTableModel model : joinTableModel.getWithTableList()) {
				sql.append("\n,");
				sql.append(model.getAlias()).append(" as(").append(model.getSql()).append(")");
			}
			sql.append("\n");
			sql.append("select * from ").append(dws).append(" a");
			sql.append(" where");
			for(int i=0,len=joinTableModel.getWithTableList().size(); i<len; i++) {
				WithTableModel model = joinTableModel.getWithTableList().get(i);
				if(i>0) {
					sql.append(" and");
				}
				sql.append(" visitid in(select visitid from ").append(model.getAlias()).append(")");
			}
			withSql = sql.toString();
		}
		
		sql.setLength(0);
		sql.append("with ").append(AbsHiveRuleHandle.WITH_TABLE_ONEDAY).append(" as(");
		sql.append(withSql);
		sql.append(")");
		String fromTable = AbsHiveRuleHandle.WITH_TABLE_ONEDAY;
		//准入条件过滤后的表
		WithTableModel accessTable = this.parseAccessCondition();
		if(accessTable!=null) {
			sql.append("\n,");
			sql.append(accessTable.getAlias()).append(" as (").append(accessTable.getSql()).append(")");
			fromTable = AbsHiveRuleHandle.WITH_TABLE_ACCESS;
		}
		//判断条件过滤后的表
		JudgeWithTableScript judgeWithTableScript = this.judgeWithTableScript(fromTable);
		if(judgeWithTableScript!=null) {
			sql.append("\n,");
			sql.append(judgeWithTableScript.getSql());
			fromTable = judgeWithTableScript.getResultTable();
		}
		sql.append("\n");
		Map<String, String> udfFieldMap = this.parseUdfFieldMap();
		if(impala) {
			sql.append("select ");
			int index = 0;
			for(Map.Entry<String, String> entry : udfFieldMap.entrySet()) {
				index++;
				sql.append(entry.getValue()).append(" ").append(entry.getKey());
				if(index<udfFieldMap.size()) {
					sql.append(",");
				}
			}
		} else {
			sql.append("select default.udf_json_out(");
			sql.append("'");
			int index = 0;
			for(Map.Entry<String, String> entry : udfFieldMap.entrySet()) {
				index++;
				sql.append(entry.getKey());
				if(index<udfFieldMap.size()) {
					sql.append(",");
				}
			}
			sql.append("','ID',");
			index = 0;
			for(Map.Entry<String, String> entry : udfFieldMap.entrySet()) {
				index++;
				sql.append(entry.getValue());
				if(index<udfFieldMap.size()) {
					sql.append(",");
				}
			}
			sql.append(")");
		}
		sql.append(" from dwb_master_info x join ").append(fromTable).append(" y on x.visitid=y.visitid");
		sql.append(" where 1=1");
		//公共查询条件
		if(joinTableModel.getMasterList()!=null) {
			for(String where : joinTableModel.getMasterList()) {
				sql.append(" and x.").append(where);
			}
		}
		//添加过滤掉指标为空值的条件
		HiveRuleIgnoreNullParser ignoreNullParser = new HiveRuleIgnoreNullParser(rule, ruleConditionList);
		List<String> ignoreNullWheres = ignoreNullParser.ignoreNullWhere();
  		if(ignoreNullWheres!=null && ignoreNullWheres.size()>0) {
  			sql.append(" and ").append(StringUtils.join(ignoreNullWheres, " and "));
  		}
		return sql.toString();
	}
}
