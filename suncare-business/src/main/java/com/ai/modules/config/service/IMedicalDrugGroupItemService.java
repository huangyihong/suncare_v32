package com.ai.modules.config.service;

import com.ai.modules.config.entity.MedicalDrugGroupItem;
import com.ai.modules.config.vo.MedicalGroupVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 药品分组项
 * @Author: jeecg-boot
 * @Date:   2020-03-02
 * @Version: V1.0
 */
public interface IMedicalDrugGroupItemService extends IService<MedicalDrugGroupItem> {

	void updateOrderByItemIds(String itemIds);

}
