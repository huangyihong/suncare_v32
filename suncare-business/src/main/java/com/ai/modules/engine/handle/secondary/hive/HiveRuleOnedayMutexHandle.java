/**
 * SecondLineDrugHandle.java	  V1.0   2020年12月4日 上午10:05:13
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.secondary.hive;

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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.jeecg.common.util.DateUtils;

import com.ai.modules.engine.handle.secondary.AbsRuleSecondHandle;
import com.ai.modules.engine.model.vo.ChargedetailVO;
import com.ai.modules.engine.model.vo.OnedayMutexComputeVO;
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
 * 功能描述：一日互斥规则二次处理（互斥项目组中的项目作为违规项目）
 *
 * @author  zhangly
 * Date: 2020年12月4日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveRuleOnedayMutexHandle extends AbsRuleSecondHandle {

	public HiveRuleOnedayMutexHandle(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList, Boolean trail) {
		super(task, batch, rule, ruleConditionList, trail);
	}

	@Override
	public void execute() throws Exception {		
  		String batchId = batch.getBatchId();
    	String itemcode = rule.getItemCodes(); //规则主体项目
  		String collection = EngineUtil.MEDICAL_TRAIL_ACTION;
	    boolean slave = false;
	    List<String> conditionList = new ArrayList<String>();
    	conditionList.add("RULE_ID:"+rule.getRuleId());
    	conditionList.add("BATCH_ID:"+batchId);
    	String groupcode = parseGroupcode();
		if(groupcode.indexOf("|")>-1) {
			groupcode = "(" + StringUtils.replace(groupcode, "|", " OR ") + ")";
		}		
		StringBuilder sb = new StringBuilder();
		SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWS_PATIENT_1VISIT_1DAY_ITEMSUM", "VISITID", "VISITID");
		sb.append("_query_:\"");
		sb.append(plugin.parse());
		sb.append("ITEMCODE:").append(rule.getItemCodes()).append(" AND ITEM_QTY:{* TO 0}\"");
		sb.append(" OR ");
		sb.append("_query_:\"");
		sb.append(plugin.parse());
		plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
        sb.append("_query_:\\\""+plugin.parse() + "TREATGROUP_CODE:" + groupcode+"\\\"");
        sb.append(" AND ITEM_QTY:{* TO 0}\"");
        conditionList.add(sb.toString());
		
    	List<String> visitidList = new ArrayList<String>();
    	Map<String, OnedayMutexComputeVO> computeMap = new HashMap<String, OnedayMutexComputeVO>();  	    	
    	int count = SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
    		String visitid = doc.get("VISITID").toString();
    		visitidList.add(visitid);
    		if(!computeMap.containsKey(visitid)) {
    			OnedayMutexComputeVO vo = new OnedayMutexComputeVO(visitid, itemcode);
    			vo.addSolrDocument(doc);
    			computeMap.put(visitid, vo);
    		} else {
    			computeMap.get(visitid).addSolrDocument(doc);
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
    		sb.setLength(0);
    		sb.append("ITEMCODE:").append(itemcode);
    		sb.append(" OR ");    		
    		plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
            sb.append("_query_:\""+plugin.parse() + "TREATGROUP_CODE:" + groupcode+"\"");            
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
    				OnedayMutexComputeVO compute = computeMap.get(visitId);
    				compute.add(vo);
    	        });
    	    }
    	    
    	    // 一日互斥项目明细数据写入文件
			String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batchId + "/" + rule.getRuleId() + ".json";
		    BufferedWriter fileWriter = new BufferedWriter(
		            new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
		    //写文件头
		    fileWriter.write("[");
		    //非一日互斥
	    	List<String> notMutextList = new ArrayList<String>();
		    for(Map.Entry<String, OnedayMutexComputeVO> entry : computeMap.entrySet()) {
    	    	OnedayMutexComputeVO vo = entry.getValue();
    	    	List<SolrDocument> documentList = vo.computeMutex("CHARGEDATE");
    	    	for(SolrDocument doc : documentList) {
    	    		BigDecimal amount = new BigDecimal(doc.get("ITEM_AMT").toString());
    	    		if(amount.compareTo(BigDecimal.ZERO)>0) {
    	    			this.writeMutex(fileWriter, doc);
    	    		} else {
    	    			notMutextList.add(doc.get("id").toString());
    	    		}
    	    		if(notMutextList.size()==pageSize) {
    	    			String query = "RULE_ID:%s AND BATCH_ID:%s AND id:(%s)";
    	    			query = String.format(query, rule.getRuleId(), batch.getBatchId(), StringUtils.join(notMutextList, " OR "));
    	    			SolrUtil.delete(collection, query, slave);
    	    			notMutextList.clear();
    	    		}
    	    	}
    	    }
		    if(notMutextList.size()>0) {
		    	String query = "RULE_ID:%s AND BATCH_ID:%s AND id:(%s)";
    			query = String.format(query, rule.getRuleId(), batch.getBatchId(), StringUtils.join(notMutextList, " OR "));
    			SolrUtil.delete(collection, query, slave);
    			notMutextList.clear();
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
    private void writeMutex(BufferedWriter fileWriter, SolrDocument doc) {
    	JSONObject json = new JSONObject();    	
    	//更新
		json.put("id", doc.get("id").toString());						
		//医保基金支付金额
		JSONObject up = new JSONObject();
		up.put("set", doc.get("MIN_ACTION_MONEY"));
		json.put("MIN_ACTION_MONEY", up);
		up = new JSONObject();
		up.put("set", doc.get("MAX_ACTION_MONEY"));
		json.put("MAX_ACTION_MONEY", up);
		//违规金额
		up = new JSONObject();
		up.put("set", doc.get("MIN_MONEY"));
		json.put("MIN_MONEY", up);
		up = new JSONObject();
		up.put("set", doc.get("MAX_MONEY"));
		json.put("MAX_MONEY", up);
		//违规数量
		up = new JSONObject();
		up.put("set", doc.get("ITEM_AMT"));
		json.put("ITEM_AMT", up);
    	try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (IOException e) {
        }
    }
    
    private String parseGroupcode() {		
		Set<String> groupcodeSet = new HashSet<String>();
    	List<MedicalRuleConditionSet> judgeList = ruleConditionList.stream().filter(s->"judge".equals(s.getType())).collect(Collectors.toList());
    	for(MedicalRuleConditionSet bean : judgeList) {
			groupcodeSet.add(bean.getExt1());
		}    
    	String result = StringUtils.join(groupcodeSet, "|");
    	return result;
	}
}
