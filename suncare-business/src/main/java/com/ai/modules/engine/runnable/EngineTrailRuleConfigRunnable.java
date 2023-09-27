/**
 * EngineCaseRunnable.java	  V1.0   2020年9月21日 下午12:24:47
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.runnable;

import org.jeecg.common.util.SpringContextUtils;

import com.ai.modules.engine.service.IEngineRuleService;

/**
 * 
 * 功能描述：不合规试算任务批次线程
 *
 * @author  zhangly
 * Date: 2020年9月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineTrailRuleConfigRunnable extends AbsEngineRunnable {
	
	private String ruleId;
	private String etlSource;

	public EngineTrailRuleConfigRunnable(String datasource, String ruleId, String etlSource) {
		super(datasource);
		this.ruleId = ruleId;
		this.etlSource = etlSource;
	}

	@Override
	public void execute() throws Exception {
		IEngineRuleService service = SpringContextUtils.getApplicationContext().getBean(IEngineRuleService.class);
		service.trailAction(ruleId, etlSource, datasource);
	}	

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getEtlSource() {
		return etlSource;
	}

	public void setEtlSource(String etlSource) {
		this.etlSource = etlSource;
	}
}
