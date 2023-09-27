package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.dto.QryProjectClueDtlDto;
import com.ai.modules.ybFj.dto.SyncClueDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueCutDtl;
import com.ai.modules.ybFj.entity.YbFjProjectClueOnsiteDtl;
import com.ai.modules.ybFj.vo.TaskClueVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 飞检项目终审确认线索明细
 * @Author: jeecg-boot
 * @Date:   2023-06-13
 * @Version: V1.0
 */
public interface IYbFjProjectClueCutDtlService extends IService<YbFjProjectClueCutDtl> {

    IPage<YbFjProjectClueCutDtl> queryProjectClueDtl(IPage<YbFjProjectClueCutDtl> page, QryProjectClueDtlDto dto) throws Exception;

    int insertCutClueDtl(SyncClueDto dto);

    TaskClueVo queryTaskClueVo(String clueIds);
}
