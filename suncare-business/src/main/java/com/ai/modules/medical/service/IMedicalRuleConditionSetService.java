package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.vo.MedicalRuleConditionSetVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 通用规则条件集
 * @Author: jeecg-boot
 * @Date:   2020-12-14
 * @Version: V1.0
 */
public interface IMedicalRuleConditionSetService extends IService<MedicalRuleConditionSet> {

    List<MedicalRuleConditionSetVO> listVOJoinDruguse(QueryWrapper<MedicalDruguse> queryWrapper);
}
