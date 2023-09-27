package com.ai.modules.drg.handle.rule;

import com.ai.common.utils.ExportXUtils;
import com.ai.modules.drg.constants.DrgCatalogConstants;
import com.ai.modules.drg.constants.DrgScriptConstants;
import com.ai.modules.drg.handle.model.DrgResultModel;
import com.ai.modules.drg.handle.model.DrgRuleModel;
import com.ai.modules.drg.handle.model.TaskBatchModel;
import com.ai.modules.drg.handle.model.TaskCatalogModel;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.util.JDBCUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.ybChargeSearch.constants.DcConstants;
import com.ai.modules.ybChargeSearch.handle.rule.DcRuleHandleUtil;
import com.ai.modules.ybChargeSearch.handle.rule.script.RuleScriptHandler;
import com.ai.modules.ybChargeSearch.vo.DatasourceAndDatabaseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author : zhangly
 * @date : 2023/3/31 14:28
 */
@Slf4j
public class DrgHandle extends AbsDrgHandle {


    public DrgHandle(TaskBatchModel batchModel, TaskCatalogModel catalogModel, DatasourceAndDatabaseVO dbVO) {
        super(batchModel, catalogModel, dbVO);
    }

    @Override
    public DrgResultModel execute() throws Exception {
        //先清除历史数据
        clear();
        //查询参数
        Map<String, String> params = placeholdersParams();
        //0.预先准备数据
        execute(DrgScriptConstants.PREPARE, params, false);
        //1.MDCA组
        execute(DrgScriptConstants.MDCA, params, true);
        //2.MDCP组
        execute(DrgScriptConstants.MDCP, params, true);
        //3.MDCY组
        execute(DrgScriptConstants.MDCY, params, true);
        //4.MDCZ组
        execute(DrgScriptConstants.MDCZ, params, true);
        //5.MDC其他组
        execute(DrgScriptConstants.MDC_OTHER, params, true);
        //5.MDC删除
        execute(DrgScriptConstants.MDC_REMOVE, params, true);
        //6.0.ADRG组(预先准备数据)
        execute(DrgScriptConstants.ADRG_PREPARE, params, false);
        //6.1.ADRG组(无手术1诊断)
        execute(DrgScriptConstants.ADRG_0SURGERY1DIAG, params, true);
        //6.2.ADRG组(无手术2诊断)
        execute(DrgScriptConstants.ADRG_0SURGERY2DIAG, params, true);
        //7.1.ADRG组(1手术1诊断)
        execute(DrgScriptConstants.ADRG_1SURGERY1DIAG, params, true);
        //7.2.ADRG组(1手术2诊断)
        execute(DrgScriptConstants.ADRG_1SURGERY2DIAG, params, true);
        //7.3.ADRG组(2手术1诊断)
        execute(DrgScriptConstants.ADRG_2SURGERY1DIAG, params, true);
        //7.4.ADRG组(3手术1诊断)
        execute(DrgScriptConstants.ADRG_3SURGERY1DIAG, params, true);
        //8.1.ADRG组(无手术室手术1诊断)
        execute(DrgScriptConstants.ADRG_0ROOM1DIAG, params, true);
        //8.2.ADRG组(无手术室手术2诊断)
        execute(DrgScriptConstants.ADRG_0ROOM2DIAG, params, true);
        //9.ADRG组(歧义)
        execute(DrgScriptConstants.ADRG_QY, params, true);
        List<DrgRuleModel> adrgRuleList = batchModel.getAdrgRuleList();
        if(adrgRuleList!=null && adrgRuleList.size()>0) {
            //包含ADRG限定规则
            for(DrgRuleModel ruleModel : adrgRuleList) {
                AbsDrgRuleScriptHandler handler = new DrgRuleScriptHandler(batchModel, ruleModel, dbVO);
                log.info("开始-规则：{}", ruleModel.getCatalogCode());
                handler.execute();
                log.info("结束-规则：{}", ruleModel.getCatalogCode());
            }
        }
        //10.0.DRG组(预先准备数据)
        execute(DrgScriptConstants.DRG_PREPARE, params, false);
        //10.1.DRG组(ADRG未找到所属DRG)
        execute(DrgScriptConstants.ADRG_NOT_MAPPING_DRG, params, true);
        //10.2.DRG(不判断次要诊断)
        execute(DrgScriptConstants.DRG_NOT_VALID_SEC_DIAG, params, true);
        //10.3.DRG(无次要诊断)
        execute(DrgScriptConstants.DRG_NOT_HAVE_SEC_DIAG, params, true);
        //10.4.DRG(有次要诊断有效MCC)
        execute(DrgScriptConstants.DRG_VALID_MCC, params, true);
        //10.5.DRG(有次要诊断有效MCC)
        execute(DrgScriptConstants.DRG_INVALID_MCC, params, true);
        //11未找到DRG分组
        execute(DrgScriptConstants.DRG_NOT_FOUND, params, true);
        List<DrgRuleModel> drgRuleList = batchModel.getDrgRuleList();
        if(drgRuleList!=null && drgRuleList.size()>0) {
            //包含DRG限定规则
            for(DrgRuleModel ruleModel : drgRuleList) {
                AbsDrgRuleScriptHandler handler = new DrgRuleScriptHandler(batchModel, ruleModel, dbVO);
                log.info("开始-规则：{}", ruleModel.getCatalogCode());
                handler.execute();
                log.info("结束-规则：{}", ruleModel.getCatalogCode());
            }
        }
        //12未入组的诊断及手术
        execute(DrgScriptConstants.DRG_NOT_JOINED, params, true);

        //最后销毁临时表
        destory(params);
        //返回drg入组结果
        return drgResult(params);
    }

