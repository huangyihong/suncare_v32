package com.ai.modules.ybFj.service.impl;

import com.ai.modules.ybFj.dto.ChatOrgFileDto;
import com.ai.modules.ybFj.entity.YbFjChatOrgFile;
import com.ai.modules.ybFj.mapper.YbFjChatOrgFileMapper;
import com.ai.modules.ybFj.service.IYbFjChatOrgFileService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.util.CommonUtil;
import org.jeecg.common.util.DateUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Description: 飞检项目聊天附件
 * @Author: jeecg-boot
 * @Date:   2023-03-21
 * @Version: V1.0
 */
@Slf4j
@Service
public class YbFjChatOrgFileServiceImpl extends ServiceImpl<YbFjChatOrgFileMapper, YbFjChatOrgFile> implements IYbFjChatOrgFileService {

    @Override
    public void download(String fileId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        YbFjChatOrgFile chatFile = this.getById(fileId);
        if(chatFile==null) {
            throw new Exception("未找到文件");
        }
        String filePath = CommonUtil.UPLOAD_PATH + chatFile.getFilePath();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            String filename = new String(chatFile.getFileSrcname().getBytes("UTF-8"),"iso-8859-1");
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
        QueryWrapper<YbFjChatOrgFile> wrapper = new QueryWrapper<>();
        wrapper.in("file_id", fileIds.split(","));
        wrapper.orderByAsc("create_time");
        List<YbFjChatOrgFile> fileList = this.list(wrapper);
        downloadZip(response, fileList);
    }

    @Override
    public void downloadZip(HttpServletResponse response, List<YbFjChatOrgFile> fileList) throws Exception {
        ZipOutputStream zos = null;
        BufferedInputStream bis = null;
        int len = 0;
        try {
            response.setContentType("application/zip");
            response.setHeader("content-disposition", "attachment;filename=" + DateUtils.getDate("yyyyMMddHHmmss")+".zip");
            zos = new ZipOutputStream(response.getOutputStream());
            byte[] buf = new byte[8192];
            Map<String, Integer> fileCntMap = new HashMap<String, Integer>();
            for(YbFjChatOrgFile record : fileList) {
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
    public IPage<YbFjChatOrgFile> queryChatOrgFile(IPage<YbFjChatOrgFile> page, ChatOrgFileDto dto) throws Exception {
        if(StringUtils.isBlank(dto.getOrgId())) {
            throw new Exception(("orgId参数不能为空"));
        }
        QueryWrapper<YbFjChatOrgFile> wrapper = new QueryWrapper<>();
        wrapper.eq("org_id", dto.getOrgId());
        if(StringUtils.isNotBlank(dto.getFileSrcname())) {
            //文件关键字
            wrapper.like("file_srcname", dto.getFileSrcname());
        }
        if(StringUtils.isNotBlank(dto.getUsername())) {
            //发送人
            wrapper.like("create_username", dto.getUsername());
        }
        wrapper.orderByDesc("create_time");
        return page(page, wrapper);
    }
}
