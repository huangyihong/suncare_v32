package com.ai.modules.ybFj.mapper;

import com.ai.modules.ybFj.dto.SyncClueDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueOnsite;
import com.ai.modules.ybFj.vo.StatOnsiteClueVo;
import com.ai.modules.ybFj.vo.StatStepClueVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 飞检项目线索现场核查
 * @Author: jeecg-boot
 * @Date:   2023-03-15
 * @Version: V1.0
 */
public interface YbFjProjectClueOnsiteMapper extends BaseMapper<YbFjProjectClueOnsite> {

    int insertOnsiteClue(SyncClueDto dto);

    StatOnsiteClueVo statisticsOnsiteClue(@Param("projectOrgId") String projectOrgId);

    StatStepClueVo statisticsStepClue(@Param(Constants.WRAPPER) Wrapper<YbFjProjectClueOnsite> wrapper);
}
