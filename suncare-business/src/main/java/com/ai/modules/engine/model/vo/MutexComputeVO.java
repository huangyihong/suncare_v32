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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrDocument;

public class MutexComputeVO {
	private String id;
	private String visitid;
	//主体项目编码
	private String itemcode;
	//主体项目
	private SolrDocument document;
	//主体项目明细
	private List<ChargedetailVO> detailList = new ArrayList<ChargedetailVO>();
	//主体项目冲销明细
	private List<ChargedetailVO> detailOffsetList = new ArrayList<ChargedetailVO>();
	//互斥项目明细
	private List<ChargedetailVO> mutexList = new ArrayList<ChargedetailVO>();
	//互斥项目冲销明细
	private List<ChargedetailVO> mutexOffsetList = new ArrayList<ChargedetailVO>();
	//一日互斥项目明细
	private Set<ItemVO> mutexItems;
	
	public MutexComputeVO(String id, String visitid, String itemcode, SolrDocument document) {
		this.id = id;
		this.visitid = visitid;
		this.itemcode = itemcode;
		this.document = document;
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
	 * <p>创建日期 ：2020年10月19日 下午2:32:34</p>
	 *
	 * @return
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
	 * 功能描述：计算互斥项目
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月21日 上午10:56:27</p>
	 *
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public void computeMutex() {
		//冲销后的主项目列表
		List<ChargedayVO> detailList = this.offsetDetail();
		//冲销后的互斥项目列表
		List<ChargedayVO> mutexList = this.offsetMutex();
		//一日互斥项目
		List<ChargedayVO> resultList = new ArrayList<ChargedayVO>();
		Map<String, ItemVO> mutexItemMap = new HashMap<String, ItemVO>();
		for(ChargedayVO detail : detailList) {
			String day = detail.getDay();
			for(ChargedayVO mutex :mutexList) {
				if(day.compareTo(mutex.getDay())==0) {
					if(detail.getAmount().compareTo(BigDecimal.ZERO)>0 && mutex.getAmount().compareTo(BigDecimal.ZERO)>0) {						
						ItemVO item = null;
						String key = mutex.getItemcode();
						if(!mutexItemMap.containsKey(key)) {
							item = new ItemVO(key, mutex.getItemname());
							mutexItemMap.put(key, item);
						} else {
							item = mutexItemMap.get(key);
						}
						item.addMutexCnt(detail.getAmount(), mutex.getAmount(), mutex.getDay());
						item.addMainMoney(detail.getFee(), detail.getFundConver());
						item.addMutexMoney(mutex.getFee(), mutex.getFundConver());
						item.computeUnitPrice(detail.getUnitPrice());
						item.computeMutexPrice(mutex.getUnitPrice());
					}
				}
			}			
		}
		if(mutexItemMap.size()>0) {
			if(mutexItems==null) {
				mutexItems = new HashSet<ItemVO>();							
			}
			for(Map.Entry<String, ItemVO> entry : mutexItemMap.entrySet()) {
				mutexItems.add(entry.getValue());
			}
		}		
	}
	
	public static void main(String[] args) throws Exception {
		MutexComputeVO computeVO = new MutexComputeVO("123", "visitid", "123", null);
		ChargedetailVO vo = new ChargedetailVO();
		vo.setPrescripttime("2020-09-13 10:12:40");
		vo.setDay("2020-09-13");
		vo.setItemcode("123");
		vo.setAmount(new BigDecimal(2));
		computeVO.add(vo);
		vo = new ChargedetailVO();
		vo.setPrescripttime("2020-09-09 11:02:10");
		vo.setDay("2020-09-09");
		vo.setItemcode("123");
		vo.setAmount(new BigDecimal(2));
		computeVO.add(vo);		
		vo = new ChargedetailVO();
		vo.setPrescripttime("2020-09-11 09:30:40");
		vo.setDay("2020-09-11");
		vo.setItemcode("123");
		vo.setAmount(new BigDecimal(1));
		computeVO.add(vo);
		vo = new ChargedetailVO();
		vo.setPrescripttime("2020-09-14 09:30:40");
		vo.setDay("2020-09-14");
		vo.setItemcode("123");
		vo.setAmount(new BigDecimal(-1));
		computeVO.add(vo);
		
		OffsetUtil.sortByChargedate(computeVO.getDetailList());
		OffsetUtil.sortByChargedate(computeVO.getDetailOffsetList());
		System.out.println(computeVO.getDetailOffsetList());
		System.out.println(computeVO.getDetailList());		
		computeVO.offsetDetail();
		System.out.println(computeVO.getDetailList());
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Set<ItemVO> getMutexItems() {
		return mutexItems;
	}
}
