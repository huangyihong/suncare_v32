/**
 * AbsRuleHandle.java	  V1.0   2020年11月4日 下午2:47:04
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 功能描述：规则计算引擎抽象类
 *
 * @author  zhangly
 * Date: 2020年11月12日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Slf4j
public abstract class AbsRuleHandle {
	//频次相关的不合规行为
	public static final Set<String> FREQUENCY_ACTION_SET = new HashSet<String>();
	static {
		FREQUENCY_ACTION_SET.add("freq1");
		FREQUENCY_ACTION_SET.add("freq2");
		FREQUENCY_ACTION_SET.add("freq3");
		FREQUENCY_ACTION_SET.add("freq4");
		FREQUENCY_ACTION_SET.add("freq5");
		FREQUENCY_ACTION_SET.add("freq6");
		FREQUENCY_ACTION_SET.add("freq7");
	}
	
	protected TaskProject task;
	//任务批次
	protected TaskProjectBatch batch;
	
	public AbsRuleHandle(TaskProject task, TaskProjectBatch batch) {
		this.task = task;
		this.batch = batch;
	}
	
	public abstract void generateUnreasonableAction() throws Exception;
	
	/**
	 * 
	 * 功能描述：规则是否忽略执行
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	protected boolean ignoreRun(MedicalRuleConfig rule) {
		//周期限频次规则暂时不跑
		String ruleLimit = rule.getRuleLimit();
		Set<String> cycleSet = new HashSet<String>();
  		//cycleSet.add("freq2");
  		cycleSet.add("freq4");
  		cycleSet.add("freq5");
  		cycleSet.add("freq6");
  		cycleSet.add("freq7");
  		if(cycleSet.contains(ruleLimit)) {
  			return true;
  		}
  		//项目的数据时间范围
  		String project_startTime = MedicalConstant.DEFAULT_START_TIME;
		String project_endTime = MedicalConstant.DEFAULT_END_TIME;
		project_startTime = task.getDataStartTime()!=null ? DateUtil.format(task.getDataStartTime(), "yyyy-MM-dd") : project_startTime;
        project_endTime = task.getDataEndTime()!=null ? DateUtil.format(task.getDataEndTime(), "yyyy-MM-dd") : project_endTime;
		//批次的数据时间范围
		String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
		String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
		batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
		batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
		//规则的数据时间范围
        String rule_startTime = DateUtil.format(rule.getStartTime(), "yyyy-MM-dd");
        String rule_endTime = DateUtil.format(rule.getEndTime(), "yyyy-MM-dd");
        if(rule_startTime.compareTo(project_endTime)>0 || rule_endTime.compareTo(project_startTime)<0) {
        	//规则与项目的数据时间范围限制没有交集
        	return true;
        }
        if(rule_startTime.compareTo(batch_endTime)>0 || rule_endTime.compareTo(batch_startTime)<0) {
        	//规则与批次的数据时间范围限制没有交集
        	return true;
        }
		return false;
	}
	
	/**
	 * 
	 * 功能描述：准入条件、判断条件分组
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月16日 下午4:12:37</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected List<List<MedicalRuleConditionSet>> parseCondition(List<MedicalRuleConditionSet> ruleConditionList, String type, Set<String> exclude) {
		List<MedicalRuleConditionSet> accessList = ruleConditionList.stream().filter(s->type.equals(s.getType())).collect(Collectors.toList());
		if("judge".equals(type)) {
			if(exclude!=null && exclude.size()>0) {
				accessList = accessList.stream().filter(s->!exclude.contains(s.getField())).collect(Collectors.toList());
			}			
		}
		if(accessList!=null && accessList.size()>0) {
			List<List<MedicalRuleConditionSet>> result = new ArrayList<List<MedicalRuleConditionSet>>();		
			//条件按组号分组
	        Map<Integer, List<MedicalRuleConditionSet>> groupRuleMap = accessList.stream().collect(Collectors.groupingBy(MedicalRuleConditionSet::getGroupNo));
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
                //tempList.get(0).setLogic(null);
                result.add(tempList);
            }
	        return result;
		}
		return null;
	}
	
	protected String getBusiType(MedicalRuleConfig rule) {
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
	
	/**
	 * 
	 * 功能描述：删除临时solr表
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月13日 下午1:16:19</p>
	 *
	 * @param batchId
	 * @param ruleId
	 * @param ruleType
	 * @param slave
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected void deleteTrailSolrByRule(String batchId, String ruleId, String ruleType, boolean slave) throws Exception {
        String where = "RULE_TYPE:%s AND RULE_ID:%s AND BATCH_ID:%s";
        where = String.format(where, ruleType, ruleId, batchId);
		SolrUtil.delete(EngineUtil.MEDICAL_TRAIL_DRUG_ACTION, where, slave);

        if("1".equals(ruleType)) {
        	where = "BUSI_TYPE:DRUG AND RULE_ID:%s AND BATCH_ID:%s";
        	where = String.format(where, ruleId, batchId);
        	SolrUtil.delete(EngineUtil.MEDICAL_TRAIL_ACTION, where);
        } else if("2".equals(ruleType)) {
        	where = "BUSI_TYPE:CHARGE AND RULE_ID:%s AND BATCH_ID:%s";
        	where = String.format(where, ruleId, batchId);
        	SolrUtil.delete(EngineUtil.MEDICAL_TRAIL_ACTION, where);
        } else if("4".equals(ruleType)) {
        	where = "BUSI_TYPE:TREAT AND RULE_ID:%s AND BATCH_ID:%s";
        	where = String.format(where, ruleId, batchId);
            SolrUtil.delete(EngineUtil.MEDICAL_TRAIL_ACTION, where);
        } else {
        	where = "BUSI_TYPE:%s AND RULE_ID:%s AND BATCH_ID:%s";
        	where = String.format(where, ruleType, ruleId, batchId);
            SolrUtil.delete(EngineUtil.MEDICAL_TRAIL_ACTION, where);
        }
    }
	
	/**
	 * 
	 * 功能描述：冲突项目多个时按编码排序。防止多个时顺序不一致时，id生成策略不一致
	 *
	 * @author  zhangly
	 *
	 * @param value
	 * @return
	 */
	protected String sortMutexItemcode(String value) {
		if(value==null) {
			return value;
		}
		value = StringUtils.replace(value, "[", "");
		value = StringUtils.replace(value, "]", "");
    	if(value.contains(",")) {
    		String[] array = StringUtils.split(value, ",");
    		Set<String> set = Arrays.stream(array).collect(Collectors.toSet());
    		Set<String> treeSet = new TreeSet<String>(set);
    		return StringUtils.join(treeSet, ",");
    	} else {
    		return value;
    	}
	}
}
