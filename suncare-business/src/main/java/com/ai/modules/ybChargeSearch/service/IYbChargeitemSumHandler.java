/**
 * IYbChargeitemSumHandler.java	  V1.0   2023年2月8日 下午5:56:51
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.ybChargeSearch.service;

public interface IYbChargeitemSumHandler {

	void syncYbChargeitemSumFromGp(String datasource) throws Exception;

	void computeYbChargeitemSum(String datasource) throws Exception;
}
