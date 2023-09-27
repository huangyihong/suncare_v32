package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.dto.QryProjectClueDtlDto;
import com.ai.modules.ybFj.dto.SyncClueDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueDtl;
import com.ai.modules.ybFj.entity.YbFjProjectClueOnsiteDtl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 飞检项目现场核查线索明细
 * @Author: jeecg-boot
 * @Date:   2023-03-15
 * @Version: V1.0
 */
public interface IYbFjProjectClueOnsiteDtlService extends IService<YbFjProjectClueOnsiteDtl> {

    IPage<YbFjProjectClueOnsiteDtl> queryProjectClueDtl(IPage<YbFjProjectClueOnsiteDtl> page, QryProjectClueDtlDto dto) throws Exception;

    int insertOnsiteClueDtl(SyncClueDto dto);
}
