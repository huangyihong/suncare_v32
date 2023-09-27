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

import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.entity.MedicalFormalBehavior;
import com.ai.modules.his.entity.HisMedicalFormalBusi;
import com.ai.modules.his.entity.HisMedicalFormalCaseBusi;
import com.ai.modules.his.service.IHisMedicalFormalBusiService;
import com.ai.modules.his.service.IHisMedicalFormalCaseBusiService;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.medical.service.IMedicalDrugRuleService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jxl.Workbook;
import jxl.write.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.engine.EngineSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.Boolean;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg","com.ai"})
public class testMainBusi3 {
	private static IMedicalDictService dictService;
	public static void main(String[] args) {
		//SpringApplication.run(EngineMain.class, args);
		LocalDateTime startTime = LocalDateTime.now();
		try {
			new EngineSpringApplication(null, new Class<?>[] { testMainBusi3.class }).run(args);
			ApplicationContext context = SpringContextUtils.getApplicationContext();
			IMedicalDrugRuleService drugRuleService = context.getBean(IMedicalDrugRuleService.class);
			dictService = context.getBean(IMedicalDictService.class);

			Page<MedicalDrugRule> page = new Page<>(1, 20);

			List<MedicalDrugRule> list = drugRuleService.list(
					new QueryWrapper<MedicalDrugRule>().select("RULE_ID","LIMIT_SCOPE", "RULE_TYPE")
							.in("RULE_TYPE", Arrays.asList("1", "2"))
			);


			for(MedicalDrugRule bean: list){
				if(StringUtils.isNotBlank(bean.getLimitScope())){
					String text = translateMedicalDictValue( "LIMIT_SCOPE", bean.getLimitScope());
					bean.setActionName("限" + text);
					bean.setActionType("1".equals(bean.getRuleType())?"DRUG":"CHARGE");
//					log.info(JSONObject.toJSONString(bean));
				}
				bean.setLimitScope(null);
				bean.setRuleType(null);
			}
			drugRuleService.updateBatchById(list);

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

	private static String translateMedicalDictValue(String code, String key) {
		if (oConvertUtils.isEmpty(key)) {
			return null;
		}
		StringBuilder textValue = new StringBuilder();

		String[] groups = key.split("\\|");
		for (String group : groups) {
			String[] keys = group.split(",");
			if (!"".equals(textValue.toString())) {
				textValue.append("|");
			}
			Arrays.sort(keys, String::compareTo);
			log.info(" 字典 key : " + Arrays.toString(keys));
			StringBuilder groupValue = new StringBuilder();
			for (String k : keys) {
				String tmpValue = null;
				log.debug(" 字典 key : " + k);
				if (k.trim().length() == 0) {
					continue; //跳过循环
				}
				tmpValue = dictService.queryDictTextByKey(code, k.trim());

				if (tmpValue != null) {
					if (!"".equals(groupValue.toString())) {
						groupValue.append("及");
					}
					groupValue.append(tmpValue);
				}
			}
			textValue.append(groupValue);
		}

		return textValue.toString();
	}

}
