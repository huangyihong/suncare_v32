/**
 * SolrRepeatDrugRuleHandle.java	  V1.0   2022年11月18日 上午10:56:05
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.solr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.jeecg.common.util.MD5Util;

import com.ai.common.MedicalConstant;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.engine.handle.rule.AbsRepeatDrugRuleHandle;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.vo.ChargedetailVO;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.engine.model.vo.RepeatComputeVO;
import com.ai.modules.engine.model.vo.RepeatDocumentVO;
import com.ai.modules.engine.model.vo.RepeatItemVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.service.impl.EngineActionServiceImpl;
import com.ai.modules.engine.service.impl.EngineCaseServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 重复用药规则计算引擎（solr模式）
 * @author  zhangly
 * Date: 2022年11月18日
 */
@Slf4j
public class SolrRepeatDrugRuleHandle extends AbsRepeatDrugRuleHandle {

	public SolrRepeatDrugRuleHandle(TaskProject task, TaskProjectBatch batch, List<MedicalYbDrug> drugList) {
		super(task, batch, drugList);
	}

	@Override
	public void generateUnreasonableAction() throws Exception {
		String ruleId = drugList.get(0).getParentCode();
		String actionId = "bhgxw-0012"; //重复用药
		MedicalActionDict actionDict = dictSV.queryActionDict(actionId);
    	if(actionDict==null) {
    		throw new Exception("未找到不合规行为编码=bhgxw-0012");
    	}
    	//同一剂型药品集合
        Map<String, Set<String>> dosageMap = this.dosageGrouping();
        if(dosageMap.size()==0) {
        	//不存在同一剂型多种药品，忽略运行
        	return;
        }
        //规则过滤条件
        Set<String> drugSet = new HashSet<String>();        	
        for (int i = 0, len = drugList.size(); i < len; i++) {
        	MedicalYbDrug drug = drugList.get(i);
        	drugSet.add(drug.getCode());
        }
        List<String> conditionList = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        conditionList.add("ITEMCODE:(" + StringUtils.join(drugSet, " OR ") + ")");
        conditionList.add("ITEM_QTY:{0 TO *}");
		conditionList.add("ITEM_AMT:{0 TO *}");
		//项目过滤条件
		ProjectFilterWhereVO filterVO = engineActionService.filterCondition(task, false);
		if(StringUtils.isNotBlank(filterVO.getCondition())) {
        	conditionList.add(filterVO.getCondition());
        }
		if (StringUtils.isNotBlank(task.getEtlSource())) {
            conditionList.add("ETL_SOURCE:" + task.getEtlSource());
        }
        if (StringUtils.isNotBlank(batch.getEtlSource())) {
            conditionList.add("ETL_SOURCE:" + batch.getEtlSource());
        }
  		String project_startTime = MedicalConstant.DEFAULT_START_TIME;
		String project_endTime = MedicalConstant.DEFAULT_END_TIME;
		project_startTime = task.getDataStartTime()!=null ? DateUtil.format(task.getDataStartTime(), "yyyy-MM-dd") : project_startTime;
        project_endTime = task.getDataEndTime()!=null ? DateUtil.format(task.getDataEndTime(), "yyyy-MM-dd") : project_endTime;
        //项目数据时间范围限制
        sb.setLength(0);	
		sb.append("VISITDATE:");
		sb.append("[").append(project_startTime).append(" TO ").append(project_endTime).append("]");
        conditionList.add(sb.toString());
        String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
		String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
		batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
		batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
		//业务数据时间范围限制
		sb.setLength(0);
		sb.append("VISITDATE:");
		sb.append("[").append(batch_startTime).append(" TO ").append(batch_endTime).append("]");
        conditionList.add(sb.toString());
        //项目医疗机构范围限制
        if(StringUtils.isNotBlank(task.getDataOrgFilter())) {
        	String value = task.getDataOrgFilter();
        	value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
        	conditionList.add("ORGID:"+value);
        }
        //批次医疗机构范围限制
        if(StringUtils.isNotBlank(batch.getCustomFilter())) {
        	String value = batch.getCustomFilter();
        	value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
        	conditionList.add("ORGID:"+value);
        }
        //基金支出金额>0
		conditionList.add("FUND_COVER:{0 TO *}");
		//自付比例<0
		conditionList.add("SELFPAY_PROP_MIN:[0 TO 1}");
		//病例重复用药map
		Map<String, RepeatDocumentVO> visitMap = new HashMap<String, RepeatDocumentVO>();
		SolrUtil.exportDocByPager(conditionList, EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, false, (doc, index) -> {
    		String visitid = doc.get("VISITID").toString();
    		String itemcode = doc.get("ITEMCODE").toString();
    		if(!visitMap.containsKey(visitid)) {
    			RepeatDocumentVO vo = new RepeatDocumentVO(visitid);
    			vo.addItemcode(itemcode);
    			vo.addDocument(doc);
    			visitMap.put(visitid, vo);
    		} else {
    			RepeatDocumentVO vo = visitMap.get(visitid);
    			vo.addItemcode(itemcode);
    			vo.addDocument(doc);
    			visitMap.put(visitid, vo);
    		}
        });
		//存在重复用药的就诊ID集合
		Set<String> visitidList = new HashSet<String>();
		Map<String, RepeatComputeVO> computeMap = new HashMap<String, RepeatComputeVO>();
		//遍历病例，删除未发生重复用药的病例
		Iterator<Map.Entry<String, RepeatDocumentVO>> it = visitMap.entrySet().iterator();  
        while(it.hasNext()){
        	Map.Entry<String, RepeatDocumentVO> entry = it.next();
        	if(entry.getValue().getItemcodeSet().size()<=1) {
        		//仅出现一种用药
        		it.remove();
        	} else {
        		List<Set<String>> repeatList = new ArrayList<Set<String>>();
        		for(Map.Entry<String, Set<String>> mapEntry : dosageMap.entrySet()) {
        			//判断同一剂型是否存在重复用药
        			Set<String> mixedSet = new HashSet<String>();
        			mixedSet.addAll(mapEntry.getValue());
        			//同一剂型多种药品与病例用药的并集
        			mixedSet.retainAll(entry.getValue().getItemcodeSet());
        			if(mixedSet.size()>1) {            				
        				repeatList.add(mixedSet);
        			}
        		}
        		if(repeatList.size()==0) {
        			it.remove();
        		} else {
        			visitidList.add(entry.getKey());
        			RepeatComputeVO computeVO = new RepeatComputeVO(entry.getKey(), repeatList);
        			computeMap.put(entry.getKey(), computeVO);
        			Map<String, SolrDocument> documentMap = entry.getValue().getDocumentMap();
        			for(Set<String> repeatSet : repeatList) {
        				for(String itemcode : repeatSet) {
        					if(!computeVO.existsDocument(itemcode)) {
        						computeVO.addDocument(documentMap.get(itemcode));
        					}
        				}
        			}
        		}
        	}
        }
        
        int pageSize = 500;
		int pageNum = (visitidList.size() + pageSize - 1) / pageSize;
		//数据分割
		List<Set<String>> mglist = new ArrayList<Set<String>>();
	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
	    	mglist.add(visitidList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toSet()));
	    });            
        for(Set<String> subList : mglist) {
        	conditionList.clear();
        	conditionList.add("PRESCRIPTTIME:?*");
            conditionList.add("ITEMCODE:(" + StringUtils.join(drugSet, " OR ") + ")");
    	    String visitidFq = "VISITID:(\"" + StringUtils.join(subList, "\",\"") + "\")";
    	    conditionList.add(visitidFq);
    	    
    	    SolrQuery solrQuery = new SolrQuery("*:*");
			// 设定查询字段
			solrQuery.addFilterQuery(conditionList.toArray(new String[0]));
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
			SolrUtil.export(solrQuery, EngineUtil.DWB_CHARGE_DETAIL, false, (map, index) -> {
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
				if(map.get("AMOUNT")!=null) {
					vo.setAmount(new BigDecimal(map.get("AMOUNT").toString()));
				}
				if(map.get("FEE")!=null) {
					vo.setFee(new BigDecimal(map.get("FEE").toString()));
				}
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
				RepeatComputeVO compute = computeMap.get(visitId);
				compute.add(vo);
	        });	
        }
        
        if(computeMap.size()>0) {            	
        	// 重复用药明细数据写入文件
  	    	String importFilePath = SolrUtil.importFolder + "/" + EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION + "/" + batch.getBatchId() + "/" + ruleId + ".json";
            BufferedWriter fileWriter = new BufferedWriter(
            	new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
      		
            //写文件头
            fileWriter.write("[");
            for(Map.Entry<String, RepeatComputeVO> entry : computeMap.entrySet()) {
            	RepeatComputeVO computeVO = entry.getValue();
            	List<RepeatItemVO> repeatItemList = computeVO.computeRepeat();
            	if(repeatItemList.size()>0) {
            		for(RepeatItemVO repeatItem : repeatItemList) {
            			JSONObject json = this.parseJSONObject(task, batch, drugList.get(0), computeVO, repeatItem, actionDict);
        	    		try {
                            fileWriter.write(json.toJSONString());
                            fileWriter.write(',');
                        } catch (Exception e) {

                        }
            		}
            	}
            }
            
            // 文件尾
            fileWriter.write("]");
            fileWriter.flush();
            fileWriter.close();
            //导入solr
            SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION);
            
            //同步到结果表
            this.syncUnreasonableAction(task, batch, ruleId, false);
            //不合规行为汇总
            String[] fqs = new String[] {"RULE_ID:"+ruleId};
            engineActionService.executeGroupBy(batch.getBatchId(), actionId, fqs);
        }
	}

	private JSONObject parseJSONObject(TaskProject task, TaskProjectBatch batch, MedicalYbDrug drug, 
			RepeatComputeVO computeVO, RepeatItemVO repeatItem, MedicalActionDict actionDict) {
		String itemcode = repeatItem.getItemcode();
		SolrDocument document = computeVO.getDocument(itemcode);
		JSONObject json = new JSONObject();
        String id = MD5Util.MD5Encode(batch.getBatchId().concat("_").concat(drug.getParentCode()).concat("_").concat(repeatItem.getItemcode()).concat("_").concat(repeatItem.getDay().concat("_").concat(computeVO.getVisitid())), "utf-8");
        json.put("id", id);
        for (String field : EngineActionServiceImpl.DURG_ACTION_FIELD) {
        	if(EngineActionServiceImpl.DETAIL_DRUGACTION_MAPPING.containsKey(field)) {
        		field = EngineActionServiceImpl.DETAIL_DRUGACTION_MAPPING.get(field);
        	}
            Object val = document.get(field);
            if (val != null) {
                json.put(field, val);
            }
        }
        json.put("ITEM_ID", document.get("id"));
        json.put("VISITID", computeVO.getVisitid());
        json.put("ITEMCODE", repeatItem.getItemcode());
        json.put("ITEMNAME", repeatItem.getItemname());
        String text = repeatItem.getItemnameSrc();//原始项目名称
        if(repeatItem.getItemnameSrcSet()!=null && repeatItem.getItemnameSrcSet().size()>0) {
        	text = StringUtils.join(repeatItem.getItemnameSrcSet(), ",");
        }
        json.put("ITEMNAME_SRC", text);
        json.put("ITEM_QTY", repeatItem.getAmount());
        json.put("ITEM_AMT", repeatItem.getFee());
        json.put("ITEMPRICE_MAX", repeatItem.getUnitPrice());
                
        json.put("RULE_ID", drug.getParentCode());
        json.put("RULE_NAME", drug.getParentName());
        String ruleFName = drug.getParentCode() + "::" + drug.getParentName();
        json.put("RULE_FNAME", ruleFName);
        String actionDesc = "同一患者同时开具2种以上药理作用相同的药物";
		if(StringUtils.isNotBlank(actionDesc)) {
			actionDesc = actionDict.getActionDesc();
		}
        json.put("ACTION_DESC", actionDesc);
        json.put("RULE_BASIS", "《医疗保障基金使用监督管理条例》第十五条“不得违反诊疗规范过度诊疗、过度检查、分解处方、超量开药、重复开药”；卫医管发〔2010〕28号《医院处方点评管理规范（试行）》“第十九条  有下列情况之一的，应当判定为超常处方：4.无正当理由为同一患者同时开具2种以上药理作用相同药物的。”");
        json.put("RULE_TYPE", RULE_TYPE);       
        json.put("ACTION_TYPE_ID", RULE_TYPE);
        json.put("ACTION_TYPE_NAME", "不合理用药-重复用药");
        json.put("RULE_GRADE", drug.getRuleGrade());
        json.put("RULE_GRADE_REMARK", drug.getRuleGradeRemark());
        json.put("ACTION_ID", actionDict.getActionId());
        json.put("ACTION_NAME", actionDict.getActionName());
        json.put("RULE_LEVEL", actionDict.getRuleLevel());
        //基金支出金额
        json.put("MIN_ACTION_MONEY", repeatItem.getFundConver());
        json.put("MAX_ACTION_MONEY", repeatItem.getFundConver());
        //收费项目费用
        json.put("MIN_MONEY", repeatItem.getFee());
        json.put("MAX_MONEY", repeatItem.getFee());
        json.put("MUTEX_ITEM_CODE", repeatItem.getMainCode());
		json.put("MUTEX_ITEM_NAME", repeatItem.getMainCode().concat(EngineUtil.SPLIT_KEY).concat(repeatItem.getMainName()));
        String content = "["+repeatItem.getItemname()+ "]与[" + repeatItem.getMainName()+"]都属于"+drug.getParentName()+"，存在重复用药";
        json.put("BREAK_RULE_CONTENT", content);
        json.put("BREAK_RULE_TIME", repeatItem.getDay());
        json.put("AI_OUT_CNT", repeatItem.getAmount());
        
        json.put("GEN_DATA_TIME", DateUtils.now());
        json.put("PROJECT_ID", task.getProjectId());
        json.put("PROJECT_NAME", task.getProjectName());
        json.put("BATCH_ID", batch.getBatchId());
        json.put("TASK_BATCH_NAME", batch.getBatchName());
        return json;
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
	protected void syncUnreasonableAction(TaskProject task, TaskProjectBatch batch, String ruleId, boolean slave) throws Exception {
    	BufferedWriter fileWriter = null;
    	SolrClient solrClient = null;
    	try {
    		// 数据写入文件
            String importFilePath = SolrUtil.importFolder + "/" + EngineUtil.MEDICAL_UNREASONABLE_ACTION + "/" + batch.getBatchId() + "/" + ruleId + ".json";
            fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
            //写文件头
            fileWriter.write("[");

    		solrClient = SolrUtil.getClient(EngineUtil.DWB_MASTER_INFO, slave);

    		List<String> conditionList = new ArrayList<String>();
        	String where = "_query_:\"%sRULE_TYPE:%s AND RULE_ID:%s AND BATCH_ID:%s\"";
        	EngineMapping mapping = new EngineMapping(EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, "VISITID", "VISITID");
    		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
        	where  = String.format(where, plugin.parse(), RULE_TYPE, ruleId, batch.getBatchId());
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

		    QueryResponse response = SolrUtil.process(solrClient, solrQuery, callback);
		    if(total[0]>0) {
		    	this.writeSyncUnreasonableActionJson(fileWriter, task, batch, ruleId, visitidList, masterMapList, slave);
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
					this.writeSyncUnreasonableActionJson(fileWriter, task, batch, ruleId, visitidList, masterMapList, slave);
				}
			}

			// 文件尾
            fileWriter.write("]");
            fileWriter.flush();
            //导入solr主服务器
            SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
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
	
	private void writeSyncUnreasonableActionJson(BufferedWriter fileWriter, TaskProject task, TaskProjectBatch batch, String ruleId,
			List<String> visitidList, Map<String, Map<String, Object>> masterMapList, boolean slave) throws Exception {
		
    	List<String> conditionList = new ArrayList<String>();
    	conditionList.add("VISITID:(\""+StringUtils.join(visitidList, "\",\"")+"\")");
    	conditionList.add("RULE_TYPE:"+RULE_TYPE);
    	conditionList.add("RULE_ID:"+ruleId);
    	conditionList.add("BATCH_ID:"+batch.getBatchId());

		SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, slave, (doc, index) -> {
		    String visitid = doc.get("VISITID").toString();
		    if(masterMapList.containsKey(visitid)) {
		    	Map<String, Object> masterMap = masterMapList.get(visitid);
		    	JSONObject json = new JSONObject();
		    	json.put("id", doc.get("id"));

		    	for(Map.Entry<String, String> entry : EngineCaseServiceImpl.CASE_FIELD_MAPPING.entrySet()) {
		            Object val = masterMap.get(entry.getValue());
		            if (val != null) {
		                json.put(entry.getKey(), val);
		            }
		        }		        
		        json.put("BUSI_TYPE", MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE);
		        json.put("GEN_DATA_TIME", DateUtils.now());
		        json.put("PROJECT_ID", task.getProjectId());
		        json.put("PROJECT_NAME", task.getProjectName());
		        json.put("BATCH_ID", batch.getBatchId());
		        json.put("TASK_BATCH_NAME", batch.getBatchName());
		        for(Map.Entry<String, String> entry : EngineActionServiceImpl.DURG_ACTION_MAPPING.entrySet()){
		            json.put(entry.getKey(), doc.get(entry.getValue()));
		        }
		        
		        json.put("FIR_REVIEW_STATUS", "init");
		        json.put("CHARGEDATE", doc.get("BREAK_RULE_TIME"));
		        
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
}
