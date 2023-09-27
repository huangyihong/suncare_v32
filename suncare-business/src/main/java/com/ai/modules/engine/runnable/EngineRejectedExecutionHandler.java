/**
 * RejectedExecutionHandler.java	  V1.0   2020年10月12日 下午3:25:19
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.runnable;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 
 * 功能描述：线程池策略，被拒绝的任务能够阻塞执行，从而阻止任务的生产速度
 *
 * @author  zhangly
 * Date: 2020年10月12日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineRejectedExecutionHandler implements RejectedExecutionHandler {

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		try {
			// 核心改造点，由blockingqueue的offer改成put阻塞方法
			executor.getQueue().put(r);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
