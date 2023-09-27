package com.ai.modules.task.mapper;

import java.util.List;

import com.ai.modules.task.vo.TaskBatchBreakRuleDelVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 违规模型详情
 * @Author: jeecg-boot
 * @Date:   2020-01-17
 * @Version: V1.0
 */
public interface TaskBatchBreakRuleDelMapper extends BaseMapper<TaskBatchBreakRuleDel> {
    List<TaskBatchBreakRuleDel> selectByBusiIds(String[] ids);

    IPage<TaskBatchBreakRuleDelVO> pageVo(Page<TaskBatchBreakRuleDel> page, @Param(Constants.WRAPPER) Wrapper queryWrapper, @Param("batchId") String batchId);
}
