package com.ai.modules.medical.service.impl;

import com.ai.modules.medical.entity.MedicalDrugRuleGroup;
import com.ai.modules.medical.entity.MedicalDrugRuleGroupDel;
import com.ai.modules.medical.mapper.MedicalDrugRuleGroupDelMapper;
import com.ai.modules.medical.service.IMedicalDrugRuleGroupDelService;
import com.ai.modules.medical.vo.MedicalDrugRuleGroupDelVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 医疗字典分组子项
 * @Author: jeecg-boot
 * @Date:   2019-12-30
 * @Version: V1.0
 */
@Service
public class MedicalDrugRuleGroupDelServiceImpl extends ServiceImpl<MedicalDrugRuleGroupDelMapper, MedicalDrugRuleGroupDel> implements IMedicalDrugRuleGroupDelService {

    @Override
    public Map<String, List<MedicalDrugRuleGroupDelVO>> getMapByKinds(String[] kinds) {
        Map<String, List<MedicalDrugRuleGroupDelVO>> map = new HashMap<>();
        List<MedicalDrugRuleGroupDelVO> list = this.baseMapper.getMapByKinds(kinds);
        String kind = "";
        List<MedicalDrugRuleGroupDelVO> tempList = new ArrayList<>();
        for (MedicalDrugRuleGroupDelVO dict : list) {
            if (kind.equals(dict.getKind())) {
                tempList.add(dict);
            } else {
                kind = dict.getKind();
                tempList = new ArrayList<>();
                tempList.add(dict);
                map.put(kind, tempList);
            }

        }
        return map;
    }

    @Override
    public IPage<MedicalDrugRuleGroupDelVO> list(Page<MedicalDrugRuleGroupDel> page, MedicalDrugRuleGroupDel medicalDrugRuleGroupDel, MedicalDrugRuleGroup medicalDrugRuleGroup) {
        return this.baseMapper.listBySelfAndGroup(page, medicalDrugRuleGroupDel, medicalDrugRuleGroup);
    }

}
