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

import com.ai.modules.medical.entity.vo.DictMergeVO;
import com.ai.modules.probe.entity.MedicalFlowTempl;
import com.ai.modules.probe.entity.MedicalFlowTemplRule;
import com.ai.modules.probe.service.IMedicalFlowTemplRuleService;
import com.ai.modules.probe.service.IMedicalFlowTemplService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

public class CaseTmplDictMergeHandle extends AbsCaseDictMergeHandle {
	protected String tableName;
	protected String colName;

	public CaseTmplDictMergeHandle(String tableName, String colName) {
		this.tableName = tableName;
		this.colName = colName;
	}

	public CaseTmplDictMergeHandle(String colName) {
		this.colName = colName;
	}

	@Override
	public List<DictMergeVO> merge(String main, String repeat) throws Exception {
		return this.mergeTmplCase(main, repeat);
	}

	/**
	 *
	 * 功能描述：模板节点
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年7月15日 下午3:43:00</p>
	 *
	 * @param main
	 * @param repeat
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private List<DictMergeVO> mergeTmplCase(String main, String repeat) throws Exception {
		IMedicalFlowTemplRuleService service = this.getBean(IMedicalFlowTemplRuleService.class);
		IMedicalFlowTemplService nodeSV = this.getBean(IMedicalFlowTemplService.class);
		QueryWrapper<MedicalFlowTemplRule> wrapper = new QueryWrapper<MedicalFlowTemplRule>();
		StringBuilder sb = new StringBuilder();
		sb.append("select NODE_ID from medical_flow_templ_rule where ");
		sb.append("col_name='").append(colName).append("'");
		sb.append(" and compare_value='").append(repeat).append("'");
		if(StringUtils.isNotBlank(tableName)) {
			sb.append(" and table_name='").append(tableName).append("'");
		}
		wrapper.inSql("NODE_ID", sb.toString());
		List<MedicalFlowTemplRule> ruleList = service.list(wrapper);
		//需要更新的规则
		List<MedicalFlowTemplRule> updateList = new ArrayList<MedicalFlowTemplRule>();
		//需要删除的规则
		List<String> removeList = new ArrayList<String>();
		//需要前台确认的模型
		Set<String> caseSet = new HashSet<String>();
		//按模型分组
		Map<String, List<MedicalFlowTemplRule>> caseRuleMap = ruleList.stream().collect(Collectors.groupingBy(MedicalFlowTemplRule::getNodeId));
		for(Map.Entry<String, List<MedicalFlowTemplRule>> caseRule : caseRuleMap.entrySet()) {
			List<MedicalFlowTemplRule> dataList = caseRule.getValue();
        	if(dataList.size()>0) {
        		//按组号分组
        		Map<String, List<MedicalFlowTemplRule>> grpRuleMap = dataList.stream().collect(Collectors.groupingBy(MedicalFlowTemplRule::getGroupNo));
                if (grpRuleMap != null) {
                	List<MedicalFlowTemplRule> upList = new ArrayList<MedicalFlowTemplRule>();
                	List<String> rmList = new ArrayList<String>();
                	boolean need = false; //是否复杂的规则组，需要前台确认
                	for(Map.Entry<String, List<MedicalFlowTemplRule>> group : grpRuleMap.entrySet()) {
                		dataList = group.getValue();
                		//过滤出需要替换的规则
                		dataList = dataList.stream().filter(s->colName.equals(s.getColName())).collect(Collectors.toList());
        	        	if(StringUtils.isNotBlank(tableName)) {
        	        		dataList = dataList.stream().filter(s->tableName.equals(s.getTableName())).collect(Collectors.toList());
        	        	}
        	        	dataList = dataList.stream().filter(s->main.equals(s.getCompareValue()) || repeat.equals(s.getCompareValue())).collect(Collectors.toList());
                		dataList.sort(Comparator.comparing(MedicalFlowTemplRule::getOrderNo));
                		if(dataList.size()==1) {
                			//仅有一条
                			MedicalFlowTemplRule rule = dataList.get(0);
                			if(repeat.equals(rule.getCompareValue())) {
                				rule.setCompareValue(main);
                				upList.add(rule);
                			}
                		} else {
                			if(this.equalLogic(group.getValue())) {
                				//组内所有条件逻辑相同
                				MedicalFlowTemplRule rule = dataList.get(0);
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
				MedicalFlowTempl node = nodeSV.getOne(new QueryWrapper<MedicalFlowTempl>().eq("NODE_ID", caseId));
				vo.setItemname(node.getNodeName());
				vo.setItemtype("tmpl");
				result.add(vo);
			}
		}
		return result;
	}

	private boolean equalLogic(List<MedicalFlowTemplRule> ruleList) {
		Set<String> set = new HashSet<String>();
		ruleList.sort(Comparator.comparing(MedicalFlowTemplRule::getOrderNo));
		int index = 0;
		for(MedicalFlowTemplRule rule : ruleList) {
			if(index>0 && StringUtils.isNotBlank(rule.getLogic())) {
				set.add(rule.getLogic());
			}
			index++;
		}
		return set.size()==1;
	}
}
