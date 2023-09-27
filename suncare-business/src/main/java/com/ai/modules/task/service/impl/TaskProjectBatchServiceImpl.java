package com.ai.modules.task.service.impl;

import com.ai.common.MedicalConstant;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.mapper.MedicalFormalBehaviorMapper;
import com.ai.modules.formal.mapper.MedicalFormalCaseBehaviorMapper;
import com.ai.modules.formal.mapper.MedicalFormalCaseMapper;
import com.ai.modules.his.entity.*;
import com.ai.modules.his.mapper.*;
import com.ai.modules.task.dto.TaskBatchExecInfo;
import com.ai.modules.task.entity.*;
import com.ai.modules.task.mapper.TaskBatchBreakRuleDelMapper;
import com.ai.modules.task.mapper.TaskBatchBreakRuleMapper;
import com.ai.modules.task.mapper.TaskBatchStepItemMapper;
import com.ai.modules.task.mapper.TaskProjectBatchMapper;
import com.ai.modules.task.service.ITaskBatchBreakRuleService;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.ai.modules.task.service.ITaskReviewAssignService;
import com.ai.modules.task.vo.TaskBatchStepItemVO;
import com.ai.modules.task.vo.TaskProjectBatchVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 任务项目批次
 * @Author: jeecg-boot
 * @Date: 2020-01-03
 * @Version: V1.0
 */
@Service
public class TaskProjectBatchServiceImpl extends ServiceImpl<TaskProjectBatchMapper, TaskProjectBatch> implements ITaskProjectBatchService {

    @Autowired
    ITaskBatchBreakRuleService taskBatchBreakRuleService;
    @Autowired
    private ITaskReviewAssignService taskReviewAssignService;

    @Autowired
    TaskBatchBreakRuleMapper taskBatchBreakRuleMapper;

    @Autowired
    TaskBatchBreakRuleDelMapper taskBatchBreakRuleDelMapper;

    @Autowired
    TaskBatchStepItemMapper taskBatchStepItemMapper;

    @Autowired
    MedicalFormalBehaviorMapper medicalFormalBehaviorMapper;

    @Autowired
    MedicalFormalCaseBehaviorMapper medicalFormalCaseBehaviorMapper;

    @Autowired
    HisMedicalFormalBusiMapper hisMedicalFormalBusiMapper;

    @Autowired
    HisMedicalFormalCaseBusiMapper hisMedicalFormalCaseBusiMapper;

    @Autowired
    HisFormalFlowRuleGradeMapper hisFormalFlowRuleGradeMapper;

    @Autowired
    HisMedicalFormalFlowRuleMapper hisMedicalFormalFlowRuleMapper;

    @Autowired
    HisMedicalFormalFlowMapper hisMedicalFormalFlowMapper;

    @Autowired
    HisMedicalFormalCaseMapper hisMedicalFormalCaseMapper;

    @Autowired
    MedicalFormalCaseMapper medicalFormalCaseMapper;

    @Autowired
    private BackMapper backMapper;

    @Autowired
    private CopyHisMapper copyMapper;

    @Override
    @Transactional
    public void saveBatch(TaskProjectBatch taskProjectBatch, List<TaskBatchBreakRule> ruleList) {
        this.baseMapper.insert(taskProjectBatch);
        // 插入规则列表
        String batchId = taskProjectBatch.getBatchId();

        if (ruleList.size() == 0){
            return;
        }

        ruleList.forEach(r -> r.setBatchId(batchId));

        // 现在改成执行批次的时候备份

        // 临床路径资料库不用备份
       /* List<TaskBatchBreakRule> breakRuleList = new ArrayList<>(); // 存放关联关系
        List<String> busiIdList = new ArrayList<>(); // 存放新增 业务组
        Map<String, List<String>> hisBatchRuleMap = new HashMap<>(); // 出存放历史复制

        for (TaskBatchBreakRule rule : ruleList) {
            String hisBatchId = rule.getBatchId();
            if (hisBatchId == null) {
                if(MedicalConstant.RULE_TYPE_CASE.equals(rule.getRuleType())){
                    busiIdList.add(rule.getRuleId());
                }
            } else if(MedicalConstant.RULE_TYPE_CASE.equals(rule.getRuleType())){
                List<String> list = hisBatchRuleMap.computeIfAbsent(hisBatchId, k -> new ArrayList<>());
                list.add(rule.getRuleId());
            }

            rule.setBatchId(batchId);
            breakRuleList.add(rule);
        }
        if(busiIdList.size() > 0){
            // 备份
            this.backFlowCase(batchId,busiIdList);
        }

        if(hisBatchRuleMap.size() > 0){
            // 从备份表 复制
            hisBatchRuleMap.forEach((key,list) -> this.copyHisFlowCase(batchId,key,list));
        }*/
        // 保存批次规则关联
        taskBatchBreakRuleService.saveBatch(ruleList);
    }

