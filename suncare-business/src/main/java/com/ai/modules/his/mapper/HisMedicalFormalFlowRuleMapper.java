package com.ai.modules.his.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.FormalFlowRule;
import com.ai.modules.his.entity.HisMedicalFormalFlowRule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 模型流程节点规则备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
public interface HisMedicalFormalFlowRuleMapper extends BaseMapper<HisMedicalFormalFlowRule> {

    List<EngineNodeRule> queryEngineNodeRuleByCaseid(@Param("caseId") String caseId, @Param("batchId") String batchId);
    
    List<EngineNodeRule> queryEngineNodeRuleByTmpl(@Param("nodeId") String nodeId, @Param("nodeCode") String nodeCode);
    
    List<FormalFlowRule> queryMedicalFormalFlowRuleByCaseid(@Param("caseId") String caseId, @Param("batchId") String batchId);
    
    List<FormalFlowRule> queryMedicalFormalFlowRuleByTmpl(@Param("nodeId") String nodeId, @Param("nodeCode") String nodeCode);
}
