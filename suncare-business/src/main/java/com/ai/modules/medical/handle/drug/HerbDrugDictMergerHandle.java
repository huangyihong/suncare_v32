/**
 * HerbDrugDictMergerHandle.java	  V1.0   2021年7月12日 上午9:33:48
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.medical.handle.drug;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ai.modules.config.entity.MedicalChineseDrug;
import com.ai.modules.config.entity.MedicalDrugGroupItem;
import com.ai.modules.config.service.IMedicalChineseDrugService;
import com.ai.modules.config.service.IMedicalDrugGroupItemService;
import com.ai.modules.medical.handle.AbsDictMergeHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 *
 * 功能描述：中草药字典合并处理类
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Service
@Transactional
public class HerbDrugDictMergerHandle extends AbsDictMergeHandle {
	@Autowired
	IMedicalChineseDrugService service;
	@Autowired
	IMedicalDrugGroupItemService groupItemSV;
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public void merge(String main, String repeat) throws Exception {
		List<MedicalChineseDrug> drugList = service.list(new QueryWrapper<MedicalChineseDrug>().eq("code", main));
		if(drugList==null || drugList.size()==0) {
			throw new Exception("未找到中草药药品编码"+main);
		}
		MedicalChineseDrug drug = drugList.get(0);
		//先替换
		MedicalDrugGroupItem entity = new MedicalDrugGroupItem();
		entity.setCode(main);
		entity.setValue(drug.getName());
		groupItemSV.update(entity, new QueryWrapper<MedicalDrugGroupItem>().eq("code", repeat).eq("table_type", "STD_HERB"));
		//再删除重复数据
		String sql = "select GROUP_ID from medical_drug_group_item where code=? and table_type='STD_HERB' group by group_id having count(1)>1";
		List<Map<String, Object>> repeatList = jdbcTemplate.queryForList(sql, main);
		if(repeatList!=null && repeatList.size()>0) {
			sql = "delete from medical_drug_group_item where item_id<(select max(item_id) from medical_drug_group_item where table_type='STD_HERB' and group_id=? and code=?) and table_type='STD_HERB' and group_id=? and code=?";
			for(Map<String, Object> map :repeatList) {
				String groupId = map.get("GROUP_ID").toString();
				jdbcTemplate.update(sql, groupId, main, groupId, main);
			}
		}
	}
}
