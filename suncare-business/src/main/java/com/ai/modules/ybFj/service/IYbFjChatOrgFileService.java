package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.dto.ChatOrgFileDto;
import com.ai.modules.ybFj.entity.YbFjChatOrgFile;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description: 飞检项目聊天附件
 * @Author: jeecg-boot
 * @Date:   2023-03-21
 * @Version: V1.0
 */
public interface IYbFjChatOrgFileService extends IService<YbFjChatOrgFile> {

    void download(String fileId, HttpServletRequest request, HttpServletResponse response) throws Exception;

    void downloadZip(HttpServletResponse response, String fileIds) throws Exception;

    void downloadZip(HttpServletResponse response, List<YbFjChatOrgFile> fileList) throws Exception;

    IPage<YbFjChatOrgFile> queryChatOrgFile(IPage<YbFjChatOrgFile> page, ChatOrgFileDto dto) throws Exception;
}
