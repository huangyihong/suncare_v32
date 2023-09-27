/**
 * ApiEngineServiceImpl.java	  V1.0   2021年1月4日 下午9:26:56
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
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

import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.config.entity.StdHoslevelFundpayprop;
import com.ai.modules.engine.service.IApiEngineService;

@Service
public class ApiEngineServiceImpl implements IApiEngineService {
	
	@Override
	public List<StdHoslevelFundpayprop> findStdHoslevelFundpayprop(String datasource) {
		Map<String, String> busiParams = new HashMap<String, String>();
    	busiParams.put("datasource", datasource);
    	List<StdHoslevelFundpayprop> result = ApiOauthUtil.responseArray("/oauth/api/engine/fundpayRatio", busiParams, "post", StdHoslevelFundpayprop.class);
    	return result;
	}
}
