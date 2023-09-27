package com.ai.modules.review.controller;

import com.ai.common.query.SolrQueryGenerator;
import com.ai.common.utils.StringUtil;
import com.ai.common.utils.ThreadUtils;
import com.ai.common.utils.TimeUtil;
import com.ai.modules.review.service.IDynamicFieldService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.entity.NewV3Tmp;
import com.ai.modules.review.runnable.EnginePushRunnable;
import com.ai.modules.review.service.IReviewNewFirstService;
import com.ai.modules.review.service.IReviewNewPushService;
import com.ai.modules.review.service.IReviewService;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.review.vo.ReviewNewPushVo;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "推送")
@RestController
@RequestMapping("/reviewNewPush")
public class ReviewNewPushController {
    @Autowired
    private IReviewNewPushService service;

    @Autowired
    private IReviewNewFirstService reviewNewFirstService;
    @Autowired
    IReviewService reviewService;

    @Autowired
    IDynamicFieldService dynamicFieldService;

    private static Map<String, String> FIELD_MAPPING = SolrUtil.initFieldMap(ReviewNewPushVo.class);

    /**
     * 推送列表-分页列表查询
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
   /* @AutoLog(value = "推送列表-不合规行为结果分页列表查询")
    @ApiOperation(value = "推送列表-不合规行为结果分页列表查询", notes = "推送列表-不合规行为结果分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> list(MedicalUnreasonableActionVo searchObj,
                          @RequestParam(name = "batchId") String batchId,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) throws Exception {
        SolrQuery solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());

        Page<MedicalUnreasonableActionVo> page = new Page<>(pageNo, pageSize);
        IPage<MedicalUnreasonableActionVo> pageList = SolrQueryGenerator.page(page, MedicalUnreasonableActionVo.class,
                solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, FIELD_MAPPING);

        return Result.ok(pageList);
    }*/

    /**
     * 查询DWB_MASTER_INFO
     *
     * @param tmpIds
     * @param fields
     * @param req
     * @return
     */
    @AutoLog(value = "查询-风控报告就诊信息查询")
    @ApiOperation(value = "查询-风控报告就诊信息查询", notes = "查询-风控报告就诊信息查询")
    @PostMapping(value = "/queryDwbMasterInfoList")
    public Result<?> queryDwbMasterInfoList(@RequestParam(value = "tmpIds") List<String> tmpIds,
                                            @RequestParam(value = "fields") List<String> fields,
                                            HttpServletRequest req) throws Exception {
        // 分割表和字段
        Map<String, Set<String>> tabFieldMap = new HashMap<>();
        for (String filed : fields) {
            String[] tabFiledArray = filed.split("\\.");
            if ("action".equals(tabFiledArray[0])) {
                continue;
            }
            if (tabFiledArray[1].startsWith("ALIA")) {
                tabFiledArray[1] = tabFiledArray[1] + ":" + tabFiledArray[1].substring(tabFiledArray[1].indexOf("_") + 1);
            }
            Set<String> fieldList = tabFieldMap.computeIfAbsent(tabFiledArray[0], k -> new HashSet<>());
            fieldList.add(tabFiledArray[1]);
        }
        String idFq = "id:(\"" + StringUtil.join(tmpIds, "\",\"") + "\")";
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery(idFq);
        Page<SolrDocument> page = new Page<>(1, tmpIds.size());
        IPage<SolrDocument> pageList = reviewService.pageDynamicResult(tabFieldMap, solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, page);
        return Result.ok(pageList.getRecords());
    }


