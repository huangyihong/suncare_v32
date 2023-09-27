/**
 * AbstractHandler.java	  V1.0   2020年2月11日 上午10:17:40
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.job.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.ai.modules.engine.job.meta.JobMeta;
import com.ai.modules.engine.model.RTimer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractJobHandler {
	private JobMeta jobMeta;
 
	public AbstractJobHandler(JobMeta jobMeta) {
		this.jobMeta = jobMeta;
	}
	
	public boolean run() {
		boolean result = true;
		RTimer timer = new RTimer();
    	SimpleDateFormat df = new SimpleDateFormat("H:mm:ss.SSS", Locale.getDefault());
    	df.setTimeZone(TimeZone.getTimeZone("UTC"));    
    	
		result = execute();		
		log.info("程序消耗时长:" + df.format(new Date((long)timer.getTime())));
		return result;
	}
 
	protected abstract boolean execute();

	public JobMeta getJobMeta() {
		return jobMeta;
	}

	public void setJobMeta(JobMeta jobMeta) {
		this.jobMeta = jobMeta;
	}
}
