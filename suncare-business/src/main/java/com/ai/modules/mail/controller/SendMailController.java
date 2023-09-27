package com.ai.modules.mail.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai.modules.mail.service.IMailService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 发送邮件
 */
@Slf4j
@Api(tags="发送邮件")
@RestController
@RequestMapping("/mail/sendMail")
public class SendMailController {
	@Autowired
	private IMailService mailService;
	
	/**
	 * 发送邮件
	 * 
	 * @param mailMessage
	 * @return
	 * @throws Exception 
	 */
	@AutoLog(value = "发送邮件")
	@ApiOperation(value="发送邮件", notes="发送邮件")
	@PostMapping(value = "/send")
	public Result<?> send(@RequestParam String to,String cc,@RequestParam String subject,@RequestParam String content) throws Exception {
		boolean flag = mailService.sendMail(to,cc,subject,content,null,null);
		return Result.ok(flag);
	}


}
