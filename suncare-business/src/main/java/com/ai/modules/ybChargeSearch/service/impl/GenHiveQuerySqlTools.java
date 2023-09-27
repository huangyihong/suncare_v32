package com.ai.modules.ybChargeSearch.service.impl;


import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.vo.YbChargeQueryDatabase;
import com.ai.modules.ybChargeSearch.vo.YbChargeQuerySql;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 生成HIVEsql查询工具
 */
public class GenHiveQuerySqlTools {

    //SQL执行顺序号和当前线程的
    private static Object synObj = new Object();
    private static int sqlSeq =0;

    /**
     * 获取当前的执行序列
     * @return
     */
    public static String getCurrentSqlSeq(){
        String tempStr = "";
        synchronized (synObj){
            sqlSeq = sqlSeq +1;
            if(sqlSeq>=1000){
                sqlSeq=1;
            }
            tempStr = sqlSeq + "";
        }

        return  tempStr;
    }

    /**
     * 获取当前SQL的最大返回数限制，
     * 小于等于0表示不限制
     * 如果返回值大于0，需要先执行count，如果结果集总数大于指定的最大数量，则报错不让下载
     */
    public static long  getMaxCountLimit(String querySql){
        int index = querySql.indexOf("limited-max-count");
        if(index<0) {
            return -1L;
        }

        querySql = StringUtils.replace(querySql ,"\n" ," ");
        //第一步 ，查找 limited-max-count 背后的等号
        index = querySql.indexOf("=" ,index);
        if(index<0) {
            return -1L;
        }

        //第二步：获取等号后面的内容并trim
        querySql = querySql.substring(index+1).trim() + " ";

        //第三步，获取等号后面的数字
        index = querySql.indexOf(" ");
        String limitCountStr = querySql.substring(0 ,index);

        try{
            return Long.parseLong(limitCountStr);
        } catch (Exception ex){
            return -1;
        }
    }

    /**
     * 根据查询SQL获取计算当前SQL结果数量的SQL
     * @param querySql
     * @return
     */
    public static String getCountSqlByQuerySql(String querySql){
        int index = querySql.lastIndexOf(";");
        if(index>0){
            querySql = querySql.substring(index +1).trim();
        }
        //判断是否有 order by 语句:
        String tmpSql = querySql.toLowerCase();
        tmpSql = StringUtils.replace(tmpSql ,"\n" ," ");
        index = tmpSql.lastIndexOf(" order ");
        if(index >0){
            querySql = querySql.substring(0 ,index);
        }

        querySql = "select count(1) as count from (" + querySql + ") t";
        return querySql;
    }

    /**
     * 获取清除当前线程临时表的SQL
     * @return
     */
    public static String  getDropTempTablSql(List<YbChargeQuerySql> sqlList){
        if(sqlList == null || sqlList.size()==0){
            return "";
        }

        String dropTempTableSql = "";
        for(YbChargeQuerySql ybChargeQuerySql : sqlList){
            String querySql = ybChargeQuerySql.getQuerySql();
            if(querySql == null || "".equalsIgnoreCase(querySql)){
                continue;
            }
            String sqls[] = StringUtils.split(querySql ,";");
            for(String sql :sqls){
                String tmpSql1 = sql.toLowerCase().trim();

                if(tmpSql1.indexOf("drop ") <0 || tmpSql1.indexOf(" exists ") <0 ){
                    continue;
                }

                //去除注释 再拼接
                String tmpSqls2 [] = StringUtils.split(sql ,"\n");
                for(String tmpSql2 :tmpSqls2){
                    if(tmpSql2.trim().startsWith("--")){
                        continue;
                    }
                    dropTempTableSql = dropTempTableSql + tmpSql2 +  "\n";
                }

                dropTempTableSql = dropTempTableSql + ";\n";
            }

        }

        return dropTempTableSql;
    }

    /**
     * 根据条件，科室前10住院费用收费清单
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genDeoptTop10ChargeListSql(
            YbChargeSearchTask searchTask, YbChargeQueryDatabase querySysPara) throws Exception {
        return (new GenHiveQueryDeptTop10()).genDeoptChargeListSqlInner(searchTask, querySysPara,true);
    }

    /**
     * 根据条件，项目汇总---就诊层级汇总
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genVisitChargeListSql(
            YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception {
        return (new GenHiveQueryDeptTop10()).genDeoptChargeListSqlInner(searchTask, querySysPara,false);
    }
        /**
         * 根据条件，生成 住院、门慢 清单 的查询SQL
         * @param searchTask
         * @return
         * @throws Exception
         */
    public static String genVisitListSql(
            YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception{
        return  (new GenHiveQueryVisitList()).genVisitListSqlInner(searchTask, querySysPara);

    }

    /**
     * 根据条件，生成 按机构统计的 医保收费项目汇总清单 的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genItemNameStaticSqlByOrg(
            YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryItemStatics()).genItemNameStaticSql(searchTask,
                querySysPara ,false);
    }

    /**
     * 根据条件，生成 按科室统计的 医保收费项目汇总清单 的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genItemNameStaticSqlByDept(
            YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryItemStatics()).genItemNameStaticSql(searchTask,
                querySysPara,true);
    }

    /**
     * 根据机构名称 和 数据来源(yb/his) 获取查询科室列表的SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genQueryDeptListSql(
            YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryItemStatics()).genQueryDeptListSql(searchTask,querySysPara);
    }

    /**
     * 根据项目和菜单名称，得到获取机构列表
     * @param querySysPara
     * @return
     * @throws Exception
     */
    public static String genQueryOrgListSql(YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryItemStatics()).genQueryOrgListSql(searchTask,querySysPara);
    }

