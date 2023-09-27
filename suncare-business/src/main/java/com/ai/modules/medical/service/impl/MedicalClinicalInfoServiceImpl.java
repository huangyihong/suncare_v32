package com.ai.modules.medical.service.impl;

import com.ai.modules.medical.entity.MedicalClinicalInfo;
import com.ai.modules.medical.mapper.MedicalClinicalInfoMapper;
import com.ai.modules.medical.service.IMedicalClinicalInfoService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 临床路径资料信息
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
@Service
public class MedicalClinicalInfoServiceImpl extends ServiceImpl<MedicalClinicalInfoMapper, MedicalClinicalInfo> implements IMedicalClinicalInfoService {

    @Override
    public MedicalClinicalInfo getByCode(String code) {
        return this.baseMapper.getByCode(code);
    }
}
