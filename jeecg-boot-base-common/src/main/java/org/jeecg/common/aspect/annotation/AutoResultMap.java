/**
 * AutoResultMap.java	  V1.0   2022年6月9日 上午9:49:37
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package org.jeecg.common.aspect.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被注解的自定义Bean注入到mybatis的ResultMap中，同在xml中配置<resultMap></resultMap>起到一样的效果
 * 类中只能允许包含基类型字段，否则报错：No typehandler found for property xxx，导致项目启动失败
 * @author  zhangly
 * Date: 2022年6月9日
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoResultMap {

}
