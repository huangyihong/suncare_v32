/**
 * Test.java	  V1.0   2020年1月2日 上午11:11:26
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package org.jeecg.modules.engine.main;

import java.time.Duration;
import java.time.LocalDateTime;

import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.engine.EngineSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.ai.modules.engine.service.IEngineDrugService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg","com.ai"})
public class EngineOneDrugMain {
	public static void main(String[] args) throws Exception {
		LocalDateTime startTime = LocalDateTime.now();
		try {
			if(args.length<1) {
				throw new Exception("请输入批次号及药品编码参数");
			}
			new EngineSpringApplication(null, new Class<?>[] { EngineMain.class }).run(args);
			ApplicationContext context = SpringContextUtils.getApplicationContext();
			
			IEngineDrugService drugService = context.getBean(IEngineDrugService.class);			
			String batchId = args[0];
			String drugCode = args[1];
			drugService.generateUnreasonableAction(batchId, drugCode);
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