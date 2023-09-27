package org.jeecg.config.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

/**
 * mybatis拦截器，自动注入创建人、创建时间、修改人、修改时间
 *
 * @Author scott
 * @Date 2019-01-19
 */
@Slf4j
@Component
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class MybatisInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        String sqlId = mappedStatement.getId();
        log.debug("------sqlId------" + sqlId);
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Object parameter = invocation.getArgs()[1];
        log.debug("------sqlCommandType------" + sqlCommandType);

        if (parameter == null) {
            return invocation.proceed();
        }
        if (SqlCommandType.INSERT == sqlCommandType) {
            Field[] fields = oConvertUtils.getAllFields(parameter);
            for (Field field : fields) {
                String fieldName = field.getName();
                log.debug("------field.name------" + fieldName);
                try {
                    //update-begin--Author:scott  Date:20190828 for：关于使用Quzrtz 开启线程任务， #465
                    // 获取登录用户信息
                    LoginUser sysUser;
                    try {
                        sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    } catch (Exception e) {
                        sysUser = null;
                    }
                    //update-end--Author:scott  Date:20190828 for：关于使用Quzrtz 开启线程任务， #465
                    if ("createUser".equals(fieldName) || "createBy".equals(fieldName) || "createStaff".equals(fieldName)|| "createdBy".equals(fieldName)) {
                        field.setAccessible(true);
                        Object local_createBy = field.get(parameter);
                        field.setAccessible(false);
                        if (local_createBy == null || "".equals(local_createBy)) {
                            if (sysUser != null) {
                                field.setAccessible(true);
                                field.set(parameter, sysUser.getUsername());
                                field.setAccessible(false);

                            }
                        }
                        // 注入创建时间
                    } else if ("createTime".equals(fieldName)||"createdTime".equals(fieldName) ) {
                        field.setAccessible(true);
                        Object local_createDate = field.get(parameter);
                        field.setAccessible(false);
                        if (local_createDate == null || "".equals(local_createDate)) {
                            field.setAccessible(true);
                            field.set(parameter, new Date());
                            field.setAccessible(false);
                        }
                        //注入创建人姓名
                    } else if ("createUsername".equals(fieldName) || "createStaffName".equals(fieldName) || "createdByName".equals(fieldName)) {
                        field.setAccessible(true);
                        Object local_createUserName = field.get(parameter);
                        field.setAccessible(false);
                        if (local_createUserName == null || "".equals(local_createUserName)) {
                            if (sysUser != null) {
                                // 登录账号
                                field.setAccessible(true);
                                field.set(parameter, sysUser.getRealname());
                                field.setAccessible(false);
                            }

                        }
                        //注入部门编码
                    } else if ("sysOrgCode".equals(fieldName)) {
                        field.setAccessible(true);
                        Object local_sysOrgCode = field.get(parameter);
                        field.setAccessible(false);
                        if (local_sysOrgCode == null || local_sysOrgCode.equals("")) {
                            if (sysUser != null) {
                                field.setAccessible(true);
                                field.set(parameter, sysUser.getOrgCode());
                                field.setAccessible(false);
                            }
                        }
                    } else if ("updateUser".equals(fieldName) || "updateBy".equals(fieldName) || "updateStaff".equals(fieldName)|| "updatedBy".equals(fieldName)) {
                        field.setAccessible(true);
                        Object local_updateBy = field.get(parameter);
                        field.setAccessible(false);
                        if (local_updateBy == null || "".equals(local_updateBy)) {
                            if (sysUser != null) {
                                field.setAccessible(true);
                                field.set(parameter, sysUser.getUsername());
                                field.setAccessible(false);
                            }
                        }
                    } else if ("updateUsername".equals(fieldName) || "updateStaffName".equals(fieldName) || "updatedByName".equals(fieldName)) {
                        field.setAccessible(true);
                        Object local_createUserName = field.get(parameter);
                        field.setAccessible(false);
                        if (local_createUserName == null || "".equals(local_createUserName)) {
                            if (sysUser != null) {
                                // 登录账号
                                field.setAccessible(true);
                                field.set(parameter, sysUser.getRealname());
                                field.setAccessible(false);
                            }

                        }
                        //注入部门编码
                    } else if ("updateTime".equals(fieldName)||"updatedTime".equals(fieldName)) {
                        field.setAccessible(true);
                        Object local_updateDate = field.get(parameter);
                        field.setAccessible(false);
                        if (local_updateDate == null || "".equals(local_updateDate)) {
                            field.setAccessible(true);
                            field.set(parameter, new Date());
                            field.setAccessible(false);
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        if (SqlCommandType.UPDATE == sqlCommandType) {
            Field[] fields = null;
            if (parameter instanceof ParamMap) {
                ParamMap<?> p = (ParamMap<?>) parameter;
                //update-begin-author:scott date:20190729 for:批量更新报错issues/IZA3Q--
                if (p.containsKey("et")) {
                    parameter = p.get("et");
                } else {
                    parameter = p.get("param1");
                }
                //update-end-author:scott date:20190729 for:批量更新报错issues/IZA3Q-

                //update-begin-author:scott date:20190729 for:更新指定字段时报错 issues/#516-
                if (parameter == null) {
                    return invocation.proceed();
                }
                //update-end-author:scott date:20190729 for:更新指定字段时报错 issues/#516-

                fields = oConvertUtils.getAllFields(parameter);
            } else {
                fields = oConvertUtils.getAllFields(parameter);
            }

            for (Field field : fields) {
                String fieldName = field.getName();
                log.debug("------field.name------" + fieldName);
                try {
                    LoginUser sysUser;
                    try {
                        sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    } catch (Exception e) {
                        sysUser = null;
                    }
                    if ("updateUser".equals(fieldName) || "updateBy".equals(fieldName) || "updateStaff".equals(fieldName)|| "updatedBy".equals(fieldName)) {
                        field.setAccessible(true);
                        Object local_updateBy = field.get(parameter);
                        field.setAccessible(false);
                        if (local_updateBy == null || "".equals(local_updateBy)) {
                            if (sysUser != null) {
                                field.setAccessible(true);
                                field.set(parameter, sysUser.getUsername());
                                field.setAccessible(false);
                            }
                        }
                    } else if ("updateUsername".equals(fieldName) || "updateStaffName".equals(fieldName) || "updatedByName".equals(fieldName)) {
                        field.setAccessible(true);
                        Object local_createUserName = field.get(parameter);
                        field.setAccessible(false);
                        if (local_createUserName == null || "".equals(local_createUserName)) {
                            if (sysUser != null) {
                                // 登录账号
                                field.setAccessible(true);
                                field.set(parameter, sysUser.getRealname());
                                field.setAccessible(false);
                            }

                        }
                        //注入部门编码
                    } else if ("updateTime".equals(fieldName)||"updatedTime".equals(fieldName)) {
                        field.setAccessible(true);
                        Object local_updateDate = field.get(parameter);
                        field.setAccessible(false);
                        if (local_updateDate == null || "".equals(local_updateDate)) {
                            field.setAccessible(true);
                            field.set(parameter, new Date());
                            field.setAccessible(false);
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // TODO Auto-generated method stub
    }

}
