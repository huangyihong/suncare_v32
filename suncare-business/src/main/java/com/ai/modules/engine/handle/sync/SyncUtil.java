/**
 * AbsSyncHandle.java	  V1.0   2022年1月27日 下午2:13:53
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.sync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ai.common.MedicalConstant;
import com.ai.modules.api.util.ApiOauthClientUtil;
import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.config.vo.MedicalGroupVO;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.ObjectCacheWithFile;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.vo.QueryMedicalRuleConfigVO;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.solr.Hive2SolrMain;
import com.ai.solr.HiveJDBCUtil;
import com.google.common.collect.Sets;

public class SyncUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(SyncUtil.class);	
	private static final String CACHE_TYPE = "hive_sync_solr";
	private static final int EXPIRED_SECOND = 30*60;
	private static IApiTaskService taskSV = SpringContextUtils.getApplicationContext().getBean(IApiTaskService.class);
	
	/**
	 * 
	 * 功能描述：验证solr中的数据是否有效{0:全量同步，1:增量同步，-1:有效不用同步}
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2022年1月28日 上午10:28:57</p>
	 *
	 * @param datasource
	 * @param table
	 * @param configList
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static int effective(String datasource, String table, String cacheName, List<QueryMedicalRuleConfigVO> configList) throws Exception {		
		String sql = "select max(etl_time) etl_time from DWS_PATIENT_1VISIT_1DAY_ITEMSUM where project='"+datasource+"'";
		String etlTime = HiveJDBCUtil.query(sql, "etl_time");
		String syncTime = null;
		SolrQuery solrQuery = new SolrQuery("*:*");
		solrQuery.setStart(0);
		solrQuery.setRows(1);
		solrQuery.setSort(SolrQuery.SortClause.desc("SYNC_TIME"));
		solrQuery.addField("SYNC_TIME");
		QueryResponse queryResponse = SolrUtil.call(solrQuery, table, false);
		SolrDocumentList documents = queryResponse.getResults();
        if(documents!=null && documents.size()>0) {
        	syncTime = documents.get(0).get("SYNC_TIME").toString();
        }
        logger.info("etl_time:{}, sync_time:{}", etlTime, syncTime);
        if(StringUtils.isNotBlank(etlTime) && (StringUtils.isBlank(syncTime) || syncTime.compareTo(etlTime)<0)) {
        	//solr同步时间小于数仓数据生成时间
        	return 0;
        }
        
        if(configList!=null && configList.size()>0) {
        	configList.sort(new Comparator<QueryMedicalRuleConfigVO>() {
    			@Override
    			public int compare(QueryMedicalRuleConfigVO o1, QueryMedicalRuleConfigVO o2) {
    				String t1 = DateUtils.formatDate(o1.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
    				if(o1.getUpdateTime()!=null) {
    					t1 = DateUtils.formatDate(o1.getUpdateTime(), "yyyy-MM-dd HH:mm:ss");
    				}
    				String t2 = DateUtils.formatDate(o2.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
    				if(o2.getUpdateTime()!=null) {
    					t2 = DateUtils.formatDate(o2.getUpdateTime(), "yyyy-MM-dd HH:mm:ss");
    				}
    				return t2.compareTo(t1);
    			}			
    		});
        	QueryMedicalRuleConfigVO config = configList.get(0);
        	String ruleTime = DateUtils.formatDate(config.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
			if(config.getUpdateTime()!=null) {
				ruleTime = DateUtils.formatDate(config.getUpdateTime(), "yyyy-MM-dd HH:mm:ss");
			}
			logger.info("rule_time:{}, sync_time:{}", ruleTime, syncTime);
			if(StringUtils.isBlank(syncTime) || syncTime.compareTo(ruleTime)<0) {
				//solr同步时间小于规则变更时间
				return 1;
	        }
        }
        //基础数据项目组的最近变更时间
		String lasttime = ApiOauthUtil.response("/oauth/api/dict/project/lasttime", null, "post", String.class);
		logger.info("project dict last charge time:{}, sync_time:{}", lasttime, syncTime);
		if(StringUtils.isBlank(syncTime) || syncTime.compareTo(lasttime)<0) {
			//solr同步时间小于项目组最近变更时间
			return 1;
        }
		TaskBatchBreakRuleLog log = taskSV.findTaskBatchBreakRuleLog(cacheName);
		if(log==null) {
			return 0;
		}
		if(!"normal".equals(log.getStatus())) {
			//上一次同步过程发生异常
			return 1;
		}        
		return -1;
	}
	
	/**
	 * 
	 * 功能描述：同步收费合规一日限频次数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2022年1月28日 上午10:30:27</p>
	 *
	 * @param datasource
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public synchronized static void syncChargeFreq(String datasource) throws Exception {
		String table = "MEDICAL_PATIENT_1VISIT_1DAY_ITEMSUM_CHARGE_FREQ";
		String cacheName = "CHARGE_FREQ_".concat(datasource);
		Object object = ObjectCacheWithFile.getObjectFromFile(CACHE_TYPE, cacheName, EXPIRED_SECOND);
		if(object!=null) {
			return;
		}
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("ruleType", "CHARGE");
    	busiParams.put("ruleLimit", "freq2");
    	List<QueryMedicalRuleConfigVO> configList = ApiOauthUtil.responseArray("/oauth/api/rule/config", busiParams, "post", QueryMedicalRuleConfigVO.class);
		int effective = SyncUtil.effective(datasource, table, cacheName, configList);
		if(effective==-1) {
			//缓存
			ObjectCacheWithFile.saveObjectToFile(CACHE_TYPE, cacheName, "true");
			return;
		}
		boolean allin = effective==0; //是否全量同步
		boolean success = true;
		String error = null;
		//项目编码
    	Set<String> includeSet = new HashSet<String>();
    	try {    
    		//项目组编码
        	Set<String> groupSet = new HashSet<String>();
        	for(QueryMedicalRuleConfigVO config : configList) {        		
        		if(MedicalConstant.ITEM_PROJECTGRP.equals(config.getItemTypes())) {
        			//主体是项目组
        			groupSet.add(config.getItemCodes());
        		} else {
        			includeSet.add(config.getItemCodes());
        		}
        	}
        	if(groupSet.size()>0) {
        		int pageSize = 1000;
        		List<MedicalGroupVO> voList = null;
        		if(groupSet.size()<pageSize) {
        			voList = ApiOauthClientUtil.projectGrp(StringUtils.join(groupSet, ","));
        		} else {
        			int pageNum = (groupSet.size() + pageSize - 1) / pageSize;
        			//数据分割
        			List<Set<String>> mglist = new ArrayList<>();
        		    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
        		    	mglist.add(groupSet.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toSet()));
        		    });	    
        		    for(int i=0,len=mglist.size(); i<len; i++) {
        		    	Set<String> subset = mglist.get(i);
        		    	voList = ApiOauthClientUtil.projectGrp(StringUtils.join(subset, "','"));
        		    }
        		}
        		for(MedicalGroupVO vo : voList) {
        			includeSet.add(vo.getCode());
        		}
        	}
        	//本次需要同步的项目编码
        	Set<String> itemcodeSet = null;
        	TaskBatchBreakRuleLog log = taskSV.findTaskBatchBreakRuleLog(cacheName);
        	if(log==null) {
        		itemcodeSet = includeSet;
        		log = new TaskBatchBreakRuleLog();
        		log.setLogId(cacheName);
        		log.setBatchId(cacheName);
        		log.setItemId(table);
        		log.setItemType(CACHE_TYPE);		
        		log.setStatus("running");
        		log.setMessage("运行中");
        		log.setCreateTime(new Date());
        		log.setStartTime(new Date());
        		log.setRuleJson(StringUtils.join(itemcodeSet, ","));
        		log.setWhereJson(StringUtils.join(includeSet, ","));
        		taskSV.saveTaskBatchBreakRuleLog(log);
        	} else {
        		//上一次同步的项目
        		String text = log.getItemName();
        		if(allin || StringUtils.isBlank(text)) {
        			itemcodeSet = includeSet;
        		} else {        			
        			String[] array = text.split(",");
        			//排查掉此次不需要同步的项目编码
            		Set<String> exculdeSet = Arrays.stream(array).collect(Collectors.toSet());
            		itemcodeSet = Sets.difference(includeSet, exculdeSet).immutableCopy();
        		}
        		TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
        		entity.setStatus("running");
        		entity.setMessage("运行中");
        		entity.setStartTime(new Date());
        		entity.setRuleJson(StringUtils.join(itemcodeSet, ","));
        		entity.setWhereJson(StringUtils.join(includeSet, ","));
        		taskSV.updateTaskBatchBreakRuleLog(cacheName, CACHE_TYPE, table, entity);
        	}    				
        	        	    	
        	String path = HiveJDBCUtil.STORAGE_ROOT+"/"+datasource+"/MEDICAL_PATIENT_1VISIT_1DAY_ITEMSUM/CHARGE_FREQ";
    		if(itemcodeSet.size()>0) {
    			String sql = parseItemcodeSql(datasource, path, itemcodeSet);
        		HiveJDBCUtil.execute(sql);
        		
        		if(allin || includeSet.size()==itemcodeSet.size()) {
        			//全量同步
        			logger.info("全量同步");
        			SolrUtil.delete(table, "*:*");
        		}
        		String collection = SolrUtil.getSolrUrl(datasource)+"/"+table+"/update";		
        		Hive2SolrMain main = new Hive2SolrMain();
        		main.execute(path, collection, true);
    		}    						    		
 
    		//缓存
    		ObjectCacheWithFile.saveObjectToFile(CACHE_TYPE, cacheName, "true");
    	} catch(Exception e) {
    		success = false;
			error = e.getMessage();
    		throw e;
    	} finally {
    		TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
            entity.setEndTime(new Date());
            entity.setStatus(success ? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
            if (!success) {
                error = error.length() > 2000 ? error.substring(0, 2000) : error;
                entity.setMessage(error);
            } else {
            	//同步成功，保存本次所涉及的项目，用于下次做增量同步
            	entity.setMessage("成功");
            	entity.setItemName(StringUtils.join(includeSet, ","));
            }
            taskSV.updateTaskBatchBreakRuleLog(cacheName, CACHE_TYPE, table, entity);
    	}		
	}
	
	/**
	 * 
	 * 功能描述：同步合理诊疗一日限频次数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2022年1月28日 上午10:31:01</p>
	 *
	 * @param datasource
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public synchronized static void syncTreatFreq(String datasource) throws Exception {
		String table = "MEDICAL_PATIENT_1VISIT_1DAY_ITEMSUM_TREAT_FREQ";
		String cacheName = "TREAT_FREQ_".concat(datasource);
		Object object = ObjectCacheWithFile.getObjectFromFile(CACHE_TYPE, cacheName, EXPIRED_SECOND);
		if(object!=null) {
			return;
		}
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("ruleType", "TREAT");
    	busiParams.put("ruleLimit", "freq2");
    	List<QueryMedicalRuleConfigVO> configList = ApiOauthUtil.responseArray("/oauth/api/rule/config", busiParams, "post", QueryMedicalRuleConfigVO.class);
		int effective = SyncUtil.effective(datasource, table, cacheName, configList);
		if(effective==-1) {
			//缓存
			ObjectCacheWithFile.saveObjectToFile(CACHE_TYPE, cacheName, "true");
			return;
		}		
    	
		boolean allin = effective==0; //是否全量同步		
		boolean success = true;
		String error = null;
		//项目编码
    	Set<String> includeSet = new HashSet<String>();		
    	try {
    		//项目组编码
        	Set<String> groupSet = new HashSet<String>();
        	for(QueryMedicalRuleConfigVO config : configList) {
        		for(MedicalRuleConditionSet condition : config.getConditionList()) {
        			String groupcode = condition.getExt4();
        			groupcode = StringUtils.replace(groupcode, "|", ",");
        			String[] array = StringUtils.split(groupcode, ",");
        			for(String code : array) {
        				groupSet.add(code);
        			}
        		}
        		if(MedicalConstant.ITEM_PROJECTGRP.equals(config.getItemTypes())) {
        			//主体是项目组
        			groupSet.add(config.getItemCodes());
        		} else {
        			includeSet.add(config.getItemCodes());
        		}
        	}
        	if(groupSet.size()>0) {
        		int pageSize = 1000;
        		List<MedicalGroupVO> voList = null;
        		if(groupSet.size()<pageSize) {
        			voList = ApiOauthClientUtil.projectGrp(StringUtils.join(groupSet, ","));
        		} else {
        			int pageNum = (groupSet.size() + pageSize - 1) / pageSize;
        			//数据分割
        			List<Set<String>> mglist = new ArrayList<>();
        		    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
        		    	mglist.add(groupSet.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toSet()));
        		    });	    
        		    for(int i=0,len=mglist.size(); i<len; i++) {
        		    	Set<String> subset = mglist.get(i);
        		    	voList = ApiOauthClientUtil.projectGrp(StringUtils.join(subset, "','"));
        		    }
        		}
        		for(MedicalGroupVO vo : voList) {
        			includeSet.add(vo.getCode());
        		}
        	}
        	//本次需要同步的项目编码
        	Set<String> itemcodeSet = null;
        	TaskBatchBreakRuleLog log = taskSV.findTaskBatchBreakRuleLog(cacheName);
        	if(log==null) {
        		itemcodeSet = includeSet;
        		log = new TaskBatchBreakRuleLog();
        		log.setLogId(cacheName);
        		log.setBatchId(cacheName);
        		log.setItemId(table);
        		log.setItemType(CACHE_TYPE);		
        		log.setStatus("running");
        		log.setMessage("运行中");
        		log.setCreateTime(new Date());
        		log.setStartTime(new Date());
        		log.setRuleJson(StringUtils.join(itemcodeSet, ","));
        		log.setWhereJson(StringUtils.join(includeSet, ","));
        		taskSV.saveTaskBatchBreakRuleLog(log);
        	} else {
        		//上一次同步的项目
        		String text = log.getItemName();
        		if(allin || StringUtils.isBlank(text)) {
        			itemcodeSet = includeSet;
        		} else {        			
        			String[] array = text.split(",");
        			//排查掉此次不需要同步的项目编码
            		Set<String> exculdeSet = Arrays.stream(array).collect(Collectors.toSet());
            		itemcodeSet = Sets.difference(includeSet, exculdeSet).immutableCopy();
        		}
        		TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
        		entity.setStatus("running");
        		entity.setMessage("运行中");
        		entity.setStartTime(new Date());
        		entity.setRuleJson(StringUtils.join(itemcodeSet, ","));
        		entity.setWhereJson(StringUtils.join(includeSet, ","));
        		taskSV.updateTaskBatchBreakRuleLog(cacheName, CACHE_TYPE, table, entity);
        	}
    		
        	if(itemcodeSet.size()>0) {
        		String path = HiveJDBCUtil.STORAGE_ROOT+"/"+datasource+"/MEDICAL_PATIENT_1VISIT_1DAY_ITEMSUM/TREAT_FREQ";
        		String sql = parseItemcodeSql(datasource, path, itemcodeSet);
        		HiveJDBCUtil.execute(sql);
        						
        		if(allin || includeSet.size()==itemcodeSet.size()) {
        			//全量同步
        			logger.info("全量同步");
        			SolrUtil.delete(table, "*:*");
        		}
        		String collection = SolrUtil.getSolrUrl(datasource)+"/"+table+"/update";		
        		Hive2SolrMain main = new Hive2SolrMain();
        		main.execute(path, collection, true);
        	}
		
    		//缓存
    		ObjectCacheWithFile.saveObjectToFile(CACHE_TYPE, cacheName, "true");
    	} catch(Exception e) {
    		success = false;
			error = e.getMessage();
    		throw e;
    	} finally {
    		TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
            entity.setEndTime(new Date());
            entity.setStatus(success ? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
            if (!success) {
                error = error.length() > 2000 ? error.substring(0, 2000) : error;
                entity.setMessage(error);
            } else {
            	//同步成功，保存本次所涉及的项目，用于下次做增量同步
            	entity.setMessage("成功");
            	entity.setItemName(StringUtils.join(includeSet, ","));
            }
            taskSV.updateTaskBatchBreakRuleLog(cacheName, CACHE_TYPE, table, entity);
    	}    	
	}
	
	/**
	 * 
	 * 功能描述：同步收费合规一日互斥数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2022年1月28日 下午4:14:20</p>
	 *
	 * @param datasource
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public synchronized static void syncChargeOnedayMutex(String datasource) throws Exception {
		String table = "MEDICAL_PATIENT_1VISIT_1DAY_ITEMSUM";
		String cacheName = "CHARGE_ONEDAYMUTEX_".concat(datasource);
		Object object = ObjectCacheWithFile.getObjectFromFile(CACHE_TYPE, cacheName, EXPIRED_SECOND);
		if(object!=null) {
			return;
		}
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("ruleType", "CHARGE");
    	busiParams.put("ruleLimit", "dayUnfitGroups1");
    	List<QueryMedicalRuleConfigVO> configList = ApiOauthUtil.responseArray("/oauth/api/rule/config", busiParams, "post", QueryMedicalRuleConfigVO.class);
		int effective = SyncUtil.effective(datasource, table, cacheName, configList);
		if(effective==-1) {
			//缓存
			ObjectCacheWithFile.saveObjectToFile(CACHE_TYPE, cacheName, "true");
			return;
		}		
		boolean allin = effective==0; //是否全量同步		
		boolean success = true;
		String error = null;
		//项目编码
    	Set<String> includeSet = new HashSet<String>();
    	try {    		
        	//项目组编码
        	Set<String> groupSet = new HashSet<String>();
        	for(QueryMedicalRuleConfigVO config : configList) {        		
        		for(MedicalRuleConditionSet condition : config.getConditionList()) {
        			String groupcode = condition.getExt1();
        			groupcode = StringUtils.replace(groupcode, "|", ",");
        			String[] array = StringUtils.split(groupcode, ",");
        			for(String code : array) {
        				groupSet.add(code);
        			}
        		}
        		if(MedicalConstant.ITEM_PROJECTGRP.equals(config.getItemTypes())) {
        			//主体是项目组
        			groupSet.add(config.getItemCodes());
        		} else {
        			includeSet.add(config.getItemCodes());
        		}
        	}
        	if(groupSet.size()>0) {
        		int pageSize = 1000;
        		List<MedicalGroupVO> voList = null;
        		if(groupSet.size()<pageSize) {
        			voList = ApiOauthClientUtil.projectGrp(StringUtils.join(groupSet, ","));
        		} else {
        			int pageNum = (groupSet.size() + pageSize - 1) / pageSize;
        			//数据分割
        			List<Set<String>> mglist = new ArrayList<>();
        		    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
        		    	mglist.add(groupSet.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toSet()));
        		    });	    
        		    for(int i=0,len=mglist.size(); i<len; i++) {
        		    	Set<String> subset = mglist.get(i);
        		    	voList = ApiOauthClientUtil.projectGrp(StringUtils.join(subset, "','"));
        		    }
        		}
        		for(MedicalGroupVO vo : voList) {
        			includeSet.add(vo.getCode());
        		}
        	}
        	//本次需要同步的项目编码
        	Set<String> itemcodeSet = null;
        	TaskBatchBreakRuleLog log = taskSV.findTaskBatchBreakRuleLog(cacheName);
        	if(log==null) {
        		itemcodeSet = includeSet;
        		log = new TaskBatchBreakRuleLog();
        		log.setLogId(cacheName);
        		log.setBatchId(cacheName);
        		log.setItemId(table);
        		log.setItemType(CACHE_TYPE);		
        		log.setStatus("running");
        		log.setMessage("运行中");
        		log.setCreateTime(new Date());
        		log.setStartTime(new Date());
        		log.setRuleJson(StringUtils.join(itemcodeSet, ","));
        		log.setWhereJson(StringUtils.join(includeSet, ","));
        		taskSV.saveTaskBatchBreakRuleLog(log);
        	} else {
        		//上一次同步的项目
        		String text = log.getItemName();
        		if(allin || StringUtils.isBlank(text)) {
        			itemcodeSet = includeSet;
        		} else {        			
        			String[] array = text.split(",");
        			//排查掉此次不需要同步的项目编码
            		Set<String> exculdeSet = Arrays.stream(array).collect(Collectors.toSet());
            		itemcodeSet = Sets.difference(includeSet, exculdeSet).immutableCopy();
        		}
        		TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
        		entity.setStatus("running");
        		entity.setMessage("运行中");
        		entity.setStartTime(new Date());
        		entity.setRuleJson(StringUtils.join(itemcodeSet, ","));
        		entity.setWhereJson(StringUtils.join(includeSet, ","));
        		taskSV.updateTaskBatchBreakRuleLog(cacheName, CACHE_TYPE, table, entity);
        	}
    		        	
        	if(itemcodeSet.size()>0) {
        		String path = HiveJDBCUtil.STORAGE_ROOT+"/"+datasource+"/MEDICAL_PATIENT_1VISIT_1DAY_ITEMSUM/CHARGE_ONEDAYMUTEX";
        		String sql = parseItemcodeSql(datasource, path, itemcodeSet);
        		HiveJDBCUtil.execute(sql);
        						
        		if(allin || includeSet.size()==itemcodeSet.size()) {
        			//全量同步
        			logger.info("全量同步");
        			SolrUtil.delete(table, "*:*");
        		}
        		String collection = SolrUtil.getSolrUrl(datasource)+"/"+table+"/update";		
        		Hive2SolrMain main = new Hive2SolrMain();
        		main.execute(path, collection, true);
        	}
		
    		//缓存
    		ObjectCacheWithFile.saveObjectToFile(CACHE_TYPE, cacheName, "true");
    	} catch(Exception e) {
    		success = false;
			error = e.getMessage();
    		throw e;
    	} finally {
    		TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
            entity.setEndTime(new Date());
            entity.setStatus(success ? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
            if (!success) {
                error = error.length() > 2000 ? error.substring(0, 2000) : error;
                entity.setMessage(error);
            } else {
            	//同步成功，保存本次所涉及的项目，用于下次做增量同步
            	entity.setMessage("成功");
            	entity.setItemName(StringUtils.join(includeSet, ","));
            }
            taskSV.updateTaskBatchBreakRuleLog(cacheName, CACHE_TYPE, table, entity);
    	}
	}
	
	/**
	 * 
	 * 功能描述：同步合理诊疗一日互斥数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2022年1月28日 下午4:14:20</p>
	 *
	 * @param datasource
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public synchronized static void syncTreatOnedayMutex(String datasource) throws Exception {
		String table = "MEDICAL_PATIENT_1VISIT_1DAY_ITEMSUM_TREAT_ONEDAYMUTEX";
		String cacheName = "TREAT_ONEDAYMUTEX_".concat(datasource);
		Object object = ObjectCacheWithFile.getObjectFromFile(CACHE_TYPE, cacheName, EXPIRED_SECOND);
		if(object!=null) {
			return;
		}
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("ruleType", "TREAT");
    	busiParams.put("ruleLimit", "YRCFSF1");
    	List<QueryMedicalRuleConfigVO> configList = ApiOauthUtil.responseArray("/oauth/api/rule/config", busiParams, "post", QueryMedicalRuleConfigVO.class);
		int effective = SyncUtil.effective(datasource, table, cacheName, configList);
		if(effective==-1) {
			//缓存
			ObjectCacheWithFile.saveObjectToFile(CACHE_TYPE, cacheName, "true");
			return;
		}
		boolean allin = effective==0; //是否全量同步
		boolean success = true;
		String error = null;
		//项目编码
    	Set<String> includeSet = new HashSet<String>();
    	try {
    		//项目组编码
        	Set<String> groupSet = new HashSet<String>();
        	for(QueryMedicalRuleConfigVO config : configList) {
        		for(MedicalRuleConditionSet condition : config.getConditionList()) {
        			String groupcode = condition.getExt1();
        			groupcode = StringUtils.replace(groupcode, "|", ",");
        			String[] array = StringUtils.split(groupcode, ",");
        			for(String code : array) {
        				groupSet.add(code);
        			}
        		}
        		if(MedicalConstant.ITEM_PROJECTGRP.equals(config.getItemTypes())) {
        			//主体是项目组
        			groupSet.add(config.getItemCodes());
        		} else {
        			includeSet.add(config.getItemCodes());
        		}
        	}
        	if(groupSet.size()>0) {
        		int pageSize = 1000;
        		List<MedicalGroupVO> voList = null;
        		if(groupSet.size()<pageSize) {
        			voList = ApiOauthClientUtil.projectGrp(StringUtils.join(groupSet, ","));
        		} else {
        			int pageNum = (groupSet.size() + pageSize - 1) / pageSize;
        			//数据分割
        			List<Set<String>> mglist = new ArrayList<>();
        		    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
        		    	mglist.add(groupSet.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toSet()));
        		    });	    
        		    for(int i=0,len=mglist.size(); i<len; i++) {
        		    	Set<String> subset = mglist.get(i);
        		    	voList = ApiOauthClientUtil.projectGrp(StringUtils.join(subset, "','"));
        		    }
        		}
        		for(MedicalGroupVO vo : voList) {
        			includeSet.add(vo.getCode());
        		}
        	}
        	//本次需要同步的项目编码
        	Set<String> itemcodeSet = null;
        	TaskBatchBreakRuleLog log = taskSV.findTaskBatchBreakRuleLog(cacheName);
        	if(log==null) {
        		itemcodeSet = includeSet;
        		log = new TaskBatchBreakRuleLog();
        		log.setLogId(cacheName);
        		log.setBatchId(cacheName);
        		log.setItemId(table);
        		log.setItemType(CACHE_TYPE);		
        		log.setStatus("running");
        		log.setMessage("运行中");
        		log.setCreateTime(new Date());
        		log.setStartTime(new Date());
        		log.setRuleJson(StringUtils.join(itemcodeSet, ","));
        		log.setWhereJson(StringUtils.join(includeSet, ","));
        		taskSV.saveTaskBatchBreakRuleLog(log);
        	} else {
        		//上一次同步的项目
        		String text = log.getItemName();
        		if(allin || StringUtils.isBlank(text)) {
        			itemcodeSet = includeSet;
        		} else {        			
        			String[] array = text.split(",");
        			//排查掉此次不需要同步的项目编码
            		Set<String> exculdeSet = Arrays.stream(array).collect(Collectors.toSet());
            		itemcodeSet = Sets.difference(includeSet, exculdeSet).immutableCopy();
        		}
        		TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
        		entity.setStatus("running");
        		entity.setMessage("运行中");
        		entity.setStartTime(new Date());
        		entity.setRuleJson(StringUtils.join(itemcodeSet, ","));
        		entity.setWhereJson(StringUtils.join(includeSet, ","));
        		taskSV.updateTaskBatchBreakRuleLog(cacheName, CACHE_TYPE, table, entity);
        	}
    		
        	if(itemcodeSet.size()>0) {
        		String path = HiveJDBCUtil.STORAGE_ROOT+"/"+datasource+"/MEDICAL_PATIENT_1VISIT_1DAY_ITEMSUM/TREAT_ONEDAYMUTEX";
        		String sql = parseItemcodeSql(datasource, path, itemcodeSet);
        		HiveJDBCUtil.execute(sql);
        						
        		if(allin || includeSet.size()==itemcodeSet.size()) {
        			//全量同步
        			logger.info("全量同步");
        			SolrUtil.delete(table, "*:*");
        		}
        		String collection = SolrUtil.getSolrUrl(datasource)+"/"+table+"/update";		
        		Hive2SolrMain main = new Hive2SolrMain();
        		main.execute(path, collection, true);
        	}
    				
    		//缓存
    		ObjectCacheWithFile.saveObjectToFile(CACHE_TYPE, cacheName, "true");
    	} catch(Exception e) {
    		success = false;
			error = e.getMessage();
    		throw e;
    	} finally {
    		TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
            entity.setEndTime(new Date());
            entity.setStatus(success ? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
            if (!success) {
                error = error.length() > 2000 ? error.substring(0, 2000) : error;
                entity.setMessage(error);
            } else {
            	//同步成功，保存本次所涉及的项目，用于下次做增量同步
            	entity.setMessage("成功");
            	entity.setItemName(StringUtils.join(includeSet, ","));
            }
            taskSV.updateTaskBatchBreakRuleLog(cacheName, CACHE_TYPE, table, entity);
    	}    	    	
	}
	
	private static String parseItemcodeSql(String datasource, String path, Set<String> itemcodeSet) {
		String column = ApiOauthUtil.getProperty("MEDICAL_PATIENT_1VISIT_1DAY_ITEMSUM");
		StringBuilder sb = new StringBuilder();
		sb.append("insert overwrite directory '").append(path).append("'");
		sb.append(" select default.udf_json_out(");
		sb.append("'");
		sb.append(column).append(",SYNC_TIME");
		sb.append("','ID',");
		sb.append(column).append(",from_unixtime(unix_timestamp(), 'yyyy-MM-dd HH:mm:ss')");
		sb.append(")");
		sb.append(" from medical.dws_patient_1visit_1day_itemsum");
		sb.append(" where");
		sb.append(" project='").append(datasource).append("'");
		int pageSize = 1000;
		if(itemcodeSet.size()<pageSize) {
			sb.append(" and itemcode in('").append(StringUtils.join(itemcodeSet, "','")).append("')");
		} else {
			sb.append(" and (");
			int pageNum = (itemcodeSet.size() + pageSize - 1) / pageSize;
			//数据分割
			List<Set<String>> mglist = new ArrayList<>();
		    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
		    	mglist.add(itemcodeSet.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toSet()));
		    });	    
		    for(int i=0,len=mglist.size(); i<len; i++) {
		    	Set<String> subset = mglist.get(i);
		    	if(i>0) {
		    		sb.append(" or");
		    	}
		    	sb.append(" itemcode in('").append(StringUtils.join(subset, "','")).append("')");
		    }
		    sb.append(")");
		}
		return sb.toString();
	}
	
	private static String parseGroupcodeSql(String datasource, String path, Set<String> itemcodeSet, Set<String> groupSet) {
		String column = ApiOauthUtil.getProperty("MEDICAL_PATIENT_1VISIT_1DAY_ITEMSUM");
		StringBuilder sb = new StringBuilder();
		sb.append("insert overwrite directory '").append(path).append("'");
		sb.append(" select default.udf_json_out(");
		sb.append("'");
		sb.append(column).append(",SYNC_TIME");
		sb.append("','ID',");
		sb.append(column).append(",from_unixtime(unix_timestamp(), 'yyyy-MM-dd HH:mm:ss')");
		sb.append(")");
		sb.append(" from medical.dws_patient_1visit_1day_itemsum a");
		sb.append(" left semi join medical_gbdp.STD_TREATGROUP std on a.itemcode = std.treatcode");
		sb.append(" and (");
		int pageSize = 1000;
		if(groupSet.size()<pageSize) {
			sb.append(" std.treatgroup_code in('").append(StringUtils.join(groupSet, "','")).append("')");
		} else {
			int pageNum = (groupSet.size() + pageSize - 1) / pageSize;
			//数据分割
			List<Set<String>> mglist = new ArrayList<>();
		    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
		    	mglist.add(groupSet.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toSet()));
		    });	    
		    for(int i=0,len=mglist.size(); i<len; i++) {
		    	Set<String> subset = mglist.get(i);
		    	if(i>0) {
		    		sb.append(" or");
		    	}
		    	sb.append(" std.treatgroup_code in('").append(StringUtils.join(subset, "','")).append("')");
		    }
		}
		sb.append(")");
		sb.append(" where");
		sb.append(" project='").append(datasource).append("'");
		if(itemcodeSet.size()<pageSize) {
			sb.append(" and itemcode not in('").append(StringUtils.join(itemcodeSet, "','")).append("')");
		} else {
			int pageNum = (itemcodeSet.size() + pageSize - 1) / pageSize;
			//数据分割
			List<Set<String>> mglist = new ArrayList<>();
		    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
		    	mglist.add(itemcodeSet.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toSet()));
		    });	    
		    for(int i=0,len=mglist.size(); i<len; i++) {
		    	Set<String> subset = mglist.get(i);
		    	if(i>0) {
		    		sb.append(" and");
		    	}
		    	sb.append(" itemcode not in('").append(StringUtils.join(subset, "','")).append("')");
		    }
		}
		return sb.toString();
	}
}
