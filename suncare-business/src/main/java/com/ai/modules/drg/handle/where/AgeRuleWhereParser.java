package com.ai.modules.drg.handle.where;

import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleWhere;

import java.util.List;

/**
 * 年龄规则限制内容解析
 * @author : zhangly
 * @date : 2023/2/16 13:43
 */
public class AgeRuleWhereParser extends AbsRuleWhereParser {

    public AgeRuleWhereParser(DrgRuleLimites ruleLimites) {
        super(ruleLimites);
    }

    @Override
    protected List<RuleWhere> parseRuleWhere() {
        //计算年龄=就诊日期-出生日期
        String column = "extract(year from age(admitdate::timestamp, birthday::timestamp))";
        RangeRuleWhereParser parser = new RangeRuleWhereParser(ruleLimites, column);
        return parser.parseRuleWhere();
    }
}
