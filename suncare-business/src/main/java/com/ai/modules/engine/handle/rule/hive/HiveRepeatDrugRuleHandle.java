/**
 * HiveRepeatDrugRuleHandle.java	  V1.0   2022年11月18日 上午11:26:10
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.MD5Util;

import com.ai.common.MedicalConstant;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.AbsRepeatDrugRuleHandle;
import com.ai.modules.engine.handle.rule.SolrConstField;
import com.ai.modules.engine.handle.rule.SolrFieldAnnotation;
import com.ai.modules.engine.handle.rule.hive.model.JoinTableModel;
import com.ai.modules.engine.handle.rule.hive.model.WithTableModel;
import com.ai.modules.engine.model.ColumnType;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.engine.service.impl.EngineCaseServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.JDBCUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;

/**
 * 重复用药规则计算引擎（impala|gp模式）
 * @author  zhangly
 * Date: 2022年11月18日
 */
public class HiveRepeatDrugRuleHandle extends AbsRepeatDrugRuleHandle {

	public HiveRepeatDrugRuleHandle(TaskProject task, TaskProjectBatch batch, List<MedicalYbDrug> drugList) {
		super(task, batch, drugList);
	}

	@Override
	public void generateUnreasonableAction() throws Exception {
		String actionId = "bhgxw-0012"; //重复用药
		MedicalActionDict actionDict = dictSV.queryActionDict(actionId);
    	if(actionDict==null) {
    		throw new Exception("未找到不合规行为编码=bhgxw-0012");
    	}
    	//同一剂型药品集合
        Map<String, Set<String>> dosageMap = this.dosageGrouping();
        if(dosageMap.size()==0) {
        	//不存在同一剂型多种药品，忽略运行
        	return;
        }
        
        if(!HiveJDBCUtil.enabledStorageGp()) {
        	long count = this.generateStorageSolr(dosageMap, actionDict);
            if(count>0) {
            	//不合规行为汇总
            	MedicalYbDrug drug = drugList.get(0);
        		String ruleId = drug.getParentCode();
    			String[] fqs = new String[] {"RULE_ID:"+ruleId};
    			engineActionService.executeGroupBy(batch.getBatchId(), actionDict.getActionId(), fqs);
            }
        } else {
        	this.generateStorageGp(dosageMap, actionDict);
        }
	}
	
	private long generateStorageSolr(Map<String, Set<String>> dosageMap, MedicalActionDict actionDict) throws Exception {
		MedicalYbDrug drug = drugList.get(0);
		String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/" + drug.getParentCode() + ".json";
		String sql = this.masterInfoJoinDwsChargeSql(true, dosageMap, actionDict);
		logger.info("\n规则:{parentCode:{}}\nhive sql:\n{}", drug.getParentCode(), sql);
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
            	JSONObject jsonObject = this.parseJSONObject(rs, columnSet, drug);
		        try {
		            fileWriter.write(jsonObject.toJSONString());
		            fileWriter.write(',');
		        } catch (IOException e) {
		        }
            	count++;
            }
            logger.info("\n规则:{parentCode:{}}计算结果数：{}", drug.getParentCode(), count);
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
            return count;
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
	
