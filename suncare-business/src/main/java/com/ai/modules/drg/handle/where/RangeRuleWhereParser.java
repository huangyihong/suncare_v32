package com.ai.modules.drg.handle.where;

import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleWhere;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.superSearch.QueryRuleEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 数字范围规则限制内容解析
 * @author : zhangly
 * @date : 2023/2/16 13:43
 */
public class RangeRuleWhereParser extends AbsRuleWhereParser {

    private String column;

    public RangeRuleWhereParser(DrgRuleLimites ruleLimites, String column) {
        super(ruleLimites);
        this.column = column;
    }

    @Override
    protected List<RuleWhere> parseRuleWhere() {
        List<RuleWhere> ruleWhereList = new ArrayList<>();
        String operator = ruleLimites.getCompareType();
        String value = ruleLimites.getCompareValue();
        if(StringUtils.isNotBlank(operator) && StringUtils.isNotBlank(value)) {
            RuleWhere ruleWhere = new RuleWhere(DcConstants.SRC_HIS_ZY_MASTER_INFO, column,
                    value, QueryRuleEnum.getByValue(operator), DcConstants.TYPE_NUMBER);
            ruleWhereList.add(ruleWhere);
        }
        operator = ruleLimites.getCompareType2();
        value = ruleLimites.getCompareValue2();
        if(StringUtils.isNotBlank(operator) && StringUtils.isNotBlank(value)) {
            RuleWhere ruleWhere = new RuleWhere(DcConstants.SRC_HIS_ZY_MASTER_INFO, column,
                    value, QueryRuleEnum.getByValue(operator), DcConstants.TYPE_NUMBER);
            ruleWhereList.add(ruleWhere);
        }
        return ruleWhereList;
    }
}
