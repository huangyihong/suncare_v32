package com.ai.modules.ybChargeSearch.mapper;

import com.ai.modules.ybChargeSearch.entity.YbChargeSearchHistory;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 收费明细查询历史分析表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
public interface YbChargeSearchHistoryMapper extends BaseMapper<YbChargeSearchHistory> {
    IPage<YbChargeSearchHistory> selectPageVO(Page<YbChargeSearchHistory> page, @Param(Constants.WRAPPER) Wrapper<YbChargeSearchHistory> wrapper);

    List<YbChargeSearchHistory> selectListVO(@Param(Constants.WRAPPER) Wrapper<YbChargeSearchHistory> wrapper);
}
