/**
 * AbsCaseSecondHandle.java	  V1.0   2021年11月24日 下午2:39:04
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.secondary.cases;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineOutputRule;
import com.ai.modules.engine.model.vo.OutputComputeVO;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.alibaba.fastjson.JSONObject;

public abstract class AbsCaseSecondHandle {
	protected HisMedicalFormalCase formalCase;
	protected List<EngineNodeRule> ruleList;
	
	public AbsCaseSecondHandle(HisMedicalFormalCase formalCase, List<EngineNodeRule> ruleList) {
		this.formalCase = formalCase;
		this.ruleList = ruleList;
	}
	
	public abstract void execute() throws Exception;
	
	protected abstract boolean filter(EngineNodeRule rule);
	
	/**
	 * 
	 * 功能描述：满足条件的数据回写solr
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年11月30日 上午9:36:48</p>
	 *
	 * @param computeMap
	 * @param field
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected void update(Map<String, OutputComputeVO> computeMap, String field) throws Exception {
		String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
		// 满足条件的数据写入文件
		String importFilePath = SolrUtil.importFolder + "/" + collection + "/" + formalCase.getBatchId() + "/"
				+ formalCase.getCaseId() + ".json";
		BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(
				FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
		// 写文件头
		fileWriter.write("[");
		for (Map.Entry<String, OutputComputeVO> entry : computeMap.entrySet()) {
			OutputComputeVO computeVO = entry.getValue();
			List<Set<String>> setList = computeVO.getOutputSetList();
			List<String> outputList = new ArrayList<String>();
			for(Set<String> set : setList) {
				outputList.add(StringUtils.join(set, ","));
			}
			for (String id : computeVO.getIds()) {
				JSONObject json = new JSONObject();
				json.put("id", id);
				JSONObject up = new JSONObject();
				up.put("set", StringUtils.join(outputList, "和"));
				json.put(field, up);
				fileWriter.write(json.toJSONString());
	            fileWriter.write(',');
			}
		}
		// 文件尾
		fileWriter.write("]");
		fileWriter.flush();
		fileWriter.close();
		// 导入solr
		SolrUtil.importJsonToSolr(importFilePath, collection);
	}
	
	protected List<List<EngineOutputRule>> parseEngineNodeRule() {
		List<EngineNodeRule> whereList = new ArrayList<EngineNodeRule>();
		for(EngineNodeRule rule : ruleList) {
			if(this.filter(rule)) {
				whereList.add(rule);
			}
		}
		if(whereList.size()>0) {
			List<List<EngineOutputRule>> ruleGrpList = new ArrayList<List<EngineOutputRule>>();
			//规则按节点分组
	        Map<String, List<EngineNodeRule>> nodeRuleMap = whereList.stream().collect(Collectors.groupingBy(EngineNodeRule::getNodeCode));
	        for(Map.Entry<String, List<EngineNodeRule>> nodeEntry : nodeRuleMap.entrySet()) {
	        	Map<String, EngineOutputRule> outputRuleMap = new LinkedHashMap<String, EngineOutputRule>();
	        	List<EngineNodeRule> ruleList = nodeEntry.getValue();
	        	for(EngineNodeRule rule : ruleList) {
	        		String key = rule.getTableName().concat(".").concat(rule.getColName()).concat(rule.getCompareType());
	        		if(outputRuleMap.containsKey(key)) {
	        			EngineOutputRule outputRule = outputRuleMap.get(key);
	        			outputRule.addCompareValue(rule.getCompareValue());
	        		} else {
	        			EngineOutputRule outputRule = new EngineOutputRule(rule.getTableName(), rule.getColName(), rule.getCompareType(), rule.getCompareValue());
	        			outputRuleMap.put(key, outputRule);
	        		}
	        	}
	        	List<EngineOutputRule> outputRuleList = new ArrayList<EngineOutputRule>();
	        	for(Map.Entry<String, EngineOutputRule> entry : outputRuleMap.entrySet()) {
	        		outputRuleList.add(entry.getValue());
	        	}
	        	ruleGrpList.add(outputRuleList);
	        }
	        return ruleGrpList;
		}
		return null;
	}
	
	protected String parseWhere(EngineOutputRule rule, String replace) {
		String colName = rule.getColName();
		if(StringUtils.isNotBlank(replace)) {
			colName = replace;
		}
		String compareType = rule.getCompareType();
		Set<String> value = rule.getCompareValueSet();				
		StringBuilder sb = new StringBuilder();		
		if("=".equals(compareType)) {
			sb.append(colName).append(":(").append(StringUtils.join(value, " OR ")).append(")");
		} else if (compareType.equalsIgnoreCase("like")) {
			// 包含
			sb.append(colName).append(":(*").append(StringUtils.join(value, "* OR *")).append("*)");
		} else if (compareType.equalsIgnoreCase("llike")) {
			// 以..开始
			sb.append(colName).append(":(*").append(StringUtils.join(value, " OR *")).append(")");
		} else if (compareType.equalsIgnoreCase("rlike")) {
			// 以..结尾
			sb.append(colName).append(":(").append(StringUtils.join(value, "* OR ")).append("*)");
		} else {
			sb.append(colName).append(":(").append(StringUtils.join(value, " OR ")).append(")");
		}
		return sb.toString();
	}

	public HisMedicalFormalCase getFormalCase() {
		return formalCase;
	}

	public void setFormalCase(HisMedicalFormalCase formalCase) {
		this.formalCase = formalCase;
	}

	public List<EngineNodeRule> getRuleList() {
		return ruleList;
	}

	public void setRuleList(List<EngineNodeRule> ruleList) {
		this.ruleList = ruleList;
	}
}
