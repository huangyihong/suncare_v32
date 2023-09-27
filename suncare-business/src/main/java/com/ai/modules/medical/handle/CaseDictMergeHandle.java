/**
 * CaseDictMergeHandle.java	  V1.0   2021年7月6日 下午2:52:57
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.medical.handle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;

import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.entity.MedicalFormalFlowRule;
import com.ai.modules.formal.service.IMedicalFormalCaseService;
import com.ai.modules.formal.service.IMedicalFormalFlowRuleService;
import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.probe.entity.MedicalProbeFlowRule;
import com.ai.modules.probe.service.IMedicalProbeFlowRuleService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

public class CaseDictMergeHandle extends AbsCaseDictMergeHandle {
	protected String tableName;
	protected String colName;

	public CaseDictMergeHandle(String tableName, String colName) {
		this.tableName = tableName;
		this.colName = colName;
	}

	public CaseDictMergeHandle(String colName) {
		this.colName = colName;
	}

	@Override
	public List<DictMergeVO> merge(String main, String repeat) throws Exception {
		IMedicalProbeFlowRuleService service = this.getBean(IMedicalProbeFlowRuleService.class);
		//模型探查库
		MedicalProbeFlowRule rule = new MedicalProbeFlowRule();
		rule.setCompareValue(main);
		QueryWrapper<MedicalProbeFlowRule> wrapper = new QueryWrapper<MedicalProbeFlowRule>();
		if(StringUtils.isNotBlank(tableName)) {
			wrapper.eq("table_name", tableName);
		}
		wrapper.eq("col_name", colName).eq("compare_value", repeat);
		service.update(rule, wrapper);
		List<DictMergeVO> result = new ArrayList<DictMergeVO>();
		//模型正式库
		List<DictMergeVO> caseList = this.mergeFormalCase(main, repeat);
		if(caseList!=null) {
			result.addAll(caseList);
		}
		//模板节点
		AbsCaseDictMergeHandle handle = new CaseTmplDictMergeHandle(tableName, colName);
		List<DictMergeVO> tmplList = handle.merge(main, repeat);
		if(tmplList!=null) {
			result.addAll(tmplList);
		}
		return result;
	}

	/**
	 *
	 * 功能描述：模型正式库
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年7月14日 下午4:40:39</p>
	 *
	 * @param main
	 * @param repeat
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private List<DictMergeVO> mergeFormalCase(String main, String repeat) throws Exception {
		IMedicalFormalFlowRuleService service = this.getBean(IMedicalFormalFlowRuleService.class);
		IMedicalFormalCaseService caseSV = this.getBean(IMedicalFormalCaseService.class);
		QueryWrapper<MedicalFormalFlowRule> wrapper = new QueryWrapper<MedicalFormalFlowRule>();
		StringBuilder sb = new StringBuilder();
		sb.append("select 1 from medical_formal_flow_rule r where MEDICAL_FORMAL_FLOW_RULE.CASE_ID=r.CASE_ID");
		sb.append(" and col_name='").append(colName).append("'");
		sb.append(" and "+DbDataEncryptUtil.decryptFunc("compare_value")+"='").append(repeat).append("'");
		if(StringUtils.isNotBlank(tableName)) {
			sb.append(" and table_name='").append(tableName).append("'");
		}
		wrapper.exists(sb.toString());
		List<MedicalFormalFlowRule> ruleList = service.list(wrapper);
		//需要更新的规则
		List<MedicalFormalFlowRule> updateList = new ArrayList<MedicalFormalFlowRule>();
		//需要删除的规则
		List<String> removeList = new ArrayList<String>();
		//需要前台确认的模型
		Set<String> caseSet = new HashSet<String>();
		//按模型分组
		Map<String, List<MedicalFormalFlowRule>> caseRuleMap = ruleList.stream().collect(Collectors.groupingBy(MedicalFormalFlowRule::getCaseId));
		for(Map.Entry<String, List<MedicalFormalFlowRule>> caseRule : caseRuleMap.entrySet()) {
			List<MedicalFormalFlowRule> caseRuleList = caseRule.getValue();
			//规则按节点分组
	        Map<String, List<MedicalFormalFlowRule>> nodeRuleMap = caseRuleList.stream().collect(Collectors.groupingBy(MedicalFormalFlowRule::getNodeCode));
	        for(Map.Entry<String, List<MedicalFormalFlowRule>> entry : nodeRuleMap.entrySet()) {
	        	List<MedicalFormalFlowRule> dataList = entry.getValue();
	        	if(dataList.size()>0) {
	        		//按组号分组
	        		Map<String, List<MedicalFormalFlowRule>> grpRuleMap = dataList.stream().collect(Collectors.groupingBy(MedicalFormalFlowRule::getGroupNo));
	                if (grpRuleMap != null) {
	                	List<MedicalFormalFlowRule> upList = new ArrayList<MedicalFormalFlowRule>();
	                	List<String> rmList = new ArrayList<String>();
	                	boolean need = false; //是否复杂的规则组，需要前台确认
	                	for(Map.Entry<String, List<MedicalFormalFlowRule>> group : grpRuleMap.entrySet()) {
	                		dataList = group.getValue();
	                		//过滤出需要替换的规则
	                		dataList = dataList.stream().filter(s->colName.equals(s.getColName())).collect(Collectors.toList());
	        	        	if(StringUtils.isNotBlank(tableName)) {
	        	        		dataList = dataList.stream().filter(s->tableName.equals(s.getTableName())).collect(Collectors.toList());
	        	        	}
	        	        	dataList = dataList.stream().filter(s->main.equals(s.getCompareValue()) || repeat.equals(s.getCompareValue())).collect(Collectors.toList());
	                		dataList.sort(Comparator.comparing(MedicalFormalFlowRule::getOrderNo));
	                		if(dataList.size()==0) {
	                			continue;
	                		}
	                		if(dataList.size()==1) {
	                			//仅有一条
	                			MedicalFormalFlowRule rule = dataList.get(0);
	                			if(repeat.equals(rule.getCompareValue())) {
	                				rule.setCompareValue(main);
	                				upList.add(rule);
	                			}
	                		} else {
	                			if(this.equalLogic(group.getValue())) {
	                				//组内所有条件逻辑相同
	                				MedicalFormalFlowRule rule = dataList.get(0);
	                				//第一次就是需要替换的规则
            						rule.setCompareValue(main);
	                				updateList.add(rule);
	                				for(int i=1, len=dataList.size(); i<len; i++) {
	                					rmList.add(dataList.get(i).getRuleId());
	                				}
	                			} else {
	                				//组内所有条件逻辑不同
	                				caseSet.add(caseRule.getKey());
	                				need = true;
	                			}
	                		}
	                	}
	                	if(!need) {
	                		updateList.addAll(upList);
		                	removeList.addAll(rmList);
	                	}
	                }
	        	}
	        }
		}
		if(updateList.size()>0) {
			service.updateBatchById(updateList);
		}
		if(removeList.size()>0) {
			service.removeByIds(removeList);
		}
		List<DictMergeVO> result = null;
		if(caseSet.size()>0) {
			result = new ArrayList<DictMergeVO>();
			for(String caseId : caseSet) {
				DictMergeVO vo = new DictMergeVO();
				vo.setItemid(caseId);
				MedicalFormalCase formalCase = caseSV.getOne(new QueryWrapper<MedicalFormalCase>().eq("CASE_ID", caseId));
				vo.setItemname(formalCase.getCaseName());
				vo.setItemtype("case");
				result.add(vo);
			}
		}
		return result;
	}

	private boolean equalLogic(List<MedicalFormalFlowRule> ruleList) {
		Set<String> set = new HashSet<String>();
		ruleList.sort(Comparator.comparing(MedicalFormalFlowRule::getOrderNo));
		int index = 0;
		for(MedicalFormalFlowRule rule : ruleList) {
			if(index>0 && StringUtils.isNotBlank(rule.getLogic())) {
				set.add(rule.getLogic());
			}
			index++;
		}
		return set.size()==1;
	}
}
