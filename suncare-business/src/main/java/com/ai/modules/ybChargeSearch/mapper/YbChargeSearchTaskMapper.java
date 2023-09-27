package com.ai.modules.ybChargeSearch.mapper;

import java.util.List;
import java.util.Map;

import com.ai.modules.ybChargeSearch.vo.YbChargeSearchTaskCountVo;
import com.ai.modules.ybChargeSearch.vo.YbChargeSearchTaskFunCountVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 收费明细查询任务表
 * @Author: jeecg-boot
 * @Date:   2022-10-09
 * @Version: V1.0
 */
public interface YbChargeSearchTaskMapper extends BaseMapper<YbChargeSearchTask> {

    IPage<Map<String,Object>> getUseCountPage(Page<YbChargeSearchTask> page, @Param("query") YbChargeSearchTaskCountVo chargeSearchTaskCountVo, @Param("fieldSql") String sql);

    List<Map<String,Object>> getUseCountList(@Param("query") YbChargeSearchTaskCountVo chargeSearchTaskCountVo, @Param("fieldSql") String sql);

    IPage<Map<String, Object>> getSearchTaskFunCount(Page<YbChargeSearchTask> page,@Param("query") YbChargeSearchTaskFunCountVo chargeSearchTaskFunCountVo);

    List<Map<String, Object>> getSearchTaskFunCountList(@Param("query") YbChargeSearchTaskFunCountVo chargeSearchTaskFunCountVo);
}
