/**
 * SolrIndicationRuleHandle.java	  V1.0   2021年3月22日 下午3:12:07
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.hive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.rule.hive.HiveParamGrpRule;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;

/**
 * 必要前提条件规则
 * @author  zhangly
 * Date: 2022年11月22日
 */
public class HivePreconditionRuleHandle extends HiveRuleHandle {
	
	public HivePreconditionRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource, rule, ruleConditionList);
	}
	
	/**
	 * 解析判断条件
	 */
	@Override
	protected WithTableModel parseJudgeCondition(String fromTable) {
		Set<String> exclude = new HashSet<String>();
    	exclude.add("fitTimeRange");
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", exclude);
    	if(judgeList==null || judgeList.size()==0) {
    		throw new RuntimeException(rule.getItemNames()+"规则未配置判断条件！");
    	}
    	MedicalRuleConditionSet bean = judgeList.get(0);
		if(this.isOnedayRely()) {
			//一日依赖关系
			return this.parseOnedayRelyCondition(bean, fromTable);
		} else {
			//一次就诊依赖关系
			return this.parseOnetimeRelyCondition(bean, fromTable);
		}
	}

	/**
	 * 
	 * 功能描述：一次就诊依赖规则with脚本
	 *
	 * @author  zhangly
	 *
	 * @param bean
	 * @param fromTable
	 * @return
	 */
	private WithTableModel parseOnetimeRelyCondition(MedicalRuleConditionSet bean, String fromTable) {
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		if(StringUtils.isNotBlank(bean.getExt1())) {
			HiveParamGrpRule paramRule = new HiveParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", bean.getExt1(), fromTable);
			paramRule.setRelation("2");
			String alias = "table_"+this.getClass().getSimpleName()+"_treat";
			WithTableModel withTable = new WithTableModel(alias, paramRule.where());
			withTableList.add(withTable);
		}
		if(StringUtils.isNotBlank(bean.getExt3())) {
			HiveParamGrpRule paramRule = new HiveParamGrpRule("STD_DRUGGROUP", "DRUGGROUP_CODE", bean.getExt3(), fromTable);
			String alias = "table_"+this.getClass().getSimpleName()+"_drug";
			WithTableModel withTable = new WithTableModel(alias, paramRule.where(), "OR");
			withTableList.add(withTable);
		}
		String sql = WithTableUtil.parseWithTableList(fromTable, withTableList);
		String alias = AbsHiveRuleHandle.WITH_TABLE_JUDGE;
		return new WithTableModel(alias, sql);		
	}
	
	/**
	 * 
	 * 功能描述：一日依赖规则with脚本
	 *
	 * @author  zhangly
	 *
	 * @param bean
	 * @param fromTable
	 * @return
	 */
	private WithTableModel parseOnedayRelyCondition(MedicalRuleConditionSet bean, String fromTable) {		
		StringBuilder sb = new StringBuilder();
		sb.append("select visitid,itemcode,");
		if(HiveJDBCUtil.enabledProcessGp()) {
			sb.append("to_char(prescripttime,'yyyy-mm-dd') daytime");
		} else {
			sb.append("substr(prescripttime,1,10) daytime");
		}
		sb.append(",sum(AMOUNT) AMOUNT,sum(FEE) FEE,sum(FUND_COVER) FUND_COVER from dwb_charge_detail");
		sb.append(" where ");
		//主体项目查询条件
		sb.append(StringUtils.join(this.parseWhere(), " and "));
		sb.append(" and visitid in(select visitid from ").append(fromTable).append(")");
		sb.append(" group by visitid,itemcode,");
		if(HiveJDBCUtil.enabledProcessGp()) {
			sb.append("to_char(prescripttime,'yyyy-mm-dd')");
		} else {
			sb.append("substr(prescripttime,1,10)");
		}
		String alias = "tmp_dwb_charge_detail";
		WithTableModel mainWithTable = new WithTableModel(alias, sb.toString());
		//依赖项目、药品
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		if(StringUtils.isNotBlank(bean.getExt1())) {
			//项目组
			sb.setLength(0);
			String value = bean.getExt1();
			value = StringUtils.replace(value, ",", "|");
			value = "'"+StringUtils.replace(value, "|", "','")+"'";
			String where = " itemcode in(select std.TREATCODE from medical_gbdp.STD_TREATGROUP std where TREATGROUP_CODE in(%s))";
			where = String.format(where, value);
			sb.append("select visitid,itemcode,");
			if(HiveJDBCUtil.enabledProcessGp()) {
				sb.append("to_char(prescripttime,'yyyy-mm-dd') daytime");
			} else {
				sb.append("substr(prescripttime,1,10) daytime");
			}
			sb.append(",sum(AMOUNT) AMOUNT,sum(FEE) FEE,sum(FUND_COVER) FUND_COVER from dwb_charge_detail");
			sb.append(" where");
			sb.append(where);
			sb.append(" and visitid in(select visitid from ").append(fromTable).append(")");
			sb.append(" group by visitid,itemcode,");
			if(HiveJDBCUtil.enabledProcessGp()) {
				sb.append("to_char(prescripttime,'yyyy-mm-dd')");
			} else {
				sb.append("substr(prescripttime,1,10)");
			}
			alias = this.getClass().getSimpleName() + "_treat";
			withTableList.add(new WithTableModel(alias, sb.toString()));
		}
		if(StringUtils.isNotBlank(bean.getExt3())) {
			//药品组
			sb.setLength(0);
			String value = bean.getExt1();
			value = StringUtils.replace(value, ",", "|");
			value = "'"+StringUtils.replace(value, "|", "','")+"'";
			String where = " itemcode in(select std.ATC_DRUGCODE from medical_gbdp.STD_DRUGGROUP std where DRUGGROUP_CODE in(%s))";
			where = String.format(where, value);
			sb.append("select visitid,itemcode,");
			if(HiveJDBCUtil.enabledProcessGp()) {
				sb.append("to_char(prescripttime,'yyyy-mm-dd') daytime");
			} else {
				sb.append("substr(prescripttime,1,10) daytime");
			}
			sb.append(",sum(AMOUNT) AMOUNT,sum(FEE) FEE,sum(FUND_COVER) FUND_COVER from dwb_charge_detail");
			sb.append(" where");
			sb.append(where);
			sb.append(" and visitid in(select visitid from ").append(fromTable).append(")");
			sb.append(" group by visitid,itemcode,");
			if(HiveJDBCUtil.enabledProcessGp()) {
				sb.append("to_char(prescripttime,'yyyy-mm-dd')");
			} else {
				sb.append("substr(prescripttime,1,10)");
			}
			alias = this.getClass().getSimpleName() + "_drug";
			withTableList.add(new WithTableModel(alias, sb.toString()));
		}
		if(withTableList.size()>=2) {
			//合并项目组、药品组
			sb.setLength(0);
			for(int i=0,len=withTableList.size(); i<len; i++) {
				WithTableModel withTable = withTableList.get(i);
				if(i>0) {
					sb.append(" \nunion\n ");
				}
				sb.append("select * from ").append(withTable.getAlias());
			}
			alias = this.getClass().getSimpleName() + "_union";
			withTableList.add(new WithTableModel(alias, sb.toString()));
		}
		WithTableModel prevWithTable = withTableList.get(withTableList.size()-1);
		//满足依赖关系数据
		sb.setLength(0);
		sb.append("select * from ").append(mainWithTable.getAlias()).append(" x1");
		sb.append(" where exists(select 1 from ").append(prevWithTable.getAlias()).append(" x2");
		sb.append(" where x1.visitid=x2.visitid and x1.daytime=x2.daytime and x1.FUND_COVER>0 and x2.FUND_COVER>0)");
		alias = "table_rely";
		WithTableModel relyWithTable = new WithTableModel(alias, sb.toString());
		withTableList.add(relyWithTable);
		
		//判断条件结果表（白名单）
		sb.setLength(0);
		sb.append("with ").append(mainWithTable.getAlias()).append(" as(").append(mainWithTable.getSql()).append(")");
		for(int i=0,len=withTableList.size(); i<len; i++) {
			sb.append("\n,");
			WithTableModel withTable = withTableList.get(i);
			sb.append(withTable.getAlias()).append(" as(").append(withTable.getSql()).append(")");
		}
		sb.append("\n");
		sb.append("select * from ").append(fromTable);
		sb.append(" where visitid in(select visitid from ").append(alias).append(")");
		return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_JUDGE, sb.toString()); 
	}
	
	/**
	 * 
	 * 功能描述：是否是一日依赖规则
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	private boolean isOnedayRely() {
		List<MedicalRuleConditionSet> conditionList = ruleConditionList.stream().filter(s->"fitTimeRange".equals(s.getField())).collect(Collectors.toList());
		if(conditionList!=null && conditionList.size()>0) {
			MedicalRuleConditionSet bean = conditionList.get(0);
			return "1day".equals(bean.getExt1());
		}
		return false;
	}
}
