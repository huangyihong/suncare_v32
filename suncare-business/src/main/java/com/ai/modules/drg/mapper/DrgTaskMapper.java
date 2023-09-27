package com.ai.modules.drg.mapper;

import com.ai.modules.drg.entity.DrgTask;
import com.ai.modules.drg.vo.DrgTaskVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: drg任务表
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
public interface DrgTaskMapper extends BaseMapper<DrgTask> {

    IPage<DrgTaskVo> selectPageVO(Page<DrgTask> page, @Param(Constants.WRAPPER) Wrapper<DrgTask> wrapper);
}
