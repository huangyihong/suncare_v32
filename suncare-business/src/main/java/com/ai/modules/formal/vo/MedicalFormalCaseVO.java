package com.ai.modules.formal.vo;

import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.entity.MedicalFormalFlowRule;
import com.ai.modules.formal.entity.MedicalFormalFlowRuleGrade;
import lombok.Data;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2019/11/29 14
 * @Description:
 */
@Data
public class MedicalFormalCaseVO extends MedicalFormalCase {
    private List<MedicalFormalFlowRule> rules;
    private List<MedicalFormalFlowRuleGrade> grades;
    private String actionGrpName;
    private String relaItemType;
    private List<String> relaItemIds;
    private List<String> relaItemNames;
}
