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
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.MD5Util;

import com.ai.common.MedicalConstant;
import com.ai.modules.api.util.ApiOauthClientUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.handle.rule.parse.RuleIgnoreNullParser;
import com.ai.modules.engine.model.rule.EngineParamGrpRule;
import com.ai.modules.engine.model.vo.ChargedetailVO;
import com.ai.modules.engine.model.vo.RelyComputeVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.service.impl.EngineActionServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：一日依赖项目组规则二次处理
 *
 * @author zhangly Date: 2020年12月4日 Copyright (c) 2020 AILK
 *
 *         <p>
 *         修改历史：(修改人，修改时间，修改原因/内容)
 *         </p>
 */
public class RuleOnedayRelyHandle extends AbsRuleSecondHandle {
	
	public RuleOnedayRelyHandle(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList,
			Boolean trail) {
		super(task, batch, rule, ruleConditionList, trail);
	}

	@Override
	public void execute() throws Exception {
		boolean exists = false;// 是否是一日依赖项目组规则
		MedicalRuleConditionSet rely = null;
		for (MedicalRuleConditionSet bean : ruleConditionList) {
			if ("fitTimeRange".equals(bean.getField())) {
				if ("1day".equals(bean.getExt1())) {
					exists = true;
				}
			} else if ("fitGroups".equals(bean.getField())) {
				rely = bean;
			}
		}
		if (!exists || rely == null) {
			return;
		}
		String batchId = batch.getBatchId();				
		String[] array = StringUtils.split(rule.getItemCodes(), ",");
		for (String itemCode : array) {			
			List<String> visitidList = new ArrayList<String>();
			Map<String, RelyComputeVO> computeMap = new HashMap<String, RelyComputeVO>();			
			boolean slave = false;
			List<String> conditionList = new ArrayList<String>();        
	        conditionList.add("ITEMCODE:" + itemCode);
	        conditionList.add("ITEM_QTY:{0 TO *}");
			conditionList.add("ITEM_AMT:{0 TO *}");
			conditionList.addAll(this.parseCommonCondition());
			//添加准入与限定条件
			conditionList.add(this.parseJudgeCondition());
	        //添加过滤掉指标为空值的条件
			RuleIgnoreNullParser ignoreNullParser = new RuleIgnoreNullParser(rule, ruleConditionList);
	  		conditionList.addAll(ignoreNullParser.ignoreNullWhere());
			int count = SolrUtil.exportDocByPager(conditionList, EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, slave, (doc, index) -> {
				String visitid = doc.get("VISITID").toString();
				visitidList.add(visitid);
				String id = doc.get("id").toString();
				String itemcode = doc.get("ITEMCODE").toString();
				if (!computeMap.containsKey(visitid)) {
					RelyComputeVO vo = new RelyComputeVO(id, visitid, itemcode, doc);
					computeMap.put(visitid, vo);
				}
			});
			if (count > 0) {				
		        // 一日依赖项目组或药品组				
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
				if(StringUtils.isNotBlank(rely.getExt1())) {
					String relyTreatCodes = rely.getExt1();					
					relyTreatCodes = StringUtils.replace(relyTreatCodes, ",", "|");
					sb.append(" OR ");
					sb.append("_query_:\"");
					SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
					sb.append(plugin.parse());
					sb.append("TREATGROUP_CODE:");
					String values = "(" + StringUtils.replace(relyTreatCodes, "|", " OR ") + ")";
					sb.append(values);
					sb.append("\"");
				}
				if(StringUtils.isNotBlank(rely.getExt3())) {
					String relyDrugCodes = rely.getExt3();
					relyDrugCodes = StringUtils.replace(relyDrugCodes, ",", "|");
					sb.append(" OR ");
					sb.append("_query_:\"");
					SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_DRUGGROUP", "DRUGCODE", "ITEMCODE");
					sb.append(plugin.parse());
					sb.append("DRUGGROUP_CODE:");
					String values = "(" + StringUtils.replace(relyDrugCodes, "|", " OR ") + ")";
					sb.append(values);
					sb.append("\"");
				}
				conditionList.add(sb.toString());

				int pageSize = 500;
				int pageNum = (visitidList.size() + pageSize - 1) / pageSize;
				// 数据分割
				List<List<String>> mglist = new ArrayList<>();
				Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
					mglist.add(visitidList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
				});

				String collection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
				// 追加非一日依赖的就诊ID
				// 数据写入文件
		        String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batchId + "/" + rule.getRuleId() + ".json";
		        BufferedWriter fileWriter = new BufferedWriter(
		                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
		        //写文件头
		        fileWriter.write("[");
				for (List<String> subList : mglist) {
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
					solrQuery.addField("CLIENTID");
					solrQuery.addField("CHARGEDATE");
					solrQuery.addField("PRESCRIPTTIME");
					solrQuery.addField("AMOUNT");
					solrQuery.addField("FEE");
					solrQuery.addField("FUND_COVER");
					solrQuery.setSort(SolrQuery.SortClause.asc("VISITID"));
					SolrUtil.export(solrQuery, EngineUtil.DWB_CHARGE_DETAIL, slave, (map, index) -> {
						// 处方日期PRESCRIPTTIME
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
						// 基金支出金额
						if (map.get("FUND_COVER") != null) {
							vo.setFundConver(new BigDecimal(map.get("FUND_COVER").toString()));
						}
						RelyComputeVO compute = computeMap.get(visitId);
						compute.add(vo);
					});
				}
				//不合规行为字典映射  		
		        MedicalActionDict actionDict = dictSV.queryActionDict(rule.getActionId());
				String actionTypeName = null;
		  		if(StringUtils.isNotBlank(rule.getActionType())) {        	
		  			actionTypeName = ApiOauthClientUtil.parseText("ACTION_TYPE", rule.getActionType());
		        }
				for (Map.Entry<String, RelyComputeVO> entry : computeMap.entrySet()) {
					RelyComputeVO vo = entry.getValue();
					if (!vo.computeRely()) {
						//非一日依赖
						writerJson(fileWriter, vo.getDocument(), actionTypeName, actionDict);
					}
				}
				// 文件尾
		        fileWriter.write("]");
		        fileWriter.flush();
		        fileWriter.close();
		        //导入solr
		        SolrUtil.importJsonToSolr(importFilePath, collection);
			}
		}
	}
	
