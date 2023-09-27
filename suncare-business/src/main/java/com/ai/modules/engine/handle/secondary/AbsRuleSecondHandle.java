/**
 * AbsSecondaryHandle.java	  V1.0   2020年12月4日 上午10:00:08
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.secondary;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.jeecg.common.util.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;

import cn.hutool.core.date.DateUtil;

public abstract class AbsRuleSecondHandle {
	protected static final Logger logger = LoggerFactory.getLogger(AbsRuleSecondHandle.class);
	
	protected IEngineActionService engineActionService = SpringContextUtils.getApplicationContext().getBean(IEngineActionService.class);
	protected IApiDictService dictSV = SpringContextUtils.getApplicationContext().getBean(IApiDictService.class);
	
	protected TaskProject task;
	//任务批次
	protected TaskProjectBatch batch;
	//规则对象
	protected MedicalRuleConfig rule;
	//规则条件
	protected List<MedicalRuleConditionSet> ruleConditionList;
	protected Boolean trail;
	
	public AbsRuleSecondHandle(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList, Boolean trail) {
		this.task = task;
		this.batch = batch;
		this.rule = rule;
		this.ruleConditionList = ruleConditionList;
		this.trail = trail;
	}
	
	public abstract void execute() throws Exception;
	
	/**
	 * 
	 * 功能描述：违规数据导入hive表
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月23日 上午11:36:50</p>
	 *
	 * @param txtPath
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public boolean loadMedicalBreakRuleAction(String txtPath) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		try {			
			conn = HiveJDBCUtil.getConnection();
			stmt = conn.createStatement();
			String sql = "load data inpath '%s' overwrite into table medical_gbdp.MEDICAL_BREAK_RULE_ACTION partition(batch_id='%s', rule_id='%s')";
			sql = String.format(sql, txtPath, batch.getBatchId(), rule.getRuleId());
			logger.info("data load hive command:"+sql);
			stmt.execute(sql);
			return true;
		} catch(Exception e) {		
			logger.error("", e);
			throw e;
		} finally {
			if(stmt!=null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if(conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}		
	}
	
	protected String line(SolrDocument document) {
		StringBuilder text = new StringBuilder();
		Set<String> fieldSet = new LinkedHashSet<String>();
		fieldSet.add("id");
		fieldSet.add("VISITID");
		fieldSet.add("ORGID");
		fieldSet.add("ORGNAME");
		fieldSet.add("VISITDATE");
		fieldSet.add("CLIENTID");
		fieldSet.add("ID_NO");
		fieldSet.add("CLIENTNAME");
		fieldSet.add("SEX");
		fieldSet.add("BIRTHDAY");
		fieldSet.add("ITEMCODE");
		fieldSet.add("ITEMNAME");
		fieldSet.add("CHARGECLASS_ID");
		fieldSet.add("CHARGECLASS");
		fieldSet.add("ITEM_QTY");
		fieldSet.add("VISITTYPE");
		fieldSet.add("ITEM_AMT");
		fieldSet.add("ZY_DAYS_CALCULATE");
		fieldSet.add("ITEM_DAYAVG_QTY");
		fieldSet.add("ITEM_DAYAVG_AMT");
		for(String key : fieldSet) {
			String value = this.getValue(document, key);
			if(value!=null) {
				text.append(value);
			}
			text.append("\t");
		}
		return text.toString();
	}
	
	private String getValue(SolrDocument document, String key) {
		Object object = document.get(key);
		if(object==null) {
			return null;
		}
		return object.toString();
	}
	
	/**
	 * 
	 * 功能描述：公共查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年12月10日 上午9:44:50</p>
	 *
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected List<String> parseCommonCondition() throws Exception {
		//项目的数据时间范围
  		String project_startTime = MedicalConstant.DEFAULT_START_TIME;
		String project_endTime = MedicalConstant.DEFAULT_END_TIME;
		project_startTime = task.getDataStartTime()!=null ? DateUtil.format(task.getDataStartTime(), "yyyy-MM-dd") : project_startTime;
        project_endTime = task.getDataEndTime()!=null ? DateUtil.format(task.getDataEndTime(), "yyyy-MM-dd") : project_endTime;
		//批次的数据时间范围
  		String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
  		String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
  		batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
  		batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
  		//规则的数据时间范围
        String rule_startTime = DateUtil.format(rule.getStartTime(), "yyyy-MM-dd");
        String rule_endTime = DateUtil.format(rule.getEndTime(), "yyyy-MM-dd");
		List<String> conditionList = new ArrayList<String>();
		//项目过滤条件
        ProjectFilterWhereVO filterVO = engineActionService.filterCondition(task, false);
        if(StringUtils.isNotBlank(filterVO.getCondition())) {
        	conditionList.add(filterVO.getCondition());
        }
        if(filterVO.isDiseaseFilter() && EngineUtil.existsDisease(ruleConditionList)) {
        	//疾病映射不全过滤
        	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_DIAG", "VISITID", "VISITID");
        	conditionList.add("*:* -"+plugin.parse()+"-DISEASENAME:?*");
        }
        if (StringUtils.isNotBlank(task.getEtlSource())) {
            conditionList.add("ETL_SOURCE:" + task.getEtlSource());
        }
        if (StringUtils.isNotBlank(batch.getEtlSource())) {
            conditionList.add("ETL_SOURCE:" + batch.getEtlSource());
        }  
        //项目数据时间范围限制
        StringBuilder sb = new StringBuilder();		
		sb.append("VISITDATE:");
		sb.append("[").append(project_startTime).append(" TO ").append(project_endTime).append("]");
        conditionList.add(sb.toString());
        //批次数据时间范围限制
        sb.setLength(0);		
		sb.append("VISITDATE:");
		sb.append("[").append(batch_startTime).append(" TO ").append(batch_endTime).append("]");
        conditionList.add(sb.toString());
        if(StringUtils.isNotBlank(task.getDataOrgFilter())) {
        	//医疗机构范围限制
        	String value = task.getDataOrgFilter();
        	value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
        	conditionList.add("ORGID:"+value);
        }
        if(StringUtils.isNotBlank(batch.getCustomFilter())) {
        	//自定义数据范围限制
        	String value = batch.getCustomFilter();
        	value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
        	conditionList.add("ORGID:"+value);
        }
        //规则的数据时间范围限制
        sb.setLength(0);
		sb.append("VISITDATE:");
		sb.append("[").append(rule_startTime);
		sb.append(" TO ");
		sb.append(rule_endTime).append("]");
		conditionList.add(sb.toString());
		//基金支出金额>0
		conditionList.add("FUND_COVER:{0 TO *}");
		//自付比例<0
		conditionList.add("SELFPAY_PROP_MIN:[0 TO 1}");
		if("1".equals(batch.getYbFundRm0())) {
			//过滤掉病例基金支出金额为0的数据
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
			conditionList.add(plugin.parse()+"FUNDPAY:{0 TO *}");
		}
  		return conditionList;
	}
	
	/**
	 * 
	 * 功能描述：判断主体项目是否为项目组
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	protected boolean isProjectGrp() {
		String itemType = rule.getItemTypes();
		if(MedicalConstant.ITEM_PROJECTGRP.equals(itemType)) {
			return true;
		}
		return false;
	}

	public TaskProject getTask() {
		return task;
	}

	public void setTask(TaskProject task) {
		this.task = task;
	}

	public TaskProjectBatch getBatch() {
		return batch;
	}

	public void setBatch(TaskProjectBatch batch) {
		this.batch = batch;
	}

	public MedicalRuleConfig getRule() {
		return rule;
	}

	public void setRule(MedicalRuleConfig rule) {
		this.rule = rule;
	}

	public List<MedicalRuleConditionSet> getRuleConditionList() {
		return ruleConditionList;
	}

	public void setRuleConditionList(List<MedicalRuleConditionSet> ruleConditionList) {
		this.ruleConditionList = ruleConditionList;
	}

	public boolean isTrail() {
		return trail;
	}

	public void setTrail(Boolean trail) {
		this.trail = trail;
	}
}
