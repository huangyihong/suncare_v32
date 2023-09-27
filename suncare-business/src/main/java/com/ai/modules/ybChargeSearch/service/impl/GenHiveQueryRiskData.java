package com.ai.modules.ybChargeSearch.service.impl;


import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.vo.YbChargeQueryDatabase;
import com.ai.modules.ybChargeSearch.vo.YbChargeSearchConstant;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;

/**
 * 医院内部超标准收费
 */
public class GenHiveQueryRiskData extends GenHiveQueryCommon{

    /**
     * 低标准入院--DwbVisitTag
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genDwbVisitTagSql(YbChargeSearchTask searchTask,
                                     YbChargeQueryDatabase querySysPara, String tagId) throws Exception {

        String sqlId = "dwb_visitid_tag";
        return innerGenPatientRisk(searchTask ,querySysPara ,tagId ,sqlId);
    }

    /**
     * 医院内部超标准收费  --datamining_chargeitem_risk_data
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genChargeitemRiskDataSql(YbChargeSearchTask searchTask,
                                            YbChargeQueryDatabase querySysPara, String tagId) throws Exception {

        String sqlId = "datamining_chargeitem_risk_data";
        return innerGenPatientRisk(searchTask ,querySysPara ,tagId ,sqlId);
    }


    /**
     * 根据项目地查询datamining_chargeitem_risk_data表获取标签ID和名称
     * @param querySysPara 项目地
     * @return
     * @throws Exception
     */
    public  String genTagInfoForChargeItemSql(YbChargeQueryDatabase querySysPara,YbChargeSearchTask searchTask) throws Exception {
       /* String sqlId = "datamining_chargeitem_tagInfo";
        if(YbChargeSearchConstant.RISK_STATISTICS.equalsIgnoreCase(taskType)){
            sqlId = "datamining_chargeitem_tagInfo";

        }
        else if(YbChargeSearchConstant.DIAG_RISK_STATISTICS.equalsIgnoreCase(taskType)){
            sqlId = "datamining_diag_tagInfo";
        }*/
        String sql="";
        String taskType = searchTask.getTaskType();
        if(YbChargeSearchConstant.VISIT_STATISTICS.equals(taskType)
                || YbChargeSearchConstant.ITEM_STATISTICS.equals(taskType)
                || YbChargeSearchConstant.ITEM_BY_DEPTSTATISTICS.equals(taskType)
                || YbChargeSearchConstant.ITEM_BY_VISIT_STATISTICS.equals(taskType)
                || YbChargeSearchConstant.DEPT_STATISTICS.equals(taskType)
        ) {
            sql = genTagListSql(querySysPara, searchTask);
        }else if(YbChargeSearchConstant.PATIENT_RISK_GROUP_STATISTICS.equals(taskType)
                ||YbChargeSearchConstant.PATIENT_RISK_STATISTICS.equals(taskType)){
            String sqlId = "tagInfo_patient";
            sql= innerGenSql(searchTask,querySysPara,"" ,sqlId);
        }else if(YbChargeSearchConstant.DOCTOR_RISK_GROUP_STATISTICS.equals(taskType)
                ||YbChargeSearchConstant.DOCTOR_RISK_STATISTICS.equals(taskType)){
            String sqlId = "tagInfo_doctor";
            sql= innerGenSql(searchTask,querySysPara,"" ,sqlId);
        }else{
            String sqlId = "tagInfo_all";
            sql= innerGenSql(searchTask,querySysPara,"" ,sqlId);
        }
        return sql;

    }

