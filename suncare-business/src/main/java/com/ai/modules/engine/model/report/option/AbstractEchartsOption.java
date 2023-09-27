/**
 * AbstractEchartsOption.java	  V1.0   2019年4月9日 下午3:12:19
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.modules.engine.model.report.option;

import com.ai.modules.engine.model.report.ReportEchartsEntity;
import com.github.abel533.echarts.json.GsonOption;

/**
 * 
 * 功能描述：可渲染echars的option
 *
 * @author  zhangly
 * Date: 2019年4月9日
 * Copyright (c) 2019 AILK
 *
 * <p>修改历史：(修改人，修改时间，修改原因/内容)</p>
 */
public abstract class AbstractEchartsOption {
	protected ReportEchartsEntity chartsEntity;
	public AbstractEchartsOption(ReportEchartsEntity chartsEntity) {
		this.chartsEntity = chartsEntity;
	}
	
	public abstract GsonOption option();

	public ReportEchartsEntity getChartsEntity() {
		return chartsEntity;
	}

	public void setChartsEntity(ReportEchartsEntity chartsEntity) {
		this.chartsEntity = chartsEntity;
	}
}
