package com.ai.modules.ybChargeSearch.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : zhangly
 * @date : 2023/2/16 13:10
 */
public class DcConstants {
    public static final String TYPE_STRING = "String";
    public static final String TYPE_NUMBER = "Number";
    public static final String TYPE_DATE = "Date";

    public static final String SRC_YB_CHARGE_DETAIL = "src_yb_charge_detail";
    public static final String SRC_YB_MASTER_INFO = "src_yb_master_info";

    public static final String SRC_HIS_ZY_MASTER_INFO = "src_his_zy_master_info";

    public static final String RULE_OPERATOR_REGEXLIKE = "REGEXLIKE";
    public static final String DB_TYPE_GP = "greenplum";
    public static final String DB_TYPE_MYSQL = "mysql";

    /**规则限制类型*/
    /**年龄*/
    public static final String RULE_LIMIT_AGE = "age";
    /**天龄*/
    public static final String RULE_LIMIT_DAYAGE = "dayAge";
    public static final String RULE_LIMIT_SEX = "sex";
    public static final String RULE_LIMIT_VISITTYPE = "visittype";
    public static final String RULE_LIMIT_INSURANCETYPE = "insurancetype";
    public static final String RULE_LIMIT_HOSPLEVEL = "hosplevel";

    /**数字范围*/
    public static final String RULE_LIMIT_RANGE = "range";
    /**住院天数*/
    public static final String RULE_LIMIT_ZYDAYS = "zyDays";
    /**出生体重*/
    public static final String RULE_LIMIT_WEIGHT = "weight";
    /**离院方式*/
    public static final String RULE_LIMIT_LEAVETYPE = "leavetype";

    public static final String VISITTYPE_ZY = "ZY";
    public static final String VISITTYPE_MZ = "MZ";
    public static final String VISITTYPE_MM = "MM";
    public static final String VISITTYPE_GY = "GY";

    public static final String ETL_SOURCE_YB = "yb";
    public static final String ETL_SOURCE_HIS = "his";

    public static final String DATA_STATIC_LEVEL_SRC = "src";
    public static final String DATA_STATIC_LEVEL_ODS = "ods";
}
