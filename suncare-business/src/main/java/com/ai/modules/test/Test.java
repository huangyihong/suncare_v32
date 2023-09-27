package com.ai.modules.test;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
    public static void main(String args[]){
        String sql = "  @`id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL" +
                "  @`visitid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL" +
                "  @`medical_no`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`clientid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`id_no`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`insurancetype`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`clientname`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sex_code`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sex`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sex_code_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sex_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`birthday`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`yearage`@ int4" +
                "  @`monthage`@ int4" +
                "  @`dayage`@ int4" +
                "  @`visittype_id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`visittype`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`visittype_id_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`visittype_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`visitdate`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`visit_sign`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`orgid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`orgname`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`orgid_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`orgname_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`hosplevel`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`hospgrade`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`orgtype_code`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`orgtype`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`deptid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`deptname`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`deptid_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`deptname_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`doctorid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`doctorname`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`totalfee`@ numeric(38,4)" +
                "  @`leavedate`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`diseasecode`@ varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`diseasename`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`diseasecode_src`@ varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`diseasename_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`pathonogy_disease`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`pathonogy_diseasecode`@ varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`yb_visitid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`his_visitid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`his_visitid_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`visitid_dummy`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`visitid_connect`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`zy_days`@ int4" +
                "  @`zy_days_calculate`@ int4" +
                "  @`fundpay`@ numeric(38,4)" +
                "  @`fun_settleway_id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`fun_settleway_name`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`data_resouce_id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`data_resouce`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`etl_source`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`etl_source_name`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`etl_time`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`break_state`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`case_id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`case_name`@ varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`gen_data_time`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`project_id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`project_name`@ varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`batch_id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`action_money`@ numeric(38,4)" +
                "  @`case_score`@ numeric(38,4)" +
                "  @`action_type_id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`action_type_name`@ varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`action_id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`action_name`@ varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`action_desc`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`rule_level`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`busi_type`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`break_rule_content`@ varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`max_action_money`@ numeric(38,4)" +
                "  @`min_money`@ numeric(38,4)" +
                "  @`max_money`@ numeric(38,4)" +
                "  @`rule_basis`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`rule_id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`rule_fname`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`rule_limit`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`rule_grade`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`rule_grade_remark`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`review_name`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`fir_review_userid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`fir_review_username`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`fir_review_time`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`fir_review_status`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`fir_review_classify`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`fir_review_remark`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`push_status`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`push_userid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`push_username`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`push_time`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sec_review_userid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sec_review_username`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sec_review_time`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sec_review_status`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sec_review_remark`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sec_review_classify`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sec_push_status`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sec_push_userid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sec_push_username`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`sec_push_time`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`main_flag`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`cus_review_userid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`cus_review_username`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`cus_review_time`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`cus_review_status`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`cus_review_remark`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`clinical_group_ids`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`clinical_group_names`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`clinical_drug_money`@ numeric(38,4)" +
                "  @`clinical_treat_money`@ numeric(38,4)" +
                "  @`clinical_drug_money_ratio`@ numeric(38,4)" +
                "  @`clinical_treat_money_ratio`@ numeric(38,4)" +
                "  @`clinical_drug_beyond_money`@ numeric(38,4)" +
                "  @`clinical_treat_beyond_money`@ numeric(38,4)" +
                "  @`clinical_drug_beyond_money_ratio`@ numeric(38,4)" +
                "  @`clinical_treat_beyond_money_ratio`@ numeric(38,4)" +
                "  @`itemcode`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`itemname`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`item_amt`@ numeric(38,4)" +
                "  @`item_qty`@ int4" +
                "  @`fund_cover`@ numeric(38,4)" +
                "  @`itemprice_max`@ numeric(38,4)" +
                "  @`selfpay_prop_min`@ numeric(38,4)" +
                "  @`rule_scope`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`rule_scope_name`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`mutex_item_code`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`mutex_item_name`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`ai_item_cnt`@ int4" +
                "  @`ai_out_cnt`@ int4" +
                "  @`item_id`@ varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`chargedate`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`itemcode_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`itemname_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`chargeclass_id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`chargeclass`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`his_itemcode`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`his_itemname`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`his_itemcode_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`his_itemname_src`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`togetherid`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`togethername`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`group_patient_qty`@ int4" +
                "  @`group_cnt`@ int4" +
                "  @`issue_id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`issue_name`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`xmkh_id`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`xmkh_name`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`task_batch_name`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`handle_status`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`summary_field`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`summary_field_value`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`ext1`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`ext2`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`ext3`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`ext4`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`ext5`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`ext6`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`ext7`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`ext8`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`ext9`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`ext10`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`proof_diag`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`proof_treat`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`proof_drug`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`probility`@ numeric(38,4)" +
                "  @`predict_label`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL" +
                "  @`project`@ varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL\n";

        String sql2="id IS 主键;\n" +

                "visitid IS 就诊id;\n" +

                "medical_no IS 病案号DWB_MASTER_INFO.CASE_ID;\n" +

                "clientid IS yx患者编号;\n" +

                "id_no IS 身份证件号码;\n" +

                "insurancetype IS 参保类别;\n" +

                "clientname IS 患者姓名;\n" +

                "sex_code IS 性别代码;\n" +

                "sex IS 性别名称;\n" +

                "sex_code_src IS 性别代码_src;\n" +

                "sex_src IS 性别名称_src;\n" +

                "birthday IS 出生日期;\n" +

                "yearage IS 年龄（岁）;\n" +

                "monthage IS 年龄（月）;\n" +

                "dayage IS 年龄（天）;\n" +

                "visittype_id IS 就诊类型代码;\n" +

                "visittype IS 就诊类型名称;\n" +

                "visittype_id_src IS 就诊类型代码_src;\n" +

                "visittype_src IS 就诊类型名称_src;\n" +

                "visitdate IS 就诊日期时间;\n" +

                "visit_sign IS 就诊标志;\n" +

                "orgid IS 就诊医疗机构编码;\n" +

                "orgname IS 就诊医疗机构名称;\n" +

                "orgid_src IS 就诊医疗机构编码_src;\n" +

                "orgname_src IS 就诊医疗机构名称_src;\n" +

                "hosplevel IS 医院级别;\n" +

                "hospgrade IS 医疗机构等级;\n" +

                "orgtype_code IS 医疗机构类型编码;\n" +

                "orgtype IS 医疗机构类型;\n" +

                "deptid IS 就诊科室编码;\n" +

                "deptname IS 就诊科室名称;\n" +

                "deptid_src IS 就诊科室编码_src;\n" +

                "deptname_src IS 就诊科室名称_src;\n" +

                "doctorid IS 就诊医师编码;\n" +

                "doctorname IS 就诊医师姓名;\n" +

                "totalfee IS 医疗费用总金额;\n" +

                "leavedate IS 出院日期;\n" +

                "diseasecode IS 疾病编码;\n" +

                "diseasename IS 疾病名称;\n" +

                "diseasecode_src IS 疾病编码（原始）;\n" +

                "diseasename_src IS 疾病名称（原始）;\n" +

                "pathonogy_disease IS 病理诊断名称;\n" +

                "pathonogy_diseasecode IS 病理诊断疾病编码;\n" +

                "yb_visitid IS 医保/农合就诊id;\n" +

                "his_visitid IS his就诊id;\n" +

                "his_visitid_src IS his就诊id_src;\n" +

                "visitid_dummy IS 虚拟就诊id;\n" +

                "visitid_connect IS his和医保/农合关联的visitid;\n" +

                "zy_days IS 实际住院天数;\n" +

                "zy_days_calculate IS 计算的住院天数;\n" +

                "fundpay IS 本次基金支付金额;\n" +

                "fun_settleway_id IS 医保结算方式代码;\n" +

                "fun_settleway_name IS 医保结算方式名称;\n" +

                "data_resouce_id IS 数据来源编码;\n" +

                "data_resouce IS 数据来源名称;\n" +

                "etl_source IS etl来源编码;\n" +

                "etl_source_name IS etl来源名称;\n" +

                "etl_time IS etl时间;\n" +

                "break_state IS 违规说明;\n" +

                "case_id IS 模型ID，临床路径ID，药品ID，收费项目ID等;\n" +

                "case_name IS 模型名称，临床路径名称，药品名称，收费项目名称等;\n" +

                "gen_data_time IS 数据生成时间;\n" +

                "project_id IS 项目ID;\n" +

                "project_name IS 项目名称;\n" +

                "batch_id IS 项目批次号;\n" +

                "action_money IS 最小基金支出金额;\n" +

                "case_score IS 模型得分;\n" +

                "action_type_id IS 不合规行为类型ID;\n" +

                "action_type_name IS 不合规行为类型名称;\n" +

                "action_id IS 不合规行为ID;\n" +

                "action_name IS 不合规行为名称;\n" +

                "action_desc IS 不合规行为释义;\n" +

                "rule_level IS 不合规行为级别;\n" +

                "busi_type IS 不合规行为级别;\n" +

                "break_rule_content IS 违规内容(关联项目内容);\n" +

                "max_action_money IS 最大基金支出金额;\n" +

                "min_money IS 最小违规金额;\n" +

                "max_money IS 最大违规金额;\n" +

                "rule_basis IS 规则依据;\n" +

                "rule_id IS 合规ID;\n" +

                "rule_fname IS 合规名称;\n" +

                "rule_limit IS 规则限定类型;\n" +

                "rule_grade IS 规则级别;\n" +

                "rule_grade_remark IS 规则级别备注;\n" +

                "review_name IS 审核名称;\n" +

                "fir_review_userid IS 初审人ID;\n" +

                "fir_review_username IS 初审人姓名;\n" +

                "fir_review_time IS 初审时间;\n" +

                "fir_review_status IS 初审状态{init:待处理,white:白名单,blank:黑名单,grey:灰名单};\n" +

                "fir_review_classify IS 初审归类;\n" +

                "fir_review_remark IS 初审备注;\n" +

                "push_status IS 初审是否推送{1:是,0:否};\n" +

                "push_userid IS 推送人ID;\n" +

                "push_username IS 推送人;\n" +

                "push_time IS 推送时间;\n" +

                "sec_review_userid IS 第二次审核人ID;\n" +

                "sec_review_username IS 第二次审核人姓名;\n" +

                "sec_review_time IS 第二次审核时间;\n" +

                "sec_review_status IS 第二次审核状态{init:未处理,wait:待审查,begin:审查中,exclude:排除,end:审查结束,reject:驳回,recover:收回};\n" +

                "sec_review_remark IS 第二次审核备注;\n" +

                "sec_review_classify IS 第二次审核备注;\n" +

                "sec_push_status IS 第二次是否推送客户{1:是,0:否};\n" +

                "sec_push_userid IS 第二次推送人ID;\n" +

                "sec_push_username IS 第二次推送人;\n" +

                "sec_push_time IS 第二次推送人;\n" +

                "main_flag IS 是否主要违规行为{1:是,0:否};\n" +

                "cus_review_userid IS 客户审查人ID;\n" +

                "cus_review_username IS 客户审查人姓名;\n" +

                "cus_review_time IS 客户审查时间;\n" +

                "cus_review_status IS 客户审查状态{init:未处理,wait:待审查,begin:审查中,exclude:排除,sure:确认,reject:驳回};\n" +

                "cus_review_remark IS 客户审查备注;\n" +

                "clinical_group_ids IS 满足的临床路径准入条件组ID;\n" +

                "clinical_group_names IS 满足的临床路径准入条件组名称;\n" +

                "clinical_drug_money IS 临床路径药品范围内金额;\n" +

                "clinical_treat_money IS 临床路径项目范围内金额;\n" +

                "clinical_drug_money_ratio IS 临床路径药品范围内金额占比;\n" +

                "clinical_treat_money_ratio IS 临床路径项目范围内金额占比;\n" +

                "clinical_drug_beyond_money IS 临床路径药品范围外金额（预留）;\n" +

                "clinical_treat_beyond_money IS 临床路径项目范围外金额（预留）;\n" +

                "clinical_drug_beyond_money_ratio IS 临床路径药品范围外金额占比（预留）;\n" +

                "clinical_treat_beyond_money_ratio IS 临床路径项目范围外金额占比（预留）;\n" +

                "itemcode IS 项目编码;\n" +

                "itemname IS 项目名称;\n" +

                "item_amt IS 项目费用;\n" +

                "item_qty IS 项目数量;\n" +

                "fund_cover IS 医保基金支出金额;\n" +

                "itemprice_max IS 项目最高单价;\n" +

                "selfpay_prop_min IS 项目最低自付比例;\n" +

                "rule_scope IS 违规范围;\n" +

                "rule_scope_name IS 违规范围名称;\n" +

                "mutex_item_code IS 冲突项目编码;\n" +

                "mutex_item_name IS 冲突项目名称;\n" +

                "ai_item_cnt IS 项目频次/数量;\n" +

                "ai_out_cnt IS 超出频次/数量;\n" +

                "item_id IS 关联ID;\n" +

                "chargedate IS 收费日期;\n" +

                "itemcode_src IS 项目编码_src;\n" +

                "itemname_src IS 项目名称_src;\n" +

                "chargeclass_id IS 收费类别编码;\n" +

                "chargeclass IS 收费类别名称;\n" +

                "his_itemcode IS 医院收费项目编码;\n" +

                "his_itemname IS 医院收费项目名称;\n" +

                "his_itemcode_src IS 医院收费项目编码（原始）;\n" +

                "his_itemname_src IS 医院收费项目名称（原始）;\n" +

                "togetherid IS 结伴组ID;\n" +

                "togethername IS 结伴组名称;\n" +

                "group_patient_qty IS 结伴人数;\n" +

                "group_cnt IS 结伴次数;\n" +

                "issue_id IS 周期ID;\n" +

                "issue_name IS 周期名称;\n" +

                "xmkh_id IS 项目客户ID;\n" +

                "xmkh_name IS 项目客户名称;\n" +

                "task_batch_name IS 任务批次名称;\n" +

                "handle_status IS 处理状态0.待处理,1.已处理;\n" +

                "summary_field IS 汇总字段;\n" +

                "summary_field_value IS 汇总字段值;\n" +

                "ext1 IS 汇总字段预留;\n" +

                "ext2 IS 汇总字段预留;\n" +

                "ext3 IS 汇总字段预留;\n" +

                "ext4 IS 汇总字段预留;\n" +

                "ext5 IS 汇总字段预留;\n" +

                "ext6 IS 汇总字段预留;\n" +

                "ext7 IS 汇总字段预留;\n" +

                "ext8 IS 汇总字段预留;\n" +

                "ext9 IS 汇总字段预留;\n" +

                "ext10 IS 汇总字段预留;\n" +

                "proof_diag IS 满足条件的疾病信息;\n" +

                "proof_treat IS 满足条件的检查项目信息;\n" +

                "proof_drug IS 满足条件的药品信息;\n" +

                "probility IS AI识别的概率;\n" +

                "predict_label IS AI识别的黑灰白结果 white:白名单,blank:黑名单,grey:灰名单;\n" +

                "project IS 项目地;\n";

        Map<String,String> map = new HashMap<>();

        String[] list = sql.split("  @");
        List<String> fields = new ArrayList<>();
        Arrays.stream(list).forEach(t->{
            if(StringUtils.isNotBlank(t)){
                String[] filed = t.split("@");
                //System.out.println(filed[0]+filed[1]);
                map.put(filed[0],filed[1]);
                fields.add(filed[0]);
            }

        });
       String[] list2 = sql2.split(";\n");
         Arrays.stream(list2).forEach(t->{
            if(StringUtils.isNotBlank(t)){
                String[] filed = t.split(" IS ");
                //System.out.println(filed[0]+filed[1]);
                String field2 = map.get("`"+filed[0]+"`");
                field2  +=" COMMENT '"+filed[1]+"',";
                map.put("`"+filed[0]+"`",field2);
            }

        });
        fields.stream().forEach(t->{
            System.out.println(t+map.get(t));
        });

        return;
/*


        List<Map<String, Object>> list= new ArrayList<>();
        Map<String, Object>  map = new HashMap<>();
//        map.put("CODE","Z98");
//        map.put("VALUE","3");
//        map.put("PARENT_CODE","Z80-Z99");
//        list.add(map);

        map = new HashMap<>();
        map.put("CODE","Z80-Z99");
        map.put("VALUE","2");
        map.put("PARENT_CODE","Z00-Z99");
        list.add(map);

//        map = new HashMap<>();
//        map.put("CODE","Z98.8");
//        map.put("VALUE","4");
//        map.put("PARENT_CODE","Z98");
//        list.add(map);


        map = new HashMap<>();
        map.put("CODE","Z00-Z99");
        map.put("VALUE","1");
        map.put("PARENT_CODE","");
        list.add(map);

//        list=null;

//        list.stream()
//                .filter(d-> StringUtils.isBlank((String)d.get("PARENT_CODE")))
//                .flatMap(d-> Stream.concat(Stream.of(d), findChildren(d,list))).forEach(System.out::println);

        List<Map<String, Object>> data = (List<Map<String, Object>>) list.stream()
                .filter(d-> StringUtils.isBlank((String)d.get("PARENT_CODE")))
                .flatMap(d-> Stream.concat(Stream.of(d), getChildNode(d,list))).collect(Collectors.toList());

        data.forEach(System.out::println);
        */
/*List<Map<String, Object>> data = new ArrayList<>();
        list.forEach(t ->{
            if(Strings.isBlank((String)t.get("PARENT_CODE"))){
                data.add(t);

//            }else{
//                data.add(getChildNode(t, list));
            }
        });

        for(int i=0;i<list.size()-1;i++){
            data.add(getChildNode(data.get(data.size()-1), list));
        }


*//*



//        data.add(getChildNode(data.get(data.size()-1), list));
//        data.add(getChildNode(data.get(data.size()-1), list));
//        data.forEach(
//                System.out::println);
*/
/*

        list.stream().filter(t -> Strings.isBlank((String)t.get("PARENT_CODE")))
               .map(t -> {
                    t.put("childList",getChildNode(t, list));
                    return t;
                }).

*//*


*/

    }

    private static List<Map<String, Object>> getChildrens(Map<String, Object> root,List<Map<String, Object>> alllist){
        List<Map<String, Object>> children=alllist.stream().filter(t ->{
            return  Objects.equals((String)t.get("PARENT_CODE"),(String)root.get("CODE"));
        }).map(t -> {
            t.put("childList",getChildrens(t, alllist));
//            return (String)t.get("CODE");
            return t;
        }).collect(Collectors.toList());
        return children;
    }

//    private static Map<String, Object> getChildNode(Map<String, Object> root,List<Map<String, Object>> alllist){
//        List<Map<String, Object>> children=alllist.stream().filter(t ->{
//            return  Objects.equals((String)t.get("PARENT_CODE"),(String)root.get("CODE"));
//        }).collect(Collectors.toList());
//        if(children!=null&&children.size()>0)
//            return children.get(0);
//        else
//            return null;
//    }

    private static Stream findChildren(Map<String, Object> root,List<Map<String, Object>> alllist) {
        return alllist.stream()
                .filter(d-> Objects.equals((String)d.get("PARENT_CODE"),(String)root.get("CODE")))
                .flatMap(d->Stream.concat(Stream.of(d), findChildren(d,alllist)));

    }

    private static Stream getChildNode(Map<String, Object> root, List<Map<String, Object>> alllist) {
        return alllist.stream()
                .filter(d-> Objects.equals((String)d.get("PARENT_CODE"),(String)root.get("CODE")))
                .flatMap(d->Stream.concat(Stream.of(d), getChildNode(d,alllist)));
    }

}
