package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.dto.YbFjProjectTaskDto;
import com.ai.modules.ybFj.entity.YbFjProjectTask;
import com.ai.modules.ybFj.vo.TaskClueVo;
import com.ai.modules.ybFj.vo.YbFjProjectOrgVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Description: 飞检项目审核任务表
 * @Author: jeecg-boot
 * @Date:   2023-03-10
 * @Version: V1.0
 */
public interface IYbFjProjectTaskService extends IService<YbFjProjectTask> {

    TaskClueVo queryTaskClueVo(String clueIds);

    TaskClueVo queryTaskClueVoInCut(String clueIds);

    void saveProjectTask(YbFjProjectTaskDto dto, String taskType, MultipartFile[] multipartFiles) throws Exception;

    void updateProjectTask(YbFjProjectTaskDto dto, MultipartFile[] multipartFiles, String fileIds) throws Exception;

    void removeProjectTask(String taskId);

    void removeProjectTasks(String taskIds);

    /**
     *
     * 功能描述：已审核的任务
     * @author zhangly
     * @date 2023-03-10 14:13:03
     *
     * @param page
     *
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.ai.modules.ybFj.entity.YbFjProjectTask>
     *
     */
    IPage<YbFjProjectTask> queryProjectTaskAlreadyAudit(IPage<YbFjProjectTask> page, String projectOrgId, String taskType);

    /**
     *
     * 功能描述：我创建的任务
     * @author zhangly
     * @date 2023-03-10 14:16:41
     *
     * @param page
     *
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.ai.modules.ybFj.entity.YbFjProjectTask>
     *
     */
    IPage<YbFjProjectTask> queryProjectTaskMineCreate(IPage<YbFjProjectTask> page, String projectOrgId, String taskType);

    /**
     *
     * 功能描述：待审核的任务
     * @author zhangly
     * @date 2023-03-10 14:21:06
     *
     * @param page
     *
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.ai.modules.ybFj.entity.YbFjProjectTask>
     *
     */
    IPage<YbFjProjectTask> queryProjectTaskWaitAudit(IPage<YbFjProjectTask> page, String projectOrgId, String taskType);

    /**
     *
     * 功能描述：线索提交环节-任务审核
     * @author zhangly
     * @date 2023-03-10 14:58:08
     *
     * @param taskId
     * @param auditState
     * @param auditOpinion
     * @param multipartFiles
     *
     * @return void
     *
     */
    void auditProjectTask(String taskId, String auditState, String auditOpinion, MultipartFile[] multipartFiles) throws Exception;

    /**
     *
     * 功能描述：医院复核环节-任务反馈
     * @author zhangly
     * @date 2023-03-14 16:39:49
     *
     * @param taskId
     * @param auditState
     * @param auditOpinion
     * @param multipartFiles
     *
     * @return void
     *
     */
    void auditHospTask(String taskId, String auditState, String auditOpinion, MultipartFile[] multipartFiles) throws Exception;

    /**
     *
     * 功能描述：线上核减环节-核减任务审核
     * @author zhangly
     * @date 2023-03-14 16:41:10
     *
     * @param taskId
     * @param auditState
     * @param auditOpinion
     * @param multipartFiles
     *
     * @return void
     *
     */
    void auditCutTask(String taskId, String auditState, String auditOpinion, MultipartFile[] multipartFiles) throws Exception;

    /**
     *
     * 功能描述：医院端已审核的任务
     * @author zhangly
     * @date 2023-03-14 14:56:35
     *
     * @param page
     * @param orgId
     *
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.ai.modules.ybFj.entity.YbFjProjectTask>
     *
     */
    IPage<YbFjProjectTask> queryHospTaskAlreadyAudit(IPage<YbFjProjectTask> page, String orgId, String projectOrgId);

    /**
     *
     * 功能描述：医院端我创建的任务
     * @author zhangly
     * @date 2023-03-14 15:02:51
     *
     * @param page
     * @param orgId
     *
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.ai.modules.ybFj.entity.YbFjProjectTask>
     *
     */
    IPage<YbFjProjectTask> queryHospTaskMineCreate(IPage<YbFjProjectTask> page, String orgId, String projectOrgId);

    /**
     *
     * 功能描述：医院端待审核的任务
     * @author zhangly
     * @date 2023-03-14 15:03:41
     *
     * @param page
     * @param orgId
     *
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.ai.modules.ybFj.entity.YbFjProjectTask>
     *
     */
    IPage<YbFjProjectTask> queryHospTaskWaitAudit(IPage<YbFjProjectTask> page, String orgId, String projectOrgId);

    /**
     * 
     * 功能描述：医院端线上核减任务
     * @author zhangly
     * @date 2023-03-14 16:35:55
     *
     * @param page
     * @param orgId
     * 
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.ai.modules.ybFj.entity.YbFjProjectTask>
     *
     */
    IPage<YbFjProjectTask> queryCutTaskMineCreate(IPage<YbFjProjectTask> page, String orgId, String projectOrgId);
}
