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

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.service.IEngineTrialService;
import com.ai.modules.formal.entity.MedicalFormalCase;

/**
 * 
 * 功能描述：模型节点试算记录数线程
 *
 * @author  zhangly
 * Date: 2020年9月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineFormalFlowTrialRunnable extends AbsEngineRunnable {	
	private MedicalFormalCase formalCase;
	private List<EngineNode> flow;

	public EngineFormalFlowTrialRunnable(String datasource, MedicalFormalCase formalCase, List<EngineNode> flow) {
		super(datasource);
		this.formalCase = formalCase;
		this.flow = flow;
	}

	@Override
	public void execute() throws Exception {
		IEngineTrialService service = SpringContextUtils.getApplicationContext().getBean(IEngineTrialService.class);
		service.trialCaseFlowCnt(formalCase, flow);
	}
}