    @Override
    @Transactional
    public void updateBatch(TaskProjectBatch taskProjectBatch, List<TaskBatchBreakRule> ruleList, List<String> editRuleTypes) {
        this.baseMapper.updateById(taskProjectBatch);
        // 插入规则列表
        String batchId = taskProjectBatch.getBatchId();
        // 删除旧的关联
        this.taskBatchBreakRuleService.remove(
                new QueryWrapper<TaskBatchBreakRule>()
                        .eq("BATCH_ID", batchId)
                        .in("RULE_TYPE", editRuleTypes));


        if (ruleList.size() == 0){
            return;
        }

        ruleList.forEach(r -> r.setBatchId(batchId));

        // 保存批次规则关联
        taskBatchBreakRuleService.saveBatch(ruleList);
        /*List<TaskBatchBreakRule> breakRuleList = new ArrayList<>(); // 存放关联关系
        List<String> busiIdList = new ArrayList<>(); // 存放新增 业务组
        Map<String, List<String>> hisBatchRuleMap = new HashMap<>(); // 出存放历史复制

        for (TaskBatchBreakRule rule : ruleList) {
            String hisBatchId = rule.getBatchId();
            if (hisBatchId == null) {
                if(MedicalConstant.RULE_TYPE_CASE.equals(rule.getRuleType())){
                    busiIdList.add(rule.getRuleId());
                }
            } else if(MedicalConstant.RULE_TYPE_CASE.equals(rule.getRuleType())){
                List<String> list = hisBatchRuleMap.computeIfAbsent(hisBatchId, k -> new ArrayList<>());
                list.add(rule.getRuleId());
            }

            rule.setBatchId(batchId);
            breakRuleList.add(rule);
        }

        List<String> inHisRuleIdList = hisBatchRuleMap.get(batchId);
        if(inHisRuleIdList == null){
            inHisRuleIdList = new ArrayList<>();
        } else {
            hisBatchRuleMap.remove(batchId);
        }
        // 删除被删除的历史模型项
        this.deleteHisFlowCaseNotIn(batchId,inHisRuleIdList);

        if(busiIdList.size() > 0){
            // 备份
            this.backFlowCase(batchId,busiIdList);
        }

        if(hisBatchRuleMap.size() > 0){
            // 从备份表 复制
            hisBatchRuleMap.forEach((key,list) -> this.copyHisFlowCase(batchId,key,list));
        }
        // 删除旧的关联
        this.taskBatchBreakRuleService.remove(
                new QueryWrapper<TaskBatchBreakRule>()
                        .eq("BATCH_ID", batchId)
                        .in("RULE_TYPE", ruleTypes));

        // 保存批次规则关联
        taskBatchBreakRuleService.saveBatch(breakRuleList);*/
    }

