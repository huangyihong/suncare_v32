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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.jeecg.common.util.DateUtils;

import com.ai.modules.engine.model.EngineLimitScopeEnum;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：二线用药规则二次处理
 *
 * @author  zhangly
 * Date: 2020年12月4日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class DrugSecondLineHandle extends AbsSecondaryHandle {

	public DrugSecondLineHandle(String batchId, String itemCode, List<MedicalDrugRule> ruleList, boolean trail) {
		super(batchId, itemCode, ruleList, trail);
	}

	@Override
	public void execute() throws Exception {
		//是否存在二线用药规则
    	boolean exists = false;
		String limitScopeCode = EngineLimitScopeEnum.CODE_12.getCode();
		//二线用药项目
    	String mutexCode = null;
  		for(int i=0; i<ruleList.size(); i++) {
  			MedicalDrugRule rule = ruleList.get(i);
  			String[] limitScope = rule.getLimitScope().split(",");
  			Set<String> limitScopeSet = new HashSet<String>(Arrays.asList(limitScope));
  	  		if(limitScopeSet.contains(limitScopeCode)) {
  	  			exists = true;
  	  			if(mutexCode==null) {
	  	  			mutexCode = rule.getTwoLimitDrug();
	  	  		} else {
	  	  			mutexCode = mutexCode + "|" + rule.getTwoLimitDrug();
	  	  		}
			}
  		}
  		if(!exists) {
  			return;
  		}
  		List<String> conditionList = new ArrayList<String>();
		conditionList.add("RULE_SCOPE:"+limitScopeCode);
    	conditionList.add("ITEMCODE:"+itemCode);
    	conditionList.add("BATCH_ID:"+batchId);
    	List<String> visitidList = new ArrayList<String>();
    	Map<String, SolrDocument> masterMap = new HashMap<String, SolrDocument>();
    	String collection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
    	boolean slave = false;
    	int count = SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
    		String visitid = doc.get("VISITID").toString();
    		visitidList.add(visitid);
    		masterMap.put(visitid, doc);
        });
    	
    	if(count>0) {
    		String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batchId + "/" + itemCode + ".json";
            BufferedWriter fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));    		
            //写文件头
            fileWriter.write("[");
    		for(String visitid : visitidList) {
    			SolrDocument master = masterMap.get(visitid);
    			Object visitdate = master.get("VISITDATE");
				String day = DateUtils.dateformat(visitdate.toString(), "yyyy-MM-dd");
    			//筛查条件
    			conditionList.clear();    			
    			conditionList.add("VISITDATE:{* TO "+day+"}");
    			conditionList.add("CLIENTID:"+master.get("CLIENTID"));
        		
        		Set<String> mutexVisitidSet = new HashSet<String>();
        		SolrQuery solrQuery = new SolrQuery("*:*");
    			solrQuery.addFilterQuery(conditionList.toArray(new String[0]));
    			solrQuery.setStart(0);
    			solrQuery.setRows(EngineUtil.MAX_ROW);    		
    			solrQuery.addField("id");
    			solrQuery.addField("VISITID");
    			solrQuery.addField("CLIENTID");
    			solrQuery.addField("VISITDATE");
    			solrQuery.addField("ETL_SOURCE");
    			solrQuery.addField("HIS_VISITID");
    			solrQuery.addField("YB_VISITID");
    			solrQuery.setSort(SolrQuery.SortClause.asc("VISITID"));
    			int cnt = SolrUtil.exportDoc(solrQuery, EngineUtil.DWB_MASTER_INFO, slave, (doc, index) -> {
    				String source = doc.get("ETL_SOURCE").toString();
    				String mutexVisitid = null;
    				if("A01".equals(source)) {
    					mutexVisitid = doc.get("YB_VISITID").toString();
    				} else if("A03".equals(source)) {
    					mutexVisitid = doc.get("HIS_VISITID").toString();
    				}
    				if(StringUtils.isNotBlank(mutexVisitid)) {
    					mutexVisitidSet.add(mutexVisitid);
    				}
    	        });
    			if(cnt>0) {
    				JSONObject json = new JSONObject();
    				json.put("id", master.get("id"));
    				JSONObject up = new JSONObject();
    				up.put("set", StringUtils.join(mutexVisitidSet, ","));
    				json.put("BEFORE_VISITID_SRC", up);
    				try {
    		            fileWriter.write(json.toJSONString());
    		            fileWriter.write(',');
    		        } catch (IOException e) {
    		        }
    			}
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
