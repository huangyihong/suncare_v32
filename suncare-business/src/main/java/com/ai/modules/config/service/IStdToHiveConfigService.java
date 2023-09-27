package com.ai.modules.config.service;

import java.util.List;
import java.util.Map;

import com.ai.modules.config.entity.StdToHiveConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 基础数据同步到HIVE的配置文件
 * @Author: jeecg-boot
 * @Date:   2021-01-05
 * @Version: V1.0
 */
public interface IStdToHiveConfigService extends IService<StdToHiveConfig> {
	/**
	 * 根据表名，获取需要同步的数据
	 * @param tableName
	 * @return
	 */
	public List<Map<String, Object>> queryStdData(String tableName);
	
	public List<Map<String, Object>> queryMedicalDictForCSV(String tableName);
}
