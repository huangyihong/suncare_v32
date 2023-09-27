package com.ai.modules.ybFj.service;

import com.ai.modules.ybFj.entity.YbFjChatOrgLog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: 飞检项目聊天记录
 * @Author: jeecg-boot
 * @Date:   2023-03-21
 * @Version: V1.0
 */
public interface IYbFjChatOrgLogService extends IService<YbFjChatOrgLog> {

    IPage<YbFjChatOrgLog> queryChatOrgLog(IPage<YbFjChatOrgLog> page, String orgId);

    /**
     *
     * 功能描述：向医院端发送消息
     * @author zhangly
     * @date 2023-03-21 11:34:11
     *
     * @param orgId
     * @param chatMsg
     *
     * @return void
     *
     */
    YbFjChatOrgLog sendFromServer(String orgId, String chatMsg) throws Exception;

    /**
     *
     * 功能描述：向医院端发送文件
     * @author zhangly
     * @date 2023-03-21 12:14:54
     *
     * @param orgId
     * @param multipartFile
     *
     * @return void
     *
     */
    YbFjChatOrgLog sendFromServer(String orgId, MultipartFile multipartFile) throws Exception;

    /**
     *
     * 功能描述：向服务端发送消息
     * @author zhangly
     * @date 2023-03-21 11:34:11
     *
     * @param orgId
     * @param chatMsg
     *
     * @return void
     *
     */
    YbFjChatOrgLog sendFromOrg(String orgId, String chatMsg) throws Exception;

    /**
     *
     * 功能描述：向服务端发送文件
     * @author zhangly
     * @date 2023-03-21 12:14:54
     *
     * @param orgId
     * @param multipartFile
     *
     * @return void
     *
     */
    YbFjChatOrgLog sendFromOrg(String orgId, MultipartFile multipartFile) throws Exception;

    /**
     *
     * 功能描述：设置已读
     * @author zhangly
     * @date 2023-03-21 17:40:17
     *
     * @param logIds
     *
     * @return void
     *
     */
    void settingRead(String logIds);
}
