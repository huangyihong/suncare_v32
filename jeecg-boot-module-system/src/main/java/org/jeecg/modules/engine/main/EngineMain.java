/**
 * EngineMain.java	  V1.0   2019年12月25日 下午5:45:50
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package org.jeecg.modules.engine.main;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.engine.EngineSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.engine.service.IEngineCaseService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg","com.ai"})
public class EngineMain {

	public static void main(String[] args) {
		//SpringApplication.run(EngineMain.class, args);
		LocalDateTime startTime = LocalDateTime.now();
		try {
			if(args.length==0) {
				throw new Exception("请输入批次号参数");
			}
			new EngineSpringApplication(null, new Class<?>[] { EngineMain.class }).run(args);
			ApplicationContext context = SpringContextUtils.getApplicationContext();

			String batchId = args[0];

			Map<String, String> busiParams = new HashMap<String, String>();
			busiParams.put("batchId", batchId);
			List<String> list = ApiOauthUtil.responseArray("/oauth/api/task/batch/progress", busiParams, "post", String.class);
			Set<String> ruleTypeSet = new HashSet<>(list);
			IEngineCaseService service = context.getBean(IEngineCaseService.class);
			service.generateUnreasonableActionByBatch(batchId, ruleTypeSet);
		} catch(Exception e) {
			log.error("", e);
		} finally {
			LocalDateTime endTime = LocalDateTime.now();
			Duration duration = Duration.between(startTime, endTime);
			long seconds = duration.toMillis() / 1000;//相差毫秒数
			long minutes = seconds / 60;
			System.out.println("运行时长： "+minutes +"分钟，"+ seconds % 60+"秒 。");

			System.exit(0);
		}
	}
}
