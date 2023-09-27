/**
 * OverFrequencyFeeHandle.java	  V1.0   2020年10月29日 上午10:56:37
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.fee;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;

import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.vo.ChargebxVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalDrugRule;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 功能描述：超频次/数量违规金额计算
 *
 * @author  zhangly
 * Date: 2020年10月29日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
@Slf4j
public class OverFrequencyFeeHandle extends AbsFeeHandle {

	public OverFrequencyFeeHandle(MedicalDrugRule rule, SolrDocument document) {
		super(rule, document);
	}

	@Override
	public FeeResult compulate() {
		FeeResult result = null;
		String period = rule.getPeriod();
		if(StringUtils.isNotBlank(period)) {
			//限制频次
			BigDecimal limit = new BigDecimal(rule.getFrequency());
			String compare = rule.getCompare();
			if("<".equals(compare)) {
				limit = limit.subtract(BigDecimal.ONE);
			}
			BigDecimal frequency = limit;
			result = this.compulate(period, frequency);
		}
				
		//第二个限定频次条件
		period = rule.getTwoFrequency();
		if(StringUtils.isNotBlank(period)) {
			//限制频次
			BigDecimal limit = new BigDecimal(rule.getTwoFrequency());
			String compare = rule.getCompare();
			if("<".equals(compare)) {
				limit = limit.subtract(BigDecimal.ONE);
			}
			BigDecimal frequency = limit;
			FeeResult two = this.compulate(period, frequency);
			if(result==null && two!=null) {
				result = two;
			} else if(two!=null && result.getActionMoney().compareTo(two.getActionMoney())>0) {
				result = two;
			}
		}
		return result;
	}
	
	private FeeResult compulate(String period, BigDecimal frequency) {
		FeeResult result = new FeeResult();
		BigDecimal money = BigDecimal.ZERO;
		if("1".equals(period) || "6".equals(period)) {
			//报销比例
			BigDecimal ratio = BigDecimal.ZERO;
			if(document.get("SELFPAY_PROP_MIN")!=null) {
				ratio = new BigDecimal(document.get("SELFPAY_PROP_MIN").toString());
				ratio = BigDecimal.ONE.subtract(ratio);
			}			
			//项目使用的次数
			BigDecimal qty = new BigDecimal(document.get("ITEM_QTY").toString());
			result.setCnt(qty);
			//项目总费用
			BigDecimal amt = new BigDecimal(document.get("ITEM_AMT").toString());
			//项目单价
			BigDecimal avg = BigDecimal.ZERO;
			if(document.get("ITEMPRICE_MAX")!=null) {
				avg = new BigDecimal(document.get("ITEMPRICE_MAX").toString());
			}
			
			if("1".equals(period)) {
				//一次就诊，违规金额=(一次就诊项目实际总数量-限制频次)*单价*报销比例
				money = avg.multiply(qty.subtract(frequency));
				result.setMoney(money);
				money = money.multiply(ratio);
				result.setOutCnt(qty.subtract(frequency));
			} else {
				//日均次，违规金额=(一次就诊ID项目实际总数量-限制频次*住院天数)*单价*报销比例
				BigDecimal day = BigDecimal.ONE;
				if(document.get("ZY_DAYS_CALCULATE")!=null) {
					day = new BigDecimal(document.get("ZY_DAYS_CALCULATE").toString());
				}
				//超出使用数量=一次就诊ID项目实际总数量-限制频次*住院天数
				BigDecimal cnt = frequency.multiply(day);
				cnt = qty.subtract(cnt);
				money = avg.multiply(cnt);
				result.setMoney(money);
				money = money.multiply(ratio);
				if(money.compareTo(BigDecimal.ZERO)<0) {
					money = BigDecimal.ZERO;
				}
				result.setOutCnt(cnt);
			}			
		} else {
			List<String> conditionList = new ArrayList<String>();
			String visitid = document.get("VISITID").toString();
			if("day".equals(period)) {
				//日
				EngineMapping mapping = new EngineMapping("MAPPER_DWS_PATIENT_CHARGEITEM_SUM_D", "DWSID", "id");
				SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
				conditionList.add(plugin.parse()+"VISITID:"+visitid);
				conditionList.add("DURATIONTYPE:日");
			} else if("month".equals(period)) {
				//月
				EngineMapping mapping = new EngineMapping("MAPPER_DWS_PATIENT_CHARGEITEM_SUM_M", "DWSID", "id");
				SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
				conditionList.add(plugin.parse()+"VISITID:"+visitid);
				conditionList.add("DURATIONTYPE:月");
			} else if("week".equals(period)) {
				//周
				EngineMapping mapping = new EngineMapping("MAPPER_DWS_PATIENT_CHARGEITEM_SUM_W", "DWSID", "id");
				SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
				conditionList.add(plugin.parse()+"VISITID:"+visitid);
				conditionList.add("DURATIONTYPE:周");
			} else if("quarter".equals(period)
					|| "3month".equals(period)) {
				//季
				EngineMapping mapping = new EngineMapping("MAPPER_DWS_PATIENT_CHARGEITEM_SUM_Q", "DWSID", "id");
				SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
				conditionList.add(plugin.parse()+"VISITID:"+visitid);
				conditionList.add("DURATIONTYPE:季");
			} else if("year".equals(period)) {
				//年
				EngineMapping mapping = new EngineMapping("MAPPER_DWS_PATIENT_CHARGEITEM_SUM_Y", "DWSID", "id");
				SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
				conditionList.add(plugin.parse()+"VISITID:"+visitid);
				conditionList.add("DURATIONTYPE:年");
			} else {
				conditionList.add("DURATIONTYPE:"+period);
			}
			conditionList.add("ITEMCODE:"+rule.getDrugCode());
			conditionList.add("DURATION:?*");
			conditionList.add("ITEM_QTY:{0 TO *}");
			SolrQuery solrQuery = new SolrQuery("*:*");
			// 设定查询字段
			solrQuery.addFilterQuery(conditionList.toArray(new String[0]));
			solrQuery.setStart(0);
			solrQuery.setRows(EngineUtil.MAX_ROW);    		
			solrQuery.addField("VISITID");
			solrQuery.addField("ITEMCODE");
			solrQuery.addField("ITEMNAME");
			solrQuery.addField("DURATION");
			solrQuery.addField("ITEM_QTY");
			solrQuery.addField("ITEM_TOTAL_AMT");
			List<ChargebxVO> list = new ArrayList<ChargebxVO>();
			try {
				SolrUtil.exportDoc(solrQuery, "DWS_PATIENT_CHARGEITEM_SUM", (map, index) -> {
					//当天项目使用的次数
					BigDecimal itemQty = new BigDecimal(map.get("ITEM_QTY").toString());
					if(itemQty.compareTo(frequency)>0) {
						Object value = map.get("DURATION");
	    				String day = value.toString();
	    				String code = map.get("ITEMCODE").toString();
	    				ChargebxVO vo = new ChargebxVO();
	    				vo.setDay(day);
	    				vo.setItemcode(code);
	    				vo.setItemname(map.get("ITEMNAME").toString());
	    				vo.setAmount(itemQty);
	    				vo.setFee(new BigDecimal(map.get("ITEM_TOTAL_AMT").toString()));
	    				if(map.get("ITEMPRICE_MAX")!=null) {
	    					vo.setUnitPrice(new BigDecimal(map.get("ITEMPRICE_MAX").toString()));
	    				}
	    				if(map.get("SELFPAY_PROP_MIN")!=null) {
	    					BigDecimal ratio = new BigDecimal(map.get("SELFPAY_PROP_MIN").toString());
	    					vo.setRatio(BigDecimal.ONE.subtract(ratio));
	    				}
	    				list.add(vo);										
					}				
		        });
			} catch(Exception e) {
				log.error("", e);
			}
			//超频次，计算违规金额=(当日该患者ID该项目实际总数量-限制频次)*单价*报销比例
			int index = 0;
			for(ChargebxVO vo : list) {
				BigDecimal fee = BigDecimal.ZERO;
				fee = vo.getFee().divide(vo.getAmount(),4, BigDecimal.ROUND_HALF_UP);
				fee = fee.setScale(2, BigDecimal.ROUND_HALF_DOWN);
				fee = fee.multiply(vo.getAmount().subtract(frequency));
				BigDecimal actionFee = fee.multiply(vo.getRatio());
				if(index==0) {
					money = actionFee;
					result.setMoney(fee);
					result.setCnt(vo.getAmount());
					result.setOutCnt(vo.getAmount().subtract(frequency));
				} else {
					if(money.compareTo(actionFee)>0) {
						money = actionFee;
						result.setMoney(fee);
						result.setCnt(vo.getAmount());
						result.setOutCnt(vo.getAmount().subtract(frequency));
					}
				}
				index++;
			}
		}
		if(money!=null) {
			money = money.setScale(2, BigDecimal.ROUND_HALF_DOWN);
		}
		result.setActionMoney(money);
		return result;
	}
}
