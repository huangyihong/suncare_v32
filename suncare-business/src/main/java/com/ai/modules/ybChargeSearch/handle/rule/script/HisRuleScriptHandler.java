package com.ai.modules.ybChargeSearch.handle.rule.script;

import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.handle.rule.DcRuleHandleUtil;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleScriptResult;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleTaskModel;
import com.ai.modules.ybChargeSearch.service.impl.GenHiveQuerySqlTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.*;

/**
 * @author : zhangly
 * @date : 2023/2/21 16:41
 */
@Slf4j
public class HisRuleScriptHandler extends RuleScriptHandler {

    public HisRuleScriptHandler(RuleTaskModel ruleTaskModel) {
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
            String sql = this.generateRuleScript(whereList);
            return RuleScriptResult.ok(sql);
        } catch (Exception e) {
            log.error("", e);
            return RuleScriptResult.error(e.getMessage());
        }
    }

    @Override
    protected boolean validateRule(List<String> whereList) throws Exception {
        if(DcConstants.RULE_LIMIT_HOSPLEVEL.equals(ruleTaskModel.getRuleLimitModel().getLimitType())
            && DcConstants.DATA_STATIC_LEVEL_SRC.equals(ruleTaskModel.getDataStaticsLevel())) {
            throw new Exception("数据来源=HIS，数据层级=原始层，暂不支持限医院级别的规则检索");
        }
        return super.validateRule(whereList);
    }

    private String generateRuleScript(List<String> whereList) throws Exception {
        String currentSqlSeq = GenHiveQuerySqlTools.getCurrentSqlSeq();
        String xmlFileName = "QueryRuleHis";
        if(DcConstants.DB_TYPE_GP.equals(ruleTaskModel.getDbType())){
            xmlFileName = "GP_"  + xmlFileName;
        }else if(DcConstants.DB_TYPE_MYSQL.equals(ruleTaskModel.getDbType())){
            xmlFileName = "mysql/"  + xmlFileName;
        }
        String xmlpath = "/com/ai/modules/ybChargeSearch/querysql/" + xmlFileName + ".xml";
        InputStream is = RuleScriptHandler.class.getResourceAsStream(xmlpath);
        Map<String, String> sqlMap = DcRuleHandleUtil.parseRuleXml(is);

        Set<String> scriptSet = new HashSet<String>();
        if(DcConstants.VISITTYPE_ZY.equals(ruleTaskModel.getVisittype())) {
            scriptSet.add(DcConstants.VISITTYPE_ZY);
        } else if(DcConstants.VISITTYPE_MM.equals(ruleTaskModel.getVisittype())) {
            scriptSet.add(DcConstants.VISITTYPE_MM);
        } else {
            scriptSet.add(DcConstants.VISITTYPE_ZY);
            scriptSet.add(DcConstants.VISITTYPE_MM);
        }
        if(DcConstants.RULE_LIMIT_INSURANCETYPE.equals(ruleTaskModel.getRuleLimitModel().getLimitType())) {
            //限参保类型规则
            scriptSet.remove(DcConstants.VISITTYPE_MM);
        }

        StringBuilder sb = new StringBuilder();
//        sb.append("with ");
        if(scriptSet.contains(DcConstants.VISITTYPE_ZY)) {
            //住院脚本
            String sql = sqlMap.get("zy_ruleBlackList");
            List<String> zyWheres = new ArrayList<>(whereList);
            for(int i=0,len=zyWheres.size(); i<len; i++) {
                //查询字段替换
                String where = zyWheres.get(i);
                where = RegExUtils.replaceAll(where, "visitdate", "admitdate");
                where = RegExUtils.replaceAll(where, "insurancetype", "insurancetypename");
                zyWheres.set(i, where);
            }
            sql = this.afterHandleRule(sql, zyWheres,currentSqlSeq);
            sb.append(sql);
        }
        if(scriptSet.contains(DcConstants.VISITTYPE_MM)) {
            //门诊
            sb.append(",\n");
            String sql = sqlMap.get("mz_ruleBlackList");
            List<String> mzWheres = new ArrayList<>(whereList);
            for(int i=0,len=mzWheres.size(); i<len; i++) {
                //查询字段替换
                String where = mzWheres.get(i);
                where = RegExUtils.replaceAll(where, "insurancetype", "insurancetypename");
                mzWheres.set(i, where);
            }
            sql = this.afterHandleRule(sql, mzWheres,currentSqlSeq);
            sb.append(sql);
        }
        String sql = sqlMap.get("ruleBlackList");
        Map<String, String> params = new HashMap<>();
        if(scriptSet.size()==1) {
            String visittype = scriptSet.iterator().next();
            if(DcConstants.VISITTYPE_ZY.equals(visittype)) {
                params.put("ZY", "住院");
            } else if(DcConstants.VISITTYPE_MM.equals(visittype)) {
                params.put("MM", "门慢");
            }
        } else {
            params.put("ZY+MM", "住院+门慢");
        }
        params.put("sqlSeq", currentSqlSeq);
        sql = PlaceholderResolverUtil.replacePlaceholders(sql, params);
        sql = this.removePlaceholder(sql);
        sb.append(sql);
        sql = sb.toString();
        if(DcConstants.DATA_STATIC_LEVEL_ODS.equals(ruleTaskModel.getDataStaticsLevel())) {
            //ods层级，替换表名
            sql = StringUtils.replace(sql, "src_his_", "ods_his_");
        }
        return sql;
    }

    @Override
    protected Map<String, String> placeholdersParams(String currentSqlSeq) {
        Map<String, String> params = super.placeholdersParams(currentSqlSeq);
        String limitType = ruleTaskModel.getRuleLimitModel().getLimitType();
        if(DcConstants.RULE_LIMIT_HOSPLEVEL.equals(limitType)) {
            //限医院级别
            params.put("limit_hosplevel", "limit_hosplevel");
        } else {
            params.put("limit_not_hosplevel", "limit_not_hosplevel");
            if(DcConstants.RULE_LIMIT_INSURANCETYPE.equals(limitType)) {
                //限参保类型规则
                params.put("zy_settlement", "结算表");
            } else {
                params.put("zy_master", "就诊信息表");
            }
        }
        return params;
    }
}
