package com.ai.modules.ybChargeSearch.service.impl;

import com.ai.modules.api.util.ApiOauthClientUtil;
import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.entity.YbChargeDrugRule;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.handle.rule.DcRuleHandleFactory;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleScriptResult;
import com.ai.modules.ybChargeSearch.handle.rule.script.AbsRuleScriptHandler;
import com.ai.modules.ybChargeSearch.vo.YbChargeQueryDatabase;
import com.ai.modules.ybChargeSearch.vo.YbChargeQuerySql;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : zhangly
 * @date : 2023/2/20 14:38
 */
public class GenRuleScriptHandler {

    public List<YbChargeQuerySql> generateRuleScript(YbChargeSearchTask task, YbChargeQueryDatabase database) throws Exception {

        StringBuilder errorSb = new StringBuilder();
        List<YbChargeQuerySql> result = new ArrayList<YbChargeQuerySql>();
        List<YbChargeDrugRule> ruleList = JSONArray.parseArray(task.getJsonStr(), YbChargeDrugRule.class);
        if(ruleList==null || ruleList.size()==0) {
            throw new Exception("药品不能为空");
        }
        for(YbChargeDrugRule rule : ruleList) {
            String limitTypeDesc = ApiOauthClientUtil.parseText("DC_DRUG_LIMIT_TYPE", rule.getLimitType());
            //String limitTypeDesc = rule.getLimitType();
            DcRuleHandleFactory factory = new DcRuleHandleFactory(rule.getLimitType(), rule.getLimitContent());
            AbsRuleScriptHandler handler = factory.bulidRuleScriptHandler(rule, task, database);
            RuleScriptResult scriptResult = handler.parseRuleScript();
            if(!scriptResult.isSuccess()) {
                errorSb.append("药品名称（").append(rule.getDrugName()).append("）参数验证失败。失败原因：")
                        .append(scriptResult.getMessage()).append("\n");
            } else {
                String sheetName = rule.getDrugName()+"("+limitTypeDesc+")";
                YbChargeQuerySql bean = new YbChargeQuerySql();
                bean.setQuerySql(scriptResult.getScript());
                bean.setSheetName(rule.getDrugName());
                result.add(bean);
            }
        }
        if(errorSb.length()>0) {
            throw new Exception(errorSb.toString());
        }
        return result;
    }
}
