package com.ai.modules.ybFj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.ai.common.utils.IdUtils;
import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.dto.YbFjProjectTaskDto;
import com.ai.modules.ybFj.entity.YbFjProjectClue;
import com.ai.modules.ybFj.entity.YbFjProjectClueFile;
import com.ai.modules.ybFj.entity.YbFjProjectOrg;
import com.ai.modules.ybFj.entity.YbFjProjectTask;
import com.ai.modules.ybFj.mapper.YbFjProjectTaskMapper;
import com.ai.modules.ybFj.service.*;
import com.ai.modules.ybFj.vo.TaskClueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.util.CommonUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 飞检项目审核任务表
 * @Author: jeecg-boot
 * @Date:   2023-03-10
 * @Version: V1.0
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class YbFjProjectTaskServiceImpl extends ServiceImpl<YbFjProjectTaskMapper, YbFjProjectTask> implements IYbFjProjectTaskService {

    @Autowired
    private IYbFjProjectOrgService projectOrgService;
    @Autowired
    private IYbFjProjectClueFileService clueFileService;
    @Autowired
    private IYbFjProjectClueDtlService clueDtlService;
    @Autowired
    private IYbFjProjectClueCutDtlService clueCutDtlService;

    @Override
    public TaskClueVo queryTaskClueVo(String clueIds) {
        if(StringUtils.isBlank(clueIds)) {
            return new TaskClueVo();
        }
        TaskClueVo vo = clueDtlService.queryTaskClueVo(clueIds);
        if(vo==null) {
            vo = new TaskClueVo();
        }
        vo.setFileList(clueFileService.queryProjectClueFiles(clueIds));
        return vo;
    }

    @Override
    public TaskClueVo queryTaskClueVoInCut(String clueIds) {
        if(StringUtils.isBlank(clueIds)) {
            return new TaskClueVo();
        }
        TaskClueVo vo = clueCutDtlService.queryTaskClueVo(clueIds);
        if(vo==null) {
            vo = new TaskClueVo();
        }
        vo.setFileList(clueFileService.queryProjectClueFiles(clueIds));
        return vo;
    }

    @Override
    public void saveProjectTask(YbFjProjectTaskDto dto, String taskType, MultipartFile[] multipartFiles) throws Exception {
        if(StringUtils.isBlank(dto.getProjectOrgId())) {
            throw new Exception("projectOrgId参数不能为空");
        }
        if(DcFjConstants.CLUE_STEP_SUBMIT.equals(taskType)
                && StringUtils.isBlank(dto.getAuditUser())) {
            throw new Exception("auditUser参数不能为空，请指定任务审核人");
        }
        YbFjProjectTask task = BeanUtil.toBean(dto, YbFjProjectTask.class);
        task.setTaskType(taskType);
        String taskId = IdUtils.uuid();
        task.setTaskId(taskId);
        YbFjProjectOrg projectOrg = projectOrgService.getById(dto.getProjectOrgId());
        task.setProjectId(projectOrg.getProjectId());
        task.setAuditState(DcFjConstants.CLUE_STATE_INIT);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        task.setCreateTime(DateUtils.getDate());
        task.setCreateUser(user.getUsername());
        task.setCreateUsername(user.getRealname());
        String stepType = DcFjConstants.FILE_STEP_TASK;
        if(DcFjConstants.CLUE_STEP_HOSP.equals(taskType)) {
            //医院复核环节
            stepType = DcFjConstants.FILE_STEP_HOSP_TASK;
        } else if(DcFjConstants.CLUE_STEP_CUT.equals(taskType)) {
            //线上核减环节
            stepType = DcFjConstants.FILE_STEP_CUT_TASK;
        }
        if(StringUtils.isNotBlank(dto.getClueIds())) {
            List<YbFjProjectClueFile> fileList = clueFileService.queryProjectClueFiles(dto.getClueIds());
            if(fileList!=null && fileList.size()>0) {
                //拷贝文件
                for(YbFjProjectClueFile record : fileList) {
                    String filePath = CommonUtil.UPLOAD_PATH + record.getFilePath();
                    this.copyClueFile(task, record, stepType);
                }
            }
        }
        if(multipartFiles!=null && multipartFiles.length>0) {
            for(MultipartFile multipartFile : multipartFiles) {
                this.saveClueMultipartFile(task, multipartFile, stepType);
            }
        }
        this.save(task);
    }

    private void copyClueFile(YbFjProjectTask task, YbFjProjectClueFile record, String stepType) throws Exception {
        String fileSrcname = record.getFileSrcname();
        String filePath = CommonUtil.UPLOAD_PATH + record.getFilePath();
        int index = fileSrcname.lastIndexOf(".");
        // 文件扩展名
        String extname = fileSrcname.substring(index+1);
        String newname = DateUtils.getDate("yyyyMMddHHmmssSSS")+"."+extname;
        String path = File.separator + "fj" + File.separator + "task" + File.separator + DateUtils.getDate("yyyyMM");
        File folder = new File(CommonUtil.UPLOAD_PATH + path);
        if(!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(filePath);
        String copyPath = folder + File.separator + newname;
        File saveFile = new File(copyPath);
        FileCopyUtils.copy(file, saveFile);
        YbFjProjectClueFile clueFile = this.getProjectClueFile(task, extname, fileSrcname, newname, path, file.length(), stepType);
        clueFileService.save(clueFile);
    }

    private void saveClueMultipartFile(YbFjProjectTask task, MultipartFile multipartFile, String stepType) throws Exception {
        String fileSrcname = multipartFile.getOriginalFilename();
        int index = fileSrcname.lastIndexOf(".");
        // 文件扩展名
        String extname = fileSrcname.substring(index+1);
        String newname = DateUtils.getDate("yyyyMMddHHmmssSSS")+"."+extname;
        String path = File.separator + "fj" + File.separator + "task" + File.separator + DateUtils.getDate("yyyyMM");
        File folder = new File(CommonUtil.UPLOAD_PATH + path);
        if(!folder.exists()) {
            folder.mkdirs();
        }
        String copyPath = folder + File.separator + newname;
        File saveFile = new File(copyPath);
        FileCopyUtils.copy(multipartFile.getBytes(), saveFile);
        YbFjProjectClueFile clueFile = this.getProjectClueFile(task, extname, fileSrcname, newname, path, saveFile.length(), stepType);
        clueFileService.save(clueFile);
    }

    private YbFjProjectClueFile getProjectClueFile(YbFjProjectTask task, String extname, String fileSrcname,
            String newname, String path, long size, String stepType) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        YbFjProjectClueFile clueFile = new YbFjProjectClueFile();
        clueFile.setFileId(IdUtils.uuid());
        clueFile.setClueId(task.getTaskId());
        clueFile.setProjectId(task.getProjectId());
        clueFile.setProjectOrgId(task.getProjectOrgId());
        clueFile.setStepGroup(task.getTaskType());
        clueFile.setStepType(stepType);
        if(DcFjConstants.FILE_EXT_XLS.equalsIgnoreCase(extname)
                || DcFjConstants.FILE_EXT_XLSX.equalsIgnoreCase(extname)) {
            clueFile.setFileType(DcFjConstants.FILE_TYPE_EXCEL);
        } else if(DcFjConstants.FILE_EXT_DOC.equalsIgnoreCase(extname)
                || DcFjConstants.FILE_EXT_DOCX.equalsIgnoreCase(extname)) {
            clueFile.setFileType(DcFjConstants.FILE_TYPE_WORD);
        } else if(DcFjConstants.FILE_EXT_PDF.equalsIgnoreCase(extname)) {
            clueFile.setFileType(DcFjConstants.FILE_TYPE_PDF);
        } else {
            clueFile.setFileType(extname);
        }
        clueFile.setOperType(DcFjConstants.FILE_OPER_TYPE_UP);
        clueFile.setFileSrcname(fileSrcname);
        clueFile.setFileName(newname);
        clueFile.setFilePath(path + File.separator + newname);
        clueFile.setFileSize(size);
        clueFile.setCreateTime(DateUtils.getDate());
        clueFile.setCreateUser(user.getUsername());
        clueFile.setCreateUsername(user.getRealname());
        return clueFile;
    }

    @Override
    public void updateProjectTask(YbFjProjectTaskDto dto, MultipartFile[] multipartFiles, String fileIds) throws Exception {
        if(StringUtils.isBlank(dto.getAuditUser())) {
            throw new Exception("auditUser参数不能为空，请指定任务审核人");
        }
        YbFjProjectTask task = this.getById(dto.getTaskId());
        BeanUtil.copyProperties(dto, task, CopyOptions.create().ignoreNullValue());
        task.setAuditState(DcFjConstants.CLUE_STATE_INIT);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        task.setUpdateTime(DateUtils.getDate());
        task.setUpdateUser(user.getUsername());
        task.setUpdateUsername(user.getRealname());
        this.updateById(task);
        String taskType = task.getTaskType();
        String stepType = DcFjConstants.FILE_STEP_TASK;
        if(DcFjConstants.CLUE_STEP_HOSP.equals(taskType)) {
            //医院复核环节
            stepType = DcFjConstants.FILE_STEP_HOSP_TASK;
        } else if(DcFjConstants.CLUE_STEP_CUT.equals(taskType)) {
            //线上核减环节
            stepType = DcFjConstants.FILE_STEP_CUT_TASK;
        }
        if(multipartFiles!=null && multipartFiles.length>0) {
            for(MultipartFile multipartFile : multipartFiles) {
                this.saveClueMultipartFile(task, multipartFile, stepType);
            }
        }
        if(StringUtils.isNotBlank(fileIds)) {
            //删除附件
            clueFileService.deleteClueFiles(fileIds);
        }
    }

    @Override
    public void removeProjectTask(String taskId) {
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.eq("clue_id", taskId);
        clueFileService.deleteClueFile(fileQueryWrapper);
        this.removeById(taskId);
    }

    @Override
    public void removeProjectTasks(String taskIds) {
        String[] ids = taskIds.split(",");
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.in("clue_id", ids);
        clueFileService.deleteClueFile(fileQueryWrapper);
        this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public IPage<YbFjProjectTask> queryProjectTaskAlreadyAudit(IPage<YbFjProjectTask> page, String projectOrgId, String taskType) {
        QueryWrapper<YbFjProjectTask> wrapper = new QueryWrapper<>();
        wrapper.eq("project_org_id", projectOrgId);
        wrapper.ne("audit_state", DcFjConstants.CLUE_STATE_INIT);
        wrapper.eq("task_type", taskType);
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }

    @Override
    public IPage<YbFjProjectTask> queryProjectTaskMineCreate(IPage<YbFjProjectTask> page, String projectOrgId, String taskType) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        QueryWrapper<YbFjProjectTask> wrapper = new QueryWrapper<>();
        wrapper.eq("project_org_id", projectOrgId);
        wrapper.eq("create_user", user.getUsername());
        wrapper.eq("task_type", taskType);
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }

    @Override
    public IPage<YbFjProjectTask> queryProjectTaskWaitAudit(IPage<YbFjProjectTask> page, String projectOrgId, String taskType) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        QueryWrapper<YbFjProjectTask> wrapper = new QueryWrapper<>();
        wrapper.eq("project_org_id", projectOrgId);
        if(DcFjConstants.CLUE_STEP_SUBMIT.equals(taskType)) {
            //待我审核
            wrapper.eq("audit_user", user.getUsername());
        }
        wrapper.eq("audit_state", DcFjConstants.CLUE_STATE_INIT);
        wrapper.eq("task_type", taskType);
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }

    @Override
    public void auditProjectTask(String taskId, String auditState, String auditOpinion, MultipartFile[] multipartFiles) throws Exception {
        YbFjProjectTask task = this.getById(taskId);
        task.setAuditState(auditState);
        task.setAuditOpinion(auditOpinion);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(DcFjConstants.CLUE_STEP_SUBMIT.equals(task.getTaskType())
                && StringUtils.isNotBlank(task.getAuditUser())
                && !task.getAuditUser().equals(user.getUsername())) {
            throw new Exception("您没有审核权限");
        }
        task.setAuditTime(DateUtils.getDate());
        this.updateById(task);
        if(multipartFiles!=null && multipartFiles.length>0) {
            for(MultipartFile multipartFile : multipartFiles) {
                this.saveClueMultipartFile(task, multipartFile, DcFjConstants.FILE_STEP_TASK_AUDIT);
            }
        }
    }

    @Override
    public void auditHospTask(String taskId, String auditState, String auditOpinion, MultipartFile[] multipartFiles) throws Exception {
        YbFjProjectTask task = this.getById(taskId);
        task.setAuditState(auditState);
        task.setAuditOpinion(auditOpinion);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        task.setAuditTime(DateUtils.getDate());
        this.updateById(task);
        if(multipartFiles!=null && multipartFiles.length>0) {
            for(MultipartFile multipartFile : multipartFiles) {
                this.saveClueMultipartFile(task, multipartFile, DcFjConstants.FILE_STEP_HOSP_TASK_AUDIT);
            }
        }
    }

    @Override
    public void auditCutTask(String taskId, String auditState, String auditOpinion, MultipartFile[] multipartFiles) throws Exception {
        YbFjProjectTask task = this.getById(taskId);
        task.setAuditState(auditState);
        task.setAuditOpinion(auditOpinion);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        task.setAuditTime(DateUtils.getDate());
        this.updateById(task);
        if(multipartFiles!=null && multipartFiles.length>0) {
            for(MultipartFile multipartFile : multipartFiles) {
                this.saveClueMultipartFile(task, multipartFile, DcFjConstants.FILE_STEP_CUT_TASK_AUDIT);
            }
        }
    }

    @Override
    public IPage<YbFjProjectTask> queryHospTaskAlreadyAudit(IPage<YbFjProjectTask> page, String orgId, String projectOrgId) {
        QueryWrapper<YbFjProjectTask> wrapper = new QueryWrapper<>();
        wrapper.eq("project_org_id", projectOrgId);
        wrapper.inSql("project_org_id", "select project_org_id from yb_fj_project_org b where b.org_id='"+orgId+"'");
        wrapper.ne("audit_state", DcFjConstants.CLUE_STATE_INIT);
        wrapper.eq("task_type", DcFjConstants.CLUE_STEP_HOSP);
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }

    @Override
    public IPage<YbFjProjectTask> queryHospTaskMineCreate(IPage<YbFjProjectTask> page, String orgId, String projectOrgId) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        QueryWrapper<YbFjProjectTask> wrapper = new QueryWrapper<>();
        wrapper.eq("project_org_id", projectOrgId);
        wrapper.inSql("project_org_id", "select project_org_id from yb_fj_project_org b where b.org_id='"+orgId+"'");
        wrapper.eq("create_user", user.getUsername());
        wrapper.eq("task_type", DcFjConstants.CLUE_STEP_HOSP);
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }

    @Override
    public IPage<YbFjProjectTask> queryHospTaskWaitAudit(IPage<YbFjProjectTask> page, String orgId, String projectOrgId) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        QueryWrapper<YbFjProjectTask> wrapper = new QueryWrapper<>();
        wrapper.eq("project_org_id", projectOrgId);
        wrapper.inSql("project_org_id", "select project_org_id from yb_fj_project_org b where b.org_id='"+orgId+"'");
        wrapper.eq("audit_state", DcFjConstants.CLUE_STATE_INIT);
        wrapper.eq("task_type", DcFjConstants.CLUE_STEP_HOSP);
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }

    @Override
    public IPage<YbFjProjectTask> queryCutTaskMineCreate(IPage<YbFjProjectTask> page, String orgId, String projectOrgId) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        QueryWrapper<YbFjProjectTask> wrapper = new QueryWrapper<>();
        wrapper.eq("project_org_id", projectOrgId);
        wrapper.inSql("project_org_id", "select project_org_id from yb_fj_project_org b where b.org_id='"+orgId+"'");
        //wrapper.eq("create_user", user.getUsername());
        wrapper.eq("task_type", DcFjConstants.CLUE_STEP_CUT);
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }
}
