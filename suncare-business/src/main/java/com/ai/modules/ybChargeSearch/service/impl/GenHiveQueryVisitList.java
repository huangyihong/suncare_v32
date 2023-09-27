package com.ai.modules.ybChargeSearch.service.impl;


import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.vo.YbChargeQueryDatabase;

import java.text.SimpleDateFormat;

/**
 * 下载门慢清单
 */
public class GenHiveQueryVisitList extends GenHiveQueryCommon{

    /**
     * 根据条件，生成 住院、门慢 清单 的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public String genVisitListSqlInner(YbChargeSearchTask searchTask,
                                       YbChargeQueryDatabase querySysPara) throws Exception{
        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();

        ///预处理查询条件
        preDealSearchTask(searchTask);

        String sqlId="querySql_YB";

        //来源于HIS数据
        if(ETL_SOURCE_HIS_SEARCH_PARA.equalsIgnoreCase(searchTask.getEtlSource())){
            sqlId = "querySql_HIS";
        }

        String xmlFileName="QueryVisitList.xml";
        String dbType = querySysPara.getDbtype();

        String querySql =getSqlFromXml(xmlFileName,sqlId,dbType);

        querySql = replaceValueForSql(querySql ,searchTask, dataStoreVersion , dataStoreProject);

        return querySql;
    }

    public static void main(String[] args) {
        try {


            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            YbChargeSearchTask searchTask = new YbChargeSearchTask();
            searchTask.setOrgs("镇巴县中医院");
            searchTask.setOrgids("H61072800066");
            searchTask.setClientname("陈思雨");
            searchTask.setChargedateStartdate(format.parse("2020-02-04"));
            searchTask.setChargedateEnddate(format.parse("2023-02-04"));
//            searchTask.setVisittype("zy");
//            searchTask.setVisittype("MM");
            //searchTask.setDataStaticsLevel("ods");
            searchTask.setEtlSource("yb");
            //searchTask.setIsFundpay("1");
            String dataStoreVersion ="";
            String dataStoreProject = "shanxi";
            searchTask.setTagName("aa");
            searchTask.setExcludeItemname("A#B#C");
            YbChargeQueryDatabase querySysPara = new YbChargeQueryDatabase();
            querySysPara.setDataStoreProject(dataStoreProject);
            querySysPara.setDataStoreVersion(dataStoreVersion);

            querySysPara.setDbtype("greenplum");

            String sql = (new GenHiveQueryVisitList()).genVisitListSqlInner(searchTask, querySysPara);

            System.out.println(sql);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
