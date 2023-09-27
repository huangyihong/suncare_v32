/**
 * RuleRegexParserFactory.java	  V1.0   2023年2月16日 上午11:14:19
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.ybChargeSearch.handle.rule;

import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.entity.YbChargeDrugRule;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleLimitModel;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleTaskModel;
import com.ai.modules.ybChargeSearch.handle.rule.regex.AbsRuleRegexParser;
import com.ai.modules.ybChargeSearch.handle.rule.regex.RuleRegexParser;
import com.ai.modules.ybChargeSearch.handle.rule.script.AbsRuleScriptHandler;
import com.ai.modules.ybChargeSearch.handle.rule.script.HisRuleScriptHandler;
import com.ai.modules.ybChargeSearch.handle.rule.script.RuleScriptHandler;
import com.ai.modules.ybChargeSearch.vo.YbChargeQueryDatabase;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.util.DateUtils;

public class DcRuleHandleFactory {

	private String limitType;
	private String limitText;

	public DcRuleHandleFactory(String limitType, String limitText) {
		this.limitType = limitType;
		this.limitText = limitText;
	}

	public AbsRuleRegexParser buildRuleRegexParser() {
		RuleLimitModel ruleLimitModel = new RuleLimitModel(limitType, limitText);
		return new RuleRegexParser(ruleLimitModel);
	}

	public AbsRuleScriptHandler bulidRuleScriptHandler(YbChargeDrugRule rule, YbChargeSearchTask task, YbChargeQueryDatabase database) {
		RuleLimitModel ruleLimitModel = new RuleLimitModel(limitType, limitText);
		RuleTaskModel ruleTaskModel = new RuleTaskModel();
		ruleTaskModel.setRuleLimitModel(ruleLimitModel);
		ruleTaskModel.setDbType(database.getDbtype());
		ruleTaskModel.setRuleName(rule.getDrugName());
		ruleTaskModel.setOrgname(task.getOrgs());
		ruleTaskModel.setOrgid(task.getOrgids());
		ruleTaskModel.setStartDate(DateUtils.formatDate(task.getChargedateStartdate(), "yyyy-MM-dd"));
		ruleTaskModel.setEndDate(DateUtils.formatDate(task.getChargedateEnddate(), "yyyy-MM-dd")+" 23:59:59");
		ruleTaskModel.setVisittype(task.getVisittype());
		ruleTaskModel.setClientname(task.getClientname());
		ruleTaskModel.setIdNo(task.getIdNo());
		ruleTaskModel.setVisitid(task.getVisitid());
		if(task.getLeavedate()!=null) {
			ruleTaskModel.setLeavedate(DateUtils.formatDate(task.getLeavedate(), "yyyy-MM-dd"));
		}
		ruleTaskModel.setEtlSource(task.getEtlSource());
		ruleTaskModel.setDataStaticsLevel(task.getDataStaticsLevel());
		ruleTaskModel.setCaseid(task.getCaseId());

		if(DcConstants.ETL_SOURCE_HIS.equals(ruleTaskModel.getEtlSource())) {
			return new HisRuleScriptHandler(ruleTaskModel);
		}

		return new RuleScriptHandler(ruleTaskModel);
	}
}
