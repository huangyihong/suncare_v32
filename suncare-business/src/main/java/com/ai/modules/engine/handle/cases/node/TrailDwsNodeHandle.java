/**
 * NodeHandle.java	  V1.0   2020年4月10日 上午9:20:42
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node;

import java.util.List;

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.util.EngineUtil;

public class TrailDwsNodeHandle extends AbsNodeHandle {

	public TrailDwsNodeHandle(EngineNode node) {
		super(node);
	}

	@Override
	public String parseConditionExpression() {
		if(EngineUtil.NODE_TYPE_START.equalsIgnoreCase(node.getNodeType())
				|| EngineUtil.NODE_TYPE_END.equalsIgnoreCase(node.getNodeType())) {
			//开始或结束节点
			return null;
		}
		int size = node.getWheres()==null ? 0 : node.getWheres().size();
		StringBuilder sb = new StringBuilder();
		boolean diam = node.getNodeType().toLowerCase().startsWith(EngineUtil.NODE_TYPE_CONDITIONAL); //是否条件节点
		if(diam && "NO".equalsIgnoreCase(node.getCondition())) {
			sb.append("*:* -");
		}
		TrailDwsNodeRuleHandle handle = this.getTrailDwsNodeRuleHandle(node.getWheres());
		sb.append(handle.where());
		return sb.toString();
	}

	private TrailDwsNodeRuleHandle getTrailDwsNodeRuleHandle(List<EngineNodeRuleGrp> grpWheres) {
		if(EngineUtil.NODE_TYPE_PATIENT.equalsIgnoreCase(node.getNodeType())) {
			//按病人筛查
			return new DwsPatientNodeRuleHandle(grpWheres);
		} else if(EngineUtil.NODE_TYPE_DOCTOR.equalsIgnoreCase(node.getNodeType())) {
			//按医生筛查
			return new DwsDoctorNodeRuleHandle(grpWheres);
		} else if(EngineUtil.NODE_TYPE_ORG.equalsIgnoreCase(node.getNodeType())) {
			//按医院筛查
			return new DwsOrgNodeRuleHandle(grpWheres);
		} else if(EngineUtil.NODE_TYPE_DEPT.equalsIgnoreCase(node.getNodeType())) {
			//按科室筛查
			return new DwsDeptNodeRuleHandle(grpWheres);
		}
		return new TrailDwsNodeRuleHandle(grpWheres);
	}
}
