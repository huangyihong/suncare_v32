/**
 * EngineRepairSolrServiceImpl.java	  V1.0   2021年12月23日 下午3:06:13
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.apache.solr.common.SolrDocument;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.service.IEngineRepairSolrService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.solr.Hive2SolrMain;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineRepairSolrServiceImpl implements IEngineRepairSolrService {
	@Autowired
	protected IApiDictService dictSV;
	
	@Override
	public void repair(List<String> whereList) throws Exception {
		String collection = "MEDICAL_UNREASONABLE_ACTION";		
		Set<String> itemSet = this.groupByItem(whereList);
		for(String itemcode : itemSet) {
			String importFilePath = SolrUtil.importFolder + "/repair/"+collection+"/"+itemcode+".json";
		    BufferedWriter fileWriter = new BufferedWriter(
	        	new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));  		
		    //写文件头
		    fileWriter.write("[");
			//不合规行为字典映射  		
	        Map<String, MedicalActionDict> actionDictMap = dictSV.queryActionDict();        
			List<String> conditionList = new ArrayList<String>();
			conditionList.add("ITEMCODE:"+itemcode);
			conditionList.addAll(whereList);
			
			final AtomicInteger count = new AtomicInteger(0);
			Map<String, List<String>> visitidMap = new HashMap<String, List<String>>();
			Map<String, JSONObject> jsonMap = new HashMap<String, JSONObject>();
	    	SolrUtil.exportDoc(conditionList, collection, false, (doc, index) -> {
	    		count.getAndIncrement();
	    		String id = doc.get("id").toString();
	    		String visitid = doc.get("VISITID").toString();	    		
	    		JSONObject json = new JSONObject();
				json.put("id", id);								
	    		String actionId = doc.get("ACTION_ID").toString();
	    		json.put("RULE_LEVEL", SolrUtil.initActionValue(null, "set"));
	    		if(actionDictMap.containsKey(actionId)) {
	    			MedicalActionDict actionDict = actionDictMap.get(actionId);
	    			json.put("RULE_LEVEL", SolrUtil.initActionValue(actionDict.getRuleLevel(), "set"));
	    		}
	    		if(!visitidMap.containsKey(visitid)) {
					List<String> list = new ArrayList<String>();
					list.add(id);
					visitidMap.put(visitid, list);
				} else {
					visitidMap.get(visitid).add(id);
				}
				jsonMap.put(id, json);
				int size = count.get();
	    		if(size%500==0) {
	    			log.info("pageNo:"+size/500);
	    			try {
		    			this.repairDwbChargeDetail(fileWriter, visitidMap, itemcode, jsonMap);		    			
			        } catch (Exception e) {
			        	e.printStackTrace();
			        }
	    			visitidMap.clear();
	    			jsonMap.clear();
	    		}
	        });
	    	if(visitidMap.size()>0) {
	    		log.info("pageNo:"+(count.get()/500)+1);
	    		this.repairDwbChargeDetail(fileWriter, visitidMap, itemcode, jsonMap);	    			
    			visitidMap.clear();
    			jsonMap.clear();
	    	}
	    	// 文件尾
	        fileWriter.write("]");
	        fileWriter.flush();
	        fileWriter.close();
	        System.out.println("文件路径："+importFilePath);
	        //导入solr
	        SolrUtil.importJsonToSolr(importFilePath, collection);
		}		
	}
	
	@Override
	public void repairByHive(List<String> whereList) throws Exception {
		repairYHBG(whereList);
	}
	
	private void repairByHiveUDF(List<String> whereList) throws Exception {
		Set<String> set = new LinkedHashSet<String>();
		set.add("ID");
		set.add("RULE_LEVEL");
		set.add("ITEMCODE_SRC");
		set.add("ITEMNAME_SRC");
		set.add("CHARGECLASS_ID");
		set.add("CHARGECLASS");
		StringBuilder sql = new StringBuilder();
		String path = HiveJDBCUtil.STORAGE_ROOT+"/repair/"+DateUtils.getDate("yyyyMMdd");
		sql.append("insert overwrite directory '").append(path).append("'");
		sql.append(" select ");
		sql.append(" default.udf_json_repair('").append(StringUtils.join(set, ",")).append("',");
		sql.append(StringUtils.join(set, ","));
		sql.append(")");
		sql.append(" from medical_gbdp.medical_unreasonable_action_repair");
		if(whereList!=null && whereList.size()>0) {
			sql.append(" where 1=1");
			for(int i=0, len=whereList.size(); i<len; i++) {
				String where = whereList.get(i);
				sql.append(" and ").append(where);
			}
		}
		HiveJDBCUtil.execute(sql.toString());
		//导入solr
		Hive2SolrMain main = new Hive2SolrMain();
		String collection = SolrUtil.getSolrUrl("shangrao3")+"/MEDICAL_UNREASONABLE_ACTION/update";
		main.execute(path, collection, true);
	}
	
	private void repairYHBG(List<String> whereList) throws Exception {
		//风控平台结果表与用户报告映射关系
		Map<String, String> mapping = new LinkedHashMap<String, String>();
		mapping.put("ID", "ID");
		mapping.put("RULE_LEVEL", "RULE_LEVEL");
		mapping.put("ITEMCODE_SRC", "WGXMMC_ID");
		mapping.put("ITEMNAME_SRC", "WGXMMC_NAME");
		mapping.put("CHARGECLASS_ID", "CHARGECLASS_ID");
		mapping.put("CHARGECLASS", "CHARGECLASS");
		StringBuilder sql = new StringBuilder();
		String path = HiveJDBCUtil.STORAGE_ROOT+"/repair/"+DateUtils.getDate("yyyyMMdd");
		sql.append("insert overwrite directory '").append(path).append("'");
		sql.append(" select ");
		sql.append(" default.udf_json_repair('").append(StringUtils.join(mapping.values(), ",")).append("',");
		sql.append(StringUtils.join(mapping.keySet(), ","));
		sql.append(")");
		sql.append(" from medical_gbdp.medical_unreasonable_action_repair");
		if(whereList!=null && whereList.size()>0) {
			sql.append(" where 1=1");
			for(int i=0, len=whereList.size(); i<len; i++) {
				String where = whereList.get(i);
				sql.append(" and ").append(where);
			}
		}
		HiveJDBCUtil.execute(sql.toString());
		//导入solr
		Hive2SolrMain main = new Hive2SolrMain();
		String collection = SolrUtil.getSolrUrl("yhbg")+"/NEWS_V3_RESULT_SHANGRAO3/update";
		main.execute(path, collection, true);
	}
	
	private void repairByHiveRs(List<String> whereList) throws Exception {
		StringBuilder sql = new StringBuilder("select * from medical_gbdp.medical_unreasonable_action_repair");
		if(whereList!=null && whereList.size()>0) {
			sql.append(" where ");
			for(int i=0, len=whereList.size(); i<len; i++) {
				String where = whereList.get(i);
				if(i>1) {
					sql.append(" and ");
				}
				sql.append(where);
			}
		}
		Set<String> set = new HashSet<String>();
		set.add("RULE_LEVEL");
		set.add("ITEMCODE_SRC");
		set.add("ITEMNAME_SRC");
		set.add("CHARGECLASS_ID");
		set.add("CHARGECLASS");
		String importFilePath = SolrUtil.importFolder + "/repair/"+DateUtils.getDate("yyyy-MM-dd")+".json";
	    BufferedWriter fileWriter = new BufferedWriter(
        	new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));  		
	    //写文件头
	    fileWriter.write("[");
		Connection conn = HiveJDBCUtil.getConnection();
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(sql.toString());
		while (rs.next()) {
			JSONObject json = new JSONObject();
			json.put("id", rs.getString("ID"));
			for(String column : set) {
	    		json.put(column, SolrUtil.initActionValue(rs.getString(column), "set"));
			}
			try {
	        	fileWriter.write(json.toJSONString());
	            fileWriter.write(',');
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
		}
		// 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();
		rs.close();
		conn.close();
	}
	
	private void repairCase(List<String> whereList) throws Exception {
		String collection = "MEDICAL_UNREASONABLE_ACTION";
		SolrClient solrClient = SolrUtil.getClient(EngineUtil.DWB_CHARGE_DETAIL, false);
		String importFilePath = SolrUtil.importFolder + "/repair/"+collection+"/case.json";
	    BufferedWriter fileWriter = new BufferedWriter(
        	new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));  		
	    //写文件头
	    fileWriter.write("[");
		//不合规行为字典映射  		
        Map<String, MedicalActionDict> actionDictMap = dictSV.queryActionDict();        
		List<String> conditionList = new ArrayList<String>();
		conditionList.add("ITEMCODE:?*");
		conditionList.addAll(whereList);
    	SolrUtil.exportDocByPager(conditionList, collection, false, (doc, index) -> {
    		JSONObject json = new JSONObject();
			json.put("id", doc.get("id"));
    		String actionId = doc.get("ACTION_ID").toString();
    		json.put("RULE_LEVEL", SolrUtil.initActionValue(null, "set"));
    		if(actionDictMap.containsKey(actionId)) {
    			MedicalActionDict actionDict = actionDictMap.get(actionId);
    			json.put("RULE_LEVEL", SolrUtil.initActionValue(actionDict.getRuleLevel(), "set"));
    		}
    		json.put("ITEMCODE_SRC", SolrUtil.initActionValue(doc.get("ITEMCODE_SRC"), "set"));
			json.put("ITEMNAME_SRC", SolrUtil.initActionValue(doc.get("ITEMNAME_SRC"), "set"));
    		
        });
    	solrClient.close();
    	// 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();
        System.out.println("文件路径："+importFilePath);
        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, collection);		
	}

	private void repairDwbChargeDetail(BufferedWriter fileWriter, 
			Map<String, List<String>> visitidMap, String itemcode, Map<String, JSONObject> jsonMap) throws Exception {
		SolrClient solrClient = SolrUtil.getClient(EngineUtil.DWB_CHARGE_DETAIL, false);
		String visitidFq = "VISITID:(\"" + StringUtils.join(visitidMap.keySet(), "\",\"") + "\")";
		List<String> conditionList = new ArrayList<String>();
		conditionList.add(visitidFq);
		conditionList.add("ITEMCODE:"+itemcode);
    	String[] fq = conditionList.toArray(new String[0]);
    	SolrQuery solrQuery = new SolrQuery("*:*");
		// 设定查询字段
		solrQuery.addFilterQuery(fq);
		solrQuery.setStart(0);
		solrQuery.setRows(Integer.MAX_VALUE);
		
		Set<String> existsSet = new HashSet<String>();
		StreamingResponseCallback callback = new StreamingResponseCallback() {
            @Override
            public void streamSolrDocument(SolrDocument doc) {
            	if(doc!=null) {
            		String visitid = doc.get("VISITID").toString();
            		if(existsSet.contains(visitid)) {
            			return;
            		}            		
            		List<String> keyList = visitidMap.get(visitid);
            		for(String key : keyList) {
            			JSONObject json = jsonMap.get(key);
            			json.put("CHARGECLASS_ID", SolrUtil.initActionValue(doc.get("CHARGECLASS_ID"), "set"));
                    	json.put("CHARGECLASS", SolrUtil.initActionValue(doc.get("CHARGECLASS"), "set"));
                    	json.put("ITEMCODE_SRC", SolrUtil.initActionValue(doc.get("ITEMCODE_SRC"), "set"));
                    	json.put("ITEMNAME_SRC", SolrUtil.initActionValue(doc.get("ITEMNAME_SRC"), "set"));
                    	try {
            	        	fileWriter.write(json.toJSONString());
            	            fileWriter.write(',');
            	        } catch (IOException e) {
            	        	e.printStackTrace();
            	        }
            		}
            		existsSet.add(visitid);
            	}
            }
            @Override
            public void streamDocListInfo(long numFound, long start, Float maxScore) {
                log.info("numFound:" + numFound);
            }
		};
		SolrUtil.process(solrClient, solrQuery, callback);
		solrClient.close();
	}
	
	private Set<String> groupByItem(List<String> whereList) {
		Set<String> result = new HashSet<String>();
		String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
		SolrQuery query = new SolrQuery();
		// 设定查询字段
		String q = "*:*";
		query.add("q", q);
		query.addFilterQuery("ITEMCODE:?*");
		for(String where : whereList) {
			query.addFilterQuery(where);
		}
		query.setRows(0);
		JSONObject facetJsonMap = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", "terms");
		jsonObject.put("limit", -1);
		jsonObject.put("field", "ITEMCODE");		
		facetJsonMap.put("categories", jsonObject);
		String facetJson = JSON.toJSONString(facetJsonMap);
		query.set("json.facet", facetJson);
		query.setFacet(true);
		query.setFacetLimit(-1);
		QueryResponse response;
		try {
			response = SolrUtil.call(query, collection);
			NestableJsonFacet nestableJsonFacet = response.getJsonFacetingResponse();
			BucketBasedJsonFacet bucketBasedJsonFacet = nestableJsonFacet.getBucketBasedFacets("categories");
			if(bucketBasedJsonFacet!=null) {
				List<BucketJsonFacet> bucketJsonFacetList = bucketBasedJsonFacet.getBuckets();
				for(int i=0; i<bucketJsonFacetList.size(); i++) {
					BucketJsonFacet bucket = bucketJsonFacetList.get(i);
	        		if(bucket != null) {
	        			String itemcode = String.valueOf(bucket.getVal());
	        			result.add(itemcode);
	        		}
				}
			}
		} catch (Exception e) {
			
		}
		return result;
	}
}
