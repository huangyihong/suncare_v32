package com.ai.modules.ybChargeSearch.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.common.utils.StringCamelUtils;
import com.ai.modules.api.util.ApiTokenUtil;
import com.ai.modules.engine.util.JDBCUtil;
import com.ai.modules.system.entity.SysDatabase;
import com.ai.modules.system.entity.SysDatasource;
import com.ai.modules.system.service.ISysDatasourceService;
import com.ai.modules.ybChargeSearch.entity.*;
import com.ai.modules.ybChargeSearch.mapper.YbChargeSearchTaskMapper;
import com.ai.modules.ybChargeSearch.mapper.YbChargeitemChecklistMapper;
import com.ai.modules.ybChargeSearch.service.*;
import com.ai.modules.ybChargeSearch.vo.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 收费明细查询任务表
 * @Author: jeecg-boot
 * @Date:   2022-10-09
 * @Version: V1.0
 */
@Service
@Slf4j
public class YbChargeSearchTaskServiceImpl extends ServiceImpl<YbChargeSearchTaskMapper, YbChargeSearchTask> implements IYbChargeSearchTaskService {

    @Autowired
    IYbChargeSearchResultService searchResultService;

    @Autowired
    IYbChargeYearResultService yearResultService;

    @Autowired
    IYbChargeItemResultService itemResultService;

    @Autowired
    IYbChargeVisitResultService visitResultService;

    @Autowired
    IYbChargeDeptResultService deptResultService;

    @Autowired
    IYbChargeOverproofResultService overproofResultService;

    @Autowired
    IYbChargePatientRiskGroupService patientRiskGroupService;

    @Autowired
    IYbChargePatientRiskResultService patientRiskResultService;

    @Autowired
    IYbChargeDoctorRiskGroupService doctorRiskGroupService;

    @Autowired
    IYbChargeDoctorRiskResultService doctorRiskResultService;

    @Autowired
    IYbChargeLowResultService lowResultService;

    @Autowired
    IYbChargeSearchTaskDownloadService downloadService;

    @Autowired
    IYbChargeSearchHistoryService historyService;

    @Autowired
    YbChargeSearchTaskMapper ybChargeSearchTaskMapper;

    @Autowired
    private ISysDatasourceService sysDatasourceService;

    @Autowired
    private IYbChargeVisitTogetherResultService visitTogetherResultService;

    @Autowired
    private YbChargeitemChecklistMapper ybChargeitemChecklistMapper;

    @Autowired
    private IYbChargeMonitorDataminingStatService monitorDataminingStatService;

    @Autowired
    private  IYbChargeSuspiciousResultService suspiciousResultService;

    @Autowired
    private  IYbChargeFraudResultService fraudResultService;

    @Autowired
    private  IYbChargeDwsInhospitalApartService dwsInhospitalApartService;

    @Autowired
    private IYbChargeDoctorAdmitPatientInsickService doctorAdmitPatientInsickService;


    @Override
    public List<Map<String, Object>> getOrgList(YbChargeSearchTask bean,DatasourceAndDatabaseVO dbVO) throws Exception{
        YbChargeQueryDatabase querySysPara = getQueryDatabase(dbVO);
        if(!"greenplum".equals(querySysPara.getDbtype()) && "queryLocalTagList".equals(bean.getTaskType())){
            return new ArrayList<Map<String, Object>>();
        }
        String sql=GenHiveQuerySqlTools.genQueryOrgListSql(bean,querySysPara);
        return JDBCUtil.getResultByJdbc(dbVO.getSysDatabase(),sql);
    }

    @Override
    public Page<OdsCheckorgListVo> getOrgPageList(OdsCheckorgListVo bean, DatasourceAndDatabaseVO dbVO, Page<OdsCheckorgListVo> page) throws Exception{
        ArrayList<OdsCheckorgListVo> list = new ArrayList<>();
        YbChargeQueryDatabase querySysPara = getQueryDatabase(dbVO);
        String orgname = bean.getOrgname();
        if(StrUtil.isNotEmpty(orgname)){
            orgname=orgname.replace("#","|");
            bean.setOrgname(orgname);
        }
        String xmlFileName="QueryItemStatics.xml";
        String dbType = querySysPara.getDbtype();

        try {

            String countSql = getQuerySql(xmlFileName, page, bean,"queryOrgPageCountList",dbType);
            Connection conn2 = JDBCUtil.getDbConnection(dbVO.getSysDatabase());
            PreparedStatement countStatement = conn2.prepareStatement(countSql);
            ResultSet countSet = countStatement.executeQuery();
            int total=0;
            while(countSet.next()){
                total = countSet.getInt("total");
            }
            page.setTotal(total);

            if(total>0){
                String querySql = getQuerySql(xmlFileName, page, bean,"queryOrgPageList",dbType);
                Connection conn = JDBCUtil.getDbConnection(dbVO.getSysDatabase());
                PreparedStatement pstmt = conn.prepareStatement(querySql);
                ResultSet rSet = pstmt.executeQuery();
                list = new ArrayList<>();
                while (rSet.next()) {
                    OdsCheckorgListVo resultVo = new OdsCheckorgListVo();
                    resultVo.setOrgid(rSet.getString("orgid"));
                    resultVo.setOrgname(rSet.getString("orgname"));
                    resultVo.setOwntype(rSet.getString("owntype"));
                    resultVo.setHosplevel(rSet.getString("hosplevel"));
                    if(YbChargeSearchConstant.DB_TYPE_GREENPLUM.equalsIgnoreCase(dbType)||YbChargeSearchConstant.DB_TYPE_MYSQL.equalsIgnoreCase(dbType)){
                        resultVo.setLocalTag(rSet.getString("local_tag"));
                        resultVo.setMaxAllfundPay(rSet.getString("max_allfund_pay"));
                    }

                    list.add(resultVo);
                }
            }

           page.setRecords(list);


        } catch (Exception e) {
            String msg = e.getCause()== null? e.getMessage():e.getCause().getMessage();

            log.error("" ,e);
            throw new Exception(msg);

        }

        return page;
    }


    public String getQuerySql(String xmlFileName,Page<OdsCheckorgListVo> page,OdsCheckorgListVo bean,String sqlId,String dbType) throws Exception {
        GenHiveQueryCommon genHiveQueryCommon = new GenHiveQueryCommon();
        String querySql = genHiveQueryCommon.getSqlFromXml(xmlFileName, sqlId,dbType);
        querySql = StringUtils.replace(querySql ,"${size}" ,String.valueOf(page.getSize()));
        querySql = StringUtils.replace(querySql ,"${current}" ,String.valueOf(page.getCurrent()));
        querySql = StringUtils.replace(querySql ,"${offset}" ,String.valueOf((page.getCurrent()-1)*page.getSize()));
        if(StrUtil.isNotEmpty(bean.getOrgname())){
            querySql = StringUtils.replace(querySql ,"${orgname}" ,bean.getOrgname());
        }
        if(StrUtil.isNotEmpty(bean.getHosplevel())){
            querySql = StringUtils.replace(querySql ,"${hosplevel}" ,bean.getHosplevel());
        }
        if(StrUtil.isNotEmpty(bean.getLocalTag())){
            querySql = StringUtils.replace(querySql ,"${localTag}" ,bean.getLocalTag());
        }
        if(StrUtil.isNotEmpty(bean.getOwntype())){
            querySql = StringUtils.replace(querySql ,"${owntype}" ,bean.getOwntype());
        }
        if(StrUtil.isNotEmpty(bean.getOrgid())){
            querySql = StringUtils.replace(querySql ,"${orgid}" ,bean.getOrgid());
        }
        if(StrUtil.isNotEmpty(bean.getMaxAllfundPay()) && StrUtil.isNotEmpty(bean.getFundValType())){
            querySql = StringUtils.replace(querySql ,"${maxAllfundPay}" ,bean.getMaxAllfundPay());
            querySql = StringUtils.replace(querySql ,"${fundValType}" ,bean.getFundValType());
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
            } else{
                sqlList.add(tmpSql);
            }
        }
        querySql = StringUtils.join(sqlList ,"\n");

