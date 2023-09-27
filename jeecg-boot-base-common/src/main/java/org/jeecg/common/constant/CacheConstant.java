package org.jeecg.common.constant;

/**
 * @author: huangxutao
 * @date: 2019-06-14
 * @description: 缓存常量
 */
public interface CacheConstant {

	/**
	 * 字典信息缓存
	 */
    public static final String SYS_DICT_CACHE = "sys:cache:dict";

	/**
	 * 表字典信息缓存
	 */
    public static final String SYS_DICT_TABLE_CACHE = "sys:cache:dictTable";

	/**
	 * 医疗表字典信息缓存
	 */
	public static final String MEDICAL_DICT_TABLE_CACHE = "sun:mdict";

	/**
	 * 医疗库表字段信息缓存
	 */
	public static final String MEDICAL_COL_CONFIG_CACHE = "sun:colConfig";

	/**
	 * 远程医疗字典缓存
	 */
	public static final String REMOTE_MEDICAL_DICT_CACHE = "remote:mdict";

	/**
	 * 远程其他字典缓存
	 */
	public static final String REMOTE_OTHER_DICT_CACHE = "remote:odict";

	/**
	 * 数据权限配置缓存
	 */
    public static final String SYS_DATA_PERMISSIONS_CACHE = "sys:cache:permission:datarules";

	/**
	 * 缓存用户信息
	 */
	public static final String SYS_USERS_CACHE = "sys:cache:user";

	/**
	 * 全部部门信息缓存
	 */
	public static final String SYS_DEPARTS_CACHE = "sys:cache:depart:alldata";


	/**
	 * 全部部门ids缓存
	 */
	public static final String SYS_DEPART_IDS_CACHE = "sys:cache:depart:allids";


	/**
	 * 测试缓存key
	 */
	public static final String TEST_DEMO_CACHE = "test:demo";


	/**
	 * 其他字典信息缓存
	 */
	public static final String MEDICAL_OTHER_DICT_CACHE = "sys:cache:mOtherDict";

	/**
	 * 不合理行为动态字段配置缓存
	 */
	public static final String DYNAMIC_ACTION_FIELD_CONFIG = "sys:cache:dafc";

	/**
	 *项目地和数据源缓存
	 */
	public static final String DATASOURCE_DATABASE_CACHE= "ybChargeSearch:cache:dataSource:database";
}
