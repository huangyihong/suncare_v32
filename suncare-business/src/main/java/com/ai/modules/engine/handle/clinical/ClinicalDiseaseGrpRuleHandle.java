/**
 * EngineNodeRuleHandler.java	  V1.0   2020年4月9日 上午11:23:06
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.clinical;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;

/**
 * 
 * 功能描述：疾病组
 *
 * @author  zhangly
 * Date: 2020年4月23日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class ClinicalDiseaseGrpRuleHandle extends AbsClinicalRuleHandle {

	public ClinicalDiseaseGrpRuleHandle(String tableName, String colName, String compareValue) {
		super(tableName, colName, compareValue);
	}
	
	protected String handler() {
		StringBuilder sb = new StringBuilder();
		/*//查找疾病组中的疾病
		String where = "('"+compareValue.replaceAll(",", "','")+"')";
		QueryWrapper<MedicalDiseaseGroupItem> queryWrapper = new QueryWrapper<MedicalDiseaseGroupItem>();
		queryWrapper.inSql("GROUP_ID", "SELECT GROUP_ID FROM MEDICAL_DISEASE_GROUP WHERE GROUP_CODE in"+where);
		IMedicalDiseaseGroupItemService service = SpringContextUtils.getApplicationContext().getBean(IMedicalDiseaseGroupItemService.class);
		List<MedicalDiseaseGroupItem> dataList = service.list(queryWrapper);
		if(dataList==null || dataList.size()==0) {
			sb.append(colName).append(":*");
		} else {			
			sb.append(colName).append(":");
			sb.append("(");
			for(int i=0, len=dataList.size(); i<len; i++) {
				MedicalDiseaseGroupItem item = dataList.get(i);
				if(i>0) {
					sb.append(" OR ");
				}
				sb.append(item.getCode());
			}
			sb.append(")");
		}*/
		String where = "("+compareValue.replaceAll(",", " OR ")+")";
		EngineMapping mapping = new EngineMapping("STD_DIAGGROUP", "DISEASECODE", "DISEASECODE");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		sb.append(plugin.parse());
		sb.append("DIAGGROUP_CODE").append(":").append(where);
		return sb.toString();
	}
}
