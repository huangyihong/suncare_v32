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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.common.MedicalConstant;
import com.ai.modules.config.entity.MedicalActionDict;
import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.rule.SolrConstField;
import com.ai.modules.engine.handle.rule.SolrFieldAnnotation;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.service.IEngineService;
import com.ai.modules.engine.service.api.IApiDictService;
import com.ai.modules.engine.service.impl.EngineCaseServiceImpl;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.Hive2SolrMain;
import com.ai.solr.HiveJDBCUtil;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 
 * 功能描述：模型规则计算引擎使用hive方式计算抽象类
 *
 * @author  zhangly
 * Date: 2020年11月12日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public abstract class AbsHiveCaseHandle extends JdbcCaseHandle {

	public AbsHiveCaseHandle(String datasource, TaskProject task, TaskProjectBatch batch, 
			HisMedicalFormalCase formalCase, MedicalSpecialCaseClassify classify) {
		super(datasource, task, batch ,formalCase, classify);
	}
	
	/**
	 * 
	 * 功能描述：计算引擎入口
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月17日 下午2:57:40</p>
	 *
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	@Override
	public EngineResult generateUnreasonableAction() throws Exception {
		if(ignoreRun()) {
        	//忽略运行
        	return EngineResult.ok();
        }
		boolean slave = false;
		//删除solr历史数据
		this.deleteSolrByCase(EngineUtil.MEDICAL_UNREASONABLE_ACTION, batch.getBatchId(), formalCase.getCaseId(), slave);
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IEngineService engineSV = context.getBean(IEngineService.class);
		List<List<EngineNode>> flowList = engineSV.queryHisFormalEngineNode(formalCase.getCaseId(), batch.getBatchId());
		if(flowList!=null && flowList.size()>0) {
			//遍历流程节点条件列表
			for(List<EngineNode> flow : flowList) {
				if (flow == null || flow.size()==0) {
		            throw new EngineBizException("模型未能找到流程节点！");
		        }								
				String path = HiveJDBCUtil.STORAGE_ROOT+"/"+datasource+"/case/"+batch.getBatchId()+"/"+formalCase.getCaseId();
				StringBuilder sb = new StringBuilder();
				sb.append("insert overwrite directory '").append(path).append("'");		
				sb.append(" ").append(this.masterInfoJoinSql(flow));		
				HiveJDBCUtil.execute(sb.toString());
						
				Hive2SolrMain main = new Hive2SolrMain();
				String collection = SolrUtil.getSolrUrl(datasource)+"/MEDICAL_UNREASONABLE_ACTION/update";
				main.execute(path, collection, true);
			}
		}		
		Thread.sleep(5000L);
		return this.computeMoney();
	}
	/**
	 * 
	 * 功能描述：查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月13日 上午11:12:33</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected abstract List<String> parseWhere(List<EngineNode> flow) throws Exception;
	
	/**
	 * 
	 * 功能描述：
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
	protected String masterInfoJoinSql(List<EngineNode> flow) throws Exception {
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
				
		Map<String, String> params = new HashMap<String, String>();
		StringBuilder sql = new StringBuilder(engineSql);
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
		
		//模型流程节点
		if(flow!=null && flow.size()>0) {
			//过滤掉开始与结束节点
			flow = flow.stream().filter(s->!EngineUtil.NODE_TYPE_START.equals(s.getNodeType()) && !EngineUtil.NODE_TYPE_END.equals(s.getNodeType())).collect(Collectors.toList());
			for(EngineNode node : flow) {
				String table = this.getEngineNodeTable(node);
				params.put(table, table);
			}
		}
		
		String text = sql.toString();
		StringWriter sw = null;
		//解析引擎sql模板
		try {
			Configuration conf= new Configuration(Configuration.VERSION_2_3_28);
			sw = new StringWriter();
			Template template = new Template(null, text, conf);
			template.process(params, sw);
			sw.flush();
			text = sw.toString();
		} catch(Exception e) {
			
		} finally {
			if(sw!=null) {
				sw.close();
			}
		}		
		
		if(!text.contains("BATCH_ID")) {
			throw new EngineBizException("sql未找到批次号信息");
		}
		if(text.contains("${")) {
			throw new EngineBizException("sql解析异常，存在未解析参数");
		}
		//重新设置引擎sql
		classify.setEngineSql(text);
		sql.setLength(0);
		sql.append(text);
		
		//追加流程节点的查询条件		
		List<String> wheres = parseWhere(flow);
		if(wheres!=null && wheres.size()>0) {
			for(String where : wheres) {
				where = where.trim();
				if(!where.startsWith("and")) {
					where = "and ".concat(where);
				}
				sql.append(" ").append(where.trim());
			}
		}
		return sql.toString();
	}
}
