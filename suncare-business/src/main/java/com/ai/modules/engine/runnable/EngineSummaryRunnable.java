/**
 * EngineDrugRunnable.java	  V1.0   2020年9月21日 上午10:48:51
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.runnable;

import org.jeecg.common.util.SpringContextUtils;

import com.ai.modules.engine.service.IEngineSummaryService;
import com.ai.modules.task.entity.TaskActionFieldConfig;

/**
 *
 * 功能描述：批次不合规行为汇总
 *
 * @author  zhangly
 * Date: 2020年9月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineSummaryRunnable extends AbsEngineRunnable {

	private String batchId;
	private String actionName;
	private TaskActionFieldConfig config;

	public EngineSummaryRunnable(String datasource, String batchId, String actionName, TaskActionFieldConfig config) {
		super(datasource);
		this.batchId = batchId;
		this.actionName = actionName;
		this.config = config;
	}

	@Override
	public void execute() throws Exception {
		IEngineSummaryService service = SpringContextUtils.getApplicationContext().getBean(IEngineSummaryService.class);
		service.summary(batchId, actionName, config);
	}
}
