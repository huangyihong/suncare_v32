package com.ai.modules.ybChargeSearch.service.impl;

import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.vo.YbChargeQueryDatabase;
import com.ai.modules.ybChargeSearch.vo.YbChargeQuerySql;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 生成为医保数据的收费明细
 */
public class GenHiveQueryChargeDetail extends GenHiveQueryCommon {

    /**
     * 根据条件列表生成 下载收费明细 的SQL
     * @param searchTaskList
     * @return
     * @throws Exception
     */
    public  List<YbChargeQuerySql> genSrcChargeDetailSqlList(List<YbChargeSearchTask> searchTaskList,
                                                             YbChargeQueryDatabase querySysPara) throws Exception{

        //第一步，验证并解析参数
        if(searchTaskList == null || searchTaskList.size() ==0){
            throw new Exception("查询参数不能为空！");
        }

        //先按查询条件拆分任务，前台需要按搜索关键字分别搜索
        List<YbChargeSearchTask> splitSearchList = splitTaskSearchForDetailQuery(searchTaskList);

        //返回前端的SQL列表
        ArrayList<YbChargeQuerySql> resultList = new ArrayList<YbChargeQuerySql>();

        for(int i=0; i<splitSearchList.size(); i++) {
            YbChargeSearchTask searchTask = splitSearchList.get(i);

            String sheetName =  (i+1) +"-";

            //如果以医院项目名称查询，则按医院项目名称拆分
            if(StringUtils.isNotBlank(searchTask.getItemname()) ) {
                sheetName = sheetName + searchTask.getItemname();
            }
            else if(StringUtils.isNotBlank(searchTask.getHisItemName())){
                sheetName = sheetName + searchTask.getHisItemName();
            }

            //获得查询SQL
            String querySql = genSrcChargeDetailSqlInner(searchTask, querySysPara);

            //添加到返回值
            YbChargeQuerySql sqlObj = new YbChargeQuerySql();
            sqlObj.setQuerySql(querySql);
            sqlObj.setSplitColumnName(""); //改成客户端都不拆分
            sqlObj.setSheetName(sheetName);
            sqlObj.setSearchTaskBean(searchTask);
            resultList.add(sqlObj);

            //添加月度统计，月度统计标识为1 ，并且项目名称A不为空
            if("1".equals(searchTask.getIsSearchDrug()) && (
                    StringUtils.isNotBlank(searchTask.getItemname()) || StringUtils.isNotBlank(searchTask.getHisItemName())
            )){
                //获取月度统计SQL
                String monthCountSql=genSrcMonthChargeSqlInner(searchTask, querySysPara);

                YbChargeQuerySql monthSqlObj = new YbChargeQuerySql();
                monthSqlObj.setQuerySql(monthCountSql);
                monthSqlObj.setSplitColumnName(""); //改成客户端都不拆分
                monthSqlObj.setSheetName(sheetName+"_月度用量分析");
                monthSqlObj.setSearchTaskBean(searchTask);
                monthSqlObj.setIsMonth(true);
                resultList.add(monthSqlObj);
            }


        }

        return resultList;
    }

