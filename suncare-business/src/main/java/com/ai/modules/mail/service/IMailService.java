package com.ai.modules.mail.service;

import java.io.ByteArrayOutputStream;

public interface IMailService {

    /**
     * 发送邮件
     * @param to 目的地
     * @param cc 抄送人
     * @param subject 主题
     * @param content 内容
     * @param os 附件
     * @param attachmentFilename 附件名
     * @throws Exception
     */
    public boolean sendMail(String to,String cc,String subject, String content,  ByteArrayOutputStream os, String attachmentFilename) throws Exception;
}
