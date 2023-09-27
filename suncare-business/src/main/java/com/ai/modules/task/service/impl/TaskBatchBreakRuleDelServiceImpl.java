package com.ai.modules.task.service.impl;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ExportUtils;
import com.ai.common.utils.IdUtils;
import com.ai.modules.formal.entity.MedicalFormalBusi;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.formal.mapper.MedicalFormalBusiMapper;
import com.ai.modules.formal.mapper.MedicalFormalCaseBusiMapper;
import com.ai.modules.formal.mapper.MedicalFormalCaseMapper;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.mapper.TaskBatchBreakRuleDelMapper;
import com.ai.modules.task.mapper.TaskBatchBreakRuleMapper;
import com.ai.modules.task.service.ITaskBatchBreakRuleDelService;
import com.ai.modules.task.vo.TaskBatchBreakRuleDelVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description: 违规模型详情
 * @Author: jeecg-boot
 * @Date:   2020-01-17
 * @Version: V1.0
 */
@Service
public class TaskBatchBreakRuleDelServiceImpl extends ServiceImpl<TaskBatchBreakRuleDelMapper, TaskBatchBreakRuleDel> implements ITaskBatchBreakRuleDelService {

    @Autowired
    TaskBatchBreakRuleMapper taskBatchBreakRuleMapper;

    @Autowired
    MedicalFormalBusiMapper medicalFormalBusiMapper;

    @Autowired
    MedicalFormalCaseMapper medicalFormalCaseMapper;

    @Autowired
    MedicalFormalCaseBusiMapper medicalFormalCaseBusiMapper;

    @Override
    public void save(String batchId, List<TaskBatchBreakRule> breakRuleList) throws Exception {
        /*int oldCount = this.baseMapper.selectCount(
                new QueryWrapper<TaskBatchBreakRuleDel>().eq("BATCH_ID",batchId));
        if(oldCount > 0){
            TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
            bean.setStatus(MedicalConstant.RUN_STATE_WAIT);
            this.baseMapper.update(bean, new QueryWrapper<TaskBatchBreakRuleDel>().eq("BATCH_ID",batchId));
            return;
        }*/
        // 删除旧数据
        this.baseMapper.delete(new QueryWrapper<TaskBatchBreakRuleDel>().eq("BATCH_ID",batchId));

        // 规则归类
        List<TaskBatchBreakRule> busiRules = new ArrayList<>();
        List<TaskBatchBreakRule> caseRules = new ArrayList<>();
        List<TaskBatchBreakRule> clinicalRules = new ArrayList<>();
        for(TaskBatchBreakRule rule: breakRuleList){
            if(MedicalConstant.RULE_TYPE_CASE.equals(rule.getRuleType())){
                busiRules.add(rule);
            } else if(MedicalConstant.RULE_TYPE_CLINICAL_NEW.equals(rule.getRuleType())){
                clinicalRules.add(rule);
            } else if(MedicalConstant.RULE_TYPE_NEWCASE.equals(rule.getRuleType())){
                caseRules.add(rule);
            }
        }

        List<TaskBatchBreakRuleDel> list = new ArrayList<>();
        // 模型业务组进度列表
        if(busiRules.size() > 0){
            String[] busiIds = busiRules.stream().map(TaskBatchBreakRule::getRuleId).toArray(String[]::new);
            List<TaskBatchBreakRuleDel> ruleDelList = this.baseMapper.selectByBusiIds(busiIds);
            Set<String> caseIdSet = new HashSet<>();
            for(TaskBatchBreakRuleDel bean: ruleDelList){
//            bean.setId(IdUtils.uuid());
                if(caseIdSet.contains(bean.getCaseId())) {
                    continue;
                }
                caseIdSet.add(bean.getCaseId());
                bean.setRuleType(MedicalConstant.RULE_TYPE_CASE);
                bean.setBatchId(batchId);
                bean.setStatus(MedicalConstant.RUN_STATE_WAIT);
                bean.setReviewStatus(MedicalConstant.REVIEW_STATE_UN_AUDIT);
            }
            list.addAll(ruleDelList);
        }

        // 模型进度列表
        for(TaskBatchBreakRule rule: caseRules){
            TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
            bean.setCaseId(rule.getRuleId());
            bean.setCaseName(rule.getRuleName());
            bean.setRuleType(MedicalConstant.RULE_TYPE_NEWCASE);
            bean.setBatchId(batchId);
            bean.setStatus(MedicalConstant.RUN_STATE_WAIT);
            list.add(bean);
        }

        // 临床路径进度列表
        for(TaskBatchBreakRule rule: clinicalRules){
            TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
            bean.setCaseId(rule.getRuleId());
            bean.setCaseName(rule.getRuleName());
            bean.setRuleType(MedicalConstant.RULE_TYPE_CLINICAL_NEW);
            bean.setBatchId(batchId);
            bean.setStatus(MedicalConstant.RUN_STATE_WAIT);
            list.add(bean);
        }
        // 插入数据库
        this.saveBatch(list);
    }

