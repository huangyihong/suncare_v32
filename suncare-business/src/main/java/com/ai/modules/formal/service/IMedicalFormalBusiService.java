package com.ai.modules.formal.service;

import java.util.List;

import com.ai.modules.formal.entity.MedicalFormalBusi;
import com.ai.modules.formal.vo.MedicalFormalBusiVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 业务组表
 * @Author: jeecg-boot
 * @Date:   2019-11-28
 * @Version: V1.0
 */
public interface IMedicalFormalBusiService extends IService<MedicalFormalBusi> {
	/**
	 * 保存业务组件及业务组件模型关联信息
	 * @param medicalFormalBusiVO
	 */
	void saveBusiAndCaseBusi(MedicalFormalBusiVO medicalFormalBusiVO);

	/**
	 * 修改业务组件及业务组件模型关联信息
	 * @param medicalFormalBusiVO
	 */
	void updateBusiAndCaseBusi(MedicalFormalBusiVO medicalFormalBusiVO);

	/**
	 * 删除业务组件及业务组件模型关联信息
	 * @param id
	 */
	void removeBusiAndCaseBusiById(String id);

	/**
	 * 批量删除业务组件及业务组件模型关联信息 
	 * @param idList
	 */
	void removeBusiAndCaseBusiByIds(List<String> idList);

}
