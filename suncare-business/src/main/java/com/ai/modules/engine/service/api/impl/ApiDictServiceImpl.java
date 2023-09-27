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

import org.springframework.stereotype.Service;

import com.ai.modules.api.util.ApiOauthClientUtil;
import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.ai.modules.engine.service.api.IApiDictService;

@Service
public class ApiDictServiceImpl implements IApiDictService {

	@Override
	public String queryDictTextByKey(String code, String key) {
		return ApiOauthClientUtil.parseText(code, key);
	}

	@Override
	public Map<String, String> queryDict(String code) {
		Map<String, String> result = new HashMap<String, String>();
		List<MedicalDictItemVO> list = ApiOauthClientUtil.parse(code);
		for(MedicalDictItemVO vo : list) {
			result.put(vo.getCode(), vo.getValue());
		}
		return result;
	}

	@Override
	public List<MedicalDictItemVO> queryMedicalDictByKind(String kind) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("kind", kind);
		List<MedicalDictItemVO> list = ApiOauthUtil.responseArray("/oauth/api/dict/dictByKind", busiParams, "post", MedicalDictItemVO.class);
		return list;
	}
	
	@Override
	public List<MedicalDictItemVO> queryMedicalDictByGroupId(String groupId) {
		Map<String, String> busiParams = new HashMap<String, String>();
		busiParams.put("groupId", groupId);
		List<MedicalDictItemVO> list = ApiOauthUtil.responseArray("/oauth/api/dict/dictByGroup", busiParams, "post", MedicalDictItemVO.class);
		return list;
	}

	@Override
	public MedicalColConfig queryMedicalColConfig(String tableName, String colName) {
		return ApiOauthClientUtil.queryMedicalColConfig(tableName, colName);
	}

	@Override
	public Map<String, MedicalActionDict> queryActionDict() {
		Map<String, MedicalActionDict> result = new HashMap<String, MedicalActionDict>();
		Map<String, String> busiParams = new HashMap<String, String>();
    	List<MedicalActionDict> list = ApiOauthUtil.responseArray("/oauth/api/dict/action/list", busiParams, "post", MedicalActionDict.class);
    	for(MedicalActionDict dict : list) {
			result.put(dict.getActionId(), dict);
		}
		return result;
	}

	@Override
	public MedicalActionDict queryActionDict(String actionId) {
		Map<String, MedicalActionDict> map = this.queryActionDict();
		return map.get(actionId);
	}

}
