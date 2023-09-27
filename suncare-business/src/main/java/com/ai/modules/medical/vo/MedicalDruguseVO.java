package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.entity.MedicalDruguseRuleGroup;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/11/6 15
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalDruguseVO extends MedicalDruguse {
    private List<MedicalRuleConditionSet> ruleGroups;
}