	private long generateStorageGp(Map<String, Set<String>> dosageMap, MedicalActionDict actionDict) throws Exception {
		MedicalYbDrug drug = drugList.get(0);
		String sql = this.masterInfoJoinDwsChargeSql(true, dosageMap, actionDict);
		logger.info("\n规则:{parentCode:{}}\nhive sql:\n{}", drug.getParentCode(), sql);
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
	        while(rs.next()) {
            	JSONObject jsonObject = this.parseJSONObject(rs, columnSet, drug);
            	count++;
            	dataList.add(jsonObject);
            	if(dataList.size()==JDBCUtil.PAGE_LIMIT) {
            		String script = JDBCUtil.parseInsertIntoScript(task.getDataSource(), dataList, columnSet);
            		targetStmt.addBatch(script);
            		dataList.clear();
            	}
		        if(count%JDBCUtil.COMMIT_LIMIT==0) {
					targetStmt.executeBatch();
                    targetConn.commit();
                    targetStmt.clearBatch();
				}
            }
            logger.info("\n规则:{parentCode:{}}计算结果数：{}", drug.getParentCode(), count);
            return count;
		} catch(Exception e) {		
			throw e;
		} finally {
			HiveJDBCUtil.destroy(rs, conn, stmt);
			JDBCUtil.destroy(targetConn, targetStmt);
		}
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
	protected String masterInfoJoinDwsChargeSql(boolean impala, Map<String, Set<String>> dosageMap, MedicalActionDict actionDict) throws Exception {		
		Set<String> itemcodeSet = new HashSet<String>();
		for(Map.Entry<String, Set<String>> entry : dosageMap.entrySet()) {
			itemcodeSet.addAll(entry.getValue());
		}
		StringBuilder sql = new StringBuilder();
		sql.append("select * from dws_patient_1visit_itemsum");
		sql.append(" where itemcode in('").append(StringUtils.join(itemcodeSet, "','")).append("')");
		//默认查询条件
		sql.append(this.appendDefaultWhere());
		//公共查询条件
		sql.append(this.appendCommonWhere(null));
		String withSql = sql.toString();
		
		sql.setLength(0);
		sql.append("with ").append(AbsHiveRuleHandle.WITH_TABLE).append(" as(");
		sql.append(withSql);
		sql.append(")");
		sql.append(this.repeatWithTableScript(dosageMap));
		sql.append("\n");
		Map<String, String> udfFieldMap = this.parseUdfFieldMap(actionDict);
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
		sql.append(" from dwb_master_info x join ").append(AbsHiveRuleHandle.WITH_TABLE_RESULT).append(" y on x.visitid=y.visitid");
		sql.append(" where 1=1");
		//项目的dwb_master_info查询条件
		JoinTableModel joinTableModel = this.joinTables();
		if(joinTableModel.getMasterList()!=null) {
			for(String where : joinTableModel.getMasterList()) {
				sql.append(" and x.").append(where);
			}
		}
		return sql.toString();
	}
	
	protected String repeatWithTableScript(Map<String, Set<String>> dosageMap) {
		List<WithTableModel> withTableList = new ArrayList<WithTableModel>();
		int groupNo = 0;
		for(Map.Entry<String, Set<String>> entry : dosageMap.entrySet()) {
			Set<String> drugSet = entry.getValue();
			withTableList.add(this.repeatWithTable(drugSet, groupNo));
			groupNo++;
		}
		StringBuilder sb = new StringBuilder();
		if(withTableList.size()==1) {
			//按剂型分组后仅有一组出现多种药品
			WithTableModel withTable = withTableList.get(0);
			sb.append("\n,");
			sb.append(AbsHiveRuleHandle.WITH_TABLE_RESULT).append(" as(").append(withTable.getSql()).append(")");
			return sb.toString();
		}
		for(WithTableModel withTable : withTableList) {
			sb.append("\n,");
			sb.append(withTable.getAlias()).append(" as(").append(withTable.getSql()).append(")");
		}
		sb.append("\n,");
		sb.append(AbsHiveRuleHandle.WITH_TABLE_RESULT).append(" as(");
		for(int i=0, len=withTableList.size(); i<len; i++) {
			WithTableModel withTable = withTableList.get(i);
			if(i>0) {
				sb.append("\n");
				sb.append(" union all ");
			}
			sb.append("select * from ").append(withTable.getAlias());
		}
		sb.append(")");
		return sb.toString();
	}
	
