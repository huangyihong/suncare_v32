package com.ai.modules.his.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * @Description: 备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
public interface CopyHisMapper {
	int copyHisMedicalFormalCaseByBusiIds(@Param("batchId") String batchId, @Param("hisBatchId") String hisBatchId,@Param("busiIds") String[] busiIds);
	int copyHisMedicalFormalFlowByBusiIds(@Param("batchId") String batchId, @Param("hisBatchId") String hisBatchId,@Param("busiIds") String[] busiIds);
	int copyHisMedicalFormalFlowRuleByBusiIds(@Param("batchId") String batchId, @Param("hisBatchId") String hisBatchId,@Param("busiIds") String[] busiIds);
	int copyHisMedicalFormalFlowRuleGradeByBusiIds(@Param("batchId") String batchId, @Param("hisBatchId") String hisBatchId,@Param("busiIds") String[] busiIds);
	int copyHisMedicalFormalFlowBusiByBusiIds(@Param("batchId") String batchId, @Param("hisBatchId") String hisBatchId,@Param("busiIds") String[] busiIds);
	int copyHisMedicalFormalFlowCaseBusiByBusiIds(@Param("batchId") String batchId, @Param("hisBatchId") String hisBatchId,@Param("busiIds") String[] busiIds);

}
