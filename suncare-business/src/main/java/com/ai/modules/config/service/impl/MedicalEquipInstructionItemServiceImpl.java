package com.ai.modules.config.service.impl;

import com.ai.modules.config.entity.MedicalEquipInstructionItem;
import com.ai.modules.config.mapper.MedicalEquipInstructionItemMapper;
import com.ai.modules.config.service.IMedicalEquipInstructionItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Description: 医疗器械说明书子项
 * @Author: jeecg-boot
 * @Date:   2020-11-05
 * @Version: V1.0
 */
@Service
public class MedicalEquipInstructionItemServiceImpl extends ServiceImpl<MedicalEquipInstructionItemMapper, MedicalEquipInstructionItem> implements IMedicalEquipInstructionItemService {

    @Override
    public void updateOrderByItemIds(String itemIds) {
        if(StringUtils.isNotBlank(itemIds)){
            String[] idArray = itemIds.split(",");
            for(int i = 0, len = idArray.length; i < len; i++){
                if(StringUtils.isNotBlank(idArray[i])) {
                    MedicalEquipInstructionItem item = this.getById(idArray[i]);
                    if(item!=null) {
                        item.setIsOrder((long)i);
                        this.updateById(item);
                    }
                }

            }
        }
    }
}
