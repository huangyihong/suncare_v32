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

public class InteractionComputeVO {
	//主体项目
	private SolrDocument document;
	//互斥项目明细
	private List<InteractionItemVO> mutexItems = new ArrayList<InteractionItemVO>();
	//互斥项目中最小最大基金支出金额
	private BigDecimal[] foundArray;
	//互斥项目中最小最大项目金额
	private BigDecimal[] feeArray;
		
	public InteractionComputeVO(SolrDocument document) {
		this.document = document;
		//主项目费用
		BigDecimal fee = new BigDecimal(document.get("FUND_COVER").toString());
		foundArray = new BigDecimal[] {fee, fee};
		feeArray = new BigDecimal[] {fee, fee};
	}
	
	public void add(InteractionItemVO vo) {
		if(vo.getAmount().compareTo(BigDecimal.ZERO)>=0) {
			mutexItems.add(vo);
		}
	}
	
	/**
	 * 
	 * 功能描述：计算互斥项目中的最小、最大费用
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月19日 下午5:06:41</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public void computeMutexMinMaxFee() {		
		if(mutexItems==null) {
			return ;
		}
		//计算互斥项目中最小最大基金支出金额
		BigDecimal min = new BigDecimal(document.get("FUND_COVER").toString());
		BigDecimal max = new BigDecimal(document.get("FUND_COVER").toString());
		for(InteractionItemVO mutex : mutexItems) {
			if(min.compareTo(mutex.getFundConver())>0) {
				min = mutex.getFundConver();
			}
			if(max.compareTo(mutex.getFundConver())<0) {
				max = mutex.getFundConver();
			}
		}
		foundArray[0] = min;
		foundArray[1] = max;
		//计算互斥项目中最小最大基金支出项目金额
		min = new BigDecimal(document.get("ITEM_AMT").toString());
		max = new BigDecimal(document.get("ITEM_AMT").toString());
		for(InteractionItemVO mutex : mutexItems) {
			if(min.compareTo(mutex.getFee())>0) {
				min = mutex.getFee();
			}
			if(max.compareTo(mutex.getFee())<0) {
				max = mutex.getFee();
			}
		}
		feeArray[0] = min;
		feeArray[1] = max;
	}	
	
	public SolrDocument getDocument() {
		return document;
	}

	public List<InteractionItemVO> getMutexItems() {
		return mutexItems;
	}

	public BigDecimal[] getFoundArray() {
		return foundArray;
	}

	public BigDecimal[] getFeeArray() {
		return feeArray;
	}	
}
