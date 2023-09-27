/**
 * EngineNode.java	  V1.0   2019年11月20日 下午9:28:30
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
 * 功能描述：引擎流程节点对象
 *
 * @author  zhangly
 * Date: 2019年11月20日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Data
public class EngineNode {
	private String nodeId;
	//流程节点编码
	private String nodeCode;
	//流程节点名称
	private String nodeName;
	//节点类型
	private String nodeType;
	//节点条件{yes:是，no:否}
	private String condition;
	//上一个节点编码
	private String prevNodeCode;
	//上一个节点条件{yes:是，no:否}
	private String prevNodeCondition;
	private Integer orderNo;
	//节点的查询条件
	private List<EngineNodeRuleGrp> wheres;
	//模板节点编码
	private String paramCode;

	@Override
	public String toString() {
		return "EngineNode [nodeId=" + nodeId + ", nodeCode=" + nodeCode + ", nodeName=" + nodeName + ", nodeType="
				+ nodeType + ", condition=" + condition + ", prevNodeCode=" + prevNodeCode + ", prevNodeCondition="
				+ prevNodeCondition + ", orderNo=" + orderNo + ", wheres=" + wheres
				+ ", paramCode=" + paramCode + ", mark=" + mark + "]";
	}
	
	private Boolean mark = false;
	
	/**
	 * 
	 * 功能描述：节点所有的查询条件是否允许合并
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	public boolean mergeRuleEnabled() {
		if(wheres!=null) {
			if(!mark) {
				//判断是否允许合并之前，先进行节点规则查询的表、字段替换
				this.replaceRule();
			}
			//查询条件个数
			int size = 0;
			//查询条件之间的关系and or
			Set<String> logicSet = new HashSet<String>();
			//表名
			Set<String> tableSet = new HashSet<String>();
			//字段名
			Set<String> columnSet = new HashSet<String>();
			for(EngineNodeRuleGrp grp : wheres) {
				if(StringUtils.isNotBlank(grp.getLogic())) {
					logicSet.add(grp.getLogic().toLowerCase());
				}
				for(EngineNodeRule rule : grp.getRuleList()) {
					if(rule.getCompareType().equalsIgnoreCase("notlike")) {
						//存在不包含条件返回false
						return false;
					}
					if(StringUtils.isNotBlank(rule.getLogic())) {
						logicSet.add(rule.getLogic().toLowerCase());
					}
					String table = rule.getTableName().toUpperCase();
					tableSet.add(table);
					columnSet.add(table.concat(".").concat(rule.getColName().toUpperCase()));
					size++;
				}
			}
			if(size>1 && tableSet.size()==1) {
				//节点中仅有一张表
				String first = logicSet.iterator().next();
				if(logicSet.size()==1 && "or".equals(first)) {
					//所有条件之间都是or关系
					return true;
				}
				if(size==columnSet.size()) {
					//所有条件字段都不一致
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 
	 * 功能描述：替换规则
	 *
	 * @author  zhangly
	 *
	 */
	private void replaceRule() {
		if(wheres!=null) {
			for(EngineNodeRuleGrp grp : wheres) {
				for(EngineNodeRule rule : grp.getRuleList()) {
					String table = rule.getTableName().toUpperCase();
					String column = rule.getColName().toUpperCase();
					if("DWB_DIAG".equals(table) && "DISEASECODEGROUP".equals(column)) {
						//诊断组
						rule.setTableName("STD_DIAGGROUP");
						rule.setColName("DIAGGROUP_CODE");
					} else if("DWB_CHARGE_DETAIL".equals(table)) {
						if("DRUGCODE".equals(column) || "TREATCODE".equals(column)) {
							//药品或项目
							rule.setColName("ITEMCODE");
						} else if("DRUGCODEGROUP".equals(column)) {
							//药品组
							rule.setTableName("STD_DRUGGROUP");
							rule.setColName("DRUGGROUP_CODE");
						} else if("TREATCODEGROUP".equals(column)) {
							//项目组
							rule.setTableName("STD_TREATGROUP");
							rule.setColName("TREATGROUP_CODE");
						}
					}
				}
			}
		}
		this.mark = true;
	}
}
