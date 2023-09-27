/**
 * IDictService.java	  V1.0   2020年12月23日 下午5:51:56
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.api;

import java.util.List;
import java.util.Map;

import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.config.vo.MedicalDictItemVO;

public interface IApiDictService {
	String queryDictTextByKey(String code, String key);
	
	Map<String, String> queryDict(String code);
	
	List<MedicalDictItemVO> queryMedicalDictByKind(String kind);
	
	List<MedicalDictItemVO> queryMedicalDictByGroupId(String groupId);
	
	MedicalColConfig queryMedicalColConfig(String tableName, String colName);
	
	Map<String, MedicalActionDict> queryActionDict();
	
	MedicalActionDict queryActionDict(String actionId);
}
