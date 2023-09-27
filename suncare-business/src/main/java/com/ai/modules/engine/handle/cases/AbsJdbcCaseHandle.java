/**
 * AbsJdbcCaseHandle.java	  V1.0   2022年12月6日 下午3:57:13
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.MD5Util;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.common.MedicalConstant;
import com.ai.modules.engine.handle.rule.hive.model.JoinTableModel;
import com.ai.modules.engine.model.ColumnType;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;

public abstract class AbsJdbcCaseHandle extends AbsCaseHandle {

	public AbsJdbcCaseHandle(String datasource, TaskProject task, TaskProjectBatch batch,
			HisMedicalFormalCase formalCase) {
		super(datasource, task, batch, formalCase);
	}
	
	/**
	 * 
	 * 功能描述：公共查询条件（包含：数据时间范围、项目地、数据源、医疗机构等）
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2022年1月12日 上午9:33:09</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected List<String> appendCommonWhere(String alias) throws Exception {
		List<String> wheres = new ArrayList<String>();
		String project_startTime = MedicalConstant.DEFAULT_START_TIME;
		String project_endTime = MedicalConstant.DEFAULT_END_TIME;
		String batch_startTime = MedicalConstant.DEFAULT_START_TIME;
		String batch_endTime = MedicalConstant.DEFAULT_END_TIME;
		String case_startTime = MedicalConstant.DEFAULT_START_TIME;
        String case_endTime = MedicalConstant.DEFAULT_END_TIME;
        project_startTime = task.getDataStartTime()!=null ? DateUtil.format(task.getDataStartTime(), "yyyy-MM-dd") : project_startTime;
        project_endTime = task.getDataEndTime()!=null ? DateUtil.format(task.getDataEndTime(), "yyyy-MM-dd") : project_endTime;
		batch_startTime = batch.getStartTime()!=null ? DateUtil.format(batch.getStartTime(), "yyyy-MM-dd") : batch_startTime;
		batch_endTime = batch.getEndTime()!=null ? DateUtil.format(batch.getEndTime(), "yyyy-MM-dd") : batch_endTime;
		case_startTime = formalCase.getStartTime()!=null ? DateUtils.formatDate(formalCase.getStartTime(), "yyyy-MM-dd") : case_startTime;
        case_endTime = formalCase.getEndTime()!=null ? DateUtils.formatDate(formalCase.getEndTime(), "yyyy-MM-dd") : case_endTime;
        //业务数据时间范围限制
		String where = "%s.visitdate>='%s'";
		where = String.format(where, alias, project_startTime);
		wheres.add(where);
		where = "%s.visitdate<='%s 23:59:59'";
		where = String.format(where, alias, project_endTime);
		wheres.add(where);
		where = "%s.visitdate>='%s'";
		where = String.format(where, alias, batch_startTime);
		wheres.add(where);
		where = "%s.visitdate<='%s 23:59:59'";
		where = String.format(where, alias, batch_endTime);
		wheres.add(where);
		//模型的数据时间范围限制
		where = "%s.visitdate>='%s'";
		where = String.format(where, alias, case_startTime);
		wheres.add(where);
		where = "%s.visitdate<='%s 23:59:59'";
		where = String.format(where, alias, case_endTime);
		wheres.add(where);
		//业务数据项目地限制
		where = "%s.project='%s'";
		where = String.format(where, alias, datasource);
		wheres.add(where);
		//项目的数据来源限制
		if (StringUtils.isNotBlank(task.getEtlSource())) {
			where = "%s.etl_source='%s'";
			where = String.format(where, alias, task.getEtlSource());
			wheres.add(where);
		}
		//批次的数据来源限制
		if (StringUtils.isNotBlank(batch.getEtlSource())) {
			where = "%s.etl_source='%s'";
			where = String.format(where, alias, batch.getEtlSource());
			wheres.add(where);
		}
		
		StringBuilder sql = new StringBuilder();
		//项目的数据来源限制
		if (StringUtils.isNotBlank(task.getEtlSource())) {
			sql.append("$alias.etl_source='").append(task.getEtlSource()).append("'");
		}
		//批次的数据来源限制
		if (StringUtils.isNotBlank(batch.getEtlSource())) {
			sql.append("$alias.etl_source='").append(batch.getEtlSource()).append("'");
		}
		//项目的医疗机构范围限制
		if(StringUtils.isNotBlank(task.getDataOrgFilter())) {
			String value = task.getDataOrgFilter();
			value = "'" + StringUtils.replace(value, ",", "','") + "'";
			where = "%s.orgid in(%s)";
			where = String.format(where, alias, value);
			wheres.add(where);
		}
		//批次的医疗机构范围限制
		if(StringUtils.isNotBlank(batch.getCustomFilter())) {
			String value = batch.getCustomFilter();
			value = "'" + StringUtils.replace(value, ",", "','") + "'";
			where = "%s.orgid in(%s)";
			where = String.format(where, alias, value);
			wheres.add(where);
		}
		//过滤掉病例基金支出金额为0的数据
		if("1".equals(batch.getYbFundRm0())) {
			where = "%s.fundpay>=0";
			where = String.format(where, alias);
			wheres.add(where);
		}
		return wheres;
	}
	
	/**
	 * 
	 * 功能描述：解析每一行ResultSet
	 *
	 * @author  zhangly
	 *
	 * @param rs
	 * @param columnSet
	 * @return
	 * @throws Exception
	 */
	protected JSONObject parseJSONObject(ResultSet rs, Set<ColumnType> columnSet) throws Exception {
		JSONObject jsonObject = new JSONObject();
		for(ColumnType bean : columnSet) {
			String column = bean.getColumnName(); 
    		Object value = rs.getObject(column);
    		if(value!=null && !"".equals(value.toString())) {
    			if("id".equalsIgnoreCase(column)) {
    				jsonObject.put("id", value);
    			} else {
    				jsonObject.put(column.toUpperCase(), value);
    			}
    		}
    	}
    	//id生成策略
	    String template = jsonObject.getString("id");
        String id = MD5Util.MD5Encode(template, "UTF-8");
        jsonObject.put("id", id);
        jsonObject.put("GEN_DATA_TIME", DateUtils.now());
        jsonObject.put("PROJECT_ID", task.getProjectId());
        jsonObject.put("PROJECT_NAME", task.getProjectName());
        jsonObject.put("BATCH_ID", batch.getBatchId());
        jsonObject.put("TASK_BATCH_NAME", batch.getBatchName());
        return jsonObject;
	}
	
	/**
	 * 
	 * 功能描述：项目关联表过滤条件
	 *
	 * @author  zhangly
	 *
	 * @return
	 * @throws Exception
	 */
	protected JoinTableModel joinTables(List<EngineNode> nodeList) throws Exception {
		JoinTableModel result = new JoinTableModel();
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IApiTaskService taskService = context.getBean(IApiTaskService.class);
		List<TaskCommonConditionSet> list = taskService.queryTaskCommonConditionSet(task.getProjectId());
		if(list!=null && list.size()>0) {
			for(TaskCommonConditionSet record : list) {
				String condiType = record.getField();
				String value = record.getExt1();
				if("visittype".equals(condiType)) {
					//就诊类型
					String[] array = StringUtils.split(value, ",");
					for(String val : array) {
						String where = "VISITTYPE_ID not like '%s%'";
						where = String.format(where, val);
						result.addMasterWhere(where);
					}
				} else if("payway".equals(condiType)) {
					//支付方式
					value = "'" + StringUtils.replace(value, ",", "','") + "'";
					String where = "PAYWAY_ID not in(%s)";
					where = String.format(where, value);
					result.addMasterWhere(where);
				} else if("funSettleway".equals(condiType)) {
					//结算方式
					value = "'" + StringUtils.replace(value, ",", "','") + "'";
					String where = "FUN_SETTLEWAY_ID not in(%s)";
					where = String.format(where, value);
					result.addMasterWhere(where);
				} else if("diseaseDiag".equals(condiType)) {
					//疾病诊断
					value = "'" + StringUtils.replace(value, ",", "','") + "'";
					StringBuilder sb = new StringBuilder();
					sb.append("select * from dwb_master_info x1");
					sb.append(" where not exists(select 1 from DWB_DIAG x2 where x1.visitid=x2.visitid and DISEASECODE in("+value+"))");
					result.addWithTable(sb.toString());
				} else if("diseaseMappingFilter".equals(condiType) && "1".equals(value)
						&& EngineUtil.caseExistsDisease(nodeList)) {
					//疾病诊断映射不全过滤
					StringBuilder sb = new StringBuilder();
					sb.append("select * from dwb_master_info x1");
					sb.append(" where not exists(select 1 from DWB_DIAG x2 where x1.visitid=x2.visitid and diseasename is null)");
					result.addWithTable(sb.toString());
				}
			}
		}
		return result;
	}
}
