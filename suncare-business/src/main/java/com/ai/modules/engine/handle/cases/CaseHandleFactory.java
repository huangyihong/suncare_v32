/**
 * CaseHandleFactory.java	  V1.0   2022年12月6日 上午11:30:17
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.engine.handle.cases.special.hive.HiveSolrCaseHandle;
import com.ai.modules.engine.handle.cases.special.hive.ImpalaSpecialCaseHandle;
import com.ai.modules.engine.service.api.IApiCaseService;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;

public class CaseHandleFactory {
	
	//数据来源
	protected String datasource;
	protected TaskProject task;
	//任务批次
	protected TaskProjectBatch batch;
	//模型
	protected HisMedicalFormalCase formalCase;
	
	public CaseHandleFactory(String datasource, TaskProject task, TaskProjectBatch batch, HisMedicalFormalCase formalCase) {
		this.datasource = datasource;
		this.task = task;
		this.batch = batch;
		this.formalCase = formalCase;
	}

	public AbsCaseHandle build() throws Exception {
		AbsCaseHandle handle = null;
		boolean isSolr = !HiveJDBCUtil.enabledProcessGp(); //是否solr计算引擎
		if(StringUtils.isBlank(formalCase.getCaseClassify())) {
			if(isSolr) {
				handle = new SolrCaseHandle(datasource, task, batch, formalCase);
			} else {
				handle = new ImpalaCaseHandle(datasource, task, batch, formalCase);
			}
		} else {
			//特殊模型
			ApplicationContext context = SpringContextUtils.getApplicationContext();
			IApiCaseService caseSV = context.getBean(IApiCaseService.class);
			MedicalSpecialCaseClassify classify = caseSV.findMedicalSpecialCaseClassify(formalCase.getCaseClassify());
			if(isSolr && HiveJDBCUtil.isHive()) {
				//solr模式且数仓是hive
				handle = new HiveSolrCaseHandle(datasource, task, batch, formalCase, classify);
			} else {
				handle = new ImpalaSpecialCaseHandle(datasource, task, batch, formalCase, classify);
			}
		}
		return handle;
	}
}