        return querySql;
    }

    @Override
    public List<Map<String,Object>> getDeptList(YbChargeSearchTask bean,DatasourceAndDatabaseVO dbVO) throws Exception{
        try {
            YbChargeQueryDatabase querySysPara = this.getQueryDatabase(dbVO);
            String sql=GenHiveQuerySqlTools.genQueryDeptListSql(bean,querySysPara);
            return JDBCUtil.getResultByJdbc(dbVO.getSysDatabase(),sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Map<String,Object>> getTagList(YbChargeSearchTask bean,DatasourceAndDatabaseVO dbVO) throws Exception{
        try {
            YbChargeQueryDatabase querySysPara = this.getQueryDatabase(dbVO);
            String sql=GenHiveQuerySqlTools.genTagInfoForChargeItemSql(querySysPara,bean);
            return JDBCUtil.getResultByJdbc(dbVO.getSysDatabase(),sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Map<String,Object>> getDataminingOrgSum(YbChargeSearchTask bean,DatasourceAndDatabaseVO dbVO) throws Exception{
        try {
            YbChargeQueryDatabase querySysPara = this.getQueryDatabase(dbVO);
            String sql=GenHiveQuerySqlTools.genDataminingOrgSumResultSql(querySysPara,bean);
            return JDBCUtil.getResultByJdbc(dbVO.getSysDatabase(),sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    public List<Map<String,Object>> genTaskTypeResultData(YbChargeSearchTask bean,DatasourceAndDatabaseVO dbVO) throws Exception{
        try {
            YbChargeQueryDatabase querySysPara = this.getQueryDatabase(dbVO);
            String sql=GenHiveQuerySqlTools.genTaskTypeResultSql(bean,querySysPara);
            System.out.println(bean.getTaskType()+"::::"+sql);
            return JDBCUtil.getResultByJdbc(dbVO.getSysDatabase(),sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    @Transactional
    public boolean deleteYbChargeSearchTask(String id) {
        YbChargeSearchTask bean = this.baseMapper.selectById(id);
       /* String filePath = bean.getFileFullpath();
        if(StringUtils.isNotBlank(filePath)){
            File file = new File(filePath);
            if(file.exists()){
                file.delete();
            }
        }*/
        this.baseMapper.deleteById(id);
        String taskType = bean.getTaskType();
        this.deleteSearchResult(id, taskType);
        return true;
    }

    private void deleteSearchResult(String id, String taskType) {
        switch (taskType) {
            case YbChargeSearchConstant.SEARCH: {
                searchResultService.remove(new QueryWrapper<YbChargeSearchResult>().eq("TASK_ID",id));
                historyService.remove(new QueryWrapper<YbChargeSearchHistory>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.YEAR_USERATE_STATISTICS:
            case YbChargeSearchConstant.YEAR_SURGERY_STATISTICS:
            case YbChargeSearchConstant.YEAR_ORG_ONLINE_PATIENT_COUNT:
            case YbChargeSearchConstant.YEAR_STATISTICS: {
                yearResultService.remove(new QueryWrapper<YbChargeYearResult>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.ITEM_STATISTICS:
            case YbChargeSearchConstant.ITEM_BY_DEPTSTATISTICS: {
                itemResultService.remove(new QueryWrapper<YbChargeItemResult>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.DEPT_STATISTICS:
            case YbChargeSearchConstant.DATAMINING_SQL_DETAIL:
            case YbChargeSearchConstant.ITEM_BY_VISIT_STATISTICS: {
                deptResultService.remove(new QueryWrapper<YbChargeDeptResult>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.VISIT_STATISTICS: {
                visitResultService.remove(new QueryWrapper<YbChargeVisitResult>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.LOW_STATISTICS: {
                lowResultService.remove(new QueryWrapper<YbChargeLowResult>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.RISK_STATISTICS:
            case YbChargeSearchConstant.ORG_RISK_STATISTICS:
            case YbChargeSearchConstant.ORG_HOLIDAY_RISK_STATISTICS:
            case YbChargeSearchConstant.DIAG_RISK_STATISTICS: {
                overproofResultService.remove(new QueryWrapper<YbChargeOverproofResult>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.PATIENT_RISK_GROUP_STATISTICS: {
                patientRiskGroupService.remove(new QueryWrapper<YbChargePatientRiskGroup>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.PATIENT_RISK_STATISTICS: {
                patientRiskResultService.remove(new QueryWrapper<YbChargePatientRiskResult>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.DOCTOR_RISK_GROUP_STATISTICS: {
                doctorRiskGroupService.remove(new QueryWrapper<YbChargeDoctorRiskGroup>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.DOCTOR_RISK_STATISTICS: {
                doctorRiskResultService.remove(new QueryWrapper<YbChargeDoctorRiskResult>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.DRUG_TOP200:
            case YbChargeSearchConstant.DRUG_TOP200_DETAIL:
            case YbChargeSearchConstant.DRUG_RULE_STATISTICS: {
                searchResultService.remove(new QueryWrapper<YbChargeSearchResult>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.VISIT_TOGETHER_STATISTICS: {
                visitTogetherResultService.remove(new QueryWrapper<YbChargeVisitTogetherResult>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.TAG_STATISTICS: {
                monitorDataminingStatService.remove(new QueryWrapper<YbChargeMonitorDataminingStat>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.DWB_VISIT_TAG:
            case YbChargeSearchConstant.SUSPICIOUS_GROUP_STATISTICS:{
                suspiciousResultService.remove(new QueryWrapper<YbChargeSuspiciousResult>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.FRAUD_PROJECT:
            case YbChargeSearchConstant.FRAUD_HOSPITAL:
            case YbChargeSearchConstant.FRAUD_PATIENT:{
                fraudResultService.remove(new QueryWrapper<YbChargeFraudResult>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.DWS_INHOSPITAL_APART:{
                dwsInhospitalApartService.remove(new QueryWrapper<YbChargeDwsInhospitalApart>().eq("TASK_ID",id));
                break;
            }
            case YbChargeSearchConstant.DOCTOR_ADMIT_PATIENT_INSICK:{
                doctorAdmitPatientInsickService.remove(new QueryWrapper<YbChargeDoctorAdmitPatientInsick>().eq("TASK_ID",id));
                break;
            }
        }
    }

    @Override
    public Map<String,Object> run(YbChargeSearchTask bean,DatasourceAndDatabaseVO dbVO) {
        Map<String,Object> map = new HashMap<>();
        //预览数据50条
        List<Map<String, Object>> dataList=new ArrayList<Map<String, Object>>();
        //收费项目搜索历史数据
        List<YbChargeSearchHistory> historyList = new ArrayList<>();

        String outPath = bean.getFileFullpath();
        String taskType = bean.getTaskType();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rSet = null;
        String status=YbChargeSearchConstant.TASK_RUNING;
        String errorMsg = "";

        int recordCount = 0;
        List<YbChargeQuerySql> sqlList = new ArrayList<>();
        try {
            YbChargeQueryDatabase queryDatabase = this.getQueryDatabase(dbVO);

            String sheetName = "";
            String titleStr = "";
            String fieldStr = "";
            if(YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType)!=null){
                sheetName = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getSheefName();
                titleStr = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getTitleStr();
                fieldStr = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getFieldStr();
            }

            Map<String,String> sqlFieldMap =  YbChargeSearchConstant.getSqlFieldMap(taskType);//字段映射
            Map<String,String> formatMap = YbChargeSearchConstant.getFormatMap(taskType);//值转换

            String [] titleArr = titleStr.split(",");
            String [] fieldArr = fieldStr.split(",");
            //年度统计 src层去掉orgname_src
            if(YbChargeSearchConstant.YEAR_STATISTICS.equals(taskType)&&"src".equals(bean.getDataStaticsLevel())){
                titleArr = Arrays.asList(titleArr).stream().filter(t->!"原始机构名称".equals(t)).collect(Collectors.toList()).toArray(new String[titleArr.length-1]);
                fieldArr =  Arrays.asList(fieldArr).stream().filter(t->!"orgnameSrc".equals(t)).collect(Collectors.toList()).toArray(new String[fieldArr.length-1]);
            }

            String [] titleMonthArr = null;
            String [] fieldMonthArr = null;

            //获取sql
            sqlList = getSqlListByTaskBean(bean,queryDatabase,sheetName);

            //创建表格
            SXSSFWorkbook xssfWorkbook = new SXSSFWorkbook(5000);

            SXSSFSheet sheet = null;
            int rowNum = 0;
            Map<String,SXSSFSheet> mapSheet = new HashMap<>();
            Map<String,Integer> mapRowNum = new HashMap<>();
            List<String> colNameList = new ArrayList<>();
            List<String> colNameMonthList = new ArrayList<>();
            int colNum =0;
            int colMonthNum =0;
            conn = JDBCUtil.getDbConnection(dbVO.getSysDatabase());
            for(YbChargeQuerySql sqlBean:sqlList){
                String sql = sqlBean.getQuerySql();
                sheetName = sqlBean.getSheetName();
                boolean isMonth = sqlBean.getIsMonth();
                String splitColumnName = sqlBean.getSplitColumnName();
                if(StringUtils.isNotBlank(splitColumnName)){
                    //按指定字段的内容一个一个sheet导出
                    splitColumnName = StringCamelUtils.underline2Camel(splitColumnName);
                }
                log.info(sql);

                //判断下SQL中是否有分号，如果有，则需要将创建临时表和查询分开
                int index = sql.lastIndexOf(";");
                if(index>0){
                    String querySql = sql.substring(index +1).trim();

                    //如果查询SQL不为空，则需先执行创建临时表SQL
                    if("".equalsIgnoreCase(querySql) == false){
                        String executeSql = sql.substring(0 ,index);
                        sql = querySql;

                        pstmt = conn.prepareStatement(executeSql);
                        pstmt.execute();

                    }
                }


                //获取总条数
                long maxLimit = GenHiveQuerySqlTools.getMaxCountLimit(sql);
                String countSql = GenHiveQuerySqlTools.getCountSqlByQuerySql(sql);
                if(maxLimit>0&&StringUtils.isNotBlank(countSql)){
                    System.out.println(countSql);
                    pstmt = conn.prepareStatement(countSql);
                    rSet = pstmt.executeQuery();
                    long count = 0;
                    while (rSet.next()) {
                        count = rSet.getLong("count");
                        break;
                    }
                    if(count>maxLimit){
                        status=YbChargeSearchConstant.TASK_FAIL;
                        errorMsg="数据下载失败，数据查询结果超过1百万条！";
                        break;
                    }
                }

                pstmt = conn.prepareStatement(sql);
                rSet = pstmt.executeQuery();
                rSet.setFetchSize(1000);//防止大数据量

                if(isMonth) {//月度统计
                    //查询结果的字段
                    if(colNameMonthList.size()==0){
                        colMonthNum = rSet.getMetaData().getColumnCount();

                        titleMonthArr = new String [colMonthNum];
                        fieldMonthArr = new String [colMonthNum];

                        for (int j = 1; j <= colMonthNum; j++) {
                            String colName = rSet.getMetaData().getColumnLabel(j);
                            if(colName.startsWith("y")&&colName.indexOf("年")!=-1&&colName.indexOf("月")!=-1){
                                titleMonthArr[j-1] = colName.replace("y","");
                            }else{
                                //字段名处理映射
                                colName = getFormatColName(sqlFieldMap, colName);
                                titleMonthArr[j-1] = colName.replace("itemname","项目名称").replace("name","患者姓名+出生日期")
                                        .replace("amount","总数量").replace("fee","总金额");
                            }
                            fieldMonthArr[j-1] = colName;
                            colNameMonthList.add(colName);
                        }
                    }
                }else{
                    //查询结果的字段
                    if(colNameList.size()==0||!taskType.equals(sqlBean.getTaskType())){
                        colNameList = new ArrayList<>();
                        colNum = rSet.getMetaData().getColumnCount();
                        for (int j = 1; j <= colNum; j++) {
                            String colName = rSet.getMetaData().getColumnLabel(j);
                            //字段名处理映射
                            colName = getFormatColName(sqlFieldMap, colName);
                            colNameList.add(colName);
                        }
                    }
                }

                //查询其他sheet结果
                String sqlTaskType = taskType;
                if(StringUtils.isNotBlank(sqlBean.getTaskType())&&!taskType.equals(sqlBean.getTaskType())){
                    titleArr = YbChargeSearchConstant.TASK_TYPE_MAP.get(sqlBean.getTaskType()).getTitleStr().split(",");
                    fieldArr = YbChargeSearchConstant.TASK_TYPE_MAP.get(sqlBean.getTaskType()).getFieldStr().split(",");
                    sqlTaskType = sqlBean.getTaskType();
                    sqlFieldMap =  YbChargeSearchConstant.getSqlFieldMap(sqlTaskType);//字段映射
                    formatMap = YbChargeSearchConstant.getFormatMap(sqlTaskType);//值转换
                }




                int sheetRecordCount = 0 ;
                BigDecimal historyTotalFee = new BigDecimal(0);

                String [] titleArr1 = null;
                String [] fieldArr1 = null;
                int colNum1 =0;

                while (rSet.next()) {
                    if(!isMonth) {
                        if(taskType.equals(sqlTaskType)){
                            recordCount++;
                        }
                        sheetRecordCount++;
                        colNum1 = colNum;
                        //明细查询增加itemprice
                        if(YbChargeSearchConstant.SEARCH.equals(sqlTaskType)&&colNameList.contains("itemprice")){
                            titleArr1 = (titleStr+",单价").split(",");
                            fieldArr1 = (fieldStr+",itemprice").split(",");
                        }else{
                            titleArr1= titleArr;
                            fieldArr1 = fieldArr;
                        }
                    }else{
                        colNum1 = colMonthNum;
                        titleArr1= titleMonthArr;
                        fieldArr1 = fieldMonthArr;
                    }


                    Map<String, Object> beanMap=new HashMap<String, Object>();
                    for (int j = 1; j <= colNum1; j++) {
                        String colName = "";
                        if(isMonth) {
                            colName=colNameMonthList.get(j-1);
                        }else{
                            colName=colNameList.get(j-1);
                        }
                        Object value = rSet.getObject(j);
                        value = formatStr(rSet.getObject(j));
                        value = YbChargeSearchConstant.getFormatValue(formatMap,colName, value);
                        beanMap.put(colName, value);
                        //金额累计
                        if(YbChargeSearchConstant.SEARCH.equals(sqlTaskType)&&"fee".equals(colName)&&!isMonth){
                            historyTotalFee= historyTotalFee.add((BigDecimal) value);
                        }
                    }

                    if(StringUtils.isNotBlank(splitColumnName)){
                        sheetName = formatStr(beanMap.get(splitColumnName)).toString();
                    }

                    sheetName = formatSheetName(sheetName);

                    if(mapSheet.get(sheetName)!=null){
                        sheet = mapSheet.get(sheetName);
                        rowNum = mapRowNum.get(sheetName);
                    }else{
                        rowNum = 0;
                        sheet = (SXSSFSheet) xssfWorkbook.createSheet(sheetName);
                        mapSheet.put(sheetName,sheet);
                        CellStyle colTitleStyle = createColTitleCellStyle(xssfWorkbook);//title样式
                        // 创建第一页的第一行，索引从0开始
                        Row row0 = sheet.createRow(rowNum++);
                        //表头数据,循环将表头数据填充到第1行
                        for (int i = 0; i < titleArr1.length; i++) {
                            Cell c1 = row0.createCell(i);
                            c1.setCellStyle(colTitleStyle);
                            c1.setCellValue(titleArr1[i]);
                        }
                        mapRowNum.put(sheetName,rowNum);
                    }

                    //写入单元格
                    Row temp = sheet.createRow((rowNum++));
                    for (int j = 0; j < fieldArr1.length; j++) {
                        Cell c = temp.createCell(j);
                        if(beanMap.get(fieldArr1[j])!=null){
                            if(beanMap.get(fieldArr1[j]) instanceof BigDecimal||beanMap.get(fieldArr1[j]) instanceof Double||beanMap.get(fieldArr1[j]) instanceof Float){
                                c.setCellValue(Double.valueOf(beanMap.get(fieldArr1[j]).toString()));
                            }else if(beanMap.get(fieldArr1[j]) instanceof Integer){
                                c.setCellValue(Integer.valueOf(beanMap.get(fieldArr1[j]).toString()));
                            }else{
                                c.setCellValue(formatStr(beanMap.get(fieldArr1[j])).toString());
                                beanMap.put(fieldArr1[j],formatStr(beanMap.get(fieldArr1[j])).toString());
                            }
                        }

                    }
                    mapRowNum.put(sheetName,rowNum);

                    if(!isMonth) {
                        if(dataList.size()<50&&taskType.equals(sqlTaskType)){
                            dataList.add(beanMap);
                        }

                        if(sheetRecordCount>1048570){
                            log.info("最大行数："+sheetRecordCount);
                            break;
                        }
                        if(recordCount%10000==0) {
                            log.info("正在导出数量："+recordCount);
                        }
                    }

                }

                //收费项目搜索历史数据YbChargeSearchHistory
                if(sheetRecordCount>0&&YbChargeSearchConstant.SEARCH.equals(sqlTaskType)&&!isMonth){
                    YbChargeSearchTask sheetTask = sqlBean.getSearchTaskBean();
                    if(sheetTask!=null&&(StringUtils.isNotBlank(sheetTask.getItemname())||StringUtils.isNotBlank(sheetTask.getItemname1())||
                            StringUtils.isNotBlank(sheetTask.getHisItemName())||StringUtils.isNotBlank(sheetTask.getHisItemName1()))){

                        YbChargeSearchHistory historyBean = new YbChargeSearchHistory();
                        BeanUtils.copyProperties(sheetTask,historyBean);
                        historyBean.setOrgids(formatStr(sheetTask.getOrgids()));
                        historyBean.setOrgs(formatStr(sheetTask.getOrgs()));
                        historyBean.setItemname(formatStr(sheetTask.getItemname()));
                        historyBean.setItemname1(formatStr(sheetTask.getItemname1()));
                        historyBean.setId(UUID.randomUUID().toString());
                        historyBean.setTaskId(bean.getId());
                        historyBean.setRecordCount(sheetRecordCount);
                        historyBean.setCreateUser(bean.getUpdateUser());
                        historyBean.setCreateUserId(bean.getUpdateUserId());
                        historyBean.setCreateTime(bean.getUpdateTime());
                        if("his".equals(sheetTask.getItemType())){
                            historyBean.setItemname(formatStr(sheetTask.getHisItemName()));
                            historyBean.setItemname1(formatStr(sheetTask.getHisItemName1()));
                        }
                        historyBean.setTotalFee(historyTotalFee);
                        historyList.add(historyBean);
                    }
                }

            }
            //成功执行下面操作
            if(!YbChargeSearchConstant.TASK_FAIL.equals(status)){
                log.info("导出数量："+recordCount);

                FileOutputStream out = null;
                try{
                    //输出excel文件
                    File outFile = new File(outPath);
                    if (!outFile.getParentFile().exists()) {
                        outFile.getParentFile().mkdirs();
                    }
                    out=new FileOutputStream(outFile);
                    xssfWorkbook.write(out);
                    out.flush();
                } catch (Exception e) {
                    log.error("导出Excel文件失败", e);
                }finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            log.error("导出Excel文件关闭输出流失败", e);
                        } finally {
                            out = null;
                        }
                    }
                    if(xssfWorkbook!=null){
                        xssfWorkbook.dispose();
                        xssfWorkbook = null;
                    }
                }

                status=YbChargeSearchConstant.TASK_SUCCESS;
                map.put("dataList",dataList);
                log.info("进程结束：");
            }
        } catch (Exception e) {
            e.printStackTrace();
            status=YbChargeSearchConstant.TASK_FAIL;
            errorMsg = e.getMessage();
            if(errorMsg.length()>2000){
                errorMsg = errorMsg.substring(0, 2000);
            }
        } finally {

            try {
                //删除临时表
                String tempTableSql = this.getDropTempTablSql(sqlList);
                if(StringUtils.isNotBlank(tempTableSql)){
                    pstmt = conn.prepareStatement(tempTableSql);
                    pstmt.execute();
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            JDBCUtil.destroy(rSet,conn,pstmt);

            //更新状态 条数记录
            bean.setStatus(status);
            bean.setOverTime(new Date());
            try{
                if(YbChargeSearchConstant.TASK_SUCCESS.equals(status)){
                    bean.setRecordCount(recordCount);
                    if(recordCount>0) {
                        double kb = 1024;
                        long size = new File(outPath).length();
                        double fileSize = size / kb;
                        bean.setFileSize(fileSize);
                        this.saveHistoryBatch(historyList);
                    }
                    this.saveResultData(bean, dataList, taskType);
                }else{
                    bean.setRecordCount(0);
                    bean.setFileSize(0.0);
                }
            }catch (Exception e){
                e.printStackTrace();
                errorMsg = e.getMessage();
                if(errorMsg.length()>2000){
                    errorMsg = errorMsg.substring(0, 2000);
                }
            }
            bean.setErrorMsg(errorMsg);
            ApiTokenUtil.putBodyApi("/ybChargeSearch/ybChargeSearchTask/edit", bean);
        }
        map.put("status",status);
        map.put("bean",bean);
        return map;

    }

    //sheetName特殊字符处理
    private String formatSheetName(String sheetName) {
        if(StringUtils.isBlank(sheetName)){
            sheetName = "空";
        }
        sheetName = sheetName.replaceAll("\\[","");
        sheetName = sheetName.replaceAll("]","");
        sheetName = sheetName.replaceAll("\\*","");
        sheetName = sheetName.replaceAll("\\?","");
        sheetName = sheetName.replaceAll("？","");
        sheetName = sheetName.replaceAll("/","");
        sheetName = sheetName.replaceAll("\\\\","");
        sheetName = sheetName.replaceAll(":","");
        if(sheetName.length()>30){
            sheetName = sheetName.substring(0,30);
        }
        return sheetName;
    }


    private  String formatStr(String rlikeStr) throws Exception{
        if(StringUtils.isBlank(rlikeStr)){
            return "";
        }else{
            rlikeStr = StringUtils.replace(rlikeStr, "^","");
            rlikeStr = StringUtils.replace(rlikeStr, "$","");
            rlikeStr = StringUtils.replace(rlikeStr, "\\\\\\(", "(" );
            rlikeStr = StringUtils.replace(rlikeStr,"\\\\\\)", ")" );
            rlikeStr = StringUtils.replace(rlikeStr, "\\\\\\[","[" );
            rlikeStr = StringUtils.replace(rlikeStr,"\\\\\\]", "]" );
            rlikeStr = StringUtils.replace(rlikeStr,"\\\\*", "*" );
            rlikeStr = StringUtils.replace(rlikeStr,"\\\\+", "+" );
            rlikeStr = StringUtils.replace(rlikeStr,"\\\\.", "." );
            rlikeStr = StringUtils.replace(rlikeStr,"\\{", "{" );
            return rlikeStr;
        }
    }


    @Override
    public String getSqlByTaskBean(YbChargeSearchTask bean,YbChargeQueryDatabase querySysPara) throws Exception {
        String sql="";
        switch (bean.getTaskType()){
            //年度统计指标
            case YbChargeSearchConstant.YEAR_STATISTICS:{
                sql = GenHiveQuerySqlTools.genHospitalYearStaticSql(bean,querySysPara);
                break;
            }
            case YbChargeSearchConstant.YEAR_USERATE_STATISTICS:{
                //年度统计指标--医保项目使用率
                sql = GenHiveQuerySqlTools.genHospitalYearCountSql(bean,querySysPara);
                break;
            }
            //医院手术情况统计表
            case YbChargeSearchConstant.YEAR_SURGERY_STATISTICS:{
                sql = GenHiveQuerySqlTools.genHospitalYearSurgerySql(bean,querySysPara);
                break;
            }
            //医院每日在院人数统计
            case YbChargeSearchConstant.YEAR_ORG_ONLINE_PATIENT_COUNT:{
                sql = GenHiveQuerySqlTools.genHospitalOnLinePatientCount(bean,querySysPara);
                break;
            }
            //按机构-进行项目汇总
            case YbChargeSearchConstant.ITEM_STATISTICS:{
                sql = GenHiveQuerySqlTools.genItemNameStaticSqlByOrg(bean,querySysPara);
                break;
            }
            //按科室-进行项目汇总
            case YbChargeSearchConstant.ITEM_BY_DEPTSTATISTICS:{
                sql = GenHiveQuerySqlTools.genItemNameStaticSqlByDept(bean,querySysPara);
                break;
            }

            //科室金额前10名
            case YbChargeSearchConstant.DEPT_STATISTICS:{
                sql = GenHiveQuerySqlTools.genDeoptTop10ChargeListSql(bean,querySysPara);
                break;
            }

            //按就诊号项目汇总
            case YbChargeSearchConstant.ITEM_BY_VISIT_STATISTICS:{
                sql = GenHiveQuerySqlTools.genVisitChargeListSql(bean,querySysPara);
                break;
            }

            //住院及门慢清单
            case YbChargeSearchConstant.VISIT_STATISTICS:{
                sql = GenHiveQuerySqlTools.genVisitListSql(bean,querySysPara);
                break;
            }

            //低标准入院可疑数据
            case YbChargeSearchConstant.LOW_STATISTICS:{
                sql = GenHiveQuerySqlTools.genInHospitalRiskSql(bean,querySysPara);
                break;
            }
            //医院收费项目异常数据
            case YbChargeSearchConstant.RISK_STATISTICS:{
                sql = GenHiveQuerySqlTools.genHospChargeRiskSql(bean,querySysPara);
                break;
            }
            //诊断汇总数据异常
            case YbChargeSearchConstant.DIAG_RISK_STATISTICS:{
                sql = GenHiveQuerySqlTools.genDiagRiskSql(bean,querySysPara);
                break;
            }
            //假期住院人次异常
            case YbChargeSearchConstant.ORG_HOLIDAY_RISK_STATISTICS:
            //医院总量异常
            case YbChargeSearchConstant.ORG_RISK_STATISTICS:{
                sql = GenHiveQuerySqlTools.genOrgRiskSql(bean,querySysPara);
                break;
            }
            //结伴就医
            case YbChargeSearchConstant.VISIT_TOGETHER_STATISTICS:{
                sql = GenHiveQuerySqlTools.genVisitTogetherSql(bean,querySysPara);
                break;
            }
            //患者异常情况汇总表
            case YbChargeSearchConstant.PATIENT_RISK_GROUP_STATISTICS:{
                sql = GenHiveQuerySqlTools.genPatientRiskGroupSql(bean,querySysPara);
                break;
            }
            //患者异常情况明细表
            case YbChargeSearchConstant.PATIENT_RISK_STATISTICS:{
                sql = GenHiveQuerySqlTools.genPatientRiskResultSql(bean,querySysPara);
                break;
            }
            //医生异常情况汇总表
            case YbChargeSearchConstant.DOCTOR_RISK_GROUP_STATISTICS:{
                sql = GenHiveQuerySqlTools.genDoctorRiskGroupSql(bean,querySysPara);
                break;
            }
            //医生异常情况明细表
            case YbChargeSearchConstant.DOCTOR_RISK_STATISTICS:{
                sql = GenHiveQuerySqlTools.genDoctorRiskResultSql(bean,querySysPara);
                break;
            }
            //标签结果汇总
            case YbChargeSearchConstant.TAG_STATISTICS:{
                sql = GenHiveQuerySqlTools.genTagStatResultSql(bean,querySysPara);
                break;
            }
            //就诊标注结果
            case YbChargeSearchConstant.DWB_VISIT_TAG:
            //可疑就诊标签汇总表
            case YbChargeSearchConstant.SUSPICIOUS_GROUP_STATISTICS:{
                sql = GenHiveQuerySqlTools.genSuspiciousStatResultSql(bean,querySysPara);
                break;
            }
            //欺诈专题 城市年度统计
            case YbChargeSearchConstant.FRAUD_PROJECT:
            //欺诈专题 医院年度统计
            case YbChargeSearchConstant.FRAUD_HOSPITAL:
            //欺诈专题 患者年度统计
            case YbChargeSearchConstant.FRAUD_PATIENT: {
                sql = GenHiveQuerySqlTools.genFraudPatientResultSql(bean,querySysPara);
                break;
            }
            //标签模型明细数据
            case YbChargeSearchConstant.DATAMINING_SQL_DETAIL:
            //医生住院期间收治病人
            case YbChargeSearchConstant.DOCTOR_ADMIT_PATIENT_INSICK:
            //分解住院
            case YbChargeSearchConstant.DWS_INHOSPITAL_APART:
            //top200口服药明细
            case YbChargeSearchConstant.DRUG_TOP200_DETAIL:
            //top200口服药
            case YbChargeSearchConstant.DRUG_TOP200: {
                sql = GenHiveQuerySqlTools.genTaskTypeResultSql(bean,querySysPara);
                break;
            }
        }
        return sql;
    }

    //获取删除临时表的语句
    private String getDropTempTablSql(List<YbChargeQuerySql> sqlList){
        return GenHiveQuerySqlTools.getDropTempTablSql(sqlList);
    }

    @Override
    public  List<YbChargeQuerySql> getSqlListByTaskBean(YbChargeSearchTask taskBean, YbChargeQueryDatabase querySysPara, String sheetName) throws Exception {
        YbChargeSearchTask bean = new YbChargeSearchTask();
        BeanUtils.copyProperties(taskBean,bean);
        List<YbChargeQuerySql> sqlList = new ArrayList<>();

        if(YbChargeSearchConstant.SEARCH.equals(bean.getTaskType())) {
            List<YbChargeSearchTask> searchList = JSONArray.parseArray(bean.getJsonStr(), YbChargeSearchTask.class);
            sqlList = GenHiveQuerySqlTools.genSrcYbChargeDetailSqlList(searchList, querySysPara);
        } else if(YbChargeSearchConstant.DRUG_RULE_STATISTICS.equals(bean.getTaskType())) {
            //药品规则sql脚本
            GenRuleScriptHandler handler = new GenRuleScriptHandler();
            sqlList = handler.generateRuleScript(bean, querySysPara);
        }else if(YbChargeSearchConstant.PATIENT_RISK_GROUP_STATISTICS.equals(bean.getTaskType())){
            //汇总明细都导出
            YbChargeQuerySql groupSql = new  YbChargeQuerySql();
            groupSql.setQuerySql(this.getSqlByTaskBean(bean,querySysPara));
            groupSql.setSheetName(sheetName);
            sqlList.add(groupSql);

            YbChargeQuerySql detailSql = new  YbChargeQuerySql();
            bean.setTaskType(YbChargeSearchConstant.PATIENT_RISK_STATISTICS);
            detailSql.setQuerySql(this.getSqlByTaskBean(bean,querySysPara));
            detailSql.setSheetName(YbChargeSearchConstant.TASK_TYPE_MAP.get(YbChargeSearchConstant.PATIENT_RISK_STATISTICS).getSheefName());
            detailSql.setTaskType(YbChargeSearchConstant.PATIENT_RISK_STATISTICS);
            sqlList.add(detailSql);
        }else if(YbChargeSearchConstant.DOCTOR_RISK_GROUP_STATISTICS.equals(bean.getTaskType())){
            //汇总明细都导出
            YbChargeQuerySql groupSql = new  YbChargeQuerySql();
            groupSql.setQuerySql(this.getSqlByTaskBean(bean,querySysPara));
            groupSql.setSheetName(sheetName);
            sqlList.add(groupSql);

            YbChargeQuerySql detailSql = new  YbChargeQuerySql();
            bean.setTaskType(YbChargeSearchConstant.DOCTOR_RISK_STATISTICS);
            detailSql.setQuerySql(this.getSqlByTaskBean(bean,querySysPara));
            detailSql.setSheetName(YbChargeSearchConstant.TASK_TYPE_MAP.get(YbChargeSearchConstant.DOCTOR_RISK_STATISTICS).getSheefName());
            detailSql.setTaskType(YbChargeSearchConstant.DOCTOR_RISK_STATISTICS);
            sqlList.add(detailSql);
        }else{
//            if(YbChargeSearchConstant.YEAR_STATISTICS.equals(bean.getTaskType())
//                    ||YbChargeSearchConstant.YEAR_USERATE_STATISTICS.equals(bean.getTaskType())
//                    ||YbChargeSearchConstant.YEAR_SURGERY_STATISTICS.equals(bean.getTaskType())
//                    ||YbChargeSearchConstant.ITEM_STATISTICS.equals(bean.getTaskType())
//                    ||YbChargeSearchConstant.VISIT_STATISTICS.equals(bean.getTaskType())
//                    ||YbChargeSearchConstant.FRAUD_PATIENT.equals(bean.getTaskType())
//                    ||YbChargeSearchConstant.FRAUD_PROJECT.equals(bean.getTaskType())
//                    ||YbChargeSearchConstant.FRAUD_HOSPITAL.equals(bean.getTaskType())
//                    ||YbChargeSearchConstant.DRUG_TOP200.equals(bean.getTaskType())
//                    ||YbChargeSearchConstant.DRUG_TOP200_DETAIL.equals(bean.getTaskType())
//                    ||YbChargeSearchConstant.YEAR_ORG_ONLINE_PATIENT_COUNT.equals(bean.getTaskType())
//            ){
                String orgs = bean.getOrgs();
                String orgids = bean.getOrgids();
                if(StringUtils.isNotBlank(bean.getJsonStr())){
                    bean = JSON.parseObject(bean.getJsonStr(), YbChargeSearchTask.class);
                    //json中医院未拆分,设置为任务信息中的医院
                    bean.setOrgs(orgs);
                    bean.setOrgids(orgids);
                }
//            }
            YbChargeQuerySql tempSql = new  YbChargeQuerySql();
            tempSql.setQuerySql(this.getSqlByTaskBean(bean,querySysPara));
            tempSql.setSheetName(sheetName);
            sqlList.add(tempSql);
        }
        return sqlList;
    }

    //预览50条数据插入数据库
    private void saveResultData(YbChargeSearchTask bean, List<Map<String, Object>> dataList, String taskType) throws Exception{
        if(dataList!=null && dataList.size()>0) {
            switch (taskType){
                case YbChargeSearchConstant.SEARCH:
                case YbChargeSearchConstant.DRUG_TOP200:
                case YbChargeSearchConstant.DRUG_TOP200_DETAIL:
                case YbChargeSearchConstant.DRUG_RULE_STATISTICS:{
                    this.saveResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.YEAR_USERATE_STATISTICS:
                case YbChargeSearchConstant.YEAR_SURGERY_STATISTICS:
                case YbChargeSearchConstant.YEAR_ORG_ONLINE_PATIENT_COUNT:
                case YbChargeSearchConstant.YEAR_STATISTICS:{
                    this.saveYearResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.ITEM_STATISTICS:
                case YbChargeSearchConstant.ITEM_BY_DEPTSTATISTICS:{
                    this.saveItemResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.DEPT_STATISTICS:
                case YbChargeSearchConstant.DATAMINING_SQL_DETAIL:
                case YbChargeSearchConstant.ITEM_BY_VISIT_STATISTICS:{
                    this.saveDeptResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.VISIT_STATISTICS:{
                    this.saveVisitResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.LOW_STATISTICS:{
                    this.saveLowResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.RISK_STATISTICS:
                case YbChargeSearchConstant.ORG_RISK_STATISTICS:
                case YbChargeSearchConstant.ORG_HOLIDAY_RISK_STATISTICS:
                case YbChargeSearchConstant.DIAG_RISK_STATISTICS:{
                    this.saveOverproofResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.PATIENT_RISK_GROUP_STATISTICS:{
                    this.savePatientRiskGroupResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.PATIENT_RISK_STATISTICS:{
                    this.savePatientRiskResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.DOCTOR_RISK_GROUP_STATISTICS:{
                    this.saveDoctorRiskGroupResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.DOCTOR_RISK_STATISTICS:{
                    this.saveDoctorRiskResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.VISIT_TOGETHER_STATISTICS:{
                    this.saveVisitTogetherResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.TAG_STATISTICS:{
                    this.saveTagStatResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.DWB_VISIT_TAG:
                case YbChargeSearchConstant.SUSPICIOUS_GROUP_STATISTICS:{
                    this.saveSuspiciousResultBatch(dataList,bean.getId());
                    break;
                }
                case YbChargeSearchConstant.FRAUD_PROJECT:
                case YbChargeSearchConstant.FRAUD_HOSPITAL:
                case YbChargeSearchConstant.FRAUD_PATIENT:{
                    this.saveFraudResultBatch(dataList,bean.getId());
                }
                case YbChargeSearchConstant.DWS_INHOSPITAL_APART:{
                    this.saveDwsInhospitalApartBatch(dataList,bean.getId());
                }
                case YbChargeSearchConstant.DOCTOR_ADMIT_PATIENT_INSICK:{
                    this.saveDoctorAdmitPatientInsickBatch(dataList,bean.getId());
                }

            }
        }
    }


    private void saveResultBatch(List<Map<String, Object>> dataList,String taskId) throws Exception{
        List<YbChargeSearchResult> resultlist =  new ArrayList<>();
        for(Map<String, Object> map:dataList){
            YbChargeSearchResult resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeSearchResult.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeSearchResult/saveBatch", resultlist);
        }
    }

    private void saveYearResultBatch(List<Map<String, Object>> dataList,String taskId) throws Exception{
        List<YbChargeYearResult> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeYearResult resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeYearResult.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeYearResult/saveBatch", resultlist);
        }
    }

    public void saveItemResultBatch(List<Map<String, Object>> dataList,String taskId) throws Exception {
        List<YbChargeItemResult> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeItemResult resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeItemResult.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeItemResult/saveBatch", resultlist);
        }
    }

    public void saveVisitResultBatch(List<Map<String, Object>> dataList,String taskId) throws Exception{
        List<YbChargeVisitResult> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeVisitResult resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeVisitResult.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeVisitResult/saveBatch", resultlist);
        }
    }

    private void saveDeptResultBatch(List<Map<String, Object>> dataList,String taskId) throws Exception{
        List<YbChargeDeptResult> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeDeptResult resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeDeptResult.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeDeptResult/saveBatch", resultlist);
        }
    }

    private void saveLowResultBatch(List<Map<String, Object>> dataList,String taskId) throws Exception{
        List<YbChargeLowResult> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeLowResult resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeLowResult.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeLowResult/saveBatch", resultlist);
        }
    }

    private void saveOverproofResultBatch(List<Map<String, Object>> dataList,String taskId) throws Exception{
        List<YbChargeOverproofResult> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeOverproofResult resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeOverproofResult.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeOverproofResult/saveBatch", resultlist);
        }
    }

    private void savePatientRiskGroupResultBatch(List<Map<String, Object>> dataList,String taskId) throws Exception{
        List<YbChargePatientRiskGroup> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargePatientRiskGroup resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargePatientRiskGroup.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargePatientRiskGroup/saveBatch", resultlist);
        }
    }

    private void savePatientRiskResultBatch(List<Map<String, Object>> dataList,String taskId) throws Exception{
        List<YbChargePatientRiskResult> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargePatientRiskResult resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargePatientRiskResult.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargePatientRiskResult/saveBatch", resultlist);
        }
    }

    private void saveDoctorRiskGroupResultBatch(List<Map<String, Object>> dataList,String taskId) throws Exception{
        List<YbChargeDoctorRiskGroup> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeDoctorRiskGroup resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeDoctorRiskGroup.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeDoctorRiskGroup/saveBatch", resultlist);
        }
    }

    private void saveDoctorRiskResultBatch(List<Map<String, Object>> dataList,String taskId) throws Exception{
        List<YbChargeDoctorRiskResult> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeDoctorRiskResult resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeDoctorRiskResult.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeDoctorRiskResult/saveBatch", resultlist);
        }
    }

    private void saveHistoryBatch(List<YbChargeSearchHistory> historyList) throws Exception{
        if(historyList.size()>0) {
            //查看是否有需要系统自动添加的关键字
            saveCheckList(historyList);

            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeSearchHistory/saveBatch", historyList);
        }
    }

    private void saveCheckList(List<YbChargeSearchHistory> historyList){
        ArrayList<YbChargeitemChecklist> result = new ArrayList<>();
        for(YbChargeSearchHistory his:historyList){
            List<YbChargeitemChecklist> list=new ArrayList<>();
            String itemname = his.getItemname();
            String itemname1 = his.getItemname1();
            //如果A/B都不为空，且规则库中不存在此查询关键字，则添加到规则库，状态是待审核，添加人为“系统自动”。
            if(StrUtil.isNotEmpty(itemname) && StrUtil.isNotEmpty(itemname1)){
                LambdaQueryWrapper<YbChargeitemChecklist> queryWrapper = new LambdaQueryWrapper();
                queryWrapper.eq(YbChargeitemChecklist::getItemname,itemname);
                queryWrapper.eq(YbChargeitemChecklist::getItemname1,itemname1);
                list = ybChargeitemChecklistMapper.selectList(queryWrapper);
                if(list.size()==0){
                    YbChargeitemChecklist ybChargeitemChecklist = new YbChargeitemChecklist();
                    ybChargeitemChecklist.setItemname(itemname);
                    ybChargeitemChecklist.setPackageItem1(itemname);
                    ybChargeitemChecklist.setItemname1(itemname1);
                    ybChargeitemChecklist.setPackageItem2(itemname1);
                    ybChargeitemChecklist.setExamineStatus("0");
                    ybChargeitemChecklist.setCreatedBy("系统自动");
                    ybChargeitemChecklist.setSorter("系统自动");
                    result.add(ybChargeitemChecklist);
                }

            //如果A不为空，B 为空，则判断查询次数是否>=3，
            // 满足条件，再检查（是否纳入规则）字段是否为否（否表示业务分析人员已经否决过这条规则），不是则添加到规则库
            }else if(StrUtil.isNotEmpty(itemname) && StrUtil.isEmpty(itemname1)){
                LambdaQueryWrapper<YbChargeSearchHistory> hisQueryWrapper = new LambdaQueryWrapper<>();
                hisQueryWrapper.eq(YbChargeSearchHistory::getItemname,itemname);
                hisQueryWrapper.eq(YbChargeSearchHistory::getItemname1,"");
                List<YbChargeSearchHistory> hisList = historyService.list(hisQueryWrapper);
                if(hisList.size()>2){
                    LambdaQueryWrapper<YbChargeitemChecklist> queryWrapper = new LambdaQueryWrapper();
                    queryWrapper.eq(YbChargeitemChecklist::getItemname,itemname);
                    queryWrapper.eq(YbChargeitemChecklist::getItemname1,"");
                    list = ybChargeitemChecklistMapper.selectList(queryWrapper);
                    if(list.size()==0){
                        YbChargeitemChecklist ybChargeitemChecklist = new YbChargeitemChecklist();
                        ybChargeitemChecklist.setItemname(itemname);
                        ybChargeitemChecklist.setPackageItem1(itemname);
                        ybChargeitemChecklist.setItemname1(itemname1);
                        ybChargeitemChecklist.setPackageItem2(itemname1);
                        ybChargeitemChecklist.setExamineStatus("0");
                        ybChargeitemChecklist.setCreatedBy("系统自动");
                        ybChargeitemChecklist.setSorter("系统自动");
                        result.add(ybChargeitemChecklist);
                    }
                }
            }

        }
        ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeitemChecklist/saveBatch", result);

    }

    private void saveVisitTogetherResultBatch(List<Map<String, Object>> dataList,String taskId){
        List<YbChargeVisitTogetherResult> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeVisitTogetherResult resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeVisitTogetherResult.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeVisitTogetherResult/saveBatch", resultlist);
        }
    }

    private void saveTagStatResultBatch(List<Map<String, Object>> dataList,String taskId){
        List<YbChargeMonitorDataminingStat> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeMonitorDataminingStat resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeMonitorDataminingStat.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeMonitorDataminingStat/saveBatch", resultlist);
        }
    }

    private void saveSuspiciousResultBatch(List<Map<String, Object>> dataList,String taskId){
        List<YbChargeSuspiciousResult> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeSuspiciousResult resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeSuspiciousResult.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeSuspiciousResult/saveBatch", resultlist);
        }
    }

    private void saveFraudResultBatch(List<Map<String, Object>> dataList,String taskId){
        List<YbChargeFraudResult> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeFraudResult resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeFraudResult.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeFraudResult/saveBatch", resultlist);
        }
    }

    private void saveDwsInhospitalApartBatch(List<Map<String, Object>> dataList,String taskId){
        List<YbChargeDwsInhospitalApart> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeDwsInhospitalApart resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeDwsInhospitalApart.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeDwsInhospitalApart/saveBatch", resultlist);
        }
    }

    private void saveDoctorAdmitPatientInsickBatch(List<Map<String, Object>> dataList,String taskId){
        List<YbChargeDoctorAdmitPatientInsick> resultlist =  new ArrayList<>();
        int i=1;
        for(Map<String, Object> map:dataList){
            YbChargeDoctorAdmitPatientInsick resultBean = JSON.parseObject(JSON.toJSONString(map), YbChargeDoctorAdmitPatientInsick.class);
            resultBean.setId(UUID.randomUUID().toString());
            resultBean.setTaskId(taskId);
            resultlist.add(resultBean);
        }
        if(resultlist.size()>0) {
            ApiTokenUtil.postBodyApi("/ybChargeSearch/ybChargeDoctorAdmitPatientInsick/saveBatch", resultlist);
        }
    }


    private String getFormatColName(Map<String,String> sqlFieldMap, String colName) {
        colName = StringCamelUtils.underline2Camel(colName.replace("t.",""));
        colName = StringUtils.isNotBlank(sqlFieldMap.get(colName))?sqlFieldMap.get(colName):colName;
        return colName;
    }


    private CellStyle createColTitleCellStyle(Workbook workbook) {
        CellStyle headstyle = workbook.createCellStyle();
        // 设置字体
        Font headfont = workbook.createFont();
        // 字体大小
        headfont.setFontHeightInPoints((short) 10);
        // 加粗
        headfont.setBold(true);
        headstyle.setFont(headfont);
        headstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headstyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        return headstyle;
    }


    private Object formatStr(Object obj){
        if(obj==null||"NULL".equals(obj.toString())){
            return "";
        }
        return obj;
    }


    @Override
    public YbChargeSearchTask deleteYbChargeSearchTaskByRunAgain(String id) {
        YbChargeSearchTask bean = this.baseMapper.selectById(id);
        this.deleteSearchResult(id, bean.getTaskType());

        //修改状态
        bean.setStatus(YbChargeSearchConstant.TASK_WAIT);
        bean.setRecordCount(null);
        bean.setFileSize(null);
        bean.setOverTime(null);
        this.baseMapper.updateById(bean);

        return bean;
    }

    @Override
    @Cacheable(value = CacheConstant.DATASOURCE_DATABASE_CACHE,key = "#dataSource")
    public DatasourceAndDatabaseVO getDatasourceAndDatabase(String dataSource) {
        DatasourceAndDatabaseVO dbVO = new DatasourceAndDatabaseVO();
        //根据dataSource获取项目地配置信息
        Map<String, String> map = new HashMap<>();
        map.put("code", dataSource);
        SysDatasource sysDatasource = ApiTokenUtil.getObj("/system/sysDatasource/queryByCode", map, SysDatasource.class);
        if(sysDatasource==null){
            return null;
        }
        dbVO.setSysDatasource(sysDatasource);

        //根据dbname获取数据源信息
        if(StringUtils.isNotBlank(sysDatasource.getDatabaseSource())){
            map = new HashMap<>();
            map.put("dbname", sysDatasource.getDatabaseSource());
            SysDatabase sysDatabase = ApiTokenUtil.getObj("/system/sysDatabase/queryByDbname", map, SysDatabase.class);
            if(sysDatabase!=null){
                dbVO.setSysDatabase(sysDatabase);
                return dbVO;
            }
        }

        return null;
    }

    @Override
    @CacheEvict(value=CacheConstant.DATASOURCE_DATABASE_CACHE,key = "#dataSource")
    public void clearCacheDatasourceAndDatabase(String dataSource) {

    }

    @Override
    public YbChargeQueryDatabase getQueryDatabase(DatasourceAndDatabaseVO dbVO) throws Exception{
        if(dbVO==null){
            throw new Exception("获取项目地数据源信息失败");
        }
        if(dbVO.getSysDatasource()==null){
            throw new Exception("获取项目地信息失败");
        }
        if(dbVO.getSysDatabase()==null){
            throw new Exception("获取项目地关联数据源信息失败");
        }
        YbChargeQueryDatabase querySysPara = new YbChargeQueryDatabase();
        querySysPara.setDataStoreVersion(dbVO.getSysDatasource().getDataVersion());
        String project = dbVO.getSysDatasource().getDataProject();
        if(StringUtils.isBlank(project)){
            project = dbVO.getSysDatasource().getCode();
        }
        querySysPara.setDataStoreProject(project);
        querySysPara.setDbtype(dbVO.getSysDatabase().getDbtype());
        return querySysPara;
    }

    @Override
    public IPage<Map<String, Object>> getSearchTaskCount(YbChargeSearchTaskCountVo chargeSearchTaskCountVo, Page<YbChargeSearchTask> page) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String systemCode = loginUser.getSystemCode();
        List<SysDatasource> list = sysDatasourceService.list(new LambdaQueryWrapper<SysDatasource>().eq(SysDatasource::getSystemCode, systemCode).or().isNull(SysDatasource::getSystemCode).or().eq(SysDatasource::getSystemCode, ""));
        StringBuffer sql = new StringBuffer();
        for(int i=0;i<list.size();i++){
            SysDatasource datasource = list.get(i);
            sql.append("SUM(IF(dataSource = '"+datasource.getCode()+"',dataNum,0)) AS "+datasource.getCode());

            if(i !=list.size()-1){
                sql.append(",");
            }
        }
        IPage<Map<String,Object>> result=ybChargeSearchTaskMapper.getUseCountPage(page,chargeSearchTaskCountVo,sql.toString());
        List<Map<String, Object>> records = result.getRecords();

        if(records.size()>0){
            //列数据统计
            for(Map<String, Object> map:records){
                int totalNum=0;
                for(SysDatasource datasource:list){
                    String code = datasource.getCode();
                    int num = Integer.parseInt(map.get(code).toString());
                    totalNum +=num;
                }
                map.put("total",totalNum);

            }
            //行数据统计
            HashMap<String, Object> cellCount = new HashMap<>();
            cellCount.put("weekStart","合计");
            for(SysDatasource datasource:list){
                String code = datasource.getCode();
                IntSummaryStatistics collect = records.stream().collect(Collectors.summarizingInt(b -> Integer.parseInt(b.get(code).toString())));
                int sum = (int)collect.getSum();
                cellCount.put(code,sum);
            }
            IntSummaryStatistics collect = records.stream().collect(Collectors.summarizingInt(b -> Integer.parseInt(b.get("total").toString())));
            int total = (int)collect.getSum();
            cellCount.put("total",total);

            records.add(cellCount);



            for(Map<String, Object> map:records){
                Set<Map.Entry<String, Object>> entries = map.entrySet();
                for(Map.Entry<String,Object> entry:entries){
                    Object value = entry.getValue();
                    String key = entry.getKey();
                    if(ObjectUtil.isEmpty(value) || String.valueOf(value).equals("0") || String.valueOf(value).equals("0.0") || String.valueOf(value).equals("0.00")){
                        map.put(key,"");
                    }
                }
            }

        }

        return result;
    }

    @Override
    public List<Map<String,Object>> getSearchTaskCountList(YbChargeSearchTaskCountVo chargeSearchTaskCountVo) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String systemCode = loginUser.getSystemCode();
        List<SysDatasource> list = sysDatasourceService.list(new LambdaQueryWrapper<SysDatasource>().eq(SysDatasource::getSystemCode, systemCode).or().isNull(SysDatasource::getSystemCode).or().eq(SysDatasource::getSystemCode, ""));
        StringBuffer sql = new StringBuffer();
        for(int i=0;i<list.size();i++){
            SysDatasource datasource = list.get(i);
            sql.append("SUM(IF(dataSource = '"+datasource.getCode()+"',dataNum,0)) AS "+datasource.getCode());

            if(i !=list.size()-1){
                sql.append(",");
            }
        }
        List<Map<String,Object>> result=ybChargeSearchTaskMapper.getUseCountList(chargeSearchTaskCountVo,sql.toString());

       if(result.size()>0){
           //列数据统计
           for(Map<String, Object> map:result){
               int totalNum=0;
               for(SysDatasource datasource:list){
                   String code = datasource.getCode();
                   int num = Integer.parseInt(map.get(code).toString());
                   totalNum +=num;
               }
               map.put("total",totalNum);

           }
           //行数据统计
           HashMap<String, Object> cellCount = new HashMap<>();
           cellCount.put("weekStart","合计");
           for(SysDatasource datasource:list){
               String code = datasource.getCode();
               IntSummaryStatistics collect = result.stream().collect(Collectors.summarizingInt(b -> Integer.parseInt(b.get(code).toString())));
               int sum = (int)collect.getSum();
               cellCount.put(code,sum);
           }
           IntSummaryStatistics collect = result.stream().collect(Collectors.summarizingInt(b -> Integer.parseInt(b.get("total").toString())));
           int total = (int)collect.getSum();
           cellCount.put("total",total);

           result.add(cellCount);

       }


        return result;
    }

    @Override
    public IPage<Map<String, Object>> getSearchTaskFunCount(YbChargeSearchTaskFunCountVo chargeSearchTaskFunCountVo, Page<YbChargeSearchTask> page) {
        IPage<Map<String,Object>> result=ybChargeSearchTaskMapper.getSearchTaskFunCount(page,chargeSearchTaskFunCountVo);
        return result;
    }

    @Override
    public List<Map<String, Object>> getSearchTaskFunCountList(YbChargeSearchTaskFunCountVo chargeSearchTaskFunCountVo) {
        List<Map<String, Object>> result=ybChargeSearchTaskMapper.getSearchTaskFunCountList(chargeSearchTaskFunCountVo);
        return result;
    }

    @Override
    public IPage<Map<String,Object>> getUseCountPage(Page<YbChargeSearchTask> page, YbChargeSearchTaskCountVo chargeSearchTaskCountVo,String sql) {
        IPage<Map<String,Object>> result=ybChargeSearchTaskMapper.getUseCountPage(page,chargeSearchTaskCountVo,sql);
        return result;
    }

    @Override
    public IPage<Map<String, Object>> querySearchResult(YbChargeSearchTask bean, DatasourceAndDatabaseVO dbVO, Page<Map<String, Object>> page) throws Exception{
        YbChargeQueryDatabase queryDatabase = this.getQueryDatabase(dbVO);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rSet = null;
        List<YbChargeQuerySql> sqlList = getSqlListByTaskBean(bean,queryDatabase,"");
        try {
            if(sqlList.size()>0){
                String sql = sqlList.get(0).getQuerySql();
                if(StringUtils.isBlank(sql)){
                    throw new Exception("未获取到sql语句");
                }
                conn = JDBCUtil.getDbConnection(dbVO.getSysDatabase());

                //判断下SQL中是否有分号，如果有，则需要将创建临时表和查询分开
                int index = sql.lastIndexOf(";");
                if(index>0){
                    String querySql = sql.substring(index +1).trim();

                    //如果查询SQL不为空，则需先执行创建临时表SQL
                    if("".equalsIgnoreCase(querySql) == false){
                        String executeSql = sql.substring(0 ,index);
                        sql = querySql;

                        pstmt = conn.prepareStatement(executeSql);
                        pstmt.execute();

                    }
                }

                //获取总条数
                pstmt = conn.prepareStatement("select count(1) as count from ("+sql+") t");
                rSet = pstmt.executeQuery();
                long count = 0;
                while (rSet.next()) {
                    count = rSet.getLong("count");
                    break;
                }
                String taskType = bean.getTaskType();
                String pageOrderField = "";
                String fieldStr = "";
                Map<String,String> sqlFieldMap = new HashMap<>();
                Map<String,String> formatMap = new HashMap<>();
                if(YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType)!=null){
                    pageOrderField = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getPageOrderField();
                    fieldStr = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getFieldStr();
                    sqlFieldMap =  YbChargeSearchConstant.getSqlFieldMap(taskType);//字段映射
                    formatMap = YbChargeSearchConstant.getFormatMap(taskType);//值转换
                }else{
                    throw new Exception("参数异常");
                }

                if(StringUtils.isNotBlank(bean.getOrderby())){
                    pageOrderField = bean.getOrderby();
                }

                String [] fieldArr = fieldStr.split(",");
                if(count>0){

                    String pageSql = "select * from (select * ,row_number() over (order by "+pageOrderField+") as rownum  from ("+sql+") t ) t where t.rownum between "+((page.getCurrent()-1)*page.getSize())+" and "+(page.getCurrent()*page.getSize())+" ";
                    if(YbChargeSearchConstant.DB_TYPE_GREENPLUM.equalsIgnoreCase(queryDatabase.getDbtype())||
                            YbChargeSearchConstant.DB_TYPE_MYSQL.equalsIgnoreCase(queryDatabase.getDbtype())){
                        pageSql = "select * from ("+ sql + ") t order by "+pageOrderField+" limit "+page.getSize()+" offset "+((page.getCurrent()-1)*page.getSize());
                    }
                    System.out.println(pageSql);
                    pstmt = conn.prepareStatement(pageSql);
                    rSet = pstmt.executeQuery();
                    List<String> colNameList = new ArrayList<>();
                    int colNum = 0;
                    //查询结果的字段
                    if(colNameList.size()==0){
                        colNum = rSet.getMetaData().getColumnCount();
                        for (int j = 1; j <= colNum; j++) {
                            String colName = rSet.getMetaData().getColumnLabel(j);
                            //字段名处理映射
                            colName = getFormatColName(sqlFieldMap, colName);
                            colNameList.add(colName);
                        }
                    }
                    List<Map<String, Object>> list = new ArrayList<>();
                    while (rSet.next()) {
                        Map<String, Object> beanMap=new HashMap<String, Object>();
                        for (int j = 1; j <= colNum; j++) {
                            String colName = colNameList.get(j-1);
                            Object value = rSet.getObject(j);
                            value = formatStr(rSet.getObject(j));
                            value = YbChargeSearchConstant.getFormatValue(formatMap,colName, value);
                            beanMap.put(colName, value!=null?value.toString():"");
                        }
                        list.add(beanMap);
                    }
                    IPage<Map<String, Object>> pageList = new Page<>();
                    pageList.setTotal(count);
                    pageList.setRecords(list);
                    return pageList;

                }
                return new Page<>();


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //删除临时表
                String tempTableSql = this.getDropTempTablSql(sqlList);
                if(StringUtils.isNotBlank(tempTableSql)){
                    pstmt = conn.prepareStatement(tempTableSql);
                    pstmt.execute();
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            JDBCUtil.destroy(rSet,conn,pstmt);
        }

        return new Page<>();
    }


    @Override
    public void createYearSql(DatasourceAndDatabaseVO dbVO) throws Exception {
        YbChargeQueryDatabase querySysPara = getQueryDatabase(dbVO);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rSet = null;
        try {
            List<String> sqlList=GenHiveQuerySqlTools.genCreateYearSql(querySysPara);
            conn = JDBCUtil.getDbConnection(dbVO.getSysDatabase());
            for(String sql:sqlList){
                if (StringUtils.isBlank(sql)) {
                    throw new Exception("未获取到sql语句");
                }
                System.out.println("execute sql:"+sql);
                pstmt = conn.prepareStatement(sql);
                pstmt.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtil.destroy(rSet,conn,pstmt);
        }

    }

}
