package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2021/2/1 16
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalRuleConditionSetVO extends MedicalRuleConditionSet {
    private MedicalRuleConfig ruleConfig;
    private String itemCodes;
    private String itemNames;
    private String itemTypes;

}
