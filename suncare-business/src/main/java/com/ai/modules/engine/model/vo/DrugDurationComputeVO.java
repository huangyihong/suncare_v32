/**
 * MutexCaseVO.java	  V1.0   2020年9月14日 上午10:11:42
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrDocument;

public class DrugDurationComputeVO {
	private String visitid;
	//主体项目编码
	private String itemcode;
	//主体项目
	private SolrDocument document;
	//超过时限项目明细
	private List<ChargeVO> detailList = new ArrayList<ChargeVO>();
	
	public DrugDurationComputeVO(String visitid, String itemcode, SolrDocument document) {
		this.visitid = visitid;
		this.itemcode = itemcode;
		this.document = document;
	}
	
	public void add(ChargeVO vo) {
		detailList.add(vo);
	}
	
	/**
	 * 
	 * 功能描述：汇总超时限用药明细
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月21日 上午10:56:27</p>
	 *
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public DrugDurationVO compute() {
		if(detailList.size()>0) {
			DrugDurationVO result = new DrugDurationVO();
			result.setItemcode(itemcode);
			result.setItemname(document.get("ITEMNAME").toString());
			for(ChargeVO vo : detailList) {
				BigDecimal b = result.getAmount();
				b = b.add(vo.getAmount());
				result.setAmount(b);
				b = result.getFee();
				b = b.add(vo.getFee());
				result.setFee(b);
				b = result.getFundConver();
				b = b.add(vo.getFundConver());
				result.setFundConver(b);
				result.addDay(vo.getDay());
				result.addItemnameSrc(vo.getItemnameSrc());
			}
			return result;
		}
		return null;
	}
	
	public String getVisitid() {
		return visitid;
	}

	public void setVisitid(String visitid) {
		this.visitid = visitid;
	}

	public String getItemcode() {
		return itemcode;
	}

	public SolrDocument getDocument() {
		return document;
	}

	public List<ChargeVO> getDetailList() {
		return detailList;
	}
}
