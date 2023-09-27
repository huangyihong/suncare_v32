package com.ai.modules.medical.service.impl;

import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.mapper.MedicalRuleConditionSetMapper;
import com.ai.modules.medical.service.IMedicalRuleConditionSetService;
import com.ai.modules.medical.vo.MedicalRuleConditionSetVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 通用规则条件集
 * @Author: jeecg-boot
 * @Date:   2020-12-14
 * @Version: V1.0
 */
@Service
public class MedicalRuleConditionSetServiceImpl extends ServiceImpl<MedicalRuleConditionSetMapper, MedicalRuleConditionSet> implements IMedicalRuleConditionSetService {

    @Override
    public List<MedicalRuleConditionSetVO> listVOJoinDruguse(QueryWrapper<MedicalDruguse> queryWrapper) {
        return this.baseMapper.listVOJoinDruguse(queryWrapper);
    }
}
