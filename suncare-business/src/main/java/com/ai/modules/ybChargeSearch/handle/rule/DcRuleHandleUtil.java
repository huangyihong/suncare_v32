/**
 * DcRuleHandleUtil.java	  V1.0   2023年2月16日 下午12:07:02
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.ybChargeSearch.handle.rule;

import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.handle.rule.model.*;
import com.ai.modules.ybChargeSearch.handle.rule.regex.AbsRuleRegexParser;
import com.ai.modules.ybChargeSearch.handle.rule.script.RuleScriptHandler;
import com.ai.modules.ybChargeSearch.handle.rule.where.AbsRuleWhereParser;
import com.ai.modules.ybChargeSearch.handle.rule.where.DayAgeRuleWhereParser;
import com.ai.modules.ybChargeSearch.handle.rule.where.RegexRuleWhereParser;
import com.ai.modules.ybChargeSearch.service.impl.GenHiveQueryCommon;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.*;

public class DcRuleHandleUtil {

    /**
     *
     * 功能描述：验证规则限制内容是否满足要求
     * @author zhangly
     * @date 2023-02-16 12:20:54
     *
     * @param limitType
     * @param limitText
     *
     * @return com.ai.modules.ybChargeSearch.handle.rule.model.RuleRegexResult
     *
     */
    public static RuleRegexResult validRuleRegex(String limitType, String limitText) {
        DcRuleHandleFactory factory = new DcRuleHandleFactory(limitType, limitText);
        AbsRuleRegexParser parser = factory.buildRuleRegexParser();
        return parser.validate();
    }

    /**
     *
     * 功能描述：替换regex like查询条件值中包含的特殊字符
     * @author zhangly
     * @date 2023-02-17 16:07:18
     *
     * @param value
     * @param dbType
     *
     * @return java.lang.String
     *
     */
    public static String replaceRegexLikeValue(String value, String dbType) {
        //去空格
        value = value.trim();
        value = StringUtils.replace(value,"#" ,"|");
        //特殊字符转义
        Set<String> set = new HashSet<String>();
        set.add("(");
        set.add(")");
        set.add("[");
        set.add("]");
        set.add("*");
        set.add("+");
        set.add(".");
        set.add("{");
        set.add("}");
        for(String searchString : set) {
            value = StringUtils.replace(value, searchString,"\\"+searchString);
        }
        if(!DcConstants.DB_TYPE_GP.equals(dbType)) {
            //impala模式， 特殊字符需要使用\\进行查询
            value = StringUtils.replace(value, "\\", "\\\\");
        }
        //&& 表示前台查询通配符，查询的时候需要替换成.*
        value = StringUtils.replace(value, "&&" ,".*");
        return value;
    }

    /**
     *
     * 功能描述：替换regex like查询条件值，进行全匹配查询
     * @author zhangly
     * @date 2023-02-17 16:28:17
     *
     * @param value
     *
     * @return java.lang.String
     *
     */
    public static String replaceRegexLikeValueForFull(String value) {
        value = StringUtils.replace(value,"#" ,"|");
        value = StringUtils.replace(value,"|" ,"$|^");
        value= "^" + value + "$";
        return  value;
    }

    public static Map<String, String> parseRuleXml(InputStream is) throws Exception {
        Map<String, String> result = new HashMap<String, String>();
        // 创建SAXReader的对象reader
        SAXReader reader = new SAXReader();
        // 通过reader对象的read方法加载xml文件，获取docuemnt对象
        Document document = reader.read(is);
        // 通过document对象获取根节点fields
        Element root = document.getRootElement();
        // 通过element对象的elementIterator方法获取迭代器
        Iterator<?> it = root.elementIterator();
        // 遍历迭代器，获取根节点中的字段
        while (it.hasNext()) {
            Element element = (Element) it.next();
            String id = element.attributeValue("id");
            String sql = element.getText();
            result.put(id, sql);
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        RuleLimitModel ruleLimitModel = new RuleLimitModel("dayAge", "<=28");
        AbsRuleWhereParser parser = new DayAgeRuleWhereParser(ruleLimitModel, DcConstants.DB_TYPE_GP);
        List<String> wheres = parser.parse();
        System.out.println(StringUtils.join(wheres, " and "));
        parser = new DayAgeRuleWhereParser(ruleLimitModel, "impala");
        wheres = parser.parse();
        System.out.println(StringUtils.join(wheres, " and "));

        ruleLimitModel = new RuleLimitModel("insuranccetype", "工伤#生育");
        parser = new RegexRuleWhereParser(ruleLimitModel, "greenplum", "insurancetype");
        wheres = parser.parse();
        System.out.println(StringUtils.join(wheres, " and "));

        ruleLimitModel = new RuleLimitModel("visittype", "普通门诊，\\(急门诊)\\");
        parser = new RegexRuleWhereParser(ruleLimitModel, "greenplum", "visittype");
        wheres = parser.parse();
        System.out.println(StringUtils.join(wheres, " and "));

        System.out.println("\\\\\\(");

        RuleLimitModel limitModel = new RuleLimitModel("visittype", "住院");
        RuleTaskModel ruleTaskModel = new RuleTaskModel();
        ruleTaskModel.setDbType("greenplum");
        ruleTaskModel.setRuleLimitModel(limitModel);
        ruleTaskModel.setRuleName("银杏达莫");
        ruleTaskModel.setOrgname("西安医学院附属汉江医院");
        ruleTaskModel.setStartDate("2021-01-01");
        ruleTaskModel.setEndDate("2022-12-31");

        RuleScriptHandler handler = new RuleScriptHandler(ruleTaskModel);
        RuleScriptResult scriptResult = handler.parseRuleScript();
        String sql = scriptResult.getScript();

        System.out.println("sql:"+sql);
    }
}
