/**
 * EngineResultHandle.java	  V1.0   2022年5月17日 下午2:49:32
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.apache.avro.reflect.ReflectData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.model.RTimer;
import com.ai.modules.engine.model.WorkflowSolr;
import com.ai.modules.engine.service.IEngineResultHandle;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskBatchBreakRuleLogService;
import com.ai.solr.HiveJDBCUtil;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineResultHandle implements IEngineResultHandle {
	@Autowired
    private IApiTaskService taskSV;
	@Autowired
	private ITaskBatchBreakRuleLogService logService;
	
	/**字段顺序*/
	public static final String[] META_FIELD = new String[] {"id", "project", "project_name", "batch_id", 
			"action_type_name", "action_name", "visitid", "itemname", "itemcode", "rule_id", 
			"hosplevel", "orgtype_code", "insurancetype", "visittype_id", "deptid", "diseasecode", 
			"sex_code", "yearage", "dayage", "zy_days_calculate", "item_qty", "item_amt", "fund_cover",
			"sync_time"};		
	
	/**
	 * 
	 * 功能描述：计算结果数据从solr同步到数仓
	 *
	 * @author  zhangly
	 *
	 * @param batch
	 * @param task
	 */
	private void syncSolr2Warehouse(TaskProjectBatch batch, TaskProject task) {
		RTimer rtimer = new RTimer();
    	SimpleDateFormat df = new SimpleDateFormat("H:mm:ss.SSS", Locale.getDefault());
    	df.setTimeZone(TimeZone.getTimeZone("UTC"));
		boolean success = true;
        StringBuilder error = new StringBuilder();
		try {
			TaskProjectBatch entity = new TaskProjectBatch();
			entity.setBatchId(batch.getBatchId());
			entity.setWorkflowState("running");
			entity.setWorkflowRemark("同步hive中");
            taskSV.updateTaskProjectBatch(entity);
	        
            if(HiveJDBCUtil.isHive()) {
            	syncSolr2Hdfs(batch, task);
            } else {
            	syncSolr2Mysql(batch, task);
            }
		} catch(Exception e) {
			success = false;
            error.append(e.getMessage());
            log.error("", e);
		} finally {
			TaskProjectBatch entity = new TaskProjectBatch();
			entity.setBatchId(batch.getBatchId());
			entity.setWorkflowState(success ? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
            entity.setWorkflowRemark("同步完成");
			if (!success) {
                if(error.length() > 2000) {
                	entity.setWorkflowRemark(error.substring(0, 2000));
                } else {
                	entity.setWorkflowRemark(error.toString());
                }
            }
            taskSV.updateTaskProjectBatch(entity);
		}
		log.info("{}批次同步到hive总耗时:{}", new Object[] {batch.getBatchId(), df.format(new Date((long)rtimer.getTime()))});
	}
	
	/**
	 * 
	 * 功能描述：计算结果数据从solr同步到hive
	 *
	 * @author  zhangly
	 *
	 * @param batch
	 * @param task
	 * @throws Exception
	 */
	private void syncSolr2Hdfs(TaskProjectBatch batch, TaskProject task) throws Exception {
		// 数据写入文件
        String importFilePath = HiveJDBCUtil.STORAGE_ROOT + "/hive/" + batch.getBatchId() + ".txt";
        Configuration conf = new Configuration();
		conf.set("fs.default.name", HiveJDBCUtil.FS_DEFAULT_NAME);
		FileSystem fileSystem = FileSystem.get(conf);
		FSDataOutputStream fsDataOutputStream = fileSystem.create(new Path(importFilePath));
		int count = 0;
    	try {    		    		
    		String line = System.getProperty("line.separator");
    		String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;   		
    		List<String> conditionList = new ArrayList<String>();
        	conditionList.add("BATCH_ID:"+batch.getBatchId());
        	Set<String> set = new HashSet<String>();
        	set.add("DRUG");
        	set.add("TREAT");
        	set.add("NEWDRUG");
        	set.add("NEWTREAT");
        	set.add("DRUGUSE");
        	conditionList.add("BUSI_TYPE:("+StringUtils.join(set, " OR ")+")");
        	StringBuilder text = new StringBuilder();
    		count = SolrUtil.exportDocByPager(conditionList, collection, false, (doc, index) -> {
    			text.setLength(0);    		   
    		    for(String field : META_FIELD) {
    		    	if("project".equals(field)) {
    		    		text.append(task.getDataSource());
    		    	} else if("sync_time".equals(field)) {
    		    		text.append(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
    		    	} else {
    		    		if(!"id".equals(field)) {
        		    		field = field.toUpperCase();
        		    	}        		    	
        		    	Object object = doc.get(field);
        		    	if(object==null) {
        		    		text.append("");
        		    	} else {
        		    		String value = object.toString();
        		    		if(object instanceof Collection) {
        		    			value = value.replace("[", "");
        		    			value = value.replace("]", "");
        		    		}
        		    		text.append(value);
        		    	}
    		    	}
    		    	text.append("\t");
    		    }
    		    try {
    		    	fsDataOutputStream.write(text.toString().getBytes());
    		    	fsDataOutputStream.write(line.getBytes());
		        } catch (IOException e) {
		        }
    		});
    		fsDataOutputStream.flush();    		    		
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if(fsDataOutputStream!=null) {
    			fsDataOutputStream.close();
    		}
    	}
    	if(count>0) {
    		//数据写入hive
    		this.loadData(importFilePath, batch.getBatchId(), task.getDataSource());
    		//写通知表
        	this.writeParquetWorkflow(batch.getBatchId(), task.getDataSource());
    	}
    	//删除hdfs文件
    	fileSystem.delete(new Path(importFilePath), true);
	}
	
	/**
	 * 
	 * 功能描述：计算结果数据从solr同步到mysql
	 *
	 * @author  zhangly
	 *
	 * @param batch
	 * @param task
	 * @throws Exception
	 */
	private void syncSolr2Mysql(TaskProjectBatch batch, TaskProject task) throws Exception {
		// 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + ".csv";
        //删除文件
    	if (FileUtil.exist(importFilePath)) {
			FileUtil.del(importFilePath);
		}
        FileWriter writer = new FileWriter(importFilePath, "UTF-8");
		int count = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
    	try {    		    		
    		String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;   		
    		List<String> conditionList = new ArrayList<String>();
        	conditionList.add("BATCH_ID:"+batch.getBatchId());
        	Set<String> set = new HashSet<String>();
        	set.add("DRUG");
        	set.add("TREAT");
        	set.add("NEWDRUG");
        	set.add("NEWTREAT");
        	set.add("DRUGUSE");
        	conditionList.add("BUSI_TYPE:("+StringUtils.join(set, " OR ")+")");
        	
        	int limit = 1000;
        	List<String> lineList = new ArrayList<String>();
        	StringBuilder text = new StringBuilder();
    		count = SolrUtil.exportDocByPager(conditionList, collection, false, (doc, index) -> {
    			text.setLength(0);    		   
    		    for(int i=0, len=META_FIELD.length; i<len; i++) {
    		    	String field = META_FIELD[i];
    		    	if("project".equals(field)) {
    		    		text.append(task.getDataSource());
    		    	} else if("sync_time".equals(field)) {
    		    		text.append(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
    		    	} else {
    		    		if(!"id".equals(field)) {
        		    		field = field.toUpperCase();
        		    	}        		    	
        		    	Object object = doc.get(field);
        		    	if(object==null) {
        		    		text.append("\\N");
        		    	} else {
        		    		String value = object.toString();
        		    		if(object instanceof Collection) {
        		    			value = value.replace("[", "");
        		    			value = value.replace("]", "");
        		    		}
        		    		text.append(value);
        		    	}
    		    	}
    		    	if(i<META_FIELD.length-1) {
    		    		text.append("||");
    		    	}
    		    }
    		    text.append("||").append(task.getDataSource());
    		    text.append("||").append(batch.getBatchId());
    		    lineList.add(text.toString());
    		    if(lineList.size()>=limit) {
		    		writer.writeLines(lineList, true);
		    		lineList.clear();
		    	}
    		});
    		if(lineList.size()>0) {
	    		writer.writeLines(lineList, true);
	    		lineList.clear();
	    	}
    		
    		if(count>0) {
    			String sql = "delete from medical_gbdp.rule_result where batch_id='"+batch.getBatchId()+"'";
    			HiveJDBCUtil.execute(sql);
    			List<String> fieldList = Arrays.asList(META_FIELD);
    			fieldList.add("part_project");
    			fieldList.add("part_batchid");
        		//数据load mysql
        		StringBuilder sb = new StringBuilder();
    			sb.append("load data local infile");
    			sb.append(" '").append(importFilePath).append("'");
    			sb.append(" into table medical_gbdp.rule_result");
    			sb.append(" CHARACTER SET utf8");
    			sb.append(" FIELDS TERMINATED BY '||'");
    			sb.append(" OPTIONALLY ENCLOSED BY ''");
    			sb.append(" LINES TERMINATED BY '\\n'");
    			sb.append(" (").append(StringUtils.join(fieldList, ",")).append(")");
    			HiveJDBCUtil.execute(sb.toString());
    			//写通知表        		
        		sql = "delete from medical_gbdp.workflow_solr where insert_segment='"+batch.getBatchId()+"'";
        		HiveJDBCUtil.execute(sql);        		
        		sql = "insert into medical_gbdp.workflow_solr(project, table_name, insert_segment, table_update_time, update_type, part_project, part_table, part_segment) values(?, ?, ?, ?, ?, ?, ?, ?)";
        		conn = HiveJDBCUtil.getConnection();
    			pstmt = conn.prepareStatement(sql);
        		pstmt.setString(1, task.getDataSource());
        		pstmt.setString(2, "rule_result");
        		pstmt.setString(3, batch.getBatchId());
        		pstmt.setString(4, DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        		pstmt.setString(5, "1");
        		pstmt.setString(6, task.getDataSource());
        		pstmt.setString(7, "rule_result");
        		pstmt.setString(8, batch.getBatchId());
        		pstmt.executeUpdate();
        	}
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {}
			}
    		if(conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
    	}
    	//删除文件
    	if (FileUtil.exist(importFilePath)) {
			FileUtil.del(importFilePath);
		}
	}
	
	@Override
	public void syncSolr2Warehouse(String batchId) {
		TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
        if (batch == null) {
            throw new RuntimeException("未找到任务批次");
        }
        if("running".equals(batch.getWorkflowState())) {
        	throw new RuntimeException("正在同步hive中...");
        }
        TaskProject task = taskSV.findTaskProject(batch.getProjectId());
        if (task == null) {
            throw new RuntimeException("未找到项目");
        }
        this.syncSolr2Warehouse(batch, task);
	}
	
	@Override
	public void syncSolr2WarehouseByThread(String batchId) throws Exception {
		TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
        if (batch == null) {
            throw new RuntimeException("未找到任务批次");
        }
        if("running".equals(batch.getWorkflowState())) {
        	throw new RuntimeException("正在同步hive中...");
        }
        TaskProject task = taskSV.findTaskProject(batch.getProjectId());
        if (task == null) {
            throw new RuntimeException("未找到项目");
        }
        new Thread(() -> {
        	syncSolr2Warehouse(batch, task);
        }).start();
	}

	@Override
	public void write(String batchId) throws Exception {
		TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
        if (batch == null) {
            throw new RuntimeException("未找到任务批次");
        }
        TaskProject task = taskSV.findTaskProject(batch.getProjectId());
        if (task == null) {
            throw new RuntimeException("未找到项目");
        }
        // 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/hive/" + batch.getBatchId() + ".txt";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), 
                		Charset.forName("utf8")));
    	try {
    		String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;   		
    		List<String> conditionList = new ArrayList<String>();
        	conditionList.add("BATCH_ID:"+batch.getBatchId());

        	Set<String> set = new HashSet<String>();
        	set.add("DRUG");
        	set.add("TREAT");
        	set.add("NEWDRUG");
        	set.add("NEWTREAT");
        	set.add("DRUGUSE");
        	StringBuilder text = new StringBuilder();
    		SolrUtil.exportDocByPager(conditionList, collection, false, (doc, index) -> {
    		    String busiType = doc.get("BUSI_TYPE").toString();
    		    if(set.contains(busiType)) {
    		    	text.setLength(0);    		   
        		    for(String field : META_FIELD) {
        		    	if("project".equals(field)) {
        		    		text.append(task.getDataSource());
        		    	} else if("sync_time".equals(field)) {
        		    		text.append(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        		    	} else {
        		    		if(!"id".equals(field)) {
            		    		field = field.toUpperCase();
            		    	}        		    	
            		    	Object object = doc.get(field);
            		    	if(object==null) {
            		    		text.append("");
            		    	} else {
            		    		String value = object.toString();
            		    		if(object instanceof Collection) {
            		    			value = value.replace("[", "");
            		    			value = value.replace("]", "");
            		    		}
            		    		text.append(value);
            		    	}
        		    	}
        		    	text.append("\t");
        		    }
        		    try {
    		            fileWriter.write(text.toString());
    		            fileWriter.write("\r\n");
    		        } catch (IOException e) {
    		        }
    		    }
    		    
    		});
            fileWriter.flush();
    	} catch(Exception e) {    		
    		throw e;
    	} finally {
    		if(fileWriter!=null) {
    			fileWriter.close();
    		}
    	}
	}
	
	public boolean loadData(String txtPath, String batchId, String project) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = HiveJDBCUtil.getConnection();
			stmt = conn.createStatement();
			//数据写入hive临时表
			String sql = "load data inpath '%s' overwrite into table medical_gbdp.medical_rule_result partition(part_batchid='%s', part_project='%s')";
			sql = String.format(sql, txtPath, batchId, project);
			log.info("hive sql:\n{}", sql);
			stmt.execute(sql);
			//hive临时表写入正式表
			StringBuilder sb = new StringBuilder();
			sb.append("insert overwrite table medical_gbdp.rule_result partition(");
			sb.append("part_batchid='").append(batchId).append("', ");
			sb.append("part_project='").append(project).append("')");
			sb.append(" select ");
			int size = 0;
			for(String field : META_FIELD) {
				size++;
				sb.append(field);
				if(size<META_FIELD.length) {
					sb.append(",");
				}				
			}
			sb.append(" from medical_gbdp.medical_rule_result");
			sb.append(" where part_batchid='").append(batchId).append("'");
			sb.append(" and part_project='").append(project).append("'");
			log.info("hive sql:\n{}", sb.toString());
			stmt.execute(sb.toString());
			//删除hive临时表
			sql = "alter table medical_gbdp.medical_rule_result drop partition(part_batchid='%s', part_project='%s')";
			sql = String.format(sql, batchId, project);
			log.info("hive sql:\n{}", sql);
			stmt.execute(sql);
			return true;
		} catch(Exception e) {		
			throw e;
		} finally {
			if(stmt!=null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if(conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}
	
	/**
	 * 
	 * 功能描述：写通知表
	 *
	 * @author  zhangly
	 *
	 * @param batchId
	 * @throws Exception
	 */
	private void writeWorkflow(String batchId, String project) throws Exception {
		String importFilePath = HiveJDBCUtil.STORAGE_ROOT + "/hive/workflow/" + batchId + ".txt";
        Configuration conf = new Configuration();
		conf.set("fs.default.name", HiveJDBCUtil.FS_DEFAULT_NAME);
		FileSystem fileSystem = FileSystem.get(conf);
		FSDataOutputStream fsDataOutputStream = fileSystem.create(new Path(importFilePath));
    	try {
    		StringBuilder text = new StringBuilder();
    		text.append(project);
    		text.append("\t");
    		text.append("rule_result");
    		text.append("\t");
    		text.append(batchId);
    		text.append("\t");
    		text.append(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
    		text.append("\t");
    		text.append("1");
    		fsDataOutputStream.write(text.toString().getBytes());
    		fsDataOutputStream.flush();    		    		
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if(fsDataOutputStream!=null) {
    			fsDataOutputStream.close();
    		}
    	}
    	//写通知表
		String sql = "load data inpath '%s' overwrite into table medical_gbdp.workflow_solr partition(part_project='%s', part_table='rule_result', part_segment='%s')";
		sql = String.format(sql, importFilePath, project, batchId);
		HiveJDBCUtil.execute(sql);
    	//删除hdfs文件
    	fileSystem.delete(new Path(importFilePath), true);
	}
	
	/**
	 * 
	 * 功能描述：写通知表（parquet格式）
	 *
	 * @author  zhangly
	 *
	 * @param batchId
	 * @throws Exception
	 */
	private void writeParquetWorkflow(String batchId, String project) throws Exception {
		String importFilePath = HiveJDBCUtil.STORAGE_ROOT + "/hive/workflow/" + batchId + ".parquet";
        Configuration conf = new Configuration();
		conf.set("fs.default.name", HiveJDBCUtil.FS_DEFAULT_NAME);
		Path dataFile = new Path(importFilePath);
		ParquetWriter<WorkflowSolr> writer = AvroParquetWriter.<WorkflowSolr>builder(dataFile)
			     .withSchema(ReflectData.AllowNull.get().getSchema(WorkflowSolr.class))
			     .withDataModel(ReflectData.get())
			     .withConf(conf)
			     .withCompressionCodec(CompressionCodecName.SNAPPY)
			     .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
			     .build();
		WorkflowSolr entity = new WorkflowSolr();
		entity.setProject(project);
		entity.setTable_name("rule_result");
		entity.setInsert_segment(batchId);
		entity.setTable_update_time(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
		entity.setUpdate_type("1");
		writer.write(entity);
		writer.close();
    	//写通知表
		String sql = "load data inpath '%s' overwrite into table medical_gbdp.workflow_solr partition(part_project='%s', part_table='rule_result', part_segment='%s')";
		sql = String.format(sql, importFilePath, project, batchId);
		HiveJDBCUtil.execute(sql);
		//删除hdfs文件
		FileSystem fileSystem = FileSystem.get(conf);
		fileSystem.delete(dataFile, true);
	}
}
