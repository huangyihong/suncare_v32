package org.jeecg.common.util.dbencrypt;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.jeecg.common.util.encryption.AesEncryptUtil;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @since: 2022/3/9 16:21
 * 类型转换器，处理EncryptType类型，用于数据加解密
 **/
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(String.class)
public class EncryptTypeHandler extends BaseTypeHandler<String> {


    //存储是加密
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        if (StringUtils.isBlank(parameter)) {
            ps.setString(i, null);
            return;
        }

        //加密
        String encrypt = DbDataEncryptUtil.dbDataEncryptString(parameter);
        ps.setString(i, encrypt);
    }


    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        //从数据库查询并解密
        String dbStoreString = rs.getString(columnName);
        return DbDataEncryptUtil.dbDataDecryptString(dbStoreString);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        //从数据库查询并解密
        String dbStoreString = rs.getString(columnIndex);
        return DbDataEncryptUtil.dbDataDecryptString(dbStoreString);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        //从数据库查询并解密
        String dbStoreString = cs.getString(columnIndex);
        return DbDataEncryptUtil.dbDataDecryptString(dbStoreString);
    }
}