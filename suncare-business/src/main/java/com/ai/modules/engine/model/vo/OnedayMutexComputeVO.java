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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrDocument;

public class OnedayMutexComputeVO {
	private String visitid;
	//主体项目编码
	private String itemcode;
	//主体项目明细
	private List<ChargedetailVO> detailList = new ArrayList<ChargedetailVO>();
	//主体项目冲销明细
	private List<ChargedetailVO> detailOffsetList = new ArrayList<ChargedetailVO>();
	//互斥项目明细
	private List<ChargedetailVO> mutexList = new ArrayList<ChargedetailVO>();
	//互斥项目冲销明细
	private List<ChargedetailVO> mutexOffsetList = new ArrayList<ChargedetailVO>();
	private List<SolrDocument> documentList = new ArrayList<SolrDocument>();
	
	public OnedayMutexComputeVO(String visitid, String itemcode) {
		this.visitid = visitid;
		this.itemcode = itemcode;
	}
	
	public void addSolrDocument(SolrDocument doc) {
		documentList.add(doc);
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
	 * 功能描述：冲销后的互斥项目收费明细
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月19日 下午2:32:35</p>
	 *
	 * @returnS
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private List<ChargedayVO> offsetMutex() {
		OffsetUtil.offset(this.mutexList, this.mutexOffsetList);
		
		Map<String, ChargedayVO> mutexMap = new HashMap<String, ChargedayVO>();
		for(ChargedetailVO vo : mutexList) {
			put(mutexMap, vo);
		}
		List<ChargedayVO> mutexList = new ArrayList<ChargedayVO>();
		for(Map.Entry<String, ChargedayVO> entry : mutexMap.entrySet()) {
			mutexList.add(entry.getValue());
		}
		//按收费日期排序
		OffsetUtil.sortByDay(mutexList);
		return mutexList;
	}
	
	/**
	 * 
	 * 功能描述：计算互斥项目（互斥项目作为违规项目输出）
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月21日 上午10:56:27</p>
	 *
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public List<SolrDocument> computeMutex(String dayField) {
		//冲销后的主项目列表
		List<ChargedayVO> detailList = this.offsetDetail();
		//冲销后的互斥项目列表
		List<ChargedayVO> mutexList = this.offsetMutex();
		
		for(SolrDocument doc : documentList) {
			String code = doc.get("ITEMCODE").toString();
			List<ChargedayVO> filterDetailList = detailList.stream().filter(s->code.equals(s.getItemcode())).collect(Collectors.toList());
			List<ChargedayVO> filterMutexList = mutexList.stream().filter(s->code.equals(s.getItemcode())).collect(Collectors.toList());
			boolean exists = false;			
			String time = doc.get(dayField).toString();
			for(ChargedayVO mutex : filterMutexList) {				
				String day = mutex.getDay();
				if(time.compareTo(day)<0) {
					break;
				}
				if(time.compareTo(day)==0) {
					for(ChargedayVO detail :filterDetailList) {
						if(!code.equals(mutex.getItemcode())) {
							continue;
						}
						if(time.compareTo(detail.getDay())<0) {
							break;
						}
						if(day.compareTo(detail.getDay())==0) {
							if(mutex.getAmount().compareTo(BigDecimal.ZERO)>0 
									&& detail.getAmount().compareTo(BigDecimal.ZERO)>0) {
								//存在某天发生互斥
								exists = true;
								doc.put("ITEM_AMT", mutex.getAmount());
								//基金支出金额
						        doc.put("MIN_ACTION_MONEY", mutex.getFundConver());
						        doc.put("MAX_ACTION_MONEY", mutex.getFundConver());
						        //收费项目费用
						        doc.put("MIN_MONEY", mutex.getFee());
						        doc.put("MAX_MONEY", mutex.getFee());
							}
						}
					}
				}
			}
			if(!exists) {
				doc.put("ITEM_AMT", 0);
				//基金支出金额
		        doc.put("MIN_ACTION_MONEY", 0);
		        doc.put("MAX_ACTION_MONEY", 0);
		        //收费项目费用
		        doc.put("MIN_MONEY", 0);
		        doc.put("MAX_MONEY", 0);
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

	public String getItemcode() {
		return itemcode;
	}

	public List<ChargedetailVO> getDetailList() {
		return detailList;
	}

	public List<ChargedetailVO> getDetailOffsetList() {
		return detailOffsetList;
	}

	public List<ChargedetailVO> getMutexList() {
		return mutexList;
	}

	public List<ChargedetailVO> getMutexOffsetList() {
		return mutexOffsetList;
	}

	public List<SolrDocument> getDocumentList() {
		return documentList;
	}
}
