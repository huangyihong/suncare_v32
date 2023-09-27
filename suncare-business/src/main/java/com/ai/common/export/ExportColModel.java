/**
 * ExportColModel.java	  V1.0   2016年3月14日 下午2:36:01
 *
 * Copyright (c) 2016 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.common.export;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

public class ExportColModel {
	/**表头标题*/
	private String title;
	/**对应的属性*/
	private String field;
	/**占用行数*/
	private int rowspan = 1;
	/**占用列数*/
	private int colspan = 1;
	/**字典参数解析key*/
	private String dictKey;
	/**是否需要导出，默认需要导出*/
	private Boolean hasExport = true;
	/**格式化*/
	private String format;
	private String jsFormat;
	/**一列多个属性值*/
	private ExportFields fields;

	public ExportColModel() {

	}

	public ExportColModel(String title, String field) {
		this.title = title;
		this.field = field;
	}

	public ExportColModel(String title, String field, String dictKey) {
		this.title = title;
		this.field = field;
		this.dictKey = dictKey;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public int getRowspan() {
		return rowspan;
	}
	public void setRowspan(int rowspan) {
		this.rowspan = rowspan;
	}
	public int getColspan() {
		return colspan;
	}
	public void setColspan(int colspan) {
		this.colspan = colspan;
	}
	public String getDictKey() {
		return dictKey;
	}
	public void setDictKey(String dictKey) {
		this.dictKey = dictKey;
	}

	public Boolean getHasExport() {
		return hasExport;
	}
	public void setHasExport(Boolean hasExport) {
		this.hasExport = hasExport;
	}

	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}

	public String getJsFormat() {
		return jsFormat;
	}
	public void setJsFormat(String jsFormat) {
		this.jsFormat = jsFormat;
	}

	public ExportFields getFields() {
		return fields;
	}
	public void setFields(ExportFields fields) {
		this.fields = fields;
	}

	@Override
	public String toString() {
		return "ExportColModel [title=" + title + ", field=" + field + ", rowspan=" + rowspan + ", colspan=" + colspan
				+ ", dictKey=" + dictKey + ", hasExport=" + hasExport + ", format=" + format + ", fields=" + fields
				+ "]";
	}
	public static List<List<ExportColModel>> createHeaderList(String headerJson) throws Exception {
		List<List<ExportColModel>> result = null;
		JSONArray jsonArray = JSON.parseArray(headerJson);
		if(null!=jsonArray) {
			result = new ArrayList<List<ExportColModel>>();
			for(int i=0; i<jsonArray.size(); i++) {
				List<ExportColModel> list = JSON.parseArray(jsonArray.getString(i), ExportColModel.class);
				if(list!=null) {
					result.add(list);
				}
			}
		}
		return result;
	}

	public static void main(String[] args) throws Exception {
		//String data = "[{\"title\":\"订单编码\",\"field\":\"outOrderId\",\"colspan\":1,\"rowspan\":1},{\"title\":\"订单下单时间\",\"field\":\"orderDate\",\"colspan\":1,\"rowspan\":1},{\"title\":\"支付方式\",\"field\":\"payType\",\"colspan\":1,\"rowspan\":1},{\"title\":\"新老用户\",\"field\":\"isNewUser\",\"colspan\":1,\"rowspan\":1},{\"title\":\"地区\",\"field\":\"areaId\",\"colspan\":1,\"rowspan\":1},{\"title\":\"库管时间\",\"field\":\"storeDate\",\"colspan\":1,\"rowspan\":1},{\"title\":\"回访时间\",\"field\":\"callDate\",\"colspan\":1,\"rowspan\":1},{\"title\":\"开户时间\",\"field\":\"handleDate\",\"colspan\":1,\"rowspan\":1},{\"title\":\"稽核时间\",\"field\":\"checkDate\",\"colspan\":1,\"rowspan\":1},{\"title\":\"发货时间\",\"field\":\"sendDate\",\"colspan\":1,\"rowspan\":1},{\"title\":\"处理人\",\"field\":\"handler\",\"colspan\":1,\"rowspan\":1}]";
		String data = "[[{\"title\":\"订单编码\",\"field\":\"outOrderId\",\"colspan\":1,\"rowspan\":2},{\"title\":\"订单下单时间\",\"field\":\"orderDate\",\"colspan\":1,\"rowspan\":2},{\"title\":\"支付方式\",\"field\":\"payType\",\"colspan\":1,\"rowspan\":2},{\"title\":\"新老用户\",\"field\":\"isNewUser\",\"colspan\":1,\"rowspan\":2},{\"title\":\"地区\",\"field\":\"areaId\",\"colspan\":1,\"rowspan\":2},{\"title\":\"处理过程\",\"colspan\":6,\"rowspan\":1}],[{\"title\":\"库管时间\",\"field\":\"storeDate\",\"colspan\":1,\"rowspan\":1},{\"title\":\"回访时间\",\"field\":\"callDate\",\"colspan\":1,\"rowspan\":1},{\"title\":\"开户时间\",\"field\":\"handleDate\",\"colspan\":1,\"rowspan\":1},{\"title\":\"稽核时间\",\"field\":\"checkDate\",\"colspan\":1,\"rowspan\":1},{\"title\":\"发货时间\",\"field\":\"sendDate\",\"colspan\":1,\"rowspan\":1},{\"title\":\"处理人\",\"field\":\"handler\",\"colspan\":1,\"rowspan\":1}]]";

		List<List<ExportColModel>> result = ExportColModel.createHeaderList(data);
		if(null!=result) {
			System.out.println(result.size());
			for(int i=0; i<result.size(); i++) {
				System.out.println("******************");
				List<ExportColModel> list = result.get(i);
				if(list!=null) {
					for(ExportColModel bean:list) {
						System.out.println(bean);
					}
				}
			}
		}
	}
}
