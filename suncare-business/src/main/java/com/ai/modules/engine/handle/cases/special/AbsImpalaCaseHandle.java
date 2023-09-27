/**
 * AbsRuleHandle.java	  V1.0   2020年11月4日 下午2:47:04
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.special;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.common.MedicalConstant;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.rule.SolrConstField;
import com.ai.modules.engine.handle.rule.SolrFieldAnnotation;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.service.impl.EngineCaseServiceImpl;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.util.JdbcConstants;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 
 * 功能描述：模型规则计算引擎使用impala方式计算抽象类
 *
 * @author  zhangly
 * Date: 2020年11月12日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public abstract class AbsImpalaCaseHandle extends JdbcCaseHandle {

	public AbsImpalaCaseHandle(String datasource, TaskProject task, TaskProjectBatch batch, 
			HisMedicalFormalCase formalCase, MedicalSpecialCaseClassify classify) {
		super(datasource, task, batch ,formalCase, classify);
	}
	
	/**
	 * 
	 * 功能描述：解析引擎sql模板
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月16日 下午4:11:15</p>
	 *
	 * @param constBean
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected String processEngineSql() throws Exception {
		SolrConstField constBean = new SolrConstField();
		constBean.setCaseId(formalCase.getCaseId());
		constBean.setCaseName(formalCase.getCaseName());
		constBean.setActionTypeId(formalCase.getActionType());
		constBean.setActionTypeName(formalCase.getActionTypeName());
		constBean.setActionId(formalCase.getActionId());
		constBean.setActionName(formalCase.getActionName());
		constBean.setActionDesc(formalCase.getActionDesc());
		constBean.setRuleBasis(formalCase.getRuleBasis());
		constBean.setRuleScopeName(formalCase.getActionTypeName());
		constBean.setRuleGrade(formalCase.getRuleGrade());
		constBean.setRuleGradeRemark(formalCase.getRuleGradeRemark());
		constBean.setBusiType(MedicalConstant.ENGINE_BUSI_TYPE_CASE);
		constBean.setProjectId(task.getProjectId());
		constBean.setProjectName(task.getProjectName());
		constBean.setBatchId(batch.getBatchId());
		constBean.setBatchName(batch.getBatchName());
		constBean.setGenDataTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IApiDictService dictSV = context.getBean(IApiDictService.class);
		MedicalActionDict actionDict = dictSV.queryActionDict(formalCase.getActionId());
		if(actionDict!=null) {
			constBean.setActionName(actionDict.getActionName());
			constBean.setRuleLevel(actionDict.getRuleLevel());
		}
		
		String sql = engineSql;
		sql = sql.replaceAll("<#if .*</#if>", "");
		//关键字转小写
		sql = SQLUtils.format(sql, JdbcConstants.HIVE, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION);
		//再重新压缩成一行
		sql = StringUtils.replace(sql, "\n", " ");
		sql = StringUtils.replace(sql, "\r\n", " ");
		sql = StringUtils.replace(sql, "\t", " ");
		sql = StringUtils.replace(sql, "  ", " ");
		//固定字段替换
		StringBuilder constFieldSb = new StringBuilder();
		StringBuilder constFieldValueSb = new StringBuilder();
		//dwb_master_info字段
		String alais = this.masterTableAlias();
		for(Map.Entry<String, String> entry : EngineCaseServiceImpl.CASE_FIELD_MAPPING.entrySet()) {
			constFieldSb.append(entry.getKey()).append(",");
			if(alais!=null) {
				constFieldValueSb.append(alais).append(".");
			}
			constFieldValueSb.append(entry.getValue()).append(",");
		}
		Map<String, String> params = new HashMap<String, String>();
		//追加常量字段
		Class<?> clazz = SolrConstField.class;
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields) {
			if(field.isAnnotationPresent(SolrFieldAnnotation.class)) {
				 String methodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
				 Method method = clazz.getMethod(methodName);
				 Object value = method.invoke(constBean);
				 if(value!=null) {
					 String name = field.getAnnotation(SolrFieldAnnotation.class).value();
					 constFieldSb.append(name).append(",");
					 constFieldValueSb.append("'").append(value.toString()).append("',");
					 params.put(name, String.valueOf(value));
				 }
			}
		}		
		constFieldSb.deleteCharAt(constFieldSb.length()-1);
		constFieldValueSb.deleteCharAt(constFieldValueSb.length()-1);
		params.put("CONST_FIELD", constFieldSb.toString());
		params.put("CONST_FIELD_VALUE", constFieldValueSb.toString());
		
		StringWriter sw = null;
		//解析引擎sql模板
		try {
			Configuration conf= new Configuration(Configuration.VERSION_2_3_28);
			sw = new StringWriter();
			Template template = new Template(null, sql, conf);
			template.process(params, sw);
			sw.flush();
			sql = sw.toString();
		} catch(Exception e) {
			
		} finally {
			if(sw!=null) {
				sw.close();
			}
		}		
		
		if(!sql.contains("BATCH_ID")) {
			throw new EngineBizException("sql未找到批次号信息");
		}
		if(sql.contains("${")) {
			throw new EngineBizException("sql解析异常，存在未解析参数");
		}
		return sql;
	}
	
	/**
	 * 
	 * 功能描述：解析查询字段与结果表字段映射关系（即查询字段别名）
	 *
	 * @author  zhangly
	 *
	 * @param sql
	 * @return
	 */
	protected Map<String, String> parseUdfFieldMap(String sql) throws Exception {
		String regex = "(default.udf_json_out\\(')(.*?)(')";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(sql);
		String mappingStr = null;
		while(matcher.find()) {
			mappingStr = matcher.group(2);
		}
		mappingStr = StringUtils.replace(mappingStr, " ", "");
		//别名字段
		String[] mappingArray = StringUtils.split(mappingStr, ",");
		
		String find = "md5(concat_ws";
		int start = sql.indexOf(find);
		find = ") from";
		int end = sql.indexOf(find);
		String fieldStr = sql.substring(start, end);
		fieldStr = StringUtils.replace(fieldStr, " ", "");
		//查询字段，逗号分割排除函数内的逗号
		regex = ",(?![^()]*+\\))";		
		String[] fieldArray = fieldStr.split(regex);
		//再重新设置第一列值，impala没有md5函数
		fieldArray[0] = fieldArray[0].substring(4, fieldArray[0].length()-1);
		//key=别名，value=查询字段
		Map<String, String> mapping = new LinkedHashMap<String, String>();
		for(int i=0,len=mappingArray.length; i<len; i++) {
			String key = mappingArray[i];
			String value = fieldArray[i];
			if("id".equalsIgnoreCase(key)) {
				key = "id";
			} else {
				key = key.toUpperCase();
			}
			mapping.put(key, value);
		}
		return mapping;
	}
}
