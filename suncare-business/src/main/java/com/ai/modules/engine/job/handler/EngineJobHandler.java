/**
 * CaseJobHandler.java	  V1.0   2020年2月11日 下午2:23:32
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.job.handler;

import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.common.utils.ThreadUtils;
import com.ai.modules.engine.job.meta.EngineJobMeta;
import com.ai.modules.engine.job.meta.JobMeta;
import com.ai.modules.engine.service.IEngineCaseService;
import com.ai.modules.engine.service.IEngineChargeService;
import com.ai.modules.engine.service.IEngineClinicalService;
import com.ai.modules.engine.service.IEngineDrugService;
import com.ai.modules.engine.service.IEngineDrugUseService;
import com.ai.modules.engine.service.IEngineTreatService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class EngineJobHandler extends AbstractJobHandler {
	private EngineJobMeta meta;

	public EngineJobHandler(JobMeta jobMeta) {
		super(jobMeta);
		this.meta = (EngineJobMeta)jobMeta;
	}

	@Override
	public boolean execute() {
		log.info("batchNo:{},datasource:{}", meta.getBatchNo(), meta.getBaseMeta().getDatasource());
		ApplicationContext context = SpringContextUtils.getApplicationContext();		
		try {
			String datasource = meta.getBaseMeta().getDatasource();
			//设置solr数据源
			ThreadUtils.setDatasource(datasource);
			//任务批次号
			String batchId = meta.getBatchNo();
			
			IEngineCaseService service = context.getBean(IEngineCaseService.class);
			//不合规模型			
			service.generateUnreasonableAction(batchId);
		 	//不合规临床路径
		 	IEngineClinicalService clinicalService = context.getBean(IEngineClinicalService.class);
		 	clinicalService.generateUnreasonableAction(batchId);
		    //药品不合规
		 	IEngineDrugService drugService = context.getBean(IEngineDrugService.class);
		 	drugService.generateUnreasonableAction(batchId);
		 	//收费不合规
		 	IEngineChargeService chargeService = context.getBean(IEngineChargeService.class);
		 	chargeService.generateUnreasonableAction(batchId);
		 	//不合理诊疗
		 	IEngineTreatService treatService = context.getBean(IEngineTreatService.class);
		 	treatService.generateUnreasonableAction(batchId);
		 	//不合理用药
		 	IEngineDrugUseService druguseService = context.getBean(IEngineDrugUseService.class);
		 	druguseService.generateUnreasonableAction(batchId);
			return true;
		} catch (Exception e) {
			log.error("", e);
		}
		return false;
	}

}
