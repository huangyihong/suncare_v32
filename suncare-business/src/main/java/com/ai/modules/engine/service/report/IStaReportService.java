/**
 * IStaReportService.java	  V1.0   2020年8月21日 上午11:14:59
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.service.report;

import java.util.List;

import com.ai.modules.engine.model.report.ReportFormField;
import com.ai.modules.engine.model.report.StatisticsReportModel;

public interface IStaReportService {

	/**
	 * 
	 * 功能描述：生成图表
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年8月21日 上午11:16:12</p>
	 *
	 * @param reportId
	 * @param whereFields
	 * @return
	 * @throws Exception
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	StatisticsReportModel gerenate(String reportId, List<ReportFormField> whereFields) throws Exception;
}
