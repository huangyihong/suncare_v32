package com.ai.modules.ybFj.mapper;

import com.ai.modules.ybFj.dto.SyncClueDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueCut;
import com.ai.modules.ybFj.vo.StatOnsiteClueVo;
import com.ai.modules.ybFj.vo.StatStepClueVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 飞检项目线索核减
 * @Author: jeecg-boot
 * @Date:   2023-03-14
 * @Version: V1.0
 */
public interface YbFjProjectClueCutMapper extends BaseMapper<YbFjProjectClueCut> {

    int insertCutClue(SyncClueDto dto);

    StatOnsiteClueVo statisticsCutClue(@Param("projectOrgId") String projectOrgId);

    StatStepClueVo statisticsStepClue(@Param(Constants.WRAPPER) Wrapper<YbFjProjectClueCut> wrapper);
}
