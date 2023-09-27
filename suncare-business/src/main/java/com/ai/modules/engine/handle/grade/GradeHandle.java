/**
 * GradeHandle.java	  V1.0   2020年5月9日 下午5:13:55
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

public class GradeHandle extends AbsGradeHandle {

	public GradeHandle(EngineRuleGrade grade, JSONObject jsonObject) {
		super(grade, jsonObject);
	}

	@Override
	public BigDecimal grade() {
		BigDecimal score = new BigDecimal(0);
		if(jsonObject.get(grade.getFieldName())!=null) {
			BigDecimal bd = jsonObject.getBigDecimal(grade.getFieldName());
			bd = bd.subtract(grade.getStandard());
			bd = bd.abs().divide(grade.getWeight(), 4);
			if(bd.compareTo(grade.getWeight())>0) {
				bd = grade.getWeight();
			}
			score = score.add(bd);
		}
		return score;
	}

}
