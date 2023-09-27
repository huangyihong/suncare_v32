package com.ai.modules.ybChargeSearch.vo;

import com.ai.common.utils.StringCamelUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据服务挖掘 常量
 */
public class YbChargeSearchConstant {
    //收费明细导出
    public static final String SEARCH="search";
    //年度统计指标
    public static final String YEAR_STATISTICS="yearStatistics";
    //医保项目使用率汇总
    public static final String YEAR_USERATE_STATISTICS="yearUserateStatistics";
    //医院手术情况统计
    public static final String YEAR_SURGERY_STATISTICS="yearSurgeryStatistics";

    //医院在院人数统计
    public static final String YEAR_ORG_ONLINE_PATIENT_COUNT="yearOrgOnlinePatientCount";

    //医保收费清单按医院汇总
    public static final String ITEM_STATISTICS="itemStatistics";
    //医保收费清单按科室汇总
    public static final String ITEM_BY_DEPTSTATISTICS="itemByDeptStatistics";
    //科室金额前10就诊明细
    public static final String DEPT_STATISTICS="deptStatistics";
    //医保收费清单按就诊汇总
    public static final String ITEM_BY_VISIT_STATISTICS="itemByVisitStatistics";
    //住院及门慢清单
    public static final String VISIT_STATISTICS="visitStatistics";
    //低标准入院明细
    public static final String LOW_STATISTICS="lowStatistics";
    //医院收费项目异常明细
    public static final String RISK_STATISTICS="riskStatistics";
    //诊断汇总数据异常明细
    public static final String DIAG_RISK_STATISTICS="diagRiskStatistics";
    //患者异常情况汇总表
    public static final String PATIENT_RISK_GROUP_STATISTICS="patientRiskGroupStatistics";
    //患者异常情况明细表
    public static final String PATIENT_RISK_STATISTICS="patientRiskStatistics";
    //医生异常情况汇总表
    public static final String DOCTOR_RISK_GROUP_STATISTICS="doctorRiskGroupStatistics";
    //医生异常情况明细表
    public static final String DOCTOR_RISK_STATISTICS="doctorRiskStatistics";
    //药品收费违规查询
    public static final String DRUG_RULE_STATISTICS="drugRuleStatistics";
    //医院总量异常明细
    public static final String ORG_RISK_STATISTICS="orgRiskStatistics";
    //假期住院人次异常
    public static final String ORG_HOLIDAY_RISK_STATISTICS="orgHolidayRiskStatistics";
    //结伴就医明细
    public static final String VISIT_TOGETHER_STATISTICS="visitTogetherStatistics";
    //标签结果汇总
    public static final String TAG_STATISTICS="tagStatistics";
    //标签结果汇总
    public static final String SUSPICIOUS_GROUP_STATISTICS="suspiciousGroupStatistics";
    //就诊其他标签类型
    public static final String DWB_VISIT_TAG="dwb_visit_tag";

    //欺诈专题 患者年度统计
    public static final String FRAUD_PATIENT="fraud_patient";
    //欺诈专题 城市年度统计
    public static final String FRAUD_PROJECT="fraud_project";
    //欺诈专题 医院年度统计
    public static final String FRAUD_HOSPITAL="fraud_hospital";

    //top200口服药
    public static final String DRUG_TOP200="drug_top200";

    //top200口服药明细
    public static final String DRUG_TOP200_DETAIL="drug_top200_detail";

    //标签图形种类列表
    public static final String DATAMINING_GRAPH_DATA_GROUP="datamining_graph_data_group";

    //图形数据结果
    public static final String DATAMINING_GRAPH_DATA="datamining_graph_data";

    //标签模型明细数据
    public static final String DATAMINING_SQL_DETAIL="datamining_sql_detail";
    //医生住院期间收治病人
    public static final String DOCTOR_ADMIT_PATIENT_INSICK="doctor_admit_patient_insick";
    //分解住院
    public static final String DWS_INHOSPITAL_APART="dws_inhospital_apart";

    //下载记录
    public static final String DOWNLOAD="download";
    //关键字历史查询数据分析
    public static final String HISTORY="history";

    public static final String DB_TYPE_GREENPLUM = "greenplum";
    public static final String DB_TYPE_MYSQL = "mysql";

    //值格式化
    public static final String FORMAT_VALUE_YEARAGE = "yearage";//年龄

    public static final String FORMAT_VALUE_ROUND_HALF_UP2 = "ROUND_HALF_UP2";//保留两位小数

    public static final String FORMAT_VALUE_INT = "int";//整数

    public static final String FORMAT_VALUE_ETL_SOURCE = "etlSource";//字典值转换
    public static final String FORMAT_VALUE_RATE = "rate";//百分比  val*100+%
    public static final String FORMAT_VALUE_BED_AMOUNT = "bedAmount";//值特殊处理

    //任务状态
    // 正在运行
    public static final String TASK_RUNING="00";
    //等待
    public static final String TASK_WAIT = "-1";
    // 已生成
    public static final String TASK_SUCCESS = "01";
    // 失败
    public static final String TASK_FAIL = "02";

    public static final Map<String,String[]> LABEL_TABLE_MAP = new HashMap<String, String[]>();
    static {
        //LABEL_TABLE_MAP.put("打标结果表",new String[]{"关联字段名","excel对应列字段名"});
        LABEL_TABLE_MAP.put("datamining_diag_risk_data_label",new String[]{"yyear,orgname,primarydiag_name_src,tag_name","year,orgname,primarydiagNameSrc,tagName"});
        LABEL_TABLE_MAP.put("datamining_patient_risk_data_label",new String[]{"yyear,clientid,tag_name,compare_object","yyear,clientid,tagName,compareObject"});
        LABEL_TABLE_MAP.put("datamining_org_risk_data_label",new String[]{"yyear,orgname,tag_name","year,orgname,tagName"});
        LABEL_TABLE_MAP.put("datamining_doctor_risk_data_label",new String[]{"yyear,mmonth,dday,orgname,doctorid,tag_name","yyear,mmonth,dday,orgname,doctorid,tagName"});
        LABEL_TABLE_MAP.put("datamining_chargeitem_risk_data_label",new String[]{"yyear,orgname,itemcode_src,his_itemname_src,itemname_src,tag_name,visitid_src,fee_occur_time","year,orgname,itemcodeSrc,hisItemnameSrc,itemnameSrc,tagName,visitidSrc,feeOccurTime"});
        LABEL_TABLE_MAP.put("dwb_visitid_tag_label",new String[]{"orgname,visitid,tag_name","orgname,visitid,tagName"});
        LABEL_TABLE_MAP.put("dwb_visitid_tag_label2",new String[]{"orgname,visitid,tag_name","orgnameSrc,visitidSrc,tagName"});
        LABEL_TABLE_MAP.put("dwb_visitid_tag_label3",new String[]{"orgname,visitid,tag_name","orgnameThis,zyIdThis,tagName"});
    }


