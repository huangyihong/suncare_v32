/**
 * IEngineDrugService.java	  V1.0   2020年1月2日 上午11:06:38
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.service;

import java.util.Map;

import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.google.common.collect.Sets.SetView;

/**
 *
 * 功能描述：临床路径合规检查
 *
 * @author  zhangly
 * Date: 2020年1月19日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public interface IEngineClinicalService extends IEngineBaseService {

	void generateMedicalUnreasonableClinicalActionByThreadPool(String batchId, String clinicalId);

	/**
	 *
	 * 功能描述：按每个临床路径计算违规数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年1月17日 下午2:35:33</p>
	 *
	 * @param batchId
	 * @param clinicalId
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void generateMedicalUnreasonableClinicalAction(String batchId, String clinicalId) throws Exception;
}