    /**
     * 根据条件，生成 年度统计指标 的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genHospitalYearStaticSql(
            YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryYearStatics()).genHospitalYearStaticSqlInner(searchTask, querySysPara);

    }

    /**
     * 根据条件，生成 年度统计指标--医保项目使用率的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genHospitalYearCountSql(
            YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryYearStatics()).genHospitalYearCountSqlInner(searchTask, querySysPara);

    }

    /**
     * 根据条件，生成 年度统计指标--医院手术情况统计的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genHospitalYearSurgerySql(
            YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryYearStatics()).genHospitalYearSurgerySqlInner(searchTask, querySysPara);

    }
    /**
     * 根据条件，生成 年度统计指标--医院每日住院人数
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genHospitalOnLinePatientCount(
            YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryYearStatics()).genHospitalOnLinePatientCount(searchTask, querySysPara);

    }
    /**
     * 根据条件列表生成 下载收费明细 的SQL
     * @param searchTaskList
     * @return
     * @throws Exception
     */
    public static List<YbChargeQuerySql> genSrcYbChargeDetailSqlList(
            List<YbChargeSearchTask> searchTaskList, YbChargeQueryDatabase querySysPara) throws Exception{
        GenHiveQueryChargeDetail obj = new GenHiveQueryChargeDetail();
        return obj.genSrcChargeDetailSqlList(searchTaskList, querySysPara);

    }



    /**
     * 根据条件，生成 超标准收费的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genOverStandardFeeSql( YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception{

        String tagId = "hosp_itemprice_risk01"; //超标准收费
        return (new GenHiveQueryRiskData()).genChargeitemRiskDataSql(searchTask,
                querySysPara,tagId);
    }

    /**
     * 根据条件，生成 医院年度收费项目总金額超高的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genHospChargeitenRisk01Sql( YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception{

        String tagId = "hosp_chargeitem_risk01"; //医院年度收费项目总金額超高的查询SQL
        return (new GenHiveQueryRiskData()).genChargeitemRiskDataSql(searchTask,
                querySysPara,tagId);
    }

    /**
     * 根据条件，生成 医院年度收费项目人均金額超高的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genHospChargeitenRisk02Sql( YbChargeSearchTask searchTask,
                                                     YbChargeQueryDatabase querySysPara) throws Exception{

        String tagId = "hosp_chargeitem_risk02"; //医院年度收费项目人均金額超高的查询SQL
        return (new GenHiveQueryRiskData()).genChargeitemRiskDataSql(searchTask,
                querySysPara,tagId);
    }

    /**
     * 低标准入院
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genInHospitalRiskSql( YbChargeSearchTask searchTask,
                                               YbChargeQueryDatabase querySysPara) throws Exception{

        String tagId = "inhospital_risk01|inhospital_risk02"; //低标准入院

        return (new GenHiveQueryRiskData()).genDwbVisitTagSql(searchTask,
                querySysPara,tagId);

    }

    /**
     * 根据条件，生成 医院收费项目异常的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genHospChargeRiskSql( YbChargeSearchTask searchTask,
                                               YbChargeQueryDatabase querySysPara) throws Exception{

        String tagId = searchTask.getTagId(); //标签(算法)ID
        return (new GenHiveQueryRiskData()).genChargeitemRiskDataSql(searchTask,
                querySysPara,tagId);
    }

    /**
     * 根据项目地查询datamining_chargeitem_risk_data表获取标签ID和名称
     * @param querySysPara 项目地等条件
     * @return
     * @throws Exception
     */
    public static String genTagInfoForChargeItemSql(YbChargeQueryDatabase querySysPara,YbChargeSearchTask searchTask) throws Exception {
        return (new GenHiveQueryRiskData()).genTagInfoForChargeItemSql(querySysPara,searchTask);
    }

    /**
     * 根据项目地获取数据标签列表
     * @param querySysPara 项目地等条件
     * @return
     * @throws Exception
     */
    public static String genTagListSql(YbChargeQueryDatabase querySysPara,YbChargeSearchTask searchTask) throws Exception {
        return (new GenHiveQueryRiskData()).genTagListSql(querySysPara,searchTask);
    }


