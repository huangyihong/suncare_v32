package com.ai.common;

/**
 * @Auther: zhangpeng
 * @Date: 2020/1/16 15
 * @Description:
 */
public class MedicalConstant {
    // 字典类缓存时间 6小时
//    public static final long EXPIRE_DICT_TIME = 60 * 60 * 6;
    // 字典类缓存时间 6小时
    public static final long EXPIRE_DICT_TIME = 60 * 60 * 2;

    public static String SWITCH_STOP = "stop";
    public static String SWITCH_NORMAL = "normal";

    public static String DICT_KIND_COMMON = "99";

    public static String RUN_STATE_WAIT = "wait";
    // 正在生成
    public static String RUN_STATE_RUNNING = "running";
    // 已生成
    public static String RUN_STATE_NORMAL = "normal";
    // 异常
    public static String RUN_STATE_ABNORMAL = "abnormal";
    // 已删除
    public static String RUN_STATE_DELETED = "deleted";
    // 未审核
    public static String REVIEW_STATE_UN_AUDIT = "unAudit";
    // 已审核
    public static String REVIEW_STATE_AUDITED = "audited";

    // 已推送
    public static String REVIEW_STATE_PUSHED = "pushed";
    public static String REVIEW_STATE_PUSHING = "pushing";
    public static String REVIEW_STATE_PUSH_WAIT = "push_wait";
    // 推送失败
    public static String REVIEW_STATE_PUSH_ABNORMAL = "pushAB";


    public static String PLATFORM_SERVICE = "service";


    // 01业务组 02药品 03收费 04老版本临床路径 05KPI 06临床路径 07诊疗 -1特殊批次
    public static String RULE_TYPE_CASE = "01";
    public static String RULE_TYPE_NEWCASE = "11";
    public static String RULE_TYPE_DRUG = "02";
    public static String RULE_TYPE_CHARGE = "03";
    public static String RULE_TYPE_CLINICAL = "04";
    public static String RULE_TYPE_KPI = "05";
    public static String RULE_TYPE_CLINICAL_NEW = "06";
    public static String RULE_TYPE_TREAT = "07";
    public static String RULE_TYPE_DRUGUSE = "08";
    public static String RULE_TYPE_NEWCHARGE = "09";
    public static String RULE_TYPE_NEWTREAT = "10";
    public static String RULE_TYPE_NEWDRUG = "12";
    public static String RULE_TYPE_MANUAL = "-1";

    //
    public static int BATCH_STEP_CREATE = 0;
    public static int BATCH_STEP_SYSTEM = 1;
    /*public static int BATCH_STEP_PEOPLE = 2;
    public static int BATCH_STEP_ACTION = 3;
    public static int BATCH_STEP_CUSTOM = 4;*/

    //模型
    public static String ENGINE_BUSI_TYPE_CASE = "CASE";
    //临床路径
    public static String ENGINE_BUSI_TYPE_CLINICAL = "CLINICAL";
    //药品合规
    public static String ENGINE_BUSI_TYPE_DRUG = "DRUG";
    //收费合规
    public static String ENGINE_BUSI_TYPE_CHARGE = "CHARGE";
    //诊疗合规
    public static String ENGINE_BUSI_TYPE_TREAT = "TREAT";
    //用药合理
    public static String ENGINE_BUSI_TYPE_DRUGUSE = "DRUGUSE";
    //重复用药
    public static String ENGINE_BUSI_TYPE_DRUGREPEAT = "DRUGREPEAT";
    //新版收费合规
    public static String ENGINE_BUSI_TYPE_NEWCHARGE = "NEWCHARGE";
    //新版诊疗合理
    public static String ENGINE_BUSI_TYPE_NEWTREAT = "NEWTREAT";

    public static String ENGINE_BUSI_TYPE_NEWDRUG = "NEWDRUG";
    //特殊批次
    public static String ENGINE_BUSI_TYPE_MANUAL = "MANUAL";

    public static String DEFAULT_START_TIME = "2000-01-01";
    public static String DEFAULT_END_TIME = "2037-01-01";

    //模型关联项目类型
    public static String CASE_RELA_TYPE_DRUG = "drug";
    public static String CASE_RELA_TYPE_PROJECT = "peoject";
    public static String CASE_RELA_TYPE_DRUGGROUP = "drugGroup";
    public static String CASE_RELA_TYPE_PROJECTGROUP = "peojectGroup";

    //有效
    public static String STATUS_VALID = "1";
    //无效
    public static String STATUS_INVALID = "0";
    
    public static String ITEM_PROJECTGRP = "PROJECTGRP";
}
