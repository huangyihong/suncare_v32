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

import com.ai.modules.engine.service.IEngineChargeService;
import com.ai.modules.engine.service.IEngineDrugService;
import com.ai.modules.engine.service.IEngineTreatService;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.medical.service.IMedicalDrugRuleService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg","com.ai"})
public class EngineTrailMain {
	public static void main(String[] args) throws Exception {
		LocalDateTime startTime = LocalDateTime.now();
		try {
			if(args.length==0) {
				throw new Exception("请输入规则编号参数");
			}
			new EngineSpringApplication(null, new Class<?>[] { EngineMain.class }).run(args);
			ApplicationContext context = SpringContextUtils.getApplicationContext();						
			
			String ruleId = args[0];
			String source = null;
			if(args.length>1) {
				source = args[1];
			}
			IMedicalDrugRuleService ruleService = context.getBean(IMedicalDrugRuleService.class);
			MedicalDrugRule rule = ruleService.getById(ruleId);
			if("1".equals(rule.getRuleType())) {
				//药品合规
				IEngineDrugService drugService = context.getBean(IEngineDrugService.class);
				drugService.trailDrugAction(ruleId, source, SolrUtil.getLoginUserDatasource());
			} else if("2".equals(rule.getRuleType())) {
				//收费合规
				IEngineChargeService chargeService = context.getBean(IEngineChargeService.class);
				chargeService.trailChargeAction(ruleId, source, SolrUtil.getLoginUserDatasource());
			} else if("4".equals(rule.getRuleType())) {
				//诊疗合规
				IEngineTreatService treatService = context.getBean(IEngineTreatService.class);
				treatService.trailTreatAction(ruleId, source, SolrUtil.getLoginUserDatasource());
			}
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
