/**
 * java	  V1.0   2020年12月18日 下午5:56:58
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.parse;

import java.util.HashSet;
import java.util.Set;

import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

public abstract class AbsRuleParser {
	//项目组条件
	public static final String RULE_CONDI_PROJGRP = "projectGroup";	 
	public static final String RULE_CONDI_ACCESS_PROJGRP = "accessProjectGroup";
	//药品组
	public static final String RULE_CONDI_DRUGGRP = "drugGroup";
	public static final String RULE_CONDI_ACCESS_DRUGGRP = "accessDrugGroup";	
	//依赖项目组
	public static final String RULE_CONDI_FITGROUPS = "fitGroups";
	//项目条件
	public static final String RULE_CONDI_PROJ = "project";
	//疾病组条件
	public static final String RULE_CONDI_DISEASEGRP = "diseaseGroup";
	public static final String RULE_CONDI_ACCESS_DISEASEGRP = "accessDiseaseGroup";
	//疾病条件
	public static final String RULE_CONDI_DISEASE = "disease";
	//频次条件
	public static final String RULE_CONDI_FREQUENCY = "frequency";
	//互斥项目组
	public static final String RULE_CONDI_UNFITGROUPS = "unfitGroups";
	//一日互斥项目组
	public static final String RULE_CONDI_UNFITGROUPSDAY = "dayUnfitGroups";
	//适应症
	public static final String RULE_CONDI_INDICATION = "indication";
	//禁忌症
	public static final String RULE_CONDI_UNINDICATION = "unIndication";
	//性别
	public static final String RULE_CONDI_SEX = "sex";
	public static final String RULE_CONDI_ACCESS_SEX = "accessSex";
	//年龄
	public static final String RULE_CONDI_AGE = "age";
	public static final String RULE_CONDI_ACCESS_AGE = "accessAge";
	//就诊类型
	public static final String RULE_CONDI_JZLX = "visittype";
	public static final String RULE_CONDI_ACCESS_JZLX = "accessVisittype";
	//医保类型
	public static final String RULE_CONDI_YBLX = "insurancetype";
	public static final String RULE_CONDI_ACCESS_YBLX = "accessInsurancetype";
	//医院级别
	public static final String RULE_CONDI_YYJB = "hosplevel";
	public static final String RULE_CONDI_ACCESS_YYJB = "accessHosplevel";
	//科室
	public static final String RULE_CONDI_OFFICE = "dept";
	public static final String RULE_CONDI_ACCESS_OFFICE = "accessDept";
	//医院
	public static final String RULE_CONDI_ORG = "org";
	//医院类别
	public static final String RULE_CONDI_ORGTYPE = "orgType";
	public static final String RULE_CONDI_ACCESS_ORGTYPE = "accessOrgType";
	//不能报销
	public static final String RULE_CONDI_UNEXPENSE = "unExpense";
	//不能收费
	public static final String RULE_CONDI_UNCHARGE = "unCharge";
	//历史项目组
	public static final String RULE_CONDI_HISGROUPS = "hisGroups";
	//二线用药
	public static final String RULE_CONDI_SECDRUG = "secDrug";
	//合用不予支付
	public static final String RULE_CONDI_UNPAYDRUG = "unpayDrug";
	//医院级别+医院类别
	public static final String RULE_CONDI_HOSPLEVELTYPE = "hosplevelType";
	//医保限定用药量
	public static final String RULE_CONDI_DOSAGE = "dosage";
	//医保药品超过最大持续使用时间
	public static final String RULE_CONDI_PAYDURATION = "payDuration";
	//给药途径
	public static final String RULE_CONDI_DRUGUSAGE = "drugUsage";
	//药品使用缺少必要药品或项目
	public static final String RULE_CONDI_ITEMORDRUGGROUP = "itemOrDrugGroup";
	//限特定人群
	public static final String RULE_CONDI_XTDRQ = "xtdrq";
	
	//规则类别
	//适应症
	public static final String RULE_LIMIT_INDICATION = "indication";
	public static final String RULE_LIMIT_INDICATION1 = "indication1";
	//门慢适应症
	public static final String RULE_LIMIT_CHRONICINDICATION = "chronicIndication";
	//禁忌症
	public static final String RULE_LIMIT_UNINDICATION = "unIndication1";
	//项目与既往项目不符
	public static final String RULE_LIMIT_ITEMWRONG = "itemWrong1";
	//疾病与既往项目不符
	public static final String RULE_LIMIT_DIAGWRONG = "diagWrong1";
	//医保限定卫生机构类别用药
	public static final String RULE_LIMIT_HOSPLEVELTYPE = "hosplevelType";
	//医保合用不予支付药品
	public static final String RULE_LIMIT_UNPAYDRUG = "unpayDrug";
	//医保限定用药时限
	public static final String RULE_LIMIT_DRUGDURATION = "drugDuration";
	//医保限定用药量
	public static final String RULE_LIMIT_DOSAGE = "dosage";
	//医保药品超过最大持续使用时间
	public static final String RULE_LIMIT_PAYDURATION = "payDuration";
	//药品使用缺少必要药品或项目
	public static final String RULE_LIMIT_LACKITEMS = "lackItems";
	//限特定人群
	public static final String RULE_LIMIT_XTDRQ = "XTDRQ";
	//不能报销
	public static final String RULE_LIMIT_UNEXPENSE = "unExpense";
	public static final String RULE_LIMIT_UNEXPENSE1 = "unExpense1";
	//不能收费
	public static final String RULE_LIMIT_UNCHARGE = "unCharge1";
	
	//dwb_master_info筛查字段
	public static final Set<String> RULE_MASTER_SET = new HashSet<String>();	
	static {
		RULE_MASTER_SET.add(RULE_CONDI_AGE);
		RULE_MASTER_SET.add(RULE_CONDI_ACCESS_AGE);
		RULE_MASTER_SET.add(RULE_CONDI_SEX);
		RULE_MASTER_SET.add(RULE_CONDI_ACCESS_SEX);
		RULE_MASTER_SET.add(RULE_CONDI_JZLX);
		RULE_MASTER_SET.add(RULE_CONDI_ACCESS_JZLX);
		RULE_MASTER_SET.add(RULE_CONDI_YBLX);
		RULE_MASTER_SET.add(RULE_CONDI_YYJB);
		RULE_MASTER_SET.add(RULE_CONDI_ACCESS_YYJB);
		RULE_MASTER_SET.add(RULE_CONDI_OFFICE);
		RULE_MASTER_SET.add(RULE_CONDI_ACCESS_OFFICE);
		RULE_MASTER_SET.add(RULE_CONDI_ORG);
		RULE_MASTER_SET.add(RULE_CONDI_HOSPLEVELTYPE);
	}
	
	protected MedicalRuleConfig rule;
	protected MedicalRuleConditionSet condition;
	
	public AbsRuleParser(MedicalRuleConfig rule, MedicalRuleConditionSet condition) {
		this.rule = rule;
		this.condition = condition;
	}
	
	public abstract String parseCondition();
}
