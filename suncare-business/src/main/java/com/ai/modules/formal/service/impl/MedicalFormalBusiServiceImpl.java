package com.ai.modules.formal.service.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ai.common.utils.IdUtils;
import com.ai.modules.formal.entity.MedicalFormalBusi;
import com.ai.modules.formal.entity.MedicalFormalCaseBusi;
import com.ai.modules.formal.mapper.MedicalFormalBusiMapper;
import com.ai.modules.formal.mapper.MedicalFormalCaseBusiMapper;
import com.ai.modules.formal.service.IMedicalFormalBusiService;
import com.ai.modules.formal.vo.MedicalFormalBusiVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 业务组表
 * @Author: jeecg-boot
 * @Date:   2019-11-28
 * @Version: V1.0
 */
@Service
@Transactional
public class MedicalFormalBusiServiceImpl extends ServiceImpl<MedicalFormalBusiMapper, MedicalFormalBusi> implements IMedicalFormalBusiService {

	@Autowired
	MedicalFormalBusiMapper medicalFormalBusiMapper;
	
	@Autowired
	MedicalFormalCaseBusiMapper medicalFormalCaseBusiMapper;
	
	/**
	 * 保存业务组件及业务组件模型关联信息
	 */
	@Override
	public void saveBusiAndCaseBusi(MedicalFormalBusiVO bean) {
		medicalFormalBusiMapper.insert(bean);
		//保存业务组件模型关联信息
		saveBatchCaseBusi(bean);
		
	}

	@Override
	public void updateBusiAndCaseBusi(MedicalFormalBusiVO bean) {
		medicalFormalBusiMapper.updateById(bean);
		//删除业务组件模型关联信息
		medicalFormalCaseBusiMapper.delete(new QueryWrapper<MedicalFormalCaseBusi>().eq("BUSI_ID", bean.getBusiId()));
		//保存业务组件模型关联信息
		saveBatchCaseBusi(bean);
	}

	private void saveBatchCaseBusi(MedicalFormalBusiVO bean) {
		if(StringUtils.isNotBlank(bean.getCaseIds())) {
			String[] caseIdsArr = bean.getCaseIds().split(",");
			for(String caseId:caseIdsArr) {
				MedicalFormalCaseBusi caseBusiBean = new MedicalFormalCaseBusi();
				caseBusiBean.setRelaId(IdUtils.uuid());
				caseBusiBean.setCaseId(caseId);
				caseBusiBean.setBusiId(bean.getBusiId());
				medicalFormalCaseBusiMapper.insert(caseBusiBean);
			}
		}
	}

	@Override
	public void removeBusiAndCaseBusiById(String id) {
		medicalFormalBusiMapper.deleteById(id);
		//删除业务组件模型关联信息
		medicalFormalCaseBusiMapper.delete(new QueryWrapper<MedicalFormalCaseBusi>().eq("BUSI_ID", id));
	}

	@Override
	public void removeBusiAndCaseBusiByIds(List<String> idList) {
		medicalFormalBusiMapper.deleteBatchIds(idList);
		//删除业务组件模型关联信息
		medicalFormalCaseBusiMapper.delete(new QueryWrapper<MedicalFormalCaseBusi>().in("BUSI_ID",idList));
	}

}