    /**
     * 根据项目地获取数据标签列表
     * @param querySysPara 项目地
     * @return
     * @throws Exception
     */
    public  String genTagListSql(YbChargeQueryDatabase querySysPara,YbChargeSearchTask searchTask) throws Exception {
        String taskType = searchTask.getTaskType();
        String sqlId = "";
        //住院及门慢清单
        if(YbChargeSearchConstant.VISIT_STATISTICS.equals(taskType)){
            sqlId="dwb_visitid_tag";
        }
        //医保收费清单按医院汇总
        if(YbChargeSearchConstant.ITEM_STATISTICS.equals(taskType)){
            sqlId="datamining_chargeitem_risk_data";
        }
        //医保收费清单按科室汇总
        if(YbChargeSearchConstant.ITEM_BY_DEPTSTATISTICS.equals(taskType)){
            sqlId="datamining_chargeitem_risk_data";
        }
        //医保收费清单按就诊汇总
        if(YbChargeSearchConstant.ITEM_BY_VISIT_STATISTICS.equals(taskType)){
            sqlId="dwb_visitid_tag";
        }

        //科室金额前10名
        if(YbChargeSearchConstant.DEPT_STATISTICS.equals(taskType)){
            sqlId="dwb_visitid_tag";
        }

        return labelGenSql(searchTask,querySysPara,"" ,sqlId);
    }


    /**
     * 诊断汇总数据异常  --datamining_diag_risk_data
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genDiagRiskDataSql(YbChargeSearchTask searchTask,
                                      YbChargeQueryDatabase querySysPara,String tagId) throws Exception {
        String sqlId = "datamining_diag_risk_data";
        return innerGenPatientRisk(searchTask ,querySysPara ,tagId ,sqlId);
    }

    /**
     * 医院总量异常  --datamining_org_risk_data
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genOrgRiskDataSql(YbChargeSearchTask searchTask,
                                      YbChargeQueryDatabase querySysPara,String tagId) throws Exception {
        String sqlId = "datamining_org_risk_data";
        return innerGenPatientRisk(searchTask ,querySysPara ,tagId ,sqlId);
    }

    /**
     * 结伴就医  --datamining_visit_together
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genVisitTogetherDataSql(YbChargeSearchTask searchTask,
                                     YbChargeQueryDatabase querySysPara,String tagId) throws Exception {
        String sqlId = "datamining_visit_together";
        return innerGenPatientRisk(searchTask ,querySysPara ,tagId ,sqlId);
    }

    /**
     * 医院内部超标准收费
     * @param searchTask
     * @return
     * @throws Exception
     */
    private  String innerGenSql(YbChargeSearchTask searchTask,
                                YbChargeQueryDatabase querySysPara,String tagId ,String sqlId) throws Exception {
        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();

        //预处理查询条件
        preDealSearchTask(searchTask);

        String xmlFileName="QueryRiskData.xml";
        String dbType = querySysPara.getDbtype();

        String querySql =getSqlFromXml(xmlFileName ,sqlId,dbType);

        //替换算法名称
        querySql = StringUtils.replace(querySql,"${tag_id}" ,tagId);
        if(YbChargeSearchConstant.ORG_HOLIDAY_RISK_STATISTICS.equals(searchTask.getTaskType())){
            querySql = StringUtils.replace(querySql,"${abnormal_distince}" ,"2");
        }else{
            querySql = StringUtils.replace(querySql,"${is_org_holiday_risk}" ,"");
        }
        querySql = replaceValueForSql(querySql ,searchTask, dataStoreVersion , dataStoreProject);

        return querySql;
    }

    /**
     * 数据标签sql
     * @param searchTask
     * @return
     * @throws Exception
     */
    private  String labelGenSql(YbChargeSearchTask searchTask,
                                YbChargeQueryDatabase querySysPara,String tagId ,String sqlId) throws Exception {
        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();

        //预处理查询条件
        preDealSearchTask(searchTask);

        String xmlFileName="QueryTagList.xml";
        String dbType = querySysPara.getDbtype();

        String querySql =getSqlFromXml(xmlFileName ,sqlId,dbType);

        querySql = replaceValueForSql(querySql ,searchTask, dataStoreVersion , dataStoreProject);

        return querySql;
    }

