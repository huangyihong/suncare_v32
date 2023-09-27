package com.ai.modules.task.mapper;

import java.util.List;

import com.ai.modules.task.vo.TaskBatchBreakRuleVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 批次规则关联
 * @Author: jeecg-boot
 * @Date:   2020-01-02
 * @Version: V1.0
 */
public interface TaskBatchBreakRuleMapper extends BaseMapper<TaskBatchBreakRule> {

    List<TaskBatchBreakRuleVO> listByType(@Param(Constants.WRAPPER) QueryWrapper<TaskBatchBreakRule> queryWrapper,@Param("type") String type);

    List<TaskBatchBreakRuleVO> listByTypeHis(@Param(Constants.WRAPPER) QueryWrapper<TaskBatchBreakRule> queryWrapper,
                                             @Param("type") String type);

    List<TaskBatchBreakRuleVO> listInFormalByType(@Param("ruleIds") String[] ruleIds, @Param("type") String type);
}
