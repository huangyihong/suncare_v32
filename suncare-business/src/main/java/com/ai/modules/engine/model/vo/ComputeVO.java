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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeVO {
	protected String visitid;
	//项目明细
	protected List<ChargedetailVO> detailList = new ArrayList<ChargedetailVO>();
	//项目冲销明细
	protected List<ChargedetailVO> detailOffsetList = new ArrayList<ChargedetailVO>();
	
	public ComputeVO(String visitid) {
		this.visitid = visitid;
	}
	
	public void add(ChargedetailVO vo) {
		if(vo.getAmount().compareTo(BigDecimal.ZERO)>=0) {
			detailList.add(vo);
		} else {
			detailOffsetList.add(vo);
		}
	}
	
	protected void put(Map<String, ChargedayVO> detailMap, ChargedetailVO vo) {
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
	 * 功能描述：冲销后的项目收费明细
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月19日 下午2:32:34</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public List<ChargedayVO> offsetDetail() {
		offset(this.detailList, this.detailOffsetList);
		
		Map<String, ChargedayVO> detailMap = new HashMap<String, ChargedayVO>();
		for(ChargedetailVO vo : detailList) {
			put(detailMap, vo);
		}
		List<ChargedayVO> detailList = new ArrayList<ChargedayVO>();
		for(Map.Entry<String, ChargedayVO> entry : detailMap.entrySet()) {
			detailList.add(entry.getValue());
		}
		//按收费日期排序
		sortByDay(detailList);
		return detailList;
	}
	
	/**
	 * 
	 * 功能描述：项目明细冲销计算
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月19日 上午10:08:39</p>
	 *
	 * @param list 项目收费正数明细
	 * @param offsetList 项目收费负数明细
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected void offset(List<ChargedetailVO> list, List<ChargedetailVO> offsetList) {
		//按收费日期排序
		sortByChargedate(list);		
		if(offsetList.size()>0) {
			//主项目明细冲销计算
			sortByChargedate(offsetList);
			for(ChargedetailVO slave : offsetList) {
				for(int i=0, len=list.size(); i<len; i++) {
					int index = i;
					ChargedetailVO master1 = list.get(i);
					ChargedetailVO master2 = null;
					if(i+1<len) {
						master2 = list.get(i+1);
					}
					if(master2!=null) {
						if(master1.getPrescripttime().compareTo(slave.getPrescripttime())<0
								&& slave.getPrescripttime().compareTo(master2.getPrescripttime())<0) {
							offset(list, index, slave);
							break;
						}
					} else {
						offset(list, index, slave);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * 功能描述：使用递归算法冲销
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月19日 上午10:51:20</p>
	 *
	 * @param list
	 * @param index
	 * @param offset
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private void offset(List<ChargedetailVO> list, int index, ChargedetailVO offset) {
		ChargedetailVO master = list.get(index);
		offset = master.sub(offset);
		if(offset.getAmount().compareTo(BigDecimal.ZERO)<0 && index>0) {
			offset(list, --index, offset);
		}
	}
	
	/**
	 * 
	 * 功能描述：按收费日期（到秒）排序
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月19日 上午10:00:03</p>
	 *
	 * @param list
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected void sortByChargedate(List<ChargedetailVO> list) {
		list.sort(new Comparator<ChargedetailVO>() {
			@Override
			public int compare(ChargedetailVO o1, ChargedetailVO o2) {
				return o1.getPrescripttime().compareTo(o2.getPrescripttime());
			}			
		});
	}
	
	/**
	 * 
	 * 功能描述：按收费日期（到天）排序
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月19日 上午10:00:03</p>
	 *
	 * @param list
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected void sortByDay(List<ChargedayVO> list) {
		list.sort(new Comparator<ChargedayVO>() {
			@Override
			public int compare(ChargedayVO o1, ChargedayVO o2) {
				return o1.getDay().compareTo(o2.getDay());
			}			
		});
	}
	
	public String getVisitid() {
		return visitid;
	}

	public void setVisitid(String visitid) {
		this.visitid = visitid;
	}

	public List<ChargedetailVO> getDetailList() {
		return detailList;
	}

	public List<ChargedetailVO> getDetailOffsetList() {
		return detailOffsetList;
	}
}
