package com.ai.modules.probe.service.impl;

import com.ai.common.utils.IdUtils;
import com.ai.modules.probe.entity.MedicalFlowTemplRule;
import com.ai.modules.probe.entity.MedicalProbeCase;
import com.ai.modules.probe.entity.MedicalProbeFlow;
import com.ai.modules.probe.entity.MedicalProbeFlowRule;
import com.ai.modules.probe.mapper.MedicalFlowTemplRuleMapper;
import com.ai.modules.probe.mapper.MedicalProbeCaseMapper;
import com.ai.modules.probe.mapper.MedicalProbeFlowMapper;
import com.ai.modules.probe.mapper.MedicalProbeFlowRuleMapper;
import com.ai.modules.probe.service.IMedicalProbeCaseService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 流程图
 * @Author: jeecg-boot
 * @Date: 2019-11-21
 * @Version: V1.0
 */
@Service
public class MedicalProbeCaseServiceImpl extends ServiceImpl<MedicalProbeCaseMapper, MedicalProbeCase> implements IMedicalProbeCaseService {

    @Autowired
    private MedicalProbeFlowRuleMapper medicalProbeFlowRuleMapper;

    @Autowired
    private MedicalFlowTemplRuleMapper medicalFlowTemplRuleMapper;
    @Override
    @Transactional
    public void addProbeCase(MedicalProbeCase medicalProbeCase, List<MedicalProbeFlowRule> ruleList) {
        if (this.baseMapper.insert(medicalProbeCase) > 0) {
            String caseId = medicalProbeCase.getCaseId();
            for (MedicalProbeFlowRule rule : ruleList) {
//                rule.setRuleId(IdUtils.uuid());
                rule.setCaseId(caseId);
                medicalProbeFlowRuleMapper.insert(rule);
            }
        }
    }

    @Override
    @Transactional
    public void updateProbeCase(MedicalProbeCase medicalProbeCase, List<MedicalProbeFlowRule> ruleList) throws JeecgBootException {

        MedicalProbeCase probeCase = this.baseMapper.selectOne(
                new QueryWrapper<MedicalProbeCase>()
                        .eq("CASE_ID", medicalProbeCase.getCaseId())
                        .ne("CASE_STATUS", "submited"));
        if (probeCase != null) {
            probeCase.setCaseName(medicalProbeCase.getCaseName());
            probeCase.setCaseRemark(medicalProbeCase.getCaseRemark());
            probeCase.setFlowJson(medicalProbeCase.getFlowJson());
            probeCase.setCaseVersion(probeCase.getCaseVersion() + 0.01f);
            this.baseMapper.updateById(probeCase);
            String caseId = medicalProbeCase.getCaseId();
            this.removeRuleByCaseId(caseId);
            for (MedicalProbeFlowRule rule : ruleList) {
                rule.setRuleId(IdUtils.uuid());
                rule.setCaseId(caseId);
                medicalProbeFlowRuleMapper.insert(rule);
            }

        } else {
            throw new JeecgBootException("更新项不存在");
        }

    }

    @Override
    @Transactional
    public void removeProbeCaseByIds(List<String> ids) {
        // 筛选出状态为等待提交的项
        List<MedicalProbeCase> list = this.baseMapper.selectList(new QueryWrapper<MedicalProbeCase>().in("CASE_ID", ids).eq("CASE_STATUS", "wait").select("CASE_ID"));
        List<String> delIds = new ArrayList<>();
        for (MedicalProbeCase item : list) {
            delIds.add(item.getCaseId());
        }
        if (delIds.size() > 0) {
            this.baseMapper.deleteBatchIds(delIds);
            medicalProbeFlowRuleMapper.delete(new QueryWrapper<MedicalProbeFlowRule>().in("CASE_ID", delIds));
        }
    }

    @Override
    @Transactional
    public void removeProbeCaseById(String id) {
        this.baseMapper.deleteById(id);
        this.removeRuleByCaseId(id);

    }

    private int removeRuleByCaseId(String caseId) {
        return medicalProbeFlowRuleMapper.delete(new QueryWrapper<MedicalProbeFlowRule>().eq("CASE_ID", caseId));
    }

