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

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.secondary.AbsSecondaryHandle;
import com.ai.modules.engine.handle.secondary.ChargeOnedayMutexHandle;
import com.ai.modules.engine.model.EngineCntResult;
import com.ai.modules.engine.model.EngineDrugRuleEntity;
import com.ai.modules.engine.model.EngineLimitScopeEnum;
import com.ai.modules.engine.model.RTimer;
import com.ai.modules.engine.model.dto.ActionTypeDTO;
import com.ai.modules.engine.model.rule.AbsEngineParamRule;
import com.ai.modules.engine.model.rule.BaseEngineParamRule;
import com.ai.modules.engine.model.rule.EngineParamChargeGrpRule;
import com.ai.modules.engine.model.rule.EngineParamFrequencyGrpRule;
import com.ai.modules.engine.model.rule.EngineParamFrequencyRule;
import com.ai.modules.engine.model.rule.EngineParamIndicationRule;
import com.ai.modules.engine.model.rule.EngineParamMasterInfoRule;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.runnable.EngineItemRunnable;
import com.ai.modules.engine.runnable.EngineRejectedExecutionHandler;
import com.ai.modules.engine.runnable.EngineRunnable;
import com.ai.modules.engine.runnable.EngineTrailTreatRunnable;
import com.ai.modules.engine.runnable.EngineTreatRunnable;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.IEngineTargetService;
import com.ai.modules.engine.service.IEngineTreatService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.service.api.IApiQueryTaskBatchRuleService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineTreatServiceImpl implements IEngineTreatService {
	@Autowired
    private IApiTaskService taskSV;
    @Autowired
    private IApiQueryTaskBatchRuleService queryBatchRuleSV;
	@Autowired
    private IApiDictService dictSV;
	@Autowired
    private IEngineTargetService targetService;
	@Autowired
	private IEngineActionService engineActionService;

	@Override
	public void generateMedicalUnreasonableTreatAction(TaskProject task, TaskProjectBatch batch, String itemCode, List<MedicalDrugRule> ruleList) throws Exception {
		boolean success = true;
		String error = null;
		try {
			//设置任务状态
    		TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
            entity.setStartTime(new Date());
            entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            entity.setRuleJson(JSON.toJSONString(ruleList));
            taskSV.updateTaskBatchBreakRuleLog(batch.getBatchId(), "treat", itemCode, entity);

            //规则包含的所有限制范围
        	Set<String> limitScopeSet = new HashSet<String>();
			List<String> conditionList = new ArrayList<String>();
			StringBuilder sb = new StringBuilder();
			for(int i=0, len=ruleList.size(); i<len; i++) {
				MedicalDrugRule rule = ruleList.get(i);
				EngineDrugRuleEntity ruleEntity = new EngineDrugRuleEntity(rule);
				if(ruleEntity.isEmptyScope()) {
	      			throw new EngineBizException(rule.getRuleId()+"["+rule.getDrugNames()+"]未设置限制范围！");
	      		}
				if(i>0) {
					sb.append(" OR ");
				}
				limitScopeSet.addAll(ruleEntity.getLimitScopeSet());
				List<String> tempList = this.parseTreatRuleCondition(ruleEntity);
				if(!tempList.isEmpty()) {
					sb.append("(");
					for(int j=0; j<tempList.size(); j++) {
						String condition = tempList.get(j);
						if(j>0) {
							sb.append(" AND ");
						}
						sb.append("(").append(condition).append(")");
					}
					sb.append(")");
				}
			}

			conditionList.add("*:* -("+sb.toString()+")");
			conditionList.add("ITEMCODE:"+itemCode);
			conditionList.add("ITEM_QTY:{0 TO *}");
			conditionList.add("ITEM_AMT:{0 TO *}");
			//项目过滤条件
			ProjectFilterWhereVO filterVO = engineActionService.filterCondition(task, false);
			if(StringUtils.isNotBlank(filterVO.getCondition())) {
            	conditionList.add(filterVO.getCondition());
            }
            if(filterVO.isDiseaseFilter() && EngineUtil.ruleExistsDisease(ruleList)) {
            	//疾病映射不全过滤
            	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_DIAG", "VISITID", "VISITID");
            	conditionList.add("*:* -"+plugin.parse()+"-DISEASENAME:?*");
            }
			if(StringUtils.isNotBlank(batch.getEtlSource())) {
				conditionList.add("ETL_SOURCE:"+batch.getEtlSource());
			}
			String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
			String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
			batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
			batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
			//业务数据时间范围限制
			sb.setLength(0);
			sb.append("VISITDATE:");
			sb.append("[").append(batch_startTime).append(" TO ").append(batch_endTime).append("]");
			conditionList.add(sb.toString());
			for(MedicalDrugRule rule : ruleList) {
				//规则的数据时间范围限制
				sb.setLength(0);
				sb.append("VISITDATE:");
				sb.append("[").append(DateUtil.format(rule.getStartTime(), "yyyy-MM-dd"));
				sb.append(" TO ");
				sb.append(DateUtil.format(rule.getEndTime(), "yyyy-MM-dd")).append("]");
				conditionList.add(sb.toString());
			}
			if(StringUtils.isNotBlank(batch.getCustomFilter())) {
            	//自定义数据范围限制
				//conditionList.add(batch.getCustomFilter());
            	String value = batch.getCustomFilter();
            	value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
            	conditionList.add("ORGID:"+value);
            }
			//基金支出金额>0
			conditionList.add("FUND_COVER:{0 TO *}");
			//自付比例<0
			conditionList.add("SELFPAY_PROP_MIN:[0 TO 1}");			
			//添加过滤掉指标为空值的条件
			for(int i=0, len=ruleList.size(); i<len; i++) {
				MedicalDrugRule rule = ruleList.get(i);
				List<String> ignoreNullList = engineActionService.ignoreNullWhere(rule);
				if(ignoreNullList!=null && ignoreNullList.size()>0) {
	  				conditionList.addAll(ignoreNullList);
	  			}
			}

			//一个诊疗项目只有一条规则且限定范围唯一时，直接沉淀限定范围无需再次计算
	  		EngineLimitScopeEnum limitScopeEnumTemp = null;
	  		if(ruleList.size()==1) {
	  			EngineDrugRuleEntity ruleEntity = new EngineDrugRuleEntity(ruleList.get(0));
	  			if(ruleEntity.singleScope()) {
	  				limitScopeEnumTemp = ruleEntity.getFirstScopeEnum();
	  	  		}
	  		}
	  		final EngineLimitScopeEnum limitScopeEnum = limitScopeEnumTemp;

			// 数据写入文件
	        String importFilePath = SolrUtil.importFolder + "/" + EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION + "/" + batch.getBatchId() + "/" + itemCode + ".json";
	        BufferedWriter fileWriter = new BufferedWriter(
	                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
	        //写文件头
	        fileWriter.write("[");
	        //使用备份solr服务器
	        boolean slave = true;
	        ActionTypeDTO dto = this.getActionTypeDTO();
	        Map<String, MedicalActionDict> actionDictMap = dictSV.queryActionDict();
	        int count = SolrUtil.exportByPager(conditionList, EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, slave, (map, index) -> {
	            // 循环一条数据写入文件
	        	engineActionService.writerJson(fileWriter, map, task, batch, itemCode, ruleList, limitScopeEnum, dto, actionDictMap);
	        });
	        // 文件尾
	        fileWriter.write("]");
	        fileWriter.flush();
	        fileWriter.close();

	        //导入solr
	      	if(count>0) {
	      		SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, slave);
	      		//计算违规限定范围
	      		if(limitScopeEnum==null) {
	      			for(MedicalDrugRule rule : ruleList) {
	          			targetService.calculateBreakActionTarget(EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, slave, batch.getBatchId(), rule, null, false);
	          		}
	      			//删除限定范围是空的数据
		    	    String where = "BATCH_ID:%s AND ITEMCODE:%s AND -RULE_SCOPE:?*";
		    		where = String.format(where, batch.getBatchId(), itemCode);
		        	SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION, where, false);
	      		}

	      		if(limitScopeSet.contains(EngineLimitScopeEnum.CODE_29.getCode())) {
	      			//计算一日互斥，排除非同日互斥的就诊记录
	      			AbsSecondaryHandle handle = new ChargeOnedayMutexHandle(batch.getBatchId(), itemCode, ruleList, false);
	      			handle.execute();
	      		}

	          	//同步数据
				engineActionService.syncUnreasonableAction(task, batch, itemCode, "4", true);				
				//不合规行为汇总
	            engineActionService.executeGroupBy(batch.getBatchId(), itemCode, MedicalConstant.ENGINE_BUSI_TYPE_TREAT);
	      	}
		} catch(Exception e) {
			success = false;
			error = e.getMessage();
			throw e;
		} finally {
        	TaskBatchBreakRuleLog entity = new TaskBatchBreakRuleLog();
            entity.setEndTime(new Date());
            entity.setStatus(success ? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
            entity.setMessage(error);
            taskSV.updateTaskBatchBreakRuleLog(batch.getBatchId(), "treat", itemCode, entity);
        }
	}

	@Override
	public void generateMedicalUnreasonableTreatActionByThreadPool(String batchId){
		String datasource = SolrUtil.getLoginUserDatasource();
   	 	ThreadUtils.THREAD_DRUG_POOL.add(new EngineRunnable(datasource, batchId, this));
	}

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
			taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_TREAT, entity);

			TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
			if(batch==null) {
				throw new RuntimeException("未找到任务批次");
			}
			TaskProject task = taskSV.findTaskProject(batch.getProjectId());
			if(task==null) {
				throw new RuntimeException("未找到项目");
			}
			//先导出已审核的不合规行为结果
			EngineCntResult cntResult = engineActionService.importApprovalAction(batchId, MedicalConstant.ENGINE_BUSI_TYPE_TREAT);
			taskSV.removeTaskBatchBreakRuleLog(batchId, "treat");
			//删除历史solr数据
			engineActionService.deleteSolr(batchId, "4", true);

			List<MedicalDrugRule> drugRuleList = queryBatchRuleSV.queryMedicalDrugRule(batchId, MedicalConstant.RULE_TYPE_TREAT);
			if(drugRuleList!=null && drugRuleList.size()>0) {
				//按诊疗项目分组
				Map<String, List<MedicalDrugRule>> drugRuleGroupMap = drugRuleList.stream().collect(Collectors.groupingBy(MedicalDrugRule::getDrugCode));
				int count = drugRuleGroupMap.size();
				log.info("批次诊疗项目数量:"+count);
				List<TaskBatchBreakRuleLog> logList = new ArrayList<TaskBatchBreakRuleLog>();
                //遍历诊疗项目，写运行日志
				for (Map.Entry<String, List<MedicalDrugRule>> entry : drugRuleGroupMap.entrySet()) {
					TaskBatchBreakRuleLog log = new TaskBatchBreakRuleLog();
					log.setLogId(UUIDGenerator.generate());
					log.setBatchId(batchId);
					log.setItemId(entry.getKey());
					if(entry.getValue().size()==1 && entry.getValue().get(0).getChargeItems().indexOf(",")==-1) {
						log.setItemName(entry.getValue().get(0).getChargeItems());
					} else {
						log.setItemName(queryBatchRuleSV.getTreatname(entry.getKey()));
					}
					log.setItemType("treat");
					log.setCreateTime(DateUtils.getDate());
					log.setStatus(MedicalConstant.RUN_STATE_WAIT);
                    logList.add(log);
				}
				taskSV.saveTaskBatchBreakRuleLog(logList);
				//开启线程池跑任务
				List<EngineTreatRunnable> runnables = new ArrayList<EngineTreatRunnable>();
				// 有界队列
				BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(20);
				// 线程池
				ThreadPoolExecutor executor = new ThreadPoolExecutor(EngineUtil.CORE_POOL_SIZE, EngineUtil.MAXIMUM_POOL_SIZ, EngineUtil.KEEP_ALIVE_TIME, TimeUnit.MINUTES, queue);
				executor.setRejectedExecutionHandler(new EngineRejectedExecutionHandler());
                String datasource = SolrUtil.getLoginUserDatasource();
				//遍历诊疗项目
				for(Map.Entry<String, List<MedicalDrugRule>> entry : drugRuleGroupMap.entrySet()) {
					EngineTreatRunnable runnable = new EngineTreatRunnable(datasource, true, task, batch, entry.getKey(), entry.getValue());
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
						Thread.sleep(3000L);
					} catch (InterruptedException e) {
					}
				}
				for(EngineTreatRunnable runnable : runnables) {
					if(!runnable.isSuccess()) {
						success = false;
						error.append(runnable.getMessage()).append("\n");
					}
				}
			}

			if(cntResult.isSuccess() && cntResult.getCount()>0) {
    			//最后导入已审核的不合规行为结果
    			SolrUtil.importJsonToSolrNotDeleteFile(cntResult.getMessage(), EngineUtil.MEDICAL_UNREASONABLE_ACTION);
    		}
		} catch(Exception e) {
			success = false;
			error.append(e.getMessage());
            log.error("", e);
		} finally {
			TaskBatchStepItem entity = new TaskBatchStepItem();
			entity.setUpdateTime(new Date());
			entity.setEndTime(new Date());
			entity.setStatus(success? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
			if (!success) {
                if(error.length() > 2000) {
                	entity.setMsg(error.substring(0, 2000));
                } else {
                	entity.setMsg(error.toString());
                }
            }
			taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_TREAT, entity);
		}

		log.info(batchId+"批次诊疗项目总耗时:" + df.format(new Date((long)rtimer.getTime())));
	}

	@Override
	public void generateUnreasonableAction(String batchId, String itemCode) {
		boolean success = true;
		StringBuilder error = new StringBuilder();
		try {
			TaskBatchStepItem entity = new TaskBatchStepItem();
			entity.setUpdateTime(new Date());
			entity.setStartTime(new Date());
			entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
			taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_TREAT, entity);

			TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
			if(batch==null) {
				throw new RuntimeException("未找到任务批次");
			}
			TaskProject task = taskSV.findTaskProject(batch.getProjectId());
			if(task==null) {
				throw new RuntimeException("未找到项目");
			}
			//先导出已审核的不合规行为结果
			EngineCntResult cntResult = engineActionService.importApprovalAction(batchId, MedicalConstant.ENGINE_BUSI_TYPE_TREAT, itemCode);
			//删除历史solr数据
			engineActionService.deleteSolr(batchId, itemCode, "4", true);

			List<MedicalDrugRule> ruleList = queryBatchRuleSV.queryMedicalDrugRuleByItem(batchId, MedicalConstant.RULE_TYPE_TREAT, itemCode);
			if(ruleList!=null && ruleList.size()>0) {
				this.generateMedicalUnreasonableTreatAction(task, batch, itemCode, ruleList);
			}
			if(cntResult.isSuccess() && cntResult.getCount()>0) {
    			//最后回填已审核的不合规行为结果
    			SolrUtil.importJsonToSolr(cntResult.getMessage(), EngineUtil.MEDICAL_UNREASONABLE_ACTION);
    		}
		} catch(Exception e) {
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
            	Map<String, Integer> statusMap = taskSV.groupByTaskBatchBreakRuleLog(batchId, "treat");
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
            taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_TREAT, entity);
		}
	}

	@Override
	public void generateUnreasonableActionByThreadPool(String batchId, String itemCode) {
		TaskBatchStepItem entity = new TaskBatchStepItem();
		entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
		taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_TREAT, entity);
		//设置运行状态
		TaskBatchBreakRuleLog log = new TaskBatchBreakRuleLog();
        log.setStatus(MedicalConstant.RUN_STATE_WAIT);
        taskSV.updateTaskBatchBreakRuleLog(batchId, "treat", itemCode, log);
		String datasource = SolrUtil.getLoginUserDatasource();
		ThreadUtils.THREAD_DRUGITEM_POOL.add(new EngineItemRunnable(datasource, batchId, itemCode, this));
	}

	@Override
    public void generateUnreasonableActionFailRerun(String batchId) {
    	List<MedicalDrugRule> ruleList = queryBatchRuleSV.queryMedicalDrugRuleFail(batchId, MedicalConstant.RULE_TYPE_TREAT);
        if (ruleList == null || ruleList.size() == 0) {
        	return;
        }
    	TaskBatchStepItem entity = new TaskBatchStepItem();
		entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
		taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_TREAT, entity);

		List<String> codes = new ArrayList<String>();
		for(MedicalDrugRule rule : ruleList) {
			codes.add(rule.getDrugCode());
		}
		int pageSize = 1000;
		int pageNum = (codes.size() + pageSize - 1) / pageSize;
		//数据分割
		List<List<String>> mglist = new ArrayList<>();
	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
	    	mglist.add(codes.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
	    });
	    //设置状态
		TaskBatchBreakRuleLog log = new TaskBatchBreakRuleLog();
        log.setStatus(MedicalConstant.RUN_STATE_WAIT);
	    for(List<String> sublist : mglist) {
	    	taskSV.updateTaskBatchBreakRuleLog(batchId, "treat", sublist, log);
	    }

        String datasource = SolrUtil.getLoginUserDatasource();
        for(MedicalDrugRule rule : ruleList) {
        	ThreadUtils.THREAD_DRUGITEM_POOL.add(new EngineItemRunnable(datasource, batchId, rule.getDrugCode(), this));
        }
    }

	private List<String> parseTreatRuleCondition(EngineDrugRuleEntity ruleEntity) {
		MedicalDrugRule rule = ruleEntity.getRule();
		Map<String, EngineLimitScopeEnum> limitScopeEnumMap = ruleEntity.getLimitScopeEnumMap();
		List<String> conditionList = new ArrayList<String>();
		//频率
		String where = frequencyCondition(rule.getDrugCode(), rule, limitScopeEnumMap);
		if(StringUtils.isNotBlank(where)) {
			conditionList.add(where);
		}
		List<AbsEngineParamRule> list = new ArrayList<AbsEngineParamRule>();
		//关联DWB_MASTER_INFO条件
		EngineParamMasterInfoRule paramMasterInfoRule = new EngineParamMasterInfoRule(rule);
		list.add(paramMasterInfoRule);
		if(limitScopeEnumMap.containsKey("37") && StringUtils.isNotBlank(rule.getPayDurationPeriod())) {
			//支付时长
			StringBuilder sb = new StringBuilder();
			sb.append("_query_:\"");
			String fromIndex = "MAPPER_DWS_PATIENT_CHARGEITEM_SUM";
			switch(rule.getPayDurationPeriod()) {
				case "1M":
					fromIndex = fromIndex.concat("_M");
					break;
				case "3M":
					fromIndex = fromIndex.concat("_Q");
					break;
				case "1Y":
					fromIndex = fromIndex.concat("_Y");
					break;
				default:
					fromIndex = fromIndex.concat("_Y");
					break;
			}
			//中间表
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin(fromIndex, "VISITID", "VISITID");
			sb.append(plugin.parse());
			plugin = new SolrJoinParserPlugin("DWS_PATIENT_CHARGEITEM_SUM", "id", "DWSID");
			sb.append(plugin.parse());
			if("1M".equals(rule.getPayDurationPeriod())) {
				sb.append("DAYS_QTY:");
			} else if("3M".endsWith(rule.getPayDurationPeriod())) {
				if("day".equals(rule.getPayDurationUnit())) {
					sb.append("DAYS_QTY:");
				} else if("month".equals(rule.getPayDurationUnit())) {
					sb.append("MONTHS_QTY:");
				} else {
					sb.append("DAYS_QTY:");
				}
			} else {
				if("day".equals(rule.getPayDurationUnit())) {
					sb.append("DAYS_QTY:");
				} else if("month".equals(rule.getPayDurationUnit())) {
					sb.append("MONTHS_QTY:");
				} else {
					sb.append("DAYS_QTY:");
				}
			}
			sb.append("[0 TO ").append(rule.getPayDuration()).append("]");
			sb.append(" AND ITEMCODE:").append(ruleEntity.getRule().getDrugCode());
			sb.append("\"");
			conditionList.add(sb.toString());
		}
		if(limitScopeEnumMap.containsKey("27") && StringUtils.isNotBlank(rule.getFitGroupCodes())) {
			//合规项目组
			EngineParamChargeGrpRule paramRule = new EngineParamChargeGrpRule(rule.getDrugCode(), "STD_TREATGROUP", "TREATGROUP_CODE", rule.getFitGroupCodes());
			list.add(paramRule);
		}
		if(limitScopeEnumMap.containsKey("28") && StringUtils.isNotBlank(rule.getUnfitGroupCodes())) {
			//互斥项目组
			EngineParamChargeGrpRule paramRule = new EngineParamChargeGrpRule(rule.getDrugCode(), "STD_TREATGROUP", "TREATGROUP_CODE", rule.getUnfitGroupCodes());
			paramRule.setReverse(true);
			paramRule.addCondition("FUND_COVER:{0 TO *}");
			paramRule.addCondition("SELFPAY_PROP_MIN:[0 TO 1}");
			list.add(paramRule);
		}
		if(limitScopeEnumMap.containsKey("29") && StringUtils.isNotBlank(rule.getUnfitGroupCodesDay())) {
			//一日内互斥项目组，特殊处理：先筛选出存在互斥项目组的数据，后续再过滤掉非同日内互斥的数据
			EngineParamChargeGrpRule paramRule = new EngineParamChargeGrpRule(rule.getDrugCode(), "STD_TREATGROUP", "TREATGROUP_CODE", rule.getUnfitGroupCodesDay());
			paramRule.setReverse(true);
			paramRule.addCondition("FUND_COVER:{0 TO *}");
			paramRule.addCondition("SELFPAY_PROP_MIN:[0 TO 1}");
			list.add(paramRule);
		}

		if (limitScopeEnumMap.containsKey("13") && StringUtils.isNotBlank(rule.getIndication())) {
            //适用症
            EngineParamIndicationRule paramRule = new EngineParamIndicationRule("DIAGGROUP_CODE", rule.getIndication());
            list.add(paramRule);
        }

		if (limitScopeEnumMap.containsKey("31") && StringUtils.isNotBlank(rule.getUnIndication())) {
            //禁忌症
            EngineParamIndicationRule paramRule = new EngineParamIndicationRule("DIAGGROUP_CODE", rule.getUnIndication());
            paramRule.setReverse(true);
            list.add(paramRule);
        }

		if(limitScopeEnumMap.containsKey("32") && StringUtils.isNotBlank(rule.getUnExpense())) {
        	//不能报销
			//EngineParamUnExpenseRule paramRule = new EngineParamUnExpenseRule();
			BaseEngineParamRule paramRule = new BaseEngineParamRule("FUND_COVER", ">", "0");
			paramRule.setReverse(true);
        	list.add(paramRule);
        }

		if(limitScopeEnumMap.containsKey("36") && StringUtils.isNotBlank(rule.getUnCharge())) {
			//不能诊疗
			BaseEngineParamRule paramRule = new BaseEngineParamRule("ITEM_AMT", ">", "0");
			paramRule.setReverse(true);
			list.add(paramRule);
		}

		/*if(limitScopeSet.contains("39") && StringUtils.isNotBlank(rule.getDiseasegroupFreq())
				&& StringUtils.isNotBlank(rule.getDiseasegroupCodes())) {
			//频率疾病组
			EngineParamIndicationRule paramRule = new EngineParamIndicationRule("DIAGGROUP_CODE", rule.getDiseasegroupCodes());
		    if(!"=".equals(rule.getDiseasegroupFreq())) {
		    	paramRule.setReverse(true);
		    }
			list.add(paramRule);
		}*/

		for(AbsEngineParamRule bean : list) {
			String condition = bean.where();
			if(condition!=null) {
				conditionList.add(condition);
			}
		}
		//conditionList.add("ITEMCODE:"+rule.getDrugCode());
		//log.info("conditionList:" + StringUtils.join(conditionList, ","));
		return conditionList;
	}

	/**
	 *
	 * 功能描述：频次查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年7月21日 上午9:40:43</p>
	 *
	 * @param itemCode
	 * @param rule
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private String frequencyCondition(String itemCode, MedicalDrugRule rule, Map<String, EngineLimitScopeEnum> limitScopeEnumMap) {
		if(limitScopeEnumMap.containsKey(EngineLimitScopeEnum.CODE_38.getCode())){
			//频次
			EngineParamFrequencyRule paramFrequencyRule = new EngineParamFrequencyRule(rule);
			String condition = paramFrequencyRule.where();

			if(limitScopeEnumMap.containsKey(EngineLimitScopeEnum.CODE_39.getCode())
					&& StringUtils.isNotBlank(rule.getDiseasegroupFreq())
					&& StringUtils.isNotBlank(rule.getDiseasegroupCodes())) {
				StringBuilder where = new StringBuilder();
				//频率疾病组
				EngineParamFrequencyGrpRule paramRule = new EngineParamFrequencyGrpRule("DIAGGROUP_CODE", rule.getDiseasegroupCodes());
	            if("=".equals(rule.getDiseasegroupFreq())) {
	            	//等于
	            	where.append("(");
	            	paramRule.setReverse(true);
	            	where.append(paramRule.where());
	            	where.append(") OR (");
	            	paramRule.setReverse(false);
	            	where.append(paramRule.where());
	            	where.append(" AND (").append(condition).append(")");
	            	where.append(")");
	            } else {
	            	//不等于
	            	where.append("(");
	            	where.append(paramRule.where());
	            	where.append(") OR (");
	            	paramRule.setReverse(true);
	            	where.append(paramRule.where());
	            	where.append(" AND (").append(condition).append(")");
	            	where.append(")");
	            }
	            return where.toString();
			} else {
				return condition;
			}
		}
		return null;
	}

	@Override
    public void trailTreatActionThreadPool(String ruleId, String etlSource) {
		String ds = SolrUtil.getLoginUserDatasource();
		String batchId = ruleId;
        taskSV.removeTaskBatchStepItem(batchId, ds);
        Date now = new Date();
        String status = MedicalConstant.RUN_STATE_WAIT;
        TaskBatchStepItem step = new TaskBatchStepItem();
        step.setId(UUIDGenerator.generate());
        step.setBatchId(batchId);
        step.setStep(1);
        step.setItemId(MedicalConstant.RULE_TYPE_TREAT);
        step.setCreateTime(now);
        step.setStatus(status);
		step.setDataSource(ds);
        taskSV.saveTaskBatchStepItem(step);

        ThreadUtils.THREAD_TRAIL_POOL.add(new EngineTrailTreatRunnable(ds, ruleId, etlSource));
    }

	@Override
    public void trailTreatAction(String ruleId, String etlSource, String datasource) {
		String batchId = ruleId;
        boolean success = true;
        String error = null;
        try {
            //删除历史数据
        	String where = "RULE_TYPE:4 AND BATCH_ID:%s";
        	where = String.format(where, batchId);
            SolrUtil.delete(EngineUtil.MEDICAL_TRAIL_DRUG_ACTION, where);
            where = "BUSI_TYPE:TREAT AND BATCH_ID:%s";
        	where = String.format(where, batchId);
        	SolrUtil.delete(EngineUtil.MEDICAL_TRAIL_ACTION, where);

            TaskBatchStepItem entity = new TaskBatchStepItem();
            entity.setUpdateTime(new Date());
            entity.setStartTime(new Date());
            entity.setStatus(MedicalConstant.RUN_STATE_RUNNING);
            entity.setDataSource(datasource);
            taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_TREAT, datasource, entity);

            List<MedicalDrugRule> drugRuleList = queryBatchRuleSV.queryMedicalDrugRuleByRuleid(ruleId);
            EngineDrugRuleEntity result = null;
            for (MedicalDrugRule rule : drugRuleList) {
                try {
                    result = this.trailTreatAction(rule, etlSource);
                } catch (Exception e) {
                    log.error("", e);
                    success = false;
                    error = error + "\n" + e.getMessage();
                }
            }

            //试算结果数据同步
            engineActionService.syncTrailAction(ruleId, "4");
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
            taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_TREAT, entity);
        }
	}

	private EngineDrugRuleEntity trailTreatAction(MedicalDrugRule rule, String etlSource) throws Exception {
  		EngineDrugRuleEntity result = new EngineDrugRuleEntity(rule);
  		if(result.isEmptyScope()) {
  			throw new EngineBizException(rule.getRuleId()+"["+rule.getDrugNames()+"]未设置限制范围！");
  		}
  		EngineLimitScopeEnum limitScopeEnum = null;
  		if(result.singleScope()) {
  			limitScopeEnum = result.getFirstScopeEnum();
  		}
  		final EngineLimitScopeEnum scopeEnum = limitScopeEnum;
        List<String> conditionList = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        List<String> tempList = this.parseTreatRuleCondition(result);
        if (!tempList.isEmpty()) {
            sb.append("(");
            for (int j = 0; j < tempList.size(); j++) {
                String condition = tempList.get(j);
                if (j > 0) {
                    sb.append(" AND ");
                }
                sb.append("(").append(condition).append(")");
            }
            sb.append(")");
        }
        conditionList.add("*:* -(" + sb.toString() + ")");
        conditionList.add("ITEMCODE:" + rule.getDrugCode());
        conditionList.add("ITEM_QTY:{0 TO *}");
		conditionList.add("ITEM_AMT:{0 TO *}");
        if (StringUtils.isNotBlank(etlSource)) {
            conditionList.add("ETL_SOURCE:" + etlSource);
        }

        //规则的数据时间范围限制
  		sb.setLength(0);
  		sb.append("VISITDATE:");
  		sb.append("[").append(DateUtil.format(rule.getStartTime(), "yyyy-MM-dd"));
  		sb.append(" TO ");
  		sb.append(DateUtil.format(rule.getEndTime(), "yyyy-MM-dd")).append("]");
  		conditionList.add(sb.toString());
        //添加过滤掉指标为空值的条件
        List<String> ignoreNullList = engineActionService.ignoreNullWhere(rule);
        if(ignoreNullList!=null && ignoreNullList.size()>0) {
			conditionList.addAll(ignoreNullList);
		}

        String batchId = rule.getRuleId();
        // 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/" + EngineUtil.MEDICAL_TRAIL_DRUG_ACTION + "/" + batchId + "/" + rule.getDrugCode() + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");
        ActionTypeDTO dto = this.getActionTypeDTO();
        List<JSONObject> dataList = new ArrayList<JSONObject>();
        SolrUtil.exportByPager(conditionList, EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, (map, index) -> {
            // 循环一条数据写入文件
        	JSONObject json = engineActionService.writerJson(fileWriter, map, rule, scopeEnum, dto);
        	dataList.add(json);
        });
        // 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();

        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_TRAIL_DRUG_ACTION);

        if(!result.singleScope()) {
        	//计算违规限定范围
            targetService.calculateBreakActionTarget(EngineUtil.MEDICAL_TRAIL_DRUG_ACTION, false, batchId, rule, null, true);
            //删除限定范围是空的数据
    	    String where = "BATCH_ID:%s AND ITEMCODE:%s AND -RULE_SCOPE:?*";
    		where = String.format(where, batchId, rule.getDrugCode());
        	SolrUtil.delete(EngineUtil.MEDICAL_TRAIL_DRUG_ACTION, where, false);
        }

        List<MedicalDrugRule> ruleList = new ArrayList<MedicalDrugRule>();
        ruleList.add(rule);
        //规则包含的所有限制范围
    	Set<String> limitScopeSet = new HashSet<String>();
    	limitScopeSet.addAll(result.getLimitScopeSet());
        if(limitScopeSet.contains(EngineLimitScopeEnum.CODE_29.getCode())) {
  			//计算一日互斥，排除非同日互斥的就诊记录
  			AbsSecondaryHandle handle = new ChargeOnedayMutexHandle(batchId, rule.getDrugCode(), ruleList, true);
  			handle.execute();
  		}

    	return result;
    }

    private ActionTypeDTO getActionTypeDTO() {
    	String busiType = MedicalConstant.ENGINE_BUSI_TYPE_TREAT;
    	ActionTypeDTO dto = new ActionTypeDTO();
        dto.setActionTypeId(busiType);
        dto.setActionTypeName(dictSV.queryDictTextByKey("ACTION_TYPE", busiType));
        return dto;
    }
}
