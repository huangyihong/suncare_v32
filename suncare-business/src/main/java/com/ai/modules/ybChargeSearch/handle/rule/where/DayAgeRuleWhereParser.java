package com.ai.modules.ybChargeSearch.handle.rule.where;

import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleLimitModel;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleWhere;

import java.util.List;

/**
 * 天龄规则限制内容解析
 * @author : zhangly
 * @date : 2023/2/16 15:51
 */
public class DayAgeRuleWhereParser extends AgeRuleWhereParser {
    public DayAgeRuleWhereParser(RuleLimitModel ruleLimitModel, String dpType) {
        super(ruleLimitModel, dpType);
    }

    @Override
    protected List<RuleWhere> parseRuleWhere() {
        //替换查询字段，天龄=就诊日期-出生日期
        String column = "extract(day from (visitdate::timestamp-birthday::timestamp))";
        if(!DcConstants.DB_TYPE_GP.equals(dbType)) {
            //impala模式
            column = "datediff(visitdate,birthday)";
        }
        List<RuleWhere> ruleWhereList = super.parseRuleWhere();
        for(RuleWhere ruleWhere : ruleWhereList) {
            ruleWhere.setColumn(column);
        }
        return ruleWhereList;
    }
}
