/**
 * EngineTransferServiceImpl.java	  V1.0   2020年9月16日 上午9:25:04
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ai.modules.engine.service.IEngineTransferService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineTransferServiceImpl implements IEngineTransferService {
	
	private static final String FIELDS = "id,VISITID,CLIENTID,INSURANCETYPE,CLIENTNAME,"
			+ "SEX_CODE,SEX,BIRTHDAY,YEARAGE,MONTHAGE,DAYAGE,VISITTYPE_ID,VISITTYPE,VISITDATE"
			+ ",VISIT_SIGN,ORGID,ORGNAME,HOSPLEVEL,HOSPGRADE,DEPTID,DEPTNAME,DOCTORID,DOCTORNAME"
			+ ",TOTALFEE ,LEAVEDATE,DISEASECODE,DISEASENAME,PATHONOGY_DISEASE,PATHONOGY_DISEASECODE"
			+ ",HIS_VISITID,VISITID_DUMMY,ZY_DAYS,ZY_DAYS_CALCULATE,DATA_RESOUCE_ID,DATA_RESOUCE,"
			+ "ETL_SOURCE,ETL_SOURCE_NAME,ETL_TIME,CASE_ID,CASE_NAME  ,GEN_DATA_TIME,PROJECT_ID,"
			+ "PROJECT_NAME,BATCH_ID,ACTION_MONEY,CASE_SCORE,ACTION_TYPE_ID,ACTION_TYPE_NAME,"
			+ "ACTION_ID,ACTION_NAME,ACTION_DESC,BUSI_TYPE,REVIEW_NAME,FIR_REVIEW_USERID,"
			+ "FIR_REVIEW_USERNAME,FIR_REVIEW_TIME,FIR_REVIEW_STATUS,FIR_REVIEW_REMARK,"
			+ "PUSH_STATUS,PUSH_USERID,PUSH_USERNAME,SEC_REVIEW_USERID,SEC_REVIEW_USERNAME,"
			+ "SEC_REVIEW_TIME,SEC_REVIEW_STATUS,SEC_REVIEW_REMARK,SEC_PUSH_STATUS,SEC_PUSH_USERID,"
			+ "SEC_PUSH_USERNAME,MAIN_FLAG,CUS_REVIEW_USERID,CUS_REVIEW_USERNAME,CUS_REVIEW_TIME,"
			+ "CUS_REVIEW_STATUS,CUS_REVIEW_REMARK,CLINICAL_GROUP_IDS,CLINICAL_GROUP_NAMES,"
			+ "CLINICAL_DRUG_MONEY,CLINICAL_TREAT_MONEY,CLINICAL_DRUG_MONEY_RATIO,"
			+ "CLINICAL_TREAT_MONEY_RATIO,CLINICAL_DRUG_BEYOND_MONEY,CLINICAL_TREAT_BEYOND_MONEY,"
			+ "CLINICAL_DRUG_BEYOND_MONEY_RATIO,CLINICAL_TREAT_BEYOND_MONEY_RATIO,ITEM_QTY,"
			+ "RULE_SCOPE,RULE_SCOPE_NAME,MUTEX_ITEM_CODE,MUTEX_ITEM_NAME";
	private static final Set<String> TRANSFER_FIELDS = new LinkedHashSet<String>();
	static {
		String[] array = FIELDS.split(",");
		for(String field : array) {
			TRANSFER_FIELDS.add(field);
		}
	}
	
	//@Value("${solr.trans.csv-folder}")
	private String csvFolder;
	//@Value("${solr.trans.sqlldr-ctrl-folder}")
	private String sqlldrCtrlFolder;
	//@Value("${solr.trans.sqlldr-work-folder}")
	private String sqlldrWorkFolder;
	@Value("${spring.datasource.dynamic.datasource.master.username}")
	private String dbUsername;
	@Value("${spring.datasource.dynamic.datasource.master.password}")
	private String dbPassword;
	@Value("${spring.datasource.dynamic.datasource.master.url}")
	private String dbUrl;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void solrTransferOracle(String batchId) throws Exception {
		String sql = "delete from MEDICAL_UNREASONABLE_ACTION where batch_id=?";
		jdbcTemplate.update(sql, batchId);
		
		String csvFilePath = csvFolder + "/" + batchId + ".txt";
        final BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(csvFilePath)), Charset.forName("utf8")));
        int idx = 0;
        for(String field : TRANSFER_FIELDS) {
        	fileWriter.write(field);
        	idx++;
        	if(idx<TRANSFER_FIELDS.size()) {
        		fileWriter.write("\t");
        	}
        }
        fileWriter.write("\n");
        
        List<String> conditionList = new ArrayList<String>();
    	String where = "BATCH_ID:%s";
    	where  = String.format(where, batchId);
    	conditionList.add(where);
    	int count = SolrUtil.exportByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (map, index) -> {
    		try {
    			int number = 0;
                for(String field : TRANSFER_FIELDS) {
                	if(map.get(field)!=null) {
                		fileWriter.write(map.get(field).toString());
                	} else {
                		fileWriter.write("");
                	}
                	number++;
                	if(number<TRANSFER_FIELDS.size()) {
                		fileWriter.write("\t");
                	}
                }
                fileWriter.write("\n");
    		} catch(Exception e) {
    			
    		}
        });
    	fileWriter.close();
    	//c85a9ba6c5c002da9c4710450e5c31ca
    	if(count>0) {
    		String workFolder = sqlldrWorkFolder + "/" + batchId;
        	String ctrlFolder = sqlldrCtrlFolder + "/MEDICAL_UNREASONABLE_ACTION.ctl";
        	dbUrl = dbUrl.replace("jdbc:oracle:thin:@", "");
        	int index = dbUrl.lastIndexOf(":");
        	dbUrl = dbUrl.substring(0, index) + "/" + dbUrl.substring(index);
        	StringBuilder cmd = new StringBuilder();
        	cmd.append("sqlldr");
        	cmd.append(" ").append(dbUsername).append("/").append(dbPassword).append("@").append(dbUrl);
        	cmd.append(" DIRECT=false parallel=true");
        	cmd.append(" control=").append(ctrlFolder);
        	cmd.append(" log=").append(workFolder).append(".log");
        	cmd.append(" bad=").append(workFolder).append(".bad");
        	cmd.append(" data=").append(csvFilePath);
        	log.info("sqlldr cmd:{}", cmd);
            Process process = Runtime.getRuntime().exec(cmd.toString());
            print(process);
            process.waitFor();            
    	}
	}
	
	private static void print(Process process) {
		Reader reader = null;
		BufferedReader bf = null;
		try {
			StringBuffer buf = new StringBuffer();
			reader = new InputStreamReader(process.getInputStream(), "GB2312");
			bf = new BufferedReader(reader);
			String line = null;
			while ((line = bf.readLine()) != null) {
				buf.append(line).append("\n");
			}
			log.info(buf.toString());
		} catch(Exception e) {
			
		} finally {
			if(bf!=null) {
				try {
					bf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(reader!=null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
