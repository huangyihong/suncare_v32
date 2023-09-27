package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalDiseaseGroupItem;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 疾病分组子项
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
public interface IMedicalDiseaseGroupItemService extends IService<MedicalDiseaseGroupItem> {

	void updateOrderByItemIds(String itemIds);
}
