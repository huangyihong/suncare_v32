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
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.config.service.IMedicalOtherDictService;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;

/**
 * 
 * 功能描述：检验结果参数
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineParamTreatResultRule extends AbsEngineParamRule {	
	private String itemType;
	private String itemCode;
	private String valueType;
	private String valueUnit;
	
	public EngineParamTreatResultRule(String compareValue, String itemType, String itemCode, 
			String valueType, String valueUnit) {
		this.compareValue = compareValue;
		this.itemCode = itemCode;
		this.itemType = itemType;
		this.valueType = valueType;
		this.valueUnit = valueUnit;
	}

	@Override
	public String where() {
		StringBuilder sb = new StringBuilder();
		sb.append("_query_:\"");
		EngineMapping mapping = new EngineMapping("DWB_TEST_RESULT", "VISITID", "VISITID");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		String itemCodes = "(" + StringUtils.replace(itemCode, "|", " OR ") + ")";
		if("ITEM".equals(itemType)) {
			//项目
			sb.append("ITEMCODE:").append(itemCodes);			
		} else {
			//项目组
			sb.append("_query_:\\\"");
			plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
			sb.append(plugin.parse());
			sb.append("TREATGROUP_CODE:").append(itemCodes);
			sb.append("\\\"");
		}				
		if("2".equals(valueType)) {
			//定性
			sb.append(" AND TIP:").append(compareValue);
		} else {
			//定量
			ApplicationContext context = SpringContextUtils.getApplicationContext();
			IMedicalOtherDictService dictSV = context.getBean(IMedicalOtherDictService.class);
			valueUnit = dictSV.getValueByCode("testvalueunit", valueUnit);
			sb.append(" AND TESTVALUEUNIT:").append(valueUnit);
			sb.append(" AND TEST_VALUE:");
			compareValue = compareValue.replace("(", "{");
			compareValue = compareValue.replace(")", "}");
			compareValue = compareValue.replace(",", " TO ");
			sb.append(compareValue);
		}
		sb.append("\"");
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}		
	
	public String ignoreWhere() {
		StringBuilder sb = new StringBuilder();
		sb.append("_query_:\"");				
		EngineMapping mapping = new EngineMapping("DWB_TEST_RESULT", "VISITID", "VISITID");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		sb.append("VISITID:*");
		if(!"2".equals(valueType)) {
			//定量
			sb.append(" AND TESTVALUEUNIT:").append(valueUnit);
		}
		sb.append("\"");
		return sb.toString();
	}
}
