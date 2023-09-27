/**
 * CaseDiseaseDictMergeHandle.java	  V1.0   2021年7月6日 下午2:37:18
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.medical.handle.disease;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.utils.ReflectHelper;
import com.ai.modules.config.entity.MedicalDiseaseGroup;
import com.ai.modules.config.service.IMedicalDiseaseGroupService;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.medical.handle.AbsDictMergeHandle;
import com.ai.modules.medical.service.IMedicalRuleConditionSetService;
import com.ai.modules.medical.service.IMedicalRuleConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 
 * 功能描述：规则遇到疾病组字典合并处理类
 *
 * @author  zhangly
 * Date: 2022年4月13日
 * Copyright (c) 2022 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Service
public class RuleDiseaseDictGroupMergeHandle extends AbsDictMergeHandle {
	
	//涉及疾病组的规则配置
	private static Map<String, String> kv = new HashMap<String, String>();
	static {
		kv.put("ext1", "accessDiseaseGroup|diseaseGroup");
		kv.put("ext2", "indication|unIndication");
		kv.put("ext4", "xtdrq");
	}
	
	@Autowired
	private IMedicalRuleConditionSetService service;
	@Autowired
	private IMedicalRuleConfigService ruleConfigService;
	@Autowired
	private IMedicalDiseaseGroupService groupService;

	@Override
	public void merge(String main, String repeat) throws Exception {
		List<MedicalRuleConditionSet> result = new ArrayList<MedicalRuleConditionSet>();
		Set<String> ruleIds = new HashSet<String>();
		for(Map.Entry<String, String> entry : kv.entrySet()) {
			QueryWrapper<MedicalRuleConditionSet> wrapper = new QueryWrapper<MedicalRuleConditionSet>();
			wrapper.isNotNull(entry.getKey());
			String v = entry.getValue();
			wrapper.in(DbDataEncryptUtil.decryptFunc("field"), Arrays.asList(StringUtils.split(v, "|")));
			String where = "instr(concat('|', replace("+DbDataEncryptUtil.decryptFunc(entry.getKey())+", ',', '|'), '|'), '|"+repeat+"|')>0";
			wrapper.apply(where);
			List<MedicalRuleConditionSet> dataList = service.list(wrapper);			
			if(dataList!=null && dataList.size()>0) {
				for(MedicalRuleConditionSet bean : dataList) {
					ruleIds.add(bean.getRuleId());
					log.info("----------------------------------------------------------------------");
					log.info("规则id={},field={}", bean.getRuleId(), entry.getKey());
					String value = String.valueOf(ReflectHelper.getValue(bean, entry.getKey()));
					log.info("替换前：value={}", value);
					if(!value.contains(",") && !value.contains("|")) {
						//配置项仅有唯一值
						value = StringUtils.replace(value, repeat, main);
					} else {
						value = "|"+value+"|";
						value = StringUtils.replace(value, "|"+repeat+"|", "|"+main+"|");
						value = StringUtils.replace(value, "|"+repeat+",", "|"+main+",");
						value = StringUtils.replace(value, ","+repeat+"|", ","+main+"|");
						value = StringUtils.replace(value, ","+repeat+",", ","+main+",");
						value = value.substring(1, value.length()-1);
					}					
					ReflectHelper.setValue(bean, entry.getKey(), value);
					log.info("替换后：value={}", value);					
				}
				result.addAll(dataList);
			}
		}
		if(result.size()>0) {
			service.updateBatchById(result);
			//规则更新
			MedicalDiseaseGroup mainGroup = groupService.getOne(new QueryWrapper<MedicalDiseaseGroup>().eq("group_code", main));
			MedicalDiseaseGroup repeatGroup = groupService.getOne(new QueryWrapper<MedicalDiseaseGroup>().eq("group_code", repeat));
			MedicalRuleConfig config = new MedicalRuleConfig();
			config.setUpdateTime(DateUtils.getDate());
			LoginUser user = null;
			if(SolrUtil.isWeb()) {
				Subject subject = SecurityUtils.getSubject();
				user = (LoginUser) subject.getPrincipal();
				config.setUpdateUser(user.getUsername());
				config.setUpdateUsername(user.getRealname());				
			}
			String text = "替换疾病组[%s(%s)->%s(%s)]";
			text = String.format(text, repeatGroup.getGroupName(), repeat, mainGroup.getGroupName(), main);
			config.setUpdateReason(text);
			ruleConfigService.update(config, new QueryWrapper<MedicalRuleConfig>().in("rule_id", ruleIds));
		}
	}
}
