/**
 * EngineDrugServiceImpl.java	  V1.0   2020年1月2日 上午11:07:02
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.engine.handle.rule.AbsRepeatDrugRuleHandle;
import com.ai.modules.engine.handle.rule.hive.HiveRepeatDrugRuleHandle;
import com.ai.modules.engine.handle.rule.solr.SolrRepeatDrugRuleHandle;
import com.ai.modules.engine.model.EngineCntResult;
import com.ai.modules.engine.model.RTimer;
import com.ai.modules.engine.runnable.EngineDrugRepeatByRuleidRunnable;
import com.ai.modules.engine.runnable.EngineDrugRepeatRunnable;
import com.ai.modules.engine.runnable.EngineRejectedExecutionHandler;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.IEngineGreenplumService;
import com.ai.modules.engine.service.IEngineRepeatDrugService;
import com.ai.modules.engine.service.api.IApiQueryTaskBatchRuleService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineRepeatDrugServiceImpl implements IEngineRepeatDrugService {
	@Autowired
    private IApiTaskService taskSV;
	@Autowired
	private IEngineActionService engineActionService;
	@Autowired
    private IApiQueryTaskBatchRuleService queryBatchRuleSV;
	@Autowired
    private IEngineGreenplumService greenplumService;
	
	@Override
	public void generateMedicalUnreasonableAction(TaskProject task, TaskProjectBatch batch, List<MedicalYbDrug> drugList) throws Exception {
		boolean success = true;
		String error = null;
		String ruleId = drugList.get(0).getParentCode();
    	try {
    		//设置任务状态
    		TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
            entity.setStartTime(new Date());
            entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            entity.setRuleJson(JSON.toJSONString(drugList));
            taskSV.updateTaskBatchBreakRuleLog(batch.getBatchId(), MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, ruleId, entity);
            
            boolean isSolr = !HiveJDBCUtil.enabledProcessGp(); //是否solr计算引擎           
            AbsRepeatDrugRuleHandle handle = null;
            if(isSolr) {
            	//solr模式
            	handle = new SolrRepeatDrugRuleHandle(task, batch, drugList);
            } else {
            	//impala、gp模式
            	handle = new HiveRepeatDrugRuleHandle(task, batch, drugList);
            }
            handle.generateUnreasonableAction();
        } catch(Exception e) {
        	success = false;
			error = e.getMessage();
			throw e;
        } finally {
        	TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
            entity.setEndTime(new Date());
            entity.setStatus(success ? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
            entity.setMessage(error);
            taskSV.updateTaskBatchBreakRuleLog(batch.getBatchId(), MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, ruleId, entity);
        }
	}

	@Override
	public void generateMedicalUnreasonableAction(String batchId) {
		RTimer rtimer = new RTimer();
    	SimpleDateFormat df = new SimpleDateFormat("H:mm:ss.SSS", Locale.getDefault());
    	df.setTimeZone(TimeZone.getTimeZone("UTC"));
    	boolean success = true;
        StringBuilder error = new StringBuilder();
		try {
			List<TaskBatchBreakRule> batchRuleList = taskSV.findTaskBatchBreakRuleByStep(batchId, MedicalConstant.RULE_TYPE_DRUGUSE);
			if(batchRuleList!=null) {
				batchRuleList = batchRuleList.stream().filter(s->"0001".contains(s.getRuleId())).collect(Collectors.toList());
			}
			if(batchRuleList==null || batchRuleList.size()==0) {
				return;
			}
			
			TaskBatchStepItem entity = new TaskBatchStepItem();
			entity.setUpdateTime(new Date());
			entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
			taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_DRUGUSE, entity);

			TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
			if(batch==null) {
				throw new RuntimeException("未找到任务批次");
			}
			TaskProject task = taskSV.findTaskProject(batch.getProjectId());
			if(task==null) {
				throw new RuntimeException("未找到项目");
			}
			taskSV.removeTaskBatchBreakRuleLog(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, AbsRepeatDrugRuleHandle.RULE_TYPE);
			EngineCntResult cntResult = null;
			//先导出已审核的不合规行为结果
			if(!HiveJDBCUtil.enabledStorageGp()) {
				List<String> conditionList = new ArrayList<String>();
	            conditionList.add("ACTION_TYPE_ID:"+AbsRepeatDrugRuleHandle.RULE_TYPE);
				cntResult = engineActionService.importApprovalAction(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, conditionList);			
				// 删除solr历史数据
				this.deleteSolr(batchId);
			}
			//重复用药药品
			List<MedicalYbDrug> drugList = queryBatchRuleSV.queryMedicalDrugrepeat();
			if(drugList!=null && drugList.size()>0) {
				//重复用药按分类分组
				Map<String, List<MedicalYbDrug>> drugGroupMap = drugList.stream().collect(Collectors.groupingBy(MedicalYbDrug::getParentCode));
				int count = drugGroupMap.size();
				log.info("批次重复药品分组数量:"+count);
				List<TaskBatchBreakRuleLog> logList = new ArrayList<TaskBatchBreakRuleLog>();
				//遍历药品组
				for(Map.Entry<String, List<MedicalYbDrug>> entry : drugGroupMap.entrySet()) {					
					TaskBatchBreakRuleLog log = new TaskBatchBreakRuleLog();
					log.setLogId(UUIDGenerator.generate());
					log.setBatchId(batchId);
					log.setItemId(entry.getKey());
					log.setItemName(entry.getValue().get(0).getParentName());
					log.setItemType(MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE);
					log.setCreateTime(DateUtils.getDate());
					log.setStatus(MedicalConstant.RUN_STATE_WAIT);
					log.setItemStype(AbsRepeatDrugRuleHandle.RULE_TYPE);
                    logList.add(log);
				}
				taskSV.saveTaskBatchBreakRuleLog(logList);
				//开启线程池跑任务
				List<EngineDrugRepeatRunnable> runnables = new ArrayList<EngineDrugRepeatRunnable>();
				// 有界队列
				BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(20);
				// 线程池
				ThreadPoolExecutor executor = new ThreadPoolExecutor(EngineUtil.CORE_POOL_SIZE, EngineUtil.MAXIMUM_POOL_SIZ, EngineUtil.KEEP_ALIVE_TIME, TimeUnit.MINUTES, queue);
				executor.setRejectedExecutionHandler(new EngineRejectedExecutionHandler());
                String datasource = SolrUtil.getLoginUserDatasource();
                for (Map.Entry<String, List<MedicalYbDrug>> entry : drugGroupMap.entrySet()) {					
                	EngineDrugRepeatRunnable runnable = new EngineDrugRepeatRunnable(datasource, true, task, batch, entry.getValue());
                	runnables.add(runnable);
                	executor.execute(runnable);
                }
				
				executor.shutdown();
				//等待线程池中任务全部完成
				while (true) {
					if (executor.isTerminated()) {
						break;
					}
					try {
						Thread.sleep(5000L);
					} catch (InterruptedException e) {
					}
				}
				for(EngineDrugRepeatRunnable runnable : runnables) {
					if(!runnable.isSuccess()) {
						success = false;
						error.append(runnable.getMessage()).append("\n");
					}
				}
			}
			if(!HiveJDBCUtil.enabledStorageGp()) {
				if(cntResult.isSuccess() && cntResult.getCount()>0) {
	    			//最后导入已审核的不合规行为结果
	    			SolrUtil.importJsonToSolrNotDeleteFile(cntResult.getMessage(), EngineUtil.MEDICAL_UNREASONABLE_ACTION);
	    		}
			}
		} catch(Exception e) {
			success = false;
	    	error.append(e.getMessage());
	    	log.error("", e);
		} finally {
			TaskBatchStepItem entity = new TaskBatchStepItem();
            entity.setUpdateTime(new Date());
            entity.setEndTime(new Date());
            entity.setStatus(success ? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
            if (!success) {
                if(error.length() > 2000) {
                	entity.setMsg(error.substring(0, 2000));
                } else {
                	entity.setMsg(error.toString());
                }
            }
			taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_DRUGUSE, entity);
		}
		log.info(batchId+"批次重复药品总耗时:" + df.format(new Date((long)rtimer.getTime())));
	}

	private void deleteSolr(String batchId) throws Exception {
		SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, "RULE_TYPE:"+AbsRepeatDrugRuleHandle.RULE_TYPE+" AND BATCH_ID:" + batchId);
		SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, "BUSI_TYPE:DRUGUSE AND ACTION_TYPE_ID:"+AbsRepeatDrugRuleHandle.RULE_TYPE+" AND BATCH_ID:" + batchId);
	}
	
	@Override
	public void generateUnreasonableActionFailRerun(String batchId) {
		List<MedicalYbDrug> drugList = queryBatchRuleSV.queryMedicalDrugrepeatFail(batchId);
        if (drugList != null && drugList.size() > 0) {
        	TaskBatchStepItem entity = new TaskBatchStepItem();
            entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_DRUGUSE, entity);
    		
    		List<String> codes = new ArrayList<String>();
    		for(MedicalYbDrug drug : drugList) {
    			codes.add(drug.getParentCode());
    		}
    		taskSV.waitTaskBatchBreakRuleLog(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, codes);
            
            String ds = SolrUtil.getLoginUserDatasource();
            for(MedicalYbDrug drug : drugList) {
    			EngineDrugRepeatByRuleidRunnable runnable = new EngineDrugRepeatByRuleidRunnable(ds, batchId, drug.getParentCode());
    	    	ThreadUtils.THREAD_DRUGITEM_POOL.add(runnable);
    		}
        }        
	}

	@Override
	public void generateUnreasonableAction(String batchId, String ruleId) {
		boolean success = true;
        StringBuilder error = new StringBuilder();
        try {
            TaskBatchStepItem entity = new TaskBatchStepItem();
            entity.setUpdateTime(new Date());
            entity.setStartTime(new Date());
            entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_DRUGUSE, entity);

            TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
            if (batch == null) {
                throw new RuntimeException("未找到任务批次");
            }
            TaskProject task = taskSV.findTaskProject(batch.getProjectId());
            if (task == null) {
                throw new RuntimeException("未找到项目");
            }
            //先导出已审核的不合规行为结果
            EngineCntResult cntResult = null;
			if(!HiveJDBCUtil.enabledStorageGp()) {
				cntResult = engineActionService.importApprovalActionFromRule(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, ruleId);
	            //删除历史solr数据
	            engineActionService.deleteSolrByRule(batchId, ruleId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, false);
			} else {
				//删除gp
	            greenplumService.removeRule(batchId, ruleId);
			}

            List<MedicalYbDrug> drugList = queryBatchRuleSV.queryMedicalDrugrepeat(ruleId);
            this.generateMedicalUnreasonableAction(task, batch, drugList);
            
            if(!HiveJDBCUtil.enabledStorageGp()) {
	            if(cntResult.isSuccess() && cntResult.getCount()>0) {
	    			//最后回填已审核的不合规行为结果
	    			SolrUtil.importJsonToSolr(cntResult.getMessage(), EngineUtil.MEDICAL_UNREASONABLE_ACTION);
	    		}
            } else {
            	//回填gp
                greenplumService.backFillRule(batchId, ruleId);
            }           
        } catch (Exception e) {
            success = false;
            error.append(e.getMessage());
            log.error("", e);
        } finally {        
        	TaskBatchStepItem entity = new TaskBatchStepItem();
            entity.setUpdateTime(new Date());
            entity.setEndTime(new Date());
            if (!success) {            	
                entity.setStatus(MedicalConstant.RUN_STATE_ABNORMAL);
                if(error.length() > 2000) {
                	entity.setMsg(error.substring(0, 2000));
                } else {
                	entity.setMsg(error.toString());
                }                
            } else {
            	Map<String, Integer> statusMap = taskSV.groupByTaskBatchBreakRuleLog(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE);
            	if(statusMap.containsKey(MedicalConstant.RUN_STATE_ABNORMAL)) {
            		entity.setStatus(MedicalConstant.RUN_STATE_ABNORMAL);
                    entity.setMsg("出现异常");
            	} else if(statusMap.containsKey(MedicalConstant.RUN_STATE_RUNNING)
            			|| statusMap.containsKey(MedicalConstant.RUN_STATE_WAIT)) {
            		entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            	} else {
            		entity.setStatus(MedicalConstant.RUN_STATE_NORMAL);
            	}
            }
            taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_DRUGUSE, entity);
        }
	}
	
	@Override
	public void generateUnreasonableActionByThreadPool(String batchId, String ruleId) {
    	TaskBatchStepItem entity = new TaskBatchStepItem();
        entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
        taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_DRUGUSE, entity);
        
        TaskBatchBreakRuleLog log = new TaskBatchBreakRuleLog();
        log.setStatus(MedicalConstant.RUN_STATE_WAIT);
        taskSV.updateTaskBatchBreakRuleLog(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, ruleId, log);
		String ds = SolrUtil.getLoginUserDatasource();
		EngineDrugRepeatByRuleidRunnable runnable = new EngineDrugRepeatByRuleidRunnable(ds, batchId, ruleId);
    	ThreadUtils.THREAD_DRUGITEM_POOL.add(runnable);
	}
}
