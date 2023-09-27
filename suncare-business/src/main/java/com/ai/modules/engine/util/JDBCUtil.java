package com.ai.modules.engine.util;

import com.ai.modules.engine.model.ColumnType;
import com.ai.modules.system.entity.SysDatabase;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

@Component
public class JDBCUtil {
	protected static final Logger logger = LoggerFactory.getLogger(JDBCUtil.class);

	/**数据commit条数限制，设置为PAGE_LIMIT的倍数*/
	public final static int COMMIT_LIMIT = 50000;
	public final static int PAGE_LIMIT = 100;
	public final static int FETCH_SIZE = 1000;
	public final static int QUERY_TIMEOUT = 600;
	private static final String GP_DATASOURCE = "greenplum";
	private static JdbcTemplate jdbcTemplate;

    @Autowired
    public JDBCUtil(JdbcTemplate jdbcTemplate) {
    	JDBCUtil.jdbcTemplate = jdbcTemplate;
    }

	public static Connection getConnection() throws Exception {
		return getConnection(GP_DATASOURCE);
	}

	public static Connection getConnection(String ds) throws Exception {
		DynamicRoutingDataSource routing = (DynamicRoutingDataSource)jdbcTemplate.getDataSource();
		DruidDataSource datasource = (DruidDataSource)routing.getDataSource(ds);
		String driver = datasource.getDriverClassName();
		String url = datasource.getUrl();
		String username = datasource.getUsername();
		String password = datasource.getPassword();
		logger.info("driver-class-name:{}", driver);
		logger.info("jdbc-url:{}", url);
		logger.info("jdbc-username:{}", username);
		logger.info("jdbc-password:{}", password);
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, username, password);
		return conn;
	}

	//获取数据源 来源于数据源配置库表
	public static Connection getDbConnection(SysDatabase sysDatabase)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		String className = sysDatabase.getDbver();
		String url =  sysDatabase.getUrl();
		String username = sysDatabase.getDbUser();
		String password = sysDatabase.getDbPassword();
		if(!StringUtils.isBlank(password)){
			password = DbDataEncryptUtil.dbDataDecryptString(password);
		}
		Class.forName(className);
		Connection conn = DriverManager.getConnection(url,username,password);
		return conn;
	}

	public static boolean execute(String sql) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			logger.info("execute sql:\n{}", sql);
			stmt.execute(sql);
			return true;
		} catch(Exception e) {
			throw e;
		} finally {
			if(stmt!=null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if(conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static int executeUpdate(String sql) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			logger.info("hive sql:\n{}", sql);
			return stmt.executeUpdate(sql);
		} catch(Exception e) {
			throw e;
		} finally {
			if(stmt!=null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if(conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static String query(String sql, String field) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			logger.info("execute sql:\n{}", sql);
			rs = stmt.executeQuery(sql);
			if(rs.next()) {
				return rs.getString(field);
			}
			return null;
		} catch(Exception e) {
			throw e;
		} finally {
			if(stmt!=null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if(conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static void destroy(Connection conn, Statement stmt) {
		if(stmt!=null) {
			try {
				stmt.close();
			} catch (SQLException e) {}
		}
		if(conn!=null) {
			try {
				conn.close();
			} catch (SQLException e) {}
		}
	}

	public static void destroy(ResultSet rs, Connection conn, Statement stmt) {
		if(rs!=null) {
			try {
				rs.close();
			} catch (SQLException e) {}
		}
		destroy(conn, stmt);
	}

	/**
	 *
	 * 功能描述：批量数据解析生成insert into values脚本
	 *
	 * @author  zhangly
	 *
	 * @param datasource
	 * @param dataList
	 * @param columnSet
	 * @return
	 * @throws Exception
	 */
	public static String parseInsertIntoScript(String datasource, List<JSONObject> dataList, Set<ColumnType> columnSet) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into MEDICAL_UNREASONABLE_ACTION(");
		for(ColumnType bean : columnSet) {
			sb.append(bean.getColumnName()).append(",");
		}
		sb.append("project) values ");
		for(JSONObject jsonObject : dataList) {
			sb.append("(");
			for(ColumnType bean : columnSet) {
				String columnName = bean.getColumnName();
				String columnType = bean.getColumnType();
				columnName = "id".equalsIgnoreCase(columnName) ? "id" : columnName.toUpperCase();
				Object object = jsonObject.get(columnName);
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
			sb.append("'").append(datasource).append("'");
			sb.append("),");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	public static List<Map<String, Object>> getResultSet(ResultSet rSet) throws Exception {
		List<Map<String, Object>> rs=new ArrayList<Map<String, Object>>();
		try {
			while (rSet.next()) {
				int i = rSet.getMetaData().getColumnCount();
				Map<String, Object> row=new HashMap<String, Object>();
				for (int j = 1; j <= i; j++) {
					String colName=rSet.getMetaData().getColumnLabel(j);
					row.put(colName.toUpperCase(), rSet.getObject(j));
				}
				rs.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
		return rs;
	}

	public static List<Map<String, Object>> getResultByJdbc(SysDatabase sysDatabase,String sql){
		//第1步： 实时取
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rSet = null;
		try {
			conn = getDbConnection(sysDatabase);
			stmt = conn.prepareStatement(sql);
			rSet = stmt.executeQuery();
			List<Map<String, Object>> dataList = getResultSet(rSet);
			return dataList;
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			destroy(rSet, conn, stmt);
		}
		return null;
	}
}
