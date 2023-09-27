package com.ai.modules.ybChargeSearch.service.impl;



import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.vo.YbChargeQueryDatabase;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据报表---年度统计指标报表
 */
public class GenHiveQueryYearStatics extends GenHiveQueryCommon {


    /**
     * 根据条件，生成 年度统计指标 的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genHospitalYearStaticSqlInner(YbChargeSearchTask searchTask,
                                                 YbChargeQueryDatabase querySysPara) throws Exception{

        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();


        //预处理查询条件
        preDealSearchTask(searchTask);
        String sqlId="querySql_YB";

        //来源于HIS数据
        if(ETL_SOURCE_HIS_SEARCH_PARA.equalsIgnoreCase(searchTask.getEtlSource())){
            sqlId = "querySql_HIS";
        }

        String xmlFileName="QueryYearStatics.xml";
        String dbType = querySysPara.getDbtype();

        String querySql =getSqlFromXml(xmlFileName ,sqlId,dbType);

        querySql = replaceValueForSql(querySql ,searchTask, dataStoreVersion , dataStoreProject);

        return querySql;
    }


    /**
     * 根据条件，生成 年度统计指标--医保项目使用率的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genHospitalYearCountSqlInner(YbChargeSearchTask searchTask,
                                                 YbChargeQueryDatabase querySysPara) throws Exception{

        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();


        //预处理查询条件
        preDealSearchTask(searchTask);
        String sqlId="year_count_YB";

        //来源于HIS数据 HIS TODO
//        if(ETL_SOURCE_HIS_SEARCH_PARA.equalsIgnoreCase(searchTask.getEtlSource())){
//            sqlId = "querySql_HIS";
//        }

        String xmlFileName="QueryYearCount.xml";
        String dbType = querySysPara.getDbtype();

        String querySql =getSqlFromXml(xmlFileName ,sqlId,dbType);

        querySql = replaceValueForSql(querySql ,searchTask, dataStoreVersion , dataStoreProject);

        return querySql;
    }


    public List<String> genCreateYearSql(YbChargeQueryDatabase querySysPara) throws Exception{
        String xmlFileName="QueryYearStatics.xml";
        String xmlFileName2="QueryYearCount.xml";
        String dbType = querySysPara.getDbtype();
        List<String> sqlList = new ArrayList<>();
        sqlList.add(StringUtils.replace(getSqlFromXml(xmlFileName ,"createSql_YB",dbType),"${sqlSeq}" ,GenHiveQuerySqlTools.getCurrentSqlSeq()));
        sqlList.add(StringUtils.replace(getSqlFromXml(xmlFileName ,"createSql_HIS",dbType),"${sqlSeq}" ,GenHiveQuerySqlTools.getCurrentSqlSeq()));
        sqlList.add(StringUtils.replace(getSqlFromXml(xmlFileName2 ,"create_year_count_YB",dbType),"${sqlSeq}" ,GenHiveQuerySqlTools.getCurrentSqlSeq()));
        return sqlList;
    }


    /**
     * 根据条件，年度统计指标--医院手术情况统计的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genHospitalYearSurgerySqlInner(YbChargeSearchTask searchTask,
                                                 YbChargeQueryDatabase querySysPara) throws Exception{

        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();


        //预处理查询条件
        preDealSearchTask(searchTask);

        String sqlId="dws_surgery_stat";

        String xmlFileName="QueryYearStatics.xml";
        String dbType = querySysPara.getDbtype();

        String querySql =getSqlFromXml(xmlFileName ,sqlId,dbType);

        querySql = replaceValueForSql(querySql ,searchTask, dataStoreVersion , dataStoreProject);

        return querySql;
    }

    /**
     * 根据条件，年度统计指标--查询医院每日在院人数SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genHospitalOnLinePatientCount(YbChargeSearchTask searchTask,
                                                  YbChargeQueryDatabase querySysPara) throws Exception{

        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();


        //预处理查询条件
        preDealSearchTask(searchTask);

        String sqlId="dws_org_day_sum";

        String xmlFileName="QueryYearStatics.xml";
        String dbType = querySysPara.getDbtype();

        String querySql =getSqlFromXml(xmlFileName ,sqlId,dbType);

        querySql = replaceValueForSql(querySql ,searchTask, dataStoreVersion , dataStoreProject);

        return querySql;
    }


    public static void main(String[] args) {
        try {

            String queryText = "血细胞分析" + "\r\n" +
                    "血细胞分析|血液加温治疗|脑反射治疗" + "\r\n" +
                    "心脏彩色多普勒超声＋左心功能测定" + "\r\n" +
                    "心脏彩色多普勒超声＋左心功能测定|室壁运动分析";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            YbChargeSearchTask searchTask = new YbChargeSearchTask();
            searchTask.setOrgs("汉滨区第二医院");
            //searchTask.setItemname("腹膜透析液#罗沙司他#司维拉姆#尿激酶#碳酸镧#碘伏帽");
            searchTask.setChargedateStartdate(format.parse("2020-01-04"));
            searchTask.setChargedateEnddate(format.parse("2022-12-31"));
            searchTask.setVisittype("MM");
            //searchTask.setOrderby("sum_totalfee desc");
            //searchTask.setDataStaticsLevel("ods");
            searchTask.setEtlSource("his");
            searchTask.setIsFundpay("0");
            String dataStoreVersion="";
            String dataStoreProject="yangshuo";

            YbChargeQueryDatabase querySysPara = new YbChargeQueryDatabase();
            querySysPara.setDataStoreProject(dataStoreProject);
            querySysPara.setDataStoreVersion(dataStoreVersion);
            querySysPara.setDbtype("greenplum");

            String sql = (new GenHiveQueryYearStatics()).genHospitalYearStaticSqlInner
                    (searchTask, querySysPara);

            System.out.println(sql);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


}
