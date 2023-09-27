package com.ai.modules.dcmapping.service.impl;

import com.ai.modules.config.entity.MedicalAuditLogConstants;
import com.ai.modules.dcmapping.entity.DcMappingResultColumn;
import com.ai.modules.dcmapping.entity.DcMappingResultManual;
import com.ai.modules.dcmapping.entity.DcMappingResultTable;
import com.ai.modules.dcmapping.entity.DcMappingTask;
import com.ai.modules.dcmapping.mapper.DcMappingResultColumnMapper;
import com.ai.modules.dcmapping.mapper.DcMappingResultManualMapper;
import com.ai.modules.dcmapping.mapper.DcMappingResultTableMapper;
import com.ai.modules.dcmapping.mapper.DcMappingTaskMapper;
import com.ai.modules.dcmapping.service.IDcMappingTaskService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description: 采集映射任务信息表
 * @Author: jeecg-boot
 * @Date:   2022-04-18
 * @Version: V1.0
 */
@Service
public class DcMappingTaskServiceImpl extends ServiceImpl<DcMappingTaskMapper, DcMappingTask> implements IDcMappingTaskService {

    @Autowired
    DcMappingResultTableMapper tableMapper;

    @Autowired
    DcMappingResultColumnMapper columnMapper;

    @Autowired
    DcMappingResultManualMapper manualMapper;

    @Override
    @Transactional
    public void deleteByIds(String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        List<HashSet<String>> setList = MedicalAuditLogConstants.getIdSetList(idList,100);
        for(Set<String> strList:setList){
            //删除任务
            this.baseMapper.deleteBatchIds(strList);

            //删除table
            this.tableMapper.delete(new QueryWrapper<DcMappingResultTable>().in("TASK_ID",strList));

            //删除column
            this.columnMapper.delete(new QueryWrapper<DcMappingResultColumn>().in("TASK_ID",strList));

            //删除manual
            this.manualMapper.delete(new QueryWrapper<DcMappingResultManual>().in("TASK_ID",strList));
        }
    }
}
