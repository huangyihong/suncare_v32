package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalEquipInstructionItem;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 医疗器械说明书子项
 * @Author: jeecg-boot
 * @Date:   2020-11-05
 * @Version: V1.0
 */
public interface IMedicalEquipInstructionItemService extends IService<MedicalEquipInstructionItem> {

    void updateOrderByItemIds(String itemIds);
}
