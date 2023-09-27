/**
 * AbsDictMergeHandle.java	  V1.0   2021年7月6日 上午10:45:02
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.handle;

import org.jeecg.common.util.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * 
 * 功能描述：合并疾病、药品、项目等字典抽象类
 *
 * @author  zhangly
 * Date: 2021年7月6日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public abstract class AbsDictMergeHandle {
	protected static final Logger log = LoggerFactory.getLogger(AbsDictMergeHandle.class);
	/**
	 * 
	 * 功能描述：合并
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年7月6日 上午10:49:44</p>
	 *
	 * @param main 主项，保留项
	 * @param repeat 被替换项，逻辑删除项
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public abstract void merge(String main, String repeat) throws Exception;
	
	/**
	 * 
	 * 功能描述：获取spring容器注册对象
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年7月6日 下午3:01:31</p>
	 *
	 * @param <T>
	 * @param clazz
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected <T> T getBean(Class<T> clazz) throws Exception {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		return context.getBean(clazz);
	}
}
