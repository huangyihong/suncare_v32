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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.jeecg.common.util.DateUtils;

/**
 * 
 * 功能描述：医保药品超过最大持续使用时间
 *
 * @author  zhangly
 * Date: 2021年4月19日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class PayDurationComputeVO extends OffsetComputeVO {
	//限制时长
	private int limit;
	//时间单位
	private String timeUnit;
	
	public PayDurationComputeVO(String visitid, String itemcode, SolrDocument document, int limit, String timeUnit) {
		super(visitid, itemcode, document);
		this.limit = limit;
		this.timeUnit = timeUnit;
	}
	
	/**
	 * 
	 * 功能描述：计算超出数量及金额
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月21日 上午10:56:27</p>
	 *
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public PayDurationVO compute() {
		//冲销后的主项目列表
		List<ChargedayVO> detailList = this.offsetDetail();
		if(detailList.size()<=limit) {
			return null;
		}
		String format = "yyyy-MM-dd";		
		Map<String, ChargedayVO> detailMap = new LinkedHashMap<String, ChargedayVO>();
		for(ChargedayVO detail : detailList) {		
			String time = detail.getDay();
			if("month".equals(timeUnit)) {
				time = DateUtils.dateformat(time, format);
			} else if("year".equals(timeUnit)) {
				time = DateUtils.dateformat(time, format);
			}
			if(!detailMap.containsKey(time)) {
				detailMap.put(time, detail);
			} else {
				ChargedayVO record = detailMap.get(time);
				BigDecimal b = record.getAmount();
				b = b.add(detail.getAmount());
				record.setAmount(b);
				b = record.getFee();
				b = b.add(detail.getFee());
				record.setFee(b);
				b = record.getFundConver();
				b = b.add(detail.getFundConver());
				record.setFundConver(b);
			}			
		}
		if("day".equals(timeUnit)) {
			//+1是为了减少假阳性
			limit = limit + 1;
		}
		if(detailMap.size()<=limit) {
			return null;
		}
		PayDurationVO result = new PayDurationVO();
		result.setPayDay(detailList.size());
		result.setItemcode(itemcode);
		result.setItemname(document.get("ITEMNAME").toString());
		int index = 0;
		for(Map.Entry<String, ChargedayVO> entry : detailMap.entrySet()) {
			index++;
			if(index<=limit) {
				continue;
			}
			BigDecimal b = result.getAmount();
			ChargedayVO detail = entry.getValue();
			b = b.add(detail.getAmount());
			result.setAmount(b);
			b = result.getFee();
			b = b.add(detail.getFee());
			result.setFee(b);
			b = result.getFundConver();
			b = b.add(detail.getFundConver());
			result.setFundConver(b);
			result.addItemnameSrc(detail.getItemnameSrcSet());
		}		
		return result;
	}
}
