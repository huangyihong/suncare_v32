package com.ai.modules.medical.service.impl;

import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.ai.modules.medical.mapper.MedicalClinicalRangeGroupMapper;
import com.ai.modules.medical.service.IMedicalClinicalRangeGroupService;
import com.ai.modules.medical.vo.MedicalClinicalRangeGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 临床路径范围组
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
@Service
public class MedicalClinicalRangeGroupServiceImpl extends ServiceImpl<MedicalClinicalRangeGroupMapper, MedicalClinicalRangeGroup> implements IMedicalClinicalRangeGroupService {

    @Override
    public List<MedicalClinicalRangeGroupVO> listDetail(QueryWrapper<MedicalClinicalRangeGroup> queryWrapper, String type) {
        return this.baseMapper.listDetail(queryWrapper,type);
    }
}
