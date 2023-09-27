/**
 * EngineHttpSolrClient.java	  V1.0   2021年11月4日 下午5:11:24
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.solr;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.FastStreamingDocsCallback;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.StreamingBinaryResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;

public class EngineHttpSolrClient extends HttpSolrClient {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private SolrAuth auth;

	public EngineHttpSolrClient(Builder builder, SolrAuth auth) {
		super(builder);
		this.auth = auth;
	}

	@Override
	public QueryResponse query(String collection, SolrParams params) throws SolrServerException, IOException {
		QueryRequest req = new QueryRequest(params);
		if (auth != null) {
			// 增加身份认证
			req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
		}
		return req.process(this, collection);
	}

	@Override
	public QueryResponse query(String collection, SolrParams params, METHOD method)
			throws SolrServerException, IOException {
		QueryRequest req = new QueryRequest(params, method);
		if (auth != null) {
			// 增加身份认证
			req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
		}
		return req.process(this, collection);
	}

	@Override
	public QueryResponse queryAndStreamResponse(String collection, SolrParams params,
			StreamingResponseCallback callback) throws SolrServerException, IOException {
		return getQueryResponse(collection, params, new StreamingBinaryResponseParser(callback));
	}

	@Override
	public QueryResponse queryAndStreamResponse(String collection, SolrParams params,
			FastStreamingDocsCallback callback) throws SolrServerException, IOException {
		return getQueryResponse(collection, params, new StreamingBinaryResponseParser(callback));
	}

	private QueryResponse getQueryResponse(String collection, SolrParams params, ResponseParser parser)
			throws SolrServerException, IOException {
		QueryRequest req = new QueryRequest(params);
		if (auth != null) {
			// 增加身份认证
			req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
		}
		if (parser instanceof StreamingBinaryResponseParser) {
			req.setStreamingResponseCallback(((StreamingBinaryResponseParser) parser).callback);
		}
		req.setResponseParser(parser);
		return req.process(this, collection);
	}

	@Override
	public UpdateResponse add(String collection, SolrInputDocument doc, int commitWithinMs)
			throws SolrServerException, IOException {
		UpdateRequest req = new UpdateRequest();
		if (auth != null) {
			// 增加身份认证
			req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
		}
		req.add(doc);
		req.setCommitWithin(commitWithinMs);
		return req.process(this, collection);
	}

	@Override
	public UpdateResponse add(String collection, Collection<SolrInputDocument> docs, int commitWithinMs)
			throws SolrServerException, IOException {
		UpdateRequest req = new UpdateRequest();
		if (auth != null) {
			// 增加身份认证
			req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
		}
		req.add(docs);
		req.setCommitWithin(commitWithinMs);
		return req.process(this, collection);
	}

	@Override
	public UpdateResponse add(String collection, Iterator<SolrInputDocument> docIterator)
			throws SolrServerException, IOException {
		UpdateRequest req = new UpdateRequest();
		if (auth != null) {
			// 增加身份认证
			req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
		}
		req.setDocIterator(docIterator);
		return req.process(this, collection);
	}

	@Override
	public UpdateResponse deleteByQuery(String collection, String query, int commitWithinMs)
			throws SolrServerException, IOException {
		UpdateRequest req = new UpdateRequest();
		req.deleteByQuery(query);
		if (auth != null) {
			// 增加身份认证
			req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
		}
		req.setCommitWithin(commitWithinMs);
		return req.process(this, collection);
	}

	@Override
	public UpdateResponse deleteById(String collection, String id, int commitWithinMs)
			throws SolrServerException, IOException {
		UpdateRequest req = new UpdateRequest();
		if (auth != null) {
			// 增加身份认证
			req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
		}
		req.deleteById(id);
		req.setCommitWithin(commitWithinMs);
		return req.process(this, collection);
	}

	@Override
	public UpdateResponse deleteById(String collection, List<String> ids, int commitWithinMs)
			throws SolrServerException, IOException {
		if (ids == null)
			throw new IllegalArgumentException("'ids' parameter must be non-null");
		if (ids.isEmpty())
			throw new IllegalArgumentException("'ids' parameter must not be empty; should contain IDs to delete");

		UpdateRequest req = new UpdateRequest();
		if (auth != null) {
			// 增加身份认证
			req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
		}
		req.deleteById(ids);
		req.setCommitWithin(commitWithinMs);
		return req.process(this, collection);
	}

	@Override
	public UpdateResponse commit(String collection, boolean waitFlush, boolean waitSearcher)
			throws SolrServerException, IOException {
		UpdateRequest req = new UpdateRequest();
		if (auth != null) {
			// 增加身份认证
			req.setBasicAuthCredentials(auth.getUser(), auth.getPassword());
		}
		req.setAction(UpdateRequest.ACTION.COMMIT, waitFlush, waitSearcher);
		return req.process(this, collection);
	}

	public SolrAuth getAuth() {
		return auth;
	}

	public void setAuth(SolrAuth auth) {
		this.auth = auth;
	}
}
