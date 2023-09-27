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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineTableEntity;
import com.ai.modules.engine.model.EngineTableRelationshipsEntity;
import com.ai.modules.engine.parse.node.AbsHiveNodeParser;
import com.ai.modules.engine.parse.node.HiveNodeParser;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

/**
 * 
 * 功能描述：hive与solr相结合方式计算医嘱特殊模型
 *
 * @author  zhangly
 * Date: 2020年11月16日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveDoctorAdviceCaseHandle extends HiveSolrCaseHandle {
	
	public HiveDoctorAdviceCaseHandle(String datasource, TaskProject task, TaskProjectBatch batch,
			HisMedicalFormalCase formalCase, MedicalSpecialCaseClassify classify) {
		super(datasource, task, batch, formalCase, classify);
	}

	/**
	 * 
	 * 功能描述：解析流程节点查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月18日 下午3:16:39</p>
	 *
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected List<String> parseNodeWhere(List<EngineNode> flow, Map<String, EngineTableEntity> tableAliasMap) throws Exception {
		List<String> wheres = new ArrayList<String>();		
		//过滤掉开始与结束节点
		flow = flow.stream().filter(s->!EngineUtil.NODE_TYPE_START.equals(s.getNodeType()) && !EngineUtil.NODE_TYPE_END.equals(s.getNodeType())).collect(Collectors.toList());		
		//引擎表之间的关联关系
		Map<String, EngineTableRelationshipsEntity> relationshipsMap = this.parseEngineRelationships(tableAliasMap, flow);
		for(EngineNode node : flow) {
			if(EngineUtil.NODE_TYPE_CONDITIONAL.equalsIgnoreCase(node.getNodeType())
					&& "NO".equalsIgnoreCase(node.getCondition())) {
				//否条件节点
				this.addSolrEngineNode(node);
				continue;
			}
			String table = this.getEngineNodeTable(node);
			if("DWS_PATIENT_1VISIT_ITEMSUM".equals(table)) {
				StringBuilder sb = new StringBuilder();
				sb.append(" and exists(select y.id from DWS_PATIENT_1VISIT_ITEMSUM x join ");
				Collection<EngineTableEntity> list = tableAliasMap.values();
				for(EngineTableEntity entity : list) {
					if(!EngineUtil.DWB_MASTER_INFO.equals(entity.getTable())
							 && "b".equals(entity.getAlias())) {
						sb.append(entity.getTable());
						break;
					}
				}
				sb.append(" y on x.visitid=y.visitid and x.itemcode=y.itemcode and x.itemname=y.itemname");
				sb.append(" where x.project='").append(datasource).append("'");
				sb.append(" and y.project='").append(datasource).append("'");
				sb.append(" and y.id=b.id");
				AbsHiveNodeParser nodeParser = new HiveNodeParser(node, "x");
				sb.append(" and ").append(nodeParser.handler());
				sb.append(")");
				wheres.add(sb.toString());
			} else if(tableAliasMap.containsKey(table)) {
				//节点配置查询条件的表在引擎sql中
				EngineTableEntity entity = tableAliasMap.get(table);
				AbsHiveNodeParser nodeParser = new HiveNodeParser(node, entity.getAlias());
				wheres.add("and "+nodeParser.handler());
			} else {
				//关联表
				EngineTableRelationshipsEntity relationships = relationshipsMap.get(table);
				if(relationships!=null) {
					//主表别名
					EngineTableEntity entity = tableAliasMap.get(relationships.getFrom());
					String where = relationships.getWhere();
					if(StringUtils.isNotBlank(where)) {
						where = StringUtils.replace(where, relationships.getTo(), "x");
						where = StringUtils.replace(where, relationships.getFrom(), entity.getAlias());
					}				
					AbsHiveNodeParser nodeParser = new HiveNodeParser(node, "x");
					StringBuilder sb = new StringBuilder();
					if(relationships.getFromIndex().indexOf(",")>0) {
						//多个关联字段
						sb.append(" and exists(");
						sb.append("select 1 from ").append(relationships.getTo()).append(" x");
						sb.append(" where x.project='").append(datasource).append("'");
						String[] fromIndexArray = StringUtils.split(relationships.getFromIndex(), ",");
						String[] toIndexArray = StringUtils.split(relationships.getToIndex(), ",");
						for(int i=0; i<fromIndexArray.length; i++) {					
							sb.append(" and ");
							sb.append(entity.getAlias()).append(".").append(fromIndexArray[i]);
							sb.append("=");
							sb.append("x.").append(toIndexArray[i]);
						}
						if(StringUtils.isNotBlank(where)) {
							sb.append(" and ").append(where);
						}
						sb.append(" and ").append(nodeParser.handler());
						sb.append(")");
					} else {
						sb.append(" and ").append(entity.getAlias()).append(".").append(relationships.getFromIndex());
						sb.append(" in(select x.").append(relationships.getToIndex());
						sb.append(" from ").append(relationships.getTo()).append(" x");
						sb.append(" where x.project='").append(datasource).append("'");					
						if(StringUtils.isNotBlank(where)) {
							sb.append(" and ").append(where);
						}
						sb.append(" and ").append(nodeParser.handler());
						sb.append(")");
					}
					wheres.add(sb.toString());
				} else {
					this.addSolrEngineNode(node);
				}
			}
		}
		return wheres;
	}
}
