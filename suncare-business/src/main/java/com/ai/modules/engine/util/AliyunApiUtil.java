/**
 * AliyunApiUtil.java	  V1.0   2021年4月1日 下午4:29:59
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AliyunApiUtil {

	public static String accessKeyId;
	public static String accessKeySecret;
	public static String domain;

	@Value("${jeecg.sms.accessKeyId}")
	public void accessKeyId(String accessKeyId) {
		AliyunApiUtil.accessKeyId = accessKeyId;
    }

	@Value("${jeecg.sms.accessKeySecret}")
	public void accessKeySecret(String accessKeySecret) {
		AliyunApiUtil.accessKeySecret = accessKeySecret;
    }

	@Value("${jeecg.sms.domain}")
	public void domain(String domain) {
		AliyunApiUtil.domain = domain;
	}

	public static IAcsClient getAcsClient() throws Exception {
		DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
		DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Dysmsapi", domain);
		IAcsClient client = new DefaultAcsClient(profile);
		return client;
	}

	public static SendSmsResponse sendSms(String phoneNum, String smsTemplateId, String templateParam) throws Exception {
		SendSmsRequest request = new SendSmsRequest();
		// 必填:待发送手机号
		request.setPhoneNumbers(phoneNum);
		// 必填:短信签名-可在短信控制台中找到
		request.setSignName("亚信数据");
		// 必填:短信模板-可在短信控制台中找到
		request.setTemplateCode(smsTemplateId);
		// 可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
		request.setTemplateParam(templateParam);
		SendSmsResponse response = getAcsClient().getAcsResponse(request);
		return response;
	}
}
