/**
 * EngineTranspositionServiceImpl.java	  V1.0   2020年11月2日 下午4:24:30
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.model.RTimer;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.IEngineTranspositionService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineTranspositionServiceImpl implements IEngineTranspositionService {
	@Autowired
	private IApiTaskService taskSV;
	@Autowired
	private IEngineActionService engineActionService;

	@Override
	public void generateUnreasonableAction(String batchId) {
		RTimer rtimer = new RTimer();
		SimpleDateFormat df = new SimpleDateFormat("H:mm:ss.SSS", Locale.getDefault());
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		boolean success = true;
		StringBuilder error = new StringBuilder();
		SolrClient solrClient = null;
		try {
			TaskBatchStepItem entity = new TaskBatchStepItem();
			entity.setUpdateTime(new Date());
			entity.setStartTime(new Date());
			entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
			taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_CHARGE, entity);

			TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
			if(batch==null) {
				throw new RuntimeException("未找到任务批次");
			}
			TaskProject task = taskSV.findTaskProject(batch.getProjectId());
			if(task==null) {
				throw new RuntimeException("未找到项目");
			}
			//删除历史solr数据
			engineActionService.deleteSolr(batchId, "2", true);

			List<String> conditionList = new ArrayList<String>();
			if(StringUtils.isNotBlank(batch.getEtlSource())) {
				conditionList.add("ETL_SOURCE:"+batch.getEtlSource());
			}
			String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
			String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
			batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
			batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
			//业务数据时间范围限制
			StringBuilder sb = new StringBuilder();
			sb.setLength(0);
			sb.append("CHARGEDATE:");
			sb.append("[").append(batch_startTime).append(" TO ").append(batch_endTime).append("]");
			conditionList.add(sb.toString());
			conditionList.add("IFEXACT_SIGN:否");
			sb.append("[").append(batch_startTime).append(" TO ").append(batch_endTime).append("]");
			conditionList.add(sb.toString());
			if(StringUtils.isNotBlank(batch.getCustomFilter())) {
            	//自定义数据范围限制
				conditionList.add(batch.getCustomFilter());
            }

			int pageSize = 5000;
	    	String cursorMark = "*";
	    	String[] fq = conditionList.toArray(new String[0]);
	    	SolrQuery solrQuery = new SolrQuery("*:*");
			// 设定查询字段
			solrQuery.addFilterQuery(fq);
			solrQuery.setStart(0);
			solrQuery.setRows(pageSize);
			solrQuery.set("cursorMark", cursorMark);
			solrQuery.setSort(SolrQuery.SortClause.asc("id"));

			int index = 1;
			final Long[] total = {0L};
			final AtomicInteger count = new AtomicInteger(0);
			Set<String> visitidSet = new HashSet<String>();
			List<SolrDocument> documents = new ArrayList<SolrDocument>();
			StreamingResponseCallback callback = new StreamingResponseCallback() {
                @Override
                public void streamSolrDocument(SolrDocument doc) {
                    String visitid = doc.get("VISITID").toString();
                    visitidSet.add(visitid);
                    count.getAndIncrement();
                    documents.add(doc);
                }
                @Override
                public void streamDocListInfo(long numFound, long start, Float maxScore) {
                	total[0] = numFound;
                    log.info("numFound:" + numFound);
                }
			};

			solrClient = SolrUtil.getClient(EngineUtil.DWB_CHARGE_DETAIL, false);
		    QueryResponse response = SolrUtil.process(solrClient, solrQuery, callback);
		    if(total[0]>0) {
		    	this.writeUnreasonableActionJson(batchId, task, visitidSet, documents, index*pageSize, total[0]);
		    }
		    if(total[0]>pageSize) {
				String nextCursorMark = response.getNextCursorMark();
				// 使用游标方式进行分页查询
				while(!cursorMark.equals(nextCursorMark)
						&& count.get()<total[0]) {
					visitidSet.clear();
					documents.clear();
					cursorMark = nextCursorMark;
					solrQuery.set("cursorMark", cursorMark);
					log.info("pageNo:{},cursorMark:{}", ++index, cursorMark);
					response = SolrUtil.process(solrClient, solrQuery, callback);
					nextCursorMark = response.getNextCursorMark();
					this.writeUnreasonableActionJson(batchId, task, visitidSet, documents, index*pageSize, total[0]);
				}
			}
		} catch(Exception e) {
			success = false;
			error.append(e.getMessage());
            log.error("", e);
		} finally {
			TaskBatchStepItem entity = new TaskBatchStepItem();
			entity.setUpdateTime(new Date());
			entity.setEndTime(new Date());
			entity.setStatus(success? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
			if (!success) {
                if(error.length() > 2000) {
                	entity.setMsg(error.substring(0, 2000));
                } else {
                	entity.setMsg(error.toString());
                }
            }
			taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_CHARGE, entity);
		}

		log.info(batchId+"批次串换项目总耗时:" + df.format(new Date((long)rtimer.getTime())));
	}

	@Override
	public void generateUnreasonableAction(String batchId, String itemCode) {

	}

	/**
	 *
	 * 功能描述：结果数据写入json文件并导入solr
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月2日 下午5:25:37</p>
	 *
	 * @param batchId
	 * @param task
	 * @param visitidSet
	 * @param documents
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private void writeUnreasonableActionJson(String batchId, TaskProject task, Set<String> visitidSet, List<SolrDocument> documents, int offset, long pageTotal) throws Exception {
		// 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/" + EngineUtil.MEDICAL_UNREASONABLE_ACTION + "/" + batchId + "/transposition.json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");

        int size = visitidSet.size();
        int pageSize = 500;
		int pageNum = (size + pageSize - 1) / pageSize;
		//数据分割
		List<Set<String>> mglist = new ArrayList<Set<String>>();
	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
	    	mglist.add(visitidSet.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toSet()));
	    });
	    Map<String, SolrDocument> masterMap = new HashMap<String, SolrDocument>();
	    //使用备份solr服务器
        boolean slave = true;
	    for(Set<String> subSet : mglist) {
	    	String visitidFq = "VISITID:(\"" + StringUtils.join(subSet, "\",\"") + "\")";
	        SolrQuery solrQuery = new SolrQuery("*:*");
			// 设定查询字段
			solrQuery.addFilterQuery(visitidFq);
			solrQuery.setStart(0);
			solrQuery.setRows(pageSize);
	        int count = SolrUtil.exportDoc(solrQuery, EngineUtil.DWB_MASTER_INFO, slave, (doc, index) -> {
	        	String visitid = doc.get("VISITID").toString();
	        	masterMap.put(visitid, doc);
	        });
	    }

        for(SolrDocument doc : documents) {
        	String visitid = doc.get("VISITID").toString();
        	SolrDocument master = masterMap.get(visitid);
        	if(master!=null) {
        		JSONObject json = new JSONObject();
		        json.put("id", UUIDGenerator.generate());

		        for(Map.Entry<String, String> entry : EngineCaseServiceImpl.CASE_FIELD_MAPPING.entrySet()) {
		            Object val = master.get(entry.getValue());
		            if (val != null) {
		                json.put(entry.getKey(), val);
		            }
		        }

		        json.put("CASE_ID", doc.get("ITEMCODE"));
		        json.put("CASE_NAME", doc.get("ITEMNAME"));
		        json.put("ACTION_NAME", "医保医院项目信息不一致");
		        json.put("ACTION_TYPE_ID", "41");
		        json.put("ACTION_TYPE_NAME", "项目信息不一致");
		        json.put("ACTION_DESC", "医保数据中的医保项目与医院项目不一致");
		        json.put("ACTION_MONEY", doc.get("FEE"));
		        json.put("MAX_ACTION_MONEY", doc.get("FEE"));
		        json.put("ITEM_AMT", doc.get("FEE"));
		        json.put("ITEM_QTY", doc.get("AMOUNT"));
		        json.put("FUND_COVER", doc.get("FUND_COVER"));
		        json.put("RULE_SCOPE_NAME", "医保医院项目信息不一致");
		        json.put("ITEM_ID", doc.get("id"));
		        json.put("CHARGEDATE", doc.get("CHARGEDATE"));
		        json.put("ITEMCODE_SRC", doc.get("ITEMCODE_SRC"));
		        json.put("ITEMNAME_SRC", doc.get("ITEMNAME_SRC"));
		        json.put("HIS_ITEMCODE", doc.get("HIS_ITEMCODE"));
		        json.put("HIS_ITEMNAME", doc.get("HIS_ITEMNAME"));
		        json.put("HIS_ITEMCODE_SRC", doc.get("HIS_ITEMCODE_SRC"));
		        json.put("HIS_ITEMNAME_SRC", doc.get("HIS_ITEMNAME_SRC"));

		        json.put("BUSI_TYPE", MedicalConstant.ENGINE_BUSI_TYPE_CHARGE);
		        json.put("GEN_DATA_TIME", DateUtils.now());
		        json.put("PROJECT_ID", task.getProjectId());
		        json.put("PROJECT_NAME", task.getProjectName());
		        json.put("BATCH_ID", batchId);
		        json.put("FIR_REVIEW_STATUS", "init");

		        fileWriter.write(json.toJSONString());
	            fileWriter.write(',');
        	}
        }
        // 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();

        boolean commit = offset==pageTotal ? true : offset%50000 == 0; //每50000条提交一次
        SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION, slave, commit);
	}
}
