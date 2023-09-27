package com.ai.modules.drg.handle.where;

import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleWhere;

import java.util.List;

/**
 * 出生体重规则限制内容解析
 * @author : zhangly
 * @date : 2023/2/16 15:51
 */
public class WeightRuleWhereParser extends AbsRuleWhereParser {
    public WeightRuleWhereParser(DrgRuleLimites ruleLimites) {
        super(ruleLimites);
    }

    @Override
    protected List<RuleWhere> parseRuleWhere() {
        RangeRuleWhereParser parser = new RangeRuleWhereParser(ruleLimites, "newborn_birth_weight::int");
        return parser.parseRuleWhere();
    }
}
