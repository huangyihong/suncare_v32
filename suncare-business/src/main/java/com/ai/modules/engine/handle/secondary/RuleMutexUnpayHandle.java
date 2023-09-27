/**
 * SecondLineDrugHandle.java	  V1.0   2020年12月4日 上午10:05:13
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.secondary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;

import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：合用不予支付规则二次处理
 *
 * @author  zhangly
 * Date: 2021年4月14日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class RuleMutexUnpayHandle extends AbsRuleSecondHandle {

	public RuleMutexUnpayHandle(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList, Boolean trail) {
		super(task, batch, rule, ruleConditionList, trail);
	}

	@Override
	public void execute() throws Exception {		
  		List<MedicalRuleConditionSet> judgeList = ruleConditionList.stream().filter(s->"judge".equals(s.getType())).collect(Collectors.toList());
  		MedicalRuleConditionSet condition = judgeList.get(0);
  		//合用药品
    	String mutexCode = condition.getExt1();

    	String batchId = batch.getBatchId();
  		String[] array = StringUtils.split(rule.getItemCodes(), ",");
  		for(String itemCode : array) {
  			List<String> conditionList = new ArrayList<String>();
  	    	conditionList.add("ITEMCODE:"+itemCode);
  	    	conditionList.add("RULE_ID:"+rule.getRuleId());
  	    	conditionList.add("BATCH_ID:"+batchId);
  	    	List<String> visitidList = new ArrayList<String>();
  	    	Map<String, SolrDocument> visitMap = new HashMap<String, SolrDocument>();
  	    	String collection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
  	    	boolean slave = false;
  	    	int count = SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
  	    		String visitid = doc.get("VISITID").toString();
  	    		visitidList.add(visitid);
  	    		visitMap.put(visitid, doc);
  	        });
  	    	if(count>0) {
  	    		String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batchId + "/" + itemCode + ".json";
                BufferedWriter fileWriter = new BufferedWriter(
                        new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));        		
                //写文件头
                fileWriter.write("[");
	                
  	    		int pageSize = 500;
  	    		int pageNum = (visitidList.size() + pageSize - 1) / pageSize;
  	    		//数据分割
  	    		List<List<String>> mglist = new ArrayList<>();
  	    	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
  	    	    	mglist.add(visitidList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
  	    	    });
  	    	    
  	    	    conditionList.clear();  	    		
  	    		StringBuilder sb = new StringBuilder();
  	  			//合用不予支付
  	  			if("DRUGGROUP".equals(condition.getExt2())) {
  	  				//药品组
  	  				sb.append("_query_:\"");
	  	  			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
		  	  		sb.append(plugin.parse());
		  	  		sb.append("DRUGGROUP_CODE:").append(mutexCode);
		  	  		//排除自己
		  	  		sb.append(" AND -ATC_DRUGCODE:").append(rule.getItemCodes());
		  	  		sb.append("\"");
  	  			} else {
  	  				sb.append("ITEMCODE:").append(mutexCode); 				
  	  			}
  	    		conditionList.add(sb.toString());
  	    		conditionList.add("ITEM_QTY:{0 TO *}");
  	    		conditionList.add("ITEM_AMT:{0 TO *}");
  	    		//基金支出金额>0
    			conditionList.add("FUND_COVER:{0 TO *}");
  	    		//自付比例<0
  	    		conditionList.add("SELFPAY_PROP_MIN:[0 TO 1}");
  	    		    		
  	    	    for(List<String> subList : mglist) {
  	    	    	String visitidFq = "VISITID:(\"" + StringUtils.join(subList, "\",\"") + "\")";
  	    	    	SolrQuery solrQuery = new SolrQuery("*:*");
  	    			// 设定查询字段
  	    			solrQuery.addFilterQuery(conditionList.toArray(new String[0]));
  	    			solrQuery.addFilterQuery(visitidFq);
  	    			solrQuery.setStart(0);
  	    			solrQuery.setRows(EngineUtil.MAX_ROW);
  	    			solrQuery.setSort(SolrQuery.SortClause.asc("VISITID"));
  	    			solrQuery.addSort(SolrQuery.SortClause.asc("ITEMCODE"));
  	    			SolrUtil.export(solrQuery, EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, slave, (doc, index) -> {
  	    				String visitId = doc.get("VISITID").toString();
  	    				String code = doc.get("ITEMCODE").toString();
  	    				String name = doc.get("ITEMNAME").toString();
  	    				if(visitMap.containsKey(visitId)) {
  	    					SolrDocument visitDoc = visitMap.get(visitId);
  	    					JSONObject jsonObject = new JSONObject();
  	    					jsonObject.put("id", visitDoc.get("id").toString());
  	    					jsonObject.put("MUTEX_ITEM_CODE", SolrUtil.initActionValue(code, "add"));
  	    					name = code.concat(EngineUtil.SPLIT_KEY).concat(name);
  	    					jsonObject.put("MUTEX_ITEM_NAME", SolrUtil.initActionValue(name, "add"));
  	    					try {
  	    			            fileWriter.write(jsonObject.toJSONString());
  	    			            fileWriter.write(',');
  	    			        } catch (IOException e) {
  	    			        }
  	    				}
  	    	        });
  	    	    }
  	    	    // 文件尾
                fileWriter.write("]");
                fileWriter.flush();
                fileWriter.close();
                //导入solr
                SolrUtil.importJsonToSolr(importFilePath, collection, slave);
  	    	}
  		}
	}
}
