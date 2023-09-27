/**
 * EngineConstant.java	  V1.0   2022年11月17日 下午3:14:14
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ai.modules.engine.handle.rule.AbsRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveChronicIndicationRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveDosageRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveDrugDurationRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveFrequencyRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveIndicationRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveItemNomatchRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveLackItemsRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveMutexRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveMutexUnpayRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveOnedayMutexRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveOverFreqRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HivePayDurationRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HivePreconditionRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveTreatOverFreqRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveUnChargeRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveUnExpenseRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveUnindicationRuleHandle;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.handle.rule.solr.SolrChronicIndicationRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrDosageRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrDrugDurationRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrHospLvlAndTypeRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrIndicationRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrItemNomatchRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrLackItemsRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrMutexRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrOnedayMutexRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrOverAvgdayFreqRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrOverFreqRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrPayDurationRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrPreconditionRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrTreatOverFreqRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrXtdrqRuleHandle;
import com.ai.modules.engine.handle.secondary.AbsRuleSecondHandle;
import com.ai.modules.engine.handle.secondary.RuleDosageHandle;
import com.ai.modules.engine.handle.secondary.RuleItemNomatchHandle;
import com.ai.modules.engine.handle.secondary.RuleMutexUnpayHandle;
import com.ai.modules.engine.handle.secondary.RuleOnedayMutex2Handle;
import com.ai.modules.engine.handle.secondary.RuleOnedayRelyHandle;
import com.ai.modules.engine.handle.secondary.RuleOverFrequency;
import com.ai.modules.engine.handle.secondary.RuleOverFrequencyFromDetail;
import com.ai.modules.engine.handle.secondary.RuleUnindicationHandle;

@Component
public class EngineConstant {
	//规则引擎处理类映射
	public static final Map<String, EngineHandleMapping> HANDLE_MAPPING = new HashMap<String, EngineHandleMapping>();
	//规则引擎二次处理类映射
	public static final Map<String, Class<? extends AbsRuleSecondHandle>> SEC_HANDLE_MAPPING = new HashMap<String, Class<? extends AbsRuleSecondHandle>>();
	static {
		//收费合规-一次就诊限频次
		EngineHandleMapping mapping = EngineHandleMapping.hive("freq1", HiveFrequencyRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//日均限频次
		mapping = new EngineHandleMapping("freq3", SolrOverAvgdayFreqRuleHandle.class, HiveFrequencyRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//收费合规-一日限频次
		mapping = new EngineHandleMapping("CHARGE.freq2", SolrOverFreqRuleHandle.class, HiveOverFreqRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//合理诊疗-一日限频次
		mapping = new EngineHandleMapping("TREAT.freq2", SolrTreatOverFreqRuleHandle.class, HiveTreatOverFreqRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//收费合规-一日互斥规则
		mapping = new EngineHandleMapping("dayUnfitGroups1", SolrOnedayMutexRuleHandle.class, HiveOnedayMutexRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//合理诊疗-一日互斥规则
		mapping = new EngineHandleMapping("YRCFSF1", SolrOnedayMutexRuleHandle.class, HiveOnedayMutexRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//一次就诊互斥规则
		mapping = new EngineHandleMapping("unfitGroups1", SolrMutexRuleHandle.class, HiveMutexRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//必要前提条件规则
		mapping = new EngineHandleMapping("fitGroups1", SolrPreconditionRuleHandle.class, HivePreconditionRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//适应症
		mapping = new EngineHandleMapping(AbsRuleParser.RULE_LIMIT_INDICATION, SolrIndicationRuleHandle.class, HiveIndicationRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		mapping = new EngineHandleMapping(AbsRuleParser.RULE_LIMIT_INDICATION1, SolrIndicationRuleHandle.class, HiveIndicationRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//门慢适应症
		mapping = new EngineHandleMapping(AbsRuleParser.RULE_LIMIT_CHRONICINDICATION, SolrChronicIndicationRuleHandle.class, HiveChronicIndicationRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//禁忌症
		mapping = new EngineHandleMapping(AbsRuleParser.RULE_LIMIT_UNINDICATION, SolrIndicationRuleHandle.class, HiveUnindicationRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//医保限定卫生机构类别用药
		mapping = EngineHandleMapping.solr(AbsRuleParser.RULE_LIMIT_HOSPLEVELTYPE, SolrHospLvlAndTypeRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//医保限定用药时限
		mapping = new EngineHandleMapping(AbsRuleParser.RULE_LIMIT_DRUGDURATION, SolrDrugDurationRuleHandle.class, HiveDrugDurationRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//医保合用不予支付
		mapping = EngineHandleMapping.hive(AbsRuleParser.RULE_LIMIT_UNPAYDRUG, HiveMutexUnpayRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//医保限定用药量
		mapping = new EngineHandleMapping(AbsRuleParser.RULE_LIMIT_DOSAGE, SolrDosageRuleHandle.class, HiveDosageRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//医保药品超过最大持续使用时间
		mapping = new EngineHandleMapping(AbsRuleParser.RULE_LIMIT_PAYDURATION, SolrPayDurationRuleHandle.class, HivePayDurationRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//药品使用缺少必要药品或项目
		mapping = new EngineHandleMapping(AbsRuleParser.RULE_LIMIT_LACKITEMS, SolrLackItemsRuleHandle.class, HiveLackItemsRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//限特定人群
		mapping = EngineHandleMapping.solr(AbsRuleParser.RULE_LIMIT_XTDRQ, SolrXtdrqRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//不能报销
		mapping = EngineHandleMapping.hive(AbsRuleParser.RULE_LIMIT_UNEXPENSE, HiveUnExpenseRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		mapping = EngineHandleMapping.hive(AbsRuleParser.RULE_LIMIT_UNEXPENSE1, HiveUnExpenseRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//不能收费
		mapping = EngineHandleMapping.hive(AbsRuleParser.RULE_LIMIT_UNCHARGE, HiveUnChargeRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//项目与既往项目不符
		mapping = new EngineHandleMapping(AbsRuleParser.RULE_LIMIT_ITEMWRONG, SolrItemNomatchRuleHandle.class, HiveItemNomatchRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
		//疾病与既往项目不符
		mapping = new EngineHandleMapping(AbsRuleParser.RULE_LIMIT_DIAGWRONG, SolrItemNomatchRuleHandle.class, HiveItemNomatchRuleHandle.class);
		HANDLE_MAPPING.put(mapping.getRuleLimit(), mapping);
	}
	
	static {
		//以下是二次处理映射关系
		//限频次规则
		for(String key : AbsRuleHandle.FREQUENCY_ACTION_SET) {
			SEC_HANDLE_MAPPING.put(key, RuleOverFrequency.class);
		}
		//一日限频次规则
		SEC_HANDLE_MAPPING.put("freq2", RuleOverFrequencyFromDetail.class);
		//收费合规-一日互斥规则
		SEC_HANDLE_MAPPING.put("dayUnfitGroups1", RuleOnedayMutex2Handle.class);
		//合理诊疗-一日互斥规则
		SEC_HANDLE_MAPPING.put("YRCFSF1", RuleOnedayMutex2Handle.class);
		//必要前提条件规则-一日依赖项目组规则
		SEC_HANDLE_MAPPING.put("fitGroups1", RuleOnedayRelyHandle.class);
		//禁忌症规则
		SEC_HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_UNINDICATION, RuleUnindicationHandle.class);
		//项目与既往项目不符规则
		SEC_HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_ITEMWRONG, RuleItemNomatchHandle.class);
		//疾病与既往项目不符规则
		SEC_HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_DIAGWRONG, RuleItemNomatchHandle.class);
		//医保合用不予支付药品
		SEC_HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_UNPAYDRUG, RuleMutexUnpayHandle.class);
		//医保限定用药量
		SEC_HANDLE_MAPPING.put(AbsRuleParser.RULE_LIMIT_DOSAGE, RuleDosageHandle.class);
	}
	
	//病例主表与业务表关联关系
	public final static Map<String, EngineMapping> ENGINE_MAPPING = new HashMap<String, EngineMapping>();		
	static {
		EngineMapping mapping = new EngineMapping("DWB_DOCTOR", "DOCTORID", "DOCTORID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_CLIENT", "CLIENTID", "CLIENTID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_CHARGE_DETAIL", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_SETTLEMENT", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("STD_ORGANIZATION", "ORGID", "ORGID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_DEPARTMENT", "DEPTID", "DEPTID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_CHRONIC_PATIENT", "CLIENTID", "CLIENTID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_DIAG", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		mapping = new EngineMapping("DWB_ORDER", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//dwb_化验结果
		mapping = new EngineMapping("DWB_TEST_RESULT", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//dwb_化验主记录
		mapping = new EngineMapping("DWB_TEST", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//dwb_影像检查主记录
		mapping = new EngineMapping("DWB_PACS_INFO", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//dwb_影像检查报告
		mapping = new EngineMapping("DWB_PACS_REPORT", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//DWS层-直接关联dwb_master_info.visitid
		//病人一次就诊某项目统计
		mapping = new EngineMapping("DWS_PATIENT_1VISIT_ITEMSUM", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//病人+单次就诊+药品分类
		mapping = new EngineMapping("DWS_PATIENT_1VISIT_DRUGCLASSSUM", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//病人+单次就诊+诊疗项目分类
		mapping = new EngineMapping("DWS_PATIENT_1VISIT_TREATCLASSSUM", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//业务规则-相邻两次门急诊相隔天数
		mapping = new EngineMapping("DWS_MZAPART_DAYS", "MZ_ID_THIS", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//业务规则-相邻两次住院相隔天数
		mapping = new EngineMapping("DWS_ZYAPART_DAYS", "ZY_ID_THIS", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则-住院期间门诊就诊
		mapping = new EngineMapping("DWS_CLINIC_INHOSPITAL", "MZ_ID_INHOSPITAL", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则-同一次就诊化验执行日期晚于出院日期
		mapping = new EngineMapping("DWS_TESTDATE_LEAVEDATE", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则-同一次就诊检查执行日期晚于出院日期
		mapping = new EngineMapping("DWS_CHECKDATE_LEAVEDATE", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则-重叠住院
		mapping = new EngineMapping("DWS_INHOSPITAL_OVERLAP", "ZY_ID_OTHER", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则_his和医保数据源的比较结果
		mapping = new EngineMapping("DWS_TAG_COMPARE_HIS_YB", "YB_VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则_节假日期间住院
		mapping = new EngineMapping("DWS_INHOSPITAL_VACATION", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则-患者死亡后就诊
		mapping = new EngineMapping("DWS_CLIENTDEATH_VISIT_DETAIL", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
		//dws_业务规则-医生死亡后出诊
		mapping = new EngineMapping("DWS_DOCTORDEATH_PRACTICE_DETAIL", "VISITID", "VISITID");
		ENGINE_MAPPING.put(mapping.getFromIndex(), mapping);
	}
	
	//病例主表与DWS表关联关系
	public final static Map<String, EngineDwsMapping> ENGINE_DWS_MAPPING = new HashMap<String, EngineDwsMapping>();
	//病例主表与DWS通过中间表关联关系
	public final static Map<String, EngineMiddleDwsMapping> ENGINE_MIDDLE_DWS_MAPPING = new HashMap<String, EngineMiddleDwsMapping>();
	static {
		EngineDwsMapping dwsMapping = null;
		//病人门诊和住院总统计
		dwsMapping = new EngineDwsMapping("DWS_PATIENT_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(hosplevel,''),nvl(clientid,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//病人+医疗机构级别
		dwsMapping = new EngineDwsMapping("DWS_PATIENT_HOSLEVEL_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(hosplevel,''),nvl(clientid,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//病人门诊统计
		dwsMapping = new EngineDwsMapping("DWS_PATIENT_MZ_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(hosplevel,''),nvl(clientid,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//病人住院统计
		dwsMapping = new EngineDwsMapping("DWS_PATIENT_ZY_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(hosplevel,''),nvl(clientid,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//病人+医疗机构汇总表
		dwsMapping = new EngineDwsMapping("DWS_PATIENT_ORG_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(clientid,''),nvl(orgid,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//病人按就诊地区统计
		dwsMapping = new EngineDwsMapping("DWS_PATIENT_VISITAREA_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(clientid,''),nvl(org.addrcounty_code,''),nvl(org.addrcounty_name,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//病人统计概览统计
		dwsMapping = new EngineDwsMapping("DWS_PATIENT_ALL_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(clientid,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//病人+科室
		dwsMapping = new EngineDwsMapping("DWS_PATIENT_DEPT_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(clientid,''),nvl(orgid,''),nvl(deptid_src,''),nvl(deptname_src,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//病人+医生
		dwsMapping = new EngineDwsMapping("DWS_PATIENT_DOCTOR_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(orgid,''),nvl(deptid_src,''),nvl(deptname_src,''),nvl(doctorid,''),nvl(doctorname,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//科室统计
		dwsMapping = new EngineDwsMapping("DWS_DEPT_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(orgid,''),nvl(deptid_src,''),nvl(deptname_src,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//医疗机构级别
		dwsMapping = new EngineDwsMapping("DWS_ORG_HOSPLEVEL_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(org.hosplevel,''),nvl(org.hosplevel_name,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//医疗机构所有制形式（公立、私立）
		dwsMapping = new EngineDwsMapping("DWS_ORG_OWNTYPE_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(org.owntype,''),nvl(org.owntype_name,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//医疗机构概览统计
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		dwsMapping = new EngineDwsMapping("DWS_ORG_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(orgid,''))");
		//医疗机构概览统计
		dwsMapping = new EngineDwsMapping("DWS_DOCTOR_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''),nvl(orgid,''),nvl(doctorid,''),nvl(doctorname,''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		//整个地区的数据统计
		dwsMapping = new EngineDwsMapping("DWS_ALL_SUM", "ID", "concat_ws('_',etl_source,'${durationType}',nvl(${duration},''))");
		ENGINE_DWS_MAPPING.put(dwsMapping.getFromIndex(), dwsMapping);
		
		//以下是ENGINE_MIDDLE_DWS_MAPPING
		//病人收费明细统计
		EngineMiddleDwsMapping mapping = new EngineMiddleDwsMapping("DWS_PATIENT_CHARGEITEM_SUM", "concat_ws('_',nvl(etl_source,''),'${durationType}',nvl(${duration},''),nvl(clientid,''),nvl(itemname,''))");
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//病人+疾病组
		mapping = new EngineMiddleDwsMapping("DWS_PATIENT_DIAGGROUP_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//病人+疾病组+医疗机构
		mapping = new EngineMiddleDwsMapping("DWS_PATIENT_DIAGGROUP_ORG_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//病人+药品分类
		mapping = new EngineMiddleDwsMapping("DWS_PATIENT_DRUGCLASSSUM", "concat_ws('_',nvl(etl_source,''),'${durationType}',nvl(${duration},nvl(clientid,''),nvl(std.atc_code3,''),nvl(std.atc_name3,''))");
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//病人+诊疗项目分类
		mapping = new EngineMiddleDwsMapping("DWS_PATIENT_TREATCLASS_SUM", "concat_ws('_',nvl(etl_source,''),'${durationType}',nvl(${duration},nvl(clientid,''),nvl(std.treat_classCode2,''),nvl(std.treat_className2,''))");
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//病人+疾病组+药品分类
		mapping = new EngineMiddleDwsMapping("DWS_PATIENT_DIAGGROUP_DRUGCLASS", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//疾病组+地区
		mapping = new EngineMiddleDwsMapping("DWS_DIAGGROUP_AREA_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//疾病组+医生
		mapping = new EngineMiddleDwsMapping("DWS_DIAGGROUP_DOCTOR_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//疾病组+医疗机构
		mapping = new EngineMiddleDwsMapping("DWS_DIAGGROUP_ORG_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//疾病组+医疗机构所有制形式（公立、私立）
		mapping = new EngineMiddleDwsMapping("DWS_DIAGGROUP_OWNTYPE_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//疾病组
		mapping = new EngineMiddleDwsMapping("DWS_DIAGGROUP_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//疾病+药品
		mapping = new EngineMiddleDwsMapping("DWS_DISEASE_DRUGDIST", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//疾病（具体的某个ICD10疾病编码）
		mapping = new EngineMiddleDwsMapping("DWS_DISEASE_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//疾病+诊疗项目
		mapping = new EngineMiddleDwsMapping("DWS_DISEASE_TREATDIST", "concat_ws('_',nvl(etl_source,''),'${durationType}',nvl(dwb_diag.diseasecode,''),nvl(dwb_diag.diseasename,''),nvl(itemcode,''),nvl(itemname,''))");
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//科室+收费项目（包括药品和诊疗项目）
		mapping = new EngineMiddleDwsMapping("DWS_DEPT_CHARGEITEM_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//科室+疾病组
		mapping = new EngineMiddleDwsMapping("DWS_DIAGGROUP_DEPT_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//收费项目
		mapping = new EngineMiddleDwsMapping("DWS_ITEM_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//医疗机构级别+疾病+收费项目
		mapping = new EngineMiddleDwsMapping("DWS_HOSPLEVEL_DISEASE_ITEM_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//医疗机构级别+药品
		mapping = new EngineMiddleDwsMapping("DWS_HOSPLEVEL_DRUG_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//医疗机构经营性质(即营利性医院和非营利性医院之间各指标的比较)
		mapping = new EngineMiddleDwsMapping("DWS_ORG_BUSSTYPE_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//医疗机构+收费项目（包括药品和诊疗项目）
		mapping = new EngineMiddleDwsMapping("DWS_ORG_CHARGEITEM_SUM", "concat_ws('_',nvl(etl_source,''),'${durationType}',nvl(orgid,''),nvl(itemcode,''),nvl(itemname,''),nvl(itemclass_id,''),nvl(itemclass,''))");
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
		//医生+收费项目
		mapping = new EngineMiddleDwsMapping("DWS_DOCTOR_CHARGEITEM_SUM", null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);		
		//业务规则-医生相邻两次处方相隔时间
		mapping = new EngineMiddleDwsMapping(new EngineMapping("DWS_RXAPART_DAYS", "PRESCRIPTNO_THIS", "PRESCRIPTNO"), new EngineMapping("DWB_ORDER", "VISITID", "VISITID"), null);
		ENGINE_MIDDLE_DWS_MAPPING.put(mapping.getDws().getFromIndex(), mapping);
	}
	
	/**重跑批次回填字段*/
	public static Set<String> BACKFILL_FIELD = new LinkedHashSet<String>();
	static {
		BACKFILL_FIELD.add("FIR_REVIEW_STATUS");
    	BACKFILL_FIELD.add("FIR_REVIEW_REMARK");
    	BACKFILL_FIELD.add("FIR_REVIEW_USERID");
    	BACKFILL_FIELD.add("FIR_REVIEW_USERNAME");
    	BACKFILL_FIELD.add("FIR_REVIEW_TIME");
    	BACKFILL_FIELD.add("PUSH_STATUS");
    	BACKFILL_FIELD.add("PUSH_USERID");
    	BACKFILL_FIELD.add("PUSH_USERNAME");
    	BACKFILL_FIELD.add("SEC_PUSH_STATUS");
    	BACKFILL_FIELD.add("SEC_PUSH_USERID");
    	BACKFILL_FIELD.add("SEC_PUSH_USERNAME");
    	BACKFILL_FIELD.add("ISSUE_ID");
    	BACKFILL_FIELD.add("ISSUE_NAME");
    	BACKFILL_FIELD.add("XMKH_ID");
    	BACKFILL_FIELD.add("XMKH_NAME");
    	BACKFILL_FIELD.add("TASK_BATCH_NAME");
    	BACKFILL_FIELD.add("HANDLE_STATUS");
	}
}
