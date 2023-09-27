package com.ai.modules.mail.service.impl;

import java.io.ByteArrayOutputStream;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.modules.mail.service.IMailService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MailServiceImpl implements IMailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.fromMail.addr}")
    private String from;

    @Override
    public boolean sendMail(String to,String cc, String subject, String content, ByteArrayOutputStream os, String attachmentFilename) throws Exception {
    	boolean flag = true;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to.split(","));
            if(StringUtils.isNotBlank(cc)) {
            	 helper.setCc(cc.split(","));
            }
            helper.setSubject(subject);
            helper.setText(content,true);
            if (null != os) {
                //附件
                InputStreamSource inputStream = new ByteArrayResource(os.toByteArray());
                helper.addAttachment(attachmentFilename, inputStream);
            }

            mailSender.send(message);
            log.info("邮件已经发送。");
        } catch (Exception e) {
        	flag = false;
            log.error("发送简单邮件时发生异常！", e);
        }
        return flag;
    }
}
