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

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.*;
import com.ai.modules.api.util.ApiTokenCommon;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.runnable.EngineFunctionRunnable;
import com.ai.modules.review.service.IReviewNewFirstService;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.review.vo.ReviewSystemDrugViewVo;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class ReviewNewFirstServiceImpl implements IReviewNewFirstService {
//    @Autowired
//    IMedicalDictService medicalDictService;

    @Override
    public void pushBatchByCaseId(String collection, SolrQuery solrQuery, JSONObject commonDoc, boolean isPush) throws Exception {

        // 数据写入xml
        String importFilePath = SolrUtil.importFolder + EngineUtil.SOLR_IMPORT_STEP + System.currentTimeMillis() + "_" + IdUtils.uuid() + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");
        solrQuery.setFields("id");
        SolrUtil.export(solrQuery, collection, (map, index) -> {
            commonDoc.put("id", map.get("id"));
            try {
                fileWriter.write(commonDoc.toJSONString());
                fileWriter.write(",\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        fileWriter.write("]");
        fileWriter.close();
        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, collection);
    }

    @Override
    public void pushRecordByIds(List<String> ids, SolrInputDocument commonDoc) throws Exception {
        SolrClient solr = SolrUtil.getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        for (String id : ids) {
            SolrInputDocument document = commonDoc.deepCopy();
            document.setField("id", id);
            solr.add(document);
        }
        solr.commit();
        solr.close();
    }

    @Override
    public void pushRecord(SolrQuery solrQuery, JSONObject commonDoc) throws Exception {
        this.pushRecord(solrQuery, commonDoc, count -> {});
    }

    @Override
    public int pushRecord(SolrQuery solrQuery, JSONObject commonDoc, Consumer<Integer> updateProcess) throws Exception {

        int maxFileCount = 200000;
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        List<String> filePathList = new ArrayList<>();
        BufferedWriter[] fileWriter = new BufferedWriter[]{null};
        AtomicInteger lastIndex = new AtomicInteger();
        String filePath = SolrUtil.importFolder + File.separator + EngineUtil.SOLR_IMPORT_STEP + File.separator + System.currentTimeMillis() + "_" + IdUtils.uuid();

        solrQuery.setFields("id");
        SolrUtil.export(solrQuery, collection, (map, index) -> {
            commonDoc.put("id", map.get("id"));
            try {
                if (index % maxFileCount == 0) {
                    if (fileWriter[0] != null) {
                        fileWriter[0].write("]");
                        fileWriter[0].close();
                    }
                    // 数据写入xml
                    String importFilePath = filePath + "_" + filePathList.size() + ".json";
                    filePathList.add(importFilePath);
                    fileWriter[0] = new BufferedWriter(
                            new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
                    //写文件头
                    fileWriter[0].write("[");
                }
                fileWriter[0].write(commonDoc.toJSONString());
                fileWriter[0].write(",\n");
                lastIndex.set(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if(fileWriter[0] == null){
            return 0;
        }

        fileWriter[0].write("]");
        fileWriter[0].close();

        int totalCount = lastIndex.get() + 1;

        int fileCount = filePathList.size();
        if(fileCount == 1){
            SolrUtil.importJsonToSolr(filePathList.get(0), collection);
            updateProcess.accept(totalCount);
        } else {
            final CountDownLatch count = new CountDownLatch(fileCount);
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(fileCount > 2?3:fileCount);

            for (int i = 0, len = filePathList.size(); i < len; i++) {
                String path = filePathList.get(i);
                int fileRecordNum = i + 1 == len?totalCount % maxFileCount: maxFileCount;
                fixedThreadPool.submit(new EngineFunctionRunnable(() -> {
                    try {
                        SolrUtil.importJsonToSolr(path, collection);
                        updateProcess.accept(fileRecordNum);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        count.countDown();
                    }
                }));
            }
            count.await();
            fixedThreadPool.shutdown();
        }

        return totalCount;
    }


    @Override
    public void copyRecordPropByIds(List<String> ids, JSONObject commonDoc, Map<String, String> flMap) throws Exception {
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("id:(" +StringUtils.join(ids, " OR ") + ")");
        this.copyRecordProp(solrQuery, commonDoc, flMap);
    }

    @Override
    public void copyRecordProp(SolrQuery solrQuery, JSONObject commonDoc, Map<String, String> flMap) throws Exception {
        this.copyRecordProp(solrQuery, commonDoc, flMap, count -> {});
    }

    @Override
    public void copyRecordProp(SolrQuery solrQuery, JSONObject commonDoc, Map<String, String> flMap, Consumer<Integer> updateProcess) throws Exception {

        int maxFileCount = 200000;
        String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;

        List<String> filePathList = new ArrayList<>();
        BufferedWriter[] fileWriter = new BufferedWriter[]{null};
        AtomicInteger lastIndex = new AtomicInteger();
        String filePath = SolrUtil.importFolder + File.separator + EngineUtil.SOLR_IMPORT_STEP + File.separator + System.currentTimeMillis() + "_" + IdUtils.uuid();

        solrQuery.setFields("id");
        for(String fl: flMap.keySet()){
            solrQuery.addField(fl);
        }
        SolrUtil.export(solrQuery, collection, (map, index) -> {
            commonDoc.put("id", map.get("id"));
            for(Map.Entry<String, String> entry: flMap.entrySet()){
                commonDoc.put(entry.getValue(), SolrUtil.initActionValue(map.get(entry.getKey()), "set"));
            }
            try {
                if (index % maxFileCount == 0) {
                    if (fileWriter[0] != null) {
                        fileWriter[0].write("]");
                        fileWriter[0].close();
                    }
                    // 数据写入xml
                    String importFilePath = filePath + "_" + filePathList.size() + ".json";
                    filePathList.add(importFilePath);
                    fileWriter[0] = new BufferedWriter(
                            new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
                    //写文件头
                    fileWriter[0].write("[");
                }
                fileWriter[0].write(commonDoc.toJSONString());
                fileWriter[0].write(",\n");
                lastIndex.set(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if(fileWriter[0] == null){
            return;
        }

        fileWriter[0].write("]");
        fileWriter[0].close();

        int fileCount = filePathList.size();
        if(fileCount == 1){
            SolrUtil.importJsonToSolr(filePathList.get(0), collection);
            updateProcess.accept(lastIndex.get() + 1);
        } else {
            final CountDownLatch count = new CountDownLatch(fileCount);
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(fileCount > 2?3:fileCount);

            for (int i = 0, len = filePathList.size(); i < len; i++) {
                String path = filePathList.get(i);
                int fileRecordNum = i + 1 == len?(lastIndex.get() + 1) % maxFileCount: maxFileCount;
                fixedThreadPool.submit(new EngineFunctionRunnable(() -> {
                    try {
                        SolrUtil.importJsonToSolr(path, collection);
                        updateProcess.accept(fileRecordNum);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        count.countDown();
                    }
                }));
            }
            count.await();
            fixedThreadPool.shutdown();
        }
    }

    @Override
    public String importReviewStatus(MultipartFile file, MedicalUnreasonableActionVo searchObj) throws Exception {
        List<List<String>> list = ExcelXUtils.readSheet(0, 0, file.getInputStream());
        List<String> titles = list.remove(0);
        int idIndex = titles.indexOf("记录ID");
        int reviewStatusIndex = titles.indexOf("判定结果");
        int reviewRemarkIndex = titles.indexOf("判定理由");
        int pushStatusIndex = titles.indexOf("推送状态");
        int reviewClassifyIndex = titles.indexOf("白名单归因");
        if (idIndex == -1) {
            throw new Exception("缺少“记录ID”列");
        }
        if (reviewStatusIndex == -1) {
            throw new Exception("缺少“判定结果”列");
        }
        if (reviewRemarkIndex == -1) {
            throw new Exception("缺少“判定理由”列");
        }
        if (pushStatusIndex == -1) {
            throw new Exception("缺少“通过状态”列");
        }

        /*List<String> idList = new ArrayList<>();

        for(List<String> record: list){
            String id = record.get(idIndex);
            idList.add(id);
            if(idList.size()> 100){
                break;
            }
        }

        SolrQueryGenerator.list(EngineUtil.MEDICAL_UNREASONABLE_ACTION, new String[]{"id:(\"" + StringUtils.join(idList, "\",\"") + "\")"}, new String[]{"BUSI_TYPE","BATCH_ID", "CASE_ID"});
*/


        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        JSONObject commonJson = new JSONObject();
        commonJson.put("FIR_REVIEW_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonJson.put("FIR_REVIEW_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
        commonJson.put("FIR_REVIEW_TIME", SolrUtil.initActionValue(TimeUtil.getNowTime(), "set"));
        commonJson.put("PUSH_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonJson.put("PUSH_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
        JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictNameMapByKey("FIRST_REVIEW_STATUS");
        JSONObject pushStatusMap = ApiTokenCommon.queryMedicalDictNameMapByKey("FIRST_PUSH_STATUS");
        JSONObject reviewClassifyMap = ApiTokenCommon.queryOtherDictNameMapByType("reasontype");
        pushStatusMap.put("未通过", "");


        Map<String, AtomicInteger> reviewStatusCountMap = new HashMap<>();
        Map<String, AtomicInteger> pushStatusMapCountMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : reviewStatusMap.entrySet()) {
            reviewStatusCountMap.put(entry.getValue().toString(), new AtomicInteger());
        }
        for (Map.Entry<String, Object> entry : pushStatusMap.entrySet()) {
            pushStatusMapCountMap.put(entry.getValue().toString(), new AtomicInteger());
        }

        final int[] notExistCount = {0};

        BiFunction<List<List<String>>, JSONObject, Exception> actionFun = (dataList,json) -> {


            String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
            BufferedWriter fileWriter;
            try {
                // 数据写入xml
                String importFilePath = SolrUtil.importFolder + EngineUtil.SOLR_IMPORT_STEP + System.currentTimeMillis() + "_" + list.size() + ".json";

                fileWriter = new BufferedWriter(
                        new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
                fileWriter.write("[");


                for (List<String> record : dataList) {
                    if (reviewStatusIndex >= record.size()) {
                        continue;
                    }
                    String reviewStatusVal = record.get(reviewStatusIndex);
                    String reviewStatus = reviewStatusMap.getOrDefault(reviewStatusVal, "init").toString();

                    reviewStatusCountMap.get(reviewStatus).incrementAndGet();
                    String pushStatus = pushStatusIndex < record.size() ?
                            pushStatusMap.getOrDefault(record.get(pushStatusIndex), "").toString()
                            : "";
                    pushStatusMapCountMap.get(pushStatus).incrementAndGet();
                    String reviewRemark = reviewRemarkIndex < record.size() ?record.get(reviewRemarkIndex): "";

                    String reviewClassify = reviewClassifyIndex > -1 && reviewClassifyIndex < record.size() ? record.get(reviewClassifyIndex): "";

                    if(StringUtils.isNotBlank(reviewClassify)){
                        reviewClassify = reviewClassifyMap.getString(reviewClassify);
                        if(reviewClassify == null){
                            reviewClassify = "";
                        }
                    }
                    json.put("id", record.get(idIndex));
                    json.put("FIR_REVIEW_STATUS", SolrUtil.initActionValue(reviewStatus, "set"));
                    json.put("FIR_REVIEW_REMARK", SolrUtil.initActionValue(reviewRemark, "set"));
                    json.put("FIR_REVIEW_CLASSIFY", SolrUtil.initActionValue(reviewClassify, "set"));
                    json.put("PUSH_STATUS", SolrUtil.initActionValue(pushStatus, "set"));
                    if(!"1".equals(pushStatus)){
                        json.put("SEC_PUSH_STATUS", SolrUtil.initActionValue("0", "set"));
                        json.put("HANDLE_STATUS", SolrUtil.initActionValue("", "set"));
                    }
                    fileWriter.write(json.toJSONString());
                    fileWriter.write(",\n");
                }
                //写文件尾
                fileWriter.write("]");
                fileWriter.close();
                //导入solr
                SolrUtil.importJsonToSolr(importFilePath, collection);
                notExistCount[0] = (int) SolrQueryGenerator.count(EngineUtil.MEDICAL_UNREASONABLE_ACTION, new String[]{"-BATCH_ID:*"});
                // 刪除失效数据
                SolrUtil.delete(collection, "-BATCH_ID:*");
                return null;
            } catch (Exception e) {
                return e;
            }
        };
        if (list.size() > 200000) {

            ThreadUtils.ASYNC_POOL.addFirstImport(searchObj, list.size(), (processFunc) -> {
                Exception e = actionFun.apply(list, commonJson);
                if (e == null) {
                    List<String> msg = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : reviewStatusMap.entrySet()) {
                        msg.add(entry.getKey() + "：" + reviewStatusCountMap.get(entry.getValue().toString()));
                    }
                    for (Map.Entry<String, Object> entry : pushStatusMap.entrySet()) {
                        msg.add(entry.getKey() + "：" + pushStatusMapCountMap.get(entry.getValue().toString()));
                    }
                    processFunc.accept(list.size());
                    return Result.ok("数据量：" + list.size() + "，" + StringUtils.join(msg, "，") + "， 删除失效数据：" + notExistCount[0]);
                } else {
                    return Result.error(e.getMessage());
                }
            });
//            ThreadUtils.THREAD_SOLR_REQUEST_POOL.add(new EngineFunctionRunnable(ds, () -> actionFun.apply(commonJson)));
            return null;
        } else {
            Exception e = actionFun.apply(list, commonJson);
            if (e == null) {
                List<String> msg = new ArrayList<>();
                for (Map.Entry<String, Object> entry : reviewStatusMap.entrySet()) {
                    msg.add(entry.getKey() + "：" + reviewStatusCountMap.get(entry.getValue().toString()));
                }
                for (Map.Entry<String, Object> entry : pushStatusMap.entrySet()) {
                    msg.add(entry.getKey() + "：" + pushStatusMapCountMap.get(entry.getValue().toString()));
                }
                return "数据量：" + list.size() + "，" + StringUtils.join(msg, "，") + "， 删除失效数据：" + notExistCount[0];
            } else {
                throw e;
            }

        }


    }


    @Override
    public String importGroupReviewStatus(MultipartFile file, MedicalUnreasonableActionVo searchObj, SolrQuery solrQuery) throws Exception {
        List<List<String>> dataList = ExcelXUtils.readSheet(0, 0, file.getInputStream());
        List<String> titles = dataList.remove(0);
        int idIndex = titles.indexOf("记录ID");
        int reviewStatusIndex = titles.indexOf("判定结果");
        int countIndex = titles.indexOf("数量");
        int reviewClassifyIndex = titles.indexOf("白名单归因");
        int reviewRemarkIndex = titles.indexOf("判定理由");
        if (idIndex == -1) {
            throw new Exception("缺少“记录ID”列");
        }
        if (reviewStatusIndex == -1) {
            throw new Exception("缺少“判定结果”列");
        }
        if (reviewRemarkIndex == -1) {
            throw new Exception("缺少“判定理由”列");
        }

        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        JSONObject commonJson = new JSONObject();
        commonJson.put("FIR_REVIEW_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonJson.put("FIR_REVIEW_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
        commonJson.put("FIR_REVIEW_TIME", SolrUtil.initActionValue(TimeUtil.getNowTime(), "set"));

        JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictNameMapByKey("FIRST_REVIEW_STATUS");
        JSONObject reviewClassifyMap = ApiTokenCommon.queryOtherDictNameMapByType("reasontype");

        String reviewStatusCache;
        int totalCount = countIndex == -1 ? -1 : 0;
        Map<String, List<String>> map = new HashMap<>();
        for (List<String> record : dataList) {
            if (reviewStatusIndex >= record.size()
                    || StringUtils.isBlank(reviewStatusCache = record.get(reviewStatusIndex))
            ) {
                continue;
            }
            Object statusObj = reviewStatusMap.get(reviewStatusCache);
            if (statusObj == null) {
                throw new Exception("判定结果不存在：" + reviewStatusCache);
            }

            String fqStr = record.get(idIndex);
            String reviewRemark = reviewRemarkIndex < record.size() ? record.get(reviewRemarkIndex) : "";
            String reviewClassify = reviewClassifyIndex > -1 && reviewClassifyIndex < record.size() ? record.get(reviewClassifyIndex) : "";

            map.computeIfAbsent(reviewStatusCache + "::" + reviewRemark + "::" + reviewClassify, k -> new ArrayList<>()).add(fqStr);

            if (countIndex > -1) {
                totalCount += Integer.parseInt(record.get(countIndex));
            }
        }

        Function<Consumer<Integer>, Object> actionFun = (processFunc) -> {
            Map<String, Integer> statusCountMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String key = entry.getKey();
                int index = key.indexOf("::");
                int lastIndex = key.lastIndexOf("::");
                String reviewStatusName = key.substring(0, index);
                String reviewStatus = reviewStatusMap.get(reviewStatusName).toString();
                String reviewRemark = key.substring(index + 2, lastIndex);
                String reviewClassify = key.substring(lastIndex + 2);
                if(StringUtils.isNotBlank(reviewClassify)){
                    reviewClassify = reviewClassifyMap.getString(reviewClassify);
                    if(reviewClassify == null){
                        reviewClassify = "";
                    }
                }

                commonJson.put("FIR_REVIEW_STATUS", SolrUtil.initActionValue(reviewStatus, "set"));
                commonJson.put("FIR_REVIEW_REMARK", SolrUtil.initActionValue(reviewRemark, "set"));
                commonJson.put("FIR_REVIEW_CLASSIFY", SolrUtil.initActionValue(reviewClassify, "set"));

                List<String> itemList = entry.getValue();
                for (int i = 0, j, len = itemList.size(); i < len; i = j) {
                    j = i + 500;
                    if (j > len) {
                        j = len;
                    }
                    // 构造主表条件
                    SolrQuery query = solrQuery.getCopy();
//                    solrQuery.addFilterQuery("-SEC_PUSH_STATUS:1");
                    query.addFilterQuery("-(FIR_REVIEW_STATUS:" + reviewStatus + " AND FIR_REVIEW_CLASSIFY:\"" + reviewClassify + "\")");
                    query.addFilterQuery("(" + StringUtils.join(itemList.subList(i, j), ") OR (") + ")");
                    try {
                        int count = this.pushRecord(query, commonJson, processFunc);
                        statusCountMap.put(reviewStatusName, statusCountMap.getOrDefault(reviewStatusName, 0) + count);
                    } catch (Exception e) {
                        return e;
                    }
                }

            }
            return statusCountMap;
        };

        if (totalCount == -1 || totalCount > 200000) {
//        if(totalCount == -1 || totalCount > 1){
            int finalTotalCount = totalCount;
            ThreadUtils.ASYNC_POOL.addFirstGroupImport(searchObj, totalCount, (processFunc) -> {
                Object e = actionFun.apply(processFunc);
                if (e instanceof Exception) {
                    return Result.error(((Exception) e).getMessage());
                } else {
                    List<String> msg = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : ((Map<String, Integer>) e).entrySet()) {
                        msg.add(entry.getKey() + "：" + entry.getValue());
                    }
                    return Result.ok("数据量：" + (finalTotalCount == -1 ? "未知" : finalTotalCount) + "，" + StringUtils.join(msg, "，"));
                }
            });
            return null;
        } else {

            Object e = actionFun.apply(count -> {
            });
            if (e instanceof Exception) {
                throw (Exception) e;
            } else {
                List<String> msg = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : ((Map<String, Integer>) e).entrySet()) {
                    msg.add(entry.getKey() + "：" + entry.getValue());
                }
                return "数据量：" + totalCount + "，" + StringUtils.join(msg, "，");

            }

        }


    }

    @Override
    public void exportDrugList(SolrQuery solrQuery, String collection, Map<String, String> fieldMap, String title, String ruleType, OutputStream os) throws Exception {
        solrQuery.setRows(1000000);
        List<ReviewSystemDrugViewVo> list = SolrQueryGenerator.list(collection, solrQuery, ReviewSystemDrugViewVo.class
                , fieldMap);
        JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictMapByKey("FIRST_REVIEW_STATUS");
        JSONObject pushStatusMap = ApiTokenCommon.queryMedicalDictMapByKey("FIRST_PUSH_STATUS");

        for (ReviewSystemDrugViewVo bean : list) {
            // 冲突项目数组
            List<String> mutexItemNames = bean.getMutexItemName();
            if (mutexItemNames != null) {
                List<String> mutexItemCodes = new ArrayList<>();
                for (int i = 0, len = mutexItemNames.size(); i < len; i++) {
                    String[] array = mutexItemNames.get(i).split("::");
                    mutexItemCodes.add(array[0]);
                    mutexItemNames.set(i, array[1]);
                }
                bean.setMutexItemCode(mutexItemCodes);
            }
            if (StringUtils.isNotBlank(bean.getFirReviewStatus())) {
                bean.setFirReviewStatus(reviewStatusMap.getString(bean.getFirReviewStatus()));
            }

            if (StringUtils.isNotBlank(bean.getPushStatus())) {
                bean.setPushStatus(pushStatusMap.getOrDefault(bean.getPushStatus(), "未通过").toString());
            } else {
//                bean.setPushStatus("待推送");
                bean.setPushStatus("未通过");
            }
        }

        String[] titles = new String[]{"记录ID", "就诊ID", "项目名称", "项目编码", "项目数量", "项目收费项目等级",
                "冲突项目编码", "冲突项目名称",
                "本次就诊总金额", "涉案金额", "违反限定范围", "提示信息", "诊断疾病名称",
                "医疗机构名称", "医疗机构级别", "病人姓名", "性别", "年龄(岁)", "年龄(月)", "年龄(天)", "就诊类型", "就诊日期", "住院天数"
                , "判定结果", "通过状态"};
        String[] fields = new String[]{"id", "visitid", "caseName", "caseId", "itemQty", "",
                "mutexItemCode", "mutexItemName",
                "totalfee", "actionMoney", "ruleScopeName", "actionDesc", "diseasename",
                "orgname", "hosplevel", "clientname", "sex", "yearage", "monthage", "dayage", "visittype", "visitdate", "zyDaysCalculate"
                , "firReviewStatus", "pushStatus"};

        // 创建文件输出流
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 生成一个表格
        ExportXUtils.exportExl(list, ReviewSystemDrugViewVo.class, titles, fields, workbook, title);

        workbook.write(os);
        workbook.dispose();
    }


    @Override
    public void exportClinicalList(SolrQuery solrQuery, String collection, Map<String, String> fieldMap, String title, String ruleType, OutputStream os) throws Exception {
        solrQuery.setRows(1000000);
        List<ReviewSystemDrugViewVo> list = SolrQueryGenerator.list(collection, solrQuery, ReviewSystemDrugViewVo.class
                , fieldMap);

        String[] titles = new String[]{"就诊ID", "项目名称", "项目编码", "项目数量", "项目收费项目等级",
                "冲突项目编码", "冲突项目名称",
                "本次就诊总金额", "涉案金额", "违反限定范围", "提示信息", "诊断疾病名称",
                "医疗机构名称", "医疗机构级别", "病人姓名", "性别", "年龄(岁)", "年龄(月)", "年龄(天)", "就诊类型", "就诊日期", "住院天数"};
        String[] fields = new String[]{"visitid", "caseName", "caseId", "itemQty", "",
                "mutexItemCode", "mutexItemName",
                "totalfee", "actionMoney", "ruleScopeName", "actionDesc", "diseasename",
                "orgname", "hosplevel", "clientname", "sex", "yearage", "monthage", "dayage", "visittype", "visitdate", "zyDaysCalculate"};

        // 创建文件输出流
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 生成一个表格
        ExportXUtils.exportExl(list, ReviewSystemDrugViewVo.class, titles, fields, workbook, title);

        workbook.write(os);
        workbook.dispose();
    }


}
