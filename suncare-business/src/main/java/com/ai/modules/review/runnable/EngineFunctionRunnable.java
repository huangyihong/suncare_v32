/**
 * EngineCaseRunnable.java	  V1.0   2020年9月21日 下午12:24:47
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.review.runnable;

import com.ai.common.utils.ThreadUtils;
import com.ai.modules.engine.runnable.AbsEngineRunnable;
import com.ai.modules.engine.util.SolrUtil;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * 功能描述：不合规行为推送线程
 *
 * @author  zhangly
 * Date: 2020年9月21日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineFunctionRunnable extends AbsEngineRunnable {
	private Runnable runnable;
	private String token;

	public EngineFunctionRunnable(String datasource,String token, Runnable runnable) {
		super(datasource);
		this.token = token;
		this.runnable = runnable;
	}

	// 线程中的线程使用
	public EngineFunctionRunnable(Runnable runnable) {
		super(SolrUtil.getLoginUserDatasource());
		this.token = ThreadUtils.getToken();
		this.runnable = runnable;
	}

	@Override
	public void execute() throws Exception {
		if(StringUtils.isNotBlank(token)){
			ThreadUtils.setToken(token);
		} else {
			ThreadUtils.setTokenDef();
		}
		try {
			runnable.run();
		} finally {
			ThreadUtils.removeDatasource();
		}
	}
}
