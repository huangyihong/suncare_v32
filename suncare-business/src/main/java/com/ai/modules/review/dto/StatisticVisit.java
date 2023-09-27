package com.ai.modules.review.dto;

import com.ai.modules.review.vo.ReviewSystemDrugViewVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.solr.common.SolrDocument;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class StatisticVisit extends ReviewSystemDrugViewVo {
    private Integer ruleScopeCount;
    private Double sumItemQty;
    private Double sumFee;

    public void setBaseInfo(List<SolrDocument> list) {
        Set<String> ruleScopeSet = new HashSet<>();
        Double sumItemQty = 0.0;
        Double sumFee = 0.0;

        for (SolrDocument doc : list) {
            ruleScopeSet.addAll(doc.getFieldValues("RULE_SCOPE").stream()
                    .map(Object::toString).collect(Collectors.toList()));
            sumItemQty += Double.parseDouble(doc.getFieldValue("ITEM_QTY").toString());
            sumFee += Double.parseDouble(doc.getFieldValue("ACTION_MONEY").toString());
        }
        this.ruleScopeCount = ruleScopeSet.size();
        this.sumItemQty = sumItemQty;
        this.sumFee = sumFee;


    }

}
