package com.ai.modules.ybChargeSearch.service.impl;

import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.vo.YbChargeQueryDatabase;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;

/**
 * 生成科室前10明细数据
 */
public class GenHiveQueryDeptTop10  extends GenHiveQueryCommon{

    /**
     * 根据条件，科室前10住院费用收费清单
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genDeoptChargeListSqlInner(YbChargeSearchTask searchTask,
                                              YbChargeQueryDatabase querySysPara,
                                              boolean isTop10) throws Exception {
        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();
        //预处理查询条件
        preDealSearchTask(searchTask);

        String sqlIds[];

        if(ETL_SOURCE_HIS_SEARCH_PARA.equalsIgnoreCase(searchTask.getEtlSource())){
            if("ZY".equalsIgnoreCase(searchTask.getVisittype())){
                sqlIds = new String[]{"querySql_HIS_ZY"};
            }
            else if("MM".equalsIgnoreCase(searchTask.getVisittype())){
                sqlIds = new String[]{"querySql_HIS_MZ"};
            }
            else{
                throw new Exception("针对HIS数据，不能同时选择门诊数据和住院数据！");
            }
        }

        else{
            if(isTop10 == true) {
                sqlIds = new String[]{"querySql_YB_itenlist_by_dept_top10" };
            }else {
                sqlIds = new String[]{"querySql_YB_itenlist_by_visit" };
            }
        }

        String xmlFileName="QueryDeptTop10.xml";
        String dbType = querySysPara.getDbtype();

        String querySql =getSqlFromXml(xmlFileName ,sqlIds,dbType);

        //针对科室前10
        if(isTop10 == true){
            querySql =  StringUtils.replace(querySql,"--${TOP10}","");
        }else{
            querySql =  StringUtils.replace(querySql,"--${NORMAL}","");
        }
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
            searchTask.setOrgs("阳朔县福利镇顺梅村卫生室");
            searchTask.setOrgids("11111");
            searchTask.setItemname("腹膜透析液#罗沙司他#司维拉姆#尿激酶#碳酸镧#碘伏帽");
            searchTask.setChargedateStartdate(format.parse("2020-02-04"));
            searchTask.setChargedateEnddate(format.parse("2022-02-04"));
            searchTask.setVisittype("ZY");
            //searchTask.setOrderby("sum_totalfee desc");
            //searchTask.setDataStaticsLevel("ods");
            //searchTask.setEtlSource("his");
            searchTask.setTagName("dddd");
            searchTask.setVisitid("vis");
            //searchTask.setIsFundpay("1");

            String dataStoreVersion="sr1";
            String dataStoreProject = "shanxi";

            YbChargeQueryDatabase querySysPara = new YbChargeQueryDatabase();
            querySysPara.setDataStoreProject(dataStoreProject);
            querySysPara.setDataStoreVersion(dataStoreVersion);

            querySysPara.setDbtype("greenplum");

            String sql =  (new GenHiveQueryDeptTop10()).genDeoptChargeListSqlInner(searchTask,
                    querySysPara,true);
            System.out.println(sql);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
