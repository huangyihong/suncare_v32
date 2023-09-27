/**
 * AbsHiveRuleHandle.java	  V1.0   2020年11月5日 下午3:57:26
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 功能描述：规则使用hive计算引擎抽象类
 *
 * @author  zhangly
 * Date: 2020年11月12日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Slf4j
public abstract class AbsHiveRuleHandle extends AbsRuleHandle {
	//DWB_CHARGE_DETAIL与MEDICAL_UNREASONABLE_ACTION映射关系
	public static final Map<String, String> DWB_CHARGEDTL_FIELD_MAPPING = new LinkedHashMap<String, String>();
	//DWS_PATIENT_1VISIT_ITEMSUM与MEDICAL_UNREASONABLE_ACTION映射关系
	public static final Map<String, String> DWS_CHARGEDTL_FIELD_MAPPING = new LinkedHashMap<String, String>();
	public static final String WITH_TABLE = "tmp_dws_patient_1visit_itemsum";
	public static final String WITH_TABLE_MASTER = "tmp_dwb_master_info";
	public static final String WITH_TABLE_ONEDAY = "tmp_dws_patient_1visit_1day_itemsum";
	public static final String WITH_TABLE_HIS = "his_dws_patient_1visit_itemsum";
	public static final String WITH_TABLE_ACCESS = "table_access";
	public static final String WITH_TABLE_JUDGE = "table_judge";
	public static final String WITH_TABLE_RESULT = "table_result";
	static {
		DWB_CHARGEDTL_FIELD_MAPPING.put("CASE_ID", "ITEMCODE");
		DWB_CHARGEDTL_FIELD_MAPPING.put("CASE_NAME", "ITEMNAME");
		DWB_CHARGEDTL_FIELD_MAPPING.put("ITEMCODE", "ITEMCODE");
		DWB_CHARGEDTL_FIELD_MAPPING.put("ITEMNAME", "ITEMNAME");
		DWB_CHARGEDTL_FIELD_MAPPING.put("ACTION_MONEY", "FUND_COVER");
		DWB_CHARGEDTL_FIELD_MAPPING.put("MAX_ACTION_MONEY", "FUND_COVER");
		DWB_CHARGEDTL_FIELD_MAPPING.put("MIN_MONEY", "FEE");
		DWB_CHARGEDTL_FIELD_MAPPING.put("MAX_MONEY", "FEE");
		DWB_CHARGEDTL_FIELD_MAPPING.put("ITEM_ID", "ID");
		DWB_CHARGEDTL_FIELD_MAPPING.put("ITEM_QTY", "AMOUNT");
		DWB_CHARGEDTL_FIELD_MAPPING.put("CHARGEDATE", "CHARGEDATE");
		DWB_CHARGEDTL_FIELD_MAPPING.put("ITEMCODE_SRC", "ITEMCODE_SRC");
		DWB_CHARGEDTL_FIELD_MAPPING.put("ITEMNAME_SRC", "ITEMNAME_SRC");
		DWB_CHARGEDTL_FIELD_MAPPING.put("HIS_ITEMCODE", "HIS_ITEMCODE");
		DWB_CHARGEDTL_FIELD_MAPPING.put("HIS_ITEMNAME", "HIS_ITEMNAME");
		DWB_CHARGEDTL_FIELD_MAPPING.put("HIS_ITEMCODE_SRC", "HIS_ITEMCODE_SRC");
		DWB_CHARGEDTL_FIELD_MAPPING.put("HIS_ITEMNAME_SRC", "HIS_ITEMNAME_SRC");
		
		DWS_CHARGEDTL_FIELD_MAPPING.put("CASE_ID", "ITEMCODE");
		DWS_CHARGEDTL_FIELD_MAPPING.put("CASE_NAME", "ITEMNAME");
		DWS_CHARGEDTL_FIELD_MAPPING.put("ITEMCODE", "ITEMCODE");
		DWS_CHARGEDTL_FIELD_MAPPING.put("ITEMNAME", "ITEMNAME");
		DWS_CHARGEDTL_FIELD_MAPPING.put("ITEMNAME_SRC", "ITEMNAME_SRC");
		DWS_CHARGEDTL_FIELD_MAPPING.put("ACTION_MONEY", "FUND_COVER");
		DWS_CHARGEDTL_FIELD_MAPPING.put("MAX_ACTION_MONEY", "FUND_COVER");
		DWS_CHARGEDTL_FIELD_MAPPING.put("MIN_MONEY", "ITEM_AMT");
		DWS_CHARGEDTL_FIELD_MAPPING.put("MAX_MONEY", "ITEM_AMT");
		DWS_CHARGEDTL_FIELD_MAPPING.put("FUND_COVER", "FUND_COVER");
		DWS_CHARGEDTL_FIELD_MAPPING.put("ITEM_ID", "ID");
		DWS_CHARGEDTL_FIELD_MAPPING.put("ITEM_AMT", "ITEM_AMT");
		DWS_CHARGEDTL_FIELD_MAPPING.put("ITEM_QTY", "ITEM_QTY");
		DWS_CHARGEDTL_FIELD_MAPPING.put("ITEMPRICE_MAX", "ITEMPRICE_MAX");
		DWS_CHARGEDTL_FIELD_MAPPING.put("SELFPAY_PROP_MIN", "SELFPAY_PROP_MIN");
		DWS_CHARGEDTL_FIELD_MAPPING.put("CHARGECLASS_ID", "CHARGECLASS_ID");
		DWS_CHARGEDTL_FIELD_MAPPING.put("CHARGECLASS", "CHARGECLASS");		
	}
	//数据源(项目地)
	protected String datasource;
	//是否试算
	protected Boolean trail;

	public AbsHiveRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, String datasource) {
		super(task, batch);
		this.trail = trail;
		this.datasource = datasource;
	}
}
