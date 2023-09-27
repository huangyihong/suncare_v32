/**
 * AbsNodeRuleHandle.java	  V1.0   2020年4月9日 下午12:03:38
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;

/**
 * DWS层医院筛查
 * 功能描述：
 *
 * @author  zhangly
 * Date: 2020年5月14日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class DwsOrgNodeRuleHandle extends TrailDwsNodeRuleHandle {
	//医院与DWS表关联关系
	private final static Map<String, EngineMapping> ORG_DWS_MAPPING = new HashMap<String, EngineMapping>();
	static {
		//DWS层-关联dwb_doctor
		Set<String> dwsMappingSet = new HashSet<String>();
		//病人+医疗机构汇总表
		dwsMappingSet.add("DWS_PATIENT_ORG_SUM");
		//病人+科室
		dwsMappingSet.add("DWS_PATIENT_DEPT_SUM");
		//病人+医生
		dwsMappingSet.add("DWS_PATIENT_DOCTOR_SUM");
		//医疗机构级别
		dwsMappingSet.add("DWS_ORG_HOSPLEVEL_SUM");
		//医疗机构所有制形式（公立、私立）
		dwsMappingSet.add("DWS_ORG_OWNTYPE_SUM");
		//医疗机构概览统计
		dwsMappingSet.add("DWS_ORG_SUM");
		//医疗机构概览统计
		dwsMappingSet.add("DWS_DOCTOR_SUM");
		EngineMapping mapping = null;
		for(String key : dwsMappingSet) {
			mapping = new EngineMapping(key, "ORGID", "ORGID");
			ORG_DWS_MAPPING.put(mapping.getFromIndex(), mapping);
		}
	}
	
	public DwsOrgNodeRuleHandle(List<EngineNodeRuleGrp> grpWheres) {
		super(grpWheres);
	}
	
	public DwsOrgNodeRuleHandle withMaster(String master) {
		this.master = master;
		return this;
	}
	
	@Override
	public String where() {
		if(grpWheres==null || grpWheres.size()==0 || grpWheres.get(0).getRuleList().size()==0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		String tableName = grpWheres.get(0).getRuleList().get(0).getTableName();
		if(!master.equalsIgnoreCase(tableName)) {
			if((EngineUtil.STD_ORGANIZATION.equalsIgnoreCase(master) || EngineUtil.DWB_MASTER_INFO.equalsIgnoreCase(master))
					&& ORG_DWS_MAPPING.containsKey(tableName.toUpperCase())) {
				//关联std_organization或dwb_master_info
				EngineMapping mapping = ORG_DWS_MAPPING.get(tableName.toUpperCase());
				SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
				sb.append(plugin.parse());
			}
		}
		
		sb.append(mergeCommonWhere());
		return sb.toString();
	}
}
