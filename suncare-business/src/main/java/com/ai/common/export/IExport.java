/**
 * IExport.java	  V1.0   2022年3月16日 上午10:13:03
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.export;

import java.util.List;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public interface IExport {
	/**
	 * 
	 * 功能描述：导出单表头excel
	 *
	 * @author  zhangly
	 *
	 * @param exportTitle
	 * @param header
	 * @param dataJson
	 * @return
	 * @throws Exception
	 */
	public SXSSFWorkbook exportExcelSingleHead(String exportTitle, List<ExportColModel> header, String dataJson) throws Exception;
	
	/**
	 * 
	 * 功能描述：导出多表头excel
	 *
	 * @author  zhangly
	 *
	 * @param headers
	 * @param dataJson
	 * @return
	 * @throws Exception
	 */
	public SXSSFWorkbook exportExcelMultHead(String exportTitle, List<List<ExportColModel>> headers, String dataJson) throws Exception;
	public SXSSFWorkbook exportExcelMultHead(String exportTitle, String headerJson, String dataJson) throws Exception;
	public SXSSFWorkbook exportExcelMultHead(String headerJson, String dataJson) throws Exception;
	
	/**
	 * 
	 * 功能描述：获取字典翻译值
	 *
	 * @author  zhangly
	 *
	 * @param dictKey
	 * @param itemKey
	 * @return
	 * @throws Exception
	 */
	public String getDictitem(String dictKey, String itemKey) throws Exception;
}
