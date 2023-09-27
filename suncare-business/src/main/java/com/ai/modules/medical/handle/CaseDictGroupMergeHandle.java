/**
 * CaseDictMergeHandle.java	  V1.0   2021年7月6日 下午2:52:57
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.medical.handle;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.dbencrypt.DbDataEncryptUtil;

import com.ai.modules.formal.entity.MedicalFormalFlowRule;
import com.ai.modules.formal.service.IMedicalFormalFlowRuleService;
import com.ai.modules.probe.entity.MedicalFlowTemplRule;
import com.ai.modules.probe.entity.MedicalProbeFlowRule;
import com.ai.modules.probe.service.IMedicalFlowTemplRuleService;
import com.ai.modules.probe.service.IMedicalProbeFlowRuleService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

public class CaseDictGroupMergeHandle extends AbsDictMergeHandle {
	protected String tableName;
	protected String colName;

	public CaseDictGroupMergeHandle(String tableName, String colName) {
		this.tableName = tableName;
		this.colName = colName;
	}

	public CaseDictGroupMergeHandle(String colName) {
		this.colName = colName;
	}

	@Override
	public void merge(String main, String repeat) throws Exception {		
		//模型探查库
		IMedicalProbeFlowRuleService service = this.getBean(IMedicalProbeFlowRuleService.class);
		QueryWrapper<MedicalProbeFlowRule> wrapper = new QueryWrapper<MedicalProbeFlowRule>();		
		if(StringUtils.isNotBlank(tableName)) {
			wrapper.eq("table_name", tableName);
		}
		wrapper.eq("col_name", colName);
		wrapper.eq("compare_value", repeat);
		MedicalProbeFlowRule entity = new MedicalProbeFlowRule();
		entity.setCompareValue(main);
		service.update(entity, wrapper);
		//模型正式库
		this.mergeFormalCase(main, repeat);
		//模板节点
		this.mergeTmlCase(main, repeat);		
	}

	/**
	 *
	 * 功能描述：模型正式库
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年7月14日 下午4:40:39</p>
	 *
	 * @param main
	 * @param repeat
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private void mergeFormalCase(String main, String repeat) throws Exception {
		IMedicalFormalFlowRuleService service = this.getBean(IMedicalFormalFlowRuleService.class);
		QueryWrapper<MedicalFormalFlowRule> wrapper = new QueryWrapper<MedicalFormalFlowRule>();		
		if(StringUtils.isNotBlank(tableName)) {
			wrapper.eq("table_name", tableName);
		}
		wrapper.eq("col_name", colName);
		wrapper.eq(DbDataEncryptUtil.decryptFunc("compare_value"), repeat);
		MedicalFormalFlowRule entity = new MedicalFormalFlowRule();
		entity.setCompareValue(main);
		service.update(entity, wrapper);		
	}
	
	/**
	 * 
	 * 功能描述：模板节点
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2022年4月12日 下午5:40:49</p>
	 *
	 * @param main
	 * @param repeat
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private void mergeTmlCase(String main, String repeat) throws Exception {
		IMedicalFlowTemplRuleService service = this.getBean(IMedicalFlowTemplRuleService.class);
		QueryWrapper<MedicalFlowTemplRule> wrapper = new QueryWrapper<MedicalFlowTemplRule>();		
		if(StringUtils.isNotBlank(tableName)) {
			wrapper.eq("table_name", tableName);
		}
		wrapper.eq("col_name", colName);
		wrapper.eq("compare_value", repeat);
		MedicalFlowTemplRule entity = new MedicalFlowTemplRule();
		entity.setCompareValue(main);
		service.update(entity, wrapper);		
	}
}