    /**
     * 患者异常情况汇总表  --patient_risk_group_list
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genPatientRiskGroupSql(YbChargeSearchTask searchTask,
                                            YbChargeQueryDatabase querySysPara, String tagId) throws Exception {

        String sqlId = "patient_risk_group_list";
        return innerGenPatientRisk(searchTask ,querySysPara ,tagId ,sqlId);
    }

    /**
     * 患者异常情况明细表  --datamining_patient_risk_data
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genPatientRiskResultSql(YbChargeSearchTask searchTask,
                                           YbChargeQueryDatabase querySysPara, String tagId) throws Exception {
        String sqlId = "datamining_patient_risk_data";
        return innerGenPatientRisk(searchTask ,querySysPara ,tagId ,sqlId);
    }

    /**
     * 医生异常情况汇总表  --doctor_risk_group_list
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genDoctorRiskGroupSql(YbChargeSearchTask searchTask,
                                          YbChargeQueryDatabase querySysPara, String tagId) throws Exception {

        String sqlId = "doctor_risk_group_list";
        return innerGenPatientRisk(searchTask ,querySysPara ,tagId ,sqlId);
    }

    /**
     * 医生异常情况明细表  --datamining_doctor_risk_data
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genDoctorRiskResultSql(YbChargeSearchTask searchTask,
                                          YbChargeQueryDatabase querySysPara, String tagId) throws Exception {
        String sqlId = "datamining_doctor_risk_data";
        return innerGenPatientRisk(searchTask ,querySysPara ,tagId ,sqlId);
    }

    /**
     * 标签结果汇总 monitor_datamining_stat
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genTagStatResultSql(YbChargeSearchTask searchTask,
                                       YbChargeQueryDatabase querySysPara) throws Exception {
        String sqlId = "monitor_datamining_stat";
        return innerGenPatientRisk(searchTask ,querySysPara,null,sqlId);
    }

    /**
     * 可疑标签汇总表
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genSuspiciousStatResultSql(YbChargeSearchTask searchTask,
                                       YbChargeQueryDatabase querySysPara) throws Exception {
        String sqlId = "suspicious_stat";
        return innerGenPatientRisk(searchTask ,querySysPara,null,sqlId);
    }

    /**
     *
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genDataminingOrgSumResultSql(YbChargeSearchTask searchTask,
                                              YbChargeQueryDatabase querySysPara) throws Exception {
        String sqlId = "datamining_org_sum";
        return innerGenPatientRisk(searchTask ,querySysPara,null,sqlId);
    }

    /**
     * 生成 欺诈专题 患者年度统计的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String genFraudPatientResultSql(YbChargeSearchTask searchTask,
                                              YbChargeQueryDatabase querySysPara) throws Exception {
        String sqlId = "fraud_patient";
        if(YbChargeSearchConstant.FRAUD_PROJECT.equals(searchTask.getTaskType())){
            sqlId = "fraud_project";
        }else if(YbChargeSearchConstant.FRAUD_HOSPITAL.equals(searchTask.getTaskType())){
            sqlId = "fraud_hospital";
        }
        return innerGenPatientRisk(searchTask ,querySysPara,null,sqlId);
    }

    /**
     * 生成 任务类型的查询SQL
     * @param searchTask
     * @return
     * @throws Exception
     */
    public  String  genTaskTypeResultSql(YbChargeSearchTask searchTask,
                                         YbChargeQueryDatabase querySysPara) throws Exception {
        String sqlId = searchTask.getTaskType();
        return innerGenPatientRisk(searchTask ,querySysPara,searchTask.getTagId(),sqlId);
    }





