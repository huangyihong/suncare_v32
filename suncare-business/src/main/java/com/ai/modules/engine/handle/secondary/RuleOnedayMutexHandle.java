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
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.vo.ChargedetailVO;
import com.ai.modules.engine.model.vo.ItemVO;
import com.ai.modules.engine.model.vo.MutexComputeVO;
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
 * 功能描述：一日互斥规则二次处理
 *
 * @author  zhangly
 * Date: 2020年12月4日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class RuleOnedayMutexHandle extends AbsRuleSecondHandle {

	public RuleOnedayMutexHandle(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList, Boolean trail) {
		super(task, batch, rule, ruleConditionList, trail);
	}

	@Override
	public void execute() throws Exception {		
  		List<MedicalRuleConditionSet> judgeList = ruleConditionList.stream().filter(s->"judge".equals(s.getType())).collect(Collectors.toList());
  		//一日互斥组
    	Set<String> mutexCodeSet = new HashSet<String>();
    	for(MedicalRuleConditionSet bean : judgeList) {
    		mutexCodeSet.add(bean.getExt1());
    	}
    	String batchId = batch.getBatchId();
  		String[] array = StringUtils.split(rule.getItemCodes(), ",");
  		for(String itemCode : array) {
  			List<String> conditionList = new ArrayList<String>();
  	    	conditionList.add("ITEMCODE:"+itemCode);
  	    	conditionList.add("RULE_ID:"+rule.getRuleId());
  	    	conditionList.add("BATCH_ID:"+batchId);
  	    	List<String> visitidList = new ArrayList<String>();
  	    	Map<String, MutexComputeVO> computeMap = new HashMap<String, MutexComputeVO>();
  	    	String collection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
  	    	boolean slave = false;
  	    	int count = SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
  	    		String visitid = doc.get("VISITID").toString();
  	    		visitidList.add(visitid);
  	    		String id = doc.get("id").toString();
  	    		String itemcode = doc.get("ITEMCODE").toString();
  	    		if(!computeMap.containsKey(visitid)) {
  	    			MutexComputeVO vo = new MutexComputeVO(id, visitid, itemcode, doc);
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
  	    		conditionList.add("PRESCRIPTTIME:?*");
  	    		conditionList.add("AMOUNT:{0 TO *}");
  	    		conditionList.add("FEE:{0 TO *}");
  	    		//基金支出金额>0
    			conditionList.add("FUND_COVER:{0 TO *}");
    			//自付比例<0
    			conditionList.add("SELFPAY_PROP:[0 TO 1}");
  	    		StringBuilder sb = new StringBuilder();
  	    		sb.append("ITEMCODE:").append(itemCode);
  	    		sb.append(" OR ");
  	    		sb.append("_query_:\"");
  	    		EngineMapping mapping = new EngineMapping("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
  	    		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
  	    		sb.append(plugin.parse());
  	    		sb.append("TREATGROUP_CODE:");
  	    		String mutexCode = StringUtils.join(mutexCodeSet, "|");
  	    		String values = "(" + StringUtils.replace(mutexCode, "|", " OR ") + ")";
  				sb.append(values);
  	    		sb.append("\"");
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
  	    			solrQuery.addField("ITEMNAME_SRC");
  	    			solrQuery.addField("CLIENTID");
  	    			solrQuery.addField("CHARGEDATE");
  	    			solrQuery.addField("PRESCRIPTTIME");
  	    			solrQuery.addField("AMOUNT");
  	    			solrQuery.addField("FEE");
  	    			solrQuery.addField("FUND_COVER");
  	    			solrQuery.addField("ITEMPRICE");
  	    			solrQuery.setSort(SolrQuery.SortClause.asc("VISITID"));
  	    			SolrUtil.export(solrQuery, EngineUtil.DWB_CHARGE_DETAIL, slave, (map, index) -> {
  	    				//处方日期PRESCRIPTTIME
  	    				Object value = map.get("PRESCRIPTTIME");
  	    				String prescripttime = value.toString();
  	    				String day = DateUtils.dateformat(prescripttime, "yyyy-MM-dd");
  	    				String visitId = map.get("VISITID").toString();
  	    				String code = map.get("ITEMCODE").toString();
  	    				ChargedetailVO vo = new ChargedetailVO();
  	    				vo.setPrescripttime(prescripttime);
  	    				vo.setDay(day);
  	    				vo.setItemcode(code);
  	    				vo.setItemname(map.get("ITEMNAME").toString());
  	    				vo.setAmount(new BigDecimal(map.get("AMOUNT").toString()));
  	    				vo.setFee(new BigDecimal(map.get("FEE").toString()));
  	    				//基金支出金额
  	    				if(map.get("FUND_COVER")!=null) {
  	    					vo.setFundConver(new BigDecimal(map.get("FUND_COVER").toString()));
  	    				} 
  	    				if(map.get("ITEMPRICE")!=null) {
  	    					vo.setUnitPrice(new BigDecimal(map.get("ITEMPRICE").toString()));
  	    				}
  	    				if(map.get("ITEMNAME_SRC")!=null) {
  	    					vo.setItemnameSrc(map.get("ITEMNAME_SRC").toString());
  	    				}
  	    				MutexComputeVO compute = computeMap.get(visitId);
  	    				compute.add(vo);
  	    	        });
  	    	    }
  	    	    
  	    	    int index = 0;
  	    	    //一日互斥
  	    	    List<MutexComputeVO> mutexList = new ArrayList<MutexComputeVO>();    	    
  	    	    //非一日互斥
  	    	    List<String> notMutextList = new ArrayList<String>();
  	    	    for(Map.Entry<String, MutexComputeVO> entry : computeMap.entrySet()) {
  	    	    	MutexComputeVO vo = entry.getValue();
  	    	    	vo.computeMutex();
  	    	    	if(vo.getMutexItems()!=null && vo.getMutexItems().size()>0) {
  	    	    		mutexList.add(vo);
  	    	    	} else {
  	    	    		notMutextList.add(vo.getVisitid());
  	    	    	}
  	    	    	index++;
  	    	    	if(index==pageSize) {
  	    	    		if(notMutextList.size()>0) {
  	    	    			sb.setLength(0);
  	    				    sb.append("BATCH_ID:").append(batchId);
  	    				    sb.append(" AND ITEMCODE:").append(itemCode);
  	    				    sb.append(" AND VISITID:(\"");
  	    			    	sb.append(StringUtils.join(notMutextList, "\",\""));
  	    			    	sb.append("\")");
  	    			    	sb.append(" AND RULE_ID:"+rule.getRuleId());
  	    			    	SolrUtil.delete(collection, sb.toString(), slave);
  	    			    	notMutextList.clear();
  	    	    		}
  				    	index = 0;
  	    	    	}
  	    	    }
  	    	    if(notMutextList.size()>0) {
  	    	    	sb.setLength(0);
  				    sb.append("BATCH_ID:").append(batchId);
  				    sb.append(" AND ITEMCODE:").append(itemCode);
  				    sb.append(" AND VISITID:(\"");
  			    	sb.append(StringUtils.join(notMutextList, "\",\""));
  			    	sb.append("\")");
  			    	sb.append(" AND RULE_ID:"+rule.getRuleId());
  			    	SolrUtil.delete(collection, sb.toString(), slave);
  			    	notMutextList.clear();
  	    	    }
  	    	        	    
  	    	    if(mutexList.size()>0) {
  	    	    	// 一日互斥项目明细数据写入文件
  	    	    	String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batchId + "/" + itemCode + ".json";
  	                BufferedWriter fileWriter = new BufferedWriter(
  	                        new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
  	        		
  	                //写文件头
  	                fileWriter.write("[");
  	                //拆分结果
  	    	    	for(MutexComputeVO computeVO : mutexList) {
  	    	    		index = 0;
    	    			for(ItemVO mutexVO : computeVO.getMutexItems()) {
        	    			if(index==0) {
        	    				//第一个只做更新
        	    				this.writeSplitMutex(fileWriter, computeVO, mutexVO, false);
        	    			} else {
        	    				this.writeSplitMutex(fileWriter, computeVO, mutexVO, true);
        	    			}
        	    			index++;
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
	}
	
	/**
     * 
     * 功能描述：一日互斥规则拆分结果写入文件
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
    protected void writeSplitMutex(BufferedWriter fileWriter, MutexComputeVO computeVO, ItemVO mutexVO, boolean create) {
    	String content = "互斥发生日期：%s";
    	content = String.format(content, StringUtils.join(mutexVO.getDaySet(), ","));
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
    		json.put("MIN_ACTION_MONEY", mutexVO.computeFoundArray()[0]);
    		json.put("MAX_ACTION_MONEY", mutexVO.computeFoundArray()[1]);
    		//违规金额
    		json.put("MIN_MONEY", mutexVO.computeFeeArray()[0]);
    		json.put("MAX_MONEY", mutexVO.computeFeeArray()[1]);
    		json.put("MUTEX_ITEM_CODE", mutexVO.getItemcode());
			json.put("MUTEX_ITEM_NAME", mutexVO.getItemcode().concat(EngineUtil.SPLIT_KEY).concat(mutexVO.getItemname()));
			//互斥项目数量
			json.put("AI_ITEM_CNT", mutexVO.getMutexCnt());
			//互斥项目最高单价
			json.put("AI_OUT_CNT", mutexVO.getMutexPrice());
    	} else {    	
    		//更新
			json.put("id", computeVO.getDocument().get("id").toString());						
			JSONObject up = new JSONObject();
			up.put("set", content);
			json.put("BREAK_RULE_CONTENT", up);
			//医保基金支付金额
			up = new JSONObject();
			up.put("set", mutexVO.computeFoundArray()[0]);
			json.put("MIN_ACTION_MONEY", up);
			up = new JSONObject();
			up.put("set", mutexVO.computeFoundArray()[1]);
			json.put("MAX_ACTION_MONEY", up);
			//违规金额
			up = new JSONObject();
			up.put("set", mutexVO.computeFeeArray()[0]);
			json.put("MIN_MONEY", up);
			up = new JSONObject();
			up.put("set", mutexVO.computeFeeArray()[1]);
			json.put("MAX_MONEY", up);
			up = new JSONObject();
			up.put("set", mutexVO.getItemcode());
			json.put("MUTEX_ITEM_CODE", up);
			up = new JSONObject();
			up.put("set", mutexVO.getItemcode().concat(EngineUtil.SPLIT_KEY).concat(mutexVO.getItemname()));
			json.put("MUTEX_ITEM_NAME", up);
			//互斥项目数量
			up = new JSONObject();
			up.put("set", mutexVO.getMutexCnt());
			json.put("AI_ITEM_CNT", up);
			//互斥项目最高单价
			up = new JSONObject();
			up.put("set", mutexVO.getMutexPrice());
			json.put("AI_OUT_CNT", up);
    	}
    	try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (IOException e) {
        }
    }
}
