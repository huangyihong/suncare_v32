/**
 * EngineSummaryServiceImpl.java	  V1.0   2020年12月8日 上午11:33:00
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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.engine.exception.EngineSolrException;
import com.ai.modules.engine.handle.report.AbstractReportHandler;
import com.ai.modules.engine.handle.report.BaseReportHandler;
import com.ai.modules.engine.model.report.ReportFacetBucketField;
import com.ai.modules.engine.model.report.ReportParamModel;
import com.ai.modules.engine.runnable.EngineSummaryRunnable;
import com.ai.modules.engine.service.IEngineSummaryService;
import com.ai.modules.engine.service.api.IApiSummaryService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.task.entity.TaskActionFieldConfig;
import com.ai.modules.task.entity.TaskBatchActionFieldConfig;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineSummaryServiceImpl implements IEngineSummaryService {
	@Autowired
    private IApiTaskService taskSV;
	@Autowired
	private IApiSummaryService summarySV;

	@Override
	public void summary(String batchId, String actionName) {
		summary(batchId, actionName, true);
	}
	
	@Override
	public void summary(String batchId, String actionName, boolean valid) {
		TaskActionFieldConfig config = summarySV.findTaskActionFieldConfig(actionName);
		//[{"code":"ORGNAME","value":"医疗机构名称"},{"code":"ITEMNAME","value":"项目名称"}]
		//config.setGroupFields("[{\"code\":\"ORGNAME\",\"value\":\"医疗机构名称\"},{\"code\":\"ITEMNAME\",\"value\":\"项目名称\"}]");
		if(valid) {
			if(config==null || StringUtils.isBlank(config.getGroupFields())) {
				throw new RuntimeException("未找到“"+actionName+"”不合规行为汇总字段配置");
			}
			JSONArray jsonArray = JSON.parseArray(config.getGroupFields());
			if(jsonArray==null || jsonArray.size()==0) {
				throw new RuntimeException("未找到“"+actionName+"”不合规行为汇总字段配置");
			}
			TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
	        if (batch == null) {
	            throw new RuntimeException("未找到任务批次");
	        }
		}
		if(config!=null && StringUtils.isNotBlank(config.getGroupFields())) {
        	JSONArray jsonArray = JSON.parseArray(config.getGroupFields());
        	if(jsonArray!=null && jsonArray.size()>0) {
        		summarySV.removeTaskBatchActionFieldConfig(batchId, actionName);
        		TaskBatchActionFieldConfig record = new TaskBatchActionFieldConfig();
        		record.setId(UUIDGenerator.generate());
        		record.setActionName(actionName);
        		record.setBatchId(batchId);
        		record.setStatus(MedicalConstant.RUN_STATE_WAIT);
        		record.setCreateTime(DateUtils.getDate());
        		record.setSummaryField(config.getGroupFields());
        		summarySV.saveTaskBatchActionFieldConfig(record);
        		
        		//this.summary(batchId, actionName, config);
        		String datasource = SolrUtil.getLoginUserDatasource();
        		ThreadUtils.THREAD_TRAIL_POOL.add(new EngineSummaryRunnable(datasource, batchId, actionName, config));
        	}
		}		
	}
	
	@Override
	public void summary(String batchId, String actionName, TaskActionFieldConfig config) {
		boolean success = true;
		String error = null;
		try {			
			TaskBatchActionFieldConfig entity = new TaskBatchActionFieldConfig();
			entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
			entity.setStartTime(DateUtils.getDate());
			summarySV.updateTaskBatchActionFieldConfig(batchId, actionName, entity);
            StringBuilder template = new StringBuilder();
			Set<String> fieldSet = new LinkedHashSet<String>();
			JSONArray jsonArray = JSON.parseArray(config.getGroupFields());
			for(int i=0, len=jsonArray.size(); i<len; i++) {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				String field = jsonObject.getString("code");
				fieldSet.add(field);
				if(i>0) {
					template.append("##");
				}
				template.append("${").append(field).append("}");
			}
			
			List<String> conditionList = new ArrayList<String>();
			conditionList.add("ACTION_NAME:"+actionName);
			conditionList.add("BATCH_ID:"+batchId);
			this.summaryOneCommit(conditionList, template.toString(), fieldSet);                        
		} catch(Exception e) {
			success = false;
			error = e.getMessage();
		} finally {
			TaskBatchActionFieldConfig entity = new TaskBatchActionFieldConfig();
			entity.setEndTime(DateUtils.getDate());
            entity.setStatus(success ? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
            if (!success) {
                error = error.length() > 2000 ? error.substring(0, 2000) : error;
                entity.setRemark(error);
            }
            summarySV.updateTaskBatchActionFieldConfig(batchId, actionName, entity);
		}
	}
	
	private void summary(List<String> conditionList, String template, Set<String> fieldSet) throws Exception {
		final AtomicInteger page = new AtomicInteger(0);//上传次数
		final int limit = 200000;//限制每20万条上传一次
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		Map<String, String> properties = new HashMap<String, String>();
		SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, false, (doc, index) -> {				
			JSONObject json = new JSONObject();
			json.put("id", doc.get("id"));
			JSONObject up = new JSONObject();
			String text = template;
			text = text.replace("${", "").replace("}", "");
			up.put("set", text);
			json.put("SUMMARY_FIELD", up);
			properties.clear();
			for(String field : fieldSet) {
				if(doc.get(field)==null) {
					properties.put(field, "");
				} else {
					properties.put(field, doc.get(field).toString());
				}
			}
			text = PlaceholderResolverUtil.replacePlaceholders(template, properties);			
			up = new JSONObject();
			up.put("set", text);
			json.put("SUMMARY_FIELD_VALUE", up);
			dataList.add(json);
			int size = dataList.size();
			if(size>=limit) {
				page.getAndIncrement();
				writeUpdateAction(dataList, true);
				dataList.clear();
			}
		});
		if(dataList.size()>0) {
			writeUpdateAction(dataList, true);
		}
	}
	
	private void summaryOneCommit(List<String> conditionList, String template, Set<String> fieldSet) throws Exception {
		HttpURLConnection urlc = SolrUtil.getSolrHttpURLConnection(EngineUtil.MEDICAL_UNREASONABLE_ACTION, false, true);
		urlc.connect();
		OutputStream out = urlc.getOutputStream();
		//写数据流
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		bw.write("[");
		Map<String, String> properties = new HashMap<String, String>();
		SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, false, (doc, index) -> {				
			JSONObject json = new JSONObject();
			json.put("id", doc.get("id"));
			JSONObject up = new JSONObject();
			String text = template;
			text = text.replace("${", "").replace("}", "");
			up.put("set", text);
			json.put("SUMMARY_FIELD", up);
			properties.clear();
			for(String field : fieldSet) {
				if(doc.get(field)==null) {
					properties.put(field, "");
				} else {
					properties.put(field, doc.get(field).toString());
				}
			}
			text = PlaceholderResolverUtil.replacePlaceholders(template, properties);			
			up = new JSONObject();
			up.put("set", text);
			json.put("SUMMARY_FIELD_VALUE", up);
			try {
				bw.write(json.toJSONString());
				bw.write(',');
			} catch(Exception e) {}
		});
		bw.write("]");
		bw.flush();
		if(bw!=null) {
			bw.close();
		}
		if (urlc != null) {
			urlc.disconnect();
		}
	}
	
	private void summaryByDocument(List<String> conditionList, String template, Set<String> fieldSet) throws Exception {		
		final SolrClient solrClient = SolrUtil.getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
		try {
			Map<String, String> properties = new HashMap<String, String>();
			SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, false, (doc, index) -> {
				SolrInputDocument document = new SolrInputDocument();
				document.setField("id", doc.get("id"));
				String text = template;
				text = text.replace("${", "").replace("}", "");
				JSONObject up = new JSONObject();
				up.put("set", text);
				document.setField("SUMMARY_FIELD", up);
				properties.clear();
				for(String field : fieldSet) {
					if(doc.get(field)==null) {
						properties.put(field, "");
					} else {
						properties.put(field, doc.get(field).toString());
					}
				}
				text = PlaceholderResolverUtil.replacePlaceholders(template, properties);			
				up = new JSONObject();
				up.put("set", text);
				document.setField("SUMMARY_FIELD_VALUE", up);
				try {
					solrClient.add(document);
				} catch (Exception e) {}
			});
			solrClient.commit();			
		} catch(SolrServerException e) {
			throw new EngineSolrException("调用solr失败：" + e.getMessage());
		} catch(Exception e) {
			throw e;
		} finally {
			if(solrClient!=null) {
        		solrClient.close();
        	}
		}
	}
	
	@Deprecated
	private void summary(String batchId, String actionName, List<String> conditionList, String template, Set<String> fieldSet) throws Exception {
		final AtomicInteger page = new AtomicInteger(0);//上传次数
		final int limit = 200000;//限制每20万条上传一次
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		Map<String, String> properties = new HashMap<String, String>();
		SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, false, (doc, index) -> {				
			JSONObject json = new JSONObject();
			json.put("id", doc.get("id"));
			JSONObject up = new JSONObject();
			String text = template;
			text = text.replace("${", "").replace("}", "");
			up.put("set", text);
			json.put("SUMMARY_FIELD", up);
			properties.clear();
			for(String field : fieldSet) {
				if(doc.get(field)==null) {
					properties.put(field, "");
				} else {
					properties.put(field, doc.get(field).toString());
				}
			}
			text = PlaceholderResolverUtil.replacePlaceholders(template, properties);			
			up = new JSONObject();
			up.put("set", text);
			json.put("SUMMARY_FIELD_VALUE", up);
			dataList.add(json);
			int size = dataList.size();
			if(size>=limit) {
				page.getAndIncrement();
				writeUpdateAction(batchId, actionName, dataList);
				dataList.clear();
			}
		});
		if(dataList.size()>0) {
			writeUpdateAction(batchId, actionName, dataList);
		}
	}
	
	/**
	 * 
	 * 功能描述：使用文件流方式上传solr数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月17日 下午3:47:50</p>
	 *
	 * @param batchId
	 * @param actionName
	 * @param dataList
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	@Deprecated
	private void writeUpdateAction(String batchId, String actionName, List<JSONObject> dataList) {
		BufferedWriter fileWriter = null;
		try {
			String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batchId + "/" + actionName + ".json";
	        fileWriter = new BufferedWriter(
	                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
	        //写文件头
	        fileWriter.write("[");
	        for(JSONObject json : dataList) {
	        	fileWriter.write(json.toJSONString());
	            fileWriter.write(',');            
	        }
	        // 文件尾
	        fileWriter.write("]");
	        fileWriter.flush();	        
	        //导入solr
	        SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
		} catch(Exception e) {
			log.debug("", e);
		} finally {
			try {
				if(fileWriter!=null) {
					fileWriter.close();
				}				
			} catch (IOException e) {}			
		}
	}
	
	/**
	 * 
	 * 功能描述：调用solr http方式上传solr数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月17日 下午3:48:57</p>
	 *
	 * @param dataList
	 * @param commit
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private void writeUpdateAction(List<JSONObject> dataList, boolean commit) {
		try {
			SolrUtil.postData(EngineUtil.MEDICAL_UNREASONABLE_ACTION, false, dataList, commit);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * 
	 * 功能描述：调用solr api方式上传solr数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月17日 下午3:48:29</p>
	 *
	 * @param solrClient
	 * @param dataList
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private void writeUpdateActionByDocument(SolrClient solrClient, List<SolrInputDocument> dataList) {		
		try {
			solrClient.add(dataList);
			solrClient.commit();
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	@Override
    public void settingSummayField(JSONObject json, Map<String, TaskActionFieldConfig> dynamicActionConfigMap) {
		if(json.get("ACTION_NAME")==null) {
			return;
		}
    	//TaskActionFieldConfig config = summarySV.findTaskActionFieldConfigByCache(json.getString("ACTION_NAME"));
		TaskActionFieldConfig config = dynamicActionConfigMap.get(json.getString("ACTION_NAME"));
		if(config!=null && StringUtils.isNotBlank(config.getGroupFields())) {
        	JSONArray jsonArray = JSON.parseArray(config.getGroupFields());
        	if(jsonArray!=null && jsonArray.size()>0) {
        		StringBuilder template = new StringBuilder();
        		Set<String> fieldSet = new LinkedHashSet<String>();	                		
        		for(int i=0, len=jsonArray.size(); i<len; i++) {
    				JSONObject dynamicObject = (JSONObject) jsonArray.get(i);
    				String field = dynamicObject.getString("code");
    				fieldSet.add(field);
    				if(i>0) {
    					template.append("##");
    				}
    				template.append("${").append(field).append("}");
    			}
        		String text = template.toString();
				text = text.replace("${", "").replace("}", "");
				json.put("SUMMARY_FIELD", text);
				Map<String, String> properties = new HashMap<String, String>();
				for(String field : fieldSet) {
					if(json.get(field)==null) {
						properties.put(field, "");
					} else {
						properties.put(field, json.get(field).toString());
					}
				}
				text = PlaceholderResolverUtil.replacePlaceholders(template.toString(), properties);			
				json.put("SUMMARY_FIELD_VALUE", text);
        	}
        }
    }
	
	@Override
    public void settingSummayField(JSONObject json, TaskActionFieldConfig dynamicActionConfig) {
		if(json.get("ACTION_NAME")==null) {
			return;
		}
		if(dynamicActionConfig!=null && StringUtils.isNotBlank(dynamicActionConfig.getGroupFields())) {
        	JSONArray jsonArray = JSON.parseArray(dynamicActionConfig.getGroupFields());
        	if(jsonArray!=null && jsonArray.size()>0) {
        		StringBuilder template = new StringBuilder();
        		Set<String> fieldSet = new LinkedHashSet<String>();	                		
        		for(int i=0, len=jsonArray.size(); i<len; i++) {
    				JSONObject dynamicObject = (JSONObject) jsonArray.get(i);
    				String field = dynamicObject.getString("code");
    				fieldSet.add(field);
    				if(i>0) {
    					template.append("##");
    				}
    				template.append("${").append(field).append("}");
    			}
        		String text = template.toString();
				text = text.replace("${", "").replace("}", "");
				json.put("SUMMARY_FIELD", text);
				Map<String, String> properties = new HashMap<String, String>();
				for(String field : fieldSet) {
					if(json.get(field)==null) {
						properties.put(field, "");
					} else {
						properties.put(field, json.get(field).toString());
					}
				}
				text = PlaceholderResolverUtil.replacePlaceholders(template.toString(), properties);			
				json.put("SUMMARY_FIELD_VALUE", text);
        	}
        }
    }
	
	@Override
	public void insertBatchActionFieldConfig(String batchId) {
		summarySV.removeTaskBatchActionFieldConfig(batchId);
		ReportParamModel reportModel = new ReportParamModel();
		reportModel.addWhere("BATCH_ID:"+batchId);
		reportModel.addWhere("SUMMARY_FIELD:?*");
		List<MedicalSpecialCaseClassify> classifyList = summarySV.findMedicalSpecialCaseClassify();
		if(classifyList!=null && classifyList.size()>0) {
			//排除特殊模型不合规行为
			Set<String> set = new HashSet<String>();
			for(MedicalSpecialCaseClassify bean : classifyList) {
				if(StringUtils.isNotBlank(bean.getActionName())) {
					set.add(bean.getActionName());
				}
			}
			reportModel.addWhere("-ACTION_NAME:("+StringUtils.join(set, " OR ")+")");
		}
		reportModel.setxLimit(EngineUtil.MAX_ROW);
    	reportModel.setGroupBy(new String[] {"ACTION_NAME"});
    	reportModel.setSolrCollection(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
    	AbstractReportHandler handler = new BaseReportHandler(reportModel);
    	List<ReportFacetBucketField> bucketList = handler.singleDimCallSolr();
    	List<TaskBatchActionFieldConfig> list = new ArrayList<TaskBatchActionFieldConfig>();
    	for(ReportFacetBucketField bucket : bucketList) {
    		String actionName = bucket.getField();
    		TaskActionFieldConfig config = summarySV.findTaskActionFieldConfigByCache(actionName);
    		if(config!=null) {
    			TaskBatchActionFieldConfig record = new TaskBatchActionFieldConfig();
        		record.setId(UUIDGenerator.generate());
        		record.setActionName(actionName);
        		record.setBatchId(batchId);
        		record.setStatus(MedicalConstant.RUN_STATE_NORMAL);
        		record.setCreateTime(DateUtils.getDate());
        		record.setStartTime(DateUtils.getDate());
        		record.setEndTime(DateUtils.getDate());
        		record.setSummaryField(config.getGroupFields());
        		list.add(record);
    		}
    	}		
		summarySV.saveTaskBatchActionFieldConfig(list);
	}
}
