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

import com.ai.modules.engine.service.IEngineCaseService;
import com.ai.modules.review.service.IReviewNewPushService;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.engine.EngineSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg","com.ai"})
public class ExportActionSolrMain {

	public static void main(String[] args) {
		//SpringApplication.run(EngineMain.class, args);
		LocalDateTime startTime = LocalDateTime.now();
		try {
			if(args.length==0) {
				throw new Exception("请输入批次号参数");
			}
			boolean isProject= false;
			if(args.length>1){
				isProject = true;
			}
			new EngineSpringApplication(null, new Class<?>[] { ExportActionSolrMain.class }).run(args);
			ApplicationContext context = SpringContextUtils.getApplicationContext();
			String batchId = args[0];
			IReviewNewPushService service = context.getBean(IReviewNewPushService.class);
			service.exportActionSolrMain(batchId,isProject);
			//service.generateMedicalUnreasonableAction(batchId, "b8888147ac034718b99c5b0c1b24a469", "a655b1d02fd04bbcb2db3879c731fb1e");
			//service.generateUnreasonableActionAll(batchId);
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
