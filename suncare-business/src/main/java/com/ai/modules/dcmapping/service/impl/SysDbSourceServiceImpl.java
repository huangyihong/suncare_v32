package com.ai.modules.dcmapping.service.impl;

import com.ai.modules.dcmapping.entity.SysDbSource;
import com.ai.modules.dcmapping.mapper.SysDbSourceMapper;
import com.ai.modules.dcmapping.service.ISysDbSourceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.stereotype.Service;
import parquet.org.apache.thrift.transport.TTransportException;

import java.net.ConnectException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 数据库数据源
 * @Author: jeecg-boot
 * @Date:   2022-04-18
 * @Version: V1.0
 */
@Service
public class SysDbSourceServiceImpl extends ServiceImpl<SysDbSourceMapper, SysDbSource> implements ISysDbSourceService {

    @Override
    public List<Map<String, Object>> getDbColumnList(String tableName, SysDbSource sysDbSource) {
        String sql = "";
        if("sqlserver".equals(sysDbSource.getDbType())){
            sql = "SELECT isnull(g.[value],a.name) COLUMN_NAME \n" +
                    "FROM syscolumns a\n" +
                    "left join systypes b on a.xusertype=b.xusertype \n" +
                    "inner join sysobjects d on a.id=d.id  and d.xtype in ('V','S','U') and  d.name<>'dtproperties' \n" +
                    "left join syscomments e on a.cdefault=e.id\n" +
                    "left join sys.extended_properties g on a.id=g.major_id and a.colid=g.minor_id \n" +
                    "left join sys.extended_properties f on d.id=f.major_id and f.minor_id=0 \n" +
                    "where d.name='" + tableName+ "' \n" +
                    "order by a.id,a.colorder";
        }
        if("oracle".equals(sysDbSource.getDbType())){
            sql  = "SELECT b.column_name COLUMN_NAME "
                    + ",b.data_type DATA_TYPE "
                    + ",b.data_length   DATA_LENGTH "
                    + ",nvl(a.comments,b.table_name) COMMENTS "
                    + ",b.table_name TABLE_NAME "
                    + ",b.DATA_TYPE  DATA_TYPE "
                    + ",b.DATA_PRECISION  DATA_PRECISION "
                    + ",b.DATA_SCALE DATA_SCALE "
                    + ",b.DEFAULT_LENGTH  DEFAULT_LENGTH "
                    + ",b.COLUMN_ID  COLUMN_ID "
                    + ",b.NULLABLE  NULLABLE "
                    + ",b.DATA_DEFAULT DATA_DEFAULT "
                    + "FROM all_tab_columns b "
                    + "left join all_col_comments a on a.column_name=b.column_name and a.table_name = b.table_name and  A.owner=B.owner "
                    + "where b.TABLE_NAME = '" + tableName.toUpperCase()+ "' order by b.COLUMN_ID " ;
        }


        List<Map<String, Object>> dataList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rSet = null;
        try {
            //获取数据源
            conn = getDbConnection(sysDbSource);
            stmt = conn.prepareStatement(sql);
            //stmt.setString(1, tableName);
            rSet = stmt.executeQuery();
            dataList = this.getResultSet(rSet);
            return dataList;
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
            } finally {
                rSet = null;
                stmt = null;
                conn = null;
            }
        }
        return dataList;
    }

    @Override
    public Map<String, Object> tableDataByPage(Page<List<Map<String, Object>>> page, SysDbSource sysDbSource, String tableName, String column) {
        String sql = "";

        if("sqlserver".equals(sysDbSource.getDbType())){
            sql = "select top "+page.getSize()+" *\n" +
                    "from (select row_number()\n" +
                    "over(order by "+column+" asc) as rownumber,*\n" +
                    "from "+tableName+") temp_row\n" +
                    "where rownumber>(("+page.getCurrent()+"-1)*"+page.getSize()+")";
        }else if("oracle".equals(sysDbSource.getDbType())){
            sql = "select * from (select t.*,rownum num from  "+tableName+" t) \n" +
                    "where num<=("+page.getCurrent()+"*"+page.getSize()+") and num>(("+page.getCurrent()+"-1)*"+page.getSize()+")";
        }

        long cnt = 0;
        List<Map<String, Object>> records = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rSet = null;
        try {
            //获取数据源
            conn = getDbConnection(sysDbSource);
            stmt = conn.prepareStatement("select count(1) as cnt from " + tableName +" where 1=1");
            rSet = stmt.executeQuery();
            while (rSet.next()) {
                cnt = rSet.getLong("cnt");
            }
            if(cnt>0){
                stmt = conn.prepareStatement(sql);
                rSet = stmt.executeQuery();
                records = this.getResultSet(rSet);
            }
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
            } finally {
                rSet = null;
                stmt = null;
                conn = null;
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total",cnt);
        data.put("records",records);
        return data;
    }

    @Override
    public Result<?> testDbConnection(SysDbSource sysDbSource) {
        Result resultVO = new Result();
        String resourceType = sysDbSource.getDbType();
        String username = sysDbSource.getDbUser();

        //解密数据库密码
        String password = DbDataEncryptUtil.dbDataDecryptString(sysDbSource.getDbPassword());
        String dbHost = sysDbSource.getDbHost();
        int dbPort = sysDbSource.getDbPort();
        String dbName = sysDbSource.getDbName();
        try{
            switch (resourceType) {
                case "sqlserver": {
                    String className = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                    String url = "jdbc:sqlserver://" + dbHost + ":" + dbPort + ";DatabaseName=" + dbName + "";
                     try {
                         Class.forName(className);
                         resultVO.setSuccess(false);
                         resultVO.setMessage("获取数据库连接异常！");
                         try (Connection connection = DriverManager.getConnection(url, username, password)) {
                             String sql = " select  1 ";
                             Statement statement = connection.createStatement();
                             ResultSet result = statement.executeQuery(sql);
                             result.close();
                             statement.close();
                             resultVO.setSuccess(true);
                             resultVO.setMessage("连接成功！");
                         }
                     } catch (ClassNotFoundException e) {
                         resultVO.setSuccess(false);
                         resultVO.setMessage("系统错误！");
                         return resultVO;
                     } catch (SQLException e) {
                         Throwable cause = e.getCause();
                         // ip或端口写错
                         if (cause instanceof ConnectException) {
                             resultVO.setSuccess(false);
                             resultVO.setMessage("资源IP或资源端口填写错误！");
                             return resultVO;
                         } else if (cause == null) {
                             // 用户名或者密码写错
                             if (1045 == e.getErrorCode()) {
                                 resultVO.setSuccess(false);
                                 resultVO.setMessage("用户名或密码错误！");
                                 return resultVO;
                             }

                             // 数据库不存在，可以创建数据库
                             if (1049 == e.getErrorCode()) {
                                 resultVO.setSuccess(true);
                                 resultVO.setMessage("连接成功，可以创建该数据库！");
                                 return resultVO;
                             }
                         } else {
                             resultVO.setSuccess(false);
                             resultVO.setMessage("链接sqlServer数据库错误，请检查链接信息是否正确");
                             break;
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                         resultVO.setSuccess(false);
                         resultVO.setMessage("获取数据库连接异常！");
                         break;
                     }
                    break;
                }
                case "oracle": {
                    String className = "oracle.jdbc.driver.OracleDriver";
                    String url = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + dbName + "";
                    try {
                        Class.forName(className);
                        resultVO.setSuccess(false);
                        resultVO.setMessage("获取数据库连接异常！");
                        try (Connection connection = DriverManager.getConnection(url, username, password)) {
                            resultVO.setSuccess(true);
                            resultVO.setMessage("连接成功！");
                        }
                    } catch (ClassNotFoundException e) {
                        resultVO.setSuccess(false);
                        resultVO.setMessage("系统错误！");
                        return resultVO;
                    } catch (SQLException e) {
                        Throwable cause = e.getCause();
                        // ip或端口写错
                        if (cause instanceof ConnectException) {
                            resultVO.setSuccess(false);
                            resultVO.setMessage("资源IP或资源端口填写错误！");
                            return resultVO;
                        } else if (cause == null) {
                            // 用户名或者密码写错
                            if (1017 == e.getErrorCode()) {
                                resultVO.setSuccess(false);
                                resultVO.setMessage("用户名或密码错误！");
                                return resultVO;
                            }

                            // 数据库不存在，可以创建数据库
                            if (12505 == e.getErrorCode()) {
                                resultVO.setSuccess(false);
                                resultVO.setMessage("连接成功，可以创建该数据库！");
                                return resultVO;
                            }

                            // 用户已存在
                            if (1920 == e.getErrorCode()) {
                                resultVO.setSuccess(false);
                                resultVO.setMessage("用户已存在！");
                                return resultVO;
                            }
                        } else {
                            resultVO.setSuccess(false);
                            resultVO.setMessage("链接oracle数据库错误，请检查链接信息是否正确");
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        resultVO.setSuccess(false);
                        resultVO.setMessage("获取数据库连接异常！");
                        break;
                    }
                    break;
                }
                case "hive": {
                    String className = "org.apache.hive.jdbc.HiveDriver";
                    String url = "jdbc:hive2://" + dbHost + ":" + dbPort + "/" + dbName + "";
                    try {
                        Class.forName(className);
                        resultVO.setSuccess(false);
                        resultVO.setMessage("获取数据库连接异常！");
                        try (Connection connection = DriverManager.getConnection(url, username, password)) {
                            Statement hiveStatement = connection.createStatement();
                            String sql = "show tables";
                            ResultSet result = hiveStatement.executeQuery(sql);
                            result.close();
                            hiveStatement.close();

                            resultVO.setSuccess(true);
                            resultVO.setMessage("连接成功！");
                        }
                    } catch (ClassNotFoundException e) {
                        resultVO.setSuccess(false);
                        resultVO.setMessage("系统错误！");
                        return resultVO;
                    } catch (SQLException e) {
                        Throwable cause = e.getCause();
                        // ip或端口写错
                        if (cause instanceof ConnectException) {
                            resultVO.setSuccess(false);
                            resultVO.setMessage("资源IP或资源端口填写错误！");
                            return resultVO;
                        } else if (cause == null) {
                            // 用户名或者密码写错
                            if (1045 == e.getErrorCode()) {
                                resultVO.setSuccess(false);
                                resultVO.setMessage("用户名或密码错误！");
                                return resultVO;
                            }

                            // 数据库不存在，可以创建数据库
                            if (1049 == e.getErrorCode()) {
                                resultVO.setSuccess(false);
                                resultVO.setMessage("连接成功，可以创建该数据库！");
                                return resultVO;
                            }

                            // 用户已存在
                            if (1920 == e.getErrorCode()) {
                                resultVO.setSuccess(false);
                                resultVO.setMessage("用户已存在！");
                                return resultVO;
                            }
                        } else if (cause instanceof TTransportException) {
                            resultVO.setSuccess(false);
                            resultVO.setMessage("hive数据库拒绝链接，请检查ip地址和端口是否正确！");
                            break;
                        } else {
                            resultVO.setSuccess(false);
                            resultVO.setMessage("链接hive数据库错误，请检查链接信息是否正确");
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        resultVO.setSuccess(false);
                        resultVO.setMessage("获取数据库连接异常！");
                        break;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultVO.setSuccess(false);
            resultVO.setMessage("获取数据库连接异常！");
        }
        return resultVO;
    }

    //获取数据源
    private Connection getDbConnection(SysDbSource sysDbSource)throws ClassNotFoundException, SQLException {
        Connection conn = null;
        String resourceType = sysDbSource.getDbType();
        String username = sysDbSource.getDbUser();
        String password = DbDataEncryptUtil.dbDataDecryptString( sysDbSource.getDbPassword());
        String dbHost = sysDbSource.getDbHost();
        int dbPort = sysDbSource.getDbPort();
        String dbName = sysDbSource.getDbName();
        switch (resourceType) {
            case "sqlserver": {
                String className = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                String url = "jdbc:sqlserver://" + dbHost + ":" + dbPort + ";DatabaseName=" + dbName + "";
                Class.forName(className);
                conn = DriverManager.getConnection(url, username, password);
                break;
            }
            case "oracle": {
                String className = "oracle.jdbc.driver.OracleDriver";
                String url = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + dbName + "";
                Class.forName(className);
                conn = DriverManager.getConnection(url, username, password);
                break;
            }
            //后续其他数据库
        }
        return conn;
    }

    private List<Map<String, Object>> getResultSet(ResultSet rSet) throws Exception {
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
