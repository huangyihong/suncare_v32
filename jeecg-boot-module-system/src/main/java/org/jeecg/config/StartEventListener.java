/**
 * StartEventListener.java	  V1.0   2022年4月19日 下午5:30:51
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package org.jeecg.config;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.jeecg.common.aspect.annotation.AutoResultMap;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 项目启动事件通知
 * @author  zhangly
 * Date: 2022年4月19日
 */
@Slf4j
@Configuration
public class StartEventListener {
	
	@Autowired
	protected SqlSessionTemplate sqlSessionTemplate;

	@Order
	@EventListener(WebServerInitializedEvent.class)
	public void afterStart(WebServerInitializedEvent event) throws Exception {
		Environment environment = event.getApplicationContext().getEnvironment();				
		int port = event.getWebServer().getPort();
		String profile = StringUtils.arrayToCommaDelimitedString(environment.getActiveProfiles());
		log.warn("======启动完成，当前使用的端口:[{}]，环境变量:[{}]=====", port, profile);				
	}
	
	@PostConstruct
    public void initAutoResultMap() throws Exception {
    	//扫描指定包，遇到AutoResultMap注解类自动注入到mybatis-plus的ResultMap中
		log.info("--- start register @AutoResultMap ---");		
		String basePackage = "com.ai.modules.**.vo";
		Set<Class<?>> classes = new HashSet<Class<?>>();
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
				resolveBasePackage(new StandardEnvironment(), basePackage) + "/**/*.class";
		log.info("scan {}", basePackage);
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
		MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();		
		for (Resource resource : resources) {			
			MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
			AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
			String className = annotationMetadata.getClassName();
			Class<?> clazz = Class.forName(className);
			if(clazz.isAnnotationPresent(AutoResultMap.class)) {
				//AutoResultMap注解类
				classes.add(clazz);
			}			
		}
		
		org.apache.ibatis.session.Configuration configuration = sqlSessionTemplate.getConfiguration();
		for(Class<?> clazz : classes) {
			log.info(clazz.getName());
			String currentNamespace = clazz.getPackage().getName();
			MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
			assistant.setCurrentNamespace(currentNamespace);
			TableInfo tableInfo = TableInfoHelper.initTableInfo(assistant, clazz);
			if(!tableInfo.isAutoInitResultMap()) {
				ReflectUtil.setFieldValue(tableInfo, "autoInitResultMap", true);
				ReflectUtil.invoke(tableInfo, "initResultMapIfNeed");
			}
		}
		log.info("--- end register @AutoResultMap ---");
    }
	
	protected static String resolveBasePackage(Environment environment, String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(environment.resolveRequiredPlaceholders(basePackage));
	}
}
