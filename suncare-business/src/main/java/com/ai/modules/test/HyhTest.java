package com.ai.modules.test;

import org.apache.commons.lang.StringUtils;
import org.jeecg.common.util.DateUtils;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HyhTest {

    public static void main(String args[]) throws ParseException {
        System.out.println(getParameterFromUrl("jdbc:hive2://10.63.82.218:21050/medical_XIAOGAN","currentSchema"));


        List<String> month = getMonthBetweenDate("2021年03月06日","2022年11月06日");
        System.out.println(month);

        String rlikeStr="X线计算机体层\\\\\\(CT\\\\\\)平扫（同时增强扫描加收）";
        if(StringUtils.isBlank(rlikeStr)){

        }else{
            rlikeStr = StringUtils.replace(rlikeStr, "^","");
            rlikeStr = StringUtils.replace(rlikeStr, "$","");
            rlikeStr = StringUtils.replace(rlikeStr, "\\\\\\(", "(" );
            rlikeStr = StringUtils.replace(rlikeStr,"\\\\\\)", ")" );
            rlikeStr = StringUtils.replace(rlikeStr, "\\\\\\[","[" );
            rlikeStr = StringUtils.replace(rlikeStr,"\\\\\\]", "]" );
            rlikeStr = StringUtils.replace(rlikeStr,"\\\\*", "*" );
            rlikeStr = StringUtils.replace(rlikeStr,"\\\\+", "+" );
            rlikeStr = StringUtils.replace(rlikeStr,"\\\\.", "." );
            rlikeStr = StringUtils.replace(rlikeStr,"\\{", "{" );
            System.out.println(rlikeStr);
        }

    }

    public static String getParameterFromUrl(String url, String key) {
        HashMap<String, String> urlMap = new HashMap<String, String>();
        String queryString = StringUtils.substringAfter(url, "?");
        for (String param : queryString.split("&")) {
            urlMap.put(StringUtils.substringBefore(param, "="), StringUtils.substringAfter(param, "="));
        }
        return urlMap.get(key);
    }


    //获取两个时间之间的月份
    public static List<String> getMonthBetweenDate(String minDate,String maxDate) throws ParseException {
        ArrayList<String> result = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");//格式化为年月

        Calendar min = Calendar.getInstance();
        Calendar max = Calendar.getInstance();

        min.setTime(sdf.parse(minDate));
        min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);

        max.setTime(sdf.parse(maxDate));
        max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

        Calendar curr = min;
        while (curr.before(max)) {
            result.add(sdf.format(curr.getTime()));
            curr.add(Calendar.MONTH, 1);
        }

        return result;
    }

    public static void main11(String args[]){
        String sheetName = "1-丙泊酚中/长链脂肪乳";
        System.out.println(formatSheetName(sheetName));

    }

    //sheetName特殊字符处理
    public static String formatSheetName(String sheetName) {
        if(StringUtils.isBlank(sheetName)){
            sheetName = "空";
        }
        sheetName = sheetName.replaceAll("\\[","");
        sheetName = sheetName.replaceAll("]","");
        sheetName = sheetName.replaceAll("\\*","");
        sheetName = sheetName.replaceAll("\\?","");
        sheetName = sheetName.replaceAll("？","");
        sheetName = sheetName.replaceAll("/","");
        sheetName = sheetName.replaceAll("\\\\","");
        sheetName = sheetName.replaceAll(":","");
        if(sheetName.length()>30){
            sheetName = sheetName.substring(0,30);
        }
        return sheetName;
    }


    public static void main333(String args[]){

        //第1步： 实时取
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rSet = null;
        try {
            String sql="SELECT * from test001 ";
            conn = getDbConnection2("");
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
        }
    }

    private static Connection getDbConnection2(String dataname)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        String resourceType="greenplum";

        String className = "org.postgresql.Driver";
        String url =  "jdbc:postgresql://10.63.82.191:5432/test?public";
        String username = "dw_rw";
        String password = "Yxsj@123";
        if(!StringUtils.isBlank(password)){
            //password = DbDataEncryptUtil.dbDataDecryptString(password);
        }
        Class.forName(className);
        String logicDbname = url;
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(logicDbname,username,password);
        } catch(Exception e) {
            e.printStackTrace();
            //conn = DriverManager.getConnection(url,username,password);
        }
        return conn;
    }


    //获取数据源
    private static Connection getDbConnection(String dataname)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        String resourceType="hive";

        String className = "org.apache.hive.jdbc.HiveDriver";
        String url =  "jdbc:hive2://10.175.33.161:10000/medical_shangrao6";
        String username = "gbdp";
        String password = "SnNK9z50i0hiZSdG";
        if(!StringUtils.isBlank(password)){
            //password = DbDataEncryptUtil.dbDataDecryptString(password);
        }
        Class.forName(className);
        String logicDbname = "jdbc:hive2://10.2.74.3:21050/medical_jiaxiang2;auth=noSasl";
        Connection conn = null;
        if("hive".equals(resourceType)&& StringUtils.isNotBlank(logicDbname)){
            try{
                conn = DriverManager.getConnection(logicDbname,username,password);
            } catch(Exception e) {
                e.printStackTrace();
                //conn = DriverManager.getConnection(url,username,password);
            }
        }
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
                    System.out.println(colName.toUpperCase()+"::::"+rSet.getObject(j));
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
