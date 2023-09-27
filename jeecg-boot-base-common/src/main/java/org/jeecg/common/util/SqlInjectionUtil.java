package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * sql注入处理工具类
 *
 * @author zhoujf
 */
@Slf4j
public class SqlInjectionUtil {
//	final static String xssStr = "'|and |exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |;|or |+|,";
	final static String[] xssArr = "and |exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |or ".split("\\|");;

	/**
	 * sql注入过滤处理，遇到注入关键字抛异常
	 *
	 * @param value
	 * @return
	 */
	public static void filterContent(String value) {
		if (value == null || "".equals(value)) {
			return;
		}
//		log.info("filterContent:" +value);
		// 统一转为小写  替换空白符
		value = value.toLowerCase().replaceAll("[\n\t]", " ");
		for (int i = 0; i < xssArr.length; i++) {
			if (value.contains(xssArr[i])) {
				log.error("请注意，值可能存在SQL注入风险!---> {}", value);
				throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
			}
		}
	}

	/**
	 * sql注入过滤处理，遇到注入关键字抛异常
	 *
	 * @param value
	 * @return
	 */
	public static void filterContent(String[] values) {
		for (String value : values) {
			if (value == null || "".equals(value)) {
				return;
			}
            // 统一转为小写  替换空白符
            value = value.toLowerCase().replaceAll("[\n\t]", " ");
			for (int i = 0; i < xssArr.length; i++) {
				if (value.contains(xssArr[i])) {
					log.error("请注意，值可能存在SQL注入风险!---> {}", value);
					throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
				}
			}
		}
	}

	/**
	 * @特殊方法(不通用) 仅用于字典条件SQL参数，注入过滤
	 * @param value
	 * @return
	 */
	@Deprecated
	public static void specialFilterContent(String value) {
		String specialXssStr = "exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |;|+|";
		String[] xssArr = specialXssStr.split("\\|");
		if (value == null || "".equals(value)) {
			return;
		}
		value = value.toLowerCase();// 统一转为小写
		for (int i = 0; i < xssArr.length; i++) {
			if (value.contains(xssArr[i])) {
				log.error("请注意，值可能存在SQL注入风险!---> {}", value);
				throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
			}
		}
	}


	/**
	 * @特殊方法(不通用) 仅用于Online报表SQL解析，注入过滤
	 * @param value
	 * @return
	 */
	@Deprecated
	public static void specialFilterContentForOnlineReport(String value) {
		String specialXssStr = "exec |insert |delete |update |drop |chr |mid |master |truncate |char |declare |";
		String[] xssArr = specialXssStr.split("\\|");
		if (value == null || "".equals(value)) {
			return;
		}
		value = value.toLowerCase();// 统一转为小写
		for (int i = 0; i < xssArr.length; i++) {
			if (value.contains(xssArr[i])) {
				log.error("请注意，值可能存在SQL注入风险!---> {}", value);
				throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
			}
		}
	}

}
