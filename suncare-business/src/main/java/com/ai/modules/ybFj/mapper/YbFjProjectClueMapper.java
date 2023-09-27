package com.ai.modules.ybFj.mapper;

import com.ai.modules.ybFj.entity.YbFjProjectClue;
import com.ai.modules.ybFj.vo.StatProjectClueVo;
import com.ai.modules.ybFj.vo.StatStepClueVo;
import com.ai.modules.ybFj.vo.YbFjProjectClueCutVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 飞检项目线索
 * @Author: jeecg-boot
 * @Date:   2023-03-07
 * @Version: V1.0
 */
public interface YbFjProjectClueMapper extends BaseMapper<YbFjProjectClue> {

    StatProjectClueVo statisticsProjectClue(@Param("projectOrgId") String projectOrgId);

    Integer statisticsProjectClueAmount(@Param("projectId") String projectId);

    StatStepClueVo statisticsSubmitClue(@Param(Constants.WRAPPER) Wrapper<YbFjProjectClue> wrapper);

    StatStepClueVo statisticsHospClue(@Param(Constants.WRAPPER) Wrapper<YbFjProjectClue> wrapper);

    StatStepClueVo statisticsCutClue(@Param(Constants.WRAPPER) Wrapper<YbFjProjectClue> wrapper);

    IPage<YbFjProjectClueCutVo> queryProjectClueByOrg(IPage<YbFjProjectClueCutVo> page, @Param("orgId") String orgId, @Param(Constants.WRAPPER) Wrapper<YbFjProjectClue> wrapper);

    List<YbFjProjectClueCutVo> queryProjectClueByOrg(@Param("orgId") String orgId, @Param(Constants.WRAPPER) Wrapper<YbFjProjectClue> wrapper);

    StatStepClueVo statisticsCutClueByOrg(@Param("orgId") String orgId, @Param(Constants.WRAPPER) Wrapper<YbFjProjectClue> wrapper);
}
