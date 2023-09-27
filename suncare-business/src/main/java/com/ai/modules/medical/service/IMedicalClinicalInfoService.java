package com.ai.modules.medical.service;

import com.ai.modules.medical.entity.MedicalClinicalInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 临床路径资料信息
 * @Author: jeecg-boot
 * @Date:   2020-03-09
 * @Version: V1.0
 */
public interface IMedicalClinicalInfoService extends IService<MedicalClinicalInfo> {
    MedicalClinicalInfo getByCode(String code);
}
