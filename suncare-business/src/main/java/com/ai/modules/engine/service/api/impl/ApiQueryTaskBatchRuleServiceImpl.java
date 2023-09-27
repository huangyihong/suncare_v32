/**
 * QueryTaskBatchRuleServiceImpl.java	  V1.0   2020年12月22日 上午9:18:16
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.ai.modules.api.rsp.ApiResponse;
import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.engine.service.api.IApiQueryTaskBatchRuleService;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.entity.MedicalDruguseRuleGroup;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

@Service
public class ApiQueryTaskBatchRuleServiceImpl implements IApiQueryTaskBatchRuleService {

	@Override
	public List<MedicalRuleConfig> queryMedicalRuleConfig(String batchId, String stepType) {
		Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("batchId", batchId);
        busiParams.put("stepType", stepType);
        List<MedicalRuleConfig> result = ApiOauthUtil.responseArray("/oauth/api/engine/new/rule", busiParams, "post", MedicalRuleConfig.class);
        return result;
	}
	
	@Override
	public List<MedicalRuleConfig> queryMedicalRuleConfigFail(String batchId, String stepType) {
		Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("batchId", batchId);
        busiParams.put("stepType", stepType);
        List<MedicalRuleConfig> result = ApiOauthUtil.responseArray("/oauth/api/engine/new/rule/fail", busiParams, "post", MedicalRuleConfig.class);
        return result;
	}
	
	private List<MedicalRuleConfig> queryMedicalRuleConfig(List<String> ruleIds) {
		int size = ruleIds.size();
		int pageSize = 1000;
		List<MedicalRuleConfig> result = new ArrayList<MedicalRuleConfig>();
		if(size<pageSize) {
			result = this.queryMedicalRuleConfigByApi(ruleIds);
		} else {
    		int pageNum = (ruleIds.size() + pageSize - 1) / pageSize;
    		//数据分割
    		List<List<String>> mglist = new ArrayList<>();
    	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
    	    	mglist.add(ruleIds.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
    	    });
    	    for(List<String> sublist : mglist) {
    	    	List<MedicalRuleConfig> list = this.queryMedicalRuleConfigByApi(sublist);
    	    	if(list!=null) {
    	    		result.addAll(list);
    	    	}
    	    }
		}
		return result;
	}

	private List<MedicalRuleConfig> queryMedicalRuleConfigByApi(List<String> ruleIds) {
		Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("ruleIds", StringUtils.join(ruleIds, ","));
        List<MedicalRuleConfig> result = ApiOauthUtil.responseArray("/oauth/api/engine/new/rule", busiParams, "post", MedicalRuleConfig.class);
        return result;
	}

	@Override
	public List<MedicalRuleConditionSet> queryMedicalRuleConditionSet(String ruleId) {
		Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("ruleId", ruleId);
        List<MedicalRuleConditionSet> ruleConditionList = ApiOauthUtil.responseArray("/oauth/api/engine/new/ruleConditionList", busiParams, "post", MedicalRuleConditionSet.class);
        return ruleConditionList;
	}

	@Override
	public MedicalRuleConfig queryMedicalRuleConfig(String ruleId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("ruleId", ruleId);
    	MedicalRuleConfig rule = ApiOauthUtil.response("/oauth/api/engine/new/ruleById", busiParams, "post", MedicalRuleConfig.class);
    	return rule;
	}

	@Override
	public List<MedicalDrugRule> queryMedicalDrugRule(String batchId, String stepType) {
		Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("batchId", batchId);
        busiParams.put("stepType", stepType);
        List<MedicalDrugRule> result = ApiOauthUtil.responseArray("/oauth/api/engine/rule", busiParams, "post", MedicalDrugRule.class);
		return result;
	}
	
	@Override
	public List<MedicalDrugRule> queryMedicalDrugRuleFail(String batchId, String stepType) {
		Map<String, String> busiParams = new HashMap<String, String>();
        busiParams.put("batchId", batchId);
        busiParams.put("stepType", stepType);
        List<MedicalDrugRule> result = ApiOauthUtil.responseArray("/oauth/api/engine/rule/fail", busiParams, "post", MedicalDrugRule.class);
		return result;
	}

	@Override
	public List<MedicalDrugRule> queryMedicalDrugRuleByRuleid(String ruleId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("ruleId", ruleId);
    	List<MedicalDrugRule> ruleList = ApiOauthUtil.responseArray("/oauth/api/engine/ruleById", busiParams, "post", MedicalDrugRule.class);
    	return ruleList;
	}

	@Override
	public String getDrugname(String itemcode) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("itemcode", itemcode);
    	ApiResponse<?> rsp = ApiOauthUtil.response("/oauth/api/engine/queryDrugName", busiParams, "post");
    	if(rsp.isSuccess() && rsp.getResult()!=null) {
    		return rsp.getResult().toString();
    	}
    	return null;
	}

	@Override
	public List<MedicalDrugRule> queryMedicalDrugRuleByItem(String batchId, String stepType, String itemCode) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	busiParams.put("stepType", stepType);
    	busiParams.put("itemCode", itemCode);
    	List<MedicalDrugRule> ruleList = ApiOauthUtil.responseArray("/oauth/api/engine/ruleByItem", busiParams, "post", MedicalDrugRule.class);
    	return ruleList;
	}

	@Override
	public String getTreatname(String itemcode) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("itemcode", itemcode);
    	ApiResponse<?> rsp = ApiOauthUtil.response("/oauth/api/engine/queryTreatName", busiParams, "post");
    	if(rsp.isSuccess() && rsp.getResult()!=null) {
    		return rsp.getResult().toString();
    	}
    	return null;
	}

	@Override
	public List<MedicalDruguse> queryMedicalDruguseByBatchid(String batchId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	List<MedicalDruguse> ruleList = ApiOauthUtil.responseArray("/oauth/api/engine/druguse", busiParams, "post", MedicalDruguse.class);
    	return ruleList;
	}

	@Override
	public List<MedicalDruguse> queryMedicalDruguseFail(String batchId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	List<MedicalDruguse> ruleList = ApiOauthUtil.responseArray("/oauth/api/engine/druguse/fail", busiParams, "post", MedicalDruguse.class);
    	return ruleList;
	}

	@Override
	public MedicalDruguse queryMedicalDruguse(String ruleId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("ruleId", ruleId);
    	MedicalDruguse rule = ApiOauthUtil.response("/oauth/api/engine/druguse/byId", busiParams, "post", MedicalDruguse.class);
    	return rule;
	}

	@Override
	public List<MedicalDruguseRuleGroup> queryMedicalDruguseRuleGroup(String ruleId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("ruleId", ruleId);
    	List<MedicalDruguseRuleGroup> ruleGroupList = ApiOauthUtil.responseArray("/oauth/api/engine/druguse/ruleGroup", busiParams, "post", MedicalDruguseRuleGroup.class);
    	return ruleGroupList;
	}

	@Override
	public boolean existsMedicalRuleEngine(String ruleId, String ruleType) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("ruleId", ruleId);
    	busiParams.put("ruleType", ruleType);
    	ApiResponse<?> rsp = ApiOauthUtil.response("/oauth/api/engine/openHive", busiParams, "post");
    	if(rsp.isSuccess() && rsp.getResult()!=null) {
    		int count = Integer.parseInt(rsp.getResult().toString());
    		return count>0;
    	}
    	return false;
	}

	@Override
	public List<MedicalYbDrug> queryMedicalDrugrepeat() {
    	List<MedicalYbDrug> ruleList = ApiOauthUtil.responseArray("/oauth/api/engine/drugrepeat", null, "post", MedicalYbDrug.class);
    	return ruleList;
	}

	@Override
	public List<MedicalYbDrug> queryMedicalDrugrepeatFail(String batchId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("batchId", batchId);
    	List<MedicalYbDrug> ruleList = ApiOauthUtil.responseArray("/oauth/api/engine/drugrepeat/fail", busiParams, "post", MedicalYbDrug.class);
    	return ruleList;
	}

	@Override
	public List<MedicalYbDrug> queryMedicalDrugrepeat(String ruleId) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("parentCode", ruleId);
    	List<MedicalYbDrug> ruleList = ApiOauthUtil.responseArray("/oauth/api/engine/drugrepeat/byParent", busiParams, "post", MedicalYbDrug.class);
    	return ruleList;
	}
}
