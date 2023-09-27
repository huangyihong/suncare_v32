/**
 * EngineServiceImpl.java	  V1.0   2019年11月29日 上午11:06:14
 * <p>
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 * <p>
 * Modification history(By    Time    Reason):
 * <p>
 * Description:
 */

package com.ai.modules.review.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.ai.common.MedicalConstant;
import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.*;
import com.ai.modules.api.util.ApiTokenCommon;
import com.ai.modules.engine.service.IEngineCaseService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.entity.NewV3Tmp;
import com.ai.modules.review.mapper.NewV3TmpMapper;
import com.ai.modules.review.service.IReviewNewPushService;
import com.ai.modules.review.vo.DwbMasterInfoVo;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.ai.modules.task.service.ITaskProjectService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.MD5Util;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ReviewNewPushServiceImpl implements IReviewNewPushService {

    //数据库用户名
    @Value("${spring.datasource.dynamic.datasource.master.username}")
    private String dbUserName;

    //数据库密码
    @Value("${spring.datasource.dynamic.datasource.master.password}")
    private String dbPassword;

    //SQLLDR_DBURL数据库连接串
    @Value("${spring.datasource.dynamic.datasource.master.url}")
    String dbUrl;

    //sqlldrCTRL文件夹
    @Value("${sqlldr.ctrl.folder:/home/web/sqlLoader/ctrlfolder}")
    String sqlldrCtrlFolder;

    //sqlldr工作文件夹
    @Value("${sqlldr.work.folder:/home/web/sqlLoader/workfolder}")
    String sqlldrWorkFolder;

    @Autowired
    private NewV3TmpMapper newV3TmpMapper;

    @Autowired
    private ITaskProjectBatchService batchService;

    @Autowired
    private IEngineCaseService engineCaseService;

    @Autowired
    private ITaskProjectService taskProjectService;

    @Autowired
    private IApiTaskService taskSV;

    private int batchSize = 100;

    private String[] csvFields = new String[]{"tmpId", "issueId", "issueName", "xmkhId", "xmkhName", "xmmcId", "xmmcName", "wgxmmcId", "wgxmmcName", "taskBatchId", "taskBatchName", "visitid", "oldVisitid",
            "handleStatus", "bhgxwlxId", "bhgxwlxName", "bhgxwmcId", "bhgxwmcName", "bhgxwsy", "totalfee2", "fundpay", "drugfee", "sjje", "jgjy", "hospitalId", "hospitalName", "deptid", "deptname", "totalfee",
            "clientid", "clientname", "sex", "age", "visittype", "visitdate", "zdjbmc", "result", "zyDays", "leavedate", "leavereason", "payway", "createBy", "createByName", "createTime", "updateBy", "updateName", "updateTime",
            "doctorid", "doctorname", "dataSource", "insurancetype", "zyDaysCalculate", "maxActionMoney"};

    @Override
    public void pushBatchBySolr(SolrQuery solrQuery, NewV3Tmp tmpBean) throws Exception {
        exportMedicalUnreasonableAction(solrQuery, tmpBean);
    }

    @Override
    public void exportActionSolrMain(String batchId, boolean isProject) throws Exception {
        if (isProject) {
            SolrQuery solrQuery = new SolrQuery("*:*");
            solrQuery.addFilterQuery("PROJECT_ID:" + batchId);
            solrQuery.addFilterQuery("MUTEX_ITEM_CODE:?*");
            NewV3Tmp tmpBean = new NewV3Tmp();
            tmpBean.setXmmcId(batchId);
            exportActionByProject(solrQuery, tmpBean);
        } else {
            SolrQuery solrQuery = new SolrQuery("*:*");
            solrQuery.addFilterQuery("BATCH_ID:" + batchId);
            NewV3Tmp tmpBean = new NewV3Tmp();
            tmpBean.setTaskBatchId(batchId);
            exportMedicalUnreasonableAction(solrQuery, tmpBean);
        }
    }

    private void batchInsetNewsTmp(HashMap<String, NewV3Tmp> addTmpMap, BufferedWriter csvWriter, String filename) throws Exception {
        //获取主表信息
        Set<String> visitidList = new HashSet();
        addTmpMap.forEach((key, addTmpBean) -> {
            visitidList.add(addTmpBean.getVisitid());
        });
        String visitIdFq = "VISITID:(\"" + StringUtil.join(visitidList, "\",\"") + "\")";
        SolrQuery solrQuery1 = new SolrQuery("*:*");
        solrQuery1.addFilterQuery(visitIdFq);
        solrQuery1.setFields("VISITID", "FUNDPAY", "DRUGFEE", "RESULT", "ZY_DAYS", "ZY_DAYS_CALCULATE", "LEAVEREASON", "PAYWAY");
        solrQuery1.setRows(batchSize);
        SolrDocumentList masterList = SolrUtil.call(solrQuery1, EngineUtil.DWB_MASTER_INFO).getResults();
        Map<String, SolrDocument> masterMap = new HashMap<>();
        for (SolrDocument masterDoc : masterList) {
            masterMap.put(masterDoc.getFieldValue("VISITID").toString(), masterDoc);
        }
        List<NewV3Tmp> addUpdateList = new ArrayList<NewV3Tmp>();
        List<NewV3Tmp> deleteList = new ArrayList<NewV3Tmp>();
        addTmpMap.forEach((key, addTmpBean) -> {
            SolrDocument masterDoc = masterMap.get(addTmpBean.getVisitid());
            Object fundpay = masterDoc.getFieldValue("FUNDPAY");
            Object drugfee = masterDoc.getFieldValue("DRUGFEE");
            Object result = masterDoc.getFieldValue("RESULT");
            Object zy_days = masterDoc.getFieldValue("ZY_DAYS");
            Object zy_days_calculate = masterDoc.getFieldValue("ZY_DAYS_CALCULATE");
            Object leavereason = masterDoc.getFieldValue("LEAVEREASON");
            Object payway = masterDoc.getFieldValue("PAYWAY");
            if (fundpay != null) {
                addTmpBean.setFundpay(new BigDecimal(fundpay.toString()));
            }
            if (drugfee != null) {
                addTmpBean.setDrugfee(new BigDecimal(drugfee.toString()));
            }
            if (result != null) {
                addTmpBean.setResult(result.toString());
            }
            if (zy_days != null) {
                addTmpBean.setZyDays(zy_days.toString());
            }
            if (zy_days_calculate != null) {
                addTmpBean.setZyDaysCalculate(zy_days_calculate.toString());
            }
            if (leavereason != null) {
                addTmpBean.setLeavereason(leavereason.toString());
            }
            if (payway != null) {
                addTmpBean.setPayway(payway.toString());
            }
            addUpdateList.add(addTmpBean);
        });
        //newTmpService.saveOrUpdateBatch(addUpdateList);

        Pattern p = Pattern.compile("\\t|\r|\n");
        StringBuffer sb = new StringBuffer();
        for (NewV3Tmp bean : addUpdateList) {
            JSONObject jsonBean = JSONObject.parseObject(JSONObject.toJSON(bean).toString());
            for (int i = 0; i < csvFields.length; i++) {
                sb.append("\"");
                if (csvFields[i].equals("createTime")) {
                    sb.append(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
                } else if (jsonBean.get(csvFields[i]) != null) {
                    if (jsonBean.get(csvFields[i]) instanceof String) {
                        Matcher m = p.matcher((String) jsonBean.get(csvFields[i]));
                        sb.append(m.replaceAll(" "));
                    } else {
                        sb.append(jsonBean.get(csvFields[i]));
                    }

                }
                sb.append("\",");
            }
            sb.append("\"").append(filename).append("\"");
            csvWriter.write(sb.toString());
            csvWriter.write("\n");
            sb.setLength(0);
        }
    }

    /**
     * 将数据文件导入ORACLE
     * @param importFileName
     * @throws InterruptedException
     * @throws IOException
     */
    private void sqlldrLoad(String importFileName, String fileName, String createBy, String taskBatchId, boolean repush) throws InterruptedException, IOException {
        //如果没有配置sqlldr.work.folder，则忽略数据导入
        if (sqlldrWorkFolder == null || "".equals(sqlldrWorkFolder)) {
            return;
        }
        String sqlldrPrex = sqlldrWorkFolder + "/" + fileName;

        String dbUrlStr = dbUrl.substring(dbUrl.substring(0, dbUrl.indexOf("@")).length() + 1, dbUrl.length());
        dbUrlStr = replaceLast(dbUrlStr, ":", "/");
        String ctlName = "NEWS_V3_TMP";
        if (repush) {
            ctlName = "NEWS_V3_TMP_CTL";
        }
        // DIRECT=true  表示忽略索引生成
        String shStr = "sqlldr " + dbUserName + "/" + dbPassword + "@" + dbUrlStr + " DIRECT=true parallel=true control =" + sqlldrCtrlFolder
                + "/" + ctlName + ".ctl" + " log=" + sqlldrPrex + ".log" + " bad=" + sqlldrPrex + ".bad" + " data=" + importFileName;

        System.out.println("shStr=" + shStr);

        Runtime rt = Runtime.getRuntime();
        Process p = null;
        try {
            p = rt.exec(shStr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (p != null) {
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                p.destroy();

                //删除文件
//				File file = new File(importFileName);
//				if(file.exists()) {
//					file.delete();
//				}
                if (repush) {//临时文件插入结果表
                    batchSql(fileName, createBy, taskBatchId);
                }

            }
        }
    }

    private String replaceLast(String text, String strToReplace, String replaceWithThis) {
        return text.replaceFirst("(?s)" + strToReplace + "(?!.*?" + strToReplace + ")", replaceWithThis);
    }

    //ctl临时表拷贝到结果表
    private void batchSql(String filename, String createBy, String taskBatchId) {
		/*String insertSql = "insert into news_v3_tmp select * from news_v3_tmp_ctl t where t.filename='"+filename+"' " +
				"and not exists ( select 1 from news_v3_tmp t1 where t1.tmp_id = t.tmp_id  )";
		jdbcTemplate.execute(insertSql);
		String delSql = "delete from news_v3_tmp_ctl t where t.filename='"+filename+"' ";*/

        System.out.println("开始调用存储过程：call NEWS_CTL_TO_TMP_PRO2('" + filename + "','" + createBy + "','" + taskBatchId + "')");
        newV3TmpMapper.newsCtlToTmpPro(filename, createBy, taskBatchId);

    }


    private void exportMedicalUnreasonableAction(SolrQuery solrQuery, NewV3Tmp tmpBean) throws Exception {
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        final String fileName = tmpBean.getTaskBatchId();
        // 数据写入xml
        String importFilePath = SolrUtil.importFolder + "/pushBatchBySolr/" + fileName + ".json";
        //数据写入txt导入oracle
        String importOracleFilePath = SolrUtil.importFolder + "/pushBatchByOracle/" + fileName + ".txt";
        BufferedWriter csvWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importOracleFilePath)), Charset.forName("utf8")));

        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");
        solrQuery.addFilterQuery("FIR_REVIEW_USERID:?*");

        //数据条数
        long count = SolrQueryGenerator.count(collection, solrQuery);
        solrQuery.setRows((int) count);

        SolrUtil.exportDoc(solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (doc, index) -> {
            if (index % 10000 == 0) {
                System.out.println("运行到第" + index + "条数");
            }
            try {
                MedicalUnreasonableActionVo actionVo = SolrUtil.solrDocumentToPojo(doc, MedicalUnreasonableActionVo.class, SolrUtil.initFieldMap(MedicalUnreasonableActionVo.class));
                JSONObject commonDoc1 = new JSONObject();
                String template = "${batchId}_${itemCode}_${actionName}_${visitid}";
                String ruleId = "";
                if (StringUtil.isNotBlank(actionVo.getRuleId())) {
                    ruleId = actionVo.getRuleId().replace("[", "").replace("]", "");
                }
                if ("CASE".equals(actionVo.getBusiType())) {
                    template = "${batchId}_${caseId}_${visitid}";
                    Properties properties = new Properties();
                    properties.put("batchId", actionVo.getBatchId());
                    properties.put("caseId", actionVo.getCaseId());
                    properties.put("visitid", actionVo.getVisitid());
                    template = PlaceholderResolverUtil.replacePlaceholders(template, properties);

                } else if ("DRUGUSE".equals(actionVo.getBusiType())) {
                    template = "${batchId}_${ruleId}_${itemCode}_${visitid}";
                    Properties properties = new Properties();
                    properties.put("batchId", actionVo.getBatchId());
                    properties.put("ruleId", ruleId);
                    properties.put("itemCode", actionVo.getItemcode());
                    properties.put("visitid", actionVo.getVisitid());
                    template = PlaceholderResolverUtil.replacePlaceholders(template, properties);

                } else {
                    template = "${batchId}_${itemCode}_${actionName}_${visitid}";
                    Properties properties = new Properties();
                    properties.put("batchId", actionVo.getBatchId());
                    properties.put("itemCode", actionVo.getItemcode());
                    properties.put("actionName", actionVo.getActionName());
                    properties.put("visitid", actionVo.getVisitid());
                    template = PlaceholderResolverUtil.replacePlaceholders(template, properties);

                }
                String id = MD5Util.MD5Encode(template, "UTF-8");
                commonDoc1.put("id", id);
                csvWriter.write(id + "," + actionVo.getBatchId() + "," + actionVo.getItemcode() + "," + actionVo.getActionName() + "," + actionVo.getVisitid() + "," + actionVo.getCaseId() + "," + ruleId);

                csvWriter.write("\n");

                // 初审推送信息
                commonDoc1.put("FIR_REVIEW_STATUS", SolrUtil.initActionValue(actionVo.getFirReviewStatus() == null ? "" : actionVo.getFirReviewStatus(), "set"));
                commonDoc1.put("PUSH_STATUS", SolrUtil.initActionValue(actionVo.getPushStatus() == null ? "" : actionVo.getPushStatus(), "set"));
                commonDoc1.put("FIR_REVIEW_REMARK", SolrUtil.initActionValue(actionVo.getFirReviewRemark() == null ? "" : actionVo.getFirReviewRemark(), "set"));
                commonDoc1.put("FIR_REVIEW_USERID", SolrUtil.initActionValue(actionVo.getFirReviewUserid() == null ? "" : actionVo.getFirReviewUserid(), "set"));
                commonDoc1.put("FIR_REVIEW_USERNAME", SolrUtil.initActionValue(actionVo.getFirReviewUsername() == null ? "" : actionVo.getFirReviewUsername(), "set"));
                commonDoc1.put("FIR_REVIEW_TIME", SolrUtil.initActionValue(actionVo.getFirReviewTime() == null ? "" : actionVo.getFirReviewTime(), "set"));
                commonDoc1.put("PUSH_USERID", SolrUtil.initActionValue(actionVo.getPushUserid() == null ? "" : actionVo.getPushUserid(), "set"));
                commonDoc1.put("PUSH_USERNAME", SolrUtil.initActionValue(actionVo.getPushUsername() == null ? "" : actionVo.getPushUsername(), "set"));

                fileWriter.write(commonDoc1.toJSONString());
                fileWriter.write(",\n");


            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        fileWriter.write("]");
        fileWriter.close();

        csvWriter.close();
    }

    private void exportActionByProject(SolrQuery solrQuery, NewV3Tmp tmpBean) throws Exception {
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
        final String fileName = tmpBean.getXmmcId();
        // 数据写入xml
        String importFilePath = SolrUtil.importFolder + "/pushBatchBySolr/" + fileName + ".json";
        //数据写入txt导入oracle
        String importOracleFilePath = SolrUtil.importFolder + "/pushBatchByOracle/" + fileName + ".txt";
        BufferedWriter csvWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importOracleFilePath)), Charset.forName("utf8")));

        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");
        solrQuery.addFilterQuery("FIR_REVIEW_USERID:?*");

        //数据条数
        long count = SolrQueryGenerator.count(collection, solrQuery);
        solrQuery.setRows((int) count);

        SolrUtil.exportDoc(solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (doc, index) -> {
            if (index % 10000 == 0) {
                System.out.println("运行到第" + index + "条数");
            }
            try {
                MedicalUnreasonableActionVo actionVo = SolrUtil.solrDocumentToPojo(doc, MedicalUnreasonableActionVo.class, SolrUtil.initFieldMap(MedicalUnreasonableActionVo.class));
                JSONObject commonDoc1 = new JSONObject();
                String template = "${batchId}_${itemCode}_${actionName}_${visitid}";
                String ruleId = "";

                if (StringUtil.isNotBlank(actionVo.getRuleId())) {
                    ruleId = actionVo.getRuleId().replace("[", "").replace("]", "");
                }
                Properties properties = new Properties();
                properties.put("batchId", actionVo.getBatchId());
                properties.put("itemCode", actionVo.getItemcode());
                properties.put("actionName", actionVo.getActionName());
                properties.put("visitid", actionVo.getVisitid());
                template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
                String mutex_item_code = "";
                if (doc.get("MUTEX_ITEM_CODE") != null
                        && StringUtil.isNotBlank(doc.get("MUTEX_ITEM_CODE").toString())) {
                    mutex_item_code = doc.get("MUTEX_ITEM_CODE").toString();
                    //mutex_item_code = mutex_item_code.replace("[","").replace("]","");
                    template = template.concat("_").concat(mutex_item_code);
                }
                String id = MD5Util.MD5Encode(template, "UTF-8");
                commonDoc1.put("id", id);
                csvWriter.write(id + "," + actionVo.getBatchId() + "," + actionVo.getItemcode() + "," + actionVo.getActionName() + "," + actionVo.getVisitid() + "," + actionVo.getCaseId() + "," + ruleId + "," + mutex_item_code);

                csvWriter.write("\n");

                // 初审推送信息
                commonDoc1.put("FIR_REVIEW_STATUS", SolrUtil.initActionValue(actionVo.getFirReviewStatus() == null ? "" : actionVo.getFirReviewStatus(), "set"));
                commonDoc1.put("PUSH_STATUS", SolrUtil.initActionValue(actionVo.getPushStatus() == null ? "" : actionVo.getPushStatus(), "set"));
                commonDoc1.put("FIR_REVIEW_REMARK", SolrUtil.initActionValue(actionVo.getFirReviewRemark() == null ? "" : actionVo.getFirReviewRemark(), "set"));
                commonDoc1.put("FIR_REVIEW_USERID", SolrUtil.initActionValue(actionVo.getFirReviewUserid() == null ? "" : actionVo.getFirReviewUserid(), "set"));
                commonDoc1.put("FIR_REVIEW_USERNAME", SolrUtil.initActionValue(actionVo.getFirReviewUsername() == null ? "" : actionVo.getFirReviewUsername(), "set"));
                commonDoc1.put("FIR_REVIEW_TIME", SolrUtil.initActionValue(actionVo.getFirReviewTime() == null ? "" : actionVo.getFirReviewTime(), "set"));
                commonDoc1.put("PUSH_USERID", SolrUtil.initActionValue(actionVo.getPushUserid() == null ? "" : actionVo.getPushUserid(), "set"));
                commonDoc1.put("PUSH_USERNAME", SolrUtil.initActionValue(actionVo.getPushUsername() == null ? "" : actionVo.getPushUsername(), "set"));

                fileWriter.write(commonDoc1.toJSONString());
                fileWriter.write(",\n");


            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        fileWriter.write("]");
        fileWriter.close();

        csvWriter.close();
    }



    @Override
    @Transactional
    public String importMedicalUnreasonableAction(MultipartFile file, TaskProjectBatch taskProjectBatch) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //生成批次信息
        taskProjectBatch.setStep(1);
        taskProjectBatch.setRuleTypes(MedicalConstant.RULE_TYPE_MANUAL);
        taskProjectBatch.setCreateTime(new Date());
        taskProjectBatch.setCreateUser(user.getId());
        taskProjectBatch.setCreateUserName(user.getRealname());
        batchService.save(taskProjectBatch);
        //插入批次步骤信息
        String batchId = taskProjectBatch.getBatchId();
        Set<String> ruleTypeSet = new HashSet();
        ruleTypeSet.add(taskProjectBatch.getRuleTypes());
        engineCaseService.initStep(batchId,ruleTypeSet);

        //获取project
        TaskProject project = taskProjectService.getById(taskProjectBatch.getProjectId());
        if(project==null){
            throw new Exception("找不到项目信息，项目ID参数异常");
        }


        String[] mappingFields = new String[]{"itemname","itemcode","minMoney","maxMoney","visitid","actionTypeName","actionName","actionDesc","ruleBasis","actionMoney","maxActionMoney","firReviewUsername","firReviewStatus"};
        List<MedicalUnreasonableActionVo> list = ExcelXUtils.readSheet(MedicalUnreasonableActionVo.class, mappingFields, 0, 1, file.getInputStream());

        JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictNameMapByKey("FIRST_REVIEW_STATUS");
        JSONObject actionTypeMap = ApiTokenCommon.queryMedicalDictNameMapByKey("ACTION_TYPE");
        JSONObject actionListMap = ApiTokenCommon.queryMedicalDictNameMapByKey("ACTION_LIST");

        BufferedWriter fileWriter;
        try {
            // 数据写入xml
            String importFilePath = SolrUtil.importFolder +  "/importMedicalUnreasonableAction/" + System.currentTimeMillis() + "_" + list.size() + ".json";

            fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
            fileWriter.write("[");

            List<MedicalUnreasonableActionVo> actionVoList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                MedicalUnreasonableActionVo record = list.get(i);
                if(StringUtils.isBlank(record.getItemname())){
                    throw new Exception("导入的数据中“项目名称”不能为空，如：第" + (i + 2) + "行数据“项目名称”为空");
                }
                if(StringUtils.isBlank(record.getItemcode())){
                    throw new Exception("导入的数据中“项目编码”不能为空，如：第" + (i + 2) + "行数据“项目编码”为空");
                }
                if(StringUtils.isBlank(record.getVisitid())){
                    throw new Exception("导入的数据中“就诊id”不能为空，如：第" + (i + 2) + "行数据“就诊id”为空");
                }
                if(StringUtils.isBlank(record.getVisitid())){
                    throw new Exception("导入的数据中“就诊id”不能为空，如：第" + (i + 2) + "行数据“就诊id”为空");
                }
                if(StringUtils.isBlank(record.getActionTypeName())){
                    throw new Exception("导入的数据中“不合规行为类型”不能为空，如：第" + (i + 2) + "行数据“不合规行为类型”为空");
                }
                String actionTypeId = actionTypeMap.getOrDefault(record.getActionTypeName(), "").toString();
                record.setActionTypeId(actionTypeId);

                if(StringUtils.isBlank(record.getActionTypeId())){
                    throw new Exception("导入的数据中“不合规行为类型”在系统中不存在，如：第" + (i + 2) + "行数据“不合规行为类型”数据");
                }

                if(StringUtils.isBlank(record.getActionName())){
                    throw new Exception("导入的数据中“不合规行为”不能为空，如：第" + (i + 2) + "行数据“不合规行为”为空");
                }
                String actionId = actionListMap.getOrDefault(record.getActionName(), "").toString();
                record.setActionId(actionId);

                if(StringUtils.isBlank(record.getActionId())){
                    throw new Exception("导入的数据中“不合规行为”在系统中不存在，如：第" + (i + 2) + "行数据“不合规行为”数据");
                }

                record.setProjectId(project.getProjectId());
                record.setProjectName(project.getProjectName());
                record.setBatchId(taskProjectBatch.getBatchId());
                record.setTaskBatchName(taskProjectBatch.getBatchName());
                record.setBusiType(MedicalConstant.ENGINE_BUSI_TYPE_MANUAL);

                String reviewStatus = reviewStatusMap.getOrDefault(record.getFirReviewStatus(), "init").toString();
                record.setFirReviewStatus(reviewStatus);
                record.setFirReviewTime(TimeUtil.getNowTime());
                if(StringUtils.isBlank(record.getFirReviewUsername())){
                    record.setFirReviewUserid(user.getId());
                    record.setFirReviewUsername(user.getRealname());
                }

                actionVoList.add(record);
                if(actionVoList.size()==batchSize){
                    batchQueryMasterInfo(actionVoList,fileWriter);
                    actionVoList.clear();
                }
            }

            if(actionVoList.size()>0){
                batchQueryMasterInfo(actionVoList,fileWriter);
                actionVoList.clear();
            }
            //写文件尾
            fileWriter.write("]");
            fileWriter.close();

            //导入solr
            SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);

            //修改批次步骤状态
            TaskBatchStepItem step = new TaskBatchStepItem();
            step.setUpdateTime(new Date());
            step.setEndTime(new Date());
            step.setStatus(MedicalConstant.RUN_STATE_NORMAL);
            taskSV.updateTaskBatchStepItem(taskProjectBatch.getBatchId(), MedicalConstant.RULE_TYPE_MANUAL, step);

            return "数据量：" + list.size();
        } catch (Exception e) {
            throw e;
        }
    }

    private void batchQueryMasterInfo(List<MedicalUnreasonableActionVo> actionVoList,BufferedWriter fileWriter) throws Exception{
        //获取主表信息
        Set<String> visitidList = new HashSet();
        actionVoList.forEach(actionVo ->{
            visitidList.add(actionVo.getVisitid());
        });
        String visitIdFq = "VISITID:(\"" + StringUtil.join(visitidList, "\",\"") + "\")";
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery(visitIdFq);
        solrQuery.setFields("VISITID","CLIENTID","INSURANCETYPE","CLIENTNAME","SEX_CODE",
                "SEX","BIRTHDAY","YEARAGE","MONTHAGE","DAYAGE","VISITTYPE_ID","VISITTYPE",
                "VISITDATE","ORGID","ORGNAME","HOSPLEVEL","HOSPGRADE","ORGTYPE_CODE",
                "ORGTYPE","DEPTID","DEPTNAME","DEPTID_SRC","DEPTNAME_SRC","DOCTORID","DOCTORNAME",
                "TOTALFEE","LEAVEDATE","DISEASECODE","DISEASENAME",
                "YB_VISITID","HIS_VISITID","VISITID_DUMMY","VISITID_CONNECT","ZY_DAYS","ZY_DAYS_CALCULATE",
                "FUNDPAY","DATA_RESOUCE_ID","DATA_RESOUCE","ETL_SOURCE","ETL_SOURCE_NAME","ETL_TIME");
        solrQuery.setRows(visitidList.size());
        List<DwbMasterInfoVo> masterList = SolrQueryGenerator.list(EngineUtil.DWB_MASTER_INFO,solrQuery,DwbMasterInfoVo.class,SolrUtil.initFieldMap(DwbMasterInfoVo.class));
        Map<String, DwbMasterInfoVo> masterMap = new HashMap<>();
        for (DwbMasterInfoVo masterInfo : masterList) {
            masterMap.put(masterInfo.getVisitid(),masterInfo);
        }

        actionVoList.forEach(actionVo ->{
            DwbMasterInfoVo masterInfo = masterMap.get(actionVo.getVisitid());
            if(masterInfo!=null){
                masterInfo.setCaseId(null);
                BeanUtil.copyProperties(masterInfo,actionVo);
            }
            actionVo.setCaseName(actionVo.getItemname());
            actionVo.setCaseId(actionVo.getItemcode());
            String template = "${batchId}_${itemCode}_${actionName}_${visitid}";
            Properties properties = new Properties();
            properties.put("batchId", actionVo.getBatchId());
            properties.put("itemCode", actionVo.getItemcode());
            properties.put("actionName", actionVo.getActionName());
            properties.put("visitid", actionVo.getVisitid());
            template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
            String id = MD5Util.MD5Encode(template, "UTF-8");
            actionVo.setId(id);
            try {
                commonWriteJson(fileWriter, actionVo, "id");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private void commonWriteJson(BufferedWriter bufWriter, Object bean, String idFiled) throws IOException {
        JSONObject commonDoc = new JSONObject();
        JSONObject jsonBean = JSONObject.parseObject(JSONObject.toJSON(bean).toString());
        commonDoc.put("id", jsonBean.get(idFiled));
        for(Map.Entry<String, Object> entry : jsonBean.entrySet()) {
            if(!"ID".equals(oConvertUtils.camelToUnderlineUpper(entry.getKey()))){
                commonDoc.put(oConvertUtils.camelToUnderlineUpper(entry.getKey()), entry.getValue());
            }
        }
        bufWriter.write(commonDoc.toJSONString());
        bufWriter.write(',');
        bufWriter.write("\n");
    }

}
