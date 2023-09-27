/**
 * IApiEngineService.java	  V1.0   2021年1月4日 下午9:26:32
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service;

import java.util.List;

import com.ai.modules.config.entity.StdHoslevelFundpayprop;

public interface IApiEngineService {
	/**
	 * 
	 * 功能描述：报销比例
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年1月4日 下午9:21:37</p>
	 *
	 * @param datasource
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	List<StdHoslevelFundpayprop> findStdHoslevelFundpayprop(String datasource);
}
