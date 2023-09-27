package org.jeecg.test;

import com.ai.common.utils.ExportXUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: zhangpeng
 * @Date: 2021/1/26 10
 * @Description:
 */
public class SolrTest4 {

    public static void main(String[] args) throws Exception {
        String[] fieldTitles = {"项目名称（原始）","医院项目名称（原始）", "数量"};
        String[] fields = {"ITEMNAME_SRC","HIS_ITEMNAME_SRC", "count(*)"};

        String jsf = "facet(MEDICAL_UNREASONABLE_ACTION,q=\"*:*\",bucketSorts=\"ITEMNAME_SRC asc\",count(*),rows=-1,buckets=\"ITEMNAME_SRC, HIS_ITEMNAME_SRC\"" +
                ",fq=\"BATCH_ID:945d13c9918972f0c87e8726d47cfeb5 AND CASE_ID:2c90db1330b641bf8b6e2024f0ebb7d2 AND BUSI_TYPE:CASE\",limit=9999999,overrequest=999999)";

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
            list.remove(list.size() - 1);
            for(Map<String, Object> map: list ){
                map.put("ITEMNAME_SRC", map.get("ITEMNAME_SRC").toString().replaceAll("\t", "\\\\t").replaceAll("\n", "\\\\n"));
                map.put("HIS_ITEMNAME_SRC", map.get("HIS_ITEMNAME_SRC").toString().replaceAll("\t", "\\\\t").replaceAll("\n", "\\\\n"));
            }
            System.out.println("记录数：" + list.size());
            // 移除终点项

        } catch (SolrServerException | IOException e) {
            throw new Exception("调用solr失败：" + e.getMessage());
        } finally {
            if(solrClient!=null) {
                solrClient.close();
            }
        }

        File outFile = new File("E:\\ASIAProject\\suncare_v3\\excel\\信息不一致原始项目统计导出.xlsx");
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
