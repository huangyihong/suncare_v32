package com.ai.modules.review.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class StatisticRule {
    private String code;
    private String name;
    private Set<String> ruleScopeSet;
    private Set<String> visitidset;
    private Double sumFee;
    private Integer ruleScopeCount;
    private Integer visitidCount;

    public StatisticRule(String code, String name) {
        this.code = code;
        this.name = name;
        ruleScopeSet = new HashSet<>();
        visitidset = new HashSet<>();
        sumFee = 0.0;
    }

    public void addRuleScope(List<String> list) {
        ruleScopeSet.addAll(list);
    }

    public void addVisitid(String id) {
        visitidset.add(id);
    }

    public void addFee(Double fee) {
        sumFee += fee;
    }

    public void toCount() {
        this.ruleScopeCount = ruleScopeSet.size();
        this.visitidCount = visitidset.size();
    }

}
