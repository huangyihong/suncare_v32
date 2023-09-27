/**
 * ApiOauthClientUtil.java	  V1.0   2020年12月21日 下午3:25:27
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.api.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.config.vo.MedicalDictItemVO;
import com.ai.modules.config.vo.MedicalGroupVO;

public class ApiOauthClientUtil {
	/**
	 * 
	 * 功能描述：字典解析
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月21日 下午3:27:02</p>
	 *
	 * @param code
	 * @param key
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public static String parseText(String code, String key) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("code", code);
    	busiParams.put("key", key);
		String text = ApiOauthUtil.response("/oauth/api/dict/parseText", busiParams, "post", String.class);
		return text;
	}
	
	public static List<MedicalDictItemVO> parse(String code) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("code", code);
    	List<MedicalDictItemVO> list = ApiOauthUtil.responseArray("/oauth/api/dict/parse", busiParams, "post", MedicalDictItemVO.class);
		return list;
	}
	
	public static MedicalColConfig queryMedicalColConfig(String tableName, String colName) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("tableName", tableName);
    	busiParams.put("colName", colName);
    	MedicalColConfig result = ApiOauthUtil.response("/oauth/api/dict/medicalColConfig", busiParams, "post", MedicalColConfig.class);
		return result;
	}
	
	/**
	 * 
	 * 功能描述：根据项目组编码获取项目列表
	 *
	 * @author  zhangly
	 *
	 * @param groupCode
	 * @return
	 */
	public static List<MedicalGroupVO> projectGrp(String groupCode) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("groupCode", groupCode);
    	List<MedicalGroupVO> list = ApiOauthUtil.responseArray("/oauth/api/dict/projectGrp", busiParams, "post", MedicalGroupVO.class);
		return list;
	}
}
