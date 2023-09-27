/**
 * EngineDrugRuleResult.java	  V1.0   2020年7月29日 上午9:13:15
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.medical.entity.MedicalDrugRule;

public class EngineDrugRuleEntity {
	private MedicalDrugRule rule;
	private Map<String, EngineLimitScopeEnum> limitScopeEnumMap;
	
	public EngineDrugRuleEntity(MedicalDrugRule rule) {
		this.rule = rule;
		if(StringUtils.isNotBlank(rule.getLimitScope())) {
			//限制范围
	  		String[] limitScope = rule.getLimitScope().split(",");
	  		Map<String, EngineLimitScopeEnum> limitScopeEnumMap = new HashMap<String, EngineLimitScopeEnum>();
	  		for(String scope : limitScope) {
	  			EngineLimitScopeEnum limitScopeEnum = EngineLimitScopeEnum.enumValueOf(scope);
	  			if(limitScopeEnum!=null) {
	  				limitScopeEnumMap.put(scope, limitScopeEnum);
	  			}
	  		}
	  		this.limitScopeEnumMap = limitScopeEnumMap;
		}
	}
	
	public EngineDrugRuleEntity(MedicalDrugRule rule, Map<String, EngineLimitScopeEnum> limitScopeEnumMap) {
		this.rule = rule;
		this.limitScopeEnumMap = limitScopeEnumMap;
	}
	
	public Set<String> getLimitScopeSet() {
		if(isEmptyScope()) {
			return null;
		}
		return limitScopeEnumMap.keySet();
	}
	
	public boolean isEmptyScope() {
		return limitScopeEnumMap==null || limitScopeEnumMap.size()==0;
	}
	
	public boolean singleScope() {
		return (limitScopeEnumMap!=null && limitScopeEnumMap.size()==1) ? true : false;
	}
	
	public EngineLimitScopeEnum getFirstScopeEnum() {
		if(limitScopeEnumMap!=null && limitScopeEnumMap.size()>0) {
			return limitScopeEnumMap.entrySet().iterator().next().getValue();
		}
		return null;
	}
	
	public MedicalDrugRule getRule() {
		return rule;
	}
	public void setRule(MedicalDrugRule rule) {
		this.rule = rule;
	}
	public Map<String, EngineLimitScopeEnum> getLimitScopeEnumMap() {
		return limitScopeEnumMap;
	}
	public void setLimitScopeEnumMap(Map<String, EngineLimitScopeEnum> limitScopeEnumMap) {
		this.limitScopeEnumMap = limitScopeEnumMap;
	}
}
