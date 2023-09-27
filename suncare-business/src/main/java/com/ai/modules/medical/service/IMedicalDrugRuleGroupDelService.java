package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalDrugRuleGroup;
import com.ai.modules.medical.entity.MedicalDrugRuleGroupDel;
import com.ai.modules.medical.vo.MedicalDrugRuleGroupDelVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @Description: 医疗字典分组子项
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
public interface IMedicalDrugRuleGroupDelService extends IService<MedicalDrugRuleGroupDel> {

    Map<String, List<MedicalDrugRuleGroupDelVO>> getMapByKinds(String[] kinds);

    IPage<MedicalDrugRuleGroupDelVO> list(Page<MedicalDrugRuleGroupDel> page, MedicalDrugRuleGroupDel medicalDrugRuleGroupDel, MedicalDrugRuleGroup medicalDrugRuleGroup);
}
