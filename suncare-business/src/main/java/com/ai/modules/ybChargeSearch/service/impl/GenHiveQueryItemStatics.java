package com.ai.modules.ybChargeSearch.service.impl;

import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.vo.YbChargeQueryDatabase;
import com.ai.modules.ybChargeSearch.vo.YbChargeSearchConstant;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;


/**
 * 生成项目年度统计报表
 */
public class GenHiveQueryItemStatics extends GenHiveQueryCommon {

    /**
     * 根据条件，生成 按医院或者机构统计的医保收费项目汇总清单 的查询SQL
     * @param searchTask
     * @param querySysPara
     * @param isStaticByDept true 时按科室统计， false按机构统计
     * @return
     * @throws Exception
     */
    public String genItemNameStaticSql(YbChargeSearchTask searchTask,
                                       YbChargeQueryDatabase querySysPara,
                                       boolean isStaticByDept ) throws Exception{

        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();
         //预处理查询条件
        preDealSearchTask(searchTask);
        String sqlId="querySql_YB";

        //来源于HIS数据
        if(ETL_SOURCE_HIS_SEARCH_PARA.equalsIgnoreCase(searchTask.getEtlSource())){
            sqlId = "querySql_HIS";
        }

        String xmlFileName="QueryItemStatics.xml";
        String dbType = querySysPara.getDbtype();
        String querySql =getSqlFromXml(xmlFileName ,sqlId,dbType);

        //如果是按科室统计，则打开科室的开关
        if( isStaticByDept == true){
            querySql = StringUtils.replace(querySql ,"--${DEPT_STATIC}" ,"");
        }else{
            //按机构统计
            querySql = StringUtils.replace(querySql ,"--${ORG_STATIC}" ,"");
        }

        querySql = replaceValueForSql(querySql ,searchTask, dataStoreVersion , dataStoreProject);

        return querySql;
    }

    /**
     * 根据机构名称 和 数据来源 获取查询科室列表的SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public String genQueryDeptListSql(YbChargeSearchTask searchTask ,YbChargeQueryDatabase querySysPara) throws Exception{
        //预处理查询条件
        preDealSearchTask(searchTask);

        String sqlId="queryDeptListFromYb";

        //来源于HIS数据
        if(ETL_SOURCE_HIS_SEARCH_PARA.equalsIgnoreCase(searchTask.getEtlSource())){
            sqlId = "queryDeptListFromHis";
        }
        String xmlFileName="QueryItemStatics.xml";
        String dbType = querySysPara.getDbtype();

        String querySql =getSqlFromXml(xmlFileName ,sqlId,dbType);

        querySql = replaceValueForSql(querySql ,searchTask, "" , "");

        return querySql;
    }

    /**
     * 根据项目和菜单名称，得到获取机构列表
     * @param searchTask
     * @return
     * @throws Exception
     */
    public String genQueryOrgListSql(YbChargeSearchTask searchTask,YbChargeQueryDatabase querySysPara) throws Exception{

        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();

        String sqlId="queryOrgList";
        String table = "";
        if(YbChargeSearchConstant.LOW_STATISTICS.equalsIgnoreCase(searchTask.getTaskType())){
            sqlId="queryOrgListFromLow";
            searchTask.setTagId("inhospital_risk01|inhospital_risk02");
        }else if(YbChargeSearchConstant.RISK_STATISTICS.equalsIgnoreCase(searchTask.getTaskType())) {
            sqlId = "queryOrgListFromRisk";
            table = "datamining_chargeitem_risk_data";
        }else if(YbChargeSearchConstant.DIAG_RISK_STATISTICS.equalsIgnoreCase(searchTask.getTaskType())){
            sqlId="queryOrgListFromRisk";
            table = "datamining_diag_risk_data";
        }
        if("queryOrgListFromRisk".equals(sqlId)&&StringUtils.isBlank(searchTask.getTagId())){
            sqlId="queryOrgListFromRiskAlltag";
        }

        //机构属性列表
        if("queryOwntypeList".equals(searchTask.getTaskType())){
            sqlId="queryOwntypeList";
        }
        //机构所在地列表
        if("queryLocalTagList".equals(searchTask.getTaskType())){
            sqlId="queryLocalTagList";
        }
        if("queryHosplevelList".equals(searchTask.getTaskType())){
            sqlId="queryHosplevelList";
        }
        if("queryOwntypeLocalTagHosplevel".equals(searchTask.getTaskType())){
            sqlId="queryOwntypeLocalTagHosplevel";
        }

        String xmlFileName="QueryItemStatics.xml";
        String dbType = querySysPara.getDbtype();

        String querySql =getSqlFromXml(xmlFileName ,sqlId,dbType);
        if(StringUtils.isNotBlank(dataStoreProject)){
            querySql = StringUtils.replace(querySql,"${project}" ,dataStoreProject);
        }
        if(StringUtils.isNotBlank(table)){
            querySql = StringUtils.replace(querySql ,"$TABLE$" ,table);
        }
//        if(StringUtils.isNotBlank(searchTask.getTagId())){
            querySql = StringUtils.replace(querySql ,"${tag_id}" ,StringUtils.isNotBlank(searchTask.getTagId())?searchTask.getTagId():"");
//        }
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
            //searchTask.setOrgs("江西益丰大药房连锁有限公司上饶五三大道店");
            searchTask.setOrgids("1000SLYY0003|9901000SLYY0003|H61100200113");
            //searchTask.setItemname("腹膜透析液#罗沙司他#司维拉姆#尿激酶#碳酸镧#碘伏帽");
            searchTask.setChargedateStartdate(format.parse("2020-01-041"));
            searchTask.setChargedateEnddate(format.parse("2024-02-04"));
            //searchTask.setVisittype("ZY");
           // searchTask.setDeptname("deptaa");
            //searchTask.setOrderby("sum_fee desc");
            //searchTask.setDataStaticsLevel("ods");
            //searchTask.setEtlSource("his");
            searchTask.setIsFundpay("0");
            String dataStoreVersion = "";
            String dataStoreProject = "shanxi";

            //searchTask.setLocalTag("1");

            YbChargeQueryDatabase querySysPara = new YbChargeQueryDatabase();
            querySysPara.setDataStoreProject(dataStoreProject);
            querySysPara.setDataStoreVersion(dataStoreVersion);

            querySysPara.setDbtype("greenplum");

            String sql = (new GenHiveQueryItemStatics()).
                    genItemNameStaticSql(searchTask, querySysPara,
                            false);
            //String sql = (new GenHiveQueryItemStatics()).
            //        genQueryOrgListSql(searchTask, querySysPara);

            System.out.println(sql);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