        /**
         * 根据条件，生成 医院诊断汇总数据异常的查询SQL
         * @param searchTask
         * @return
         * @throws Exception
         */
    public static String genDiagRiskSql( YbChargeSearchTask searchTask,
                                         YbChargeQueryDatabase querySysPara) throws Exception{

        String tagId = searchTask.getTagId(); //标签(算法)ID
        return (new GenHiveQueryRiskData()).genDiagRiskDataSql(searchTask,
                querySysPara,tagId);
    }

    /**
     * 根据条件，生成 医院总量异常的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genOrgRiskSql( YbChargeSearchTask searchTask,
                                         YbChargeQueryDatabase querySysPara) throws Exception{

        String tagId = searchTask.getTagId(); //标签(算法)ID
        return (new GenHiveQueryRiskData()).genOrgRiskDataSql(searchTask,
                querySysPara,tagId);
    }

    /**
     * 根据条件，生成 结伴就医的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genVisitTogetherSql( YbChargeSearchTask searchTask,
                                        YbChargeQueryDatabase querySysPara) throws Exception{

        String tagId = searchTask.getTagId(); //标签(算法)ID
        return (new GenHiveQueryRiskData()).genVisitTogetherDataSql(searchTask,
                querySysPara,tagId);
    }

    /**
     * 根据条件，生成 患者异常情况汇总表的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genPatientRiskGroupSql( YbChargeSearchTask searchTask,
                                               YbChargeQueryDatabase querySysPara) throws Exception{

        String tagId = searchTask.getTagId(); //标签(算法)ID
        return (new GenHiveQueryRiskData()).genPatientRiskGroupSql(searchTask,
                querySysPara,tagId);
    }

    /**
     * 根据条件，生成 患者异常情况明细表的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genPatientRiskResultSql( YbChargeSearchTask searchTask,
                                                 YbChargeQueryDatabase querySysPara) throws Exception{

        String tagId = searchTask.getTagId(); //标签(算法)ID
        return (new GenHiveQueryRiskData()).genPatientRiskResultSql(searchTask,
                querySysPara,tagId);
    }

    /**
     * 根据条件，生成 医生异常情况汇总表的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genDoctorRiskGroupSql( YbChargeSearchTask searchTask,
                                                 YbChargeQueryDatabase querySysPara) throws Exception{

        String tagId = searchTask.getTagId(); //标签(算法)ID
        return (new GenHiveQueryRiskData()).genDoctorRiskGroupSql(searchTask,
                querySysPara,tagId);
    }

    /**
     * 根据条件，生成 医生异常情况明细表的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genDoctorRiskResultSql( YbChargeSearchTask searchTask,
                                                  YbChargeQueryDatabase querySysPara) throws Exception{

        String tagId = searchTask.getTagId(); //标签(算法)ID
        return (new GenHiveQueryRiskData()).genDoctorRiskResultSql(searchTask,
                querySysPara,tagId);
    }

    /**
     * 根据条件，生成 标签结果汇总monitor_datamining_stat的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genTagStatResultSql( YbChargeSearchTask searchTask,
                                                 YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryRiskData()).genTagStatResultSql(searchTask,
                querySysPara);
    }

    /**
     * 根据条件，生成 可疑标签汇总表的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genSuspiciousStatResultSql( YbChargeSearchTask searchTask,
                                                     YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryRiskData()).genSuspiciousStatResultSql(searchTask,
                querySysPara);
    }

    /**
     * 根据条件，生成 datamining_org_sum的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genDataminingOrgSumResultSql(YbChargeQueryDatabase querySysPara,YbChargeSearchTask searchTask) throws Exception {
        return (new GenHiveQueryRiskData()).genDataminingOrgSumResultSql(searchTask,
                querySysPara);
    }

    public static List<String> genCreateYearSql(YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryYearStatics()).genCreateYearSql(querySysPara);
    }

    /**
     * 根据条件，生成 欺诈专题 患者年度统计的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genFraudPatientResultSql( YbChargeSearchTask searchTask,
                                                     YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryRiskData()).genFraudPatientResultSql(searchTask,
                querySysPara);
    }
    /**
     * 根据条件，生成 任务类型的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public static String genTaskTypeResultSql( YbChargeSearchTask searchTask,
                                                   YbChargeQueryDatabase querySysPara) throws Exception{
        return (new GenHiveQueryRiskData()).genTaskTypeResultSql(searchTask,
                querySysPara);
    }

    public static void main(String[] args) {
        List<YbChargeQuerySql> sqlList = new ArrayList<YbChargeQuerySql>();
        YbChargeQuerySql obj = new YbChargeQuerySql();
        obj.setQuerySql("ajk;select * From medical.tmptable");
        sqlList.add(obj);

        sqlList.add(obj);
        String sql = getDropTempTablSql(sqlList);
        System.out.println(sql);
    }


}
