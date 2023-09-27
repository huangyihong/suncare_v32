package com.ai.modules.drg.handle.where;

import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleWhere;

import java.util.List;

/**
 * 住院天数规则限制内容解析
 * @author : zhangly
 * @date : 2023/2/16 15:51
 */
public class ZyDaysRuleWhereParser extends AbsRuleWhereParser {
    public ZyDaysRuleWhereParser(DrgRuleLimites ruleLimites) {
        super(ruleLimites);
    }

    @Override
    protected List<RuleWhere> parseRuleWhere() {
        RangeRuleWhereParser parser = new RangeRuleWhereParser(ruleLimites, "zy_days::int");
        return parser.parseRuleWhere();
    }
}
