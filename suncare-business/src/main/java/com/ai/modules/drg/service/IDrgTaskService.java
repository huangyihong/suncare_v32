package com.ai.modules.drg.service;

import com.ai.modules.drg.entity.DrgTask;
import com.ai.modules.drg.vo.DrgTaskVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: drg任务表
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
public interface IDrgTaskService extends IService<DrgTask> {
    IPage<DrgTaskVo> pageVO(Page<DrgTask> page, Wrapper<DrgTask> wrapper);

}
