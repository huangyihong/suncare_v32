/**
 * EngineNodeRuleGrp.java	  V1.0   2019年11月20日 下午10:14:41
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

/**
 *
 * 功能描述：引擎节点规则组对象（查询条件集合）
 *
 * @author  zhangly
 * Date: 2019年11月20日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Data
public class EngineNodeRuleGrp {
	//逻辑运算符
	private String logic;
	//规则集合
	private List<EngineNodeRule> ruleList;
	
	/**
	 * 
	 * 功能描述：节点某组内所有的查询条件是否允许合并
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	public boolean mergeRuleEnabled() {
		if(ruleList!=null) {
			//查询条件个数
			int size = 0;
			//查询条件之间的关系and or
			Set<String> logicSet = new HashSet<String>();
			//表名
			Set<String> tableSet = new HashSet<String>();
			//字段名
			Set<String> columnSet = new HashSet<String>();
			//比较符
			Set<String> compareSet = new HashSet<String>();
			for(EngineNodeRule rule : ruleList) {
				/*if(rule.getCompareType().equalsIgnoreCase("notlike")) {
					//存在不包含条件返回false
					return false;
				}*/
				if(StringUtils.isNotBlank(rule.getLogic())) {
					logicSet.add(rule.getLogic().toLowerCase());
				}
				String table = rule.getTableName().toUpperCase();
				tableSet.add(table);
				columnSet.add(table.concat(".").concat(rule.getColName().toUpperCase()));
				compareSet.add(rule.getCompareType());
				size++;
			}
			if(size>1 && tableSet.size()==1) {
				//节点中仅有一张表
				String first = logicSet.iterator().next();
				String compare = compareSet.iterator().next();
				if("notlike".equals(compare) || "<>".equals(compare)) {
					//不包含或不等于条件
					if(compareSet.size()==1 && logicSet.size()==1 && "and".equals(first)) {
						//所有条件都是不包含且之间是and关系
						return true;
					}
				} else if(logicSet.size()==1 && "or".equals(first)) {
					//所有条件之间都是or关系
					return true;
				} else if(size==columnSet.size()) {
					//所有条件字段都不一致
					return true;
				}
			}
		}
		return false;
	}
}
