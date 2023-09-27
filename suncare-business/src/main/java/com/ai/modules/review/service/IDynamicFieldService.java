package com.ai.modules.review.service;

import com.ai.modules.review.dto.DynamicLinkProp;
import com.ai.modules.task.entity.TaskActionFieldCol;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @Auther: zhangpeng
 * @Date: 2021/3/23 15
 * @Description:
 */
public interface IDynamicFieldService {


    // 根据关联信息反查表
    void addAttrFromOther(List<SolrDocument> list,
                          DynamicLinkProp dynamicLinkProp) throws Exception;


    void saveExtFieldValue(SolrQuery solrQuery, String collection, Map<String, Set<String>> tabFieldMap, String[][] extFieldMap, Consumer<Integer> updateProcess) throws Exception;

    void saveBreakStateValue(String collection, SolrQuery solrQuery, Map<String, Set<String>> tabFieldMap,
                             List<TaskActionFieldCol> colList, String[] nodes, Consumer<Integer> updateProcess) throws Exception;

    // 获取分组统计反查字段
    Set<String> getFromOtherField(Map<String, Set<String>> tabFieldMap);

    // 分组统计反查
    void addGroupAttrFromOther(List<Map<String, Object>> resultList, Map<String, Set<String>> tabFieldMap) throws Exception;

    Map<String, SolrDocumentList> listDynamicResult(Map<String, Set<String>> tabFieldMap, SolrQuery solrQuery) throws Exception;

    DynamicLinkProp initLinkProp(Map<String, Set<String>> tabFieldMap) throws Exception;

    List<String> getSearchFqs(String dynamicSearch);
}
