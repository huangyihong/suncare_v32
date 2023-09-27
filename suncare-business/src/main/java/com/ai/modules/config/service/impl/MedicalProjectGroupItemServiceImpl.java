package com.ai.modules.config.service.impl;

import org.springframework.stereotype.Service;

import com.ai.modules.config.entity.MedicalProjectGroupItem;
import com.ai.modules.config.mapper.MedicalProjectGroupItemMapper;
import com.ai.modules.config.service.IMedicalProjectGroupItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @Description: 医疗服务项目分组子项
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
@Service
public class MedicalProjectGroupItemServiceImpl extends ServiceImpl<MedicalProjectGroupItemMapper, MedicalProjectGroupItem> implements IMedicalProjectGroupItemService {

	@Override
	public void updateOrderByItemIds(String itemIds) {
		if(StringUtils.isNotBlank(itemIds)){
			String[] idArray = itemIds.split(",");
			for(int i = 0, len = idArray.length; i < len; i++){
				if(StringUtils.isNotBlank(idArray[i])) {
					MedicalProjectGroupItem item = this.getById(idArray[i]);
					if(item!=null) {
						item.setIsOrder((long)i);
						this.updateById(item);
					}
				}
				
            }
		}
	}
}
