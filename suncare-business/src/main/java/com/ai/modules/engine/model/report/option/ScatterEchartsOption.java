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
import com.github.abel533.echarts.AxisPointer;
import com.github.abel533.echarts.Legend;
import com.github.abel533.echarts.Tooltip;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.AxisType;
import com.github.abel533.echarts.code.PointerType;
import com.github.abel533.echarts.code.Position;
import com.github.abel533.echarts.code.SeriesType;
import com.github.abel533.echarts.json.GsonOption;
import com.github.abel533.echarts.series.Scatter;

/***
 * 
 * 功能描述：散点图
 *
 * @author  zhangly
 * Date: 2019年4月9日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class ScatterEchartsOption extends AbstractEchartsOption {

	public ScatterEchartsOption(ReportEchartsEntity chartsEntity) {
		super(chartsEntity);
	}

	@Override
	public GsonOption option() {
		GsonOption option = new GsonOption();
		option.tooltip(new Tooltip()
				.showDelay(0)
				.position(Position.top)
				.formatter("function (params) {if(params){return params.value[1] + ',' + params.value[0] + ':' + params.value[2];}else{return '';}}")
				.axisPointer(new AxisPointer().type(PointerType.none)));
		Legend legend = new Legend("数量");
		legend.show(false);
		option.legend(legend);

		// x轴
		ValueAxis xValueAxis = new ValueAxis().scale(true);
		xValueAxis.type(AxisType.category);
		xValueAxis.data(chartsEntity.getxAxis());
		option.xAxis(xValueAxis);
		// y轴
		ValueAxis yValueAxis = new ValueAxis().scale(true);
		yValueAxis.type(AxisType.category);
		yValueAxis.data(chartsEntity.getLegend());		
		option.yAxis(yValueAxis);
		
		Scatter scatter = new Scatter();
		scatter.setName("数量");
		scatter.setType(SeriesType.scatter);
		
		BigDecimal min = null;
		BigDecimal max = null;
		List<Object[]> seriasList = new ArrayList<Object[]>();
		for(int y=0; y<chartsEntity.getLegend().length; y++) {
			List<BigDecimal> data = chartsEntity.getSeries().get(y).getData();
			for(int x=0; x<chartsEntity.getxAxis().length; x++) {
				BigDecimal value = data.get(x);
				seriasList.add(new Object[] {chartsEntity.getxAxis()[x], chartsEntity.getLegend()[y], value});
				if (min == null) {
					min = value;
				} else {
					if (null!=value && min.compareTo(value)>0) {
						min = value;
					}
				}
				if (max == null) {
					max = value;
				} else {
					if (null!=value && max.compareTo(value)<0) {
						max = value;
					}
				}
			}
		}
		
		scatter.setSymbolSize("function (value){if(value[2]){return parseInt((value[2]-"+min+")/(("+max+"-"+min+")/25)+3);}else{return 1;}}");
		scatter.data(seriasList.toArray());
		option.series(scatter);
		
		return option;
	}

}
