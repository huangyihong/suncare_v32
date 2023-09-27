package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.dto.SyncClueDto;
import com.ai.modules.ybFj.dto.YbFjProjectClueCutDto;
import com.ai.modules.ybFj.dto.YbFjProjectClueOnsiteDto;
import com.ai.modules.ybFj.entity.YbFjProjectClue;
import com.ai.modules.ybFj.entity.YbFjProjectClueOnsite;
import com.ai.modules.ybFj.vo.StatOnsiteClueVo;
import com.ai.modules.ybFj.vo.StatStepClueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description: 飞检项目线索现场核查
 * @Author: jeecg-boot
 * @Date:   2023-03-15
 * @Version: V1.0
 */
public interface IYbFjProjectClueOnsiteService extends IService<YbFjProjectClueOnsite> {

    /**
     *
     * 功能描述：线索提交
     * @author zhangly
     * @date 2023-03-16 16:41:36
     *
     * @param projectOrgId
     * @param file
     *
     * @return void
     *
     */
    void importOnsiteClue(String projectOrgId, MultipartFile file) throws Exception;

    /**
     *
     * 功能描述：线索覆盖
     * @author zhangly
     * @date 2023-03-13 11:58:43
     *
     * @param projectOrgId
     * @param file
     *
     * @return void
     *
     */
    void coverOnsiteClue(String projectOrgId, MultipartFile file) throws Exception;

    /**
     *
     * 功能描述：上传线索明细
     * @author zhangly
     * @date 2023-06-06 16:10:37
     *
     * @param clueId
     * @param file
     *
     * @return void
     *
     */
    void importOnsiteClueDtl(String clueId, MultipartFile file) throws Exception;

    /**
     *
     * 功能描述：覆盖线索明细
     * @author zhangly
     * @date 2023-06-06 16:10:56
     *
     * @param clueId
     * @param file
     *
     * @return void
     *
     */
    void coverOnsiteClueDtl(String clueId, MultipartFile file) throws Exception;

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
    void appendOnsiteClue(String clueId, MultipartFile file) throws Exception;

    void updateProjectClueOnsite(YbFjProjectClueOnsiteDto dto);

    /**
     *
     * 功能描述：批量修改
     * @author zhangly
     * @date 2023-03-20 14:07:57
     *
     * @param dtoList
     *
     * @return void
     *
     */
    void updateProjectClueOnsite(List<YbFjProjectClueOnsiteDto> dtoList);

    /**
     *
     * 功能描述：保存核减
     * @author zhangly
     * @date 2023-03-15 12:39:26
     *
     * @param dto
     *
     * @return void
     *
     */
    void saveProjectClueOnsiteCut(YbFjProjectClueCutDto dto) throws Exception;

    void removeProjectClueOnsite(String clueId);

    void removeProjectClueOnsites(String clueIds);

    int insertOnsiteClue(SyncClueDto dto);

    void exportOnsiteClues(String projectOrgId, HttpServletResponse response) throws Exception;

    void importOnsiteClues(String projectOrgId, MultipartFile file) throws Exception;

    List<YbFjProjectClueOnsite> queryOnsiteClues(YbFjProjectClueOnsiteDto dto) throws Exception;

    IPage<YbFjProjectClueOnsite> queryOnsiteClues(IPage<YbFjProjectClueOnsite> page, YbFjProjectClueOnsiteDto dto) throws Exception;

    void uploadOnsiteFile(String projectOrgId, MultipartFile file) throws Exception;

    /**
     *
     * 功能描述：导出签字反馈表
     * @author zhangly
     * @date 2023-03-17 09:45:41
     *
     * @param dto
     * @param response
     *
     * @return void
     *
     */
    void exportOnsiteFeedback(YbFjProjectClueOnsiteDto dto, HttpServletResponse response) throws Exception;

    /**
     *
     * 功能描述：载线索附件（压缩包）
     * @author zhangly
     * @date 2023-03-17 10:38:00
     *
     * @param clueId
     * @param response
     *
     * @return void
     *
     */
    void downloadOnsiteClueFilesZip(String clueId, HttpServletResponse response) throws Exception;

    String exportOnsiteClueTemplate(YbFjProjectClueOnsiteDto dto) throws Exception;

    /**
     *
     * 功能描述：归档文件输出
     * @author zhangly
     * @date 2023-03-20 14:52:14
     *
     * @param dto
     * @param templateCodes
     *
     * @return void
     *
     */
    void outOnsiteClueTemplate(HttpServletResponse response, YbFjProjectClueOnsiteDto dto, String[] templateCodes) throws Exception;

    /**
     *
     * 功能描述：统计线索总数、线索总金额等
     * @author zhangly
     * @date 2023-03-17 16:58:40
     *
     * @param projectOrgId
     *
     * @return com.ai.modules.ybFj.vo.StatOnsiteClueVo
     *
     */
    StatOnsiteClueVo statisticsOnsiteClue(String projectOrgId);

    StatStepClueVo statisticsStepClue(QueryWrapper<YbFjProjectClueOnsite> wrapper);
}
