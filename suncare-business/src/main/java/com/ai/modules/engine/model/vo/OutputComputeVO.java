/**
 * OutputComputeVO.java	  V1.0   2021年11月26日 上午10:01:21
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.vo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OutputComputeVO {
	private Set<String> ids;
	private String visitid;
	private List<Set<String>> outputSetList;
	
	public OutputComputeVO(String id, String visitid) {
		ids = new HashSet<String>();
		ids.add(id);
		this.visitid = visitid;
		outputSetList = new ArrayList<Set<String>>();
	}
	
	@Override
	public String toString() {
		return "OutputComputeVO [ids=" + ids + ", visitid=" + visitid + ", outputSetList=" + outputSetList + "]";
	}
	
	public void addId(String id) {
		ids.add(id);
	}

	public void addOutput(String value, boolean set) {
		Set<String> outputSet = null;
		if(set) {
			outputSet = new HashSet<String>();
			outputSetList.add(outputSet);
		} else {
			outputSet = outputSetList.get(outputSetList.size()-1);
		}
		outputSet.add(value);
	}

	public Set<String> getIds() {
		return ids;
	}

	public String getVisitid() {
		return visitid;
	}

	public List<Set<String>> getOutputSetList() {
		return outputSetList;
	}

	
}
