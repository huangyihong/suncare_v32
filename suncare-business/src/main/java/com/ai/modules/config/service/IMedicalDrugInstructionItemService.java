package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalDrugInstructionItem;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 药品说明书子项
 * @Author: jeecg-boot
 * @Date:   2020-11-03
 * @Version: V1.0
 */
public interface IMedicalDrugInstructionItemService extends IService<MedicalDrugInstructionItem> {

    void updateOrderByItemIds(String itemIds);

}
