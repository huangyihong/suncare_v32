package com.ai.modules.engine.util.stream;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/4/23 10
 * @Description:
 */
@Data
public class FacetConstruct {
    private String collection;
    private List<String> qs;
    private int rows;
    private int overrequest;
    private List<String> buckets;
    private List<String> bucketSorts;
    private List<String> stats;

    public FacetConstruct(String collection){
        this.collection = collection;
        this.qs = new ArrayList<>();
        this.stats = new ArrayList<>();
        this.buckets = new ArrayList<>();
        this.bucketSorts = new ArrayList<>();

        this.rows = -1;
        this.overrequest = Integer.MAX_VALUE;
        this.stats.add("count(*)");

    }

    public String toExpression(){
        StringBuilder sb = new StringBuilder("facet(");
        sb.append(collection);
        sb.append(",q=\"");
        if(qs.size() == 0){
            sb.append("*:*");
        } else {
            sb.append(StringUtils.join(qs,","));
        }
        sb.append("\"");
        sb.append(",rows=").append(rows);
        sb.append(",buckets=\"").append(StringUtils.join(buckets,",")).append("\"");
        sb.append(",bucketSorts=\"");
        if(bucketSorts.size() == 0){
            bucketSorts.add(buckets.get(0) + " asc");
        }
        sb.append(StringUtils.join(bucketSorts,","));
        sb.append("\"");
        sb.append(",").append(StringUtils.join(stats,","));
        sb.append(")");
        // facet(MZ_CFXXB0,q="*:*",bucketSorts="UUID desc",count(*),rows=10,buckets="UUID")
        return sb.toString();
    }

    public void addQuery(String q){
        qs.add(q);
    }

    public void addQuery(String ...q){
        qs.addAll(Arrays.asList(q));
    }

    public void addBucket(String field){
        buckets.add(field);
    }
    public void addBucket(String ...field){
        buckets.addAll(Arrays.asList(field));
    }
    public void addSort(String sort){
        bucketSorts.add(sort);
    }
    public void addSort(String ...sort){
        bucketSorts.addAll(Arrays.asList(sort));
    }

    public void addStat(String stat){
        stats.add(stat);
    }
    public void addStat(String ...stat){
        stats.addAll(Arrays.asList(stat));
    }


}
