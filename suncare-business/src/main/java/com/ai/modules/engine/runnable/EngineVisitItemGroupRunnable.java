/**
 * EngineVisitItemGroup.java	  V1.0   2020年10月10日 下午3:33:58
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.runnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.util.SpringContextUtils;

import com.ai.modules.config.entity.MedicalDrugGroup;
import com.ai.modules.config.entity.MedicalProjectGroup;
import com.ai.modules.engine.service.IEngineClinicalService;
import com.ai.modules.engine.util.SolrUtil;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * 
 * 功能描述：临床路径必需药品、必需项目未做线程
 *
 * @author  zhangly
 * Date: 2020年10月12日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineVisitItemGroupRunnable extends AbsEngineRunnable {
	private String visitid;
	//临床路径必需药品
	private Set<String> drugGroups;
	private Map<String, MedicalDrugGroup> drugGroupMap;
	//临床路径必需项目
	private Set<String> treatGroups;
	private Map<String, MedicalProjectGroup> treatGroupMap;
	private String result;
	
	public EngineVisitItemGroupRunnable(String datasource, String visitid, 
			Set<String> drugGroups, Set<String> treatGroups,
			Map<String, MedicalDrugGroup> drugGroupMap, Map<String, MedicalProjectGroup> treatGroupMap) {
		super(datasource);
		this.visitid = visitid;
		this.drugGroups = drugGroups;
		this.treatGroups = treatGroups;
		this.drugGroupMap = drugGroupMap;
		this.treatGroupMap = treatGroupMap;
	}

	@Override
	public void execute() throws Exception {
		
	}

	public String getVisitid() {
		return visitid;
	}

	public String getResult() {
		return result;
	}
}
