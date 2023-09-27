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

import com.ai.modules.engine.service.IEngineTreatService;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：诊疗合规任务批次线程
 *
 * @author  zhangly
 * Date: 2020年9月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineTreatRunnable extends AbsEngineRunnable {	
	private TaskProject task;
	private TaskProjectBatch batch;
	private String itemcode;
	private List<MedicalDrugRule> ruleList;

	public EngineTreatRunnable(String datasource, boolean redo, TaskProject task, TaskProjectBatch batch,
			String itemcode, List<MedicalDrugRule> ruleList) {
		super(datasource, redo);
		this.task = task;
		this.batch = batch;
		this.itemcode = itemcode;
		this.ruleList = ruleList;
	}

	@Override
	public void execute() throws Exception {
		IEngineTreatService service = SpringContextUtils.getApplicationContext().getBean(IEngineTreatService.class);
		service.generateMedicalUnreasonableTreatAction(task, batch, itemcode, ruleList);
	}
}
