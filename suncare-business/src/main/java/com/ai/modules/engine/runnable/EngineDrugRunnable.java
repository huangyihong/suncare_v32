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

import java.util.List;

import org.jeecg.common.util.SpringContextUtils;

import com.ai.modules.engine.service.IEngineDrugService;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：药品合规任务批次线程
 *
 * @author  zhangly
 * Date: 2020年9月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineDrugRunnable extends AbsEngineRunnable {	
	private TaskProject task;
	private TaskProjectBatch batch;
	private String drugCode;
	private List<MedicalDrugRule> ruleList;

	public EngineDrugRunnable(String datasource, boolean redo, TaskProject task, TaskProjectBatch batch,
			String drugCode, List<MedicalDrugRule> ruleList) {
		super(datasource, redo);
		this.task = task;
		this.batch = batch;
		this.drugCode = drugCode;
		this.ruleList = ruleList;
	}

	@Override
	public void execute() throws Exception {
		IEngineDrugService service = SpringContextUtils.getApplicationContext().getBean(IEngineDrugService.class);
		service.generateMedicalUnreasonableDrugAction(task, batch, drugCode, ruleList);
	}
}
