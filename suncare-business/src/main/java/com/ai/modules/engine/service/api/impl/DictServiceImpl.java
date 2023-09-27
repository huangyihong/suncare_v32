/**
 * DictServiceImpl.java	  V1.0   2020年12月23日 下午5:52:34
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.config.service.IMedicalColConfigService;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.ai.modules.engine.service.api.IApiDictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

//@Service
public class DictServiceImpl implements IApiDictService {
	@Autowired
    private IMedicalDictService dictService;
	@Autowired
    private IMedicalColConfigService configService;
	@Autowired
	private IMedicalActionDictService actionDictService;

	@Override
	public String queryDictTextByKey(String code, String key) {
		String text = dictService.queryDictTextByKey(code, key);
		return text;
	}

	@Override
	public Map<String, String> queryDict(String code) {
		Map<String, String> result = new HashMap<String, String>();
		List<MedicalDictItemVO> list = dictService.queryByType(code);
		for(MedicalDictItemVO vo : list) {
			result.put(vo.getCode(), vo.getValue());
		}
		return result;
	}

	@Override
	public List<MedicalDictItemVO> queryMedicalDictByKind(String kind) {
		return dictService.queryMedicalDictByKind(kind);
	}
	
	@Override
	public List<MedicalDictItemVO> queryMedicalDictByGroupId(String groupId) {
		return dictService.queryMedicalDictByGroupId(groupId);
	}

	@Override
	public MedicalColConfig queryMedicalColConfig(String tableName, String colName) {
		MedicalColConfig config = configService.getMedicalColConfigByCache(colName, tableName);
		return config;
	}

	@Override
	public Map<String, MedicalActionDict> queryActionDict() {
		Map<String, MedicalActionDict> result = new HashMap<String, MedicalActionDict>();
		List<MedicalActionDict> list = actionDictService.list();
		for(MedicalActionDict dict : list) {
			result.put(dict.getActionId(), dict);
		}
		return result;
	}

	@Override
	public MedicalActionDict queryActionDict(String actionId) {
		List<MedicalActionDict> list = actionDictService.list(new QueryWrapper<MedicalActionDict>().eq("ACTION_ID", actionId));
		if(list!=null && list.size()>0) {
			return list.get(0);
		}
		return null;
	}

}
