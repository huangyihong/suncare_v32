package com.ai.modules.config.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.ai.modules.config.entity.MedicalDataTagDef;
import com.ai.modules.config.mapper.MedicalDataTagDefMapper;
import com.ai.modules.config.service.IMedicalDataTagDefService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 标签管理
 * @Author: jeecg-boot
 * @Date:   2021-11-04
 * @Version: V1.0
 */
@Service
public class MedicalDataTagDefServiceImpl extends ServiceImpl<MedicalDataTagDefMapper, MedicalDataTagDef> implements IMedicalDataTagDefService {
	public void saveDataTag(MedicalDataTagDef medicalDataTagDef) throws Exception{
		//新增操作
		if(StringUtils.isBlank(medicalDataTagDef.getTagId())) {
			//判断该表下是否存在同样的字段名
			String tagName = medicalDataTagDef.getTagName().trim();
			String ownTableName = medicalDataTagDef.getOwnTableName();
			
			QueryWrapper<MedicalDataTagDef> queryWrapper = new QueryWrapper<MedicalDataTagDef>();
			queryWrapper.eq("TAG_NAME", tagName);
			queryWrapper.eq("OWN_TABLE_NAME", ownTableName);
			
			List<MedicalDataTagDef> list = this.baseMapper.selectList(queryWrapper);
			if (list != null && list.size() > 0) {
				throw new Exception("添加失败！"+tagName +"标签已经存在！");
			}
			
			this.save(medicalDataTagDef);
		}
	}
}
