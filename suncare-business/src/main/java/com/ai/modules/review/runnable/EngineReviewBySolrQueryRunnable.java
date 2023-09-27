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

import com.ai.common.query.SolrQueryGenerator;
import com.ai.modules.engine.util.EngineUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.util.SpringContextUtils;

import com.ai.modules.engine.runnable.AbsEngineRunnable;
import com.ai.modules.review.service.IReviewNewFirstService;

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
public class EngineReviewBySolrQueryRunnable extends AbsEngineRunnable {
	private SolrQuery solrQuery;
	private JSONObject doc;

	public EngineReviewBySolrQueryRunnable(String datasource, SolrQuery solrQuery, JSONObject doc) {
		super(datasource);
		this.solrQuery = solrQuery;
		this.doc = doc;
	}

	@Override
	public void execute() throws Exception {
//		SolrQueryGenerator.updateByQuery(EngineUtil.MEDICAL_UNREASONABLE_ACTION, doc, solrQuery);
		IReviewNewFirstService service = SpringContextUtils.getApplicationContext().getBean(IReviewNewFirstService.class);
		service.pushRecord(solrQuery, doc);
	}
}
