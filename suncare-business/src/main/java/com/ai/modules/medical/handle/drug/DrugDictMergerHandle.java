/**
 * DrugDictMergerHandle.java	  V1.0   2021年7月12日 上午11:21:25
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

import com.ai.modules.config.entity.MedicalDrug;
import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.config.service.IMedicalDrugService;
import com.ai.modules.config.service.IMedicalYbDrugService;
import com.ai.modules.medical.handle.AbsDictMergeHandle;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 *
 * 功能描述：medical_drug药品字典合并处理类
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Service
@Transactional
public class DrugDictMergerHandle extends AbsDictMergeHandle {
	@Autowired
	IMedicalDrugService service;
	@Autowired
	IMedicalYbDrugService repeatService;
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public void merge(String main, String repeat) throws Exception {
		List<MedicalDrug> drugList = service.list(new QueryWrapper<MedicalDrug>().eq("code", main));
		if(drugList==null || drugList.size()==0) {
			throw new Exception("未找到药品编码"+main);
		}
		MedicalDrug drug = drugList.get(0);
		//重复用药
		//先替换
		MedicalYbDrug entity = new MedicalYbDrug();
		entity.setCode(main);
		entity.setName(drug.getName());
		repeatService.update(entity, new QueryWrapper<MedicalYbDrug>().eq("code", repeat).eq("table_type", "STD_DRUG_INFO"));
		//再删除重复数据
		String sql = "select PARENT_CODE from medical_yb_drug where code=? and table_type='STD_DRUG_INFO' group by parent_code having count(1)>1";
		List<Map<String, Object>> repeatList = jdbcTemplate.queryForList(sql, main);
		if(repeatList!=null && repeatList.size()>0) {
			sql = "delete from medical_yb_drug where id<(select max(id) from medical_yb_drug where table_type='STD_DRUG_INFO' and parent_code=? and code=?) and table_type='STD_DRUG_INFO' and parent_code=? and code=?";
			for(Map<String, Object> map :repeatList) {
				String parentCode = map.get("PARENT_CODE").toString();
				jdbcTemplate.update(sql, parentCode, main, parentCode, main);
			}
		}
	}
}