    @Override
    public JSONObject getProbeCaseById(String id) {
        MedicalProbeCase medicalProbeCase = this.baseMapper.selectById(id);
        List<MedicalProbeFlowRule> ruleList = medicalProbeFlowRuleMapper.selectList(new QueryWrapper<MedicalProbeFlowRule>().eq("CASE_ID", id));

        // 虚拟节点
        JSONObject jsonFlow = (JSONObject) JSONObject.parse(medicalProbeCase.getFlowJson());
        JSONArray nodeArray = jsonFlow.getJSONArray("nodeDataArray");
        Map<String, String> idCodeMap = new HashMap<>();
        for(int i = 0, len = nodeArray.size(); i < len; i++){
            JSONObject nodeObj = nodeArray.getJSONObject(i);
            if(nodeObj.getString("type").contains("_v")){
                idCodeMap.put(nodeObj.getString("param"), nodeObj.getString("key"));
            }
        }
        if(idCodeMap.size() > 0){
            List<MedicalFlowTemplRule> templRuleList = medicalFlowTemplRuleMapper.selectList(new QueryWrapper<MedicalFlowTemplRule>().in("NODE_ID", idCodeMap.keySet()));
            ruleList.addAll(templRuleList.stream().map(r -> {
                MedicalProbeFlowRule flowRule = new MedicalProbeFlowRule();
                BeanUtils.copyProperties(r, flowRule);
                flowRule.setCaseId(id);
                flowRule.setNodeCode(idCodeMap.get(r.getNodeId()));
                return flowRule;
            }).collect(Collectors.toList()));
        }
        JSONObject jsonData = (JSONObject) JSONObject.toJSON(medicalProbeCase);
        jsonData.put("rules", ruleList);
        return jsonData;
    }

    @Override
    public List<JSONObject> getProbeCaseByIds(List<String> ids) {
        List<JSONObject> list = new ArrayList<>();
        List<MedicalProbeCase> caseList = this.baseMapper.selectList(new QueryWrapper<MedicalProbeCase>().in("CASE_ID", ids).orderByAsc("CASE_ID"));
        List<MedicalProbeFlowRule> ruleList = medicalProbeFlowRuleMapper.selectList(new QueryWrapper<MedicalProbeFlowRule>().in("CASE_ID", ids).orderByAsc("CASE_ID"));
        // 虚拟节点
        Set<String> tempRuleSet = new HashSet<>();
        Map<String, Map<String, String>> caseTempIdNodeCodeMap = new HashMap<>();
        for (MedicalProbeCase caseItem : caseList) {
            JSONObject jsonFlow = (JSONObject) JSONObject.parse(caseItem.getFlowJson());
            JSONArray nodeArray = jsonFlow.getJSONArray("nodeDataArray");
            Map<String, String> idCodeMap = new HashMap<>();
            for(int i = 0, len = nodeArray.size(); i < len; i++){
                JSONObject nodeObj = nodeArray.getJSONObject(i);
                if(nodeObj.getString("type").contains("_v")){
                    idCodeMap.put(nodeObj.getString("param"), nodeObj.getString("key"));
                }
            }
            if(idCodeMap.size() > 0){
                tempRuleSet.addAll(idCodeMap.keySet());
                caseTempIdNodeCodeMap.put(caseItem.getCaseId(), idCodeMap);
            }
        }
        List<MedicalFlowTemplRule> templRuleList = new ArrayList<>();
        if(tempRuleSet.size() > 0){
            templRuleList = medicalFlowTemplRuleMapper.selectList(new QueryWrapper<MedicalFlowTemplRule>().in("NODE_ID", tempRuleSet));
        }

        int ruleIndex = 0;
        int ruleLen = ruleList.size();
        for (MedicalProbeCase caseItem : caseList) {
            String caseId = caseItem.getCaseId();
            // 容器放置
            List<MedicalProbeFlowRule> rules = new ArrayList<>();
            JSONObject jsonData = (JSONObject) JSONObject.toJSON(caseItem);
            jsonData.put("rules", rules);
            list.add(jsonData);
            // 规则归位
            for (; ruleIndex < ruleLen; ruleIndex++) {
                MedicalProbeFlowRule rule = ruleList.get(ruleIndex);
                if (caseId.equals(rule.getCaseId())) {
                    rules.add(rule);
                } else {
                    break;
                }
            }
            Map<String, String> idCodeMap = caseTempIdNodeCodeMap.get(caseId);
            if(idCodeMap != null && idCodeMap.size() > 0){
                for(MedicalFlowTemplRule templRule: templRuleList){
                    String code = idCodeMap.get(templRule.getNodeId());
                    if(code != null){
                        MedicalProbeFlowRule flowRule = new MedicalProbeFlowRule();
                        BeanUtils.copyProperties(templRule, flowRule);
                        flowRule.setCaseId(caseId);
                        flowRule.setNodeCode(code);
                        rules.add(flowRule);
                    }
                }
            }
        }
        return list;
    }

}
