/**
 * CustomSpringApplication.java	  V1.0   2019年12月27日 下午3:30:02
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package org.jeecg.modules.engine;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ResourceLoader;

/**
 *
 * 功能描述：
 *
 * @author  zhangly
 * Date: 2019年12月27日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineSpringApplication extends SpringApplication {

	public EngineSpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
		super(resourceLoader, primarySources);
		//this.setWebApplicationType(WebApplicationType.NONE);
	}

	@Override
	protected ConfigurableApplicationContext createApplicationContext() {
		Class<?> contextClass = null;
		try {
			contextClass = Class.forName("org.jeecg.modules.engine.EngineServletWebServerApplicationContext");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return (ConfigurableApplicationContext) BeanUtils.instantiateClass(contextClass);
	}
}
