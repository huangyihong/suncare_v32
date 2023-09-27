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
import com.alibaba.fastjson.TypeReference;
import jxl.Workbook;
import jxl.write.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.InputStreamResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.util.NamedList;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.lang.Boolean;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg", "com.ai"})
public class SolrTest2 {
    private static int MAX_ROW = Integer.MAX_VALUE;
    public static void initProp() throws IOException {
//        List<Map<String, Object>> loaded = (new YamlPropertySourceLoader()).load();

//        Map<String, Object> map = new Yaml().load(new ClassPathResource("/application-dev.yml").getInputStream());
   /*     SolrUtil.SolrProp solrProp = JSON.parseObject(JSON.toJSONString(map.get("solr")), new TypeReference<SolrUtil.SolrProp>() {
        });
        SolrUtil solrUtil = new SolrUtil();
        solrUtil.setSolrProp(solrProp);*/
    }
    public static void main(String[] args) throws Exception {
//        new EngineSpringApplication(null, new Class<?>[] { SolrTest.class }).run(args);
//        ApplicationContext context = SpringContextUtils.getApplicationContext();

        initProp();
        String batchId = "0a15fe20853b9b7686f41661f6f79c7f";
        String path = "E:/ASIAProject/suncare_v3/excel/2019业务组结果统计-机构病例" + batchId +".xls";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        String[] titles = {"医疗机构","病例数","涉及总金额"};
        String[] fields = {"ORGNAME", "count(*)", "sum(TOTALFEE)"};
//        String[] titles = {"医疗机构编码","医疗机构名称", "出现次数", "涉及总金额"};
//        String[] fields = {"ORGID","ORGNAME", "count(*)", "sum(FEE)"};

        WritableCellFormat[] formats = {new WritableCellFormat(NumberFormats.TEXT),new WritableCellFormat(NumberFormats.TEXT),
                new WritableCellFormat(NumberFormats.FLOAT), new WritableCellFormat(NumberFormats.FLOAT)};
        WritableCellFormat textFormat = new WritableCellFormat(NumberFormats.TEXT);
        FileOutputStream os = new FileOutputStream(path);
        WritableWorkbook wwb = Workbook.createWorkbook(os);
        WritableSheet sheet = wwb.createSheet("sheet1", 0);
        AtomicInteger startHang = new AtomicInteger();
        for(int i = 0, len = titles.length; i < len; i++){
            sheet.addCell(new Label(i, startHang.get(), titles[i]));
        }

        int len = fields.length;

        String expr = "facet(MEDICAL_UNREASONABLE_ACTION,q=\"BATCH_ID:" + batchId + "\"" +
                ",fq=\"{!join from=VISITID fromIndex=DWB_MASTER_INFO to=VISITID}VISITDATE:2019*\",bucketSorts=\""+fields[0] +" asc\",count(*)" +
                ",rows=" + MAX_ROW + ",buckets=\"" + fields[0] +"\",sum(TOTALFEE))";
        SolrUtil.stream(expr, (map, index) -> {
            Boolean eof = (Boolean)map.get("EOF");
            if(eof != null && eof){
                return;
            }
            startHang.incrementAndGet();
            for(int i = 0; i < len; i++){
                Object val = map.get(fields[i]);
                if(val != null) {
                    if(val instanceof Double || val instanceof Float){
                        val = new BigDecimal(val.toString()).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();  ;
                    }
                    Label label =  new Label(i,startHang.get(), val.toString(),
                            textFormat);
                    try {
                        sheet.addCell(label);
                    } catch (WriteException e) {
                        e.printStackTrace();
                    }
                }

            }
        });


        wwb.write();
        wwb.close();

        os.close();
        System.exit(0);

    /*    SolrUtil solrUtil = new SolrUtil();
//        solrUtil.setSolrUrl("http://10.63.80.131:8983/solr");
        Map<String,String> zkMap = new HashMap<>();
//        zkMap.put("funan","10.63.80.131:2381");
//        solrUtil.setZkUrl(zkMap);
        search();

        System.exit(0);

        long startTime = System.currentTimeMillis();
        search();
        long endTime = System.currentTimeMillis();
        long seconds = (endTime - startTime) / 1000;//相差毫秒数

        search2();
        long endTime2 = System.currentTimeMillis();
        long seconds2 = (endTime2 - endTime) / 1000;//相差毫秒数
        System.out.println("运行时长1： "+ (seconds / 60) +"分钟，"+ seconds % 60+"秒 。" + (endTime - startTime));
        System.out.println("运行时长2： "+ (seconds2 / 60) +"分钟，"+ seconds2 % 60+"秒 。" + (endTime2 - endTime));*/

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
        solrQuery.setRows(3);
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
