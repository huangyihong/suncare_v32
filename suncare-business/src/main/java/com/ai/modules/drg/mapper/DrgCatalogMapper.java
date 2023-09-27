package com.ai.modules.drg.mapper;

import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.vo.DrgCatalogVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: DRG分组目录版本表
 * @Author: jeecg-boot
 * @Date:   2023-02-20
 * @Version: V1.0
 */
public interface DrgCatalogMapper extends BaseMapper<DrgCatalog> {
    IPage<DrgCatalogVo> selectPageVO(Page<DrgCatalog> page, @Param(Constants.WRAPPER) Wrapper<DrgCatalog> wrapper);
}
