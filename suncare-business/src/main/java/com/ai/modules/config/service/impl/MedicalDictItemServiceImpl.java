package com.ai.modules.config.service.impl;

import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.MedicalDictItem;
import com.ai.modules.config.mapper.MedicalDictItemMapper;
import com.ai.modules.config.service.IMedicalDictItemService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @Description: 医疗字典子项
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
@Service
public class MedicalDictItemServiceImpl extends ServiceImpl<MedicalDictItemMapper, MedicalDictItem> implements IMedicalDictItemService {

	@Override
	public void updateOrderByItemIds(String itemIds) {
		if(StringUtils.isNotBlank(itemIds)){
			String[] idArray = itemIds.split(",");
			for(int i = 0, len = idArray.length; i < len; i++){
				if(StringUtils.isNotBlank(idArray[i])) {
					MedicalDictItem dictItem = this.getById(idArray[i]);
					if(dictItem!=null) {
						dictItem.setIsOrder((long)i);
						this.updateById(dictItem);
					}
				}
				
            }
		}
	}

}
