/**
 * AbsRuleHandle.java	  V1.0   2020年11月4日 下午2:47:04
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.MD5Util;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.common.MedicalConstant;
import com.ai.modules.config.entity.StdHoslevelFundpayprop;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.service.IApiEngineService;
import com.ai.modules.engine.service.impl.EngineActionServiceImpl;
import com.ai.modules.engine.service.impl.EngineCaseServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 功能描述：规则使用solr计算引擎抽象类
 *
 * @author  zhangly
 * Date: 2020年11月12日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Slf4j
public abstract class AbsSolrRuleHandle extends AbsRuleHandle {
	//是否试算
	protected Boolean trail;
	
	public AbsSolrRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail) {
		super(task, batch);
		this.trail = trail;
	}
	
	public AbsSolrRuleHandle(TaskProject task, TaskProjectBatch batch) {
		super(task, batch);
		this.trail = false;
	}
		
	/**
	 * 
	 * 功能描述：不合规数据同步结果表MEDICAL_UNREASONABLE_ACTION
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月10日 下午2:26:06</p>
	 *
	 * @param itemcode
	 * @param type
	 * @param slave
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected void syncUnreasonableAction(MedicalRuleConfig rule, String type, boolean slave) throws Exception {
    	BufferedWriter fileWriter = null;
    	SolrClient solrClient = null;
    	try {
    		String ruleId = rule.getRuleId();
    		String collection = trail ? EngineUtil.MEDICAL_TRAIL_ACTION : EngineUtil.MEDICAL_UNREASONABLE_ACTION;
    		String drugCollection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
    		// 数据写入文件
            String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batch.getBatchId() + "/" + ruleId + ".json";
            fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
            //写文件头
            fileWriter.write("[");

    		solrClient = SolrUtil.getClient(EngineUtil.DWB_MASTER_INFO, slave);

    		List<String> conditionList = new ArrayList<String>();
        	String where = "_query_:\"%sRULE_TYPE:%s AND RULE_ID:%s AND BATCH_ID:%s\"";
        	EngineMapping mapping = new EngineMapping(drugCollection, "VISITID", "VISITID");
    		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
        	where  = String.format(where, plugin.parse(), type, ruleId, batch.getBatchId());
        	conditionList.add(where);

	    	int pageSize = 1000;
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
			List<String> visitidList = new ArrayList<String>();
	    	Map<String, Map<String, Object>> masterMapList = new HashMap<String, Map<String, Object>>();
			StreamingResponseCallback callback = new StreamingResponseCallback() {
                @Override
                public void streamSolrDocument(SolrDocument doc) {
                    String visitid = doc.get("VISITID").toString();
                    masterMapList.put(visitid, doc.getFieldValueMap());
                    visitidList.add(visitid);
                    count.getAndIncrement();
                }
                @Override
                public void streamDocListInfo(long numFound, long start, Float maxScore) {
                	total[0] = numFound;
                    log.info("numFound:" + numFound);
                }
			};

			ApplicationContext context = SpringContextUtils.getApplicationContext();
			IApiEngineService apiEngineSV = context.getBean(IApiEngineService.class);
			//医院等级报销比例
	        List<StdHoslevelFundpayprop> fundpayRatioList = apiEngineSV.findStdHoslevelFundpayprop(task.getDataSource());
		    QueryResponse response = SolrUtil.process(solrClient, solrQuery, callback);
		    if(total[0]>0) {
		    	this.writeSyncUnreasonableActionJson(fileWriter, rule, visitidList, masterMapList, type, slave, fundpayRatioList);
		    }
			if(total[0]>pageSize) {
				String nextCursorMark = response.getNextCursorMark();
				// 使用游标方式进行分页查询
				while(!cursorMark.equals(nextCursorMark)
						&& count.get()<total[0]) {
					visitidList.clear();
					masterMapList.clear();
					cursorMark = nextCursorMark;
					solrQuery.set("cursorMark", cursorMark);
					log.info("pageNo:{},cursorMark:{}", ++index, cursorMark);
					response = SolrUtil.process(solrClient, solrQuery, callback);
					nextCursorMark = response.getNextCursorMark();
					this.writeSyncUnreasonableActionJson(fileWriter, rule, visitidList, masterMapList, type, slave, fundpayRatioList);
				}
			}

			// 文件尾
            fileWriter.write("]");
            fileWriter.flush();
            //导入solr主服务器
            SolrUtil.importJsonToSolr(importFilePath, collection);
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if(fileWriter!=null) {
    			fileWriter.close();
    		}
    		if(solrClient!=null) {
        		solrClient.close();
        	}
    	}
    }
	
	private void writeSyncUnreasonableActionJson(BufferedWriter fileWriter, MedicalRuleConfig rule,
			List<String> visitidList, Map<String, Map<String, Object>> masterMapList, String type, boolean slave,
			List<StdHoslevelFundpayprop> fundpayRatioList) throws Exception {
		String ruleId = rule.getRuleId();
		String drugCollection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
		
    	List<String> conditionList = new ArrayList<String>();
    	conditionList.add("VISITID:(\""+StringUtils.join(visitidList, "\",\"")+"\")");
    	conditionList.add("RULE_TYPE:"+type);
    	conditionList.add("RULE_ID:"+ruleId);
    	conditionList.add("BATCH_ID:"+batch.getBatchId());

		SolrUtil.exportDocByPager(conditionList, drugCollection, slave, (doc, index) -> {
		    String visitid = doc.get("VISITID").toString();
		    if(masterMapList.containsKey(visitid)) {
		    	Map<String, Object> masterMap = masterMapList.get(visitid);
		    	JSONObject json = new JSONObject();
		        //json.put("id", UUIDGenerator.generate());
		    	//String id = MD5Util.MD5Encode(batch.getBatchId().concat("_").concat(ruleId).concat("_").concat(doc.get("ITEMCODE").toString()).concat("_").concat(visitid), "UTF-8");
		    	//id生成策略
		    	String template = "${batchId}_${ruleId}_${itemCode}_${visitid}";
		        Properties properties = new Properties();
		        properties.put("batchId", batch.getBatchId());
		        properties.put("ruleId", ruleId);
		        properties.put("itemCode", doc.get("ITEMCODE"));
		        properties.put("visitid", visitid);
		        template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
		        if(doc.get("MUTEX_ITEM_CODE")!=null
		        		&& StringUtils.isNotBlank(doc.get("MUTEX_ITEM_CODE").toString())) {
		        	//互斥规则特殊处理
		        	String mutex_item_code = doc.get("MUTEX_ITEM_CODE").toString();
		        	mutex_item_code = this.sortMutexItemcode(mutex_item_code);
		        	template = template.concat("_").concat(mutex_item_code);
		        }
		        Set<String> dateLimitSet = new HashSet<String>();
		        dateLimitSet.add("freq2");//一日限频次
		        dateLimitSet.add("dayUnfitGroups1");//收费合规-一日互斥
		        dateLimitSet.add("YRCFSF1");//合理诊疗-一日互斥
		        if(rule.getRuleLimit()!=null && dateLimitSet.contains(rule.getRuleLimit())
		        		&& doc.get("BREAK_RULE_TIME")!=null
		        		&& StringUtils.isNotBlank(doc.get("BREAK_RULE_TIME").toString())) {
		        	String time = doc.get("BREAK_RULE_TIME").toString();
		        	template = template.concat("_").concat(time);
		        }
		        String id = MD5Util.MD5Encode(template, "UTF-8");
		    	json.put("id", id);

		    	for(Map.Entry<String, String> entry : EngineCaseServiceImpl.CASE_FIELD_MAPPING.entrySet()) {
		            Object val = masterMap.get(entry.getValue());
		            if (val != null) {
		                json.put(entry.getKey(), val);
		            }
		        }
		        if("1".equals(type)) {
		        	//药品合规
		        	json.put("BUSI_TYPE", MedicalConstant.ENGINE_BUSI_TYPE_DRUG);
		        } else if("2".equals(type)) {
		        	//收费合规
		        	json.put("BUSI_TYPE", MedicalConstant.ENGINE_BUSI_TYPE_CHARGE);
		        } else if("4".equals(type)) {
                	//诊疗合规
                	json.put("BUSI_TYPE", MedicalConstant.ENGINE_BUSI_TYPE_TREAT);
                } else {
                	json.put("BUSI_TYPE", type);
                }
		        json.put("GEN_DATA_TIME", DateUtils.now());
		        json.put("PROJECT_ID", task.getProjectId());
		        json.put("PROJECT_NAME", task.getProjectName());
		        json.put("BATCH_ID", batch.getBatchId());
		        json.put("TASK_BATCH_NAME", batch.getBatchName());
		        for(Map.Entry<String, String> entry : EngineActionServiceImpl.DURG_ACTION_MAPPING.entrySet()){
		            json.put(entry.getKey(), doc.get(entry.getValue()));
		        }
		        json.put("CHARGEDATE", doc.get("BREAK_RULE_TIME"));
		        //计算违规金额=项目可纳入报销费用*医院级别报销比例
		        this.settingActionMoney(masterMap, json, doc, fundpayRatioList);
		        
		        json.put("FIR_REVIEW_STATUS", "init");
		        
		        //违规金额、基金支出金额保留2位小数
		        Set<String> set = new HashSet<String>();
		        set.add("ACTION_MONEY");
		        set.add("MAX_ACTION_MONEY");
		        set.add("MIN_MONEY");
		        set.add("MAX_MONEY");
		        for(String field : set) {
		        	Object object = json.get(field);
		        	if(object!=null && StringUtils.isNotBlank(object.toString())) {
		        		BigDecimal value = new BigDecimal(object.toString());
		        		value = value.setScale(2, BigDecimal.ROUND_DOWN);
		        		json.put(field, value);
		        	}
		        }
		        
		        try {
		            fileWriter.write(json.toJSONString());
		            fileWriter.write(',');
		        } catch (IOException e) {
		        }
		    }
		});
    }
	
	/**
     * 
     * 功能描述：设置违规金额，计算违规金额=项目可纳入报销费用*医院级别报销比例
     *
     * @author  zhangly
     * <p>创建日期 ：2020年11月10日 下午1:39:13</p>
     *
     * @param masterMap:DWB_MASTER_INFO数据
     * @param task:批次任务
     * @param json:new JSONObject()
     * @param doc:MEDICAL_UNREASONABLE_DRUG_ACTION或MEDICAL_TRAIL_DRUG_ACTION数据
     * @param scopeEnum:限定范围
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    private void settingActionMoney(Map<String, Object> masterMap, JSONObject json, SolrDocument doc, List<StdHoslevelFundpayprop> fundpayRatioList) {
    	String actionId = json.getString("ACTION_ID");
    	String ruleScope = null;
    	if(json.getString("RULE_SCOPE")!=null) {
    		ruleScope = json.getString("RULE_SCOPE");
    		ruleScope = StringUtils.replace(ruleScope, "[", "");
    		ruleScope = StringUtils.replace(ruleScope, "]", "");
    	}
    	boolean flag = (ruleScope!=null && FREQUENCY_ACTION_SET.contains(ruleScope)) || actionId!=null && FREQUENCY_ACTION_SET.contains(actionId);
    	if(flag) {
    		String hosplevel = null;
    		//由医院级别改成物价级别获取报销比例
			/*if(masterMap.get("HOSPLEVEL")!=null) {
				hosplevel = masterMap.get("HOSPLEVEL").toString();
			}*/
    		if(masterMap.get("PRICE_LEVEL")!=null && !"null".equals(masterMap.get("PRICE_LEVEL").toString())) {
				hosplevel = masterMap.get("PRICE_LEVEL").toString().trim();				
			}
    		boolean find = false;//是否配置了医院级别报销比例
    		BigDecimal rate = BigDecimal.ZERO;
            if(hosplevel!=null) {            	
                String visitdate = masterMap.get("VISITDATE").toString();
                String day = DateUtils.dateformat(visitdate, "yyyy-MM-dd");
                for(StdHoslevelFundpayprop bean : fundpayRatioList) {
                	if(hosplevel.equals(bean.getHosplevelName().trim()) 
                			|| hosplevel.equals(bean.getHosplevel().trim())) {
                		String start = DateUtils.formatDate(bean.getStartdate(), "yyyy-MM-dd");
                		String end = DateUtils.formatDate(bean.getEnddate(), "yyyy-MM-dd");
                		if(start.compareTo(day)<=0 && end.compareTo(day)>=0) {
                			rate = new BigDecimal(bean.getFundpayprop());
                			find = true;
                			break;
                		}
                	}
                }
            }
            if(find) {
            	//医院级别报销比例
            	rate = rate.divide(new BigDecimal(100));
        		//超频次特殊处理
        		Collection<Object> array = doc.getFieldValues("ARRAY_ACTION_MONEY");
        		if(array!=null && array.size()>0) {
        			//取可纳入报销金额中的最小值
        			BigDecimal min = null;
        			for(Object obj : array) {
        				BigDecimal value = new BigDecimal(String.valueOf(obj));
        				if(min==null) {
        					min = value;
        				} else if(min.compareTo(value)>0) {
        					min = value;
        				}
        			}
        			min = min.multiply(rate);
        	        min = min.setScale(2, BigDecimal.ROUND_HALF_DOWN);
        	        json.put("ACTION_MONEY", min);
        	        json.put("MAX_ACTION_MONEY", min);
        		}
            }
    	}
    }
}
