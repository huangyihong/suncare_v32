package com.ai.modules.ybFj.service;

import com.ai.common.export.ExportColModel;
import com.ai.modules.ybFj.dto.YbFjProjectClueDto;
import com.ai.modules.ybFj.entity.YbFjProjectClue;
import com.ai.modules.ybFj.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description: 飞检项目线索
 * @Author: jeecg-boot
 * @Date:   2023-03-07
 * @Version: V1.0
 */
public interface IYbFjProjectClueService extends IService<YbFjProjectClue> {

    /**
     *
     * 功能描述：线索提交
     * @author zhangly
     * @date 2023-03-16 16:41:49
     *
     * @param projectOrgId
     * @param file
     *
     * @return void
     *
     */
    void importProjectClue(String projectOrgId, MultipartFile file) throws Exception;

    /**
     *
     * 功能描述：线索覆盖
     * @author zhangly
     * @date 2023-03-13 11:58:43
     *
     * @param clueId
     * @param file
     *
     * @return void
     *
     */
    void coverProjectClue(String clueId, MultipartFile file) throws Exception;

    /**
     *
     * 功能描述：线索明细追加
     * @author zhangly
     * @date 2023-03-13 11:58:57
     *
     * @param clueId
     * @param file
     *
     * @return void
     *
     */
    void appendProjectClue(String clueId, MultipartFile file) throws Exception;

    void downTemplate(String filePath, String alias, HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     *
     * 功能描述：线索审核
     * @author zhangly
     * @date 2023-03-08 09:30:40
     *
     * @param clueId
     * @param auditState
     * @param auditOpinion
     *
     * @return void
     *
     */
    void auditProjectClue(String clueId, String auditState, String auditOpinion);

    void auditProjectClues(String clueIds, String auditState, String auditOpinion);

    void auditHospClue(String clueId, String auditState, String auditOpinion);

    void auditHospClues(String clueIds, String auditState, String auditOpinion);

    void auditCutClue(String clueId, String auditState, String auditOpinion);

    void auditCutClues(String clueIds, String auditState, String auditOpinion);

    void saveProjectClue(YbFjProjectClueDto dto) throws Exception;

    void updateProjectClue(YbFjProjectClueDto dto) throws Exception;

    void removeProjectClue(String clueId) throws Exception;

    void removeProjectClues(String clueIds) throws Exception;

    /**
     *
     * 功能描述：推送其他环节
     * @author zhangly
     * @date 2023-03-09 17:29:30
     *
     * @param clueIds
     * @param nextStep
     * @param prevStep
     *
     * @return void
     *
     */
    void pushProjectClue(String clueIds, String nextStep, String prevStep) throws Exception;

    /**
     *
     * 功能描述：下载线索附件（压缩包）
     * @author zhangly
     * @date 2023-03-13 10:23:11
     *
     * @param clueId
     *
     * @return void
     *
     */
    void downloadProjectClueFilesZip(String clueId, HttpServletResponse response) throws Exception;

    /**
     * 
     * 功能描述：线索推送到医院
     * @author zhangly
     * @date 2023-03-14 11:30:19
     *
     * @param clueIds
     * 
     * @return void
     *
     */
    void pushProjectClueToOrg(String clueIds) throws Exception;

    /**
     *
     * 功能描述：统计线索总数、线索总金额等
     * @author zhangly
     * @date 2023-03-17 17:00:48
     *
     * @param projectOrgId
     *
     * @return com.ai.modules.ybFj.vo.StatProjectClueVo
     *
     */
    StatProjectClueVo statisticsProjectClue(String projectOrgId);

    Integer statisticsProjectClueAmount(String projectId);

    StatStepClueVo statisticsStepClue(String statType, QueryWrapper<YbFjProjectClue> wrapper);

    StatStepClueVo statisticsStepClueByOrg(String orgId);

    void exportProjectClues(List<YbFjProjectClue> dataList, HttpServletResponse response) throws Exception;

    void exportProjectClues(String projectOrgId, String stepType, HttpServletResponse response) throws Exception;


    List<ExportColModel> getHeaderList(boolean isOrg);

    /**
     *
     * 功能描述：医院端线索总览列表
     * @author zhangly
     * @date 2023-03-27 17:04:23
     *
     * @param page
     * @param orgId
     * @param clue
     *
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.ai.modules.ybFj.vo.YbFjProjectClueCutDto>
     *
     */
    IPage<YbFjProjectClueCutVo> queryProjectClueByOrg(IPage<YbFjProjectClueCutVo> page, String orgId, YbFjProjectClue clue);

    List<YbFjProjectClueCutVo> queryProjectClueByOrg(String orgId, YbFjProjectClue clue);

    void exportOrgClientClues(List<YbFjProjectClueCutVo> dataList, HttpServletResponse response) throws Exception;

    /**
     *
     * 功能描述：线索汇总上传
     * @author zhangly
     * @date 2023-06-01 11:20:07
     *
     * @param projectOrgId
     * @param file
     *
     * @return void
     *
     */
    void importClue(String projectOrgId, MultipartFile file) throws Exception;

    /**
     *
     * 功能描述：线索汇总覆盖
     * @author zhangly
     * @date 2023-06-01 14:21:04
     *
     * @param projectOrgId
     * @param file
     *
     * @return void
     *
     */
    void coverClue(String projectOrgId, MultipartFile file) throws Exception;

    /**
     *
     * 功能描述：线索明细上传
     * @author zhangly
     * @date 2023-06-01 14:30:02
     *
     * @param clueId
     * @param file
     *
     * @return void
     *
     */
    void importClueDtl(String clueId, MultipartFile file) throws Exception;

    /**
     *
     * 功能描述：线索明细覆盖
     * @author zhangly
     * @date 2023-06-01 14:36:45
     *
     * @param clueId
     * @param file
     *
     * @return void
     *
     */
    void coverClueDtl(String clueId, MultipartFile file) throws Exception;
}
