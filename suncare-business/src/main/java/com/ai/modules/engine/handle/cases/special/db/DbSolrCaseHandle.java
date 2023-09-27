/**
 * HiveCaseHandle.java	  V1.0   2020年11月16日 下午2:48:24
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.special.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.cases.special.hive.HiveSolrCaseHandle;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.service.IEngineService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 
 * 功能描述：db与solr相结合方式计算特殊模型
 *
 * @author  zhangly
 * Date: 2020年11月16日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class DbSolrCaseHandle extends HiveSolrCaseHandle {
	
	public DbSolrCaseHandle(String datasource, TaskProject task, TaskProjectBatch batch,
			HisMedicalFormalCase formalCase, MedicalSpecialCaseClassify classify) {
		super(datasource, task, batch, formalCase, classify);
	}
	
	/**
	 * 
	 * 功能描述：计算引擎入口
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月17日 下午2:57:40</p>
	 *
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public EngineResult generateUnreasonableAction() throws Exception {
		if(ignoreRun()) {
        	//忽略运行
        	return EngineResult.ok();
        }
		boolean slave = false;
		//删除solr历史数据
		this.deleteSolrByCase(EngineUtil.MEDICAL_UNREASONABLE_ACTION, batch.getBatchId(), formalCase.getCaseId(), slave);		
		
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IEngineService engineSV = context.getBean(IEngineService.class);
		List<List<EngineNode>> flowList = engineSV.queryHisFormalEngineNode(formalCase.getCaseId(), batch.getBatchId());
		if(flowList!=null && flowList.size()>0) {
			this.deleteSolrByCase(EngineUtil.MEDICAL_TRAIL_ACTION, batch.getBatchId(), formalCase.getCaseId(), slave);
			solrEngineNodeList.clear();
			//遍历流程节点条件列表
			for(List<EngineNode> flow : flowList) {
				if (flow == null || flow.size()==0) {
		            throw new EngineBizException("模型未能找到流程节点！");
		        }
				if(solrEngineNodeList.size()==0) {			
					writeStreamToSolr(flow, "MEDICAL_UNREASONABLE_ACTION");
				} else {
					//特殊模型仅有一条流程路线时，直接结果直接保存正式表后再进行二次过滤；否则结果先保存临时表后再进行二次过滤
					boolean formal = flowList.size()==1 ? true : false;
					String collection = formal ? "MEDICAL_UNREASONABLE_ACTION" : "MEDICAL_TRAIL_ACTION";
					long count = writeStreamToSolr(flow, collection);
					if(count>0) {
						//进行solr节点条件过滤
						this.filter(formal);
					}
				}
			}
		}
								
		Thread.sleep(5000L);
		return this.computeMoney();
	}
	
	/**
	 * 
	 * 功能描述：sql执行结果导入solr
	 *
	 * @author  zhangly
	 *
	 * @param flow
	 * @param collection
	 * @throws Exception
	 */
	private long writeStreamToSolr(List<EngineNode> flow, String collection) throws Exception {
		String sql = this.masterInfoJoinSql(flow);
		//解析出关系型数据库可执行的sql
		String find = "md5(concat_ws";
		int index = sql.indexOf(find);
		String query = "select " + sql.substring(index);
		query = StringUtils.replace(query, "  ", " ");
		query = StringUtils.replace(query, ") from", " from");
		String fieldStr = sql.substring(0, index);
		find = "default.udf_json_out('";
		fieldStr = StringUtils.replace(fieldStr, find, "");
		fieldStr = StringUtils.replace(fieldStr, "select ", "");
		fieldStr = StringUtils.replace(fieldStr, " ", "");
		index = fieldStr.indexOf("'");
		fieldStr = fieldStr.substring(0, index);
		String[] fieldArray = StringUtils.split(fieldStr, ",");
		
		BufferedWriter fileWriter = null;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			conn = HiveJDBCUtil.getConnection();
			conn.setAutoCommit(false);
            stmt = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            log.info("execute sql:{}", query);
            rs = stmt.executeQuery();
            // 数据写入文件
            String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/" + formalCase.getCaseId() + ".json";
            fileWriter = new BufferedWriter(
                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
            //写文件头
            fileWriter.write("[");
            long count = 0;
            while(rs.next()) {
            	JSONObject json = new JSONObject();
            	for (int i=1, len=fieldArray.length; i<=len; i++) {
            		String key = fieldArray[i-1];
            		if("ID".equalsIgnoreCase(key)) {
            			key = "id";
            		}
            		Object value = rs.getObject(i);
            		json.put(key, value);
            	}
            	fileWriter.write(JSON.toJSONString(json, SerializerFeature.WriteDateUseDateFormat));
	            fileWriter.write(',');
	            count++;
            }
            // 文件尾
            fileWriter.write("]");
            fileWriter.flush();
            //导入solr服务器
            SolrUtil.importJsonToSolr(importFilePath, collection);
            return count;
		} catch(Exception e) {
			throw e;
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {}
			}
			if(stmt!=null) {
				try {
					stmt.close();
				} catch (SQLException e) {}
			}
			if(conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
			if(fileWriter!=null) {
    			fileWriter.close();
    		}
		}
	}
}
