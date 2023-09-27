package com.ai.modules.drg.handle.rule;

import com.ai.modules.drg.constants.DrgCatalogConstants;
import com.ai.modules.drg.entity.DrgRuleLimites;
import com.ai.modules.drg.handle.model.DrgRuleModel;
import com.ai.modules.drg.handle.model.TaskBatchModel;
import com.ai.modules.drg.handle.where.*;
import com.ai.modules.engine.util.JDBCUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.handle.rule.DcRuleHandleUtil;
import com.ai.modules.ybChargeSearch.handle.rule.model.RuleScriptResult;
import com.ai.modules.ybChargeSearch.vo.DatasourceAndDatabaseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : zhangly
 * @date : 2023/2/17 14:20
 */
@Slf4j
public class DrgRuleScriptHandler extends AbsDrgRuleScriptHandler {
    public DrgRuleScriptHandler(TaskBatchModel batchModel, DrgRuleModel ruleModel, DatasourceAndDatabaseVO dbVO) {
        super(batchModel, ruleModel, dbVO);
    }

    @Override
    public void execute() throws Exception {
        RuleScriptResult scriptResult = this.parseRuleScript();
        if(!scriptResult.isSuccess()) {
            throw new Exception(scriptResult.getMessage());
        }
        Connection conn = null;
        Statement stmt = null;
        String sql = null;
        try {
            sql = scriptResult.getScript();
            conn = JDBCUtil.getDbConnection(dbVO.getSysDatabase());
            conn.setAutoCommit(true);
            stmt = conn.createStatement();
            stmt.setQueryTimeout(60*30);
            log.info("execute sql:\n{}", sql);
            stmt.execute(sql);
        } catch(Exception e) {
            log.error("", e);
            throw e;
        } finally {
            //销毁临时表
            StringBuilder sb = new StringBuilder();
            String regex = ".*drop table if exists medical_gbdp.t_.*;";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sql);
            int cnt = 0;
            while(matcher.find()) {
                String match = matcher.group();
                match = match.trim();
                if(cnt>0) {
                    sb.append("\n");
                }
                sb.append(match);
                cnt++;
            }
            if(cnt>0) {
                log.info("execute sql:\n{}", sb.toString());
                stmt.execute(sb.toString());
            }
            JDBCUtil.destroy(conn, stmt);
        }
    }

    @Override
    public RuleScriptResult parseRuleScript() {
        try {
            String where = parseRuleWhere();
            if(StringUtils.isBlank(where)) {
                String message = "规则=%s，限制条件解析失败";
                message = String.format(message, ruleModel.getCatalogCode());
                throw new Exception(message);
            }
            String sql = this.generateRuleScript();
            //替换sql中的查询条件
            sql = this.afterHandleRule(sql, where);
            return RuleScriptResult.ok(sql);
        } catch (Exception e) {
            log.error("", e);
            return RuleScriptResult.error(e.getMessage());
        }
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
        String xmlpath = "/com/ai/modules/drg/handle/sql/DRG_RULE.xml";
        InputStream is = DrgRuleScriptHandler.class.getResourceAsStream(xmlpath);
        Map<String, String> sqlMap = DcRuleHandleUtil.parseRuleXml(is);
        String key = "drgScript";
        if(DrgCatalogConstants.ADRG_V.equals(ruleModel.getCatalogType())) {
            key = "adrgScript";
        } else if(DrgCatalogConstants.MDC_V.equals(ruleModel.getCatalogType())) {
            key = "mdcScript";
        }
        String sql = sqlMap.get(key);
        return sql;
    }

    @Override
    public String parseRuleWhere() {
        StringBuilder sb = new StringBuilder();
        List<DrgRuleLimites> ruleLimitesList = ruleModel.getRuleLimitesList();
        if(ruleLimitesList.size()>1) {
            sb.append("(");
        }
        boolean first = true;
        for(DrgRuleLimites ruleLimites : ruleLimitesList) {
            String whereType = ruleLimites.getWhereType();
            AbsRuleWhereParser parser = null;
            switch (whereType) {
                case DcConstants.RULE_LIMIT_AGE:
                    //年龄
                    parser = new AgeRuleWhereParser(ruleLimites);
                    break;
                case DcConstants.RULE_LIMIT_DAYAGE:
                    //天龄
                    parser = new DayAgeRuleWhereParser(ruleLimites);
                    break;
                case DcConstants.RULE_LIMIT_ZYDAYS:
                    //住院天数
                    parser = new ZyDaysRuleWhereParser(ruleLimites);
                    break;
                case DcConstants.RULE_LIMIT_WEIGHT:
                    //出生体重
                    parser = new WeightRuleWhereParser(ruleLimites);
                    break;
                case DcConstants.RULE_LIMIT_LEAVETYPE:
                    //离院方式
                    parser = new BaseRuleWhereParser(ruleLimites, "leavetype");
                    break;
                default:
                    break;
            }
            if(parser!=null) {
                String where = parser.parse();
                if(!first) {
                    String logic = "and";
                    if(StringUtils.isNotBlank(ruleLimites.getLogic())) {
                        logic = ruleLimites.getLogic();
                    }
                    sb.append(" ").append(logic).append(" ");
                }
                sb.append(where);
                first = false;
            }
        }
        if(ruleLimitesList.size()>1) {
            sb.append(")");
        }
        return sb.toString();
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

    }

    /**
     *
     * 功能描述：对sql进行查询条件替换、追加处理
     * @author zhangly
     * @date 2023-02-17 16:51:34
     *
     * @param sql
     * @param where
     *
     * @return void
     *
     */
    protected String afterHandleRule(String sql, String where) {
        Map<String, String> params = placeholdersParams();
        //规则限制内容查询条件
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
    protected Map<String, String> placeholdersParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("seq", batchModel.getCurrentSqlSeq());
        params.put("batchId", batchModel.getBatchId());
        params.put("batchName", batchModel.getBatchName());
        params.put("project", batchModel.getProject());
        params.put("drg", ruleModel.getCatalogCode());
        params.put("adrg", ruleModel.getCatalogCode());
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
