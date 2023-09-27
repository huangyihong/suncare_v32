/**
 * SolrCaseHandle.java	  V1.0   2022年12月6日 上午11:04:59
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.MD5Util;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.common.MedicalConstant;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.grade.AbsGradeHandle;
import com.ai.modules.engine.handle.grade.GradeHandleFactory;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.model.EngineRuleGrade;
import com.ai.modules.engine.model.vo.ProjectFilterWhereVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.service.IEngineActionService;
import com.ai.modules.engine.service.IEngineService;
import com.ai.modules.engine.service.api.IApiCaseService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.service.impl.EngineCaseServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.PlaceholderResolverUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.formal.entity.MedicalFormalCaseItemRela;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SolrCaseHandle extends AbsCaseHandle {

	public SolrCaseHandle(String datasource, TaskProject task, TaskProjectBatch batch, HisMedicalFormalCase formalCase) {
		super(datasource, task, batch, formalCase);
	}

	@Override
	public EngineResult generateUnreasonableAction() throws Exception {
		if (this.ignoreRun()) {
			//忽略运行
			return EngineResult.ok();
		}
		int count = 0;
		final BigDecimal[] money = { BigDecimal.ZERO, BigDecimal.ZERO };
		if (!"normal".equalsIgnoreCase(formalCase.getCaseStatus())) {
			throw new EngineBizException("模型不是正常启用状态！");
		}
		String batchId = batch.getBatchId();
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IEngineService engineService = context.getBean(IEngineService.class);
		List<List<EngineNode>> flowList = engineService.queryHisFormalEngineNode(formalCase.getCaseId(), batchId);
		if (flowList == null || flowList.size() == 0) {
			throw new EngineBizException("模型未能找到流程节点！");
		}

		String project_startTime = MedicalConstant.DEFAULT_START_TIME;
		String project_endTime = MedicalConstant.DEFAULT_END_TIME;
		String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
		String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
		String case_startTime = MedicalConstant.DEFAULT_START_TIME;
		String case_endTime = MedicalConstant.DEFAULT_END_TIME;
		project_startTime = task.getDataStartTime() != null ? DateUtil.format(task.getDataStartTime(), "yyyy-MM-dd")
				: project_startTime;
		project_endTime = task.getDataEndTime() != null ? DateUtil.format(task.getDataEndTime(), "yyyy-MM-dd")
				: project_endTime;
		batch_startTime = batch.getStartTime() != null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd")
				: batch_startTime;
		batch_endTime = batch.getEndTime() != null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
		case_startTime = formalCase.getStartTime() != null
				? DateUtils.formatDate(formalCase.getStartTime(), "yyyy-MM-dd")
				: case_startTime;
		case_endTime = formalCase.getEndTime() != null ? DateUtils.formatDate(formalCase.getEndTime(), "yyyy-MM-dd")
				: case_endTime;

		List<String> conditionList = new ArrayList<String>();
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		//评分列表
		IApiCaseService caseSV = context.getBean(IApiCaseService.class);
		List<EngineRuleGrade> gradeList = caseSV.findEngineRuleGrade(batchId, formalCase.getCaseId());
		for (int i = 0, len = flowList.size(); i < len; i++) {
			conditionList.clear();
			List<EngineNode> subList = flowList.get(i);
			Set<String> fqSet = engineService.constructConditionExpression(subList);
			if (fqSet != null) {
				conditionList.addAll(fqSet);
			}
			if (i > 0) {
				//排除已经跑出的数据
				String where = "*:* -(_query_:\"%sBUSI_TYPE:CASE AND BATCH_ID:%s\")";
				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("MEDICAL_UNREASONABLE_ACTION", "VISITID",
						"VISITID");
				where = String.format(where, plugin.parse(), batchId);
				conditionList.add(where);
			}
			//项目过滤条件
			IEngineActionService engineActionService = context.getBean(IEngineActionService.class);
			ProjectFilterWhereVO filterVO = engineActionService.filterCondition(task, true);
			if (StringUtils.isNotBlank(filterVO.getCondition())) {
				conditionList.add(filterVO.getCondition());
			}
			if (filterVO.isDiseaseFilter() && EngineUtil.caseExistsDisease(subList)) {
				//疾病映射不全过滤
				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_DIAG", "VISITID", "VISITID");
				conditionList.add("*:* -" + plugin.parse() + "-DISEASENAME:?*");
			}
			//项目的数据来源
			if (StringUtils.isNotBlank(task.getEtlSource())) {
				conditionList.add("ETL_SOURCE:" + task.getEtlSource());
			}
			//批次的数据来源
			if (StringUtils.isNotBlank(batch.getEtlSource())) {
				conditionList.add("ETL_SOURCE:" + batch.getEtlSource());
			}
			//业务数据时间范围
			conditionList.add("VISITDATE:[" + project_startTime + " TO " + project_endTime + "]");
			conditionList.add("VISITDATE:[" + batch_startTime + " TO " + batch_endTime + "]");
			conditionList.add("VISITDATE:[" + case_startTime + " TO " + case_endTime + "]");
			if (StringUtils.isNotBlank(task.getDataOrgFilter())) {
				//医疗机构范围限制
				String value = task.getDataOrgFilter();
				value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
				conditionList.add("ORGID:" + value);
			}
			if (StringUtils.isNotBlank(batch.getCustomFilter())) {
				//自定义数据范围限制
				//conditionList.add(batch.getCustomFilter());
				String value = batch.getCustomFilter();
				value = "(" + StringUtils.replace(value, ",", " OR ") + ")";
				conditionList.add("ORGID:" + value);
			}
			if ("1".equals(batch.getYbFundRm0())) {
				//过滤掉病例基金支出金额为0的数据
				conditionList.add("FUNDPAY:{0 TO *}");
			}

			IApiDictService dictSV = context.getBean(IApiDictService.class);
			Map<String, MedicalActionDict> actionDictMap = dictSV.queryActionDict();
			// 数据写入xml
			String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId() + "/"
					+ formalCase.getCaseId() + ".json";
			BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(
					FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
			//写文件头
			fileWriter.write("[");

			int cnt = SolrUtil.exportByPager(conditionList, EngineUtil.DWB_MASTER_INFO, (map, index) -> {
				// 循环一条数据里面的内容
				if (map.get("TOTALFEE") != null) {
					money[0] = money[0].add(new BigDecimal(map.get("TOTALFEE").toString()));
				}
				if (map.get("FUNDPAY") != null) {
					money[1] = money[1].add(new BigDecimal(map.get("FUNDPAY").toString()));
				}
				JSONObject json = this.constructJson(map, task, batch, formalCase, gradeList, actionDictMap);
				dataList.add(json);
				try {
					fileWriter.write(json.toJSONString());
					fileWriter.write(',');
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			count = count + cnt;
			// 文件尾
			fileWriter.write("]");
			fileWriter.flush();
			fileWriter.close();
			//导入solr
			SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
		}
		if (count == 0) {
			return EngineResult.ok();
		}

		EngineResult result = EngineResult.ok();
		MedicalFormalCaseItemRela rela = caseSV.findMedicalFormalCaseItemRela(formalCase.getCaseId());
		if (rela != null && StringUtils.isNotBlank(rela.getItemIds())) {
			//模型项目层级的违规
			result = this.splitCaseAction(dataList, task, batchId, formalCase.getCaseId(), rela);
		} else {
			BigDecimal actionMoney = money[1];
			result.setCount(count);
			result.setMoney(money[0]);
			result.setActionMoney(actionMoney);
		}

		if ("A01".equals(batch.getOutSource()) && "A03".equals(batch.getEtlSource())) {
			//转换输出结果
			this.transformation(task, batch, formalCase);
		}
		return result;
	}

	private JSONObject constructJson(Map<String, Object> map, TaskProject task, TaskProjectBatch batch,
			HisMedicalFormalCase formalCase, List<EngineRuleGrade> gradeList,
			Map<String, MedicalActionDict> actionDictMap) {
		//String id = MD5Util.MD5Encode(batch.getBatchId().concat("_").concat(formalCase.getCaseId()).concat("_").concat((String) map.get("VISITID")), "UTF-8");
		//id生成策略
		String template = "${batchId}_${caseId}_${visitid}";
		Properties properties = new Properties();
		properties.put("batchId", batch.getBatchId());
		properties.put("caseId", formalCase.getCaseId());
		properties.put("visitid", map.get("VISITID"));
		template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
		String id = MD5Util.MD5Encode(template, "UTF-8");
		JSONObject json = new JSONObject();
		json.put("id", id);

		for (Map.Entry<String, String> entry : EngineCaseServiceImpl.CASE_FIELD_MAPPING.entrySet()) {
			Object val = map.get(entry.getValue());
			if (val != null) {
				json.put(entry.getKey(), val);
			}
		}
		json.put("CASE_ID", formalCase.getCaseId());
		json.put("CASE_NAME", formalCase.getCaseName());
		//模型编码、模型名称当做原始值沉淀
		json.put("ITEMCODE_SRC", formalCase.getCaseId());
		json.put("ITEMNAME_SRC", formalCase.getCaseName());
		if (formalCase.getActionName() != null) {
			json.put("ACTION_NAME", formalCase.getActionName());
		}
		if (formalCase.getActionId() != null) {
			json.put("ACTION_ID", formalCase.getActionId());
		}
		if (actionDictMap.containsKey(formalCase.getActionId())) {
			MedicalActionDict actionDict = actionDictMap.get(formalCase.getActionId());
			json.put("ACTION_NAME", actionDict.getActionName());
			json.put("RULE_LEVEL", actionDict.getRuleLevel());
		}
		if (formalCase.getActionType() != null) {
			json.put("ACTION_TYPE_ID", formalCase.getActionType());
		}
		if (formalCase.getActionTypeName() != null) {
			json.put("ACTION_TYPE_NAME", formalCase.getActionTypeName());
		}
		if (formalCase.getActionDesc() != null) {
			json.put("ACTION_DESC", formalCase.getActionDesc());
		}
		json.put("RULE_BASIS", formalCase.getRuleBasis());
		json.put("RULE_GRADE", formalCase.getRuleGrade());
		json.put("RULE_GRADE_REMARK", formalCase.getRuleGradeRemark());
		json.put("BUSI_TYPE", MedicalConstant.ENGINE_BUSI_TYPE_CASE);
		json.put("GEN_DATA_TIME", DateUtils.now());
		json.put("PROJECT_ID", task.getProjectId());
		json.put("PROJECT_NAME", task.getProjectName());
		json.put("BATCH_ID", batch.getBatchId());
		json.put("TASK_BATCH_NAME", batch.getBatchName());
		//医保基金支付金额
		json.put("ACTION_MONEY", map.get("FUNDPAY"));
		json.put("MAX_ACTION_MONEY", map.get("FUNDPAY"));
		//就诊总金额
		json.put("MIN_MONEY", map.get("TOTALFEE"));
		json.put("MAX_MONEY", map.get("TOTALFEE"));
		json.put("FIR_REVIEW_STATUS", "init");

		//计算得分
		boolean need = false;
		if (need && gradeList != null && gradeList.size() > 0) {
			BigDecimal score = new BigDecimal(0);
			for (EngineRuleGrade grade : gradeList) {
				GradeHandleFactory factory = new GradeHandleFactory(grade, json);
				AbsGradeHandle handle = factory.build();
				score = score.add(handle.grade());
			}
			score = score.setScale(2, BigDecimal.ROUND_HALF_UP);
			json.put("CASE_SCORE", score.doubleValue());
		}
		return json;
	}

	/**
	 * 
	 * 功能描述：模型项目层级违规，按项目拆分输出结果
	 *
	 * @author  zhangly
	 *
	 * @param dataList
	 * @param task
	 * @param batchId
	 * @param caseId
	 * @param rela
	 * @return
	 * @throws Exception
	 */
	private EngineResult splitCaseAction(List<JSONObject> dataList, TaskProject task, String batchId, String caseId,
			MedicalFormalCaseItemRela rela) throws Exception {
		EngineResult result = EngineResult.ok();
		if (dataList == null || dataList.size() == 0) {
			return result;
		}
		Map<String, JSONObject> visitMap = new HashMap<String, JSONObject>();
		for (JSONObject jsonObject : dataList) {
			visitMap.put(jsonObject.getString("VISITID"), jsonObject);
		}
		// 数据写入文件
		String importFilePath = SolrUtil.importFolder + "/" + EngineUtil.MEDICAL_UNREASONABLE_ACTION + "/" + batchId
				+ "/" + caseId + ".json";
		BufferedWriter fileWriter = new BufferedWriter(
				new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
		//写文件头
		fileWriter.write("[");

		List<String> conditionList = new ArrayList<String>();
		String where = "_query_:\"%sBATCH_ID:%s AND CASE_ID:%s\"";
		SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("MEDICAL_UNREASONABLE_ACTION", "VISITID", "VISITID");
		where = String.format(where, plugin.parse(), batchId, caseId);
		conditionList.add(where);
		if (MedicalConstant.CASE_RELA_TYPE_DRUGGROUP.equals(rela.getType())) {
			//药品组
			String value = "(\\\"" + StringUtils.replace(rela.getItemIds(), ",", "\\\",\\\"") + "\\\")";
			where = "_query_:\"%sDRUGGROUP_CODE:%s\"";
			plugin = new SolrJoinParserPlugin("STD_DRUGGROUP", "ATC_DRUGCODE", "ITEMCODE");
			where = String.format(where, plugin.parse(), value);
			conditionList.add(where);
		} else if (MedicalConstant.CASE_RELA_TYPE_PROJECTGROUP.equals(rela.getType())) {
			//项目组
			String value = "(\\\"" + StringUtils.replace(rela.getItemIds(), ",", "\\\",\\\"") + "\\\")";
			where = "_query_:\"%sTREATGROUP_CODE:%s\"";
			plugin = new SolrJoinParserPlugin("STD_TREATGROUP", "TREATCODE", "ITEMCODE");
			where = String.format(where, plugin.parse(), value);
			conditionList.add(where);
		} else if (MedicalConstant.CASE_RELA_TYPE_DRUG.equals(rela.getType())) {
			//查找药品编码
			String value = "(\"" + StringUtils.replace(rela.getItemIds(), ",", "\",\"") + "\")";
			conditionList.add("ITEMCODE:" + value);
		} else {
			//项目
			String value = "(\"" + StringUtils.replace(rela.getItemIds(), ",", "\",\"") + "\")";
			conditionList.add("ITEMCODE:" + value);
		}

		int count = SolrUtil.exportDocByPager(conditionList, EngineUtil.DWS_PATIENT_1VISIT_ITEMSUM, false,
				(doc, idx) -> {
					JSONObject jsonObject = new JSONObject();
					String visitid = doc.get("VISITID").toString();
					if (visitMap.containsKey(visitid)) {
						JSONObject visitJson = visitMap.get(visitid);
						for (Map.Entry<String, Object> entry : visitJson.entrySet()) {
							jsonObject.put(entry.getKey(), entry.getValue());
						}
						String itemcode = doc.get("ITEMCODE").toString();
						String itemname = doc.get("ITEMNAME").toString();
						//id生成策略
						String template = "${batchId}_${caseId}_${itemcode}_${visitid}";
						Properties properties = new Properties();
						properties.put("batchId", visitJson.get("BATCH_ID"));
						properties.put("caseId", visitJson.get("CASE_ID"));
						properties.put("itemcode", itemcode);
						properties.put("visitid", visitJson.get("VISITID"));
						template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
						String id = MD5Util.MD5Encode(template, "UTF-8");
						jsonObject.put("id", id);
						jsonObject.put("ITEMCODE", itemcode);
						jsonObject.put("ITEMNAME", itemname);
						jsonObject.put("ITEMNAME_SRC", doc.get("ITEMNAME_SRC"));
						jsonObject.put("CHARGECLASS_ID", doc.get("CHARGECLASS_ID"));
						jsonObject.put("CHARGECLASS", doc.get("CHARGECLASS"));
						jsonObject.put("ITEM_AMT", doc.get("ITEM_AMT"));
						jsonObject.put("ITEM_QTY", doc.get("ITEM_QTY"));
						jsonObject.put("FUND_COVER", doc.get("FUND_COVER"));
						jsonObject.put("ITEMPRICE_MAX", doc.get("ITEMPRICE_MAX"));
						jsonObject.put("SELFPAY_PROP_MIN", doc.get("SELFPAY_PROP_MIN"));
						//违规基金支出金额
						jsonObject.put("ACTION_MONEY", doc.get("FUND_COVER"));
						jsonObject.put("MAX_ACTION_MONEY", doc.get("FUND_COVER"));
						//违规金额
						jsonObject.put("MIN_MONEY", doc.get("ITEM_AMT"));
						jsonObject.put("MAX_MONEY", doc.get("ITEM_AMT"));

						if (doc.get("FUND_COVER") != null) {
							BigDecimal money = new BigDecimal(doc.get("FUND_COVER").toString());
							money = result.getActionMoney().add(money);
							result.setActionMoney(money);
						}
						if (doc.get("ITEM_AMT") != null) {
							BigDecimal money = new BigDecimal(doc.get("ITEM_AMT").toString());
							money = result.getMoney().add(money);
							result.setMoney(money);
						}
					}
					try {
						fileWriter.write(jsonObject.toJSONString());
						fileWriter.write(',');
					} catch (Exception e) {
					}
				});
		result.setCount(count);

		// 文件尾
		fileWriter.write("]");
		fileWriter.flush();
		fileWriter.close();
		//导入solr
		SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);

		//删除itemcode为空的数据
		where = "-ITEMCODE:?* AND BATCH_ID:%s AND CASE_ID:%s";
		where = String.format(where, batchId, caseId);
		SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
		return result;
	}

	/**
	 * 
	 * 功能描述：按批次每个模型转换输出结果
	 *
	 * @author  zhangly
	 *
	 * @param task
	 * @param batch
	 * @param formalCase
	 */
	private void transformation(TaskProject task, TaskProjectBatch batch, HisMedicalFormalCase formalCase) {
		try {
			log.info("{}({})模型，开始转换输出结果", formalCase.getCaseName(), formalCase.getCaseId());
			List<String> conditionList = new ArrayList<String>();
			conditionList.add("BATCH_ID:" + batch.getBatchId());
			conditionList.add("CASE_ID:" + formalCase.getCaseId());

			Map<String, Map<String, Object>> srcMap = new HashMap<String, Map<String, Object>>();
			List<String> visitidDummys = new ArrayList<String>();
			int count = SolrUtil.exportByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, (map, index) -> {
				if (map.get("VISITID_CONNECT") != null) {
					String visitidDummy = map.get("VISITID_CONNECT").toString();
					visitidDummys.add(visitidDummy);
					srcMap.put(visitidDummy, map);
				}
			});

			List<JSONObject> dataList = new ArrayList<JSONObject>();
			if (count > 0) {
				int pageSize = 100;
				int pageNum = (visitidDummys.size() + pageSize - 1) / pageSize;
				//数据分割
				List<List<String>> mglist = new ArrayList<>();
				Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
					mglist.add(visitidDummys.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
				});

				// 数据写入文件
				String importFilePath = SolrUtil.importFolder + "/MEDICAL_UNREASONABLE_ACTION/" + batch.getBatchId()
						+ "/" + formalCase.getCaseId() + ".json";
				BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(
						FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
				//写文件头
				fileWriter.write("[");
				for (List<String> list : mglist) {
					SolrQuery solrQuery = new SolrQuery("*:*");
					// 设定查询字段
					solrQuery.addFilterQuery("VISITID_CONNECT:(\"" + StringUtils.join(list, "\",\"") + "\")");
					solrQuery.addFilterQuery("ETL_SOURCE:" + batch.getOutSource());
					solrQuery.setStart(0);
					solrQuery.setRows(EngineUtil.MAX_ROW);
					solrQuery.setSort(SolrQuery.SortClause.asc("VISITID"));
					SolrUtil.export(solrQuery, EngineUtil.DWB_MASTER_INFO, (map, index) -> {
						if (map.get("VISITID_CONNECT") != null) {
							String visitidDummy = map.get("VISITID_CONNECT").toString();
							Map<String, Object> actionMap = srcMap.get(visitidDummy);
							if (actionMap != null) {
								try {
									JSONObject json = constructJson(map, actionMap);
									dataList.add(json);
									fileWriter.write(json.toJSONString());
									fileWriter.write(',');
								} catch (IOException e) {

								}
							}
						}
					});
				}

				// 文件尾
				fileWriter.write("]");
				fileWriter.flush();
				fileWriter.close();
				//导入solr
				SolrUtil.importJsonToSolr(importFilePath, EngineUtil.MEDICAL_UNREASONABLE_ACTION);
				Thread.sleep(3000);
				//删除数据
				String where = "BATCH_ID:" + batch.getBatchId() + " AND ETL_SOURCE:" + batch.getEtlSource()
						+ " AND CASE_ID:" + formalCase.getCaseId();
				SolrUtil.delete(EngineUtil.MEDICAL_UNREASONABLE_ACTION, where);
			}
			log.info("结束转换输出结果");
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private JSONObject constructJson(Map<String, Object> masterMap, Map<String, Object> actionMap) {
    	JSONObject json = new JSONObject();
        //json.put("id", actionMap.get("id"));
    	//String id = MD5Util.MD5Encode(String.valueOf(actionMap.get("BATCH_ID")).concat("_").concat(String.valueOf(actionMap.get("CASE_ID"))).concat("_").concat((String) masterMap.get("VISITID")), "UTF-8");
    	//id生成策略
    	String template = "${batchId}_${caseId}_${visitid}";
        Properties properties = new Properties();
        properties.put("batchId", actionMap.get("BATCH_ID"));
        properties.put("caseId", actionMap.get("CASE_ID"));
        properties.put("visitid", masterMap.get("VISITID"));
        template = PlaceholderResolverUtil.replacePlaceholders(template, properties);
        String id = MD5Util.MD5Encode(template, "UTF-8");
    	json.put("id", id);

    	for(Map.Entry<String, String> entry : EngineCaseServiceImpl.CASE_FIELD_MAPPING.entrySet()) {
            Object val = masterMap.get(entry.getValue());
            if (val != null) {
                json.put(entry.getKey(), val);
            }
        }
        json.put("CASE_ID", actionMap.get("CASE_ID"));
        json.put("CASE_NAME", actionMap.get("CASE_NAME"));
        json.put("ACTION_NAME", actionMap.get("ACTION_NAME"));
        json.put("ACTION_ID", actionMap.get("ACTION_ID"));
        json.put("ACTION_TYPE_ID", actionMap.get("ACTION_TYPE_ID"));
        json.put("ACTION_TYPE_NAME", actionMap.get("ACTION_TYPE_NAME"));
        json.put("ACTION_DESC", actionMap.get("ACTION_DESC"));
        json.put("RULE_BASIS", actionMap.get("RULE_BASIS"));
        json.put("RULE_GRADE", actionMap.get("RULE_GRADE"));
        json.put("RULE_GRADE_REMARK", actionMap.get("RULE_GRADE_REMARK"));
        //json.put("ACTION_MONEY", actionMap.get("ACTION_MONEY"));
        //json.put("MAX_ACTION_MONEY", actionMap.get("ACTION_MONEY"));
        json.put("BUSI_TYPE", MedicalConstant.ENGINE_BUSI_TYPE_CASE);
        json.put("GEN_DATA_TIME", actionMap.get("GEN_DATA_TIME"));
        json.put("PROJECT_ID", actionMap.get("PROJECT_ID"));
        json.put("PROJECT_NAME", actionMap.get("PROJECT_NAME"));
        json.put("BATCH_ID", actionMap.get("BATCH_ID"));
        json.put("TASK_BATCH_NAME", actionMap.get("TASK_BATCH_NAME"));
        json.put("FIR_REVIEW_STATUS", actionMap.get("FIR_REVIEW_STATUS"));
        json.put("CASE_SCORE", actionMap.get("CASE_SCORE"));
        json.put("ACTION_MONEY", masterMap.get("TOTALFEE"));
        json.put("MAX_ACTION_MONEY", masterMap.get("TOTALFEE"));
        return json;
    }
}
