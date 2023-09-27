package com.ai.modules.ybChargeSearch.service.impl;

import com.ai.common.utils.ExportXUtils;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchHistory;
import com.ai.modules.ybChargeSearch.mapper.YbChargeSearchHistoryMapper;
import com.ai.modules.ybChargeSearch.service.IYbChargeSearchHistoryService;
import com.ai.modules.ybChargeSearch.vo.YbChargeSearchConstant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.List;

/**
 * @Description: 收费明细查询历史分析表
 * @Author: jeecg-boot
 * @Date:   2022-11-23
 * @Version: V1.0
 */
@Service
public class YbChargeSearchHistoryServiceImpl extends ServiceImpl<YbChargeSearchHistoryMapper, YbChargeSearchHistory> implements IYbChargeSearchHistoryService {

    @Override
    public IPage<YbChargeSearchHistory> selectPageVO(Page<YbChargeSearchHistory> page, QueryWrapper<YbChargeSearchHistory> queryWrapper) {
        return this.baseMapper.selectPageVO(page,queryWrapper);
    }

    @Override
    public List<YbChargeSearchHistory> selectListVO(QueryWrapper<YbChargeSearchHistory> queryWrapper) {
        return this.baseMapper.selectListVO(queryWrapper);
    }

    @Override
    public void exportExcel(List<YbChargeSearchHistory> list, OutputStream os) throws Exception{
        String titleStr = YbChargeSearchConstant.TASK_TYPE_MAP.get(YbChargeSearchConstant.HISTORY).getTitleStr();
        String fieldStr = YbChargeSearchConstant.TASK_TYPE_MAP.get(YbChargeSearchConstant.HISTORY).getFieldStr();

        String[] titles = titleStr.split(",");
        String[] fields = fieldStr.split(",");
        for (YbChargeSearchHistory bean : list) {
            if(bean.getQtyNum()==0){
                bean.setQtyNum(null);
            }
        }
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        ExportXUtils.exportExl(list, YbChargeSearchHistory.class, titles, fields, workbook, "检索关键字使用统计");
        workbook.write(os);
        workbook.dispose();
    }
}
