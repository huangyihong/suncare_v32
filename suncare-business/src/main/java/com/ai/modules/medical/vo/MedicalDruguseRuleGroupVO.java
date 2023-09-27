package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalDruguseRuleGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther: zhangpeng
 * @Date: 2020/11/9 17
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalDruguseRuleGroupVO extends MedicalDruguseRuleGroup {
    private String itemCodes;
    private String itemNames;
    private String itemTypes;
    private String ruleCode;
}
