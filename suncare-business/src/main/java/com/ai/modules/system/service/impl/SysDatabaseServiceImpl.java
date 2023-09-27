package com.ai.modules.system.service.impl;

import com.ai.modules.system.entity.SysDatabase;
import com.ai.modules.system.mapper.SysDatabaseMapper;
import com.ai.modules.system.service.ISysDatabaseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.postgresql.util.PSQLException;
import org.springframework.stereotype.Service;
import parquet.org.apache.thrift.transport.TTransportException;

import java.net.ConnectException;
import java.sql.*;

/**
 * @Description: 数据源配置
 * @Author: jeecg-boot
 * @Date:   2022-11-22
 * @Version: V1.0
 */
@Service
public class SysDatabaseServiceImpl extends ServiceImpl<SysDatabaseMapper, SysDatabase> implements ISysDatabaseService {

    @Override
    public Result<?> testDbConnection(SysDatabase sysDatabase) {
        Result resultVO = new Result();
        String resourceType = sysDatabase.getDbtype();
        String username = sysDatabase.getDbUser();

        //解密数据库密码
        String password = DbDataEncryptUtil.dbDataDecryptString(sysDatabase.getDbPassword());
        String className = sysDatabase.getDbver();
        String url = sysDatabase.getUrl();
        try{
            switch (resourceType) {
                case "sqlserver": {
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
                case "mysql": {
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
                            resultVO.setMessage("链接mysql数据库错误，请检查链接信息是否正确");
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
                case "postgresql":
                case "greenplum": {
                    try {
                        Class.forName(className);
                        resultVO.setSuccess(false);
                        resultVO.setMessage("获取数据库连接异常！");
                        try (Connection connection = DriverManager.getConnection(url, username, password)) {
                            String sql = " select 1 col";
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
                        } else if (cause instanceof PSQLException) {
                            resultVO.setSuccess(false);
                            resultVO.setMessage("用户名或密码错误！");
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
                                resultVO.setSuccess(true);
                                resultVO.setMessage("连接成功，可以创建该数据库！");
                                return resultVO;
                            }

                            // 用户已存在
                            if (1920 == e.getErrorCode()) {
                                resultVO.setSuccess(false);
                                resultVO.setMessage("用户已存在！");
                                return resultVO;
                            }

                            resultVO.setSuccess(false);
                            resultVO.setMessage(e.getMessage());
                            return resultVO;

                        } else if (cause instanceof TTransportException) {
                            resultVO.setSuccess(false);
                            resultVO.setMessage("greenplum数据库拒绝链接，请检查ip地址和端口是否正确！");
                            break;
                        } else {
                            resultVO.setSuccess(false);
                            resultVO.setMessage("链接greenplum数据库错误，请检查链接信息是否正确");
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

    @Override
    public SysDatabase getByDbname(String dbname) {
        SysDatabase bean = this.baseMapper.selectOne(new LambdaQueryWrapper<SysDatabase>().eq(SysDatabase::getDbname,dbname));
        return bean;
    }


}
