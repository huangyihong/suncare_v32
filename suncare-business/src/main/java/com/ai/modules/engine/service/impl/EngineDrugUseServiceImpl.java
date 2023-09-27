/**
 * EngineDrugUseServiceImpl.java	  V1.0   2020年11月10日 上午10:53:02
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

import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.entity.MedicalYbDrug;
import com.ai.modules.engine.handle.rule.AbsRuleHandle;
import com.ai.modules.engine.handle.rule.RuleHandleFactory;
import com.ai.modules.engine.model.EngineCntResult;
import com.ai.modules.engine.model.RTimer;
import com.ai.modules.engine.runnable.EngineDrugUseByRuleidRunnable;
import com.ai.modules.engine.runnable.EngineDrugUseRunnable;
import com.ai.modules.engine.runnable.EngineRejectedExecutionHandler;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.IEngineDrugUseService;
import com.ai.modules.engine.service.IEngineGreenplumService;
import com.ai.modules.engine.service.IEngineRepeatDrugService;
import com.ai.modules.engine.service.api.IApiQueryTaskBatchRuleService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineDrugUseServiceImpl implements IEngineDrugUseService {
	@Autowired
    private IApiTaskService taskSV;
    @Autowired
    private IApiQueryTaskBatchRuleService queryBatchRuleSV;
    @Autowired
	private IEngineActionService engineActionService;
    @Autowired
    private IEngineRepeatDrugService repeatDrugSV;
    @Autowired
    private IEngineGreenplumService greenplumService;
	
	@Override
    public void generateUnreasonableAction(String batchId) {
		RTimer rtimer = new RTimer();
    	SimpleDateFormat df = new SimpleDateFormat("H:mm:ss.SSS", Locale.getDefault());
    	df.setTimeZone(TimeZone.getTimeZone("UTC"));
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
            taskSV.removeTaskBatchBreakRuleLog(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE);
            //先导出已审核的不合规行为结果
            EngineCntResult cntResult = null;
            if(!HiveJDBCUtil.enabledStorageGp()) {
            	List<String> conditionList = new ArrayList<String>();
                conditionList.add("ACTION_TYPE_ID:DRUGUSE");
    			cntResult = engineActionService.importApprovalAction(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, conditionList);                
                //删除历史solr数据
                engineActionService.deleteSolr(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, false);
            } else {
            	//删除gp
            	greenplumService.remove(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE);
            }         
            
            List<MedicalRuleConfig> ruleList = queryBatchRuleSV.queryMedicalRuleConfig(batchId, MedicalConstant.RULE_TYPE_DRUGUSE);
            if (ruleList != null && ruleList.size() > 0) {
                int count = ruleList.size();
				log.info("批次合理用药规则数量:"+count);
				List<TaskBatchBreakRuleLog> logList = new ArrayList<TaskBatchBreakRuleLog>();
                //遍历药品，写运行日志
				for (MedicalRuleConfig rule : ruleList) {
					TaskBatchBreakRuleLog log = new TaskBatchBreakRuleLog();
					log.setLogId(UUIDGenerator.generate());
					log.setBatchId(batchId);
					log.setItemId(rule.getRuleId());
					log.setItemName(rule.getItemNames());
					log.setItemType(MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE);
					log.setCreateTime(DateUtils.getDate());
					log.setStatus(MedicalConstant.RUN_STATE_WAIT);
					log.setItemStype(MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE);
                    logList.add(log);                    
				}
				taskSV.saveTaskBatchBreakRuleLog(logList);
				//开启线程池跑任务
				List<EngineDrugUseRunnable> runnables = new ArrayList<EngineDrugUseRunnable>();
				// 有界队列
				BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(20);
				// 线程池
				ThreadPoolExecutor executor = new ThreadPoolExecutor(EngineUtil.CORE_POOL_SIZE, EngineUtil.MAXIMUM_POOL_SIZ, EngineUtil.KEEP_ALIVE_TIME, TimeUnit.MINUTES, queue);
				executor.setRejectedExecutionHandler(new EngineRejectedExecutionHandler());
                String datasource = SolrUtil.getLoginUserDatasource();
				for (MedicalRuleConfig rule : ruleList) {					
                	EngineDrugUseRunnable runnable = new EngineDrugUseRunnable(datasource, true, task, batch, rule);
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
				for(EngineDrugUseRunnable runnable : runnables) {
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
            } else {
            	//回填gp
        		greenplumService.backFill(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE);
            }
        } catch (Exception e) {
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
        log.info(batchId+"批次合理用药规则总耗时:" + df.format(new Date((long)rtimer.getTime())));
        
        //合理用药-重复用药规则        
        repeatDrugSV.generateMedicalUnreasonableAction(batchId);
    }
	
	@Override
    public void generateMedicalUnreasonableDrugAction(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule) throws Exception {
    	boolean success = true;
		String error = null;
    	try {
            List<MedicalRuleConditionSet> ruleConditionList = queryBatchRuleSV.queryMedicalRuleConditionSet(rule.getRuleId());
    		//设置任务状态
    		TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
            entity.setStartTime(new Date());
            entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            entity.setRuleJson(JSON.toJSONString(rule));
            entity.setWhereJson(JSON.toJSONString(ruleConditionList));
            taskSV.updateTaskBatchBreakRuleLog(batch.getBatchId(), MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, rule.getRuleId(), entity);
                        
            //规则计算引擎
            RuleHandleFactory factory = new RuleHandleFactory(task, batch, false, rule, ruleConditionList);
        	AbsRuleHandle handle = factory.build();
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
            taskSV.updateTaskBatchBreakRuleLog(batch.getBatchId(), MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, rule.getRuleId(), entity);
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
			EngineCntResult cntResult = engineActionService.importApprovalActionFromRule(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, ruleId);
            //删除历史solr数据
            engineActionService.deleteSolrByRule(batchId, ruleId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, false);

            MedicalRuleConfig rule = queryBatchRuleSV.queryMedicalRuleConfig(ruleId);
            this.generateMedicalUnreasonableDrugAction(task, batch, rule);
            
            if(cntResult.isSuccess() && cntResult.getCount()>0) {
    			//最后回填已审核的不合规行为结果
    			SolrUtil.importJsonToSolr(cntResult.getMessage(), EngineUtil.MEDICAL_UNREASONABLE_ACTION);
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
    	MedicalRuleConfig rule = queryBatchRuleSV.queryMedicalRuleConfig(ruleId);
    	if(rule!=null) {
    		//不合理用药
    		TaskBatchStepItem entity = new TaskBatchStepItem();
            entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_DRUGUSE, entity);
            
            TaskBatchBreakRuleLog log = new TaskBatchBreakRuleLog();
            log.setStatus(MedicalConstant.RUN_STATE_WAIT);
            taskSV.updateTaskBatchBreakRuleLog(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, ruleId, log);
    		String ds = SolrUtil.getLoginUserDatasource();
    		EngineDrugUseByRuleidRunnable runnable = new EngineDrugUseByRuleidRunnable(ds, batchId, ruleId);
        	ThreadUtils.THREAD_DRUGITEM_POOL.add(runnable);
    	} else {
    		//不合理用药-重复用药
    		List<MedicalYbDrug> drugList = queryBatchRuleSV.queryMedicalDrugrepeat(ruleId);
    		if(drugList!=null) {
    			repeatDrugSV.generateUnreasonableActionByThreadPool(batchId, ruleId);
    		}
    	}
	}

	@Override
	public void generateUnreasonableActionFailRerun(String batchId) {
		List<MedicalRuleConfig> ruleList = queryBatchRuleSV.queryMedicalRuleConfigFail(batchId, MedicalConstant.RULE_TYPE_DRUGUSE);
        if (ruleList != null && ruleList.size() > 0) {
        	TaskBatchStepItem entity = new TaskBatchStepItem();
            entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_DRUGUSE, entity);
    		
    		List<String> codes = new ArrayList<String>();
    		for(MedicalRuleConfig rule : ruleList) {
    			codes.add(rule.getRuleId());
    		}
    		taskSV.waitTaskBatchBreakRuleLog(batchId, MedicalConstant.ENGINE_BUSI_TYPE_DRUGUSE, codes);
            
            String ds = SolrUtil.getLoginUserDatasource();
            for(MedicalRuleConfig rule : ruleList) {
    			EngineDrugUseByRuleidRunnable runnable = new EngineDrugUseByRuleidRunnable(ds, batchId, rule.getRuleId());
    	    	ThreadUtils.THREAD_DRUGITEM_POOL.add(runnable);
    		}
        }        
        
        //合理用药-重复用药规则
        repeatDrugSV.generateUnreasonableActionFailRerun(batchId);
	}
}
