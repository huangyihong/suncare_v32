/**
 * AbsHiveDwsNodeRuleHandle.java	  V1.0   2023年1月9日 上午9:50:48
 *
 * Copyright (c) 2023 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.node.hive;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineConstant;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.EngineMiddleDwsMapping;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.solr.HiveJDBCUtil;

public abstract class AbsHiveDwsNodeRuleHandle extends AbsHiveNodeRuleHandle {
	
	protected String durationType;

	public AbsHiveDwsNodeRuleHandle(EngineNodeRule rule, String fromTable, String alias) {
		super(rule, fromTable, alias);
	}

	public AbsHiveDwsNodeRuleHandle(EngineNodeRule rule, String fromTable) {
		super(rule, fromTable, null);
	}
	
	protected String parseDuration() {
		String duration = null;
		if(HiveJDBCUtil.enabledProcessGp()) {
	    	if("日".equals(durationType)) {
		    	duration = "to_char(visitdate, 'yyyy-MM-dd')";
		    } else if("周".equals(durationType)) {
		    	duration = "concat(to_char(visitdate, 'yyyy'), '年第', weekofyear(visitdate), '周')";
		    } else if("月".equals(durationType)) {
		    	duration = "to_char(visitdate, 'yyyy-MM')";
		    } else if("季".equals(durationType)) {
		    	
		    } else if("年".equals(durationType)) {
		    	duration = "to_char(visitdate, 'yyyy')";
		    }
	    } else {
	    	if("日".equals(durationType)) {
		    	duration = "from_unixtime(unix_timestamp(visitdate, 'yyyy-MM-dd'), 'yyyy-MM-dd')";
		    } else if("周".equals(durationType)) {
		    	duration = "concat(from_unixtime(unix_timestamp(visitdate, 'yyyy-MM-dd'), 'yyyy'), '年第', weekofyear(visitdate), '周')";
		    } else if("月".equals(durationType)) {
		    	duration = "from_unixtime(unix_timestamp(visitdate, 'yyyy-MM-dd'), 'yyyy-MM')";
		    } else if("季".equals(durationType)) {
		    	
		    } else if("年".equals(durationType)) {
		    	duration = "from_unixtime(unix_timestamp(visitdate, 'yyyy-MM-dd'), 'yyyy')";
		    }
	    }
		return duration;
	}
	
	public String getDurationType() {
		return durationType;
	}

	public void setDurationType(String durationType) {
		this.durationType = durationType;
	}
}
