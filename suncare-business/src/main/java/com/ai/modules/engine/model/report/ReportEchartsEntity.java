/**
 * EchartsEntity.java	  V1.0   2019年4月9日 下午2:21:25
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.report;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ai.modules.engine.model.EchartsSeriesEntity;
import com.ai.modules.engine.model.report.option.AbstractEchartsOption;
import com.ai.modules.engine.model.report.option.BarEchartsOption;
import com.ai.modules.engine.model.report.option.LineEchartsOption;
import com.ai.modules.engine.model.report.option.PileBarEchartsOption;
import com.ai.modules.engine.model.report.option.RadarEchartsOption;
import com.ai.modules.engine.model.report.option.ScatterEchartsOption;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.abel533.echarts.json.GsonOption;

/**
 * 
 * 功能描述：存储渲染echarts图表的数据对象
 *
 * @author  zhangly
 * Date: 2019年4月12日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class ReportEchartsEntity implements Serializable {
	private String[] legend;
	// x轴数据
	private String[] xAxis;
	// y轴数据
	private List<ReportEchartsSeriesEntity> series;

	@Override
	public String toString() {
		return "ReportEchartsEntity [legend=" + Arrays.toString(legend) + ", xAxis=" + Arrays.toString(xAxis)
				+ ", series=" + series + "]";
	}

	public String[] getLegend() {
		return legend;
	}

	public void setLegend(String[] legend) {
		this.legend = legend;
	}

	public String[] getxAxis() {
		return xAxis;
	}

	public void setxAxis(String[] xAxis) {
		this.xAxis = xAxis;
	}

	public List<ReportEchartsSeriesEntity> getSeries() {
		return series;
	}

	public void setSeries(List<ReportEchartsSeriesEntity> series) {
		this.series = series;
	}
	
	public static void main(String[] args) throws Exception {
		ReportEchartsEntity chartsEntity = new ReportEchartsEntity();
		chartsEntity.setLegend(new String[] {"上海市", "北京市", "四川省", "安徽省", "山东省", "广东省", "江苏省", "河南省", "浙江省", "福建省"});
		chartsEntity.setxAxis(new String[] {"2007","2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2017"});
		
		List<ReportEchartsSeriesEntity> series = new ArrayList<ReportEchartsSeriesEntity>();
		ReportEchartsSeriesEntity seriesEntity = new ReportEchartsSeriesEntity();
		seriesEntity.setName("上海市");
		List<BigDecimal> data = new ArrayList<BigDecimal>();
		data.add(new BigDecimal(36836));
		data.add(new BigDecimal(41526));
		data.add(new BigDecimal(54414));
		data.add(new BigDecimal(59138));
		data.add(new BigDecimal(71873));
		data.add(new BigDecimal(79219));
		data.add(new BigDecimal(82143));
		data.add(new BigDecimal(83327));
		data.add(new BigDecimal(93725));
		data.add(new BigDecimal(114379));
		seriesEntity.add(data);
		series.add(seriesEntity);
		
		seriesEntity = new ReportEchartsSeriesEntity();
		seriesEntity.setName("北京市");
		data = new ArrayList<BigDecimal>();
		data.add(new BigDecimal(32645));
		data.add(new BigDecimal(45097));
		data.add(new BigDecimal(52062));
		data.add(new BigDecimal(60043));
		data.add(new BigDecimal(75365));
		data.add(new BigDecimal(91333));
		data.add(new BigDecimal(102133));
		data.add(new BigDecimal(115694));
		data.add(new BigDecimal(131463));
		data.add(new BigDecimal(141034));
		seriesEntity.add(data);
		series.add(seriesEntity);
		
		seriesEntity = new ReportEchartsSeriesEntity();
		seriesEntity.setName("四川省");
		data = new ArrayList<BigDecimal>();
		data.add(new BigDecimal(16269));
		data.add(new BigDecimal(21260));
		data.add(new BigDecimal(29634));
		data.add(new BigDecimal(32045));
		data.add(new BigDecimal(42548));
		data.add(new BigDecimal(60222));
		data.add(new BigDecimal(66605));
		data.add(new BigDecimal(79433));
		data.add(new BigDecimal(89716));
		data.add(new BigDecimal(130576));
		seriesEntity.add(data);
		series.add(seriesEntity);
		
		seriesEntity = new ReportEchartsSeriesEntity();
		seriesEntity.setName("安徽省");
		data = new ArrayList<BigDecimal>();
		data.add(new BigDecimal(5812));
		data.add(new BigDecimal(10119));
		data.add(new BigDecimal(15048));
		data.add(new BigDecimal(31668));
		data.add(new BigDecimal(41234));
		data.add(new BigDecimal(68528));
		data.add(new BigDecimal(81352));
		data.add(new BigDecimal(96908));
		data.add(new BigDecimal(116902));
		data.add(new BigDecimal(156433));
		seriesEntity.add(data);
		series.add(seriesEntity);
		
		seriesEntity = new ReportEchartsSeriesEntity();
		seriesEntity.setName("山东省");
		data = new ArrayList<BigDecimal>();
		data.add(new BigDecimal(32265));
		data.add(new BigDecimal(40884));
		data.add(new BigDecimal(49711));
		data.add(new BigDecimal(62986));
		data.add(new BigDecimal(84027));
		data.add(new BigDecimal(106427));
		data.add(new BigDecimal(124952));
		data.add(new BigDecimal(135576));
		data.add(new BigDecimal(164314));
		data.add(new BigDecimal(165493));
		seriesEntity.add(data);
		series.add(seriesEntity);
		
		seriesEntity = new ReportEchartsSeriesEntity();
		seriesEntity.setName("广东省");
		data = new ArrayList<BigDecimal>();
		data.add(new BigDecimal(87114));
		data.add(new BigDecimal(96000));
		data.add(new BigDecimal(119264));
		data.add(new BigDecimal(139265));
		data.add(new BigDecimal(172052));
		data.add(new BigDecimal(214679));
		data.add(new BigDecimal(228998));
		data.add(new BigDecimal(249749));
		data.add(new BigDecimal(298010));
		data.add(new BigDecimal(553069));
		seriesEntity.add(data);
		series.add(seriesEntity);
		
		seriesEntity = new ReportEchartsSeriesEntity();
		seriesEntity.setName("江苏省");
		data = new ArrayList<BigDecimal>();
		data.add(new BigDecimal(59967));
		data.add(new BigDecimal(91348));
		data.add(new BigDecimal(124292));
		data.add(new BigDecimal(172865));
		data.add(new BigDecimal(272053));
		data.add(new BigDecimal(381858));
		data.add(new BigDecimal(341719));
		data.add(new BigDecimal(337321));
		data.add(new BigDecimal(342544));
		data.add(new BigDecimal(424543));
		seriesEntity.add(data);
		series.add(seriesEntity);
		
		seriesEntity = new ReportEchartsSeriesEntity();
		seriesEntity.setName("河南省");
		data = new ArrayList<BigDecimal>();
		data.add(new BigDecimal(11066));
		data.add(new BigDecimal(13986));
		data.add(new BigDecimal(16705));
		data.add(new BigDecimal(21386));
		data.add(new BigDecimal(29303));
		data.add(new BigDecimal(35228));
		data.add(new BigDecimal(41733));
		data.add(new BigDecimal(51397));
		data.add(new BigDecimal(62533));
		data.add(new BigDecimal(102942));
		seriesEntity.add(data);
		series.add(seriesEntity);
		
		seriesEntity = new ReportEchartsSeriesEntity();
		seriesEntity.setName("浙江省");
		data = new ArrayList<BigDecimal>();
		data.add(new BigDecimal(64740));
		data.add(new BigDecimal(76481));
		data.add(new BigDecimal(100620));
		data.add(new BigDecimal(112147));
		data.add(new BigDecimal(158839));
		data.add(new BigDecimal(227156));
		data.add(new BigDecimal(236647));
		data.add(new BigDecimal(238676));
		data.add(new BigDecimal(273476));
		data.add(new BigDecimal(343784));
		seriesEntity.add(data);
		series.add(seriesEntity);
		
		seriesEntity = new ReportEchartsSeriesEntity();
		seriesEntity.setName("福建省");
		data = new ArrayList<BigDecimal>();
		data.add(new BigDecimal(10028));
		data.add(new BigDecimal(12261));
		data.add(new BigDecimal(16495));
		data.add(new BigDecimal(21097));
		data.add(new BigDecimal(31200));
		data.add(new BigDecimal(40920));
		data.add(new BigDecimal(47525));
		data.add(new BigDecimal(53983));
		data.add(new BigDecimal(73178));
		data.add(new BigDecimal(106326));
		seriesEntity.add(data);
		series.add(seriesEntity);
		
		chartsEntity.setSeries(series);
		
		AbstractEchartsOption echarts = new BarEchartsOption(chartsEntity);
		GsonOption option = echarts.option();
		System.out.println(option.toString());
		
		echarts = new LineEchartsOption(chartsEntity);
		option = echarts.option();		
		System.out.println(option.toString());
		
		echarts = new PileBarEchartsOption(chartsEntity);
		option = echarts.option();		
		System.out.println(option.toString());
		
		echarts = new ScatterEchartsOption(chartsEntity);
		option = echarts.option();		
		System.out.println(option.toString());
		JSONObject jsonObject = JSON.parseObject(option.toString());
		System.out.println("JSONObject:"+JSON.toJSONString(jsonObject));
		
		echarts = new RadarEchartsOption(chartsEntity);
		option = echarts.option();		
		System.out.println(option.toString());
	}
}
