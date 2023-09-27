/**
 * DiseaseDictMergeHandle.java	  V1.0   2021年7月6日 上午10:53:52
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.medical.handle.treat;

import java.util.LinkedList;
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
 * 功能描述：临床路径诊疗项目合并处理类
 *
 * @author  zhangly
 * Date: 2021年7月6日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Service
@Transactional
public class ClinicalTreatDictMergeHandle extends AbsDictMergeHandle {
	@Autowired
	IMedicalClinicalAccessGroupService service;

	@Override
	public void merge(String main, String repeat) throws Exception {
		//准入条件组
		QueryWrapper<MedicalClinicalAccessGroup> wrapper = new QueryWrapper<MedicalClinicalAccessGroup>();
		wrapper.isNotNull("check_items");
//		wrapper.inSql("group_id", "select group_id from medical_clinical_access_group where instr(concat(concat(',', check_items), ','), ',"+repeat+",')>0");
		wrapper.inSql("GROUP_ID", "select GROUP_ID from medical_clinical_access_group where locate(',"+repeat+",',concat(concat(',', check_items), ','))>0");
		List<MedicalClinicalAccessGroup> dataList = service.list(wrapper);
		if(dataList.size()>0) {
			for(MedicalClinicalAccessGroup bean : dataList) {
				String value = bean.getCheckItems();
				String[] keyArray = StringUtils.split(value, ",");
				String[] valueArray = StringUtils.split(bean.getCheckItemsDesc(), ",");
				List<String> keySet = new LinkedList<String>();
				List<String> valueSet = new LinkedList<String>();
				for(int i=0, len=keyArray.length; i<len; i++) {
					String key = keyArray[i];
					if(key.equals(main) || key.equals(repeat)) {
						if(keySet.contains(main)) {
							//已经包含主项
							continue;
						}
						keySet.add(key);
						valueSet.add(valueArray[i]);
					} else {
						keySet.add(key);
						valueSet.add(valueArray[i]);
					}
				}
				bean.setCheckItems(StringUtils.join(keySet, ","));
				bean.setCheckItemsDesc(StringUtils.join(valueSet, ","));
			}
			service.updateBatchById(dataList);
		}
	}
}
