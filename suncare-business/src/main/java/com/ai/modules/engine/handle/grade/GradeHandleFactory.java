/**
 * GradeHandleFactory.java	  V1.0   2020年5月9日 下午5:19:52
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.grade;

import com.ai.modules.engine.model.EngineRuleGrade;
import com.alibaba.fastjson.JSONObject;

public class GradeHandleFactory {
	private EngineRuleGrade grade;
	private JSONObject jsonObject;
	
	public GradeHandleFactory(EngineRuleGrade grade, JSONObject jsonObject) {
		this.grade = grade;
		this.jsonObject = jsonObject;
	}
	
	public AbsGradeHandle build() {
		return new GradeHandle(grade, jsonObject);
	}
}
