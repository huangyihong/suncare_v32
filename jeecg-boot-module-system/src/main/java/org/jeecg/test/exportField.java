/**
 * EngineMain.java	  V1.0   2019年12月25日 下午5:45:50
 * <p>
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 * <p>
 * Modification history(By    Time    Reason):
 * <p>
 * Description:
 */

package org.jeecg.test;

import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.InputStreamResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.util.NamedList;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg", "com.ai"})
public class exportField {
    private static int MAX_ROW = 2000000;

    public static void main(String[] args) throws Exception {
        SolrInputDocument solrInputDocument = new SolrInputDocument();
//        solrInputDocument.setField("a", "111");
        solrInputDocument.addField("a", "111");
        for(Map.Entry<String, SolrInputField> entry: solrInputDocument.entrySet()){
            System.out.println(entry.getKey());

            SolrInputField inputField = entry.getValue();
           System.out.println(inputField.getName());
           System.out.println(inputField.getValue());
           System.out.println(inputField.getValues());
        }
    }


    private static void search2() throws Exception {

        SolrQuery solrQuery = new SolrQuery("*:*");

        solrQuery.setRows(MAX_ROW);
        solrQuery.setRequestHandler("/query");
        log.info("======solr query: " + URLDecoder.decode(solrQuery.toQueryString(), "UTF-8"));

        QueryRequest request = new QueryRequest( solrQuery );
        request.setResponseParser(new InputStreamResponseParser("json"));
        request.setMethod(SolrRequest.METHOD.POST);

        SolrClient solrClient = SolrUtil.getClient(EngineUtil.DWB_CHARGE_DETAIL);
        NamedList<Object> genericResponse = solrClient.request(request);
        InputStream inputStream = (InputStream)genericResponse.get("stream");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        final AtomicInteger count = new AtomicInteger(0);
        // 流式解析JSON文件
        JSONReader jsonReader = new JSONReader(bufferedReader);
        jsonReader.startObject();
        while (jsonReader.hasNext()) {
            String elem = jsonReader.readString();
            if ("responseHeader".equals(elem)) {
                jsonReader.startObject();
                while (jsonReader.hasNext()) {
                    String key = jsonReader.readString();
                    Object value = jsonReader.readObject();
                    //判断状态
                    if ("status".equalsIgnoreCase(key) && !value.toString().equals("0")) {
                        throw new Exception("查询SOLR失败!");
                    }
                }
                jsonReader.endObject();
            } else if ("response".equals(elem)) {
                jsonReader.startObject();
                // 跳过其他信息
                while (jsonReader.hasNext()) {
                    String key = jsonReader.readString();
                    if ("docs".equalsIgnoreCase(key)) {
                        //读取docs数组信息
                        jsonReader.startArray();
                        // 循环ARRAY
                        while (jsonReader.hasNext()) {
//                            log.info(jsonReader.readObject().toString());
                            System.out.print(count.getAndIncrement());
                        }
                        jsonReader.endArray();
                    } else {
                        jsonReader.readObject();
                    }
                }
                jsonReader.endObject();
            }
        }
        jsonReader.endObject();

        jsonReader.close();

    }

    private static void search() throws Exception {
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.addFilterQuery("HOSPLEVEL:3");
        solrQuery.addFilterQuery("VISITTYPE:住院");
        solrQuery.addFilterQuery("_query_:\"{!join fromIndex=DWB_CLIENT from=CLIENTID to=CLIENTID}SEX:男性\"");
        solrQuery.addFilterQuery("_query_:\"{!join fromIndex=DWB_CHARGE_DETAIL from=VISITID to=VISITID}ITEMNAME:*子宫*\"");

        SolrUtil.export(solrQuery, EngineUtil.DWB_MASTER_INFO, (map, index) -> {
            log.info(index + "--" + map.toString());
        });

        /*solrQuery.setRequestHandler("/export");
        final AtomicInteger count = new AtomicInteger(0);
        solrQuery.setRows(MAX_ROW);
        SolrClient solrClient = SolrUtil.getClient(EngineUtil.DWB_CHARGE_DETAIL);
        solrClient.queryAndStreamResponse(solrQuery, new StreamingResponseCallback() {
            @Override
            public void streamSolrDocument(SolrDocument doc) {
                System.out.print(count.getAndIncrement());
            }

            @Override
            public void streamDocListInfo(long numFound, long start, Float maxScore) {
                log.info("numFound:" + numFound + ",start:" + start + ",maxScore:" + maxScore);

            }
        });
        solrClient.close();*/

    }
    private static void stream() throws Exception {

        String expr = "search(DWB_CHARGE_DETAIL,q=\"*:*\",fl=\"id,ETL_TIME,PRESCRIPTNO,CHARGEUNIT,ITEMCLASS,ORGNAME,ITEMCLASS_ID,YB_VISITID,CHARGECLASS,DATA_RESOUCE,DOCTORID,YB_CHARGEID,ITEMPRICE,VISITTYPE\"," +
                "rows=10)";
//        String expr = "search(" + EngineUtil.DWB_MASTER_INFO + ",q=\"*:*\",fl=\"ORGNAME\",sort=\"ORGNAME asc\",rows=300,qt=\"/query\")";

//        SolrUtil.stream(expr, (tuple, index) -> log.info(index + ":   " + JSON.toJSONString(tuple)));
        SolrUtil.stream(expr);
    }
    private static void facet() throws Exception {
        final AtomicInteger count = new AtomicInteger(0);
        String expr = "facet(" + EngineUtil.DWB_MASTER_INFO + ",q=\"*:*\",bucketSorts=\"id desc\",count(*),rows=1000,buckets=\"id\")";
//        String expr = "search(" + EngineUtil.DWB_MASTER_INFO + ",q=\"*:*\",fl=\"ORGNAME\",sort=\"ORGNAME asc\",rows=300,qt=\"/query\")";

        SolrUtil.stream(expr, (tuple, index) -> log.info(index + ":   " + JSON.toJSONString(tuple)));


    }

}
