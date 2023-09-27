package com.ai.modules.probe.service.impl;

import com.ai.modules.formal.entity.MedicalFormalFlowRule;
import com.ai.modules.probe.entity.MedicalProbeFlowRule;
import com.ai.modules.probe.mapper.MedicalProbeFlowRuleMapper;
import com.ai.modules.probe.service.IMedicalProbeFlowRuleService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 流程图树
 * @Author: jeecg-boot
 * @Date:   2019-11-21
 * @Version: V1.0
 */
@Service
public class MedicalProbeFlowRuleServiceImpl extends ServiceImpl<MedicalProbeFlowRuleMapper, MedicalProbeFlowRule> implements IMedicalProbeFlowRuleService {
    @Override
    public List<MedicalProbeFlowRule> queryByCaseId(String caseId) {
        return this.baseMapper.selectList(new QueryWrapper<MedicalProbeFlowRule>().eq("CASE_ID",caseId));
    }

    @Override
    public List<MedicalFormalFlowRule> listFlowRule(Wrapper<MedicalFormalFlowRule> wrapper) {
        return this.baseMapper.listFlowRule(wrapper);
    }
}
