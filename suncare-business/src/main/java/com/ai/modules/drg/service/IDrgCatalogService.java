package com.ai.modules.drg.service;

import com.ai.modules.drg.entity.DrgCatalog;
import com.ai.modules.drg.vo.DrgCatalogVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;

/**
 * @Description: DRG分组目录版本表
 * @Author: jeecg-boot
 * @Date:   2023-02-20
 * @Version: V1.0
 */
public interface IDrgCatalogService extends IService<DrgCatalog> {
    IPage<DrgCatalogVo> pageVO(Page<DrgCatalog> page, Wrapper<DrgCatalog> wrapper);

    Result<?> delete(String id);

    Result<?> deleteBatch(String ids);

    DrgCatalog findDrgCatalog(String version);

    DrgCatalog findCatalog(String catalogType, String version);
}
