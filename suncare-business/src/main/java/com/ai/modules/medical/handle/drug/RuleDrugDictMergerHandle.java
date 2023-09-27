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

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ai.modules.config.entity.MedicalStdAtc;
import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.config.service.IMedicalStdAtcService;
import com.ai.modules.config.service.IMedicalYbDrugService;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.handle.AbsDictMergeHandle;
import com.ai.modules.medical.service.IMedicalRuleConditionSetService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 *
 * 功能描述：规则遇到药品合并处理类
 *
 * @author  zhangly
 * Date: 2021年7月12日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Service
@Transactional
public class RuleDrugDictMergerHandle extends AbsDictMergeHandle {
	@Autowired
	IMedicalStdAtcService atcService;
	@Autowired
	IMedicalRuleConditionSetService service;
	@Autowired
	IMedicalYbDrugService repeatService;
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public void merge(String main, String repeat) throws Exception {
		List<MedicalStdAtc> drugList = atcService.list(new QueryWrapper<MedicalStdAtc>().eq("code", main));
		if(drugList==null || drugList.size()==0) {
			throw new Exception("未找到ATC药品编码"+main);
		}

		//二线用药规则
		QueryWrapper<MedicalRuleConditionSet> wrapper = new QueryWrapper<MedicalRuleConditionSet>();
		wrapper.eq(DbDataEncryptUtil.decryptFunc("field"), "secDrug");
		wrapper.isNotNull("ext1");
//		wrapper.inSql("id", "select id from medical_rule_condition_set where instr(concat(concat('|', ext1), '|'), '|"+repeat+"|')>0");
		wrapper.inSql("ID", "select ID from medical_rule_condition_set where locate('|"+repeat+"|',concat(concat('|', "+DbDataEncryptUtil.decryptFunc("ext1")+"), '|'))>0");
		List<MedicalRuleConditionSet> dataList = service.list(wrapper);
		if(dataList.size()>0) {
			for(MedicalRuleConditionSet bean : dataList) {
				String value = "|"+bean.getExt1()+"|";
				if(value.contains("|"+main+"|")) {
					value = StringUtils.replace(value, "|"+repeat+"|", "|");
				} else {
					value = StringUtils.replace(value, "|"+repeat+"|", "|"+main+"|");
				}
				value = value.substring(1, value.length()-1);
				bean.setExt1(value);
			}
			service.updateBatchById(dataList);
		}

		MedicalStdAtc drug = drugList.get(0);
		//重复用药
		//先替换
		MedicalYbDrug entity = new MedicalYbDrug();
		entity.setCode(main);
		entity.setName(drug.getName());
		repeatService.update(entity, new QueryWrapper<MedicalYbDrug>().eq("code", repeat).eq("table_type", "STD_ATC"));
		//再删除重复数据
		String sql = "select PARENT_CODE from medical_yb_drug where code=? and table_type='STD_ICD' group by parent_code having count(1)>1";
		List<Map<String, Object>> repeatList = jdbcTemplate.queryForList(sql, main);
		if(repeatList!=null && repeatList.size()>0) {
			sql = "delete from medical_yb_drug where id<(select max(id) from medical_yb_drug where table_type='STD_ATC' and parent_code=? and code=?) and table_type='STD_ATC' and parent_code=? and code=?";
			for(Map<String, Object> map :repeatList) {
				String parentCode = map.get("PARENT_CODE").toString();
				jdbcTemplate.update(sql, parentCode, main, parentCode, main);
			}
		}
	}
}
