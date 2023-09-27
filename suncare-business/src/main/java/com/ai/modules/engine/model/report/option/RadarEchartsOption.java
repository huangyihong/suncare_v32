/**
 * BarEchartsOption.java	  V1.0   2019年4月9日 下午3:16:15
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package com.ai.modules.engine.model.report.option;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.ai.modules.engine.model.report.ReportEchartsEntity;
import com.ai.modules.engine.model.report.ReportEchartsSeriesEntity;
import com.alibaba.fastjson.JSONObject;
import com.github.abel533.echarts.Radar;
import com.github.abel533.echarts.Radar.Indicator;
import com.github.abel533.echarts.json.GsonOption;
import com.github.abel533.echarts.series.RadarSeries;

/***
 *
 * 功能描述：雷达图
 *
 * @author  zhangly
 * Date: 2019年4月9日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class RadarEchartsOption extends AbstractEchartsOption {

	public RadarEchartsOption(ReportEchartsEntity chartsEntity) {
		super(chartsEntity);
	}

	@Override
	public GsonOption option() {
		GsonOption option = new GsonOption();
		option.tooltip();
		option.legend(chartsEntity.getLegend());

		Radar radar = new Radar();
		List<Indicator> indicatorList = new ArrayList<Indicator>();
		int index=0;
		for(String axis : chartsEntity.getxAxis()) {
			BigDecimal max = null;
			// 查找最大值
			for(ReportEchartsSeriesEntity seriesEntity : chartsEntity.getSeries()) {
				BigDecimal value = seriesEntity.getData().get(index);
				if(max==null) {
					max = value;
				} else {
					if (value != null && max.compareTo(value)<0) {
						max = value;
					}
				}
			}
			// 查找最大值
			Indicator indicator = new Indicator();
			indicator.name(axis);
			indicator.max(max);
			indicatorList.add(indicator);
			index++;
		}
		radar.indicator(indicatorList);
		option.radar(radar);

		List<Object> seriasList = new ArrayList<Object>();
		for(ReportEchartsSeriesEntity entity : chartsEntity.getSeries()) {
			JSONObject object = new JSONObject();
			object.put("name", entity.getName());
			object.put("value", entity.getData().toArray());
			seriasList.add(object);
		}
		List<RadarSeries> radarSeries = new ArrayList<RadarSeries>();
		RadarSeries radarSerie = new RadarSeries();
		radarSerie.data(seriasList.toArray());
		radarSeries.add(radarSerie);

		RadarSeries[] array = new RadarSeries[radarSeries.size()];
		option.series(radarSeries.toArray(array));
		return option;
	}

}