    /**
     * 判断是否使用新方法：从临时表直接读取数据，如果该方法能返回数据，则用该方法返回的SQL，
     * 否则仍然用旧的逻辑
     * @param searchTask
     * @param dbType
     * @param dataStoreProject
     * @return
     * @throws Exception
     */
    private String getNewChargeDetailSql(YbChargeSearchTask searchTask,
                                           String dbType ,String dataStoreVersion,
                                         String dataStoreProject) throws Exception {
        System.out.println(DB_TYPE_GREENPLUM.equalsIgnoreCase(dbType));
        System.out.println(DB_TYPE_MYSQL.equalsIgnoreCase(dbType));
        //目前只处理GP的SQL
        if(DB_TYPE_GREENPLUM.equalsIgnoreCase(dbType) == false&&DB_TYPE_MYSQL.equalsIgnoreCase(dbType) == false){
            return null;
        }

        //目前只处理医保的数据
        if(ETL_SOURCE_HIS_SEARCH_PARA.equalsIgnoreCase(searchTask.getEtlSource())){
            return null;
        }

        //项目名称1是否为空
        boolean isItemName1Null = StringUtils.isBlank(searchTask.getItemname()) &&
                StringUtils.isBlank(searchTask.getHisItemName());

        //项目名称2是否为空
        boolean isItemName2Null = StringUtils.isBlank(searchTask.getItemname1()) &&
                StringUtils.isBlank(searchTask.getHisItemName1());

        if (isItemName1Null &&
                StringUtils.isBlank(searchTask.getOrgids()) && StringUtils.isBlank(searchTask.getOrgs()) &&
                StringUtils.isBlank(searchTask.getVisitid()) && StringUtils.isBlank(searchTask.getCaseId())
                && StringUtils.isBlank(searchTask.getClientname()) && StringUtils.isBlank(searchTask.getIdNo())
                && StringUtils.isBlank(searchTask.getDoctorname()) && StringUtils.isBlank(searchTask.getDoctorid())
                && searchTask.getItemChargedate() == null) {
            //至少有一个条件，不能都为空
            throw new Exception("医院名称、项目名称、就诊ID、病案号、患者姓名、身份证号码、项目收费日期、医生姓名这些查询条件不能同时为空！");
        }

        ArrayList<String> sqlIdList = new ArrayList<String>();

        //下载整个医院层级的数据,医院名称不为空，上述的其它几个条件为空
        if ( (StringUtils.isNotBlank(searchTask.getOrgids()) || StringUtils.isNotBlank(searchTask.getOrgs())) &&
                isItemName1Null &&
                StringUtils.isBlank(searchTask.getVisitid()) && StringUtils.isBlank(searchTask.getCaseId())
                && StringUtils.isBlank(searchTask.getClientname()) && StringUtils.isBlank(searchTask.getIdNo())
                && StringUtils.isBlank(searchTask.getDoctorname()) && StringUtils.isBlank(searchTask.getDoctorid())
                && searchTask.getItemChargedate() == null) {
            //添加就诊层级的项目明细数据下载
            sqlIdList.add("query_result_by_visit_lvl");
        }
        else{
            //添加第一个项目查询
            sqlIdList.add("query_first_itemname");
        }



        //如果第二个项目名称不为空，则添加第二个查询
        if ( isItemName2Null == false) {
            //为第一个结果表加索引
            sqlIdList.add("add_index_for_first_itemname");

            //查询第二个关键字
            sqlIdList.add("query_second_itemname");

            //A项目存在，B项目不存在违规
            if("notHaveB".equals(searchTask.getItem1Wgtype())){
                sqlIdList.add("query_nothaveb_weigui");
            }
            //A 项目存在，B项目存在违规
            else{
                sqlIdList.add("query_haveb_weigui");

            }
        }

        //项目名称1 不为空的前提下，如果有一次就诊数量限制、项目数量不超过住院天数* 24、
        // 项目数量不超过住院天数 + 指定次数的限制，则需要将不满足
        //条件的记录从临时表删除,
        boolean isContainVisitiCountLimit = (searchTask.getVistidQty()!=null&&searchTask.getVistidQty()>0)
                || (searchTask.getInhosQty()!=null&&searchTask.getInhosQty()>0)
                ||"inhos24_qty".equalsIgnoreCase(searchTask.getQtyType());
        if(isItemName1Null == false && isContainVisitiCountLimit == true ){

            //如果第一个结果表妹加索引，则为第一个结果表加索引
            if(sqlIdList.contains("add_index_for_first_itemname") == false) {
                sqlIdList.add("add_index_for_first_itemname");
            }

            sqlIdList.add("one_visit_item_sum_limit");
        }

        //如果项目名称1 不为空，并且需要输出同一天手术的话，则需要将当天手术信息加入结果表
        //如果项目名称1 为空，第一步结果肯定包含了手术了，所以不必对手术信息额外处理
        if(isItemName1Null == false && "1".equalsIgnoreCase(searchTask.getIsSameDay()) ){
            sqlIdList.add("add_operator_info");
        }

        //加入最终的查询SQL
        sqlIdList.add("finalQuery");
        String xmlFileName = "QueryChargeDetailYb_New.xml" ;

        String   querySql = getSqlFromXml(xmlFileName, sqlIdList,dbType);

        querySql = replaceValueForSql(querySql, searchTask, dataStoreVersion, dataStoreProject);

        return querySql;
    }
    /**
     * 根据条件 生成HIS来源的 下载收费明细 的SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genSrcChargeDetailSqlInner(YbChargeSearchTask searchTask,
                                              YbChargeQueryDatabase querySysPara) throws Exception{
        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();
        String dbType = querySysPara.getDbtype();
        //预处理查询条件
        preDealSearchTask(searchTask);


        //判断是否使用新方法：从临时表直接读取数据，如果该方法能返回数据，则用该方法返回的SQL，
        //否则仍然用旧的逻辑
        String newQuerySql = getNewChargeDetailSql(searchTask ,dbType ,
                dataStoreVersion,dataStoreProject);
        if(StringUtils.isNotEmpty(newQuerySql)){
            return newQuerySql;
        }

        //定义ID列表 detail + master + final
        ArrayList<String> sqlIdList = new ArrayList<String>();

        boolean isNoItemNameWithOriginList = false;// 没有项目名称，并且是最原始的清单查询

        //detail 定义 如果没有项目名称查询
        if (StringUtils.isBlank(searchTask.getItemname()) && StringUtils.isBlank(searchTask.getHisItemName())) {
            //就诊ID 患者姓名 病案号 出院日期 只要1个不为空，表示清单查询，直接输出最原始的明细，不做任何合并
            if(StringUtils.isNotBlank(searchTask.getVisitid()) || StringUtils.isNotBlank(searchTask.getCaseId())
                    || StringUtils.isNotBlank(searchTask.getClientname()) || StringUtils.isNotBlank(searchTask.getIdNo()) || searchTask.getItemChargedate() != null) {
                sqlIdList.add("detail_no_itemname_with_originlist");
                isNoItemNameWithOriginList = true;
            }
            else{
                sqlIdList.add("detail_no_itemname");
            }
        }
        //只有1个项目名称(第二哥项目名称为空）
        else if (StringUtils.isBlank(searchTask.getItemname1()) && StringUtils.isBlank(searchTask.getHisItemName1())) {
            sqlIdList.add("detail_with_one_itemname");
        } else {
            sqlIdList.add("detail_with_two_itemname");
        }

        //加入final
        sqlIdList.add("finalQuery");


        String xmlFileName="QueryChargeDetailYb.xml";

        if(ETL_SOURCE_HIS_SEARCH_PARA.equalsIgnoreCase(searchTask.getEtlSource())){
            xmlFileName = "QueryChargeDetailHis.xml";
        }

        String   querySql = getSqlFromXml(xmlFileName, sqlIdList,dbType);

        //针对清单查询，打开其选项
        if(isNoItemNameWithOriginList == true){
            querySql = StringUtils.replace(querySql,"--${no_itemname_with_originlist}","");
        }
        querySql = replaceValueForSql(querySql, searchTask, dataStoreVersion, dataStoreProject);

        return querySql;
    }



    /**
     * 根据条件生成 项目月度用量分析表 的SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genSrcMonthChargeSqlInner(YbChargeSearchTask searchTask,
                                             YbChargeQueryDatabase querySysPara) throws Exception {

        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();
        String dbType = querySysPara.getDbtype();

        //定义ID列表 detail  + final
        ArrayList<String> sqlIdList = new ArrayList<String>();

        //detail 定义 如果没有项目名称查询
        if (StringUtils.isBlank(searchTask.getItemname()) && StringUtils.isBlank(searchTask.getHisItemName())) {
            sqlIdList.add("detail_no_itemname");
        }
        //只有1个项目名称(第二哥项目名称为空）
        else{
            sqlIdList.add("detail_with_one_itemname");
        }

        //加入final
        sqlIdList.add("finalQuery");

        String xmlFileName="QueryMonthChargeYb.xml";
        if(ETL_SOURCE_HIS_SEARCH_PARA.equalsIgnoreCase(searchTask.getEtlSource())){
            xmlFileName = "QueryMonthChargeHis.xml";
        }

        String querySql = getSqlFromXml(xmlFileName, sqlIdList,dbType);

        querySql = replaceValueForSql(querySql, searchTask, dataStoreVersion, dataStoreProject);

        return querySql;
    }



    /**
     * 将一个查询任务按关键字拆分成多个任务
     * @param searchTaskList
     * @return
     */
    private  List<YbChargeSearchTask> splitTaskSearchForDetailQuery(List<YbChargeSearchTask> searchTaskList) throws Exception {
        List<YbChargeSearchTask> resultTaskList = new ArrayList<YbChargeSearchTask>();


        for(int i=0; i<searchTaskList.size(); i++) {
            YbChargeSearchTask searchTask = searchTaskList.get(i);

            //用来校验是否重复查询的set
            HashSet<String> judgeSet = new HashSet<String>();

            //如果存在同时收费的项目，则不拆分
            if (StringUtils.isNotBlank(searchTask.getItemname1()) || StringUtils.isNotBlank(searchTask.getHisItemName1())) {
                resultTaskList.add(searchTask);
                continue;
            }

            //如果医保项目名称不为空，则按医保项目名称拆分
            if (StringUtils.isNotBlank(searchTask.getItemname())){
                String oriItemName = searchTask.getItemname();
                oriItemName = StringUtils.replace(oriItemName ,"|" ,"#");
                String itemNames[] = StringUtils.split(oriItemName,"#");

                //如果就1个项目，直接返回
                if(itemNames.length ==1){
                    resultTaskList.add(searchTask);
                    continue;
                }

                //按项目名称拆分
                for(String itemName : itemNames){
                    if(StringUtils.isBlank(itemName) || judgeSet.contains(itemName)){
                        continue;
                    }
                    judgeSet.add(itemName);

                    YbChargeSearchTask cloneObj =  new YbChargeSearchTask();
                    BeanUtils.copyProperties(searchTask,cloneObj);

                    cloneObj.setItemname(itemName);
                    resultTaskList.add(cloneObj);
                }
            }
            //如果医院项目名称不为空，则按医院项目名称拆分
            else if (StringUtils.isNotBlank(searchTask.getHisItemName())){
                String oriHisItemName = searchTask.getHisItemName();
                oriHisItemName = StringUtils.replace(oriHisItemName ,"|" ,"#");
                String hisItemNames[] = StringUtils.split(oriHisItemName,"#");

                //如果就1个项目，直接返回
                if(hisItemNames.length ==1){
                    resultTaskList.add(searchTask);
                    continue;
                }

                //按项目名称拆分
                for(String hisItemName : hisItemNames){
                    if(StringUtils.isBlank(hisItemName) || judgeSet.contains(hisItemName)){
                        continue;
                    }
                    judgeSet.add(hisItemName);

                    YbChargeSearchTask cloneObj =  new YbChargeSearchTask();
                    BeanUtils.copyProperties(searchTask,cloneObj);
                    cloneObj.setHisItemName(hisItemName);
                    resultTaskList.add(cloneObj);
                }
            }
            else{
                resultTaskList.add(searchTask);
            }

        }

        return resultTaskList;
    }


