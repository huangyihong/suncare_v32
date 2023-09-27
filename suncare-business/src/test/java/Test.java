import com.ai.solr.HiveJDBCUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test.java	  V1.0   2019年12月3日 下午3:37:04
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

public class Test {

	
	public static void main(String[] args) throws Exception {
		/*String text = "{\r\n" +
				"  \"responseHeader\":{\r\n" +
				"    \"zkConnected\":true,\r\n" +
				"    \"status\":0,\r\n" +
				"    \"QTime\":1249,\r\n" +
				"    \"params\":{\r\n" +
				"      \"q\":\"*:*\",\r\n" +
				"      \"json.facet\":\"{min:\\\"min(YEARAGE)\\\", max:\\\"max(YEARAGE)\\\"}\",\r\n" +
				"      \"rows\":\"0\",\r\n" +
				"      \"wt\":\"json\"}},\r\n" +
				"  \"response\":{\"numFound\":10861792,\"start\":0,\"maxScore\":1.0,\"docs\":[]\r\n" +
				"  },\r\n" +
				"  \"facets\":{\r\n" +
				"    \"count\":10861792,\r\n" +
				"    \"min\":-83,\r\n" +
				"    \"max\":244}}";
		JSONObject jsonObject = JSON.parseObject(text);
		System.out.println(jsonObject.getString("facets"));
		jsonObject = JSON.parseObject(jsonObject.getString("facets"));
		BigDecimal min = new BigDecimal(jsonObject.getString("min"));
		BigDecimal max = new BigDecimal(jsonObject.getString("max"));
		System.out.println(min+"~"+max);*/
		/*String dimDict = "{\"dim1\":\"WS364.1/CV07.10.003\", \"dim2\":\"WS364.1/CV07.10.002\"}";
		JSONObject jsonObject = JSON.parseObject(dimDict);
		System.out.println(jsonObject.get("dim2"));*/
		/*BigDecimal rate = new BigDecimal(75);
		rate = rate.divide(new BigDecimal(100));
		System.out.println(rate);
        BigDecimal min = new BigDecimal("12.5");
        min = min.multiply(rate);
        min = min.setScale(2, BigDecimal.ROUND_HALF_DOWN);
        System.out.println(min);*/



//		int [] data = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
//		int size = 10;
//		int fileCount = (int) Math.ceil(data.length/Float.parseFloat(size+""));//文件分割个数
//		for(int i=0;i<fileCount;i++){
//
//			int [] newData =Arrays.copyOfRange(data, i*size, (i+1)*size>data.length?data.length:(i+1)*size);
//			System.out.print(Arrays.toString(newData));
//		}

		        String resourceType="hive";



	/*	//第1步： 实时取
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rSet = null;
		try {
			String sql="SELECT orgname from ods_checkorg_list ";
			conn = getDbConnection("");
			stmt = conn.prepareStatement(sql);
			rSet = stmt.executeQuery();
			List<Map<String, Object>> dataList = getResultSet(rSet);

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			try {
				if (rSet != null) {
					rSet.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				rSet = null;
				stmt = null;
				conn = null;
			}
		}*/


	}

	//获取数据源
	private static Connection getDbConnection(String dataname)
			throws Exception {
		String className = "org.apache.hive.jdbc.HiveDriver";
		String url =  "jdbc:hive2://10.175.33.161:10000/medical_shangrao6";
		String username = "dataquality";
		String password = "RwKWa72RBW7TcF3B";

		Class.forName(className);
		String logicDbname = "jdbc:hive2://10.175.33.12:21050/medical_shangrao6";
		Connection conn = DriverManager.getConnection(logicDbname,username,password);
		return conn;
	}

	private static List<Map<String, Object>> getResultSet(ResultSet rSet) throws Exception {
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

}
