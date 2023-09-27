package com.ai.modules.ybFj.mapper;

import com.ai.modules.ybFj.dto.SyncClueDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueCutDtl;
import com.ai.modules.ybFj.vo.TaskClueVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 飞检项目终审确认线索明细
 * @Author: jeecg-boot
 * @Date:   2023-06-13
 * @Version: V1.0
 */
public interface YbFjProjectClueCutDtlMapper extends BaseMapper<YbFjProjectClueCutDtl> {

    int insertCutClueDtl(SyncClueDto dto);

    TaskClueVo queryTaskClueVo(@Param(Constants.WRAPPER) Wrapper<YbFjProjectClueCutDtl> wrapper);
}
