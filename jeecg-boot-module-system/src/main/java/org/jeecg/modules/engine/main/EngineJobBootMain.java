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

import org.jeecg.modules.engine.EngineSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ai.modules.engine.job.handler.AbstractJobHandler;
import com.ai.modules.engine.job.handler.EngineJobHandler;
import com.ai.modules.engine.job.meta.BaseMeta;
import com.ai.modules.engine.job.meta.EngineJobMeta;
import com.ai.modules.engine.job.util.JobParserUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg","com.ai"})
public class EngineJobBootMain {

	public static void main(String[] args) {
		//SpringApplication.run(EngineMain.class, args);
		LocalDateTime startTime = LocalDateTime.now();
		try {
			if(args.length==0) {
				StringBuilder sb = new StringBuilder();
				sb.append("\n参数参考如下：");
				sb.append("\n-serial 20101016164021546 -f EngineJobHandler -ds funan -pc 95f98c140493adf53be94df89475bf2b");
				sb.append("\n-serial 流水号");
				sb.append("\n-f 处理程序");
				sb.append("\n-ds solr数据源");
				sb.append("\n-pc 批次号");
				log.info(sb.toString());
				throw new Exception("请输入参数");
			}
			new EngineSpringApplication(null, new Class<?>[] { EngineJobBootMain.class }).run(args);
									
			BaseMeta base = JobParserUtil.parse(args);
			EngineJobMeta meta = new EngineJobMeta(base);
			AbstractJobHandler handler = new EngineJobHandler(meta);
			handler.run();
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
