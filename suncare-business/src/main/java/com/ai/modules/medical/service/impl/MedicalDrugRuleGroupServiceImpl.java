package com.ai.modules.medical.service.impl;

import com.ai.common.utils.IdUtils;
import com.ai.modules.medical.entity.MedicalDrugRuleGroup;
import com.ai.modules.medical.entity.MedicalDrugRuleGroupDel;
import com.ai.modules.medical.mapper.MedicalDrugRuleGroupDelMapper;
import com.ai.modules.medical.mapper.MedicalDrugRuleGroupMapper;
import com.ai.modules.medical.service.IMedicalDrugRuleGroupService;
import com.ai.modules.medical.vo.MedicalDrugRuleGroupDict;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import net.sf.saxon.expr.Component;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 药品合规规则分组
 * @Author: jeecg-boot
 * @Date: 2019-12-19
 * @Version: V1.0
 */
@Service
public class MedicalDrugRuleGroupServiceImpl extends ServiceImpl<MedicalDrugRuleGroupMapper, MedicalDrugRuleGroup> implements IMedicalDrugRuleGroupService {

    @Autowired
    MedicalDrugRuleGroupDelMapper medicalDrugRuleGroupDelMapper;

    @Override
    public Map<String, List<MedicalDrugRuleGroupDict>> getGroupDictMapByKinds(String[] kinds) {
        Map<String, List<MedicalDrugRuleGroupDict>> map = new HashMap<>();
        List<MedicalDrugRuleGroupDict> list = this.baseMapper.getGroupDictMapByKinds(kinds);
        String kind = "";
        List<MedicalDrugRuleGroupDict> tempList = new ArrayList<>();
        for (MedicalDrugRuleGroupDict dict : list) {
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
    @Transactional
    public void saveGroup(MedicalDrugRuleGroup medicalDrugRuleGroup, String codes, String names) {
        this.save(medicalDrugRuleGroup);
        String groupId = medicalDrugRuleGroup.getId();
        // 插入子项
        if(StringUtils.isNotBlank(codes)){
            String[] codeArray = codes.split(",");
            String[] nameArray = names.split(",");
            MedicalDrugRuleGroupDel groupDel = new MedicalDrugRuleGroupDel();
            groupDel.setGroupId(groupId);
            for(int i = 0, len = codeArray.length; i < len; i++){
                groupDel.setId(IdUtils.uuid());
                groupDel.setIsOrder((long)i);
                groupDel.setCode(codeArray[i]);
                groupDel.setValue(nameArray[i]);
                this.medicalDrugRuleGroupDelMapper.insert(groupDel);
            }
        }
    }

    @Override
    @Transactional
    public void updateGroup(MedicalDrugRuleGroup medicalDrugRuleGroup, String codes, String names) {
        String groupId = medicalDrugRuleGroup.getId();
        this.updateById(medicalDrugRuleGroup);
        // 删除子项
        this.medicalDrugRuleGroupDelMapper.delete(new QueryWrapper<MedicalDrugRuleGroupDel>()
                .eq("GROUP_ID", groupId));
        // 插入子项
        if(StringUtils.isNotBlank(codes)){
            String[] codeArray = codes.split(",");
            String[] nameArray = names.split(",");
            MedicalDrugRuleGroupDel groupDel = new MedicalDrugRuleGroupDel();
            groupDel.setGroupId(groupId);
            for(int i = 0, len = codeArray.length; i < len; i++){
                groupDel.setId(IdUtils.uuid());
                groupDel.setIsOrder((long)i);
                groupDel.setCode(codeArray[i]);
                groupDel.setValue(nameArray[i]);
                this.medicalDrugRuleGroupDelMapper.insert(groupDel);
            }
        }
    }

    @Override
    @Transactional
    public void deleteGroup(String id) {
        this.baseMapper.deleteById(id);
        this.medicalDrugRuleGroupDelMapper.delete(new QueryWrapper<MedicalDrugRuleGroupDel>()
                .eq("GROUP_ID", id));
    }

    @Override
    @Transactional
    public void deleteGroups(List<String> list) {
        this.removeByIds(list);
        this.medicalDrugRuleGroupDelMapper.delete(new QueryWrapper<MedicalDrugRuleGroupDel>()
                .in("GROUP_ID", list));
    }
}
