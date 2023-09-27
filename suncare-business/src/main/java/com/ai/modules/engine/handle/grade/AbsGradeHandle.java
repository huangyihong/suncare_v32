/**
 * AbsGradeHandle.java	  V1.0   2020年5月9日 下午5:10:28
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.grade;

import java.math.BigDecimal;

import com.ai.modules.engine.model.EngineRuleGrade;
import com.alibaba.fastjson.JSONObject;

public abstract class AbsGradeHandle {
	protected EngineRuleGrade grade;
	protected JSONObject jsonObject;
	
	public AbsGradeHandle(EngineRuleGrade grade, JSONObject jsonObject) {
		this.grade = grade;
		this.jsonObject = jsonObject;
	}
	
	public abstract BigDecimal grade();
}
