package com.ai.modules.his.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * @Description: 备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
public interface BackMapper {
	int backMedicalFormalCaseByBusiIds(@Param("batchId") String batchId, @Param("busiIds") String[] busiIds);
	int backMedicalFormalFlowByBusiIds(@Param("batchId") String batchId, @Param("busiIds") String[] busiIds);
	int backMedicalFormalFlowRuleByBusiIds(@Param("batchId") String batchId, @Param("busiIds") String[] busiIds);
	int backMedicalFormalFlowRuleGradeByBusiIds(@Param("batchId") String batchId, @Param("busiIds") String[] busiIds);
	int backMedicalFormalFlowBusiByBusiIds(@Param("batchId") String batchId, @Param("busiIds") String[] busiIds);
	int backMedicalFormalFlowCaseBusiByBusiIds(@Param("batchId") String batchId, @Param("busiIds") String[] busiIds);


	// 备份模型，在模型归纳更新的时候用到
/*	int backMedicalFormalCaseByVersion(@Param("caseId") String caseId, @Param("version") Float version);
	int backMedicalFormalFlowByVersion(@Param("caseId") String caseId, @Param("version") Float version);
	int backMedicalFormalFlowRuleByVersion(@Param("caseId") String caseId, @Param("version") Float version);
	int backMedicalFormalFlowRuleGradeByVersion(@Param("caseId") String caseId, @Param("version") Float version);*/


	/**
	 *
	 * 功能描述：备份模型
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年3月4日 下午3:15:57</p>
	 *
	 * @param batchId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	int backMedicalFormalCase(String batchId);

	int backMedicalFormalFlow(String batchId);

	int backMedicalFormalFlowRule(String batchId);

	int backMedicalFormalFlowRuleGrade(String batchId);

	int backMedicalFormalFlowBusi(String batchId);

	int backMedicalFormalFlowCaseBusi(String batchId);

	int backTaskBatchBreakRule(String batchId);

	int backMedicalFormalCaseByCaseid(@Param("batchId") String batchId, @Param("caseId") String caseId);

	int backMedicalFormalFlowByCaseid(@Param("batchId") String batchId, @Param("caseId") String caseId);

	int backMedicalFormalFlowRuleByCaseid(@Param("batchId") String batchId, @Param("caseId") String caseId);

	int backMedicalFormalFlowRuleGradeByCaseid(@Param("batchId") String batchId, @Param("caseId") String caseId);
}
