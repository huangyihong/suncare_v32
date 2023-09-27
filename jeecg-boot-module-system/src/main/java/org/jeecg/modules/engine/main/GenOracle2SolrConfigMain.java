/**
 * Test.java	  V1.0   2019年11月25日 下午5:22:41
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

import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.engine.EngineSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.ai.modules.engine.service.GenOracle2SolrConfigService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg","com.ai"})
public class GenOracle2SolrConfigMain {

	public static void main(String[] args) throws Exception {
		if(args.length == 0 || args.length<2){
			throw new Exception("请指定表名以及表的主键（作为solr表id）");
		}
		String name = args[0].toUpperCase();
		String pk = args[1].toUpperCase();		
		
		LocalDateTime startTime = LocalDateTime.now();
		try {
			new EngineSpringApplication(null, new Class<?>[] { EngineMain.class }).run(args);
			ApplicationContext context = SpringContextUtils.getApplicationContext();
			GenOracle2SolrConfigService service = (GenOracle2SolrConfigService)context.getBean(GenOracle2SolrConfigService.class);
			service.genSolrConfig(name, pk);
		} catch(Exception e) {
			log.error("", e);
		} finally {
			LocalDateTime endTime = LocalDateTime.now();
			Duration duration = Duration.between(startTime, endTime);
			long seconds = duration.toMillis() / 1000;//相差毫秒数
			System.out.println("运行时长： "+seconds+"秒 。");
			
			System.exit(0);						
		}		
	}

}
