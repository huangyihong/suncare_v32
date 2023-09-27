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
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;

public class OverFreqComputeVO {
	private BigDecimal limit;
	private String visitid;
	private boolean solr;
	//主体项目明细
	private List<ChargedetailVO> detailList = new ArrayList<ChargedetailVO>();
	//主体项目冲销明细
	private List<ChargedetailVO> detailOffsetList = new ArrayList<ChargedetailVO>();
	//主体项目
	private List<SolrDocument> documentList = new ArrayList<SolrDocument>();
	
	public OverFreqComputeVO(BigDecimal limit, String visitid, boolean solr) {
		this.limit = limit;
		this.visitid = visitid;
		this.solr = solr;
	}
	
	public void addSolrDocument(SolrDocument doc) {
		documentList.add(doc);
	}
	
	public void add(ChargedetailVO vo) {
		if(vo.getAmount().compareTo(BigDecimal.ZERO)>=0) {
			detailList.add(vo);
		} else {
			detailOffsetList.add(vo);
		}
	}
	
	private void put(Map<String, ChargedayVO> detailMap, ChargedetailVO vo) {
		if(vo.getAmount().compareTo(BigDecimal.ZERO)<=0) {
			return;
		}
		String key = vo.getItemcode().concat("::").concat(vo.getDay());		
		if(!detailMap.containsKey(key)) {
			detailMap.put(key, vo);
		} else {
			ChargedayVO record = detailMap.get(key);
			record.merge(vo);
		}
	}
	
	/**
	 * 
	 * 功能描述：冲销后的主项目收费明细
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月19日 下午2:32:34</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private List<ChargedayVO> offsetDetail() {
		OffsetUtil.offset(this.detailList, this.detailOffsetList);
		
		Map<String, ChargedayVO> detailMap = new HashMap<String, ChargedayVO>();
		for(ChargedetailVO vo : detailList) {
			put(detailMap, vo);
		}
		List<ChargedayVO> detailList = new ArrayList<ChargedayVO>();
		for(Map.Entry<String, ChargedayVO> entry : detailMap.entrySet()) {
			detailList.add(entry.getValue());
		}
		//按收费日期排序
		OffsetUtil.sortByDay(detailList);
		return detailList;
	}
	
	/**
	 * 
	 * 功能描述：计算一日超频次
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年12月28日 下午4:51:27</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public List<SolrDocument> compute() {
		String dayField = "BREAK_RULE_TIME";
		if(!solr) {
			dayField = "CHARGEDATE";
		}
		//冲销后的主项目列表
		List<ChargedayVO> detailList = this.offsetDetail();		
		for(SolrDocument doc : documentList) {
			boolean exists = false;			
			String time = doc.get(dayField).toString();
			for(ChargedayVO detail : detailList) {				
				String day = detail.getDay();
				if(time.compareTo(day)<0) {
					break;
				}
				if(time.compareTo(day)==0) {
					if(detail.getAmount().compareTo(BigDecimal.ZERO)>0 
							&& detail.getAmount().compareTo(limit)>0) {
						//存在某天超频次
						exists = true;
						doc.put("ITEM_AMT", detail.getAmount());
						doc.put("AI_ITEM_CNT", detail.getAmount());
						doc.put("AI_OUT_CNT", detail.getAmount().subtract(limit));
						//计算超出金额
						BigDecimal fee = BigDecimal.ZERO;
						fee = detail.getFee().divide(detail.getAmount(),4, BigDecimal.ROUND_HALF_UP);
						fee = fee.setScale(2, BigDecimal.ROUND_HALF_DOWN);
						fee = fee.multiply(detail.getAmount().subtract(limit));
						BigDecimal actionFee = fee.multiply(detail.getRatio());
						actionFee = actionFee.setScale(2, BigDecimal.ROUND_HALF_DOWN);
				        doc.put("ARRAY_ACTION_MONEY", actionFee);
				        doc.put("ARRAY_MONEY", fee);
				        doc.put("MIN_MONEY", fee);
				        doc.put("MAX_MONEY", fee);
				        BigDecimal fundcover = BigDecimal.ZERO;
				        fundcover = detail.getFundConver().divide(detail.getAmount(),4, BigDecimal.ROUND_HALF_UP);
				        fundcover = fundcover.setScale(2, BigDecimal.ROUND_HALF_DOWN);
				        fundcover = fundcover.multiply(detail.getAmount().subtract(limit));				        
				        doc.put("MAX_ACTION_MONEY", fundcover);
				        if(solr) {
				        	doc.put("MIN_ACTION_MONEY", fundcover);
				        } else {
				        	doc.put("ACTION_MONEY", fundcover);
				        }
					}
				}
			}
			if(!exists) {
				doc.put("ITEM_AMT", 0);
				doc.put("AI_ITEM_CNT", 0);
				doc.put("AI_OUT_CNT", 0);
		        doc.put("ARRAY_ACTION_MONEY", null);
		        doc.put("ARRAY_MONEY", null);
			}
		}
		return documentList;
	}

	public String getVisitid() {
		return visitid;
	}

	public void setVisitid(String visitid) {
		this.visitid = visitid;
	}

	public List<SolrDocument> getDocumentList() {
		return documentList;
	}
}
