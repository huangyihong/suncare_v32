/**
 * HerbDrugDictMergerHandle.java	  V1.0   2021年7月12日 上午9:33:48
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.medical.handle.treat;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ai.modules.config.entity.MedicalDrugGroupItem;
import com.ai.modules.config.entity.MedicalProjectGroupItem;
import com.ai.modules.config.entity.MedicalTreatProject;
import com.ai.modules.config.service.IMedicalProjectGroupItemService;
import com.ai.modules.config.service.IMedicalTreatProjectService;
import com.ai.modules.medical.handle.AbsDictMergeHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 *
 * 功能描述：诊疗项目字典合并处理类
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Service
@Transactional
public class TreatDictMergerHandle extends AbsDictMergeHandle {
	@Autowired
	IMedicalTreatProjectService service;
	@Autowired
	IMedicalProjectGroupItemService groupItemSV;
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public void merge(String main, String repeat) throws Exception {
		List<MedicalTreatProject> dataList = service.list(new QueryWrapper<MedicalTreatProject>().eq("code", main));
		if(dataList==null || dataList.size()==0) {
			throw new Exception("未找到项目编码"+main);
		}
		MedicalTreatProject treat = dataList.get(0);
		//先替换
		MedicalProjectGroupItem entity = new MedicalProjectGroupItem();
		entity.setCode(main);
		entity.setValue(treat.getName());
		groupItemSV.update(entity, new QueryWrapper<MedicalProjectGroupItem>().eq("code", repeat).eq("table_type", "STD_TREATMENT"));
		//再删除重复数据
		String sql = "select GROUP_ID from medical_project_group_item where code=? and table_type='STD_TREATMENT' group by group_id having count(1)>1";
		List<Map<String, Object>> repeatList = jdbcTemplate.queryForList(sql, main);
		if(repeatList!=null && repeatList.size()>0) {
			sql = "delete from medical_project_group_item where item_id<(select max(item_id) from medical_project_group_item where table_type='STD_TREATMENT' and group_id=? and code=?) and table_type='STD_TREATMENT' and group_id=? and code=?";
			for(Map<String, Object> map :repeatList) {
				String groupId = map.get("GROUP_ID").toString();
				jdbcTemplate.update(sql, groupId, main, groupId, main);
			}
		}
	}
}
