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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ai.modules.engine.model.report.ReportEchartsEntity;
import com.ai.modules.engine.model.report.ReportEchartsSeriesEntity;
import com.ai.modules.engine.model.report.ReportFacetBucketFieldList;
import com.ai.modules.engine.model.report.ReportParamModel;
import com.ai.modules.engine.model.report.StatisticsReportModel;

/**
 * 
 * 功能描述：二维度报表处理器
 *
 * @author  zhangly
 * Date: 2019年4月11日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class MultiReportHandler extends AbstractReportHandler {
	
	public MultiReportHandler(ReportParamModel paramModel) {
		super(paramModel);
	}

	@Override
	public StatisticsReportModel handle() {
		List<ReportFacetBucketFieldList> dataList = this.multiDimCallSolr();
		if(paramModel.getxAxisSet()==null || paramModel.getxAxisSet().size()==0) {
			String[] xAxis = this.getXAxis(dataList);
			for(String axis : xAxis) {
				paramModel.addAxis(axis);
			}
		}				
		//进行补零处理
		dataList = this.repair(dataList, paramModel.getxAxisSet());		
		logger.info("结果数据："+dataList.toString());
		//遍历数据
		if(dataList!=null && dataList.size()>0) {
			ReportEchartsEntity echartsData = new ReportEchartsEntity();
			String[] legend = new String[dataList.size()];
			String[] xAxis = paramModel.getxAxisSet().toArray(new String[0]);
			List<ReportEchartsSeriesEntity> series = new ArrayList<ReportEchartsSeriesEntity>();			
			int y = 0;
			for(ReportFacetBucketFieldList bucketList : dataList) {	
				legend[y++] = bucketList.getTitle();
				ReportEchartsSeriesEntity seriesEntity = new ReportEchartsSeriesEntity(bucketList.getTitle());
				List<BigDecimal> data = new ArrayList<BigDecimal>();
				for(int x=0; x<xAxis.length; x++) {
					if(bucketList.getBuckets()!=null) {
						BigDecimal value = bucketList.getBuckets().get(x).getValue();
						data.add(value);
					}
				}
				seriesEntity.add(data);
				series.add(seriesEntity);
			}
			if("Q".equals(paramModel.getReportType())) {
				// 季度报表特殊处理xAxis
				Set<String> xAxisSet = new LinkedHashSet<String>();
				for(String axis : xAxis) {
					String year = axis.substring(0, 4);
					String quarter = axis.substring(4);
					axis = year.concat("年").concat(quarter).concat("季度");
					xAxisSet.add(axis);
				}
				xAxis = xAxisSet.toArray(new String[0]);
			}
			echartsData.setLegend(legend);
			echartsData.setxAxis(xAxis);
			echartsData.setSeries(series);
			
			return render(echartsData);
		}
		
		return null;
	}
	
	/**
	 * 
	 * 功能描述：获取x轴指标
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2020年8月25日 下午4:02:42</p>
	 *
	 * @param dataList
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	private String[] getXAxis(List<ReportFacetBucketFieldList> dataList) {
		Set<String> xAxisSet = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2); //升序排列
            }
        });
		for(ReportFacetBucketFieldList bucketList : dataList) {	
			for(int x=0, len=bucketList.getBuckets().size(); x<len; x++) {
				if(bucketList.getBuckets()!=null) {
					String field = bucketList.getBuckets().get(x).getField();
					xAxisSet.add(field);
				}
			}
		}
		return xAxisSet.toArray(new String[0]);
	}
}
