/**
 * EngineNodeRule.java	  V1.0   2019年11月20日 下午9:46:28
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.model;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.config.entity.MedicalColConfig;
import com.ai.modules.engine.util.EngineUtil;

/**
 *
 * 功能描述：引擎节点规则对象（单个查询条件对象）
 *
 * @author  zhangly
 * Date: 2019年11月20日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class EngineNodeRule {
	//表名
	private String tableName;
	//字段名
	private String colName;
	//比较运算符
	private String compareType;
	//比较值
	private String compareValue;
	//逻辑运算符
	private String logic;
	//序列
	private int orderNo;
	//所属节点
	private String nodeCode;
	//所属组号
	private int groupNo;

	//字段定义
	private MedicalColConfig colConfig;
	
	/**
	 * 
	 * 功能描述：特殊字符处理
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2021年9月1日 上午11:22:04</p>
	 *
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public EngineNodeRule escapeQueryChars() {
		if(StringUtils.isBlank(compareValue)) {
			return this;
		} else if("regx".equals(compareType)) {
			return this;
		} else if("id".equalsIgnoreCase(colName)) {
			return this;
		} else if(colConfig!=null 
				&& "DATE".equalsIgnoreCase(colConfig.getDataType())
				&& compareValue.indexOf(" ")>-1) {
			compareValue = "\""+compareValue+"\"";
			return this;
		}		
		//替换特殊字符
		compareValue = EngineUtil.escapeQueryChars(compareValue);
		return this;
	}

	@Override
	public String toString() {
		return "EngineNodeRule [tableName=" + tableName + ", colName=" + colName + ", compareType=" + compareType
				+ ", compareValue=" + compareValue + ", logic=" + logic + ", orderNo=" + orderNo + ", nodeCode="
				+ nodeCode + ", groupNo=" + groupNo + "]";
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColName() {
		return colName;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	public String getCompareType() {
		return compareType;
	}
	public void setCompareType(String compareType) {
		this.compareType = compareType;
	}
	public String getCompareValue() {
		return compareValue;
	}
	public void setCompareValue(String compareValue) {
		this.compareValue = compareValue;
	}
	public String getLogic() {
		return logic;
	}
	public void setLogic(String logic) {
		this.logic = logic;
	}
	public int getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}
	public String getNodeCode() {
		return nodeCode;
	}
	public void setNodeCode(String nodeCode) {
		this.nodeCode = nodeCode;
	}
	public int getGroupNo() {
		return groupNo;
	}
	public void setGroupNo(int groupNo) {
		this.groupNo = groupNo;
	}
	public MedicalColConfig getColConfig() {
		return colConfig;
	}
	public void setColConfig(MedicalColConfig colConfig) {
		this.colConfig = colConfig;
	}
}
