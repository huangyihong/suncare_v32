/**
 * MedicalColumnQualityServiceImpl.java	  V1.0   2021年3月15日 上午11:02:35
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.medical.service.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jeecg.common.util.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ExportUtils;
import com.ai.common.utils.ExportXUtils;
import com.ai.common.utils.ReflectHelper;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.config.service.IMedicalActionDictService;
import com.ai.modules.config.service.IMedicalColConfigService;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.engine.handle.rule.parse.AbsRuleParser;
import com.ai.modules.engine.model.vo.TestResultVO;
import com.ai.modules.engine.service.impl.EngineActionServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.medical.entity.MedicalColumnQuality;
import com.ai.modules.medical.entity.MedicalDrugRule;
import com.ai.modules.medical.entity.MedicalRuleConditionColumn;
import com.ai.modules.medical.entity.MedicalRuleRely;
import com.ai.modules.medical.entity.MedicalRuleRelyDtl;
import com.ai.modules.medical.mapper.MedicalColumnQualityMapper;
import com.ai.modules.medical.mapper.QueryMedicalColumnMapper;
import com.ai.modules.medical.service.IMedicalColumnQualityService;
import com.ai.modules.medical.service.IMedicalDrugRuleService;
import com.ai.modules.medical.service.IMedicalRuleConditionColumnService;
import com.ai.modules.medical.service.IMedicalRuleRelyDtlService;
import com.ai.modules.medical.service.IMedicalRuleRelyService;
import com.ai.modules.medical.vo.DwbDataqualitySolrVO;
import com.ai.modules.medical.vo.MedicalCaseColumnVO;
import com.ai.modules.medical.vo.MedicalColumnQualityExportVO;
import com.ai.modules.medical.vo.MedicalColumnQualityVO;
import com.ai.modules.medical.vo.MedicalDruguseColumnVO;
import com.ai.modules.medical.vo.MedicalRuleConfigColumnVO;
import com.ai.modules.medical.vo.MedicalRuleRelyDtlVO;
import com.ai.modules.task.entity.TaskBatchBreakRuleLog;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.modules.task.service.ITaskBatchBreakRuleLogService;
import com.ai.modules.task.service.ITaskProjectBatchService;
import com.ai.modules.task.service.ITaskProjectService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MedicalColumnQualityServiceImpl extends ServiceImpl<MedicalColumnQualityMapper, MedicalColumnQuality> implements IMedicalColumnQualityService {

	@Autowired
	private QueryMedicalColumnMapper qryMedicalColumnMapper;
	@Autowired
	private IMedicalRuleConditionColumnService ruleConditionColumnService;
	@Autowired
	private IMedicalDrugRuleService drugRuleService;
	@Autowired
	private IMedicalColConfigService configService;
	@Autowired
	private IMedicalRuleRelyDtlService ruleRelyDtlService;
	@Autowired
	private IMedicalRuleRelyService ruleRelyService;
	@Autowired
	private IMedicalActionDictService actionDictService;
	@Autowired
	private IMedicalDictService dictService;
	@Autowired
	private ITaskProjectService projectService;
	@Autowired
	private ITaskProjectBatchService batchService;
	@Autowired
	private ITaskBatchBreakRuleLogService logService;

	@Override
	public void computeMedicalColumnQualityVO() throws Exception {
		//不合规行为字典映射
		Map<String, MedicalActionDict> actionDictMap = new HashMap<String, MedicalActionDict>();
		List<MedicalActionDict> list = actionDictService.list();
		for(MedicalActionDict dict : list) {
			actionDictMap.put(dict.getActionId(), dict);
		}
		this.computeRuleRelyDtl(actionDictMap);

		//先删除历史数据
		this.remove(new QueryWrapper<MedicalColumnQuality>());
		//数仓字段配置
		List<MedicalColConfig> configList = configService.list(new QueryWrapper<MedicalColConfig>().inSql("TAB_NAME", "SELECT TABLE_NAME FROM MEDICAL_RULE_RELY_DTL"));
		Map<String, MedicalColConfig> configMap = new HashMap<String, MedicalColConfig>();
		for(MedicalColConfig config : configList) {
			String column = config.getTabName().concat(".").concat(config.getColName());
			configMap.put(column, config);
		}
		//统计全部
		List<MedicalRuleRelyDtl> relyList = ruleRelyDtlService.list();
		this.saveMedicalColumnQuality(relyList, actionDictMap, configMap, null, null);

		//按项目统计
		List<TaskProject> projects = projectService.list();
		for(TaskProject project : projects) {
			TaskProjectBatch batch = new TaskProjectBatch();
			batch.setProjectId(project.getProjectId());
			/*relyList = qryMedicalColumnMapper.queryMedicalRuleRelyDtlByProject(batch);
			this.saveMedicalRuleRelyDtl(relyList, actionDictMap, configMap, project, null);*/
			//医保
			batch.setEtlSource("A01");
			relyList = qryMedicalColumnMapper.queryMedicalRuleRelyDtlByProject(batch);
			this.saveMedicalColumnQuality(relyList, actionDictMap, configMap, project, batch);
			//农合
			batch.setEtlSource("A02");
			relyList = qryMedicalColumnMapper.queryMedicalRuleRelyDtlByProject(batch);
			this.saveMedicalColumnQuality(relyList, actionDictMap, configMap, project, batch);
			//his
			batch.setEtlSource("A03");
			relyList = qryMedicalColumnMapper.queryMedicalRuleRelyDtlByProject(batch);
			this.saveMedicalColumnQuality(relyList, actionDictMap, configMap, project, batch);
		}
		//按批次统计
		/*for(TaskProject project : projects) {
			List<TaskProjectBatch> batchList = batchService.list(new QueryWrapper<TaskProjectBatch>().eq("PROJECT_ID", project.getProjectId()));
			for(TaskProjectBatch batch : batchList) {
				relyList = qryMedicalColumnMapper.queryMedicalRuleRelyDtlByBatch(batch);
				this.saveMedicalRuleRelyDtl(relyList, actionDictMap, configMap, project, batch);
			}
		}*/
	}
	
	public void computeRuleRelyDtl(Map<String, MedicalActionDict> actionDictMap) throws Exception {
		List<TaskBatchBreakRuleLog> logList = logService.list(new QueryWrapper<TaskBatchBreakRuleLog>().eq("BATCH_ID", "MEDICAL_RULE_RELY"));
		TaskBatchBreakRuleLog log = null;
		if(logList.size()>0) {
			log = logList.get(0);
			if("running".equals(log.getStatus())) {
				throw new Exception("正在计算中...");
			}
			log.setStatus("running");
			log.setStartTime(new Date());
			logService.update(log, new QueryWrapper<TaskBatchBreakRuleLog>().eq("BATCH_ID", "MEDICAL_RULE_RELY"));
		} else {
			log = new TaskBatchBreakRuleLog();
			log.setLogId(UUIDGenerator.generate());
			log.setBatchId("MEDICAL_RULE_RELY");
			log.setStatus("running");
			log.setCreateTime(new Date());
			log.setStartTime(new Date());
			logService.save(log);
		}
		ruleRelyDtlService.remove(new QueryWrapper<MedicalRuleRelyDtl>());
		ruleRelyService.remove(new QueryWrapper<MedicalRuleRely>());
		//模型依赖查询字段
		this.addRuleRelyDtlFromCase(actionDictMap);
		//新版药品、收费、诊疗规则、用药合规规则
		this.addRuleRelyDtlFromRuleConfig(actionDictMap);
		//老版药品、收费、诊疗规则
		//this.addRuleRelyDtlFromDrug(actionDictMap);
		//用药合规规则
		//this.addRuleRelyDtlFromDruguse(actionDictMap);
		log.setStatus("normal");
		log.setEndTime(new Date());
		logService.update(log, new QueryWrapper<TaskBatchBreakRuleLog>().eq("BATCH_ID", "MEDICAL_RULE_RELY"));
	}

	private void saveMedicalColumnQuality(List<MedicalRuleRelyDtl> relyList, Map<String, MedicalActionDict> actionDictMap,
			Map<String, MedicalColConfig> configMap, TaskProject project, TaskProjectBatch batch) {
		List<MedicalColumnQualityVO> result = new ArrayList<MedicalColumnQualityVO>();
		Map<String, MedicalColumnQualityVO> columnQualityMap = new TreeMap<String, MedicalColumnQualityVO>();
		for(MedicalRuleRelyDtl record : relyList) {
			MedicalColumnQualityVO qualityVO = null;
			String column = record.getTableName().concat(".").concat(record.getColumnName());
			if(!columnQualityMap.containsKey(column)) {
				qualityVO = new MedicalColumnQualityVO(column);
				columnQualityMap.put(column, qualityVO);
			} else {
				qualityVO = columnQualityMap.get(column);
			}
			qualityVO.addActionId(record.getActionId());
			qualityVO.addRule(record.getRuleId());
		}
		Set<String> tableSet = new HashSet<String>();
		for(Map.Entry<String, MedicalColumnQualityVO> entry : columnQualityMap.entrySet()) {
			MedicalColumnQualityVO qualityVO = entry.getValue();
			for(String actionId : qualityVO.getActionSet()) {
				String actionName = actionId;
				if(actionDictMap.containsKey(actionId)) {
		        	MedicalActionDict actionDict = actionDictMap.get(actionId);
		        	actionName = actionDict.getActionName();
		        }
				qualityVO.addActionName(actionName);
			}
			result.add(entry.getValue());
			String[] array = StringUtils.split(qualityVO.getColumn(), ".");
			if(array.length>1) {
				tableSet.add(array[0]);
			}
		}
		String column = null;
		List<MedicalColumnQuality> qualityList = new ArrayList<MedicalColumnQuality>();
		for(MedicalColumnQualityVO vo : result) {
			column = vo.getColumn();
			String[] array = StringUtils.split(column, ".");
			if(array.length>1) {
				MedicalColumnQuality bean = new MedicalColumnQuality();
				bean.setColumnId(UUIDGenerator.generate());
				bean.setTableName(array[0]);
				bean.setColumnName(array[1]);
				String text = dictService.queryDictTextByKey("DATA", array[0]);
				bean.setTableCnname(text);
				bean.setActionNames(StringUtils.join(vo.getActionNameSet(), ","));
				bean.setActionCnt(vo.getActionSet().size());
				bean.setRuleCnt(vo.getRuleSet().size());
				bean.setCreateTime(new Date());
				MedicalColConfig config = configMap.get(column);
				if(config!=null && StringUtils.isNotBlank(config.getColChnName())) {
					bean.setColumnCnname(config.getColChnName());
				} else {
					bean.setColumnCnname(array[1]);
				}
				if(project!=null) {
					bean.setProjectId(project.getProjectId());
					bean.setProjectName(project.getProjectName());
					bean.setDataSource(project.getDataSource());
				}
				if(batch!=null) {
					bean.setBatchId(batch.getBatchId());
					bean.setBatchName(batch.getBatchName());
					bean.setEtlSource(batch.getEtlSource());
				}
				qualityList.add(bean);
			}
		}
		this.saveBatch(qualityList);
	}

	private void addRuleRelyDtlFromCase(Map<String, MedicalActionDict> actionDictMap) {
		Set<MedicalRuleRelyDtlVO> dtlList = new HashSet<MedicalRuleRelyDtlVO>();
		List<MedicalRuleRely> relyList = new ArrayList<MedicalRuleRely>();
		//模型规则
		List<MedicalCaseColumnVO> caseColumnList = this.queryMedicalCaseColumn();
		Map<String, List<MedicalCaseColumnVO>> caseColumnListMap = caseColumnList.stream().collect(Collectors.groupingBy(MedicalCaseColumnVO::getCaseId));		
		for(Map.Entry<String, List<MedicalCaseColumnVO>> entry : caseColumnListMap.entrySet()) {
			Set<String> columnSet = new HashSet<String>();
			if(entry.getValue().size()>0) {
				MedicalCaseColumnVO caseVO = entry.getValue().get(0);
				String actionName = caseVO.getActionName();
				if(actionDictMap.containsKey(caseVO.getActionId())) {
		        	MedicalActionDict actionDict = actionDictMap.get(caseVO.getActionId());
		        	actionName = actionDict.getActionName();
		        }
				for(MedicalCaseColumnVO vo : entry.getValue()) {					
					MedicalRuleRelyDtlVO dtl = new MedicalRuleRelyDtlVO();
					dtl.setRuleId(vo.getCaseId());
					dtl.setTableName(vo.getTableName());
					dtl.setColumnName(vo.getColName());
					dtl.setActionId(vo.getActionId());
					dtl.setActionName(actionName);			
					dtlList.add(dtl);										
					columnSet.add(vo.getTableName().concat(".").concat(vo.getColName()));
				}
				MedicalRuleRely rely = new MedicalRuleRely();
				rely.setId(UUIDGenerator.generate());
				rely.setRuleId(caseVO.getCaseId());
				rely.setRuleName(caseVO.getCaseName());
				rely.setActionId(caseVO.getActionId());
				rely.setActionName(actionName);
				rely.setJudgeColumn(StringUtils.join(columnSet, ","));
				rely.setCreateTime(new Date());
				rely.setRuleType("CASE");
				relyList.add(rely);
			}
		}
		List<MedicalRuleRelyDtl> dataList = new ArrayList<MedicalRuleRelyDtl>();
		for(MedicalRuleRelyDtlVO vo : dtlList) {
			MedicalRuleRelyDtl dtl = new MedicalRuleRelyDtl();
			dtl.setId(UUIDGenerator.generate());
			dtl.setRuleId(vo.getRuleId());
			dtl.setTableName(vo.getTableName());
			dtl.setColumnName(vo.getColumnName());
			dtl.setActionId(vo.getActionId());
			dtl.setActionName(vo.getActionName());
			dtl.setCreateTime(new Date());
			dtl.setFieldType("judge");
			dtl.setRuleType("CASE");
			dataList.add(dtl);
		}
		ruleRelyDtlService.saveBatch(dataList);
		ruleRelyService.saveBatch(relyList);
	}

	private void addRuleRelyDtlFromRuleConfig(Map<String, MedicalActionDict> actionDictMap) {
		//准入条件
		Map<String, List<MedicalRuleConditionColumn>> accessColumnMap = new HashMap<String, List<MedicalRuleConditionColumn>>();
		//判断条件
		Map<String, List<MedicalRuleConditionColumn>> judgeColumnMap = new HashMap<String, List<MedicalRuleConditionColumn>>();
		List<MedicalRuleConditionColumn> columnList = ruleConditionColumnService.list();
		for(MedicalRuleConditionColumn record : columnList) {
			if("access".equals(record.getFieldType())) {
				if(!accessColumnMap.containsKey(record.getRelyType())) {
					List<MedicalRuleConditionColumn> list = new ArrayList<MedicalRuleConditionColumn>();
					list.add(record);
					accessColumnMap.put(record.getRelyType(), list);
				} else {
					accessColumnMap.get(record.getRelyType()).add(record);
				}
			} else {
				if(!judgeColumnMap.containsKey(record.getRelyType())) {
					List<MedicalRuleConditionColumn> list = new ArrayList<MedicalRuleConditionColumn>();
					list.add(record);
					judgeColumnMap.put(record.getRelyType(), list);
				} else {
					judgeColumnMap.get(record.getRelyType()).add(record);
				}
			}
		}
		List<MedicalRuleRely> relyList = new ArrayList<MedicalRuleRely>();
		List<MedicalRuleRelyDtl> dataList = new ArrayList<MedicalRuleRelyDtl>();
		List<MedicalRuleConfigColumnVO> ruleColumnList = this.queryMedicalRuleConfigColumn();
		Map<String, List<MedicalRuleConfigColumnVO>> ruleColumnListMap = ruleColumnList.stream().collect(Collectors.groupingBy(MedicalRuleConfigColumnVO::getRuleId));		
		for(Map.Entry<String, List<MedicalRuleConfigColumnVO>> entry : ruleColumnListMap.entrySet()) {
			//准入条件
			Set<String> accessColumnSet = new HashSet<String>();
			//判断条件
			Set<String> judgeColumnSet = new HashSet<String>();
			String column = null;
			MedicalRuleConfigColumnVO config = entry.getValue().get(0);
			String actionName = config.getActionId();
			String ruleType = config.getRuleType();
			String ruleLimit = config.getRuleLimit();
			Set<String> onedayRuleSet = new HashSet<String>();
			onedayRuleSet.add("freq2");
			onedayRuleSet.add("dayUnfitGroups1");
			onedayRuleSet.add("YRCFSF1");
			if(onedayRuleSet.contains(ruleLimit)) {
				//一日相关规则
				column = "DWS_PATIENT_1VISIT_1DAY_ITEMSUM.ITEMCODE";
				accessColumnSet.add(column);
				column = "DWS_PATIENT_1VISIT_1DAY_ITEMSUM.ITEM_QTY";
				accessColumnSet.add(column);
				column = "DWS_PATIENT_1VISIT_1DAY_ITEMSUM.ITEM_AMT";
				accessColumnSet.add(column);
				column = "DWS_PATIENT_1VISIT_1DAY_ITEMSUM.FUND_COVER";
				accessColumnSet.add(column);
			} else {
				column = "DWS_PATIENT_1VISIT_ITEMSUM.ITEMCODE";
				accessColumnSet.add(column);
				column = "DWS_PATIENT_1VISIT_ITEMSUM.ITEM_QTY";
				accessColumnSet.add(column);
				column = "DWS_PATIENT_1VISIT_ITEMSUM.ITEM_AMT";
				accessColumnSet.add(column);
				column = "DWS_PATIENT_1VISIT_ITEMSUM.FUND_COVER";
				accessColumnSet.add(column);
			}
			
			for(MedicalRuleConfigColumnVO vo : entry.getValue()) {
				String relyType = vo.getField();
				if(accessColumnMap.containsKey(relyType)) {
					//准入条件存在数据库配置
					columnList = accessColumnMap.get(relyType);
					for(MedicalRuleConditionColumn record : columnList) {
						if(StringUtils.isNotBlank(record.getRequiredField())) {
							try {
								String[] fields = StringUtils.split(record.getRequiredField(), "|");
								Object obj = null;
								for(String field : fields) {
									obj = ReflectHelper.getValue(vo, field);
									if(obj!=null) {
										break;
									}
								}	
								if(obj!=null) {
									accessColumnSet.add(record.getRelyColumn());
								}
							} catch (Exception e) {
								
							}
						} else {
							accessColumnSet.add(record.getRelyColumn());
						}
					}
				}
				if(judgeColumnMap.containsKey(relyType)) {
					//判断条件存在数据库配置
					columnList = judgeColumnMap.get(relyType);
					for(MedicalRuleConditionColumn record : columnList) {
						if(StringUtils.isNotBlank(record.getRequiredField())) {
							try {
								String[] fields = StringUtils.split(record.getRequiredField(), "|");
								Object obj = null;
								for(String field : fields) {
									obj = ReflectHelper.getValue(vo, field);
									if(obj!=null) {
										break;
									}
								}	
								if(obj!=null) {
									judgeColumnSet.add(record.getRelyColumn());
								}
							} catch (Exception e) {
								
							}
						} else {
							judgeColumnSet.add(record.getRelyColumn());
						}
					}
				}
				if(AbsRuleParser.RULE_CONDI_AGE.equals(relyType)
						|| AbsRuleParser.RULE_CONDI_ACCESS_AGE.equals(relyType)) {
					//年龄
					String ageUnit = vo.getExt2();
					String field = "YEARAGE";
					if("day".equals(ageUnit) || "日".equals(ageUnit)) {
						field = "DAYAGE";
					} else if("month".equals(ageUnit) || "月".equals(ageUnit)) {
						field = "MONTHAGE";
					}
					column = EngineUtil.DWB_MASTER_INFO.concat(".").concat(field);
					if(AbsRuleParser.RULE_CONDI_ACCESS_AGE.equals(relyType)) {
						accessColumnSet.add(column);
					} else {
						judgeColumnSet.add(column);
					}
				} else if("xtdrq".equals(relyType)) {
					//限特定人群
					if(StringUtils.isNotBlank(vo.getExt1())) {
						//年龄
						String ageUnit = vo.getExt2();
						String field = "YEARAGE";
						if("day".equals(ageUnit) || "日".equals(ageUnit)) {
							field = "DAYAGE";
						} else if("month".equals(ageUnit) || "月".equals(ageUnit)) {
							field = "MONTHAGE";
						}
						column = EngineUtil.DWB_MASTER_INFO.concat(".").concat(field);
						judgeColumnSet.add(column);
					}
				} else if(AbsRuleParser.RULE_CONDI_INDICATION.equals(relyType)
						|| AbsRuleParser.RULE_CONDI_UNINDICATION.equals(relyType)) {
					//适用症
					if(StringUtils.isNotBlank(vo.getExt4())) {
						//检查结果
						List<TestResultVO> list = JSON.parseArray(vo.getExt4(), TestResultVO.class);
						for(TestResultVO bean : list) {
							if("2".equals(bean.getValueType())) {
								//定性
								column = "DWB_TEST_RESULT.TIP";
								judgeColumnSet.add(column);
							} else {
								//定量
								column = "DWB_TEST_RESULT.TESTVALUEUNIT";
								judgeColumnSet.add(column);
								column = "DWB_TEST_RESULT.TEST_VALUE";
								judgeColumnSet.add(column);
							}
						}						
					}
				} else if(AbsRuleParser.RULE_CONDI_FREQUENCY.equals(relyType)) {
					//频次
					String period = vo.getExt1();
					if(StringUtils.isNotBlank(period)) {
						//频次
						if("1time".equals(period)) {
							//一次就诊							
						} else if("avgday".equals(period)) {
							//日均次
							column = "DWB_MASTER_INFO.ZY_DAYS_CALCULATE";
							judgeColumnSet.add(column);
						} else {
							column = "DWB_CHARGE_DETAIL.PRESCRIPTTIME";
							judgeColumnSet.add(column);
						}
					}
				} else if ("fitTimeRange".equals(relyType)) {
					//一日依赖项目组规则
					column = "DWB_CHARGE_DETAIL.PRESCRIPTTIME";
					judgeColumnSet.add(column);
				}
				if("dayUnfitGroups".equals(vo.getRuleLimit())) {
					//一日互斥规则
					column = "DWB_CHARGE_DETAIL.PRESCRIPTTIME";
					judgeColumnSet.add(column);
				}				
			}
			
			if(actionDictMap.containsKey(config.getActionId())) {
	        	MedicalActionDict actionDict = actionDictMap.get(config.getActionId());
	        	actionName = actionDict.getActionName();
	        }
			for(String value : accessColumnSet) {
				String[] array = StringUtils.split(value, ".");
				MedicalRuleRelyDtl dtl = new MedicalRuleRelyDtl();
				dtl.setRuleId(config.getRuleId());
				dtl.setTableName(array[0]);
				dtl.setColumnName(array[1]);
				dtl.setActionId(config.getActionId());
				dtl.setActionName(actionName);
				dtl.setFieldType("access");
				dtl.setRuleType(ruleType);
				dtl.setRuleLimit(ruleLimit);
				dataList.add(dtl);
			}
			for(String value : judgeColumnSet) {
				String[] array = StringUtils.split(value, ".");
				MedicalRuleRelyDtl dtl = new MedicalRuleRelyDtl();
				dtl.setRuleId(config.getRuleId());
				dtl.setTableName(array[0]);
				dtl.setColumnName(array[1]);
				dtl.setActionId(config.getActionId());
				dtl.setActionName(actionName);
				dtl.setFieldType("judge");
				dtl.setRuleType(ruleType);
				dtl.setRuleLimit(ruleLimit);
				dataList.add(dtl);
			}
			MedicalRuleRely rely = new MedicalRuleRely();
			rely.setId(UUIDGenerator.generate());
			rely.setRuleId(config.getRuleId());
			rely.setRuleName(config.getItemNames().concat("(").concat(config.getItemCodes()).concat(")"));
			rely.setActionId(config.getActionId());
			rely.setActionName(actionName);
			rely.setAccessColumn(StringUtils.join(accessColumnSet, ","));
			rely.setJudgeColumn(StringUtils.join(judgeColumnSet, ","));
			rely.setCreateTime(new Date());
			rely.setRuleType(ruleType);
			rely.setRuleLimit(ruleLimit);
			relyList.add(rely);
			
			int limit = 5000;
			if(dataList.size()>=limit) {
				ruleRelyDtlService.saveBatch(dataList);
				dataList.clear();
			}
			if(relyList.size()>=limit) {
				ruleRelyService.saveBatch(relyList);
				relyList.clear();
			}
		}		
		if(dataList.size()>0) {
			ruleRelyDtlService.saveBatch(dataList);
			dataList.clear();
		}
		if(relyList.size()>0) {
			ruleRelyService.saveBatch(relyList);
			relyList.clear();
		}
	}

	private void addRuleRelyDtlFromDrug(Map<String, MedicalActionDict> actionDictMap) {
		Map<String, List<MedicalRuleConditionColumn>> columnMap = new HashMap<String, List<MedicalRuleConditionColumn>>();
		List<MedicalRuleConditionColumn> columnList = ruleConditionColumnService.list();
		for(MedicalRuleConditionColumn record : columnList) {
			if(!columnMap.containsKey(record.getRelyType())) {
				List<MedicalRuleConditionColumn> list = new ArrayList<MedicalRuleConditionColumn>();
				list.add(record);
				columnMap.put(record.getRelyType(), list);
			} else {
				columnMap.get(record.getRelyType()).add(record);
			}
		}
		List<MedicalRuleRelyDtl> dataList = new ArrayList<MedicalRuleRelyDtl>();
		Set<String> columnSet = new HashSet<String>();
		List<MedicalDrugRule> ruleList = drugRuleService.list(new QueryWrapper<MedicalDrugRule>().eq("RULE_TYPE", "1"));
		for(MedicalDrugRule rule : ruleList) {			
			if(StringUtils.isNotBlank(rule.getLimitScope())) {
				String[] scopes = rule.getLimitScope().split(",");
				for(String key : scopes) {
					String actionId = "LIMIT_SCOPE_TO_DRUG_ACTION".concat("-").concat(key);
					if(EngineActionServiceImpl.SCOPE_MAPPING.containsKey(actionId)) {
						actionId = EngineActionServiceImpl.SCOPE_MAPPING.get(actionId);			        	
			        }
					if(!actionDictMap.containsKey(actionId)) {
						continue;
					}
					columnSet.clear();
					String column = "DWB_CHARGE_DETAIL.ITEMCODE";
					columnSet.add(column);
					column = "DWB_CHARGE_DETAIL.AMOUNT";
					columnSet.add(column);
					column = "DWB_CHARGE_DETAIL.FEE";
					columnSet.add(column);
					column = "DWB_CHARGE_DETAIL.FUND_COVER";
					columnSet.add(column);
					if(columnMap.containsKey(key)) {
						//存在数据库配置
						columnList = columnMap.get(key);
						for(MedicalRuleConditionColumn record : columnList) {
							columnSet.add(record.getRelyColumn());
						}
					} else {
						//未配置
						if("01".equals(key)) {
							//年龄
							String ageUnit = rule.getAgeUnit();
							String field = "YEARAGE";
							if("day".equals(ageUnit) || "日".equals(ageUnit)) {
								field = "DAYAGE";
							} else if("month".equals(ageUnit) || "月".equals(ageUnit)) {
								field = "MONTHAGE";
							}
							column = EngineUtil.DWB_MASTER_INFO.concat(".").concat(field);
							columnSet.add(column);
						} else if("40".equals(key)) {
							//检查结果
							column = "DWB_TEST_RESULT.TIP";
							columnSet.add(column);
							column = "DWB_TEST_RESULT.TESTVALUEUNIT";
							columnSet.add(column);
							column = "DWB_TEST_RESULT.TEST_VALUE";
							columnSet.add(column);
						}
					}
					for(String value : columnSet) {						
						String[] array = StringUtils.split(value, ".");
						MedicalRuleRelyDtl dtl = new MedicalRuleRelyDtl();
						dtl.setRuleId(rule.getRuleId());
						dtl.setTableName(array[0]);
						dtl.setColumnName(array[1]);
						dtl.setActionId(actionId);	
						MedicalActionDict actionDict = actionDictMap.get(actionId);
				        dtl.setActionName(actionDict.getActionName());
						dataList.add(dtl);				
					}
					if(dataList.size()>=5000) {
						ruleRelyDtlService.saveBatch(dataList);
						dataList.clear();
					}
				}
			}						
		}
		if(dataList.size()>5000) {
			ruleRelyDtlService.saveBatch(dataList);
			dataList.clear();
		}
	}

	private void addRuleRelyDtlFromDruguse(Map<String, MedicalActionDict> actionDictMap) {
		List<MedicalRuleRelyDtl> dataList = new ArrayList<MedicalRuleRelyDtl>();
		List<MedicalDruguseColumnVO> druguseColumnList = this.queryMedicalDruguseColumn();
		Set<String> columnSet = new HashSet<String>();
		for(MedicalDruguseColumnVO vo : druguseColumnList) {
			columnSet.clear();
			String column = "DWB_CHARGE_DETAIL.ITEMCODE";
			columnSet.add(column);
			column = "DWB_CHARGE_DETAIL.AMOUNT";
			columnSet.add(column);
			column = "DWB_CHARGE_DETAIL.FEE";
			columnSet.add(column);
			column = "DWB_CHARGE_DETAIL.FUND_COVER";
			columnSet.add(column);

			if(StringUtils.isNotBlank(vo.getSex())) {
				//性别
				column = EngineUtil.DWB_MASTER_INFO.concat(".").concat("SEX_CODE");
				columnSet.add(column);
			}
			if(StringUtils.isNotBlank(vo.getAgeUnit())) {
				//年龄
				String ageUnit = vo.getAgeUnit();
				String field = "YEARAGE";
				if("day".equals(ageUnit) || "日".equals(ageUnit)) {
					field = "DAYAGE";
				} else if("month".equals(ageUnit) || "月".equals(ageUnit)) {
					field = "MONTHAGE";
				}
				column = EngineUtil.DWB_MASTER_INFO.concat(".").concat(field);
				columnSet.add(column);
			}
			if(StringUtils.isNotBlank(vo.getExt2())) {
				//疾病组
				column = EngineUtil.DWB_DIAG.concat(".").concat("DISEASECODE");
				columnSet.add(column);
			}
			if(StringUtils.isNotBlank(vo.getExt4())) {
				//检查结果
				column = "DWB_TEST_RESULT.TIP";
				columnSet.add(column);
				column = "DWB_TEST_RESULT.TESTVALUEUNIT";
				columnSet.add(column);
				column = "DWB_TEST_RESULT.TEST_VALUE";
				columnSet.add(column);
			}

			String actionName = vo.getActionId();
			if(actionDictMap.containsKey(vo.getActionId())) {
	        	MedicalActionDict actionDict = actionDictMap.get(vo.getActionId());
	        	actionName = actionDict.getActionName();
	        }
			for(String value : columnSet) {
				String[] array = StringUtils.split(value, ".");
				MedicalRuleRelyDtl dtl = new MedicalRuleRelyDtl();
				dtl.setRuleId(vo.getRuleId());
				dtl.setTableName(array[0]);
				dtl.setColumnName(array[1]);
				dtl.setActionId(vo.getActionId());
				dtl.setActionName(actionName);
				dataList.add(dtl);
			}
			if(dataList.size()>=5000) {
				ruleRelyDtlService.saveBatch(dataList);
				dataList.clear();
			}
		}
		if(dataList.size()>0) {
			ruleRelyDtlService.saveBatch(dataList);
			dataList.clear();
		}
	}

	@Override
	public List<MedicalCaseColumnVO> queryMedicalCaseColumn() {
		return qryMedicalColumnMapper.queryMedicalCaseColumn();
	}

	@Override
	public List<MedicalRuleConfigColumnVO> queryMedicalRuleConfigColumn() {
		return qryMedicalColumnMapper.queryMedicalRuleConfigColumn();
	}

	@Override
	public List<MedicalDruguseColumnVO> queryMedicalDruguseColumn() {
		return qryMedicalColumnMapper.queryMedicalDruguseColumn();
	}

	@Override
	public boolean exportExcel(List<MedicalColumnQualityExportVO> listVO, OutputStream os, String suffix) throws Exception {
		String titleStr = "字段名称,表名称(来源),字段英文名称,涉及不合规行为,涉及不合规行为数量,所涉规则数量"
				+ ",数据完整性(%),是否有质控结果";
		String[] titles = titleStr.split(",");
		String fieldStr =  "columnCnname,tableName,columnName,actionNames,actionCnt,ruleCnt"
				+ ",result,hasResult";//导出的字段
		String[] fields = fieldStr.split(",");

		if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
			SXSSFWorkbook workbook = new SXSSFWorkbook();
			ExportXUtils.exportExl(listVO, MedicalColumnQualityExportVO.class, titles, fields, workbook, "项目数据质量验证信息");
			workbook.write(os);
			workbook.dispose();
		} else {
			// 创建文件输出流
			WritableWorkbook wwb = Workbook.createWorkbook(os);
			WritableSheet sheet = wwb.createSheet("项目数据质量验证信息", 0);
			ExportUtils.exportExl(listVO, MedicalColumnQualityExportVO.class, titles, fields, sheet, "");
			wwb.write();
			wwb.close();
		}
		return false;
	}

	@Override
	public boolean exportExcelSolr(List<DwbDataqualitySolrVO> listVO, OutputStream os, String suffix) throws Exception {
		String titleStr = "机构名称,表中文名称(来源),表英文名称(来源),字段名称,字段英文名称,数据来源,数据完整性(%),涉及不合规行为,涉及不合规行为数量,所涉规则数量"
				+ ",是否有质控结果,发布时间";
		String[] titles = titleStr.split(",");
		String fieldStr =  "orgname,tableCnname,tableName,columnCnname,columnName,etlSourceName,result,actionNames,actionCnt,ruleCnt"
				+ ",hasResult,createdate";//导出的字段
		String[] fields = fieldStr.split(",");

		if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
			SXSSFWorkbook workbook = new SXSSFWorkbook();
			ExportXUtils.exportExl(listVO, DwbDataqualitySolrVO.class, titles, fields, workbook, "项目数据质量验证信息");
			workbook.write(os);
			workbook.dispose();
		} else {
			// 创建文件输出流
			WritableWorkbook wwb = Workbook.createWorkbook(os);
			WritableSheet sheet = wwb.createSheet("项目数据质量验证信息", 0);
			ExportUtils.exportExl(listVO, DwbDataqualitySolrVO.class, titles, fields, sheet, "");
			wwb.write();
			wwb.close();
		}
		return false;
	}
}
