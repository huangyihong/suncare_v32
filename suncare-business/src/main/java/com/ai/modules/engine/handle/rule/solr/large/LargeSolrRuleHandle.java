/**
 * BetterSolrRuleHandle.java	  V1.0   2021年12月9日 上午9:27:27
 *
 * Copyright (c) 2021 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.rule.solr.large;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.MD5Util;

import com.ai.common.MedicalConstant;
import com.ai.modules.api.util.ApiOauthUtil;
import com.ai.modules.engine.handle.rule.solr.SolrRuleHandle;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.medical.entity.MedicalRuleConditionSet;
import com.ai.modules.medical.entity.MedicalRuleConfig;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.Hive2SolrMain;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;

public abstract class LargeSolrRuleHandle extends SolrRuleHandle {

	public LargeSolrRuleHandle(TaskProject task, TaskProjectBatch batch, Boolean trail, MedicalRuleConfig rule,
			List<MedicalRuleConditionSet> ruleConditionList) {
		super(task, batch, trail, rule, ruleConditionList);
	}
	
	protected List<String> filterHiveWheres() {
		String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
		String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
		batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
		batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
        String rule_startTime = DateUtil.format(rule.getStartTime(), "yyyy-MM-dd");
        String rule_endTime = DateUtil.format(rule.getEndTime(), "yyyy-MM-dd");
        List<String> wheres = new ArrayList<String>();
        //批次的数据时间范围限制
		String where = "and visitdate>='%s'";
		where = String.format(where, batch_startTime);
		wheres.add(where);
		where = "and visitdate<='%s'";
		where = String.format(where, batch_endTime);
		wheres.add(where);
		//规则的数据时间范围限制
		where = "and visitdate>='%s'";
		where = String.format(where, rule_startTime);
		wheres.add(where);
		where = "and visitdate<='%s'";
		where = String.format(where, rule_endTime);
		wheres.add(where);
		//业务数据数据区域限制
		where = "and project='%s'";
		where = String.format(where, task.getDataSource());
		wheres.add(where);
		//自定义数据范围限制
		if(StringUtils.isNotBlank(batch.getCustomFilter())) {
			String value = batch.getCustomFilter();
			value = "'" + StringUtils.replace(value, ",", "','") + "'";
			where = "and orgid in(%s)";
			where = String.format(where, value);
			wheres.add(where);
		}
		return wheres;
	}

	/**
	 * 
	 * 功能描述：通过solr模式筛选满足条件的DWB_CHARGE_DETAIL数据同步solr
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年12月8日 下午2:33:03</p>
	 *
	 * @param conditionList
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected boolean syncMedicalChargeDetailFromSolr(List<String> conditionList) throws Exception {
		String column = ApiOauthUtil.getProperty("MEDICAL_CHARGE_DETAIL");
		String[] fields = StringUtils.split(column, ",");
        String importFilePath = SolrUtil.importFolder + "/MEDICAL_CHARGE_DETAIL/" + batch.getBatchId() + "/" + rule.getRuleId() + ".json";
        BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(FileUtils.openOutputStream(new File(importFilePath)), Charset.forName("utf8")));
        //写文件头
        fileWriter.write("[");
        int count = SolrUtil.exportDocByPager(conditionList, EngineUtil.DWB_CHARGE_DETAIL, (doc, index) -> {
            // 循环每条数据写入文件
        	JSONObject json = new JSONObject();            
            for (String field : fields) {
                Object val = doc.get(field);
                if (val != null) {
                    json.put(field, val);
                }
            }
            String id = MD5Util.MD5Encode(batch.getBatchId().concat("_").concat(rule.getRuleId()).concat("_").concat((String) doc.get("id")), "utf-8");
            json.put("id", id);
            json.put("BATCH_ID", batch.getBatchId());
            json.put("RULE_ID", rule.getRuleId());
            try {
                fileWriter.write(json.toJSONString());
                fileWriter.write(',');
            } catch (Exception e) {

            }
        });
        // 文件尾
        fileWriter.write("]");
        fileWriter.flush();
        fileWriter.close();

        //导入solr
        SolrUtil.importJsonToSolr(importFilePath, "MEDICAL_CHARGE_DETAIL");
        return count>0;
	}
	
	/**
	 * 
	 * 功能描述：通过hive模式将MEDICAL_CHARGE_DETAIL数据同步solr
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年12月9日 下午3:10:59</p>
	 *
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected void syncSolrFromHive(String table) throws Exception {		
		String column = ApiOauthUtil.getProperty(table);
		String[] fields = StringUtils.split(column, ",");
		String path = HiveJDBCUtil.STORAGE_ROOT+"/"+table+"/"+batch.getBatchId()+"/"+rule.getRuleId();
		StringBuilder sb = new StringBuilder();
		sb.append("insert overwrite directory '").append(path).append("'");		
		sb.append(" ");
		sb.append("select default.udf_json_out('").append(column).append(",batch_id,rule_id',");
		sb.append("'ID',");
		for(String field : fields) {
			if("id".equalsIgnoreCase(field)) {
				field = "md5(concat_ws('_', id, '%s', '%s'))";
				field = String.format(field, batch.getBatchId(), rule.getRuleId());
			}
			sb.append(field).append(",");
		}
		sb.append("'").append(batch.getBatchId()).append("','").append(rule.getRuleId()).append("')");
		sb.append(" from medical_gbdp.").append(table);
		String where = " where batch_id='%s' and rule_id='%s'";
		where = String.format(where, batch.getBatchId(), rule.getRuleId());
		sb.append(where);
		HiveJDBCUtil.execute(sb.toString());
		Hive2SolrMain main = new Hive2SolrMain();
		String collection = SolrUtil.getSolrUrl(task.getDataSource())+"/"+table+"/update";
		main.execute(path, collection, true);
	}
	
	protected void removeMedicalChargeDetail(boolean hive) throws Exception {
		String query = "RULE_ID:%s AND BATCH_ID:%s";
		query = String.format(query, rule.getRuleId(), batch.getBatchId());
		SolrUtil.delete("MEDICAL_CHARGE_DETAIL", query);
		if(hive) {
			query = "alter table medical_gbdp.medical_charge_detail drop partition(batch_id='%s', rule_id='%s')";
			query = String.format(query, batch.getBatchId(), rule.getRuleId());
			HiveJDBCUtil.execute(query);
		}
		removeMedicalPatient1visitItemsum(hive);
	}
	
	private void removeMedicalPatient1visitItemsum(boolean hive) throws Exception {
		String query = "RULE_ID:%s AND BATCH_ID:%s";
		query = String.format(query, rule.getRuleId(), batch.getBatchId());
		SolrUtil.delete("MEDICAL_PATIENT_1VISIT_ITEMSUM", query);
		if(hive) {
			query = "alter table medical_gbdp.medical_patient_1visit_itemsum drop partition(batch_id='%s', rule_id='%s')";
			query = String.format(query, batch.getBatchId(), rule.getRuleId());
			HiveJDBCUtil.execute(query);
		}
	}
}
