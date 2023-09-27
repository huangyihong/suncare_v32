/**
 * HiveCaseHandle.java	  V1.0   2020年11月16日 下午2:48:24
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.special.hive;

import java.util.ArrayList;
import java.util.List;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.handle.cases.special.AbsHiveCaseHandle;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.parse.node.AbsHiveNodeParser;
import com.ai.modules.engine.parse.node.HiveNodeParser;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

import cn.hutool.core.date.DateUtil;

/**
 * 
 * 功能描述：hive方式计算串换项目不合规行为
 *
 * @author  zhangly
 * Date: 2020年11月16日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveCaseTranspositionHandle extends AbsHiveCaseHandle {

	public HiveCaseTranspositionHandle(String datasource, TaskProject task, TaskProjectBatch batch,
			HisMedicalFormalCase formalCase, MedicalSpecialCaseClassify classify) {
		super(datasource, task, batch, formalCase, classify);
	}

	@Override
	public List<String> parseWhere(List<EngineNode> flow) throws Exception {
		List<String> wheres = new ArrayList<String>();
		//查询条件
		String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
		String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
		batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
		batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
		//业务数据时间范围限制
		wheres.add("and a.visitdate>='"+batch_startTime+"'");
		wheres.add("and a.visitdate<='"+batch_endTime+"'");
		wheres.add("and a.project='"+datasource+"'");
		wheres.add("and b.project='"+datasource+"'");
		wheres.addAll(this.parseNodeWhere(flow));
		return wheres;
	}

	private List<String> parseNodeWhere(List<EngineNode> flow) throws Exception {
		List<String> wheres = new ArrayList<String>();
		for(EngineNode node : flow) {
			if(EngineUtil.NODE_TYPE_START.equalsIgnoreCase(node.getNodeType())
					|| EngineUtil.NODE_TYPE_END.equalsIgnoreCase(node.getNodeType())) {
				continue;
			}
			String table = this.getEngineNodeTable(node);
			if(EngineUtil.DWB_MASTER_INFO.equals(table)) {
				AbsHiveNodeParser nodeParser = new HiveNodeParser(node, "a");
				wheres.add("and "+nodeParser.handler());
			} else if(EngineUtil.DWB_CHARGE_DETAIL.equals(table)) {
				AbsHiveNodeParser nodeParser = new HiveNodeParser(node, "b");
				wheres.add("and "+nodeParser.handler());
			} else if("DWS_ITEMEXCHANGE1_DIFFITEM".equals(table)) {
				StringBuilder sb = new StringBuilder();
				sb.append(" and exists(");
				sb.append("select 1 from DWS_ITEMEXCHANGE1_DIFFITEM x");
				sb.append(" where x.YB_ITEMCODE=b.ITEMCODE and x.YB_ITEMCODE=x.HIS_ITEMCODE");
				sb.append(" and x.project='").append(datasource).append("'");
				AbsHiveNodeParser nodeParser = new HiveNodeParser(node, "x");
				sb.append(" and ").append(nodeParser.handler());
				sb.append(")");
				wheres.add(sb.toString());
			}
		}
		return wheres;
	}
}