    @Override
    public void update(String batchId, String busiId, String caseId) {
        List<TaskBatchBreakRuleDel> list = this.baseMapper.selectList(
                new QueryWrapper<TaskBatchBreakRuleDel>()
                        .eq("BATCH_ID", batchId)
                        .eq("BUSI_ID", busiId)
                        .eq("CASE_ID", caseId)
        );
        if(list.size() > 0){
            TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
            bean.setStatus(MedicalConstant.RUN_STATE_WAIT);
        } else {
            this.save(batchId, busiId, caseId);
        }
    }

    @Override
    public void update(String actionId) {

    }


    @Override
    public void save(String batchId, String busiId, String caseId) {
        MedicalFormalBusi formalBusi = medicalFormalBusiMapper.selectById(busiId);
        MedicalFormalCase caseRule = medicalFormalCaseMapper.selectById(caseId);
        TaskBatchBreakRuleDel bean = new TaskBatchBreakRuleDel();
        bean.setBatchId(batchId);
        bean.setBusiId(busiId);
        bean.setBusiName(formalBusi.getBusiName());
        bean.setCaseId(caseId);
        bean.setCaseName(caseRule.getCaseName());
        bean.setStatus(MedicalConstant.RUN_STATE_WAIT);
        this.baseMapper.insert(bean);
    }


    private String[] titles = {"业务组名称","违规模型名称","违规记录数","违规总金额","状态","异常信息","执行开始时间","执行结束时间"};
    private String[] fields = {"busiName","caseName","recordNum","totalAcount","status","errorMsg","startTime","endTime"};

    private String[] caseTitles = {"违规模型名称","违规记录数","违规总金额","状态","异常信息","执行开始时间","执行结束时间"};
    private String[] caseFields = {"caseName","recordNum","totalAcount","status","errorMsg","startTime","endTime"};


    private String[] clinicalTitles = {"临床路径名称","违规记录数","违规总金额","状态","异常信息","执行开始时间","执行结束时间"};
    private String[] clinicalFields = {"caseName","recordNum","totalAcount","status","errorMsg","startTime","endTime"};

    @Override
    public void exportExcel(String ruleType,QueryWrapper<TaskBatchBreakRuleDel> queryWrapper, OutputStream os) throws Exception {
        // 获取数据
        List<TaskBatchBreakRuleDel> list = this.list(queryWrapper);
        // 创建文件输出流
        WritableWorkbook wwb = Workbook.createWorkbook(os);
        WritableSheet sheet = wwb.createSheet("sheet1", 0);
        // 导出
        if(MedicalConstant.RULE_TYPE_CASE.equals(ruleType)){
            ExportUtils.exportExl(list,TaskBatchBreakRuleDel.class,titles,fields,sheet, "违规模型详情");
        } else if(MedicalConstant.RULE_TYPE_NEWCASE.equals(ruleType)){
            ExportUtils.exportExl(list,TaskBatchBreakRuleDel.class,caseTitles,caseFields,sheet, "违规模型详情");
        } else if(MedicalConstant.RULE_TYPE_CLINICAL_NEW.equals(ruleType)){
            ExportUtils.exportExl(list,TaskBatchBreakRuleDel.class,clinicalTitles,clinicalFields,sheet, "违规临床路径详情");
        }
//        ExportUtils.exportExl(list,TaskBatchBreakRuleDel.class,fields,sheet);
        wwb.write();
        wwb.close();
    }

    @Override
    public IPage<TaskBatchBreakRuleDelVO> pageVo(Page<TaskBatchBreakRuleDel> page, QueryWrapper<TaskBatchBreakRuleDel> queryWrapper, String batchId) {
        return this.baseMapper.pageVo(page, queryWrapper, batchId);
    }

}
