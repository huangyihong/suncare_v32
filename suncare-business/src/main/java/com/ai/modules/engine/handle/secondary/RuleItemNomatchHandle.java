/**
 * SecondLineDrugHandle.java	  V1.0   2020年12月4日 上午10:05:13
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.handle.secondary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.jeecg.common.util.DateUtils;

import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.model.EngineMapping;
import com.ai.modules.engine.model.rule.AbsEngineParamRule;
import com.ai.modules.engine.model.rule.EngineParamGrpRule;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 *
 * 功能描述：项目与既往项目不符规则二次处理
 *
 * @author  zhangly
 * Date: 2021年3月23日
 * Copyright (c) 2021 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class RuleItemNomatchHandle extends AbsRuleSecondHandle {

	public RuleItemNomatchHandle(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule, List<MedicalRuleConditionSet> ruleConditionList,
			Boolean trail) {
		super(task, batch, rule, ruleConditionList, trail);
	}

	@Override
	public void execute() throws Exception {
		String batchId = batch.getBatchId();
		String collection = trail ? EngineUtil.MEDICAL_TRAIL_DRUG_ACTION : EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION;
		boolean slave = false;
		List<MedicalRuleConditionSet> judgeList = ruleConditionList.stream().filter(s->"judge".equals(s.getType())).collect(Collectors.toList());
		List<String> treatWheres = new ArrayList<String>();//项目组查询条件
		for(MedicalRuleConditionSet bean : judgeList) {
			String condiType = bean.getField();
			if(AbsRuleParser.RULE_CONDI_HISGROUPS.equals(condiType)) {
				AbsEngineParamRule paramRule = new EngineParamGrpRule("STD_TREATGROUP", "TREATGROUP_CODE", bean.getExt1());
				treatWheres.add(paramRule.where());
			}
		}

		String[] array = StringUtils.split(rule.getItemCodes(), ",");
		for (String itemCode : array) {
			Map<String, String> unindicationDayMap = this.unindicationDayMap(collection, slave, itemCode, treatWheres);
			if(unindicationDayMap.size()>0) {
				//排除病人发生日期之前的病例
				List<String> excludeList = new ArrayList<String>();
				List<String> conditionList = new ArrayList<String>();
				conditionList.add("ITEMCODE:" + itemCode);
				conditionList.add("RULE_ID:" + rule.getRuleId());
				conditionList.add("BATCH_ID:" + batchId);
				SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
					String clientid = doc.get("CLIENTID").toString();
					String day = doc.get("VISITDATE").toString();
					day = DateUtils.dateformat(day, "yyyy-MM-dd");
					if(unindicationDayMap.containsKey(clientid)) {
						String temp = unindicationDayMap.get(clientid);
						if(day.compareTo(temp)<=0) {
							//病例项目发生日期之前
							excludeList.add(doc.get("id").toString());
						}
					} else {
						excludeList.add(doc.get("id").toString());
					}
				});
				if(excludeList.size()>0) {
					int pageSize = 1000;
		    		int pageNum = (excludeList.size() + pageSize - 1) / pageSize;
		    		//数据分割
		    		List<List<String>> mglist = new ArrayList<>();
		    	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
		    	    	mglist.add(excludeList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
		    	    });
		    	    for(List<String> sublist : mglist) {
		    	    	this.deleteSolr(collection, slave, itemCode, sublist);
		    	    }
					excludeList.clear();
				}
			}
		}
	}

	/**
	 *
	 * 功能描述：病人既往项目出现日期集合
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月25日 下午5:51:29</p>
	 *
	 * @param collection
	 * @param slave
	 * @param itemCode
	 * @param treatWheres
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private Map<String, String> unindicationDayMap(String collection, boolean slave,
			String itemCode, List<String> treatWheres) throws Exception {
		Map<String, String> unindicationDayMap = new HashMap<String, String>();
		List<String> conditionList = new ArrayList<String>();
		String where = "_query_:\"%sITEMCODE:%s AND RULE_ID:%s AND BATCH_ID:%s\"";
		EngineMapping mapping = new EngineMapping(collection, "CLIENTID", "CLIENTID");
		SolrJoinParserPlugin plugin = SolrJoinParserPlugin.build(mapping);
		where = String.format(where, plugin.parse(), itemCode, rule.getRuleId(), batch.getBatchId());
		conditionList.add(where);
		//项目组与组之间默认or关系
		conditionList.add(StringUtils.join(treatWheres, " OR "));
		/*SolrUtil.exportDocByPager(conditionList, EngineUtil.DWB_MASTER_INFO, slave, (doc, index) -> {
			String clientid = doc.get("CLIENTID").toString();
			String day = doc.get("VISITDATE").toString();
			day = DateUtils.dateformat(day, "yyyy-MM-dd");
			if(!unindicationDayMap.containsKey(clientid)) {
				unindicationDayMap.put(clientid, day);
			} else {
				String temp = unindicationDayMap.get(clientid);
				unindicationDayMap.put(clientid, minDay(temp, day));
			}
		});*/
		//使用分组聚合函数统计
		SolrQuery query = new SolrQuery();
		// 设定查询字段
		String q = "*:*";
		query.add("q", q);
		query.addFilterQuery(conditionList.toArray(new String[0]));
		query.setRows(0);
		JSONObject facetJsonMap = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", "terms");
		jsonObject.put("limit", -1);
		jsonObject.put("field", "CLIENTID");
		// 使用聚合函数统计值，病人病例的最小日期
		JSONObject subFacteMap = new JSONObject();
		subFacteMap.put("day", "min(VISITDATE)");
		jsonObject.put("facet", subFacteMap);
		facetJsonMap.put("categories", jsonObject);

		String facetJson = JSON.toJSONString(facetJsonMap);
		logger.info("======json.facet:"+facetJson);

		query.set("json.facet", facetJson);
		query.setFacet(true);
		query.setFacetLimit(-1);
		QueryResponse response = SolrUtil.call(query, EngineUtil.DWB_MASTER_INFO);
		NamedList<Object> responseList = response.getResponse();
		responseList = (NamedList<Object>)responseList.get("facets");
		responseList = (NamedList<Object>)responseList.get("categories");
		List<NamedList<Object>> buckets = (List<NamedList<Object>>)responseList.get("buckets");
		if(buckets!=null) {
			for(NamedList<Object> map : buckets) {
				unindicationDayMap.put(map.get("val").toString(), map.get("day").toString());
			}
		}
		return unindicationDayMap;
	}

	private void deleteSolr(String collection, boolean slave, String itemCode, List<String> excludeList) {
		String query = "ITEMCODE:%s AND RULE_ID:%s AND BATCH_ID:%s AND id:(%s)";
		query = String.format(query, itemCode, rule.getRuleId(), batch.getBatchId(), StringUtils.join(excludeList, " OR "));
		try {
			SolrUtil.delete(collection, query, slave);
		} catch (Exception e) {
		}
	}

	/**
	 *
	 * 功能描述：取两天中的最小值
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年3月24日 上午10:55:39</p>
	 *
	 * @param day1
	 * @param day2
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private String minDay(String day1, String day2) {
		if(day1.compareTo(day2)>0) {
			return day2;
		}
		return day1;
	}
}
