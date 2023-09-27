/**
 * ExcelExportUtil.java	  V1.0   2022年3月16日 上午10:30:05
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.common.export;

import com.ai.modules.config.service.IMedicalDictService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.List;

public class ExcelExportUtil extends ExcelExport implements IExport {
	
	private static Logger log = LoggerFactory.getLogger(ExcelExportUtil.class);
	
	private static ExcelExportUtil instance = new ExcelExportUtil();
	
	private static IMedicalDictService dictSV = SpringContextUtils.getApplicationContext().getBean(IMedicalDictService.class);
	
	private ExcelExportUtil() {
		
	}
	
	public static ExcelExportUtil getInstance() {
		if(null==instance) {
			instance = new ExcelExportUtil();
		}
		return instance;
	}

	@Override
	public String getDictitem(String dictKey, String itemKey) throws Exception {
		String text = dictSV.queryDictTextByKey(dictKey, itemKey);
		if(StringUtils.isNotBlank(text)) {
			return text;
		}
		return itemKey;
	}

	public void export(HttpServletResponse response, String exportTitle, List<ExportColModel> header, String dataJson) throws Exception {
		SXSSFWorkbook workbook = null;
		OutputStream ouputStream = null;
		try {
			String fileName = exportTitle;
			if(StringUtils.isBlank(fileName)) {
				fileName = "报表_"+DateUtils.formatDate(DateUtils.getDate(), "yyyyMMddHHmmss");
			}
			fileName = new String(fileName.getBytes(), "ISO8859-1");
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");

			workbook = exportExcelSingleHead(exportTitle, header, dataJson);
			ouputStream = response.getOutputStream();
			workbook.write(ouputStream);
			ouputStream.flush();
			ouputStream.close();
		} catch(Exception e) {
			log.error("", e);
		} finally {
			if(null != ouputStream) {
				ouputStream.close();
			}
			if(null != workbook) {
				workbook = null;
			}
		}

	}
}
