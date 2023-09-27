/**
 * EngineParamRule.java	  V1.0   2019年12月31日 下午5:23:44
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.rule;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;

/**
 * 
 * 功能描述：频次规则
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineRuleFrequency extends AbsEngineParamRule {
	private MedicalRuleConfig rule;
	private MedicalRuleConditionSet condition;
	public EngineRuleFrequency(MedicalRuleConfig rule, MedicalRuleConditionSet condition) {
		this.rule = rule;
		this.condition = condition;
	}

	@Override
	public String where() {
		StringBuilder sb = new StringBuilder();	
		sb.append("VISITID:*");
		String period = condition.getExt1();
		String compare = condition.getCompare();
		String frequency = condition.getExt2();
		boolean need = false;
		if(StringUtils.isNotBlank(period) 
				&& StringUtils.isNotBlank(compare) 
				&& StringUtils.isNotBlank(frequency)) {
			//频次			
			if("1time".equals(period)) {
				//一次就诊
				sb.append(" AND ITEM_QTY:");
			} else if("avgday".equals(period)) {
				//日均次
				sb.append(" AND ITEM_DAYAVG_QTY:");							    
			} else {
				need = true;
				sb.append(" AND _query_:\"");
				String fromIndex = "MAPPER_DWS_PATIENT_CHARGEITEM_SUM_"+EngineUtil.getDwsPeriod(period);
				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(fromIndex, "VISITID", "VISITID");
				sb.append(plugin.parse());
				plugin = new SolrJoinParserPlugin("DWS_PATIENT_CHARGEITEM_SUM", "id", "DWSID");
				sb.append(plugin.parse());
				//sb.append("DTTYPE:").append(period);
				String itemCode = rule.getItemCodes();
				if(this.isProjectGrp(rule)) {
					//项目组
					plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
					sb.append(plugin.parse() + "TREATGROUP_CODE:" + itemCode);
				} else {
					sb.append("ITEMCODE:"+itemCode);
				}
				sb.append(" AND ITEM_QTY:");				
			}
			if(">".equals(compare)) {
				sb.append("{").append(frequency).append(" TO *}");
			} else if(">=".equals(compare)) {
				sb.append("[").append(frequency).append(" TO *}");
			} else if("<".equals(compare)) {
				sb.append("{* TO ").append(frequency).append("}");
			} else if("<=".equals(compare)) {
				sb.append("{* TO ").append(frequency).append("]");
			} else {
				sb.append(frequency);
			}
			if(need) {
				sb.append("\"");
			}
		}
				
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}		
	}		
}
