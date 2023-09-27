package com.ai.modules.his.mapper;

import java.util.List;

import com.ai.modules.engine.model.EngineNode;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.his.entity.HisMedicalFormalFlow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 风控模型正式流程备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
public interface HisMedicalFormalFlowMapper extends BaseMapper<HisMedicalFormalFlow> {

    List<EngineNode> recursionMedicalFormalFlowByCaseid(@Param("caseId") String caseId, @Param("batchId") String batchId);

    List<EngineNode> queryHisMedicalFormalFlow(@Param("caseId") String caseId, @Param("batchId") String batchId);
}
