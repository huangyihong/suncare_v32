/**
 * EngineMain.java	  V1.0   2019年12月25日 下午5:45:50
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package org.jeecg.test;

import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.mapper.MedicalFormalCaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.jeecg.JeecgApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg","com.ai"})
public class testMain {

	public static void main(String[] args) {
		//SpringApplication.run(EngineMain.class, args);
		LocalDateTime startTime = LocalDateTime.now();
		try {
		/*	new EngineSpringApplication(null, new Class<?>[] { testMain.class }).run(args);
			ApplicationContext context = SpringContextUtils.getApplicationContext();
			INewsWgblInfoService service = context.getBean(INewsWgblInfoService.class);*/
			ConfigurableApplicationContext context=
					new SpringApplicationBuilder(testMain.class)
							.properties
							//这个路径不提示代码,所以最好用默认
									("spring.config.location=classpath:/application-dev.yml")
//							.properties("spring.profiles.active=dev")
							.run(args);

		/*	SpringApplication application = new SpringApplication( testMain.class );

			// 如果是web环境，默认创建AnnotationConfigEmbeddedWebApplicationContext，因此要指定applicationContextClass属性
			application.setApplicationContextClass( AnnotationConfigApplicationContext.class );
			application.run( args );*/

			// 如果不想让spring容器退出，可以使用以下代码
		/*	CountDownLatch latch = new CountDownLatch( 1 );
			latch.await();*/

		/*	String[] ids = {"12796915_mz_49389526X370785_A03",
					"18462_mz_493895817370785_A03",
					"1846341_mz_12370785MB28490674_A03"
			};
			*//*List<NewsWgblInfo> list =  service.listByVisitIds(Arrays.asList(ids));
			System.out.println("count:" + list.size());*//*
			SolrQuery solrQuery = new SolrQuery("*:*");
			solrQuery.addFilterQuery("id:190901000037188_mz_PDY00212X341225_A02");

			SolrUtil.export(solrQuery, EngineUtil.DWB_MASTER_INFO, (map,index) -> {
				log.info(JSON.toJSONString(map));
			});*/

			SolrQuery solrQuery = new SolrQuery("*:*");
			/*solrQuery.addFilterQuery("HOSPLEVEL:3");
			solrQuery.addFilterQuery("VISITTYPE:住院");*/
			solrQuery.addFilterQuery("id:190901000037188_mz_PDY00212X341225_A02");
//			solrQuery.setRows(3);
			SolrUtil.export(solrQuery, EngineUtil.DWB_MASTER_INFO, (map, index) -> {
				log.info(index + "--" + map.toString());
			});
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

	private static void settingStatus(ApplicationContext context, MedicalFormalCase formalCase, Integer status) {
		MedicalFormalCaseMapper caseMapper = context.getBean(MedicalFormalCaseMapper.class);
		MedicalFormalCase entity = new MedicalFormalCase();
		entity.setResultDataStatus(status);
		entity.setResultEndTime(new Date());
		caseMapper.update(entity, new QueryWrapper<MedicalFormalCase>().eq("CASE_ID", formalCase.getCaseId()));
	}
}
