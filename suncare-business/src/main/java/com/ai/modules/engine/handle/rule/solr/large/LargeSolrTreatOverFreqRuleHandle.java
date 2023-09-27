/**
 * SolrIndicationRuleHandle.java	  V1.0   2021年3月22日 下午3:12:07
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.solr.large;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.handle.rule.parse.RuleIgnoreNullParser;
import com.ai.modules.engine.handle.secondary.AbsRuleSecondHandle;
import com.ai.modules.engine.handle.secondary.LargeTreatRuleOverFreq;
import com.ai.modules.engine.model.rule.EngineParamGrpRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;

/**
 * 
 * 功能描述：合理诊疗一日就诊限频次规则
 *
 * @author  zhangly
 * Date: 2021年3月24日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class LargeSolrTreatOverFreqRuleHandle extends LargeSolrRuleHandle {
	
	public LargeSolrTreatOverFreqRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
			List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, rule, ruleConditionList);
	}
	
	@Override
	public void generateUnreasonableAction() throws Exception {
		if(ignoreRun()) {
			//忽略运行
			return;
		}
		boolean hive = true;
        this.removeMedicalChargeDetail(hive);
        //过滤明细数据        	        	
    	this.syncMedicalChargeDetail();
    			
		String itemcode = rule.getItemCodes();
		if(itemcode.indexOf(",")>-1) {
			itemcode = "(" + StringUtils.replace(itemcode, ",", " OR ") + ")";
		}
		List<String> conditionList = new ArrayList<String>();
		conditionList.add("BATCH_ID:"+batch.getBatchId());
  		conditionList.add("RULE_ID:"+rule.getRuleId());
        conditionList.add("ITEMCODE:" + itemcode);
        conditionList.add("ITEM_QTY:{0 TO *}");
		conditionList.add("ITEM_AMT:{0 TO *}");
		conditionList.addAll(this.parseCommonCondition());
		//添加准入与限定条件
		conditionList.addAll(this.parseCondition());
        //添加过滤掉指标为空值的条件
		RuleIgnoreNullParser ignoreNullParser = new RuleIgnoreNullParser(rule, ruleConditionList);
  		conditionList.addAll(ignoreNullParser.ignoreNullWhere());
  		
  		int count = this.handle(conditionList, EngineUtil.MEDICAL_PATIENT_1VISIT_ITEMSUM);
        if(count>0) {
        	//规则二次处理
        	AbsRuleSecondHandle handle = new LargeTreatRuleOverFreq(task, batch, rule, ruleConditionList, trail);        	
        	handle.execute();
        	
        	String busiType = this.getBusiType();
            //同步数据
            syncUnreasonableAction(rule, busiType, false);
            //不合规行为汇总
            String[] fqs = new String[] {"RULE_ID:"+rule.getRuleId()};
            engineActionService.executeGroupBy(batch.getBatchId(), rule.getActionId(), fqs);
        }   
        
        //处理完删除明细数据
      	this.removeMedicalChargeDetail(hive);
	}
	
	/**
	 * 
	 * 功能描述：不合规查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月9日 下午5:49:29</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected List<String> parseCondition() {
		List<String> result = new ArrayList<String>();
	    if(ruleConditionList!=null) {
	    	//准入条件
	    	String condition = this.parseAccessCondition();
	    	if(StringUtils.isNotBlank(condition)) {
	    		result.add(condition);
	    	}
	    	//判断条件
			/*condition = this.parseJudgeCondition();
			if(StringUtils.isBlank(condition)) {
				throw new RuntimeException(rule.getItemNames()+"规则未配置判断条件！");
			}
			result.add(condition);*/
	    }	            
	    return result;
	}
	
	/**
	 * 
	 * 功能描述：解析判断条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月24日 下午3:13:34</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	@Override
	protected String parseJudgeCondition() {		
		//限定条件
		List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", null);
		if(judgeList!=null) {
			List<String> wheres = new ArrayList<String>();
			for(MedicalRuleConditionSet bean : judgeList) {
				String where = this.parseCondition(bean);
				if(StringUtils.isNotBlank(where)) {
					wheres.add(where);
				}
			}
			String condition = StringUtils.join(wheres, " OR ");
			//黑名单取反
			condition = "*:* -(" + condition + ")";
			return condition;
		}    	
    	return null;
	}
	
	private String parseCondition(MedicalRuleConditionSet bean) {
		StringBuilder sb = new StringBuilder();
		String condiType = bean.getField();
		String frequency = bean.getExt2();
		if(StringUtils.isBlank(frequency)) {
			return null;
		}
		if(AbsRuleParser.RULE_CONDI_FREQUENCY.equals(condiType)) {
			String compare = bean.getCompare();			
			sb.append("_query_:\"");
			String fromIndex = "MAPPER_DWS_PATIENT_CHARGEITEM_SUM_D";
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(fromIndex, "VISITID", "VISITID");
			sb.append(plugin.parse());
			plugin = new SolrJoinParserPlugin("DWS_PATIENT_CHARGEITEM_SUM", "id", "DWSID");
			sb.append(plugin.parse());
			sb.append("ITEMCODE:"+rule.getItemCodes());
			sb.append(" AND ITEM_QTY:");
			if(">".equals(compare)) {
				sb.append("{").append(frequency).append(" TO *}");
			} else if(">=".equals(compare)) {
				sb.append("[").append(frequency).append(" TO *}");
			} else if("<".equals(compare)) {
				sb.append("{* TO ").append(frequency).append("}");
			} else if("<=".equals(compare)) {
				sb.append("{* TO ").append(frequency).append("]");
			} else {
				sb.append(frequency);
			}
			sb.append("\"");
						
			if(StringUtils.isNotBlank(bean.getExt4())) {			
				//项目组前提条件
				sb.append(" AND ");
				EngineParamGrpRule paramRule = new EngineParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", bean.getExt4());
				paramRule.setRelation("2");
				if("≠".equals(bean.getExt3())) {
					sb.append("(*:* -").append(paramRule.where()).append(")");
				} else {
					sb.append(paramRule.where());
				}
			}
		}
        return "(" + sb.toString() + ")";
	}
	
	/**
	 * 
	 * 功能描述：通过hive模式筛选满足条件的DWB_CHARGE_DETAIL数据同步solr
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年12月9日 下午3:10:59</p>
	 *
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private void syncMedicalChargeDetail() throws Exception {
		List<String> wheres = this.filterHiveWheres();
		StringBuilder sb = new StringBuilder();
		String column = ApiOauthUtil.getProperty("MEDICAL_CHARGE_DETAIL");
		sb.append("insert overwrite table medical_gbdp.medical_charge_detail partition(batch_id='").append(batch.getBatchId()).append("',rule_id='").append(rule.getRuleId()).append("')");
		sb.append(" select ").append(column).append(" from dwb_charge_detail a");
		sb.append(" where a.itemcode='").append(rule.getItemCodes()).append("'");
		for(String condition : wheres) {
			sb.append(" ").append(condition);
		}
		HiveJDBCUtil.execute(sb.toString());		
		
		List<MedicalRuleConditionSet> judgeList = ruleConditionList.stream().filter(s->"judge".equals(s.getType())).collect(Collectors.toList());
  		//项目组
    	Set<String> mutexCodeSet = new HashSet<String>();
    	boolean exists = true;
    	for(MedicalRuleConditionSet bean : judgeList) {
    		if(StringUtils.isNotBlank(bean.getExt4())) {
    			mutexCodeSet.add(bean.getExt4());
    		}
    		if(StringUtils.isBlank(bean.getExt4()) || "≠".equals(bean.getExt3())) {
    			exists = false;
			}
			if("=".equals(bean.getExt3()) && StringUtils.isNotBlank(bean.getExt4())) {
				exists = exists && true;
			}
    	}
    	if(mutexCodeSet.size()>0) {
    		String groupcode = "('" + StringUtils.join(mutexCodeSet, "','") + "')";
        	sb.setLength(0);
    		sb.append("insert into medical_gbdp.medical_charge_detail partition(batch_id='").append(batch.getBatchId()).append("',rule_id='").append(rule.getRuleId()).append("')");
    		sb.append(" select ").append(column).append(" from dwb_charge_detail a");
    		sb.append(" left semi join medical_gbdp.STD_TREATGROUP std on std.treatcode=a.itemcode");
    		sb.append(" and std.treatgroup_code in").append(groupcode);    		
    		sb.append(" left semi join medical_gbdp.medical_charge_detail b on a.visitid=b.visitid");
    		sb.append(" and b.batch_id='").append(batch.getBatchId()).append("'");
    		sb.append(" and b.rule_id='").append(rule.getRuleId()).append("'");
    		sb.append(" where a.itemcode!='").append(rule.getItemCodes()).append("'");
    		HiveJDBCUtil.execute(sb.toString());
    		
    		if(exists) {
    			//筛选同一天既有主体项目又有互斥项目的病例明细数据
    			sb.setLength(0);
        		sb.append("insert overwrite table medical_gbdp.medical_charge_detail partition(batch_id='").append(batch.getBatchId()).append("',rule_id='").append(rule.getRuleId()).append("')");
        		sb.append(" select ").append(column).append(" from medical_gbdp.medical_charge_detail x");
        		sb.append(" where x.visitid in(select a.visitid from medical_gbdp.medical_charge_detail a join medical_gbdp.medical_charge_detail b");
        		sb.append(" on a.visitid=b.visitid and a.batch_id=b.batch_id and a.rule_id=b.rule_id");
        		sb.append(" where a.batch_id='").append(batch.getBatchId()).append("'");
        		sb.append(" and a.rule_id='").append(rule.getRuleId()).append("'");
        		sb.append(" and a.itemcode='").append(rule.getItemCodes()).append("'");
        		sb.append(" and b.itemcode!='").append(rule.getItemCodes()).append("'");
        		sb.append(" and from_unixtime(unix_timestamp(a.prescripttime, 'yyyy-MM-dd'), 'yyyy-MM-dd')=from_unixtime(unix_timestamp(b.prescripttime, 'yyyy-MM-dd'), 'yyyy-MM-dd')");
        		sb.append(")");
        		HiveJDBCUtil.execute(sb.toString());
    		}
    	}
    	//开始同步medical_charge_detail数据到solr		
    	this.syncSolrFromHive("MEDICAL_CHARGE_DETAIL");
		
		column = ApiOauthUtil.getProperty("MEDICAL_PATIENT_1VISIT_ITEMSUM");
		sb.setLength(0);
		sb.append("insert overwrite table medical_gbdp.medical_patient_1visit_itemsum partition(batch_id='").append(batch.getBatchId()).append("',rule_id='").append(rule.getRuleId()).append("')");
		sb.append(" select ").append(column).append(" from dws_patient_1visit_itemsum a");
		sb.append(" left semi join medical_gbdp.medical_charge_detail b on a.visitid=b.visitid and a.itemcode=b.itemcode");
		sb.append(" and b.batch_id='").append(batch.getBatchId()).append("'");
		sb.append(" and b.rule_id='").append(rule.getRuleId()).append("'");
		HiveJDBCUtil.execute(sb.toString());		
		//开始同步medical_patient_1visit_itemsum数据到solr		
		this.syncSolrFromHive("MEDICAL_PATIENT_1VISIT_ITEMSUM");
	}
}
