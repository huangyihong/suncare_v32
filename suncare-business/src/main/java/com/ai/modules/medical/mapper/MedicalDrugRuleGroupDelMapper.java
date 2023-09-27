package com.ai.modules.medical.mapper;

import java.util.List;
import java.util.Map;

import com.ai.modules.medical.entity.MedicalDrugRuleGroup;
import com.ai.modules.medical.vo.MedicalDrugRuleGroupDelVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.ai.modules.medical.entity.MedicalDrugRuleGroupDel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 医疗字典分组子项
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
public interface MedicalDrugRuleGroupDelMapper extends BaseMapper<MedicalDrugRuleGroupDel> {

    List<MedicalDrugRuleGroupDelVO> getMapByKinds(String[] kinds);

    IPage<MedicalDrugRuleGroupDelVO> listBySelfAndGroup(Page<MedicalDrugRuleGroupDel> page, @Param("del") MedicalDrugRuleGroupDel medicalDrugRuleGroupDel, @Param("group") MedicalDrugRuleGroup medicalDrugRuleGroup);
}
