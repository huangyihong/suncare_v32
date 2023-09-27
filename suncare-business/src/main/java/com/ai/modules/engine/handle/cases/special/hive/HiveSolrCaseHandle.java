/**
 * HiveSolrCaseHandle.java	  V1.0   2020年11月16日 下午2:48:24
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.cases.special.hive;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.engine.exception.EngineBizException;
import com.ai.modules.engine.handle.cases.special.AbsHiveCaseHandle;
import com.ai.modules.engine.model.EngineNode;
import com.ai.modules.engine.model.EngineNodeRule;
import com.ai.modules.engine.model.EngineNodeRuleGrp;
import com.ai.modules.engine.model.EngineResult;
import com.ai.modules.engine.model.EngineTableEntity;
import com.ai.modules.engine.model.EngineTableRelationshipsEntity;
import com.ai.modules.engine.parse.SolrJoinParserPlugin;
import com.ai.modules.engine.parse.node.AbsHiveNodeParser;
import com.ai.modules.engine.parse.node.HiveNodeParser;
import com.ai.modules.engine.parse.node.solr.SolrNodeParser;
import com.ai.modules.engine.service.IEngineService;
import com.ai.modules.engine.service.api.IApiTaskService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.his.entity.HisMedicalFormalCase;
import com.ai.modules.medical.entity.MedicalSpecialCaseClassify;
import com.ai.modules.task.entity.TaskCommonConditionSet;
import com.ai.modules.task.entity.TaskProject;
import com.ai.modules.task.entity.TaskProjectBatch;
import com.ai.solr.Hive2SolrMain;
import com.ai.solr.HiveJDBCUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 功能描述：hive与solr相结合方式计算特殊模型
 *
 * @author  zhangly
 * Date: 2020年11月16日
 * Copyright (c) 2020 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class HiveSolrCaseHandle extends AbsHiveCaseHandle {
	
	protected List<EngineNode> solrEngineNodeList = new ArrayList<EngineNode>();

	public HiveSolrCaseHandle(String datasource, TaskProject task, TaskProjectBatch batch,
			HisMedicalFormalCase formalCase, MedicalSpecialCaseClassify classify) {
		super(datasource, task, batch, formalCase, classify);
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
			this.deleteSolrByCase(EngineUtil.MEDICAL_TRAIL_ACTION, batch.getBatchId(), formalCase.getCaseId(), slave);
			solrEngineNodeList.clear();
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
				if(solrEngineNodeList.size()==0) {			
					String collection = SolrUtil.getSolrUrl(datasource)+"/MEDICAL_UNREASONABLE_ACTION/update";
					main.execute(path, collection, true);
				} else {
					//特殊模型仅有一条流程路线时，直接结果直接保存正式表后再进行二次过滤；否则结果先保存临时表后再进行二次过滤
					boolean formal = flowList.size()==1 ? true : false;
					String collection = formal ? "MEDICAL_UNREASONABLE_ACTION" : "MEDICAL_TRAIL_ACTION";
					collection = SolrUtil.getSolrUrl(datasource) +"/" + collection + "/update";
					main.execute(path, collection, true);			
					//进行solr节点条件过滤
					this.filter(formal);
				}
			}
		}
								
		Thread.sleep(5000L);
		return this.computeMoney();
	}
	
	/**
	 * 
	 * 功能描述：过滤掉不满足节点条件的数据
	 *
	 * @author  zhangly
	 *
	 * @param formal
	 * @throws Exception
	 */
	protected void filter(boolean formal) throws Exception {
		List<String> conditionList = new ArrayList<String>();
		conditionList.add("CASE_ID:"+formalCase.getCaseId());
		conditionList.add("BATCH_ID:"+batch.getBatchId());		
		if(formal) {
			//数据结果在正式表
			//遍历solr节点过滤条件
			Set<String> set = new HashSet<String>();
			for(EngineNode node : solrEngineNodeList) {
				SolrNodeParser parser = new SolrNodeParser(node);
				String condition = parser.handler();
				set.add(condition);
			}
			//取反，查出结果数据进行删除
			String where = "*:* -(" + StringUtils.join(set, " AND ") + ")";
			conditionList.add(where);
			List<String> excludeList = new ArrayList<String>();
			SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_UNREASONABLE_ACTION, false, (doc, index) -> {
				excludeList.add(doc.get("id").toString());
				if(excludeList.size()>=1000) {
					this.deleteSolr(EngineUtil.MEDICAL_UNREASONABLE_ACTION, excludeList);
					excludeList.clear();
				}
			});
			if(excludeList.size()>0) {
				this.deleteSolr(EngineUtil.MEDICAL_UNREASONABLE_ACTION, excludeList);
				excludeList.clear();
			}
		} else {
			//数据结果在临时表
			//遍历solr节点过滤条件
			for(EngineNode node : solrEngineNodeList) {
				SolrNodeParser parser = new SolrNodeParser(node);
				String condition = parser.handler();
				conditionList.add(condition);
			}
			HttpURLConnection urlc = SolrUtil.getSolrHttpURLConnection(EngineUtil.MEDICAL_UNREASONABLE_ACTION, false, true);
			urlc.connect();
			OutputStream out = urlc.getOutputStream();
			//写数据流
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
	    	try {
	    		//写文件头
	            bw.write("[");    		
	            SolrUtil.exportDocByPager(conditionList, EngineUtil.MEDICAL_TRAIL_ACTION, false, (doc, index) -> {
	    		    JSONObject json = new JSONObject();
	    		    for(Entry<String, Object> entry : doc.entrySet()) {
	        			if(!"_version_".equals(entry.getKey())) {
	        				json.put(entry.getKey(), entry.getValue());
	        			}
	        		}
	        		json.put("GEN_DATA_TIME", DateUtils.now());
	                json.put("PROJECT_ID", task.getProjectId());
	                json.put("PROJECT_NAME", task.getProjectName());
	                json.put("BATCH_ID", batch.getBatchId());
	                json.put("TASK_BATCH_NAME", batch.getBatchName());
	    			try {
	    				bw.write(json.toJSONString());
	    				bw.write(',');
			        } catch (IOException e) {
			        }
	    		});

				// 文件尾
	            bw.write("]");
	            bw.flush();            
	    	} catch(Exception e) {
	    		throw e;
	    	} finally {
	    		try {
	    			if(bw!=null) {
	    				bw.close();
	    			}
	    			if (urlc != null) {
	    				urlc.disconnect();
	    			}
	    		} catch(Exception e) {}
	    	}
		}
	}
	
	private void deleteSolr(String collection, List<String> excludeList) {
		String query = "CASE_ID:%s AND BATCH_ID:%s AND id:(%s)";
		query = String.format(query, formalCase.getCaseId(), batch.getBatchId(), StringUtils.join(excludeList, " OR "));
		try {
			SolrUtil.delete(collection, query, false);
		} catch(Exception e) {
			
		}
	}

	@Override
	public List<String> parseWhere(List<EngineNode> flow) throws Exception {
		List<String> wheres = new ArrayList<String>();
		//查询条件
		Map<String, EngineTableEntity> tableAliasMap = this.parseEngineSql();
		EngineTableEntity master = tableAliasMap.get(EngineUtil.DWB_MASTER_INFO);
		if(master!=null) {
			wheres.addAll(this.appendCommonWhere(master.getAlias()));
			//项目过滤条件
			String where = this.filterConditionFromProject(master, flow);
			if(StringUtils.isNotBlank(where)) {
				wheres.add("and " + where);
			}
		}
		//业务数据数据区域限制
		for(Map.Entry<String, EngineTableEntity> entry : tableAliasMap.entrySet()) {
			EngineTableEntity entity = entry.getValue();
			if(!entity.getTable().startsWith("STD_")) {//非字典表
				String where = "and %s.project='%s'";
				where = String.format(where, entity.getAlias(), datasource);
				wheres.add(where);
			}
		}
		//流程节点查询条件
		wheres.addAll(this.parseNodeWhere(flow, tableAliasMap));
		return wheres;
	}

	/**
	 * 
	 * 功能描述：解析流程节点查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年11月18日 下午3:16:39</p>
	 *
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected List<String> parseNodeWhere(List<EngineNode> flow, Map<String, EngineTableEntity> tableAliasMap) throws Exception {
		List<String> wheres = new ArrayList<String>();		
		//过滤掉开始与结束节点
		flow = flow.stream().filter(s->!EngineUtil.NODE_TYPE_START.equals(s.getNodeType()) && !EngineUtil.NODE_TYPE_END.equals(s.getNodeType())).collect(Collectors.toList());		
		//引擎表之间的关联关系
		Map<String, EngineTableRelationshipsEntity> relationshipsMap = this.parseEngineRelationships(tableAliasMap, flow);
		for(EngineNode node : flow) {
			if(EngineUtil.NODE_TYPE_CONDITIONAL.equalsIgnoreCase(node.getNodeType())
					&& "NO".equalsIgnoreCase(node.getCondition())) {
				//否条件节点
				this.addSolrEngineNode(node);
				continue;
			}
			String table = this.getEngineNodeTable(node);
			if(tableAliasMap.containsKey(table)) {
				//节点配置查询条件的表在引擎sql中
				EngineTableEntity entity = tableAliasMap.get(table);
				AbsHiveNodeParser nodeParser = new HiveNodeParser(node, entity.getAlias());
				wheres.add("and "+nodeParser.handler());
			} else {
				//关联表
				EngineTableRelationshipsEntity relationships = relationshipsMap.get(table);
				if(relationships!=null) {
					//主表别名
					EngineTableEntity entity = tableAliasMap.get(relationships.getFrom());
					String where = relationships.getWhere();
					if(StringUtils.isNotBlank(where)) {
						where = StringUtils.replace(where, relationships.getTo(), "x");
						where = StringUtils.replace(where, relationships.getFrom(), entity.getAlias());
					}				
					AbsHiveNodeParser nodeParser = new HiveNodeParser(node, "x");
					StringBuilder sb = new StringBuilder();
					if(relationships.getFromIndex().indexOf(",")>0) {
						//多个关联字段
						sb.append(" and exists(");
						sb.append("select 1 from ").append(relationships.getTo()).append(" x");
						sb.append(" where x.project='").append(datasource).append("'");
						String[] fromIndexArray = StringUtils.split(relationships.getFromIndex(), ",");
						String[] toIndexArray = StringUtils.split(relationships.getToIndex(), ",");
						for(int i=0; i<fromIndexArray.length; i++) {					
							sb.append(" and ");
							sb.append(entity.getAlias()).append(".").append(fromIndexArray[i]);
							sb.append("=");
							sb.append("x.").append(toIndexArray[i]);
						}
						if(StringUtils.isNotBlank(where)) {
							sb.append(" and ").append(where);
						}
						sb.append(" and ").append(nodeParser.handler());
						sb.append(")");
					} else {
						sb.append(" and ").append(entity.getAlias()).append(".").append(relationships.getFromIndex());
						sb.append(" in(select x.").append(relationships.getToIndex());
						sb.append(" from ").append(relationships.getTo()).append(" x");
						sb.append(" where x.project='").append(datasource).append("'");					
						if(StringUtils.isNotBlank(where)) {
							sb.append(" and ").append(where);
						}
						sb.append(" and ").append(nodeParser.handler());
						sb.append(")");
					}
					wheres.add(sb.toString());
				} else {
					this.addSolrEngineNode(node);
				}
			}
		}
		return wheres;
	}
	
	/**
	 * 
	 * 功能描述：项目过滤条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年5月31日 上午9:36:33</p>
	 *
	 * @param master
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected String filterConditionFromProject(EngineTableEntity master, List<EngineNode> nodeList) throws Exception {
		ApplicationContext context = SpringContextUtils.getApplicationContext();
		IApiTaskService taskService = context.getBean(IApiTaskService.class);
		List<TaskCommonConditionSet> list = taskService.queryTaskCommonConditionSet(task.getProjectId());
		if(list==null || list.size()==0) {
			return null;
		}
		Set<String> set = new HashSet<String>();
		StringBuilder sb = new StringBuilder();
		for(TaskCommonConditionSet record : list) {
			sb.setLength(0);
			String condiType = record.getField();
			String value = record.getExt1();
			if("visittype".equals(condiType)) {
				//就诊类型
				String[] array = StringUtils.split(value, ",");
				int index = 0;
				if(array.length>1) {
					sb.append("(");
				}
				for(String val : array) {
					if(index>0) {
						sb.append(" and ");
					}
					sb.append(master.getAlias()).append(".VISITTYPE_ID not like '%").append(val).append("%'");
					index++;
				}
				if(array.length>1) {
					sb.append(")");
				}
			} else if("payway".equals(condiType)) {
				//支付方式
				value = "'" + StringUtils.replace(value, ",", "','") + "'";
				sb.append(master.getAlias()).append(".PAYWAY_ID not in(").append(value).append(")");
				
			} else if("funSettleway".equals(condiType)) {
				//结算方式
				value = "'" + StringUtils.replace(value, ",", "','") + "'";
				sb.append(master.getAlias()).append(".FUN_SETTLEWAY_ID not in(").append(value).append(")");
			} else if("diseaseDiag".equals(condiType)) {
				//疾病诊断，特殊处理使用solr方式过滤
				EngineNode node = new EngineNode();
				node.setNodeType("diam");
				node.setNodeName("疾病诊断");
				node.setCondition("NO");
				List<EngineNodeRuleGrp> wheres = new ArrayList<EngineNodeRuleGrp>();
				EngineNodeRuleGrp grp = new EngineNodeRuleGrp();
				
				List<EngineNodeRule> ruleList = new ArrayList<EngineNodeRule>();
				String[] array = StringUtils.split(value, ",");
				int index = 0;
				for(String val : array) {
					EngineNodeRule nodeRule = new EngineNodeRule();
					if(index>0) {
						nodeRule.setLogic("OR");
					}
					nodeRule.setTableName(EngineUtil.DWB_DIAG);
					nodeRule.setColName("DISEASECODE");
					nodeRule.setCompareType("=");
					nodeRule.setCompareValue(val);
					nodeRule.setOrderNo(index++);
					nodeRule.setGroupNo(0);					
					ruleList.add(nodeRule);
				}
				grp.setRuleList(ruleList);
				wheres.add(grp);
				node.setWheres(wheres);
				this.addSolrEngineNode(node);
			} else if("diseaseMappingFilter".equals(condiType) && "1".equals(value)
					&& EngineUtil.caseExistsDisease(nodeList)) {
				EngineNode node = new EngineNode();
				node.setNodeType("filter");
				node.setNodeName("疾病诊断映射不全过滤");
				SolrJoinParserPlugin plugin = new SolrJoinParserPlugin("DWB_DIAG", "VISITID", "VISITID");
				node.setCondition("*:* -"+plugin.parse()+"-DISEASENAME:?*");
				this.addSolrEngineNode(node);
			}
			if(sb.length()>0) {
				set.add(sb.toString());
			}
		}
		if(set.size()>0) {
			return "(" + StringUtils.join(set, " OR ") + ")";
		}
		return null;
	}
	
	protected void addSolrEngineNode(EngineNode node) {
		this.solrEngineNodeList.add(node);
	}
}
