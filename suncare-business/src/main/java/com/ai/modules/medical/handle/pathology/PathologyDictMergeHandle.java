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
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ai.modules.config.entity.MedicalDiseaseGroupItem;
import com.ai.modules.config.entity.MedicalPathology;
import com.ai.modules.config.service.IMedicalDiseaseGroupItemService;
import com.ai.modules.config.service.IMedicalPathologyService;
import com.ai.modules.medical.handle.AbsDictMergeHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 *
 * 功能描述：形态学字典合并处理类
 *
 * @author  zhangly
 * Date: 2021年7月6日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Service
@Transactional
public class PathologyDictMergeHandle extends AbsDictMergeHandle {
	@Autowired
	IMedicalPathologyService service;
	@Autowired
	IMedicalDiseaseGroupItemService groupItemSV;
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public void merge(String main, String repeat) throws Exception {
		List<MedicalPathology> dataList = service.list(new QueryWrapper<MedicalPathology>().eq("code", main));
		if(dataList==null || dataList.size()==0) {
			throw new Exception("未找到形态学编码"+main);
		}
		MedicalPathology diag = dataList.get(0);
		//先替换
		MedicalDiseaseGroupItem entity = new MedicalDiseaseGroupItem();
		entity.setCode(main);
		entity.setValue(diag.getName());
		groupItemSV.update(entity, new QueryWrapper<MedicalDiseaseGroupItem>().eq("code", repeat).eq("table_type", "STD_PATHOLOGY"));
		//再删除重复数据
		String sql = "select GROUP_ID from medical_disease_group_item where code=? and table_type='STD_PATHOLOGY' group by group_id having count(1)>1";
		List<Map<String, Object>> repeatList = jdbcTemplate.queryForList(sql, main);
		if(repeatList!=null && repeatList.size()>0) {
			sql = "delete from medical_disease_group_item where item_id<(select max(item_id) from medical_disease_group_item where table_type='STD_PATHOLOGY' and group_id=? and code=?) and table_type='STD_PATHOLOGY' and group_id=? and code=?";
			for(Map<String, Object> map :repeatList) {
				String groupId = map.get("GROUP_ID").toString();
				jdbcTemplate.update(sql, groupId, main, groupId, main);
			}
		}
	}
}
