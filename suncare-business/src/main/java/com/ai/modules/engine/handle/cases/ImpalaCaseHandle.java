/**
 * ImpalaCaseHandle.java	  V1.0   2022年12月6日 下午3:15:09
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.common.MedicalConstant;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.cases.node.hive.AbsHiveNodeHandle;
import com.ai.modules.engine.handle.cases.node.hive.HiveNodeHandleFactory;
import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.SolrConstField;
import com.ai.modules.engine.handle.rule.SolrFieldAnnotation;
import com.ai.modules.engine.handle.rule.hive.WithTableUtil;
import com.ai.modules.engine.handle.rule.hive.model.JoinTableModel;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.ColumnType;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.service.IEngineService;
import com.ai.modules.engine.service.api.IApiCaseService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.service.impl.EngineCaseServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.JDBCUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.entity.MedicalFormalCaseItemRela;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImpalaCaseHandle extends AbsJdbcCaseHandle {

	public ImpalaCaseHandle(String datasource, TaskProject task, TaskProjectBatch batch,
			HisMedicalFormalCase formalCase) {
		super(datasource, task, batch, formalCase);
	}

	@Override
	public EngineResult generateUnreasonableAction() throws Exception {
		if (this.ignoreRun()) {
			//忽略运行
			return EngineResult.ok();
		}
		if (!"normal".equalsIgnoreCase(formalCase.getCaseStatus())) {
			throw new EngineBizException("模型不是正常启用状态！");
		}
		String batchId = batch.getBatchId();
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IEngineService engineService = context.getBean(IEngineService.class);
		List<List<EngineNode>> flowList = engineService.queryHisFormalEngineNode(formalCase.getCaseId(), batchId);
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
			for (int i = 0, len = flowList.size(); i < len; i++) {
				List<EngineNode> flow = flowList.get(i);
				this.generate(flow);
			}
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
		String sql = this.masterInfoSql(flow);
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
            LocalDateTime startTime = LocalDateTime.now();
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
	        LocalDateTime endTime = LocalDateTime.now();
			Duration duration = Duration.between(startTime, endTime);
			long seconds = duration.toMillis() / 1000;//相差毫秒数
			long minutes = seconds / 60;
			log.info("导入gp耗时： "+minutes +"分钟，"+ seconds % 60+"秒 。");
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
		String sql = this.masterInfoSql(flow);
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
	
	private String masterInfoSql(List<EngineNode> flow) throws Exception {
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
		List<WithTableModel> withTableList = this.parseNodeWhere(flow, fromTable);
		if(withTableList.size()>0) {
			for(WithTableModel withTable : withTableList) {
				sql.append("\n,").append(withTable.getAlias()).append(" as(").append(withTable.getSql()).append(")");
			}
			fromTable = withTableList.get(withTableList.size()-1).getAlias();
		}
		sql.append("\n");
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IApiCaseService caseSV = context.getBean(IApiCaseService.class);
		MedicalFormalCaseItemRela rela = caseSV.findMedicalFormalCaseItemRela(formalCase.getCaseId());
		//是否明细层级违规
		boolean isDetail = rela != null && StringUtils.isNotBlank(rela.getItemIds());
		Map<String, String> udfFieldMap = this.parseFieldMap(isDetail);
		sql.append("select ");
		int index = 0;
		for(Map.Entry<String, String> entry : udfFieldMap.entrySet()) {
			index++;
			sql.append(entry.getValue()).append(" ").append(entry.getKey());
			if(index<udfFieldMap.size()) {
				sql.append(",");
			}
		}
		sql.append(" from ").append(fromTable).append(" x");
		if(isDetail) {
			//明细层级违规
			sql.append(" join ").append(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM).append(" y on x.visitid=y.visitid");
			sql.append(" where ");
			if (MedicalConstant.CASE_RELA_TYPE_DRUGGROUP.equals(rela.getType())) {
				//药品组
				String where = "y.itemcode in(select ATC_DRUGCODE from medical_gbdp.STD_DRUGGROUP std where std.DRUGGROUP_CODE in(%s))";
				String value = "'" + StringUtils.replace(rela.getItemIds(), ",", "','") + "'";
				where = String.format(where, value);
				sql.append(where);
			} else if (MedicalConstant.CASE_RELA_TYPE_PROJECTGROUP.equals(rela.getType())) {
				//项目组
				String where = "y.itemcode in(select TREATCODE from medical_gbdp.STD_TREATGROUP std where std.TREATGROUP_CODE in(%s))";
				String value = "'" + StringUtils.replace(rela.getItemIds(), ",", "','") + "'";
				where = String.format(where, value);
				sql.append(where);
			} else if (MedicalConstant.CASE_RELA_TYPE_DRUG.equals(rela.getType())) {
				//查找药品编码
				String where = "y.itemcode in(%s)";
				String value = "'" + StringUtils.replace(rela.getItemIds(), ",", "','") + "'";
				where = String.format(where, value);
				sql.append(where);
			} else {
				//项目
				String where = "y.itemcode in(%s)";
				String value = "'" + StringUtils.replace(rela.getItemIds(), ",", "','") + "'";
				where = String.format(where, value);
				sql.append(where);
			}
		}
		return sql.toString();
	}
	
	/**
	 * 
	 * 功能描述：解析模型节点（节点配置的表未在引擎sql中）
	 *
	 * @author  zhangly
	 *
	 * @param flow
	 * @param fromTable
	 * @return
	 * @throws Exception
	 */
	private List<WithTableModel> parseNodeWhere(List<EngineNode> flow, String fromTable) throws Exception {
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		//过滤掉开始与结束节点
		flow = flow.stream().filter(s->!EngineUtil.NODE_TYPE_START.equals(s.getNodeType()) && !EngineUtil.NODE_TYPE_END.equals(s.getNodeType())).collect(Collectors.toList());		
		if(HiveJDBCUtil.enabledProcessGp() && flow.size()>5) {
			//gp计算模式并且节点超过5个
			for(EngineNode node : flow) {
				//是否属于否条件节点
				boolean reverse = EngineUtil.NODE_TYPE_CONDITIONAL.equalsIgnoreCase(node.getNodeType()) && "NO".equalsIgnoreCase(node.getCondition());
				AbsHiveNodeHandle handle = HiveNodeHandleFactory.getHiveNodeHandle(node, fromTable);
				WithTableModel withTable = handle.parseWithTableModel();
				withTableList.add(withTable);
			}
			String script = WithTableUtil.parseWithTableList(fromTable, withTableList);
			withTableList.add(new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_RESULT, script));
		} else {
			for(EngineNode node : flow) {
				//是否属于否条件节点
				boolean reverse = EngineUtil.NODE_TYPE_CONDITIONAL.equalsIgnoreCase(node.getNodeType()) && "NO".equalsIgnoreCase(node.getCondition());
				AbsHiveNodeHandle handle = HiveNodeHandleFactory.getHiveNodeHandle(node, fromTable);
				WithTableModel withTable = handle.parseWithTableModel();
				fromTable = withTable.getAlias();
				withTableList.add(withTable);
			}
		}
		return withTableList;
	}

	/**
	 * 
	 * 功能描述：结果表查询字段
	 *
	 * @author  zhangly
	 *
	 * @param isDetail
	 * @return
	 * @throws Exception
	 */
	protected Map<String, String> parseFieldMap(boolean isDetail) throws Exception {
		Map<String, String> fieldMap = new LinkedHashMap<String, String>();
		String format = "concat_ws('_', '%s', '%s', x.visitid)";
		if(isDetail) {
			//明细层级违规
			format = "concat_ws('_', '%s', '%s', y.itemcode, x.visitid)";
		}
		format = String.format(format, batch.getBatchId(), formalCase.getCaseId());
		//主键字段
		fieldMap.put("ID", format);
		//dwb_master_info表字段
		for(Map.Entry<String, String> entry : EngineCaseServiceImpl.CASE_FIELD_MAPPING.entrySet()) {
			fieldMap.put(entry.getKey(), "x."+entry.getValue());
		}
		if(isDetail) {
			//明细层级违规，dws_patient_1visit_itemsum表字段
			for(Map.Entry<String, String> entry : AbsHiveRuleHandle.DWS_CHARGEDTL_FIELD_MAPPING.entrySet()) {
				fieldMap.put(entry.getKey(), "y."+entry.getValue());
			}
		}
		//常量字段
		SolrConstField constBean = this.getSolrConstField();
		if(!isDetail) {
			//非明细层级违规
			//违规基金支出金额
			fieldMap.put("ACTION_MONEY", "x.FUNDPAY");
			fieldMap.put("MAX_ACTION_MONEY", "x.FUNDPAY");
			//违规金额
			fieldMap.put("MIN_MONEY", "x.TOTALFEE");
			fieldMap.put("MAX_MONEY", "x.TOTALFEE");
			constBean.addConst("ITEMCODE_SRC", formalCase.getCaseId());
			constBean.addConst("ITEMNAME_SRC", formalCase.getCaseName());
		}
		Class<?> clazz = SolrConstField.class;
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields) {
			if(field.isAnnotationPresent(SolrFieldAnnotation.class)) {
				 String methodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
				 Method method = clazz.getMethod(methodName);
				 Object object = method.invoke(constBean);
				 if(object!=null) {
					 String value = object.toString();
					 if(HiveJDBCUtil.isHive()) {
						 value = StringUtils.replace(value, "'", "\\'");
					 } else {
						 value = StringUtils.replace(value, "'", "''");
					 }
					 fieldMap.put(field.getAnnotation(SolrFieldAnnotation.class).value(), "'"+value+"'");
				 }
			}
		}		
		if(constBean.getConstMap()!=null) {
			for(Map.Entry<String, String> entry : constBean.getConstMap().entrySet()) {
				fieldMap.put(entry.getKey(), "'"+entry.getValue()+"'");
			}
		}
		return fieldMap;
	}
	
	protected SolrConstField getSolrConstField() {
		SolrConstField constBean = new SolrConstField();
		constBean.setCaseId(formalCase.getCaseId());
		constBean.setCaseName(formalCase.getCaseName());
		constBean.setActionId(formalCase.getActionId());
		constBean.setActionName(formalCase.getActionName());
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IApiDictService dictSV = context.getBean(IApiDictService.class);
		MedicalActionDict actionDict = dictSV.queryActionDict(formalCase.getActionId());
		if(actionDict!=null) {
			constBean.setActionName(actionDict.getActionName());
			constBean.setRuleLevel(actionDict.getRuleLevel());
		}
		constBean.setActionTypeId(formalCase.getActionType());
		constBean.setActionTypeName(formalCase.getActionTypeName());
		constBean.setActionDesc(formalCase.getActionDesc());		
        constBean.setRuleBasis(formalCase.getRuleBasis());
        constBean.setRuleGrade(formalCase.getRuleGrade());
        constBean.setRuleGradeRemark(formalCase.getRuleGradeRemark());       
        constBean.setBusiType(MedicalConstant.ENGINE_BUSI_TYPE_CASE);
        
		constBean.setProjectId(task.getProjectId());
		constBean.setProjectName(task.getProjectName());
		constBean.setBatchId(batch.getBatchId());
		constBean.setBatchName(batch.getBatchName());
		constBean.setGenDataTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		return constBean;
	}
}