    @Override
    @Transactional
    public TaskProjectBatch removeBatch(String batchId) throws Exception {
        List<TaskBatchStepItem> stepRunList = taskBatchStepItemMapper.selectList(
                new QueryWrapper<TaskBatchStepItem>()
                        .eq("BATCH_ID",batchId)
                        .eq("STATUS",MedicalConstant.RUN_STATE_RUNNING));
        if(stepRunList.size() > 0){
            throw new Exception("该批次正在计算中，请完成后操作");
        }

        TaskProjectBatch taskProjectBatch = this.baseMapper.selectById(batchId);
        /*int step = taskProjectBatch.getStep();
        try {
            *//*if (step >= MedicalConstant.BATCH_STEP_ACTION) {
                // 删除不合规数据
                SolrUtil.delete(EngineUtil.MEDICAL_BREAK_BEHAVIOR_RESULT, "BATCH_ID:" + batchId);
                step--;
            }*//*
            if (step >= MedicalConstant.BATCH_STEP_SYSTEM) {
                // 删除系统审核(人工审核)数据
                SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "BATCH_ID:" + batchId);
                SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, "BATCH_ID:" + batchId);
                step -= 2;
                *//*taskReviewAssignService.remove(
                        new QueryWrapper<TaskReviewAssign>()
                                .eq("BATCH_ID", batchId));*//*
            }
        } catch (Exception e) {
            if (step != taskProjectBatch.getStep()) {
                taskProjectBatch.setStep(step);
                this.baseMapper.updateById(taskProjectBatch);
            }
            throw e;
        }*/
        /*List<MedicalFormalBehavior> behaviorList = this.medicalFormalBehaviorMapper.selectList(
                new QueryWrapper<MedicalFormalBehavior>()
                        .eq("BATCH_ID", batchId));
        if (behaviorList.size() > 0) {
            // 刪除不合规模型关联
            this.medicalFormalCaseBehaviorMapper.delete(new QueryWrapper<MedicalFormalCaseBehavior>().
                    in("BEHAVIOR_ID", behaviorList.stream().map(MedicalFormalBehavior::getBatchId).collect(Collectors.toList())));
            // 刪除批次不合规
            this.medicalFormalBehaviorMapper.delete(new QueryWrapper<MedicalFormalBehavior>()
                    .eq("BATCH_ID", batchId));
        }*/
        // 删除步骤运行状态
        this.taskBatchStepItemMapper.delete(new QueryWrapper<TaskBatchStepItem>()
                .eq("BATCH_ID", batchId));
        // 删除规则关联
        this.taskBatchBreakRuleMapper.delete(
                new QueryWrapper<TaskBatchBreakRule>()
                        .eq("BATCH_ID", batchId));
        // 删除模型运行状态
        this.taskBatchBreakRuleDelMapper.delete(
                new QueryWrapper<TaskBatchBreakRuleDel>()
                        .eq("BATCH_ID", batchId));
        // 批次主体
        this.baseMapper.deleteById(batchId);
        // 删除备份
        this.removeHis(batchId);
        return taskProjectBatch;
    }

    @Override
    public IPage<TaskProjectBatchVO> pageVO(Page<TaskProjectBatchVO> page, QueryWrapper<TaskProjectBatch> queryWrapper) {
        return this.baseMapper.selectPageVO(page, queryWrapper);
    }

    @Override
    public List<TaskBatchStepItemVO> selectTopBatchItems(int topNum, String dataSource) {
        return this.baseMapper.selectTopBatchItems(topNum, dataSource);
    }

    @Override
    public TaskBatchExecInfo queryExecInfoById(String batchId){
        Map<String, Object> timeMap = this.baseMapper.queryExecTimeById(batchId);
        Map<String, Object> numMap = this.baseMapper.queryExecNumById(batchId);
        if(timeMap == null){
            timeMap = new HashMap<>();
        }
        if(numMap == null){
            numMap = new HashMap<>();
        }
        TaskBatchExecInfo bean = new TaskBatchExecInfo();
        bean.setBatchId(batchId);
        bean.setStartTime((Date) timeMap.get("START_TIME"));
        bean.setEndTime((Date) timeMap.get("END_TIME"));
        bean.setRunNum(Integer.parseInt(numMap.get("RUN_NUM").toString()));
        bean.setEndNum(Integer.parseInt(numMap.get("END_NUM").toString()));
        return bean;
    }

