package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalProjectGroupItem;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 医疗服务项目分组子项
 * @Author: jeecg-boot
 * @Date:   2020-03-03
 * @Version: V1.0
 */
public interface IMedicalProjectGroupItemService extends IService<MedicalProjectGroupItem> {

	void updateOrderByItemIds(String itemIds);
}
