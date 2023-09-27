/**
 * QueryTaskBatchBreakRuleMapper.java	  V1.0   2020年1月8日 下午8:08:58
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.mapper;

import com.ai.modules.his.entity.HisMedicalFormalCase;

import java.util.List;

public interface HisMapper {
	List<HisMedicalFormalCase> queryMedicalFormalFlowCaseByBusiid(String busiId);

}
