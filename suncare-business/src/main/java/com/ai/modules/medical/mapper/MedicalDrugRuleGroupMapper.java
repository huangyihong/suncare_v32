package com.ai.modules.medical.mapper;

import java.util.List;

import com.ai.modules.medical.entity.MedicalDrugRuleGroup;
import com.ai.modules.medical.entity.vo.DrugRuleGroupDtlVO;
import com.ai.modules.medical.vo.MedicalDrugRuleGroupDict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 药品合规规则分组
 * @Author: jeecg-boot
 * @Date:   2019-12-19
 * @Version: V1.0
 */
public interface MedicalDrugRuleGroupMapper extends BaseMapper<MedicalDrugRuleGroup> {
    List<MedicalDrugRuleGroupDict> getGroupDictMapByKinds(String[] kinds);
    
    List<DrugRuleGroupDtlVO> findDrugRuleGroupDtlByKind(String kind);
}
