package com.ai.solr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HiveJDBCUtil {
	protected static final Logger logger = LoggerFactory.getLogger(HiveJDBCUtil.class);
	
	private static final String HIVE_DRIVER = "org.apache.hive.jdbc.HiveDriver";
	private static final String HIVE_CAPACITY_SMALL = "small";
	
	private static WarehouseProperty warehouseProperty;
    public static String FS_DEFAULT_NAME;
    public static String STORAGE_ROOT;
    private static String CAPACITY;

    @Autowired
    public HiveJDBCUtil(WarehouseProperty warehouseProperty) {
    	HiveJDBCUtil.warehouseProperty = warehouseProperty;
    }
    
    @Value("${engine.warehouse.hdfs.fs-default-name:hdfs://localhost:8020}")
    public void fsDefaultName(String fsDefaultName) {
    	HiveJDBCUtil.FS_DEFAULT_NAME = fsDefaultName;
    }
    @Value("${engine.warehouse.hdfs.storage-root:/tmp/solr/suncare}")
    public void storageRoot(String storageRoot) {
    	HiveJDBCUtil.STORAGE_ROOT = storageRoot;
    }
    @Value("${engine.warehouse.hdfs.capacity:small}")
    public void capacity(String capacity) {
    	HiveJDBCUtil.CAPACITY = capacity;
    }
    
    public static boolean isHive() {
    	return HIVE_DRIVER.equals(warehouseProperty.getDriverClassName());
    }
    
    public static boolean isSmall() {
    	return CAPACITY!=null && CAPACITY.equals(HIVE_CAPACITY_SMALL);
    }
    
    /**
     * 
     * 功能描述：计算引擎方式{true:gp, false:solr}
     *
     * @author  zhangly
     *
     * @return
     */
    public static boolean enabledProcessGp() {
    	return warehouseProperty.isEnabledProcessGp();
    }
    
    /**
     * 
     * 功能描述：计算结果存储方式{true:gp, false:solr}
     *
     * @author  zhangly
     *
     * @return
     */
    public static boolean enabledStorageGp() {
    	return warehouseProperty.isEnabledStorageGp();
    }
		
	public static Connection getConnection() throws Exception {
		Class.forName(warehouseProperty.getDriverClassName());
		String url = warehouseProperty.getUrl();
		String username = warehouseProperty.getUsername();
		String password = warehouseProperty.getPassword();
		logger.info("driver-class-name:{}", warehouseProperty.getDriverClassName());
		logger.info("jdbc-url:{}", url);
		logger.info("jdbc-username:{}", username);
		logger.info("jdbc-password:{}", password);
		Connection conn = DriverManager.getConnection(url, username, password);
		return conn;
	}
	
	public static Connection getWarehouseConnection() throws Exception {
		Class.forName(warehouseProperty.getDriverClassName());
		String url = warehouseProperty.getUrl();
		String username = warehouseProperty.getUsername();
		String password = warehouseProperty.getPassword();
		if(isHive()) {
			url = url.replace("10000", "21050");
			//url = url.concat(";auth=noSasl");
		}
		logger.info("driver-class-name:{}", warehouseProperty.getDriverClassName());
		logger.info("hive jdbc-url:{}", url);
		logger.info("hive jdbc-username:{}", username);
		logger.info("hive jdbc-password:{}", password);
		Connection conn = DriverManager.getConnection(url, username, password);
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
	
	public static void destroy(Connection conn, PreparedStatement stmt) {
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

	public static void destroy(ResultSet rs, Connection conn, PreparedStatement stmt) {
		if(rs!=null) {
			try {
				rs.close();
			} catch (SQLException e) {}
		}
		destroy(conn, stmt);
	}

	public static WarehouseProperty getWarehouseProperty() {
		return warehouseProperty;
	}
}