    /**
     *
     * 功能描述：清除历史数据
     * @author zhangly
     * @date 2023-03-31 15:05:16
     *
     * @param
     *
     * @return void
     *
     */
    protected void clear() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("delete from medical_gbdp.medical_visit_mdc where batch_id='%s';\n");
        sb.append("delete from medical_gbdp.medical_visit_adrg where batch_id='%s';\n");
        sb.append("delete from medical_gbdp.medical_visit_drg where batch_id='%s';");
        String sql = String.format(sb.toString(), batchModel.getBatchId(), batchModel.getBatchId(), batchModel.getBatchId());
        log.info("execute sql:\n{}", sql);
        JDBCUtil.execute(sql);
    }

    protected void destory(Map<String, String> params) throws Exception {
        log.info("删除临时表");
        Set<DrgScriptConstants> codeSet = new HashSet<DrgScriptConstants>();
        codeSet.add(DrgScriptConstants.PREPARE);
        codeSet.add(DrgScriptConstants.ADRG_PREPARE);
        codeSet.add(DrgScriptConstants.DRG_PREPARE);
        for(DrgScriptConstants script : codeSet) {
            String xml = script.getCode();
            String sql = generateScript(xml);
            sql = PlaceholderResolverUtil.replacePlaceholders(sql, params);
            //删除未被替换的行${}
            sql = removePlaceholder(sql);
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
                JDBCUtil.execute(sb.toString());
            }
        }
    }

    /**
     *
     * 功能描述：执行每一步骤的sql
     * @author zhangly
     * @date 2023-03-31 15:24:25
     *
     * @param scriptConstants
     * @param params
     * @param rm 是否销毁临时表
     *
     * @return void
     *
     */
    private void execute(DrgScriptConstants scriptConstants, Map<String, String> params, boolean rm) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        String sql = null;
        try {
            log.info("开始-执行步骤：{}", scriptConstants.getName());
            String xml = scriptConstants.getCode();
            sql = generateScript(xml);
            sql = PlaceholderResolverUtil.replacePlaceholders(sql, params);
            //删除未被替换的行${}
            sql = removePlaceholder(sql);
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
            if(rm && stmt!=null) {
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
            }
            log.info("结束-执行步骤：{}", scriptConstants.getName());
            JDBCUtil.destroy(conn, stmt);
        }
    }

    protected String generateScript(String xml) throws Exception {
        Map<String, String> sqlMap = generateScriptMap(xml);
        String sql = sqlMap.get("script");
        return sql;
    }

    private Map<String, String> generateScriptMap(String xml) throws Exception {
        String xmlpath = "/com/ai/modules/drg/handle/sql/" + xml + ".xml";
        InputStream is = RuleScriptHandler.class.getResourceAsStream(xmlpath);
        if(is==null) {
            throw new Exception("未找到文件"+xmlpath);
        }
        Map<String, String> sqlMap = DcRuleHandleUtil.parseRuleXml(is);
        return sqlMap;
    }

    protected Map<String, String> placeholdersParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("seq", batchModel.getCurrentSqlSeq());
        params.put("batchId", batchModel.getBatchId());
        params.put("batchName", batchModel.getBatchName());
        params.put("project", batchModel.getProject());
        params.put("drgCatalogId", catalogModel.getDrgCatalogId());
        params.put("mdcCatalogId", catalogModel.getMdcCatalogId());
        params.put("adrgCatalogId", catalogModel.getAdrgCatalogId());
        params.put("adrgDiagCatalogId", catalogModel.getAdrgDiagCatalogId());
        params.put("surgeryCatalogId", catalogModel.getSurgeryCatalogId());
        params.put("mdcDiagCatalogId", catalogModel.getMdcDiagCatalogId());
        params.put("mccCatalogId", catalogModel.getMccCatalogId());
        params.put("ccCatalogId", catalogModel.getCcCatalogId());
        params.put("excludeCatalogId", catalogModel.getExcludeCatalogId());
        //医院编码
        String orgid = batchModel.getOrgIds();
        if(StringUtils.isNotBlank(orgid)) {
            orgid = StringUtils.replace(orgid, ",", "|");
            orgid = "('" + StringUtils.replace(orgid, "|", "','") + "')";
            params.put("orgid", orgid);
        }
        String startDate = batchModel.getStartVisitdate();
        if(StringUtils.isNotBlank(startDate)) {
            params.put("startDate", startDate);
        }
        String endDate = batchModel.getEndVisitdate();
        if(StringUtils.isNotBlank(endDate)) {
            params.put("endDate", endDate);
        }
        return params;
    }

    private DrgResultModel drgResult(Map<String, String> params) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            int enrollment = 0;
            int noEnrollment = 0;
            Map<String, String> sqlMap = generateScriptMap("DRG_COUNT");
            String sql = sqlMap.get("enrollmentDrg");
            sql = PlaceholderResolverUtil.replacePlaceholders(sql, params);
            //删除未被替换的行${}
            sql = removePlaceholder(sql);
            conn = JDBCUtil.getDbConnection(dbVO.getSysDatabase());
            conn.setAutoCommit(true);
            stmt = conn.createStatement();
            log.info("execute sql:\n{}", sql);
            rs = stmt.executeQuery(sql);
            if(rs.next()) {
                enrollment = rs.getInt("cnt");
            }

            sql = sqlMap.get("noEnrollmentDrg");
            sql = PlaceholderResolverUtil.replacePlaceholders(sql, params);
            //删除未被替换的行${}
            sql = removePlaceholder(sql);
            rs = stmt.executeQuery(sql);
            if(rs.next()) {
                noEnrollment = rs.getInt("cnt");
            }
            DrgResultModel resultModel = DrgResultModel.ok();
            resultModel.setEnrollment(enrollment);
            resultModel.setNoEnrollment(noEnrollment);
            return resultModel;
        } catch(Exception e) {
            log.error("", e);
            throw e;
        } finally {
            JDBCUtil.destroy(conn, stmt);
        }
    }
}
