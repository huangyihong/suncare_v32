package com.ai.modules.ybFj.service.impl;

import com.ai.common.utils.IdUtils;
import com.ai.modules.ybFj.constants.DcFjConstants;
import com.ai.modules.ybFj.entity.YbFjChatOrg;
import com.ai.modules.ybFj.entity.YbFjChatOrgFile;
import com.ai.modules.ybFj.entity.YbFjChatOrgLog;
import com.ai.modules.ybFj.mapper.YbFjChatOrgLogMapper;
import com.ai.modules.ybFj.service.IYbFjChatOrgFileService;
import com.ai.modules.ybFj.service.IYbFjChatOrgLogService;
import com.ai.modules.ybFj.service.IYbFjChatOrgService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Update;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.util.CommonUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @Description: 飞检项目聊天记录
 * @Author: jeecg-boot
 * @Date:   2023-03-21
 * @Version: V1.0
 */
@Service
public class YbFjChatOrgLogServiceImpl extends ServiceImpl<YbFjChatOrgLogMapper, YbFjChatOrgLog> implements IYbFjChatOrgLogService {

    @Autowired
    private IYbFjChatOrgService chatOrgService;
    @Autowired
    private IYbFjChatOrgFileService chatOrgFileService;

    @Override
    public IPage<YbFjChatOrgLog> queryChatOrgLog(IPage<YbFjChatOrgLog> page, String orgId) {
        QueryWrapper<YbFjChatOrgLog> wrapper = new QueryWrapper<>();
        wrapper.eq("org_id", orgId);
        wrapper.orderByDesc("create_time");
        return page(page, wrapper);
    }

    @Override
    public YbFjChatOrgLog sendFromServer(String orgId, String chatMsg) throws Exception {
        if(StringUtils.isBlank(orgId)) {
            throw new Exception(("orgId参数不能为空"));
        }
        YbFjChatOrgLog log = new YbFjChatOrgLog();
        log.setLogId(IdUtils.uuid());
        log.setOrgId(orgId);
        log.setChatMsg(chatMsg);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        log.setCreateTime(DateUtils.getDate());
        log.setCreateUser(user.getUsername());
        log.setCreateUsername(user.getRealname());
        log.setTransferType(DcFjConstants.CHAT_TRANSTER_TYPE_SYS);
        log.setChatType(DcFjConstants.CHAT_TYPE_TXT);
        log.setReadState(DcFjConstants.CHAT_READ_STATE_NO);
        this.save(log);

        YbFjChatOrg chatOrg = new YbFjChatOrg();
        chatOrg.setOrgId(orgId);
        chatOrg.setUpdateTime(DateUtils.getDate());
        chatOrg.setUpdateUser(user.getUsername());
        chatOrg.setUpdateUsername(user.getRealname());
        chatOrg.setTopTime(DateUtils.getDate());
        chatOrg.setChatTime(DateUtils.getDate());
        chatOrgService.updateById(chatOrg);
        return log;
    }

    private YbFjChatOrgFile bulidChatOrgFile(String orgId, MultipartFile multipartFile, String transferType) throws Exception {
        String filename = multipartFile.getOriginalFilename();
        int index = filename.lastIndexOf(".");
        // 文件扩展名
        String extname = filename.substring(index+1);
        String newname = DateUtils.getDate("yyyyMMddHHmmssSSS")+"."+extname;
        String path = "/fj/chat/" + DateUtils.getDate("yyyyMM");
        File folder = new File(CommonUtil.UPLOAD_PATH + path);
        if(!folder.exists()) {
            folder.mkdirs();
        }
        String savePath = folder + File.separator + newname;
        File saveFile = new File(savePath);
        FileCopyUtils.copy(multipartFile.getBytes(), saveFile);

        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        YbFjChatOrgFile chatFile = new YbFjChatOrgFile();
        chatFile.setFileId(IdUtils.uuid());
        chatFile.setOrgId(orgId);
        chatFile.setTransferType(transferType);
        if(DcFjConstants.FILE_EXT_XLS.equalsIgnoreCase(extname)
                || DcFjConstants.FILE_EXT_XLSX.equalsIgnoreCase(extname)) {
            chatFile.setFileType(DcFjConstants.FILE_TYPE_EXCEL);
        } else if(DcFjConstants.FILE_EXT_DOC.equalsIgnoreCase(extname)
                || DcFjConstants.FILE_EXT_DOCX.equalsIgnoreCase(extname)) {
            chatFile.setFileType(DcFjConstants.FILE_TYPE_WORD);
        } else if(DcFjConstants.FILE_EXT_PDF.equalsIgnoreCase(extname)) {
            chatFile.setFileType(DcFjConstants.FILE_TYPE_PDF);
        } else {
            chatFile.setFileType(extname);
        }
        chatFile.setFileSrcname(filename);
        chatFile.setFileName(newname);
        chatFile.setFilePath(path + File.separator + newname);
        chatFile.setFileSize(saveFile.length());
        chatFile.setCreateTime(DateUtils.getDate());
        chatFile.setCreateUser(user.getUsername());
        chatFile.setCreateUsername(user.getRealname());
        return chatFile;
    }

