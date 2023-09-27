package com.ai.modules.ybChargeSearch.handle.rule.where;

import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleLimitModel;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleWhere;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.superSearch.QueryRuleEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 正则匹配规则限制内容解析
 * @author : zhangly
 * @date : 2023/2/16 16:29
 */
public class RegexRuleWhereParser extends AbsRuleWhereParser {

    protected String column;

    public RegexRuleWhereParser(RuleLimitModel ruleLimitModel, String dbType, String column) {
        super(ruleLimitModel, dbType);
        this.column = column;
    }

    @Override
    protected List<RuleWhere> parseRuleWhere() {
        List<RuleWhere> ruleWhereList = new ArrayList<>();
        String text = ruleLimitModel.getLimitText();
        text = replaceCharacter(text);
        text = StringUtils.replace(text, " ", "");
        text = RegExUtils.replacePattern(text, "(，|#)", ",");
        QueryRuleEnum queryRuleEnum = QueryRuleEnum.SQL_RULES;
        queryRuleEnum.setValue(DcConstants.RULE_OPERATOR_REGEXLIKE);
        RuleWhere ruleWhere = new RuleWhere(DcConstants.SRC_YB_MASTER_INFO, column,
                text, queryRuleEnum);
        ruleWhereList.add(ruleWhere);
        return ruleWhereList;
    }
}
