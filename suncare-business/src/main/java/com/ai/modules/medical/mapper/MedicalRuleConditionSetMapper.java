package com.ai.modules.medical.mapper;

import java.util.List;

import com.ai.modules.medical.entity.MedicalDruguse;
import com.ai.modules.medical.vo.MedicalRuleConditionSetVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 通用规则条件集
 * @Author: jeecg-boot
 * @Date:   2020-12-14
 * @Version: V1.0
 */
public interface MedicalRuleConditionSetMapper extends BaseMapper<MedicalRuleConditionSet> {

    List<MedicalRuleConditionSetVO> listVOJoinDruguse(@Param(Constants.WRAPPER)  QueryWrapper<MedicalDruguse> queryWrapper);
}
