package com.ai.modules.ybChargeSearch.service.impl;

import com.ai.common.utils.IdUtils;
import com.ai.common.utils.StringCamelUtils;
import com.ai.modules.engine.util.JDBCUtil;
import com.ai.modules.ybChargeSearch.entity.YbChargeDrugRule;
import com.ai.modules.ybChargeSearch.entity.YbChargeSearchTask;
import com.ai.modules.ybChargeSearch.service.IYbChargeTagLabelService;
import com.ai.modules.ybChargeSearch.vo.DatasourceAndDatabaseVO;
import com.ai.modules.ybChargeSearch.vo.YbChargeSearchConstant;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class YbChargeTagLabelServiceImpl implements IYbChargeTagLabelService {

    private static int batchSize = 100;
    private static int executeBatchSize = 1000;

    @Override
    public List<Map<String,Object>> checkImportExcel(List<List<String>> list,String taskType,boolean checkFlag) throws Exception {
        //获取表格标题
        List<String> titles = list.remove(0);
        String[] titleArr = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getTitleStr().split(",");
        String[] fieldArr = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getFieldStr().split(",");
        Map<Integer, String> titleLinkIndexMap = new HashMap<>();
        for(int i=0;i<titleArr.length;i++){
            String title = titleArr[i];
            int index = titles.indexOf(title);
            if(index!=-1){
                titleLinkIndexMap.put(index,fieldArr[i]);
            }
        }
        if(checkFlag){
            if(titles.indexOf("线索价值(有用/无用)")==-1){
                throw new Exception("缺少“线索价值(有用/无用)”列");
            }
        }
        //非空校验
        List<Map<String,Object>> dataList = new ArrayList<>();
        int i=0;
        StringBuffer message = new StringBuffer();
        for (List<String> record : list) {
            Map<String,Object> data = new HashMap<>();
            titleLinkIndexMap.forEach((key,value)->{
                data.put(value, key < record.size() ? record.get(key) : "");
            });
            if(checkFlag){
                if(StringUtils.isBlank(data.get("labelName").toString())){
                    message.append("第" + (i + 2) + "行线索价值(有用/无用)为空\n");
                    continue;
                }else{
                    if(!"有用".equals(data.get("labelName").toString())&&!"无用".equals(data.get("labelName").toString())){
                        message.append("第" + (i + 2) + "行线索价值(有用/无用)为值不正确,应为有用或者无用\n");
                        continue;
                    }
                }
            }
            dataList.add(data);
            i++;
        }
        if(message.length() != 0){
            throw new Exception(message.toString());
        }
        return dataList;
    }

    @Override
    public int getExistCount(List<Map<String, Object>> dataList,DatasourceAndDatabaseVO dbVO,String taskType) throws Exception {
        String targetTable = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getLableTargetTable();
        int existCount = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rSet = null;
        try {
            conn = JDBCUtil.getDbConnection(dbVO.getSysDatabase());
            StringBuilder sql = new StringBuilder();
            //获取关联字段
            String[] allCols = YbChargeSearchConstant.LABEL_TABLE_MAP.get(targetTable);
            if(allCols!=null&&allCols.length==2){
                String cols = allCols[0];
                String[] excelCols = allCols[1].split(",");
                sql.append("select count(1) as count from medical."+targetTable.replace("_label2","_label")+" t \n") ;
                sql.append("where EXISTS (select 1 from  (values %s ) \n") ;
                sql.append("as tmp("+cols+") \n") ;
                sql.append("where 1=1 ");
                for(String colname: cols.split(",") ){
                    sql.append("and t."+colname+"=tmp."+colname+" ");
                }

                sql.append(" ) ");
                List<String> values = new ArrayList<>();
                String valueStr = "";
                for(int i = 0; i < dataList.size(); i++) {
                    for(String excelCol: excelCols ){
                        if(StringUtils.isNotBlank(valueStr)){
                            valueStr +=",";
                        }
                        valueStr += "'"+dataList.get(i).get(excelCol)+"'";
                    }
                    values.add("(" +valueStr+") \n");
                    valueStr = "";
                    if ((i + 1) % batchSize == 0 || i + 1 == dataList.size()) {
                        pstmt = conn.prepareStatement(String.format(sql.toString(),StringUtils.join(values, ",")));
                        rSet = pstmt.executeQuery();
                        while(rSet.next()){
                            existCount +=rSet.getInt("count");
                        }
                        values = new ArrayList<>();
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            JDBCUtil.destroy(rSet,conn,pstmt);
        }
        return existCount;
    }

    @Override
    public void deleteExistData(List<Map<String, Object>> dataList,DatasourceAndDatabaseVO dbVO,String taskType) throws Exception {
        String targetTable = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getLableTargetTable();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rSet = null;
        try {
            conn = JDBCUtil.getDbConnection(dbVO.getSysDatabase());
            StringBuilder sql = new StringBuilder();
            //获取关联字段
            String[] allCols = YbChargeSearchConstant.LABEL_TABLE_MAP.get(targetTable);
            if(allCols!=null&&allCols.length==2){
                String cols = allCols[0];
                String[] excelCols = allCols[1].split(",");
                sql.append("delete from medical."+targetTable.replace("_label2","_label")+" t \n") ;
                sql.append("where EXISTS (select 1 from  (values %s ) \n") ;
                sql.append("as tmp("+cols+") \n") ;
                sql.append("where 1=1 ");
                for(String colname: cols.split(",") ){
                    sql.append("and t."+colname+"=tmp."+colname+" ");
                }

                sql.append(" ) ");
                List<String> values = new ArrayList<>();
                String valueStr = "";
                for(int i = 0; i < dataList.size(); i++) {
                    for(String excelCol: excelCols ){
                        if(StringUtils.isNotBlank(valueStr)){
                            valueStr +=",";
                        }
                        valueStr += "'"+dataList.get(i).get(excelCol)+"'";
                    }
                    values.add("(" +valueStr+") \n");
                    valueStr = "";
                    if ((i + 1) % batchSize == 0 || i + 1 == dataList.size()) {
                        pstmt = conn.prepareStatement(String.format(sql.toString(),StringUtils.join(values, ",")));
                        pstmt.execute();
                        values = new ArrayList<>();
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            JDBCUtil.destroy(rSet,conn,pstmt);
        }
    }

    @Override
    public void insertImportData(List<Map<String, Object>> dataList,DatasourceAndDatabaseVO dbVO,String taskType) throws Exception {
        String targetTable = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getLableTargetTable();
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rSet = null;
        try {

            conn = JDBCUtil.getDbConnection(dbVO.getSysDatabase());
            //获取关联字段
            String[] allCols = YbChargeSearchConstant.LABEL_TABLE_MAP.get(targetTable);
            if(allCols!=null&&allCols.length==2){
                String cols = allCols[0];
                String[] excelCols = allCols[1].split(",");
                StringBuilder sql = new StringBuilder();
                sql.append("insert into medical." + targetTable.replace("_label2","_label")+"("+cols+",label_user,label_name,label_time )  \n") ;
                sql.append("select "+cols+",");
                sql.append("'" + user.getRealname() + "' as label_user, label_name,'" + DateUtils.now() + "' as label_time  from  \n");
                sql.append("(values %s ) as \n");
                sql.append("tmp("+cols+",label_name) \n");
                List<String> values = new ArrayList<>();
                String valueStr = "";
                for(int i = 0; i < dataList.size(); i++) {
                    for(String excelCol: excelCols ){
                        if(StringUtils.isNotBlank(valueStr)){
                            valueStr +=",";
                        }
                        valueStr += "'"+dataList.get(i).get(excelCol)+"'";
                    }
                    valueStr += ",'"+dataList.get(i).get("labelName")+"'";
                    values.add("(" +valueStr+") \n");
                    valueStr = "";
                    if ((i + 1) % batchSize == 0 || i + 1 == dataList.size()) {
                        pstmt = conn.prepareStatement(String.format(sql.toString(),StringUtils.join(values, ",")));
                        pstmt.execute();
                        values = new ArrayList<>();
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            JDBCUtil.destroy(rSet,conn,pstmt);
        }
    }

    @Override
    @Transactional
    public void deleteAndInsertImportData(List<Map<String, Object>> dataList, DatasourceAndDatabaseVO dbVO, String taskType) throws Exception {
        this.deleteExistData(dataList,dbVO,taskType);
        this.insertImportData(dataList,dbVO,taskType);
    }



    private List<String> getColumnNameList(Connection conn,PreparedStatement pstmt,ResultSet rSet,String sourceTable) throws SQLException {
        //获取表元数据
        String sql = "SELECT column_name FROM information_schema.columns \n" +
                " WHERE table_name = '"+ sourceTable+"' and table_schema='medical'";
        pstmt = conn.prepareStatement(sql);
        rSet = pstmt.executeQuery();
        List<String> columnNameList = new ArrayList<>();
        while(rSet.next()){
            columnNameList.add(rSet.getString("column_name"));
        }
        return columnNameList;
    }

    private String getExtendColValue(List<YbChargeSearchConstant.ColsInfo> extendColList,List<Map<String, Object>> dataList,int i){
        StringBuilder sb = new StringBuilder();
        for(YbChargeSearchConstant.ColsInfo item:extendColList) {
            if(YbChargeSearchConstant.FORMAT_VALUE_ROUND_HALF_UP2.equals(item.getFormatValue())||YbChargeSearchConstant.FORMAT_VALUE_INT.equals(item.getFormatValue())){
                sb.append(","+dataList.get(i).get(item.getField()));
            }else{
                sb.append(",'"+dataList.get(i).get(item.getField())+"'");
            }
        }
        return sb.toString();
    }


    @Override
    public int dwbVisitTagCountByTagname(DatasourceAndDatabaseVO dbVO,YbChargeSearchTask ybChargeSearchTask, String tagName,String tagId)  throws Exception{
        StringBuilder sb = new StringBuilder();
        sb.append("select count(1) as count from medical.dwb_visitid_tag t where 1=1 ");
        sb.append("and tag_type_name = '数据挖掘'");
        sb.append("and project = '"+this.getProject(dbVO)+"'");
        sb.append("and task_id is not  null ");
        sb.append("and tag_name = '"+tagName+"'");
        if(StringUtils.isNotBlank(ybChargeSearchTask.getOrgs())){
            sb.append("and orgname = '"+ybChargeSearchTask.getOrgs()+"'");
        }
        String itemname = this.getItemname(ybChargeSearchTask);
        if(StringUtils.isNotBlank(itemname)){
            sb.append("and itemname ~ '"+itemname+"'");
        }

        System.out.println(sb.toString());

        int existCount = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rSet = null;
        try {
            conn = JDBCUtil.getDbConnection(dbVO.getSysDatabase());
            pstmt = conn.prepareStatement(sb.toString());
            rSet = pstmt.executeQuery();
            while(rSet.next()){
                existCount = rSet.getInt("count");
            }
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            JDBCUtil.destroy(rSet,conn,pstmt);
        }
        return existCount;
    }

    @Override
    public void deleteExistDwbVisitTagAndImport(List<Map<String,Object>> dataList,DatasourceAndDatabaseVO dbVO, YbChargeSearchTask ybChargeSearchTask, String tagName, String tagId) throws Exception {
      this.deleteExistDwbVisitTag(dataList,dbVO,ybChargeSearchTask,tagName);
      this.dwbVisitTagImport(dataList,dbVO,ybChargeSearchTask,tagName,tagId);
    }

    private void deleteExistDwbVisitTag(List<Map<String,Object>> dataList,DatasourceAndDatabaseVO dbVO, YbChargeSearchTask ybChargeSearchTask,String tagName) throws Exception {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rSet = null;
        try {
            conn = JDBCUtil.getDbConnection(dbVO.getSysDatabase());
            StringBuilder sb = new StringBuilder();
            sb.append("delete from medical.dwb_visitid_tag t where 1=1 \n") ;
            sb.append("and tag_type_name = '数据挖掘'");
            sb.append("and project = '"+this.getProject(dbVO)+"'");
            sb.append("and task_id is not  null ");
            sb.append("and tag_name = '"+tagName+"'");
            if(StringUtils.isNotBlank(ybChargeSearchTask.getOrgs())){
                sb.append("and orgname = '"+ybChargeSearchTask.getOrgs()+"'");
            }
            String itemname = this.getItemname(ybChargeSearchTask);
            if(StringUtils.isNotBlank(itemname)){
                sb.append("and itemname ~ '"+itemname+"'");
            }
            pstmt = conn.prepareStatement(sb.toString());
            pstmt.execute();

           /* sql.append("where  tag_type_name = '数据挖掘' and project = '"+this.getProject(dbVO)+"' \n") ;
            sql.append(" and EXISTS (select 1 from  (values %s ) \n") ;
            sql.append("as tmp(orgid,visitid,tag_name) \n") ;
            sql.append("where t.orgid = tmp.orgid and t.visitid=tmp.visitid and t.tag_name=tmp.tag_name ) ");
            List<String> values = new ArrayList<>();
            for(int i = 0; i < dataList.size(); i++) {
                values.add("('" + dataList.get(i).get("orgid") + "','"+dataList.get(i).get("orgname")+"','"+dataList.get(i).get("visitid")+"','"+tagName+"') \n");
                if ((i + 1) % batchSize == 0 || i + 1 == dataList.size()) {
                    pstmt = conn.prepareStatement(String.format(sql.toString(),StringUtils.join(values, ",")));
                    pstmt.execute();
                    values = new ArrayList<>();
                }
            }*/
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            JDBCUtil.destroy(rSet,conn,pstmt);
        }
    }

    @Override
    public void dwbVisitTagImport(List<Map<String,Object>> dataList,DatasourceAndDatabaseVO dbVO, YbChargeSearchTask ybChargeSearchTask, String tagName, String tagId) throws Exception {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rSet = null;
        String etlSource = "A01";
        if("his".equalsIgnoreCase(ybChargeSearchTask.getEtlSource())){
            etlSource = "A03";
        }
        try {

            conn = JDBCUtil.getDbConnection(dbVO.getSysDatabase());
            List<String> values = new ArrayList<>();
            StringBuilder sql = new StringBuilder();
            sql.append("insert into medical.dwb_visitid_tag(id,orgid,orgname,visitid,orgname_src,visitid_src," +
                    "tag_type_name,tag_id,tag_name,itemname,remark,task_id,etl_source,data_etl_time,tag_time,project  )  \n") ;
            sql.append("select tmp.id as id,tmp.orgid as orgid,tmp.orgname as orgname,tmp.visitid as visitid,tmp.orgname as orgname_src,tmp.visitid as visitid_src,\n");
            sql.append("'数据挖掘' as tag_type_name,'"+tagId+"' as tag_id,'"+tagName+"' as tag_name," +
                    "tmp.itemname as itemname,'"+this.getWgRemark(ybChargeSearchTask)+"' as remark,'"+ybChargeSearchTask.getId()+"' as task_id,'"+etlSource+"' as etl_source," +
                    "'" + DateUtils.now() + "' as data_etl_time,'" + DateUtils.now() + "' as tag_time,'"+this.getProject(dbVO)+"' as project \n");
            sql.append("from (values %s ) as \n");
            sql.append("tmp(id,orgid,orgname,visitid,itemname) \n");
            System.out.println(sql);
            for(int i = 0; i < dataList.size(); i++) {
                values.add("('"+IdUtils.uuid()+"','" + dataList.get(i).get("orgid") + "','" + dataList.get(i).get("orgname") + "','"+dataList.get(i).get("visitid")+"','"+dataList.get(i).get("itemname")+"') \n");
                if ((i + 1) % batchSize == 0 || i + 1 == dataList.size()) {
                    pstmt = conn.prepareStatement(String.format(sql.toString(),StringUtils.join(values, ",")));
                    pstmt.execute();
                    values = new ArrayList<>();
                }
            }

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            JDBCUtil.destroy(rSet,conn,pstmt);
        }
    }




    @Override
    public List<Map<String, Object>> getTaskFileData(List<List<String>> list, String taskType) throws Exception {
        //获取表格标题
        List<String> titles = list.remove(0);
        String[] titleArr = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getTitleStr().split(",");
        String[] fieldArr = YbChargeSearchConstant.TASK_TYPE_MAP.get(taskType).getFieldStr().split(",");
        Map<Integer, String> titleLinkIndexMap = new HashMap<>();
        for(int i=0;i<titleArr.length;i++){
            String title = titleArr[i];
            int index = titles.indexOf(title);
            titleLinkIndexMap.put(index,fieldArr[i]);
        }
        List<Map<String,Object>> dataList = new ArrayList<>();
        for (List<String> record : list) {
            Map<String,Object> data = new HashMap<>();
            titleLinkIndexMap.forEach((key,value)->{
                data.put(value, key < record.size() ? record.get(key) : "");
            });
            dataList.add(data);
        }
        return dataList;
    }

    private String getProject(DatasourceAndDatabaseVO dbVO) {
        String project = dbVO.getSysDatasource().getDataProject();
        //project = StringUtils.replace(project,"__gp" ,"");
        return project;
    }

    private String getItemname(YbChargeSearchTask ybChargeSearchTask) {
        List<String> itemnames = new ArrayList<>();
        //药品
        if(YbChargeSearchConstant.SEARCH.equals(ybChargeSearchTask.getTaskType())){
            List<YbChargeSearchTask> data = JSONObject.parseArray(ybChargeSearchTask.getJsonStr(), YbChargeSearchTask.class);
            itemnames = data.stream().filter(t-> StringUtils.isNotBlank(t.getItemname())||StringUtils.isNotBlank(t.getHisItemName())).map(t->{
                if(StringUtils.isBlank(t.getItemname())){
                    return t.getHisItemName();
                }
                return t.getItemname();
            }).collect(Collectors.toList());
        }else if(YbChargeSearchConstant.DRUG_RULE_STATISTICS.equals(ybChargeSearchTask.getTaskType())){
            List<YbChargeDrugRule> drugdata = JSONObject.parseArray(ybChargeSearchTask.getJsonStr(), YbChargeDrugRule.class);
            itemnames =drugdata.stream().filter(t-> StringUtils.isNotBlank(t.getDrugName())).map(t->t.getDrugName()).collect(Collectors.toList());
        }
        String itemname = itemnames.stream().map(String::valueOf).collect(Collectors.joining("|"));
        if(StringUtils.isNotBlank(itemname)){
            itemname = StringUtils.replace(itemname,"#" ,"|");
        }
        return itemname;
    }

    private String getWgRemark(YbChargeSearchTask ybChargeSearchTask) {
        String wgRemark = "";
        //药品
        if(YbChargeSearchConstant.SEARCH.equals(ybChargeSearchTask.getTaskType())){
            List<YbChargeSearchTask> data = JSONObject.parseArray(ybChargeSearchTask.getJsonStr(), YbChargeSearchTask.class);
           if(data.size()>0){
               wgRemark = data.get(0).getWgRemark();
           }
        }else if(YbChargeSearchConstant.DRUG_RULE_STATISTICS.equals(ybChargeSearchTask.getTaskType())){
            List<YbChargeDrugRule> drugdata = JSONObject.parseArray(ybChargeSearchTask.getJsonStr(), YbChargeDrugRule.class);
            if(drugdata.size()>0){
                wgRemark = drugdata.get(0).getRemark().toString();
            }
        }
        return wgRemark;
    }
}
