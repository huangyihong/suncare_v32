package com.ai.modules.task.controller;

import com.ai.common.MedicalConstant;
import com.ai.common.query.SolrQueryGenerator;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.review.vo.MedicalUnreasonableActionVo;
import com.ai.modules.task.dto.TaskReviewAssignDTO;
import com.ai.modules.task.dto.TaskReviewAssignManualDTO;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.entity.TaskReviewAssign;
import com.ai.modules.task.service.ITaskBatchBreakRuleDelService;
import com.ai.modules.task.service.ITaskReviewAssignService;
import com.ai.modules.task.vo.TaskReviewRuleTotalVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 系统审核任务分配
 * @Author: jeecg-boot
 * @Date: 2020-05-19
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "审核任务分配")
@RestController
@RequestMapping("/task/taskReviewAssign")
public class TaskReviewAssignController extends JeecgController<TaskReviewAssign, ITaskReviewAssignService> {
    @Autowired
    private ITaskReviewAssignService taskReviewAssignService;

    @Autowired
    private ITaskBatchBreakRuleDelService taskBatchBreakRuleDelService;

    @AutoLog(value = "系统审核任务分配-组长获取列表")
    @ApiOperation(value = "系统审核任务分配-组长获取列表", notes = "系统审核任务分配-组长获取列表")
    @GetMapping(value = "/listBySelf")
    public Result<?> listBySelf(@RequestParam(name = "batchId") String batchId,
                                @RequestParam(name = "ruleType", required = false) String ruleType,
                                @RequestParam(name = "step") Integer step
    ) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<TaskReviewAssign> list = taskReviewAssignService.listJoinUserInfo(batchId, user.getId(), ruleType, step);
        return Result.ok(list);
    }

    @AutoLog(value = "系统审核任务分配-获取批次模型结果和审核情况")
    @ApiOperation(value = "系统审核任务分配-获取批次模型结果和审核情况", notes = "系统审核任务分配-获取批次模型结果和审核情况")
    @GetMapping(value = "/queryRuleResult")
    public Result<?> queryRuleResult(@RequestParam(name = "batchId") String batchId, @RequestParam(name = "ruleType") String ruleType) {
        List<TaskReviewRuleTotalVO> list = taskReviewAssignService.ruleResultInfo(batchId, ruleType);
        return Result.ok(list);
    }

    @AutoLog(value = "人工审核-获取病例审核情况")
    @ApiOperation(value = "人工审核-获取病例审核情况", notes = "人工审核-获取病例审核情况")
    @GetMapping(value = "/queryManualResult")
    public Result<?> queryManualResult(@RequestParam(name = "batchId") String batchId) {

        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("BATCH_ID:" + batchId);
        solrQuery.addFilterQuery("REVIEW_CASE_IDS: * OR CLINICAL_IDS:*");
        solrQuery.setRows(0);
        String sb = "{" +
                "group:{ type: terms, field: \"FIR_REVIEW_USERID\"" +
                ", limit: " + Integer.MAX_VALUE +
                ", overrequest:" + Integer.MAX_VALUE + " }," +
                "body:{type: query, q: \"FIR_REVIEW_STATUS:* AND -FIR_REVIEW_STATUS:init\", " +
                "facet:{ " +
                "group:{ type: terms, field: \"FIR_REVIEW_USERID\"" +
                ",  limit: " + Integer.MAX_VALUE +
                ", overrequest:" + Integer.MAX_VALUE + " } } }}";
        solrQuery.set("json.facet", sb);

        QueryResponse queryResponse;
        try {
            queryResponse = SolrUtil.call(solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        JSONObject result = new JSONObject();
        NestableJsonFacet nestableJsonFacet = queryResponse.getJsonFacetingResponse();
        List<BucketJsonFacet> userList = nestableJsonFacet.getBucketBasedFacets("group").getBuckets();
        Map<String, Long> userMap = new HashMap<>();
        for(BucketJsonFacet bucketJsonFacet: userList){
            userMap.put(String.valueOf(bucketJsonFacet.getVal()), bucketJsonFacet.getCount());
        }
        long total = nestableJsonFacet.getCount();
        nestableJsonFacet = nestableJsonFacet.getQueryFacet("body");

        Map<String, Long> userAuditedMap = new HashMap<>();
        long audited = nestableJsonFacet.getCount();
        if(audited > 0){
            List<BucketJsonFacet> userInfoList = nestableJsonFacet.getBucketBasedFacets("group").getBuckets();
            for(BucketJsonFacet bucketJsonFacet: userInfoList){
                userAuditedMap.put(String.valueOf(bucketJsonFacet.getVal()), bucketJsonFacet.getCount());
            }
        }
        result.put("total", total);
        result.put("audited", audited);
        result.put("userMap", userMap);
        result.put("userAuditedMap", userAuditedMap);

        return Result.ok(result);
    }

    @AutoLog(value = "系统审核任务分配-获取归属组员审核规则")
    @ApiOperation(value = "系统审核任务分配-获取归属组员审核规则", notes = "系统审核任务分配-获取归属组员审核规则")
    @GetMapping(value = "/listRuleByUserId")
    public Result<?> listRuleByUserId(@RequestParam(name = "batchId") String batchId
            , @RequestParam(name = "ruleType") String ruleType
            , @RequestParam(name = "userId") String userId) {

        QueryWrapper<TaskBatchBreakRuleDel> queryWrapper = new QueryWrapper<TaskBatchBreakRuleDel>()
                .eq("BATCH_ID", batchId)
                .eq("RULE_TYPE", ruleType)
                .eq("REVIEW_USERID", userId)
                .select("ID", "BUSI_NAME", "CASE_ID", "CASE_NAME", "STATUS");
        List<TaskBatchBreakRuleDel> list = taskBatchBreakRuleDelService.list(queryWrapper);
        return Result.ok(list);
    }

    @AutoLog(value = "系统审核任务分配-获取归属组员审核规则ID")
    @ApiOperation(value = "系统审核任务分配-获取归属组员审核规则ID", notes = "系统审核任务分配-获取归属组员审核规则ID")
    @GetMapping(value = "/listRuleId")
    public Result<?> listRuleId(@RequestParam(name = "batchId") String batchId
            , @RequestParam(name = "ruleType") String ruleType) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        QueryWrapper<TaskBatchBreakRuleDel> queryWrapper = new QueryWrapper<TaskBatchBreakRuleDel>()
                .eq("BATCH_ID", batchId)
                .eq("RULE_TYPE", ruleType)
                .eq("REVIEW_USERID", user.getId())
                .select("CASE_ID");
        List<Map<String, Object>> list = taskBatchBreakRuleDelService.listMaps(queryWrapper);
        return Result.ok(list);
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "系统审核任务分配-通过id查询")
    @ApiOperation(value = "系统审核任务分配-通过id查询", notes = "系统审核任务分配-通过id查询")
    @GetMapping(value = "/queryByUserID")
    public Result<?> queryByUserID(@RequestParam(name = "member") String member,
                               @RequestParam(name = "batchId") String batchId,
                               @RequestParam(name = "step") Integer step) {
        QueryWrapper<TaskReviewAssign> queryWrapper = new QueryWrapper<TaskReviewAssign>()
                .eq("MEMBER", member)
                .eq("BATCH_ID", batchId)
                .eq("STEP", step);
        TaskReviewAssign taskReviewAssign = taskReviewAssignService.getOne(queryWrapper);
        return Result.ok(taskReviewAssign);
    }

    /**
     * 添加
     *
     * @param taskReviewAssign
     * @return
     */
    @AutoLog(value = "人工审核任务分配-添加")
    @ApiOperation(value = "人工审核任务分配-添加", notes = "人工审核任务分配-添加")
    @PostMapping(value = "/addManual")
    @Transactional
    public Result<?> addManual(@RequestBody TaskReviewAssignManualDTO taskReviewAssign) {
        // 设置病例归属
        String memberId = taskReviewAssign.getMember();
        String memberName = taskReviewAssign.getMemberName();
        try {
            String[] selectKeys = taskReviewAssign.getSelectKeys();
            // 选中的记录
            if (selectKeys != null && selectKeys.length > 0) {
                SolrClient solrClient = SolrUtil.getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
                for (String id : selectKeys) {
                    // 构造更新参数
                    SolrInputDocument document = new SolrInputDocument();
                    document.addField("id", id);
                    document.setField("FIR_REVIEW_USERID", SolrUtil.initActionValue(memberId, "set"));
                    document.setField("FIR_REVIEW_USERNAME", SolrUtil.initActionValue(memberName, "set"));
                    solrClient.add(document);

                }
                solrClient.commit();
                solrClient.close();
            } else {
                // 全部 或 区间
                cn.hutool.json.JSONObject paramJson = taskReviewAssign.getParams();
                MedicalUnreasonableActionVo paramBean = paramJson.toBean(MedicalUnreasonableActionVo.class);
                // 构造查询参数map
                Map<String, String[]> requestMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : paramJson.entrySet()) {
                    requestMap.put(entry.getKey(), new String[]{entry.getValue().toString()});
                }
                SolrQuery solrQuery = SolrQueryGenerator.initQuery(paramBean, requestMap);
                if (taskReviewAssign.getRangeStart() != null && taskReviewAssign.getRangeEnd() != null) {
                    solrQuery.setStart(taskReviewAssign.getRangeStart());
                    solrQuery.setRows(taskReviewAssign.getRangeEnd() - taskReviewAssign.getRangeStart());
                } else {
                    solrQuery.setStart(0);
                    solrQuery.setRows(Integer.MAX_VALUE);
                }
                solrQuery.setFields("id");
                // 获取查询出的ID
                SolrClient solrClient = SolrUtil.getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
                SolrUtil.export(solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (map, index) -> {
                    String id = String.valueOf(map.get("id"));
                    // 构造更新参数
                    SolrInputDocument document = new SolrInputDocument();
                    document.addField("id", id);
                    document.setField("FIR_REVIEW_USERID", SolrUtil.initActionValue(memberId, "set"));
                    document.setField("FIR_REVIEW_USERNAME", SolrUtil.initActionValue(memberName, "set"));
                    try {
                        solrClient.add(document);
                    } catch (SolrServerException | IOException e) {
                        e.printStackTrace();
                    }
                });
                // 提交
                solrClient.commit();
                solrClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        if(StringUtils.isBlank(taskReviewAssign.getAssignId())){
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            taskReviewAssign.setLeader(user.getId());
            taskReviewAssignService.save(taskReviewAssign);
        }
        return Result.ok("添加成功！");
    }

    /**
     * 添加
     *
     * @param taskReviewAssign
     * @return
     */
    @AutoLog(value = "系统审核任务分配-添加")
    @ApiOperation(value = "系统审核任务分配-添加", notes = "系统审核任务分配-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody TaskReviewAssignDTO taskReviewAssign) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        taskReviewAssign.setLeader(user.getId());
        taskReviewAssignService.save(taskReviewAssign);
        // 设置模型审核归属
        List<String> caseIds = taskReviewAssign.getCaseIds();
        if (caseIds.size() > 0) {
            UpdateWrapper<TaskBatchBreakRuleDel> updateWrapper = new UpdateWrapper<TaskBatchBreakRuleDel>()
                    .eq("BATCH_ID", taskReviewAssign.getBatchId())
                    .in("CASE_ID", caseIds)
                    .isNull("REVIEW_USERID")
                    .set("REVIEW_USERID", taskReviewAssign.getMember())
                    .set("REVIEW_USERNAME", taskReviewAssign.getMemberName());
            taskBatchBreakRuleDelService.update(updateWrapper);
        }
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param taskReviewAssign
     * @return
     */
    @AutoLog(value = "系统审核任务分配-编辑")
    @ApiOperation(value = "系统审核任务分配-编辑", notes = "系统审核任务分配-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody TaskReviewAssignDTO taskReviewAssign) {
        taskReviewAssignService.updateById(taskReviewAssign);        // 设置模型审核归属
        List<String> caseIds = taskReviewAssign.getCaseIds();
        // 设置模型审核归属
        resetRuleBelong(taskReviewAssign);
        // 设置模型审核归属
        if (caseIds.size() > 0) {
            UpdateWrapper<TaskBatchBreakRuleDel> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("BATCH_ID", taskReviewAssign.getBatchId())
                    .in("CASE_ID", caseIds)
                    .isNull("REVIEW_USERID")
                    .set("REVIEW_USERID", taskReviewAssign.getMember())
                    .set("REVIEW_USERNAME", taskReviewAssign.getMemberName());
            taskBatchBreakRuleDelService.update(updateWrapper);
        }
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param assignId
     * @return
     */
    @AutoLog(value = "系统审核任务分配-通过id删除")
    @ApiOperation(value = "系统审核任务分配-通过id删除", notes = "系统审核任务分配-通过id删除")
    @DeleteMapping(value = "/deleteManual")
    public Result<?> deleteManual(@RequestParam(name = "id") String assignId) {
        TaskReviewAssign taskReviewAssign = taskReviewAssignService.getById(assignId);

        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.setFields("id");
        solrQuery.addFilterQuery(
                "BATCH_ID:" + taskReviewAssign.getBatchId(),
                "FIR_REVIEW_USERID:" + taskReviewAssign.getMember(),
                "FIR_REVIEW_STATUS:init"
        );
        // 获取查询出的ID
        SolrClient solrClient = SolrUtil.getClient(EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        try {
            SolrUtil.export(solrQuery, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (map, index) -> {
                String id = String.valueOf(map.get("id"));
                // 构造更新参数
                SolrInputDocument document = new SolrInputDocument();
                document.addField("id", id);
                document.setField("FIR_REVIEW_USERID", SolrUtil.initActionValue(null, "set"));
                document.setField("FIR_REVIEW_USERNAME", SolrUtil.initActionValue(null, "set"));
                try {
                    solrClient.add(document);
                } catch (SolrServerException | IOException e) {
                    e.printStackTrace();
                }
            });
            // 提交
            solrClient.commit();
            solrClient.close();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }

        taskReviewAssignService.removeById(assignId);
        return Result.ok("删除成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "人工审核任务分配-通过id删除")
    @ApiOperation(value = "人工审核任务分配-通过id删除", notes = "人工审核任务分配-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        TaskReviewAssign taskReviewAssign = taskReviewAssignService.getById(id);

        // 设置模型审核归属
        resetRuleBelong(taskReviewAssign);

        taskReviewAssignService.removeById(id);
        return Result.ok("删除成功!");
    }

    private boolean resetRuleBelong(TaskReviewAssign taskReviewAssign) {
        // 设置模型审核归属
        UpdateWrapper<TaskBatchBreakRuleDel> updateWrapper = new UpdateWrapper<TaskBatchBreakRuleDel>()
                .eq("BATCH_ID", taskReviewAssign.getBatchId())
                .eq("REVIEW_USERID", taskReviewAssign.getMember())
                .ne("REVIEW_STATUS", MedicalConstant.REVIEW_STATE_AUDITED)
                .setSql("REVIEW_USERID = NULL")
                .setSql("REVIEW_USERNAME = NULL")
    /*            .set("REVIEW_USERID", null) 报错
                .set("REVIEW_USERID", null)*/;
        return taskBatchBreakRuleDelService.update(updateWrapper);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
/*	@AutoLog(value = "系统审核任务分配-批量删除")
	@ApiOperation(value="系统审核任务分配-批量删除", notes="系统审核任务分配-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.taskReviewAssignService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}*/

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "系统审核任务分配-通过id查询")
    @ApiOperation(value = "系统审核任务分配-通过id查询", notes = "系统审核任务分配-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        TaskReviewAssign taskReviewAssign = taskReviewAssignService.getById(id);
        return Result.ok(taskReviewAssign);
    }

}
