package com.ai.modules.review.dto;

import lombok.Data;
import org.apache.solr.common.SolrDocument;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class StatisticClient {
    private String clientid;
    private Integer ruleScopeCount;
    private Integer visitidCount;
    private Integer caseIdCount;
    private Double sumFee;

    public StatisticClient(String clientid, List<SolrDocument> list) {
        Set<String> ruleScopeSet = new HashSet<>();
        Set<String> visitidSet = new HashSet<>();
        Set<String> caseIdSet = new HashSet<>();
        Double sumFee = 0.0;
        for (SolrDocument doc : list) {
            ruleScopeSet.addAll(doc.getFieldValues("RULE_SCOPE").stream()
                    .map(Object::toString).collect(Collectors.toList()));
            visitidSet.add((String) doc.getFieldValue("VISITID"));
            caseIdSet.add((String) doc.getFieldValue("CASE_ID"));
            sumFee += Double.parseDouble(doc.getFieldValue("ACTION_MONEY").toString());
        }

        this.clientid = clientid;
        this.ruleScopeCount = ruleScopeSet.size();
        this.ruleScopeCount = ruleScopeSet.size();
        this.visitidCount = visitidSet.size();
        this.caseIdCount = caseIdSet.size();
        this.sumFee = sumFee;

    }

}
