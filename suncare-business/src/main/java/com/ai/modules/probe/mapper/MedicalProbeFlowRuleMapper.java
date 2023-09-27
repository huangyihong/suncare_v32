package com.ai.modules.probe.mapper;

import java.util.List;

import com.ai.modules.formal.entity.MedicalFormalFlowRule;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.probe.entity.MedicalProbeFlowRule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 流程图树
 * @Author: jeecg-boot
 * @Date:   2019-11-21
 * @Version: V1.0
 */
public interface MedicalProbeFlowRuleMapper extends BaseMapper<MedicalProbeFlowRule> {

    List<MedicalFormalFlowRule> listFlowRule(@Param(Constants.WRAPPER) Wrapper<MedicalFormalFlowRule> wrapper);
}
