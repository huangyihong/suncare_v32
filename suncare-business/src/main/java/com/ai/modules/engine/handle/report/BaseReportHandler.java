/**
 * BaseReportHandler.java	  V1.0   2019年4月11日 下午3:09:45
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.handle.report;

import java.util.ArrayList;
import java.util.List;

import com.ai.modules.engine.model.report.ReportEchartsEntity;
import com.ai.modules.engine.model.report.ReportEchartsSeriesEntity;
import com.ai.modules.engine.model.report.ReportFacetBucketField;
import com.ai.modules.engine.model.report.ReportParamModel;
import com.ai.modules.engine.model.report.StatisticsReportModel;

/**
 * 
 * 功能描述：单维度报表处理器
 *
 * @author  zhangly
 * Date: 2019年4月11日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class BaseReportHandler extends AbstractReportHandler {
	
	public BaseReportHandler(ReportParamModel paramModel) {
		super(paramModel);
	}

	@Override
	public StatisticsReportModel handle() {
		List<ReportFacetBucketField> list = this.singleDimCallSolr();
		if(list!=null && list.size()>0) {
			ReportEchartsEntity echartsData = new ReportEchartsEntity();
			String title = paramModel.getGroupBy()[0];
			echartsData.setLegend(new String[] {title});
			// x轴数据
			String[] xAxis = new String[list.size()];
			// y轴数据
			ReportEchartsSeriesEntity seriesEntity = new ReportEchartsSeriesEntity(title);
			int index = 0;
			for(ReportFacetBucketField bucketField : list) {
				xAxis[index++] = bucketField.getField();
				seriesEntity.add(bucketField.getValue());
			}
			List<ReportEchartsSeriesEntity> series = new ArrayList<ReportEchartsSeriesEntity>();
			series.add(seriesEntity);
			echartsData.setxAxis(xAxis);
			echartsData.setSeries(series);
			
			return render(echartsData);
		}
		
		return null;
	}
	
}
