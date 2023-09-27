/**
 * OutputDiagHandle.java	  V1.0   2021年11月24日 下午4:50:39
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.secondary.cases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineOutputRule;
import com.ai.modules.engine.model.vo.OutputComputeVO;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.his.entity.HisMedicalFormalCase;

/**
 * 
 * 功能描述：输出满足模型的诊断信息
 *
 * @author zhangly Date: 2021年11月24日 Copyright (c) 2021 AILK
 *
 *         <p>
 *         修改历史：(修改人，修改时间，修改原因/内容)
 *         </p>
 */
public class OutputDiagHandle extends AbsCaseSecondHandle {

	public OutputDiagHandle(HisMedicalFormalCase formalCase, List<EngineNodeRule> ruleList) {
		super(formalCase, ruleList);
	}

	@Override
	public void execute() throws Exception {
		List<List<EngineOutputRule>> outputRuleGrp = this.parseEngineNodeRule();
		if (outputRuleGrp == null || outputRuleGrp.size() == 0) {
			return;
		}
		List<String> conditionList = new ArrayList<String>();
		conditionList.add("BATCH_ID:" + formalCase.getBatchId());
		conditionList.add("CASE_ID:" + formalCase.getCaseId());
		List<String> visitidList = new ArrayList<String>();
		String collection = EngineUtil.MEDICAL_UNREASONABLE_ACTION;
		boolean slave = false;
		Map<String, OutputComputeVO> computeMap = new HashMap<String, OutputComputeVO>();
		int count = SolrUtil.exportDocByPager(conditionList, collection, slave, (doc, index) -> {
			String visitid = doc.get("VISITID").toString();
			visitidList.add(visitid);
			String id = doc.get("id").toString();
			if (!computeMap.containsKey(visitid)) {
				OutputComputeVO vo = new OutputComputeVO(id, visitid);
				computeMap.put(visitid, vo);
			} else {
				OutputComputeVO vo = computeMap.get(visitid);
				vo.addId(id);
			}
		});

		if (count > 0) {
			int pageSize = 500;
			int pageNum = (visitidList.size() + pageSize - 1) / pageSize;
			// 数据分割
			List<List<String>> mglist = new ArrayList<>();
			Stream.iterate(0, n -> n + 1).limit(pageNum).forEach(i -> {
				mglist.add(visitidList.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList()));
			});

			for (List<String> subList : mglist) {
				String visitidFq = "VISITID:(\"" + StringUtils.join(subList, "\",\"") + "\")";
				Set<String> visitidSet = new HashSet<String>();
				for (List<EngineOutputRule> outputRuleList : outputRuleGrp) {
					visitidSet.clear();
					conditionList.clear();
					conditionList.add(visitidFq);
					conditionList.add(this.parseWhere(outputRuleList));
					SolrUtil.exportDocByPager(conditionList, EngineUtil.DWB_DIAG, slave, (map, index) -> {
						String visitId = map.get("VISITID").toString();
						String name = map.get("DISEASENAME").toString();
						OutputComputeVO computeVO = computeMap.get(visitId);
						computeVO.addOutput(name, !visitidSet.contains(visitId));
						visitidSet.add(visitId);
					});
				}
			}
			//更新solr字段
			this.update(computeMap, "PROOF_DIAG");
		}
	}

	@Override
	protected boolean filter(EngineNodeRule rule) {
		// 节点包含诊断条件
		String compareType = rule.getCompareType();
		String tableName = rule.getTableName();
		String colName = rule.getColName();
		if ("=".equals(compareType) || "like".equals(compareType) || "llike".equals(compareType)
				|| "rlike".equals(compareType)) {
			if ("STD_DIAGGROUP".equals(tableName)) {
				return true;
			}
			if ("DWB_DIAG".equals(tableName) && "DISEASECODEGROUP".equals(colName)) {
				return true;
			}
			if ("DWB_DIAG".equals(tableName) && ("DISEASENAME".equals(colName) || "DISEASECODE".equals(colName))) {
				return true;
			}
		}
		return false;
	}

	private String parseWhere(List<EngineOutputRule> outputRuleList) {
		if (outputRuleList != null && outputRuleList.size() > 0) {
			List<String> wheres = new ArrayList<String>();
			for (EngineOutputRule rule : outputRuleList) {
				StringBuilder sb = new StringBuilder();
				String replace = null;
				if ("DWB_DIAG".equals(rule.getTableName()) && "DISEASECODEGROUP".equals(rule.getColName())) {
					replace = "DISEASECODE";
				}
				String where = this.parseWhere(rule, replace);
				if ("STD_DIAGGROUP".equals(rule.getTableName())
						|| ("DWB_DIAG".equals(rule.getTableName()) && "DISEASECODEGROUP".equals(rule.getColName()))) {
					SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("STD_DIAGGROUP", "DISEASECODE",
							"DISEASECODE");
					sb.append("_query_:\"").append(plugin.parse()).append(where).append("\"");
					wheres.add(sb.toString());
				} else {
					wheres.add(where);
				}
			}
			return StringUtils.join(wheres, " OR ");
		}
		return null;
	}
}
