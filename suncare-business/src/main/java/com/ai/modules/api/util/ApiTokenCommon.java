/**
 * ApiOauthUtil.java	  V1.0   2020年12月15日 下午8:43:07
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.api.util;

import com.ai.common.MedicalConstant;
import com.ai.modules.config.entity.MedicalOtherDict;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApiTokenCommon {

	private static RedisUtil redisUtil;

	@Autowired
	public ApiTokenCommon(RedisUtil redisUtil) {
		ApiTokenCommon.redisUtil = redisUtil;
	}

	public static JSONObject queryMedicalDictMapByKey(String type) {

		List<MedicalOtherDict> list = queryMedicalDictListByKey(type);
		JSONObject map = new JSONObject();
		for(MedicalOtherDict bean: list){
			map.put(bean.getCode(), bean.getValue());
		}
		return map;
	}

	public static JSONObject queryMedicalDictNameMapByKey(String type) {

		List<MedicalOtherDict> list = queryMedicalDictListByKey(type);
		JSONObject map = new JSONObject();
		for(MedicalOtherDict bean: list){
			map.put(bean.getValue(), bean.getCode());
		}
		return map;
	}

	public static List<MedicalOtherDict> queryMedicalDictListByKey(String type) {
		List<MedicalOtherDict> list = (List<MedicalOtherDict>) redisUtil.get(CacheConstant.REMOTE_MEDICAL_DICT_CACHE +  type);
		if(list == null){
			Map<String, String> paramMap = new HashMap<>();
			paramMap.put("type", type);
			list = ApiTokenUtil.getArray("/config/medicalDict/common/queryByType", paramMap, MedicalOtherDict.class);
			// 缓存
			redisUtil.set(CacheConstant.REMOTE_MEDICAL_DICT_CACHE + type, list, MedicalConstant.EXPIRE_DICT_TIME);
		}
		return list;
	}

	public static JSONObject queryOtherDictMapByType(String type) {
		List<MedicalOtherDict> list = queryOtherDictListByType(type);
		JSONObject map = new JSONObject();
		for(MedicalOtherDict bean: list){
			map.put(bean.getCode(), bean.getValue());
		}
		return map;
	}

	public static JSONObject queryOtherDictNameMapByType(String type) {
		List<MedicalOtherDict> list = queryOtherDictListByType(type);
		JSONObject map = new JSONObject();
		for(MedicalOtherDict bean: list){
			map.put(bean.getValue(), bean.getCode());
		}
		return map;
	}

	public static List<MedicalOtherDict> queryOtherDictListByType(String type) {
		List<MedicalOtherDict> list = (List<MedicalOtherDict>) redisUtil.get(CacheConstant.REMOTE_OTHER_DICT_CACHE + type);
		if(list == null){
			Map<String, String> paramMap = new HashMap<>();
			paramMap.put("dictEname", type);
			list = ApiTokenUtil.getArray("/config/medicalOtherDict/getDictList", paramMap, MedicalOtherDict.class);
			// 缓存
			redisUtil.set(CacheConstant.REMOTE_OTHER_DICT_CACHE + type, list, MedicalConstant.EXPIRE_DICT_TIME);
		}
		return list;
	}
}
