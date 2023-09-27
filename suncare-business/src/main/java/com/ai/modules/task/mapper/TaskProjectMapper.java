package com.ai.modules.task.mapper;

import com.ai.modules.task.vo.TaskProjectVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.task.entity.TaskProject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 任务项目
 * @Author: jeecg-boot
 * @Date:   2020-01-03
 * @Version: V1.0
 */
public interface TaskProjectMapper extends BaseMapper<TaskProject> {
    IPage<TaskProjectVO> selectPageVO(Page<TaskProject> page, @Param(Constants.WRAPPER) Wrapper<TaskProject> wrapper);
}
