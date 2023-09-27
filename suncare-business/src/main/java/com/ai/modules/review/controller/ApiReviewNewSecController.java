package com.ai.modules.review.controller;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.StringCamelUtils;
import com.ai.common.utils.StringUtil;
import com.ai.common.utils.ThreadUtils;
import com.ai.common.utils.TimeUtil;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.dto.ReviewInfoDTO;
import com.ai.modules.review.entity.NewV3Tmp;
import com.ai.modules.review.service.IDynamicFieldService;
import com.ai.modules.review.service.IReviewNewFirstService;
import com.ai.modules.review.service.IReviewNewSecService;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Auther: zhangpeng
 * @Date: 2021/5/19 17
 * @Description:
 */

@Slf4j
@Api(tags = "项目复审")
@RestController
@RequestMapping("/apiReviewSec")
public class ApiReviewNewSecController {

    @Autowired
    IReviewNewFirstService reviewNewFirstService;

    @Autowired
    IReviewNewSecService service;

    @Autowired
    IDynamicFieldService dynamicFieldService;

    @AutoLog(value = "复审-推送风控报告")
    @ApiOperation(value = "复审-推送风控报告", notes = "复审-推送风控报告")
    @PutMapping(value = "/pushRecord")
    public Result<?> pushRecord(MedicalUnreasonableActionVo searchObj,
                              @RequestParam(name = "batchId") String batchId,
                              String reviewInfo,
                              String secReviewStatus,
                              String dynamicSearch,
                              String ids, String groupBys,
                              HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 推送信息
        JSONObject commonJson = new JSONObject();
        commonJson.put("SEC_PUSH_STATUS", SolrUtil.initActionValue("1", "set"));
        commonJson.put("SEC_PUSH_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonJson.put("SEC_PUSH_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
        commonJson.put("SEC_PUSH_TIME", SolrUtil.initActionValue(TimeUtil.getNowTime(), "set"));

        NewV3Tmp tmpBean = JSONObject.parseObject(reviewInfo, NewV3Tmp.class);
        //报告周期信息
        commonJson.put("HANDLE_STATUS", SolrUtil.initActionValue("0", "set"));
        commonJson.put("ISSUE_ID", SolrUtil.initActionValue(tmpBean.getIssueId(), "set"));
        commonJson.put("ISSUE_NAME", SolrUtil.initActionValue(tmpBean.getIssueName(), "set"));
        commonJson.put("XMKH_ID", SolrUtil.initActionValue(tmpBean.getXmkhId(), "set"));
        commonJson.put("XMKH_NAME", SolrUtil.initActionValue(tmpBean.getXmkhName(), "set"));
        commonJson.put("TASK_BATCH_NAME", SolrUtil.initActionValue(tmpBean.getTaskBatchName(), "set"));

        tmpBean.setDataSource(user.getDataSource());

        if (StringUtils.isBlank(ids) || StringUtils.isNotBlank(groupBys)) {
            SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
            // 构造动态查询条件
            List<String> searchFqs = dynamicFieldService.getSearchFqs(dynamicSearch);
            if (searchFqs.size() > 0) {
                solrQuery.addFilterQuery(searchFqs.toArray(new String[0]));
            }
            // 清除排序字段，分组排序val或count会报错
            solrQuery.clearSorts();
//            solrQuery.addFilterQuery("FIR_REVIEW_STATUS:(grey OR blank)");
            if(StringUtils.isNotBlank(secReviewStatus)){
                solrQuery.addFilterQuery("SEC_REVIEW_STATUS:(" +  secReviewStatus.replaceAll("\\|", " OR ")  +")");
            }
            // 分组勾选条件
            if(StringUtils.isNotBlank(ids) && StringUtils.isNotBlank(groupBys)){
                List<String> groupByList = Arrays.asList(groupBys.split(","));
                if(groupByList.size() == 1){
                    solrQuery.addFilterQuery(groupByList.get(0) + ":(\"" + ids.replaceAll(",", "\",\"") + "\")");
                } else {
                    String fq = Arrays.stream(ids.split(",")).map(id -> {
                        String[] groupVals = id.split("::");
                        final AtomicInteger i = new AtomicInteger(0);
                        int valsLen = groupVals.length;
                        // 构造 (f1:"1" AND  f2:"2") OR (f1:"2" AND  f2:"3")
                        return "(" + groupByList.stream()
                                .map(r -> r + ":\"" + (i.get() >= valsLen?"":groupVals[i.getAndIncrement()]) + "\"")
                                .collect(Collectors.joining(" AND "))
                                + ")";
                    }).collect(Collectors.joining(" OR "));
                    solrQuery.addFilterQuery(fq);
                }

            }
            int count = (int) SolrQueryGenerator.count(EngineUtil.MEDICAL_UNREASONABLE_ACTION, solrQuery);
            if(count == 0){
                return Result.error("没有需要推送的数据，注意只推送灰黑名单！");
            }
            if(count < 200000){
                this.reviewNewFirstService.pushRecord(solrQuery, commonJson);
                return Result.ok("推送成功");
            } else {
                ThreadUtils.ASYNC_POOL.addPush2ed(searchObj, solrQuery.getFilterQueries(), count, (processFunc) -> {
                    try {
                        this.reviewNewFirstService.pushRecord(solrQuery, commonJson, processFunc);
                        return Result.ok("判定成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Result.error(e.getMessage());
                    }
                });
                return Result.ok("正在推送，请稍后查看状态");
            }

        } else {
            SolrQuery solrQuery = new SolrQuery("*:*");
            if(StringUtils.isNotBlank(secReviewStatus)){
                solrQuery.addFilterQuery("SEC_REVIEW_STATUS:(" +  secReviewStatus.replaceAll("\\|", " OR ")  +")");
            }
//            solrQuery.addFilterQuery("FIR_REVIEW_STATUS:(grey OR blank)");
            solrQuery.addFilterQuery("id:(" + StringUtil.join(ids.split(","), " OR ") + ")");
            this.reviewNewFirstService.pushRecord(solrQuery, commonJson);
            return Result.ok("推送成功");
        }

    }


    @AutoLog(value = "复审-判定结果")
    @ApiOperation(value = "复审-判定结果", notes = "复审-判定结果")
    @PutMapping(value = "/updateReviewStatus")
    public Result<?> updateReviewStatus(String ids, String groupBys, String reviewInfo,
                                        @RequestParam(name = "batchId") String batchId,
                                        MedicalUnreasonableActionVo searchObj,
                                        String dynamicSearch,
                                        HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ReviewInfoDTO reviewObj = JSONObject.parseObject(reviewInfo, ReviewInfoDTO.class);

        JSONObject commonJson = initInputJson(reviewObj);
        // 判断结果信息
        commonJson.put("SEC_REVIEW_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonJson.put("SEC_REVIEW_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
        commonJson.put("SEC_REVIEW_TIME", SolrUtil.initActionValue(TimeUtil.getNowTime(), "set"));

        if (StringUtils.isBlank(ids) || StringUtils.isNotBlank(groupBys)) {
            // 构造主表条件
            SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
            // 清除排序字段，分组排序val或count会报错
            solrQuery.clearSorts();
            solrQuery.addFilterQuery("-SEC_PUSH_STATUS:1");
            solrQuery.addFilterQuery("-(SEC_REVIEW_STATUS:" + reviewObj.getSecReviewStatus() + " AND SEC_REVIEW_CLASSIFY:\"" + reviewObj.getSecReviewClassify() + "\")");
            // 分组勾选条件
            if (StringUtils.isNotBlank(ids) && StringUtils.isNotBlank(groupBys)) {
                List<String> groupByList = Arrays.asList(groupBys.split(","));
                if (groupByList.size() == 1) {
                    solrQuery.addFilterQuery(groupByList.get(0) + ":(\"" + ids.replaceAll(",", "\",\"") + "\")");
                } else {
                    String fq = Arrays.stream(ids.split(",")).map(id -> {
                        String[] groupVals = id.split("::");
                        final AtomicInteger i = new AtomicInteger(0);
                        int valsLen = groupVals.length;
                        // 构造 (f1:"1" AND  f2:"2") OR (f1:"2" AND  f2:"3")
                        return "(" + groupByList.stream()
                                .map(r -> r + ":\"" + (i.get() >= valsLen ? "" : groupVals[i.getAndIncrement()]) + "\"")
                                .collect(Collectors.joining(" AND "))
                                + ")";
                    }).collect(Collectors.joining(" OR "));
                    solrQuery.addFilterQuery(fq);
                }
            }
            // 构造动态查询条件
            List<String> searchFqs = dynamicFieldService.getSearchFqs(dynamicSearch);
            if (searchFqs.size() > 0) {
                solrQuery.addFilterQuery(searchFqs.toArray(new String[0]));
            }

            long count = SolrQueryGenerator.count(EngineUtil.MEDICAL_UNREASONABLE_ACTION, solrQuery);

            if (count == 0) {
                return Result.error("没有需要判定的记录");
            }
            if (count < 200000) {
                reviewNewFirstService.pushRecord(solrQuery, commonJson);
                return Result.ok("判定成功");
            } else {
                ThreadUtils.ASYNC_POOL.addSecJudge(searchObj, solrQuery.getFilterQueries(), (int) count, (processFunc) -> {
                    try {
                        reviewNewFirstService.pushRecord(solrQuery, commonJson, processFunc);
                        return Result.ok("判定成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Result.error(e.getMessage());
                    }
                });
                return Result.ok("正在批量修改判定结果，请稍后查看");
            }


        } else {
            SolrInputDocument commonDoc = new SolrInputDocument();
            for (Map.Entry<String, Object> entry : commonJson.entrySet()) {
                commonDoc.setField(entry.getKey(), entry.getValue());
            }
            this.reviewNewFirstService.pushRecordByIds(Arrays.asList(ids.split(",")), commonDoc);
            return Result.ok("判定成功");
        }


    }

    @AutoLog(value = "复审-初审合格")
    @ApiOperation(value = "复审-初审合格", notes = "复审-初审合格")
    @PutMapping(value = "/copyReviewStatus")
    public Result<?> copyReviewStatus(String ids, String groupBys, String reviewInfo,
                                        @RequestParam(name = "batchId") String batchId,
                                        MedicalUnreasonableActionVo searchObj,
                                        String dynamicSearch,
                                        HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        JSONObject commonJson = new JSONObject();
        // 判断结果信息
        commonJson.put("SEC_REVIEW_USERID", SolrUtil.initActionValue(user.getId(), "set"));
        commonJson.put("SEC_REVIEW_USERNAME", SolrUtil.initActionValue(user.getRealname(), "set"));
        commonJson.put("SEC_REVIEW_TIME", SolrUtil.initActionValue(TimeUtil.getNowTime(), "set"));

        Map<String, String> copyFlMap = new HashMap<>();
        copyFlMap.put("FIR_REVIEW_STATUS", "SEC_REVIEW_STATUS");
        copyFlMap.put("FIR_REVIEW_CLASSIFY", "SEC_REVIEW_CLASSIFY");
        copyFlMap.put("FIR_REVIEW_REMARK", "SEC_REVIEW_REMARK");

        if (StringUtils.isBlank(ids) || StringUtils.isNotBlank(groupBys)) {
            // 构造主表条件
            SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
            // 清除排序字段，分组排序val或count会报错
            solrQuery.clearSorts();
            solrQuery.addFilterQuery("-SEC_PUSH_STATUS:1");
            // 分组勾选条件
            if (StringUtils.isNotBlank(ids) && StringUtils.isNotBlank(groupBys)) {
                List<String> groupByList = Arrays.asList(groupBys.split(","));
                if (groupByList.size() == 1) {
                    solrQuery.addFilterQuery(groupByList.get(0) + ":(\"" + ids.replaceAll(",", "\",\"") + "\")");
                } else {
                    String fq = Arrays.stream(ids.split(",")).map(id -> {
                        String[] groupVals = id.split("::");
                        final AtomicInteger i = new AtomicInteger(0);
                        int valsLen = groupVals.length;
                        // 构造 (f1:"1" AND  f2:"2") OR (f1:"2" AND  f2:"3")
                        return "(" + groupByList.stream()
                                .map(r -> r + ":\"" + (i.get() >= valsLen ? "" : groupVals[i.getAndIncrement()]) + "\"")
                                .collect(Collectors.joining(" AND "))
                                + ")";
                    }).collect(Collectors.joining(" OR "));
                    solrQuery.addFilterQuery(fq);
                }
            }
            // 构造动态查询条件
            List<String> searchFqs = dynamicFieldService.getSearchFqs(dynamicSearch);
            if (searchFqs.size() > 0) {
                solrQuery.addFilterQuery(searchFqs.toArray(new String[0]));
            }

            long count = SolrQueryGenerator.count(EngineUtil.MEDICAL_UNREASONABLE_ACTION, solrQuery);

            if (count == 0) {
                return Result.error("没有需要判定的记录");
            }
            if (count < 200000) {
                reviewNewFirstService.copyRecordProp(solrQuery, commonJson, copyFlMap);
                return Result.ok("判定成功");
            } else {
                ThreadUtils.ASYNC_POOL.addSecJudge(searchObj, solrQuery.getFilterQueries(), (int) count, (processFunc) -> {
                    try {
                        reviewNewFirstService.copyRecordProp(solrQuery, commonJson,copyFlMap,  processFunc);
                        return Result.ok("判定成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Result.error(e.getMessage());
                    }
                });
                return Result.ok("正在批量修改判定结果，请稍后查看");
            }


        } else {
            this.reviewNewFirstService.copyRecordPropByIds(Arrays.asList(ids.split(",")), commonJson, copyFlMap);
            return Result.ok("判定成功");
        }


    }


    @AutoLog(value = "复审-批量导入审核数据")
    @ApiOperation(value = "复审-批量导入审核数据", notes = "复审-批量导入审核数据")
    @PostMapping(value = "/importReviewExcel")
    public Result<?> importCaseExcel(@RequestParam("file") MultipartFile file, MedicalUnreasonableActionVo searchObj
            , HttpServletResponse response) {
        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {
                String msg = this.service.importReviewStatus(file, searchObj);
                if (msg == null) {
                    return Result.ok("数据量过大，正在后台异步导入，可在“异步操作日志”中查看进度");
                } else {
                    return Result.ok("导入成功，" + msg);
                }

            } catch (Exception e) {
//                e.printStackTrace();
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        } else {
            return Result.error("导入失败，文件存在问题");
        }

    }

    @AutoLog(value = "复审-批量导入分组统计审核数据")
    @ApiOperation(value = "复审-批量导入分组统计审核数据", notes = "复审-批量导入分组统计审核数据")
    @PostMapping(value = "/importGroupReviewExcel")
    public Result<?> importGroupReviewExcel(@RequestParam("file") MultipartFile file, MedicalUnreasonableActionVo searchObj
            , String dynamicSearch, HttpServletRequest req) {
        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {

                // 构造主表条件
                SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
                // 构造动态查询条件
                List<String> searchFqs = dynamicFieldService.getSearchFqs(dynamicSearch);
                if (searchFqs.size() > 0) {
                    solrQuery.addFilterQuery(searchFqs.toArray(new String[0]));
                }
                solrQuery.addFilterQuery("PUSH_STATUS:1");
                solrQuery.clearSorts();
                String msg = this.service.importGroupReviewStatus(file, searchObj, solrQuery);
                if (msg == null) {
                    return Result.ok("数据量过大，正在后台异步导入，可在“异步操作日志”中查看进度");
                } else {
                    return Result.ok("导入成功，" + msg);
                }

            } catch (Exception e) {
//                e.printStackTrace();
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        } else {
            return Result.error("导入失败，文件存在问题");
        }

    }


    private JSONObject initInputJson(Object obj) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        JSONObject json = new JSONObject();
        List<Field> fieldList = new ArrayList<>();
        Class clzz = obj.getClass();
        do {
            fieldList.addAll(Arrays.asList(clzz.getDeclaredFields()));
            clzz = clzz.getSuperclass(); //得到父类,然后赋给自己
            //当父类为null的时候说明到达了最上层的父类(Object类).
        } while (clzz != null);

        for (Field field : fieldList) {
            field.setAccessible(true);
            String name = field.getName();
            Object value = field.get(obj);
            String docName = StringCamelUtils.camel2Underline(name);
            if (value != null) {
                if ("id".equals(name)) {
                    json.put(name, value);
                } else {
                    json.put(docName, SolrUtil.initActionValue(value, "set"));
                }
            }
        }

        return json;
    }
}
