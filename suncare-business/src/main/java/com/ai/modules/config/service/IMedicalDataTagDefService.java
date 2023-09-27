package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalDataTagDef;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 标签管理
 * @Author: jeecg-boot
 * @Date:   2021-11-04
 * @Version: V1.0
 */
public interface IMedicalDataTagDefService extends IService<MedicalDataTagDef> {

	public void saveDataTag(MedicalDataTagDef medicalDataTagDef) throws Exception;
}
