package com.ai.modules.probe.service;

import com.ai.modules.probe.entity.MedicalProbeCase;
import com.ai.modules.probe.entity.MedicalProbeFlowRule;
import com.ai.modules.probe.vo.MedicalProbeCaseVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.exception.JeecgBootException;

import java.util.List;

/**
 * @Description: 流程图
 * @Author: jeecg-boot
 * @Date:   2019-11-21
 * @Version: V1.0
 */
public interface IMedicalProbeCaseService extends IService<MedicalProbeCase> {

    void addProbeCase(MedicalProbeCase medicalProbeCase, List<MedicalProbeFlowRule> ruleList);
    void updateProbeCase(MedicalProbeCase medicalProbeCase, List<MedicalProbeFlowRule> ruleList) throws JeecgBootException;
    void removeProbeCaseByIds(List<String> ids);
    void removeProbeCaseById(String id);

    JSONObject getProbeCaseById(String id);

    List<JSONObject> getProbeCaseByIds(List<String> ids);

}
