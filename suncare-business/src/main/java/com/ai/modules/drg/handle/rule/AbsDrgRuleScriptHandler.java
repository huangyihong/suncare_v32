/**
 * AbsRuleScriptHandler.java	  V1.0   2023年2月16日 上午9:01:31
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.drg.handle.rule;

import com.ai.modules.drg.handle.model.DrgResultModel;
import com.ai.modules.drg.handle.model.DrgRuleModel;
import com.ai.modules.drg.handle.model.TaskBatchModel;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleScriptResult;
import com.ai.modules.ybChargeSearch.vo.DatasourceAndDatabaseVO;

import java.util.List;

public abstract class AbsDrgRuleScriptHandler {

	protected TaskBatchModel batchModel;
	protected DrgRuleModel ruleModel;
	protected DatasourceAndDatabaseVO dbVO;

	public AbsDrgRuleScriptHandler(TaskBatchModel batchModel, DrgRuleModel ruleModel, DatasourceAndDatabaseVO dbVO) {
		this.batchModel = batchModel;
		this.ruleModel = ruleModel;
		this.dbVO = dbVO;
	}

	/**
	 *
	 * 功能描述：执行脚本
	 * @author zhangly
	 * @date 2023-04-26 17:51:23
	 *
	 * @param
	 *
	 * @return void
	 *
	 */
	public abstract void execute() throws Exception;

	/**
	 *
	 * 功能描述：解析规则的sql脚本
	 * @author zhangly
	 * @date 2023-02-17 14:15:30
	 *
	 * @param
	 *
	 * @return com.ai.modules.ybChargeSearch.handle.rule.model.RuleScriptResult
	 *
	 */
	public abstract RuleScriptResult parseRuleScript();

	/**
	 *
	 * 功能描述：解析规则限定内容的查询条件
	 * @author zhangly
	 * @date 2023-02-17 14:14:54
	 *
	 * @param
	 *
	 * @return java.util.List<java.lang.String>
	 *
	 */
	public abstract String parseRuleWhere();
}
