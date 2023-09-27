/**
 * EngineTrialServiceImpl.java	  V1.0   2021年8月26日 下午2:13:26
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.parse.EngineNodeResolver;
import com.ai.modules.engine.runnable.EngineFormalFlowTrialRunnable;
import com.ai.modules.engine.runnable.EngineRejectedExecutionHandler;
import com.ai.modules.engine.service.IEngineService;
import com.ai.modules.engine.service.IEngineTrialService;
import com.ai.modules.engine.service.api.IApiCaseService;
import com.ai.modules.engine.service.api.IApiTrialService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.entity.MedicalFormalCase;
import com.ai.modules.medical.entity.MedicalFlowTrial;
import com.ai.modules.probe.entity.MedicalProbeCase;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineTrialServiceImpl implements IEngineTrialService {

	@Autowired
    private IApiCaseService caseSV;
	@Autowired
	private IEngineService engineSV;
	@Autowired
	private IApiTrialService apiTrialSV;

	@Override
	public void trialCaseFlowCnt(MedicalFormalCase formalCase, List<EngineNode> flow) {
		EngineNode first = flow.get(0);
		String caseId = formalCase.getCaseId();
		String nodeCode = first.getNodeCode();
		String datasource = SolrUtil.getLoginUserDatasource();
		Long count = null;
		boolean success = true;
		String error = null;
		try {
			MedicalFlowTrial trial = new MedicalFlowTrial();
			trial.setStartTime(DateUtils.getDate());
			trial.setStatus("running");
			apiTrialSV.updateMedicalFlowTrial(caseId, nodeCode, datasource, trial);
			List<String> conditionList = new ArrayList<String>();
			Set<String> fqSet = engineSV.constructConditionExpression(flow);
			conditionList.addAll(fqSet);
			count = SolrUtil.count(conditionList, EngineUtil.DWB_MASTER_INFO, false);
		} catch(Exception e) {
			log.error("", e);
			success = false;
			error = e.getMessage();
		} finally {
			MedicalFlowTrial trial = new MedicalFlowTrial();
			trial.setEndTime(DateUtils.getDate());
			trial.setStatus(success ? "normal" : "abnormal");
			if(!success) {
				trial.setRemark(error);
			}
			trial.setNumFound(count);
			apiTrialSV.updateMedicalFlowTrial(caseId, nodeCode, datasource, trial);
		}
	}

	@Override
	public void trialFormalFlowCnt(String caseId) {
		MedicalFormalCase formalCase = caseSV.findMedicalFormalCase(caseId);
		this.trialFormalFlowCnt(formalCase);
	}

	private void trialFormalFlowCnt(MedicalFormalCase formalCase) {
		String datasource = SolrUtil.getLoginUserDatasource();
		//先删除历史记录
		apiTrialSV.removeMedicalFlowTrial(formalCase.getCaseId(), datasource);
		//模型所有节点
		List<EngineNode> nodeList = caseSV.findMedicalFormalFlowByCaseid(formalCase.getCaseId());
		//模型所有节点规则
		List<EngineNodeRule> ruleList = caseSV.queryMedicalFormalFlowRuleByCaseid(formalCase.getCaseId());
		EngineNodeResolver resolver = EngineNodeResolver.getInstance();
		List<List<EngineNode>> flowList = resolver.parseEveryNodeFlow(nodeList, ruleList);
		if(flowList!=null) {
			for(List<EngineNode> flow : flowList) {
				if(flow!=null && flow.size()>0) {
					EngineNode first = flow.get(0);
					MedicalFlowTrial trial = new MedicalFlowTrial();
					trial.setId(UUIDGenerator.generate());
					trial.setNodeCode(first.getNodeCode());
					trial.setNodeName(first.getNodeName());
					trial.setCaseId(formalCase.getCaseId());
					trial.setProject(SolrUtil.getLoginUserDatasource());
					trial.setType("formal");
					trial.setCreateTime(DateUtils.getDate());
					trial.setStatus("wait");
					trial.setOrderNo(first.getOrderNo());
					apiTrialSV.saveMedicalFlowTrial(trial);
					//试算每一个节点流程的记录数
					this.trialCaseFlowCnt(formalCase, flow);
				}
			}
		}
	}

	@Override
	public void trialCaseFlowCnt() {
		trialCaseFlowCnt(false);
	}

	@Override
	public void trialCaseFlowCnt(boolean probe) {
		// 有界队列
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(20);
		// 线程池
		ThreadPoolExecutor executor = new ThreadPoolExecutor(EngineUtil.CORE_POOL_SIZE, EngineUtil.MAXIMUM_POOL_SIZ, EngineUtil.KEEP_ALIVE_TIME, TimeUnit.MINUTES, queue);
		executor.setRejectedExecutionHandler(new EngineRejectedExecutionHandler());
		String datasource = SolrUtil.getLoginUserDatasource();
        if(probe) {
        	//探查库模型
        	List<MedicalProbeCase> probeCaseList = caseSV.findMedicalProbeCaseAll();
        	List<EngineFormalFlowTrialRunnable> runnables = new ArrayList<EngineFormalFlowTrialRunnable>();
        	List<MedicalFlowTrial> trialList = new ArrayList<MedicalFlowTrial>();
        	for(MedicalProbeCase probeCase : probeCaseList) {
    			MedicalFormalCase formalCase = new MedicalFormalCase();
    			BeanUtils.copyProperties(probeCase, formalCase);
    			//先删除历史记录
    			apiTrialSV.removeMedicalFlowTrial(probeCase.getCaseId(), datasource);
    			//模型所有节点规则
    			List<EngineNodeRule> ruleList = caseSV.queryMedicalProbeFlowRuleByCaseid(probeCase.getCaseId());
    			EngineNodeResolver resolver = EngineNodeResolver.getInstance();
    			List<List<EngineNode>> flowList = resolver.parseEveryNodeFlow(probeCase.getFlowJson(), ruleList);
    			if(flowList!=null) {
    				runnables.clear();
    				trialList.clear();
    				for(List<EngineNode> flow : flowList) {
    					if(flow!=null && flow.size()>0) {
    						EngineNode first = flow.get(0);
    						MedicalFlowTrial trial = new MedicalFlowTrial();
    						trial.setId(UUIDGenerator.generate());
    						trial.setNodeCode(first.getNodeCode());
    						trial.setNodeName(first.getNodeName());
    						trial.setCaseId(probeCase.getCaseId());
    						trial.setProject(SolrUtil.getLoginUserDatasource());
    						trial.setType("probe");
    						trial.setCreateTime(DateUtils.getDate());
    						trial.setStatus("wait");
    						trial.setOrderNo(first.getOrderNo());
    						trialList.add(trial);
    						//试算每一个节点流程的记录数
    						EngineFormalFlowTrialRunnable runnable = new EngineFormalFlowTrialRunnable(datasource, formalCase, flow);
    						runnables.add(runnable);
    					}
    				}
    				apiTrialSV.saveMedicalFlowTrial(trialList);
    				for(EngineFormalFlowTrialRunnable runnable : runnables) {
    					executor.execute(runnable);
    				}
    			}
    		}
        } else {
			//正式库模型
			List<MedicalFormalCase> caseList = caseSV.findMedicalFormalCaseAll();
			if(caseList!=null && caseList.size()>0) {
				List<EngineFormalFlowTrialRunnable> runnables = new ArrayList<EngineFormalFlowTrialRunnable>();
				List<MedicalFlowTrial> trialList = new ArrayList<MedicalFlowTrial>();
				for(MedicalFormalCase formalCase : caseList) {
					//先删除历史记录
					apiTrialSV.removeMedicalFlowTrial(formalCase.getCaseId(), datasource);
					//模型所有节点
					List<EngineNode> nodeList = caseSV.findMedicalFormalFlowByCaseid(formalCase.getCaseId());
					//模型所有节点规则
					List<EngineNodeRule> ruleList = caseSV.queryMedicalFormalFlowRuleByCaseid(formalCase.getCaseId());
					EngineNodeResolver resolver = EngineNodeResolver.getInstance();
					List<List<EngineNode>> flowList = resolver.parseEveryNodeFlow(nodeList, ruleList);
					if(flowList!=null) {
						runnables.clear();
						trialList.clear();
						for(List<EngineNode> flow : flowList) {
							if(flow!=null && flow.size()>0) {
								EngineNode first = flow.get(0);
								MedicalFlowTrial trial = new MedicalFlowTrial();
								trial.setId(UUIDGenerator.generate());
								trial.setNodeCode(first.getNodeCode());
								trial.setNodeName(first.getNodeName());
								trial.setCaseId(formalCase.getCaseId());
								trial.setProject(SolrUtil.getLoginUserDatasource());
								trial.setType("formal");
								trial.setCreateTime(DateUtils.getDate());
								trial.setStatus("wait");
								trial.setOrderNo(first.getOrderNo());
								trialList.add(trial);
								//试算每一个节点流程的记录数
								EngineFormalFlowTrialRunnable runnable = new EngineFormalFlowTrialRunnable(datasource, formalCase, flow);
								runnables.add(runnable);
							}
						}
						apiTrialSV.saveMedicalFlowTrial(trialList);
						for(EngineFormalFlowTrialRunnable runnable : runnables) {
							executor.execute(runnable);
						}
					}
				}
			}
		}
		executor.shutdown();
	}

	private void trialProbeFlowCnt(MedicalProbeCase probeCase) {
		MedicalFormalCase formalCase = new MedicalFormalCase();
		BeanUtils.copyProperties(probeCase, formalCase);
		String datasource = SolrUtil.getLoginUserDatasource();
		//先删除历史记录
		apiTrialSV.removeMedicalFlowTrial(probeCase.getCaseId(), datasource);
		//模型所有节点规则
		List<EngineNodeRule> ruleList = caseSV.queryMedicalProbeFlowRuleByCaseid(probeCase.getCaseId());
		EngineNodeResolver resolver = EngineNodeResolver.getInstance();
		List<List<EngineNode>> flowList = resolver.parseEveryNodeFlow(probeCase.getFlowJson(), ruleList);
		if(flowList!=null) {
			for(List<EngineNode> flow : flowList) {
				if(flow!=null && flow.size()>0) {
					EngineNode first = flow.get(0);
					MedicalFlowTrial trial = new MedicalFlowTrial();
					trial.setId(UUIDGenerator.generate());
					trial.setNodeCode(first.getNodeCode());
					trial.setNodeName(first.getNodeName());
					trial.setCaseId(probeCase.getCaseId());
					trial.setProject(SolrUtil.getLoginUserDatasource());
					trial.setType("probe");
					trial.setCreateTime(DateUtils.getDate());
					trial.setStatus("wait");
					trial.setOrderNo(first.getOrderNo());
					apiTrialSV.saveMedicalFlowTrial(trial);
					//试算每一个节点流程的记录数
					this.trialCaseFlowCnt(formalCase, flow);
				}
			}
		}
	}

	@Override
	public void trialProbeFlowCnt(String caseId) {
		MedicalProbeCase probeCase = caseSV.findMedicalProbeCase(caseId);
		this.trialProbeFlowCnt(probeCase);
	}
}
