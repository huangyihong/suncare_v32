/**
 * ReportWhereModel.java	  V1.0   2019年4月11日 上午10:35:05
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.report;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ReportParamModel {
	/**查询条件*/
	private List<String> wheres = new ArrayList<String>();
	/**维度1限制条数*/
	private int xLimit;
	/**维度2限制条数*/
	private int yLimit;
	/**group by 参数，最多不超过2个维度 维度1 维度2*/
	private String[] groupBy;
	/**solr统计函数*/
	private String staFunction;
	/**js算术表达式*/
	private String jsCrithExpress;
	
	/**按查询条件或者维度的limit计算出x轴axis*/
	private Set<String> xAxisSet;
	/**默认展示echart的图表类型*/
	private String currentEchart;
	/**页面传递来的查询条件*/
	private List<ReportFormField> whereFields;
	
	/**排序字段*/
	private String sort; 
	
	/**报表类型{D:日,W:周,M:月,Q:季度,Y:年}*/
	private String reportType;
	/**报表细类{tongbi:同比,huanbi:环比}*/
	private String reportSubtype;
	
	/**solr collection路径*/
	private String solrCollection;
	
	/**指标字段*/
	private String dimDict;
	
	public ReportParamModel() {
		
	}
	
	public ReportParamModel(List<ReportFormField> whereFields) {
		this.whereFields = whereFields;		
	}
	
	public void addWhere(String condition) {
		if(StringUtils.isNotBlank(condition)) {
			wheres.add(condition);
		}
	}
	
	public void addWhere(String field, String operator, String value) {
		String condition = "";
		if ("GT".equalsIgnoreCase(operator)) {
			// date>2012
			condition = "($field:[ $val TO * ])";
		} else if ("GET".equalsIgnoreCase(operator)) {
			// date>=2012
			condition = "($field:[ $val TO * ])";
		} else if ("LT".equalsIgnoreCase(operator)) {
			// date<2012
			condition = "($field:[ * TO $val ])";
		} else if ("LET".equalsIgnoreCase(operator)) {
			// date<2012
			condition = "($field:[ * TO $val ])";
		} else if ("NEQ".equalsIgnoreCase(operator)) {
			// date!=2012
			condition = "(-$field:$val)";
		} else if ("LIKE".equalsIgnoreCase(operator)) {
			// date like '%2012%'
			condition = "($field:*$val*)";
		}else if ("NOT".equalsIgnoreCase(operator)) {
			condition = "(NOT $field:$val)";
		} else {
			condition = "($field:$val)";
		}
		condition = StringUtils.replace(condition, "$field", field);
		condition = StringUtils.replace(condition, "$val", value);
		addWhere(condition);
	}
	
	public void clearWhere() {
		wheres.clear();
	}		
	
	/**
	 * 
	 * 功能描述：where条件转换为一行solr查询字符串格式
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年4月11日 上午10:55:45</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public String whereSolrString() {
		String fq[] = new String[this.wheres.size()];
		wheres.toArray(fq);
		return StringUtils.join(fq, " && ");
	}
	
	public String[] whereSolrFq() {
		String fq[] = new String[this.wheres.size()];
		wheres.toArray(fq);
		return fq;
	}
	
	public void addAxis(String axis) {
		if(xAxisSet==null) {
			xAxisSet = new LinkedHashSet<String>();
		}
		xAxisSet.add(axis);
	}
	
	/**
	 * 
	 * 功能描述：是否有周期类型报表
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年8月26日 上午9:17:07</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public boolean isPeriodReport() {
		boolean flag = false;
		if(StringUtils.isNotBlank(reportType)) {
			flag = "D".equals(reportType) || "W".equals(reportType) || "M".equals(reportType) || "Q".equals(reportType) || "Y".equals(reportType);
		}
		return flag;
	}
	/**
	 * 
	 * 功能描述：设置默认查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年5月5日 下午4:08:59</p>
	 *
	 * @param defQueryParam
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public void settingDefaultWhere(String defaultWhere) {
		if (StringUtils.isNotEmpty(defaultWhere)) {
			JSONObject jsonObject = JSON.parseObject(defaultWhere);
			if (jsonObject != null) {
				// 解析默认配置的查询条件
				JSONArray array = JSON.parseArray(jsonObject.get("fq").toString());
				if (array != null) {
					for (Object condition : array) {
						if(condition!=null) {
							this.addWhere(String.valueOf(condition));
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * 功能描述：设置页面传递的查询条件
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年5月5日 下午4:08:59</p>
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public void settingFormWhere() {
		if(whereFields!=null) {
			for(ReportFormField where : whereFields) {
				if(StringUtils.isNotBlank(where.getFieldValue())) {
					if(groupBy.length==2
							&& where.getSolrFieldName().equalsIgnoreCase(groupBy[1])) {
						// 排除第二维度字段作为查询条件，后面再构造查询
						continue;
					} else if(where.getSolrFieldName().equalsIgnoreCase(groupBy[0])) {
						// 第一维度作为页面的查询条件
						String value = where.getFieldValue();
						if(value.contains(",")) {
							StringBuilder condition = new StringBuilder();
							String[] array = value.split(",");
							condition.append("(");
							int i=0;
							for(String val : array) {
								//paramModel.addWhere(where.getSolrFieldName(), where.getOpType(), val);
								if(i>0) {
									condition.append(" OR ");
								}
								condition.append(where.getSolrFieldName()).append(":").append(val);
								i++;
							}
							condition.append(")");
							this.addWhere(condition.toString());
							// 重置第一维度限制条数
							this.setxLimit(array.length);
						} else {
							this.addWhere(where.getSolrFieldName(), where.getOpType(), where.getFieldValue());
						}
					} else {
						this.addWhere(where.getSolrFieldName(), where.getOpType(), where.getFieldValue());
					}
				}
			}
		}
	}

	public int getxLimit() {
		return xLimit;
	}

	public void setxLimit(int xLimit) {
		this.xLimit = xLimit;
	}

	public int getyLimit() {
		return yLimit;
	}

	public void setyLimit(int yLimit) {
		this.yLimit = yLimit;
	}

	public String[] getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String[] groupBy) {
		this.groupBy = groupBy;
	}	

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getCurrentEchart() {
		return currentEchart;
	}

	public void setCurrentEchart(String currentEchart) {
		this.currentEchart = currentEchart;
	}

	public Set<String> getxAxisSet() {
		return xAxisSet;
	}

	public List<ReportFormField> getWhereFields() {
		return whereFields;
	}

	public void setWhereFields(List<ReportFormField> whereFields) {
		this.whereFields = whereFields;
	}

	public String getStaFunction() {
		return staFunction;
	}

	public void setStaFunction(String staFunction) {
		this.staFunction = staFunction;
	}

	public String getJsCrithExpress() {
		return jsCrithExpress;
	}

	public void setJsCrithExpress(String jsCrithExpress) {
		this.jsCrithExpress = jsCrithExpress;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getReportSubtype() {
		return reportSubtype;
	}

	public void setReportSubtype(String reportSubtype) {
		this.reportSubtype = reportSubtype;
	}

	public String getSolrCollection() {
		return solrCollection;
	}

	public void setSolrCollection(String solrCollection) {
		this.solrCollection = solrCollection;
	}

	public String getDimDict() {
		return dimDict;
	}

	public void setDimDict(String dimDict) {
		this.dimDict = dimDict;
	}

	public List<String> getWheres() {
		return wheres;
	}
}
