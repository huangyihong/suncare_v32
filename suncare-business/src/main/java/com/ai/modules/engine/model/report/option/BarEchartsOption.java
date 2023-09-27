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

import java.util.ArrayList;
import java.util.List;

import com.ai.modules.engine.model.report.ReportEchartsEntity;
import com.ai.modules.engine.model.report.ReportEchartsSeriesEntity;
import com.github.abel533.echarts.axis.AxisLabel;
import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.PointerType;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.json.GsonOption;
import com.github.abel533.echarts.series.Bar;

/**
 * 
 * 功能描述：柱状图
 *
 * @author  zhangly
 * Date: 2019年4月9日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class BarEchartsOption extends AbstractEchartsOption {

	public BarEchartsOption(ReportEchartsEntity chartsEntity) {
		super(chartsEntity);
	}

	@Override
	public GsonOption option() {
		GsonOption option = new GsonOption();
		option.tooltip().trigger(Trigger.axis).axisPointer().type(PointerType.shadow);
		option.legend(chartsEntity.getLegend());
		// 轴分类
		CategoryAxis category = new CategoryAxis();
		// 轴数据类别
		category.data(chartsEntity.getxAxis());
		// x轴标题倾斜
		AxisLabel axisLabel = new AxisLabel();
		axisLabel.interval(0);
		axisLabel.rotate(25);
		category.axisLabel(axisLabel);
		// 横轴为类别、纵轴为值
		option.xAxis(category);
		option.yAxis(new ValueAxis());
		
		List<Bar> bars = new ArrayList<Bar>();		 
		for(ReportEchartsSeriesEntity entity : chartsEntity.getSeries()) {
			Bar bar = new Bar(entity.getName());
			bar.data(entity.getData().toArray());
			bars.add(bar);
		}		
		Bar[] array = new Bar[bars.size()];
		option.series(bars.toArray(array));
		
		return option;
	}

}
