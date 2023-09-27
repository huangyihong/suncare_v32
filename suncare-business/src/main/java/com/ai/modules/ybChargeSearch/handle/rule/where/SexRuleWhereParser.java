package com.ai.modules.ybChargeSearch.handle.rule.where;

import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleLimitModel;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleWhere;

import java.util.ArrayList;
import java.util.List;

/**
 * 性别规则限制内容解析
 * @author : zhangly
 * @date : 2023/2/16 16:29
 */
public class SexRuleWhereParser extends AbsRuleWhereParser {

    public SexRuleWhereParser(RuleLimitModel ruleLimitModel, String dbType) {
        super(ruleLimitModel, dbType);
    }

    @Override
    protected List<RuleWhere> parseRuleWhere() {
        List<RuleWhere> ruleWhereList = new ArrayList<>();
        String text = ruleLimitModel.getLimitText();
        text = replaceCharacter(text);
        RuleWhere ruleWhere = new RuleWhere(DcConstants.SRC_YB_MASTER_INFO, "sex", text);
        ruleWhereList.add(ruleWhere);
        return ruleWhereList;
    }
}
