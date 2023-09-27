package com.ai.modules.ybChargeSearch.service;

import com.ai.modules.ybChargeSearch.entity.YbChargeSearchHistory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 收费明细查询历史分析表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
public interface IYbChargeSearchHistoryService extends IService<YbChargeSearchHistory> {

    public IPage<YbChargeSearchHistory> selectPageVO(
            Page<YbChargeSearchHistory> page, QueryWrapper<YbChargeSearchHistory> queryWrapper);

    public List<YbChargeSearchHistory> selectListVO(QueryWrapper<YbChargeSearchHistory> queryWrapper);

    public void exportExcel(List<YbChargeSearchHistory> list, OutputStream os) throws Exception;
}
