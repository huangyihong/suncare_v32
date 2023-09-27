package com.ai.modules.his.service.impl;

import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.his.entity.HisMedicalFormalCaseBusi;
import com.ai.modules.his.entity.HisMedicalFormalFlow;
import com.ai.modules.his.entity.HisMedicalFormalFlowRule;
import com.ai.modules.his.mapper.HisMedicalFormalCaseBusiMapper;
import com.ai.modules.his.mapper.HisMedicalFormalCaseMapper;
import com.ai.modules.his.mapper.HisMedicalFormalFlowMapper;
import com.ai.modules.his.mapper.HisMedicalFormalFlowRuleMapper;
import com.ai.modules.his.service.IHisMedicalFormalCaseService;
import com.ai.modules.probe.entity.MedicalFlowTemplRule;
import com.ai.modules.probe.mapper.MedicalFlowTemplRuleMapper;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 风控模型正式备份
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Service
public class HisMedicalFormalCaseServiceImpl extends ServiceImpl<HisMedicalFormalCaseMapper, HisMedicalFormalCase> implements IHisMedicalFormalCaseService {
    @Autowired
    HisMedicalFormalFlowMapper hisMedicalFormalFlowMapper;

    @Autowired
    HisMedicalFormalFlowRuleMapper hisMedicalFormalFlowRuleMapper;

    @Autowired
    HisMedicalFormalCaseBusiMapper hisMedicalFormalCaseBusiMapper;

    @Autowired
    private MedicalFlowTemplRuleMapper medicalFlowTemplRuleMapper;

    @Override
    public JSONObject getFormalCaseById(String id, String batchId) {
        HisMedicalFormalCase medicalFormalCase = this.baseMapper.selectOne(
                new QueryWrapper<HisMedicalFormalCase>()
                        .eq("BATCH_ID",batchId)
                        .eq("CASE_ID",id));
        return getFormalCaseJson(medicalFormalCase, batchId);
    }


    @Override
    public List<HisMedicalFormalCase> queryByBusiId(String batchId, String busiId) {
        String selectCaseSql = "SELECT DISTINCT CASE_ID FROM HIS_MEDICAL_FORMAL_CASE_BUSI WHERE BATCH_ID = '"
                + batchId + "' and BUSI_ID = '" +  busiId + "'";
        List<HisMedicalFormalCase> list = this.baseMapper.selectList(
                new QueryWrapper<HisMedicalFormalCase>().eq("BATCH_ID",batchId)
                        .inSql("CASE_ID",selectCaseSql));
        return list;
    }

    @Override
    public HisMedicalFormalCase queryByCaseId(String batchId, String caseId) {
        return this.baseMapper.selectOne(new QueryWrapper<HisMedicalFormalCase>()
                        .eq("BATCH_ID",batchId)
                        .eq("CASE_ID",caseId));
    }

    @Override
    public JSONObject getFormalCaseByVersion(String caseId, Float version) {

        List<HisMedicalFormalCase> list = this.baseMapper.selectList(new QueryWrapper<HisMedicalFormalCase>()
                .eq("CASE_ID", caseId).eq("CASE_VERSION", version));
        if(list.size() == 0){
            return null;
        }
        HisMedicalFormalCase medicalFormalCase = list.get(0);
        return getFormalCaseJson(medicalFormalCase, medicalFormalCase.getBatchId());
    }

    private JSONObject getFormalCaseJson(HisMedicalFormalCase medicalFormalCase, String batchId){
        String id = medicalFormalCase.getCaseId();
        List<HisMedicalFormalFlowRule> ruleList = hisMedicalFormalFlowRuleMapper.selectList(
                new QueryWrapper<HisMedicalFormalFlowRule>()
                        .eq("BATCH_ID",batchId)
                        .eq("CASE_ID", id));
        // 虚拟节点
        List<HisMedicalFormalFlow> virtualFlowList = this.hisMedicalFormalFlowMapper.selectList(
                new QueryWrapper<HisMedicalFormalFlow>()
                        .eq("BATCH_ID",batchId)
                        .eq("CASE_ID", id)
                        .like("NODE_TYPE", "_v"));
        if(virtualFlowList.size() > 0){
            Map<String, HisMedicalFormalFlow> idFlowMap = new HashMap<>();
            virtualFlowList.forEach(r -> idFlowMap.put(r.getParamCode(), r));
            List<MedicalFlowTemplRule> templRuleList = medicalFlowTemplRuleMapper.selectList(new QueryWrapper<MedicalFlowTemplRule>().in("NODE_ID", idFlowMap.keySet()));
            ruleList.addAll(templRuleList.stream().map(r -> {
                HisMedicalFormalFlowRule flowRule = new HisMedicalFormalFlowRule();
                BeanUtils.copyProperties(r, flowRule);
                HisMedicalFormalFlow formalFlow = idFlowMap.get(r.getNodeId());
                flowRule.setCaseId(id);
                flowRule.setNodeId(formalFlow.getNodeId());
                flowRule.setNodeCode(formalFlow.getNodeCode());
                return flowRule;
            }).collect(Collectors.toList()));

        }
        JSONObject jsonData = (JSONObject) JSONObject.toJSON(medicalFormalCase);
        jsonData.put("rules", ruleList);
        return jsonData;
    }
}
