package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalDictItem;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 医疗字典子项
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
public interface IMedicalDictItemService extends IService<MedicalDictItem> {

	void updateOrderByItemIds(String itemIds);

}
