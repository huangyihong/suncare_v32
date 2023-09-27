/**
 * ExcelExport.java	  V1.0   2022年3月16日 上午10:13:19
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.common.export;

import com.ai.modules.config.service.IMedicalDictService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.script.Invocable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelExport {
	private static IMedicalDictService dictSV;

	@Autowired
	public void setHrssDictService(IMedicalDictService dictSV) {
		ExcelExport.dictSV = dictSV;
	}

	/**
	 *
	 * 功能描述：导出excel，仅支持单行表头
	 *
	 * @author  zhangly
	 *
	 * @param exportTitle
	 * @param header
	 * @param dataJson
	 * @return
	 * @throws Exception
	 */
	public SXSSFWorkbook exportExcelSingleHead(String exportTitle, List<ExportColModel> header, String dataJson) throws Exception {
		List<List<ExportColModel>> headers = new ArrayList<List<ExportColModel>>();
		headers.add(header);
		return exportExcelMultHead(exportTitle, headers, dataJson);
	}

	/**
	 *
	 * 功能描述：导出excel，支持多行表头
	 *
	 * @author  zhangly
	 *
	 * @param exportTitle
	 * @param headers
	 * @param dataJson
	 * @return
	 * @throws Exception
	 */
	public SXSSFWorkbook exportExcelMultHead(String exportTitle, List<List<ExportColModel>> headers, String dataJson) throws Exception {
		SXSSFWorkbook workbook = null;
		try {
			workbook = new SXSSFWorkbook(5000);
			JSONArray dataArray = JSONArray.parseArray(dataJson);
			if(StringUtils.isBlank(exportTitle)) {
				exportTitle = "报表";
			}
			Sheet sheet = workbook.createSheet(exportTitle);
			//报表表头行数
			int header_row_len = headers.size();
			//报表数据行数
			int body_row_len = dataArray.size();
			//报表列数
			int col_len = 0;
			if(null != headers){
				//计算报表列数
				List<ExportColModel> firstHead = headers.get(0);
				for(ExportColModel model : firstHead){
					col_len += (short)model.getColspan();
				}
			}
			for(int i=0; i<col_len; i++){
				sheet.setColumnWidth(i, 3766);
			}
			CellStyle headerCellStyle = this.createHeaderCellStyle(workbook);
			//列对应的所取数据的属性名;key是y坐标值如：([0,"userName"];[1,"password"])
			Map<Integer, ExportColModel> headMap = new HashMap<Integer, ExportColModel>();
			//表头填充excel单元格
			for(int header_row_idx=0; header_row_idx<header_row_len; header_row_idx++){
				//创建表头行
				Row row = sheet.createRow(header_row_idx);
				//设置高度
				row.setHeight((short)450);
				List<ExportColModel> headerTR = headers.get(header_row_idx);
				int col_idx=0;
				for(int i=0; i<headerTR.size(); i++){
					ExportColModel bean = headerTR.get(i);
					//System.out.println(bean);
					col_idx = this.hasMerged(sheet, header_row_idx, col_idx, col_len);
					int coordinateY = col_idx;	//y坐标值
					String field = bean.getField();
					if(StringUtils.isNotBlank(field) || null!=bean.getFields()) {
						headMap.put(coordinateY, bean);
					}
					//创建列，col_idx是列的位置索引(从0开始)
					Cell cell = row.createCell(col_idx);
					cell.setCellStyle(headerCellStyle);
					cell.setCellValue(bean.getTitle());
					if(bean.getColspan()>1 || bean.getRowspan()>1){
						/**
						* 合并单元格
						*    第一个参数：第一个单元格的行数（从0开始）
						*    第二个参数：第二个单元格的行数（从0开始）
						*    第三个参数：第一个单元格的列数（从0开始）
						*    第四个参数：第二个单元格的列数（从0开始）
						*/
						CellRangeAddress range = new CellRangeAddress(header_row_idx, header_row_idx+bean.getRowspan()-1, col_idx, col_idx+bean.getColspan()-1);
						sheet.addMergedRegion(range);
					}
					col_idx = (short)(col_idx + bean.getColspan());
				}
			}
			//数据填充excel单元格
			CellStyle cellStyle = this.createBodyCellStyle(workbook);
			for(int body_row_idx=0; body_row_idx<body_row_len; body_row_idx++) {
				//行的索引
				int row_idx = header_row_len+body_row_idx;
				//创建数据行
				Row row = sheet.createRow(row_idx);
				JSONObject data = (JSONObject)dataArray.get(body_row_idx);
				short col_idx=0;
				for(int i=0; i<col_len; i++) {
					Cell cell = row.createCell(col_idx++);
					ExportColModel colModel = headMap.get(i);
					String value = this.gettingCellValue(colModel, data,i);
					cell.setCellValue(value);
					cell.setCellStyle(cellStyle);
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
		}

		return workbook;
	}

	private String gettingCellValue(ExportColModel colModel, JSONObject data,int i) throws Exception {
		if(null==colModel) {
			return null;
		}
		ExportFields fieldLst = colModel.getFields();

		//一列是否包含多个属性值
		if(fieldLst!=null
				&& fieldLst.getAttrs()!=null
				&& fieldLst.getAttrs().size()>0) {
			StringBuffer sb = new StringBuffer();
			String split = fieldLst.getSplit();
			for(ExportField attr : fieldLst.getAttrs()) {
				//列属性
				String field = attr.getAttr();
				if(StringUtils.isNotBlank(field)) {
					//列的值
					String value = null;
					if(field.split(".").length==2) {
						data = data.getJSONObject(field.split(".")[0]);
						if(data==null) {
							continue;
						}
					}
					value = data.getString(field);
					if(StringUtils.isNotBlank(attr.getDictKey())) {
						value = this.getDictitem(attr.getDictKey(), value);
					}
					if(value!=null && "null".equals(value)) {
						value = null;
					}
					if(StringUtils.isBlank(sb.toString())) {
						if(StringUtils.isNotBlank(value)) {
							sb.append(value);
						}
					} else {
						if(StringUtils.isNotBlank(value)) {
							sb.append(split).append(value);
						}
					}
				}
			}
			return sb.toString();
		} else {
			//列属性
			String field = colModel.getField();
			if(StringUtils.isNotBlank(field)) {
				//列的值
				String value = null;
				if(field.split(".").length==2) {
					data = data.getJSONObject(field.split(".")[0]);
					if(data==null) {
						return null;
					}
				}
				value = data.getString(field);
				if(StringUtils.isBlank(colModel.getDictKey())) {
					if(StringUtils.isNotBlank(value) && !"null".equals(value)) {
						//是否格式化
						String format = colModel.getFormat();
						if(StringUtils.isNotBlank(format)) {
							Long time = Long.parseLong(value);
							Timestamp t = new Timestamp(time);
							value = DateUtils.formatDate(t, format);
						}
					}
				} else {
					value = this.getDictitem(colModel.getDictKey(), value);
				}
				//执行js脚本
				String jsFormat = colModel.getJsFormat();
				if(StringUtils.isNotBlank(jsFormat)){
					if(jsFormat.contains("=>")){
						jsFormat = "function formatFun"+jsFormat.replace("=>","");
					}
					String[] options = new String[] {"--language=es6"};
					NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
					NashornScriptEngine engine = (NashornScriptEngine) factory.getScriptEngine(options);
					//执行js脚本定义函数
					try {
						engine.eval(jsFormat);

						Invocable invocable = (Invocable) engine;

						value = (String) invocable.invokeFunction("formatFun",new Object[]{value,data,i});
					}catch (Exception e){
						//System.out.println(e.getMessage());
					}
				}


				if(value!=null && "null".equals(value)) {
					value = null;
				}
				return value;
			}
		}
		return null;
	}

	/***
	 *
	 * 功能描述：获取可填充的单元格
	 * 遇到合并单元格的区域往后移动一列直到找到可填充的单元格列
	 *
	 * @author  zhangly
	 *
	 * @param sheet
	 * @param row
	 * @param column 要填充的列
	 * @param column_len 表格列数
	 * @return 可填充的单元格列
	 */
	private int hasMerged(Sheet sheet, int row, int column, int column_len){
		while(column<column_len){
			int cur_val = column;
			for (int k = 0; k < sheet.getNumMergedRegions(); k++) {
				CellRangeAddress address = sheet.getMergedRegion(k);
	            if((address.getFirstRow()<=row && row<=address.getLastRow())
	            		&& (address.getFirstColumn()<=column && column<=address.getLastColumn())){
	            	//所填充的单元格在合并单元格区域内，填充的单元格需往后移动一列
	            	column += 1;
	            }
			}
			if(cur_val==column){
				//当前填充的列不在任何合并单元格区域内终止循环
				break;
			}
		}

		return column;
	}

	/**
	 *
	 * 功能描述：表头列样式
	 *
	 * @author  zhangly
	 *
	 * @param workbook
	 * @return
	 */
	private CellStyle createHeaderCellStyle(SXSSFWorkbook workbook){
		CellStyle headstyle = workbook.createCellStyle();
		// 设置字体
		Font headfont = workbook.createFont();
		// 字体大小
		headfont.setFontHeightInPoints((short) 10);
		// 加粗
		headfont.setBold(true);
		headstyle.setFont(headfont);
		// 左右居中
		headstyle.setAlignment(HorizontalAlignment.CENTER);
		// 上下居中
		headstyle.setVerticalAlignment(VerticalAlignment.CENTER);
		//headstyle.setWrapText(true);// 自动换行

		headstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headstyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		// 设置单元格的边框为粗体
		headstyle.setBorderRight(BorderStyle.THIN);
		headstyle.setBorderLeft(BorderStyle.THIN);
		headstyle.setBorderTop(BorderStyle.THIN);
		headstyle.setBorderBottom(BorderStyle.THIN);
		// 设置单元格的边框颜色
		headstyle.setRightBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		headstyle.setLeftBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		headstyle.setTopBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		headstyle.setBottomBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());

		return headstyle;
	}

	/**
	 *
	 * 功能描述：数据列样式
	 *
	 * @author  zhangly
	 *
	 * @param workbook
	 * @return
	 */
	private CellStyle createBodyCellStyle(SXSSFWorkbook workbook){
		CellStyle bodystyle = workbook.createCellStyle();
		// 上下居中
		bodystyle.setVerticalAlignment(VerticalAlignment.CENTER);
		// 自动换行
		//bodystyle.setWrapText(true);
		// 设置单元格的边框为粗体
		bodystyle.setBorderRight(BorderStyle.THIN);
		bodystyle.setBorderLeft(BorderStyle.THIN);
		bodystyle.setBorderTop(BorderStyle.THIN);
		bodystyle.setBorderBottom(BorderStyle.THIN);
		// 设置单元格的边框颜色
		bodystyle.setRightBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		bodystyle.setLeftBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		bodystyle.setTopBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		bodystyle.setBottomBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
		return bodystyle;
	}

	public SXSSFWorkbook exportExcelMultHead(String exportTitle, String headerJson, String dataJson) throws Exception {
		return exportExcelMultHead(exportTitle, ExportColModel.createHeaderList(headerJson), dataJson);
	}

	public SXSSFWorkbook exportExcelMultHead(String headerJson, String dataJson) throws Exception {
		return exportExcelMultHead(null, ExportColModel.createHeaderList(headerJson), dataJson);
	}

	public String getDictitem(String dictKey, String itemKey) throws Exception {
		return translateDictValue(dictKey,itemKey);
	}

	/**
	 *
	 * 功能描述：字段翻译
	 * @author zhangly
	 * @date 2023-03-16 10:29:13
	 *
	 * @param code
	 * @param key
	 *
	 * @return java.lang.String
	 *
	 */
	private static String translateDictValue(String code, String key) {
		if(oConvertUtils.isEmpty(key)) {
			return "";
		}
		StringBuilder textValue = new StringBuilder();
		String[] keys_group = key.split("\\|");
		for(int i=0;i<keys_group.length;i++) {
			String[] keys = keys_group[i].split(",");
			for (int j=0;j<keys.length;j++) {
				String k = keys[j];
				String tmpValue = null;
				if (k.trim().length() == 0) {
					continue; //跳过循环
				}
				tmpValue = dictSV.queryDictTextByKey(code, k.trim());

				if (tmpValue != null) {
					textValue.append(tmpValue);
					if(j!=keys.length-1&&!"".equals(textValue.toString())) {
						textValue.append(",");
					}
				}

			}
			if(i!=keys_group.length-1&&!"".equals(textValue.toString())) {
				textValue.append("|");
			}
		}
		return textValue.toString();
	}

	public static void main(String[] args) throws Exception {
		Long time = Long.parseLong("1458144000000");
		Timestamp t = new Timestamp(time);
		System.out.println(t);
		System.out.println(DateUtils.formatDate(t, "yyyy-MM-dd HH:mm:ss"));
		System.out.println(DateUtils.formatDate(t, "yyyy-MM-dd"));
	}
}
