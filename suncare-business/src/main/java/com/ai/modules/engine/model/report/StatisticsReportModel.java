/**
 * StatisticsReportModel.java	  V1.0   2019年4月10日 下午5:03:06
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.config.vo.MedicalDictItemVO;
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
 * 功能描述：前台展示报表对象
 *
 * @author  zhangly
 * Date: 2019年4月10日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public class StatisticsReportModel implements Serializable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**前台页面可渲染的echarts图表集合*/
	private Map<String, Object> echarts = new LinkedHashMap<String, Object>();
	/**前台展示的表格head对象*/
	private List<ReportTableTd> heads;
	/**前台展示的表格body对象*/
	private List<List<ReportTableTd>> bodys;
	/**echarts源数据*/
	private ReportEchartsEntity echartsData;
	/**初始页面渲染的echart图表*/
	private String currentEchart;
	/**字典解析*/
	private String dimDict;
	
	public StatisticsReportModel(ReportEchartsEntity echartsData, String currentEchart, String dimDict) {
		this.echartsData = echartsData;
		this.currentEchart = currentEchart;
		this.dimDict = dimDict;
	}
	
	public StatisticsReportModel render() {
		//字段解析
		this.parseDict();
		//echarts图表
		this.handleEcharts();
		//表格列表
		this.handleTable();
		return this;
	}
	
	/**
	 * 
	 * 功能描述：生成echarts图表数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年4月11日 上午9:30:47</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public void handleEcharts() {
		echarts.clear();
		// 柱状图
		AbstractEchartsOption chart = new BarEchartsOption(echartsData);
		GsonOption option = chart.option();
		echarts.put("bar", option);
		
		//折线图
		chart = new LineEchartsOption(echartsData);
		option = chart.option();
		echarts.put("line", option);
		
		//堆积柱状图
		chart = new PileBarEchartsOption(echartsData);
		option = chart.option();
		echarts.put("pilebar", option);
		
		//散点图
		chart = new ScatterEchartsOption(echartsData);
		option = chart.option();
		//带有function,特殊处理存储json字符串
		echarts.put("scatter", option.toString());
		
		//雷达图
		chart = new RadarEchartsOption(echartsData);
		option = chart.option();
		echarts.put("radar", option);
	}
	
	/**
	 * 
	 * 功能描述：根据echarts源数据生成table数据
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年4月10日 下午5:27:39</p>
	 *
	 * @return
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	public void handleTable() {
		this.setHeads(null);
		this.setBodys(null);
		if(echartsData!=null) {
			List<ReportTableTd> heads = new ArrayList<ReportTableTd>();
			// 表头
			ReportTableTd td = new ReportTableTd();
			td.setValue("&nbsp;");
			heads.add(td);
			for(int x=0; x<echartsData.getxAxis().length; x++) {
				td = new ReportTableTd();
				td.setValue(echartsData.getxAxis()[x]);
				heads.add(td);
			}
			this.setHeads(heads);
			// 表体
			List<List<ReportTableTd>> bodys = new ArrayList<List<ReportTableTd>>();
			for(int x=0; x<echartsData.getSeries().size(); x++) {
				List<ReportTableTd> tr = new ArrayList<ReportTableTd>();
				ReportEchartsSeriesEntity entity = echartsData.getSeries().get(x);
				td = new ReportTableTd();
				td.setValue(entity.getName());
				tr.add(td);
				for(BigDecimal value : entity.getData()) {
					td = new ReportTableTd();
					td.setValue(value);
					tr.add(td);
				}
				bodys.add(tr);
			}
			this.setBodys(bodys);
		}
	}
	
	/**
	 * 
	 * 功能描述：字典解析
	 *
	 * @author  zhangly
	 * <p>创建日期 ：2019年5月5日 上午10:30:41</p>
	 *
	 * @param reportModel
	 *
	 * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
	 */
	protected void parseDict() {
		String dimDictJson = this.getDimDict();		
		if(StringUtils.isNotBlank(dimDictJson) && dimDictJson.startsWith("{")) {
			ApplicationContext context = SpringContextUtils.getApplicationContext();			
			IMedicalDictService dictService = context.getBean(IMedicalDictService.class);
			JSONObject jsonObject = JSON.parseObject(dimDictJson);
			if(jsonObject.get("dim1")!=null) {
				String dimDict = jsonObject.get("dim1").toString();				
				List<MedicalDictItemVO> dicts = dictService.queryByType(dimDict);
				if(!dicts.isEmpty()) {
					Map<String, MedicalDictItemVO> dictMap = new HashMap<String, MedicalDictItemVO>();
					for(MedicalDictItemVO dict : dicts) {
						dictMap.put(dict.getCode(), dict);
					}
					
					ReportEchartsEntity echartsData = this.getEchartsData();
					String[] xAxis = echartsData.getxAxis();
					for(int i=0; i<xAxis.length; i++) {
						String name = xAxis[i];
						if(dictMap.containsKey(name)) {
							MedicalDictItemVO dict = dictMap.get(name);
							xAxis[i] = dict.getValue();						
						}
					}
				}
			}
			if(jsonObject.get("dim2")!=null) {
				String dimDict = jsonObject.get("dim2").toString();				
				List<MedicalDictItemVO> dicts = dictService.queryByType(dimDict);
				if(!dicts.isEmpty()) {
					Map<String, MedicalDictItemVO> dictMap = new HashMap<String, MedicalDictItemVO>();
					for(MedicalDictItemVO dict : dicts) {
						dictMap.put(dict.getCode(), dict);
					}
					
					ReportEchartsEntity echartsData = this.getEchartsData();
					String[] legend = echartsData.getLegend();
					for(int i=0; i<legend.length; i++) {
						String name = legend[i];
						if(dictMap.containsKey(name)) {
							MedicalDictItemVO dict = dictMap.get(name);
							legend[i] = dict.getValue();						
							echartsData.getSeries().get(i).setName(dict.getValue());
						}
					}
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return "StatisticsReportModel [echarts=" + echarts + ", heads=" + heads + ", bodys=" + bodys + ", echartsData="
				+ echartsData + ", currentEchart=" + currentEchart + ", dimDict=" + dimDict + "]";
	}

	public String getDimDict() {
		return dimDict;
	}

	public void setDimDict(String dimDict) {
		this.dimDict = dimDict;
	}

	public Map<String, Object> getEcharts() {
		return echarts;
	}
	public List<ReportTableTd> getHeads() {
		return heads;
	}

	public void setHeads(List<ReportTableTd> heads) {
		this.heads = heads;
	}

	public List<List<ReportTableTd>> getBodys() {
		return bodys;
	}

	public void setBodys(List<List<ReportTableTd>> bodys) {
		this.bodys = bodys;
	}

	public ReportEchartsEntity getEchartsData() {
		return echartsData;
	}

	public void setEchartsData(ReportEchartsEntity echartsData) {
		this.echartsData = echartsData;
	}
	public String getCurrentEchart() {
		return currentEchart;
	}

	public void setCurrentEchart(String currentEchart) {
		this.currentEchart = currentEchart;
	}
}
