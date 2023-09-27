package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.dto.ClueStepFileDto;
import com.ai.modules.ybFj.dto.ProjectFileDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueFile;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.zip.ZipOutputStream;

/**
 * @Description: 飞检项目线索附件
 * @Author: jeecg-boot
 * @Date:   2023-03-08
 * @Version: V1.0
 */
public interface IYbFjProjectClueFileService extends IService<YbFjProjectClueFile> {

    List<YbFjProjectClueFile> queryProjectClueFiles(String clueIds);

    /**
     *
     * 功能描述：删除线索附件
     * @author zhangly
     * @date 2023-03-09 09:16:02
     *
     * @param fileQueryWrapper
     *
     * @return void
     *
     */
    void deleteClueFile(QueryWrapper<YbFjProjectClueFile> fileQueryWrapper);

    void deleteClueFile(String fileId);

    void deleteClueFiles(String fileIds);

    IPage<YbFjProjectClueFile> queryProjectClueFilesFromStep(IPage<YbFjProjectClueFile> page, ClueStepFileDto dto);

    IPage<YbFjProjectClueFile> queryProjectClueFilesByTemplate(IPage<YbFjProjectClueFile> page, ClueStepFileDto dto);

    /**
     *
     * 功能描述：根据任务ID获取附件列表
     * @author zhangly
     * @date 2023-03-15 11:00:49
     *
     * @param taskId
     *
     * @return java.util.List<com.ai.modules.ybFj.entity.YbFjProjectClueFile>
     *
     */
    List<YbFjProjectClueFile> queryProjectTaskFiles(String taskId);

    void download(String fileId, HttpServletRequest request, HttpServletResponse response) throws Exception;

    void downloadZip(HttpServletResponse response, String fileIds) throws Exception;

    void downloadZip(HttpServletResponse response, List<YbFjProjectClueFile> fileList) throws Exception;

    void downloadZip(ZipOutputStream zos, List<YbFjProjectClueFile> fileList) throws Exception;

    IPage<YbFjProjectClueFile> queryProjectFiles(IPage<YbFjProjectClueFile> page, ProjectFileDto dto);

    /**
     *
     * 功能描述：医院上传的附件
     * @author zhangly
     * @date 2023-03-20 12:28:37
     *
     * @param page
     * @param projectId
     *
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.ai.modules.ybFj.entity.YbFjProjectClueFile>
     *
     */
    IPage<YbFjProjectClueFile> queryProjectOrgUploadFiles(IPage<YbFjProjectClueFile> page, String projectId);
}
