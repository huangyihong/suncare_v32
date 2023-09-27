package com.ai.modules.medical.service.impl;

import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.entity.MedicalDruguseRuleGroup;
import com.ai.modules.medical.mapper.MedicalDruguseRuleGroupMapper;
import com.ai.modules.medical.service.IMedicalDruguseRuleGroupService;
import com.ai.modules.medical.vo.MedicalDruguseRuleGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 合理用药配置条件组
 * @Author: jeecg-boot
 * @Date:   2020-11-05
 * @Version: V1.0
 */
@Service
public class MedicalDruguseRuleGroupServiceImpl extends ServiceImpl<MedicalDruguseRuleGroupMapper, MedicalDruguseRuleGroup> implements IMedicalDruguseRuleGroupService {

    @Override
    public List<MedicalDruguseRuleGroupVO> listBlankVO(QueryWrapper<MedicalDruguseRuleGroup> ruleGroupQueryWrapper) {
        return this.baseMapper.listBlankVO(ruleGroupQueryWrapper) ;
    }

    @Override
    public List<MedicalDruguseRuleGroupVO> listVOJoinBean(QueryWrapper<MedicalDruguse> queryWrapper) {
        return this.baseMapper.listVOJoinBean(queryWrapper);
    }
}
