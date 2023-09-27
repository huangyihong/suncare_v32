/**
 * AbsRuleHandle.java	  V1.0   2020年11月4日 下午2:47:04
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.special.hive;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.cases.node.hive.AbsHiveNodeHandle;
import com.ai.modules.engine.handle.cases.node.hive.CaseWithTableUtil;
import com.ai.modules.engine.handle.cases.node.hive.HiveNodeHandleFactory;
import com.ai.modules.engine.handle.cases.special.AbsImpalaCaseHandle;
import com.ai.modules.engine.handle.rule.hive.model.JoinTableModel;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.ColumnType;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.model.EngineTableEntity;
import com.ai.modules.engine.parse.node.AbsHiveNodeParser;
import com.ai.modules.engine.parse.node.HiveNodeParser;
import com.ai.modules.engine.service.IEngineService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.JDBCUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：模型规则计算引擎使用impala方式计算
 *
 * @author  zhangly
 * Date: 2020年11月12日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class ImpalaSpecialCaseHandle extends AbsImpalaCaseHandle {

	public ImpalaSpecialCaseHandle(String datasource, TaskProject task, TaskProjectBatch batch, 
			HisMedicalFormalCase formalCase, MedicalSpecialCaseClassify classify) {
		super(datasource, task, batch ,formalCase, classify);
	}
	
	@Override
	public EngineResult generateUnreasonableAction() throws Exception {
		if(ignoreRun()) {
        	//忽略运行
        	return EngineResult.ok();
        }
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IEngineService engineSV = context.getBean(IEngineService.class);
		List<List<EngineNode>> flowList = engineSV.queryHisFormalEngineNode(formalCase.getCaseId(), batch.getBatchId());
		if (flowList == null || flowList.size() == 0) {
			throw new EngineBizException("模型未能找到流程节点！");
		}
		if (flowList.size() > 1) {
			throw new EngineBizException("模型暂不支持多条流程节点！");
		}
		if(flowList.size()==1) {
			//仅有一条流程
			return this.generate(flowList.get(0));
		} else {
			//遍历流程节点条件列表
			for(List<EngineNode> flow : flowList) {
				this.generate(flow);
			}
			Thread.sleep(5000L);
			return this.computeMoney();
		}		
	}
	
	private EngineResult generate(List<EngineNode> flow) throws Exception {
		if(HiveJDBCUtil.enabledStorageGp()) {
			return generateStorageGp(flow);
		} else {
			return generateStorageSolr(flow);
		}
	}
	
	private EngineResult generateStorageSolr(List<EngineNode> flow) throws Exception {
		String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/" + formalCase.getCaseId() + ".json";
		String sql = this.masterInfoJoinSql(flow);
		log.info("\n规则:{caseId:{},caseName:{}}\nhive sql:\n{}", formalCase.getCaseId(), formalCase.getCaseName(), sql);
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		BufferedWriter fileWriter = null;
		try {
			EngineResult result = EngineResult.ok();
			conn = HiveJDBCUtil.getWarehouseConnection();
			stmt = conn.prepareStatement(sql);
	        rs = stmt.executeQuery();
	        Set<ColumnType> columnSet = new LinkedHashSet<ColumnType>();
	        ResultSetMetaData rsmd = rs.getMetaData();
	        for(int i=1,len=rsmd.getColumnCount(); i<=len; i++) {
	        	String columnName = rsmd.getColumnName(i);
	        	columnSet.add(new ColumnType(columnName, rsmd.getColumnTypeName(i)));
	        }
	        int count = 0;
	        BigDecimal actionMoney = BigDecimal.ZERO;
	        BigDecimal money = BigDecimal.ZERO;
	        fileWriter = new BufferedWriter(
	                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
	        //写文件头
            fileWriter.write("[");
	        while(rs.next()) {
            	JSONObject jsonObject = this.parseJSONObject(rs, columnSet);
            	if (jsonObject.get("TOTALFEE") != null) {
					money = money.add(new BigDecimal(jsonObject.get("TOTALFEE").toString()));
				}
				if (jsonObject.get("FUNDPAY") != null) {
					actionMoney = actionMoney.add(new BigDecimal(jsonObject.get("FUNDPAY").toString()));
				}
		        try {
		            fileWriter.write(jsonObject.toJSONString());
		            fileWriter.write(',');
		        } catch (IOException e) {
		        }
            	count++;
            }
	        log.info("\n规则:{caseId:{},caseName:{}}计算结果数：{}", formalCase.getCaseId(), formalCase.getCaseName(), count);
            // 文件尾
            fileWriter.write("]");
            fileWriter.flush();
            if(count>0) {
            	//导入solr
                SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
            } else {
            	//删除文件
                File file = new File(importFilePath);
                if(file.exists()) {
                	file.delete();
                }
            }
            result.setCount(count);
            result.setMoney(money);
            result.setActionMoney(actionMoney);
            return result;
		} catch(Exception e) {		
			throw e;
		} finally {
			HiveJDBCUtil.destroy(rs, conn, stmt);
			if(fileWriter!=null) {
    			try {
    				fileWriter.close();
    			} catch(Exception e) {}
    		}
		}
	}
	
	private EngineResult generateStorageGp(List<EngineNode> flow) throws Exception {
		String sql = this.masterInfoJoinSql(flow);
		log.info("\n规则:{caseId:{},caseName:{}}\nhive sql:\n{}", formalCase.getCaseId(), formalCase.getCaseName(), sql);
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		Connection targetConn = null;
		Statement targetStmt = null;
		try {
			EngineResult result = EngineResult.ok();
			conn = HiveJDBCUtil.getWarehouseConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setQueryTimeout(JDBCUtil.QUERY_TIMEOUT);
	        rs = stmt.executeQuery();
	        rs.setFetchSize(JDBCUtil.FETCH_SIZE);
	        Set<ColumnType> columnSet = new LinkedHashSet<ColumnType>();
	        ResultSetMetaData rsmd = rs.getMetaData();
	        for(int i=1,len=rsmd.getColumnCount(); i<=len; i++) {
	        	String columnName = rsmd.getColumnName(i);
	        	columnSet.add(new ColumnType(columnName, rsmd.getColumnTypeName(i)));
	        }
	        targetConn = JDBCUtil.getConnection();
 	        targetConn.setAutoCommit(false);//设置为不自动提交
 	        targetStmt = targetConn.createStatement();
 	        List<JSONObject> dataList = new ArrayList<JSONObject>();
	        int count = 0;
	        BigDecimal actionMoney = BigDecimal.ZERO;
	        BigDecimal money = BigDecimal.ZERO;
	        LocalDateTime startTime = LocalDateTime.now();
	        while(rs.next()) {
            	JSONObject jsonObject = this.parseJSONObject(rs, columnSet);
            	if (jsonObject.get("TOTALFEE") != null) {
					money = money.add(new BigDecimal(jsonObject.get("TOTALFEE").toString()));
				}
				if (jsonObject.get("FUNDPAY") != null) {
					actionMoney = actionMoney.add(new BigDecimal(jsonObject.get("FUNDPAY").toString()));
				}
		        count++;
            	dataList.add(jsonObject);
            	if(dataList.size()==JDBCUtil.PAGE_LIMIT) {
            		String script = JDBCUtil.parseInsertIntoScript(datasource, dataList, columnSet);
            		targetStmt.addBatch(script);
            		dataList.clear();
            	}
		        if(count%JDBCUtil.COMMIT_LIMIT==0) {
					targetStmt.executeBatch();
                    targetConn.commit();
                    targetStmt.clearBatch();
				}
            }
	        if(dataList.size()>0) {
	        	String script = JDBCUtil.parseInsertIntoScript(datasource, dataList, columnSet);
        		targetStmt.addBatch(script);
        		dataList.clear();
	        }
	        if(count%JDBCUtil.COMMIT_LIMIT>0) {
				targetStmt.executeBatch();
                targetConn.commit();
                targetStmt.clearBatch();
			}
	        LocalDateTime endTime = LocalDateTime.now();
			Duration duration = Duration.between(startTime, endTime);
			long seconds = duration.toMillis() / 1000;//相差毫秒数
			long minutes = seconds / 60;
			log.info("导入gp耗时： "+minutes +"分钟，"+ seconds % 60+"秒 。");
	        log.info("\n规则:{caseId:{},caseName:{}}计算结果数：{}", formalCase.getCaseId(), formalCase.getCaseName(), count);
	        result.setCount(count);
            result.setMoney(money);
            result.setActionMoney(actionMoney);
            return result;
		} catch(Exception e) {		
			throw e;
		} finally {
			HiveJDBCUtil.destroy(rs, conn, stmt);
			JDBCUtil.destroy(targetConn, targetStmt);
		}
	}
	
	/**
	 * 
	 * 功能描述：特殊模型sql脚本
	 *
	 * @author  zhangly
	 *
	 * @param flow
	 * @return
	 * @throws Exception
	 */
	protected String masterInfoJoinSql(List<EngineNode> flow) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from dwb_master_info x");
		sql.append(" where ");
		//公共查询条件
		sql.append(StringUtils.join(this.appendCommonWhere("x"), " and "));
		JoinTableModel joinTableModel = this.joinTables(flow);
		//项目相关的主表查询条件
		if(joinTableModel.getMasterList()!=null) {
			for(String where : joinTableModel.getMasterList()) {
				sql.append(" and ").append(where);
			}
		}
		String withSql = sql.toString();
		if(joinTableModel.getWithTableList()!=null 
				&& joinTableModel.getWithTableList().size()>0) {
			sql.setLength(0);
			sql.append("with dwb_master_info as(");
			sql.append(withSql);
			sql.append(")");
			for(WithTableModel model : joinTableModel.getWithTableList()) {
				sql.append("\n,");
				sql.append(model.getAlias()).append(" as(").append(model.getSql()).append(")");
			}
			sql.append("\n");
			sql.append("select * from dwb_master_info a");
			sql.append(" where");
			for(int i=0,len=joinTableModel.getWithTableList().size(); i<len; i++) {
				WithTableModel model = joinTableModel.getWithTableList().get(i);
				if(i>0) {
					sql.append(" and");
				}
				sql.append(" visitid in(select visitid from ").append(model.getAlias()).append(")");
			}
			withSql = sql.toString();
		}
		
		sql.setLength(0);
		String fromTable = "tmp_dwb_master_info";
		sql.append("with ").append(fromTable).append(" as(");
		sql.append(withSql);
		sql.append(")");
		//节点规则的with脚本
		Map<String, EngineTableEntity> tableAliasMap = this.parseEngineSql();
		List<WithTableModel> withTableList = this.parseNodeWhere(flow, tableAliasMap, fromTable);
		if(withTableList.size()>0) {
			for(WithTableModel withTable : withTableList) {
				sql.append("\n,").append(withTable.getAlias()).append(" as(").append(withTable.getSql()).append(")");
			}
			fromTable = withTableList.get(withTableList.size()-1).getAlias();
		}
		sql.append("\n");
		String engineSql = this.processEngineSql();
		//解析出关系型数据库可执行的sql
		Map<String, String> udfFieldMap = this.parseUdfFieldMap(engineSql);
		sql.append("select ");
		int index = 0;
		for(Map.Entry<String, String> entry : udfFieldMap.entrySet()) {
			index++;
			sql.append(entry.getValue()).append(" ").append(entry.getKey());
			if(index<udfFieldMap.size()) {
				sql.append(",");
			}
		}
		String find = " from";
		int start = engineSql.indexOf(find);
		String query = engineSql.substring(start);
		query = StringUtils.replace(query, "dwb_master_info", fromTable);
		sql.append(query);
		//在引擎sql脚本中表的查询条件
		List<String> wheres = this.parseNodeInMasterWhere(flow, tableAliasMap);
		for(String where : wheres) {
			sql.append(" and ").append(where);
		}
		return sql.toString();
	}
	
	/**
	 * 
	 * 功能描述：解析模型节点（节点配置的表在引擎sql中）
	 *
	 * @author  zhangly
	 *
	 * @param flow
	 * @param tableAliasMap
	 * @throws Exception
	 */
	private List<String> parseNodeInMasterWhere(List<EngineNode> flow, Map<String, EngineTableEntity> tableAliasMap) throws Exception {
		List<String> wheres = new ArrayList<String>();
		//过滤掉开始与结束节点
		flow = flow.stream().filter(s->!EngineUtil.NODE_TYPE_START.equals(s.getNodeType()) && !EngineUtil.NODE_TYPE_END.equals(s.getNodeType())).collect(Collectors.toList());		
		for(EngineNode node : flow) {
			if(EngineUtil.NODE_TYPE_CONDITIONAL.equalsIgnoreCase(node.getNodeType())
					&& "NO".equalsIgnoreCase(node.getCondition())) {
				//否条件节点				
				continue;
			}
			String table = this.getEngineNodeTable(node);
			if(tableAliasMap.containsKey(table)) {
				//节点配置查询条件的表在引擎sql中
				EngineTableEntity entity = tableAliasMap.get(table);
				AbsHiveNodeParser nodeParser = new HiveNodeParser(node, entity.getAlias());
				wheres.add(nodeParser.handler());
			}
		}
		return wheres;
	}
	
	/**
	 * 
	 * 功能描述：解析模型节点（节点配置的表未在引擎sql中）
	 *
	 * @author  zhangly
	 *
	 * @param flow
	 * @param tableAliasMap
	 * @return
	 * @throws Exception
	 */
	private List<WithTableModel> parseNodeWhere(List<EngineNode> flow, Map<String, EngineTableEntity> tableAliasMap, String fromTable) throws Exception {
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		//过滤掉开始与结束节点
		flow = flow.stream().filter(s->!EngineUtil.NODE_TYPE_START.equals(s.getNodeType()) && !EngineUtil.NODE_TYPE_END.equals(s.getNodeType())).collect(Collectors.toList());		
		for(EngineNode node : flow) {
			//是否属于否条件节点
			boolean reverse = EngineUtil.NODE_TYPE_CONDITIONAL.equalsIgnoreCase(node.getNodeType()) && "NO".equalsIgnoreCase(node.getCondition());
			String table = this.getEngineNodeTable(node);
			if(!reverse && tableAliasMap.containsKey(table)) {
				//忽略，节点已处理过
				continue;
			}
			if(tableAliasMap.containsKey(table)) {
				//表在引擎sql脚本中
				StringBuilder sb = new StringBuilder();
				sb.append("select * from ").append(table);
				sb.append(" where ");
				sb.append("$field not in(");
				sb.append("select $field from ").append(table);
				sb.append(" where ");
				AbsHiveNodeParser parser = new HiveNodeParser(node, null);
				sb.append(parser.handler());
				sb.append(")");
				String sql = sb.toString();
				if("DWB_MASTER_INFO".equals(table)) {
					sql = StringUtils.replace(sql, "$field", "visitid");
				} else {
					sql = StringUtils.replace(sql, "$field", "id");
				}
				String alias = CaseWithTableUtil.buildWithTableAlias(node, table);
				withTableList.add(new WithTableModel(alias, sql));
			} else {
				//表不在引擎sql脚本中
				AbsHiveNodeHandle handle = HiveNodeHandleFactory.getHiveNodeHandle(node, fromTable);
				WithTableModel withTable = handle.parseWithTableModel();
				fromTable = withTable.getAlias();
				withTableList.add(withTable);
			}
		}
		return withTableList;
	}
}
