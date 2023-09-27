package com.ai.modules.review.service.impl;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.ExcelXUtils;
import com.ai.common.utils.ThreadUtils;
import com.ai.common.utils.TimeUtil;
import com.ai.modules.api.util.ApiTokenCommon;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.service.IReviewNewFirstService;
import com.ai.modules.review.service.IReviewNewSecService;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @Auther: zhangpeng
 * @Date: 2021/5/20 17
 * @Description:
 */
@Service
@Slf4j
public class ReviewNewSecServiceImpl implements IReviewNewSecService {

    @Autowired
    IReviewNewFirstService reviewNewFirstService;

    @Override
    public String importReviewStatus(MultipartFile file, MedicalUnreasonableActionVo searchObj) throws Exception {
        List<List<String>> list = ExcelXUtils.readSheet(0, 0, file.getInputStream());
        List<String> titles = list.remove(0);
        int idIndex = titles.indexOf("记录ID");
        int reviewStatusIndex = titles.indexOf("复审判定");
        int reviewClassifyIndex = titles.indexOf("白名单归因");
        int reviewRemarkIndex = titles.indexOf("复审判定理由");
        if (idIndex == -1) {
            throw new Exception("缺少“记录ID”列");
        }
        if (reviewStatusIndex == -1) {
            throw new Exception("缺少“复审判定”列");
        }
        if (reviewRemarkIndex == -1) {
            throw new Exception("缺少“复审判定理由”列");
        }


        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        JSONObject commonJson = new JSONObject();
        commonJson.put("SEC_REVIEW_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonJson.put("SEC_REVIEW_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
        commonJson.put("SEC_REVIEW_TIME", SolrUtil.initActionValue(TimeUtil.getNowTime(), "set"));


        JSONObject reviewStatusMap = ApiTokenCommon.queryMedicalDictNameMapByKey("FIRST_REVIEW_STATUS");
        JSONObject reviewClassifyMap = ApiTokenCommon.queryOtherDictNameMapByType("reasontype");


        Map<String, AtomicInteger> reviewStatusCountMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : reviewStatusMap.entrySet()) {
            reviewStatusCountMap.put(entry.getValue().toString(), new AtomicInteger());
        }

        final int[] notExistCount = {0};

        BiFunction<List<List<String>>, JSONObject, Exception> actionFun = (dataList, json) -> {


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

                    String reviewRemark = reviewRemarkIndex < record.size() ? record.get(reviewRemarkIndex) : "";

                    String reviewClassify = reviewClassifyIndex > -1 && reviewClassifyIndex < record.size() ? record.get(reviewClassifyIndex): "";
                    if(StringUtils.isNotBlank(reviewClassify)){
                        reviewClassify = reviewClassifyMap.getString(reviewClassify);
                        if(reviewClassify == null){
                            reviewClassify = "";
                        }
                    }
                    json.put("id", record.get(idIndex));
                    json.put("SEC_REVIEW_STATUS", SolrUtil.initActionValue(reviewStatus, "set"));
                    json.put("SEC_REVIEW_REMARK", SolrUtil.initActionValue(reviewRemark, "set"));
                    json.put("SEC_REVIEW_CLASSIFY", SolrUtil.initActionValue(reviewClassify, "set"));
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

            ThreadUtils.ASYNC_POOL.addSecImport(searchObj, list.size(), (processFunc) -> {
                Exception e = actionFun.apply(list, commonJson);
                if (e == null) {
                    List<String> msg = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : reviewStatusMap.entrySet()) {
                        msg.add(entry.getKey() + "：" + reviewStatusCountMap.get(entry.getValue().toString()));
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
        commonJson.put("SEC_REVIEW_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonJson.put("SEC_REVIEW_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
        commonJson.put("SEC_REVIEW_TIME", SolrUtil.initActionValue(TimeUtil.getNowTime(), "set"));

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

            map.computeIfAbsent(reviewStatusCache + "::" + reviewRemark+ "::" + reviewClassify, k -> new ArrayList<>()).add(fqStr);

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


                commonJson.put("SEC_REVIEW_STATUS", reviewStatus);
                commonJson.put("SEC_REVIEW_REMARK", reviewRemark);
                commonJson.put("SEC_REVIEW_CLASSIFY", SolrUtil.initActionValue(reviewClassify, "set"));

                List<String> itemList = entry.getValue();
                for (int i = 0, j, len = itemList.size(); i < len; i = j) {
                    j = i + 500;
                    if (j > len) {
                        j = len;
                    }
                    // 构造主表条件
                    SolrQuery query = solrQuery.getCopy();
//                    solrQuery.addFilterQuery("-SEC_PUSH_STATUS:1");
                    query.addFilterQuery("-(SEC_REVIEW_STATUS:" + reviewStatus + " AND SEC_REVIEW_CLASSIFY:\"" + reviewClassify + "\")");
                    query.addFilterQuery("(" + StringUtils.join(itemList.subList(i, j), ") OR (") + ")");
                    try {
                        int count = this.reviewNewFirstService.pushRecord(query, commonJson, processFunc);
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
            ThreadUtils.ASYNC_POOL.addSecGroupImport(searchObj, totalCount, (processFunc) -> {
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


}
