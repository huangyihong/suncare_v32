/**
 * TreatOverFreqComputeVO.java	  V1.0   2021年9月8日 上午10:51:12
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreatOverFreqComputeVO extends ComputeVO {
	//主体项目编码
	private String itemcode;
	//互斥项目明细
	private List<ChargedetailVO> mutexList = new ArrayList<ChargedetailVO>();
	//互斥项目冲销明细
	private List<ChargedetailVO> mutexOffsetList = new ArrayList<ChargedetailVO>();

	public TreatOverFreqComputeVO(String visitid, String itemcode) {
		super(visitid);
		this.itemcode = itemcode;
	}

	public void add(ChargedetailVO vo) {
		if(vo.getItemcode().equals(itemcode)) {
			if(vo.getAmount().compareTo(BigDecimal.ZERO)>=0) {
				detailList.add(vo);
			} else {
				detailOffsetList.add(vo);
			}
		} else {
			if(vo.getAmount().compareTo(BigDecimal.ZERO)>=0) {
				mutexList.add(vo);
			} else {
				mutexOffsetList.add(vo);
			}
		}
	}
	
	public Set<String> existsMutexDay() {
		offset(this.mutexList, this.mutexOffsetList);
		
		Map<String, ChargedayVO> mutexMap = new HashMap<String, ChargedayVO>();
		for(ChargedetailVO vo : mutexList) {
			put(mutexMap, vo);
		}
		List<ChargedayVO> mutexList = new ArrayList<ChargedayVO>();
		for(Map.Entry<String, ChargedayVO> entry : mutexMap.entrySet()) {
			mutexList.add(entry.getValue());
		}
		Set<String> daySet = new HashSet<String>();
		for(ChargedayVO vo : mutexList) {
			daySet.add(vo.getDay());
		}
		return daySet;
	}

	public String getItemcode() {
		return itemcode;
	}

	public List<ChargedetailVO> getMutexList() {
		return mutexList;
	}

	public List<ChargedetailVO> getMutexOffsetList() {
		return mutexOffsetList;
	}
}
