/**
 * YbChargeitemSumHandler.java	  V1.0   2023年2月8日 下午5:57:05
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.ybChargeSearch.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

import com.ai.modules.engine.util.PlaceholderResolverUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ai.modules.engine.model.ColumnType;
import com.ai.modules.engine.util.JDBCUtil;
import com.ai.modules.ybChargeSearch.service.IYbChargeitemSumHandler;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class YbChargeitemSumHandler implements IYbChargeitemSumHandler {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void syncYbChargeitemSumFromGp(String datasource) throws Exception {
		Connection greenplumConn = null;
		PreparedStatement greenplumStmt = null;
		ResultSet rs = null;
		Connection targetConn = null;
		Statement targetStmt = null;
		try {
			String sql = "truncate table yb_chargeitem_sum_%s";
        	sql = String.format(sql, datasource);
        	jdbcTemplate.execute(sql);
        	
			sql = "select item_month, item_type, orgname, itemname, fee from medical_gbdp.yb_chargeitem_sum where project='"+datasource+"'";
			greenplumConn = JDBCUtil.getConnection();
			greenplumStmt = greenplumConn.prepareStatement(sql);
	        rs = greenplumStmt.executeQuery();
	        
	        Set<ColumnType> columnSet = new LinkedHashSet<ColumnType>();
	        ResultSetMetaData rsmd = rs.getMetaData();
	        for(int i=1,len=rsmd.getColumnCount(); i<=len; i++) {
	        	String columnName = rsmd.getColumnName(i);
	        	columnSet.add(new ColumnType(columnName, rsmd.getColumnTypeName(i)));
	        }
	        
	        targetConn = jdbcTemplate.getDataSource().getConnection();
 	        targetConn.setAutoCommit(false);//设置为不自动提交
 	        targetStmt = targetConn.createStatement();
	        
	        int count = 0;
	        int limit = 1000;
        	List<String> lineList = new ArrayList<String>();
	        while(rs.next()) {
	        	count++;
	        	lineList.add(parseInsertIntoValueScript(rs, columnSet));
	        	if(lineList.size()==limit) {
	        		String script = parseInsertIntoScript(columnSet, lineList, datasource);
            		targetStmt.addBatch(script);
            		lineList.clear();
	        	}
	        	if(count%JDBCUtil.COMMIT_LIMIT==0) {
	        		targetStmt.executeBatch();
                    targetConn.commit();
                    targetStmt.clearBatch();
	        	}
	        }
	        if(lineList.size()>0) {
        		String script = parseInsertIntoScript(columnSet, lineList, datasource);
        		targetStmt.addBatch(script);
        		lineList.clear();
        		targetStmt.executeBatch();
                targetConn.commit();
                targetStmt.clearBatch();
        	}	        
		} catch(Exception e) {
			log.error("", e);
		} finally {
			JDBCUtil.destroy(rs, greenplumConn, greenplumStmt);
			JDBCUtil.destroy(targetConn, targetStmt);
		}
	}

	private String parseInsertIntoScript(Set<ColumnType> columnSet, List<String> values, String datasource) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into yb_chargeitem_sum_%s(");
		for(ColumnType bean : columnSet) {
			sb.append(bean.getColumnName()).append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(") values ");
		sb.append(StringUtils.join(values, ","));
		String sql = sb.toString();
		sql = String.format(sql, datasource);
		return sql;
	}
	
	private String parseInsertIntoValueScript(ResultSet rs, Set<ColumnType> columnSet) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(ColumnType bean : columnSet) {
			String columnName = bean.getColumnName();
			String columnType = bean.getColumnType();
    		Object object = rs.getObject(columnName);
			if(object==null) {
				sb.append("null");
			} else {
				Set<String> numberSet = new HashSet<String>();
				numberSet.add("int");
				numberSet.add("double");
				numberSet.add("float");
				numberSet.add("int4");
				numberSet.add("numeric");
				numberSet.add("decimal");
				numberSet.add("integer");
				if(numberSet.contains(columnType)) {
					sb.append(String.valueOf(object));
				} else {						
					sb.append("'").append(String.valueOf(object)).append("'");
				}
			}
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public void computeYbChargeitemSum(String datasource) throws Exception {
		Connection greenplumConn = null;
		Statement greenplumStmt = null;
		try {
			StringBuilder sb = new StringBuilder();
			Map<String, String> params = new HashMap<String, String>();
			params.put("project", datasource);
			//删除历史记录
			sb.append("delete from medical_gbdp.yb_chargeitem_sum where project='${project}'");
			String sql = PlaceholderResolverUtil.replacePlaceholders(sb.toString(), params);

			greenplumConn = JDBCUtil.getConnection();
			greenplumStmt = greenplumConn.createStatement();
			greenplumStmt.execute(sql);
			//医保收费项目sum
			sb.setLength(0);
			sb.append("insert into medical_gbdp.yb_chargeitem_sum(item_month, item_type, orgname, itemname, fee, project, item_class)");
			sb.append(" select item_month, 'yb', orgname, itemname, fee, '${project}', 'charge' from (");
			sb.append(" select substr(cast(prescripttime as varchar),1,7) item_month,orgname,itemname,sum(fee) fee from medical_${project}.src_yb_charge_detail a");
			sb.append(" where prescripttime is not null");
			sb.append(" and exists(select 1 from medical_gbdp.yb_chargeitem_checklist b where position(a.itemname in concat_ws('#', b.itemname, b.itemname1))>0 and b.item_type='yb')");
			sb.append(" group by substr(cast(prescripttime as varchar),1,7),orgname,itemname");
			sb.append(") t");
			sql = PlaceholderResolverUtil.replacePlaceholders(sb.toString(), params);
			greenplumStmt.execute(sql);
			//his收费项目
			sb.setLength(0);
			sb.append("insert into medical_gbdp.yb_chargeitem_sum(item_month, item_type, orgname, itemname, fee, project, item_class)");
			sb.append(" select item_month, 'his', orgname, itemname, fee, '${project}', 'charge' from (");
			sb.append(" select substr(cast(prescripttime as varchar),1,7) item_month,orgname,his_itemname itemname,sum(fee) fee from medical_${project}.src_yb_charge_detail a");
			sb.append(" where prescripttime is not null");
			sb.append(" and exists(select 1 from medical_gbdp.yb_chargeitem_checklist b where position(a.his_itemname in concat_ws('#', b.itemname, b.itemname1))>0 and b.item_type='his')");
			sb.append(" group by substr(cast(prescripttime as varchar),1,7),orgname,his_itemname");
			sb.append(") t");
			sql = PlaceholderResolverUtil.replacePlaceholders(sb.toString(), params);
			greenplumStmt.execute(sql);
			//医保药品sum
			sb.setLength(0);
			sb.append("insert into medical_gbdp.yb_chargeitem_sum(item_month, item_type, orgname, itemname, fee, project, item_class)");
			sb.append(" select item_month, 'yb', orgname, itemname, fee, '${project}', 'drug' from (");
			sb.append(" select substr(cast(prescripttime as varchar),1,7) item_month,orgname,itemname,sum(fee) fee from medical_${project}.src_yb_charge_detail a");
			sb.append(" where prescripttime is not null");
			sb.append(" and exists(select 1 from medical_gbdp.yb_charge_drug_rule b where position(a.itemname in b.drug_name)>0)");
			sb.append(" group by substr(cast(prescripttime as varchar),1,7),orgname,itemname");
			sb.append(") t");
			sql = PlaceholderResolverUtil.replacePlaceholders(sb.toString(), params);
			greenplumStmt.execute(sql);
		} catch(Exception e) {
			log.error("", e);
		} finally {
			JDBCUtil.destroy(greenplumConn, greenplumStmt);
		}
	}
}