    @Override
    public YbFjChatOrgLog sendFromServer(String orgId, MultipartFile multipartFile) throws Exception {
        if(StringUtils.isBlank(orgId)) {
            throw new Exception(("orgId参数不能为空"));
        }
        YbFjChatOrgLog log = new YbFjChatOrgLog();
        log.setLogId(IdUtils.uuid());
        log.setOrgId(orgId);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        log.setCreateTime(DateUtils.getDate());
        log.setCreateUser(user.getUsername());
        log.setCreateUsername(user.getRealname());
        log.setTransferType(DcFjConstants.CHAT_TRANSTER_TYPE_SYS);
        log.setChatType(DcFjConstants.CHAT_TYPE_FILE);
        log.setReadState(DcFjConstants.CHAT_READ_STATE_NO);

        YbFjChatOrgFile chatFile = this.bulidChatOrgFile(orgId, multipartFile, DcFjConstants.CHAT_TRANSTER_TYPE_SYS);
        log.setChatMsg(JSON.toJSONString(chatFile));
        chatOrgFileService.save(chatFile);

        this.save(log);
        YbFjChatOrg chatOrg = new YbFjChatOrg();
        chatOrg.setOrgId(orgId);
        chatOrg.setUpdateTime(DateUtils.getDate());
        chatOrg.setUpdateUser(user.getUsername());
        chatOrg.setUpdateUsername(user.getRealname());
        chatOrg.setTopTime(DateUtils.getDate());
        chatOrg.setChatTime(DateUtils.getDate());
        chatOrgService.updateById(chatOrg);
        return log;
    }

    @Override
    public YbFjChatOrgLog sendFromOrg(String orgId, String chatMsg) throws Exception {
        if(StringUtils.isBlank(orgId)) {
            throw new Exception(("orgId参数不能为空"));
        }
        YbFjChatOrgLog log = new YbFjChatOrgLog();
        log.setLogId(IdUtils.uuid());
        log.setOrgId(orgId);
        log.setChatMsg(chatMsg);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        log.setCreateTime(DateUtils.getDate());
        log.setCreateUser(user.getUsername());
        log.setCreateUsername(user.getRealname());
        log.setTransferType(DcFjConstants.CHAT_TRANSTER_TYPE_ORG);
        log.setChatType(DcFjConstants.CHAT_TYPE_TXT);
        log.setReadState(DcFjConstants.CHAT_READ_STATE_NO);
        this.save(log);

        YbFjChatOrg chatOrg = new YbFjChatOrg();
        chatOrg.setOrgId(orgId);
        chatOrg.setUpdateTime(DateUtils.getDate());
        chatOrg.setUpdateUser(user.getUsername());
        chatOrg.setUpdateUsername(user.getRealname());
        chatOrg.setTopTime(DateUtils.getDate());
        chatOrg.setChatTime(DateUtils.getDate());
        chatOrgService.updateById(chatOrg);
        return log;
    }

    @Override
    public YbFjChatOrgLog sendFromOrg(String orgId, MultipartFile multipartFile) throws Exception {
        if(StringUtils.isBlank(orgId)) {
            throw new Exception(("orgId参数不能为空"));
        }
        YbFjChatOrgLog log = new YbFjChatOrgLog();
        log.setLogId(IdUtils.uuid());
        log.setOrgId(orgId);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        log.setCreateTime(DateUtils.getDate());
        log.setCreateUser(user.getUsername());
        log.setCreateUsername(user.getRealname());
        log.setTransferType(DcFjConstants.CHAT_TRANSTER_TYPE_ORG);
        log.setChatType(DcFjConstants.CHAT_TYPE_FILE);
        log.setReadState(DcFjConstants.CHAT_READ_STATE_NO);

        YbFjChatOrgFile chatFile = this.bulidChatOrgFile(orgId, multipartFile, DcFjConstants.CHAT_TRANSTER_TYPE_ORG);
        log.setChatMsg(JSON.toJSONString(chatFile));
        chatOrgFileService.save(chatFile);

        this.save(log);
        YbFjChatOrg chatOrg = new YbFjChatOrg();
        chatOrg.setOrgId(orgId);
        chatOrg.setUpdateTime(DateUtils.getDate());
        chatOrg.setUpdateUser(user.getUsername());
        chatOrg.setUpdateUsername(user.getRealname());
        chatOrg.setTopTime(DateUtils.getDate());
        chatOrg.setChatTime(DateUtils.getDate());
        chatOrgService.updateById(chatOrg);
        return log;
    }

    @Override
    public void settingRead(String logIds) {
        YbFjChatOrgLog entity = new YbFjChatOrgLog();
        entity.setReadState(DcFjConstants.CHAT_READ_STATE_YES);
        String[] ids = logIds.split(",");
        QueryWrapper<YbFjChatOrgLog> wrapper = new QueryWrapper<>();
        wrapper.in("log_id", ids);
        this.update(entity, wrapper);
    }
}
