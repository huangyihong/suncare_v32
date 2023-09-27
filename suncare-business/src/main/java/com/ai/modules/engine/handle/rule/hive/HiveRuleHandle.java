/**
 * DrugSecondLineHandle.java	  V1.0   2020年11月4日 下午4:09:05
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.hive;

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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.MD5Util;
import org.jeecg.common.util.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ai.common.MedicalConstant;
import com.ai.modules.api.util.ApiOauthClientUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.SolrConstField;
import com.ai.modules.engine.handle.rule.SolrFieldAnnotation;
import com.ai.modules.engine.handle.rule.hive.model.GroupWithTableModel;
import com.ai.modules.engine.handle.rule.hive.model.JoinTableModel;
import com.ai.modules.engine.handle.rule.hive.model.JudgeWithTableScript;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.handle.rule.parse.hive.HiveRuleIgnoreNullParser;
import com.ai.modules.engine.model.ColumnType;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.service.impl.EngineCaseServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.JDBCUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.Hive2SolrMain;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;

/**
 * 
 * 功能描述：规则计算引擎（impala|gp模式）
 *
 * @author  zhangly
 * Date: 2020年11月4日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveRuleHandle extends AbsHiveRuleHandle {
	protected static final Logger logger = LoggerFactory.getLogger(HiveRuleHandle.class);	
	//规则对象
	protected MedicalRuleConfig rule;
	//规则条件
	protected List<MedicalRuleConditionSet> ruleConditionList;
	
	protected IEngineActionService engineActionService = SpringContextUtils.getApplicationContext().getBean(IEngineActionService.class);
	protected IApiDictService dictSV = SpringContextUtils.getApplicationContext().getBean(IApiDictService.class);
	
	public HiveRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource,
			MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, datasource);
		this.rule = rule;
		this.ruleConditionList = ruleConditionList;
	}
	
	/**
	 * 
	 * 功能描述：规则是否忽略执行
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年12月10日 上午9:28:47</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected boolean ignoreRun() {
		return ignoreRun(rule);
	}
	
	@Override
	public void generateUnreasonableAction() throws Exception {
		if(ignoreRun()) {
			//忽略运行
			return;
		}
		if(!HiveJDBCUtil.enabledStorageGp()) {
			String busiType = this.getBusiType();
			boolean slave = false;
			//删除临时solr表数据
			this.deleteTrailSolrByRule(batch.getBatchId(), rule.getRuleId(), busiType, slave);
			
			generateStorageSolr();
			
			//不合规行为汇总
			String[] fqs = new String[] {"RULE_ID:"+rule.getRuleId()};
			engineActionService.executeGroupBy(batch.getBatchId(), rule.getActionId(), fqs);
		} else {
			generateStorageGp();
		}
	}
	
	/**
	 * 
	 * 功能描述：hive模式
	 *
	 * @author  zhangly
	 *
	 * @throws Exception
	 */
	private void generateByHive() throws Exception {
		String path = HiveJDBCUtil.STORAGE_ROOT+"/"+datasource+"/"+batch.getBatchId()+"/"+rule.getRuleId();
		StringBuilder sb = new StringBuilder();
		sb.append("insert overwrite directory '").append(path).append("'");		
		sb.append(" ").append(this.masterInfoJoinDwsChargeSql());
		logger.info("hive sql:\n{}", sb.toString());
		HiveJDBCUtil.execute(sb.toString());
				
		Hive2SolrMain main = new Hive2SolrMain();
		//先将计算结果存储临时solr表
		String collection = SolrUtil.getSolrUrl(datasource)+"/MEDICAL_TRAIL_ACTION/update";
		main.execute(path, collection, true);
		
		if(!trail) {
			String busiType = this.getBusiType();
			//同步到结果表		
			this.syncUnreasonableActionFromTrailSolr(rule.getRuleId(), busiType, false);		
			//不合规行为汇总
			String[] fqs = new String[] {"RULE_ID:"+rule.getRuleId()};
			engineActionService.executeGroupBy(batch.getBatchId(), rule.getActionId(), fqs);
		}
	}
	
	/**
	 * 
	 * 功能描述：impala、gp模式计算结果存储到solr
	 *
	 * @author  zhangly
	 *
	 * @throws Exception
	 */
	private void generateStorageSolr() throws Exception {
		String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/" + rule.getRuleId() + ".json";
		String sql = this.masterInfoJoinDwsChargeSql(true);
		logger.info("\n规则:{ruleId:{},ruleLimit:{},itemCode:{},itemName:{}}\nhive sql:\n{}", rule.getRuleId(), rule.getRuleLimit(), rule.getItemCodes(), rule.getItemNames(), sql);
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		BufferedWriter fileWriter = null;
		try {
			conn = HiveJDBCUtil.getWarehouseConnection();
			stmt = conn.prepareStatement(sql);
	        rs = stmt.executeQuery();
	        Set<ColumnType> columnSet = new LinkedHashSet<ColumnType>();
	        ResultSetMetaData rsmd = rs.getMetaData();
	        for(int i=1,len=rsmd.getColumnCount(); i<=len; i++) {
	        	String columnName = rsmd.getColumnName(i);
	        	columnSet.add(new ColumnType(columnName, rsmd.getColumnTypeName(i)));
	        }
	        
	        long count = 0;
	        fileWriter = new BufferedWriter(
	                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
	        //写文件头
            fileWriter.write("[");
	        while(rs.next()) {
            	JSONObject jsonObject = this.parseJSONObject(rs, columnSet);
            	//logger.info(jsonObject.toJSONString());
		        try {
		            fileWriter.write(jsonObject.toJSONString());
		            fileWriter.write(',');
		        } catch (IOException e) {
		        }
            	count++;
            }
	        logger.info("\n规则:{ruleId:{},ruleLimit:{},itemCode:{},itemName:{}}\n计算结果数：{}", rule.getRuleId(), rule.getRuleLimit(), rule.getItemCodes(), rule.getItemNames(), count);
            // 文件尾
            fileWriter.write("]");
            fileWriter.flush();
            if(count>0) {
            	//导入solr
                SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
                //不合规行为汇总
    			String[] fqs = new String[] {"RULE_ID:"+rule.getRuleId()};
    			engineActionService.executeGroupBy(batch.getBatchId(), rule.getActionId(), fqs);
            } else {
            	//删除文件
                File file = new File(importFilePath);
                if(file.exists()) {
                	file.delete();
                }
            }
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
	
	/**
	 * 
	 * 功能描述：impala、gp模式计算结果存储到gp
	 *
	 * @author  zhangly
	 *
	 * @throws Exception
	 */
	private void generateStorageGp() throws Exception {
		String sql = this.masterInfoJoinDwsChargeSql(true);
		logger.info("\n规则:{ruleId:{},ruleLimit:{},itemCode:{},itemName:{}}\nhive sql:\n{}", rule.getRuleId(), rule.getRuleLimit(), rule.getItemCodes(), rule.getItemNames(), sql);
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		Connection targetConn = null;
		Statement targetStmt = null;
		try {
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
	        long count = 0;
	        LocalDateTime startTime = LocalDateTime.now();
	        while(rs.next()) {
            	JSONObject jsonObject = this.parseJSONObject(rs, columnSet);
            	//logger.info(jsonObject.toJSONString());
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
			logger.info("导入gp耗时： "+minutes +"分钟，"+ seconds % 60+"秒 。");
            logger.info("\n规则:{ruleId:{},ruleLimit:{},itemCode:{},itemName:{}}\n计算结果数：{}", rule.getRuleId(), rule.getRuleLimit(), rule.getItemCodes(), rule.getItemNames(), count);            
		} catch(Exception e) {		
			throw e;
		} finally {
			HiveJDBCUtil.destroy(rs, conn, stmt);
			JDBCUtil.destroy(targetConn, targetStmt);
		}
	}
	
	/**
	 * 
	 * 功能描述：解析每一行ResultSet
	 *
	 * @author  zhangly
	 *
	 * @param rs
	 * @param columnSet
	 * @return
	 * @throws Exception
	 */
	protected JSONObject parseJSONObject(ResultSet rs, Set<ColumnType> columnSet) throws Exception {
		JSONObject jsonObject = new JSONObject();
		for(ColumnType bean : columnSet) {
			String column = bean.getColumnName();
    		Object value = rs.getObject(column);
    		if(value!=null && !"".equals(value.toString())) {
    			if("id".equalsIgnoreCase(column)) {
    				jsonObject.put("id", value);
    			} else {
    				jsonObject.put(column.toUpperCase(), value);
    			}
    		}
    	}
    	//id生成策略
	    String template = "${batchId}_${ruleId}_${itemCode}_${visitid}";
        Properties properties = new Properties();
        properties.put("batchId", batch.getBatchId());
        properties.put("ruleId", rule.getRuleId());
        properties.put("itemCode", jsonObject.get("ITEMCODE"));
        properties.put("visitid", jsonObject.get("VISITID"));
        template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
        String id = MD5Util.MD5Encode(template, "UTF-8");
        jsonObject.put("id", id);
        jsonObject.put("GEN_DATA_TIME", DateUtils.now());
        jsonObject.put("PROJECT_ID", task.getProjectId());
        jsonObject.put("PROJECT_NAME", task.getProjectName());
        jsonObject.put("BATCH_ID", batch.getBatchId());
        jsonObject.put("TASK_BATCH_NAME", batch.getBatchName());
        
        //违规金额、基金支出金额保留2位小数
        Set<String> set = new HashSet<String>();
        set.add("ACTION_MONEY");
        set.add("MAX_ACTION_MONEY");
        set.add("MIN_MONEY");
        set.add("MAX_MONEY");
        for(String field : set) {
        	Object object = jsonObject.get(field);
        	if(object!=null && StringUtils.isNotBlank(object.toString())) {
        		BigDecimal value = new BigDecimal(object.toString());
        		value = value.setScale(2, BigDecimal.ROUND_DOWN);
        		jsonObject.put(field, value);
        	}
        }
        return jsonObject;
	}
	
	/**
	 * 
	 * 功能描述：主体项目查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月13日 上午11:12:33</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected List<String> parseWhere() {
		List<String> wheres = new ArrayList<String>();
		if(this.isProjectGrp()) {
			//项目组
			String where = "itemcode in(select std.TREATCODE from medical_gbdp.STD_TREATGROUP std where TREATGROUP_CODE='%s')";
			where = String.format(where, rule.getItemCodes());
			wheres.add(where);
		} else {
			wheres.add("itemcode='"+rule.getItemCodes()+"'");
		}
		return wheres;
	}
	
	/**
	 * 
	 * 功能描述：查询字段
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2022年1月18日 上午10:39:22</p>
	 *
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected Map<String, String> parseUdfFieldMap() throws Exception {
		Map<String, String> fieldMap = new LinkedHashMap<String, String>();
		String format = "concat_ws('_', '%s', '%s', y.itemcode, y.visitid)";
		format = String.format(format, batch.getBatchId(), rule.getRuleId());
		//主键字段
		fieldMap.put("ID", format);
		//dwb_master_info表字段
		for(Map.Entry<String, String> entry : EngineCaseServiceImpl.CASE_FIELD_MAPPING.entrySet()) {
			fieldMap.put(entry.getKey(), "x."+entry.getValue());
		}
		//dws_patient_1visit_itemsum表字段
		for(Map.Entry<String, String> entry : DWS_CHARGEDTL_FIELD_MAPPING.entrySet()) {
			fieldMap.put(entry.getKey(), "y."+entry.getValue());
		}
		//常量字段
		SolrConstField constBean = this.getSolrConstField();
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
	
	/**
	 * 
	 * 功能描述：dwb_master_info关联dws_patient_1visit_itemsum
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月13日 上午11:12:02</p>
	 *
	 * @param impala 计算方式{true:impala模式, false:hive模式}
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected String masterInfoJoinDwsChargeSql(boolean impala) throws Exception {
		String dws = "dws_patient_1visit_itemsum";
		StringBuilder sql = new StringBuilder();
		sql.append("select * from ").append(dws);
		sql.append(" where ");
		//主体项目查询条件
		sql.append(StringUtils.join(this.parseWhere(), " and "));
		//默认查询条件
		sql.append(this.appendDefaultWhere());
		//公共查询条件
		sql.append(this.appendCommonWhere());
		String withSql = sql.toString();
		JoinTableModel joinTableModel = this.joinTables();
		if(joinTableModel.getWithTableList()!=null 
				&& joinTableModel.getWithTableList().size()>0) {
			sql.setLength(0);
			sql.append("with ").append(dws).append(" as(");
			sql.append(withSql);
			sql.append(")");
			for(WithTableModel model : joinTableModel.getWithTableList()) {
				sql.append("\n,");
				sql.append(model.getAlias()).append(" as(").append(model.getSql()).append(")");
			}
			sql.append("\n");
			sql.append("select * from ").append(dws).append(" a");
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
		sql.append("with ").append(AbsHiveRuleHandle.WITH_TABLE).append(" as(");
		sql.append(withSql);
		sql.append(")");
		String fromTable = AbsHiveRuleHandle.WITH_TABLE;
		//准入条件过滤后的表
		WithTableModel accessTable = this.parseAccessCondition();
		if(accessTable!=null) {
			sql.append("\n,");
			sql.append(accessTable.getAlias()).append(" as (").append(accessTable.getSql()).append(")");
			fromTable = AbsHiveRuleHandle.WITH_TABLE_ACCESS;
		}
		/*if(MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE.equals(rule.getRuleType())) {
			//合理用药规则，排除慢性病种病人
			WithTableModel chronicWithTable = this.filterChronicWithTableModel(fromTable);
			if(chronicWithTable!=null) {
				sql.append("\n,");
				sql.append(chronicWithTable.getAlias()).append(" as (").append(chronicWithTable.getSql()).append(")");
				fromTable = chronicWithTable.getAlias();
			}
		}*/
		//判断条件过滤后的表
		JudgeWithTableScript judgeWithTableScript = this.judgeWithTableScript(fromTable);
		if(judgeWithTableScript!=null) {
			sql.append("\n,");
			sql.append(judgeWithTableScript.getSql());
			fromTable = judgeWithTableScript.getResultTable();
		}
		sql.append("\n");
		Map<String, String> udfFieldMap = this.parseUdfFieldMap();
		if(impala) {
			sql.append("select ");
			//sql.append(" count(1) cnt");
			int index = 0;
			for(Map.Entry<String, String> entry : udfFieldMap.entrySet()) {
				index++;
				sql.append(entry.getValue()).append(" ").append(entry.getKey());
				if(index<udfFieldMap.size()) {
					sql.append(",");
				}
			}
		} else {
			sql.append("select default.udf_json_out(");
			sql.append("'");
			int index = 0;
			for(Map.Entry<String, String> entry : udfFieldMap.entrySet()) {
				index++;
				sql.append(entry.getKey());
				if(index<udfFieldMap.size()) {
					sql.append(",");
				}
			}
			sql.append("','ID',");
			index = 0;
			for(Map.Entry<String, String> entry : udfFieldMap.entrySet()) {
				index++;
				sql.append(entry.getValue());
				if(index<udfFieldMap.size()) {
					sql.append(",");
				}
			}
			sql.append(")");
		}
		sql.append(" from dwb_master_info x join ").append(fromTable).append(" y on x.visitid=y.visitid");
		sql.append(" where 1=1");
		//公共查询条件
		//sql.append(this.appendCommonWhere("x", false));
		if(joinTableModel.getMasterList()!=null) {
			for(String where : joinTableModel.getMasterList()) {
				sql.append(" and x.").append(where);
			}
		}
		//添加过滤掉指标为空值的条件
		HiveRuleIgnoreNullParser ignoreNullParser = new HiveRuleIgnoreNullParser(rule, ruleConditionList);
		List<String> ignoreNullWheres = ignoreNullParser.ignoreNullWhere();
  		if(ignoreNullWheres!=null && ignoreNullWheres.size()>0) {
  			sql.append(" and ").append(StringUtils.join(ignoreNullWheres, " and "));
  		}
		return sql.toString();
	}
	
	protected String masterInfoJoinDwsChargeSql() throws Exception {
		return masterInfoJoinDwsChargeSql(false);
	}
	
	/**
	 * 
	 * 功能描述：判断条件过滤的with脚本
	 *
	 * @author  zhangly
	 *
	 * @param fromTable
	 * @return
	 */
	protected JudgeWithTableScript judgeWithTableScript(String fromTable) throws Exception {
		StringBuilder sql = new StringBuilder();
		//判断条件过滤后的表（黑名单需要取反）
		WithTableModel judgeTable = this.parseJudgeCondition(fromTable);
		if(judgeTable!=null) {
			sql.append(judgeTable.getAlias()).append(" as (").append(judgeTable.getSql()).append(")");
			//创建结果表
			sql.append("\n,");
			sql.append(AbsHiveRuleHandle.WITH_TABLE_RESULT).append(" as (");
			sql.append("select * from ").append(fromTable);
			sql.append(" where visitid not in(select visitid from ").append(judgeTable.getAlias()).append(")");
			sql.append(")");
			return new JudgeWithTableScript(sql.toString(), AbsHiveRuleHandle.WITH_TABLE_RESULT);
		}
		return null;
	}
	
	/**
	 * 
	 * 功能描述：项目关联表过滤条件
	 *
	 * @author  zhangly
	 *
	 * @return
	 * @throws Exception
	 */
	protected JoinTableModel joinTables() throws Exception {
		JoinTableModel result = new JoinTableModel();
		ProjectFilterWhereVO filterVO = engineActionService.filterCondition(task, false);
		if(filterVO!=null) {
			if((filterVO.isDiseaseFilter() && EngineUtil.existsDisease(ruleConditionList)) || filterVO.getTypeSet().contains("diseaseDiag")) {
	        	//关联疾病表
				if(filterVO.isDiseaseFilter() && EngineUtil.existsDisease(ruleConditionList)) {
					//疾病映射不全过滤
					StringBuilder sb = new StringBuilder();
					sb.append("select * from dwb_master_info x1");
					sb.append(" where not exists(select 1 from DWB_DIAG x2 where x1.visitid=x2.visitid and diseasename is null)");
					result.addWithTable(sb.toString());
				}
			}
			if(filterVO.getWhereList()!=null && filterVO.getWhereList().size()>0) {
				for(TaskCommonConditionSet record : filterVO.getWhereList()) {
					String condiType = record.getField();
					String value = record.getExt1();
					if("visittype".equals(condiType)) {
						//就诊类型
						String[] array = StringUtils.split(value, ",");
						for(String val : array) {
							result.addMasterWhere("VISITTYPE_ID not like '"+val+"%'");
						}
					} else if("payway".equals(condiType)) {
						//支付方式
						value = "'" + StringUtils.replace(value, ",", "','") + "'";
						result.addMasterWhere("PAYWAY_ID not in("+value+")");
					} else if("funSettleway".equals(condiType)) {
						//结算方式
						value = "'" + StringUtils.replace(value, ",", "','") + "'";
						result.addMasterWhere("FUN_SETTLEWAY_ID not in("+value+")");
					} else if("diseaseDiag".equals(condiType)) {
						//疾病诊断
						value = "'" + StringUtils.replace(value, ",", "','") + "'";
						StringBuilder sb = new StringBuilder();
						sb.append("select * from dwb_master_info x1");
						sb.append(" where not exists(select 1 from DWB_DIAG x2 where x1.visitid=x2.visitid and DISEASECODE in("+value+"))");
						result.addWithTable(sb.toString());
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 
	 * 功能描述：解析准入条件
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	protected WithTableModel parseAccessCondition() throws Exception {
		//准入条件
		List<GroupWithTableModel> accessWithTableList = this.parseAccessConditionGroupWithTableModel();
		if(accessWithTableList!=null && accessWithTableList.size()>0) {
			/*for(GroupWithTableModel model : accessWithTableList) {
				result.add(model.parseCondition());
			}*/
			if(accessWithTableList.size()==1) {
				GroupWithTableModel groupWithTable = accessWithTableList.get(0);
				WithTableModel bean = groupWithTable.parseCondition();
				bean.setAlias(AbsHiveRuleHandle.WITH_TABLE_ACCESS);
				return bean;
			} else {
				String sql = WithTableUtil.parseGroupWithTableList(AbsHiveRuleHandle.WITH_TABLE, accessWithTableList);
				return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_ACCESS, sql);
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * 功能描述：解析判断条件
	 *
	 * @author  zhangly
	 *
	 * @param fromTable
	 * @return
	 */
	protected WithTableModel parseJudgeCondition(String fromTable) throws Exception {
		//判断条件
		List<GroupWithTableModel> judgeWithTableList = this.parseJudgeConditionGroupWithTableModel(fromTable);
		if(judgeWithTableList!=null && judgeWithTableList.size()>0) {
			if(judgeWithTableList.size()==1) {
				GroupWithTableModel groupWithTable = judgeWithTableList.get(0);
				WithTableModel bean = groupWithTable.parseCondition();
				bean.setAlias(AbsHiveRuleHandle.WITH_TABLE_JUDGE);
				return bean;
			} else {
				String sql = WithTableUtil.parseGroupWithTableList(fromTable, judgeWithTableList);
				return new WithTableModel(AbsHiveRuleHandle.WITH_TABLE_JUDGE, sql);
			}
		}
		return null;
	}
	
	/**
	 * 
	 * 功能描述：准入条件
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	private List<GroupWithTableModel> parseAccessConditionGroupWithTableModel() {
		List<GroupWithTableModel> groupWithTableList = new ArrayList<GroupWithTableModel>();
		String type = "access";
		List<List<MedicalRuleConditionSet>> accessGrpList = this.parseCondition(ruleConditionList, type, null);
		if(accessGrpList!=null) {
    		int group = 0; //组序号
    		for(List<MedicalRuleConditionSet> judgeList : accessGrpList) {
    			GroupWithTableModel bean = new GroupWithTableModel(group, type, rule, judgeList);
    			groupWithTableList.add(bean);
    			group++;
    		}
    	}
    	return groupWithTableList;
	}
	
	/**
	 * 
	 * 功能描述：判断条件
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	private List<GroupWithTableModel> parseJudgeConditionGroupWithTableModel(String fromTable) {
		List<GroupWithTableModel> groupWithTableList = new ArrayList<GroupWithTableModel>();
		String type = "judge";
		//限定条件
    	Set<String> exclude = new HashSet<String>();
    	exclude.add("fitTimeRange");
    	exclude.add("reviewHisDisease");
    	exclude.add("reviewHisItem");
    	exclude.add("excludeInHosp");
    	List<List<MedicalRuleConditionSet>> judgeGrpList = this.parseCondition(ruleConditionList, type, exclude);
    	if(judgeGrpList!=null) {
    		int group = 0; //组序号
    		boolean isPatient = this.isPatient();
    		for(List<MedicalRuleConditionSet> judgeList : judgeGrpList) {
    			GroupWithTableModel bean = new GroupWithTableModel(group, type, rule, judgeList, fromTable);
    			bean.setPatient(isPatient);
    			groupWithTableList.add(bean);
    			group++;
    		}
    	}
    	return groupWithTableList;
	}
	
	/**
	 * 
	 * 功能描述：合理用药排除慢性病种病人
	 *
	 * @author  zhangly
	 *
	 * @param fromTable
	 * @return
	 */
	private WithTableModel filterChronicWithTableModel(String fromTable) {
		Set<String> exclude = new HashSet<String>();
    	exclude.add("fitTimeRange");
    	exclude.add("reviewHisDisease");
    	exclude.add("reviewHisItem");
    	exclude.add("excludeInHosp");
    	List<MedicalRuleConditionSet> judgeList = this.parseConditionList("judge", exclude);
		Set<String> codeSet = new HashSet<String>();
		for (MedicalRuleConditionSet bean : judgeList) {
			if (StringUtils.isNotBlank(bean.getExt2())) {
				String code = bean.getExt2();
				code = StringUtils.replace(code, ",", "|");
				codeSet.add(code);
			}			
		}
		if (codeSet.size()==0) {
			return null;
		}
		String codes = "('"+StringUtils.join(codeSet, "','")+"')";
		StringBuilder sb = new StringBuilder();
		sb.append("select * from ").append(fromTable);
		sb.append(" where ");
		sb.append(fromTable).append(".clientid not in(select clientid from DWB_CHRONIC_PATIENT x1 join STD_DIAGGROUP x2 on x1.CHRONICDIS_CODE=x2.DISEASECODE");
		sb.append(" where x2.DIAGGROUP_CODE in").append(codes);
		sb.append(" or x1.CHRONICDIS_CODE in").append(codes);
		sb.append(")");
		String alias = AbsHiveRuleHandle.WITH_TABLE_ACCESS.concat("_chronic");
		return new WithTableModel(alias, sb.toString());
	}
	
	/**
	 * 
	 * 功能描述：公共查询条件（包含：数据时间范围、项目地、数据源、医疗机构等）
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2022年1月12日 上午9:33:09</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected String appendCommonWhere(String alias) throws Exception {
		StringBuilder sql = new StringBuilder();
		//项目的数据来源限制
		if (StringUtils.isNotBlank(task.getEtlSource())) {
			sql.append(" and $alias.etl_source='").append(task.getEtlSource()).append("'");
		}
		//批次的数据来源限制
		if (StringUtils.isNotBlank(batch.getEtlSource())) {
			sql.append(" and $alias.etl_source='").append(batch.getEtlSource()).append("'");
		}
		String project_startTime = MedicalConstant.DEFAULT_START_TIME;
		String project_endTime = MedicalConstant.DEFAULT_END_TIME;
		project_startTime = task.getDataStartTime()!=null ? DateUtil.format(task.getDataStartTime(), "yyyy-MM-dd") : project_startTime;
        project_endTime = task.getDataEndTime()!=null ? DateUtil.format(task.getDataEndTime(), "yyyy-MM-dd") : project_endTime;
        //项目的数据时间范围
  		sql.append(" and $alias.visitdate>='").append(project_startTime).append("'");
  		sql.append(" and $alias.visitdate<='").append(project_endTime).append(" 23:59:59'");
        String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
		String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
		batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
		batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
		//批次的数据时间范围
		sql.append(" and $alias.visitdate>='").append(batch_startTime).append("'");
		sql.append(" and $alias.visitdate<='").append(batch_endTime).append(" 23:59:59'");
		sql.append(" and $alias.project='").append(datasource).append("'");
		//规则的数据时间范围
        String rule_startTime = DateUtil.format(rule.getStartTime(), "yyyy-MM-dd");
        String rule_endTime = DateUtil.format(rule.getEndTime(), "yyyy-MM-dd");
        sql.append(" and $alias.visitdate>='").append(rule_startTime).append("'");
		sql.append(" and $alias.visitdate<='").append(rule_endTime).append(" 23:59:59'");
		//医疗机构范围限制
		if(StringUtils.isNotBlank(task.getDataOrgFilter())) {
			String value = task.getDataOrgFilter();
			value = "'" + StringUtils.replace(value, ",", "','") + "'";
			sql.append(" and $alias.orgid in(").append(value).append(")");
		}
		//自定义数据范围限制
		if(StringUtils.isNotBlank(batch.getCustomFilter())) {
			String value = batch.getCustomFilter();
			value = "'" + StringUtils.replace(value, ",", "','") + "'";
			sql.append(" and $alias.orgid in(").append(value).append(")");
		}
		if("1".equals(batch.getYbFundRm0())) {
			//过滤掉病例基金支出金额为0的数据
			sql.append(" and $alias.FUNDPAY>0");
		}
		String text = sql.toString();
		if(StringUtils.isBlank(alias)) {
			text = StringUtils.replace(text, "$alias.", "");
		} else {
			text = StringUtils.replace(text, "$alias", alias);
		}
		return text;
	}
	
	/**
	 * 
	 * 功能描述：公共查询条件（包含：数据时间范围、项目地、数据源、医疗机构等）
	 *
	 * @author  zhangly
	 *
	 * @return
	 * @throws Exception
	 */
	protected String appendCommonWhere() throws Exception {
		return appendCommonWhere(null);
	}
	
	protected String appendDefaultWhere() throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append(" and FUND_COVER>0");
		sql.append(" and SELFPAY_PROP_MIN>=0");
		sql.append(" and SELFPAY_PROP_MIN<1");
		sql.append(" and ITEM_QTY>0");
		sql.append(" and ITEM_AMT>0");
		return sql.toString();
	}
	
	/**
	 * 
	 * 功能描述：同步数据到结果表MEDICAL_UNREASONABLE_ACTION
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月13日 上午11:36:29</p>
	 *
	 * @param ruleId
	 * @param type
	 * @param slave
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected void syncUnreasonableActionFromTrailSolr(String ruleId, String type, boolean slave) throws Exception {
		// 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/" + ruleId + ".json";
		BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
    	SolrClient solrClient = null;
    	try {
    		//写文件头
            fileWriter.write("[");
    		
            List<String> conditionList = new ArrayList<String>();
        	conditionList.add("BUSI_TYPE:"+type);
        	conditionList.add("RULE_ID:"+ruleId);
        	conditionList.add("BATCH_ID:"+batch.getBatchId());

    		SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_TRAIL_ACTION, slave, (doc, index) -> {
    		    JSONObject json = new JSONObject();
    		    for(Entry<String, Object> entry : doc.entrySet()) {
        			if(!"_version_".equals(entry.getKey())) {
        				json.put(entry.getKey(), entry.getValue());
        			}
        		}
        		//json.put("id", UUIDGenerator.generate());
    		    //id生成策略
    		    String template = "${batchId}_${ruleId}_${itemCode}_${visitid}";
		        Properties properties = new Properties();
		        properties.put("batchId", batch.getBatchId());
		        properties.put("ruleId", ruleId);
		        properties.put("itemCode", doc.get("ITEMCODE"));
		        properties.put("visitid", doc.get("VISITID"));
		        template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
		        if(doc.get("MUTEX_ITEM_CODE")!=null
		        		&& StringUtils.isNotBlank(doc.get("MUTEX_ITEM_CODE").toString())) {
		        	//互斥规则特殊处理
		        	String mutex_item_code = doc.get("MUTEX_ITEM_CODE").toString();
		        	mutex_item_code = StringUtils.replace(mutex_item_code, "[", "");
		        	mutex_item_code = StringUtils.replace(mutex_item_code, "]", "");
		        	template = template.concat("_").concat(mutex_item_code);
		        }
		        Set<String> dateLimitSet = new HashSet<String>();
		        dateLimitSet.add("freq2");//一日限频次
		        dateLimitSet.add("dayUnfitGroups1");//收费合规-一日互斥
		        dateLimitSet.add("YRCFSF1");//合理诊疗-一日互斥
		        if(rule.getRuleLimit()!=null && dateLimitSet.contains(rule.getRuleLimit())
		        		&& doc.get("CHARGEDATE")!=null
		        		&& StringUtils.isNotBlank(doc.get("CHARGEDATE").toString())) {
		        	String time = doc.get("CHARGEDATE").toString();
		        	template = template.concat("_").concat(time);
		        }
		        String id = MD5Util.MD5Encode(template, "UTF-8");
		        json.put("id", id);
        		json.put("GEN_DATA_TIME", DateUtils.now());
                json.put("PROJECT_ID", task.getProjectId());
                json.put("PROJECT_NAME", task.getProjectName());
                json.put("BATCH_ID", batch.getBatchId());
                json.put("TASK_BATCH_NAME", batch.getBatchName());
                //违规金额、基金支出金额保留2位小数
		        Set<String> set = new HashSet<String>();
		        set.add("ACTION_MONEY");
		        set.add("MAX_ACTION_MONEY");
		        set.add("MIN_MONEY");
		        set.add("MAX_MONEY");
		        for(String field : set) {
		        	Object object = json.get(field);
		        	if(object!=null && StringUtils.isNotBlank(object.toString())) {
		        		BigDecimal value = new BigDecimal(object.toString());
		        		value = value.setScale(2, BigDecimal.ROUND_DOWN);
		        		json.put(field, value);
		        	}
		        }
    			try {
		            fileWriter.write(json.toJSONString());
		            fileWriter.write(',');
		        } catch (IOException e) {
		        }
    		});

			// 文件尾
            fileWriter.write("]");
            fileWriter.flush();
            //导入solr
            SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if(fileWriter!=null) {
    			fileWriter.close();
    		}
    		if(solrClient!=null) {
        		solrClient.close();
        	}
    	}
    }
	
	protected SolrConstField getSolrConstField() {
		SolrConstField constBean = new SolrConstField();
		constBean.setRuleId(rule.getRuleId());
		constBean.setActionDesc(rule.getMessage());
		String ruleFName = rule.getRuleId() + "::" + rule.getItemNames();
        constBean.setRuleFname(ruleFName);
        constBean.setRuleBasis(rule.getRuleBasis());
        constBean.setBusiType(this.getBusiType());
        constBean.setRuleScope(rule.getRuleLimit());
        constBean.setRuleScopeName(rule.getActionName());
        constBean.setActionTypeId(rule.getActionType());
        if(StringUtils.isNotBlank(rule.getActionType())) {
			String desc = ApiOauthClientUtil.parseText("ACTION_TYPE", rule.getActionType());
			constBean.setActionTypeName(desc);
		}
        constBean.setActionId(rule.getActionId());
        constBean.setActionName(rule.getActionName());
        constBean.setRuleLimit(rule.getRuleLimit());
        constBean.setRuleGrade(rule.getRuleGrade());
        constBean.setRuleGradeRemark(rule.getRuleGradeRemark());
		MedicalActionDict actionDict = dictSV.queryActionDict(rule.getActionId());
		if(actionDict!=null) {
			constBean.setActionName(actionDict.getActionName());
			constBean.setRuleLevel(actionDict.getRuleLevel());
		}
		constBean.setProjectId(task.getProjectId());
		constBean.setProjectName(task.getProjectName());
		constBean.setBatchId(batch.getBatchId());
		constBean.setBatchName(batch.getBatchName());
		constBean.setGenDataTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		return constBean;
	}
	
	protected List<MedicalRuleConditionSet> parseConditionList(String type, Set<String> exclude) {
		List<MedicalRuleConditionSet> accessList = ruleConditionList.stream().filter(s->type.equals(s.getType())).collect(Collectors.toList());
		if(exclude!=null && exclude.size()>0) {
			accessList = accessList.stream().filter(s->!exclude.contains(s.getField())).collect(Collectors.toList());
		}
		return accessList;
	}
		
	protected String getBusiType() {
		return getBusiType(rule);
	}
	
	/**
	 * 
	 * 功能描述：是否检查病人以往历史病例
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	protected boolean isPatient() {
		List<MedicalRuleConditionSet> conditionList = ruleConditionList.stream().filter(s->"reviewHisDisease".equals(s.getField())
				||"reviewHisItem".equals(s.getField())).collect(Collectors.toList());
		if(conditionList!=null && conditionList.size()>0) {
			MedicalRuleConditionSet bean = conditionList.get(0);
			return "1".equals(bean.getExt1()) ? true : false;
		}
		return false;
	}
	
	/**
	 * 
	 * 功能描述：判断主体项目是否为项目组
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	protected boolean isProjectGrp() {
		String itemType = rule.getItemTypes();
		if(MedicalConstant.ITEM_PROJECTGRP.equals(itemType)) {
			return true;
		}
		return false;
	}
}
