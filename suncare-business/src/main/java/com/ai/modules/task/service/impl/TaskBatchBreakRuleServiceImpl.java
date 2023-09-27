package com.ai.modules.task.service.impl;

import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.mapper.TaskBatchBreakRuleMapper;
import com.ai.modules.task.service.ITaskBatchBreakRuleService;
import com.ai.modules.task.vo.TaskBatchBreakRuleVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 批次规则关联
 * @Author: jeecg-boot
 * @Date:   2020-01-02
 * @Version: V1.0
 */
@Service
public class TaskBatchBreakRuleServiceImpl extends ServiceImpl<TaskBatchBreakRuleMapper, TaskBatchBreakRule> implements ITaskBatchBreakRuleService {

    @Override
    public List<TaskBatchBreakRuleVO> listByType(QueryWrapper<TaskBatchBreakRule> queryWrapper, String type, Boolean notHis) {
        if(notHis){
            return this.baseMapper.listByType(queryWrapper, type);
        } else {
            return this.baseMapper.listByTypeHis(queryWrapper, type);

        }
    }

    @Override
    public List<TaskBatchBreakRuleVO> listInFormalByType(String[] ruleIds, String type) {
        return this.baseMapper.listInFormalByType(ruleIds, type);
    }
}
