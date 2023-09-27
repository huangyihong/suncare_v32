/**
 * BaseEngineParamRule.java	  V1.0   2019年12月31日 下午5:36:05
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule;

import java.util.HashMap;
import java.util.Map;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.medical.entity.MedicalRuleConfig;

import lombok.Data;

@Data
public abstract class AbsEngineParamRule {
	//项目主表与业务表关联关系
	public final static Map<String, EngineMapping> DWB_CHARGE_DETAIL_MAPPING = new HashMap<String, EngineMapping>();
	static {
		//就诊主表
		EngineMapping mapping = new EngineMapping(EngineUtil.DWB_MASTER_INFO, "VISITID", "VISITID");
		DWB_CHARGE_DETAIL_MAPPING.put(mapping.getFromIndex(), mapping);		
		//自关联
		mapping = new EngineMapping(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, "VISITID", "VISITID");
		DWB_CHARGE_DETAIL_MAPPING.put(mapping.getFromIndex(), mapping);
		//医疗机构
		mapping = new EngineMapping("STD_ORGANIZATION", "ORGID", "ORGID");
		DWB_CHARGE_DETAIL_MAPPING.put(mapping.getFromIndex(), mapping);
		//药品组
		mapping = new EngineMapping("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
		DWB_CHARGE_DETAIL_MAPPING.put(mapping.getFromIndex(), mapping);
		//项目组
		mapping = new EngineMapping("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
		DWB_CHARGE_DETAIL_MAPPING.put(mapping.getFromIndex(), mapping);
		//疾病组
		mapping = new EngineMapping("STD_DIAGGROUP", "DISEASECODE", "ITEMCODE");
		DWB_CHARGE_DETAIL_MAPPING.put(mapping.getFromIndex(), mapping);
	}
	//表名
	protected String tableName;
	//字段名
	protected String colName;
	//比较运算符
	protected String compareType;
	//比较值
	protected String compareValue;
	//是否取反，默认false
	protected boolean reverse = false;
	/**
	 * 判定依据，{true:以病人历史病例作为判定依据, false:以一次病例作为判定依据}，默认false
	 */
	protected boolean patient = false;
	
	public AbsEngineParamRule() {
		
	}
	
	public AbsEngineParamRule(String tableName, String colName, String compareType, String compareValue) {
		this.tableName = tableName;
		this.colName = colName;
		this.compareType = compareType;
		this.compareValue = compareValue;
	}
	
	public AbsEngineParamRule(String colName, String compareValue) {
		this(EngineUtil.DWB_CHARGE_DETAIL, colName, "=", compareValue);
	}
	
	public AbsEngineParamRule(String tableName, String colName, String compareValue) {
		this(tableName, colName, "=", compareValue);
	}		
	
	/**
	 * 
	 * 功能描述：是否需要关联查询
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年6月2日 下午5:36:18</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected boolean isJoin() {
		if(DWB_CHARGE_DETAIL_MAPPING.containsKey(tableName.toUpperCase())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * 功能描述：判断主体项目是否为项目组
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	protected boolean isProjectGrp(MedicalRuleConfig rule) {
		String itemType = rule.getItemTypes();
		if(MedicalConstant.ITEM_PROJECTGRP.equals(itemType)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * 功能描述：解析查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年12月31日 下午5:37:17</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public abstract String where();
}
