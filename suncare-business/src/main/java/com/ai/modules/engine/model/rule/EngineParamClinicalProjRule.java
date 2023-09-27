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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.config.vo.MedicalDictItemVO;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;

/**
 * 
 * 功能描述：临床路径诊疗项目的模型参数
 *
 * @author  zhangly
 * Date: 2019年12月31日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineParamClinicalProjRule extends AbsEngineParamRule {	
	private Map<String, List<MedicalDictItemVO>> dictGroupMap;
	
	public EngineParamClinicalProjRule(String colName, String compareValue) {
		super(colName, compareValue);
	}
	
	public EngineParamClinicalProjRule(String colName, String compareValue, Map<String, List<MedicalDictItemVO>> dictGroupMap) {
		super(colName, compareValue);
		this.dictGroupMap = dictGroupMap;
	}
	
	public EngineParamClinicalProjRule(String tableName, String colName, String compareValue, Map<String, List<MedicalDictItemVO>> dictGroupMap) {
		super(tableName, colName, compareValue);
		this.compareValue = compareValue;
		this.dictGroupMap = dictGroupMap;
	}

	@Override
	public String where() {
		if(dictGroupMap==null || StringUtils.isBlank(compareValue)) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		//模型参数存在分组
		String[] groups = StringUtils.split(compareValue, "|");
		for(int num=0, length=groups.length; num<length; num++) {
			String group = groups[num];
			if(num>0) {
				sb.append(" OR ");
			}
			String[] values = StringUtils.split(group, ",");
			int index = 0;
			for(String value : values) {
				String[] condi = StringUtils.split(value, "#");
				String logic = condi[0];
				String code = condi[1];
				List<MedicalDictItemVO> itemList = dictGroupMap.get(code);
				if(itemList==null || itemList.size()==0) {
					continue;
				}
				if(index>0) {
					sb.append(" AND ");
				}
				if("0".equals(logic)) {
					sb.append("-");
				}
				boolean join = false;
				if(DWB_CHARGE_DETAIL_MAPPING.containsKey(tableName.toUpperCase())) {
					join = true;
					sb.append("_query_:\"");
					EngineMapping mapping = DWB_CHARGE_DETAIL_MAPPING.get(tableName.toUpperCase());
					SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
					sb.append(plugin.parse());
				}
				
				if(itemList.size()==1) {
					sb.append(colName).append(":").append(itemList.get(0).getCode());				
				} else {
					sb.append(colName).append(":(");
					for(int i=0, len=itemList.size(); i<len; i++) {
						MedicalDictItemVO vo = itemList.get(i);
						if(i>0) {
							sb.append(" OR ");
						}
						sb.append(vo.getCode());
					}
					sb.append(")");
				}
				index++;
				if(join) {
					sb.append("\"");
				}
			}
		}
		if(reverse) {
			return "*:* -("+sb.toString()+")";
		} else {
			return sb.toString();
		}
	}		
}
