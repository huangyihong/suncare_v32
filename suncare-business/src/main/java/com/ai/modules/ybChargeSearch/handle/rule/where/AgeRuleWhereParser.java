package com.ai.modules.ybChargeSearch.handle.rule.where;

import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleLimitModel;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleWhere;
import org.jeecg.common.util.superSearch.QueryRuleEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 年龄规则限制内容解析
 * @author : zhangly
 * @date : 2023/2/16 13:43
 */
public class AgeRuleWhereParser extends AbsRuleWhereParser {

    public AgeRuleWhereParser(RuleLimitModel ruleLimitModel, String dpType) {
        super(ruleLimitModel, dpType);
    }

    @Override
    protected List<RuleWhere> parseRuleWhere() {
        List<RuleWhere> ruleWhereList = new ArrayList<>();
        String text = ruleLimitModel.getLimitText();
        text = replaceCharacter(text);
        String regex = "(>|>=|<|<=|=)\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        //计算年龄=就诊日期-出生日期
        String column = "extract(year from age(visitdate::timestamp, birthday::timestamp))";
        if(!DcConstants.DB_TYPE_GP.equals(dbType)) {
            //impala模式
            column = "months_between(visitdate, birthday)/12";
        }
        boolean match = false;
        if(!match && matcher.matches()) {
            match = true;
            String operator = text.replaceAll("\\d+", "");
            String value = text.replaceAll("(>|>=|<|<=|=)", "");
            RuleWhere ruleWhere = new RuleWhere(DcConstants.SRC_YB_MASTER_INFO, column,
                    value, QueryRuleEnum.getByValue(operator), DcConstants.TYPE_NUMBER);
            ruleWhereList.add(ruleWhere);
        }
        regex = "(\\(|\\[)\\d+,\\d+(\\)|\\])";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(text);
        if(!match && matcher.matches()) {
            String[] array = text.split(",");
            for(String exp : array) {
                exp = exp.trim();
                String operator = exp.replaceAll("\\d+", "");
                operator = operator.replace("(", ">");
                operator = operator.replace("[", ">=");
                operator = operator.replace(")", "<");
                operator = operator.replace("]", "<=");
                String value = exp.replaceAll("(\\(|\\[|\\]|\\))", "");
                RuleWhere ruleWhere = new RuleWhere(DcConstants.SRC_YB_MASTER_INFO, column,
                        value, QueryRuleEnum.getByValue(operator), DcConstants.TYPE_NUMBER);
                ruleWhereList.add(ruleWhere);
            }
        }
        regex = "(>|>=|<|<=|=)\\d+,(>|>=|<|<=|=)\\d+";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(text);
        if(!match && matcher.matches()) {
            String[] array = text.split(",");
            for(String exp : array) {
                String operator = exp.replaceAll("\\d+", "");
                String value = exp.replaceAll("(>|>=|<|<=|=)", "");
                RuleWhere ruleWhere = new RuleWhere(DcConstants.SRC_YB_MASTER_INFO, column,
                        value, QueryRuleEnum.getByValue(operator), DcConstants.TYPE_NUMBER);
                ruleWhereList.add(ruleWhere);
            }
        }
        return ruleWhereList;
    }
}
