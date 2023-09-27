package com.ai.modules.dcmapping.service.impl;

import com.ai.modules.dcmapping.entity.DcMappingResultTable;
import com.ai.modules.dcmapping.mapper.DcMappingResultTableMapper;
import com.ai.modules.dcmapping.service.IDcMappingResultTableService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 采集映射表名称映射结果表
 * @Author: jeecg-boot
 * @Date:   2022-04-18
 * @Version: V1.0
 */
@Service
public class DcMappingResultTableServiceImpl extends ServiceImpl<DcMappingResultTableMapper, DcMappingResultTable> implements IDcMappingResultTableService {

    @Override
    public void relationTable(String taskId, String destTableName, String sourceTableName) {
        DcMappingResultTable bean = new DcMappingResultTable();
        bean.setIsRelation("0");
        QueryWrapper<DcMappingResultTable> queryWrapper =new QueryWrapper();
        queryWrapper.eq("TASK_ID",taskId);
        queryWrapper.eq("DEST_TABLE_NAME",destTableName);
        queryWrapper.ne("SOURCE_TABLE_NAME",sourceTableName);
        this.baseMapper.update(bean,queryWrapper);

        bean.setIsRelation("1");
        queryWrapper =new QueryWrapper();
        queryWrapper.eq("TASK_ID",taskId);
        queryWrapper.eq("DEST_TABLE_NAME",destTableName);
        queryWrapper.eq("SOURCE_TABLE_NAME",sourceTableName);
        this.baseMapper.update(bean,queryWrapper);
    }
}
