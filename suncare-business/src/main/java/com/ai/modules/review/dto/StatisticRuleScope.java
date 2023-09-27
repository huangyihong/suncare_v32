package com.ai.modules.review.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class StatisticRuleScope {
    private String code;
    private Set<String> clientidset;
    private Set<String> caseIdset;
    private Set<String> visitidset;
    private Double sumFee;
    private Integer clientidCount;
    private Integer caseIdCount;
    private Integer visitidCount;

    public StatisticRuleScope(String code) {
        this.code = code;
        clientidset = new HashSet<>();
        caseIdset = new HashSet<>();
        visitidset = new HashSet<>();
        sumFee = 0.0;
    }

    public void addClientid(String id) {
        clientidset.add(id);
    }

    public void addCaseId(String id) {
        caseIdset.add(id);
    }

    public void addVisitid(String id) {
        visitidset.add(id);
    }

    public void addFee(Double fee) {
        sumFee += fee;
    }

    public void toCount() {
        this.clientidCount = clientidset.size();
        this.caseIdCount = caseIdset.size();
        this.visitidCount = visitidset.size();
    }

}
