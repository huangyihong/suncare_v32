package com.ai.modules.medical.service.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.MedicalDictItem;
import com.ai.modules.config.entity.MedicalDiseaseGroup;
import com.ai.modules.config.service.IMedicalDictItemService;
import com.ai.modules.config.service.IMedicalDiseaseGroupService;
import com.ai.modules.formal.entity.MedicalFormalFlowRule;
import com.ai.modules.formal.mapper.MedicalFormalCaseMapper;
import com.ai.modules.formal.vo.QueryMedicalFormalCaseVO;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.medical.mapper.MedicalRuleConfigMapper;
import com.ai.modules.medical.service.IMedicalRuleConditionSetService;
import com.ai.modules.medical.service.IMedicalRuleConfigService;
import com.ai.modules.medical.vo.MedicalChargeRuleConfigIO;
import com.ai.modules.medical.vo.MedicalDrugRuleConfigIO;
import com.ai.modules.medical.vo.MedicalDruguseRuleConfigIO;
import com.ai.modules.medical.vo.MedicalRuleConfigVO;
import com.ai.modules.medical.vo.MedicalTreatRuleConfigIO;
import com.ai.modules.medical.vo.QueryMedicalRuleConfigVO;
import com.ai.modules.medical.vo.RuleVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 通用规则配置
 * @Author: jeecg-boot
 * @Date: 2020-12-14
 * @Version: V1.0
 */
