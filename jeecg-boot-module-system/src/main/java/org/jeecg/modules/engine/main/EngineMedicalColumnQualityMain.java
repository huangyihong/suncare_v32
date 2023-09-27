/**
 * EngineMedicalColumnQualityMain.java	  V1.0   2021年3月15日 下午2:11:33
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package org.jeecg.modules.engine.main;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.engine.EngineSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.ai.modules.medical.service.IMedicalColumnQualityService;
import com.ai.modules.medical.vo.MedicalColumnQualityVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg","com.ai"})
public class EngineMedicalColumnQualityMain {

	public static void main(String[] args) {
		LocalDateTime startTime = LocalDateTime.now();
		try {
			new EngineSpringApplication(null, new Class<?>[] { EngineMain.class }).run(args);
			ApplicationContext context = SpringContextUtils.getApplicationContext();
			IMedicalColumnQualityService service = context.getBean(IMedicalColumnQualityService.class);
			service.computeMedicalColumnQualityVO();
			log.info("rule column quality is success");
		} catch(Exception e) {
			log.error("", e);
			log.info("rule column quality is fail");
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
