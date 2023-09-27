package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/12/14 18
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalRuleConfigVO extends MedicalRuleConfig {

    private List<MedicalRuleConditionSet> accessConditions;
    private List<MedicalRuleConditionSet> judgeConditions;
}