@Slf4j
@Service
public class MedicalRuleConfigServiceImpl extends ServiceImpl<MedicalRuleConfigMapper, MedicalRuleConfig>
		implements IMedicalRuleConfigService {

	@Autowired
	IMedicalRuleConditionSetService medicalRuleConditionSetService;
	@Autowired
	MedicalFormalCaseMapper caseMapper;
	@Autowired
	IMedicalDiseaseGroupService diagGroupService;
	@Autowired
	private IMedicalDictItemService dictService;

	@Transactional
	@Override
	public void updateById(MedicalRuleConfig medicalRuleConfig, List<MedicalRuleConditionSet> conditionSets) {
		this.updateById(medicalRuleConfig);
		this.medicalRuleConditionSetService
				.remove(new QueryWrapper<MedicalRuleConditionSet>().eq("RULE_ID", medicalRuleConfig.getRuleId()));
		this.medicalRuleConditionSetService.saveBatch(conditionSets);
	}

	@Transactional
	@Override
	public void removeByRuleId(String id) {
		this.removeById(id);
		this.medicalRuleConditionSetService.remove(new QueryWrapper<MedicalRuleConditionSet>().eq("RULE_ID", id));
	}

	@Transactional
	@Override
	public void removeByRuleIds(List<String> ids) {
		this.removeByIds(ids);
		this.medicalRuleConditionSetService.remove(new QueryWrapper<MedicalRuleConditionSet>().in("RULE_ID", ids));
	}

	@Override
	public List<MedicalChargeRuleConfigIO> listChargeIO(QueryWrapper<MedicalRuleConfig> queryWrapper) {
		return this.baseMapper.listChargeIO(queryWrapper);
	}

	@Override
	public List<MedicalTreatRuleConfigIO> listTreatIO(QueryWrapper<MedicalRuleConfig> queryWrapper) {
		return this.baseMapper.listTreatIO(queryWrapper);
	}

	@Override
	public List<MedicalDrugRuleConfigIO> listDrugIO(QueryWrapper<MedicalRuleConfig> queryWrapper) {
		return this.baseMapper.listDrugIO(queryWrapper);
	}

	@Override
	public List<MedicalDruguseRuleConfigIO> listDruguseIO(QueryWrapper<MedicalRuleConfig> queryWrapper) {
		return this.baseMapper.listDruguseIO(queryWrapper);
	}

	@Override
	@Transactional
	public void saveBatch(MedicalRuleConfigVO medicalRuleConfig) {
		String[] codes = medicalRuleConfig.getItemCodes().split(",");
		if (codes.length == 1) {
			this.saveVO(medicalRuleConfig);
			return;
		}
		// 构造准入条件
		List<MedicalRuleConditionSet> accessConditions = medicalRuleConfig.getAccessConditions();
		for (MedicalRuleConditionSet bean : accessConditions) {
			bean.setType("access");
			if (StringUtils.isBlank(bean.getLogic())) {
				bean.setLogic("AND");
			}
			if (StringUtils.isBlank(bean.getCompare())) {
				bean.setCompare("=");
			}
		}
		// 构造判定条件
		List<MedicalRuleConditionSet> judgeConditions = medicalRuleConfig.getJudgeConditions();
		for (MedicalRuleConditionSet bean : judgeConditions) {
			bean.setType("judge");
			if (StringUtils.isBlank(bean.getLogic())) {
				bean.setLogic("AND");
			}
			if (StringUtils.isBlank(bean.getCompare())) {
				bean.setCompare("=");
			}
		}
		List<MedicalRuleConditionSet> conditionSets = new ArrayList<>(accessConditions);
		conditionSets.addAll(judgeConditions);
		// 分割构造
		String[] names = medicalRuleConfig.getItemNames().split(",");
		List<MedicalRuleConfig> ruleConfigList = new ArrayList<>();
		List<MedicalRuleConditionSet> ruleConditionSetList = new ArrayList<>();
		// 批量构造新主体和规则
		for (int i = 0, len = codes.length; i < len; i++) {
			MedicalRuleConfig bean = new MedicalRuleConfig();
			BeanUtils.copyProperties(medicalRuleConfig, bean);
			String ruleId = IdUtils.uuid();
			bean.setRuleId(ruleId);
			bean.setItemCodes(codes[i]);
			bean.setItemNames(names[i]);
			List<MedicalRuleConditionSet> conditionSetList = conditionSets.stream().map(r -> {
				MedicalRuleConditionSet conditionSet = new MedicalRuleConditionSet();
				BeanUtils.copyProperties(r, conditionSet);
				conditionSet.setRuleId(ruleId);
				return conditionSet;
			}).collect(Collectors.toList());
			// 添加进集合
			ruleConfigList.add(bean);
			ruleConditionSetList.addAll(conditionSetList);
		}
		// 批量保存
		this.saveBatch(ruleConfigList);
		this.medicalRuleConditionSetService.saveBatch(ruleConditionSetList);

	}

	@Override
	@Transactional
	public void saveVO(MedicalRuleConfigVO medicalRuleConfig) {
		String ruleId = IdUtils.uuid();
		// 构造准入条件
		List<MedicalRuleConditionSet> accessConditions = medicalRuleConfig.getAccessConditions();
		for (MedicalRuleConditionSet bean : accessConditions) {
			bean.setType("access");
			bean.setRuleId(ruleId);
			if (StringUtils.isBlank(bean.getLogic())) {
				bean.setLogic("AND");
			}
			if (StringUtils.isBlank(bean.getCompare())) {
				bean.setCompare("=");
			}
		}
		// 构造判定条件
		List<MedicalRuleConditionSet> judgeConditions = medicalRuleConfig.getJudgeConditions();
		for (MedicalRuleConditionSet bean : judgeConditions) {
			bean.setType("judge");
			bean.setRuleId(ruleId);
			if (StringUtils.isBlank(bean.getLogic())) {
				bean.setLogic("AND");
			}
			if (StringUtils.isBlank(bean.getCompare())) {
				bean.setCompare("=");
			}
		}
		List<MedicalRuleConditionSet> conditionSets = new ArrayList<>(accessConditions);
		conditionSets.addAll(judgeConditions);
		medicalRuleConfig.setRuleId(ruleId);
		// 保存
		this.save(medicalRuleConfig);
		this.medicalRuleConditionSetService.saveBatch(conditionSets);
	}

	@Override
	public List<QueryMedicalRuleConfigVO> queryMedicalRuleConfig(String ruleType, String ruleLimit) {
		return baseMapper.queryMedicalRuleConfig(ruleType, ruleLimit);
	}

	@Override
	public List<QueryMedicalRuleConfigVO> referDiagRuleConfig() {
		return baseMapper.referDiagRuleConfig();
	}

	@Override
	public void exportReferDiagRule(OutputStream os) throws Exception {
		Map<String, Set<RuleVO>> map = new HashMap<String, Set<RuleVO>>();
		List<QueryMedicalRuleConfigVO> ruleList = this.referDiagRuleConfig();
		//遍历规则
		for (QueryMedicalRuleConfigVO config : ruleList) {
			String value = null;
			for (MedicalRuleConditionSet record : config.getConditionList()) {
				String field = record.getField();
				if ("accessDiseaseGroup".equals(field) || "diseaseGroup".equals(field)) {
					value = record.getExt1();
				} else if ("indication".equals(field) || "unIndication".equals(field)) {
					value = record.getExt2();
				} else if ("xtdrq".equals(field)) {
					value = record.getExt4();
				}
				if (StringUtils.isBlank(value)) {
					continue;
				}
				value = StringUtils.replace(value, ",", "|");
				String[] array = StringUtils.split(value, "|");
				for (String code : array) {
					RuleVO vo = new RuleVO();
					vo.setRuleType(config.getRuleType());
					vo.setRuleId(config.getRuleId());
					vo.setRuleCode(config.getItemCodes());
					vo.setRuleName(config.getItemNames());
					vo.setRuleBasis(config.getRuleBasis());
					vo.setRuleRemark(config.getMessage());
					vo.setRuleLimit(config.getRuleLimit());
					if (!map.containsKey(code)) {
						Set<RuleVO> set = new HashSet<RuleVO>();
						set.add(vo);
						map.put(code, set);
					} else {
						map.get(code).add(vo);
					}
				}
			}
		}
		//遍历模型
		List<QueryMedicalFormalCaseVO> caseList = caseMapper.referMedicalFormalCase();
		for(QueryMedicalFormalCaseVO caseVo : caseList) {
			String value = null;
			for (MedicalFormalFlowRule record : caseVo.getConditionList()) {
				value = record.getCompareValue();
				if (StringUtils.isBlank(value)) {
					continue;
				}
				value = StringUtils.replace(value, ",", "|");
				String[] array = StringUtils.split(value, "|");
				for (String code : array) {
					RuleVO vo = new RuleVO();
					vo.setRuleType("CASE");
					vo.setRuleId(caseVo.getCaseId());
					vo.setRuleCode(caseVo.getCaseCode());
					vo.setRuleName(caseVo.getCaseName());
					vo.setRuleBasis(caseVo.getRuleBasis());
					vo.setRuleRemark(caseVo.getActionDesc());
					if (!map.containsKey(code)) {
						Set<RuleVO> set = new HashSet<RuleVO>();
						set.add(vo);
						map.put(code, set);
					} else {
						map.get(code).add(vo);
					}
				}
			}
		}
		//查找所有疾病组信息
		List<MedicalDiseaseGroup> groupList = diagGroupService.list();
		Map<String, MedicalDiseaseGroup> groupMap = new HashMap<String, MedicalDiseaseGroup>();
		for(MedicalDiseaseGroup group : groupList) {
			groupMap.put(group.getGroupCode(), group);
		}
		//按疾病组编码排序
		Map<String, Set<RuleVO>> sortedMap = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors
				.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));
		SXSSFWorkbook workbook = new SXSSFWorkbook(5000);
		Sheet sheet = workbook.createSheet("涉及疾病组");
		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("疾病组编码");
		cell = row.createCell(1);
		cell.setCellValue("疾病组名称");
		int row_idx = 1;
		for(String key : sortedMap.keySet()) {
			row = sheet.createRow(row_idx++);
			cell = row.createCell(0);
			cell.setCellValue(key);
			cell = row.createCell(1);
			String value = null;
			if(groupMap.containsKey(key)) {
				value = groupMap.get(key).getGroupName();
			}
			cell.setCellValue(value);
		}

		Set<String> ruleSet = new LinkedHashSet<String>();
		ruleSet.add("CASE");	//模型
		ruleSet.add("DRUG");	//药品规则
		ruleSet.add("CHARGE");	//收费合规
		ruleSet.add("TREAT");	//合理诊疗
		ruleSet.add("DRUGUSE");	//合理用药
		for(String ruleType : ruleSet) {
			//规则明细
			Map<String, Set<RuleVO>> dataMap = new LinkedHashMap<String, Set<RuleVO>>();
			for(Map.Entry<String, Set<RuleVO>> entry : sortedMap.entrySet()) {
				Set<RuleVO> set = entry.getValue();
				Set<RuleVO> sublist = set.stream().filter(s->ruleType.equals(s.getRuleType())).collect(Collectors.toSet());
				if(sublist!=null && sublist.size()>0) {
					dataMap.put(entry.getKey(), sublist);
				}
			}
			//写明细sheet
			if(dataMap.size()>0) {
				this.exportDtlSheet(workbook, ruleType, dataMap, groupMap);
			}
		}

		workbook.write(os);
		os.flush();
        workbook.dispose();
	}

	private void exportDtlSheet(SXSSFWorkbook workbook, String type, Map<String, Set<RuleVO>> dataMap, Map<String, MedicalDiseaseGroup> groupMap) {
		dataMap = dataMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors
				.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));
		String title = "";
		String dictKey = "RULE_LIMIT_"+type;
		if("CASE".equals(type)) {
			title = "模型";
		} else if("DRUG".equals(type)) {
			title = "药品合规";
		} else if("CHARGE".equals(type)) {
			title = "收费合规";
		} else if("TREAT".equals(type)) {
			title = "合理诊疗";
		} else if("DRUGUSE".equals(type)) {
			title = "合理用药";
		}
		List<MedicalDictItem> dicts = dictService.list(new QueryWrapper<MedicalDictItem>().inSql("group_id", "select group_id from MEDICAL_DICT where group_code='"+dictKey+"'"));
		Map<String, MedicalDictItem> dictMap = new HashMap<String, MedicalDictItem>();
		if(dicts!=null && dicts.size()>0) {
			for(MedicalDictItem item : dicts) {
				dictMap.put(item.getCode(), item);
			}
		}

		Sheet sheet = workbook.createSheet(title);
		Row row = sheet.createRow(0);
		int col_idx = 0;
		Cell cell = row.createCell(col_idx++);
		cell.setCellValue("疾病组编码");
		cell = row.createCell(col_idx++);
		cell.setCellValue("疾病组名称");
		cell = row.createCell(col_idx++);
		cell.setCellValue("规则编码");
		cell = row.createCell(col_idx++);
		cell.setCellValue("规则名称");
		cell = row.createCell(col_idx++);
		cell.setCellValue("政策依据");
		if(!"CASE".equals(type)) {
			cell = row.createCell(col_idx++);
			cell.setCellValue("提示信息");
			cell = row.createCell(col_idx++);
			cell.setCellValue("规则类别");
		}
		int row_idx = 1;
		for(Map.Entry<String, Set<RuleVO>> entry : dataMap.entrySet()) {
			String groupCode = entry.getKey();
			for(RuleVO vo : entry.getValue()) {
				row = sheet.createRow(row_idx++);
				col_idx = 0;
				cell = row.createCell(col_idx++);
				cell.setCellValue(groupCode);
				cell = row.createCell(col_idx++);
				String value = null;
				if(groupMap.containsKey(groupCode)) {
					value = groupMap.get(groupCode).getGroupName();
				}
				cell.setCellValue(value);
				cell = row.createCell(col_idx++);
				cell.setCellValue(vo.getRuleCode());
				cell = row.createCell(col_idx++);
				cell.setCellValue(vo.getRuleName());
				cell = row.createCell(col_idx++);
				cell.setCellValue(vo.getRuleBasis());
				if(!"CASE".equals(type)) {
					cell = row.createCell(col_idx++);
					cell.setCellValue(vo.getRuleRemark());
					if(dictMap.containsKey(vo.getRuleLimit())) {
						value = dictMap.get(vo.getRuleLimit()).getValue();
						cell = row.createCell(col_idx++);
						cell.setCellValue(value);
					}
				}
			}
		}
	}

	@Override
	@Transactional
	public void updateActionNameByActionId(String actionId, String actionName) {
		QueryWrapper<MedicalRuleConfig> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("ACTION_ID",actionId);
		queryWrapper.ne("ACTION_NAME",actionName);
		this.baseMapper.update(new MedicalRuleConfig().setActionName(actionName),queryWrapper);
	}
}
