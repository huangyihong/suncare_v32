package com.ai.modules.dcmapping.service;

import com.ai.modules.dcmapping.entity.SysDbSource;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;

import java.util.List;
import java.util.Map;

/**
 * @Description: 数据库数据源
 * @Author: jeecg-boot
 * @Date:   2022-04-18
 * @Version: V1.0
 */
public interface ISysDbSourceService extends IService<SysDbSource> {

    /**
     * 数据表字段查询
     * @param tableName
     * @param sysDbSource
     * @return
     */
    List<Map<String, Object>> getDbColumnList(String tableName, SysDbSource sysDbSource);

    /**
     * 数据表记录分页查询
     * @param page
     * @param sysDbSource
     * @param tableName
     * @param column
     * @return
     */
    Map<String,Object> tableDataByPage(Page<List<Map<String, Object>>> page, SysDbSource sysDbSource, String tableName, String column);

    /**
     * 测试连接
     * @param sysDbSource
     * @return
     */
    Result<?> testDbConnection(SysDbSource sysDbSource);
}
