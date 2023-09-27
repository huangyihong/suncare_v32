package com.ai.modules.admin.controller;


import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai.modules.engine.util.AliyunApiUtil;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;

import cn.hutool.core.util.RandomUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags="发送短信")
@RequestMapping("/admin/sms")
public class SmsController {
	@Autowired
    private RedisUtil redisUtil;

	@AutoLog(value = "发送登录验证码")
	@ApiOperation(value = "发送登录验证码", notes = "发送登录验证码")
	@PostMapping(value = "/send")
	public Result<?> send(@RequestParam(name = "phoneNum") String phoneNum) throws Exception {
		String smsTemplateId = "SMS_214820624";
		String captcha = RandomUtil.randomNumbers(6);
		String templateParam = "{\"code\":\"%s\"}";
        templateParam = String.format(templateParam, captcha);
		SendSmsResponse smsResponse = AliyunApiUtil.sendSms(phoneNum, smsTemplateId, templateParam);
		if("OK".equals(smsResponse.getCode())) {
			//短信发送成功，验证码写入redis中2分钟内有效
			redisUtil.set(phoneNum, captcha, 120);
		} else {
			return Result.error(smsResponse.getMessage());
		}
		return Result.ok();
	}
}