    /**
     *
     * @param key 查询的关联字段
     * @param tmpIds 关联字段值数组
     * @param fields 返回字段数组
     * @param tableName 查询的表
     * @param req
     * @return
     * @throws Exception
     */
    @AutoLog(value = "查询-反查表字段信息")
    @ApiOperation(value = "查询-反查表字段信息", notes = "查询-反查表字段信息")
    @PostMapping(value = "/queryCollectionFields")
    public Result<?> queryCollectionFields(@RequestParam(value = "key") String key,
                                           @RequestParam(value = "tmpIds") List<String> tmpIds,
                                           @RequestParam(value = "fields") List<String> fields,
                                           @RequestParam(value = "tableName") String tableName,
                                           HttpServletRequest req) throws Exception {
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery(key + ":(\"" + StringUtils.join(tmpIds, "\",\"") + "\")" );
        solrQuery.setFields(fields.toArray(new String[0]));
        SolrDocumentList list = SolrQueryGenerator.list(tableName, solrQuery);
        return Result.ok(list);
    }

    @AutoLog(value = "查询-风控报告DWB表信息查询")
    @ApiOperation(value = "查询-风控报告DWB表信息查询", notes = "查询-风控报告DWB表信息查询")
    @GetMapping(value = "/queryDwbByVisitid")
    public Result<?> queryDwbByVisitid(@RequestParam(value = "fields") String fields,
                                       @RequestParam(value = "visitid") String visitid,
                                       HttpServletRequest req) throws Exception {
        // 分割表和字段
        // 分割表和字段
        Map<String, Set<String>> tabFieldMap = new HashMap<>();
        String[] tabFields = fields.split(";");
        for (String tabFiled : tabFields) {
            String[] tabFiledArray = tabFiled.split(":");
            if ("action".equals(tabFiledArray[0])) {
                continue;
            }
            tabFieldMap.put(tabFiledArray[0], new HashSet<>(Arrays.asList(tabFiledArray[1].split(","))));
        }
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("VISITID:" + visitid);
        Map<String, SolrDocumentList> map = dynamicFieldService.listDynamicResult(tabFieldMap, solrQuery);
        return Result.ok(map);
    }


    @AutoLog(value = "导出-不合规结果数据")
    @ApiOperation(value = "导出-不合规结果数据", notes = "导出-不合规结果数据")
    @PutMapping(value = "/exportMedicalUnreasonableAction")
    public Result<?> exportMedicalUnreasonableAction(MedicalUnreasonableActionVo searchObj,
                                                     @RequestParam(name = "batchId") String batchId,
                                                     NewV3Tmp tmpBean,
                                                     HttpServletRequest req) throws Exception {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery = SolrQueryGenerator.initQuery(searchObj, req.getParameterMap());
        solrQuery.addFilterQuery("BATCH_ID:" + batchId);
        tmpBean.setTaskBatchId(batchId);
        String ds = SolrUtil.getLoginUserDatasource();
        EnginePushRunnable runnable = new EnginePushRunnable(ds, solrQuery, tmpBean, null);
        ThreadUtils.THREAD_SOLR_REQUEST_POOL.add(runnable);

        return Result.ok("正在推送，请稍后查看状态");
    }

    @AutoLog(value = "违规明细导入")
    @ApiOperation(value = "违规明细导入", notes = "违规明细导入")
    @PostMapping(value = "/importMedicalUnreasonableAction")
    public Result<?> importMedicalUnreasonableAction(@RequestParam("file") MultipartFile file, TaskProjectBatch taskProjectBatch, HttpServletResponse response) {
        // 获取文件名
        String name = file.getOriginalFilename();
        // 判断文件大小、即名称
        long size = file.getSize();
        if (StringUtils.isNotBlank(name) && size > 0) {
            try {
                if(StringUtils.isBlank(taskProjectBatch.getBatchName())){
                    return Result.error("批次名称为空，导入失败：");
                }
                if(StringUtils.isBlank(taskProjectBatch.getMonth())){
                    return Result.error("风控月份为空，导入失败：");
                }
                String msg = this.service.importMedicalUnreasonableAction(file, taskProjectBatch);
                if(msg == null){
                    return Result.ok("数据量过大，正在后台异步导入，可在“异步操作日志”中查看进度");
                } else {
                    return Result.ok("导入成功，" + msg);
                }

            } catch (Exception e) {
                return Result.error("导入 " + name + " 失败：" + e.getMessage());
            }

        } else {
            return Result.error("导入失败，文件存在问题");
        }

    }
}
