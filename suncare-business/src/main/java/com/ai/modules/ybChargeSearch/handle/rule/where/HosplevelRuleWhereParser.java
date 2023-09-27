package com.ai.modules.ybChargeSearch.handle.rule.where;

import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleLimitModel;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleWhere;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.superSearch.QueryRuleEnum;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 医院级别规则限制内容解析
 * @author : zhangly
 * @date : 2023/2/16 16:29
 */
public class HosplevelRuleWhereParser extends AbsRuleWhereParser {

    public static final Map<String, String> HOSPLEVEL_MAPPING = new HashMap<String, String>();
    static {
        HOSPLEVEL_MAPPING.put("1", "一级");
        HOSPLEVEL_MAPPING.put("2", "二级");
        HOSPLEVEL_MAPPING.put("3", "三级");
    }

    public HosplevelRuleWhereParser(RuleLimitModel ruleLimitModel, String dbType) {
        super(ruleLimitModel, dbType);
    }

    @Override
    protected List<RuleWhere> parseRuleWhere() {
        List<RuleWhere> ruleWhereList = new ArrayList<>();
        String text = ruleLimitModel.getLimitText();
        text = replaceCharacter(text);
        String regex = "(>|>=|<|<=|=)\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        String column = "hosplevel";
        if(matcher.matches()) {
            String operator = text.replaceAll("\\d+", "");
            String value = text.replaceAll("(>|>=|<|<=|=)", "");
            Set<String> values = new LinkedHashSet<String>();
            if(operator.contains("=")) {
                values.add(value);
                if(HOSPLEVEL_MAPPING.containsKey(value)) {
                    values.add(HOSPLEVEL_MAPPING.get(value));
                }
            }
            if(operator.contains(">")) {
                for(Map.Entry<String, String> entry : HOSPLEVEL_MAPPING.entrySet()) {
                    if(Integer.parseInt(entry.getKey()) > Integer.parseInt(value)) {
                        values.add(entry.getKey());
                        if(HOSPLEVEL_MAPPING.containsKey(entry.getKey())) {
                            values.add(HOSPLEVEL_MAPPING.get(entry.getKey()));
                        }
                    }
                }
            }
            if(operator.contains("<")) {
                for(Map.Entry<String, String> entry : HOSPLEVEL_MAPPING.entrySet()) {
                    if(Integer.parseInt(entry.getKey()) < Integer.parseInt(value)) {
                        values.add(entry.getKey());
                        if(HOSPLEVEL_MAPPING.containsKey(entry.getKey())) {
                            values.add(HOSPLEVEL_MAPPING.get(entry.getKey()));
                        }
                    }
                }
            }
            QueryRuleEnum queryRuleEnum = QueryRuleEnum.SQL_RULES;
            queryRuleEnum.setValue(DcConstants.RULE_OPERATOR_REGEXLIKE);
            RuleWhere ruleWhere = new RuleWhere(DcConstants.SRC_YB_MASTER_INFO, "hosplevel",
                    StringUtils.join(values, ","), queryRuleEnum);
            ruleWhereList.add(ruleWhere);
        }
        return ruleWhereList;
    }
}
