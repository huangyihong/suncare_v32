/**
 * EngineTargetServiceImpl.java	  V1.0   2020年7月23日 下午3:46:50
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.MD5Util;
import org.springframework.stereotype.Service;

import com.ai.modules.engine.handle.fee.AbsFeeHandle;
import com.ai.modules.engine.handle.fee.FeeResult;
import com.ai.modules.engine.handle.fee.OverFrequencyFeeHandle;
import com.ai.modules.engine.model.EngineLimitScopeEnum;
import com.ai.modules.engine.model.rule.BaseEngineParamRule;
import com.ai.modules.engine.model.rule.EngineParamChargeGrpRule;
import com.ai.modules.engine.model.rule.EngineParamFrequencyGrpRule;
import com.ai.modules.engine.model.rule.EngineParamFrequencyRule;
import com.ai.modules.engine.model.rule.EngineParamGrpRule;
import com.ai.modules.engine.model.rule.EngineParamIndicationRule;
import com.ai.modules.engine.model.rule.EngineParamOrgRule;
import com.ai.modules.engine.model.rule.EngineParamRule;
import com.ai.modules.engine.model.rule.EngineParamTreatResultRule;
import com.ai.modules.engine.model.rule.EngineParamUsageRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.service.IEngineTargetService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineTargetServiceImpl implements IEngineTargetService {
	
    /**
     * 
     * 功能描述：计算违规指标
     *
     * @author  zhangly
     * <p>创建日期 ：2020年7月22日 下午5:37:37</p>
     *
     * @param batchId
     * @param rule
     *
     * <p>修改历史 ：2020-09-25 修改成按一个药品一个规则来跑</p>
     */
	@Override
    public void calculateBreakActionTarget(String collection, boolean slave, String batchId, MedicalDrugRule rule, Set<String> ignoreSet, boolean trail) throws Exception {
    	if(StringUtils.isBlank(rule.getLimitScope())) {
    		return;
    	}
    	if(ignoreSet!=null && ignoreSet.contains(rule.getRuleId())) {
    		return;
    	}
		/*if("1".equals(rule.getRuleType())) {
			log.info("start 计算违规限定类型 {}:{}", rule.getRuleId(), rule.getDrugNames());
		} else if("2".equals(rule.getRuleType())) {
			rule.setDrugCode(rule.getChargeItemCodes());
			log.info("start 计算违规限定类型 {}:{}", rule.getRuleId(), rule.getChargeItems());
		}*/
    	log.info("start 计算违规限定类型 {}:{}", rule.getRuleId(), rule.getDrugCode());
    	String itemCode = rule.getDrugCode();
		/*if(itemCode.indexOf(",")>-1) {
			itemCode = "(" + StringUtils.replace(itemCode, ",", " OR ") + ")";
		}*/
    	//限制范围
  		String[] limitScope = rule.getLimitScope().split(",");
  		Map<String, EngineLimitScopeEnum> limitScopeEnumMap = new HashMap<String, EngineLimitScopeEnum>();
  		for(String scope : limitScope) {
  			EngineLimitScopeEnum limitScopeEnum = EngineLimitScopeEnum.enumValueOf(scope);
  			if(limitScopeEnum!=null) {
  				limitScopeEnumMap.put(scope, limitScopeEnum);
  			}
  		}
  		if(limitScopeEnumMap.size()==0) {
  			return;
  		}
  		
  		List<String> conditionList = new ArrayList<String>();
  		conditionList.add("BATCH_ID:"+batchId);
  		conditionList.add("RULE_ID:"+rule.getRuleId());
  		conditionList.add("ITEMCODE:"+rule.getDrugCode());
        
  		// 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + batchId + "/" + rule.getRuleId() + "/" + rule.getDrugCode() + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));        
  		if(trail && limitScopeEnumMap.size()==1 && !limitScopeEnumMap.containsKey("38")) {
  			//限制范围只有一个指标
  			EngineLimitScopeEnum limitScopeEnum = limitScopeEnumMap.entrySet().iterator().next().getValue();
  			//写文件头
  	        fileWriter.write("[");
  			SolrUtil.exportByPager(conditionList, collection, slave, (map, index) -> {
  				JSONObject json = new JSONObject();
    			json.put("id", map.get("id"));
    			JSONObject up = new JSONObject();
				up.put("set", limitScopeEnum.getCode());
				json.put("RULE_SCOPE", up);
				up = new JSONObject();
				up.put("set", limitScopeEnum.getName());
				json.put("RULE_SCOPE_NAME", up);
  				try {  									
					fileWriter.write(json.toJSONString());
					fileWriter.write(',');
				} catch (IOException e) {} 	            
  	        });
  			fileWriter.write("]");  	          	        
  		} else {
  			String ruleId = rule.getRuleId();
  			StringBuilder sb = new StringBuilder();
  			EngineLimitScopeEnum limitScopeEnum = EngineLimitScopeEnum.CODE_01;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())) {
  				//年龄
  				String ageUnit = rule.getAgeUnit();
  				String ageLow = null;
  		        if(rule.getAgeLow()!=null) {
  		        	ageLow = String.valueOf(rule.getAgeLow());
  		        }
  		        String ageLowCompare = rule.getAgeLowCompare();
  		        String ageHigh = null;
  		        if(rule.getAgeHigh()!=null) {
  		        	ageHigh = String.valueOf(rule.getAgeHigh());
  		        }
  		        String ageHighCompare = rule.getAgeHighCompare();
  				
  		        sb.append("*:* -(");
  				sb.append("_query_:\"");
  				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
  				sb.append(plugin.parse());
  				sb.append("VISITID:*");
  				String field = "YEARAGE";
  				if("day".equals(ageUnit)) {
  					field = "DAYAGE";
  				} else if("month".equals(ageUnit)) {
  					field = "MONTHAGE";
  				}
  				sb.append(" AND ").append(field).append(":");
  				String low = "{", high = "}";	
  				if("<=".equals(ageLowCompare)) {
  					low = "[";
  				}
  				if("<=".equals(ageHighCompare)) {
  					high = "]";
  				}
  				sb.append(low);
  				if(StringUtils.isNotBlank(ageLow)) {
  					if(!"-1".equals(ageLow)) {
  						sb.append(ageLow);
  					} else {
  						sb.append("*");
  					}
  				} else {
  					sb.append("*");
  				}
  				sb.append(" TO ");
  				if(StringUtils.isNotBlank(ageHigh)) {
  					if(!"-1".equals(ageHigh)) {
  						sb.append(ageHigh);
  					} else {
  						sb.append("*");
  					}
  				} else {
  					sb.append("*");
  				}
  				sb.append(high);
  				sb.append("\"");
  				sb.append(")");  				
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, sb.toString(), limitScopeEnum);
  			}
  			limitScopeEnum = EngineLimitScopeEnum.CODE_02;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())) {
  				//性别
  				sb.setLength(0);
  				sb.append("*:* -(");
  				sb.append("_query_:\"");
  				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
  				sb.append(plugin.parse());
  				sb.append("VISITID:*");
  				sb.append(" AND SEX_CODE:").append(rule.getSex());
  				sb.append("\"");
  				sb.append(")");
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, sb.toString(), limitScopeEnum);
  			}
  			limitScopeEnum = EngineLimitScopeEnum.CODE_03;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())) {
  				//就医方式
  				sb.setLength(0);
  				sb.append("*:* -(");
  				sb.append("_query_:\"");
  				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
  				sb.append(plugin.parse());
  				sb.append("VISITID:*");
  				sb.append(" AND ").append(EngineUtil.parseMultParam("VISITTYPE_ID", rule.getJzlx(), true));
  				sb.append("\"");
  				sb.append(")");
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, sb.toString(), limitScopeEnum);
  			}
  			limitScopeEnum = EngineLimitScopeEnum.CODE_04;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())) {
  				//参保类别
  				sb.setLength(0);
  				sb.append("*:* -(");
  				sb.append("_query_:\"");
  				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
  				sb.append(plugin.parse());
  				sb.append("VISITID:*");
  				sb.append(" AND ").append(EngineUtil.parseMultParam("INSURANCETYPE", rule.getYblx(), false));
  				sb.append("\"");
  				sb.append(")");
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, sb.toString(), limitScopeEnum);
  			}
  			limitScopeEnum = EngineLimitScopeEnum.CODE_05;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())) {
  				//医院级别
  				sb.setLength(0);
  				sb.append("*:* -(");
  				sb.append("_query_:\"");
  				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
  				sb.append(plugin.parse());
  				sb.append("VISITID:*");
  				sb.append(" AND ").append(EngineUtil.parseMultParam("HOSPLEVEL", rule.getYyjb(), false));
  				sb.append("\"");
  				sb.append(")");
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, sb.toString(), limitScopeEnum);
  			}
  			limitScopeEnum = EngineLimitScopeEnum.CODE_06;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())) {
  				//科室
  				sb.setLength(0);
  				sb.append("*:* -(");
  				sb.append("_query_:\"");
  				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
  				sb.append(plugin.parse());
  				sb.append("VISITID:*");
  				sb.append(" AND ").append(EngineUtil.parseMultParam("DEPTID", rule.getOffice(), false));
  				sb.append("\"");
  				sb.append(")");
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, sb.toString(), limitScopeEnum);
  			}
  			limitScopeEnum = EngineLimitScopeEnum.CODE_35;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())) {
  				//医疗机构
  				sb.setLength(0);
  				sb.append("*:* -(");
  				sb.append("_query_:\"");
  				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_MASTER_INFO", "VISITID", "VISITID");
  				sb.append(plugin.parse());
  				sb.append("VISITID:*");
  				sb.append(" AND ").append(EngineUtil.parseMultParam("ORGID", rule.getOrg(), false));
  				sb.append("\"");
  				sb.append(")");
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, sb.toString(), limitScopeEnum);
  			}  			  			
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_13;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getIndication())) {
  				//适用症
  				EngineParamIndicationRule paramRule = new EngineParamIndicationRule("DIAGGROUP_CODE", rule.getIndication());
  				paramRule.setReverse(true);
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_31;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getUnIndication())) {
  				//禁忌症
  				EngineParamIndicationRule paramRule = new EngineParamIndicationRule("DIAGGROUP_CODE", rule.getUnIndication());
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_12;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getTwoLimitDrug())) {
  				//二线用药
  	            EngineParamRule paramRule = new EngineParamRule(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, "ITEMCODE", rule.getTwoLimitDrug());
  	            paramRule.setPatient(true);
  	            paramRule.setReverse(true);
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_09;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getTreatProject())) {
  				//治疗项目
  	            EngineParamGrpRule paramRule = new EngineParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", rule.getTreatProject());
  	            paramRule.setPatient(true);
  	            paramRule.setReverse(true);
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_14;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getTreatDrug())) {
  				//治疗用药
  				EngineParamRule paramRule = new EngineParamRule(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, "ITEMCODE", rule.getTreatDrug());
  				paramRule.setPatient(true);
  				paramRule.setReverse(true);
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_25;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getHealthOrgKind())) {
  				//卫生机构类别
  				EngineParamOrgRule paramRule = new EngineParamOrgRule("ORGTYPE_CODE", rule.getHealthOrgKind());
  	            paramRule.setReverse(true);
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_24;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getTwoLimitDrug2())) {
  				//合用不予支付药品
  				EngineParamRule paramRule = new EngineParamRule(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, "ITEMCODE", rule.getTwoLimitDrug2());
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_32;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getUnExpense())) {
  				//不能报销
  				//EngineParamUnExpenseRule paramRule = new EngineParamUnExpenseRule();
  				BaseEngineParamRule paramRule = new BaseEngineParamRule("FUND_COVER", ">", "0");
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_33;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getDrugUsage())) {
  				//给药途径
  				EngineParamUsageRule paramRule = new EngineParamUsageRule(rule.getDrugUsage());
  				paramRule.setReverse(true);
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_34;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getUnfitGroupCodes())) {
  				//相互作用
  	            EngineParamRule paramRule = new EngineParamRule(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, "ITEMCODE", rule.getUnfitGroupCodes());
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			//以下是收费合规限定范围
  			limitScopeEnum = EngineLimitScopeEnum.CODE_27;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getFitGroupCodes())) {
  				//合规项目组
  				EngineParamChargeGrpRule paramRule = new EngineParamChargeGrpRule(rule.getDrugCode(), "STD_TREATGROUP", "TREATGROUP_CODE", rule.getFitGroupCodes());
  				paramRule.setReverse(true);
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_28;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getUnfitGroupCodes())) {
  				//互斥项目组
  				EngineParamChargeGrpRule paramRule = new EngineParamChargeGrpRule(rule.getDrugCode(), "STD_TREATGROUP", "TREATGROUP_CODE", rule.getUnfitGroupCodes());
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_29;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getUnfitGroupCodesDay())) {
  				//一日内互斥项目组
  				EngineParamChargeGrpRule paramRule = new EngineParamChargeGrpRule(rule.getDrugCode(), "STD_TREATGROUP", "TREATGROUP_CODE", rule.getUnfitGroupCodesDay());
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum, true);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_36;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getUnCharge())) {
  				//不能收费
  				BaseEngineParamRule paramRule = new BaseEngineParamRule("ITEM_AMT", ">", "0");
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_38;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())) {
  				//频次
  				if(!limitScopeEnumMap.containsKey(EngineLimitScopeEnum.CODE_39.getCode())) {
  					//不包含频次疾病组
  					EngineParamFrequencyRule paramRule = new EngineParamFrequencyRule(rule);
  	  				paramRule.setReverse(true);
  	  				//写文件
  	  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  				} else {
  					EngineParamFrequencyRule paramFrequencyRule = new EngineParamFrequencyRule(rule);
  					String condition = paramFrequencyRule.where();
  					//频率疾病组
  					StringBuilder where = new StringBuilder();
  					where.append("*:* -(");
  					EngineParamFrequencyGrpRule paramRule = new EngineParamFrequencyGrpRule("DIAGGROUP_CODE", rule.getDiseasegroupCodes());
  					if("=".equals(rule.getDiseasegroupFreq())) {
  		            	//等于
  		            	where.append("(");
  		            	paramRule.setReverse(true);
  		            	where.append(paramRule.where());
  		            	where.append(") OR (");
  		            	paramRule.setReverse(false);
  		            	where.append(paramRule.where());
  		            	where.append(" AND (").append(condition).append(")");
  		            	where.append(")");
  		            } else {
  		            	//不等于
  		            	where.append("(");
  		            	where.append(paramRule.where());
  		            	where.append(") OR (");
  		            	paramRule.setReverse(true);
  		            	where.append(paramRule.where());
  		            	where.append(" AND (").append(condition).append(")");
  		            	where.append(")");
  		            }
  		            where.append(")");
  		            //写文件
  	  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, where.toString(), limitScopeEnum);
  				}
  			}
  			
			/*limitScopeEnum = EngineLimitScopeEnum.CODE_39;
			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
					&& StringUtils.isNotBlank(rule.getDiseasegroupCodes())) {
				//频率疾病组
				EngineParamFrequencyGrpRule paramRule = new EngineParamFrequencyGrpRule("DIAGGROUP_CODE", rule.getDiseasegroupCodes());
			    if("=".equals(rule.getDiseasegroupFreq())) {
			    	paramRule.setReverse(true);
			    }
				//写文件
			    writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
			}*/
  			limitScopeEnum = EngineLimitScopeEnum.CODE_37;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())
  					&& StringUtils.isNotBlank(rule.getPayDurationPeriod())) {
  				//支付时长
  				sb.setLength(0);
  				sb.append("*:* -(");
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
  				sb.append("[0 TO ").append(rule.getPayDuration()).append("]");
  				sb.append(" AND ITEMCODE:").append(itemCode);
  				sb.append("\"");
  				sb.append(")");
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, sb.toString(), limitScopeEnum);
  			}
  			
  			limitScopeEnum = EngineLimitScopeEnum.CODE_40;
  			if(limitScopeEnumMap.containsKey(limitScopeEnum.getCode())) {
  				//检查结果
  				EngineParamTreatResultRule paramRule = new EngineParamTreatResultRule(rule.getTestResultValue(), rule.getTestResultItemType(), rule.getTestResultItemCode(),
  	        			rule.getTestResultValueType(), rule.getTestResultUnit());
  				paramRule.setReverse(true);
  				//写文件
  	  	        writerTargetJson(fileWriter, collection, slave, batchId, rule, paramRule.where(), limitScopeEnum);
  			}
  		}
  		fileWriter.flush();
  		fileWriter.close();
  		//导入solr
	    SolrUtil.importJsonToSolr(importFilePath, collection, slave);
	    log.info("end 计算违规限定类型");
    }
    
    /**
     * 
     * 功能描述：更新批次违规限制类型json写入文件
     *
     * @author  zhangly
     * <p>创建日期 ：2020年7月23日 下午3:05:22</p>
     *
     * @param fileWriter
     * @param collection
     * @param batchId
     * @param ruleId
     * @param condition
     * @param limitScopeEnum
     * @param create:是否新增
     * @throws Exception
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    private void writerTargetJson(BufferedWriter fileWriter, String collection, boolean slave,
    		String batchId, MedicalDrugRule rule, String condition, EngineLimitScopeEnum limitScopeEnum, boolean create) throws Exception {
    	//是否频次限定范围
    	boolean frequency = limitScopeEnum.equals(EngineLimitScopeEnum.CODE_38);
    	fileWriter.write("[");
    	List<String> conditionList = new ArrayList<String>();
  		conditionList.add("BATCH_ID:"+batchId);
  		conditionList.add("RULE_ID:"+rule.getRuleId());
  		conditionList.add("ITEMCODE:"+rule.getDrugCode());
  		conditionList.add(condition);
  		SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
			JSONObject json = new JSONObject();			
			if(create) {
				//新增
				for(Entry<String, Object> entry : doc.entrySet()) {
	    			if(!"_version_".equals(entry.getKey())) {
	    				json.put(entry.getKey(), entry.getValue());
	    			}
	    		}
				//id生成策略
		    	String template = "${batchId}_${itemCode}_${ruleScope}_${visitid}";
		        Properties properties = new Properties();
		        properties.put("batchId", batchId);
		        properties.put("itemCode", rule.getDrugCode());
		        properties.put("ruleScope", limitScopeEnum.getCode());
		        properties.put("visitid", doc.get("VISITID"));
		        template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
		        String id = MD5Util.MD5Encode(template, "UTF-8");
	    		json.put("id", id);
	    		json.put("RULE_SCOPE", limitScopeEnum.getCode());
				json.put("RULE_SCOPE_NAME", limitScopeEnum.getName());
				if(frequency) {
					//超频次计算违规金额
					AbsFeeHandle handle = new OverFrequencyFeeHandle(rule, doc);
					FeeResult feeResult = handle.compulate();
					if(feeResult!=null) {
						json.put("ARRAY_ACTION_MONEY", feeResult.getActionMoney());
						json.put("ARRAY_MONEY", feeResult.getMoney());
						json.put("AI_ITEM_CNT", feeResult.getCnt());
						json.put("AI_OUT_CNT", feeResult.getOutCnt());
					}
				}
			} else {
				//修改
				json.put("id", doc.get("id"));
				JSONObject up = new JSONObject();
				up.put("add", limitScopeEnum.getCode());
				json.put("RULE_SCOPE", up);
				up = new JSONObject();
				up.put("add", limitScopeEnum.getName());
				json.put("RULE_SCOPE_NAME", up);
				if(frequency) {
					//超频次计算违规金额
					AbsFeeHandle handle = new OverFrequencyFeeHandle(rule, doc);
					FeeResult feeResult = handle.compulate();
					if(feeResult!=null) {
						up = new JSONObject();
						up.put("add", feeResult.getActionMoney());
						json.put("ARRAY_ACTION_MONEY", up);
						up = new JSONObject();
						up.put("add", feeResult.getMoney());
						json.put("ARRAY_MONEY", up);
						up = new JSONObject();
						up.put("set", feeResult.getCnt());
						json.put("AI_ITEM_CNT", up);
						up = new JSONObject();
						up.put("set", feeResult.getOutCnt());
						json.put("AI_OUT_CNT", up);
					}
				}
			}
			try {  									
				fileWriter.write(json.toJSONString());
				fileWriter.write(',');
			} catch (IOException e) {} 	            
        });
  		fileWriter.write("]");
    }
    
    private void writerTargetJson(BufferedWriter fileWriter, String collection, boolean slave,
    		String batchId, MedicalDrugRule rule, String condition, EngineLimitScopeEnum limitScopeEnum) throws Exception {
    	writerTargetJson(fileWriter, collection, slave, batchId, rule, condition, limitScopeEnum, false);
    }    
}
