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

import com.ai.common.utils.ExportXUtils;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg", "com.ai"})
public class SolrTest3 {

    public static void main(String[] args) throws Exception {
        String[] fieldTitles = {"项目名称","项目名称（原始）", "数量"};
        String[] fields = {"ITEMNAME","ITEMNAME_SRC", "count(*)"};

        String jsf = "facet(MEDICAL_UNREASONABLE_ACTION,q=\"*:*\",bucketSorts=\"ITEMNAME asc\",count(*),rows=-1,buckets=\"ITEMNAME,ITEMNAME_SRC\"" +
                ",fq=\"BATCH_ID:945d13c9918972f0c87e8726d47cfeb5 AND CASE_ID:819c35baf85746c99e082733975a24f9 AND BUSI_TYPE:CASE\",limit=9999999,overrequest=999999)";

//        SolrClient solrClient = new HttpSolrClient.Builder("http://10.63.82.219:8983/solr/MEDICAL_UNREASONABLE_ACTION" )
        SolrClient solrClient = new HttpSolrClient.Builder("http://10.175.33.3:8983/solr/MEDICAL_UNREASONABLE_ACTION" )
                .withSocketTimeout(99999999)
                .withConnectionTimeout(999999999)
                .build();

        List<Map<String, Object>> list;
        SolrQuery query = new SolrQuery("expr", jsf);
        query.setRequestHandler("/stream");
        try {

            QueryResponse response = solrClient.query(query, SolrRequest.METHOD.POST);

            NamedList<Object> genericResponse = response.getResponse();
            LinkedHashMap hashMap = (LinkedHashMap) genericResponse.get("result-set");
            list = (List<Map<String, Object>>) hashMap.get("docs");
            // 移除终点项
            list.remove(list.size() - 1);
            System.out.println("记录数：" + list.size());
            for(Map<String, Object> map: list ){
                map.put("ITEMNAME", map.get("ITEMNAME").toString().replaceAll("\t", "\\\\t").replaceAll("\n", "\\\\n"));
                map.put("ITEMNAME_SRC", map.get("ITEMNAME_SRC").toString().replaceAll("\t", "\\\\t").replaceAll("\n", "\\\\n"));
            }

        } catch (SolrServerException | IOException e) {
            throw new Exception("调用solr失败：" + e.getMessage());
        } finally {
            if(solrClient!=null) {
                solrClient.close();
            }
        }

        File outFile = new File("E:\\ASIAProject\\suncare_v3\\excel\\超物价收费项目统计导出.xlsx");
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
        FileOutputStream os = new FileOutputStream(outFile);

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 生成一个表格
        ExportXUtils.exportExl(list, fieldTitles, fields
                , workbook, "sheet1");

        workbook.write(os);
        workbook.dispose();

    }




}
