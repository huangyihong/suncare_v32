/**
 * DynamicSolrDataSourceProperties.java	  V1.0   2020年8月19日 下午5:58:13
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.solr;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "solr.datasource.dynamic")
public class DynamicSolrDataSourceProperties {
	private boolean web = true;
	/**
     * 默认的库default
     */
    private String primary = "default";
	/**
     * solr数据源参数配置
     */
	private Map<String, SolrDataSourceProperty> datasource = new LinkedHashMap<>();
	/**
     * druid参数配置
     */
    @NestedConfigurationProperty
	private SolrDruidConfig druid = new SolrDruidConfig();
    
    public SolrDataSourceProperty getDefaultSolrDataSourceProperty() {
    	return datasource.get(primary);
    }
    
    public SolrDataSourceProperty getSolrDataSourceProperty(String ds) {
    	return datasource.get(ds);
    }
    
    /**
     * 
     * 功能描述：判断是否单数据源
     *
     * @author  zhangly
     * <p>创建日期 ：2020年8月21日 上午9:13:53</p>
     *
     * @return
     *
     * <p>修改历史 ：(修改人，修改时间，修改原因/内容)</p>
     */
    public boolean isSingleton() {
    	return datasource.size()==1;
    }
    
	public Map<String, SolrDataSourceProperty> getDatasource() {
		return datasource;
	}
	public void setDatasource(Map<String, SolrDataSourceProperty> datasource) {
		this.datasource = datasource;
	}
	public SolrDruidConfig getDruid() {
		return druid;
	}
	public void setDruid(SolrDruidConfig druid) {
		this.druid = druid;
	}
	public String getPrimary() {
		return primary;
	}
	public void setPrimary(String primary) {
		this.primary = primary;
	}
	public boolean isWeb() {
		return web;
	}
	public void setWeb(boolean web) {
		this.web = web;
	}
}
