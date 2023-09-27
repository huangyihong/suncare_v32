/**
 * EngineActionServiceImpl.java	  V1.0   2020年9月9日 下午4:21:09
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
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.MD5Util;
import org.jeecg.common.util.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.entity.StdHoslevelFundpayprop;
import com.ai.modules.engine.model.EngineCntResult;
import com.ai.modules.engine.model.EngineLimitScopeEnum;
import com.ai.modules.engine.model.dto.ActionTypeDTO;
import com.ai.modules.engine.model.rule.EngineParamMasterInfoRule;
import com.ai.modules.engine.model.rule.EngineParamTreatResultRule;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.service.IApiEngineService;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.service.api.IApiSummaryService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskActionFieldConfigService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineActionServiceImpl implements IEngineActionService {
	//结果临时表的字段集合
	public static final Set<String> DURG_ACTION_FIELD = new LinkedHashSet<String>();
	//detail表与结果临时表的映射关系
	public static final Map<String, String> DETAIL_DRUGACTION_MAPPING = new LinkedHashMap<String, String>();
    public static final Set<String> UPDATE_DURG_ACTION_FIELD = new LinkedHashSet<String>();
    public static final Map<String, String> DURG_ACTION_MAPPING = new LinkedHashMap<String, String>();
    public static final Map<String, String> SCOPE_MAPPING = new HashMap<String, String>();
    static {
        DURG_ACTION_FIELD.add("VISITID");
        DURG_ACTION_FIELD.add("ORGID");
        DURG_ACTION_FIELD.add("ORGNAME");
        DURG_ACTION_FIELD.add("VISITDATE");
        DURG_ACTION_FIELD.add("CLIENTID");
        DURG_ACTION_FIELD.add("ID_NO");
        DURG_ACTION_FIELD.add("CLIENTNAME");
        DURG_ACTION_FIELD.add("SEX");
        DURG_ACTION_FIELD.add("BIRTHDAY");
        DURG_ACTION_FIELD.add("ITEMCODE");
        DURG_ACTION_FIELD.add("ITEMNAME");
        DURG_ACTION_FIELD.add("ITEMNAME_SRC");
        DURG_ACTION_FIELD.add("CHARGECLASS_ID");
        DURG_ACTION_FIELD.add("CHARGECLASS");
        DURG_ACTION_FIELD.add("ITEM_QTY");
        DURG_ACTION_FIELD.add("ITEM_QTY_HB");
        DURG_ACTION_FIELD.add("ITEM_QTY_TB");
        DURG_ACTION_FIELD.add("VISITTYPE");
        DURG_ACTION_FIELD.add("ITEM_AMT");
        DURG_ACTION_FIELD.add("ITEM_AMT_HB");
        DURG_ACTION_FIELD.add("ITEM_AMT_TB");
        DURG_ACTION_FIELD.add("ITEM_AMT_RATIO");
        DURG_ACTION_FIELD.add("ZY_DAYS_CALCULATE");
        DURG_ACTION_FIELD.add("ITEM_DAYAVG_QTY");
        DURG_ACTION_FIELD.add("ITEM_DAYAVG_AMT");
        DURG_ACTION_FIELD.add("FUND_COVER");
        DURG_ACTION_FIELD.add("ITEMPRICE_MAX");
        DURG_ACTION_FIELD.add("SELFPAY_PROP_MIN");
        DURG_ACTION_FIELD.add("ETL_SOURCE");
        DURG_ACTION_FIELD.add("ETL_SOURCE_NAME");
        DURG_ACTION_FIELD.add("ETL_TIME");
        
        DETAIL_DRUGACTION_MAPPING.put("AMOUNT", "ITEM_QTY");
        DETAIL_DRUGACTION_MAPPING.put("FEE", "ITEM_AMT");
        DETAIL_DRUGACTION_MAPPING.put("ITEMPRICE", "ITEMPRICE_MAX");
        DETAIL_DRUGACTION_MAPPING.put("SELFPAY_PROP", "SELFPAY_PROP_MIN");

        UPDATE_DURG_ACTION_FIELD.add("VISITID");
        UPDATE_DURG_ACTION_FIELD.add("YEARAGE");
        UPDATE_DURG_ACTION_FIELD.add("DISEASECODE");
        UPDATE_DURG_ACTION_FIELD.add("DISEASENAME");

        DURG_ACTION_MAPPING.put("ITEM_ID", "ITEM_ID");
        DURG_ACTION_MAPPING.put("CASE_ID", "ITEMCODE");
        DURG_ACTION_MAPPING.put("CASE_NAME", "ITEMNAME");
        DURG_ACTION_MAPPING.put("ACTION_NAME", "ACTION_NAME");
        DURG_ACTION_MAPPING.put("ACTION_ID", "ACTION_ID");
        DURG_ACTION_MAPPING.put("ACTION_TYPE_ID", "ACTION_TYPE_ID");
        DURG_ACTION_MAPPING.put("ACTION_TYPE_NAME", "ACTION_TYPE_NAME");
        DURG_ACTION_MAPPING.put("ACTION_DESC", "ACTION_DESC");
        DURG_ACTION_MAPPING.put("ACTION_MONEY", "MIN_ACTION_MONEY");
        DURG_ACTION_MAPPING.put("MAX_ACTION_MONEY", "MAX_ACTION_MONEY");
        DURG_ACTION_MAPPING.put("MIN_MONEY", "MIN_MONEY");
        DURG_ACTION_MAPPING.put("MAX_MONEY", "MAX_MONEY");
        DURG_ACTION_MAPPING.put("ITEMCODE", "ITEMCODE");
        DURG_ACTION_MAPPING.put("ITEMNAME", "ITEMNAME");
        DURG_ACTION_MAPPING.put("ITEMNAME_SRC", "ITEMNAME_SRC");
        DURG_ACTION_MAPPING.put("CHARGECLASS_ID", "CHARGECLASS_ID");
        DURG_ACTION_MAPPING.put("CHARGECLASS", "CHARGECLASS");
        DURG_ACTION_MAPPING.put("ITEM_AMT", "ITEM_AMT");
        DURG_ACTION_MAPPING.put("ITEM_QTY", "ITEM_QTY");
        DURG_ACTION_MAPPING.put("ITEMPRICE_MAX", "ITEMPRICE_MAX");
        DURG_ACTION_MAPPING.put("SELFPAY_PROP_MIN", "SELFPAY_PROP_MIN");
        DURG_ACTION_MAPPING.put("FUND_COVER", "FUND_COVER");
        DURG_ACTION_MAPPING.put("RULE_ID", "RULE_ID");
        DURG_ACTION_MAPPING.put("RULE_FNAME", "RULE_FNAME");
        DURG_ACTION_MAPPING.put("RULE_SCOPE_NAME", "RULE_SCOPE_NAME");
        DURG_ACTION_MAPPING.put("RULE_SCOPE", "RULE_SCOPE");
        DURG_ACTION_MAPPING.put("BREAK_RULE_CONTENT", "BREAK_RULE_CONTENT");
        DURG_ACTION_MAPPING.put("RULE_BASIS", "RULE_BASIS");
        DURG_ACTION_MAPPING.put("MUTEX_ITEM_CODE", "MUTEX_ITEM_CODE");
        DURG_ACTION_MAPPING.put("MUTEX_ITEM_NAME", "MUTEX_ITEM_NAME");
        DURG_ACTION_MAPPING.put("AI_ITEM_CNT", "AI_ITEM_CNT");
        DURG_ACTION_MAPPING.put("AI_OUT_CNT", "AI_OUT_CNT");
        DURG_ACTION_MAPPING.put("RULE_LEVEL", "RULE_LEVEL");
        DURG_ACTION_MAPPING.put("RULE_LIMIT", "RULE_LIMIT");
        DURG_ACTION_MAPPING.put("RULE_GRADE", "RULE_GRADE");
        DURG_ACTION_MAPPING.put("RULE_GRADE_REMARK", "RULE_GRADE_REMARK");
        
        //违反医保药品限定适应症
        SCOPE_MAPPING.put("LIMIT_SCOPE_TO_DRUG_ACTION-13", "ACTION_LIST-296");
        SCOPE_MAPPING.put("LIMIT_SCOPE_TO_DRUG_ACTION-31", "ACTION_LIST-296");
        SCOPE_MAPPING.put("LIMIT_SCOPE_TO_DRUG_ACTION-40", "ACTION_LIST-296");
        //违反物价项目内容收费
        SCOPE_MAPPING.put("LIMIT_SCOPE_TO_CHARGE_ACTION-27", "ACTION_LIST_CHARGE-fitGroups1");
        //违反诊疗项目政策限定重复收费
        SCOPE_MAPPING.put("LIMIT_SCOPE_TO_CHARGE_ACTION-29", "LIMIT_SCOPE_TO_CHARGE_ACTION-28");
    }
	
	@Autowired
    private IApiDictService dictSV;
	@Autowired
	private IApiSummaryService apiSummarySV;  
    @Autowired
    private IApiEngineService apiEngineSV;
    @Autowired
    private IApiTaskService taskService;
    @Autowired
    private ITaskActionFieldConfigService  taskActionFieldConfigService;

	@Override
    public void deleteSolr(String batchId, String ruleType, boolean slave) throws Exception {
        SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION,
                "RULE_TYPE:" + ruleType + " AND BATCH_ID:" + batchId, slave);

        if("1".equals(ruleType)) {
        	SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "BUSI_TYPE:DRUG AND BATCH_ID:" + batchId);
        } else if("2".equals(ruleType)) {
        	SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "BUSI_TYPE:CHARGE AND BATCH_ID:" + batchId);
        } else if("4".equals(ruleType)) {
            SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "BUSI_TYPE:TREAT AND BATCH_ID:" + batchId);
        } else {
        	SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "BUSI_TYPE:" + ruleType + " AND BATCH_ID:" + batchId);
        }
    }
	
	@Override
    public void deleteSolr(String batchId, String itemcode, String ruleType, boolean slave) throws Exception {
        String where = "RULE_TYPE:%s AND ITEMCODE:%s AND BATCH_ID:%s";
        where = String.format(where, ruleType, itemcode, batchId);
		SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, where, slave);

        if("1".equals(ruleType)) {
        	where = "BUSI_TYPE:DRUG AND CASE_ID:%s AND BATCH_ID:%s";
        	where = String.format(where, itemcode, batchId);
        	SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
        } else if("2".equals(ruleType)) {
        	where = "BUSI_TYPE:CHARGE AND CASE_ID:%s AND BATCH_ID:%s";
        	where = String.format(where, itemcode, batchId);
        	SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
        } else if("4".equals(ruleType)) {
        	where = "BUSI_TYPE:TREAT AND CASE_ID:%s AND BATCH_ID:%s";
        	where = String.format(where, itemcode, batchId);
            SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
        }
    }
	
	@Override
    public void deleteSolrByRule(String batchId, String ruleId, String ruleType, boolean slave) throws Exception {
        String where = "RULE_TYPE:%s AND RULE_ID:%s AND BATCH_ID:%s";
        where = String.format(where, ruleType, ruleId, batchId);
		SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, where, slave);

        if("1".equals(ruleType)) {
        	where = "BUSI_TYPE:DRUG AND RULE_ID:%s AND BATCH_ID:%s";
        	where = String.format(where, ruleId, batchId);
        	SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
        } else if("2".equals(ruleType)) {
        	where = "BUSI_TYPE:CHARGE AND RULE_ID:%s AND BATCH_ID:%s";
        	where = String.format(where, ruleId, batchId);
        	SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
        } else if("4".equals(ruleType)) {
        	where = "BUSI_TYPE:TREAT AND RULE_ID:%s AND BATCH_ID:%s";
        	where = String.format(where, ruleId, batchId);
            SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
        } else {
        	where = "BUSI_TYPE:%s AND RULE_ID:%s AND BATCH_ID:%s";
        	where = String.format(where, ruleType, ruleId, batchId);
            SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
        }
    }

	@Override
    public JSONObject writerJson(BufferedWriter fileWriter, Map<String, Object> map, 
    		TaskProject task, TaskProjectBatch batch, 
    		String drugCode, List<MedicalDrugRule> ruleList, EngineLimitScopeEnum limitScopeEnum,
    		ActionTypeDTO dto, Map<String, MedicalActionDict> actionDictMap) {
        JSONObject json = new JSONObject();
        //String id = MD5Util.MD5Encode(batch.getBatchId().concat("_").concat(drugCode).concat("_").concat((String) map.get("VISITID")), "utf-8");
        //id生成策略
    	String template = "${batchId}_${itemCode}_${visitid}";
        Properties properties = new Properties();
        properties.put("batchId", batch.getBatchId());
        properties.put("itemCode", drugCode);
        properties.put("visitid", map.get("VISITID"));
        template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
        String id = MD5Util.MD5Encode(template, "UTF-8");
        json.put("id", id);
        for (String field : DURG_ACTION_FIELD) {
            Object val = map.get(field);
            if (val != null) {
                json.put(field, val);
            }
        }

        json.put("GEN_DATA_TIME", DateUtils.now());
        json.put("PROJECT_ID", task.getProjectId());
        json.put("PROJECT_NAME", task.getProjectName());
        json.put("BATCH_ID", batch.getBatchId());
        json.put("TASK_BATCH_NAME", batch.getBatchName());
        String[] ruleIds = new String[ruleList.size()];
        String[] ruleNames = new String[ruleList.size()];
        String[] ruleDescs = new String[ruleList.size()];
        String[] ruleFNames = new String[ruleList.size()];
        String[] ruleFDescs = new String[ruleList.size()];
        Set<String> ruleBasisSet = new HashSet<String>();
        Set<String> actionDescSet = new HashSet<String>();
        int index = 0;
        for (MedicalDrugRule rule : ruleList) {
            ruleIds[index] = rule.getRuleId();
            String temp = null;
            if ("1".equals(rule.getRuleType())) {
                temp = rule.getDrugNames();
            } else if ("2".equals(rule.getRuleType())) {
                temp = rule.getChargeItems();
            } else if ("4".equals(rule.getRuleType())) {
                temp = rule.getDiseaseNames();
            }
            ruleFNames[index] = rule.getRuleId() + "::" + temp;
            ruleNames[index] = temp;
            if (rule.getMessage() != null) {
                ruleFDescs[index] = rule.getRuleId() + "::" + rule.getMessage();
                ruleDescs[index] = rule.getMessage();
            }
            json.put("RULE_TYPE", rule.getRuleType());
            json.put("ACTION_TYPE_ID", dto.getActionTypeId());
            json.put("ACTION_TYPE_NAME", dto.getActionTypeName());
            if(StringUtils.isNotBlank(rule.getActionId())) {
            	json.put("ACTION_ID", rule.getActionId());
            }
            if(StringUtils.isNotBlank(rule.getActionName())) {
            	json.put("ACTION_NAME", rule.getActionName());
            }
            if(actionDictMap.containsKey(rule.getActionId())) {
            	MedicalActionDict actionDict = actionDictMap.get(rule.getActionId());
            	json.put("ACTION_NAME", actionDict.getActionName());
            	json.put("RULE_LEVEL", actionDict.getRuleLevel());
            }
            if(StringUtils.isNotBlank(rule.getMessage())) {
            	actionDescSet.add(rule.getMessage());
            }
            if(StringUtils.isNotBlank(rule.getRuleBasis())) {
            	ruleBasisSet.add(rule.getRuleBasis());
            }
            index++;
        }
        json.put("RULE_ID", ruleIds);
        json.put("RULE_NAME", ruleNames);
        json.put("RULE_DESC", ruleDescs);
        json.put("RULE_FNAME", ruleFNames);
        json.put("RULE_FDESC", ruleFDescs);
        //基金支出金额
        json.put("MIN_ACTION_MONEY", map.get("FUND_COVER"));
        json.put("MAX_ACTION_MONEY", map.get("FUND_COVER"));
        //收费项目费用
        json.put("MIN_MONEY", map.get("ITEM_AMT"));
        json.put("MAX_MONEY", map.get("ITEM_AMT"));
        if(actionDescSet.size()>0) {
        	String desc = StringUtils.join(actionDescSet, ",");
        	if(actionDescSet.size()>1) {
        		desc = "["+StringUtils.join(actionDescSet, "],[")+"]";
        	}
        	json.put("ACTION_DESC", desc); 
        }
        if(ruleBasisSet.size()>0) {
        	json.put("RULE_BASIS", StringUtils.join(ruleBasisSet, ","));
        }
        if(limitScopeEnum!=null) {
        	json.put("RULE_SCOPE", limitScopeEnum.getCode());
			json.put("RULE_SCOPE_NAME", limitScopeEnum.getName());
        }
        try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (Exception e) {

        }
        return json;
    }

	@Override
    public JSONObject writerJson(BufferedWriter fileWriter, Map<String, Object> map, 
    		MedicalDrugRule rule, EngineLimitScopeEnum limitScopeEnum,
    		ActionTypeDTO dto) {
        JSONObject json = new JSONObject();
        json.put("id", UUIDGenerator.generate());
        for (String field : DURG_ACTION_FIELD) {
            Object val = map.get(field);
            if (val != null) {
                json.put(field, val);
            }
        }

        String batchId = rule.getRuleId();
        json.put("GEN_DATA_TIME", DateUtils.now());
        json.put("PROJECT_ID", batchId);
        json.put("BATCH_ID", batchId);
        String temp = null;
        if ("1".equals(rule.getRuleType())) {
            temp = rule.getDrugNames();
        } else if ("2".equals(rule.getRuleType())) {
            temp = rule.getChargeItems();
        } else if ("4".equals(rule.getRuleType())) {
            temp = rule.getDiseaseNames();
        }
        json.put("RULE_TYPE", rule.getRuleType());
        json.put("RULE_ID", rule.getRuleId());
        json.put("RULE_NAME", temp);
        json.put("RULE_FNAME", rule.getRuleId() + "::" + temp);
        if (rule.getMessage() != null) {
            json.put("RULE_DESC", rule.getMessage());
            json.put("RULE_FDESC", rule.getRuleId() + "::" + rule.getMessage());
        }
        if(limitScopeEnum!=null) {
        	json.put("RULE_SCOPE", limitScopeEnum.getCode());
			json.put("RULE_SCOPE_NAME", limitScopeEnum.getName());
        }
        json.put("ACTION_TYPE_ID", dto.getActionTypeId());
        json.put("ACTION_TYPE_NAME", dto.getActionTypeName());
        if(StringUtils.isNotBlank(rule.getActionId())) {
        	json.put("ACTION_ID", rule.getActionId());
        }
        if(StringUtils.isNotBlank(rule.getActionName())) {
            json.put("ACTION_NAME", rule.getActionName());
        }
        json.put("ACTION_DESC", rule.getMessage());
        json.put("RULE_BASIS", rule.getRuleBasis());
        //基金支出金额
        json.put("MIN_ACTION_MONEY", map.get("FUND_COVER"));
        json.put("MAX_ACTION_MONEY", map.get("FUND_COVER"));
        //收费项目费用
        json.put("MIN_MONEY", map.get("ITEM_AMT"));
        json.put("MAX_MONEY", map.get("ITEM_AMT"));
        try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (Exception e) {

        }
        return json;
    }

	/**
     *
     * 功能描述：更新字段
     *
     * @author  zhangly
     * <p>创建日期 ：2020年7月14日 下午5:33:20</p>
     *
     * @param dataList
     * @param collection
     * @param batchId
     * @param itemCode
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    @Override
    public void updateAction(List<JSONObject> dataList, String collection, String batchId, String itemCode) throws Exception {
    	if(dataList==null || dataList.size()==0) {
    		return;
    	}
    	log.info("更新字段开始");
    	// 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batchId + "/" + itemCode + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");

        int size = dataList.size();
        int pageSize = 1000;
		int pageNum = (size + pageSize - 1) / pageSize;
		//数据分割
		List<List<JSONObject>> mglist = new ArrayList<List<JSONObject>>();
	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
	    	mglist.add(dataList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
	    });

	    for(List<JSONObject> list : mglist) {
	    	Map<String, Set<String>> visitMap = new HashMap<String, Set<String>>();
	    	for(JSONObject map : list) {
	    		String visitId = map.get("VISITID").toString();
				String id = map.get("id").toString();
				if(visitMap.containsKey(visitId)) {
					visitMap.get(visitId).add(id);
				} else {
					Set<String> set = new HashSet<String>();
					set.add(id);
					visitMap.put(visitId, set);
				}
	    	}
	    	writeUpdateActionJson(fileWriter, visitMap);
	    }
		// 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();
        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, collection);
        log.info("更新字段结束");
    }

    /**
     *
     * 功能描述：更新数据写入文件
     *
     * @author  zhangly
     * <p>创建日期 ：2020年7月14日 下午4:18:52</p>
     *
     * @param fileWriter
     * @param visitMap
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    private void writeUpdateActionJson(BufferedWriter fileWriter, Map<String, Set<String>> visitMap) throws Exception {
    	Map<String, SolrDocument> docMap = new HashMap<String, SolrDocument>();
    	String visitIdFq = "VISITID:(\"" + StringUtils.join(visitMap.keySet(), "\",\"") + "\")";
        SolrQuery query = new SolrQuery("*:*");
        query.addFilterQuery(visitIdFq);
        for(String field : UPDATE_DURG_ACTION_FIELD) {
        	//查询的字段
        	query.addField(field);
        }
        query.setRows(Integer.MAX_VALUE);
        SolrDocumentList unreasonableList = SolrUtil.call(query, EngineUtil.DWB_MASTER_INFO).getResults();
        for (SolrDocument doc : unreasonableList) {
        	String visitId = doc.getFieldValue("VISITID").toString();
        	docMap.put(visitId, doc);
        }
        for(Map.Entry<String, Set<String>> entry : visitMap.entrySet()) {
        	if(docMap.containsKey(entry.getKey())) {
        		for(String primaryKey : entry.getValue()) {
        			JSONObject json = new JSONObject();
        			json.put("id", primaryKey);
        			JSONObject up = null;
        			SolrDocument doc = docMap.get(entry.getKey());
        			for(String field : UPDATE_DURG_ACTION_FIELD) {
        				if(!"VISITID".equals(field)) {
        					up = new JSONObject();
        					up.put("set", doc.getFieldValue(field));
        					json.put(field, up);
        				}
        			}
        			fileWriter.write(json.toJSONString());
     	            fileWriter.write(',');
        		}
        	}
        }
    }

    @Override
    public List<String> ignoreNullWhere(MedicalDrugRule rule) {
    	List<String> conditionList = new ArrayList<String>();
		EngineParamMasterInfoRule paramMasterInfoRule = new EngineParamMasterInfoRule(rule);
		String condition = paramMasterInfoRule.ignoreNullWhere();
		if(StringUtils.isNotBlank(condition)) {
			conditionList.add(condition);
		}
		//限制范围
  		String[] limitScope = rule.getLimitScope().split(",");
  		Set<String> limitScopeSet = new HashSet<String>();
  		for(String scope : limitScope) {
  			limitScopeSet.add(scope);
  		}
  		if(limitScopeSet.contains(EngineLimitScopeEnum.CODE_13.getCode())) {
  			//适用症
  			StringBuilder sb = new StringBuilder();
  			sb.append("_query_:\"");
  			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
  			sb.append(plugin.parse());
  			sb.append("DISEASECODE:?*");
  			sb.append("\"");
  			conditionList.add(sb.toString());
  		}
  		if(limitScopeSet.contains(EngineLimitScopeEnum.CODE_37.getCode())) {
  			//支付时长
  			StringBuilder sb = new StringBuilder();
			sb.append("_query_:\"");
			String fromIndex = "MAPPER_DWS_PATIENT_CHARGEITEM_SUM";
			switch(rule.getPayDurationPeriod()) {
				case "1M":
					fromIndex = fromIndex.concat("_M");
					break;
				case "3M":
					fromIndex = fromIndex.concat("_Q");
					break;	
				case "1Y":
					fromIndex = fromIndex.concat("_Y");
					break;
				default:
					fromIndex = fromIndex.concat("_Y");
					break;
			}
			//中间表
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(fromIndex, "VISITID", "VISITID");
			sb.append(plugin.parse());
			plugin = new SolrJoinParserPlugin("DWS_PATIENT_CHARGEITEM_SUM", "id", "DWSID");
			sb.append(plugin.parse());
			if("1M".equals(rule.getPayDurationPeriod())) {
				sb.append("DAYS_QTY:");
			} else if("3M".endsWith(rule.getPayDurationPeriod())) {
				if("day".equals(rule.getPayDurationUnit())) {
					sb.append("DAYS_QTY:");
				} else if("month".equals(rule.getPayDurationUnit())) {
					sb.append("MONTHS_QTY:");
				} else {
					sb.append("DAYS_QTY:");
				}
			} else {
				if("day".equals(rule.getPayDurationUnit())) {
					sb.append("DAYS_QTY:");
				} else if("month".equals(rule.getPayDurationUnit())) {
					sb.append("MONTHS_QTY:");
				} else {
					sb.append("DAYS_QTY:");
				}
			}
			sb.append("[0 TO *}");
			sb.append(" AND ITEMCODE:").append(rule.getDrugCode());
			sb.append("\"");
			conditionList.add(sb.toString());
  		}
  		if(limitScopeSet.contains(EngineLimitScopeEnum.CODE_40.getCode())) {
  			//检查结果
  			EngineParamTreatResultRule paramRule = new EngineParamTreatResultRule(rule.getTestResultValue(), rule.getTestResultItemType(), rule.getTestResultItemCode(),
        			rule.getTestResultValueType(), rule.getTestResultUnit());
  			conditionList.add(paramRule.ignoreWhere());
  		}
  		if(limitScopeSet.contains(EngineLimitScopeEnum.CODE_38.getCode())) {
  			//频次
  			Set<String> periodSet = new HashSet<String>();
  			String period = rule.getPeriod();
  			periodSet.add(period);
  			if(StringUtils.isNotBlank(rule.getTwoPeriod())) {
  				period = rule.getTwoPeriod();
  				periodSet.add(period);
  			}
  			if(periodSet.contains("1")) {
  				//一次就诊
  				conditionList.add("ITEM_QTY:{0 TO *}");
  			} else if(periodSet.contains("6")) {
  				conditionList.add("ITEM_DAYAVG_QTY:{0 TO *}");
  			}
  		}

  		return conditionList;
    }

    @Deprecated
	@Override
    public void syncUnreasonableAction(TaskProject task, TaskProjectBatch batch, String type, boolean slave) throws Exception {
    	BufferedWriter fileWriter = null;
    	SolrClient solrClient = null;
    	try {
    		// 数据写入文件
            String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/" + type + ".json";
            fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
            //写文件头
            fileWriter.write("[");

    		solrClient = SolrUtil.getClient(EngineUtil.DWB_MASTER_INFO, slave);

    		List<String> conditionList = new ArrayList<String>();
        	String where = "%sRULE_TYPE:%s AND BATCH_ID:%s";
        	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("MEDICAL_UNREASONABLE_DRUG_ACTION", "VISITID", "VISITID");
        	where  = String.format(where, plugin.parse(), type, batch.getBatchId());
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
	        //不合规行为字典映射
	        Map<String, MedicalActionDict> actionDictMap = dictSV.queryActionDict();
	        //医院等级报销比例
	        List<StdHoslevelFundpayprop> fundpayRatioList = apiEngineSV.findStdHoslevelFundpayprop(task.getDataSource());
		    if(total[0]>0) {
		    	this.writeSyncUnreasonableActionJson(fileWriter, task, batch, visitidList, masterMapList, type, actionDictMap, fundpayRatioList, slave);
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
					this.writeSyncUnreasonableActionJson(fileWriter, task, batch, visitidList, masterMapList, type, actionDictMap, fundpayRatioList, slave);
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

	@Override
    public void syncUnreasonableAction(TaskProject task, TaskProjectBatch batch, String itemcode, String type, boolean slave) throws Exception {
    	BufferedWriter fileWriter = null;
    	SolrClient solrClient = null;
    	try {
    		// 数据写入文件
            String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/" + itemcode + ".json";
            fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
            //写文件头
            fileWriter.write("[");

    		solrClient = SolrUtil.getClient(EngineUtil.DWB_MASTER_INFO, slave);

    		List<String> conditionList = new ArrayList<String>();
        	String where = "_query_:\"%sRULE_TYPE:%s AND ITEMCODE:%s AND BATCH_ID:%s\"";
        	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("MEDICAL_UNREASONABLE_DRUG_ACTION", "VISITID", "VISITID");
        	where  = String.format(where, plugin.parse(), type, itemcode, batch.getBatchId());
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
		    //不合规行为字典映射
	        Map<String, MedicalActionDict> actionDictMap = dictSV.queryActionDict();
	        //医院等级报销比例
	        List<StdHoslevelFundpayprop> fundpayRatioList = apiEngineSV.findStdHoslevelFundpayprop(task.getDataSource());
		    if(total[0]>0) {
		    	this.writeSyncUnreasonableActionJson(fileWriter, task, batch, itemcode, visitidList, masterMapList, type, actionDictMap, fundpayRatioList, slave);
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
					this.writeSyncUnreasonableActionJson(fileWriter, task, batch, itemcode, visitidList, masterMapList, type, actionDictMap, fundpayRatioList, slave);
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

	/**
	 *
	 * 功能描述：按限定范围拆分结果
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月16日 上午11:41:02</p>
	 *
	 * @param fileWriter
	 * @param task
	 * @param batch
	 * @param visitidList
	 * @param masterMapList
	 * @param type
	 * @param slave
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
    private void writeSyncUnreasonableActionJson(BufferedWriter fileWriter, TaskProject task, TaskProjectBatch batch,
			List<String> visitidList, Map<String, Map<String, Object>> masterMapList, String type, 
			Map<String, MedicalActionDict> actionDictMap, List<StdHoslevelFundpayprop> fundpayRatioList, boolean slave) throws Exception {
    	List<String> conditionList = new ArrayList<String>();
    	conditionList.add("VISITID:(\""+StringUtils.join(visitidList, "\",\"")+"\")");
    	conditionList.add("RULE_TYPE:"+type);
    	conditionList.add("BATCH_ID:"+batch.getBatchId());
    	
    	String dictKey = "LIMIT_SCOPE_TO_ACTION";
        if("1".equals(type) || MedicalConstant.ENGINE_BUSI_TYPE_DRUG.equals(type)) {
        	//药品合规
        	dictKey = "LIMIT_SCOPE_TO_DRUG_ACTION";
        } else if("2".equals(type) || MedicalConstant.ENGINE_BUSI_TYPE_CHARGE.equals(type)) {
        	//收费合规
        	dictKey = "LIMIT_SCOPE_TO_CHARGE_ACTION";
        } else if("4".equals(type) || MedicalConstant.ENGINE_BUSI_TYPE_TREAT.equals(type)) {
        	//诊疗合规
        	dictKey = "LIMIT_SCOPE_TO_TREAT_ACTION";
        }
        final String dict = dictKey;

		SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, slave, (doc, index) -> {
		    String visitid = doc.get("VISITID").toString();
		    if(masterMapList.containsKey(visitid)) {
		    	Collection<Object> limitScopeCodes = doc.getFieldValues("RULE_SCOPE");
		    	for(Object scopeCode : limitScopeCodes) {
		    		Map<String, Object> masterMap = masterMapList.get(visitid);
			    	JSONObject json = new JSONObject();
			        json.put("id", UUIDGenerator.generate());

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
	                }
			        json.put("GEN_DATA_TIME", DateUtils.now());
			        json.put("PROJECT_ID", task.getProjectId());
			        json.put("PROJECT_NAME", task.getProjectName());
			        json.put("BATCH_ID", batch.getBatchId());
			        json.put("TASK_BATCH_NAME", batch.getBatchName());
			        for(Map.Entry<String, String> entry : DURG_ACTION_MAPPING.entrySet()){
			            json.put(entry.getKey(), doc.get(entry.getValue()));
			        }
			        EngineLimitScopeEnum scopeEnum = EngineLimitScopeEnum.enumValueOf(scopeCode.toString());
			        //超频次特殊处理计算违规金额=项目可纳入报销费用*医院级别报销比例
			        this.settingActionMoney(masterMap, fundpayRatioList, json, doc, scopeEnum);
			        
			        json.put("FIR_REVIEW_STATUS", "init");			        
			        //不合规行为名称改成优先读字典表
	                //String desc = dictSV.queryDictTextByKey(dictKey, scopeCode.toString());
			        String desc = null;
			        String code = scopeCode.toString();
			        code = dict.concat("-").concat(code);
			        if(SCOPE_MAPPING.containsKey(code)) {
			        	code = SCOPE_MAPPING.get(code);			        	
			        }
			        if(actionDictMap.containsKey(code)) {
			        	MedicalActionDict actionDict = actionDictMap.get(code);
			        	desc = actionDict.getActionName();
			        }
	                if(StringUtils.isBlank(desc)) {
						desc = "违反限定".concat(scopeCode.toString());
	                }
	                json.put("ACTION_ID", code);
	                json.put("ACTION_NAME", desc);
			        try {
			            fileWriter.write(json.toJSONString());
			            fileWriter.write(',');
			        } catch (IOException e) {
			        }
		    	}
		    }
		});
    }

    private void writeSyncUnreasonableActionJson(BufferedWriter fileWriter, TaskProject task, TaskProjectBatch batch, String itemcode,
			List<String> visitidList, Map<String, Map<String, Object>> masterMapList, String type, 
			Map<String, MedicalActionDict> actionDictMap, List<StdHoslevelFundpayprop> fundpayRatioList, boolean slave) throws Exception {
    	List<String> conditionList = new ArrayList<String>();
    	conditionList.add("VISITID:(\""+StringUtils.join(visitidList, "\",\"")+"\")");
    	conditionList.add("RULE_TYPE:"+type);
    	conditionList.add("ITEMCODE:"+itemcode);
    	conditionList.add("BATCH_ID:"+batch.getBatchId());
    	
    	String dictKey = "LIMIT_SCOPE_TO_ACTION";
        if("1".equals(type) || MedicalConstant.ENGINE_BUSI_TYPE_DRUG.equals(type)) {
        	//药品合规
        	dictKey = "LIMIT_SCOPE_TO_DRUG_ACTION";
        } else if("2".equals(type) || MedicalConstant.ENGINE_BUSI_TYPE_CHARGE.equals(type)) {
        	//收费合规
        	dictKey = "LIMIT_SCOPE_TO_CHARGE_ACTION";
        } else if("4".equals(type) || MedicalConstant.ENGINE_BUSI_TYPE_TREAT.equals(type)) {
        	//诊疗合规
        	dictKey = "LIMIT_SCOPE_TO_TREAT_ACTION";
        }
        final String dict = dictKey;

    	SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, slave, (doc, index) -> {
		    String visitid = doc.get("VISITID").toString();
		    if(masterMapList.containsKey(visitid)) {
		    	Collection<Object> limitScopeCodes = doc.getFieldValues("RULE_SCOPE");
		    	for(Object scopeCode : limitScopeCodes) {
		    		Map<String, Object> masterMap = masterMapList.get(visitid);
			    	JSONObject json = new JSONObject();
			        if("1".equals(type)) {
			        	//药品合规
			        	json.put("BUSI_TYPE", MedicalConstant.ENGINE_BUSI_TYPE_DRUG);
			        } else if("2".equals(type)) {
			        	//收费合规
			        	json.put("BUSI_TYPE", MedicalConstant.ENGINE_BUSI_TYPE_CHARGE);
			        } else if("4".equals(type)) {
	                	//诊疗合规
	                	json.put("BUSI_TYPE", MedicalConstant.ENGINE_BUSI_TYPE_TREAT);
	                }
			        EngineLimitScopeEnum scopeEnum = EngineLimitScopeEnum.enumValueOf(scopeCode.toString());
			        //不合规行为名称改成优先读字典表
	                //String desc = dictSV.queryDictTextByKey(dictKey, scopeCode.toString());
			        String desc = null;
			        String code = scopeCode.toString();
			        code = dict.concat("-").concat(code);
			        if(SCOPE_MAPPING.containsKey(code)) {
			        	code = SCOPE_MAPPING.get(code);			        	
			        }
			        if(actionDictMap.containsKey(code)) {
			        	MedicalActionDict actionDict = actionDictMap.get(code);
			        	desc = actionDict.getActionName();
			        }
	                if(StringUtils.isBlank(desc)) {
						desc = "违反限定".concat(scopeCode.toString());
	                }
			    	//String id = MD5Util.MD5Encode(batch.getBatchId().concat("_").concat(itemcode).concat("_").concat(desc).concat("_").concat(visitid), "UTF-8");
			        //json.put("id", UUIDGenerator.generate());
	                //id生成策略
			    	String template = "${batchId}_${itemCode}_${actionName}_${visitid}";
			        Properties properties = new Properties();
			        properties.put("batchId", batch.getBatchId());
			        properties.put("itemCode", itemcode);
			        properties.put("actionName", desc);
			        properties.put("visitid", visitid);
			        template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
			        if(doc.get("MUTEX_ITEM_CODE")!=null
			        		&& StringUtils.isNotBlank(doc.get("MUTEX_ITEM_CODE").toString())) {
			        	String mutex_item_code = doc.get("MUTEX_ITEM_CODE").toString();
			        	mutex_item_code = StringUtils.replace(mutex_item_code, "[", "");
			        	mutex_item_code = StringUtils.replace(mutex_item_code, "]", "");
			        	template = template.concat("_").concat(mutex_item_code);
			        }
			        String id = MD5Util.MD5Encode(template, "UTF-8");
			    	json.put("id", id);

			    	for(Map.Entry<String, String> entry : EngineCaseServiceImpl.CASE_FIELD_MAPPING.entrySet()) {
			            Object val = masterMap.get(entry.getValue());
			            if (val != null) {
			                json.put(entry.getKey(), val);
			            }
			        }
			        			        
			        json.put("GEN_DATA_TIME", DateUtils.now());
			        json.put("PROJECT_ID", task.getProjectId());
			        json.put("PROJECT_NAME", task.getProjectName());
			        json.put("BATCH_ID", batch.getBatchId());
			        json.put("TASK_BATCH_NAME", batch.getBatchName());
			        for(Map.Entry<String, String> entry : DURG_ACTION_MAPPING.entrySet()){
			            json.put(entry.getKey(), doc.get(entry.getValue()));
			        }
			        json.put("RULE_SCOPE", scopeCode.toString());
			        if(scopeEnum!=null) {
			        	json.put("RULE_SCOPE_NAME", scopeEnum.getName());
			        } else {
			        	json.put("RULE_SCOPE_NAME", desc);
			        }
			        //超频次特殊处理计算违规金额=项目可纳入报销费用*医院级别报销比例
			        this.settingActionMoney(masterMap, fundpayRatioList, json, doc, scopeEnum);
			        
			        json.put("FIR_REVIEW_STATUS", "init");
			        json.put("ACTION_ID", code);
	                json.put("ACTION_NAME", desc);
	                
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
		    }
		});
    }

    @Override
    public void syncTrailAction(String ruleId, String type) throws Exception {
    	List<String> conditionList = new ArrayList<String>();
    	String where = "%sRULE_TYPE:%s AND BATCH_ID:%s";
    	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("MEDICAL_TRAIL_DRUG_ACTION", "VISITID", "VISITID");
    	where = String.format(where, plugin.parse(), type, ruleId);
    	conditionList.add(where);
    	List<String> visitidList = new ArrayList<String>();
    	Map<String, Map<String, Object>> masterMapList = new HashMap<String, Map<String, Object>>();
    	int count = SolrUtil.exportByPager(conditionList, EngineUtil.DWB_MASTER_INFO, (map, index) -> {
    		String visitid = map.get("VISITID").toString();
    		masterMapList.put(visitid, map);
    		visitidList.add(visitid);
        });
    	if(count>0) {
    		int pageSize = 1000;
    		int pageNum = (visitidList.size() + pageSize - 1) / pageSize;
    		//数据分割
    		List<List<String>> mglist = new ArrayList<>();
    	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
    	    	mglist.add(visitidList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
    	    });

    	    // 数据写入文件
            String importFilePath = SolrUtil.importFolder + "/MEDICAL_TRAIL_ACTION/" + ruleId + "/" + type + ".json";
            BufferedWriter fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));

            //写文件头
            fileWriter.write("[");
            //不合规行为字典映射
	        Map<String, MedicalActionDict> actionDictMap = dictSV.queryActionDict();
    	    for(List<String> list : mglist) {
    	    	this.writeSyncTrailActionJson(fileWriter, ruleId, list, masterMapList, type, actionDictMap);
    	    }

    	    // 文件尾
            fileWriter.write("]");
            fileWriter.flush();
            fileWriter.close();
            //导入solr
            SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_TRAIL_ACTION);
    	}
    }

    private void writeSyncTrailActionJson(BufferedWriter fileWriter, String ruleId,
			List<String> visitidList, Map<String, Map<String, Object>> masterMapList, String type, Map<String, MedicalActionDict> actionDictMap) throws Exception {
    	List<String> conditionList = new ArrayList<String>();
    	conditionList.add("VISITID:(\""+StringUtils.join(visitidList, "\",\"")+"\")");
    	conditionList.add("RULE_TYPE:"+type);
    	conditionList.add("BATCH_ID:"+ruleId);
    	
    	String dictKey = "LIMIT_SCOPE_TO_ACTION";
        if("1".equals(type) || MedicalConstant.ENGINE_BUSI_TYPE_DRUG.equals(type)) {
        	//药品合规
        	dictKey = "LIMIT_SCOPE_TO_DRUG_ACTION";
        } else if("2".equals(type) || MedicalConstant.ENGINE_BUSI_TYPE_CHARGE.equals(type)) {
        	//收费合规
        	dictKey = "LIMIT_SCOPE_TO_CHARGE_ACTION";
        } else if("4".equals(type) || MedicalConstant.ENGINE_BUSI_TYPE_TREAT.equals(type)) {
        	//诊疗合规
        	dictKey = "LIMIT_SCOPE_TO_TREAT_ACTION";
        }
        final String dict = dictKey;

    	SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_TRAIL_DRUG_ACTION, (doc, index) -> {
            String visitid = doc.get("VISITID").toString();
            if(masterMapList.containsKey(visitid)) {
            	Collection<Object> limitScopes = doc.getFieldValues("RULE_SCOPE");
		    	for(Object scopeCode : limitScopes) {
		    		Map<String, Object> masterMap = masterMapList.get(visitid);
	            	JSONObject json = new JSONObject();
	                json.put("id", UUIDGenerator.generate());

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
	                }
	                json.put("GEN_DATA_TIME", DateUtils.now());
	                json.put("BATCH_ID", ruleId);
	                for(Map.Entry<String, String> entry : DURG_ACTION_MAPPING.entrySet()){
	                    json.put(entry.getKey(), doc.get(entry.getValue()));
	                }
	                json.put("FIR_REVIEW_STATUS", "init");	                
	                //不合规行为名称改成优先读字典表
	                //String desc = dictSV.queryDictTextByKey(dictKey, scopeCode.toString());
	                String desc = null;
	                String code = scopeCode.toString();
			        code = dict.concat("-").concat(code);
			        if(SCOPE_MAPPING.containsKey(code)) {
			        	code = SCOPE_MAPPING.get(code);			        	
			        }
			        if(actionDictMap.containsKey(code)) {
			        	MedicalActionDict actionDict = actionDictMap.get(code);
			        	desc = actionDict.getActionName();
			        }
	                if(StringUtils.isBlank(desc)) {
						desc = "违反限定".concat(scopeCode.toString());
	                }
	                json.put("ACTION_ID", code);
	                json.put("ACTION_NAME", desc);
	                
	                try {
	                    fileWriter.write(json.toJSONString());
	                    fileWriter.write(',');
	                } catch (IOException e) {
	                }
		    	}
            }
        });
    }
    
    /**
     * 
     * 功能描述：超频次特殊处理计算违规金额=项目可纳入报销费用*医院级别报销比例
     *
     * @author  zhangly
     * <p>创建日期 ：2020年10月28日 下午1:39:13</p>
     *
     * @param masterMap:DWB_MASTER_INFO数据
     * @param task:批次任务
     * @param json:new JSONObject()
     * @param doc:MEDICAL_UNREASONABLE_DRUG_ACTION数据
     * @param scopeEnum:限定范围
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    private void settingActionMoney(Map<String, Object> masterMap, List<StdHoslevelFundpayprop> fundpayRatioList, JSONObject json, SolrDocument doc, EngineLimitScopeEnum scopeEnum) {    	
    	if(scopeEnum!=null && EngineLimitScopeEnum.CODE_38.equals(scopeEnum)) {
    		String hosplevel = null;
    		//由医院级别改成物价级别获取报销比例
			/*if(masterMap.get("HOSPLEVEL")!=null) {
				hosplevel = masterMap.get("HOSPLEVEL").toString();
			}*/
    		if(masterMap.get("PRICE_LEVEL")!=null && !"null".equals(masterMap.get("PRICE_LEVEL").toString())) {
				hosplevel = masterMap.get("PRICE_LEVEL").toString();				
			}
    		boolean find = false;//是否配置了医院级别报销比例
    		BigDecimal rate = BigDecimal.ZERO;
            if(hosplevel!=null) {
            	String visitdate = masterMap.get("VISITDATE").toString();
                String day = DateUtils.dateformat(visitdate, "yyyy-MM-dd");
                for(StdHoslevelFundpayprop bean : fundpayRatioList) {
                	if(hosplevel.equals(bean.getHosplevelName()) 
                			|| hosplevel.equals(bean.getHosplevel())) {
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
    		} else {
				json.put("ACTION_MONEY", null);
    	        json.put("MAX_ACTION_MONEY", null);
			}
    		Collection<Object> array = doc.getFieldValues("ARRAY_MONEY");
    		if(array!=null && array.size()>0) {
    			//取违规金额中的最小值
    			BigDecimal min = null;
    			for(Object obj : array) {
    				BigDecimal value = new BigDecimal(String.valueOf(obj));
    				if(min==null) {
    					min = value;
    				} else if(min.compareTo(value)>0) {
    					min = value;
    				}
    			}
    			json.put("MIN_MONEY", min);
    	        json.put("MAX_MONEY", min);
    		}
    	}
    }

	@Override
	public EngineCntResult importApprovalAction(String batchId, String busiType, List<String> fqs) throws Exception {
		EngineCntResult result = EngineCntResult.ok();
		List<String> conditionList = new ArrayList<String>();
		conditionList.add("BATCH_ID:" + batchId);
		conditionList.add("BUSI_TYPE:" + busiType);
		conditionList.add("FIR_REVIEW_STATUS:(white OR blank OR grey)");
		if(fqs!=null && fqs.size()>0) {
			conditionList.addAll(fqs);
		}
		
		// 数据写入文件
		String day = DateUtils.getDate("yyyyMMdd");
		String time = DateUtils.getDate("HHmmss");
        String importFilePath = SolrUtil.importFolder + "/" + EngineUtil.MEDICAL_UNREASONABLE_ACTION + "/approval/" + day + "/" + batchId + "_" + busiType.toLowerCase() + "_" + time + ".json";
        result.setMessage(importFilePath);
        File file = new File(importFilePath);
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(file), Charset.forName("utf8")));
		try {
			//记录数
			long count = SolrUtil.count(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, false);
			result.setCount(count);
			if(count==0) {
				fileWriter.close();
				if(file.exists()) {
		        	file.delete();
		        }
				return result;
			}	       
	        //写文件头
	        fileWriter.write("[");
	        SolrUtil.exportDoc(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, false, (doc, index) -> {
	            // 循环每条数据写入文件
	        	JSONObject json = new JSONObject();
	        	json.put("id", doc.get("id"));
	        	json.put("FIR_REVIEW_STATUS", SolrUtil.initActionValue(doc.get("FIR_REVIEW_STATUS") == null ? "" : doc.get("FIR_REVIEW_STATUS"), "set"));	        	
	        	//回填审核状态字段
	        	Set<String> fieldSet = new HashSet<String>();
	        	fieldSet.add("FIR_REVIEW_REMARK");
	        	fieldSet.add("FIR_REVIEW_USERID");
	        	fieldSet.add("FIR_REVIEW_USERNAME");
	        	fieldSet.add("FIR_REVIEW_TIME");
	        	fieldSet.add("PUSH_STATUS");
	        	fieldSet.add("PUSH_USERID");
	        	fieldSet.add("PUSH_USERNAME");
	        	fieldSet.add("SEC_PUSH_STATUS");
	        	fieldSet.add("SEC_PUSH_USERID");
	        	fieldSet.add("SEC_PUSH_USERNAME");
	        	fieldSet.add("ISSUE_ID");
	        	fieldSet.add("ISSUE_NAME");
	        	fieldSet.add("XMKH_ID");
	        	fieldSet.add("XMKH_NAME");
	        	fieldSet.add("TASK_BATCH_NAME");
	        	fieldSet.add("HANDLE_STATUS");
	        	for(String key : fieldSet) {
	        		if(doc.get(key)!=null) {
	        			json.put(key, SolrUtil.initActionValue(doc.get(key), "set"));
	        		}
	        	}
	        	try {
	                fileWriter.write(json.toJSONString());
	                fileWriter.write(',');
	            } catch (Exception e) {

	            }
	        });
	        // 文件尾
	        fileWriter.write("]");
	        fileWriter.flush();
		} catch(Exception e) {
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		} finally {
			try {
				if(fileWriter!=null) {
					fileWriter.close();
				}
			} catch(Exception e) {}
		}
		return result;
	}
	
	@Override
	public EngineCntResult importApprovalAction(String batchId, String busiType) throws Exception {
		List<String> list = null;
		return importApprovalAction(batchId, busiType, list);
	}
	
	@Override
	public EngineCntResult importApprovalAction(String batchId, String busiType, String caseId) throws Exception {
		EngineCntResult result = EngineCntResult.ok();
		List<String> conditionList = new ArrayList<String>();
		conditionList.add("BATCH_ID:" + batchId);
		conditionList.add("BUSI_TYPE:" + busiType);
		conditionList.add("CASE_ID:" + caseId);
		conditionList.add("FIR_REVIEW_STATUS:(white OR blank OR grey)");
		
		// 数据写入文件
		String day = DateUtils.getDate("yyyyMMdd");
		String time = DateUtils.getDate("HHmmss");
		String fileName = batchId + "_" + busiType.toLowerCase() + "_" + caseId + "_" + time + ".json";
        String importFilePath = SolrUtil.importFolder + "/" + EngineUtil.MEDICAL_UNREASONABLE_ACTION + "/approval/" + day + "/" + fileName;
        result.setMessage(importFilePath);
        File file = new File(importFilePath);
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(file), Charset.forName("utf8")));
		try {
			//记录数
			long count = SolrUtil.count(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, false);
			result.setCount(count);
			if(count==0) {
				fileWriter.close();
				if(file.exists()) {
		        	file.delete();
		        }
				return result;
			}	       
	        //写文件头
	        fileWriter.write("[");
	        SolrUtil.exportDoc(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, false, (doc, index) -> {
	            // 循环每条数据导出审核状态字段
	        	this.writeApproveAction(fileWriter, doc);
	        });
	        // 文件尾
	        fileWriter.write("]");
	        fileWriter.flush();
		} catch(Exception e) {
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		} finally {
			try {
				if(fileWriter!=null) {
					fileWriter.close();
				}
			} catch(Exception e) {}
		}
		return result;
	}
	
	@Override
	public EngineCntResult importApprovalActionFromRule(String batchId, String busiType, String ruleId) throws Exception {
		EngineCntResult result = EngineCntResult.ok();
		List<String> conditionList = new ArrayList<String>();
		conditionList.add("BATCH_ID:" + batchId);
		conditionList.add("BUSI_TYPE:" + busiType);
		conditionList.add("RULE_ID:" + ruleId);
		conditionList.add("FIR_REVIEW_STATUS:(white OR blank OR grey)");
		
		// 数据写入文件
		String day = DateUtils.getDate("yyyyMMdd");
		String time = DateUtils.getDate("HHmmss");
		String fileName = batchId + "_" + busiType.toLowerCase() + "_" + ruleId + "_" + time + ".json";
        String importFilePath = SolrUtil.importFolder + "/" + EngineUtil.MEDICAL_UNREASONABLE_ACTION + "/approval/" + day + "/" + fileName;
        result.setMessage(importFilePath);
        File file = new File(importFilePath);
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(file), Charset.forName("utf8")));
		try {
			//记录数
			long count = SolrUtil.count(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, false);
			result.setCount(count);
			if(count==0) {
				fileWriter.close();
				if(file.exists()) {
		        	file.delete();
		        }
				return result;
			}	       
	        //写文件头
	        fileWriter.write("[");
	        SolrUtil.exportDoc(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, false, (doc, index) -> {
	            // 循环每条数据导出审核状态字段
	        	this.writeApproveAction(fileWriter, doc);
	        });
	        // 文件尾
	        fileWriter.write("]");
	        fileWriter.flush();
		} catch(Exception e) {
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		} finally {
			try {
				if(fileWriter!=null) {
					fileWriter.close();
				}
			} catch(Exception e) {}
		}
		return result;
	}
	
	private void writeApproveAction(BufferedWriter fileWriter, SolrDocument doc) {
		JSONObject json = new JSONObject();
    	json.put("id", doc.get("id"));
    	json.put("FIR_REVIEW_STATUS", SolrUtil.initActionValue(doc.get("FIR_REVIEW_STATUS") == null ? "" : doc.get("FIR_REVIEW_STATUS"), "set"));	        	
    	//回填审核状态字段
    	Set<String> fieldSet = new HashSet<String>();
    	fieldSet.add("FIR_REVIEW_REMARK");
    	fieldSet.add("FIR_REVIEW_USERID");
    	fieldSet.add("FIR_REVIEW_USERNAME");
    	fieldSet.add("FIR_REVIEW_TIME");
    	fieldSet.add("PUSH_STATUS");
    	fieldSet.add("PUSH_USERID");
    	fieldSet.add("PUSH_USERNAME");
    	fieldSet.add("SEC_PUSH_STATUS");
    	fieldSet.add("SEC_PUSH_USERID");
    	fieldSet.add("SEC_PUSH_USERNAME");
    	fieldSet.add("ISSUE_ID");
    	fieldSet.add("ISSUE_NAME");
    	fieldSet.add("XMKH_ID");
    	fieldSet.add("XMKH_NAME");
    	fieldSet.add("TASK_BATCH_NAME");
    	fieldSet.add("HANDLE_STATUS");
    	for(String key : fieldSet) {
    		if(doc.get(key)!=null) {
    			json.put(key, SolrUtil.initActionValue(doc.get(key), "set"));
    		}
    	}
    	try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (Exception e) {

        }
	}
	
	@Override
	public ProjectFilterWhereVO filterCondition(TaskProject project, boolean isMaster) throws Exception {
		ProjectFilterWhereVO result = new ProjectFilterWhereVO();
		List<TaskCommonConditionSet> list = taskService.queryTaskCommonConditionSet(project.getProjectId());
		if(list==null || list.size()==0) {
			return result;
		}
		result.setWhereList(list);
		boolean join = !isMaster;
		Set<String> set = new HashSet<String>();
		StringBuilder sb = new StringBuilder();
		for(TaskCommonConditionSet record : list) {
			sb.setLength(0);
			String condiType = record.getField();
			result.addType(condiType);
			String value = record.getExt1();
			if("visittype".equals(condiType)) {
				//就诊类型
				if(join) {
					SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "VISITID");
					sb.append("_query_:\"");
					sb.append(plugin.parse());
				}				
				sb.append(EngineUtil.parseMultParam("VISITTYPE_ID", value, ",", true));
				if(join) {
					sb.append("\"");
				}
			} else if("payway".equals(condiType)) {
				//支付方式
				if(join) {
					SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "VISITID");
					sb.append("_query_:\"");
					sb.append(plugin.parse());
				}
				sb.append(EngineUtil.parseMultParam("PAYWAY_ID", value, ",", false));
				if(join) {
					sb.append("\"");
				}
			} else if("funSettleway".equals(condiType)) {
				//结算方式
				if(join) {
					SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(EngineUtil.DWB_MASTER_INFO, "VISITID", "VISITID");
					sb.append("_query_:\"");
					sb.append(plugin.parse());
				}
				sb.append(EngineUtil.parseMultParam("FUN_SETTLEWAY_ID", value, ",", false));
				if(join) {
					sb.append("\"");
				}
			} else if("diseaseDiag".equals(condiType)) {
				//疾病诊断
				value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_DIAG", "VISITID", "VISITID");
				sb.append("_query_:\"");
				sb.append(plugin.parse());
				sb.append("DISEASECODE:").append(value);
				sb.append("\"");
			} else if("diseaseMappingFilter".equals(condiType)) {
				result.setDiseaseFilter(true);
			}
			if(sb.length()>0) {
				set.add(sb.toString());
			}
		}
		String condition = set.size()>0 ? "*:* -(" + StringUtils.join(set, " OR ") + ")" : null;
		result.setCondition(condition);
		return result;
	}

	@Override
	public void executeGroupBy(String batchId, String actionId, String[] fqs) {
		ThreadUtils.setTokenDef();
		try {
			taskActionFieldConfigService.addGroupByTask(null, new String[] {batchId}, actionId, fqs, false);
		} catch (Exception e) {
			log.error("", e);
		}
		try {
			taskActionFieldConfigService.addBreakStateTemplTask(null, actionId, new String[] {batchId}, fqs, false);
		} catch(Exception e) {
			log.error("", e);
		}
		ThreadUtils.removeToken();
	}
	
	@Override
	public void executeGroupBy(String batchId, String caseId, String busiType) {
		SolrQuery query = new SolrQuery();
		// 设定查询字段
		String q = "*:*";
		query.add("q", q);
		query.addFilterQuery("BATCH_ID:"+batchId);
		query.addFilterQuery("CASE_ID:"+caseId);
		query.addFilterQuery("BUSI_TYPE:"+busiType);
		query.setRows(0);
		JSONObject facetJsonMap = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", "terms");
		jsonObject.put("limit", -1);
		jsonObject.put("field", "ACTION_ID");		
		facetJsonMap.put("categories", jsonObject);
		//String facetJson = "{categories:{type:terms,limit:20,field:JGMC00,facet:{total:\"unique(TBRID0)\"}}}}";
		String facetJson = JSON.toJSONString(facetJsonMap);
		log.info("======json.facet:"+facetJson);
		query.set("json.facet", facetJson);
		query.setFacet(true);
		query.setFacetLimit(-1);
		QueryResponse response;
		try {
			ThreadUtils.setTokenDef();
			String solrCollection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
			//logger.info("======solr query: " + URLDecoder.decode(query.toQueryString(), "UTF-8"));
			response = SolrUtil.call(query, solrCollection);
			//logger.info("======solr result:" + response.toString());
			NestableJsonFacet nestableJsonFacet = response.getJsonFacetingResponse();
			BucketBasedJsonFacet bucketBasedJsonFacet = nestableJsonFacet.getBucketBasedFacets("categories");
			if(bucketBasedJsonFacet!=null) {
				List<BucketJsonFacet> bucketJsonFacetList = bucketBasedJsonFacet.getBuckets();
				for(int i=0; i<bucketJsonFacetList.size(); i++) {
					BucketJsonFacet bucket = bucketJsonFacetList.get(i);
	        		if(bucket != null) {
	        			String actionId = String.valueOf(bucket.getVal());
	        			String[] fqs = new String[] {"CASE_ID:"+caseId, "BUSI_TYPE:"+busiType};
	        			taskActionFieldConfigService.addGroupByTask(null, new String[] {batchId}, actionId, fqs, false);
	        		}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			ThreadUtils.removeToken();
		}
	}
}
