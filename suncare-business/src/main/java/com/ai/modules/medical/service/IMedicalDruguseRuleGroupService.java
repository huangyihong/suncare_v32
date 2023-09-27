package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.entity.MedicalDruguseRuleGroup;
import com.ai.modules.medical.vo.MedicalDruguseRuleGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 合理用药配置条件组
 * @Author: jeecg-boot
 * @Date:   2020-11-05
 * @Version: V1.0
 */
public interface IMedicalDruguseRuleGroupService extends IService<MedicalDruguseRuleGroup> {

    List<MedicalDruguseRuleGroupVO> listBlankVO(QueryWrapper<MedicalDruguseRuleGroup> ruleGroupQueryWrapper);
    List<MedicalDruguseRuleGroupVO> listVOJoinBean(QueryWrapper<MedicalDruguse> queryWrapper);
}
