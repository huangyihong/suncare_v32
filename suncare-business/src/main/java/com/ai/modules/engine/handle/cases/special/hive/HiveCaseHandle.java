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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.cases.special.AbsHiveCaseHandle;
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
import com.alibaba.fastjson.JSON;

import cn.hutool.core.date.DateUtil;

/**
 * 
 * 功能描述：hive方式计算特殊的不合规行为模型
 *
 * @author  zhangly
 * Date: 2020年11月16日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveCaseHandle extends AbsHiveCaseHandle {

	public HiveCaseHandle(String datasource, TaskProject task, TaskProjectBatch batch,
			HisMedicalFormalCase formalCase, MedicalSpecialCaseClassify classify) {
		super(datasource, task, batch, formalCase, classify);
	}

	@Override
	public List<String> parseWhere(List<EngineNode> flow) throws Exception {
		List<String> wheres = new ArrayList<String>();
		//查询条件		
		String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
		String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
		String case_startTime = MedicalConstant.DEFAULT_START_TIME;
        String case_endTime = MedicalConstant.DEFAULT_END_TIME;
		batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
		batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
		case_startTime = formalCase.getStartTime()!=null ? DateUtils.formatDate(formalCase.getStartTime(), "yyyy-MM-dd") : case_startTime;
        case_endTime = formalCase.getEndTime()!=null ? DateUtils.formatDate(formalCase.getEndTime(), "yyyy-MM-dd") : case_endTime;
		Map<String, EngineTableEntity> tableAliasMap = this.parseEngineSql();
		EngineTableEntity master = tableAliasMap.get(EngineUtil.DWB_MASTER_INFO);
		if(master!=null) {
			//业务数据时间范围限制
			String where = "and %s.visitdate>='%s'";
			where = String.format(where, master.getAlias(), batch_startTime);
			wheres.add(where);
			where = "and %s.visitdate<='%s'";
			where = String.format(where, master.getAlias(), batch_endTime);
			wheres.add(where);
			//模型的数据时间范围限制
			where = "and %s.visitdate>='%s'";
			where = String.format(where, master.getAlias(), case_startTime);
			wheres.add(where);
			where = "and %s.visitdate<='%s'";
			where = String.format(where, master.getAlias(), case_endTime);
			wheres.add(where);
			//数据来源限制
			if (StringUtils.isNotBlank(batch.getEtlSource())) {
				where = "and %s.etl_source='%s'";
				where = String.format(where, master.getAlias(), batch.getEtlSource());
				wheres.add(where);
			}
		}
		//业务数据数据区域限制
		for(Map.Entry<String, EngineTableEntity> entry : tableAliasMap.entrySet()) {
			EngineTableEntity entity = entry.getValue();
			if(!entity.getTable().startsWith("STD_")) {//非字典表
				String where = "and %s.project='%s'";
				where = String.format(where, entity.getAlias(), datasource);
				wheres.add(where);
			}
		}
		//自定义数据范围限制
		if(StringUtils.isNotBlank(batch.getCustomFilter())) {
			String value = batch.getCustomFilter();
			value = "'" + StringUtils.replace(value, ",", "','") + "'";
			String where = "and %s.orgid in(%s)";
			where = String.format(where, master.getAlias(), value);
			wheres.add(where);
		}
		//流程节点查询条件
		wheres.addAll(this.parseNodeWhere(flow, tableAliasMap));
		return wheres;
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
			String table = this.getEngineNodeTable(node);
			if(tableAliasMap.containsKey(table)) {
				//节点配置查询条件的表在引擎sql中
				EngineTableEntity entity = tableAliasMap.get(table);
				AbsHiveNodeParser nodeParser = new HiveNodeParser(node, entity.getAlias());
				wheres.add("and "+nodeParser.handler());
			} else {
				//关联表
				EngineTableRelationshipsEntity relationships = relationshipsMap.get(table);
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
			}
		}
		return wheres;
	}
	
	/**
	 * 
	 * 功能描述：解析引擎表之间的关系
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月18日 下午4:39:11</p>
	 *
	 * @param tableAliasMap
	 * @param nodeList
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected Map<String, EngineTableRelationshipsEntity> parseEngineRelationships(Map<String, EngineTableEntity> tableAliasMap, 
			List<EngineNode> nodeList) throws Exception {
		List<EngineTableRelationshipsEntity> relationshipsList = null;
		if(StringUtils.isNotBlank(classify.getEngineRelationships())) {
			relationshipsList = JSON.parseArray(classify.getEngineRelationships(), EngineTableRelationshipsEntity.class);
		}
		Map<String, EngineTableRelationshipsEntity> relationshipsMap = new HashMap<String, EngineTableRelationshipsEntity>();
		if(relationshipsList!=null) {
			for(EngineTableRelationshipsEntity entity : relationshipsList) {
				String to = entity.getTo().toUpperCase();
				if(!tableAliasMap.containsKey(to)) {
					//关联关系表不在引擎sql中
					relationshipsMap.put(to, entity);
				}
			}
		}		
		//模型流程节点限制的可选表
		Set<String> limitSet = new HashSet<String>();	
		String optionalTable = classify.getOptionalTable();
		if(StringUtils.isNotBlank(optionalTable)) {
			String[] array = StringUtils.split(optionalTable, ",");
			for(String table : array) {
				limitSet.add(table.toUpperCase());
			}
		}
		//遍历流程节点		
		for(EngineNode node : nodeList) {
			String table = this.getEngineNodeTable(node);
			if(tableAliasMap.containsKey(table) || relationshipsMap.containsKey(table)) {
				//关联关系表在引擎sql中或者已存在关联关系，忽略
				continue;
			}
			if(!limitSet.contains(table)) {
				throw new EngineBizException("模型节点配置有误，请检查！");
			}
			//关联关系不在配置表中，默认关联DWB_MASTER_INFO
			EngineTableRelationshipsEntity entity = new EngineTableRelationshipsEntity();
			entity.setFrom(EngineUtil.DWB_MASTER_INFO);
			entity.setTo(table);
			entity.setFromIndex("VISITID");
			entity.setToIndex("VISITID");
			relationshipsMap.put(table, entity);
		}
		
		return relationshipsMap;
	}
}
