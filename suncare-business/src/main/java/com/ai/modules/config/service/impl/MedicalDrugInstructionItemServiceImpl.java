package com.ai.modules.config.service.impl;

import com.ai.modules.config.entity.MedicalDrugInstructionItem;
import com.ai.modules.config.mapper.MedicalDrugInstructionItemMapper;
import com.ai.modules.config.service.IMedicalDrugInstructionItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Description: 药品说明书子项
 * @Author: jeecg-boot
 * @Date:   2020-11-03
 * @Version: V1.0
 */
@Service
public class MedicalDrugInstructionItemServiceImpl extends ServiceImpl<MedicalDrugInstructionItemMapper, MedicalDrugInstructionItem> implements IMedicalDrugInstructionItemService {

    @Override
    public void updateOrderByItemIds(String itemIds) {
        if(StringUtils.isNotBlank(itemIds)){
            String[] idArray = itemIds.split(",");
            for(int i = 0, len = idArray.length; i < len; i++){
                if(StringUtils.isNotBlank(idArray[i])) {
                    MedicalDrugInstructionItem item = this.getById(idArray[i]);
                    if(item!=null) {
                        item.setIsOrder((long)i);
                        this.updateById(item);
                    }
                }

            }
        }
    }
}