	private JSONObject writerJson(BufferedWriter fileWriter, Map<String, Object> map, String actionTypeName, MedicalActionDict actionDict) {		
		JSONObject json = new JSONObject();
        String id = MD5Util.MD5Encode(batch.getBatchId().concat("_").concat(rule.getRuleId()).concat("_").concat((String) map.get("id")), "utf-8");
        json.put("id", id);
        for (String field : EngineActionServiceImpl.DURG_ACTION_FIELD) {
            Object val = map.get(field);
            if (val != null) {
                json.put(field, val);
            }
        }
                
        json.put("RULE_ID", rule.getRuleId());
        json.put("RULE_NAME", rule.getItemNames());
        json.put("RULE_DESC", rule.getMessage());
        String ruleFName = rule.getRuleId() + "::" + rule.getItemNames();
        json.put("RULE_FNAME", ruleFName);
        String ruleFDescs = rule.getRuleId() + "::" + rule.getMessage();
        json.put("RULE_FDESC", ruleFDescs);
        json.put("RULE_BASIS", rule.getRuleBasis());
        json.put("RULE_TYPE", this.getBusiType());
        json.put("RULE_SCOPE_NAME", rule.getActionName());
        json.put("ACTION_TYPE_ID", rule.getActionType());
        json.put("ACTION_TYPE_NAME", actionTypeName);        
        json.put("ACTION_ID", rule.getActionId());
        json.put("ACTION_NAME", rule.getActionName());
        if(actionDict!=null) {
        	json.put("ACTION_NAME", actionDict.getActionName());
        	json.put("RULE_LEVEL", actionDict.getRuleLevel());
        }
        json.put("ACTION_DESC", rule.getMessage());
        //基金支出金额
        json.put("MIN_ACTION_MONEY", map.get("FUND_COVER"));
        json.put("MAX_ACTION_MONEY", map.get("FUND_COVER"));
        //收费项目费用
        json.put("MIN_MONEY", map.get("ITEM_AMT"));
        json.put("MAX_MONEY", map.get("ITEM_AMT"));
        
        json.put("GEN_DATA_TIME", DateUtils.now());
        json.put("PROJECT_ID", task.getProjectId());
        json.put("PROJECT_NAME", task.getProjectName());
        json.put("BATCH_ID", batch.getBatchId());
        json.put("TASK_BATCH_NAME", batch.getBatchName());
        try {
            fileWriter.write(json.toJSONString());
            fileWriter.write(',');
        } catch (Exception e) {

        }
        return json;
    }
	
	private String getBusiType() {
		String busiType = MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE;
        switch(rule.getRuleType()) {
		case "DRUG":
			busiType = "NEWDRUG";
			break;
		case "CHARGE":
			busiType = MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE;
			break;
		case "TREAT":
			busiType = MedicalConstant.ENGINE_BUSI_TYPE_NEWTREAT;
			break;	
		default:
			busiType = rule.getRuleType();
			break;
        }
        return busiType;
	}
	
	private String parseJudgeCondition() {
		Set<String> exclude = new HashSet<String>();
    	exclude.add("fitTimeRange");
		List<MedicalRuleConditionSet> judgeList = ruleConditionList.stream().filter(s->"judge".equals(s.getType())).collect(Collectors.toList());
		if(exclude!=null && exclude.size()>0) {
			judgeList = judgeList.stream().filter(s->!exclude.contains(s.getField())).collect(Collectors.toList());
		}
    	if(judgeList!=null) {
    		List<String> wheres = new ArrayList<String>();
    		for(MedicalRuleConditionSet judge : judgeList) {
    			String where = this.parseCondition(judge);
    			if(StringUtils.isNotBlank(where)) {
    				wheres.add(where);
    			}
    		}
    		//组与组之间默认or关系
    		String condition = StringUtils.join(wheres, " OR ");
    		return condition;
    	}
    	return null;
	}

	private String parseCondition(MedicalRuleConditionSet bean) {
		List<String> wheres = new ArrayList<String>();
		if(StringUtils.isNotBlank(bean.getExt1())) {
			EngineParamGrpRule paramRule = new EngineParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", bean.getExt1());
			paramRule.setRelation("2");
			wheres.add(paramRule.where());
		}
		if(StringUtils.isNotBlank(bean.getExt3())) {
			EngineParamGrpRule paramRule = new EngineParamGrpRule("STD_DRUGGROUP", "DRUGGROUP_CODE", bean.getExt3());
			wheres.add(paramRule.where());
		}
		return StringUtils.join(wheres, " OR ");
	}
}
