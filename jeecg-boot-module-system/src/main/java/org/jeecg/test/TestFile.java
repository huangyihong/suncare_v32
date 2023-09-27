package org.jeecg.test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.solr.SimplePostTool;

public class TestFile {

	public static void main(String[] args) {
		try {


			String tableName = "STD_HOSLEVEL_FUNDPAYPROP";
			String fields = "PROJECT,HOSPLEVEL,HOSPLEVEL_NAME,FUNDPAYPROP,STARTDATE,ENDDATE";
			Map<String,String> busiParams = new HashMap<String,String>();
			busiParams.put("tableName", "MEDICAL_DISEASE_DIAG");
//			busiParams.put("fields", fields);
			busiParams.put("isNeedHead","true");

			String fileName = "E:\\solr\\"+tableName + ".txt";
			File file =new File(fileName);

			FileOutputStream outStream = new FileOutputStream(file);

			ApiOauthUtil.writeResultToStream("/oauth/api/queryMedicalDictForCSV", busiParams, outStream);

			outStream.close();
			System.out.println(file.length());

			/*//#/
			String solrUrl =  "http://10.63.82.188:8983/solr/STD_DRUGGROUP/update";


			SolrUtil.deleteSolrDataByPostTool(solrUrl, "*:*");

			SolrUtil.importJsonToSolr(file.getAbsolutePath(), solrUrl,false,true);*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
