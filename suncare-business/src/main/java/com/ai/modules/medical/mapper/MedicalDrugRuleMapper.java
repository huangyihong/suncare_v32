package com.ai.modules.medical.mapper;

import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.medical.vo.MedicalClinicalRangeGroupVO;
import com.ai.modules.medical.vo.MedicalDrugRuleVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * @Description: 药品合规规则
 * @Author: jeecg-boot
 * @Date:   2019-12-19
 * @Version: V1.0
 */
public interface MedicalDrugRuleMapper extends BaseMapper<MedicalDrugRule> {
    IPage<MedicalDrugRuleVO> selectPageVO(Page<MedicalDrugRuleVO> page, @Param(Constants.WRAPPER) Wrapper<MedicalDrugRule> wrapper
            , @Param("dataSource") String dataSource);

    List<MedicalDrugRuleVO> selectListVO(@Param(Constants.WRAPPER) Wrapper<MedicalDrugRule> wrapper, @Param("dataSource") String dataSource);
}
