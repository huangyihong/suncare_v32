package com.ai.modules.ybChargeSearch.handle.rule.script;

import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.handle.rule.DcRuleHandleUtil;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleLimitModel;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleRegexResult;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleScriptResult;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleTaskModel;
import com.ai.modules.ybChargeSearch.handle.rule.where.*;
import com.ai.modules.ybChargeSearch.service.impl.GenHiveQuerySqlTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : zhangly
 * @date : 2023/2/17 14:20
 */
@Slf4j
public class RuleScriptHandler extends AbsRuleScriptHandler {
    public RuleScriptHandler(RuleTaskModel ruleTaskModel) {
        super(ruleTaskModel);
    }

    @Override
    public RuleScriptResult parseRuleScript() {
        try {
            List<String> whereList = parseRuleWhere();
            //校验规则
            validateRule(whereList);
            //预处理规则公共参数
            this.preHandleRule();
            String sql = this.generateRuleScript();
            //替换sql中的查询条件
            String currentSqlSeq = GenHiveQuerySqlTools.getCurrentSqlSeq();
            sql = this.afterHandleRule(sql, whereList,currentSqlSeq);
            return RuleScriptResult.ok(sql);
        } catch (Exception e) {
            log.error("", e);
            return RuleScriptResult.error(e.getMessage());
        }
    }

    /**
     *
     * 功能描述：校验规则
     * @author zhangly
     * @date 2023-02-21 17:14:15
     *
     * @param whereList
     *
     * @return boolean
     *
     */
    protected boolean validateRule(List<String> whereList) throws Exception {
        RuleLimitModel ruleLimitModel = ruleTaskModel.getRuleLimitModel();
        RuleRegexResult regexResult = DcRuleHandleUtil.validRuleRegex(ruleLimitModel.getLimitType(), ruleLimitModel.getLimitText());
        if(!regexResult.isSuccess()) {
            //规则限制内容验证失败
            throw new Exception(regexResult.getMessage());
        }
        if(whereList==null || whereList.size()==0) {
            //规则限制内容解析失败
            String message = "规则=%s，限制内容=%s，查询条件解析失败";
            message = String.format(message, ruleTaskModel.getRuleName(), ruleLimitModel.getLimitText());
            throw new Exception(message);
        }
        return true;
    }

    /**
     *
     * 功能描述：生成规则sql脚本
     * @author zhangly
     * @date 2023-02-17 16:48:01
     *
     * @param
     *
     * @return java.lang.String
     *
     */
    protected String generateRuleScript() throws Exception {
        StringBuilder sb = new StringBuilder();
        String xmlFileName = "QueryRuleYb";
        if(DcConstants.DB_TYPE_GP.equals(ruleTaskModel.getDbType())){
            xmlFileName = "GP_"  + xmlFileName;
        }else if(DcConstants.DB_TYPE_MYSQL.equals(ruleTaskModel.getDbType())){
            xmlFileName = "mysql/"  + xmlFileName;
        }
        String xmlpath = "/com/ai/modules/ybChargeSearch/querysql/" + xmlFileName + ".xml";
        InputStream is = RuleScriptHandler.class.getResourceAsStream(xmlpath);
        Map<String, String> sqlMap = DcRuleHandleUtil.parseRuleXml(is);
        String sql = sqlMap.get("ruleBlackList");

        if(DcConstants.DATA_STATIC_LEVEL_ODS.equals(ruleTaskModel.getDataStaticsLevel())) {
            //ods层级，替换表名
            sql = StringUtils.replace(sql, "src_yb_", "ods_yb_");
        }
        return sql;
    }

    @Override
    public List<String> parseRuleWhere() {
        RuleLimitModel ruleLimitModel = ruleTaskModel.getRuleLimitModel();
        String limitType = ruleLimitModel.getLimitType();
        String dbType = ruleTaskModel.getDbType();
        AbsRuleWhereParser parser = null;
        switch (limitType) {
            case DcConstants.RULE_LIMIT_AGE:
                //年龄
                parser = new AgeRuleWhereParser(ruleLimitModel, dbType);
                break;
            case DcConstants.RULE_LIMIT_DAYAGE:
                //天龄
                parser = new DayAgeRuleWhereParser(ruleLimitModel, dbType);
                break;
            case DcConstants.RULE_LIMIT_SEX:
                //性别
                parser = new SexRuleWhereParser(ruleLimitModel, dbType);
                break;
            case DcConstants.RULE_LIMIT_HOSPLEVEL:
                //医院级别
                parser = new HosplevelRuleWhereParser(ruleLimitModel, dbType);
                break;
            case DcConstants.RULE_LIMIT_VISITTYPE:
                //就诊类型
                parser = new RegexRuleWhereParser(ruleLimitModel, dbType, "visittype");
                break;
            case DcConstants.RULE_LIMIT_INSURANCETYPE:
                //险种类型
                parser = new RegexRuleWhereParser(ruleLimitModel, dbType, "insurancetype");
                break;
            default:
                break;
        }
        return parser!=null ? parser.parse() : null;
    }

