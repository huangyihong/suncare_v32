/**
 * IEngineRepairSolrService.java	  V1.0   2021年12月23日 下午3:04:48
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service;

import java.util.List;

public interface IEngineRepairSolrService {
	void repair(List<String> whereList) throws Exception;
	
	void repairByHive(List<String> whereList) throws Exception;
}
