package com.ai.modules.config.service.impl;

import org.springframework.stereotype.Service;

import com.ai.modules.config.entity.MedicalDiseaseGroupItem;
import com.ai.modules.config.mapper.MedicalDiseaseGroupItemMapper;
import com.ai.modules.config.service.IMedicalDiseaseGroupItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @Description: 疾病分组子项
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Service
public class MedicalDiseaseGroupItemServiceImpl extends ServiceImpl<MedicalDiseaseGroupItemMapper, MedicalDiseaseGroupItem> implements IMedicalDiseaseGroupItemService {

	@Override
	public void updateOrderByItemIds(String itemIds) {
		if(StringUtils.isNotBlank(itemIds)){
			String[] idArray = itemIds.split(",");
			for(int i = 0, len = idArray.length; i < len; i++){
				if(StringUtils.isNotBlank(idArray[i])) {
					MedicalDiseaseGroupItem item = this.getById(idArray[i]);
					if(item!=null) {
						item.setIsOrder((long)i);
						this.updateById(item);
					}
				}
				
            }
		}
	}
}
