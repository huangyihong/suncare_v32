package com.ai.modules.probe.service;

import com.ai.modules.formal.entity.MedicalFormalFlowRule;
import com.ai.modules.probe.entity.MedicalProbeFlowRule;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 流程图树
 * @Author: jeecg-boot
 * @Date:   2019-11-21
 * @Version: V1.0
 */
public interface IMedicalProbeFlowRuleService extends IService<MedicalProbeFlowRule> {

    List<MedicalProbeFlowRule> queryByCaseId(String caseId);
    List<MedicalFormalFlowRule> listFlowRule(Wrapper<MedicalFormalFlowRule> wrapper);
}
