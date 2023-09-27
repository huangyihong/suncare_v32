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
import java.util.List;

import org.apache.solr.common.SolrDocument;

/**
 * 
 * 功能描述：用药时限
 *
 * @author  zhangly
 * Date: 2021年4月19日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class DosageComputeVO extends OffsetComputeVO {
	//超用量
	private ChargeVO dosage;
	//限制数量
	private int limit;
	
	public DosageComputeVO(String visitid, String itemcode, SolrDocument document, int limit) {
		super(visitid, itemcode, document);
		this.limit = limit;
	}
	
	/**
	 * 
	 * 功能描述：计算超用量数量及金额
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月21日 上午10:56:27</p>
	 *
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public void compute() {
		//冲销后的主项目列表
		List<ChargedayVO> detailList = this.offsetDetail();
		if(detailList.size()<=limit) {
			return; 
		}
		ChargeVO result = new ChargeVO();
		result.setItemcode(itemcode);
		result.setItemname(document.get("ITEMNAME").toString());
		int index = 0;
		for(ChargedayVO detail : detailList) {
			index++;
			if(index<=limit) {
				continue;
			}
			BigDecimal b = result.getAmount();
			b = b.add(detail.getAmount());
			result.setAmount(b);
			b = result.getFee();
			b = b.add(detail.getFee());
			result.setFee(b);
			b = result.getFundConver();
			b = b.add(detail.getFundConver());
			result.setFundConver(b);
		}	
		dosage = result;
	}
	
	public ChargeVO getDosage() {
		return dosage;
	}
}
