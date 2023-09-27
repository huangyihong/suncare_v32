/**
 * EngineDwsServiceImpl.java	  V1.0   2020年5月16日 下午8:12:37
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.cases.node.TrailDwsNodeRuleHandle;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.model.dto.EngineCaseFlowDTO;
import com.ai.modules.engine.service.IEngineDwsService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.util.SolrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class EngineDwsServiceImpl implements IEngineDwsService {
	
	@Autowired
    private IApiDictService dictSV;

	@Override
	public IPage<SolrDocument> trial(IPage<SolrDocument> page, EngineCaseFlowDTO dto) throws Exception {
		if (StringUtils.isBlank(dto.getRules())) {
            throw new EngineBizException("未传递节点参数！");
        }
		//规则
		List<EngineNodeRuleGrp> grpWheres = this.parseNodeRule(dto.getRules());
        
        String master = grpWheres.get(0).getRuleList().get(0).getTableName().toUpperCase();
        TrailDwsNodeRuleHandle handle = new TrailDwsNodeRuleHandle(grpWheres).withMaster(master);
        
        SolrQuery query = new SolrQuery("*:*");
        // 设定返回字段
        Set<String> virtualSet = new HashSet<String>();
        for(int i=0, len=dto.getCols().length; i<len; i++) {
        	String field = dto.getCols()[i];
        	MedicalColConfig config = dictSV.queryMedicalColConfig(master, field);
        	if(config!=null && config.getColType()==2 && StringUtils.isNotBlank(config.getColValueExpressionSolr())) {
        		//虚拟字段
        		query.set(field, config.getColValueExpressionSolr());
        		query.addField(field+":$"+field);
        		virtualSet.add(field);
        	} else {
        		query.addField(field);
        	}
        }
        // 设定查询字段
        query.addFilterQuery(handle.where());
        query.setStart((int) page.offset());
        query.setRows((int) page.getSize());
        if(StringUtils.isNotBlank(dto.getColumn())) {
        	String[] columns = StringUtils.split(dto.getColumn(), ",");
        	String[] orders = StringUtils.split(dto.getOrder(), ",");
        	if(columns.length!=orders.length) {
        		throw new EngineBizException("参数排序字段与排序方式的个数不一致");
        	}
        	for(int i=0, len=columns.length; i<len; i++) {
        		String order = orders[i];
        		String column = columns[i];
        		if(virtualSet.contains(column)) {
        			column = "$".concat(column);
        		}
        		if("asc".equalsIgnoreCase(order)) {
        			query.addSort(column, ORDER.asc);
        		} else {
        			query.addSort(column, ORDER.desc);
        		}
        	}
        }
        QueryResponse queryResponse = SolrUtil.call(query, master);
        SolrDocumentList documents = queryResponse.getResults();
        //			MedicalCaseVO vo = SolrUtil.solrDocumentToPojo(doc, MedicalCaseVO.class, EngineUtil.FIELD_MAPPING);
        List<SolrDocument> result = new ArrayList<>(documents);
        page.setRecords(result);
        page.setTotal(documents.getNumFound());
        return page;
	}

	/**
	 * 
	 * 功能描述：解析dws规则节点
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年5月16日 下午8:30:50</p>
	 *
	 * @param rules
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	@Override
	public List<EngineNodeRuleGrp> parseNodeRule(String rules) {
        //规则列表
        List<EngineNodeRule> ruleList = JSON.parseArray(rules, EngineNodeRule.class);        
        for (EngineNodeRule rule : ruleList) {
            MedicalColConfig config = dictSV.queryMedicalColConfig(rule.getTableName(), rule.getColName());
            if (config != null) {
                rule.setColConfig(config);
            }
        }
        //规则按组号分组
        Map<Integer, List<EngineNodeRule>> grpRuleMap = ruleList.stream().collect(Collectors.groupingBy(EngineNodeRule::getGroupNo));
        //按组序号排序
        grpRuleMap = grpRuleMap.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        int index = 0;
        List<EngineNodeRuleGrp> grpWheres = new ArrayList<EngineNodeRuleGrp>();
        for (Map.Entry<Integer, List<EngineNodeRule>> entry : grpRuleMap.entrySet()) {
            List<EngineNodeRule> tempList = entry.getValue();
            //按组内规则排序
            tempList = tempList.stream().sorted(Comparator.comparing(EngineNodeRule::getOrderNo)).collect(Collectors.toList());
            EngineNodeRuleGrp grp = new EngineNodeRuleGrp();
            if (index == 0) {
                //组间第一组的逻辑运算符设置为null
                grp.setLogic(null);
            } else {
                grp.setLogic(tempList.get(0).getLogic());
            }
            grp.setRuleList(tempList);
            //组内第一个条件的逻辑运算符设置为null
            tempList.get(0).setLogic(null);
            grpWheres.add(grp);
            index++;
        }
        return grpWheres;
    }
}
