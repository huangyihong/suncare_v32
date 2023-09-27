package com.ai.modules.ybChargeSearch.service.impl;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jeecg.common.util.DateUtils;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class GenHiveQueryCommon {

    protected final String FUND_PAY_ONLY = "0";  //只包含报销项目
    protected final String FUND_PAY_AND_SELF_PAY = "1";   //报销项目+自费项目

    protected final String ETL_SOURCE_DWB_YB = "A01";
    protected final String ETL_SOURCE_DWB_HIS = "A03";

    protected final String ETL_SOURCE_HIS_SEARCH_PARA = "his";  //前台查询条件中的数据来源-医院

    protected final String dataStoreVersionJiaxiang = "jx";  //嘉祥数仓版本
    protected static final String dataStoreVersionXianyang = "xy";  //咸阳数仓版本

    protected final String DATA_LEVEL_SRC = "src";  //原始层src
    protected final String DATA_LEVEL_ODS = "ods";  //ods层
    protected final String DATA_LEVEL_DWB = "dwb";  //dwb层

    protected final String DB_TYPE_GREENPLUM = "greenplum";
    protected final String DB_TYPE_MYSQL = "mysql";




    //全匹配
    protected String fullEqualForRlike(String str){
        if(StringUtils.isBlank(str)){
            return "";
        }

        str = StringUtils.replace(str,"#" ,"|");
        str = StringUtils.replace(str,"|" ,"$|^");
        str= "^" + str + "$";

        return  str;
    }
    /**
     * 预先处理查询条件
     * @param searchTask
     */
    protected void preDealSearchTask(YbChargeSearchTask searchTask) throws  Exception{
        //默认处理gp
        preDealSearchTask(searchTask ,DB_TYPE_GREENPLUM);
    }
    /**
     * 预先处理查询条件
     * @param searchTask
     */
    protected void preDealSearchTask(YbChargeSearchTask searchTask ,String dbType) throws  Exception{
        String orgName = searchTask.getOrgs();
        if(StringUtils.isNotBlank(orgName)){
            orgName = rlikeStrDeal(dbType,orgName);
            orgName = fullEqualForRlike(orgName);
            searchTask.setOrgs(orgName);
        }

        String orgId = searchTask.getOrgids();
        if(StringUtils.isNotBlank(orgId)){
            orgId = rlikeStrDeal(dbType,orgId);
            orgId = fullEqualForRlike(orgId);
            searchTask.setOrgids(orgId);
        }

        String visitIds = searchTask.getVisitid();
        if(StringUtils.isNotBlank(visitIds)){
            visitIds = rlikeStrDeal(dbType,visitIds);
            searchTask.setVisitid(visitIds);
        }

        //CASEIDS
        String caseIds = searchTask.getCaseId();
        if(StringUtils.isNotBlank(caseIds)){
            caseIds = rlikeStrDeal(dbType,caseIds);
            searchTask.setCaseId(caseIds);
        }

        //替换部门名称
        String deptName = searchTask.getDeptname();
        if(StringUtils.isNotBlank(deptName)){
            deptName = rlikeStrDeal(dbType,deptName);
            deptName = StringUtils.replace(deptName,"," ,"|");
            searchTask.setDeptname(deptName);
        }

        //替换医生姓名
        String doctorname = searchTask.getDoctorname();
        if(StringUtils.isNotBlank(doctorname)){
            doctorname = rlikeStrDeal(dbType,doctorname);
            doctorname = StringUtils.replace(doctorname,"," ,"|");
            searchTask.setDoctorname(doctorname);
        }

        //替换排除的收费项目
        String excludeItemname = searchTask.getExcludeItemname();
        if(StringUtils.isNotBlank(excludeItemname)){
            excludeItemname = rlikeStrDeal(dbType,excludeItemname);
            excludeItemname = StringUtils.replace(excludeItemname,"," ,"|");
            searchTask.setExcludeItemname(excludeItemname);
        }


        //数据层级，默认src
        String dataLevel = searchTask.getDataStaticsLevel();
        if(StringUtils.isBlank(dataLevel)){
            searchTask.setDataStaticsLevel(DATA_LEVEL_SRC);
        }

        //针对购药的处理，如果是购药，并且数据选HIS，则设置成空（变成医保）
        if("GY".equalsIgnoreCase(searchTask.getVisittype()) &&
                ETL_SOURCE_HIS_SEARCH_PARA.equalsIgnoreCase(searchTask.getEtlSource())){
            searchTask.setEtlSource("");
        }

        String clientName = searchTask.getClientname();
        if(StringUtils.isNotBlank(clientName)){
            clientName = rlikeStrDeal(dbType,clientName);
            searchTask.setClientname(clientName);
        }

        String idNo = searchTask.getIdNo();
        if(StringUtils.isNotBlank(idNo)){
            idNo = rlikeStrDeal(dbType,idNo);
            searchTask.setIdNo(idNo);
        }

        String clientid = searchTask.getClientid();
        if(StringUtils.isNotBlank(clientid)){
            clientid = rlikeStrDeal(dbType,clientid);
            searchTask.setClientid(clientid);
        }

        String itemName = searchTask.getItemname();
        if(StringUtils.isNotBlank(itemName)){
            itemName = rlikeStrDeal(dbType,itemName);
            searchTask.setItemname(itemName);
        }else{
            searchTask.setItemname("");
        }

        String itemName1 = searchTask.getItemname1();
        if(StringUtils.isNotBlank(itemName1)){
            itemName1 = rlikeStrDeal(dbType,itemName1);
            searchTask.setItemname1(itemName1);
        }else{
            searchTask.setItemname1("");
        }

        //如果第一个医保项目为空，则第二个医保项目名称必须为空
        if(StringUtils.isBlank(itemName)  && StringUtils.isNotBlank(itemName1)){
            throw  new Exception("当医保项目名称为空时，则同一天同时收费的医保项目必须为空！");
        }

        //判断一个条件是否是另外一个条件的子集，如果是，则子集的那个地方，子集那个部分改用全匹配
        if(StringUtils.isNotBlank(itemName) &&  StringUtils.isNotBlank(itemName1)){
            if(itemName.equals(itemName1)){
                throw  new Exception("一天同时收费的两个项目名称不能一致！");
            }

            int indexMulti1 = StringUtils.indexOf(itemName,"#");
            int indexMulti2 = StringUtils.indexOf(itemName1,"#");

            int indexContani1 = StringUtils.indexOf(itemName,itemName1);
            int indexContani2 = StringUtils.indexOf(itemName1,itemName);

            //itemName 包含 itemName1
            if(indexMulti1<0 && indexMulti2<0 && indexContani1>=0){
                itemName1= "^" + itemName1 + "$";
                searchTask.setItemname1(itemName1);
            }
            //itemName1 包含 itemName
            else if(indexMulti1<0 && indexMulti2<0 && indexContani2>=0){
                itemName= "^" + itemName + "$";
                searchTask.setItemname(itemName);
            }
        }

        String hisItemName = searchTask.getHisItemName();
        if(StringUtils.isNotBlank(hisItemName)){
            hisItemName = rlikeStrDeal(dbType,hisItemName);
            searchTask.setHisItemName(hisItemName);
        }else{
            searchTask.setHisItemName("");
        }

        String hisItemName1 = searchTask.getHisItemName1();
        if(StringUtils.isNotBlank(hisItemName1)){
            hisItemName1 = rlikeStrDeal(dbType,hisItemName1);
            searchTask.setHisItemName1(hisItemName1);
        }else{
            searchTask.setHisItemName1("");
        }

        //如果第一个医院项目为空，则第二个医院项目必须为空
        if(StringUtils.isBlank(hisItemName)  && StringUtils.isNotBlank(hisItemName1)){
            throw new Exception("当医院项目名称为空时，则同一天同时收费的医院项目必须为空！");
        }

        //医保项目名称查询和医院项目名称查询不能同时存在
        if(StringUtils.isNotBlank(itemName)  && StringUtils.isNotBlank(hisItemName)){
            throw new Exception("医保项目名称查询和医院项目名称查询不能同时存在！");
        }

        //判断一个条件是否是另外一个条件的子集，如果是，则子集的那个地方，子集那个部分改用全匹配
        if(StringUtils.isNotBlank(hisItemName) &&  StringUtils.isNotBlank(hisItemName1)){
            if(hisItemName.equals(hisItemName1)){
                throw  new Exception("一天同时收费的两个医院项目名称不能一致！");
            }

            int indexMulti1 = StringUtils.indexOf(hisItemName,"#");
            int indexMulti2 = StringUtils.indexOf(hisItemName1,"#");

            int indexContani1 = StringUtils.indexOf(hisItemName,hisItemName1);
            int indexContani2 = StringUtils.indexOf(hisItemName1,hisItemName);

            //itemName 包含 itemName1
            if(indexMulti1<0 && indexMulti2<0 && indexContani1>=0){
                hisItemName1= "^" + hisItemName1 + "$";
                searchTask.setHisItemName1(hisItemName1);
            }
            //itemName1 包含 itemName
            else if(indexMulti1<0 && indexMulti2<0 && indexContani2>=0){
                hisItemName= "^" + hisItemName + "$";
                searchTask.setHisItemName(hisItemName);
            }
        }

        //是否包含自费项目，如果为空，表示0，表示不包含
        if(StringUtils.isBlank(searchTask.getIsFundpay())){
            searchTask.setIsFundpay(FUND_PAY_ONLY);
        }

        //如果第一个项目为空，则设置是否输出同一天的手术为空
        if(StringUtils.isBlank(itemName) && StringUtils.isBlank(hisItemName)){
            searchTask.setIsSameDay(null);
        }

        //替换手术名称
        String surgeryName = searchTask.getSurgeryName();
        if(StringUtils.isNotBlank(surgeryName)){
            surgeryName = rlikeStrDeal(dbType,surgeryName);
            surgeryName = StringUtils.replace(surgeryName,"," ,"|");
            searchTask.setSurgeryName(surgeryName);
        }

    }

    /**
     * rlike字符串处理
     */
    private  String rlikeStrDeal(String dbType ,String rlikeStr) throws Exception{
        if(rlikeStr == null ){
            return "";
        }

        //去空格
        rlikeStr = rlikeStr.trim();
        rlikeStr = StringUtils.replace(rlikeStr,"#" ,"|");

        //GP的写法
        if(DB_TYPE_GREENPLUM.equalsIgnoreCase(dbType)){
            //将 (  需要转成 \\\(
            rlikeStr = StringUtils.replace(rlikeStr, "(" ,"\\(");
            rlikeStr = StringUtils.replace(rlikeStr, ")" ,"\\)");
            rlikeStr = StringUtils.replace(rlikeStr, "[" ,"\\[");
            rlikeStr = StringUtils.replace(rlikeStr, "]" ,"\\]");
            rlikeStr = StringUtils.replace(rlikeStr, "*" ,"\\*");
            rlikeStr = StringUtils.replace(rlikeStr, "+" ,"\\+");
            rlikeStr = StringUtils.replace(rlikeStr, "." ,"\\.");
            rlikeStr = StringUtils.replace(rlikeStr, "{" ,"\\{");
        }else{
            //以下是impala的写法
            //将 (  需要转成 \\\(
            rlikeStr = StringUtils.replace(rlikeStr, "(" ,"\\\\\\(");
            rlikeStr = StringUtils.replace(rlikeStr, ")" ,"\\\\\\)");
            rlikeStr = StringUtils.replace(rlikeStr, "[" ,"\\\\\\[");
            rlikeStr = StringUtils.replace(rlikeStr, "]" ,"\\\\\\]");
            rlikeStr = StringUtils.replace(rlikeStr, "*" ,"\\\\*");
            rlikeStr = StringUtils.replace(rlikeStr, "+" ,"\\\\+");
            rlikeStr = StringUtils.replace(rlikeStr, "." ,"\\\\.");
            rlikeStr = StringUtils.replace(rlikeStr, "{" ,"\\{");
        }


        //&& 表示前台查询通配符，查询的时候需要替换成.*
        rlikeStr = StringUtils.replace(rlikeStr, "&&" ,".*");
        return rlikeStr;

    }

    protected void preDealSearchTask(List<YbChargeSearchTask> searchTaskList) throws  Exception{
        for(YbChargeSearchTask searchTask : searchTaskList){
            preDealSearchTask(searchTask);
        }
    }

    /**
     * 替换SQL语句中的统计层
     * @param querySql
     * @param searchTask
     * @return
     */
    protected String convertDataStaticLevel(String querySql  , YbChargeSearchTask searchTask, String dataStoreVersion){
        String dataStaticLevel = searchTask.getDataStaticsLevel();

        if(DATA_LEVEL_ODS.equalsIgnoreCase(dataStaticLevel)  ) {

            querySql = StringUtils.replace(querySql,"src_his_mz_settlement","ods_his_mz_settlement");
            querySql = StringUtils.replace(querySql,"src_his_mz_master_info","ods_his_mz_master_info");
            querySql = StringUtils.replace(querySql,"src_his_mz_charge_detail","ods_his_mz_charge_detail");
            querySql = StringUtils.replace(querySql,"src_his_mz_diag","ods_his_mz_diag");

            querySql = StringUtils.replace(querySql,"src_his_zy_settlement","ods_his_zy_settlement");
            querySql = StringUtils.replace(querySql,"src_his_zy_master_info","ods_his_zy_master_info");
            querySql = StringUtils.replace(querySql,"src_his_zy_charge_detail","ods_his_zy_charge_detail");
            querySql = StringUtils.replace(querySql,"src_his_zy_diag","ods_his_zy_diag");

            querySql = StringUtils.replace(querySql,"src_yb_settlement","ods_yb_settlement");
            querySql = StringUtils.replace(querySql,"src_yb_master_info","ods_yb_master_info");
            querySql = StringUtils.replace(querySql,"src_yb_charge_detail","ods_yb_charge_detail");
            querySql = StringUtils.replace(querySql,"src_yb_diag","ods_yb_diag");
        }

        //嘉祥HIS对自费判断特殊处理
        if(dataStoreVersionJiaxiang.equalsIgnoreCase( dataStoreVersion) &&
                ETL_SOURCE_HIS_SEARCH_PARA.equalsIgnoreCase(searchTask.getEtlSource())){
            querySql = StringUtils.replace(querySql,"fundpay>0","payway not rlike '自费'");
        }

        return querySql;
    }

    /**
     * 根据 searchTask对象，替换查询sql
     * @param querySql
     * @param searchTask
     * @return
     */
    public String replaceValueForSql(String querySql ,YbChargeSearchTask searchTask ,
                                     String dataStoreVersion ,  String dataStoreProject){
        //获取当前SQL的执行的SEQ
        String sqlSeq = GenHiveQuerySqlTools.getCurrentSqlSeq();
        querySql = StringUtils.replace(querySql,"${sqlSeq}" ,sqlSeq);

        //最大条数限制
        querySql = StringUtils.replace(querySql,"${limitCount}" ,"1000010");

        //机构替换
        String orgName = searchTask.getOrgs();
        if(StringUtils.isNotBlank(orgName)){
            querySql = StringUtils.replace(querySql,"${orgname}" ,orgName);
        }

//        String orgId = searchTask.getOrgids();
        String orgIds = searchTask.getOrgids();
        String orgId="";
        if(StrUtil.isNotEmpty(orgIds)){
            String[] arrList=orgIds.replace("^","")
                    .replace("$","")
                    .split("\\|");
            for(String org:arrList){
                orgId +="'"+org+"'"+",";
            }
            orgId=orgId.substring(0,orgId.length()-1);
        }

        if(StringUtils.isNotBlank(orgId)){
            querySql = StringUtils.replace(querySql,"${orgid}" ,orgId);
            querySql = StringUtils.replace(querySql,"--${NOORGID}" ,"--");
        }
        else{
            querySql = StringUtils.replace(querySql,"--${NOORGID}" ,"");
        }
        //就诊关键字替换
        String diseasename = searchTask.getDiseasename();
        if(StringUtils.isNotBlank(diseasename)){
            querySql = StringUtils.replace(querySql,"${diseasename}" ,diseasename);
            querySql = StringUtils.replace(querySql,"--${hasDiseasename}" ,"");
        }else{
            querySql = StringUtils.replace(querySql,"--${NotDiseasename}" ,"");
        }

        //就诊类型
        String visittype = searchTask.getVisittype();
        //就诊类型--住院
        if("ZY".equalsIgnoreCase(visittype)){
            querySql = StringUtils.replace(querySql,"${ZY}" ,"住院");
            querySql = StringUtils.replace(querySql,"${NOTGY}" ,"");
        }
        //就诊类型  门慢 == 非住院
        else if("MM".equalsIgnoreCase(visittype)){
            querySql = StringUtils.replace(querySql,"${MM}" ,"门慢");
            querySql = StringUtils.replace(querySql,"${NOTGY}" ,"");
        }
        //购药
        else if("GY".equalsIgnoreCase(visittype)){
            querySql = StringUtils.replace(querySql,"${GY}" ,"购药");
        }
        //住院加门慢
        else{
            querySql = StringUtils.replace(querySql,"${ZY+MM}" ,"住院+门慢");
            querySql = StringUtils.replace(querySql,"${NOTGY}" ,"");
        }

        //visitId 替换
        String visitId = searchTask.getVisitid();
        if(StringUtils.isNotBlank(visitId)){
            querySql = StringUtils.replace(querySql,"${visitid}" ,visitId);

            String visitid_in = "";
            String[] arrList=visitId.replace("^","")
                    .replace("$","")
                    .split("\\|");
            for(String tmpStr:arrList){
                visitid_in +="'" + tmpStr + "'"+",";
            }
            visitid_in=visitid_in.substring(0,visitid_in.length()-1);
            querySql = StringUtils.replace(querySql,"${visitid_in}" ,visitid_in);
        }

        //caseId 替换
        String caseId = searchTask.getCaseId();
        if(StringUtils.isNotBlank(caseId)){
            querySql = StringUtils.replace(querySql,"${case_id}" ,caseId);
        }
        //clientName 替换
        String clientName = searchTask.getClientname();
        if(StringUtils.isNotBlank(clientName)){
            querySql = StringUtils.replace(querySql,"${clientname}" ,clientName);
        }

        //idNo 替换
        String idNo = searchTask.getIdNo();
        if(StringUtils.isNotBlank(idNo)){
            querySql = StringUtils.replace(querySql,"${idNo}" ,idNo);

            String idNo_in = "";
            String[] arrList=idNo.replace("^","")
                    .replace("$","")
                    .split("\\|");
            for(String tmpStr:arrList){
                idNo_in +="'" + tmpStr + "'"+",";
            }
            idNo_in=idNo_in.substring(0,idNo_in.length()-1);
            querySql = StringUtils.replace(querySql,"${idNo_in}" ,idNo_in);

        }

        //clientid 替换
        String clientid = searchTask.getClientid();
        if(StringUtils.isNotBlank(clientid)){
            querySql = StringUtils.replace(querySql,"${clientid}" ,clientid);
        }

        //同一次就诊同时收费，还是同一天就诊同时收费
        if(StringUtils.isBlank(searchTask.getItem1Type()) || !"once".equalsIgnoreCase(searchTask.getItem1Type())) {
            querySql = StringUtils.replace(querySql,"--${ONE_DAY}" ,"");
        }

        //isFund 替换 ，只有只包含医保报销金额的时候，才会替换变量
        // 1表示只包含报销明细 ，0或者空表示包含报销和自费
        String isFund = searchTask.getIsFundpay();
        if(FUND_PAY_ONLY.endsWith(isFund)){
            querySql = StringUtils.replace(querySql,"${fundpayonly}" ,"");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //itemChargedate 替换
        Date itemChargedate = searchTask.getItemChargedate();
        if(itemChargedate != null) {
            String itemChargedateStr = dateFormat.format(itemChargedate);
            querySql = StringUtils.replace(querySql,"${itemChargedateStart}" ,itemChargedateStr);
            querySql = StringUtils.replace(querySql,"${itemChargedateEnd}" ,itemChargedateStr+ " 23:59:59.59");
        }

        //leaveDate 替换
        Date leaveDate = searchTask.getLeavedate();
        if(leaveDate != null) {
            String leaveDateStr = dateFormat.format(leaveDate);
            querySql = StringUtils.replace(querySql,"${leaveDateStart}" ,leaveDateStr);
            querySql = StringUtils.replace(querySql,"${leaveDateEnd}" ,leaveDateStr+ " 23:59:59.59");
        }

        //时间范围-开始时间替换
        Date chargeStartDate = searchTask.getChargedateStartdate() ;
        if(chargeStartDate != null){
            String  chargeStartDateStr = dateFormat.format(chargeStartDate);
            querySql = StringUtils.replace(querySql,"${dateStart}" ,chargeStartDateStr);

            String startYear = chargeStartDateStr.substring(0,4);
            querySql = StringUtils.replace(querySql,"${yearStart}" ,startYear);
        }

        //时间范围-结束时间替换
        Date chageEndDate = searchTask.getChargedateEnddate() ;
        if(chageEndDate != null){
            String  chageEndDateStr = dateFormat.format(chageEndDate)+ " 23:59:59.0";
            querySql = StringUtils.replace(querySql,"${dateEnd}" ,chageEndDateStr);

            String endYear = chageEndDateStr.substring(0,4) + "-12-31 23:59:59.59";
            querySql = StringUtils.replace(querySql,"${yearEnd}" ,endYear);
        }

        //年月统计替换
        String isSearchDrug = searchTask.getIsSearchDrug();
        if(StringUtils.isNotBlank(isSearchDrug) &&isSearchDrug.equals("1") && ObjectUtil.isNotEmpty(chargeStartDate) && ObjectUtil.isNotEmpty(chageEndDate)){
            String monthCountSql = getMonthCountSql(dateFormat.format(chargeStartDate), dateFormat.format(chageEndDate));
            if(StringUtils.isNotBlank(monthCountSql)) {
                querySql = StringUtils.replace(querySql,"--${month_count}" ,monthCountSql);
            }
        }


        //医保项目名称替换
        String itemName = searchTask.getItemname();
        if(StringUtils.isNotBlank(itemName)){
            querySql = StringUtils.replace(querySql,"${itemname}" ,itemName);
        }

        String itemName1 = searchTask.getItemname1();
        if(StringUtils.isNotBlank(itemName1)){
            querySql = StringUtils.replace(querySql,"${itemname1}" ,itemName1);
        }

        //合并的医保项目名称
        String itemNameAll = "";
        if(StringUtils.isNotBlank(itemName) && StringUtils.isNotBlank(itemName1)){
            itemNameAll = itemName + "|" + itemName1;
        }
        if(StringUtils.isNotBlank(itemNameAll)){
            querySql = StringUtils.replace(querySql,"${itemname_all}" ,itemNameAll);
        }


        //医院项目名称替换
        String hisItemName = searchTask.getHisItemName();
        if(StringUtils.isNotBlank(hisItemName)){
            querySql = StringUtils.replace(querySql,"${his_itemname}" ,hisItemName);
        }

        String hisItemName1 = searchTask.getHisItemName1();
        if(StringUtils.isNotBlank(hisItemName1)){
            querySql = StringUtils.replace(querySql,"${his_itemname1}" ,hisItemName1);
        }

        //合并的医院项目名称
        String hisItemNameAll = "";
        if(StringUtils.isNotBlank(hisItemName) && StringUtils.isNotBlank(hisItemName1)){
            hisItemNameAll = hisItemName + "|" + hisItemName1;
        }
        if(StringUtils.isNotBlank(hisItemNameAll)){
            querySql = StringUtils.replace(querySql,"${his_itemname_all}" ,hisItemNameAll);
        }


        //替换在院日期
        String inHospitalDate = searchTask.getInHospitalDate();
        if(StringUtils.isNotBlank(inHospitalDate)){
            //截取前10位
            if(inHospitalDate.length()>10){
                inHospitalDate = inHospitalDate.substring(0,10);
            }
            querySql = StringUtils.replace(querySql,"${inHospitalDate}" ,inHospitalDate);
        }

        //替换project
        if(StringUtils.isNotBlank(dataStoreProject)){
            dataStoreProject = StringUtils.replace(dataStoreProject,"__gp" ,"");
            querySql = StringUtils.replace(querySql,"${project}" ,dataStoreProject);
        }

        //替换部门名称
        String deptName = searchTask.getDeptname();
        if(StringUtils.isNotBlank(deptName)){
            querySql = StringUtils.replace(querySql,"${deptname}" ,deptName);
        }

        //替换etl_source
        String dwbEtlSource = ETL_SOURCE_DWB_YB;
        if(ETL_SOURCE_HIS_SEARCH_PARA.equalsIgnoreCase(searchTask.getEtlSource())){
            dwbEtlSource = ETL_SOURCE_DWB_HIS;
        }
        if(StringUtils.isNotBlank(dwbEtlSource)){
            querySql = StringUtils.replace(querySql,"${etl_source}" ,dwbEtlSource);
        }

        //标签名称 替换
        String tagName = searchTask.getTagName();
        if(StringUtils.isNotBlank(tagName)){
            querySql = StringUtils.replace(querySql,"${tagName}" ,tagName);
        }else{
            querySql = StringUtils.replace(querySql,"${no_tagName}" ," ");
        }

        //替换排序
        String orderBy = searchTask.getOrderby();
        if(StringUtils.isNotBlank(orderBy)){
            querySql = StringUtils.replace(querySql,"${orderby}" ,orderBy);
        }

        //替换--${ods_checkorg_list} 组织机构表的几个字段
        boolean isIncludeOdsCheckOrgList = false; //是否包含组织机构表条件
        String owntype = searchTask.getOwntype();
        if(StringUtils.isNotBlank(owntype)){
            isIncludeOdsCheckOrgList= true;
            querySql = StringUtils.replace(querySql,"${owntype}" ,owntype);
        }

        String localTag = searchTask.getLocalTag();
        if(StringUtils.isNotBlank(localTag)){
            isIncludeOdsCheckOrgList= true;
            querySql = StringUtils.replace(querySql,"${localTag}" ,localTag);
        }

        String hosplevel = searchTask.getHosplevel();
        if(StringUtils.isNotBlank(hosplevel)){
            isIncludeOdsCheckOrgList= true;
            querySql = StringUtils.replace(querySql,"${hosplevel}" ,hosplevel);
        }

        //医保金额比较符和金额
        String fundValType = searchTask.getFundValType();
        String maxAllfundPay = searchTask.getMaxAllfundPay();
        if(StringUtils.isNotBlank(fundValType) && StringUtils.isNotBlank(maxAllfundPay)){
            isIncludeOdsCheckOrgList= true;
            querySql = StringUtils.replace(querySql,"${fundValType}" ,fundValType);
            querySql = StringUtils.replace(querySql,"${maxAllfundPay}" ,maxAllfundPay);
        }

        if(isIncludeOdsCheckOrgList == true){
            querySql = StringUtils.replace(querySql,"${ods_checkorg_list}" ,"");
        }


        //打开ods层开关
        String dataStaticLevel = searchTask.getDataStaticsLevel();
        if(DATA_LEVEL_SRC.equalsIgnoreCase(dataStaticLevel)  ) {
            querySql = StringUtils.replace(querySql,"--${SRC}" ,"");
        }
        else if(DATA_LEVEL_ODS.equalsIgnoreCase(dataStaticLevel)  ) {
            querySql = StringUtils.replace(querySql,"--${ODS}" ,"");
        }
        else if(DATA_LEVEL_DWB.equalsIgnoreCase(dataStaticLevel)  ) {
            querySql = StringUtils.replace(querySql,"--${DWB}" ,"");
        }

        //针对明细查询的几个特殊替换，判断是否需要计算一次就诊的数量
        if((searchTask.getVistidQty()!=null&&searchTask.getVistidQty()>0)
                || (searchTask.getInhosQty()!=null&&searchTask.getInhosQty()>0)
                ||"inhos24_qty".equalsIgnoreCase(searchTask.getQtyType()) ){
            querySql = StringUtils.replace(querySql,"--${VISIT_ITEM_QRY}" ,"");
        }
        else{
            querySql = StringUtils.replace(querySql,"--${NO_VISIT_ITEM_QRY}" ,"");
        }
        //项目数量不超过住院天数* 24
        if("inhos24_qty".equalsIgnoreCase(searchTask.getQtyType()) && ObjectUtil.isNotEmpty(searchTask.getQtyNum()) && searchTask.getQtyNum()>0){
            querySql = StringUtils.replace(querySql,"${inhos24_qty}" ,searchTask.getQtyNum() +"");
        }
        //一次就诊次数不超过 vistidQty
        if(searchTask.getVistidQty()!=null&&searchTask.getVistidQty()>0){
            querySql = StringUtils.replace(querySql,"${vistidQty}" ,searchTask.getVistidQty() +"");
        }
        ///项目数量不超过住院天数 + inhosQty
        if(searchTask.getInhosQty()!=null&&searchTask.getInhosQty()>0){
            querySql = StringUtils.replace(querySql,"${inhosQty}" ,searchTask.getInhosQty() +"");
        }

        //项目一天的数量不超过 chargeQry
        int chargeQry = searchTask.getChargeQty()!=null?searchTask.getChargeQty():0;
        if (chargeQry>0){
            querySql = StringUtils.replace(querySql,"${itemcount}" ,chargeQry+"");
        }


        //是否查询手术
        if("1".equalsIgnoreCase(searchTask.getIsSameDay()) &&
                ( StringUtils.isNotBlank(searchTask.getItemname())
                        || StringUtils.isNotBlank(searchTask.getHisItemName())
                )){
            querySql = StringUtils.replace(querySql,"--${OPERTE_QRY}" ,"");
        }else{
            querySql = StringUtils.replace(querySql,"--${NO_OPERTE_QRY}" ,"");
        }

        //重复收费项目违规类型
        if("notHaveB".equals(searchTask.getItem1Wgtype())){
            querySql = StringUtils.replace(querySql,"--${NOTHAVEB}" ,"");
        }else{
            querySql = StringUtils.replace(querySql,"--${HAVEB}" ,"");
        }

        //就诊时间范围
        if(StringUtils.isNotBlank(searchTask.getVisitdate())) {
            String visitdate = searchTask.getVisitdate();
            String startVisitdate = visitdate;
            String endVisitdate = visitdate;
            if(visitdate.length()==4) {
                //表示年份
                startVisitdate = visitdate.concat("-01-01");
                endVisitdate = visitdate.concat("-12-31 23:59:59");
            } else if(visitdate.length()==7) {
                //表示月份
                startVisitdate = visitdate.concat("-01");
                try {
                    DateFormat dfYmd = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(DateUtils.parseDate(startVisitdate, "yyyy-MM-dd"));
                    cal.add(Calendar.MONTH, 1); // 加一个月
                    cal.set(Calendar.DAY_OF_MONTH, 1); // 设置为该月第一天
                    cal.add(Calendar.DATE, -1);// 再减一天即为上个月最后一天
                    endVisitdate = dfYmd.format(cal.getTime());
                } catch(Exception e) {}
                endVisitdate = endVisitdate.concat(" 23:59:59");
            } else {
                endVisitdate = visitdate.concat(" 23:59:59");
            }
            querySql = StringUtils.replace(querySql,"${startVisitdate}", startVisitdate);
            querySql = StringUtils.replace(querySql,"${endVisitdate}", endVisitdate);
        }

        //医生
        if(StringUtils.isNotBlank(searchTask.getDoctorid())) {
            querySql = StringUtils.replace(querySql,"${doctorid}", searchTask.getDoctorid());
        }
        if(StringUtils.isNotBlank(searchTask.getDoctorname())) {
            querySql = StringUtils.replace(querySql,"${doctorname}", searchTask.getDoctorname());
        }
        //手术
        if(StringUtils.isNotBlank(searchTask.getSurgeryName())) {
            querySql = StringUtils.replace(querySql,"${surgery_name}", searchTask.getSurgeryName());
        }

        //排除收费清单中包含的项目
        String exclude_itemname = searchTask.getExcludeItemname();
        if(StringUtils.isBlank(exclude_itemname)){
            //没有排除条件，需要注释相关代码块
            querySql = StringUtils.replace(querySql,"--${none_exclude_itemname}", "");
        }
        else {
            querySql = StringUtils.replace(querySql,"${exclude_itemname}", exclude_itemname);
        }

        //删除SQL语句中没有被替换的变量
        ArrayList<String> sqlList = new ArrayList<String>();

        String tmpSqls[] = StringUtils.split(querySql ,"\n");


        for(int i=0 ;i<tmpSqls.length ; i++){
            String tmpSql = tmpSqls[i];

            //如果有变量没替换，说明需要注释改行
            if(StringUtils.indexOf(tmpSql,"${") >=0 && tmpSql.indexOf("}") >0){

                //TODO 调试完毕注释
                //tmpSql = "  --" + tmpSql;
                //sqlList.add(tmpSql);
                continue;
            }

            if(i!=0 && tmpSql.startsWith("--") ){
                tmpSql = "\n" + tmpSql;
            }
            sqlList.add(tmpSql);
        }
        querySql = StringUtils.join(sqlList ,"\n");


        //计算SQL统计所属层次
        querySql = convertDataStaticLevel(querySql , searchTask ,dataStoreVersion);
        return querySql;
    }


    /**
     * 根据sqlId从配置文件获取SQL
     * @param xmlFileName
     * @param sqlId
     * @return
     * @throws Exception
     */
    public  String getSqlFromXml(String xmlFileName ,String sqlId,String dbType) throws Exception {
        List<String> sqlIdList = new ArrayList<String>();
        sqlIdList.add(sqlId);

        return getSqlFromXml(xmlFileName ,sqlIdList,dbType);
    }
    /**
     * 根据sqlId从配置文件获取SQL
     * @param xmlFileName
     * @param sqlIds
     * @return
     * @throws Exception
     */
    public  String getSqlFromXml(String xmlFileName ,String[] sqlIds,String dbType) throws Exception {
        List<String> sqlIdList = new ArrayList<String>();
        for(String sqlId :sqlIds) {
            sqlIdList.add(sqlId);
        }
        return getSqlFromXml(xmlFileName ,sqlIdList,dbType);
    }

    /**
     * 根据sqlId列表，从配置文件按id顺序获取SQL，并拼接返回
     * @param xmlFileName
     * @param sqlIdList
     * @return
     * @throws Exception
     */
    public  String getSqlFromXml(String xmlFileName ,List<String> sqlIdList,String dbType) throws Exception {
        if(DB_TYPE_GREENPLUM.equalsIgnoreCase(dbType)){
            xmlFileName = "GP_"  + xmlFileName;
        }else if(DB_TYPE_MYSQL.equalsIgnoreCase(dbType)){
            xmlFileName = "mysql/"  + xmlFileName;
        }
        String classpath = "/com/ai/modules/ybChargeSearch/querysql/" + xmlFileName ;
        if(classpath.toLowerCase().endsWith(".xml") == false){
            classpath = classpath + ".xml";
        }

        InputStream is =GenHiveQueryCommon.class.getResourceAsStream(classpath);
        if(is == null){
            throw new Exception("找不到XML文件:" + classpath);
        }
        // 创建SAXReader的对象reader
        SAXReader reader = new SAXReader();
        // 通过reader对象的read方法加载xml文件，获取docuemnt对象
        Document document = reader.read(is);
        // 通过document对象获取根节点fields
        Element root = document.getRootElement();
        // 通过element对象的elementIterator方法获取迭代器
        Iterator<?> it = root.elementIterator();

        HashMap<String ,String> sqlMap = new HashMap<String ,String>();
        // 遍历迭代器，获取根节点中的字段
        while (it.hasNext()) {
            Element element = (Element) it.next();
            String id = element.attributeValue("id");
            String sql = element.getText();
            sqlMap.put(id ,sql);
        }

        //按sqlID顺序获取SQL ，并拼接
        String finalSql = "";
        for(String sqlId :sqlIdList){
            String sql = sqlMap.get(sqlId);

            if(sql == null){
                throw new Exception("XML文件中找不到" + sqlId +"的定义，XML地址:" + classpath);
            }

            finalSql = finalSql + sql;
        }
        return finalSql;
    }


    /**
     * 获取两个日期之间的所有月份 (年月)
     *
     * @param startTime
     * @param endTime
     * @return：list
     */
    public static List<String> getMonthBetweenDate(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 声明保存日期集合
        List<String> list = new ArrayList<>();
        try {
            // 转化成日期类型
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);

            //用Calendar 进行日期比较判断
            Calendar calendar = Calendar.getInstance();
            while (startDate.getTime() <= endDate.getTime()) {

                // 把日期添加到集合
                list.add(sdf.format(startDate));

                // 设置日期
                calendar.setTime(startDate);

                //把月数增加 1
                calendar.add(Calendar.MONTH, 1);

                // 获取增加后的日期
                startDate = calendar.getTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取年月统计列SQL
     * @return：sql
     */
    public static String getMonthCountSql(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月");
        List<String> ymList = getMonthBetweenDate(startTime, endTime);
        StringBuffer sql = new StringBuffer();
        for(String ym:ymList){
            String format = sdf.format(DateUtil.parse(ym));
            String format2 = sdf2.format(DateUtil.parse(ym));
            sql.append("sum( ");
            sql.append(" case charge when ");
            sql.append("'");
            sql.append(format);
            sql.append("'");
            sql.append(" then sl else 0 end ");
            sql.append(" ) as ");
//            sql.append("'");
            sql.append("y"+format2);
//            sql.append("'");
            sql.append(",");
        }
        return sql.toString();
    }


    public static void main(String[] args) {
        try{
            GenHiveQueryCommon obj = new GenHiveQueryCommon();
            String sql = obj.getSqlFromXml("RULE016","town","mysql");
            System.out.println(sql);
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
