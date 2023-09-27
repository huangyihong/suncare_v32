/**
 * EngineGreenplumService.java	  V1.0   2022年12月15日 上午9:12:30
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ai.modules.engine.model.EngineConstant;
import com.ai.modules.engine.service.IEngineGreenplumService;
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
@DS("greenplum")
public class EngineGreenplumServiceImpl implements IEngineGreenplumService {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void remove(String batchId, String busiType) {
		//备份批次计算结果
		String sql = "insert into tmp_medical_unreasonable_action select * from medical_unreasonable_action where FIR_REVIEW_STATUS in('white','blank','grey') and busi_type=? and batch_id=?";
		jdbcTemplate.update(sql, busiType, batchId);
		//删除批次计算结果
		sql = "delete from medical_unreasonable_action where busi_type=? and batch_id=?";
		jdbcTemplate.update(sql, busiType, batchId);
	}
	
	@Override
	public void removeCase(String batchId, String caseId) {
		//备份批次计算结果
		String sql = "insert into tmp_medical_unreasonable_action select * from medical_unreasonable_action where FIR_REVIEW_STATUS in('white','blank','grey') and case_id=? and batch_id=?";
		jdbcTemplate.update(sql, caseId, batchId);
		//删除批次计算结果
		sql = "delete from medical_unreasonable_action where case_id=? and batch_id=?";
		jdbcTemplate.update(sql, caseId, batchId);
	}
	
	@Override
	public void removeRule(String batchId, String ruleId) {
		//备份批次计算结果
		String sql = "insert into tmp_medical_unreasonable_action select * from medical_unreasonable_action where FIR_REVIEW_STATUS in('white','blank','grey') and rule_id=? and batch_id=?";
		jdbcTemplate.update(sql, ruleId, batchId);
		//删除批次计算结果
		sql = "delete from medical_unreasonable_action where rule_id=? and batch_id=?";
		jdbcTemplate.update(sql, ruleId, batchId);
	}

	@Override
	public void backFill(String batchId, String busiType) {
		//回填批次计算结果审核状态
		StringBuilder sb = new StringBuilder();
		sb.append("update medical_unreasonable_action x set ");
		for(String field : EngineConstant.BACKFILL_FIELD) {
			sb.append(field).append("=y.").append(field);
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(" from tmp_medical_unreasonable_action y where x.id=y.id and x.busi_type=? and x.batch_id=?");
		jdbcTemplate.update(sb.toString(), busiType, batchId);
		//删除备份
		String sql = "delete from tmp_medical_unreasonable_action where busi_type=? and batch_id=?";
		jdbcTemplate.update(sql, busiType, batchId);
	}
	
	@Override
	public void backFillCase(String batchId, String caseId) {
		//回填批次计算结果审核状态
		StringBuilder sb = new StringBuilder();
		sb.append("update medical_unreasonable_action x set ");
		for(String field : EngineConstant.BACKFILL_FIELD) {
			sb.append(field).append("=y.").append(field);
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(" from tmp_medical_unreasonable_action y where x.case_id=y.case_id and x.id=y.id and x.case_id=? and x.batch_id=?");
		jdbcTemplate.update(sb.toString(), caseId, batchId);
		//删除备份
		String sql = "delete from tmp_medical_unreasonable_action where case_id=? and batch_id=?";
		jdbcTemplate.update(sql, caseId, batchId);
	}
	
	@Override
	public void backFillRule(String batchId, String ruleId) {
		//回填批次计算结果审核状态
		StringBuilder sb = new StringBuilder();
		sb.append("update medical_unreasonable_action x set ");
		for(String field : EngineConstant.BACKFILL_FIELD) {
			sb.append(field).append("=y.").append(field);
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(" from tmp_medical_unreasonable_action y where x.rule_id=y.rule_id and x.id=y.id and x.rule_id=? and x.batch_id=?");
		jdbcTemplate.update(sb.toString(), ruleId, batchId);
		//删除备份
		String sql = "delete from tmp_medical_unreasonable_action where rule_id=? and batch_id=?";
		jdbcTemplate.update(sql, ruleId, batchId);
	}
}
