package com.ai.modules.medical.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.medical.vo.MedicalChargeRuleConfigIO;
import com.ai.modules.medical.vo.MedicalDrugRuleConfigIO;
import com.ai.modules.medical.vo.MedicalDruguseRuleConfigIO;
import com.ai.modules.medical.vo.MedicalTreatRuleConfigIO;
import com.ai.modules.medical.vo.QueryMedicalRuleConfigVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;

/**
 * @Description: 通用规则配置
 * @Author: jeecg-boot
 * @Date:   2020-12-14
 * @Version: V1.0
 */
public interface MedicalRuleConfigMapper extends BaseMapper<MedicalRuleConfig> {

    List<MedicalChargeRuleConfigIO> listChargeIO(@Param(Constants.WRAPPER) QueryWrapper<MedicalRuleConfig> queryWrapper);
    List<MedicalTreatRuleConfigIO> listTreatIO(@Param(Constants.WRAPPER) QueryWrapper<MedicalRuleConfig> queryWrapper);

    List<MedicalDrugRuleConfigIO> listDrugIO(@Param(Constants.WRAPPER) QueryWrapper<MedicalRuleConfig> queryWrapper);

    List<MedicalDruguseRuleConfigIO> listDruguseIO(@Param(Constants.WRAPPER) QueryWrapper<MedicalRuleConfig> queryWrapper);
    
    List<QueryMedicalRuleConfigVO> queryMedicalRuleConfig(@Param("ruleType") String ruleType, @Param("ruleLimit") String ruleLimit);
    
    List<QueryMedicalRuleConfigVO> referDiagRuleConfig();
}