    private void deleteHisFlowCaseNotIn(String batchId, List<String> busiList) {
        QueryWrapper<HisMedicalFormalCaseBusi> queryWrapper = new QueryWrapper<HisMedicalFormalCaseBusi>().eq("BATCH_ID",batchId);
        if(busiList.size() > 0 ) {
            queryWrapper.notIn("BUSI_ID", busiList);
        }
        List<HisMedicalFormalCaseBusi> caseBusiList = hisMedicalFormalCaseBusiMapper.selectList(queryWrapper);
        if(caseBusiList.size() > 0){
            List<String> caseIdList = caseBusiList.stream().map(HisMedicalFormalCaseBusi::getCaseId).collect(Collectors.toList());
            Set<String> busiIdList = caseBusiList.stream().map(HisMedicalFormalCaseBusi::getBusiId).collect(Collectors.toSet());
            hisMedicalFormalCaseBusiMapper.delete(new QueryWrapper<HisMedicalFormalCaseBusi>().eq("BATCH_ID",batchId).in("BUSI_ID", busiIdList));
            hisMedicalFormalBusiMapper.delete(new QueryWrapper<HisMedicalFormalBusi>().eq("BATCH_ID",batchId).in("BUSI_ID", busiIdList));
            hisFormalFlowRuleGradeMapper.delete(new QueryWrapper<HisFormalFlowRuleGrade>().eq("BATCH_ID",batchId).in("CASE_ID", caseIdList));
            hisMedicalFormalFlowRuleMapper.delete(new QueryWrapper<HisMedicalFormalFlowRule>().eq("BATCH_ID",batchId).in("CASE_ID", caseIdList));
            hisMedicalFormalFlowMapper.delete(new QueryWrapper<HisMedicalFormalFlow>().eq("BATCH_ID",batchId).in("CASE_ID", caseIdList));
            hisMedicalFormalCaseMapper.delete(new QueryWrapper<HisMedicalFormalCase>().eq("BATCH_ID",batchId).in("CASE_ID", caseIdList));
        }

    }

    @Override
    public void reBackHis(String batchId){
        List<TaskBatchBreakRule> busiRules = this.taskBatchBreakRuleMapper.selectList(
                new QueryWrapper<TaskBatchBreakRule>()
                        .eq("BATCH_ID", batchId)
                        .eq("RULE_TYPE", MedicalConstant.RULE_TYPE_CASE)
        );
        this.removeHis(batchId);
        if(busiRules.size() > 0){
            List<String> busiIdList = busiRules.stream().map(TaskBatchBreakRule::getRuleId).collect(Collectors.toList());
            this.backFlowCase(batchId, busiIdList);
        }
        List<TaskBatchBreakRule> caseRules = this.taskBatchBreakRuleMapper.selectList(
                new QueryWrapper<TaskBatchBreakRule>()
                        .eq("BATCH_ID", batchId)
                        .eq("RULE_TYPE", MedicalConstant.RULE_TYPE_NEWCASE)
        );
        if(caseRules.size() > 0){
            List<String> caseIdList = caseRules.stream().map(TaskBatchBreakRule::getRuleId).collect(Collectors.toList());
            this.reBackHis(batchId, caseIdList);
        }
    }


    @Override
    public void reBackHis(String batchId, List<String> caseIdList) {
        List<MedicalFormalCase> list = this.medicalFormalCaseMapper.selectList(new QueryWrapper<MedicalFormalCase>()
                .in("CASE_ID", caseIdList)
                .select("CASE_ID")
        );

        if(list.size() < caseIdList.size()){
            caseIdList = list.stream().map(MedicalFormalCase::getCaseId).collect(Collectors.toList());
        }

        this.removeHis(batchId, caseIdList);
        this.backFlowCaseByIds(batchId, caseIdList);
    }

    @Override
    public List<TaskProjectBatch> queryBatchByProjectOrDs(String[] dsArray, String[] pjArray) {
        return this.baseMapper.queryBatchByProjectOrDs(dsArray, pjArray);
    }

