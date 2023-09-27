package com.ai.modules.ybFj.service.impl;

import cn.hutool.core.io.FileUtil;
import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.dto.ClueStepFileDto;
import com.ai.modules.ybFj.dto.ProjectFileDto;
import com.ai.modules.ybFj.entity.YbFjProjectClueFile;
import com.ai.modules.ybFj.mapper.YbFjProjectClueFileMapper;
import com.ai.modules.ybFj.service.IYbFjProjectClueFileService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.util.CommonUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Description: 飞检项目线索附件
 * @Author: jeecg-boot
 * @Date:   2023-03-08
 * @Version: V1.0
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class YbFjProjectClueFileServiceImpl extends ServiceImpl<YbFjProjectClueFileMapper, YbFjProjectClueFile> implements IYbFjProjectClueFileService {

    @Override
    public List<YbFjProjectClueFile> queryProjectClueFiles(String clueIds) {
        String[] ids = clueIds.split(",");
        QueryWrapper<YbFjProjectClueFile> wrapper = new QueryWrapper<>();
        wrapper.in("clue_id", ids);
        wrapper.eq("step_group", DcFjConstants.CLUE_STEP_SUBMIT);
        wrapper.eq("step_type", DcFjConstants.CLUE_STEP_SUBMIT);
        wrapper.eq("oper_type", DcFjConstants.FILE_OPER_TYPE_UP);
        return this.list(wrapper);
    }

    @Override
    public void deleteClueFile(QueryWrapper<YbFjProjectClueFile> fileQueryWrapper) {
        List<YbFjProjectClueFile> fileList = this.list(fileQueryWrapper);
        if(fileList!=null && fileList.size()>0) {
            for(YbFjProjectClueFile record : fileList) {
                String filePath = CommonUtil.UPLOAD_PATH + record.getFilePath();
                FileUtil.del(filePath);
            }
        }
        this.remove(fileQueryWrapper);
    }

    @Override
    public void deleteClueFile(String fileId) {
        YbFjProjectClueFile record = this.getById(fileId);
        String filePath = CommonUtil.UPLOAD_PATH + record.getFilePath();
        FileUtil.del(filePath);
        this.removeById(fileId);
    }

    @Override
    public void deleteClueFiles(String fileIds) {
        String[] ids = fileIds.split(",");
        QueryWrapper<YbFjProjectClueFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.in("file_id", ids);
        deleteClueFile(fileQueryWrapper);
    }

    @Override
    public IPage<YbFjProjectClueFile> queryProjectClueFilesFromStep(IPage<YbFjProjectClueFile> page, ClueStepFileDto dto) {
        QueryWrapper<YbFjProjectClueFile> wrapper = new QueryWrapper<YbFjProjectClueFile>();
        wrapper.eq("project_org_id", dto.getProjectOrgId());
        if(StringUtils.isNotBlank(dto.getCuleId())) {
            wrapper.eq("clue_id", dto.getCuleId());
        }
        if(StringUtils.isNotBlank(dto.getStepGroup())) {
            wrapper.eq("step_group", dto.getStepGroup());
        }
        if(StringUtils.isNotBlank(dto.getStepType())) {
            wrapper.eq("step_type", dto.getStepType());
        }
        if(dto.isMine()) {
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            wrapper.eq("create_user", user.getUsername());
        }
        wrapper.eq("oper_type", DcFjConstants.FILE_OPER_TYPE_UP);
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }

    @Override
    public IPage<YbFjProjectClueFile> queryProjectClueFilesByTemplate(IPage<YbFjProjectClueFile> page, ClueStepFileDto dto) {
        QueryWrapper<YbFjProjectClueFile> wrapper = new QueryWrapper<YbFjProjectClueFile>();
        wrapper.eq("project_org_id", dto.getProjectOrgId());
        if(StringUtils.isNotBlank(dto.getCuleId())) {
            wrapper.eq("clue_id", dto.getCuleId());
        }
        if(StringUtils.isNotBlank(dto.getStepGroup())) {
            wrapper.eq("step_group", dto.getStepGroup());
        }
        if(StringUtils.isNotBlank(dto.getStepType())) {
            wrapper.eq("step_type", dto.getStepType());
        }
        if(dto.isMine()) {
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            wrapper.eq("create_user", user.getUsername());
        }
        wrapper.eq("oper_type", DcFjConstants.FILE_OPER_TYPE_OUT);
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }

    @Override
    public List<YbFjProjectClueFile> queryProjectTaskFiles(String taskId) {
        QueryWrapper<YbFjProjectClueFile> wrapper = new QueryWrapper<>();
        wrapper.in("clue_id", taskId);
        wrapper.eq("oper_type", DcFjConstants.FILE_OPER_TYPE_UP);
        return this.list(wrapper);
    }

    @Override
    public void download(String fileId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        YbFjProjectClueFile clueFile = this.getById(fileId);
        if(clueFile==null) {
            throw new Exception("未找到文件");
        }
        String filePath = CommonUtil.UPLOAD_PATH + clueFile.getFilePath();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            String filename = new String(clueFile.getFileSrcname().getBytes("UTF-8"),"iso-8859-1");
            response.setContentType("application/force-download");
            response.setHeader("Content-Disposition", "attachment;fileName=" + filename);
            inputStream = new BufferedInputStream(new FileInputStream(new File(filePath)));
            outputStream = response.getOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            response.flushBuffer();
        } catch (Exception e) {
            log.info("文件下载失败" + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
    @Override
    public void downloadZip(HttpServletResponse response, String fileIds) throws Exception {
        QueryWrapper<YbFjProjectClueFile> wrapper = new QueryWrapper<>();
        wrapper.in("file_id", fileIds.split(","));
        List<YbFjProjectClueFile> fileList = this.list(wrapper);
        downloadZip(response, fileList);
    }

    @Override
    public void downloadZip(HttpServletResponse response, List<YbFjProjectClueFile> fileList) throws Exception  {
        ZipOutputStream zos = null;
        BufferedInputStream bis = null;
        int len = 0;
        try {
            response.setContentType("application/zip");
            response.setHeader("content-disposition", "attachment;filename=" + DateUtils.getDate("yyyyMMddHHmmss")+".zip");
            zos = new ZipOutputStream(response.getOutputStream());
            byte[] buf = new byte[8192];
            Map<String, Integer> fileCntMap = new HashMap<String, Integer>();
            for(YbFjProjectClueFile record : fileList) {
                String filename = record.getFileSrcname();
                int cnt = 1;
                if(!fileCntMap.containsKey(filename)) {
                    fileCntMap.put(filename, cnt);
                } else {
                    cnt = fileCntMap.get(filename) + 1;
                    fileCntMap.put(filename, cnt);
                    int index = filename.lastIndexOf(".");
                    String extname = filename.substring(index);
                    filename = StringUtils.replace(filename, extname, "");
                    filename = filename + "_" + cnt + extname;
                }
                ZipEntry ze = new ZipEntry(filename);
                zos.putNextEntry(ze);
                File file = new File(CommonUtil.UPLOAD_PATH + record.getFilePath());
                bis = new BufferedInputStream(new FileInputStream(file));
                while ((len = bis.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
            }
            zos.closeEntry();
        } catch (Exception e) {
            throw e;
        } finally {
            if(bis != null){
                try{
                    bis.close();
                }catch(Exception e){
                }
            }
            if(zos != null){
                try{
                    zos.close();
                }catch(Exception e){
                }
            }
        }
    }

    @Override
    public void downloadZip(ZipOutputStream zos, List<YbFjProjectClueFile> fileList) throws Exception  {
        BufferedInputStream bis = null;
        int len = 0;
        try {
            byte[] buf = new byte[8192];
            Map<String, Integer> fileCntMap = new HashMap<String, Integer>();
            for(YbFjProjectClueFile record : fileList) {
                String filename = record.getFileSrcname();
                int cnt = 1;
                if(!fileCntMap.containsKey(filename)) {
                    fileCntMap.put(filename, cnt);
                } else {
                    cnt = fileCntMap.get(filename) + 1;
                    fileCntMap.put(filename, cnt);
                    int index = filename.lastIndexOf(".");
                    String extname = filename.substring(index);
                    filename = StringUtils.replace(filename, extname, "");
                    filename = filename + "_" + cnt + extname;
                }
                ZipEntry ze = new ZipEntry(filename);
                zos.putNextEntry(ze);
                File file = new File(CommonUtil.UPLOAD_PATH + record.getFilePath());
                bis = new BufferedInputStream(new FileInputStream(file));
                while ((len = bis.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
            }
            zos.closeEntry();
        } catch (Exception e) {
            throw e;
        } finally {
            if(bis != null){
                try{
                    bis.close();
                }catch(Exception e){
                }
            }
        }
    }

    @Override
    public IPage<YbFjProjectClueFile> queryProjectFiles(IPage<YbFjProjectClueFile> page, ProjectFileDto dto) {
        QueryWrapper<YbFjProjectClueFile> wrapper = new QueryWrapper<YbFjProjectClueFile>();
        wrapper.eq("project_id", dto.getProjectId());
        if(StringUtils.isNotBlank(dto.getStepGroup())) {
            wrapper.eq("step_group", dto.getStepGroup());
        }
        if(StringUtils.isNotBlank(dto.getOperType())) {
            wrapper.eq("oper_type", dto.getOperType());
            //排除医院上传的
            wrapper.notIn("step_type", DcFjConstants.FILE_STEP_HOSP_TASK_AUDIT, DcFjConstants.FILE_STEP_CUT_TASK);
        }
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }

    @Override
    public IPage<YbFjProjectClueFile> queryProjectOrgUploadFiles(IPage<YbFjProjectClueFile> page, String projectId) {
        QueryWrapper<YbFjProjectClueFile> wrapper = new QueryWrapper<YbFjProjectClueFile>();
        wrapper.eq("project_id", projectId);
        wrapper.in("step_type", DcFjConstants.FILE_STEP_HOSP_TASK_AUDIT, DcFjConstants.FILE_STEP_CUT_TASK);
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }
}
