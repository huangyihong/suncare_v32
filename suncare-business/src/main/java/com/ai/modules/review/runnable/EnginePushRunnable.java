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

import com.alibaba.fastjson.JSONObject;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.util.SpringContextUtils;

import com.ai.modules.engine.runnable.AbsEngineRunnable;
import com.ai.modules.review.entity.NewV3Tmp;
import com.ai.modules.review.service.IReviewNewPushService;

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
public class EnginePushRunnable extends AbsEngineRunnable {
	private SolrQuery solrQuery;
	private NewV3Tmp tmpBean;
	private JSONObject doc;

	public EnginePushRunnable(String datasource, SolrQuery solrQuery, NewV3Tmp tmpBean, JSONObject doc) {
		super(datasource);
		this.solrQuery = solrQuery;
		this.tmpBean = tmpBean;
		this.doc = doc;
	}

	@Override
	public void execute() throws Exception {
		IReviewNewPushService service = SpringContextUtils.getApplicationContext().getBean(IReviewNewPushService.class);
		service.pushBatchBySolr(solrQuery, tmpBean);
	}
}
