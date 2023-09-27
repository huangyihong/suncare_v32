package org.jeecg.test;

import com.ai.modules.engine.util.SolrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import netscape.javascript.JSObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.InputStreamResponseParser;
import org.apache.solr.client.solrj.impl.StreamingBinaryResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zhangpeng
 * @Date: 2019/3/26 15
 * @Description: 字典增删改查
 */

public class SolrExportJson {

	private static final Logger log = LoggerFactory.getLogger(SolrExport.class);

	private static String solrUrl = "http://10.63.82.189:8984/solr/DWB_MASTER_INFO";
	private static String solrUrl2 = "http://10.63.82.189:8984/solr/NEWS_V3_RESULT";

	// https://dev.lingwww.com/solr/ywjzxx/export?q=*:*&sort=JZID00+desc&fl=JZID00
	private static String solrZkUrl = "127.0.0.1:2181";
	private static String solrCollection = "ywjzxx";
	private static String solrClientType = "zk";



	public static void main(String[] args) throws IOException {
// 数据写入xml
		String importFilePath = "./excel/exportFile.csv";
		BufferedWriter fileWriter = new BufferedWriter(
				new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
		//写文件头
//		fileWriter.write("VISITID,CASE_ID\n");

		HttpSolrClient solrClient = null;
		HttpSolrClient solrClient2 = null;
        final long[] count = {0};
		try {
			solrClient = new HttpSolrClient.Builder(solrUrl).
					withConnectionTimeout(10000).withSocketTimeout(60000)
					.build();
			solrClient.setParser(new InputStreamResponseParser("json"));

			solrClient2 = new HttpSolrClient.Builder(solrUrl2).
					withConnectionTimeout(10000).withSocketTimeout(60000)
					.build();

			// 查询条件SolrQuery对象反序列化
			SolrQuery solrQuery = new SolrQuery("CASE_ID:*");
			solrQuery.setRequestHandler("/export");

			solrQuery.setFields("VISITID", "CASE_ID");
//			solrQuery.addFilterQuery("-PROJECT_ID:6d0f2f802a910aa9115e57f05237ecfd");
//			solrQuery.addFilterQuery("SEC_PUSH_STATUS:1");
//			solrQuery.addFilterQuery("id:(\""+ StringUtils.join(ids, "\",\"") + "\")");
			solrQuery.addFilterQuery("{!join fromIndex=NEWS_V3_RESULT from=VISITID to=VISITID}*:*");
//			solrQuery.setRows(20);
			solrQuery.setRows(1000000000);
			log.info(solrQuery.toQueryString());

//			final AtomicInteger count = new AtomicInteger(0);
			Map<String, String> map = new HashMap<>(1000);
			HttpSolrClient finalSolrClient = solrClient2;
			JSONObject jsonObject = new JSONObject();
			SolrQuery solrQuery2 = new SolrQuery("*:*");
			solrQuery2.setFields("id", "VISITID");
			solrQuery2.setRows(10000000);
			fileWriter.write('[');
			StreamingResponseCallback callback = new StreamingResponseCallback() {
				@Override
				public void streamSolrDocument(SolrDocument doc) {
					try {
						/*if(doc.containsKey("MAX_ACTION_MONEY")){
							fileWriter.write( doc.getFieldValue("id") +"," + doc.getFieldValue("MAX_ACTION_MONEY") +"\n");
						}*/

						map.put(doc.getFieldValue("VISITID").toString(), doc.getFieldValue("CASE_ID").toString());

						if(map.size() == 300){
							solrQuery2.setQuery("VISITID:(\"" + StringUtils.join(map.keySet(), "\",\"") + "\")");
							QueryRequest req2 = new QueryRequest(solrQuery2, SolrRequest.METHOD.POST);
//							log.info(solrQuery2.toQueryString());
							SolrDocumentList documents = req2.process(finalSolrClient).getResults();
							log.info("size:" + documents.size());
							for(SolrDocument document: documents){
								jsonObject.put("id", document.getFieldValue("id"));
								jsonObject.put("CASE_ID", SolrUtil.initActionValue(
										map.get(document.getFieldValue("VISITID").toString())
										, "set"));
								fileWriter.write(jsonObject.toJSONString());
								fileWriter.write(',');
							}
							map.clear();
						}

//						fileWriter.write( doc.getFieldValue("VISITID") +"," + doc.getFieldValue("CASE_ID") +"\n");

						/*if(count[0].incrementAndGet() % 10000 == 0){
//							System.out.println(count.get());
						}*/
					} catch (IOException | SolrServerException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void streamDocListInfo(long numFound, long start, Float maxScore) {
//					log.info("numFound:" + numFound + ",start:" + start + ",maxScore:" + maxScore);
                    count[0] = numFound;
				}
			};
			QueryRequest req = new QueryRequest(solrQuery, SolrRequest.METHOD.POST);
			ResponseParser parser = new StreamingBinaryResponseParser(callback);
			req.setStreamingResponseCallback(callback);
			req.setResponseParser(parser);
			req.process(solrClient);




		} catch (Throwable e) {
			log.error("", e);
		} finally {
			if (solrClient != null) {
				try {
					solrClient.close();
					solrClient2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			fileWriter.write(']');
			fileWriter.close();
			System.out.println("num:" + count[0]);
		}
	}


	/**
	 * 流式解析JSON文件
	 * @param reader
	 */
	public static void parseJsonStream(Reader reader) throws  Exception {
		JSONReader jsonReader = new JSONReader(reader);

		jsonReader.startObject();
		while (jsonReader.hasNext()) {
		    String elem = jsonReader.readString();
		    System.out.println("a=" + elem);

		    if ("responseHeader".equals(elem)) {
		    	jsonReader.startObject();

	    		//循环一条数据里面的内容
	    		while(jsonReader.hasNext()){
	    			Object name = jsonReader.readObject();
	    			Object value = jsonReader.readObject();

	    			System.out.print (name +"="+value + " ");
	    		}
	    		jsonReader.endObject();
		    }
		    else if("response".equals(elem)){
		    	jsonReader.startObject();

	    		//循环一条数据里面的内容
	    		while(jsonReader.hasNext()){
	    			Object name = jsonReader.readObject();
	    			Object value = jsonReader.readObject();

	    			System.out.print (name +"="+value + " ");
	    		}
	    		jsonReader.endObject();
		    }

		}
		jsonReader.close();

	}


	static  String[] ids = {
			"8abfd003756fae7201756fbbba4d4e90",
			"8abfd003756fae7201756fb648525b59",
			"8abfd003756fae7201756fb648515b46",
			"8abfd003756fae7201756fb648545b69",
			"7d1daee6efe1fffd26720eddcd89617e",
			"ac5e293e0c9423683f050394fe40d208",
			"6979647504a8e031339288065a1aadfc",
			"8e626e0b34e96252e5cb31b1a3f0fc29",
			"5eb1c24f5d4a436ea82d376d9a58c9bb",
			"937a8311f194b660b6c18925e597685a",
			"16f9dacbd474e86fba5dac6627ee3a2d",
			"250d84f678f02c6158da5db5b4ba7dae",
			"c4e00da05db92e838465a1c5f9700bec",
			"de42bde81573d6d757d854196bee05ee",
			"300e4c3356087630ee480619af84198f",
			"19fd4869b93cfb0eca09625f997faeaf",
			"4ecd9c9d677eb7b3ed7844b3f795bbea",
			"8e8b419626b9090e3f11fe674b751b38",
			"ef52ffa39bc61c7b999bbf2f6f4fb472",
			"b8b4917720a41f1e2effd8339e197af2",
			"adb0fea04a6a51c225549f1a718a0334",
			"db3461d71cfe874131a78d17a1506aab",
			"8abfd003756fae7201756fbb7fa74ae9",
			"fcd6abf29e0a41ff55e1c05800490b13",
			"8abfd003756fae7201756fce301b1a43",
			"8abfd003756fae7201756fb590155694",
			"8abfd003756fae7201756fbb6c7745a6",
			"8abfd003756fae7201756fbac5892e27",
			"edb3146bc78607fd17d358d9ac1931a2",
			"8abfd003756fae7201756fbb6cd44828",
			"26537aeda8ee6cab5d0acab99fb613cc",
			"8abfd003756fae7201756fbb6ca64712",
			"8abfd003756fae7201756fbe9820154e",
			"84bf22400768283018c386ecf2c1a6be",
			"8abfd003756fae7201756fbe981e1538",
			"8abfd003756fae7201756fbe981f1543",
			"31d96dd4c9a30c03b1083f076feeb4c8",
			"8db32f3d99eef5b0b49bd88fc1a09fd9",
			"1cf706e24d914fde0f9516626a4fe0f0",
			"a67d528bf8910afeb098a4dd5669aeb5",
			"b32c573583ad5ab8f4ac824bd20d542d",
			"8abfd003756fae7201756fbe57d7136a",
			"8abfd003756fae7201756fb18f632d3e",
			"8abfd003756fae7201756fbc56fc51ab",
			"8abfd003756fae7201756fcd85d56aac",
			"8abfd003756fae7201756fbe57df1400",
			"8abfd003756fae7201756fb351d749a9",
			"8abfd003756fae7201756fcbbbe4417b",
			"8abfd003756fae7201756fbb4b943dc5",
			"8abfd003756fae7201756fcd33645baf",
			"8abfd003756fae7201756fcf0b406c00",
			"8abfd003756fae7201756fbfa19e316a",
			"8abfd003756fae7201756fbc56de50f9",
			"8abfd003756fae7201756fb9a72f7d7b",
			"8abfd003756fae7201756fb9a7277d28",
			"8abfd003756fae7201756fb9a7297d37",
			"8abfd003756fae7201756fbcc2e35e98",
			"8abfd003756fae7201756fcf420071cf",
			"8abfd003756fae7201756fb2664c38fc",
			"8abfd003756fae7201756fb2665839aa",
			"8abfd003756fae7201756fb2663f3847",
			"8abfd003756fae7201756fb2664b38ee",
			"8abfd003756fae7201756fb2864a40c8",
			"8abfd003756fae7201756fb286674184",
			"8abfd003756fae7201756fb26632378a",
			"8abfd003756fae7201756fb2663f3840",
			"8abfd003756fae7201756fb2864a40c1",
			"8abfd003756fae7201756fb28667417d",
			"8abfd003756fae7201756fb286374013",
			"8abfd003756fae7201756fb28634400c",
			"8abfd003756fae7201756fb266653a58",
			"8abfd003756fae7201756fb266653a5f",
			"8abfd003756fae7201756fb2864b40cf",
			"8abfd003756fae7201756fb266663a66",
			"8abfd003756fae7201756fb266323783",
			"8abfd003756fae7201756fb2663e3839",
			"8abfd003756fae7201756fb2665939b0",
			"8abfd003756fae7201756fb266333791",
			"8abfd003756fae7201756fb2664c38f5",
			"8abfd003756fae7201756fb286664176",
			"8abfd003756fae7201756fb2665839a3",
			"8abfd003756fae7201756fb286374019",
			"8abfd003756fae7201756fc5e2986bc3",
			"8abfd003756fae7201756ffcc4a62d75",
			"8abfd003756fae7201756fb427c05462",
			"8abfd003756fae7201756fb09f540879",
			"8abfd003756fae7201756fd4dd3a69ee",
			"8abfd003756fae7201756fd4dd3b69fc",
			"8abfd003756fae7201756fb09f7909a5",
			"8abfd003756fae7201756fb0bdcc18bd",
			"8abfd003756fae7201756fb09f58089c",
			"8abfd003756fae7201756fd4dd2468b2",
			"8abfd003756fae7201756fb0a16a0c72",
			"8abfd003756fae7201756fb4265952d0",
			"8abfd003756fae7201756fc5e2826ac3",
			"8abfd003756fae7201756fb4233f4f97",
			"8abfd003756fae7201756fb09f8109f7",
			"8abfd003756fae7201756fb423635143",
			"8abfd003756fae7201756fb09fa80a15",
			"8abfd003756fae7201756fb0bdcf18e1",
			"8abfd003756fae7201756fb41e6e4c73",
			"8abfd003756fae7201756fb41f004e2d",
			"8abfd003756fae7201756ffb2bda1a14",
			"8abfd003756fae720175700d31df7eb8",
			"8abfd003756fae720175700d3a5b0c32",
			"8abfd003756fae7201756ffb2b911539",
			"8abfd003756fae720175700d31a47c20",
			"8abfd003756fae7201756ffb2bc61803",
			"8abfd003756fae7201756fb41f034e43",
			"8abfd003756fae720175700d46c118e6",
			"8abfd003756fae720175700d3a770da4",
			"8abfd003756fae720175700d469016d9",
			"8abfd003756fae7201756fb41e6d4c60",
			"8abfd003756fae720175700d31cc7de1",
			"8abfd003756fae720175700d56e32a0a",
			"8abfd003756fae720175700d6a1d311f",
			"8abfd003756fae720175700d602e2ceb",
			"8abfd003756fae720175700d721332be",
			"8abfd003756fae720175700d56c928bc",
			"8abfd003756fae720175700d4e632257",
			"8abfd003756fae720175700d69df2e57",
			"8abfd003756fae720175700d69ff2fc4",
			"8abfd003756fae720175700d56af2786",
			"8abfd003756fae720175700d600d2b63",
			"8abfd003756fae720175700dd01d5d1e",
			"8abfd003756fae720175700dd0435e75",
			"9903",
			"9905",
			"9906",
			"9901",
			"9902",
			"8abfd003756fae720175700e086b75dc",
			"8abfd003756fae720175700e086c75e3",
			"8abfd003756fae720175700dfc216f52",
			"8abfd003756fae720175700e06d6749d",
			"8abfd003756fae720175700e189b7c9b",
			"8abfd003756fae720175700e088f771c",
			"8abfd003756fae720175700e18b27dcd",
			"8abfd003756fae720175700dfbf96beb",
			"8abfd003756fae720175700deda365e5",
			"8abfd003756fae720175700deda76607",
			"8abfd003756fae720175700dedbe6712",
			"8abfd003756fae720175700deddf686c",
			"8abfd003756fae720175700dfbfc6c31",
			"9b1232cde9166c22f21c9e2d8e012ccb",
			"cf56bd1d70e0839a6430f1d3cf100036",
			"6830fa2b2eebfa9f74a369394314a626",
			"8d3d62f838be4f89279f37ef5a628280",
			"a415ae8334dccc2ab991d77767ad7bfb",
			"df260e31a8015b60cb74c9addaa2e411",
			"3405055a7ca11bb07f2aa6462d75ba70",
			"e41aa14b0c723dd1b6dbe53591994281",
			"e34213bbb35b8ee39a44b64ee6990e5e",
			"18711cedee4e6a19be3859a3d6e6c2a0",
			"edf76d2ac0084522ed43d20dd12f8be1",
			"32538283a184185a102f6b9e0ae6ddf3",
			"b49eaa1998080f586dfb04fa68d8306f",
			"dd944cdcfc7af3f9fe65b63816524a3a",
			"c76908b6364c022df2238f3458f460fc",
			"afcac762330491ffbc0917c89ab6c1b5",
			"bbca186aa5db53ce2b80e30551317f56",
			"5845aabe86807e2e48bd4fb6de223832",
			"ba2f17f674371ea6ba662218b0b3e6f3",
			"6f03eed4f072a64c79d5553fc68c5747",
			"284d03fc6639be3bf2676a46616d4ff5",
			"8ea41c953f3396dfa876f64a11bafc1b",
			"c94e9d93bdc22f1bf92561e82228f80d",
			"3a161ad90ae2b554580d299a4b83ccad",
			"3651d55a96ed3c30f78a36535cc0c2b5",
			"4f19ff8a37f6dea574a93e289a02e79d"
	};

}
