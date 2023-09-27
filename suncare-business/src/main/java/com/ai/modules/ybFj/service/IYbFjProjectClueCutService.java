package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.dto.SyncClueDto;
import com.ai.modules.ybFj.dto.YbFjProjectClueCutDto;
import com.ai.modules.ybFj.dto.YbFjProjectClueOnsiteDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueCut;
import com.ai.modules.ybFj.entity.YbFjProjectClueOnsite;
import com.ai.modules.ybFj.vo.StatStepClueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;

/**
 * @Description: 飞检项目线索核减
 * @Author: jeecg-boot
 * @Date:   2023-03-14
 * @Version: V1.0
 */
public interface IYbFjProjectClueCutService extends IService<YbFjProjectClueCut> {

    void saveProjectClueCut(YbFjProjectClueCutDto dto) throws Exception;

    int insertCutClue(SyncClueDto dto);

    void pushProjectClueToOrg(String clueIds) throws Exception;

    IPage<YbFjProjectClueCut> queryCutClues(IPage<YbFjProjectClueCut> page, YbFjProjectClueOnsiteDto dto) throws Exception;

    StatStepClueVo statisticsStepClue(QueryWrapper<YbFjProjectClueCut> wrapper);

    void downloadCutClueFilesZip(String clueId, HttpServletResponse response) throws Exception;

    /**
     *
     * 功能描述：线索导出
     * @author zhangly
     * @date 2023-06-13 17:53:24
     *
     * @param projectOrgId
     * @param response
     *
     * @return void
     *
     */
    void exportProjectClues(String projectOrgId, HttpServletResponse response) throws Exception;
}
