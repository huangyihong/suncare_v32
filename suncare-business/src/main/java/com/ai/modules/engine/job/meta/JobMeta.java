/**
 * JobMeta.java	  V1.0   2020年2月11日 上午10:20:46
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.job.meta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class JobMeta {
	protected BaseMeta baseMeta;
	
	public JobMeta(BaseMeta baseMeta) {
		this.baseMeta = baseMeta;
		try {
			this.parse();
		} catch (Exception e) {
			
		}
	}
	
	protected void parse() throws Exception {
		Map<String, String> params = baseMeta.getParams();
		if(params!=null && params.size()>0) {
			for (Class<?> clazz = this.getClass(); clazz != JobMeta.class;
					clazz = clazz.getSuperclass()) {
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					String key = field.getName();
					if(!params.containsKey(key)) {
						continue;
					}
					String value = params.get(key);
					if(StringUtils.isNotBlank(value)) {
						Class<?> fieldType = field.getType();						
						// 构造set方法名 
						String dynamicSetMethod = dynamicMethodName(key, "set");
						// 获取方法
						Method method = clazz.getMethod(dynamicSetMethod, fieldType);                        
						if(method!=null) {
							method.invoke(this, fieldType.cast(value));
						}
					}					
				}
			}
		}
	}
	
	private static String dynamicMethodName(String name, String setOrGet) {
        String setMethodName = setOrGet + name.substring(0, 1).toUpperCase() + name.substring(1);
        return setMethodName;
    }

	public BaseMeta getBaseMeta() {
		return baseMeta;
	}

	public void setBaseMeta(BaseMeta baseMeta) {
		this.baseMeta = baseMeta;
	}
}
