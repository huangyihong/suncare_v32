/**
 * IEngineSummaryService.java	  V1.0   2020年12月8日 上午11:32:34
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service;

import java.util.Map;

import com.ai.modules.task.entity.TaskActionFieldConfig;
import com.alibaba.fastjson.JSONObject;

public interface IEngineSummaryService {
	/**
	 * 
	 * 功能描述：汇总批次某不合规行为
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月8日 上午11:34:12</p>
	 *
	 * @param batchId
	 * @param actionName
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	void summary(String batchId, String actionName);
	
	void summary(String batchId, String actionName, boolean valid);
	
	void summary(String batchId, String actionName, TaskActionFieldConfig config);
	
	/**
     * 
     * 功能描述：设置不合规行为汇总字段
     *
     * @author  zhangly
     * <p>创建日期 ：2020年12月16日 上午11:04:22</p>
     *
     * @param json
     * @param dynamicActionConfigMap
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    void settingSummayField(JSONObject json, Map<String, TaskActionFieldConfig> dynamicActionConfigMap);
    void settingSummayField(JSONObject json, TaskActionFieldConfig dynamicActionConfig);
    
    /**
     * 
     * 功能描述：增加批次的不合规行为汇总字段配置日志
     *
     * @author  zhangly
     * <p>创建日期 ：2020年12月16日 下午2:36:10</p>
     *
     * @param batchId
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    void insertBatchActionFieldConfig(String batchId);
}
