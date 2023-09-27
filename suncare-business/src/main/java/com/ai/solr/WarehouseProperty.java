/**
 * WarehouseProperty.java	  V1.0   2022年6月28日 下午3:11:27
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.solr;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import com.ai.modules.engine.util.SolrUtil;

@Component
@ConfigurationProperties(prefix = "engine.warehouse")
public class WarehouseProperty {

	/**
	 * 默认数仓配置信息
	 */
	@NestedConfigurationProperty
	private WarehouseDatasourceProperty datasource;
	
	/**
	 * 为兼容项目地出现多个类型数仓的配置信息
	 */
	private Map<String, WarehouseDatasourceProperty> multiple = new LinkedHashMap<>();

	public WarehouseDatasourceProperty getDatasource() {
		return datasource;
	}

	public void setDatasource(WarehouseDatasourceProperty datasource) {
		this.datasource = datasource;
	}

	public Map<String, WarehouseDatasourceProperty> getMultiple() {
		return multiple;
	}

	public void setMultiple(Map<String, WarehouseDatasourceProperty> multiple) {
		this.multiple = multiple;
	}

	public String getDriverClassName() {
		String ds = SolrUtil.getCurrentDsName();
		if(StringUtils.isNotBlank(ds) && multiple.containsKey(ds)) {
			WarehouseDatasourceProperty property = multiple.get(ds);
			return property.getDriverClassName();
		}
		return datasource.getDriverClassName();
	}
	public String getUrl() {
		String ds = SolrUtil.getCurrentDsName();
		if(StringUtils.isNotBlank(ds) && multiple.containsKey(ds)) {
			WarehouseDatasourceProperty property = multiple.get(ds);
			return property.getUrl();
		}
		return datasource.getUrl();
	}
	public String getUsername() {
		String ds = SolrUtil.getCurrentDsName();
		if(StringUtils.isNotBlank(ds) && multiple.containsKey(ds)) {
			WarehouseDatasourceProperty property = multiple.get(ds);
			return property.getUsername();
		}
		return datasource.getUsername();
	}
	public String getPassword() {
		String ds = SolrUtil.getCurrentDsName();
		if(StringUtils.isNotBlank(ds) && multiple.containsKey(ds)) {
			WarehouseDatasourceProperty property = multiple.get(ds);
			return property.getPassword();
		}
		return datasource.getPassword();
	}
	
	/**
	 * 
	 * 功能描述：计算引擎方式{true:gp, false:solr}
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	public boolean isEnabledProcessGp() {
		String ds = SolrUtil.getCurrentDsName();
		if(StringUtils.isNotBlank(ds) && multiple.containsKey(ds)) {
			WarehouseDatasourceProperty property = multiple.get(ds);
			return property.isEnabledProcessGp();
		}
		return datasource.isEnabledProcessGp();
	}
	
	/**
	 * 
	 * 功能描述：计算结果存储方式{true:gp, false:solr}
	 *
	 * @author  zhangly
	 *
	 * @return
	 */
	public boolean isEnabledStorageGp() {
		if(!isEnabledProcessGp()) {
			//计算引擎使用solr方式
			return false;
		}
		String ds = SolrUtil.getCurrentDsName();
		if(StringUtils.isNotBlank(ds) && multiple.containsKey(ds)) {
			WarehouseDatasourceProperty property = multiple.get(ds);
			return property.isEnabledStorageGp();
		}
		return datasource.isEnabledStorageGp();
	}
}
