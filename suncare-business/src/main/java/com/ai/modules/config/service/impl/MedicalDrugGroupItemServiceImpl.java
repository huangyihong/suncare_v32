package com.ai.modules.config.service.impl;

import com.ai.modules.config.entity.MedicalDictItem;
import com.ai.modules.config.entity.MedicalDrugGroupItem;
import com.ai.modules.config.mapper.MedicalDrugGroupItemMapper;
import com.ai.modules.config.service.IMedicalDrugGroupItemService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.micrometer.core.instrument.util.StringUtils;

import java.util.List;

/**
 * @Description: 药品分组项
 * @Author: jeecg-boot
 * @Date:   2020-03-02
 * @Version: V1.0
 */
@Service
public class MedicalDrugGroupItemServiceImpl extends ServiceImpl<MedicalDrugGroupItemMapper, MedicalDrugGroupItem> implements IMedicalDrugGroupItemService {

	@Override
	public void updateOrderByItemIds(String itemIds) {
		if(StringUtils.isNotBlank(itemIds)){
			String[] idArray = itemIds.split(",");
			for(int i = 0, len = idArray.length; i < len; i++){
				if(StringUtils.isNotBlank(idArray[i])) {
					MedicalDrugGroupItem item = this.getById(idArray[i]);
					if(item!=null) {
						item.setIsOrder((long)i);
						this.updateById(item);
					}
				}

            }
		}
	}

}
