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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrDocument;

public class RepeatComputeVO extends ComputeVO {
	//同一剂型出现重复用药集合
	private List<Set<String>> repeatList;
	
	private Map<String, SolrDocument> documentMap;
	
	public RepeatComputeVO(String visitid, List<Set<String>> repeatList) {
		super(visitid);
		this.repeatList = repeatList;
	}
	
	public void addDocument(SolrDocument document) {
		if(documentMap==null) {
			documentMap = new HashMap<String, SolrDocument>();
		}
		if(document!=null) {
			documentMap.put(document.get("ITEMCODE").toString(), document);
		}
	}
	
	public boolean existsDocument(String itemcode) {
		if(documentMap==null) {
			return false;
		}
		return documentMap.containsKey(itemcode);
	}
	
	public SolrDocument getDocument(String itemcode) {
		if(documentMap==null) {
			return null;
		}
		return documentMap.get(itemcode);
	}
	
	@Override
	public void add(ChargedetailVO vo) {
		//药品明细是否出现在重复名单里
		boolean need = false;
		for(Set<String> set : repeatList) {
			if(set.contains(vo.getItemcode())) {
				need = true;
				break;
			}
		}
		if(need) {
			if(vo.getAmount().compareTo(BigDecimal.ZERO)>=0) {
				detailList.add(vo);
			} else {
				detailOffsetList.add(vo);
			}
		}
	}
		
	/**
	 * 
	 * 功能描述：计算重复用药
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月21日 上午10:56:27</p>
	 *
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public List<RepeatItemVO> computeRepeat() {
		List<RepeatItemVO> result = new ArrayList<RepeatItemVO>();
		//冲销后的项目列表
		List<ChargedayVO> detailList = this.offsetDetail();
		for(Set<String> include : repeatList) {
			//过滤掉不是重复用药的名单
			List<ChargedayVO> dataList = detailList.stream().filter(bean->include.contains(bean.getItemcode())).collect(Collectors.toList());
			//按日期分组
            Map<String, List<ChargedayVO>> dayListMap = dataList.stream().collect(Collectors.groupingBy(item-> item.getDay()));
            for(Map.Entry<String, List<ChargedayVO>> entry : dayListMap.entrySet()) {
            	if(entry.getValue().size()>1) {
            		//存在重复用药
            		dataList = entry.getValue();
            		//按基金支出金额从大到小排序
            		dataList.sort(new Comparator<ChargedayVO>() {
            			@Override
            			public int compare(ChargedayVO o1, ChargedayVO o2) {
            				return o2.getFundConver().compareTo(o1.getFundConver());
            			}			
            		});
            		//基金支出金额最大的药品作为白名单
            		ChargedayVO main = dataList.get(0);
            		for(int i=1,len=dataList.size(); i<len; i++) {
            			ChargedayVO vo = dataList.get(i);
            			RepeatItemVO item = new RepeatItemVO();
            			item.setMainCode(main.getItemcode());
            			item.setMainName(main.getItemname());
            			item.setItemcode(vo.getItemcode());
            			item.setItemname(vo.getItemname());
            			item.setAmount(vo.getAmount());
            			item.setFee(vo.getFee());
            			item.setFundConver(vo.getFundConver());
            			item.setDay(vo.getDay());
            			item.setUnitPrice(vo.getUnitPrice());
            			item.setItemnameSrc(vo.getItemnameSrc());
            			item.setItemnameSrcSet(vo.getItemnameSrcSet());
            			result.add(item);
            		}
            	}
            }
		}
		return result;		
	}

	public List<Set<String>> getRepeatList() {
		return repeatList;
	}
}
