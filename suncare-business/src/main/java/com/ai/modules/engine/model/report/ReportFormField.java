/**
 * ReportFormField.java	  V1.0   2019年4月15日 下午3:07:38
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.report;

/**
 * 
 * 功能描述：页面传递的查询条件对象
 *
 * @author  zhangly
 * Date: 2019年4月15日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class ReportFormField {
	private String fieldName;
	private String fieldValue;
	private String solrFieldName;
	private String opType;
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldValue() {
		return fieldValue;
	}
	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}
	public String getSolrFieldName() {
		return solrFieldName;
	}
	public void setSolrFieldName(String solrFieldName) {
		this.solrFieldName = solrFieldName;
	}
	public String getOpType() {
		return opType;
	}
	public void setOpType(String opType) {
		this.opType = opType;
	}
}
