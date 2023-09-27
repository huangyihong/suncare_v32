package com.ai.modules.drg.handle.where;

import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleWhere;

import java.util.List;

/**
 * 天龄规则限制内容解析
 * @author : zhangly
 * @date : 2023/2/16 15:51
 */
public class DayAgeRuleWhereParser extends AbsRuleWhereParser {
    public DayAgeRuleWhereParser(DrgRuleLimites ruleLimites) {
        super(ruleLimites);
    }

    @Override
    protected List<RuleWhere> parseRuleWhere() {
        //天龄=就诊日期-出生日期
        String column = "extract(day from (admitdate::timestamp-birthday::timestamp))";
        RangeRuleWhereParser parser = new RangeRuleWhereParser(ruleLimites, column);
        return parser.parseRuleWhere();
    }
}
