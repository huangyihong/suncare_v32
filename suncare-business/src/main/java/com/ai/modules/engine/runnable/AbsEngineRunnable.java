/**
 * EngineRunnable.java	  V1.0   2020年9月21日 上午10:42:57
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.runnable;

import org.apache.shiro.util.Assert;

import com.ai.common.utils.ThreadUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbsEngineRunnable implements Runnable {
	protected String datasource;
	private boolean success;
	private String message;
	private boolean redo;//失败是否需要重做
	
	public AbsEngineRunnable(String datasource) {
		this.datasource = datasource;
		Assert.hasLength(datasource, "solr数据源不能为空");
		this.redo = false;
	}
	
	public AbsEngineRunnable(String datasource, boolean redo) {
		this(datasource);
		this.redo = redo;
	}

	@Override
	public void run() {
		try {
			success = true;
			//设置线程的solr数据源
			ThreadUtils.setDatasource(datasource);
			execute();			
		} catch(Exception e) {
			if(redo) {
				//失败，重做一次
				try {
					Thread.sleep(60000L);
					execute();
				} catch(Exception ex) {
					log.error("", e);
					success = false;
					message = e.getMessage();
				}				
			} else {
				log.error("", e);
				success = false;
				message = e.getMessage();
			}
		} finally {
			ThreadUtils.removeDatasource();
		}
	}
	
	public abstract void execute() throws Exception;

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}
}