    /**
     *
     * 功能描述：生成sql脚本前先预处理查询参数
     * @author zhangly
     * @date 2023-02-17 16:34:22
     *
     * @param
     *
     * @return void
     *
     */
    protected void preHandleRule() {
        String dbType = ruleTaskModel.getDbType();
        String orgname = ruleTaskModel.getOrgname();
        if(StringUtils.isNotBlank(orgname)){
            orgname = DcRuleHandleUtil.replaceRegexLikeValue(orgname, dbType);
            orgname = DcRuleHandleUtil.replaceRegexLikeValueForFull(orgname);
            ruleTaskModel.setOrgname(orgname);
        }
        String visitid = ruleTaskModel.getVisitid();
        if(StringUtils.isNotBlank(visitid)) {
            visitid = DcRuleHandleUtil.replaceRegexLikeValue(visitid, dbType);
            ruleTaskModel.setVisitid(visitid);
        }
        String clientname = ruleTaskModel.getClientname();
        if(StringUtils.isNotBlank(clientname)) {
            clientname = DcRuleHandleUtil.replaceRegexLikeValue(clientname, dbType);
            clientname = DcRuleHandleUtil.replaceRegexLikeValueForFull(clientname);
            ruleTaskModel.setClientname(clientname);
        }
        String idNo = ruleTaskModel.getIdNo();
        if(StringUtils.isNotBlank(idNo)) {
            idNo = DcRuleHandleUtil.replaceRegexLikeValue(idNo, dbType);
            idNo = DcRuleHandleUtil.replaceRegexLikeValueForFull(idNo);
            ruleTaskModel.setIdNo(idNo);
        }
        String caseid = ruleTaskModel.getCaseid();
        if(StringUtils.isNotBlank(caseid)) {
            caseid = DcRuleHandleUtil.replaceRegexLikeValueForFull(caseid);
            ruleTaskModel.setCaseid(caseid);
        }
    }

    /**
     *
     * 功能描述：对sql进行查询条件替换、追加处理
     * @author zhangly
     * @date 2023-02-17 16:51:34
     *
     * @param sql
     * @param whereList
     *
     * @return void
     *
     */
    protected String afterHandleRule(String sql, List<String> whereList,String currentSqlSeq) {
        Map<String, String> params = placeholdersParams(currentSqlSeq);
        //规则限制内容查询条件
        String where = StringUtils.join(whereList, " and ");
        params.put("where", where);
        //替换查询条件
        sql = PlaceholderResolverUtil.replacePlaceholders(sql, params);
        //删除未被替换的行${}
        sql = removePlaceholder(sql);
        return sql;
    }


    /**
     *
     * 功能描述：占位符参数kv
     * @author zhangly
     * @date 2023-02-22 14:23:07
     *
     * @param
     *
     * @return java.util.Map<java.lang.String,java.lang.String>
     *
     */
    protected Map<String, String> placeholdersParams(String currentSqlSeq) {
        Map<String, String> params = new HashMap<String, String>();

        params.put("sqlSeq", currentSqlSeq);
        params.put("itemname", ruleTaskModel.getRuleName());
        String orgname = ruleTaskModel.getOrgname();
        if(StringUtils.isNotBlank(orgname)) {
            params.put("orgname", orgname);
        }

        String orgid = ruleTaskModel.getOrgid();
        if(StringUtils.isNotBlank(orgid)) {
            orgid = StringUtils.replace(orgid, ",", "|");
            orgid = "('" + StringUtils.replace(orgid, "|", "','") + "')";
            params.put("orgid", orgid);
        }
        else{
            params.put("NOORGID" ," ");
        }
        String startDate = ruleTaskModel.getStartDate();
        if(StringUtils.isNotBlank(startDate)) {
            params.put("startDate", startDate);
        }
        String endDate = ruleTaskModel.getEndDate();
        if(StringUtils.isNotBlank(endDate)) {
            params.put("endDate", endDate);
        }
        String visittype = ruleTaskModel.getVisittype();
        if(StringUtils.isNotBlank(visittype)) {
            if(DcConstants.VISITTYPE_ZY.equals(visittype)) {
                //住院
                params.put("visittype", "住院");
            } else if(DcConstants.VISITTYPE_GY.equals(visittype)) {
                //购药
                params.put("visittype", "购药");
            } else if(DcConstants.VISITTYPE_MM.equals(visittype)) {
                //门慢
                params.put("visittype", "门诊|门慢");
            } else {
                //住院+门慢
                params.put("visittype", "住院|门诊|门慢");
            }
        }
        String clientname = ruleTaskModel.getClientname();
        if(StringUtils.isNotBlank(clientname)) {
            params.put("clientname", clientname);
        }
        String idNo = ruleTaskModel.getIdNo();
        if(StringUtils.isNotBlank(idNo)) {
            params.put("idNo", idNo);
        }
        String visitid = ruleTaskModel.getVisitid();
        if(StringUtils.isNotBlank(visitid)) {
            params.put("visitid", visitid);
        }
        String leaveDate = ruleTaskModel.getLeavedate();
        if(StringUtils.isNotBlank(leaveDate)) {
            params.put("leaveStartDate", leaveDate);
            params.put("leaveEndDate", leaveDate);
        }
        String caseid = ruleTaskModel.getCaseid();
        if(StringUtils.isNotBlank(caseid)) {
            params.put("caseid", caseid);
        }
        String limitType = ruleTaskModel.getRuleLimitModel().getLimitType();
        if(DcConstants.RULE_LIMIT_HOSPLEVEL.equals(limitType)) {
            //限医院级别
            params.put("limit_hosplevel", "limit_hosplevel");
        } else {
            params.put("limit_not_hosplevel", "limit_not_hosplevel");
        }
        return params;
    }

    /**
     *
     * 功能描述：剔除未被替换的行${}
     * @author zhangly
     * @date 2023-02-22 10:05:17
     *
     * @param sql
     *
     * @return java.lang.String
     *
     */
    protected String removePlaceholder(String sql) {
        //剔除未被替换的行${}
        String regex = ".*\\$\\{.*\\}.*\n";
        sql = RegExUtils.replaceAll(sql, regex, "");
        //剔除--.*--格式的注释
        regex = "--.*--";
        sql = RegExUtils.replaceAll(sql, regex, "");
        //剔除空行
        regex = "^[ \\t]*\\n";
        sql = RegExUtils.replaceAll(sql, regex, "");
        return sql;
    }

}