    public static final Map<String,TaskTypeInfo> TASK_TYPE_MAP = new HashMap<String, TaskTypeInfo>();
    static {
        List<ColsInfo> cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("就诊年份","year","80","yyear", "",""));
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("机构ID","orgid","120","","",""));
        cols.add(new ColsInfo("就诊ID","visitid","120","","",""));
        cols.add(new ColsInfo("病案号","caseId","120","","",""));
        cols.add(new ColsInfo("医生姓名","doctorname","120","","",""));
        cols.add(new ColsInfo("科室名称","deptname","120","","",""));
        cols.add(new ColsInfo("就诊类型","visittype","100","","",""));
        cols.add(new ColsInfo("医保类型","insurancetypename","180","","",""));
        cols.add(new ColsInfo("患者名称","name","140","","",""));
        cols.add(new ColsInfo("性别","sex","80","","",""));
        cols.add(new ColsInfo("就诊日期","visitdate","100","","",""));
        cols.add(new ColsInfo("出院日期","leavedate","100","","",""));
        cols.add(new ColsInfo("年龄","yearage","80","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("主诊断","disMain","180","","",""));
        cols.add(new ColsInfo("其他诊断","dis","180","","",""));
        cols.add(new ColsInfo("收费日期","charge","100","","",""));
        cols.add(new ColsInfo("机构项目名称","hisItemname","200","","",""));
        cols.add(new ColsInfo("医保项目名称","itemname","200","","",""));
        cols.add(new ColsInfo("医保项目编码","itemcode","150","","",""));
        cols.add(new ColsInfo("标志","wgFlag","100","biaozhi","",""));
        cols.add(new ColsInfo("收费等级","chargeattri","80","","",""));
        cols.add(new ColsInfo("自付比例","selfpayProp","80","","",""));
        cols.add(new ColsInfo("数量","amount","80","sl",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("金额","fee","80","fy",FORMAT_VALUE_ROUND_HALF_UP2,""));
        TASK_TYPE_MAP.put(SEARCH, new TaskTypeInfo(
                "search_new",
                "searchResult_new",
                "收费明细导出",
                "收费明细结果",
                "yb_charge_search_result",
                "收费明细导出",
                "收费明细结果",
                cols,
                "visitid"
                )

        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        //cols.add(new ColsInfo("机构ID","orgid","120","","",""));
        cols.add(new ColsInfo("原始机构名称","orgnameSrc","100","","",""));
        cols.add(new ColsInfo("结算年份","year","80","yyear","",""));
        cols.add(new ColsInfo("就诊人次","totalCount","90","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("门诊人次","mzCount","90","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("购药人次","gyCount","90","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("住院人次","zyCount","90","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("医疗费用总额","sumTotalfee","130","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("基金使用总额","sumFundpay","130","",FORMAT_VALUE_ROUND_HALF_UP2,""));

        cols.add(new ColsInfo("次均住院金额","avgZyFee","110","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("次均住院天数","avgZyDay","110","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("平均床日费用","avgBedFee","110","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("门诊/住院占比","zyMzRate","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("床位利用率(%)","bedAmount","120","",FORMAT_VALUE_BED_AMOUNT,""));
        cols.add(new ColsInfo("床位利用率(%)","bedAmount","120","",FORMAT_VALUE_BED_AMOUNT,""));
        cols.add(new ColsInfo("本地/异地","localTag","100","","",""));

        TASK_TYPE_MAP.put(YEAR_STATISTICS, new TaskTypeInfo(
                "search_new",
                "searchResult_new",
                "年度统计指标",
                "年度统计指标",
                "yb_charge_year_result",
                "年度统计指标",
                "年度统计指标",
                        cols,
                "orgname"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("年","year","80","yyear","",""));
        cols.add(new ColsInfo("就诊类型","visittype","80","","",""));
        cols.add(new ColsInfo("收费项目大类","itemtype","100","","",""));
        cols.add(new ColsInfo("收费项目类型","chargeclass","100","","",""));
        cols.add(new ColsInfo("收费项目名称","itemname","300","","",""));
        cols.add(new ColsInfo("医保项目使用率(%)","userate","130","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("医疗费用总额","fee","130","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("本地/异地","localTag","100","","",""));
        TASK_TYPE_MAP.put(YEAR_USERATE_STATISTICS, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "医保项目使用率汇总",
                        "医保项目使用率",
                        "yb_charge_year_result",
                        "医保项目使用率汇总",
                        "医保项目使用率",
                        cols,
                        "orgname"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("机构ID","orgid","120","","",""));
        cols.add(new ColsInfo("年","year","80","yyear","",""));
        cols.add(new ColsInfo("就诊类型","visittype","100","","",""));
        cols.add(new ColsInfo("医保类型","insurancetypename","180","","",""));
        cols.add(new ColsInfo("医院收费名称","hisItemname","200","","",""));
        cols.add(new ColsInfo("医保收费名称","itemname","200","","",""));
        cols.add(new ColsInfo("医保收费编码","itemcode","150","","",""));
        cols.add(new ColsInfo("收费类别","itemclass","80","","",""));
        cols.add(new ColsInfo("收费等级","chargeattri","80","","",""));
        cols.add(new ColsInfo("自付比例","selfpayProp","80","","",""));
        cols.add(new ColsInfo("单价","itemprice","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("数量","sumAmount","80","amount",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("汇总金额","sumFee","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("标签","tagName","120","","",""));
        TASK_TYPE_MAP.put(ITEM_STATISTICS, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "医保收费清单汇总查询",
                        "医保收费清单按医院汇总",
                        "yb_charge_item_result",
                        "医保收费清单汇总查询",
                        "医保收费清单按医院汇总",
                        cols,
                "orgname"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("机构ID","orgid","120","","",""));
        cols.add(new ColsInfo("年","year","80","yyear","",""));
        cols.add(new ColsInfo("科室名称","deptname","120","","",""));
        cols.add(new ColsInfo("就诊类型","visittype","100","","",""));
        cols.add(new ColsInfo("医保类型","insurancetypename","180","","",""));
        cols.add(new ColsInfo("医院收费名称","hisItemname","200","","",""));
        cols.add(new ColsInfo("医保收费名称","itemname","200","","",""));
        cols.add(new ColsInfo("医保收费编码","itemcode","150","","",""));
        cols.add(new ColsInfo("收费类别","itemclass","80","","",""));
        cols.add(new ColsInfo("收费等级","chargeattri","80","","",""));
        cols.add(new ColsInfo("自付比例","selfpayProp","80","","",""));
        cols.add(new ColsInfo("单价","itemprice","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("数量","sumAmount","80","amount",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("汇总金额","sumFee","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("标签","tagName","120","","",""));
        TASK_TYPE_MAP.put(ITEM_BY_DEPTSTATISTICS, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "医保收费清单汇总查询",
                        "医保收费清单按科室汇总",
                        "yb_charge_item_result",
                        "医保收费清单汇总查询",
                        "医保收费清单按科室汇总",
                        cols,
                "orgname"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("机构ID","orgid","120","","",""));
        cols.add(new ColsInfo("年","year","80","yyear","",""));
        cols.add(new ColsInfo("科室名称","deptname","120","","",""));
        cols.add(new ColsInfo("就诊号","visitid","120","","",""));
        cols.add(new ColsInfo("总费用","totalfee","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("报销金额","fundpay","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("金额排名","rank1","120","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("医生姓名","doctorname","120","","",""));
        cols.add(new ColsInfo("就诊类型","visittype","100","","",""));
        cols.add(new ColsInfo("医保类型","insurancetypename","180","","",""));
        cols.add(new ColsInfo("患者名称","name","140","","",""));
        cols.add(new ColsInfo("性别","sex","80","","",""));
        cols.add(new ColsInfo("就诊日期","visitdate","100","","",""));
        cols.add(new ColsInfo("出院日期","leavedate","100","","",""));
        cols.add(new ColsInfo("年龄","yearage","80","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("诊断","dis","180","","",""));
        cols.add(new ColsInfo("医院收费名称","hisItemname","200","","",""));
        cols.add(new ColsInfo("医保收费编码","itemcode","150","","",""));
        cols.add(new ColsInfo("医保收费名称","itemname","200","","",""));
        cols.add(new ColsInfo("收费等级","chargeattri","80","","",""));
        cols.add(new ColsInfo("自付比例","selfpayProp","80","","",""));

        cols.add(new ColsInfo("单价","itemprice","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("数量","amount","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("金额","fee","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("标签","tagName","120","","",""));
        TASK_TYPE_MAP.put(DEPT_STATISTICS, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "医保收费清单汇总查询",
                        "科室金额前10就诊明细",
                        "yb_charge_dept_result",
                        "医保收费清单汇总查询",
                        "科室金额前10就诊明细",
                        cols,
                "orgname"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("机构ID","orgid","120","","",""));
        cols.add(new ColsInfo("年","year","80","yyear","",""));
        cols.add(new ColsInfo("科室名称","deptname","120","","",""));
        cols.add(new ColsInfo("就诊号","visitid","120","","",""));
        cols.add(new ColsInfo("总费用","totalfee","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("报销金额","fundpay","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("医生姓名","doctorname","120","","",""));
        cols.add(new ColsInfo("就诊类型","visittype","100","","",""));
        cols.add(new ColsInfo("医保类型","insurancetypename","180","","",""));
        cols.add(new ColsInfo("患者名称","name","140","","",""));
        cols.add(new ColsInfo("性别","sex","80","","",""));
        cols.add(new ColsInfo("就诊日期","visitdate","100","","",""));
        cols.add(new ColsInfo("出院日期","leavedate","100","","",""));
        cols.add(new ColsInfo("年龄","yearage","80","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("诊断","dis","180","","",""));
        cols.add(new ColsInfo("医院收费名称","hisItemname","200","","",""));
        cols.add(new ColsInfo("医保收费编码","itemcode","150","","",""));
        cols.add(new ColsInfo("医保收费名称","itemname","200","","",""));
        cols.add(new ColsInfo("收费等级","chargeattri","80","","",""));
        cols.add(new ColsInfo("自付比例","selfpayProp","80","","",""));
        cols.add(new ColsInfo("单价","itemprice","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("数量","amount","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("金额","fee","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("标签","tagName","120","","",""));
        TASK_TYPE_MAP.put(ITEM_BY_VISIT_STATISTICS, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "医保收费清单汇总查询",
                        "医保收费清单按就诊汇总",
                        "yb_charge_dept_result",
                        "医保收费清单汇总查询",
                        "医保收费清单按就诊汇总",
                        cols,
                "orgname"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("机构ID","orgid","120","","",""));
        cols.add(new ColsInfo("就诊号","visitid","120","","",""));
        cols.add(new ColsInfo("就诊类型","visittype","100","","",""));
        cols.add(new ColsInfo("医保类型","insurancetypename","180","","",""));
        cols.add(new ColsInfo("医生","doctorname","120","","",""));
        cols.add(new ColsInfo("科室","deptname","120","","",""));
        cols.add(new ColsInfo("患者名称","name","140","","",""));
        cols.add(new ColsInfo("年龄","yearage","80","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("性别","sex","80","","",""));
        cols.add(new ColsInfo("单位地址","workplacename","180","","",""));
        cols.add(new ColsInfo("患者归属","ifClientLocal","100","","",""));
        cols.add(new ColsInfo("就诊日期","visitdate","100","","",""));
        cols.add(new ColsInfo("出院日期","leavedate","100","","",""));
        cols.add(new ColsInfo("住院天数","visitdays","80","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("诊断","dis","180","","",""));
        cols.add(new ColsInfo("就诊总费用","sumTotalfee","100","totalfee",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("基金支付总额","sumFundpay","100","fundpay",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("标签","tagName","120","","",""));
        TASK_TYPE_MAP.put(VISIT_STATISTICS, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "住院及门慢清单",
                        "住院/门慢清单",
                        "yb_charge_visit_result",
                        "住院及门慢清单",
                        "住院及门慢清单",
                        cols,
                "orgname"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("算法名称","tagName","200","","",""));
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("就诊号","visitid","120","","",""));
        cols.add(new ColsInfo("患者姓名","clientname","120","","","",true,true));
        cols.add(new ColsInfo("年龄","yearage","80","",FORMAT_VALUE_YEARAGE,"",true,true));
        cols.add(new ColsInfo("科室名称","deptname","120","","","",true,true));
        cols.add(new ColsInfo("医生姓名","doctorname","120","","","",true,true));
        cols.add(new ColsInfo("全部诊断","dis","180","","","",true,true));
        cols.add(new ColsInfo("入院时间","visitdate","100","","","",true,true));
        cols.add(new ColsInfo("出院时间","leavedate","100","","","",true,true));
        cols.add(new ColsInfo("住院天数","visitdays","80","",FORMAT_VALUE_INT,"",true,true));
        cols.add(new ColsInfo("参保类型","insurancetype","80","","","",true,true));
        cols.add(new ColsInfo("总费用","totalfee","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("基金支付金额","fundpay","150","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("治疗费","treatAmt","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("手术费","operationAmt","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("检查费","checkTotalAmt","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("化验费","labtestAmt","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("耗材费","conamteAmt","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("西药费","wmAmt","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("中成药费","chimedAmt","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("中草药费","herbalAmt","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("护理费","nursingAmt","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("床位费","bedAmt","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("诊查费","diagAmt","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("其他费","elsefeeAmt","100","",FORMAT_VALUE_ROUND_HALF_UP2,"",true,true));
        cols.add(new ColsInfo("线索价值(有用/无用)","labelName","100","","",""));
        cols.add(new ColsInfo("上次标注人","labelUser","100","","",""));
        cols.add(new ColsInfo("标注时间","labelTime","120","","",""));
        TASK_TYPE_MAP.put(LOW_STATISTICS, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "低标准入院可疑数据",
                        "低标准入院明细",
                        "yb_charge_low_result",
                        "低标准入院可疑数据",
                        "低标准入院明细",
                        cols,
                "tag_name,visitid,visitdate",
                "dwb_visitid_tag",
                "dwb_visitid_tag_label"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("算法名称","tagName","200","","",""));
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("年","year","80","yyear","",""));
        cols.add(new ColsInfo("科室","deptnameSrc","120","","",""));
        cols.add(new ColsInfo("映射后科室","deptname","120","","",""));
        cols.add(new ColsInfo("就诊类型","visittype","100","","",""));
        cols.add(new ColsInfo("就诊号","visitidSrc","120","","",""));
        cols.add(new ColsInfo("费用发生时间","feeOccurTime","150","","",""));
        cols.add(new ColsInfo("医院收费名称","hisItemnameSrc","200","","",""));
        cols.add(new ColsInfo("医保收费名称","itemnameSrc","200","","",""));
        cols.add(new ColsInfo("医保收费编码","itemcodeSrc","150","","",""));
        cols.add(new ColsInfo("收费类型","chargeClass","80","","",""));
        cols.add(new ColsInfo("单价/平均单价","itemprice","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("总数量","totalAmount","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("总费用","totalFee","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("分析对象","compareObject","180","","",""));
        cols.add(new ColsInfo("分析对象的值","analyseValue","130","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("异常阈值","abnormalStandard","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("对比机构参考值","compareValue","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("对比机构数量","compareOrgAmount","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("异常程度","abnormalDistince","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("数据来源名称","etlSource","120","",FORMAT_VALUE_ETL_SOURCE,""));
        cols.add(new ColsInfo("线索价值(有用/无用)","labelName","100","","",""));
        cols.add(new ColsInfo("上次标注人","labelUser","100","","",""));
        cols.add(new ColsInfo("标注时间","labelTime","120","","",""));
        TASK_TYPE_MAP.put(RISK_STATISTICS, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "医院收费项目异常数据",
                        "医院收费项目异常明细",
                        "yb_charge_overproof_result",
                        "医院收费项目异常数据",
                        "医院收费项目异常明细",
                        cols,
                "tag_name,visitid_src,fee_occur_time",
                "datamining_chargeitem_risk_data",
                "datamining_chargeitem_risk_data_label"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("算法名称","tagName","200","","",""));
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("年","year","80","yyear","",""));
        cols.add(new ColsInfo("科室","deptnameSrc","120","","",""));
        cols.add(new ColsInfo("映射后科室","deptname","120","","",""));
        cols.add(new ColsInfo("就诊类型","visittype","100","","",""));
        cols.add(new ColsInfo("主诊断","primarydiagNameSrc","180","","",""));
        cols.add(new ColsInfo("映射后主诊断","primarydiagName","180","","",""));
        cols.add(new ColsInfo("总数量","totalAmount","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("总费用","totalFee","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("分析对象","compareObject","180","","",""));
        cols.add(new ColsInfo("分析对象的值","analyseValue","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("异常阈值","abnormalStandard","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("对比机构参考值","compareValue","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("对比机构数量","compareOrgAmount","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("异常程度","abnormalDistince","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("数据来源名称","etlSource","100","",FORMAT_VALUE_ETL_SOURCE,""));
        cols.add(new ColsInfo("线索价值(有用/无用)","labelName","100","","",""));
        cols.add(new ColsInfo("上次标注人","labelUser","100","","",""));
        cols.add(new ColsInfo("标注时间","labelTime","120","","",""));
        TASK_TYPE_MAP.put(DIAG_RISK_STATISTICS, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "诊断汇总数据异常",
                        "诊断汇总数据异常明细",
                        "yb_charge_overproof_result",
                        "诊断汇总数据异常",
                        "诊断汇总数据异常明细",
                        cols,
                "tag_name,yyear,total_amount desc",
                "datamining_diag_risk_data",
                "datamining_diag_risk_data_label"
                )
        );

        cols = new ArrayList<ColsInfo>();
        TASK_TYPE_MAP.put(DOWNLOAD, new TaskTypeInfo(
                        "",
                        "commonResultList",
                        "",
                        "",
                        "yb_charge_search_task_download",
                        "",
                        "",
                cols,
                ""
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("收费项目名称A","itemname","200","","",""));
        cols.add(new ColsInfo("收费项目名称B","itemname1","200","","",""));
        cols.add(new ColsInfo("重复收费类型","item1Type","110","","",""));
        cols.add(new ColsInfo("收费项目B违规判断","item1Wgtype","200","","",""));
        cols.add(new ColsInfo("超量检查的类型","qtyType","120","","",""));
        cols.add(new ColsInfo("超量的数值(不含)","qtyNum","140","","",""));
        cols.add(new ColsInfo("是否输出同一天的手术项目","isSameDay","200","","",""));
        cols.add(new ColsInfo("金额累计(元)","totalFee","200","","",""));
        cols.add(new ColsInfo("结果记录数","orgs","400","","",""));
        TASK_TYPE_MAP.put(HISTORY, new TaskTypeInfo(
                        "",
                        "commonResultList",
                        "",
                        "",
                        "",
                        "",
                        "",
                cols,
                ""
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("年","yyear","80","","",""));
        cols.add(new ColsInfo("患者ID","clientid","80","","",""));
        cols.add(new ColsInfo("患者姓名","clientname","120","","",""));
        cols.add(new ColsInfo("年龄","yearage","80","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("性别","sex","80","","",""));
        cols.add(new ColsInfo("就诊医院列表","orgList","150","","",""));
        cols.add(new ColsInfo("全部诊断","diagNameList","150","","",""));
        cols.add(new ColsInfo("参保类型","insurancetypename","80","","",""));
        cols.add(new ColsInfo("全年就诊总费用","totalfeeSum","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("全年基金支付金额","fundpaySum","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("标签数量","tagCount","80","","",""));
        cols.add(new ColsInfo("标签列表","tagName","200","","",""));
        cols.add(new ColsInfo("数据来源","etlSource","80","",FORMAT_VALUE_ETL_SOURCE,""));
        TASK_TYPE_MAP.put(PATIENT_RISK_GROUP_STATISTICS, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "患者异常情况汇总表",
                        "患者异常情况汇总表",
                        "yb_charge_patient_risk_group",
                        "患者异常情况汇总表",
                        "患者异常情况汇总表",
                        cols,
                        "tag_count desc,fundpay_sum desc"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("算法名称","tagName","200","","",""));
        cols.add(new ColsInfo("年","yyear","80","","",""));
        cols.add(new ColsInfo("患者ID","clientid","80","","",""));
        cols.add(new ColsInfo("患者姓名","clientname","120","","",""));
        cols.add(new ColsInfo("年龄","yearage","80","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("性别","sex","80","","",""));
        cols.add(new ColsInfo("就诊医院列表","orgList","150","","",""));
        cols.add(new ColsInfo("全部诊断","diagNameList","150","","",""));
        cols.add(new ColsInfo("参保类型","insurancetypename","80","","",""));
        cols.add(new ColsInfo("全年就诊总费用","totalfeeSum","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("全年基金支付金额","fundpaySum","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("分析对象","compareObject","120","","",""));
        cols.add(new ColsInfo("分析对象的值","analyseValue","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("对比参考值","compareValue","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("异常阈值","abnormalStandard","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("对比数据集人数","comparePatientAmount","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));

        //cols.add(new ColsInfo("异常程度*报销金额","abnormalMoney","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("发生概率","probability","140","","",""));
        cols.add(new ColsInfo("数据来源","etlSource","80","",FORMAT_VALUE_ETL_SOURCE,""));
        cols.add(new ColsInfo("线索价值(有用/无用)","labelName","100","","",""));
        cols.add(new ColsInfo("上次标注人","labelUser","100","","",""));
        cols.add(new ColsInfo("标注时间","labelTime","120","","",""));
        TASK_TYPE_MAP.put(PATIENT_RISK_STATISTICS, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "患者异常情况明细表",
                        "患者异常情况明细表",
                        "yb_charge_patient_risk_result",
                        "患者异常情况明细表",
                        "患者异常情况明细表",
                        cols,
                        "clientid",

                "datamining_patient_risk_data",
                "datamining_patient_risk_data_label"
                )
        );


        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("年","yyear","80","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("医院名称","orgname","140","","",""));
        cols.add(new ColsInfo("科室原始名称","deptnameSrc","110","","",""));
        cols.add(new ColsInfo("医生ID","doctorid","80","","",""));
        cols.add(new ColsInfo("医生姓名","doctorname","100","","",""));
        cols.add(new ColsInfo("当年门诊人次","mzCount","110","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("当年负责住院人次","zyCount","120","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("就诊总费用","totalfeeSum","110","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("基金支付金额","fundpaySum","110","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("当年门诊天数","mzDaysCnt","110","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("当年门诊天数(有基金报销)","mzFundpayDaysCnt","160","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("标签数量","tagCount","80","","",""));
        cols.add(new ColsInfo("标签列表","tagName","200","","",""));
        cols.add(new ColsInfo("数据来源","etlSource","100","",FORMAT_VALUE_ETL_SOURCE,""));
        TASK_TYPE_MAP.put(DOCTOR_RISK_GROUP_STATISTICS, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "医生异常情况汇总表",
                        "医生异常情况汇总表",
                        "yb_charge_doctor_risk_group",
                        "医生异常情况汇总表",
                        "医生者异常情况汇总表",
                        cols,
                        "tag_count desc,fundpay_sum desc"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("算法名称","tagName","200","","",""));
        cols.add(new ColsInfo("年","yyear","80","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("医院名称","orgname","140","","",""));
        cols.add(new ColsInfo("科室原始名称","deptnameSrc","110","","",""));
        cols.add(new ColsInfo("医生ID","doctorid","80","","",""));
        cols.add(new ColsInfo("医生姓名","doctorname","100","","",""));
        cols.add(new ColsInfo("月","mmonth","80","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("日","dday","80","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("分析对象","compareObject","120","","",""));
        cols.add(new ColsInfo("分析对象的值","analyseValue","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("对比参考值","compareValue","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("异常阈值","abnormalStandard","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("对比数据集人数","comparePatientAmount","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));

        cols.add(new ColsInfo("发生概率","probability","140","","",""));

        cols.add(new ColsInfo("数据来源","etlSource","80","",FORMAT_VALUE_ETL_SOURCE,""));
        cols.add(new ColsInfo("线索价值(有用/无用)","labelName","100","","",""));
        cols.add(new ColsInfo("上次标注人","labelUser","100","","",""));
        cols.add(new ColsInfo("标注时间","labelTime","120","","",""));
        TASK_TYPE_MAP.put(DOCTOR_RISK_STATISTICS, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "医生异常情况明细表",
                        "医生异常情况明细表",
                        "yb_charge_doctor_risk_result",
                        "医生异常情况明细表",
                        "医生异常情况明细表",
                        cols,
                        "doctorname",
                "datamining_doctor_risk_data",
                "datamining_doctor_risk_data_label"
                )
        );

        cols = new ArrayList<ColsInfo>();
        TASK_TYPE_MAP.put(DRUG_RULE_STATISTICS, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "药品收费违规导出",
                        "药品收费违规结果",
                        "yb_charge_search_result",
                        "药品收费违规导出",
                        "药品收费违规结果",
                TASK_TYPE_MAP.get(SEARCH).getCols(),
                        "visitid"
                )

        );


        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("算法名称","tagName","200","","",""));
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("年","year","80","yyear","",""));
        cols.add(new ColsInfo("级别","hosplevel","100","","",""));
        cols.add(new ColsInfo("医院类别","orgcategory","120","","",""));
        cols.add(new ColsInfo("医院大类","orgcategory2","120","","",""));
        cols.add(new ColsInfo("住院人次","zyCount","120","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("门诊人次","mzCount","120","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("住院总费用","zyTotalfeeSum","150","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("住院报销总额","zyFundpaySum","150","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("门诊总费用","mzTotalfeeSum","150","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("门诊报销总额","mzFundpaySum","150","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("分析对象","compareObject","180","","",""));
        cols.add(new ColsInfo("分析对象的值","analyseValue","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("异常阈值","abnormalStandard","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("对比机构参考值","compareValue","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("对比机构数量","compareOrgAmount","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("异常程度","abnormalDistince","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("数据来源名称","etlSource","100","",FORMAT_VALUE_ETL_SOURCE,""));
        cols.add(new ColsInfo("线索价值(有用/无用)","labelName","100","","",""));
        cols.add(new ColsInfo("上次标注人","labelUser","100","","",""));
        cols.add(new ColsInfo("标注时间","labelTime","120","","",""));
        TASK_TYPE_MAP.put(ORG_RISK_STATISTICS, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "医院总量异常",
                        "医院总量异常明细",
                        "yb_charge_overproof_result",
                        "医院总量异常",
                        "医院总量异常明细",
                        cols,
                        "yyear, orgname,tag_name desc",
                "datamining_org_risk_data",
                "datamining_org_risk_data_label"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("算法名称","tagName","200","","",""));
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("年","year","80","yyear","",""));
        cols.add(new ColsInfo("级别","hosplevel","100","","",""));
        cols.add(new ColsInfo("医院类别","orgcategory","120","","",""));
        cols.add(new ColsInfo("住院人次","zyCount","100","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("门诊人次","mzCount","100","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("住院总费用","zyTotalfeeSum","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("住院报销总额","zyFundpaySum","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("分析对象","compareObject","180","","",""));
        cols.add(new ColsInfo("分析对象的值","analyseValue","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("异常阈值","abnormalStandard","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("对比机构参考值","compareValue","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("异常程度","abnormalDistince","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("线索价值(有用/无用)","labelName","100","","",""));
        cols.add(new ColsInfo("上次标注人","labelUser","100","","",""));
        cols.add(new ColsInfo("标注时间","labelTime","120","","",""));
        TASK_TYPE_MAP.put(ORG_HOLIDAY_RISK_STATISTICS, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "假期住院人次异常",
                        "假期住院人次异常",
                        "yb_charge_overproof_result",
                        "假期住院人次异常",
                        "假期住院人次异常",
                        cols,
                        "yyear, orgname,tag_name desc",
                 "datamining_org_risk_data",
                "datamining_org_risk_data_label"
                )
        );





        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("算法名称","tagName","120","","",""));
        cols.add(new ColsInfo("医院名称(原始)","orgnameSrc","120","","",""));
        cols.add(new ColsInfo("组号","groupid","100","","",""));
        cols.add(new ColsInfo("结伴人数","groupPatientQty","80","","",""));
        cols.add(new ColsInfo("结伴次数","groupCnt","80","","",""));
        cols.add(new ColsInfo("就诊号(原始)","visitidSrc","150","","",""));
        cols.add(new ColsInfo("同组医院列表","orgnameSrcList","150","","",""));
        cols.add(new ColsInfo("同组患者列表","clientnameList","150","","",""));
        cols.add(new ColsInfo("患者姓名","clientname","80","","",""));
        cols.add(new ColsInfo("就诊类型","visittypeSrc","100","","",""));
        cols.add(new ColsInfo("年龄","yearage","80","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("科室名称(原始)","deptnameSrc","120","","","",true,true));
        cols.add(new ColsInfo("医生姓名","doctorname","80","","","",true,true));
        cols.add(new ColsInfo("原始主诊断","diseasenamePrimarySrc","150","","",""));
        cols.add(new ColsInfo("原始其他诊断","diseasenameOtherSrc","150","","",""));
        cols.add(new ColsInfo("入院时间","visitdate","100","","",""));
        cols.add(new ColsInfo("出院时间","leavedate","110","","",""));
        cols.add(new ColsInfo("住院天数","zyDaysCalculate","80","","",""));
        cols.add(new ColsInfo("参保类型","insurancetype","80","","","",true,true));
        cols.add(new ColsInfo("总费用","totalfee","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("基金支付金额","fundpay","110","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("个人负担总额","selfpay","110","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("线索价值(有用/无用)","labelName","100","","",""));
        cols.add(new ColsInfo("上次标注人","labelUser","100","","",""));
        cols.add(new ColsInfo("标注时间","labelTime","120","","",""));
        TASK_TYPE_MAP.put(VISIT_TOGETHER_STATISTICS, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "结伴就医",
                        "结伴就医明细",
                        "yb_charge_visit_together_result",
                        "结伴就医",
                        "结伴就医明细",
                        cols,
                        "groupid asc, group_patient_qty desc,group_cnt  desc",
                "dwb_visitid_tag",
                "dwb_visitid_tag_label2"
                )
        );


        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("模型类型","tagTypeName","300","","",""));
        cols.add(new ColsInfo("模型名称","tagName","300","","",""));
        cols.add(new ColsInfo("模型数量","count","120","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("标签时间","tagTime","120","","",""));
        TASK_TYPE_MAP.put(TAG_STATISTICS, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "标签结果汇总",
                        "标签结果汇总",
                        "yb_charge_monitor_datamining_stat",
                        "标签结果汇总",
                        "标签结果汇总",
                        cols,
                        "tag_rank,count desc"
                )
        );


        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("标签数量","tagCount","80","","",""));
        cols.add(new ColsInfo("标签列表","tagName","200","","",""));
        cols.add(new ColsInfo("医院名称","orgnameSrc","120","","",""));
        cols.add(new ColsInfo("就诊号","visitidSrc","120","","",""));
        cols.add(new ColsInfo("就诊类型","visittypeSrc","100","","",""));
        cols.add(new ColsInfo("险种类型","insurancetypename","180","","",""));
        cols.add(new ColsInfo("科室名称","deptnameSrc","100","","",""));
        cols.add(new ColsInfo("医生姓名","doctorname","80","","",""));
        cols.add(new ColsInfo("患者姓名","clientname","80","","",""));
        cols.add(new ColsInfo("患者标签","patientTagName","140","","",""));
        cols.add(new ColsInfo("年龄","yearageSrc","80","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("性别","sexSrc","80","","",""));
        cols.add(new ColsInfo("单位地址","workplacename","180","","",""));
        cols.add(new ColsInfo("就诊日期","visitdate","100","","",""));
        cols.add(new ColsInfo("出院日期","leavedate","100","","",""));
        cols.add(new ColsInfo("住院天数","zyDays","80","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("主诊断","diseasenamePrimarySrc","180","","",""));
        cols.add(new ColsInfo("其他诊断","diseasenameOtherSrc","180","","",""));
        cols.add(new ColsInfo("就诊总费用","totalfee","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("基金支付金额","fundpay","110","",FORMAT_VALUE_ROUND_HALF_UP2,""));

        TASK_TYPE_MAP.put(SUSPICIOUS_GROUP_STATISTICS, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "可疑就诊标签汇总表",
                        "可疑就诊标签汇总表",
                        "yb_charge_suspicious_result",
                        "可疑就诊标签汇总表",
                        "可疑就诊标签汇总表",
                        cols,
                        " tag_count desc,orgname_src,visitdate"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("标签数量","tagCount","80","","",""));
        cols.add(new ColsInfo("标签列表","tagName","200","","",""));
        cols.add(new ColsInfo("医院名称","orgnameSrc","120","","",""));
        cols.add(new ColsInfo("就诊号","visitidSrc","120","","",""));
        cols.add(new ColsInfo("就诊类型","visittypeSrc","100","","",""));
        cols.add(new ColsInfo("险种类型","insurancetypename","180","","",""));
        cols.add(new ColsInfo("科室名称","deptnameSrc","100","","",""));
        cols.add(new ColsInfo("医生姓名","doctorname","80","","",""));
        cols.add(new ColsInfo("患者姓名","clientname","80","","",""));
        cols.add(new ColsInfo("患者标签","patientTagName","140","","",""));
        cols.add(new ColsInfo("年龄","yearageSrc","80","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("性别","sexSrc","80","","",""));
        cols.add(new ColsInfo("单位地址","workplacename","180","","",""));
        cols.add(new ColsInfo("就诊日期","visitdate","100","","",""));
        cols.add(new ColsInfo("出院日期","leavedate","100","","",""));
        cols.add(new ColsInfo("住院天数","zyDays","80","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("主诊断","diseasenamePrimarySrc","180","","",""));
        cols.add(new ColsInfo("其他诊断","diseasenameOtherSrc","180","","",""));
        cols.add(new ColsInfo("就诊总费用","totalfee","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("基金支付金额","fundpay","110","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("线索价值(有用/无用)","labelName","100","","",""));
        cols.add(new ColsInfo("上次标注人","labelUser","100","","",""));
        cols.add(new ColsInfo("标注时间","labelTime","120","","",""));
        TASK_TYPE_MAP.put(DWB_VISIT_TAG, new TaskTypeInfo(
                "overproofStatistics",
                "overproofResult",
                        "就诊其他标签类型",
                        "就诊其他标签类型",
                        "yb_charge_suspicious_result",
                        "就诊其他标签类型",
                        "就诊其他标签类型",
                        cols,
                        "tag_count desc,orgname_src,visitdate",
                  "dwb_visitid_tag",
                "dwb_visitid_tag_label2"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("机构名称","orgname","120","orgnameSrc","",""));
        cols.add(new ColsInfo("年","year","80","yyear","",""));
        cols.add(new ColsInfo("手术名称","surgeryName","200","","",""));
        cols.add(new ColsInfo("医保编码","itemcode","120","","",""));
        cols.add(new ColsInfo("医保名称","itemname","200","","",""));
        cols.add(new ColsInfo("全年次数","cntSum","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("日均次数","avgDayCnt","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("日最大次数","maxDayCnt","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("最大次数日期","maxDayDate","100","","",""));
        cols.add(new ColsInfo("最高单价","maxPrice","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("最低单价","minPrice","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        TASK_TYPE_MAP.put(YEAR_SURGERY_STATISTICS, new TaskTypeInfo(
                "search_new",
                "searchResult_new",
                "医院手术情况统计",
                "医院手术情况统计",
                "yb_charge_year_result",
                "医院手术情况统计",
                "医院手术情况统计",
                cols,
                "surgery_name"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("机构名称","orgnameSrc","120","","",""));
        cols.add(new ColsInfo("年度","year","80","yyear","",""));
        cols.add(new ColsInfo("日期","ddate","100","","",""));
        cols.add(new ColsInfo("长假标志","longHolidayTag","120","","",""));
        cols.add(new ColsInfo("入院人次","admitCnt","80","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("出院人次","leaveCnt","80","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("在院人数","inhospitalCnt","80","",FORMAT_VALUE_INT,""));
        TASK_TYPE_MAP.put(YEAR_ORG_ONLINE_PATIENT_COUNT, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "医院每日在院人数统计",
                        "医院每日在院人数统计",
                        "yb_charge_year_result",
                        "医院每日在院人数统计",
                        "医院每日在院人数统计",
                        cols,
                        "inhospital_cnt"
                )
        );






        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("患者名称","clientname","100","","",""));
        cols.add(new ColsInfo("患者名称+出生日期","name","150","","",""));
        cols.add(new ColsInfo("年龄","yearage","80","","",""));
        cols.add(new ColsInfo("性别","sex","80","","",""));
        cols.add(new ColsInfo("身份证号","idNo","150","","",""));
        cols.add(new ColsInfo("参保类型","insurancetype","120","","",""));
        cols.add(new ColsInfo("地址","workplacename","180","","",""));
        cols.add(new ColsInfo("联系电话","contactorphone","100","","",""));
        cols.add(new ColsInfo("年","year","80","","",""));
        cols.add(new ColsInfo("全部门诊诊断","mzDiag","200","","",""));
        cols.add(new ColsInfo("年门诊次数","mzCnt","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年门诊金额","mzTotalfee","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("全部门诊机构","mzOrgname","150","","",""));
        cols.add(new ColsInfo("门诊口服药金额","mzKfFee","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("门诊口服药种类数量","mzKfCnt","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("全部门诊口服药名称","mzKfItemname","80","","",""));
        cols.add(new ColsInfo("是否连续三天门诊购药/药品名称","ifLxstmzgy","80","","",""));
        cols.add(new ColsInfo("年住院次数","zyCnt","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年住院金额","zyTotalfee","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("全部诊断","zyDiag","80","","",""));
        cols.add(new ColsInfo("全部手术","zySurgery","80","","",""));
        cols.add(new ColsInfo("是否结伴门诊","ifJbmz","80","","",""));
        cols.add(new ColsInfo("是否结伴住院","ifJbzy","80","","",""));
        cols.add(new ColsInfo("年手术量","surgeryCn","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年门诊基金金额","mzFundpay","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年住院基金金额","zyFundpay","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("预警标签列表","tagList","200","","",""));
        TASK_TYPE_MAP.put(FRAUD_PATIENT, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "患者年度统计",
                        "患者年度统计",
                        "yb_charge_fraud_result",
                        "患者年度统计",
                        "患者年度统计",
                        cols,
                        "COALESCE(zy_totalfee,0)  desc"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("年份","nian","80","","",""));
        cols.add(new ColsInfo("年医疗费用","totalfee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年基金金额","fundpay","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年门诊基金金额","mzFundpay","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年住院基金金额","zyFundpay","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年门诊人次","mzCnt","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年住院人次","zyCnt","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年民营机构基金金额占比","myFundpayZb","180","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年未知民营公立机构基金金额占比","wzFundpayZb","180","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("就诊人数","renshu","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("定点医疗机构数量","orgSl","150","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("三级公立医疗机构数量","sanGongSl","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("三级民营医疗机构数量","sanMinSl","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("三级未知民营公立医疗机构数量","sanWzSl","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("二级公立医疗机构数量","erGongSl","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("二级民营医疗机构数量","erMinSl","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("二级未知民营公立医疗机构数量","erWzSl","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("一级公立医疗机构数量","yiGongSl","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("一级民营医疗机构数量","yiMinSl","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("一级未知民营公立医疗机构数量","yiWzSl","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("未评级公立医疗机构数量","weiGongSl","150","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("未评级民营医疗机构数量","weiMinSl","150","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("未评级未知民营公立医疗机构数量","weiWzSl","160","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("药店数量","ydSl","80","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("门诊口服药金额","mzOralFy","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("异地基金金额","ydFundpay","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("职工基金金额","zgFundpay","150","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("居民基金金额","jmFundpay","150","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        TASK_TYPE_MAP.put(FRAUD_PROJECT, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "城市年度统计指标",
                        "城市年度统计指标",
                        "yb_charge_fraud_result",
                        "城市年度统计指标",
                        "城市年度统计指标",
                        cols,
                        "nian asc"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("机构名称","orgname","180","","",""));
        cols.add(new ColsInfo("类型(药店/医院)","orgcategory","120","","",""));
        cols.add(new ColsInfo("性质","owntype","100","","",""));
        cols.add(new ColsInfo("本地/异地","localTag","100","","",""));
        cols.add(new ColsInfo("专科类型","orgtype","120","","",""));
        cols.add(new ColsInfo("医院等级","hosplevel","100","","",""));
        cols.add(new ColsInfo("年","yyear","80","","",""));
        cols.add(new ColsInfo("医疗费用总额","totalfee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("基金金额总额","fundpay","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("门诊/住院占比(不排除自费)","mzZyRatio","120","","",""));
        cols.add(new ColsInfo("医生数量","doctorCnt","120","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("日金额最大药品名称","dayMaxDrugName","140","","",""));
        cols.add(new ColsInfo("日金额最大药品数量","dayMaxDrugCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("日金额最大药品金额","dayMaxDrugFee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年金额最大药品名称","maxDrugName","140","","",""));
        cols.add(new ColsInfo("年金额最大药品数量","maxDrugCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("年金额最大药品金额","maxDrugFee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("日金额最大诊疗项目名称","dayMaxTreatName","140","","",""));
        cols.add(new ColsInfo("日金额最大诊疗项目数量","dayMaxTreatCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("日金额最大诊疗项目金额","dayMaxTreatFee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年金额最大诊疗项目名称","maxTreatName","140","","",""));
        cols.add(new ColsInfo("年金额最大诊疗项目数量","maxTreatCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("年金额最大诊疗项目金额","maxTreatFee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("门诊数量","mzCnt","120","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("门诊医疗费用","mzTotalfee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("门诊医保基金","mzFundpay","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("日最大门诊量日期","mzDayMaxDate","140","","",""));
        cols.add(new ColsInfo("日最大门诊量人次","mzDayMaxCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("门诊次均费用","mzAvgFee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("单个患者门诊年平均数量","mzAvgTimes","200","","",""));
        cols.add(new ColsInfo("住院医疗费用","zyTotalfee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("住院医保基金","zyFundpay","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("住院人次数量","zyCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("住院均次金额","zyAvgFee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("平均住院日","zyAvgDays","140","","",""));
        cols.add(new ColsInfo("日最大住院量日期","zyDayMaxDate","140","","",""));
        cols.add(new ColsInfo("日最大住院量人次","zyDayMaxCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("日最大在院量日期","zyDayMaxInDate","140","","",""));
        cols.add(new ColsInfo("日最大在院量人次","zyDayMaxInCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("日在院平均人数","zyDayAvgInClient","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("理论最小床位数(总床位数/365天)","minBedCnt","200","","",""));
        cols.add(new ColsInfo("住院数量最多疾病名称","maxDiagName","140","","",""));
        cols.add(new ColsInfo("住院数量最多疾病数量","maxDiagCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("住院数量最多疾病数量区域占比","maxDiagZb","140","","",""));

        cols.add(new ColsInfo("日数量最大手术项目名称","dayMaxCntSurgeryName","140","","",""));
        cols.add(new ColsInfo("日数量最大手术项目数量","dayMaxCntSurgeryCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("日数量最大手术项目金额","dayMaxCntSurgeryFee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("日金额最大手术项目名称","dayMaxFeeSurgeryName","140","","",""));
        cols.add(new ColsInfo("日金额最大手术项目数量","dayMaxFeeSurgeryCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("日金额最大手术项目金额","dayMaxFeeSurgeryFee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年数量最大手术项目名称","maxCntSurgeryName","140","","",""));
        cols.add(new ColsInfo("年数量最大手术项目数量","maxCntSurgeryCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("年数量最大手术项目金额","maxCntSurgeryFee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年数量最大手术项目数量区域占比","maxCntSurgeryZb","140","","",""));
        cols.add(new ColsInfo("年金额最大手术项目名称","maxFeeSurgeryName","140","","",""));
        cols.add(new ColsInfo("年金额最大手术项目数量","maxFeeSurgeryCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("年金额最大手术项目金额","maxFeeSurgeryFee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("年金额最大手术项目金额区域占比","maxFeeSurgeryZb","140","","",""));

        cols.add(new ColsInfo("低标准入院数量","tagDbzryCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("分解住院数量","tagFjzyCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("节假日住院异常程度","tagJjrycCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("门诊就诊雷同数量","tagMzltCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("住院就诊雷同数量","tagZyltCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("结伴门诊数量","tagJbmzCnt","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("结伴住院数量","tagJbzyCnt","140","",FORMAT_VALUE_INT,""));
        TASK_TYPE_MAP.put(FRAUD_HOSPITAL, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "医院年度统计",
                        "医院年度统计",
                        "yb_charge_fraud_result",
                        "医院年度统计",
                        "医院年度统计",
                        cols,
                        "scale_order asc,orgname,yyear asc"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("年","year","100","nian","",""));
        cols.add(new ColsInfo("项目名称","itemname","300","","",""));
        cols.add(new ColsInfo("项目编码","itemcode","300","","",""));
        cols.add(new ColsInfo("金额","fee","150","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        TASK_TYPE_MAP.put(DRUG_TOP200, new TaskTypeInfo(
                "search_new",
                "searchResult_new",
                        "top200口服药",
                        "top200口服药",
                        "yb_charge_search_result",
                        "top200口服药",
                        "top200口服药",
                        cols,
                        "nian desc,fee desc"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("年","year","80","nian", "",""));
        cols.add(new ColsInfo("就诊号","visitid","120","","",""));
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("医生姓名","doctorname","120","","",""));
        cols.add(new ColsInfo("科室名称","deptname","120","","",""));
        cols.add(new ColsInfo("就诊类型","visittype","100","","",""));
        cols.add(new ColsInfo("患者名称","name","140","","",""));
        cols.add(new ColsInfo("性别","sex","80","","",""));
        cols.add(new ColsInfo("就诊日期","visitdate","100","","",""));
        cols.add(new ColsInfo("出院日期","leavedate","100","","",""));
        cols.add(new ColsInfo("年龄","yearage","80","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("主诊断","disMain","180","disPrimary","",""));
        cols.add(new ColsInfo("其他诊断","dis","180","disSecondary","",""));
        cols.add(new ColsInfo("收费日期","charge","100","chagedate","",""));
        cols.add(new ColsInfo("医院项目名称","hisItemname","200","","",""));
        cols.add(new ColsInfo("医保项目名称","itemname","200","","",""));
        cols.add(new ColsInfo("标志","wgFlag","100","sign","",""));
        cols.add(new ColsInfo("自付比例","selfpayProp","80","","",""));
        cols.add(new ColsInfo("收费等级","chargeattri","100","","",""));
        cols.add(new ColsInfo("原始规格","specificaion","120","","",""));
        cols.add(new ColsInfo("单价","itemprice","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("清洗后单价","itempriceClean","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("数量","amount","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("清洗后数量","amountClean","100","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("金额","fee","80","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        TASK_TYPE_MAP.put(DRUG_TOP200_DETAIL, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "top200口服药明细",
                        "top200口服药明细",
                        "yb_charge_search_result",
                        "top200口服药明细",
                        "top200口服药明细",
                        cols,
                        "nian desc,itemname desc,visitid desc"
                )
        );



        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("机构名称","orgname","150","","",""));
        cols.add(new ColsInfo("科室名称","deptname","120","","",""));
        cols.add(new ColsInfo("就诊号","visitid","120","","",""));
        cols.add(new ColsInfo("患者id","clientid","120","","",""));
        cols.add(new ColsInfo("身份证号","idNo","120","","",""));
        cols.add(new ColsInfo("总费用","totalfee","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("报销金额","fundpay","120","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("医生姓名","doctorname","120","","",""));
        cols.add(new ColsInfo("就诊类型","visittype","100","","",""));
        cols.add(new ColsInfo("患者名称","name","140","","",""));
        cols.add(new ColsInfo("性别","sex","80","","",""));
        cols.add(new ColsInfo("年龄","yearage","80","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("单位地址","workplacename","180","","",""));
        cols.add(new ColsInfo("就诊日期","visitdate","100","","",""));
        cols.add(new ColsInfo("出院日期","leavedate","100","","",""));
        cols.add(new ColsInfo("费用发生日期","charge","100","chargedate","",""));
        cols.add(new ColsInfo("住院天数","zyDays","80","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("疾病主诊断","disPrimary","180","","",""));
        cols.add(new ColsInfo("疾病次诊断","disSecondary","180","","",""));
        cols.add(new ColsInfo("医院收费名称","hisItemname","200","","",""));
        cols.add(new ColsInfo("医保收费名称","itemname","200","","",""));
        cols.add(new ColsInfo("数量","amount","80","sl",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("金额","fee","80","fy",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("标签","tagName","120","","",""));
        TASK_TYPE_MAP.put(DATAMINING_SQL_DETAIL, new TaskTypeInfo(
                        "search_new",
                        "searchResult_new",
                        "标签模型明细数据",
                        "标签模型明细数据",
                        "yb_charge_dept_result",
                        "标签模型明细数据",
                        "标签模型明细数据",
                        cols,
                        "orgname"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("yx患者编号","clientid","120","","",""));
        cols.add(new ColsInfo("身份证件号码","idNo","120","","",""));
        cols.add(new ColsInfo("姓名","clientname","120","","",""));
        cols.add(new ColsInfo("性别","sex","80","","",""));
        cols.add(new ColsInfo("年龄","yearage","100","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("本次住院就诊id","zyIdThis","140","","",""));
        cols.add(new ColsInfo("本次住院病案号","caseIdThis","140","","",""));
        cols.add(new ColsInfo("本次住院日期","admitdateThis","140","","",""));
        cols.add(new ColsInfo("本次出院日期","leavedateThis","140","","",""));
        cols.add(new ColsInfo("本次住院医疗机构编码","orgidThis","140","","",""));
        cols.add(new ColsInfo("本次住院医疗机构名称","orgnameThis","140","","",""));
        cols.add(new ColsInfo("科室名称（原始）","deptnameThis","140","","",""));
        cols.add(new ColsInfo("本次住院负责医师姓名","doctornameThis","140","","",""));
        cols.add(new ColsInfo("本次住院天数","zyDaysThis","100","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("本次就诊疾病名称","diseasenameThis","140","","",""));
        cols.add(new ColsInfo("本次住院总费用","totalfeeThis","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("本次住院医保基金支付金额","fundpayThis","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("上次就诊ID","zyIdLast","140","","",""));
        cols.add(new ColsInfo("上次住院病案号","caseIdLast","140","","",""));
        cols.add(new ColsInfo("上次住院日期","admitdateLast","140","","",""));
        cols.add(new ColsInfo("上次出院日期","leavedateLast","140","","",""));
        cols.add(new ColsInfo("上次住院医疗机构编码","orgidLast","140","","",""));
        cols.add(new ColsInfo("上次就诊医疗机构名称","orgnameLast","140","","",""));
        cols.add(new ColsInfo("上次就诊科室名称","deptnameLast","140","","",""));
        cols.add(new ColsInfo("上次住院负责医师姓名","doctornameLast","140","","",""));
        cols.add(new ColsInfo("上次住院天数","zyDaysLast","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("上次疾病诊断名称","diseasenameLast","140","","",""));
        cols.add(new ColsInfo("上次住院总费用","totalfeeLast","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("上次住院医保基金支付金额","fundpayLast","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("两次住院间隔天数","zyApartDays","140","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("本次上次主要诊断是否一致","primarydiagSameSign","140","","",""));
        cols.add(new ColsInfo("本次上次医疗机构是否一致","orgSameSign","140","","",""));
        cols.add(new ColsInfo("本次主要疾病名称","priDiseasenameThis","140","","",""));
        cols.add(new ColsInfo("上次主要疾病名称","priDiseasenameLast","140","","",""));

        TASK_TYPE_MAP.put(DWS_INHOSPITAL_APART, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "分解住院",
                        "分解住院",
                        "yb_charge_dws_inhospital_apart",
                        "分解住院",
                        "分解住院",
                        cols,
                        "orgname_this,admitdate_this",
                "dwb_visitid_tag",
                "dwb_visitid_tag_label3"
                )
        );

        cols = new ArrayList<ColsInfo>();
        cols.add(new ColsInfo("就诊号","visitid","140","","",""));
        cols.add(new ColsInfo("机构名称","orgname","120","","",""));
        cols.add(new ColsInfo("患者id","clientid","120","","",""));
        cols.add(new ColsInfo("身份证号","idNo","120","","",""));
        cols.add(new ColsInfo("患者姓名","name","120","","",""));
        cols.add(new ColsInfo("患者性别","sex","100","","",""));
        cols.add(new ColsInfo("患者年龄","yearage","100","",FORMAT_VALUE_YEARAGE,""));
        cols.add(new ColsInfo("就诊类型","visittype","100","","",""));
        cols.add(new ColsInfo("患者入院时间","visitdate","120","","",""));
        cols.add(new ColsInfo("患者出院时间","leavedate","120","","",""));
        cols.add(new ColsInfo("患者住院天数","zyDays","100","",FORMAT_VALUE_INT,""));
        cols.add(new ColsInfo("患者就诊科室","deptname","140","","",""));
        cols.add(new ColsInfo("患者就诊医生姓名","doctorname","140","","",""));
        cols.add(new ColsInfo("患者主诊断疾病","disPrimary","140","","",""));
        cols.add(new ColsInfo("患者次诊断疾病","disSecondary","140","","",""));
        cols.add(new ColsInfo("患者总费用","totalfee","140","","",""));
        cols.add(new ColsInfo("患者基金支付金额","fundpay","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("患者工作单位","workplacename","140","","",""));
        cols.add(new ColsInfo("医生就诊id","docVisitid","140","","",""));
        cols.add(new ColsInfo("机构名称","docOrgname","140","","",""));
        cols.add(new ColsInfo("医生名称","docClientname","140","","",""));
        cols.add(new ColsInfo("医生入院日期","docVisitdate","140","","",""));
        cols.add(new ColsInfo("医生出院日期","docLeavedate","140","","",""));
        cols.add(new ColsInfo("医生工作单位","docWorkplacename","140","","",""));
        cols.add(new ColsInfo("医生就诊总金额","docTotalfee","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("医生基金支付金额","docFundpay","140","",FORMAT_VALUE_ROUND_HALF_UP2,""));
        cols.add(new ColsInfo("医生主诊断疾病","docDisPrimary","140","","",""));
        cols.add(new ColsInfo("医生次诊断疾病","docDisSecondary","140","","",""));
        TASK_TYPE_MAP.put(DOCTOR_ADMIT_PATIENT_INSICK, new TaskTypeInfo(
                        "overproofStatistics",
                        "overproofResult",
                        "医生住院期间收治病人",
                        "医生住院期间收治病人",
                        "yb_charge_doctor_admit_patient_insick",
                        "医生住院期间收治病人",
                        "医生住院期间收治病人",
                        cols,
                        "orgname,visitdate",
                "dwb_visitid_tag",
                "dwb_visitid_tag_label"
                )
        );

        for (String key : TASK_TYPE_MAP.keySet()) {
            TaskTypeInfo infoBean = TASK_TYPE_MAP.get(key);
            List<ColsInfo> infoCols = infoBean.getCols();
            String titles = infoCols.stream().map(item->item.getTitle()).collect(Collectors.joining(","));
            String fields = infoCols.stream().map(item->item.getField()).collect(Collectors.joining(","));
            infoBean.setTitleStr(titles);
            infoBean.setFieldStr(fields);
        }
    }

    @Data
    public static class ColsInfo implements java.io.Serializable{
        private String title;
        private String field;
        private String width;
        private String minWidth;
        private String sqlField;
        private String formatValue;
        private boolean isShow = true;

        private boolean isExtendField = false;

        ColsInfo(String title,String field,String width,String sqlField,String formatValue,String minWidth){
            this.title = title;
            this.field = field;
            this.width = width;
            this.sqlField = sqlField;
            this.formatValue = formatValue;
            this.minWidth = minWidth;

        }

        ColsInfo(String title,String field,String width,String sqlField,String formatValue,String minWidth,boolean isShow){
            this(title,field,width,sqlField,formatValue,minWidth);
            this.isShow = isShow;
        }

        ColsInfo(String title,String field,String width,String sqlField,String formatValue,String minWidth,boolean isShow,boolean isExtendField){
            this(title,field,width,sqlField,formatValue,minWidth,isShow);
            this.isExtendField = isExtendField;
        }
    }


    @Data
    public static class TaskTypeInfo implements java.io.Serializable{
        //任务列表页地址
        private String page;
        //预览页地址
        private String resultPage;
        //页面title
        private String title;
        //预览页title
        private String detailTitle;
        //结果表
        private String resultTable;
        //导出文件名
        private String fileName;
        //导出sheet名
        private String sheefName;
        //字段标题
        private String titleStr;
        //字段名
        private String fieldStr;

        private List<ColsInfo> cols;

        //hive 分页排序字段
        private String pageOrderField;

        //标签结果表
        private String lableSourceTable;

        //标签存储表
        private String lableTargetTable;


        TaskTypeInfo(String page,
                     String resultPage,
                     String title,
                     String detailTitle,
                     String resultTable,
                     String fileName,
                     String sheefName,
                     List<ColsInfo> cols,
                     String pageOrderField) {
            this.page = page;
            this.resultPage = resultPage;
            this.title = title;
            this.detailTitle = detailTitle;
            this.resultTable = resultTable;
            this.fileName = fileName;
            this.sheefName = sheefName;
            this.cols = cols;
            this.pageOrderField = pageOrderField;
        }

        TaskTypeInfo(String page,
                     String resultPage,
                     String title,
                     String detailTitle,
                     String resultTable,
                     String fileName,
                     String sheefName,
                     List<ColsInfo> cols,
                     String pageOrderField,
                     String lableSourceTable,
                     String lableTargetTable) {
            this(page,resultPage,title,detailTitle,resultTable,fileName,sheefName,cols,pageOrderField);
            this.lableSourceTable = lableSourceTable;
            this.lableTargetTable = lableTargetTable;
        }
    }

    public static Map<String,String> getSqlFieldOrFormatMap(String taskType, String type) {
        List<ColsInfo> cols =TASK_TYPE_MAP.get(taskType).getCols();
        Map<String,String> map = new HashMap<>();
        for(ColsInfo colInfo:cols){
            if(StringUtils.isNotBlank(colInfo.getSqlField())&&"sqlField".equals(type)){
                map.put(colInfo.getSqlField(),colInfo.getField());
            }
            if(StringUtils.isNotBlank(colInfo.formatValue)&&"formatValue".equals(type)){
                map.put(colInfo.getField(),colInfo.formatValue);
            }
        }
        return map;
    }

    public static Map<String,String> getSqlFieldMap(String taskType) {
        return getSqlFieldOrFormatMap(taskType,"sqlField");
    }

    public static Map<String,String> getFormatMap(String taskType) {
        return getSqlFieldOrFormatMap(taskType,"formatValue");
    }



    public static Object getFormatValue(Map<String,String> map, String colName, Object value) {
        if(FORMAT_VALUE_YEARAGE.equals(map.get(colName))){
            if(StringUtils.isBlank(value.toString())){
                value = "";
            }else{
                BigDecimal year = new BigDecimal(value.toString()).setScale(1,BigDecimal.ROUND_HALF_UP);
                if(year.compareTo(new BigDecimal("1"))>-1){
                    year = year.setScale(0,BigDecimal.ROUND_HALF_UP);
                }
                value = year.toString();
            }
        }else if(FORMAT_VALUE_ROUND_HALF_UP2.equals(map.get(colName))){
            if(StringUtils.isBlank(value.toString())){
                value = null;
            }else{
                value=new BigDecimal(value.toString()).setScale(2,BigDecimal.ROUND_HALF_UP);
            }
        }else if(FORMAT_VALUE_INT.equals(map.get(colName))){
            if(StringUtils.isBlank(value.toString())){
                value = null;
            }else{
                value=Double.valueOf(value.toString()).intValue();
            }
        }else if(FORMAT_VALUE_ETL_SOURCE.equals(map.get(colName))){
            if("A01".equals(value.toString())){
                value = "医保";
            }else if("A02".equals(value.toString())){
                value = "农合";
            }else if("A03".equals(value.toString())){
                value = "HIS";
            }else if("A04".equals(value.toString())){
                value = "药店";
            }
        }else  if(colName.startsWith("y")&&colName.indexOf("年")!=-1&&colName.indexOf("月")!=-1){//月度统计 年月字段
            if(StringUtils.isBlank(value.toString())){
                value = null;
            }else{
                value=new BigDecimal(value.toString()).setScale(2,BigDecimal.ROUND_HALF_UP);
            }
        }else if(FORMAT_VALUE_RATE.equals(map.get(colName))){
            if(StringUtils.isBlank(value.toString())){
                value = null;
            }else{
                value=new BigDecimal(value.toString()).setScale(4,BigDecimal.ROUND_HALF_UP);
                value = ((BigDecimal) value).multiply(new BigDecimal("100"));
            }
        }else if(FORMAT_VALUE_BED_AMOUNT.equals(map.get(colName))){
            if(StringUtils.isBlank(value.toString())){
                value = "未获取编制床位数";
            }else if("0".equals(value.toString())){
                value = "未获取编制床位数";
            }
        }
        return value;
    }

    public static void main(String args[]) {
        /*System.out.println("{");
        for (String key : TASK_TYPE_MAP.keySet()) {

            TaskTypeInfo infoBean = TASK_TYPE_MAP.get(key);
            List<ColsInfo> cols = infoBean.getCols();
            if(cols.size()==0){
                continue;
            }
            System.out.println("'"+ key +"':{");
            System.out.println("    resultColumns:[" );
            for(ColsInfo colInfo:cols){
                System.out.println("        {\n" +
                        "        title: '"+colInfo.getTitle()+"',\n" +
                        "        dataIndex: '"+colInfo.getField()+"',\n" +
                        "        width: '"+colInfo.getWidth()+"px'},");
            }

            System.out.println("    ]");
            System.out.println("},");
        }
        System.out.println("}");*/
       /* for (String key : TASK_TYPE_MAP.keySet()) {
            List<ColsInfo> cols =TASK_TYPE_MAP.get(key).getCols();
            Map<String,String> sqlFieldMap = new HashMap<>();
            Map<String,String> formateMap = new HashMap<>();
            for(ColsInfo colInfo:cols){
                if(StringUtils.isNotBlank(colInfo.getSqlField())){
                    sqlFieldMap.put(colInfo.getSqlField(),colInfo.getField());
                }
                if(StringUtils.isNotBlank(colInfo.formatValue)){
                    formateMap.put(colInfo.getField(),colInfo.formatValue);
                }
            }
            System.out.println(key+"::::"+formateMap.toString());
        }*/

       String str = "`project`  COMMENT '项目地',\n" +
               "  `visitid`  COMMENT '就诊id',\n" +
               "  `orgname`   COMMENT '机构名称',\n" +
               "  `clientid`  COMMENT '患者id',\n" +
               "  `id_no`  COMMENT '身份证号',\n" +
               "  `name`   COMMENT '患者姓名',\n" +
               "  `sex`  COMMENT '患者性别',\n" +
               "  `yearage`  COMMENT '患者年龄',\n" +
               "  `visittype`  COMMENT '就诊类型',\n" +
               "  `visitdate`  COMMENT '患者入院时间',\n" +
               "  `leavedate`  COMMENT '患者出院时间',\n" +
               "  `zy_days` COMMENT '患者住院天数',\n" +
               "  `deptname`  COMMENT '患者就诊科室',\n" +
               "  `doctorname`  COMMENT '患者就诊医生姓名',\n" +
               "  `dis_primary`  COMMENT '患者主诊断疾病',\n" +
               "  `dis_secondary`  COMMENT '患者次诊断疾病',\n" +
               "  `totalfee` COMMENT '患者总费用',\n" +
               "  `fundpay` COMMENT '患者基金支付金额',\n" +
               "  `workplacename`  COMMENT '患者工作单位',\n" +
               "  `doc_visitid`  COMMENT '医生就诊id',\n" +
               "  `doc_orgname`   COMMENT '机构名称',\n" +
               "  `doc_clientname`  COMMENT '医生名称',\n" +
               "  `doc_visitdate`  COMMENT '医生入院日期',\n" +
               "  `doc_leavedate`  COMMENT '医生出院日期',\n" +
               "  `doc_workplacename`  COMMENT '医生工作单位',\n" +
               "  `doc_totalfee`  COMMENT '医生就诊总金额',\n" +
               "  `doc_fundpay`  COMMENT '医生基金支付金额',\n" +
               "  `doc_dis_primary`  COMMENT '医生主诊断疾病',\n" +
               "  `doc_dis_secondary`  COMMENT '医生次诊断疾病',";
       String[] strArr = str.split(",");
       for(String colarr:strArr){
           String[] cols = colarr.split("COMMENT");
           String col = cols[0].trim().replace("`","");
           String name = cols[1].trim().replace("'","");
           if(col.indexOf("_fee")>-1){
               System.out.println("cols.add(new ColsInfo(\""+name+"\",\""+StringCamelUtils.underline2Camel(col)+"\",\"140\",\"\",FORMAT_VALUE_ROUND_HALF_UP2,\"\"));");
           }else if(col.indexOf("_cnt")>-1){
               System.out.println("cols.add(new ColsInfo(\""+name+"\",\""+StringCamelUtils.underline2Camel(col)+"\",\"140\",\"\",FORMAT_VALUE_INT,\"\"));");
           }else if(name.indexOf("额")>-1){
               System.out.println("cols.add(new ColsInfo(\""+name+"\",\""+StringCamelUtils.underline2Camel(col)+"\",\"140\",\"\",FORMAT_VALUE_ROUND_HALF_UP2,\"\"));");
           }else{
               System.out.println("cols.add(new ColsInfo(\""+name+"\",\""+StringCamelUtils.underline2Camel(col)+"\",\"140\",\"\",\"\",\"\"));");
           }


       }

    }
}
