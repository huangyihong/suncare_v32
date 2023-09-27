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
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.jeecg.common.util.UUIDGenerator;

import com.ai.modules.engine.model.EngineLimitScopeEnum;
import com.ai.modules.engine.model.vo.InteractionComputeVO;
import com.ai.modules.engine.model.vo.InteractionItemVO;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：相互作用规则二次处理
 *
 * @author  zhangly
 * Date: 2020年12月4日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class DrugInteractionHandle extends AbsSecondaryHandle {

	public DrugInteractionHandle(String batchId, String itemCode, List<MedicalDrugRule> ruleList, boolean trail) {
		super(batchId, itemCode, ruleList, trail);
	}

	@Override
	public void execute() throws Exception {
		//是否存在相互作用规则
    	boolean exists = false;
    	String limitScopeCode = EngineLimitScopeEnum.CODE_34.getCode();
    	//限制范围
    	Set<String> limitScopeSet = new HashSet<String>();
    	//互斥项目
    	Set<String> mutexCodeSet = new HashSet<String>();
  		for(int i=0; i<ruleList.size(); i++) {
  			MedicalDrugRule rule = ruleList.get(i);
  			String[] limitScope = rule.getLimitScope().split(",");
  	  		for(String scope : limitScope) {
  	  			EngineLimitScopeEnum limitScopeEnum = EngineLimitScopeEnum.enumValueOf(scope);
  	  			if(limitScopeEnum!=null) {
  	  				limitScopeSet.add(limitScopeEnum.getCode());
  	  			}
  	  			if(limitScopeCode.equals(scope)) {
  	  				exists = true;
  	  				mutexCodeSet.add(rule.getUnfitGroupCodesDay());
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
    	Map<String, InteractionComputeVO> computeMap = new HashMap<String, InteractionComputeVO>();
    	String collection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
    	boolean slave = false;
    	int count = SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
    		String visitid = doc.get("VISITID").toString();
    		visitidList.add(visitid);
    		if(!computeMap.containsKey(visitid)) {
    			InteractionComputeVO vo = new InteractionComputeVO(doc);
    			computeMap.put(visitid, vo);
    		}
        });
    	
    	if(count>0) {        		
    		int pageSize = 500;
    		int pageNum = (visitidList.size() + pageSize - 1) / pageSize;
    		//数据分割
    		List<List<String>> mglist = new ArrayList<>();
    	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
    	    	mglist.add(visitidList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
    	    });
    	    
    	    conditionList.clear();
    		conditionList.add("ITEM_QTY:{0 TO *}");
    		conditionList.add("ITEM_AMT:{0 TO *}");
    		StringBuilder sb = new StringBuilder();
    		sb.append("ITEMCODE:");
    		String mutexCode = StringUtils.join(mutexCodeSet, "|");
    		String values = "(" + StringUtils.replace(mutexCode, "|", " OR ") + ")";
    		sb.append(values);
    		conditionList.add(sb.toString());
    		    		
    	    for(List<String> subList : mglist) {
    	    	String visitidFq = "VISITID:(\"" + StringUtils.join(subList, "\",\"") + "\")";
    	    	SolrQuery solrQuery = new SolrQuery("*:*");
    			// 设定查询字段
    			solrQuery.addFilterQuery(conditionList.toArray(new String[0]));
    			solrQuery.addFilterQuery(visitidFq);
    			solrQuery.setStart(0);
    			solrQuery.setRows(EngineUtil.MAX_ROW);    		
    			solrQuery.addField("id");
    			solrQuery.addField("VISITID");
    			solrQuery.addField("ITEMCODE");
    			solrQuery.addField("ITEMNAME");
    			solrQuery.addField("ITEM_QTY");
    			solrQuery.addField("ITEM_AMT");
    			solrQuery.addField("FUND_COVER");
    			solrQuery.setSort(SolrQuery.SortClause.asc("VISITID"));
    			SolrUtil.exportDoc(solrQuery, EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, slave, (doc, index) -> {
    				String visitId = doc.get("VISITID").toString();
    				String code = doc.get("ITEMCODE").toString();
    				InteractionItemVO vo = new InteractionItemVO();
    				vo.setItemcode(code);
    				vo.setItemname(doc.get("ITEMNAME").toString());
    				vo.setAmount(new BigDecimal(doc.get("ITEM_QTY").toString()));
    				vo.setFee(new BigDecimal(doc.get("ITEM_AMT").toString()));
    				if(doc.get("FUND_COVER")!=null) {
    					vo.setFundConver(new BigDecimal(doc.get("FUND_COVER").toString()));
    				}
    				InteractionComputeVO compute = computeMap.get(visitId);
    				compute.add(vo);
    	        });
    	    }
    	    
    	    String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batchId + "/" + itemCode + ".json";
            BufferedWriter fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
    		
            //写文件头
            fileWriter.write("[");
    	    for(Map.Entry<String, InteractionComputeVO> entry : computeMap.entrySet()) {
    	    	InteractionComputeVO computeVO = entry.getValue();
    	    	//计算互斥项目中的最小、最大费用
    	    	computeVO.computeMutexMinMaxFee();
    	    	if(computeVO.getMutexItems()!=null) {        	    		
    	    		if(limitScopeSet.size()==1) {
    	    			//规则中仅包含相互作用限定范围        	    			
    	    			for(int i=0,len=computeVO.getMutexItems().size(); i<len; i++) {
        	    			InteractionItemVO mutexVO = computeVO.getMutexItems().get(i);
        	    			if(i==0) {
        	    				//第一个只做更新
        	    				this.writeSplitMutex(fileWriter, computeVO, mutexVO, false);
        	    			} else {
        	    				this.writeSplitMutex(fileWriter, computeVO, mutexVO, true);
        	    			}
        	    		}
    	    		} else {
    	    			//规则中包含其他限定范围
    	    			Collection<Object> limitScopeCodes = computeVO.getDocument().getFieldValues("RULE_SCOPE");
    	    			if(limitScopeCodes.size()==1) {
    	    				//其他规则不违规
    	    				for(int i=0,len=computeVO.getMutexItems().size(); i<len; i++) {
            	    			InteractionItemVO mutexVO = computeVO.getMutexItems().get(i);
            	    			if(i==0) {
            	    				//第一个只做更新
            	    				this.writeSplitMutex(fileWriter, computeVO, mutexVO, false);
            	    			} else {
            	    				this.writeSplitMutex(fileWriter, computeVO, mutexVO, true);
            	    			}
            	    		}
    	    			}
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

	/**
     * 
     * 功能描述：相互作用规则拆分结果写入文件
     *
     * @author  zhangly
     * <p>创建日期 ：2020年10月20日 下午5:16:37</p>
     *
     * @param fileWriter
     * @param computeVO
     * @param mutexVO
     * @param create
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    private void writeSplitMutex(BufferedWriter fileWriter, InteractionComputeVO computeVO, InteractionItemVO mutexVO, boolean create) {
    	String content = mutexVO.getItemname().concat("(").concat(mutexVO.getItemcode()).concat(")");
    	JSONObject json = new JSONObject();
    	if(create) {
    		//新增
    		SolrDocument doc = computeVO.getDocument();
    		for(Entry<String, Object> entry : doc.entrySet()) {
    			if(!"_version_".equals(entry.getKey())) {
    				json.put(entry.getKey(), entry.getValue());
    			}
    		}
    		json.put("id", UUIDGenerator.generate());
    		json.put("BREAK_RULE_CONTENT", content);
    		//医保基金支付金额
    		json.put("MIN_ACTION_MONEY", computeVO.getFoundArray()[0]);
    		json.put("MAX_ACTION_MONEY", computeVO.getFoundArray()[1]);
    		//违规金额
    		json.put("MIN_MONEY", computeVO.getFeeArray()[0]);
    		json.put("MAX_MONEY", computeVO.getFeeArray()[1]);
    		json.put("MUTEX_ITEM_CODE", mutexVO.getItemcode());
			json.put("MUTEX_ITEM_NAME", mutexVO.getItemcode().concat(EngineUtil.SPLIT_KEY).concat(mutexVO.getItemname()));
    	} else {    	
    		//更新
			json.put("id", computeVO.getDocument().get("id").toString());						
			JSONObject up = new JSONObject();
			up.put("set", content);
			json.put("BREAK_RULE_CONTENT", up);
			//医保基金支付金额
			up = new JSONObject();
			up.put("set", computeVO.getFoundArray()[0]);
			json.put("MIN_ACTION_MONEY", up);
			up = new JSONObject();
			up.put("set", computeVO.getFoundArray()[1]);
			json.put("MAX_ACTION_MONEY", up);
			//违规金额
			up = new JSONObject();
			up.put("set", computeVO.getFeeArray()[0]);
			json.put("MIN_MONEY", up);
			up = new JSONObject();
			up.put("set", computeVO.getFeeArray()[1]);
			json.put("MAX_MONEY", up);
			up = new JSONObject();
			up.put("set", mutexVO.getItemcode());
			json.put("MUTEX_ITEM_CODE", up);
			up = new JSONObject();
			up.put("set", mutexVO.getItemcode().concat(EngineUtil.SPLIT_KEY).concat(mutexVO.getItemname()));
			json.put("MUTEX_ITEM_NAME", up);
    	}
    	try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (IOException e) {
        }
    }
}
