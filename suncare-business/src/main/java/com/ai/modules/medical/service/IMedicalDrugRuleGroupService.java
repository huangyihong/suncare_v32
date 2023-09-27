package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalDrugRuleGroup;
import com.ai.modules.medical.entity.MedicalDrugRuleGroupDel;
import com.ai.modules.medical.vo.MedicalDrugRuleGroupDict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @Description: 药品合规规则分组
 * @Author: jeecg-boot
 * @Date:   2019-12-19
 * @Version: V1.0
 */
public interface IMedicalDrugRuleGroupService extends IService<MedicalDrugRuleGroup> {

    Map<String, List<MedicalDrugRuleGroupDict>> getGroupDictMapByKinds(String[] split);

    void saveGroup(MedicalDrugRuleGroup medicalDrugRuleGroup, String codes, String names);

    void updateGroup(MedicalDrugRuleGroup medicalDrugRuleGroup, String codes, String names);

    void deleteGroup(String id);

    void deleteGroups(List<String> list);
}