    public static void main(String[] args) {
        try {

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            YbChargeSearchTask searchTask = new YbChargeSearchTask();
            //searchTask.setOrgs("永福县妇幼保健院");
            searchTask.setOrgids("H61102200178");
            //searchTask.setOrgids("1400002");
            searchTask.setItemname("一般专项护理");
            searchTask.setItemname1("中换药");

            searchTask.setIsFundpay("0"); // 0或者空表示只包含报销明细 ，1表示包含报销和自费
            //searchTask.setItemname("门诊诊查费");
//            searchTask.setHisItemName1("静脉输液（第一组）");
            // searchTask.setVisitid("70898888");
            //searchTask.setCaseId("3952407#12121");
            //searchTask.setClientname("蒋金文1986-08-16");
            //searchTask.setItemChargedate(format.parse("2020-01-04"));

            //searchTask.setItem1Type("once") ;//oneday 同一天 ,为空表示同一天 once同一次

            searchTask.setChargedateStartdate(format.parse("2020-01-01"));
            searchTask.setChargedateEnddate(format.parse("2022-12-31"));
            //searchTask.setVisittype("MM");
            searchTask.setOrderby("sum_fee desc");
            //searchTask.setDataStaticsLevel("ods");
            //searchTask.setEtlSource("his");
           searchTask.setEtlSource("yb");
            searchTask.setItem1Wgtype("notHaveB");
            //searchTask.setItem1Wgtype("haveB");

            //是否输出月度用药用量限制
           // searchTask.setIsSearchDrug("1");

            //一天的项目数量限制
            //searchTask.setChargeQty(1);

            //一次就诊次数不超过 vistidQty
            //searchTask.setVistidQty(4);

            //项目数量不超过住院天数 + inhosQty
            //searchTask.setInhosQty(3);

            //超过住院时间*24+QtyNum
            //searchTask.setQtyType("inhos24_qty");
            //searchTask.setQtyNum(5);

            //输出同一天的手术
            //searchTask.setIsSameDay("1");



            YbChargeSearchTask searchTask2 = new YbChargeSearchTask();
            searchTask2.setOrgs("镇巴县中医院");
            searchTask2.setOrgids("H61072800066");
            searchTask2.setChargedateStartdate(format.parse("2020-01-01"));
            searchTask2.setChargedateEnddate(format.parse("2022-12-31"));

            List<YbChargeSearchTask> searchList = new ArrayList<YbChargeSearchTask>();
            searchList.add((searchTask));

            GenHiveQueryChargeDetail obj = new GenHiveQueryChargeDetail();

            YbChargeQueryDatabase querySysPara = new YbChargeQueryDatabase();
            String dataStoreProject="shanxi";
            String dataStoreVersion="";
            querySysPara.setDataStoreProject(dataStoreProject);
            querySysPara.setDataStoreVersion(dataStoreVersion);
            querySysPara.setDbtype("greenplum");

            List<YbChargeQuerySql> list = obj.genSrcChargeDetailSqlList(searchList,querySysPara);

            System.out.println("splitCount: " + list.size());

            for(YbChargeQuerySql sqlObj : list) {
                //System.out.println("sheetname: " + sqlObj.getSheetName());
                System.out.println( sqlObj.getQuerySql());
                System.out.println("---======================");
                //System.out.println(GenHiveQuerySqlTools.getMaxCountLimit(sqlObj.getQuerySql()));
                System.out.println("---======================");


                //System.out.println(GenHiveQuerySqlTools.getCountSqlByQuerySql(sqlObj.getQuerySql()));

                //System.out.println(GenHiveQuerySqlTools.getDropTempTablSql(list));
            }

            //月度用量统计测试
            String sql = obj.genSrcMonthChargeSqlInner(searchTask,querySysPara);
            //System.out.println(sql);


        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
