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

import com.ai.modules.engine.service.IEngineDrugUseService;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：用药合规任务批次线程
 *
 * @author  zhangly
 * Date: 2020年11月10日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineDrugUseRunnable extends AbsEngineRunnable {	
	private TaskProject task;
	private TaskProjectBatch batch;
	private MedicalRuleConfig rule;

	public EngineDrugUseRunnable(String datasource, boolean redo, TaskProject task, TaskProjectBatch batch,
			MedicalRuleConfig rule) {
		super(datasource, redo);
		this.task = task;
		this.batch = batch;
		this.rule = rule;
	}

	@Override
	public void execute() throws Exception {
		IEngineDrugUseService service = SpringContextUtils.getApplicationContext().getBean(IEngineDrugUseService.class);
		service.generateMedicalUnreasonableDrugAction(task, batch, rule);
	}
}