	protected WithTableModel repeatWithTable(Set<String> drugSet, int groupNo) {
		MedicalYbDrug drug = drugList.get(0);
		StringBuilder sql = new StringBuilder();
		//按天统计药品使用种类>1的药品
		String alias_day_kinds = "table_day_kinds_"+groupNo;
		sql.append(" with ").append(alias_day_kinds).append(" as (");
		sql.append("select x1.visitid,");
		if(HiveJDBCUtil.enabledProcessGp()) { 
			sql.append("to_char(x1.prescripttime,'yyyy-mm-dd') daytime");
		} else {
			sql.append("substr(x1.prescripttime,1,10) daytime");
		}
		sql.append(",count(distinct(x1.itemcode))");
		sql.append(" from dwb_charge_detail x1 join ").append(AbsHiveRuleHandle.WITH_TABLE).append(" x2 on x1.visitid=x2.visitid and x1.itemcode=x2.itemcode");
		sql.append(" where x1.itemcode in('").append(StringUtils.join(drugSet, "','")).append("')");
		sql.append(" group by x1.visitid,");
		if(HiveJDBCUtil.enabledProcessGp()) {
			sql.append("to_char(x1.prescripttime,'yyyy-mm-dd')");
		} else {
			sql.append("substr(x1.prescripttime,1,10)");
		}
		sql.append(" having count(1)>1)");
		//按天统计药品使用量、金额等
		String alias_day = "table_day_"+groupNo;
		sql.append("\n,");
		sql.append(alias_day).append(" as (");
		sql.append("select x1.visitid,x1.itemcode,x1.itemname,");
		if(HiveJDBCUtil.enabledProcessGp()) {
			sql.append("to_char(x1.prescripttime,'yyyy-mm-dd') daytime");
		} else {
			sql.append("substr(x1.prescripttime,1,10) daytime");
		}
		sql.append(",sum(x1.AMOUNT) AMOUNT,sum(x1.FEE) FEE,sum(x1.FUND_COVER) FUND_COVER");
		sql.append(" from dwb_charge_detail x1 join ").append(AbsHiveRuleHandle.WITH_TABLE).append(" x2 on x1.visitid=x2.visitid and x1.itemcode=x2.itemcode");
		sql.append(" where x1.itemcode in('").append(StringUtils.join(drugSet, "','")).append("')");
		sql.append(" and exists(select 1 from ").append(alias_day_kinds).append(" x3 where x1.visitid=x3.visitid");
		if(HiveJDBCUtil.enabledProcessGp()) {
			sql.append(" and to_char(x1.prescripttime,'yyyy-mm-dd')=x3.daytime");
		} else {
			sql.append(" and substr(x1.prescripttime,1,10)=x3.daytime");
		}
		sql.append(")");
		sql.append(" group by x1.visitid,x1.itemcode,x1.itemname,");
		if(HiveJDBCUtil.enabledProcessGp()) {
			sql.append("to_char(x1.prescripttime,'yyyy-mm-dd')");
		} else {
			sql.append("substr(x1.prescripttime,1,10)");
		}
		sql.append(")");
		//按天重复药品
		String alias_day_repeat = "table_day_repeat_"+groupNo;
		sql.append("\n,");
		sql.append(alias_day_repeat).append(" as(");
		sql.append("select x1.*,x2.itemcode repeat_itemcode,x2.itemname repeat_itemname,x2.amount repeat_amount,x2.fee repeat_fee,x2.fund_cover repeat_fund_cover from ").append(alias_day).append(" x1 join ").append(alias_day).append(" x2 on x1.visitid=x2.visitid and x1.daytime=x2.daytime");
		sql.append(" where x1.itemcode<>x2.itemcode");
		sql.append(")");
		//最大基金支出金额的药品作为白名单
		String alias_day_max = "table_day_max_"+groupNo;
		sql.append("\n,");
		sql.append(alias_day_max).append(" as (");
		sql.append("select visitid,daytime,max(fund_cover) fund_cover from ").append(alias_day);
		sql.append(" group by visitid,daytime");
		sql.append(")");
		//按天重复药品黑名单
		String alias_day_repeat_blacklist = "table_day_repeat_blacklist_"+groupNo;
		sql.append("\n,");
		sql.append(alias_day_repeat_blacklist).append(" as(");
		sql.append("select x1.* from ").append(alias_day_repeat).append(" x1 join ").append(alias_day_max).append(" x2 on x1.visitid=x2.visitid and x1.daytime=x2.daytime and x1.repeat_fund_cover=x2.fund_cover");
		sql.append(")");
		//分组内的结果表
		sql.append("\n");
		sql.append("select x1.*");
		sql.append(",x2.AMOUNT AI_OUT_CNT");
		sql.append(",x2.FEE MIN_MONEY,x2.FEE MAX_MONEY");
		sql.append(",x2.FUND_COVER MIN_ACTION_MONEY,x2.FUND_COVER MAX_ACTION_MONEY");
		sql.append(",x2.daytime BREAK_RULE_TIME");
		sql.append(",x2.daytime CHARGEDATE");
		sql.append(",x2.repeat_itemcode MUTEX_ITEM_CODE");
		sql.append(",concat(x2.repeat_itemcode,'").append(EngineUtil.SPLIT_KEY).append("',x2.repeat_itemname) MUTEX_ITEM_NAME");
		sql.append(",concat('[',x2.itemname,']与[',x2.repeat_itemname,']都属于','").append(drug.getParentName()).append("','，存在重复用药') BREAK_RULE_CONTENT");
		sql.append(" from ").append(AbsHiveRuleHandle.WITH_TABLE).append(" x1 join ").append(alias_day_repeat_blacklist).append(" x2");
		sql.append(" on x1.visitid=x2.visitid and x1.itemcode=x2.itemcode");
		sql.append(" where x1.itemcode in('").append(StringUtils.join(drugSet, "','")).append("')");
		String alias = AbsHiveRuleHandle.WITH_TABLE_RESULT+"_"+groupNo;
		return new WithTableModel(alias, sql.toString(), "OR");
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
	protected Map<String, String> parseUdfFieldMap(MedicalActionDict actionDict) throws Exception {
		MedicalYbDrug drug = drugList.get(0);
		Map<String, String> fieldMap = new LinkedHashMap<String, String>();
		String format = "concat_ws('_', '%s', '%s', y.itemcode, y.BREAK_RULE_TIME, y.visitid)";
		format = String.format(format, batch.getBatchId(), drug.getParentCode());
		//主键字段
		fieldMap.put("ID", format);
		//dwb_master_info表字段
		for(Map.Entry<String, String> entry : EngineCaseServiceImpl.CASE_FIELD_MAPPING.entrySet()) {
			fieldMap.put(entry.getKey(), "x."+entry.getValue());
		}
		//dws_patient_1visit_itemsum表字段
		for(Map.Entry<String, String> entry : AbsHiveRuleHandle.DWS_CHARGEDTL_FIELD_MAPPING.entrySet()) {
			fieldMap.put(entry.getKey(), "y."+entry.getValue());
		}
		//常量字段
		SolrConstField constBean = this.getSolrConstField(drug, actionDict);
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
		//重新设置违规金额、违规基金支出金额等字段取值
		fieldMap.put("ITEM_QTY", "y.AI_OUT_CNT");
		fieldMap.put("ITEM_AMT", "y.MIN_MONEY");
		fieldMap.put("AI_OUT_CNT", "y.AI_OUT_CNT");
		fieldMap.put("MIN_MONEY", "y.MIN_MONEY");
		fieldMap.put("MAX_MONEY", "y.MAX_MONEY");
		fieldMap.put("ACTION_MONEY", "y.MIN_ACTION_MONEY");
		fieldMap.put("MAX_ACTION_MONEY", "y.MAX_ACTION_MONEY");
		fieldMap.put("MUTEX_ITEM_CODE", "y.MUTEX_ITEM_CODE");
		fieldMap.put("MUTEX_ITEM_NAME", "y.MUTEX_ITEM_NAME");
		fieldMap.put("BREAK_RULE_CONTENT", "y.BREAK_RULE_CONTENT");
		fieldMap.put("CHARGEDATE", "y.BREAK_RULE_TIME");
		return fieldMap;
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
	private JoinTableModel joinTables() throws Exception {
		JoinTableModel result = new JoinTableModel();
		ProjectFilterWhereVO filterVO = engineActionService.filterCondition(task, false);
		if(filterVO!=null) {
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
						result.addMasterWhere("DISEASECODE not in("+value+")");
					}
				}
			}
		}
		if("1".equals(batch.getYbFundRm0())) {
			//过滤掉病例基金支出金额为0的数据
			result.addMasterWhere("FUNDPAY>0");
		}
		return result;
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
	private String appendCommonWhere(String alias) throws Exception {
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
		sql.append(" and $alias.project='").append(task.getDataSource()).append("'");
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
		String text = sql.toString();
		if(StringUtils.isBlank(alias)) {
			text = StringUtils.replace(text, "$alias.", "");
		} else {
			text = StringUtils.replace(text, "$alias", alias);
		}
		return text;
	}
	
