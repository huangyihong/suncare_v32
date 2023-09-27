package com.ai.modules.his.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @Description: 备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
public interface CopyMapper {
	int copyMedicalFormalCaseByCaseIds(@Param("suffix") String suffix, @Param("caseIds") String[] caseIds, @Param("createInfo") Map<String, Object> createInfo);
	int copyMedicalFormalFlowByCaseIds(@Param("suffix") String suffix, @Param("caseIds") String[] caseIds);
	int copyMedicalFormalFlowRuleByCaseIds(@Param("suffix") String suffix, @Param("caseIds") String[] caseIds);
	int copyMedicalFormalFlowRuleGradeByCaseIds(@Param("suffix") String suffix, @Param("caseIds") String[] caseIds );
	int copyMedicalFormalCaseItemRelaByCaseIds(@Param("suffix") String suffix, @Param("caseIds") String[] caseIds, @Param("createInfo") Map<String, Object> createInfo );

}
