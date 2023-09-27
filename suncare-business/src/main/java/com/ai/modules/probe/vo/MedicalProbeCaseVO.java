package com.ai.modules.probe.vo;

import com.ai.modules.probe.entity.MedicalProbeCase;
import com.ai.modules.probe.entity.MedicalProbeFlowRule;
import lombok.Data;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2019/11/22 17
 * @Description:
 */
@Data
public class MedicalProbeCaseVO extends MedicalProbeCase {
    private List<MedicalProbeFlowRule> rules;
}
