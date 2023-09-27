/**
 * EngineHandleMapping.java	  V1.0   2022年11月17日 下午3:00:29
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model;

import com.ai.modules.engine.handle.rule.AbsHiveRuleHandle;
import com.ai.modules.engine.handle.rule.AbsSolrRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrRuleHandle;

import lombok.Data;

@Data
public class EngineHandleMapping {
	//规则类型
	private String ruleLimit;
	//使用solr计算
	private Class<? extends AbsSolrRuleHandle> solrHandleClazz;
	//使用hive计算（impala）
	private Class<? extends AbsHiveRuleHandle> hiveHandleClazz;
	
	public EngineHandleMapping(String ruleLimit) {
		this.ruleLimit = ruleLimit;
		this.solrHandleClazz = SolrRuleHandle.class;
		this.hiveHandleClazz = HiveRuleHandle.class;
	}
	
	public EngineHandleMapping(String ruleLimit, Class<? extends AbsSolrRuleHandle> solrHandleClazz, Class<? extends AbsHiveRuleHandle> hiveHandleClazz) {
		this.ruleLimit = ruleLimit;
		this.solrHandleClazz = solrHandleClazz;
		this.hiveHandleClazz = hiveHandleClazz;
	}
	
	/**
	 * 
	 * 功能描述：默认规则计算引擎
	 *
	 * @author  zhangly
	 *
	 * @param ruleLimit
	 * @return
	 */
	public static EngineHandleMapping def(String ruleLimit) {
		return new EngineHandleMapping(ruleLimit);
	}
	
	public static EngineHandleMapping solr(String ruleLimit, Class<? extends AbsSolrRuleHandle> solrHandleClazz) {
		return new EngineHandleMapping(ruleLimit, solrHandleClazz, HiveRuleHandle.class);
	}
	
	public static EngineHandleMapping hive(String ruleLimit, Class<? extends AbsHiveRuleHandle> hiveHandleClazz) {
		return new EngineHandleMapping(ruleLimit, SolrRuleHandle.class, hiveHandleClazz);
	}
}
