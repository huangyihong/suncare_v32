/**
 * DrugSecondLineHandle.java	  V1.0   2020年11月4日 下午4:09:05
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.solr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.MD5Util;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.common.MedicalConstant;
import com.ai.modules.api.util.ApiOauthClientUtil;
import com.ai.modules.engine.handle.rule.AbsSolrRuleHandle;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.handle.rule.parse.RuleIndicationParser;
import com.ai.modules.engine.handle.secondary.AbsRuleSecondHandle;
import com.ai.modules.engine.handle.secondary.RuleChronicHandle;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.dto.ActionTypeDTO;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.impl.EngineActionServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 功能描述：solr方式计算用药不合规行为
 *
 * @author  zhangly
 * Date: 2020年11月4日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Slf4j
public class SolrDrugUseHandle extends AbsSolrRuleHandle {
	
	private MedicalDruguse rule;
	//规则条件
	private List<MedicalRuleConditionSet> ruleConditionList;

	public SolrDrugUseHandle(TaskProject task, TaskProjectBatch batch, 
			MedicalDruguse rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch);
		this.rule = rule;
		this.ruleConditionList = ruleConditionList;
	}

	@Override
	public void generateUnreasonableAction() throws Exception {
		String itemcode = rule.getItemCodes();
		if(itemcode.indexOf(",")>-1) {
			itemcode = "(" + StringUtils.replace(itemcode, ",", " OR ") + ")";
		}
		List<String> conditionList = new ArrayList<String>();        
        conditionList.add("ITEMCODE:" + itemcode);
        conditionList.add("ITEM_QTY:{0 TO *}");
		conditionList.add("ITEM_AMT:{0 TO *}");
		//项目过滤条件
        ApplicationContext context = SpringContextUtils.getApplicationContext();
        IEngineActionService engineActionService = context.getBean(IEngineActionService.class);
        ProjectFilterWhereVO filterVO = engineActionService.filterCondition(task, false);
        if(StringUtils.isNotBlank(filterVO.getCondition())) {
        	conditionList.add(filterVO.getCondition());
        }
        if(filterVO.isDiseaseFilter() && EngineUtil.existsDisease(ruleConditionList)) {
        	//疾病映射不全过滤
        	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_DIAG", "VISITID", "VISITID");
        	conditionList.add("*:* -"+plugin.parse()+"-DISEASENAME:?*");
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
        String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
		String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
		batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
		batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
		StringBuilder sb = new StringBuilder();
		//项目数据时间范围限制
		sb.append("VISITDATE:");
		sb.append("[").append(project_startTime).append(" TO ").append(project_endTime).append("]");
        conditionList.add(sb.toString());
		//批次数据时间范围限制
		sb.setLength(0);
		sb.append("VISITDATE:");
		sb.append("[").append(batch_startTime).append(" TO ").append(batch_endTime).append("]");
        conditionList.add(sb.toString());
        if(StringUtils.isNotBlank(task.getDataOrgFilter())) {
        	//医疗机构范围限制
        	String value = task.getDataOrgFilter();
        	value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
        	conditionList.add("ORGID:"+value);
        }
        if(StringUtils.isNotBlank(batch.getCustomFilter())) {
        	//自定义数据范围限制
        	//conditionList.add(batch.getCustomFilter());
        	String value = batch.getCustomFilter();
        	value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
        	conditionList.add("ORGID:"+value);
        }
        //基金支出金额>0
  		conditionList.add("FUND_COVER:{0 TO *}");
  		//自付比例<1
  		conditionList.add("SELFPAY_PROP_MIN:[0 TO 1}");
        //添加准入与限定条件
        conditionList.addAll(this.parseCondition());
        //添加过滤掉指标为空值的条件
		/*RuleIgnoreNullParser ignoreNullParser = new RuleIgnoreNullParser(this.getMedicalRuleConfig(), ruleConditionList);
		conditionList.addAll(ignoreNullParser.ignoreNullWhere());*/
        conditionList.add(this.ignoreNullWhere());
        
        // 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/" + EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION + "/" + batch.getBatchId() + "/" + rule.getRuleId() + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");
        ActionTypeDTO dto = this.getActionTypeDTO();
        int count = SolrUtil.exportByPager(conditionList, EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, (map, index) -> {
            // 循环每条数据写入文件
            writerJson(fileWriter, map, dto);
        });
        // 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();

        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION);
        
        if(count>0) {
        	//二次处理排除慢性病种病人
        	MedicalRuleConfig config = new MedicalRuleConfig();
        	config.setRuleId(rule.getRuleId());
        	config.setItemCodes(rule.getItemCodes());
        	config.setItemNames(rule.getItemNames());
        	config.setItemTypes(rule.getItemTypes());
        	config.setRuleBasis(rule.getRuleBasis());
        	config.setRuleBasisType(rule.getRuleBasisType());
        	config.setRuleCode(rule.getRuleCode());
        	config.setActionId(rule.getActionId());
        	config.setActionName(rule.getActionName());
        	config.setActionType(rule.getActionType());
			AbsRuleSecondHandle handle = new RuleChronicHandle(task, batch, config, ruleConditionList, trail);
			handle.execute();
        }
        
        //同步数据
        MedicalRuleConfig config = new MedicalRuleConfig();
        config.setRuleId(rule.getRuleId());
        config.setActionId(rule.getActionId());
        config.setActionName(rule.getActionName());
        config.setActionType(rule.getActionType());
        config.setItemCodes(rule.getItemCodes());
        config.setItemNames(rule.getItemNames());
        syncUnreasonableAction(config, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, false);
        //不合规行为汇总
        String[] fqs = new String[] {"RULE_ID:"+rule.getRuleId()};
        engineActionService.executeGroupBy(batch.getBatchId(), rule.getActionId(), fqs);
	}
	
	/**
	 * 
	 * 功能描述：准入条件、判断条件分组
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年2月1日 上午10:56:52</p>
	 *
	 * @param type
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private List<List<MedicalRuleConditionSet>> parseCondition(String type) {
		List<MedicalRuleConditionSet> accessList = null;
		if(StringUtils.isNotBlank(type)) {
			ruleConditionList.stream().filter(s->type.equals(s.getType())).collect(Collectors.toList());
		} else {
			accessList = ruleConditionList;
		}
		if(accessList!=null && accessList.size()>0) {
			List<List<MedicalRuleConditionSet>> result = new ArrayList<List<MedicalRuleConditionSet>>();		
			//条件按组号分组
	        Map<Integer, List<MedicalRuleConditionSet>> groupRuleMap = accessList.stream().collect(Collectors.groupingBy(bean-> bean.getGroupNo()==null? 0 : bean.getGroupNo()));
	        //组号排序
	        groupRuleMap = groupRuleMap.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
	        for (Map.Entry<Integer, List<MedicalRuleConditionSet>> entry : groupRuleMap.entrySet()) {
                List<MedicalRuleConditionSet> tempList = entry.getValue();
                for(MedicalRuleConditionSet condition : tempList) {
                	if(StringUtils.isBlank(condition.getLogic())) {
                		//组内默认and关联
                		condition.setLogic("AND");
                	}
                }
                //按组内规则排序
                tempList = tempList.stream().sorted(Comparator.comparing(MedicalRuleConditionSet::getOrderNo)).collect(Collectors.toList());                
                tempList.get(0).setLogic(null);
                result.add(tempList);
            }
	        return result;
		}
		return null;
	}
	
	/**
	 * 
	 * 功能描述：不合规查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月9日 下午5:49:29</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private List<String> parseCondition() {
		List<String> result = new ArrayList<String>();
		 //准入条件
        StringBuilder sb = new StringBuilder();		
        if(StringUtils.isNotBlank(rule.getSex()) || StringUtils.isNotBlank(rule.getAge())) {
        	sb.append("_query_:\"");
    		EngineMapping mapping = new EngineMapping("DWB_MASTER_INFO", "VISITID", "VISITID");
    		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
    		sb.append(plugin.parse());
    		sb.append("VISITID:*");
        }
        if(StringUtils.isNotBlank(rule.getSex())) {
			//性别
        	sb.append(" AND SEX_CODE:"+rule.getSex());
		}
		if(StringUtils.isNotBlank(rule.getAge())) {
			//年龄
			String ageUnit = rule.getAgeUnit();
			String field = "YEARAGE";
			if("day".equals(ageUnit) || "日".equals(ageUnit)) {
				field = "DAYAGE";
			} else if("month".equals(ageUnit) || "月".equals(ageUnit)) {
				field = "MONTHAGE";
			}
			String age = rule.getAge();
			age = StringUtils.replace(age, ",)", ",*)");
			age = StringUtils.replace(age, ",]", ",*]");
			age = StringUtils.replace(age, "[,", "[*,");
			age = StringUtils.replace(age, "(,", "(*,");
			age = StringUtils.replace(age, "(", "{");
			age = StringUtils.replace(age, ")", "}");
			age = StringUtils.replace(age, ",", " TO ");
			sb.append(" AND ").append(field).append(":").append(age);
		}
		if(sb.length()>0) {
			sb.append("\"");
			result.add(sb.toString());
		}
	    if(ruleConditionList!=null) {
	    	sb.setLength(0);
	    	//限定条件        	
	    	//List<List<MedicalRuleConditionSet>> judgeGrpList = this.parseCondition("judge");
	    	List<List<MedicalRuleConditionSet>> judgeGrpList = this.parseCondition("");
	    	if(judgeGrpList!=null) {
	    		List<String> wheres = new ArrayList<String>();
	    		for(List<MedicalRuleConditionSet> judgeList : judgeGrpList) {
	    			sb.setLength(0);
	    			int size = judgeList.size();
	    			if(size>1) {
	    				sb.append("(");
	    			}
	    			for(MedicalRuleConditionSet bean : judgeList) {
	    				if(StringUtils.isNotBlank(bean.getLogic())) {
	    					sb.append(" ").append(bean.getLogic()).append(" ");
	    				}
	    				sb.append(this.parseCondition(bean));
	    			}
	    			if(size>1) {
	    				sb.append(")");
	    			}
	    			wheres.add(sb.toString());
	    		}
	    		//组与组之间默认or关系
	    		String condition = StringUtils.join(wheres, " OR ");
	    		//黑名单取反
	    		condition = "*:* -(" + condition + ")";
	    		result.add(condition);
	    	}
	    }	            
	    return result;
    }
	
	private String parseCondition(MedicalRuleConditionSet bean) {
		MedicalRuleConfig config = this.getMedicalRuleConfig();
		AbsRuleParser parser = new RuleIndicationParser(config, bean);
		return parser.parseCondition();		
	}
	
	/**
	 * 
	 * 功能描述：忽略空值条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月9日 下午5:50:10</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private String ignoreNullWhere() {
		StringBuilder sb = new StringBuilder();
		if(ruleConditionList!=null) {
			boolean exists = false; //是否存在疾病组条件
    		for(MedicalRuleConditionSet record : ruleConditionList) {
    			if(StringUtils.isNotBlank(record.getExt2())) {
    				exists = true;
    				break;
    			}
    		}
    		if(exists) {
    			sb.append("_query_:\"");
      			EngineMapping mapping = new EngineMapping("DWB_MASTER_INFO", "VISITID", "VISITID");
        		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
        		sb.append(plugin.parse());
      			sb.append("DISEASECODE:?*");
      			sb.append("\"");
    		}
		}
		
		return sb.toString();
	}
	
	private JSONObject writerJson(BufferedWriter fileWriter, Map<String, Object> map, ActionTypeDTO dto) {
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
        json.put("RULE_TYPE", MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE);
        json.put("RULE_SCOPE_NAME", rule.getActionName());
        json.put("ACTION_TYPE_ID", dto.getActionTypeId());
        json.put("ACTION_TYPE_NAME", dto.getActionTypeName());
        json.put("ACTION_ID", rule.getActionId());
        json.put("ACTION_NAME", rule.getActionName());
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
	
	private MedicalRuleConfig getMedicalRuleConfig() {
		MedicalRuleConfig config = new MedicalRuleConfig();
		config.setRuleId(rule.getRuleId());
		config.setItemCodes(rule.getItemCodes());
		config.setItemNames(rule.getItemNames());
		config.setRuleBasis(rule.getRuleBasis());
		config.setMessage(rule.getMessage());
		return config;
	}
	
	private ActionTypeDTO getActionTypeDTO() {
    	String busiType = MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE;
    	String desc = ApiOauthClientUtil.parseText("ACTION_TYPE", rule.getActionType());
    	ActionTypeDTO dto = new ActionTypeDTO();
        dto.setActionTypeId(busiType);
        dto.setActionTypeName(desc);
        return dto;
    }
}
