package com.ai.modules.medical.service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.medical.vo.MedicalChargeRuleConfigIO;
import com.ai.modules.medical.vo.MedicalDrugRuleConfigIO;
import com.ai.modules.medical.vo.MedicalDruguseRuleConfigIO;
import com.ai.modules.medical.vo.MedicalRuleConfigVO;
import com.ai.modules.medical.vo.MedicalTreatRuleConfigIO;
import com.ai.modules.medical.vo.QueryMedicalRuleConfigVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 通用规则配置
 * @Author: jeecg-boot
 * @Date:   2020-12-14
 * @Version: V1.0
 */
public interface IMedicalRuleConfigService extends IService<MedicalRuleConfig> {


    void updateById(MedicalRuleConfig medicalRuleConfig, List<MedicalRuleConditionSet> conditionSets);

    void removeByRuleId(String id);

    void removeByRuleIds(List<String> asList);

    List<MedicalChargeRuleConfigIO> listChargeIO(QueryWrapper<MedicalRuleConfig> queryWrapper);
    List<MedicalTreatRuleConfigIO> listTreatIO(QueryWrapper<MedicalRuleConfig> queryWrapper);

    List<MedicalDrugRuleConfigIO> listDrugIO(QueryWrapper<MedicalRuleConfig> queryWrapper);

    List<MedicalDruguseRuleConfigIO> listDruguseIO(QueryWrapper<MedicalRuleConfig> queryWrapper);

    void saveBatch(MedicalRuleConfigVO medicalRuleConfig);

    void saveVO(MedicalRuleConfigVO medicalRuleConfig);

    List<QueryMedicalRuleConfigVO> queryMedicalRuleConfig(String ruleType, String ruleLimit);

    List<QueryMedicalRuleConfigVO> referDiagRuleConfig();

    void exportReferDiagRule(OutputStream os) throws Exception;

    //根据actionId同步修改actionName
    void updateActionNameByActionId(String actionId, String actionName);
}
