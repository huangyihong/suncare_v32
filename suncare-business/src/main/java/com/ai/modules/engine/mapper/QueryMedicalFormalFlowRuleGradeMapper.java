/**
 * QueryMedicalFormalFlowRuleGradeMapper.java	  V1.0   2020年5月9日 下午4:23:57
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ai.modules.engine.model.EngineRuleGrade;

public interface QueryMedicalFormalFlowRuleGradeMapper {
	List<EngineRuleGrade> queryEngineRuleGrade(@Param("batchId") String batchId, @Param("caseId") String caseId);
}