    /**
     * 患者异常情况
     * @param searchTask
     * @return
     * @throws Exception
     */
    private  String innerGenPatientRisk(YbChargeSearchTask searchTask,
                                YbChargeQueryDatabase querySysPara,String tagId ,String sqlId) throws Exception {
        String dataStoreVersion = querySysPara.getDataStoreVersion();
        String dataStoreProject = querySysPara.getDataStoreProject();

        //预处理查询条件
        preDealSearchTask(searchTask);

        String xmlFileName="QueryRiskData.xml";
        String dbType = querySysPara.getDbtype();

        String querySql =getSqlFromXml(xmlFileName ,sqlId,dbType);

        //替换年份
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        String startYear = "";
        String  endYear = "";
        if(searchTask.getChargedateStartdate()!=null){
            startYear = dateFormat.format(searchTask.getChargedateStartdate());
            querySql = StringUtils.replace(querySql,"${start_year}" ,startYear);
        }
        if(searchTask.getChargedateEnddate()!=null){
            endYear = dateFormat.format(searchTask.getChargedateEnddate());
            querySql = StringUtils.replace(querySql,"${end_year}" ,endYear);
        }

        //deptname
        if(StringUtils.isNotBlank(searchTask.getDeptname())){
            querySql = StringUtils.replace(querySql,"${deptname_src}" ,searchTask.getDeptname());
        }

        //医生姓名doctorname
        if(StringUtils.isNotBlank(searchTask.getDoctorname())){
            querySql = StringUtils.replace(querySql,"${doctorname}" ,searchTask.getDoctorname());
        }

        //替换算法名称
        if(StringUtils.isNotBlank(searchTask.getTagName())){
            querySql = StringUtils.replace(querySql,"${tag_name}" ,searchTask.getTagName());
        }
        if(StringUtils.isNotBlank(tagId)){
            querySql = StringUtils.replace(querySql,"${tag_id}" ,tagId);
        }

        if(YbChargeSearchConstant.ORG_HOLIDAY_RISK_STATISTICS.equals(searchTask.getTaskType())){
            querySql = StringUtils.replace(querySql,"${abnormal_distince}" ,"2");
        }else{
            querySql = StringUtils.replace(querySql,"${is_org_holiday_risk}" ,"");
        }
        //分解住院改为其他标签结果取数据 特殊处理排序 inhospital_apart
        if("分解住院".equals(searchTask.getTagName())){
            querySql = StringUtils.replace(querySql,"--${is_inhospital_apart}" ,"");
        }else{
            querySql = StringUtils.replace(querySql,"--${is_inhospital_apart}" ,"--");
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
            searchTask.setOrgs("阳朔县阳朔镇卫生院");
            //searchTask.setItemname("腹膜透析液#罗沙司他#司维拉姆#尿激酶#碳酸镧#碘伏帽");
            searchTask.setChargedateStartdate(format.parse("2020-02-04"));
            searchTask.setChargedateEnddate(format.parse("2022-02-04"));
            searchTask.setVisittype("MM");
            searchTask.setOrderby("sum_fee desc");
            //searchTask.setDataStaticsLevel("ods");
            searchTask.setEtlSource("his");
            //searchTask.setIsFundpay("1");
            String tagId = "hosp_itemprice_risk01";
            String dataStoreVersion = "";
            String dataStoreProject = "yangshuo";

            YbChargeQueryDatabase querySysPara = new YbChargeQueryDatabase();
            querySysPara.setDataStoreProject(dataStoreProject);
            querySysPara.setDataStoreVersion(dataStoreVersion);

            querySysPara.setDbtype("greenplum");

//            String sql =  (new GenHiveQueryRiskData()).genChargeitemRiskDataSql(searchTask,
//                    querySysPara,tagId);
            //System.out.println(sql);

//            String sql =  (new GenHiveQueryRiskData()).genDwbVisitTagSql(searchTask,
//                    querySysPara,tagId);


            String sql= (new GenHiveQueryRiskData()).innerGenSql(searchTask,querySysPara,"" ,"tagInfo_all");
            System.out.println(sql);


//            sql =  (new GenHiveQueryRiskData()).genDiagRiskDataSql(searchTask,
//                    querySysPara,tagId);
//            System.out.println(sql);



//            String sql =  (new GenHiveQueryRiskData()).genVisitTogetherDataSql(searchTask,
//                    querySysPara,tagId);
//            System.out.println(sql);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