    private void removeHis(String batchId, List<String> caseIdList) {
        hisFormalFlowRuleGradeMapper.delete(new QueryWrapper<HisFormalFlowRuleGrade>()
                .eq("BATCH_ID", batchId).in("CASE_ID", caseIdList));
        hisMedicalFormalFlowRuleMapper.delete(new QueryWrapper<HisMedicalFormalFlowRule>()
                .eq("BATCH_ID", batchId).in("CASE_ID", caseIdList));
        hisMedicalFormalFlowMapper.delete(new QueryWrapper<HisMedicalFormalFlow>()
                .eq("BATCH_ID", batchId).in("CASE_ID", caseIdList));
        hisMedicalFormalCaseMapper.delete(new QueryWrapper<HisMedicalFormalCase>()
                .eq("BATCH_ID", batchId).in("CASE_ID", caseIdList));

    }

    private void copyHisFlowCase(String toBatchId, String fromBatchId, List<String> busiIdList) {
        String[] busiIds = busiIdList.toArray(new String[0]);
        copyMapper.copyHisMedicalFormalCaseByBusiIds(toBatchId,fromBatchId,busiIds);
        copyMapper.copyHisMedicalFormalFlowByBusiIds(toBatchId,fromBatchId,busiIds);
        copyMapper.copyHisMedicalFormalFlowRuleByBusiIds(toBatchId,fromBatchId,busiIds);
        copyMapper.copyHisMedicalFormalFlowRuleGradeByBusiIds(toBatchId,fromBatchId,busiIds);
        copyMapper.copyHisMedicalFormalFlowBusiByBusiIds(toBatchId,fromBatchId,busiIds);
        copyMapper.copyHisMedicalFormalFlowCaseBusiByBusiIds(toBatchId,fromBatchId,busiIds);
    }

    private void backFlowCase(String batchId, List<String> busiIdList) {
        String[] busiIds = busiIdList.toArray(new String[0]);
        backMapper.backMedicalFormalCaseByBusiIds(batchId,busiIds);
        backMapper.backMedicalFormalFlowByBusiIds(batchId,busiIds);
        backMapper.backMedicalFormalFlowRuleByBusiIds(batchId,busiIds);
        backMapper.backMedicalFormalFlowRuleGradeByBusiIds(batchId,busiIds);
        backMapper.backMedicalFormalFlowBusiByBusiIds(batchId,busiIds);
        backMapper.backMedicalFormalFlowCaseBusiByBusiIds(batchId,busiIds);
    }

    private void backFlowCaseByIds(String batchId, List<String> caseIdList) {

        for(String caseId: caseIdList){
            backMapper.backMedicalFormalCaseByCaseid(batchId,caseId);
            backMapper.backMedicalFormalFlowByCaseid(batchId,caseId);
            backMapper.backMedicalFormalFlowRuleByCaseid(batchId,caseId);
            backMapper.backMedicalFormalFlowRuleGradeByCaseid(batchId,caseId);
        }

    }
    private void backHis(String batchId) {
        backMapper.backMedicalFormalCase(batchId);
        backMapper.backMedicalFormalFlow(batchId);
        backMapper.backMedicalFormalFlowRule(batchId);
        backMapper.backMedicalFormalFlowRuleGrade(batchId);
        backMapper.backMedicalFormalFlowBusi(batchId);
        backMapper.backMedicalFormalFlowCaseBusi(batchId);
//        backMapper.backTaskBatchBreakRule(batchId);
    }

    private void removeHis(String batchId) {

        hisMedicalFormalBusiMapper.delete(new QueryWrapper<HisMedicalFormalBusi>().eq("BATCH_ID", batchId));
        hisMedicalFormalCaseBusiMapper.delete(new QueryWrapper<HisMedicalFormalCaseBusi>().eq("BATCH_ID", batchId));
        hisFormalFlowRuleGradeMapper.delete(new QueryWrapper<HisFormalFlowRuleGrade>().eq("BATCH_ID", batchId));
        hisMedicalFormalFlowRuleMapper.delete(new QueryWrapper<HisMedicalFormalFlowRule>().eq("BATCH_ID", batchId));
        hisMedicalFormalFlowMapper.delete(new QueryWrapper<HisMedicalFormalFlow>().eq("BATCH_ID", batchId));
        hisMedicalFormalCaseMapper.delete(new QueryWrapper<HisMedicalFormalCase>().eq("BATCH_ID", batchId));

    }
}
