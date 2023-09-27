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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.MedicalConstant;
import com.ai.common.utils.ThreadUtils;
import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.report.AbstractReportHandler;
import com.ai.modules.engine.handle.report.BaseReportHandler;
import com.ai.modules.engine.model.EngineCntResult;
import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.model.RTimer;
import com.ai.modules.engine.model.clinical.EngineClinicalRule;
import com.ai.modules.engine.model.dto.ActionDTO;
import com.ai.modules.engine.model.report.ReportFacetBucketField;
import com.ai.modules.engine.model.report.ReportParamModel;
import com.ai.modules.engine.model.vo.ClinicalRequiredGroupVO;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.engine.parse.EngineClinicalResolver;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.runnable.EngineClinicalRunnable;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.IEngineClinicalService;
import com.ai.modules.engine.service.api.IApiClinicalService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalClinical;
import com.ai.modules.medical.entity.MedicalClinicalAccessGroup;
import com.ai.modules.medical.entity.MedicalClinicalInfo;
import com.ai.modules.medical.entity.MedicalClinicalRangeGroup;
import com.ai.modules.task.entity.TaskBatchBreakRule;
import com.ai.modules.task.entity.TaskBatchBreakRuleDel;
import com.ai.modules.task.entity.TaskBatchStepItem;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EngineClinicalServiceImpl implements IEngineClinicalService {
	@Autowired
    private IApiTaskService taskSV;
	@Autowired
    private IApiClinicalService clinicalSV;
	@Autowired
    private IApiDictService dictSV;
    @Autowired
	private IEngineActionService engineActionService;

	@Override
	public void generateMedicalUnreasonableClinicalActionByThreadPool(String batchId, String clinicalId) {
		String ds = SolrUtil.getLoginUserDatasource();
    	EngineClinicalRunnable runnable = new EngineClinicalRunnable(ds, batchId, clinicalId);
    	ThreadUtils.THREAD_CASE_POOL.add(runnable);
	}

	@Override
	public void generateMedicalUnreasonableClinicalAction(String batchId, String clinicalId) throws Exception {		
		EngineResult res = null;
		try {
			TaskBatchStepItem step = new TaskBatchStepItem();
	        step.setUpdateTime(new Date());
	        step.setStartTime(new Date());
	        step.setStatus(MedicalConstant.RUN_STATE_RUNNING);
	        taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_CLINICAL_NEW, step);

			TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
	        if (batch == null) {
	            throw new RuntimeException("未找到任务批次");
	        }
	        TaskProject task = taskSV.findTaskProject(batch.getProjectId());
	        if (task == null) {
	            throw new RuntimeException("未找到项目");
	        }
	        //先导出已审核的不合规行为结果
			EngineCntResult cntResult = engineActionService.importApprovalAction(batchId, MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL, clinicalId);
			//删除历史数据
	        String where = "BUSI_TYPE:*%s AND BATCH_ID:%s AND CASE_ID:%s";
	        where = String.format(where, MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL, batchId, clinicalId);
	        SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
	        
	        res = this.generate(task, batch, clinicalId);
	        
	        if(cntResult.isSuccess() && cntResult.getCount()>0) {
				//最后回填已审核的不合规行为结果
				SolrUtil.importJsonToSolr(cntResult.getMessage(), EngineUtil.MEDICAL_UNREASONABLE_ACTION);
			}
		} catch(Exception e) {
			throw e;
		} finally {
			String status = MedicalConstant.RUN_STATE_NORMAL;
	        List<TaskBatchBreakRuleDel> caseList = taskSV.findTaskBatchBreakRuleDel(batchId, MedicalConstant.RULE_TYPE_CLINICAL_NEW);
	        if(caseList!=null && caseList.size()>0) {
	        	Map<String, List<TaskBatchBreakRuleDel>> caseMap = caseList.stream().collect(Collectors.groupingBy(TaskBatchBreakRuleDel::getStatus));
	            if (caseMap.containsKey(MedicalConstant.RUN_STATE_WAIT)) {
	                //存在等待的任务
	            	status = MedicalConstant.RUN_STATE_WAIT;
	            } else if (caseMap.containsKey(MedicalConstant.RUN_STATE_ABNORMAL)) {
	                //存在失败的任务
	                status = MedicalConstant.RUN_STATE_ABNORMAL;
	            }
	        }
	        TaskBatchStepItem step = new TaskBatchStepItem();
	        step.setUpdateTime(new Date());
	        step.setStatus(status);
	        taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_CLINICAL_NEW, step);
		}
	}

	private EngineResult generate(TaskProject task, TaskProjectBatch batch, String clinicalId) {
		boolean success = true;
        String error = "";
        EngineResult result = EngineResult.ok();        
		try {
			//更新任务状态
			TaskBatchBreakRuleDel dtl = new TaskBatchBreakRuleDel();
	        dtl.setStartTime(new Date());
	        dtl.setStatus(MedicalConstant.RUN_STATE_RUNNING);
	        taskSV.updateTaskBatchBreakRuleDel(batch.getBatchId(), MedicalConstant.RULE_TYPE_CLINICAL_NEW, clinicalId, dtl);
	        
			MedicalClinical clinical = clinicalSV.findMedicalClinical(clinicalId);
			MedicalClinicalInfo clinicalInfo = clinicalSV.findMedicalClinicalInfo(clinicalId);
			if(clinical==null) {
				throw new EngineBizException("未找到临床路径");
			}
			if(clinical.getInhospDaysMax()==null) {
				//throw new RuntimeException("临床路径未设置最大住院天数");
	            return result;
			}
			String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
			String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
			String case_startTime = MedicalConstant.DEFAULT_START_TIME;
			String case_endTime = MedicalConstant.DEFAULT_END_TIME;
			batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
			batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
			case_startTime = clinical.getStartTime()!=null ? DateUtils.formatDate(clinical.getStartTime(), "yyyy-MM-dd") : case_startTime;
			case_endTime = clinical.getEndTime()!=null ? DateUtils.formatDate(clinical.getEndTime(), "yyyy-MM-dd") : case_endTime;
			if(batch_endTime.compareTo(case_startTime)<0 || batch_startTime.compareTo(case_endTime)>0) {
				return result;
			}
			ActionDTO actionDTO = new ActionDTO();
			actionDTO.setActionId(clinical.getActionId());
			actionDTO.setActionTypeId(clinical.getActionType());
			if(StringUtils.isNotBlank(clinical.getActionType())) {
				String desc = dictSV.queryDictTextByKey("ACTION_TYPE", clinical.getActionType());
				actionDTO.setActionTypeName(desc);
	        }
	        if(StringUtils.isNotBlank(clinical.getActionId())) {
	        	actionDTO.setActionName(dictSV.queryDictTextByKey("ACTION_LIST", clinical.getActionId()));	        	
	        }
			//准入条件组
			List<MedicalClinicalAccessGroup> list = clinicalSV.findMedicalClinicalAccessGroup(clinicalId, "approve");
			//排除条件组
			List<EngineClinicalRule> excludeList = this.getExcludeClinicalRule(clinicalId);
			//临床路径查询条件解析器
			EngineClinicalResolver resolver = EngineClinicalResolver.getInstance();
			final BigDecimal[] money = {BigDecimal.ZERO, BigDecimal.ZERO};
			int count = 0;
			//遍历每个准入条件组
			for(MedicalClinicalAccessGroup group : list) {
				EngineClinicalRule rule = this.getEngineClinicalRule(group);
				List<String> conditionList =resolver.parseConditionExpression(rule, excludeList);

				// 数据写入xml
	            String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/" + group.getGroupId() + ".json";
	            BufferedWriter fileWriter = new BufferedWriter(
	                    new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
	            //写文件头
	            fileWriter.write("[");

	            //项目过滤条件
	            ProjectFilterWhereVO filterVO = engineActionService.filterCondition(task, true);
	            if(StringUtils.isNotBlank(filterVO.getCondition())) {
	            	conditionList.add(filterVO.getCondition());
	            }	
				//业务数据时间范围
				conditionList.add("VISITDATE:["+batch_startTime+" TO "+batch_endTime+"]");
				conditionList.add("VISITDATE:["+case_startTime+" TO "+case_endTime+"]");
				if(StringUtils.isNotBlank(batch.getCustomFilter())) {
	            	//自定义数据范围限制
					//conditionList.add(batch.getCustomFilter());
	            	String value = batch.getCustomFilter();
	            	value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
	            	conditionList.add("ORGID:"+value);
	            }
				//住院
				conditionList.add("VISITTYPE_ID:ZY*");
				SolrUtil.exportByPager(conditionList, EngineUtil.DWB_MASTER_INFO, (map, index) -> {
	                // 循环一条数据里面的内容
					if(map.get("TOTALFEE")!=null) {
						money[0] = money[0].add(new BigDecimal(map.get("TOTALFEE").toString()));
					}
					if(map.get("FUNDPAY")!=null) {
						money[1] = money[1].add(new BigDecimal(map.get("FUNDPAY").toString()));
					}
	                JSONObject json = this.constructJson(map, task, batch, clinical, clinicalInfo, group, actionDTO);
	                try {
	                    fileWriter.write(json.toJSONString());
	                    fileWriter.write(',');
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            });
	            // 文件尾
	            fileWriter.write("]");
	            fileWriter.flush();
	            fileWriter.close();

	            //导入solr
	            SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
			}

			//超住院时间+超药品+超项目不合规临床路径
			count = this.breakOutDayDrugTreatClinicalAction(batch, clinical);
			//必做药品未做或必做项目未做不合规临床路径
			count = count + this.breakDrugTreatClinicalAction(batch, clinical);
			//删除合规的临床路径
	        String where = "BUSI_TYPE:%s AND BATCH_ID:%s";
	        where = String.format(where, "TMP_"+MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL, batch.getBatchId());
	        SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
			//计算临床路径范围金额
			this.statisticsClinicalMoney(task, batch, clinicalId);
			
            result.setCount(count);
            result.setMoney(money[0]);
            
            //不合规行为汇总
            String[] fqs = new String[] {"CASE_ID:"+clinicalId};
            engineActionService.executeGroupBy(batch.getBatchId(), clinical.getActionId(), fqs);
		} catch(Exception e) {
			log.error("", e);
			error = e.getMessage();
        	success = false;
			result = EngineResult.error(e.getMessage());
		} finally {
			TaskBatchBreakRuleDel dtl = new TaskBatchBreakRuleDel();
	        if (!success) {
	            dtl.setStatus(MedicalConstant.RUN_STATE_ABNORMAL);
                error = error!=null&&error.length() > 2000 ? error.substring(0, 2000) : error;
                dtl.setErrorMsg(error);
	        } else {
	        	dtl.setRecordNum(result.getCount());
	        	dtl.setTotalAcount(result.getMoney());
	            dtl.setStatus(MedicalConstant.RUN_STATE_NORMAL);
	            dtl.setErrorMsg("执行成功");
	        }
	        dtl.setEndTime(new Date());
	        taskSV.updateTaskBatchBreakRuleDel(batch.getBatchId(), MedicalConstant.RULE_TYPE_CLINICAL_NEW, clinicalId, dtl);
		}
		return result;
	}

	/**
	 *
	 * 功能描述：计算出超住院时间+超药品+超项目不合规临床路径
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月9日 下午2:33:29</p>
	 *
	 * @param batch
	 * @param clinical
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private int breakOutDayDrugTreatClinicalAction(TaskProjectBatch batch, MedicalClinical clinical) throws Exception {
		log.info("start 计算出超住院时间+超药品+超项目不合规临床路径");
		int count = 0;
		List<String> conditionList = new ArrayList<String>();
		String where = "BATCH_ID:%s";
		where = String.format(where, batch.getBatchId());
		conditionList.add(where);
		conditionList.add("BUSI_TYPE:TMP_CLINICAL");
		where = "ZY_DAYS:{%s TO *}";
		where = String.format(where, clinical.getInhospDaysMax());
		conditionList.add(where);
		//药品、项目范围
		List<MedicalClinicalRangeGroup> rangeList = clinicalSV.findMedicalClinicalRangeGroup(clinical.getClinicalId());
		if(rangeList==null || rangeList.size()==0) {
			return 0;
		}
		//药品
		Set<String> drugGroups = new HashSet<String>();
		//检查项目
		Set<String> treatGroups = new HashSet<String>();
		for(MedicalClinicalRangeGroup record : rangeList) {
			if("drug".equalsIgnoreCase(record.getGroupType())) {
				drugGroups.add(record.getGroupCode());
			} else {
				treatGroups.add(record.getGroupCode());
			}
		}
		StringBuilder sb = new StringBuilder();
		if(drugGroups.size()>0) {
			sb.setLength(0);
			sb.append("_query_:\"");
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWS_PATIENT_1VISIT_ITEMSUM", "VISITID", "VISITID");
			sb.append(plugin.parse());
			sb.append("CHARGECLASS_ID:(400 OR 500 OR 600) AND ITEM_QTY:{0 TO *}");
			sb.append(" AND ");
			sb.append("(*:* -(");
			sb.append("_query_:\\\"");
			plugin = new SolrJoinParserPlugin("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
			sb.append(plugin.parse());
			sb.append("DRUGGROUP_CODE:(").append(StringUtils.join(drugGroups, " OR ")).append(")");
			sb.append("\\\"");
			sb.append("))");
			sb.append("\"");
			conditionList.add(sb.toString());
		}
		if(treatGroups.size()>0) {
			sb.setLength(0);
			sb.append("_query_:\"");
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWS_PATIENT_1VISIT_ITEMSUM", "VISITID", "VISITID");
			sb.append(plugin.parse());
			sb.append("-CHARGECLASS_ID:(400 OR 500 OR 600) AND ITEM_QTY:{0 TO *}");
			sb.append(" AND ");
			sb.append("(*:* -(");
			sb.append("_query_:\\\"");
			plugin = new SolrJoinParserPlugin("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
			sb.append(plugin.parse());
			sb.append("DRUGGROUP_CODE:(").append(StringUtils.join(drugGroups, " OR ")).append(")");
			sb.append("\\\"");
			sb.append("))");
			sb.append("\"");
			conditionList.add(sb.toString());
		}

		// 数据写入json
        String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/" + clinical.getClinicalId() + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");
        Map<String, SolrDocument> visitMap = new HashMap<String, SolrDocument>();
        List<String> visitidList = new ArrayList<String>();
        //1、超住院时间+超药品+超项目
        count = SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (doc, index) -> {
            String visitid = doc.get("VISITID").toString();
			visitMap.put(visitid, doc);
			visitidList.add(visitid);
        });

		int size = visitidList.size();
        int pageSize = 1000;
		int pageNum = (size + pageSize - 1) / pageSize;
		//数据分割
		List<List<String>> mglist = new ArrayList<List<String>>();
	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
	    	mglist.add(visitidList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
	    });
	    for(List<String> subList : mglist) {
	    	Map<String, Set<String>> breakDrugContentMap = new HashMap<String, Set<String>>();
	    	Map<String, Set<String>> breakTreatContentMap = new HashMap<String, Set<String>>();	    	
	    	String visitIdFq = "VISITID:(\"" + StringUtils.join(subList, "\",\"") + "\")";
	        if(drugGroups.size()>0) {
	        	//查询超出药品
	        	SolrQuery solrQuery = new SolrQuery("*:*");
		        solrQuery.addFilterQuery(visitIdFq);
		        solrQuery.addFilterQuery("CHARGECLASS_ID:(400 OR 500 OR 600)");
		        solrQuery.addFilterQuery("ITEM_QTY:{0 TO *}");
		    	where = "*:* -(_query_:\"%sDRUGGROUP_CODE:%s\")";
		    	SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
		    	where = String.format(where, plugin.parse(), "("+StringUtils.join(drugGroups, " OR ")+")");
				solrQuery.addFilterQuery(where);
				solrQuery.setRows(Integer.MAX_VALUE);
				solrQuery.addField("VISITID");
				solrQuery.addField("ITEMCODE");
				solrQuery.addField("ITEMNAME");
				SolrDocumentList docList = SolrUtil.call(solrQuery, EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM).getResults();
		        for (SolrDocument doc : docList) {
		        	String visitid = doc.getFieldValue("VISITID").toString();
		        	String itemcode = doc.getFieldValue("ITEMCODE").toString();
		        	String itemname = doc.getFieldValue("ITEMNAME").toString();
		        	String content = itemname.concat("(").concat(itemcode).concat(")");
		        	if(breakDrugContentMap.containsKey(visitid)) {
		        		breakDrugContentMap.get(visitid).add(content);
		        	} else {
		        		 Set<String> set = new HashSet<String>();
		        		 set.add(content);
		        		 breakDrugContentMap.put(visitid, set);
		        	}
		        }
	        }
	        if(treatGroups.size()>0) {
	        	//查询超出项目
		        SolrQuery solrQuery = new SolrQuery("*:*");
		        solrQuery.addFilterQuery(visitIdFq);
		        solrQuery.addFilterQuery("-CHARGECLASS_ID:(400 OR 500 OR 600)");
		        solrQuery.addFilterQuery("ITEM_QTY:{0 TO *}");
				where = "*:* -(_query_:\"%sTREATGROUP_CODE:%s\")";
				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
				where = String.format(where, plugin.parse(), "("+StringUtils.join(treatGroups, " OR ")+")");
				solrQuery.addFilterQuery(where);
				solrQuery.setRows(Integer.MAX_VALUE);
				solrQuery.addField("VISITID");
				solrQuery.addField("ITEMCODE");
				solrQuery.addField("ITEMNAME");
				SolrDocumentList docList = SolrUtil.call(solrQuery, EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM).getResults();
		        for (SolrDocument doc : docList) {
		        	String visitid = doc.getFieldValue("VISITID").toString();
		        	String itemcode = doc.getFieldValue("ITEMCODE").toString();
		        	String itemname = doc.getFieldValue("ITEMNAME").toString();
		        	String content = itemname.concat("(").concat(itemcode).concat(")");
		        	if(breakTreatContentMap.containsKey(visitid)) {
		        		breakTreatContentMap.get(visitid).add(content);
		        	} else {
		        		 Set<String> set = new HashSet<String>();
		        		 set.add(content);
		        		 breakTreatContentMap.put(visitid, set);
		        	}
		        }
	        }

	        //写文件
	        StringBuilder content = new StringBuilder();
	        int limit = 10;
	        for(String visitid : subList) {
	        	JSONObject json = new JSONObject();
	        	SolrDocument doc = visitMap.get(visitid);
        		String id = doc.get("id").toString();
	            json.put("id", id);
	            JSONObject up = new JSONObject();
				up.put("set", MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL);
				json.put("BUSI_TYPE", up);
				content.setLength(0);
				Set<String> set = breakDrugContentMap.get(visitid);
				if(set!=null) {
					if(set.size()<=limit) {
						content.append("超出药品:"+StringUtils.join(set, ","));
					} else {
						set = ImmutableSet.copyOf(Iterables.limit(set, limit));
						content.append("超出药品:"+StringUtils.join(set, ",")).append("...");
					}
					content.append(";");
				}				
				set = breakTreatContentMap.get(visitid);
				if(set!=null) {
					if(set.size()<=limit) {
						content.append("超出项目:"+StringUtils.join(set, ","));
					} else {
						set = ImmutableSet.copyOf(Iterables.limit(set, limit));
						content.append("超出项目:"+StringUtils.join(set, ",")).append("...");
					}
					content.append(";");
				}
				int day = new BigDecimal(doc.get("ZY_DAYS").toString()).intValue();
				day = day - clinical.getInhospDaysMax();
				content.append("超出住院时间:"+day).append("天");
				up = new JSONObject();
				up.put("set", content.toString());
				json.put("BREAK_RULE_CONTENT", up);
				json.put("ACTION_NAME", "过度诊疗");
	            try {
	                fileWriter.write(json.toJSONString());
	                fileWriter.write(',');
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }

        // 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();

        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        log.info("end 计算出超住院时间+超药品+超项目不合规临床路径");
		return count;
	}

	/**
	 *
	 * 功能描述：计算出必需药品未包含或必需项目未包含不合规临床路径
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年10月9日 下午2:33:29</p>
	 *
	 * @param batch
	 * @param clinical
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private int breakDrugTreatClinicalAction(TaskProjectBatch batch, MedicalClinical clinical) throws Exception {
		log.info("start 计算出必需药品未包含或必需项目未包含不合规临床路径");
		//必需药品范围
		List<MedicalClinicalRangeGroup> drugList = clinicalSV.findClinicalRequireDrugGroup(clinical.getClinicalId());
		//按同类组分组
		Map<Integer, List<MedicalClinicalRangeGroup>> drugListMap = drugList.stream().collect(Collectors.groupingBy(MedicalClinicalRangeGroup::getGroupNo));		
		//Map<String, MedicalClinicalRangeGroup> drugGroupMap = new HashMap<String, MedicalClinicalRangeGroup>();
		//同类组编码之间使用|分隔
		Set<ClinicalRequiredGroupVO> drugRequiredGroups = new HashSet<ClinicalRequiredGroupVO>();
		for(Map.Entry<Integer, List<MedicalClinicalRangeGroup>> entry : drugListMap.entrySet()) {
			if(entry.getValue().size()==1) {
				MedicalClinicalRangeGroup record = entry.getValue().get(0);
				ClinicalRequiredGroupVO vo = new ClinicalRequiredGroupVO("drug", record.getGroupCode(), record.getGroupName());
				drugRequiredGroups.add(vo);
			} else {
				Set<String> codeSet = new HashSet<String>();
				Set<String> nameSet = new HashSet<String>();
				for(MedicalClinicalRangeGroup record : entry.getValue()) {
					codeSet.add(record.getGroupCode());
					nameSet.add(record.getGroupName());
				}
				ClinicalRequiredGroupVO vo = new ClinicalRequiredGroupVO("drug", StringUtils.join(codeSet, "|"), StringUtils.join(nameSet, "|"));
				drugRequiredGroups.add(vo);
			}
		}
		//必需检查项目范围
		List<MedicalClinicalRangeGroup> treatList  = clinicalSV.findClinicalRequireTreatGroup(clinical.getClinicalId());
		//按同类组分组
		Map<Integer, List<MedicalClinicalRangeGroup>> treatListMap = treatList.stream().collect(Collectors.groupingBy(MedicalClinicalRangeGroup::getGroupNo));
		//Map<String, MedicalClinicalRangeGroup> treatGroupMap = new HashMap<String, MedicalClinicalRangeGroup>();
		//同类组编码之间使用|分隔
		Set<ClinicalRequiredGroupVO> treatRequiredGroups = new HashSet<ClinicalRequiredGroupVO>();
		for(Map.Entry<Integer, List<MedicalClinicalRangeGroup>> entry : treatListMap.entrySet()) {
			if(entry.getValue().size()==1) {
				MedicalClinicalRangeGroup record = entry.getValue().get(0);
				ClinicalRequiredGroupVO vo = new ClinicalRequiredGroupVO("project", record.getGroupCode(), record.getGroupName());
				treatRequiredGroups.add(vo);
			} else {
				Set<String> codeSet = new HashSet<String>();
				Set<String> nameSet = new HashSet<String>();
				for(MedicalClinicalRangeGroup record : entry.getValue()) {
					codeSet.add(record.getGroupCode());
					nameSet.add(record.getGroupName());
				}
				ClinicalRequiredGroupVO vo = new ClinicalRequiredGroupVO("project", StringUtils.join(codeSet, "|"), StringUtils.join(nameSet, "|"));
				treatRequiredGroups.add(vo);
			}
		}
		if(drugRequiredGroups.size()==0 && treatRequiredGroups.size()==0) {
			return 0;
		}
		//必需药品未包含或必需项目未包含计算
		Map<String, Set<ClinicalRequiredGroupVO>> visitLoseRequiredMap = new HashMap<String, Set<ClinicalRequiredGroupVO>>();	
		for(ClinicalRequiredGroupVO vo : drugRequiredGroups) {
			this.breakDrugTreatClinicalAction(batch, clinical, "drug", vo, visitLoseRequiredMap);
		}
		for(ClinicalRequiredGroupVO vo : treatRequiredGroups) {
			this.breakDrugTreatClinicalAction(batch, clinical, "treat", vo, visitLoseRequiredMap);
		}
		
		//临床路径违规内容写入json文件
        String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/" + clinical.getClinicalId() + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");
        for(Map.Entry<String, Set<ClinicalRequiredGroupVO>> entry : visitLoseRequiredMap.entrySet()) {
        	JSONObject json = new JSONObject();    		
            json.put("id", entry.getKey());
            JSONObject up = new JSONObject();
			Set<ClinicalRequiredGroupVO> loseRequiredSet = entry.getValue();
			StringBuilder sb = new StringBuilder();
			sb.append("未包含药品或项目组:");
			for(ClinicalRequiredGroupVO vo : loseRequiredSet) {
				sb.append(vo.getGroupNames());
				sb.append("(").append(vo.getGroupCodes()).append(")");
				sb.append(";");
			}
			up.put("set", sb.toString());
			json.put("BREAK_RULE_CONTENT", up);
			json.put("ACTION_NAME", "诊断不合理");
			try {
                fileWriter.write(json.toJSONString());
                fileWriter.write(',');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }        
        // 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();

        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
        log.info("end 计算出必需药品未包含或必需项目未包含不合规临床路径");
		return visitLoseRequiredMap.size();
	}
	
	/**
	 * 
	 * 功能描述：按每个必需项目计算不合规临床路径
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月20日 下午4:43:15</p>
	 *
	 * @param batch
	 * @param clinical
	 * @param type
	 * @param requiredVO
	 * @param visitLoseRequiredMap:存储每条结果对应的必需项目未做
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private void breakDrugTreatClinicalAction(TaskProjectBatch batch, MedicalClinical clinical, String type, ClinicalRequiredGroupVO requiredVO, 
			Map<String, Set<ClinicalRequiredGroupVO>> visitLoseRequiredMap) throws Exception {
		List<String> conditionList = new ArrayList<String>();
		String where = "BATCH_ID:%s";
		where = String.format(where, batch.getBatchId());
		conditionList.add(where);
		where = "ZY_DAYS:{0 TO %s]";
		where = String.format(where, clinical.getInhospDaysMax());
		conditionList.add(where);
		StringBuilder condition = new StringBuilder();
		String requiredCode = requiredVO.getGroupCodes();
		if(requiredCode.indexOf("|")>-1) {
			requiredCode = "(" + StringUtils.replace(requiredCode, "|", " OR ") + ")";
		}
		StringBuilder sb = new StringBuilder();
		if("drug".equals(type)) {
			sb.setLength(0);
			sb.append("*:* -(");
			sb.append("_query_:\"");
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWS_PATIENT_1VISIT_ITEMSUM", "VISITID", "VISITID");
			sb.append(plugin.parse());
			sb.append("CHARGECLASS_ID:(400 OR 500 OR 600) AND ITEM_QTY:{0 TO *}");
			sb.append(" AND ");
			sb.append("_query_:\\\"");
			plugin = new SolrJoinParserPlugin("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
			sb.append(plugin.parse());
			sb.append("DRUGGROUP_CODE:").append(requiredCode);
			sb.append("\\\"");
			sb.append("\"");
			sb.append(")");
			conditionList.add(sb.toString());
		} else {
			sb.setLength(0);
			sb.append("*:* -(");
			sb.append("_query_:\"");
			SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWS_PATIENT_1VISIT_ITEMSUM", "VISITID", "VISITID");
			sb.append(plugin.parse());
			sb.append("-CHARGECLASS_ID:(400 OR 500 OR 600) AND ITEM_QTY:{0 TO *}");
			sb.append(" AND ");
			sb.append("_query_:\\\"");
			plugin = new SolrJoinParserPlugin("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
			sb.append(plugin.parse());
			sb.append("DRUGGROUP_CODE:").append(requiredCode);
			sb.append("\\\"");
			sb.append("\"");
			sb.append(")");
			conditionList.add(sb.toString());
		}
		conditionList.add(condition.toString());
		// 数据写入json文件
        String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/" + clinical.getClinicalId() + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");
        int count = SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (doc, index) -> {
        	String id = doc.get("id").toString();
        	if(!visitLoseRequiredMap.containsKey(id)) {
				Set<ClinicalRequiredGroupVO> set = new HashSet<ClinicalRequiredGroupVO>();
				set.add(requiredVO);
				visitLoseRequiredMap.put(id, set);
			} else {
				visitLoseRequiredMap.get(id).add(requiredVO);
			}
			JSONObject json = new JSONObject();    		
            json.put("id", id);
            JSONObject up = new JSONObject();
			up.put("set", MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL);
			json.put("BUSI_TYPE", up);
			/*up = new JSONObject();
			up.put("set", requiredVO.getGroupCodes());
			json.put("MUTEX_ITEM_CODE", up);
			up = new JSONObject();
			up.put("set", requiredVO.getGroupCodes().concat(EngineUtil.SPLIT_KEY).concat(requiredVO.getGroupNames()));
			json.put("MUTEX_ITEM_NAME", up);*/
			try {
                fileWriter.write(json.toJSONString());
                fileWriter.write(',');
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();

        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
	}
	
	private JSONObject constructJson(Map<String, Object> map, TaskProject task, TaskProjectBatch batch, 
			MedicalClinical clinical, MedicalClinicalInfo clinicalInfo, MedicalClinicalAccessGroup group,
			ActionDTO actionDTO) {
		JSONObject json = new JSONObject();
		//String id = MD5Util.MD5Encode(batch.getBatchId().concat("_").concat(group.getClinicalId()).concat("_").concat((String) map.get("id")), "utf-8");
		//id生成策略
    	String template = "${batchId}_${clinicalId}_${visitid}";
        Properties properties = new Properties();
        properties.put("batchId", batch.getBatchId());
        properties.put("clinicalId", group.getClinicalId());
        properties.put("visitid", map.get("VISITID"));
        template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
        String id = MD5Util.MD5Encode(template, "UTF-8");
        json.put("id", id);

        for(Map.Entry<String, String> entry : EngineCaseServiceImpl.CASE_FIELD_MAPPING.entrySet()) {
            Object val = map.get(entry.getValue());
            if (val != null) {
                json.put(entry.getKey(), val);
            }
        }
        json.put("GEN_DATA_TIME", DateUtils.now());
        json.put("PROJECT_ID", task.getProjectId());
        json.put("PROJECT_NAME", task.getProjectName());
        json.put("BATCH_ID", batch.getBatchId());
        json.put("TASK_BATCH_NAME", batch.getBatchName());
        json.put("BUSI_TYPE", "TMP_".concat(MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL));
        //医保基金支付金额
        json.put("ACTION_MONEY", map.get("FUNDPAY"));
        json.put("MAX_ACTION_MONEY", map.get("FUNDPAY"));
        //就诊总金额
        json.put("MIN_MONEY", map.get("TOTALFEE"));
        json.put("MAX_MONEY", map.get("TOTALFEE"));
        json.put("CLINICAL_DRUG_MONEY", 0);
        json.put("CLINICAL_TREAT_MONEY", 0);
        json.put("CLINICAL_DRUG_MONEY_RATIO", 0);
        json.put("CLINICAL_TREAT_MONEY_RATIO", 0);
        json.put("FIR_REVIEW_STATUS", "init");

        json.put("ACTION_TYPE_ID", actionDTO.getActionTypeId());
        json.put("ACTION_TYPE_NAME", actionDTO.getActionTypeName());
        json.put("ACTION_ID", actionDTO.getActionId());
        json.put("ACTION_NAME", actionDTO.getActionName());
        
        json.put("CASE_ID", clinical.getClinicalCode());
    	json.put("CASE_NAME", clinical.getClinicalName());
    	json.put("RULE_ID", clinical.getClinicalId());
    	json.put("RULE_FNAME", clinical.getClinicalName());
    	json.put("RULE_BASIS", clinicalInfo.getClinicalFile());
        JSONObject clinicalJson = new JSONObject();
        clinicalJson.put("add", group.getGroupId());
        json.put("CLINICAL_GROUP_IDS", clinicalJson);
        clinicalJson = new JSONObject();
        clinicalJson.put("add", group.getGroupId()+"::"+group.getGroupName());
        json.put("CLINICAL_GROUP_NAMES", clinicalJson);
        return json;
    }

	private EngineClinicalRule getEngineClinicalRule(MedicalClinicalAccessGroup group) {
		EngineClinicalRule rule = new EngineClinicalRule();
		rule.setMinAge(group.getPatientAgeMin());
		rule.setMaxAge(group.getPatientAgeMax());
		rule.setAgeUnit(group.getPatientAgeUnit());
		rule.setDiseaseGroupCode(group.getDiseaseGroups());
		rule.setOperationCode(group.getOperations());
		rule.setTreatCode(group.getCheckItems());
		rule.setDurgGroupCode(group.getDrugGroups());
		rule.setPathologys(group.getPathologys());
		return rule;
	}

	/**
	 *
	 * 功能描述：获取临床路径的排除条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年4月23日 下午5:09:16</p>
	 *
	 * @param clinicalId
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private List<EngineClinicalRule> getExcludeClinicalRule(String clinicalId) {
		List<MedicalClinicalAccessGroup> list = clinicalSV.findMedicalClinicalAccessGroup(clinicalId, "reject");
		if(list==null || list.size()==0) {
			return null;
		}
		List<EngineClinicalRule> result = new ArrayList<EngineClinicalRule>();
		for(MedicalClinicalAccessGroup group : list) {
			EngineClinicalRule rule = this.getEngineClinicalRule(group);
			result.add(rule);
		}
		return result;
	}

	@Override
	public void generateUnreasonableAction(String batchId) {
		RTimer rtimer = new RTimer();
		SimpleDateFormat df = new SimpleDateFormat("H:mm:ss.SSS", Locale.getDefault());
    	df.setTimeZone(TimeZone.getTimeZone("UTC"));    	

		boolean success = true;
		StringBuilder error = new StringBuilder();
		try {
			TaskBatchStepItem step = new TaskBatchStepItem();
			step.setUpdateTime(new Date());
			step.setStartTime(new Date());
			step.setStatus(MedicalConstant.RUN_STATE_RUNNING);
			taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_CLINICAL_NEW, step);
			
			TaskProjectBatch batch = taskSV.findTaskProjectBatch(batchId);
	        if (batch == null) {
	            throw new RuntimeException("未找到任务批次");
	        }
	        TaskProject task = taskSV.findTaskProject(batch.getProjectId());
	        if (task == null) {
	            throw new RuntimeException("未找到项目");
	        }
	        //先导出已审核的不合规行为结果
			EngineCntResult cntResult = engineActionService.importApprovalAction(batchId, MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL);
			//删除历史数据
	        String where = "BUSI_TYPE:*%s AND BATCH_ID:%s";
	        where = String.format(where, MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL, batchId);
	        SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);

			List<TaskBatchBreakRule> batchRuleList = taskSV.findTaskBatchBreakRuleByStep(batchId, MedicalConstant.RULE_TYPE_CLINICAL_NEW);
			if(batchRuleList!=null && batchRuleList.size()>0) {
				int index = 0, fail = 0;
				int count = batchRuleList.size();
				log.info("批次临床路径数量:"+count);
				for (TaskBatchBreakRule item : batchRuleList) {
					RTimer timer = new RTimer();
					log.info("start 临床路径:"+item.getRuleId());
					
					EngineResult res = this.generate(task, batch, item.getRuleId());
					success = success && res.isSuccess();
					
			        log.info("end 临床路径:"+item.getRuleId());
		        	log.info("耗时:" + df.format(new Date((long)timer.getTime())));
					log.info(batchId+"批次临床路径数量:"+count+"，已跑临床路径数量:"+(++index) + "，失败数量:"+fail);
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
			TaskBatchStepItem step = new TaskBatchStepItem();
			step.setUpdateTime(new Date());
			step.setEndTime(new Date());
			step.setStatus(success ? MedicalConstant.RUN_STATE_NORMAL : MedicalConstant.RUN_STATE_ABNORMAL);
			if (!success) {
                String msg = error.length() > 2000 ? error.substring(0, 2000) : error.toString();
                step.setMsg(msg);
            }
			taskSV.updateTaskBatchStepItem(batchId, MedicalConstant.RULE_TYPE_CLINICAL_NEW, step);
			log.info(batchId+"批次临床路径总耗时:" + df.format(new Date((long)rtimer.getTime())));
		}
	}

	@Override
	public void generateUnreasonableAction(String batchId, String itemCode) {

	}

	/**
	 *
	 * 功能描述：计算临床路径药品与项目范围内的费用
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年9月4日 下午5:06:22</p>
	 *
	 * @param task
	 * @param batch
	 * @param clinicalId
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private void statisticsClinicalMoney(TaskProject task, TaskProjectBatch batch, String clinicalId) throws Exception {
		//药品、项目范围
		List<MedicalClinicalRangeGroup> rangeList = clinicalSV.findMedicalClinicalRangeGroup(clinicalId);
		if(rangeList==null || rangeList.size()==0) {
			return;
		}
		//药品
		Set<String> drugGroups = new HashSet<String>();
		//检查项目
		Set<String> treatGroups = new HashSet<String>();
		for(MedicalClinicalRangeGroup record : rangeList) {
			if("drug".equalsIgnoreCase(record.getGroupType())) {
				drugGroups.add(record.getGroupCode());
			} else {
				treatGroups.add(record.getGroupCode());
			}
		}

		List<String> conditionList = new ArrayList<String>();
    	conditionList.add("BATCH_ID:"+batch.getBatchId());
    	conditionList.add("CASE_ID:"+clinicalId);
    	conditionList.add("BUSI_TYPE:"+MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL);

    	List<String> visitidList = new ArrayList<String>();
    	Map<String, Map<String, Object>> masterMap = new HashMap<String, Map<String, Object>>();
    	int count = SolrUtil.exportByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (map, index) -> {
    		String visitid = map.get("VISITID").toString();
    		masterMap.put(visitid, map);
    		visitidList.add(visitid);
        });

    	if(count==0) {
    		return;
    	}

    	int pageSize = 1000;
		int pageNum = (visitidList.size() + pageSize - 1) / pageSize;
		//数据分割
		List<List<String>> mglist = new ArrayList<>();
	    Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
	    	mglist.add(visitidList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
	    });

	    // 数据写入文件
        String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/" + clinicalId + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));

        //写文件头
        fileWriter.write("[");
	    for(List<String> list : mglist) {
	    	if(drugGroups.size()>0) {
	    		//计算临床路径药品范围内的金额
		    	this.writeUpdateClinicalJson(fileWriter, batch.getBatchId(), list, drugGroups, masterMap, "drug");
	    	}
	    	if(treatGroups.size()>0) {
	    		//计算临床路径检查项目内的金额
	    		this.writeUpdateClinicalJson(fileWriter, batch.getBatchId(), list, treatGroups, masterMap, "treat");
	    	}
	    }

	    // 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();
        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
	}

	/**
	 *
	 * 功能描述：临床路径药品与项目范围内的费用写入文件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年9月4日 下午5:45:50</p>
	 *
	 * @param fileWriter
	 * @param batchId
	 * @param visitidList
	 * @param itemcodeList
	 * @param masterMap
	 * @param groupType
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private void writeUpdateClinicalJson(BufferedWriter fileWriter, String batchId,
			List<String> visitidList, Set<String> itemcodeList,
			Map<String, Map<String, Object>> masterMap, String groupType) throws Exception {

		ReportParamModel reportModel = new ReportParamModel();
		reportModel.addWhere("VISITID:(\""+StringUtils.join(visitidList, "\",\"")+"\")");
		String where = "%sBATCH_ID:%s AND BUSI_TYPE:%s";
		SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("MEDICAL_UNREASONABLE_ACTION", "VISITID", "VISITID");
		where = String.format(where, plugin.parse(), batchId, MedicalConstant.ENGINE_BUSI_TYPE_CLINICAL);
		reportModel.addWhere(where);
		if("drug".equalsIgnoreCase(groupType)) {
			where = "_query_:\"%sDRUGGROUP_CODE:%s\"";
			plugin = new SolrJoinParserPlugin("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
			where = String.format(where, plugin.parse(), "("+StringUtils.join(itemcodeList, " OR ")+")");
			reportModel.addWhere(where);
		} else {
			where = "_query_:\"%sTREATGROUP_CODE:%s\"";
			plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
			where = String.format(where, plugin.parse(), "("+StringUtils.join(itemcodeList, " OR ")+")");
			reportModel.addWhere(where);
		}

		reportModel.setxLimit(visitidList.size());
    	reportModel.setGroupBy(new String[] {"VISITID"});
    	reportModel.setStaFunction("sum(ITEM_AMT)");
    	reportModel.setSolrCollection(EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM);
    	AbstractReportHandler handler = new BaseReportHandler(reportModel);
    	List<ReportFacetBucketField> bucketList = handler.singleDimCallSolr();
    	for(ReportFacetBucketField bucket : bucketList) {
    		String visitid = bucket.getField();
    		//临床路径范围内金额
    		BigDecimal money = bucket.getValue();
    		Map<String, Object> master = masterMap.get(visitid);
			if(master!=null) {
				//病例就诊总金额
				BigDecimal total = BigDecimal.ZERO;
				//临床路径范围内金额占比
				BigDecimal rate = BigDecimal.ZERO;
				if(master.get("TOTALFEE")!=null) {
					total = new BigDecimal(master.get("TOTALFEE").toString());
					total = total.setScale(2, BigDecimal.ROUND_HALF_UP);
					if(total.compareTo(BigDecimal.ZERO)>0) {
						rate = money;
						rate = rate.divide(total, 4, BigDecimal.ROUND_HALF_UP);
						rate = rate.multiply(new BigDecimal(100));
					}
				}

				JSONObject json = new JSONObject();
				json.put("id", master.get("id"));
				JSONObject up = new JSONObject();
				up.put("set", money);
				if("drug".equalsIgnoreCase(groupType)) {
					json.put("CLINICAL_DRUG_MONEY", up);
					up = new JSONObject();
					up.put("set", rate);
					json.put("CLINICAL_DRUG_MONEY_RATIO", up);
				} else {
					json.put("CLINICAL_TREAT_MONEY", up);
					up = new JSONObject();
					up.put("set", rate);
					json.put("CLINICAL_TREAT_MONEY_RATIO", up);
				}
                fileWriter.write(json.toJSONString());
                fileWriter.write(',');
			}
    	}
	}
}
