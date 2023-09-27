/**
 * JdbcCaseHandle.java	  V1.0   2022年6月28日 下午3:33:58
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.special;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.cases.AbsJdbcCaseHandle;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.model.EngineTableEntity;
import com.ai.modules.engine.model.EngineTableRelationshipsEntity;
import com.ai.modules.engine.parse.HiveTableAliasVisitor;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.hive.visitor.HiveSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.stat.TableStat.Name;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSON;

public abstract class JdbcCaseHandle extends AbsJdbcCaseHandle {
	protected static final Logger log = LoggerFactory.getLogger(HiveJDBCUtil.class);

	//模型引擎
	protected MedicalSpecialCaseClassify classify;
	//引擎sql
	protected String engineSql;
	
	public JdbcCaseHandle(String datasource, TaskProject task, TaskProjectBatch batch, 
			HisMedicalFormalCase formalCase, MedicalSpecialCaseClassify classify) {		
		super(datasource, task, batch, formalCase);
		this.classify = classify;
		this.engineSql = classify.getEngineSql();
	}	
	
	/**
	 * 
	 * 功能描述：删除solr历史数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月16日 下午4:25:24</p>
	 *
	 * @param collection
	 * @param batchId
	 * @param caseId
	 * @param slave
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected void deleteSolrByCase(String collection, String batchId, String caseId, boolean slave) throws Exception {
        String where = "BUSI_TYPE:%s AND CASE_ID:%s AND BATCH_ID:%s";
    	where = String.format(where, MedicalConstant.ENGINE_BUSI_TYPE_CASE, caseId, batchId);
        SolrUtil.delete(collection, where, slave);
    }
	
	/**
	 * 
	 * 功能描述：解析引擎sql，获取计算引擎结果来源的数据表名以及表的别名
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月18日 下午3:09:43</p>
	 *
	 * @param sql
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected Map<String, EngineTableEntity> parseEngineSql() throws Exception {
		Map<String, EngineTableEntity> result = new HashMap<String, EngineTableEntity>();
		String sql = classify.getEngineSql();
		sql = sql.replaceAll("<#if .*</#if>", "");
		String dbType = JdbcConstants.HIVE;
		List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
		Map<String, String> tableAliasMap = new LinkedHashMap<String, String>();
		//解析出的独立语句的个数
		for (int i = 0; i < stmtList.size(); i++) {
			SQLStatement stmt = stmtList.get(i);
			HiveSchemaStatVisitor visitor = new HiveSchemaStatVisitor();
			stmt.accept(visitor);
			//获取表名称
			Map<Name, TableStat> tabmap = visitor.getTables();
			for (Iterator<Name> iterator = tabmap.keySet().iterator(); iterator.hasNext();) {
				Name name = (Name) iterator.next();
				tableAliasMap.put(name.toString().toUpperCase(), name.toString().toUpperCase());
			}
			//获取表别名
			HiveTableAliasVisitor aliasVisitor = new HiveTableAliasVisitor();	
			stmt.accept(aliasVisitor);
			Map<String, SQLTableSource> aliasMap = aliasVisitor.getAliasMap();
			for(Map.Entry<String, SQLTableSource> entry : aliasMap.entrySet()) {
				if(entry.getKey()!=null) {
					String alias = entry.getKey();
					String table = entry.getValue().toString();
					table = table.toUpperCase();
					tableAliasMap.put(table, alias);
				}
			}
		}
		
		sql = sql.toUpperCase();
		int index = sql.indexOf("WHERE");
		if(index>-1) {
			sql = sql.substring(0, index);
		}
		index = sql.indexOf("FROM ");
		sql = sql.substring(index);
		for(Map.Entry<String, String> entry : tableAliasMap.entrySet()) {
			String table = entry.getKey();
			if(sql.indexOf(table)>-1) {
				EngineTableEntity entity = new EngineTableEntity(table, entry.getValue());
				result.put(table, entity);
			}
		}
		return result;
	}
	
	/**
	 * 
	 * 功能描述：主表dwb_master_info别名
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年6月23日 上午11:26:50</p>
	 *
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected String masterTableAlias() throws Exception {
		String sql = this.engineSql;
		sql = sql.replaceAll("<#if .*</#if>", "");
		String dbType = JdbcConstants.HIVE;
		List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
		//解析出的独立语句的个数
		for (int i = 0; i < stmtList.size(); i++) {
			SQLStatement stmt = stmtList.get(i);
			HiveSchemaStatVisitor visitor = new HiveSchemaStatVisitor();
			stmt.accept(visitor);
			//获取表别名
			HiveTableAliasVisitor aliasVisitor = new HiveTableAliasVisitor();	
			stmt.accept(aliasVisitor);
			Map<String, SQLTableSource> aliasMap = aliasVisitor.getAliasMap();
			for(Map.Entry<String, SQLTableSource> entry : aliasMap.entrySet()) {
				String table = entry.getValue().toString();
				table = table.toUpperCase();
				if("dwb_master_info".equalsIgnoreCase(table)) {
					String alias = "dwb_master_info";
					if(entry.getKey()!=null) {
						alias = entry.getKey();
					}
					return alias;
				}
			}
		}
		return null;
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
		}
		
		return relationshipsMap;
	}
	
	/**
	 * 
	 * 功能描述：获取节点的表名
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月17日 下午12:18:50</p>
	 *
	 * @param node
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected String getEngineNodeTable(EngineNode node) {
		for(EngineNodeRuleGrp grp : node.getWheres()) {
			for(EngineNodeRule rule : grp.getRuleList()) {
				return rule.getTableName().toUpperCase();
			}
		}
		return null;
	}
}
