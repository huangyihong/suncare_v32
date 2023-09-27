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
import com.ai.modules.engine.handle.rule.AbsRuleHandle;
import com.ai.modules.engine.handle.rule.RuleHandleFactory;
import com.ai.modules.engine.model.EngineCntResult;
import com.ai.modules.engine.model.RTimer;
import com.ai.modules.engine.runnable.EngineRejectedExecutionHandler;
import com.ai.modules.engine.runnable.EngineRuleConfigByRuleidRunnable;
import com.ai.modules.engine.runnable.EngineRuleConfigRunnable;
import com.ai.modules.engine.runnable.EngineTrailRuleConfigRunnable;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.IEngineGreenplumService;
import com.ai.modules.engine.service.IEngineRuleService;
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
public class EngineRuleServiceImpl implements IEngineRuleService {
	@Autowired
    private IApiTaskService taskSV;
    @Autowired
    private IApiQueryTaskBatchRuleService queryBatchRuleSV;
    @Autowired
	private IEngineActionService engineActionService;
    @Autowired
    private IEngineGreenplumService greenplumService;
	
	@Override
    public void generateUnreasonableAction(String batchId, String busiType) {
		String stepType = this.getStepType(busiType);
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
            taskSV.updateTaskBatchStepItem(batchId, stepType, entity);

            TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
            if (batch == null) {
                throw new RuntimeException("未找到任务批次");
            }
            TaskProject task = taskSV.findTaskProject(batch.getProjectId());
            if (task == null) {
                throw new RuntimeException("未找到项目");
            }
            taskSV.removeTaskBatchBreakRuleLog(batchId, busiType);
            //先导出已审核的不合规行为结果
            EngineCntResult cntResult = null;
            if(!HiveJDBCUtil.enabledStorageGp()) {
            	cntResult = engineActionService.importApprovalAction(batchId, busiType);                
                //删除历史solr数据
                engineActionService.deleteSolr(batchId, busiType, false);
            } else {
            	//删除gp
            	greenplumService.remove(batchId, busiType);
            }
            
			List<MedicalRuleConfig> ruleList = queryBatchRuleSV.queryMedicalRuleConfig(batchId, stepType);
            if (ruleList != null && ruleList.size() > 0) {
                int count = ruleList.size();
				log.info("批次规则({})数量:{}", busiType, count);
				List<TaskBatchBreakRuleLog> logList = new ArrayList<TaskBatchBreakRuleLog>();
                //遍历药品，写运行日志
				for (MedicalRuleConfig rule : ruleList) {
					TaskBatchBreakRuleLog log = new TaskBatchBreakRuleLog();
					log.setLogId(UUIDGenerator.generate());
					log.setBatchId(batchId);
					log.setItemId(rule.getRuleId());
					log.setItemName(rule.getItemNames());
					log.setItemType(busiType);
					log.setCreateTime(DateUtils.getDate());
					log.setStatus(MedicalConstant.RUN_STATE_WAIT);					
                    logList.add(log);                    
				}
				taskSV.saveTaskBatchBreakRuleLog(logList);
				//开启线程池跑任务
				List<EngineRuleConfigRunnable> runnables = new ArrayList<EngineRuleConfigRunnable>();
				// 有界队列
				BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(20);
				// 线程池
				ThreadPoolExecutor executor = new ThreadPoolExecutor(EngineUtil.CORE_POOL_SIZE, EngineUtil.MAXIMUM_POOL_SIZ, EngineUtil.KEEP_ALIVE_TIME, TimeUnit.MINUTES, queue);
				executor.setRejectedExecutionHandler(new EngineRejectedExecutionHandler());
                String datasource = SolrUtil.getLoginUserDatasource();
				for (MedicalRuleConfig rule : ruleList) {					
					EngineRuleConfigRunnable runnable = new EngineRuleConfigRunnable(datasource, false, task, batch, rule);
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
				for(EngineRuleConfigRunnable runnable : runnables) {
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
        		greenplumService.backFill(batchId, busiType);
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
            taskSV.updateTaskBatchStepItem(batchId, stepType, entity);
        }
        log.info("{}批次规则({})总耗时:{}", new Object[] {batchId, busiType, df.format(new Date((long)rtimer.getTime()))});
    }
	
	@Override
    public void generateMedicalUnreasonableAction(TaskProject task, TaskProjectBatch batch, MedicalRuleConfig rule) throws Exception {
    	String busiType = this.getBusiType(rule.getRuleType());
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
            taskSV.updateTaskBatchBreakRuleLog(batch.getBatchId(), busiType, rule.getRuleId(), entity);
                        
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
            entity.setMessage(success ? "成功" : error);
            taskSV.updateTaskBatchBreakRuleLog(batch.getBatchId(), busiType, rule.getRuleId(), entity);
        }
    }
		
    @Override
    public void generateUnreasonableActionByRule(String batchId, String ruleId) {    	
        boolean success = true;
        StringBuilder error = new StringBuilder();
        String stepType = null;
        String busiType = null;
        try {
        	MedicalRuleConfig rule = queryBatchRuleSV.queryMedicalRuleConfig(ruleId);
    		stepType = this.getStepType(rule.getRuleType());
        	busiType = this.getBusiType(rule.getRuleType());
        	
        	TaskBatchStepItem entity = new TaskBatchStepItem();
            entity.setUpdateTime(new Date());
            entity.setStartTime(new Date());
            entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            taskSV.updateTaskBatchStepItem(batchId, stepType, entity);

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
				cntResult = engineActionService.importApprovalActionFromRule(batchId, busiType, ruleId);
	            //删除历史solr数据
	            engineActionService.deleteSolrByRule(batchId, ruleId, busiType, false);
			} else {
				//删除gp
	            greenplumService.removeRule(batchId, ruleId);
			}
            
            this.generateMedicalUnreasonableAction(task, batch, rule);
            if(!HiveJDBCUtil.enabledStorageGp()) {
            	if(cntResult.isSuccess() && cntResult.getCount()>0) {
        			//最后回填已审核的不合规行为结果
        			SolrUtil.importJsonToSolrNotDeleteFile(cntResult.getMessage(), EngineUtil.MEDICAL_UNREASONABLE_ACTION);
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
            	Map<String, Integer> statusMap = taskSV.groupByTaskBatchBreakRuleLog(batchId, busiType);
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
            taskSV.updateTaskBatchStepItem(batchId, stepType, entity);
        }
    }
    
    @Override
	public void generateUnreasonableActionByThreadPool(String batchId, String ruleId) {
    	MedicalRuleConfig rule = queryBatchRuleSV.queryMedicalRuleConfig(ruleId);
    	if(rule==null) {
    		throw new RuntimeException("未找到规则");
    	}
    	String stepType = this.getStepType(rule.getRuleType());
    	String busiType = this.getBusiType(rule.getRuleType());
    	//更新批次进度状态
    	TaskBatchStepItem entity = new TaskBatchStepItem();
        entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
        taskSV.updateTaskBatchStepItem(batchId, stepType, entity);

        TaskBatchBreakRuleLog log = new TaskBatchBreakRuleLog();
        log.setStatus(MedicalConstant.RUN_STATE_WAIT);
        taskSV.updateTaskBatchBreakRuleLog(batchId, busiType, ruleId, log);
		String ds = SolrUtil.getLoginUserDatasource();
		EngineRuleConfigByRuleidRunnable runnable = new EngineRuleConfigByRuleidRunnable(ds, batchId, ruleId);
    	ThreadUtils.THREAD_DRUGITEM_POOL.add(runnable);
	}

	@Override
	public void generateUnreasonableActionFailRerun(String batchId, String busiType) {
		String stepType = this.getStepType(busiType);
		List<MedicalRuleConfig> ruleList = queryBatchRuleSV.queryMedicalRuleConfigFail(batchId, stepType);
        if (ruleList == null || ruleList.size() == 0) {
        	return;
        }
        //更新批次进度状态
    	TaskBatchStepItem entity = new TaskBatchStepItem();
        entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
        taskSV.updateTaskBatchStepItem(batchId, stepType, entity);
		
		List<String> codes = new ArrayList<String>();
		for(MedicalRuleConfig rule : ruleList) {
			codes.add(rule.getRuleId());
		}
		taskSV.waitTaskBatchBreakRuleLog(batchId, busiType, codes);		
        
        String ds = SolrUtil.getLoginUserDatasource();
        for(MedicalRuleConfig rule : ruleList) {
			EngineRuleConfigByRuleidRunnable runnable = new EngineRuleConfigByRuleidRunnable(ds, batchId, rule.getRuleId());
	    	ThreadUtils.THREAD_DRUGITEM_POOL.add(runnable);
		}
	}
	
	/**
	 * 
	 * 功能描述：新版收费合规在项目批次中的步骤类型
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月21日 上午10:31:01</p>
	 *
	 * @param busiType
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private String getStepType(String busiType) {
		String stepType = null;
		switch(busiType) {
			case "DRUG":
				stepType = MedicalConstant.RULE_TYPE_NEWDRUG;
				break;
			case "NEWDRUG":
				stepType = MedicalConstant.RULE_TYPE_NEWDRUG;
				break;
			case "TREAT":
				stepType = MedicalConstant.RULE_TYPE_NEWTREAT;
				break;
			case "NEWTREAT":
				stepType = MedicalConstant.RULE_TYPE_NEWTREAT;
				break;	
			case "CHARGE":
				stepType = MedicalConstant.RULE_TYPE_NEWCHARGE;
				break;
			default:
				stepType = MedicalConstant.RULE_TYPE_NEWCHARGE;
				break;
		}
		return stepType;
	}
	
	/**
	 * 
	 * 功能描述：新版收费合规的业务类型
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年12月21日 上午10:31:29</p>
	 *
	 * @param ruleType
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private String getBusiType(String ruleType) {
		String busiType = ruleType;
        switch(ruleType) {
		case "DRUG":
			busiType = "NEWDRUG";
			break;
		case "CHARGE":
			busiType = MedicalConstant.ENGINE_BUSI_TYPE_NEWCHARGE;
			break;
		case "TREAT":
			busiType = MedicalConstant.ENGINE_BUSI_TYPE_NEWTREAT;
			break;	
		default:
			busiType = ruleType;
			break;
        }
        return busiType;
	}

	@Override
	public void trailActionThreadPool(String ruleId, String etlSource) {
		String ds = SolrUtil.getLoginUserDatasource();
		MedicalRuleConfig rule = queryBatchRuleSV.queryMedicalRuleConfig(ruleId);
		String batchId = ruleId;
        taskSV.removeTaskBatchStepItem(batchId, ds);
        Date now = new Date();
        String status = MedicalConstant.RUN_STATE_WAIT;
        TaskBatchStepItem step = new TaskBatchStepItem();
        step.setId(UUIDGenerator.generate());
        step.setBatchId(batchId);
        step.setStep(1);
        step.setItemId(this.getStepType(rule.getRuleType()));
        step.setCreateTime(now);
        step.setStatus(status);
        step.setDataSource(ds);
        taskSV.saveTaskBatchStepItem(step);
                
        ThreadUtils.THREAD_TRAIL_POOL.add(new EngineTrailRuleConfigRunnable(ds, ruleId, etlSource));
	}

	@Override
	public void trailAction(String ruleId, String etlSource, String datasource) {
		String batchId = ruleId;
		boolean success = true;
        String error = null;
        MedicalRuleConfig rule = queryBatchRuleSV.queryMedicalRuleConfig(ruleId);
        try {
        	String busiType = this.getBusiType(rule.getRuleType());
            //删除历史数据
        	String where = "RULE_TYPE:%s AND BATCH_ID:%s";
        	where = String.format(where, busiType, batchId);
            SolrUtil.delete(EngineUtil.MEDICAL_TRAIL_DRUG_ACTION, where);
            where = "BUSI_TYPE:%s AND BATCH_ID:%s";
        	where = String.format(where, busiType, batchId);
        	SolrUtil.delete(EngineUtil.MEDICAL_TRAIL_ACTION, where);

            TaskBatchStepItem entity = new TaskBatchStepItem();
            entity.setUpdateTime(new Date());
            entity.setStartTime(new Date());
            entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            entity.setDataSource(datasource);
            taskSV.updateTaskBatchStepItem(batchId, this.getStepType(rule.getRuleType()), datasource, entity);

            List<MedicalRuleConditionSet> ruleConditionList = queryBatchRuleSV.queryMedicalRuleConditionSet(ruleId);
            TaskProject task = new TaskProject();
            task.setProjectId(batchId);
            task.setDataSource(SolrUtil.getLoginUserDatasource());
            TaskProjectBatch batch = new TaskProjectBatch();
            batch.setBatchId(batchId);
            batch.setEtlSource(etlSource);
            //规则计算引擎
            RuleHandleFactory factory = new RuleHandleFactory(task, batch, true, rule, ruleConditionList);
        	AbsRuleHandle handle = factory.build();
        	handle.generateUnreasonableAction();
        } catch (Exception e) {
        	log.info("", e);
            error = e.getMessage();
            success = false;
        } finally {
            TaskBatchStepItem entity = new TaskBatchStepItem();
            entity.setUpdateTime(new Date());
            entity.setEndTime(new Date());
            entity.setStatus(success ? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
            if (!success) {
                error = error.length() > 2000 ? error.substring(0, 2000) : error;
                entity.setMsg(error);
            }
            taskSV.updateTaskBatchStepItem(batchId, this.getStepType(rule.getRuleType()), entity);
        }
	}
}
