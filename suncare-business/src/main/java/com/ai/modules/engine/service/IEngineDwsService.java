/**
 * EngineDwsService.java	  V1.0   2020年5月16日 下午7:19:28
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service;

import java.util.List;

import org.apache.solr.common.SolrDocument;

import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.model.dto.EngineCaseFlowDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface IEngineDwsService {
	IPage<SolrDocument> trial(IPage<SolrDocument> page, EngineCaseFlowDTO dto) throws Exception;
	
	List<EngineNodeRuleGrp> parseNodeRule(String rules);
}