	private String appendDefaultWhere() throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append(" and FUND_COVER>0");
		sql.append(" and SELFPAY_PROP_MIN>=0");
		sql.append(" and SELFPAY_PROP_MIN<1");
		sql.append(" and ITEM_QTY>0");
		sql.append(" and ITEM_AMT>0");
		return sql.toString();
	}
	
	protected SolrConstField getSolrConstField(MedicalYbDrug drug, MedicalActionDict actionDict) {
		SolrConstField constBean = new SolrConstField();
		constBean.setRuleId(drug.getParentCode());
		 String ruleFName = drug.getParentCode() + "::" + drug.getParentName();
		constBean.setRuleFname(ruleFName);
		String actionDesc = "同一患者同时开具2种以上药理作用相同的药物";
		if(StringUtils.isNotBlank(actionDesc)) {
			actionDesc = actionDict.getActionDesc();
		}
		constBean.setActionDesc(actionDesc);
		constBean.setRuleBasis("《医疗保障基金使用监督管理条例》第十五条“不得违反诊疗规范过度诊疗、过度检查、分解处方、超量开药、重复开药”；卫医管发〔2010〕28号《医院处方点评管理规范（试行）》“第十九条  有下列情况之一的，应当判定为超常处方：4.无正当理由为同一患者同时开具2种以上药理作用相同药物的。”");
        constBean.setBusiType(MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE);
        constBean.setActionTypeId(RULE_TYPE);
        constBean.setActionTypeName("不合理用药-重复用药");
        constBean.setRuleGrade(drug.getRuleGrade());
        constBean.setRuleGradeRemark(drug.getRuleGradeRemark());
        constBean.setActionId(actionDict.getActionId());
        constBean.setActionName(actionDict.getActionName());
		constBean.setRuleLevel(actionDict.getRuleLevel());
		
		constBean.setProjectId(task.getProjectId());
		constBean.setProjectName(task.getProjectName());
		constBean.setBatchId(batch.getBatchId());
		constBean.setBatchName(batch.getBatchName());
		constBean.setGenDataTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		return constBean;
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
	private JSONObject parseJSONObject(ResultSet rs, Set<ColumnType> columnSet, MedicalYbDrug drug) throws Exception {
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
	    String template = "${batchId}_${parentCode}_${itemCode}_${daytime}_${visitid}";
        Properties properties = new Properties();
        properties.put("batchId", batch.getBatchId());
        properties.put("parentCode", drug.getParentCode());
        properties.put("itemCode", jsonObject.get("ITEMCODE"));
        properties.put("daytime", jsonObject.get("CHARGEDATE"));
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
}
