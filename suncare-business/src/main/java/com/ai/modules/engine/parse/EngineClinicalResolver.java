/**
 * EngineUtil.java	  V1.0   2019年11月28日 下午3:34:22
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.handle.clinical.AbsClinicalRuleHandle;
import com.ai.modules.engine.handle.clinical.ClinicalDiseaseGrpRuleHandle;
import com.ai.modules.engine.handle.clinical.ClinicalDrugGrpRuleHandle;
import com.ai.modules.engine.handle.clinical.ClinicalSplitRuleHandle;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.clinical.EngineClinicalRule;
import com.ai.modules.engine.util.EngineUtil;

public class EngineClinicalResolver {	
	//不合理病例表与业务表关联关系
	public final static Map<String, EngineMapping> ENGIME_CLINICAL_MAPPING = new HashMap<String, EngineMapping>();
	static {
		EngineMapping mapping = new EngineMapping(EngineUtil.DWB_DOCTOR, "DOCTORID", "DOCTORID");
		ENGIME_CLINICAL_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping(EngineUtil.DWB_CLIENT, "CLIENTID", "CLIENTID");
		ENGIME_CLINICAL_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping(EngineUtil.DWB_CHARGE_DETAIL, "VISITID", "VISITID");
		ENGIME_CLINICAL_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_SETTLEMENT", "VISITID", "VISITID");
		ENGIME_CLINICAL_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("STD_ORGANIZATION", "ORGID", "ORGID");
		ENGIME_CLINICAL_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_DEPARTMENT", "DEPTID", "DEPTID");
		ENGIME_CLINICAL_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_CHRONIC_PATIENT", "CLIENTID", "CLIENTID");
		ENGIME_CLINICAL_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_DIAG", "VISITID", "VISITID");
		ENGIME_CLINICAL_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_ORDER", "VISITID", "VISITID");
		ENGIME_CLINICAL_MAPPING.put(mapping.getFromIndex(), mapping);
	}
	
	private static EngineClinicalResolver instance = new EngineClinicalResolver();
	
	private EngineClinicalResolver() {
		
	}
	
	public static EngineClinicalResolver getInstance() {
		return instance;
	}

	/**
	 *
	 * 功能描述：准入条件解析成查询条件字符串
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年11月28日 下午5:02:55</p>
	 *
	 * @param rule
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public String parseConditionExpression(EngineClinicalRule rule) {
		StringBuilder sb = new StringBuilder();
		List<String> conditionList = new ArrayList<String>();
		//年龄限制
		String ageField = "YEARAGE";
		if(StringUtils.isNotBlank(rule.getAgeUnit())) {
			if("月".equals(rule.getAgeUnit())) {
				ageField = "MONTHAGE";
			} else if("日".equals(rule.getAgeUnit())) {
				ageField = "DAYAGE";
			}
		}
		if(rule.getMinAge()!=null && rule.getMaxAge()!=null) {			
			conditionList.add(ageField+":["+rule.getMinAge()+" TO "+rule.getMaxAge()+"]");
		} else if(rule.getMinAge()!=null) {
			conditionList.add(ageField+":["+rule.getMinAge()+" TO *]");
		} else if(rule.getMaxAge()!=null) {
			conditionList.add(ageField+":[* TO "+rule.getMaxAge()+"]");
		}
		List<AbsClinicalRuleHandle> ruleHandleList = new ArrayList<AbsClinicalRuleHandle>();
		//疾病组
		if(StringUtils.isNotBlank(rule.getDiseaseGroupCode())) {
			String colName = "DISEASECODE";
			String tableName = EngineUtil.DWB_DIAG;			
			ruleHandleList.add(new ClinicalDiseaseGrpRuleHandle(tableName, colName, rule.getDiseaseGroupCode()));			
		}
		//手术
		if(StringUtils.isNotBlank(rule.getOperationCode())) {
			String colName = "ITEMCODE";
			String tableName = EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM;
			ruleHandleList.add(new ClinicalSplitRuleHandle(tableName, colName, rule.getOperationCode()));			
		}
		//诊疗项目
		if(StringUtils.isNotBlank(rule.getTreatCode())) {			
			String colName = "ITEMCODE";
			String tableName = EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM;
			ruleHandleList.add(new ClinicalSplitRuleHandle(tableName, colName, rule.getTreatCode()));
		}
		//药品组
		if(StringUtils.isNotBlank(rule.getDurgGroupCode())) {
			String colName = "ITEMCODE";
			String tableName = EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM;			
			ruleHandleList.add(new ClinicalDrugGrpRuleHandle(tableName, colName, rule.getDurgGroupCode()));
		}
		//病理形态
		if(StringUtils.isNotBlank(rule.getPathologys())) {
			String colName = "DISEASECODE";
			String tableName = EngineUtil.DWB_DIAG;			
			ruleHandleList.add(new ClinicalSplitRuleHandle(tableName, colName, rule.getPathologys()));
		}
		
		for(AbsClinicalRuleHandle ruleHandle : ruleHandleList) {
			String condition = ruleHandle.where();
			if(StringUtils.isNotBlank(condition)) {
				conditionList.add(condition);
			}
		}
		
		//遍历所有查询条件
		sb.setLength(0);
		for(int i=0, len=conditionList.size(); i<len; i++) {
			if(i>0) {
				sb.append(" AND ");
			}
			sb.append(conditionList.get(i));
		}
		return sb.toString();
	}
	
	public List<String> parseConditionExpression(EngineClinicalRule rule, List<EngineClinicalRule> excludeList) {
		List<String> conditionList = new ArrayList<String>();
		String condition = this.parseConditionExpression(rule);
		if(StringUtils.isNotBlank(condition)) {
			conditionList.add(condition);
		}
		if(excludeList!=null) {
			for(EngineClinicalRule exclude : excludeList) {
				condition = this.parseConditionExpression(exclude);
				if(StringUtils.isNotBlank(condition)) {
					condition = "-(".concat(condition).concat(")");
					conditionList.add(condition);
				}
			}
		}
		return conditionList;
	}
}
