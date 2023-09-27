/**
 * DiseaseDictMergeHandle.java	  V1.0   2021年7月6日 上午10:53:52
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.medical.handle.pathology;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ai.modules.medical.entity.MedicalClinicalAccessGroup;
import com.ai.modules.medical.handle.AbsDictMergeHandle;
import com.ai.modules.medical.service.IMedicalClinicalAccessGroupService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 *
 * 功能描述：临床路径病例形态编码合并处理类
 *
 * @author  zhangly
 * Date: 2021年7月6日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Service
@Transactional
public class ClinicalPathologyDictMergeHandle extends AbsDictMergeHandle {
	@Autowired
	IMedicalClinicalAccessGroupService service;

	@Override
	public void merge(String main, String repeat) throws Exception {
		//准入条件组
		QueryWrapper<MedicalClinicalAccessGroup> wrapper = new QueryWrapper<MedicalClinicalAccessGroup>();
//		wrapper.in("group_id", "select group_id from MEDICAL_CLINICAL_ACCESS_GROUP where instr(concat(concat(',', pathologys), ','), ',"+repeat+",')>0");
		wrapper.in("GROUP_ID", "select GROUP_ID from MEDICAL_CLINICAL_ACCESS_GROUP where locate(',"+repeat+",',concat(concat(',', pathologys), ','))>0");
		List<MedicalClinicalAccessGroup> dataList = service.list(wrapper);
		if(dataList.size()>0) {
			for(MedicalClinicalAccessGroup bean : dataList) {
				String value = ","+bean.getPathologys()+",";
				if(value.contains(","+main+",")) {
					value = StringUtils.replace(value, ","+repeat+",", ",");
				} else {
					value = StringUtils.replace(value, ","+repeat+",", ","+main+",");
				}
				value = value.substring(1, value.length()-1);
				bean.setPathologys(value);
			}
			service.updateBatchById(dataList);
		}
	}
}
