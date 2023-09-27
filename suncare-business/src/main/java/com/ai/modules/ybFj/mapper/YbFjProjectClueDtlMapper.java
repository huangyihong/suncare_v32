package com.ai.modules.ybFj.mapper;

import com.ai.modules.ybFj.entity.YbFjProjectClueDtl;
import com.ai.modules.ybFj.vo.TaskClueVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 飞检项目线索明细
 * @Author: jeecg-boot
 * @Date:   2023-03-07
 * @Version: V1.0
 */
public interface YbFjProjectClueDtlMapper extends BaseMapper<YbFjProjectClueDtl> {

    TaskClueVo queryTaskClueVo(@Param(Constants.WRAPPER) Wrapper<YbFjProjectClueDtl> wrapper);
}
